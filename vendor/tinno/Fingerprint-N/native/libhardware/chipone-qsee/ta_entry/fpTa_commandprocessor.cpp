#include "util.h"
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include "fp_log.h"
#include "fpTa_commandprocessor.h"
#include <assert.h>
#include <stdio.h>
#include "fp_common.h" 
#include "fp_tee_types.h"


#define FPTAG " fpTa_commandprocessor.cpp "

#ifdef __cplusplus
extern "C" {
#endif

extern void LOGD_HAT(const hw_auth_token_t *hat);

int32_t taProcessRsp(char *pBuffer, int32_t iLen, void *pCmdProcessor)
{
    fpTaCmdProcessor *pProcessor = (fpTaCmdProcessor *)pCmdProcessor;
    return pProcessor->getRsp(pBuffer, iLen);
}


#ifdef __cplusplus
}
#endif



fpTaCmdProcessor::fpTaCmdProcessor()
{
    LOGD(FPTAG"constructor invoked");
    pCmdBuffer = NULL;
    mCmdLen = 0;
    mRealCmdBufferLen = 0;
    //preallocate cmd buffer
    checkAndAllocCmdBuffer(sizeof(fp_cmd_t));

    pRspBuffer = NULL;
    mRealRspBufferLen = 0;
    mRspLen = 0;
    //preallocate resp buffer
    checkAndAllocRspBuffer(sizeof(fp_rsp_t));
}

fpTaCmdProcessor::~fpTaCmdProcessor()
{
    LOGD(FPTAG" destructor invoked");
    if (pCmdBuffer)
    {
        fp_free(pCmdBuffer);
    }

    pCmdBuffer = NULL;
    mCmdLen = 0;
    mRealCmdBufferLen = 0;

    if (pRspBuffer)
    {
        fp_free(pRspBuffer);
    }
    pRspBuffer = NULL;
    mRealRspBufferLen = 0;
    mRspLen = 0;
};

//--------------------------------------------------------cmd sendor
int32_t fpTaCmdProcessor::sendCmd()
{
    return tlcFunc(pCmdBuffer, mCmdLen, taProcessRsp, (void *)this);
}

int32_t fpTaCmdProcessor::constructBeginAuthCmd(int32_t cmd, uint64_t challenge)
{
    if (0 != checkAndAllocCmdBuffer(sizeof(fp_cmd_begin_auth_t)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(fp_cmd_begin_auth_t));
        return -ENOMEM;
    }

    fp_cmd_begin_auth_t *pBeginAuthCmd = (fp_cmd_begin_auth_t *)pCmdBuffer;
    pBeginAuthCmd->cmd_id = cmd;
    pBeginAuthCmd->challenge = challenge;

    mCmdLen = sizeof(fp_cmd_begin_auth_t);
    return 0;
}

int32_t fpTaCmdProcessor::construct_load_template_cmd(char *data,
                                                  size_t file_len,
                                                  size_t data_len)
{

    LOGD(FPTAG"whole file len:%zu,  data len:%zu", file_len, data_len);
    mCmdLen = sizeof(fp_cmd_byte_array_t) + data_len;

    if (0 != checkAndAllocCmdBuffer(mCmdLen))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", mCmdLen);
        return -ENOMEM;
    }
    fp_cmd_byte_array_t *p_byte_array_cmd = (fp_cmd_byte_array_t *)pCmdBuffer;
    p_byte_array_cmd->cmd_id = TCI_FP_CMD_LOAD_TEMPLATE;
    p_byte_array_cmd->file_len = file_len;
    p_byte_array_cmd->chunk_data_size = data_len;
    memset(&p_byte_array_cmd->template_data, 0, data_len);
    memcpy(&p_byte_array_cmd->template_data, data, data_len);
    
    return 0;
}

int32_t fpTaCmdProcessor::constructSetActiveGroupCmd(int32_t igid, char *pPath)
{

    LOGD(FPTAG"igid:%d", igid);
    mCmdLen = sizeof(fp_cmd_set_active_group_t);

    if (0 != checkAndAllocCmdBuffer(mCmdLen))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", mCmdLen);
        return -ENOMEM;
    }
    fp_cmd_set_active_group_t *pSetActivegroupCmd = (fp_cmd_set_active_group_t *)pCmdBuffer;
    pSetActivegroupCmd->cmd_id = TCI_FP_CMD_SET_ACTIVE_GROUP;
    pSetActivegroupCmd->igid = igid;
    memset(pSetActivegroupCmd->path, 0, TA_PATH_MAX);
    int32_t istrLen = strlen(pPath);
    memcpy(pSetActivegroupCmd->path, pPath, MIN((TA_PATH_MAX - 1), istrLen ));
    return 0;
}

