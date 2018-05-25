#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <errno.h>

#include "fp_log.h"
#include "QSEEComAPI.h"
#include "fp_ta_entry.h"

#define FPTAG " fp_tee_qsee.cpp "
#define FPSENSOR_TA_PATH "/firmware/image"
#define FPSENSOR_TA_NAME "fngap64"
#define FPSENSOR_TA_MAX_SHARE_BUFF_SIZE     MAX_TEE_SHM_SIZE

fpsensor_qsee_msg_header *qseeCmd = NULL;
fpsensor_qsee_msg_response *qseeRsp = NULL;
struct QSEECom_handle *qsee_sessionHandle = NULL;
struct qseecom_app_info app_info;
static uint32_t g_cmd_sn = 0;

int32_t tlcFunc(unsigned char *pCmd, int len, taGetRspCunc pRspFunc, void *pProcessorInstance)
{
    int32_t status = 0;
    int32_t cmd_len = 0;
    int32_t rsp_len = 0;
    fp_cmd_no_payload_t *cmd = NULL;

    if (!qsee_sessionHandle)
    {
        LOGE(FPTAG"CAN NOT send Qsee CMD, TA is not loaded\n!!!");
        return -1;
    }

    if ((uint32_t)len > sizeof(fp_Cmd))
    {
        LOGE(FPTAG"%s error, CMD len %d, but fp_cmd size %d", __func__, len, (int)sizeof(fp_Cmd));
        return -1;
    }

    qseeCmd = (fpsensor_qsee_msg_header *)qsee_sessionHandle->ion_sbuffer;
    cmd_len = sizeof(fpsensor_qsee_msg_header);
    if (cmd_len & QSEECOM_ALIGN_MASK)
    {
        cmd_len = QSEECOM_ALIGN(cmd_len);
    }

    memcpy(&qseeCmd->fpCmd, pCmd, len);
    qseeCmd->cmdLen = len;
    if (rsp_len & QSEECOM_ALIGN_MASK)
    {
        rsp_len = QSEECOM_ALIGN(rsp_len);
    }

    // tag cmd index for easier debugging
    g_cmd_sn = (g_cmd_sn++ > 100000) ? 0 : g_cmd_sn;
    qseeCmd->data1 = g_cmd_sn;

    qseeRsp = (fpsensor_qsee_msg_response *)(qsee_sessionHandle->ion_sbuffer + cmd_len);
    rsp_len = sizeof(fpsensor_qsee_msg_response);

    cmd = (fp_cmd_no_payload_t *)pCmd;
    LOGD(FPTAG"send cmd_id:%d, cmd_len:%d, sn:%u", cmd->cmd_id, len, g_cmd_sn);
    status = QSEECom_send_cmd(qsee_sessionHandle, qseeCmd, cmd_len, qseeRsp, rsp_len);

    if (status)
    {
        LOGE(FPTAG"%s send_cmd failed %i", __func__, status);
        return -1;
    }

    LOGD(FPTAG"recv rsp_code:%d, rsp_len:%d", qseeRsp->rspCode, qseeRsp->rspLen);
    if (qseeRsp->rspCode != 0)
    {
        LOGE(FPTAG"%s get response error %i", __func__, qseeRsp->rspCode);
        //return -1;
    }

    if (pRspFunc)
    {
        pRspFunc((char *) & (qseeRsp->fpRsp), qseeRsp->rspLen, pProcessorInstance);
    }

    if (qseeRsp->rspCode == -502) {
        LOGD(FPTAG"enroll authorize timeout!");
        return qseeRsp->rspCode;
    }

    return qseeRsp->rspCode;
}

int taOpen(void)
{
    int status = 0;

    LOGD(FPTAG"%s open TA %s/%s\n", __func__, FPSENSOR_TA_PATH, FPSENSOR_TA_NAME);
    status = QSEECom_start_app(&qsee_sessionHandle, FPSENSOR_TA_PATH, FPSENSOR_TA_NAME,
                               FPSENSOR_TA_MAX_SHARE_BUFF_SIZE);
    if (status)
    {
        LOGE(FPTAG"%s start_app failed: %i", __func__, status);
        goto err;
    }

    status = QSEECom_get_app_info(qsee_sessionHandle, &app_info);
    if (status)
    {
        LOGE("Error to get app info\n");
        goto err;
    }
    if (app_info.is_secure_app_64bit)
    {
        LOGD("64bit QSEE TA!\n");
    }
    else
    {
        LOGD("32bit QSEE TA!\n");
    }
    LOGD(FPTAG"%s open qsee TA OK! %s-%s", __func__, __DATE__, __TIME__);
    return 0;

err:
    return -1;
}

