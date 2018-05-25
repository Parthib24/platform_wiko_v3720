//
// Created by android on 17-5-25.
//

#include <errno.h>
#include <unistd.h>
#include <dlfcn.h>
#include <stdio.h>
#include <time.h>
#include <hardware/hardware.h>
#include "fingerprint.h"
#include "FingerprintDaemonProxy.h"


#ifdef FINGERPRINT_SUPPORT_SILEAD

#define DRV_SILEAD                  "/dev/silead_fp_dev"
#define SPI_IOC_MAGIC               'k'
#define IOCTL_HW_GPIO_REQUEST       _IOR(SPI_IOC_MAGIC, 90, __u32)
#define IOCTL_HW_GPIO_FREE          _IOR(SPI_IOC_MAGIC, 91, __u32)

#define IOCTL_HW_POWEROFF                 _IOW(SPI_IOC_MAGIC, 75, __u32)
#define IOCTL_HW_POWERON                  _IOW(SPI_IOC_MAGIC, 76, __u32)


int check_silead_sensor(void) 
{

    int fd = -1;
    int args = 0;
    int ret = -1;
    unsigned chipId = 0;

    if ((fd = open(DRV_SILEAD, O_RDWR)) < 0) {
        ALOGD("open:%s err!\n", DRV_SILEAD);
        return -1;
    }

    // hw power on.
  /*  if((ret = ioctl(fd, IOCTL_HW_POWERON, &args)) < 0) {
        goto END;
    }*/

    // gpio request.
    if((ret = ioctl(fd, IOCTL_HW_GPIO_REQUEST, &args)) < 0) {
        goto END;
    }

    // read chip id.
    ret = silead_read_chipid(&chipId);
    ALOGD("silead:%s :chipId:%d,%d\n", __func__, chipId, ret);

    // gpio free if need.
    if (SILEAD_CHIP_ID != chipId) {
        if ((ret = ioctl(fd, IOCTL_HW_GPIO_FREE, &args)) < 0) {
            goto END;
        }
        // hw power off.
       /* if((ret = ioctl(fd, IOCTL_HW_POWEROFF, &args)) < 0) {
            goto END;
        }*/
    }

END:
    if (fd > 0) {
        close(fd);
    }

    return ((SILEAD_CHIP_ID == chipId) ? 0 : -1);
}

#endif //FINGERPRINT_SUPPORT_SILEAD




#ifdef FINGERPRINT_SUPPORT_CHIPONE
bool is_64bit_system(void)
{
    long int_bits = (((long)((long *)0 + 1)) << 3);
    ALOGI(" is_64bit_system = %d", (int_bits == 64));
    return (int_bits == 64);
}

int checkFpSensor(void){
    ALOGD("checkFpSesor()\n");
    int ret = -1;

    char *pFinaFpHalPath = (char *)"/system/lib/hw/fingerprint.chipone.default.so";
    if(is_64bit_system())
        pFinaFpHalPath = (char *)"/system/lib64/hw/fingerprint.chipone.default.so";

    void* lib_handle_ = dlopen(pFinaFpHalPath, RTLD_NOW);
    if(lib_handle_ == NULL){
          ALOGE(" dlopen failed can't find hal so:%s\n",pFinaFpHalPath);
          ret = -ENOENT;
          return ret;
    }

    ALOGD(" dlopen for fpsensor hal success \n");

    void (*initFunc)(void) =  (void (*)(void))dlsym(lib_handle_, "fp_init");
    int64_t (*fpOpenFunc)() =  (int64_t (*)())dlsym(lib_handle_, "fp_openHal");
    int (*fpCloseFunc)() =  (int (*)())dlsym(lib_handle_, "fp_closeHal");
    int32_t (*fpSetExtensionStatufFunc)(int32_t) = (int32_t (*)(int32_t))dlsym(lib_handle_, "fp_set_extension_status");
    int32_t (*fpSensorTest)(int32_t,int32_t) = (int32_t (*)(int32_t,int32_t))dlsym(lib_handle_, "fp_service_control");

    if(fpSetExtensionStatufFunc == NULL || initFunc == NULL || fpOpenFunc == NULL || fpCloseFunc == NULL || fpSensorTest == NULL){
            ALOGE(" dlopen can't get funcptr\n");
            ALOGD(" fpSetExtensionStatufFunc=%p\n",fpSetExtensionStatufFunc);
            ALOGD(" initFunc=%p\n",initFunc);
            ALOGD(" fpOpenFunc=%p\n",fpOpenFunc);
            ALOGD(" fpCloseFunc=%p\n",fpCloseFunc);
            ALOGD(" fpSensorTest=%p\n",fpSensorTest);

            ret = -EINVAL;
            goto out ;
    }

    fpSetExtensionStatufFunc(0);
    initFunc();
    if(fpOpenFunc()){
        ALOGD( "Find chipone fpSensor !!!!!!!!!\n");
        ret = 0;
    }else{
        ALOGE( " Faild to find  chipone fpSensor !!!!!!!!!!!!!!!\n");
        ret = -ENOENT;
    }
    fpCloseFunc();

out:
    if (lib_handle_){
        dlclose(lib_handle_);
    }
    return ret;
}
#endif //FINGERPRINT_SUPPORT_CHIPONE