int32_t fpTaCmdProcessor::constructAuthorizeEnrolCmd(int32_t cmd, const hw_auth_token_t *token)
{
    int32_t token_len = sizeof(hw_auth_token_t);
    if (0 != checkAndAllocCmdBuffer(sizeof(fp_cmd_authorize_enrol_t) + token_len))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)(sizeof(fp_cmd_authorize_enrol_t)) + token_len);
        return -ENOMEM;
    }

    fp_cmd_authorize_enrol_t *pAuthorizeEnrolCmd = (fp_cmd_authorize_enrol_t *)pCmdBuffer;
    pAuthorizeEnrolCmd->cmd_id = cmd;
    pAuthorizeEnrolCmd->hat_size = token_len;
    memcpy(&pAuthorizeEnrolCmd->hat, token, token_len);

    mCmdLen = sizeof(fp_cmd_authorize_enrol_t) + token_len;

    return 0;
}

int32_t fpTaCmdProcessor::constructGidFidCmd(int32_t cmd, int32_t gid, int32_t fid)
{
    if (0 != checkAndAllocCmdBuffer(sizeof(fp_cmd_gid_fid_t)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(fp_cmd_gid_fid_t));
        return -ENOMEM;
    }

    fp_cmd_gid_fid_t *pGidFidCmd = (fp_cmd_gid_fid_t *)pCmdBuffer;
    pGidFidCmd->cmd_id = cmd;
    pGidFidCmd->igid = gid;
    pGidFidCmd->ifid = fid;

    mCmdLen = sizeof(fp_cmd_gid_fid_t);
    return 0;
}


int32_t fpTaCmdProcessor::constructAuthenticateCmd(int32_t igid, int32_t forEnrollMatchImage)
{
    if (0 != checkAndAllocCmdBuffer(sizeof(fp_cmd_authenticate_t)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(fp_cmd_authenticate_t));
        return -ENOMEM;
    }

    fp_cmd_authenticate_t *pAuthenticateCmd = (fp_cmd_authenticate_t *)pCmdBuffer;
    pAuthenticateCmd->cmd_id = TCI_FP_CMD_AUTHENTICATE;
    pAuthenticateCmd->challengID = 0;
    pAuthenticateCmd-> igid = igid;
    pAuthenticateCmd->forEnrollCheck = forEnrollMatchImage;
    mCmdLen = sizeof(fp_cmd_authenticate_t);
    return 0;
}

int32_t fpTaCmdProcessor::constructInjectloadCmd(char *img_buf, int32_t len, int32_t img_idx)
{
    if (0 != checkAndAllocCmdBuffer(sizeof(fp_cmd_inject_t)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(fp_cmd_inject_t));
        return -ENOMEM;
    }

    fp_cmd_inject_t *pInjectCmd = (fp_cmd_inject_t *)pCmdBuffer;

    pInjectCmd->cmd_id = TCI_FP_CMD_INJECT;
    pInjectCmd->dummy = img_idx;
    pInjectCmd-> data_len = len;
    memcpy(pInjectCmd->data, img_buf, len);
    mCmdLen = sizeof(fp_cmd_inject_t);
    return 0;
}

int32_t fpTaCmdProcessor::constructNoPayloadCmd(int32_t cmd)
{
    if (0 != checkAndAllocCmdBuffer(sizeof(int32_t)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(int32_t));
        return -ENOMEM;
    }

    memcpy(pCmdBuffer, &cmd, sizeof(int32_t));
    mCmdLen = sizeof(int32_t);
    return 0;
}

int32_t fpTaCmdProcessor::send_cmd_no_payload(int32_t cmd)
{
    int32_t status = 0;
    status = constructNoPayloadCmd(cmd);
    if (status)
    {
        return status;
    }
    return sendCmd();
}