void taClose(void)
{
    LOGD(FPTAG"%s Close QSEE TA %s\n", __func__, FPSENSOR_TA_NAME);
    int status = QSEECom_shutdown_app(&qsee_sessionHandle);

    if (status)
    {
        LOGE(FPTAG"%s shutdown_app failed: %i", __func__, status);
    }

    qsee_sessionHandle = NULL;
    return;
}

#include <hw_auth_token.h>
#define QSEE_KEYMASTER_TA_NAME "keymaster"
#define KEYMASTER_UTILS_CMD_ID  0x200UL
typedef enum {
    KEYMASTER_GET_AUTH_TOKEN_KEY = (KEYMASTER_UTILS_CMD_ID + 5UL),
    KEYMASTER_LAST_CMD_ENTRY = (int)0xFFFFFFFFULL
} keymaster_cmd_t;

typedef struct _km_get_auth_token_req_t {
    keymaster_cmd_t cmd_id;
    hw_authenticator_type_t auth_type;
}__attribute__ ((packed)) km_get_auth_token_req_t;

typedef struct _km_get_auth_token_rsp_t {
    int status;
    uint32_t auth_token_key_offset;
    uint32_t auth_token_key_len;
}__attribute__ ((packed)) km_get_auth_token_rsp_t;

void fp_qsee_km_release_encapsulated_key(uint8_t* encapsulated_key)
{
    free(encapsulated_key);
}

int fp_qsee_km_get_encapsulated_key(uint8_t **encapsulated_key, uint32_t *size_encapsulated_key)
{
    LOGD("%s begin", __func__);

    int retval = 0;
    *encapsulated_key = NULL;
    *size_encapsulated_key = 0;
    const uint32_t shared_buffer_size = 1024;
    uint32_t command_length = 0;
    uint32_t response_length = 0;
    km_get_auth_token_req_t* command = NULL;
    km_get_auth_token_rsp_t* response = NULL;
    struct QSEECom_handle* keymaster_handle = NULL;
    
    retval = QSEECom_start_app(&keymaster_handle,
                               "/system/etc/firmware",
                               QSEE_KEYMASTER_TA_NAME,
                               shared_buffer_size);


    if (retval) {
        LOGE("%s start_app failed %i", __func__, retval);
        goto out;
    }

    command = (km_get_auth_token_req_t*)keymaster_handle->ion_sbuffer;
    command_length = QSEECOM_ALIGN(sizeof(km_get_auth_token_req_t));
    response = (km_get_auth_token_rsp_t*)(keymaster_handle->ion_sbuffer + command_length);

    command->cmd_id = KEYMASTER_GET_AUTH_TOKEN_KEY;
    command->auth_type = HW_AUTH_FINGERPRINT;

    response_length = shared_buffer_size - command_length;

    retval = QSEECom_send_cmd(keymaster_handle,
                              command,
                              command_length,
                              response,
                              response_length);

    if (retval) {
        LOGE("%s failed to send key auth token key command: %d",
             __func__, retval);
        goto out;
    }

    if (response->status) {
        LOGE("%s KEYMASTER_GET_AUTH_TOKEN_KEY returned status=%d",
             __func__, response->status);
        retval = -1;
        goto out;
    }

    *encapsulated_key = (uint8_t *)malloc(response->auth_token_key_len);
    if (*encapsulated_key == NULL) {
        retval = -2;
        goto out;
    }

    *size_encapsulated_key = response->auth_token_key_len;

    memcpy(*encapsulated_key,
                   ((uint8_t*) response) + response->auth_token_key_offset,
                   *size_encapsulated_key);

out:
    if (keymaster_handle) {
        QSEECom_shutdown_app(&keymaster_handle);
    }
    return retval;
}
