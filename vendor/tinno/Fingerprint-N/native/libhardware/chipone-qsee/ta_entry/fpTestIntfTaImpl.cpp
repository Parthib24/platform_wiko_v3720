#include <unistd.h>
#include "fpTestIntfTaImpl.h"
#include "fpTa_entryproxy.h"
#include "fpTa_commandprocessor.h"
#include "fpGpioHal.h"
#include "fp_common.h"
#include "fp_tee_types.h"


#define FPTAG "fpTestIntfTaImpl.cpp "

fpTestIntfTaImpl::fpTestIntfTaImpl() : fpTestIntf()
{

}

fpTestIntfTaImpl::~fpTestIntfTaImpl()
{

}
int fpTestIntfTaImpl::do_start_enroll_cmd()
{

    LOGD(FPTAG"do_start_enroll_cmd");
    int Ret = 0;


    if (mpfpTaEntryProxy->pCmdProcessor->constructNoPayloadCmd(TCI_FP_CMD_START_ENROLL) == 0)
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() == 0)
        {
        }
    }

    return Ret;
}

int fpTestIntfTaImpl::do_finish_enroll_cmd()
{
    LOGD(FPTAG"do_finish_enroll_cmd  invoked");

    int Ret = -ENOENT;


    if (mpfpTaEntryProxy->pCmdProcessor->constructNoPayloadCmd(TCI_FP_CMD_FINISH_ENROLL) == 0)
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() == 0)
        {
            Ret = 0;
        }
    }


    return Ret;
}
int fpTestIntfTaImpl::do_enroll_image_cmd()
{
    LOGD(FPTAG"do_enroll_image_cmd invoked");

    int Ret = -ENOENT;
    int LastProgress = 0;
    int TotalEnrollCnt = 0;
    int CurEnrollCnt = 0;
    int EnrollFailReason = 0;
    int FillPart = 0;



    if (mpfpTaEntryProxy->pCmdProcessor->constructNoPayloadCmd(TCI_FP_CMD_ENROLL_IMG) == 0)
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() == 0)
        {
            if (mpfpTaEntryProxy->pCmdProcessor->processEnrollImgRsp(&LastProgress, &TotalEnrollCnt,
                                                                     &CurEnrollCnt, &EnrollFailReason, &FillPart) == 0)
            {
                Ret = 0;
            }
        }
    }
    if (0 != Ret)
    {
        LOGD(FPTAG"do_enroll_image_cmd ERROR!!");
    }

    return Ret;

}
int fpTestIntfTaImpl::do_get_finger_rect_cnt_cmd(int *pRectCnt)
{
    LOGD(FPTAG"do_get_finger_rect_cnt_cmd invoked");

    int Ret = -ENOENT;


    if (mpfpTaEntryProxy->pCmdProcessor->constructNoPayloadCmd(TCI_FP_CMD_GET_SUBRECT_COUNT) == 0)
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() == 0)
        {
            if (mpfpTaEntryProxy->pCmdProcessor->processGetFingerRectCntRsp(pRectCnt) == 0)
            {
                Ret = 0;
            }
        }
    }
    if (0 != Ret)
    {
        LOGD(FPTAG"do_get_finger_rect_cnt_cmd ERROR!!");
    }

    return Ret;

}
int fpTestIntfTaImpl::do_get_size_cmd(int *pW, int *pH)
{
    LOGD(FPTAG"do_get_size_cmd invoked");
    int Ret = mpfpTaEntryProxy->get_image_size(pW,pH);
    return Ret;
}

int fpTestIntfTaImpl::do_set_property_cmd(int iTag, int iValue)
{
    LOGD(FPTAG"do_set_property_cmd invoked tag:%d, value:%d", iTag, iValue);
    int Ret = mpfpTaEntryProxy->hal_configuration(iTag,iValue);
    return Ret;
}

int fpTestIntfTaImpl::do_get_image_quality_cmd(int *p1, int *p2 , int *p3)
{
    LOGD(FPTAG"do_get_image_quality_cmd invoked");
    return mpfpTaEntryProxy->get_image_quality(p1,p2,p3);
}

