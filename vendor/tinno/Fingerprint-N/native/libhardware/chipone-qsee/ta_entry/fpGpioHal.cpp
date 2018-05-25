#include "fpGpioHal.h"
#include <errno.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include "fpTa_entryproxy.h"
#include <poll.h>
#include "fp_input.h"
#include "fp_common.h"
#include "fp_tee_types.h"

#define FPTAG " fpGpioHal.cpp "
#define CA_RELEASE_VERSION      "v1.2.4"

/* define commands */
#define NETLINK_CHIPONE                         26//16
#define MAX_PAYLOAD                             1024
#define FPSENSOR_IOC_MAGIC                      0xf0    //CHIP
/* define commands */
#define FPSENSOR_IOC_INIT                       _IOWR(FPSENSOR_IOC_MAGIC,0,unsigned int)
#define FPSENSOR_IOC_EXIT                       _IOWR(FPSENSOR_IOC_MAGIC,1,unsigned int)
#define FPSENSOR_IOC_RESET                      _IOWR(FPSENSOR_IOC_MAGIC,2,unsigned int)
#define FPSENSOR_IOC_ENABLE_IRQ                 _IOWR(FPSENSOR_IOC_MAGIC,3,unsigned int)
#define FPSENSOR_IOC_DISABLE_IRQ                _IOWR(FPSENSOR_IOC_MAGIC,4,unsigned int)
#define FPSENSOR_IOC_GET_INT_VAL                _IOWR(FPSENSOR_IOC_MAGIC,5,unsigned int)
#define FPSENSOR_IOC_DISABLE_SPI_CLK            _IOWR(FPSENSOR_IOC_MAGIC,6,unsigned int)
#define FPSENSOR_IOC_ENABLE_SPI_CLK             _IOWR(FPSENSOR_IOC_MAGIC,7,unsigned int)
#define FPSENSOR_IOC_ENABLE_POWER               _IOWR(FPSENSOR_IOC_MAGIC,8,unsigned int)
#define FPSENSOR_IOC_DISABLE_POWER              _IOWR(FPSENSOR_IOC_MAGIC,9,unsigned int)
#define FPSENSOR_IOC_INPUT_KEY_EVENT            _IOWR(FPSENSOR_IOC_MAGIC,10,struct fpsensor_key)
/* fp sensor has change to sleep mode while screen off */
#define FPSENSOR_IOC_ENTER_SLEEP_MODE           _IOWR(FPSENSOR_IOC_MAGIC,11,unsigned int)
#define FPSENSOR_IOC_REMOVE                     _IOWR(FPSENSOR_IOC_MAGIC,12,unsigned int)
#define FPSENSOR_IOC_CANCEL_POLL                _IOWR(FPSENSOR_IOC_MAGIC,13,unsigned int)
#define FPSENSOR_IOC_SET_DEV_INFO               _IOWR(FPSENSOR_IOC_MAGIC,14,unsigned int)
#define FPSENSOR_IOC_FTM_SET_FINGER_STATE       _IOWR(FPSENSOR_IOC_MAGIC,15,unsigned int)
#define FPSENSOR_IOC_FTM_GET_FINGER_STATE       _IOWR(FPSENSOR_IOC_MAGIC,16,unsigned int)
#define FPSENSOR_IOC_RELEASE_VERSION            _IOWR(FPSENSOR_IOC_MAGIC,17,char *)
#define FPSENSOR_IOC_MAXNR                      32  /* THIS MACRO IS NOT USED NOW... */
#define SUPPORT_REE_SPI                         0

const char *kDevFile = "/dev/chipone_fp";
int file_descriptor_ = 0;


//1 lzk to disable the log
/*#ifdef LOGD
#undef LOGD
#define LOGD(...)
#endif
*/

