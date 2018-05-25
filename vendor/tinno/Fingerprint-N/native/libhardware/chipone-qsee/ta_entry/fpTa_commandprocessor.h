#ifndef FP_TA_CMD_PROCESSOR_H
#define FP_TA_CMD_PROCESSOR_H


#include <inttypes.h>
#include "fpTa_entryproxy.h"



typedef enum
{
    CHECK_FINGER_DOWN = 0,
    CHECK_NAV_DOWN,
    CHECK_FINGER_UP,
} FingerPresentMode_t;




//---------------------------------------------------rsp cmd

class fpTaCmdProcessor
{
  public:
    fpTaCmdProcessor();
    virtual ~fpTaCmdProcessor();
    int32_t sendCmd();


    int32_t construct_load_template_cmd(char *data,
                                    size_t file_len,
                                    size_t data_len);
    int32_t  constructSetActiveGroupCmd(int32_t igid, char *pPath);
    int32_t  constructGidFidCmd(int32_t cmd, int32_t gid, int32_t fid);
    int32_t  constructAuthenticateCmd(int32_t igid, int32_t forEnrollMatchImage);
    int32_t  constructRelCoordsCmd(int32_t start_flag);
    int32_t  constructSetProperityCmd(int32_t tag, int32_t value);
    int32_t  constructGetFingerRectCmd(int32_t idx);
    int32_t  constructAuthorizeEnrolCmd(int32_t cmd, const hw_auth_token_t *token);
    int32_t  constructBeginAuthCmd(int32_t cmd, uint64_t challenge);
    int32_t  constructOneParaCmd(int32_t cmd, int32_t para);
    int32_t  constructInjectloadCmd(char *img_buf, int32_t len, int32_t img_idx);
    int32_t  construct_array_para_cmd(int32_t cmd, uint8_t *p_para, uint32_t array_len);
    int32_t  getRsp(char *pBuffer, int32_t iLen);
    int32_t  processCaptureImgRsp(void);
    int32_t  processCaptureImgToolRsp(int32_t *pWidth, int32_t *pHeight, char *data);
    int32_t  processEnrollImgRsp(int32_t *pLastProgress, int32_t *pTotalEnrollCnt, int32_t *pCurEnrollCnt,
                             int32_t *pEnrollFailReason, int32_t *pFillPart);
    int32_t  processAuthenticateRsp(int32_t *pMatchResult, int32_t *pMatchFinger, char *path,
                                hw_auth_token_t *p_token);
    int32_t  processEndAuthenticateRsp(int32_t *isTemplateUpdated);
    int32_t  processNoPayloadRsp(int32_t origCmdID);
    int32_t  processPreEnrollRsp(uint64_t *pChanllge);
    int32_t  processCheckFingerPresentRsp(FingerPresentMode_t Mode);
    int32_t  processGetEnrolledFidsRsp(int32_t *pEnrolledFids, int32_t iArrayCap, int32_t *pRealCnt);
    int32_t  processGetRelCoordsRsp(int32_t *pDeltaX, int32_t *pDeltaY, int32_t *pRet);
    int32_t  processGetFingerRectCntRsp(int32_t *p_rect_cnt);
    int32_t  processGetImageQualityRsp(int32_t *p_area, int32_t *p_condition, int32_t *p_quality);
    int32_t  processGetFingerRectRsp(int32_t *p_rect);
    int32_t  processGetTemplateIdsRsp(int32_t *p_real_size, int32_t *p_ids);
    int32_t  processGetImgFormatRsp(int32_t *p_width, int32_t *p_height);
    int32_t  constructNoPayloadCmd(int32_t cmd);
    int32_t  processGetVersionRsp(char *ta_ver, char *driver_ver, char *algo_ver);
    int32_t  processGetAuthenticatorIdRsp(uint64_t *TplDbId);
    int32_t  processReadRegRsp(int32_t *value);
    int32_t  process_get_tee_info_rsp(uint32_t *p_revision, uint32_t *p_hw_id);
    int32_t  process_one_par_rsp(uint32_t *p_par);
    int32_t  process_get_byte_array_rsp(uint8_t **buff, size_t *buff_len, uint32_t *remain);
    int32_t  send_cmd_no_payload(int32_t cmd);
    int32_t  send_cmd_one_par(int32_t cmd, int32_t para);
    int32_t  send_cmd_array_para(int32_t cmd, uint8_t *p_para, uint32_t array_len);
#ifdef FP_TEE_QSEE4
	int32_t  constructSetAuthTokenKeyCmd(uint8_t *key, int32_t key_size);
#endif
  private:
    int32_t saveTpls2File(char *path, uint8_t *pFileContent, int32_t iFileLen);
    //cmd buffer store the fp command part, not the whole tci message
    int32_t  checkAndAllocCmdBuffer(int32_t iLen);
    unsigned char *pCmdBuffer;
    int32_t mRealCmdBufferLen;
    int32_t mCmdLen;

    //rsp buffer store the fp rsp buffer  mesage
    int32_t checkAndAllocRspBuffer(int32_t iLen);
    unsigned char *pRspBuffer;
    int32_t mRealRspBufferLen;
    int32_t mRspLen;
};

#endif