int fpTestIntfTaImpl::do_get_templateIds_cmd(int *pIdsArray, int *pIdsArrayLen)
{
    LOGD(FPTAG"do_get_template_ids_cmd invoked");

    int Ret = -ENOENT;


    if (mpfpTaEntryProxy->pCmdProcessor->constructNoPayloadCmd(TCI_FP_CMD_GET_TEMPLATE_IDS) == 0)
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() == 0)
        {
            if (mpfpTaEntryProxy->pCmdProcessor->processGetTemplateIdsRsp(pIdsArrayLen, pIdsArray) == 0)
            {
                Ret = 0;
            }
        }
    }
    if (0 != Ret)
    {
        LOGE(FPTAG"do_get_template_ids_cmd ERROR!!");
    }


    return Ret;

}
int fpTestIntfTaImpl::do_get_finger_rect_cmd(int idx, int *pRectData)
{
    LOGD(FPTAG"do_get_finger_rect_cmd invoked");

    int Ret = -ENOENT;


    if (mpfpTaEntryProxy->pCmdProcessor->constructGetFingerRectCmd(idx) == 0)
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() == 0)
        {
            if (mpfpTaEntryProxy->pCmdProcessor->processGetFingerRectRsp(pRectData) == 0)
            {
                Ret = 0;
            }
        }
    }
    if (0 != Ret)
    {
        LOGE(FPTAG"do_get_finger_rect_cmd ERROR!!");
    }


    return Ret;

}

int fpTestIntfTaImpl::captureImgWaitFingerLost()
{
    int Ret = 0;
    int cnt = 0;//debug

    LOGD(FPTAG"captureImgWaitFingerLost:");
wait_for_finger_up:
    cnt = (++cnt > 10000) ? 0 : cnt;//debug
    if (0 == cnt % 100)
    {
        LOGD(FPTAG"capture_img wait finger lost!!!!!!!!!!!!!!!!-->%d", cnt);
    }
    mpfpTaEntryProxy->pGpioHal->DisableSPICLK();
    mpfpTaEntryProxy->pGpioHal->ChipGpioReset();
    mpfpTaEntryProxy->pGpioHal->EnableSPICLK();

    if (0 == mpfpTaEntryProxy->pCmdProcessor->constructNoPayloadCmd(TCI_FP_CMD_WAKEUP))
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() != 0)
        {
            Ret = -ENOENT;
            return Ret;
        }
        LOGD(FPTAG"wake up sensor OK!");
    }


    if (mpfpTaEntryProxy->pCmdProcessor->constructNoPayloadCmd(TCI_FP_CMD_CHECK_FINGER_PRESENT) == 0)
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() != 0)
        {
            LOGE(FPTAG"wait_for_finger_up  send ta cmd error: TCI_FP_CMD_CHECK_FINGER_PRESENT");
            Ret = -ENOENT;
        }

        if (mpfpTaEntryProxy->pCmdProcessor->processCheckFingerPresentRsp(CHECK_FINGER_UP) == 0)
        {
            LOGD(FPTAG"capture_img wait finger up OK!");
        }
        else
        {
            if (mpfpTaEntryProxy->get_stop_status())   //wait finger up be canceled
            {
                LOGD(FPTAG"wait finger lost flow is interrupted by upper app!!");
                Ret = -FP_ERROR_USER_CANCEL;
            }
            else
            {
                usleep(10 * 1000);
                goto wait_for_finger_up;
            }
        }
    }
    else
    {
        LOGE(FPTAG"captureImgWaitFingerLost construct cmd error: TCI_FP_CMD_CHECK_FINGER_PRESENT");
        Ret = -ENOENT;
    }

    return Ret;

}

int fpTestIntfTaImpl::captureImgWaitFingerDown()
{
    int Ret = 0;

    LOGD(FPTAG"CaptureWaitFingerDown:");

wait_for_finger_dwon:
    mpfpTaEntryProxy->pGpioHal->DisableSPICLK();
    mpfpTaEntryProxy->pGpioHal->ChipGpioReset();
    mpfpTaEntryProxy->pGpioHal->EnableSPICLK();
    if (mpfpTaEntryProxy->pCmdProcessor->constructNoPayloadCmd(TCI_FP_CMD_WAIT_FINGER_DOWN) == 0)
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() == 0)
        {
            if (mpfpTaEntryProxy->pGpioHal->WaitForGpioIrq() != 0)
            {
                LOGD(FPTAG"wait_for_finger_dwon be stopped!!!!!!!!!!!!!!!!!!!!!!");
                Ret = -FP_ERROR_USER_CANCEL;
                goto out;
            }
        }
        else
        {
            LOGE(FPTAG"send TCI_FP_CMD_WAIT_FINGER_DOWN error!");
            Ret = -ENOENT;
            goto out;
        }
    }
    else
    {
        LOGE(FPTAG"construct TCI_FP_CMD_WAIT_FINGER_DOWN error!");
        Ret = -ENOENT;
        goto out;

    }
    usleep(1000);//delay 5ms for debounce
    //check_finger_present_status:
    // avoid finger leave
    LOGD(FPTAG"check finger present status:");
    if (mpfpTaEntryProxy->pCmdProcessor->constructNoPayloadCmd(TCI_FP_CMD_CHECK_FINGER_PRESENT) == 0)
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() != 0)
        {
            LOGE(FPTAG"wait_for_finger_dwon send ta cmd error: TCI_FP_CMD_CHECK_FINGER_PRESENT");
            Ret = -ENOENT;
            goto out;

        }
        if (mpfpTaEntryProxy->pCmdProcessor->processCheckFingerPresentRsp(CHECK_FINGER_DOWN) == 0)
        {
            LOGD(FPTAG"wait_for_finger_dwon OK!");
        }
        else
        {
            goto wait_for_finger_dwon;
        }
    }
    else
    {
        LOGE(FPTAG"captureImgWaitFingerDown construct cmd error: TCI_FP_CMD_CHECK_FINGER_PRESENT");
        Ret = -ENOENT;
        goto out;
    }