int32_t fpTaCmdProcessor::constructOneParaCmd(int32_t cmd, int32_t para)
{
    if (0 != checkAndAllocCmdBuffer(sizeof(fp_cmd_one_para_t)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(fp_cmd_one_para_t));
        return -ENOMEM;
    }

    fp_cmd_one_para_t *pCmd = (fp_cmd_one_para_t *) pCmdBuffer;
    pCmd->cmd_id = cmd;
    pCmd->para = para;
    mCmdLen = sizeof(fp_cmd_one_para_t);
    return 0;
}

int32_t fpTaCmdProcessor::send_cmd_one_par(int32_t cmd, int32_t para)
{
    int32_t status = 0;
    status = constructOneParaCmd(cmd, para);
    if (status)
    {
        return status;
    }
    return sendCmd();
}

int32_t fpTaCmdProcessor::construct_array_para_cmd(int32_t cmd, uint8_t *p_para, uint32_t array_len)
{
    if ((NULL == p_para) || (array_len <= 0))
    {
        return -ENOMEM;
    }
    mCmdLen = sizeof(fp_cmd_array_para_t) + array_len;
    if (0 != checkAndAllocCmdBuffer(sizeof(mCmdLen)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(fp_cmd_array_para_t));
        return -ENOMEM;
    }

    fp_cmd_array_para_t *pCmd = (fp_cmd_array_para_t *) pCmdBuffer;
    pCmd->cmd_id = cmd;
    pCmd->size   = array_len;
    memcpy(pCmd->array, p_para, array_len);
    return 0;
}

int32_t fpTaCmdProcessor::send_cmd_array_para(int32_t cmd, uint8_t *p_para, uint32_t array_len)
{
    int32_t status = 0;
    status = construct_array_para_cmd(cmd, p_para, array_len);
    if (status)
    {
        return status;
    }
    return sendCmd();
}

int32_t fpTaCmdProcessor::constructRelCoordsCmd(int32_t start_flag)
{
    if (0 != checkAndAllocCmdBuffer(sizeof(fp_cmd_rel_coords_t)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(fp_cmd_rel_coords_t));
        return -ENOMEM;
    }

    fp_cmd_rel_coords_t *pRelCoordsCmd = (fp_cmd_rel_coords_t *)pCmdBuffer;
    pRelCoordsCmd->cmd_id = TCI_FP_CMD_GET_REL_COORDS;
    pRelCoordsCmd->start = start_flag;
    mCmdLen = sizeof(fp_cmd_rel_coords_t);
    return 0;
}
//---------------capture tool relatived--------------
int32_t fpTaCmdProcessor::constructSetProperityCmd(int32_t tag, int32_t value)
{
    if (0 != checkAndAllocCmdBuffer(sizeof(fp_cmd_set_property_t)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(fp_cmd_set_property_t));
        return -ENOMEM;
    }

    fp_cmd_set_property_t *pSetPropertyCmd = (fp_cmd_set_property_t *)pCmdBuffer;
    pSetPropertyCmd->cmd_id = TCI_FP_CMD_SET_PROPERTY;
    pSetPropertyCmd->uTag = tag;
    pSetPropertyCmd->iValue = value;
    mCmdLen = sizeof(fp_cmd_set_property_t);
    return 0;
}

int32_t fpTaCmdProcessor::constructGetFingerRectCmd(int32_t idx)
{
    if (0 != checkAndAllocCmdBuffer(sizeof(fp_cmd_get_sub_rect_t)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(fp_cmd_get_sub_rect_t));
        return -ENOMEM;
    }

    fp_cmd_get_sub_rect_t *pSetPropertyCmd = (fp_cmd_get_sub_rect_t *)pCmdBuffer;
    pSetPropertyCmd->cmd_id = TCI_FP_CMD_GET_SUBRECT;
    pSetPropertyCmd->iIndex = idx;
    mCmdLen = sizeof(fp_cmd_get_sub_rect_t);
    return 0;
}

#ifdef FP_TEE_QSEE4
int32_t fpTaCmdProcessor::constructSetAuthTokenKeyCmd(uint8_t *key, int32_t key_size)
{
    if (0 != checkAndAllocCmdBuffer(sizeof(fp_cmd_inject_t)))
    {
        LOGD(FPTAG"alloc memory error, alloc len=%d", (int32_t)sizeof(fp_cmd_inject_t));
        return -ENOMEM;
    }

    fp_cmd_inject_t *pInjectCmd = (fp_cmd_inject_t *)pCmdBuffer;

    pInjectCmd->cmd_id = TCI_FP_CMD_SET_AUTH_TOKEN_KEY;
    pInjectCmd->dummy = 0x00;
    pInjectCmd->data_len = key_size;
    memcpy(pInjectCmd->data, key, key_size);
    mCmdLen = sizeof(fp_cmd_inject_t);
    return 0;
}
#endif

