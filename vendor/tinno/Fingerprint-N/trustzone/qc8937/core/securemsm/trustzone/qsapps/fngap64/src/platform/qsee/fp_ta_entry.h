#ifndef __FP_TA_ENTRY_H__
#define __FP_TA_ENTRY_H__

#include "fp_tee_types.h"

#define MAX_TEE_SHM_SIZE    (640 * 1024)
#define MAX_DATA_LEN        (512 * 1024)
#define MAX_LOAD_CHUNK      MAX_DATA_LEN
#define MAX_STORE_CHUNK     MAX_DATA_LEN

typedef struct {
    int32_t  cmdLen;
	uint32_t data1;
	uint32_t data2;
    fp_cmd_t fpCmd;
}__attribute__ ((aligned (64))) fpsensor_qsee_msg_header;

typedef struct{
    int32_t  rspCode;
    int32_t  rspLen;
    fp_rsp_t fpRsp;
}__attribute__ ((aligned (64))) fpsensor_qsee_msg_response;

#endif // __FP_TA_ENTRY_H__