fpGpioHal::fpGpioHal(fpTaEntryProxy *fpProxy)
{
    LOGD(FPTAG"constructor invoked");
    unsigned int value = 0;
    TaProxy = fpProxy;
    pfpInput = new fpInput(this);
    pthread_mutex_init(&poll_mutex, NULL);


    if (0 == file_descriptor_)
    {
        file_descriptor_ = open(kDevFile,  /*O_RDONLY*/O_RDWR);
        LOGD(FPTAG"[rickon]----after open %s : %d\n", kDevFile, file_descriptor_);
    }

    if (file_descriptor_ < 0)
    {
        LOGE(FPTAG"open %s failed file_descriptor_:%d", kDevFile, file_descriptor_);
        //return -ENOENT;
    }
    ::ioctl(file_descriptor_, FPSENSOR_IOC_EXIT, &value);
    ::ioctl(file_descriptor_, FPSENSOR_IOC_ENABLE_POWER, &value);

    cancel_fds[0] = -1;
    cancel_fds[1] = -1;

    if (pipe(cancel_fds))
    {
        LOGE(FPTAG"pipe cancel_fds error!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

}

fpGpioHal::~fpGpioHal()
{
    LOGD(FPTAG"destructor invoked");
    /*    unsigned int value = 0;
        int result   = 0;

    #ifdef LONGCHEER
        ::ioctl(file_descriptor_, FPSENSOR_IOC_DISABLE_IRQ, &value);
    #else
        ioctl(file_descriptor_, FPSENSOR_IOC_DISABLE_IRQ, NULL);
    #endif
        if (sock > 0)
        {
            close(sock);
        }
        */
    if (pfpInput)
    {
        delete pfpInput;
        pfpInput = NULL;
    }

    close(file_descriptor_);
    file_descriptor_ = 0;
    pthread_mutex_destroy(&poll_mutex);

    close(cancel_fds[0]);
    close(cancel_fds[1]);

};

int fpGpioHal::Remove(void)
{
    LOGD(FPTAG"Remove invoked");
    unsigned int value = 0;
    int result   = 0;
#ifdef FP_TEE_QSEE4
    /* Release gpio and irq resource, but keep fpsensor_dev device,
     * so next time we still can open_hal() */
   result = ::ioctl(file_descriptor_, FPSENSOR_IOC_EXIT, &value);
#else
    result = ::ioctl(file_descriptor_, FPSENSOR_IOC_REMOVE, &value);
#endif
    LOGD(FPTAG"FPSENSOR_IOC_REMOVE result:%d", result);

    return result;
}


int fpGpioHal::ChipGpioReset(void)
{
    unsigned int value = 0;
    int result   = 0;
    result = ::ioctl(file_descriptor_, FPSENSOR_IOC_RESET, &value);
    LOGD(FPTAG"ChipGpioReset result:%d", result);

    return result;

}

int fpGpioHal::SetDevInfo(void)
{
    unsigned int value = 0;
    int result   = 0;
    result = ::ioctl(file_descriptor_, FPSENSOR_IOC_SET_DEV_INFO, &value);
    LOGD(FPTAG"SetDevInfo result:%d", result);

    return result;
}

int fpGpioHal::SetVerInfo(const char *ca_ver, const char *ta_ver)
{
    int result = 0;
    char fp_ver_info[128];

    snprintf(fp_ver_info, sizeof(fp_ver_info), "CA:%s-TA:%s-ICNT7152",ca_ver, ta_ver);
    result = ::ioctl(file_descriptor_, FPSENSOR_IOC_RELEASE_VERSION, fp_ver_info);
    LOGD(FPTAG"SetVerInfo %s result:%d", fp_ver_info, result);

    return result;
}

int fpGpioHal::SetFingerState(int finger_state)
{
    int result = 0;
    result = ::ioctl(file_descriptor_, FPSENSOR_IOC_FTM_SET_FINGER_STATE, &finger_state);
    LOGD(FPTAG"SetFingerState %d result:%d", finger_state, result);

    return result;
}

int fpGpioHal::CancelWaitFingerDown(void)
{
    // LOGD(FPTAG"CancelWaitFingerDown invoked");
    unsigned int value = 0;
    int result   = 0;
    uint8_t byte = 1;

    result = ::ioctl(file_descriptor_, FPSENSOR_IOC_CANCEL_POLL, &value);
    LOGD(FPTAG"CancelPoll result:%d", result);

    if (write(cancel_fds[1], &byte, sizeof(byte)) != sizeof(byte))
    {
        LOGE("%s write failed %i", __func__, errno);
        result = -FP_ERROR_IO;
    }

    return result;
}


int fpGpioHal::CancelClear(void)
{
    LOGD(FPTAG"CancelClear");

    int result = 0;
    uint8_t byte;
    if (read(cancel_fds[0], &byte, sizeof(byte)) < 0)
    {
        LOGE("%s read failed %i", __func__, errno);
        result = -FP_ERROR_IO;
    }

    return result;
}

int fpGpioHal::EnableSPICLK(void)
{
    unsigned int value = 0;
    int result   = 0;

    result = ::ioctl(file_descriptor_, FPSENSOR_IOC_ENABLE_SPI_CLK, &value);
    // LOGD(FPTAG"EnableSPICLK result:%d", result);

    return result;

}

int fpGpioHal::DisableSPICLK(void)
{
    unsigned int value = 0;
    int result   = 0;

    result = ::ioctl(file_descriptor_, FPSENSOR_IOC_DISABLE_SPI_CLK, &value);
    // LOGD(FPTAG"DisableSPICLK result:%d", result);

    return result;
}

int fpGpioHal::IOCtrlInit(void)
{

    LOGD(FPTAG"IOCtrlInit invoked");
    int result   = 0;
    unsigned int value   = 0;

    result = ::ioctl(file_descriptor_, FPSENSOR_IOC_INIT, &value);

    if (result < 0)
    {
        LOGD(FPTAG"[rickon]---- /dev/FPSENSOR_IOC_INIT error:%d\n", result);
    }
    else
    {
        LOGD(FPTAG"[rickon]---- /dev/FPSENSOR_IOC_INIT ok \n");
    }
    result = ::ioctl(file_descriptor_, FPSENSOR_IOC_ENABLE_IRQ, &value);

    if (result < 0)
    {
        LOGD(FPTAG"[rickon]---- /dev/FPSENSOR_IOC_ENABLE_IRQ error:%d\n", result);
    }
    else
    {
        LOGD(FPTAG"[rickon]---- /dev/FPSENSOR_IOC_ENABLE_IRQ ok \n");
    }
    return result;
}
void fpGpioHal::LockPollMutex(void)
{
    pthread_mutex_lock(&poll_mutex);
}

void fpGpioHal::UnLockPollMutex(void)
{
    pthread_mutex_unlock(&poll_mutex);
}

int fpGpioHal::IrqPoll(void)
{
    LOGD(FPTAG"[liuxn]----IrqPoll");

    struct pollfd pfd[2] = {0};
    int status = 0;

    pfd[0].fd = file_descriptor_;
    pfd[0].events = POLLIN | POLLHUP | POLLERR;
    pfd[1].fd = cancel_fds[0];
    pfd[1].events = POLLIN;

    DisableSPICLK();
    UnLockPollMutex();

    // int status = ::poll(&pfd, 2, -1);//2000
    status = ::poll(pfd, 2, -1);//-1
    EnableSPICLK();
    LOGI(FPTAG" IrqPoll status:%d", status);
    // LOGD(FPTAG"pfd[0].revents:%d, pfd[0].revents:%d", pfd[0].revents, pfd[1].revents);

    if (status < 0)
    {
        LOGE(FPTAG" IrqPoll error!!");
        status = -errno;
    }
    else if ((pfd[0].revents & POLLERR) || (pfd[1].revents & POLLIN))
    {
        LOGI(FPTAG"capture image be stoped!!");
        status = -EINTR;
    }
    else if (0 == status)
    {
        LOGD(FPTAG"poll time out!!");
        status = -EAGAIN;
    }
    else
    {
        LOGD(FPTAG"Got the gpio irq  OK!!!!!!!");
        status = 0;
    }


    return status;
}

int fpGpioHal::WaitForGpioIrq(void)
{
    // LOGD(FPTAG"WaitForGpioIrq  invoked");
    LockPollMutex();
    int iRet = 0;
    unsigned int value = 0;

    if (TaProxy->get_stop_status())
    {
        LOGI(FPTAG"WaitForGpioIrq already has been stoped!!");
        iRet = -FP_ERROR_USER_CANCEL;
        UnLockPollMutex();
        goto out;
    }
    ::ioctl(file_descriptor_, FPSENSOR_IOC_ENABLE_IRQ, &value);

    iRet = ::ioctl(file_descriptor_, FPSENSOR_IOC_GET_INT_VAL, &value);
    if (0 == iRet)
    {
        if (value > 0)
        {
            LOGD(FPTAG"Gpio has already been high:%d!!!!!!!", value);
            UnLockPollMutex();
            goto out;
        }
    }

    iRet = IrqPoll();

    if (-EAGAIN == iRet)
    {
        LOGD(FPTAG"poll time out!!");
        iRet = 0;
    }

out:
    LOGD(FPTAG" WaitForGpioIrq iRet : %d", iRet);

    return iRet;
}


int fpGpioHal::NavigationKeyReport(fpsensor_key_event_t event)
{
    LOGD(FPTAG"NavigationKeyReport:%d", event);

    struct fpsensor_key report_key = {FPSENSOR_KEY_NONE, 0};
    report_key.key = event;

    if ((FPSENSOR_KEY_UP != event)
        && (FPSENSOR_KEY_DOWN != event)
        && (FPSENSOR_KEY_RIGHT != event)
        && (FPSENSOR_KEY_LEFT != event)
        && (FPSENSOR_KEY_TAP != event))
    {
        LOGE(FPTAG"Nav event error!:%d", event);
    }
    ::ioctl(file_descriptor_, FPSENSOR_IOC_INPUT_KEY_EVENT, &report_key);
    return 0;
}