//-----------------------------------------------------

int32_t fpTaCmdProcessor::checkAndAllocCmdBuffer(int32_t iLen)
{
    if (pCmdBuffer == NULL)
    {
        pCmdBuffer = (unsigned char *)fp_malloc(iLen);
        if (pCmdBuffer == NULL)
        {
            return -ENOMEM;
        }

        mRealCmdBufferLen = iLen;
        return 0;
    }

    if (mRealCmdBufferLen < iLen)
    {
        fp_free(pCmdBuffer);

        pCmdBuffer = (unsigned char *)fp_malloc(iLen);
        if (pCmdBuffer == NULL)
        {
            return -ENOMEM;
        }

        mRealCmdBufferLen = iLen;
    }
    return 0;
}

//------------------------------------------rsp cmd processor
//the origCmdID is the original cmd_id
//the returned rsp ID should equal with RSP_ID(origCmdID)
int32_t fpTaCmdProcessor::processNoPayloadRsp(int32_t origCmdID)
{
    return 0;
}



int32_t fpTaCmdProcessor::process_one_par_rsp(uint32_t *p_par)
{
    fp_rsp_one_par_t *pRsp = (fp_rsp_one_par_t *) pRspBuffer;

    *p_par = pRsp->par;

    return 0;
}


int32_t fpTaCmdProcessor::process_get_byte_array_rsp(uint8_t **buff, size_t *buff_len, uint32_t *remain)
{
    fp_rsp_byte_array_t *pRsp = (fp_rsp_byte_array_t *) pRspBuffer;

    *buff = pRsp->array;
    *buff_len = pRsp->array_len;
    *remain = pRsp->remain_size;

    return 0;
}


int32_t fpTaCmdProcessor::processCaptureImgRsp(void)
{
    fp_rsp_capture_img_t *pRsp = (fp_rsp_capture_img_t *) pRspBuffer;
    int32_t iCaptureResult = pRsp->captureResult;
    if (iCaptureResult == 0)
    {
        if (fp_global_env.fp_internal_callback.on_generate_img_file_name)
        {
            fp_global_env.fp_internal_callback.on_generate_img_file_name();
        }

        if (get_fp_config_feature_store_captured_img())
        {
#ifdef DEBUG_ENABLE
            int8_t *pImgBuf = &(pRsp->imgBufStub);
            bmp_enhancement((char *)pImgBuf,pRsp->imgWidth, pRsp->imgHeight);
            save_bmp(get_capture_img_store_file_full_name(), (char *)pImgBuf, pRsp->imgWidth, pRsp->imgHeight);
            LOGD(FPTAG"fpTaCmdProcessor the first 4 bytes is(HEX):  %x %x %x %x ", pImgBuf[0], pImgBuf[1],
                 pImgBuf[2], pImgBuf[3]);
#endif
        }
    }
    LOGD(FPTAG"processCaptureImgRsp ret:%d", iCaptureResult);

    return iCaptureResult;

}

int32_t fpTaCmdProcessor::processCaptureImgToolRsp(int32_t *pWidth, int32_t *pHeight, char *data)
{
    fp_rsp_capture_img_t *pRsp = (fp_rsp_capture_img_t *) pRspBuffer;
    int32_t iCaptureResult = pRsp->captureResult;
    if (iCaptureResult == 0)
    {
        *pWidth = pRsp->imgWidth;
        *pHeight = pRsp->imgHeight;
        memcpy(data, &(pRsp->imgBufStub), pRsp->imgWidth * pRsp->imgHeight);
        //LOGD(FPTAG"return w:%d, h:%d", *pWidth, *pHeight);
        //LOGD(FPTAG"fpTacCmdProcessor image len: %d, begin data:  %x %x %x %x ",
        //       pRsp->imgWidth * pRsp->imgHeight, data[0],data[1],data[2],data[3]);
    }
    bmp_enhancement(data,*pWidth,*pHeight);

    LOGD(FPTAG"processCaptureImgToolRsp ret:%d, w:%d, h:%d", iCaptureResult, *pWidth, *pHeight);
    return iCaptureResult;
}


