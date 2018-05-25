#include <string.h> 
#include <fcntl.h>  
#include <stdlib.h> 
#include <stdio.h> 
#include <errno.h> 
#include <stdint.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <linux/input.h>
#include <poll.h>
#include <sched.h>

#include <semaphore.h>
#include <signal.h>
#include "cutils/properties.h"
#include "fp.h"

#undef LOG_TAG
#define LOG_TAG "[Tinnofingerprint-fp.c] "
#define TAG LOG_TAG

/*****************************************************************/
static int dev_fd = -1;
static int fp_vendor = -1;
static int fp_event_value = 0;
static sem_t msg_sem;
static int fp_qwk_enable_flag = 0;
/*****************************************************************/
#define __POLL_MSG__  (0)


#define FP_BL_LOCK_RESET (0) 
#define FP_BL_LOCK (1) 
#define FP_BL_UNLOCK_AND_TRIGGER (2)

#define FP_RESET_EVENT (10)
#define FP_GET_EVENT (11)

#define BL_DEV_ATTR "/sys/class/leds/lcd-backlight/brightness"
/*****************************************************************/
static int setLcdBrightness(int brightness);
static void *key_event_monitor_thread(void *args);
static const char* dev_name_arr[] = {
    "qpnp_pon",
    NULL,
};

#if __POLL_MSG__
static void *fp_thread_do_poll(void * arg); 
#endif
/*****************************************************************/
int fp_dev_init(void) 
{
    int ret = -1;
    int err;
    pthread_t ntid;
    char value[PROPERTY_VALUE_MAX];

    memset(value, 0, sizeof(value));
    __system_property_get("persist.tinno.fp.qwk", value);
    if (strcmp(value, "1") == 0) {
       fp_vendor = FP_NATIVE; 
       fp_qwk_enable_flag = 1;
    } else if ( strcmp(value, "2") == 0) {
        fp_vendor = FP_NATIVE;
        fp_qwk_enable_flag = 2;
    } else {
        fp_qwk_enable_flag = 0;
    }

    LOGD(TAG"[%s]:%d,[persist.tinno.fp.qwk]:[%s]\n", __func__,__LINE__,value);

    // power key monitor.
    if (fp_vendor == FP_NATIVE) {
        err = pthread_create(&ntid, NULL, key_event_monitor_thread, NULL);
        if (err) {
            LOGD(TAG "[%s:%d]:pthread_create error!!!\n",__func__,__LINE__);
        }
    } else {
        getFpInfo();
    }
   
    switch(fp_vendor) 
    {
        case VENDOR_GOODIX:
            ret = goodix_dev_init();
            break;
        case VENDOR_ELAN:
            ret = elan_dev_init();
            break;
        case VENDOR_SILEAD:
            ret = silead_dev_init();
            break;
        case VENDOR_CHIPONE:
            ret = chipone_dev_init();
            break;
        case FP_NATIVE:
            ret = fp_dev_open();
            break;
        default:
            LOGE("fp verndor err:%d,[%s:%d]/n", fp_vendor,__func__,__LINE__); 
            break;
    }

    return ret;
}

int fp_dev_uninit(void)
{
    int ret = -1;

    LOGD(TAG"[fp_quick_wake_up][%s]\n", __func__);

    switch(fp_vendor) 
    {
        case VENDOR_GOODIX:
            ret = goodix_dev_uninit();
            break;
        case VENDOR_ELAN:
            ret = elan_dev_uninit();
            break;
        case VENDOR_SILEAD:
            ret = silead_dev_uninit();
            break;
        case VENDOR_CHIPONE:
            ret = chipone_dev_uninit();
        default:
            LOGD("Err:[%s] what the fuck ? /n", __func__); 
            break;
    }

    return ret;
}