out:

    return Ret;
}

int fpTestIntfTaImpl::captureImgExecution(int *pLen, char *pData)
{
    int Ret = 0;
    //*p_len = 0;
    int width = 0;
    int height = 0;

    LOGD(FPTAG"CaptureImageExecution:");
    if (mpfpTaEntryProxy->pCmdProcessor->constructOneParaCmd(TCI_FP_CMD_CAPTURE_IMAGE, 1) == 0)
    {
        if (mpfpTaEntryProxy->pCmdProcessor->sendCmd() != 0)
        {
            LOGE(FPTAG"captureImgExecution send ta cmd error: TCI_FP_CMD_CAPTURE_IMAGE");
            Ret = -ENOENT;
            goto out;
        }
        if (mpfpTaEntryProxy->pCmdProcessor->processCaptureImgToolRsp(&width, &height, pData) != 0)
        {
            Ret = -EIO;
            LOGE(FPTAG"processCaptureImgRsp: width:%d, height:%d, len:%d",  width, height, *pLen);
        }
        else
        {
            *pLen = width * height;
            Ret = 0;
            LOGD(FPTAG"captureImgExecution OK!:width:%d, height:%d, len:%d", width, height, *pLen);
        }
    }
    else
    {
        LOGE(FPTAG"construct TCI_FP_CMD_CAPTURE_IMAGE error!");
        Ret = -ENOENT;
        goto out;
    }
out:

    return Ret;
}


int fpTestIntfTaImpl::capture_image_func(int iMode, fp_capture_image_data_t *pImageData )
{
    int32_t CaptyreStyle = 0;
    if(iMode == 0) 
        CaptyreStyle = CAPTURE_STYLE_WAIT_UP | CAPTURE_STYLE_WAIT_DOWN;
    else
        CaptyreStyle = CAPTURE_STYLE_WAIT_DOWN;

    LOGD(FPTAG"capture_image_func invoked capturestyle:%d", CaptyreStyle);

    int status = 0;

    if (CAPTURE_STYLE_WAIT_UP & CaptyreStyle)
    {
        status = captureImgWaitFingerLost();
        if (status)
        {
            goto out;
        }
    }

    status = captureImgWaitFingerDown();
    if (status)
    {
        goto out;
    }

    status = captureImgExecution(&pImageData->image_length, pImageData->image_data);
    pImageData->capture_result = status;
out:
    LOGD(FPTAG"capture_image_func res:%d, image len:%d", status, pImageData->image_length);


    return status;
}


int fpTestIntfTaImpl::finger_detect_func(int32_t dummy,int32_t *result)
{
    LOGD(FPTAG"finger_detect_func invoked ");
    *result = mpfpTaEntryProxy->finger_detect_test();

out:
    LOGD(FPTAG"finger_detect_func res:%d", *result);
    return *result;
}


int fpTestIntfTaImpl::do_tool_control_cmd(int p0, int p1)
{
    LOGD(FPTAG"do_tool_control_cmd invoked p0=%d, p1=%d", p0, p1);

    return mpfpTaEntryProxy->service_control(p0, p1);
}

int fpTestIntfTaImpl::do_selftest_cmd(int *pResult )
{
    LOGD(FPTAG"doSelftestCmd invoked");
    int Ret = mpfpTaEntryProxy->self_test();
    *pResult = Ret;
    return Ret;
}
int fpTestIntfTaImpl::do_checkboard_cmd(int *pResult)
{
    LOGD(FPTAG"do_check_board_cmd invoked");
    *pResult = mpfpTaEntryProxy->check_board();
    return *pResult;
}

void fpTestIntfTaImpl::setTaProxy(fpTaEntryProxy *pfpTaEntryProxy)
{
    mpfpTaEntryProxy = pfpTaEntryProxy;
}