#define CAPTURE_FINGER_DOWN_THRESHOLD   (6)
#define NAV_FINGER_DOWN_THRESHOLD       (1)

int32_t fpTaCmdProcessor::processCheckFingerPresentRsp(FingerPresentMode_t Mode)
{
    fp_rsp_check_finger_present_t *pRsp = (fp_rsp_check_finger_present_t *) pRspBuffer;
    int32_t fpPresetSum = (int32_t)pRsp->FingerPresentSum;

    switch (Mode)
    {
        case CHECK_FINGER_DOWN:
            LOGD(FPTAG"check finger down!-->finger present status sum:%d! TH:%d", fpPresetSum,
                 CAPTURE_FINGER_DOWN_THRESHOLD);
            if (fpPresetSum <= CAPTURE_FINGER_DOWN_THRESHOLD)
            {
                LOGD(FPTAG"check finger present status: finger didn't touch enough! wait finger down again!");
                return -EAGAIN;
            }
            break;

        case CHECK_NAV_DOWN:
            LOGD(FPTAG"check navigation down!--> present status sum:%d! TH:%d", fpPresetSum,
                 NAV_FINGER_DOWN_THRESHOLD);
            if (fpPresetSum <= NAV_FINGER_DOWN_THRESHOLD)
            {
                LOGD(FPTAG"check navigation present status: finger didn't touch enough! wait finger down again!");
                return -EAGAIN;
            }
            break;

        case CHECK_FINGER_UP:
            LOGD(FPTAG"check finger up!-->finger present status sum:%d!", fpPresetSum);
            if (0 != fpPresetSum)
            {
                LOGD(FPTAG"check finger present status: finger still touch sensor! wait finger up again!");
                return -EAGAIN;
            }
            break;

        default:
            LOGE(FPTAG"CheckFingerPresent arg error!:%d", Mode);
            return -EIO;
            break;
    }

    return 0;
}

int32_t fpTaCmdProcessor::processEnrollImgRsp(int32_t *pLastProgress, int32_t *pTotalEnrollCnt,
                                          int32_t *pCurEnrollCnt, int32_t *pEnrollFailReason, int32_t *pFillPart)
{
    fp_rsp_enroll_img_t *pRsp = (fp_rsp_enroll_img_t *) pRspBuffer;
    LOGD(FPTAG"processEnrollImgRsp,LastProgress=%d, TotalEnrollCnt = %d, CurEnrollCnt=%d,EnrollFailReason=%d,FillPart=%d",
         pRsp->LastProgress, pRsp->TotalEnrollCnt, pRsp->CurEnrollCnt, pRsp->EnrollFailReason,
         pRsp->FillPart);
    *pLastProgress = pRsp->LastProgress;
    *pTotalEnrollCnt = pRsp->TotalEnrollCnt;
    *pCurEnrollCnt = pRsp->CurEnrollCnt;
    *pEnrollFailReason = pRsp->EnrollFailReason;
    *pFillPart = pRsp->FillPart;
    return 0;
}

int32_t fpTaCmdProcessor::processAuthenticateRsp(int32_t *pMatchResult, int32_t *pMatchFinger,
                                             char *path, hw_auth_token_t *p_token)
{
    fp_rsp_authenticate_t *pRsp = (fp_rsp_authenticate_t *) pRspBuffer;
    *pMatchResult = pRsp->matchResult;
    *pMatchFinger = pRsp->matchFid;
    memcpy(p_token, &pRsp->hat, pRsp->hat_size);

    if (p_token)
    {
        //LOGD_HAT(p_token);
    }

    LOGD(FPTAG"matched Fid: 0x%x\n", pRsp->matchFid);
    return 0;
}

int32_t fpTaCmdProcessor::processEndAuthenticateRsp(int32_t *isTemplateUpdated)
{
    fp_rsp_end_authenticate_t *pRsp = (fp_rsp_end_authenticate_t *) pRspBuffer;
    *isTemplateUpdated = pRsp->update_flag;
    LOGD(FPTAG"Update flag:%d \n", pRsp->update_flag);

    identify_result_t& identify_result = get_cur_identify_result();
    identify_result.score = pRsp->match_score;
    identify_result.template_id = pRsp->match_fid;
    identify_result.updated_template_size = pRsp->match_tplt_len;

    return 0;
}