int fp_update_finger_state(int *s)
{
    int ret = -1;

    switch(fp_vendor) 
    {
        case VENDOR_GOODIX:
            ret = goodix_update_finger_state(s);
            break;
        case VENDOR_ELAN:
            ret = elan_update_finger_state(s);
            break;
        case VENDOR_SILEAD:
            ret = silead_update_finger_state(s);
            break;
        case VENDOR_CHIPONE:
            ret = chipone_update_finger_state(s);
            break;

        case FP_NATIVE:
            ret = fp_native_report_event(s);
            break;
        default:
            LOGD(" Err:[%s] what the fuck ? /n", __func__); 
            break;
    }

    return ret;
}

int getFpInfo(void)
{
    char buf[50] = {0};
    int fd = -1;
    int v = UNKNOW;
    int ret = 0;

    LOGD( "%s \n", __func__);

    LOGD( "[%s]:fp_vendor:%d\n", __func__, fp_vendor);
    if(fp_vendor > UNKNOW) {
        return fp_vendor;
    }

    if ((fd = open(FP_DEV_ATTR, O_RDONLY)) > 0) {
        ret = read(fd, buf, sizeof(buf));

        if(strcmp(buf, FP_DRV_GOODIX) == 0) {
            LOGD( "match: /drv/%s Ok!\n",FP_DRV_GOODIX);
            v = VENDOR_GOODIX;
        } else if(strcmp(buf, FP_DRV_SILEAD) == 0) {
            LOGD( "match: /drv/%s Ok!\n",FP_DRV_SILEAD);
            v = VENDOR_SILEAD;
        } else if (strcmp(buf, FP_DRV_ELAN) == 0) {
            LOGD( "match: /drv/%s Ok!\n",FP_DRV_ELAN);
            v = VENDOR_ELAN;
        } else if (strcmp(buf, FP_DRV_CHIPONE) == 0) {
            LOGD( "match: /drv/%s Ok!\n",FP_DRV_CHIPONE);
            v = VENDOR_CHIPONE;
        } else {
            LOGD( "%s: Not match: any fp dev !\n", buf);
        }
        close(fd);
    }

    fp_vendor = v;
    return v;
}

/****************************************************************/
#define DO_IOCTL() \
    do { \
        int ret = ioctl(dev_fd, FP_IOC_GET_EVENT, &fp_event_value); \
        if (ret) { \
            LOGE("guomingyi %s:%d err!\n",__func__, __LINE__); \
        } else { \
            LOGD("guomingyi rec data:%d ,%s:%d\n",fp_event_value, __func__, __LINE__); \
            sem_post(&msg_sem); \
        } \
    } while(0)

#if __POLL_MSG__
#else
static void fp_input_handler(int signum)  
{	
 DO_IOCTL(); 
}

static void fp_fasync_notify_register()
{
    int oflags;
    
    signal(SIGIO, fp_input_handler);
    fcntl(dev_fd, F_SETOWN, getpid());
    oflags = fcntl(dev_fd, F_GETFL);
    fcntl(dev_fd, F_SETFL, oflags | FASYNC);
}
#endif

int fp_native_report_event(int *s)
{
    sem_wait(&msg_sem);
    *s = fp_event_value;
    return 0;
}

int fp_dev_open(void) 
{
    int err = 0;
    int cmd = fp_qwk_enable_flag;
#if __POLL_MSG__
    pthread_t ntid_poll;
#endif

    if (dev_fd > 0) {
        goto out;
    }

    dev_fd = open(FP_DEV, O_RDONLY);
    if (dev_fd < 0) {
        LOGE(" open %s failed! fd:%d\n", FP_DEV, dev_fd);	
        return -1;
    }

    // enable.
    LOGD("%s:%d - start to enable qwk support:%d\n",__func__,__LINE__,cmd);
    err = ioctl(dev_fd, FP_IOC_QWK_ENABLE, &cmd);
    if (err) {
        LOGE("FP_IOC_QWK_ENABLE err !\n");
    }

    LOGD("open %s success !\n", FP_DEV);

#if __POLL_MSG__
    err = pthread_create(&ntid_poll, NULL, fp_thread_do_poll, NULL);
    if (err) {
        LOGE(TAG "[%s:%d]:pthread_create error!!!\n",__func__,__LINE__);
        return -1;
    }
#else
    fp_fasync_notify_register();
#endif

out:
    return dev_fd;
}

