#ifndef __FP_LOG_H__
#define __FP_LOG_H__

#include <stdbool.h>
#include <qsee_log.h>

/*************************************************************************
*   Begin to define macros for printing log                             *
*************************************************************************/
#define FP_LOG_DEBUG_LEVEL   3
#define FP_LOG_INFO_LEVEL    2
#define FP_LOG_ERROR_LEVEL   1

#ifndef FP_LOG_LEVEL
#define FP_LOG_LEVEL 3
#endif

/* debug */
#if( FP_LOG_LEVEL >= FP_LOG_DEBUG_LEVEL )
#define LOGD(...)   QSEE_LOG(QSEE_LOG_MSG_ERROR, __VA_ARGS__)
#else
#define LOGD(...)
#endif

/* info */
#if( FP_LOG_LEVEL >= FP_LOG_INFO_LEVEL )
#define LOGI(...)   QSEE_LOG(QSEE_LOG_MSG_ERROR,  __VA_ARGS__)
#else
#define LOGI(...)
#endif

/* error */
#if( FP_LOG_LEVEL >= FP_LOG_ERROR_LEVEL )
#define LOGE(...)   QSEE_LOG(QSEE_LOG_MSG_ERROR,  __VA_ARGS__)
#else
#define LOGE(...)
#endif

#endif /* __FP_LOG_H__ */