int32_t fpTaCmdProcessor::processPreEnrollRsp(uint64_t *pChanllge)
{
    fp_rsp_pre_enroll_t *pRsp = (fp_rsp_pre_enroll_t *)pRspBuffer;
    *pChanllge = pRsp->changlleID;
    return 0;
}

int32_t fpTaCmdProcessor::processGetAuthenticatorIdRsp(uint64_t *TplDbId)
{
    fp_rsp_get_database_id_t *pRsp = (fp_rsp_get_database_id_t *)pRspBuffer;
    *TplDbId = pRsp->TplDataBaseID;
    return 0;
}

int32_t fpTaCmdProcessor::processGetEnrolledFidsRsp(int32_t *pEnrolledFids, int32_t iArrayCap, int32_t *pRealCnt)
{
    fp_rsp_get_enrol_fids_t *pRsp = (fp_rsp_get_enrol_fids_t *) pRspBuffer;
    LOGD(FPTAG"iArrayCap:%d", iArrayCap);
    if (iArrayCap > FINGER_MAX_COUNT)
    {
        LOGD(FPTAG"WARNING: iArrayCap:%d exceed FINGER_MAX_COUNT:%d", iArrayCap, FINGER_MAX_COUNT);
    }
    memcpy(pEnrolledFids, pRsp->Fids, sizeof(int32_t)*FINGER_MAX_COUNT);
    *pRealCnt = pRsp->FidCnt;

    LOGD(FPTAG"get fids cnt:%d--> fid[5] = %d, %d, %d, %d, %d",
         *pRealCnt, pEnrolledFids[0], pEnrolledFids[1], pEnrolledFids[2], pEnrolledFids[3],
         pEnrolledFids[4]);

    return 0;
}

int32_t fpTaCmdProcessor::processGetRelCoordsRsp(int32_t *pDeltaX, int32_t *pDeltaY, int32_t *pRet)
{
    fp_rsp_rel_coords_t *pRsp = (fp_rsp_rel_coords_t *)pRspBuffer;
    *pRet    = pRsp->GetRelCoordsRes;
    *pDeltaX = pRsp->DeltaX;
    *pDeltaY = pRsp->DeltaY;

    // LOGD(FPTAG"get relative coordinates X:%d, Y:%d.", pRsp->DeltaX, pRsp->DeltaY);
    return 0;
}
//--------capture tool relative ---------------------------------------------
int32_t fpTaCmdProcessor::processGetFingerRectCntRsp(int32_t *p_rect_cnt)
{
    fp_rsp_get_sub_rect_cnt_t *pRsp = (fp_rsp_get_sub_rect_cnt_t *) pRspBuffer;
    *p_rect_cnt = pRsp->Count;

    LOGD(FPTAG"processGetFingerRectCntRsp,rect_cnt=%d", pRsp->Count);
    return 0;
}

int32_t fpTaCmdProcessor::processGetImageQualityRsp(int32_t *p_area, int32_t *p_condition, int32_t *p_quality)
{
    fp_rsp_get_image_quality_t *pRsp = (fp_rsp_get_image_quality_t *) pRspBuffer;
    //LOGD(FPTAG"fp_rsp_get_image_quality_t,rect_cnt=%d",pRsp->Count);
    *p_area      = pRsp->Area;
    *p_condition = pRsp->Condition;
    *p_quality   = pRsp->Quality;

    return 0;
}

int32_t fpTaCmdProcessor::processReadRegRsp(int32_t *value)
{
    fp_rsp_read_reg_t *pRsp = (fp_rsp_read_reg_t *) pRspBuffer;
    *value = pRsp->reg;
    LOGD(FPTAG"read 0x1c,value=0x%x", *value);
    return 0;
}
int32_t fpTaCmdProcessor::processGetFingerRectRsp(int32_t *p_rect)
{
    fp_rsp_get_sub_rect_t *pRsp = (fp_rsp_get_sub_rect_t *) pRspBuffer;
    memcpy(p_rect, &(pRsp->Rect), 12 * sizeof(int32_t));

    LOGD(FPTAG"processGetFingerRectRsp the first 4 words is(HEX): 0x%8x, 0x%8x, 0x%8x, 0x%8x",
         p_rect[0], p_rect[1], p_rect[2], p_rect[3]);
    return 0;
}

