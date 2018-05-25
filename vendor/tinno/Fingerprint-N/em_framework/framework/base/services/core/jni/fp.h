#ifndef __FP_H__
#define __FP_H__

//#include <utils/Log.h>
#include <android/log.h>
#include <sys/system_properties.h>

#include <unistd.h>
#include <sys/types.h>

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

#include <semaphore.h>
#include <signal.h>

#include <pthread.h>  

#ifdef __SOURCE_CODE_BUILD__
#include <linux/string.h>
#endif


#ifdef __cplusplus
extern "C" {
#endif
/***************************************************************************/
#undef LOG_TAG
#define LOG_TAG "[Tinnofingerprint-jni] "

#if 1
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_FATAL  , LOG_TAG,__VA_ARGS__)
#else
#define LOGD(...)
#define LOGE(...)
#endif

#define FP_DRV_SILEAD  	"silead_fp"
#define FP_DRV_GOODIX 	"goodix_fp"
#define FP_DRV_ELAN  	"elan_fp"
#define FP_DRV_CHIPONE  "chipone_fp"

#define FP_DEV_ATTR		"/sys/devices/platform/fp_drv/fp_drv_info"


// fp backlight control.
#define  FP_DEV    "/dev/fp_drv"
#define  FP_IOC_MAGIC    't'
#define  FP_IOC_BL_CONTROL    _IO(FP_IOC_MAGIC, 10)
#define  FP_IOC_GET_EVENT    _IO(FP_IOC_MAGIC, 11)
#define  FP_IOC_RESET_EVENT    _IO(FP_IOC_MAGIC, 12)

#define  FP_IOC_QWK_ENABLE    _IO(FP_IOC_MAGIC, 13)
/***************************************************************************/

enum {
    UNKNOW = 0,
    VENDOR_SILEAD = 1,
    VENDOR_GOODIX = 2,
    VENDOR_ELAN = 3,
    VENDOR_CHIPONE = 4,
    FP_NATIVE = 5,
};

/***************************************************************************/
extern int elan_dev_init(void);
extern int elan_dev_uninit(void);
extern int elan_dev_resume(void);
extern int elan_update_finger_state(int *s);


extern int silead_dev_init(void);
extern int silead_dev_uninit(void);
extern int silead_dev_resume(void);
extern int silead_update_finger_state(int *s);

extern int goodix_dev_init(void);
extern int goodix_dev_uninit(void);
extern int goodix_dev_resume(void);
extern int goodix_update_finger_state(int *s);

extern int chipone_dev_init(void);
extern int chipone_dev_uninit(void);
extern int chipone_dev_resume(void);
extern int chipone_update_finger_state(int *s);

extern int fp_dev_init(void);
extern int fp_dev_uninit(void);
extern int fp_dev_resume(void);
extern int fp_update_finger_state(int *s);
extern int getFpInfo(void);

extern int getMainThreadRunFlag(void);
extern int set_fp_bl(int val);


extern int fp_dev_open(void);
extern int fp_native_report_event(int *s);
extern int fp_dev_ioctl(int cmd, int arg0);

extern int getLcdBrightness(void);
/***************************************************************************/
#ifdef __cplusplus
}//extern "C"
#endif

#endif