int fp_dev_ioctl(int cmd, int arg0) 
{
    int ret = -1;
    int data = 0x0;

    if (fp_qwk_enable_flag == 0) {
        LOGE("fp qwk disabled!: %s:%d\n", __func__, __LINE__);
        return 0;
    }

    if (fp_dev_open() < 0) {
        LOGD("fp_quick_wake_up:error! %s:%d\n", __func__, __LINE__);	
        goto out;
    }

    switch(cmd) {
        case FP_BL_LOCK:
        case FP_BL_LOCK_RESET:
            data |= cmd << 16;
            data |= arg0;
            LOGD("fp_quick_wake_up %s:%d| cmd:0x%x,brightness:0x%x,data:0x%08x\n", __func__, __LINE__, cmd, arg0, data);
            ret = ioctl(dev_fd, FP_IOC_BL_CONTROL, &data); 
            if (ret) {
                LOGE("ioctl error: %d,%s:%d\n", ret,__func__, __LINE__);
            }
            break;
        case FP_BL_UNLOCK_AND_TRIGGER:
            data |= cmd << 16;
            data |= arg0;
            LOGD("fp_quick_wake_up %s:%d| cmd:0x%x,brightness:0x%x,data:0x%08x\n", __func__, __LINE__, cmd, arg0, data);
            ret = ioctl(dev_fd, FP_IOC_BL_CONTROL, &data); 
            if (ret) {
                LOGE("ioctl error: %d,%s:%d\n", ret,__func__, __LINE__);
            }
            setLcdBrightness(arg0);
            break;
        case FP_RESET_EVENT:
            data = arg0;
            LOGD("fp_quick_wake_up %s:%d| cmd:FP_RESET_EVENT,data:0x%08x\n", __func__, __LINE__, data);
            ret = ioctl(dev_fd, FP_IOC_RESET_EVENT, &data);
            if (ret) {
                LOGE("ioctl error: %d,%s:%d\n", ret,__func__, __LINE__);
            }
            break;
        default:
            LOGE("fp_quick_wake_up:cmd error!%s:%d\n", __func__, __LINE__);	
            break;
    }

out:
    return ret;
}

static int input_key_event_wait(void)
{
    char dev_path[64];
    struct input_event t;
    char filename[64];
    int err;
    char ev_name[64];
    int fd;
    int i, j;
    char **p = (char **)&dev_name_arr;

    for (j = 0; p[j] != NULL; j++) {
        for (i = 0; i < 24; i++) {
            sprintf(filename, "/sys/class/input/input%d/name", i);
            fd = open(filename, O_RDONLY);
            if (fd > 0) {
                err = read(fd, &ev_name, sizeof(ev_name));
                if (err < 1)
                    ev_name[0] = '\0';
                else
                    ev_name[err-1] = '\0';

                LOGD("fp_quick_wake_up i:%d, ev_name:%s,p[%d]:%s\n",i, ev_name,j, p[j]);
                if (err <= 0) {
                    continue;
                }
                if (0 == strcmp(ev_name, p[j])) {
                    LOGD("fp_quick_wake_up find: %s ---> %s\n", p[j], filename);
                    close(fd);
                    break;
                }
                close(fd);
            } else {
                LOGD("fp_quick_wake_up %s:%d open %s failed!\n",__func__, __LINE__, filename);
            }
        }
    }

    if (i >= 24) {
        LOGE("pwr input dev not fond!\n");
        return -1;
    }

    memset(dev_path, 0, sizeof(dev_path));
    sprintf(dev_path, "/dev/input/event%d", i);
    fd = open(dev_path, O_RDONLY);
    if(fd < 0) {
        LOGE("fp_quick_wake_up open %s device error!\n", dev_path);
        return -1;
    }   

    do {
        if(read(fd, &t, sizeof(t)) == sizeof(t)) {
            if(t.type == EV_KEY && t.value == 1 && t.code == KEY_POWER) {
                int data = (FP_BL_LOCK_RESET << 16);
                /** LOGD("%s:%d (t,c,v):(%d,%d,%d,%08x)\n", __func__, __LINE__, t.type,t.code,t.value,data); */
                if (dev_fd < 0) {
                   continue;
                }

                if (ioctl(dev_fd, FP_IOC_BL_CONTROL, &data) < 0) {
                    LOGD("fp_quick_wake_up %s:%d ioctl failed!\n",__func__, __LINE__);
                }

                // report to fingerprintEventReport.
                if (fp_qwk_enable_flag >= 1) {
                    fp_event_value = t.code;
                    sem_post(&msg_sem);    
                }
            } 
        }
    } 
    while(1);

    close(fd);
    return 0;
}