int32_t fpTaCmdProcessor::processGetTemplateIdsRsp(int32_t *p_real_size, int32_t *p_ids)
{
    fp_rsp_get_template_ids_t *pRsp = (fp_rsp_get_template_ids_t *) pRspBuffer;
    *p_real_size = pRsp->cntTemplate;
    memcpy(p_ids, &(pRsp->Ids[0]), 62 * sizeof(int32_t));

    LOGD(FPTAG"processGetTemplateIdsRsp id size: %d-> ids:%d, %d, %d, %d, %d.....",
         *p_real_size, p_ids[0], p_ids[1], p_ids[2], p_ids[3], p_ids[4]);
    return 0;
}

int32_t fpTaCmdProcessor::processGetImgFormatRsp(int32_t *p_width, int32_t *p_height)
{
    fp_rsp_get_image_dimention_t *pRsp = (fp_rsp_get_image_dimention_t *) pRspBuffer;
    *p_width = pRsp->Width;
    *p_height = pRsp->Height;

    LOGD(FPTAG"processGetImgFormatRsp Width: %d, Height: %d",
         *p_width, *p_height);
    return 0;
}

int32_t fpTaCmdProcessor::processGetVersionRsp(char *ta_ver, char *driver_ver, char *algo_ver)
{
    fp_rsp_get_version_t *pRsp = (fp_rsp_get_version_t *) pRspBuffer;
    LOGI(FPTAG"processGetVersionRsp CA-Version: %s", FPDAEMON_VERSION);
    LOGI(FPTAG"processGetVersionRsp TA-Version: %s", pRsp->taVersion);
    LOGI(FPTAG"processGetVersionRsp Dr-Version: %s", pRsp->driverVersion);
    LOGI(FPTAG"processGetVersionRsp algo-Version: %s", pRsp->algoVersion);
       if(ta_ver)
       {
               strcpy(ta_ver, pRsp->taVersion);
       }
       if(driver_ver)
       {
               strcpy(driver_ver, pRsp->driverVersion);
       }
       if(algo_ver)
       {
               strcpy(algo_ver, pRsp->algoVersion);
       }

    return 0;
}

int32_t fpTaCmdProcessor::process_get_tee_info_rsp(uint32_t *p_revision, uint32_t *p_hw_id)
{
    fp_rsp_tee_info_t *pRsp = (fp_rsp_tee_info_t *) pRspBuffer;
    *p_revision = pRsp->sensor_revision;
    *p_hw_id = pRsp->sensor_hw_id;
    LOGI(FPTAG"FEATURE_TEE_STORAGE  : %d", pRsp->feature_tee_storage);
    LOGI(FPTAG"FEATURE_SEC_PAY      : %d", pRsp->feature_sec_pay);
    LOGI(FPTAG"FEATURE_HW_AUTH      : %d", pRsp->feature_hw_auth);
    LOGI(FPTAG"ALGO NAME            : %s", pRsp->alg_name);
    LOGI(FPTAG"CHIP REVISION        : 0x%x", pRsp->sensor_revision);
    LOGI(FPTAG"CHIP HARDWARE_ID     : 0x%x", pRsp->sensor_hw_id);

    return 0;
}

//--------------------------------------------------------

int32_t fpTaCmdProcessor::getRsp(char *pBuffer, int32_t iLen)
{
    if (0 != checkAndAllocRspBuffer(iLen))
    {
        return -EIO;
    }

    memcpy(pRspBuffer, pBuffer, iLen);
    mRspLen = iLen;
    return 0;
}

int32_t fpTaCmdProcessor::checkAndAllocRspBuffer(int32_t iLen)
{
    if (pRspBuffer == NULL)
    {
        pRspBuffer = (unsigned char *)fp_malloc(iLen);
        if (pRspBuffer == NULL)
        {
            return -ENOMEM;
        }

        mRealRspBufferLen = iLen;
        return 0;
    }

    if (mRealRspBufferLen < iLen)
    {
        fp_free(pRspBuffer);

        pRspBuffer = (unsigned char *)fp_malloc(iLen);
        if (pRspBuffer == NULL)
        {
            return -ENOMEM;
        }

        mRealRspBufferLen = iLen;
    }
    return 0;
}

