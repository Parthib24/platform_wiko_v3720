#include "fp_internal_api.h"
#include "fp_tee_types.h"

#include <qsee_log.h>
#include <object.h>

#include "fp_ta_entry.h"
#include "COEMUnwrapKeys.h"
#include "COEMUnwrapKeys_open.h"

char TZ_APP_NAME[] = {"fingerprint"};
extern int processCmdFingerPrint(void *pCmd, int cmdLen, void *pRsp, int *pRspLen);
extern int _process_cmd_fingerprint(void *pCmd, void *pRsp, int *pLen);
extern const char *get_cmd_str_from_cmd_id(int32_t iCmd);

/*
 * To avoid error:undefined reference to __aeabi_idiv0
 */
#if 0
int __aeabi_idiv0(int return_value)
{
    LOGE("Error!!! divide by zero\n!!!");
    return return_value;
}
#endif

/**
@brief
Add any app specific initialization code here
QSEE will call this function after secure app is loaded and
authenticated
*/
void tz_app_init(void)
{
    // Set logging according to build environment settings
    //qsee_log_set_mask(QSEE_LOG_MSG_ERROR | QSEE_LOG_MSG_FATAL
    //                | QSEE_LOG_MSG_HIGH  | QSEE_LOG_MSG_MED );
    qsee_log_set_mask(QSEE_LOG_MSG_ERROR | QSEE_LOG_MSG_FATAL);

    LOGD("tz_app_%s init, %s-%s\n", TZ_APP_NAME, __DATE__, __TIME__);
}

/**
@brief
App specific shutdown
App will be given a chance to shutdown gracefully
*/
void tz_app_shutdown(void)
{
    LOGD("tz_app_shutdown invoked");
}

void tz_app_cmd_handler(void* command, uint32_t cmdlen, void* response, uint32_t rsplen)
{
    int iRspLen = 0;
    fpsensor_qsee_msg_header* cmd = (fpsensor_qsee_msg_header *)command;
    fpsensor_qsee_msg_response* rsp = (fpsensor_qsee_msg_response *)response;

    if (!cmd || !rsp) {
        LOGE("%s invalid input", __func__);
        return;
    }

    LOGD("%s cmd length %d\n", __func__, cmd->cmdLen);
    // clean response buffer first
    memset(response, 0x00, rsplen);
    if (cmd->cmdLen > 0) {
        int status = processCmdFingerPrint(cmd, cmd->cmdLen, rsp, &iRspLen);
        LOGD("%s cmd process rsp %d, rsp_len %d\n", __func__, status, iRspLen);
        rsp->rspCode = status;
        rsp->rspLen = iRspLen;
    } else {
        LOGE("cmd length is 0, is this a test cmd sent by qseecom_sample_client?");
        rsp->rspCode = 0;
        rsp->rspLen = 0;
    }
    return ;
}

/**
 * Process a fingerprint command message.
 * The command data will be checked for in.
 *
 * @return 0 if operation has been successfully completed.
 */
int processCmdFingerPrint(void *pQseeCmd, int cmdLen, void *pQseeRsp,int *pRspLen){
        fp_cmd_t *pCmd = NULL;
        fp_rsp_t *pRsp = NULL;
        int iRet = 0;
        
        // Verify that NWd buffer is fully in NWd, and does not extend too far.
        if(cmdLen > MAX_DATA_LEN) {
            LOGE("TA Tlfingerprint: Error, invalid cipher data length (> %d).\n", MAX_DATA_LEN);
            return -1;
        }

        fpsensor_qsee_msg_header *QseeMsgCmd = (fpsensor_qsee_msg_header*)pQseeCmd;
        fpsensor_qsee_msg_response* QseeMsgRsp = (fpsensor_qsee_msg_response*)pQseeRsp;
        pRsp = &(QseeMsgRsp->fpRsp);
        pCmd = &(QseeMsgCmd->fpCmd);

        LOGD("TA received CMD %d:%s, sn:%u, %s-%s\n", pCmd->c.no_payload_cmd.cmd_id, 
		                        get_cmd_str_from_cmd_id(pCmd->c.no_payload_cmd.cmd_id), QseeMsgCmd->data1, __DATE__, __TIME__);
        
        iRet = _process_cmd_fingerprint((void *)pCmd,(void *)pRsp,pRspLen);
        return iRet;
}

int32_t tz_module_open(uint32_t uid, Object cred, Object *objOut)
{
  if (uid == COEMUnwrapKeys_UID) {
        LOGD("alipay Server chipone tz_module_open() success uid: %d", uid);
        return COEMUnwrapKeys_open(cred, objOut);
    } else {
        LOGE("alipay Server chipone tz_module_open() faild uid: %d", uid);
        *objOut = (Object){NULL, NULL};
	return Object_ERROR_INVALID;
    }
}
