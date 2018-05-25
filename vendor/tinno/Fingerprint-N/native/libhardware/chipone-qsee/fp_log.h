#ifndef FP_LOG_H
#define FP_LOG_H

#include <stddef.h>

#ifndef LOG_TAG
#define LOG_TAG "fpCoreJni"
#endif

extern void log_2_file(int log_level, const char * log_tag, const char *fmt, ...);
extern int32_t enable_logd_in_release_mode;

#include <android/log.h>
#ifdef DEBUG_ENABLE
#define  LOGD(...)  do{ __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__); log_2_file(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__); } while(0)
#else
#define  LOGD(...)  do{if(enable_logd_in_release_mode){__android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__); log_2_file(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__);}; }while(0)
#endif
#define  LOGI(...)  do{ __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__);  log_2_file(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__); } while(0)
#define  LOGE(...)  do{ __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__); log_2_file(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__); } while(0)



#endif

