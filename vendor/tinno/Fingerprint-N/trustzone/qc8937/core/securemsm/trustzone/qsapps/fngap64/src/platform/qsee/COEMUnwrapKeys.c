// Copyright (c) 2015 Qualcomm Technologies, Inc.  All Rights Reserved.
// Qualcomm Technologies Proprietary and Confidential.

#include <stringl.h>

#include "IUnwrapKeys_invoke.h"
#include "COEMUnwrapKeys.h"
#include "COEMUnwrapKeys_open.h"

#include "fp_log.h"
#include "fp_ta_entry.h"

#define MAX_MSG_LEN         300
#define MAX_NBR_TEMPLATES   FINGER_MAX_COUNT
#define LOG_TAG             "[fp_ifaa_interface]"

#define  IFAA_OK                       (0)
#define  IFAA_ERROR_GENERAL            (1)
#define  IFAA_ERROR_MEMORY             (2)
#define  IFAA_ERROR_PARAMETER          (3)
#define  IFAA_ERROR_HANDLE             (4)
#define  IFAA_ERROR_RANGE              (5)
#define  IFAA_ERROR_TIMEOUT            (6)
#define  IFAA_ERROR_STATE              (7)
#define  IFAA_ERROR_APP_NOT_FOUND      (8)
#define  IFAA_ERROR_NO_RESPONSE        (9)

typedef enum {
    IFAA_ERR_SUCCESS                     = 0x00000000,
    IFAA_ERR_UNKNOWN                     = 0x7A000001,
    IFAA_ERR_BAD_ACCESS                  = 0x7A000002,
    IFAA_ERR_BAD_PARAM                   = 0x7A000003,
    IFAA_ERR_UNKNOWN_CMD                 = 0x7A000004,
    IFAA_ERR_BUF_TO_SHORT                = 0x7A000005,
    IFAA_ERR_OUT_OF_MEM                  = 0x7A000006,
    IFAA_ERR_TIMEOUT                     = 0x7A000007,
    IFAA_ERR_HASH                        = 0x7A000008,
    IFAA_ERR_SIGN                        = 0x7A000009,
    IFAA_ERR_VERIFY                      = 0x7A00000A,
    IFAA_ERR_KEY_GEN                     = 0x7A00000B,
    IFAA_ERR_READ                        = 0x7A00000C,
    IFAA_ERR_WRITE                       = 0x7A00000D,
    IFAA_ERR_ERASE                       = 0x7A00000E,
    IFAA_ERR_NOT_MATCH                   = 0x7A00000F,
    IFAA_ERR_GEN_RESPONSE                = 0x7A000010,
    IFAA_ERR_GET_DEVICEID                = 0x7A000011,
    IFAA_ERR_GET_LAST_IDENTIFIED_RESULT  = 0x7A000012,
    IFAA_ERR_AUTHENTICATOR_SIGN          = 0x7A000013,
    IFAA_ERR_GET_ID_LIST                 = 0x7A000014,
    IFAA_ERR_GET_AUTHENTICATOR_VERSION   = 0x7A000015,
    IFAA_ERR_UN_INITIALIZED              = 0x7A000016,
    IFAA_ERR_NO_OPTIONAL_LEVEL           = 0x7A000017,
} IFAA_Result;

fpsensor_qsee_msg_header g_qsee_cmd;
fpsensor_qsee_msg_response g_qsee_rsp;
extern void tz_app_cmd_handler(void* command, uint32_t cmdlen, void* respone, uint32_t rsplen);

static void tz_app_cmd_handler_internal(void* command, uint32_t cmdlen, void* respone, uint32_t rsplen)
{
    LOGE("%s-%s-%s:enter\n", __DATE__, __TIME__, __func__);
    tz_app_cmd_handler(command, cmdlen, respone, rsplen);
    LOGE("%s-%s-%s:exit\n", __DATE__, __TIME__, __func__);
}

static int fp_sec_ifaa_getLastId(uint32_t *buf_id, uint32_t *id_len)
{
    int ret = IFAA_OK;
    int32_t cmd_len = sizeof(fpsensor_qsee_msg_header);
    int32_t rsp_len = sizeof(fpsensor_qsee_msg_response);

    fpsensor_qsee_msg_header *qseeCmd = &g_qsee_cmd;
    fpsensor_qsee_msg_response *qseeRsp = &g_qsee_rsp;
    memset(qseeCmd, 0x00, cmd_len);
    memset(qseeRsp, 0x00, rsp_len);

    do {
        if (NULL == buf_id) {
            ret = IFAA_ERR_BAD_PARAM;
            LOGE(LOG_TAG "%s bad parameter, pId is NULL!", __func__);
            break;
        }
        qseeCmd->fpCmd.c.no_payload_cmd.cmd_id = TCI_FP_CMD_GET_LAST_IDENTIFY_ID;
        qseeCmd->cmdLen = sizeof(fp_cmd_no_payload_t); 
        LOGE(LOG_TAG "cmdLen:%d\n", qseeCmd->cmdLen); 
        tz_app_cmd_handler_internal(qseeCmd, cmd_len, qseeRsp, rsp_len);
        *buf_id = qseeRsp->fpRsp.r.GetLastFingerIDRsp.FingerID;
        LOGE(LOG_TAG "%s success to TCI_FP_CMD_GET_LAST_IDENTIFY_ID", __func__);
        LOGE(LOG_TAG "LAST_IDENTIFY_ID :0x%x\n", *buf_id);
    } while (0);

    return ret;
}

