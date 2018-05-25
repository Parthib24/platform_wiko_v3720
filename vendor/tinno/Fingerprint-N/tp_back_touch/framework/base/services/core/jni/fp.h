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

#define FP_DEV_ATTR		"/sys/devices/platform/fp_drv/fp_drv_info"
/***************************************************************************/

enum {
    UNKNOW = 0,
    VENDOR_SILEAD,
    VENDOR_GOODIX,
    VENDOR_ELAN,   
};

/***************************************************************************/
extern int tp_backtouch_dev_init(void);
extern int tp_backtouch_dev_uninit(void);
extern int goodix_dev_resume(void);
extern int bt_update_finger_state(int *s);


extern int bt_dev_init(void);
extern int bt_dev_uninit(void);
extern int fp_dev_resume(void);
extern int tp_update_finger_state(int *s);

extern int getMainThreadRunFlag(void);

/***************************************************************************/
#ifdef __cplusplus
}//extern "C"
#endif

#endif

