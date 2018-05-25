/*
 * =====================================================================================
 *
 *       Filename:  sl_log.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  
 *        Revision:  none
 *
 *         Author:  WY, 
 *        Company:  
 *
 * =====================================================================================
 */
#ifndef __SL_LOG_H__
#define __SL_LOG_H__



#ifdef __cplusplus
extern "C"
{
#endif

//TODO xml control log priority
typedef enum sl_LogPriority {
    SL_LOG_SILENT = 0,     /* only for SetMinPriority(); must be last */
    SL_LOG_UNKNOWN,
    SL_LOG_DEFAULT,    /* only for SetMinPriority() */
    SL_LOG_VERBOSE,
    SL_LOG_DEBUG,
    SL_LOG_INFO,
    SL_LOG_WARN,
    SL_LOG_ERROR,
    SL_LOG_FATAL,
	SL_LOG_MAX
} sl_LogPriority;

void sl_log_xmlready();

#if !defined(__ZOS__) || defined(__ZOS__linux__)

#include <unistd.h>

int sl_log_printf(int prio, const char *tag, const char *fmt, ...);
#ifndef SL_LOGU
#define SL_LOGU(f,...)	\
	do {	\
		sl_log_printf(SL_LOG_UNKNOWN, "SLCODE","%-15s:%04d:pid=%d:ctid=%d => " f, __FUNCTION__, __LINE__,getpid(),gettid(),##__VA_ARGS__);\
	} while(0)
#endif

#ifndef SL_LOGDE
#define SL_LOGDE(f,...)		\
	do {	\
		sl_log_printf(SL_LOG_DEFAULT, "SLCODE","%-15s:%04d:pid=%d:ctid=%d => " f, __FUNCTION__, __LINE__,getpid(),gettid(),##__VA_ARGS__);\
	} while(0)
#endif

#ifndef SL_LOGV
#define SL_LOGV(f,...)	\
	do {	\
		sl_log_printf(SL_LOG_VERBOSE, "SLCODE","%-15s:%04d:pid=%d:ctid=%d => " f, __FUNCTION__, __LINE__,getpid(),gettid(),##__VA_ARGS__);\
	} while(0)
#endif

#ifndef SL_LOGD
#define SL_LOGD(f,...)	\
	do {	\
		sl_log_printf(SL_LOG_DEBUG, "SLCODE","%-15s:%04d:pid=%d:ctid=%d => " f, __FUNCTION__, __LINE__,getpid(),gettid(),##__VA_ARGS__);\
	} while(0)
#endif

#ifndef SL_LOGI
#define SL_LOGI(f,...)	\
	do {	\
		sl_log_printf(SL_LOG_INFO, "SLCODE","%-15s:%04d:pid=%d:ctid=%d => " f, __FUNCTION__, __LINE__,getpid(),gettid(),##__VA_ARGS__);\
	} while(0)
#endif

#ifndef SL_LOGW
#define SL_LOGW(f,...)	\
	do {	\
		sl_log_printf(SL_LOG_WARN, "SLCODE","%-15s:%04d:pid=%d:ctid=%d => " f, __FUNCTION__, __LINE__,getpid(),gettid(),##__VA_ARGS__);\
	} while(0)
#endif

#ifndef SL_LOGE
#define SL_LOGE(f,...)	\
	do {	\
		sl_log_printf(SL_LOG_ERROR, "SLCODE","%-15s:%04d:pid=%d:ctid=%d => " f, __FUNCTION__, __LINE__,getpid(),gettid(),##__VA_ARGS__);\
	} while(0)
#endif

#ifndef SL_LOGF
#define SL_LOGF(f,...)	\
	do {	\
		sl_log_printf(SL_LOG_FATAL, "SLCODE","%-15s:%04d:pid=%d:ctid=%d => " f, __FUNCTION__, __LINE__,getpid(),gettid(),##__VA_ARGS__);\
	} while(0)
#endif
//#ifndef SL_LOGS
//#define SL_LOGS(f,...)	sl_log_printf(SL_LOG_SILENT, "SLCODE","%-15s:%04d:pid=%d:ctid=%d => "f, __FUNCTION__, __LINE__,getpid(),gettid(),##__VA_ARGS__);
//#endif

#else

#define SLLOGTAGD "SLCODE D "
#define SLLOGTAGE "SLCODE E "
int sl_log_printf(int prio,const char *fmt, ...);

#if defined(__ZOS__tbase__)
#include "taStd.h"
#include "tee_internal_api.h"
#endif

#ifndef SL_LOGD
#define SL_LOGD(f,...)	\
	do {	\
	sl_log_printf(SL_LOG_VERBOSE,"SLCODE D %-15s:%04d => "f, __FUNCTION__, __LINE__,##__VA_ARGS__);	\
	} while(0)
#endif

#ifndef SL_LOGE
#define SL_LOGE(f,...)	\
	do {	\
	sl_log_printf(SL_LOG_ERROR,"SLCODE E %-15s:%04d => "f, __FUNCTION__, __LINE__,##__VA_ARGS__);	\
	} while(0)
#endif

#define SL_LOGU		SL_LOGD
#define SL_LOGV		SL_LOGD
#define SL_LOGI		SL_LOGD
#define SL_LOGW		SL_LOGD
#define SL_LOGF		SL_LOGE

#endif

#ifdef __cplusplus
}
#endif

#endif