static IFAA_Result fp_sec_ifaa_getIdList(uint32_t *pIdBuffer, uint32_t *pIdCount)
{
    IFAA_Result ret = IFAA_ERR_SUCCESS;
    int32_t cmd_len = sizeof(fpsensor_qsee_msg_header);
    int32_t rsp_len = sizeof(fpsensor_qsee_msg_response);

    fpsensor_qsee_msg_header *qseeCmd = &g_qsee_cmd;
    fpsensor_qsee_msg_response *qseeRsp = &g_qsee_rsp;
    memset(qseeCmd, 0x00, cmd_len);
    memset(qseeRsp, 0x00, rsp_len);
    do {
        if (NULL == pIdBuffer) {
            ret = IFAA_ERR_BAD_PARAM;
            LOGE(LOG_TAG "%s invalid parameter, pIdBuffer is NULL", __func__);
            break;
        }
        qseeCmd->fpCmd.c.no_payload_cmd.cmd_id = TCI_FP_CMD_GET_ENROLLED_FIDS;
        qseeCmd->cmdLen = sizeof(fp_cmd_no_payload_t); 
        LOGE(LOG_TAG "cmdLen:%d\n", qseeCmd->cmdLen);
        tz_app_cmd_handler_internal(qseeCmd, cmd_len, qseeRsp, rsp_len);
        *pIdCount = qseeRsp->fpRsp.r.get_enrol_fids_rsp.FidCnt;
        {
            uint8_t *pSrc = (uint8_t *)qseeRsp->fpRsp.r.get_enrol_fids_rsp.Fids;
            uint8_t *pDst = (uint8_t *)pIdBuffer;
            uint32_t i = 0;
            uint32_t count = *pIdCount * sizeof(uint32_t);
            while(i++ < count) {
                *pDst++ = *pSrc++;
            }
        }
    } while(0);

    LOGE(LOG_TAG "%s exit,ret:0x%x", __func__, ret);
    return ret;
}

static inline int32_t
COEMUnwrapKeys_unwrap(void *cxt,
                      const void *wrapped_ptr,
                      size_t wrapped_len,
                      void *unwrapped_ptr,
                      size_t unwrapped_len,
                      size_t *unwrapped_lenout)
{
    uint32_t msg_lastid_out;  
    uint8_t tmp_wrap_buf[MAX_MSG_LEN];  
    uint32_t ids[MAX_NBR_TEMPLATES];	
	uint32_t id_count = MAX_NBR_TEMPLATES;
  
    *unwrapped_lenout = memscpy(tmp_wrap_buf, sizeof(tmp_wrap_buf), wrapped_ptr, wrapped_len);
    LOGD(LOG_TAG "COEMUnwrapKeys_unwrap wr before1 =%s,%d \r\n ",
                                            tmp_wrap_buf, sizeof(tmp_wrap_buf));

    if (0 == strncmp((void *)tmp_wrap_buf, "fpidlist", 8)) {
        LOGD(LOG_TAG "COEMUnwrapKeys_unwrap cmd=[%s] \r\n", tmp_wrap_buf);
        fp_sec_ifaa_getIdList(ids, &id_count);
	    *unwrapped_lenout = memscpy(unwrapped_ptr, unwrapped_len, ids, id_count*sizeof(uint32_t));
    }
	if (0 == strncmp((void *)tmp_wrap_buf, "fplastid", 8)) {
        LOGD(LOG_TAG "COEMUnwrapKeys_unwrap cmd=[%s] \r\n ", tmp_wrap_buf);
        fp_sec_ifaa_getLastId(&msg_lastid_out, NULL);
        *unwrapped_lenout = memscpy(unwrapped_ptr, unwrapped_len, &msg_lastid_out, sizeof(msg_lastid_out));
    }
    LOGD(LOG_TAG "COEMUnwrapKeys_unwrap after unwrapptr=%s,unwrapplen=%d,unwrapout=%d \r\n ",
                                unwrapped_ptr,unwrapped_len,*unwrapped_lenout);
    return Object_OK;
}

// This implementation does not require a context record, so `retain` and
// `release` are no-ops.
#define COEMUnwrapKeys_release(ctx)   Object_OK
#define COEMUnwrapKeys_retain(ctx)    Object_OK

static IUnwrapKeys_DEFINE_INVOKE(COEMUnwrapKeys_invoke, COEMUnwrapKeys_, void*)

int32_t COEMUnwrapKeys_open(Object cred, Object *objOut)
{
    *objOut = (Object) { COEMUnwrapKeys_invoke, NULL };

    LOGD(LOG_TAG "COEMUnwrapKeys_open  \r\n ");
    return Object_OK;
}