static void *key_event_monitor_thread(void *args) {
    LOGD("fp_quick_wake_up %s:%d\n",__func__, __LINE__);
    input_key_event_wait();
    pthread_exit(NULL);
    return NULL;
}


#if __POLL_MSG__
static void *fp_thread_do_poll(void * arg) 
{
    int ret = 0;
    struct pollfd pfd;

    LOGD(TAG "[%s:%d] .entry\n",__func__,__LINE__);

    if (dev_fd <= 0) {
        goto err;
    }

    pfd.fd = dev_fd ;
    pfd.events = POLLIN;

    do {
        ret = poll(&pfd, 1, -1);
        if (ret) {
            DO_IOCTL(); 
        }

        LOGD(TAG "[%s:%d]:ret:%d\n",__func__,__LINE__,ret);
    } while(1);

err:
    LOGE(TAG "[%s:%d] err exit!\n",__func__,__LINE__);
    pthread_exit(NULL);
    return NULL;
}
#endif

static int setLcdBrightness(int brightness)
{
    char buf[64];
    int fd = -1;
    int ret = 0;
    int len = 0;

    if ((fd = open(BL_DEV_ATTR, O_RDWR)) > 0) {
        memset(buf, 0, sizeof(buf));
        len = sprintf(buf, "%d", brightness);
        ret = write(fd, buf, len);
        close(fd);
        LOGD(TAG "[%s:%d] write: buf:%s, len:%d, ret:%d\n",__func__,__LINE__, buf, len, ret);
    }
    else {
        LOGE(TAG "[%s:%d] open: %s failed!\n",__func__,__LINE__, BL_DEV_ATTR);
    }

    return ret;
}



int systemCpuNum(void)
{
    // _SC_NPROCESSORS_CONF的值为CPU个数，基于0开始编号
    return sysconf(_SC_NPROCESSORS_CONF);
}

int cpu_set(int cpu) {
/**    cpu_set_t mask; */
    /**  */
    /** CPU_ZERO(&mask); */
    /** CPU_SET(cpu, &mask);  */
    /** if (pthread_setaffinity_np(pthread_self(), sizeof(mask), &mask) < 0) { */
        /** perror("pthread_setaffinity_np"); */
        /** return -1; */
    /** } */

    return 0;
}

int getLcdBrightness(void) 
{
    char buf[64];
    int fd = -1;
    int ret = 0;
    int val = 0;

    LOGD(TAG "guomingyi.[%s:%d]systemCpuNum:%d\n",__func__,__LINE__,systemCpuNum());
    if ((fd = open(BL_DEV_ATTR, O_RDONLY)) > 0) {
        memset(buf, 0, sizeof(buf));
        ret = read(fd, buf, sizeof(buf));
        if (ret > 0) {
            val = atoi(buf);
        }
        close(fd);
        LOGD(TAG "guomingyi.[%s:%d] read: buf:%s, val:%d, ret:%d\n",__func__,__LINE__, buf, val, ret);
    }
    else {
        LOGE(TAG "guomingyi.[%s:%d] open: %s failed!\n",__func__,__LINE__, BL_DEV_ATTR);
    }

    return val;
}
