#include "fpTa_entryproxy.h"
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include "fpTa_commandprocessor.h"
#include <stdio.h>
#include "fpGpioHal.h"
#include "fp_nav.h"
#include <time.h>
#include <sys/stat.h>
#include "fp_input.h"
#include "fpTestIntfTaImpl.h"
#include "fp_tac_impl.h"
#include "fp_common.h"
#include "fp_ta_entry.h"

#define FPTAG "fpTa_entryproxy.cpp "

#define CA_RELEASE_VERSION      "v1.2.4"

fpTaProxy *create_ta_proxy(fpTacImpl *tac_impl)
{
    return new fpTaEntryProxy(tac_impl);
}


fpTaEntryProxy::fpTaEntryProxy(fpTacImpl *pTacImpl) : fpTaProxy(pTacImpl)
{
    LOGD(FPTAG"fpTaEntryProxy constructor invoked");
    int32_t result = 0;

    pCmdProcessor = new fpTaCmdProcessor();
    pGpioHal = new fpGpioHal(this);
#ifdef FP_TEE_QSEE4
    // for Qualcom QSEE platform, MUST send IOCTL init CMD first, otherwise sensor read_hw id would fail
    if (0 != pGpioHal->IOCtrlInit())
    {
        LOGE(FPTAG" IOCtrlInit Error!!!!!!!!");
        pGpioHal->Remove();
        fp_initilized = false;
        return;
    }
#endif
    if (0 == taOpen())
    {
#ifdef FP_TEE_QSEE4
        // QSEE NEED SEND AUTH TOKEN KEY
        if (qsee_set_auth_token_key())
        {
            LOGE(FPTAG"qsee set auth token key error");
            fp_initilized = false;
            pGpioHal->Remove();
            return;
        }
#endif
        result = pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_INIT);

        if (0 != result)
        {
            LOGE(FPTAG"send TCI_FP_CMD_INIT Error:%d!!!!!!!!", result);
            fp_initilized = false;
            pGpioHal->Remove();
            return;
        }
        else
        {
            fp_initilized = true;
            pGpioHal->SetDevInfo();
            char ta_ver[32];
            if(pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_GET_VERSION) == 0)
            {
                pCmdProcessor->processGetVersionRsp(ta_ver, NULL, NULL);
            }
                       
            pGpioHal->SetVerInfo(CA_RELEASE_VERSION, ta_ver);


            if (fp_global_env.fp_internal_callback.on_fp_init)
            {
                fp_global_env.fp_internal_callback.on_fp_init(this);
            }
        }
    }
    else
    {
        fp_initilized = false;
        pGpioHal->Remove();
        return;
    }
#ifndef FP_TEE_QSEE4
    if (0 != pGpioHal->IOCtrlInit())
    {
        LOGE(FPTAG" IOCtrlInit Error!!!!!!!!");
        fp_initilized = false;
        return;
    }
#endif
    //TemplatesFileBuf = (char *)fp_malloc(_FP_DB_GROUP_TEMPLATE_SIZE + _FP_DB_MARGIN);

    mChallenge = 0;
    mAuthenticator_id = 0;
    chip_revision = 0;


}

fpTaEntryProxy::~fpTaEntryProxy()
{
    LOGD(FPTAG"fpTaEntryProxy destructor invoked");

    pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_DEINIT);

    delete pCmdProcessor;
    pCmdProcessor = NULL;

    taClose();
    //fp_free(TemplatesFileBuf);

    delete pGpioHal;
    pGpioHal = NULL;
};

uint64_t fpTaEntryProxy::open_hal()
{
    LOGD(FPTAG" open_hal invoked");
    if (!fp_initilized)
    {
        LOGD("open_hal but initilized is false");
        return 0;
    }

    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_GET_VERSION))
    {
        pCmdProcessor->processGetVersionRsp(NULL, NULL, NULL);
    }

    return 0x77777777;
}

int32_t fpTaEntryProxy::close_hal()
{
    LOGD(FPTAG" close_hal invoked");
    if (fp_global_env.fp_internal_callback.on_fp_deinit)
    {
        fp_global_env.fp_internal_callback.on_fp_deinit();
    }
    return 0;
}

int32_t fpTaEntryProxy::finger_up_wait_irq(void)
{
    int32_t ret = 0;

    LOGD(FPTAG"finger_up_wait_irq:");

    while (1)
    {
        pGpioHal->DisableSPICLK();
        pGpioHal->ChipGpioReset();
        pGpioHal->EnableSPICLK();

        ret = pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_WAIT_FINGER_UP);
        if (ret)
        {
            goto out;
        }

        ret = pGpioHal->WaitForGpioIrq();
        if (ret)
        {
            ret = -FP_ERROR_USER_CANCEL;
            goto out;
        }

        ret = pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_CHECK_FINGER_PRESENT);
        if (ret)
        {
            goto out;
        }

        ret = pCmdProcessor->processCheckFingerPresentRsp(CHECK_FINGER_UP);
        if (0 == ret)
        {
            LOGD(FPTAG"wait for finger up irq OK!!!");
            break;
        }

    }


out:
    LOGD(FPTAG"finger_up_wait_irq result:%d", ret);
    return ret;
}

int32_t fpTaEntryProxy::CaptureWaitFingerUp(void)
{
    int32_t Ret = 0;
    int32_t cnt = 0;//debug
    // LOGD(FPTAG"CaptureWaitFingerUp:");
    if (((0x7153 == chip_hardware_id) && (chip_revision >= 0x17)))
        // ||((0x7230 == chip_hardware_id) && (chip_revision > 0x1)))
    {
        Ret = finger_up_wait_irq();
        goto out;
    }


    while (1)
    {
        pGpioHal->DisableSPICLK();
        pGpioHal->ChipGpioReset();
        pGpioHal->EnableSPICLK();
        if (get_stop_status())
        {
            LOGI(FPTAG"wait finger up flow is stopped by upper!!");
            Ret = -FP_ERROR_USER_CANCEL;
            goto out;
        }

        if (pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_WAKEUP) != 0)
        {
            Ret = -ENOENT;
            goto out;
        }
        LOGD(FPTAG"wake up sensor OK!");

        if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_CHECK_FINGER_PRESENT))
        {
            if (0 == pCmdProcessor->processCheckFingerPresentRsp(CHECK_FINGER_UP))
            {
                LOGD(FPTAG"capture_img wait finger up OK!");
                Ret = 0;
                goto out;
            }
            else
            {
                cnt = (++cnt > 1000) ? 0 : cnt;//debug
                if (cnt % 100 == 0)
                {
                    LOGD(FPTAG"capture_img wait finger up again!!!!!!!!!!!!!!!!-->%d", cnt);
                }

            }
        }
        else
        {
            LOGE(FPTAG"wait_for_finger_up  send ta cmd error: TCI_FP_CMD_CHECK_FINGER_PRESENT");
            Ret = -ENOENT;
            goto out;
        }
        usleep(10 * 1000);//delay 10ms
    }

out:

    LOGD(FPTAG"CaptureWaitFingerUp result:%d", Ret);
    return Ret;
}


int32_t fpTaEntryProxy::CaptureWaitFingerDown(int32_t capture_style)
{
    int32_t Ret = 0;
    int32_t reg_0x1c = 0;
    int32_t exception_flag = 0;
    LOGD(FPTAG"CaptureWaitFingerDown:");

wait_for_finger_dwon:
    exception_flag = 0;
    pGpioHal->DisableSPICLK();
    pGpioHal->ChipGpioReset();
    pGpioHal->EnableSPICLK();
    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_WAIT_FINGER_DOWN))
    {
        if (0 != pGpioHal->WaitForGpioIrq())
        {
            // LOGD(FPTAG"wait_for_finger_dwon be stopped!");
            Ret = -FP_ERROR_USER_CANCEL;
            goto out;
        }
        if (0 == pCmdProcessor->send_cmd_one_par(TCI_FP_CMD_READ_REG, 0x1c))
        {
            pCmdProcessor->processReadRegRsp(&reg_0x1c);
            //if ((0x7f == reg_0x1c) || (0xff == reg_0x1c) || (0xfe == reg_0x1c) || (0x00 == reg_0x1c))
            if ((0x1 != reg_0x1c) && (0x81 != reg_0x1c))
            {
                exception_flag = 1;
                identify_result_t &identify_result = get_cur_identify_result();
                identify_result.esd_exception++;
                LOGE(FPTAG"INT EXCEPTION occurred!");
                //here just dump all regs
                // if(pCmdProcessor->constructNoPayloadCmd(TCI_FP_CMD_WAKEUP)){
                //     if(pCmdProcessor->sendCmd() != RET_OK){
                //         Ret = -ENOENT;
                //         goto out;
                //     }
                //     LOGD(FPTAG"exception 0 wake up sensor OK! here just dump all regs!");
                // }
            }
        }

    }
    else
    {
        LOGE(FPTAG"send TCI_FP_CMD_WAIT_FINGER_DOWN error!");
        Ret = -ENOENT;
        goto out;

    }
    usleep(5 * 1000);//delay 5ms for debounce
//check_finger_present_status:
// avoid finger leave
//    LOGD(FPTAG"check finger present status:");
    if (1 == exception_flag)
    {
        pGpioHal->DisableSPICLK();
        pGpioHal->ChipGpioReset();
        pGpioHal->EnableSPICLK();
        if (0 != pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_WAKEUP))
        {
            Ret = -ENOENT;
            goto out;
        }
        LOGD(FPTAG"exception wake up sensor OK!")   ;
    }
    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_CHECK_FINGER_PRESENT))
    {

        if (0 == pCmdProcessor->processCheckFingerPresentRsp(CHECK_FINGER_DOWN))
        {
            LOGD(FPTAG"wait_for_finger_dwon OK!");
        }
        else
        {
            if (capture_style & CAPTURE_STYLE_WAIT_DOWN)
            {
                goto wait_for_finger_dwon;
            }
            else
            {
                return -MSG_TYPE_FINGER_MOVE;
            }
        }
    }
    else
    {
        LOGE(FPTAG"wait_for_finger_dwon send cmd error: TCI_FP_CMD_CHECK_FINGER_PRESENT");
        Ret = -ENOENT;
        goto out;
    }

out:

    return Ret;
}

int32_t fpTaEntryProxy::NavWaitFingerDown(void)
{
    int32_t Ret = -ENOENT;
    int32_t reg_0x1c = 0;
nav_wait_for_finger_dwon:

    LOGD(FPTAG"Navigation WaitFingerDown:");

    pGpioHal->DisableSPICLK();
    pGpioHal->ChipGpioReset();
    pGpioHal->EnableSPICLK();

    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_WAIT_FINGER_DOWN))
    {
        if (0 != pGpioHal->WaitForGpioIrq())
        {
            // LOGE(FPTAG"nav_wait_for_finger_dwon be stopped!");
            Ret = -FP_ERROR_USER_CANCEL;
            goto out;//nav_wait_gpio_be_stoped;
        }
        else
        {
            Ret = 0;//wait gpio ok
        }
        if (0 == pCmdProcessor->send_cmd_one_par(TCI_FP_CMD_READ_REG, 0x1c))
        {
            pCmdProcessor->processReadRegRsp(&reg_0x1c);
            //if ((0x7f == reg_0x1c) || (0xff == reg_0x1c) || (0xfe == reg_0x1c) || (0x00 == reg_0x1c))
            if ((0x1 != reg_0x1c) && (0x81 != reg_0x1c))
            {
                identify_result_t &identify_result = get_cur_identify_result();
                identify_result.esd_exception++;
                LOGE(FPTAG"INT EXCEPTION occurred!");
                pGpioHal->DisableSPICLK();
                pGpioHal->ChipGpioReset();
                pGpioHal->EnableSPICLK();
                if (0 != pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_WAKEUP))
                {
                    Ret = -ENOENT;
                    goto out;
                }
                LOGD(FPTAG"exception wake up sensor OK!")   ;
            }
        }
    }
    else
    {
        LOGE(FPTAG"send TCI_FP_CMD_WAIT_FINGER_DOWN error!");
        Ret = -ENOENT;
        goto out;
    }
    usleep(5 * 1000); //delay 5ms for debounce
//check_finger_present_status:
// avoid finger leave
    // LOGD(FPTAG"check finger present status:");
    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_CHECK_FINGER_PRESENT))
    {
        if (0 == pCmdProcessor->processCheckFingerPresentRsp(CHECK_NAV_DOWN))
        {
            LOGD(FPTAG"nav_wait_for_finger_dwon OK!");
        }
        else
        {
            goto nav_wait_for_finger_dwon;
        }

    }
    else
    {
        LOGE(FPTAG"nav_wait_for_finger_dwon send cmd error: TCI_FP_CMD_CHECK_FINGER_PRESENT");
        Ret = -ENOENT;
        goto out;
    }

out:
    return Ret;
}

int32_t fpTaEntryProxy::CaptureImageExecution(void)
{
    int32_t Ret = 0;
    int32_t capture_mode = 0;
    LOGD(FPTAG"CaptureImageExecution:");
    struct timeval tvStart = ca_timer_start("CaptureImageExecution begin");

    if (get_fp_config_feature_store_captured_img())
    {
#ifdef DEBUG_ENABLE
        capture_mode = 1;
#endif
    }

    if (0 == pCmdProcessor->send_cmd_one_par(TCI_FP_CMD_CAPTURE_IMAGE,
                                             capture_mode))  //hare para 0 means do not return the captureImage back to CA
    {
        int32_t CaptureRes = 0;
        ca_timer_end("KPI capture image end", tvStart);
        Ret = pCmdProcessor->processCaptureImgRsp();
        if (0 != Ret)
        {
            LOGE(FPTAG"processCaptureImgRsp Error result: %d!", CaptureRes);
        }
    }
    else
    {
        LOGE(FPTAG"send TCI_FP_CMD_CAPTURE_IMAGE error!");
        Ret = -ENOENT;
    }


    return Ret;
}

int32_t fpTaEntryProxy::set_stop_status(bool newStatus)
{
    LOGD(FPTAG"set_stop_status:%d", newStatus);

    pGpioHal->LockPollMutex();
    fpTaProxy::set_stop_status(newStatus);

    if (newStatus)
    {
        pGpioHal->CancelWaitFingerDown();
    }
    else
    {
        pGpioHal->CancelClear();
    }
    pGpioHal->UnLockPollMutex();

    return 0;
}

int32_t fpTaEntryProxy::read_file(const char *path, char **p_buff, size_t *file_len)
{
    FILE *file = NULL;
    char path_bak[PATH_MAX] = {0};
    struct stat file_info = {0};
    file_info.st_size = 0;
    int32_t ret = 0;
    int32_t path_len = 0;

    file = fopen(path, "rb");

    if (NULL == file) //the file didn't exist, so just create it.
    {
        path_len = sprintf(path_bak, "%s.bak", path);
        LOGD(FPTAG"%s did not exist, open bak file:%s, len:%d\n", path, path_bak, path_len);
        file = fopen(path_bak, "rb");

        if (NULL != file)
        {
            LOGD(FPTAG"open bak file:%s OK!, rename it to %s\n", path_bak, path);
            fclose(file);
            rename(path_bak, path);
            file = fopen(path, "rb");
        }
        else
        {
            file = fopen(path, "wb+");
            if (NULL == file)
            {
                LOGE(FPTAG"error: %s creat file failed!", path);
                return -EINVAL;
            }
            LOGD(FPTAG"the file didn't exist, so just create it.");
        }
    }

    if (stat(path, &file_info))
    {
        LOGE("%s stat failed with error %s", __func__, strerror(errno));
        ret = -ENOENT;
        goto out;
    }
    LOGD(FPTAG"Template size:%d", (int32_t)file_info.st_size);

    *file_len = file_info.st_size;

    if (0 == file_info.st_size)
    {
        LOGD("WARNINGG: first creat template file, can read nothing to TA!!!");
        ret = 0;
        goto out;
    }
    else
    {

        *p_buff = (char *)fp_malloc(file_info.st_size);
        if (NULL == *p_buff)
        {
            LOGE("malloc error!!!");
            ret = -ENOMEM;
            goto out;
        }

        ret = fread(*p_buff, file_info.st_size, 1, file);

        LOGD(FPTAG"ret= %d", ret);
        if (1 != ret)
        {
            LOGE(FPTAG"%s is empty file, can't read anything!", path);
        }
        ret = 0;
        //LOGD(FPTAG"TemplatesFileBuf first 4 bytes is(HEX):  0x%x  ",(int32_t *)*p_buff);
    }

out:
    fclose(file);
    return ret;
//---------------------------------------------------
}

#ifdef FEATURE_TEE_STORAGE
int32_t fpTaEntryProxy::load_template(void)
{
    int32_t ret = 0;
    char *buff_dummy = NULL;

    if (0 == pCmdProcessor->construct_load_template_cmd((char *)&buff_dummy[0],
                                                        0, 0))
    {
        ret = pCmdProcessor->sendCmd();
    }
    else
    {
        ret = -ENOENT;
    }
    LOGD(FPTAG"load_template ret:%d", ret);

    return ret;
}
#else
int32_t fpTaEntryProxy::load_template(void)
{
    size_t remaining = 0;
    size_t file_offset = 0;
    uint32_t remain_rsp = 0;
    int32_t ret = 0;
    char *buff = NULL;
    size_t file_size = 0;

    ret = read_file(TemplatePath, &buff, &file_size);
    if (ret)
    {
        goto out;
    }

    remaining = file_size;

    while (remaining)
    {
        size_t chunk_size = MIN(MAX_LOAD_CHUNK, remaining);

        LOGD(FPTAG"file size:%zu, chunk_size:%zu, remaining:%zu, offset:%zu", file_size, chunk_size,
             remaining,
             file_offset);
        if (0 == pCmdProcessor->construct_load_template_cmd((char *)&buff[file_offset],
                                                            file_size, chunk_size))
        {
            ret = pCmdProcessor->sendCmd();
            if (ret)
            {
                goto out;
            }
            pCmdProcessor->process_one_par_rsp(&remain_rsp);
            LOGD(FPTAG"remain_rsp: %d", remain_rsp);

            file_offset += chunk_size;
            remaining -= chunk_size;
            //On success we return expected remaining data size;
            if (remaining != (size_t)remain_rsp)
            {
                LOGE(FPTAG "%s, remaning data size out of sync, expected %zu, got %d",
                     __func__,
                     remaining,
                     remain_rsp);
                ret = -ENOENT;
            }
        }
        else
        {
            ret = -ENOENT;
            goto out;
        }
    }

out:
    if (NULL != buff)
    {
        fp_free(buff);
    }
    return ret;
}
#endif
int32_t fpTaEntryProxy::set_active_group(int32_t igid, char *pPath)
{
    int32_t ret = 0;
    int32_t path_len = 0;

    if (fpTaProxy::set_active_group(igid, pPath) != 0)
    {
        return -ENOENT;
    }
    LOGD(FPTAG"fpTaEntryProxy set_active_group invoked");

//----read templates from file path added by liuxn------------
    memset(TemplatePath, 0, PATH_MAX);
    path_len = sprintf(TemplatePath, "%s/user_data", pPath);
    LOGD(FPTAG"template path:%s len:%d\n", TemplatePath, path_len);

    ret = -ENOENT;
    if (0 == pCmdProcessor->constructSetActiveGroupCmd(igid, pPath))
    {
        if (pCmdProcessor->sendCmd() == 0)
        {
            ret = 0;
        }
    }

    ret = load_template();

    return ret;
}

uint64_t fpTaEntryProxy::pre_enroll()
{
    if (fpTaProxy::pre_enroll() != 0)
    {
        return -ENOENT;
    }
    uint64_t Ret = 0;
    LOGD(FPTAG"fpTaEntryProxy pre_enroll invoked");

    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_PRE_ENROLL))
    {
        if (0 == pCmdProcessor->processPreEnrollRsp(&mChallenge))
        {
            Ret = mChallenge;
            LOGD(FPTAG"pre_enroll get challenge :%" PRIu64 "\n", mChallenge);
        }
    }

    return Ret;//no hardware or hardware error
}

int32_t fpTaEntryProxy::delete_fid(int32_t gid, int32_t fid)
{
    LOGD(FPTAG"fpTaEntryProxy Remove gid=%d,fid=%d", gid, fid);
    if (fpTaProxy::delete_fid(gid, fid) != 0)
    {
        return -ENOENT;
    }
    int32_t Ret = -ENOENT;

    if (0 == pCmdProcessor->constructGidFidCmd(TCI_FP_CMD_REMOVE, gid, fid))
    {
        Ret = pCmdProcessor->sendCmd();
        if (Ret)
        {
            goto out;
        }
    }

    Ret = store_template();

out:
    if (0 != Ret)
    {
        LOGE(FPTAG"delete_fid failed");
    }

    return Ret;
}

int32_t fpTaEntryProxy::enroll_img(int32_t *pLastProgress, int32_t *pTotalEnrollCnt,
                                   int32_t *pCurEnrollCnt,
                                   int32_t *pEnrollFailReason, int32_t *pFillPart)
{
    LOGD(FPTAG"fpTaEntryProxy::enroll_img invoked");
    if (fpTaProxy::enroll_img(pLastProgress, pTotalEnrollCnt, pCurEnrollCnt, pEnrollFailReason,
                              pFillPart) != 0)
    {
        return -ENOENT;
    }

    if (get_stop_status())
    {
        return -FP_ERROR_USER_CANCEL;
    }

    int32_t Ret = -ENOENT;

    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_ENROLL_IMG))
    {
        if (0 == pCmdProcessor->processEnrollImgRsp(pLastProgress, pTotalEnrollCnt, pCurEnrollCnt,
                                                    pEnrollFailReason, pFillPart))
        {
            Ret = 0;
        }
    }
    if (0 != Ret)
    {
        LOGD(FPTAG"enroll_img ERROR!!!!!!!!!1");
    }
    return Ret;
}

int32_t fpTaEntryProxy::enrol_store_template(int32_t iGid, int32_t *pFid)
{
    LOGD(FPTAG"enrol_store_template invoked");
    if (fpTaProxy::enrol_store_template(iGid, pFid) != 0)
    {
        return -ENOENT;
    }

    int32_t Ret = -ENOENT;

    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_END_ENROLL))
    {
        pCmdProcessor->process_one_par_rsp((uint32_t *)pFid);
        LOGD(FPTAG"Enrolled Finger ID: 0x%x", *pFid);
        Ret = 0;
    }
    if (0 != Ret)
    {
        LOGE(FPTAG"end_enrol failed!");
        goto out;
    }

    Ret = store_template();
out:

    return Ret;
}


int32_t fpTaEntryProxy::saveTpls2File(char *path, uint8_t *pFileContent, int32_t iFileLen)
{
    char temp_path[PATH_MAX] = {0};

    if (snprintf(temp_path, PATH_MAX, "%s.bak", path) >= PATH_MAX)
    {
        LOGE("%s input:path too long", __func__);
        return -EIO;
    }

    FILE *file = NULL;
    file = fopen(temp_path, "wb+");//wb the file must exist.
    if (NULL == file)
    {
        LOGE(FPTAG"error: %s creat file failed!!", temp_path);
        return -EIO;
    }
    LOGD(FPTAG"open wb+ %s OK!", temp_path);
    if (1 != fwrite(pFileContent, iFileLen, 1, file))
    {
        LOGE(FPTAG"write templates file error!");
        fclose(file);
        return -EIO;
    }
    fclose(file);
    remove(path);
    rename(temp_path, path);

    /*    int32_t *log_temp = (int32_t *)pFileContent;
        LOGD(FPTAG"processAuthenticateRsp the first 4 words is(HEX): 0x8x, 0x%8x, 0x%8x, 0x%8x",
            log_temp[0], log_temp[1], log_temp[2], log_temp[3]);
        LOGD(FPTAG"processAuthenticateRsp the end 4 words is(HEX): 0x%2x%2x%2x%2x",
            pFileContent[iFileLen-1],pFileContent[iFileLen-2],pFileContent[iFileLen-3],pFileContent[iFileLen-4]);*/
    return 0;
}

#ifdef FEATURE_TEE_STORAGE
int32_t fpTaEntryProxy::store_template(void)
{
    uint8_t *p_buff = NULL;
    size_t buff_len = 0;
    uint32_t dummy = 0;

    LOGD(FPTAG"store_template invoked");
    int32_t Ret = -ENOENT;

    Ret = pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_STORETEMPLATE);

    if (Ret < 0)
    {
        LOGE(FPTAG"TCI_FP_CMD_STORETEMPLATE error! Ret:%d", Ret);
        goto out;
    }

    pCmdProcessor->process_get_byte_array_rsp(&p_buff, &buff_len, &dummy);
    if (0 == Ret)
    {
        LOGD(FPTAG"TEE store template OK! file_size:%zu", buff_len);
    }

out:

    if (0 != Ret)
    {
        LOGE(FPTAG"storeTemplate failed!");
    }

    return Ret;
}
#else
int32_t fpTaEntryProxy::store_template(void)
{
    size_t blob_size = 0;
    uint8_t *p_buff = NULL;
    size_t buff_len = 0;
    size_t remaining = 0;
    size_t chunk_size = 0;
    uint8_t *blob = NULL;
    size_t blob_offset = 0;
    uint32_t remain_rsp = 0;

    LOGD(FPTAG"store_template invoked");

    int32_t Ret = -ENOENT;

    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_GET_TEMPLATE_SIZE))
    {
        pCmdProcessor->process_one_par_rsp((uint32_t *)&blob_size);
        LOGD(FPTAG"group template size: %zu", blob_size);
        if (0 == blob_size)
        {
            LOGE(FPTAG"ERROR: Template from TEE is empty!!!");
            goto out;
        }
        Ret = 0;
    }
    else
    {
        goto out;
    }

    blob = (uint8_t *) fp_malloc(blob_size);
    if (!blob)
    {
        Ret = -FP_ERROR_ALLOC;
        goto out;
    }

    remaining = blob_size;

    while (remaining)
    {
        chunk_size = MIN(remaining, MAX_STORE_CHUNK);
        remain_rsp = 0;
        LOGD(FPTAG" remaining:%zu, chunk_size:%zu, buff_len:%zu, MAX_STORE_CHUNK:%zu", remaining,
             chunk_size, buff_len, MAX_STORE_CHUNK);

        Ret = pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_STORETEMPLATE);
        if (Ret)
        {
            LOGE(FPTAG"TCI_FP_CMD_STORETEMPLATE error! Ret:%d", Ret);
            goto out;
        }
        pCmdProcessor->process_get_byte_array_rsp(&p_buff, &buff_len, &remain_rsp);
        if ((NULL != p_buff) && (buff_len == chunk_size))
        {
            LOGD(FPTAG"get template array OK! buff_len:%zu", buff_len);
        }
        else
        {
            LOGE(FPTAG"get template array failed! chunk_size:%zu, buff_len:%zu", chunk_size, buff_len);
            Ret = -ENOENT;
            goto out;
        }


        remaining -= chunk_size;
        if (remaining != (size_t)remain_rsp)
        {
            LOGE(FPTAG"%s, remaning data size out of sync, expected %zu, got %d",
                 __func__,
                 remaining,
                 remain_rsp);
            Ret = -FP_ERROR_COMM;
            goto out;
        }

        memcpy(&blob[blob_offset], p_buff, chunk_size);
        blob_offset += chunk_size;
    }

    // LOGD(FPTAG" first 4 words is(HEX): 0x%2x%2x%2x%2x,", blob[0], blob[1], blob[2],
    //      blob[3]);
    // LOGD(FPTAG" last 4 words is(HEX): 0x%2x%2x%2x%2x,", blob[blob_size - 4], blob[blob_size - 3],
    //      blob[blob_size - 2], blob[blob_size - 1]);

    Ret = saveTpls2File(TemplatePath, blob, blob_size);

out:
    fp_free(blob);
    if (0 != Ret)
    {
        LOGE(FPTAG"storeTemplate failed!");
    }

    return Ret;
}
#endif//end of #ifdef FEATURE_TEE_STORAGE

int32_t fpTaEntryProxy::authorize_enrol(const hw_auth_token_t *hat)
{
    int32_t Ret = -ENOENT;
    LOGD(FPTAG"authorize_enrol invoked");
    if (fpTaProxy::authorize_enrol(hat) != 0)
    {
        return -ENOENT;
    }

    if (0 == pCmdProcessor->constructAuthorizeEnrolCmd(TCI_FP_CMD_AUTHORIZE_ENROL, hat))
    {
        Ret = pCmdProcessor->sendCmd();
        if (Ret == -FP_ERROR_TIMEDOUT)
        {
            Ret = -ETIMEDOUT;
        }
    }
    LOGD(FPTAG"authorize_enrol result:%d", Ret);
    return Ret;

}

int32_t fpTaEntryProxy::start_enroll(int32_t igid, int32_t *fid)
{
    int32_t Ret = 0;

    LOGD(FPTAG"start_enroll invoked");
    if (fpTaProxy::start_enroll(igid, fid) != 0)
    {
        return -ENOENT;
    }

    Ret = pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_START_ENROLL);

    LOGD(FPTAG"start_enroll result:%d", Ret);
    return Ret;
}

int32_t fpTaEntryProxy::finish_enroll()
{
    int32_t Ret = 0;

    LOGD(FPTAG"fpTaEntryProxy::finish_enroll  invoked");

    if (fpTaProxy::finish_enroll() != 0)
    {
        return -ENOENT;
    }

    Ret = pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_FINISH_ENROLL);
    return Ret;
}


int32_t read_bmp_data(char *file_name, char *img_buffer, int32_t buffer_len, int32_t *len, int32_t *width,int32_t *height)
{
    FILE *pfile;
    int32_t ret = 0;
    pfile = fopen(file_name, "rb");

    if (pfile != NULL)
    {
        LOGD("file %s open success.\n", file_name);
        fseek(pfile, 0, SEEK_END);
        int32_t file_length = ftell(pfile);
        *len = file_length - 1078;
        if (*len < 0 || *len > buffer_len)
        {
            LOGE("file length error, cur length = %d", *len);
            ret = -EINVAL;
            goto out;
        }

        fseek(pfile, 18, SEEK_SET);
        fread(width, 4, 1, pfile);
        fread(height, 4, 1, pfile);

        fseek(pfile, 1078, SEEK_SET);
        fread(img_buffer, *len, 1, pfile);
        LOGD("img width = %d, height = %d, file content:%d,%d,%d,%d",*width, *height, img_buffer[0], img_buffer[1], img_buffer[2], img_buffer[3]);
    }
    else
    {
        LOGE("file %s open fail,errno = %d.\n", file_name, -errno);
        ret = -ENOENT;
    }

out:
    if (pfile)
    {
        fclose(pfile);
    }

    return ret;
}

int32_t read_inject_image(char *data_buf, int32_t buffer_len, int32_t idx, int32_t *data_len,int32_t *width,int32_t *height)
{
    char image_name[PATH_MAX];
    sprintf(image_name, "/sdcard/bmp_inject_dir/%04d.bmp", idx);
    LOGD(FPTAG"read_inject_image invoked, filename = %s", image_name);
    return read_bmp_data(image_name, data_buf, buffer_len, data_len,width,height);
}

#define IMAGE_DATA_BUFFERE_LEN (160 * 160)
static char inject_data_buf[IMAGE_DATA_BUFFERE_LEN];

int32_t fpTaEntryProxy::after_image_captured(void)
{
    LOGD(FPTAG"after_image_captured invoked, inject img to ta,idx = %d", g_inject_img_idx);

    if (get_stop_status())
    {
        return -FP_ERROR_USER_CANCEL;
    }

    usleep(200 * 1000);
    int32_t data_len = 0;
    int32_t width = 0;
    int32_t height = 0;
    int32_t iret = read_inject_image(inject_data_buf, IMAGE_DATA_BUFFERE_LEN, g_inject_img_idx, &data_len,&width,&height);
    if (iret == 0)
    {
        LOGD(FPTAG"inject img to ta ok");
        if (0 == pCmdProcessor->constructInjectloadCmd(inject_data_buf, data_len, g_inject_img_idx))
        {
            if (pCmdProcessor->sendCmd() == 0)
            {

            }
        }

        //save file to sdcard as capture_img did
        if (fp_global_env.fp_internal_callback.on_generate_img_file_name)
        {
            fp_global_env.fp_internal_callback.on_generate_img_file_name();
        }

        identify_result_t& identify_result = get_cur_identify_result();
        snprintf(identify_result.image_file_name, sizeof(identify_result.image_file_name), "%s/%04d.bmp",
            fp_global_env.fp_internal_callback.get_fp_debug_base_dir(),g_inject_img_idx);

        if (get_fp_config_feature_store_captured_img())
        {
#ifdef DEBUG_ENABLE
//the saved file name is different with the default file, it same with the inject file name
            static char inject_file_name[PATH_MAX];
            snprintf(inject_file_name,PATH_MAX,"%s/%04d.bmp",
                fp_global_env.fp_internal_callback.get_fp_debug_base_dir(),g_inject_img_idx);

            save_bmp(inject_file_name, inject_data_buf, width, height);
#endif
        }

        g_inject_img_idx++;
    }
    else
    {
        LOGE(FPTAG"inject img to ta failed,ret = %d,please stop injection test", iret);
    }
    return iret;
}

int32_t fpTaEntryProxy::capture_img(int32_t eCaptyreStyle)
{
    LOGD(FPTAG"capture_img  invoked capturestyle:%d", eCaptyreStyle);
    if (fpTaProxy::capture_img(eCaptyreStyle) != 0)
    {
        return -ENOENT;
    }
    int32_t Ret = 0;

#ifdef DEBUG_ENABLE
    if (g_inject_enabled)
    {
        LOGD(FPTAG"skip capture Image, instead of injection img,ret = %d", Ret);
        Ret = after_image_captured();
    }
    else
#endif
    {
        LOGD(FPTAG"Capture Start!!!!!!");

        if (CAPTURE_STYLE_WAIT_UP & eCaptyreStyle)
        {
            Ret = CaptureWaitFingerUp();
            if (Ret)
            {
                if (-FP_ERROR_USER_CANCEL == Ret)
                {
                    LOGE(FPTAG"CaptureWaitFingerUp be stopped!!!");
                }
                else
                {
                    LOGE(FPTAG"CaptureWaitFingerUp error:%d", Ret);
                }
                goto out;
            }
        }

        Ret = CaptureWaitFingerDown(eCaptyreStyle);

        if (Ret)
        {
            LOGE(FPTAG"CaptureWaitFingerDown result:%d", Ret);
            goto out;
        }

        Ret = CaptureImageExecution();
        if (Ret)
        {
            LOGE(FPTAG"CaptureImageExecution error:%d", Ret);
            goto out;
        }
    }

out:
    LOGD(FPTAG"Capture Image End!!!!!!!res:%d", Ret);
    return Ret;
}

int32_t fpTaEntryProxy::begin_authenticate(uint64_t challangeID)
{
    int32_t Ret = -ENOENT;
    LOGD(FPTAG"begin_authenticate  invoked");
    if (fpTaProxy::begin_authenticate(challangeID) != 0)
    {
        return -ENOENT;
    }


    if (0 == pCmdProcessor->constructBeginAuthCmd(TCI_FP_CMD_BEGIN_AUTHENTICATE, challangeID))
    {
        if (pCmdProcessor->sendCmd() == 0)
        {
            Ret = 0;
        }
    }
    return Ret;
}

int32_t fpTaEntryProxy::end_authenticate(int32_t *pTemplateUpdated, int32_t allow_update_template)
{
    LOGD(FPTAG"end_authenticate  invoked");

    if (fpTaProxy::end_authenticate(pTemplateUpdated, allow_update_template) != 0)
    {
        return -ENOENT;
    }

    int32_t Ret = -ENOENT;

    if (get_fp_config_feature_record_statistical())
    {
        int32_t predict_result = hal_configuration(SET_PROPERTY_CMD_ALGO_IMAGE_PREDICT_RESULT, 0);
        identify_result_t &identify_result = get_cur_identify_result();

        identify_result.predict_result = predict_result;
    }

    if (0 == pCmdProcessor->send_cmd_one_par(TCI_FP_CMD_END_AUTHENTICATE, allow_update_template))
    {
        if (0 == pCmdProcessor->processEndAuthenticateRsp(pTemplateUpdated))
        {
            LOGD(FPTAG"processEndAuthenticateRsp OK");
            Ret = 0;
        }
    }

    if (*pTemplateUpdated)
    {
        Ret = store_template();
    }
    return Ret;
}

int32_t fpTaEntryProxy::authenticate( int32_t igid, int32_t *matchResult, int32_t *imatchFinger,
                                      bool forEnrollMatchImage, hw_auth_token_t *p_token)
{
    LOGD(FPTAG"authenticate invoked");

    if (fpTaProxy::authenticate(igid, matchResult, imatchFinger, forEnrollMatchImage, p_token) != 0)
    {
        return -ENOENT;
    }

    if (get_stop_status())
    {
        LOGD(FPTAG" user cancel received");
        return -FP_ERROR_USER_CANCEL;
    }

    int32_t Ret = -ENOENT;

    if (0 == pCmdProcessor->constructAuthenticateCmd(igid, forEnrollMatchImage))
    {
        if (pCmdProcessor->sendCmd() == 0)
        {
            if (0 == pCmdProcessor->processAuthenticateRsp(matchResult, imatchFinger, TemplatePath, p_token))
            {
                Ret = 0;
            }
        }
    }

    return Ret;
}

uint64_t fpTaEntryProxy::get_authenticator_id()
{
    uint64_t AuthenticatorId = 0;
    if (fpTaProxy::get_authenticator_id() != 0)
    {
        return -ENOENT;
    }

    LOGD(FPTAG"get_authenticator_id");


    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_GET_TEMPLATE_DB_ID))
    {
        pCmdProcessor->processGetAuthenticatorIdRsp(&AuthenticatorId);
    }

    return AuthenticatorId;
}

int32_t fpTaEntryProxy::get_enrolled_fids(int32_t *pEnrolledFids, int32_t iArrayCap,
                                          int32_t *pRealCnt)
{
    LOGD(FPTAG"get_enrolled_fids invoked");


    if (fpTaProxy::get_enrolled_fids(pEnrolledFids, iArrayCap, pRealCnt) != 0)
    {
        return -ENOENT;
    }
    if (pEnrolledFids == NULL || pRealCnt == NULL || iArrayCap <= 0)
    {
        LOGE(FPTAG"get_enrolled_fids args error!!!");
        return -EINVAL;
    }
    int32_t Ret = -ENOENT;

    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_GET_ENROLLED_FIDS))
    {
        if (0 == pCmdProcessor->processGetEnrolledFidsRsp(pEnrolledFids, iArrayCap, pRealCnt))
        {
            Ret = 0;
        }
    }

    return Ret;
}

int32_t fpTaEntryProxy::nav_loop()
{
    LOGD(FPTAG"nav_loop invoked");
    int32_t Ret = -ENOENT;
    int32_t GetRelRet = 0;
    int32_t start_flag = 1;
    int32_t DeltaX = 0;
    int32_t DeltaY = 0;
    int32_t sumX = 0;
    int32_t sumY = 0;
    uint64_t diffTime = 0;
    uint64_t jiffiess = 0;
    bool finger_lost = false;

    if (fpTaProxy::nav_loop() != 0)
    {
        return -ENOENT;
    }

    // new_nav_stop_finger_up = true;//if finger up didn't over, just kill it.
    usleep(10 * 1000);

    Ret = CaptureWaitFingerUp();
    if (Ret)
    {
        if (-FP_ERROR_USER_CANCEL == Ret)
        {
            LOGI(FPTAG"CaptureWaitFingerUp be stopped!!!");
        }
        else
        {
            LOGE(FPTAG"CaptureWaitFingerUp error:%d", Ret);
        }
        goto nav_loop_end;
    }
    finger_lost = true;

    while (1)
    {
        if (finger_lost)
        {
            init_enhanced_navi_setting();

            Ret = NavWaitFingerDown();

            if (0 != Ret)
            {
                goto nav_loop_end;
            }

            process_navi_event(0, 0, FNGR_ST_DETECTED, pGpioHal);

            nav_time_update(get_jiffiess_ms());

        }
        finger_lost = false;
        if (get_stop_status())
        {
            LOGD(FPTAG"Gpio high has reorted already, so we break here.");
            Ret = -FP_ERROR_USER_CANCEL;
            goto nav_loop_end;
        }

        if (0 == pCmdProcessor->constructRelCoordsCmd(start_flag))
        {
            if (0 == pCmdProcessor->sendCmd())
            {
                if (0 == pCmdProcessor->processGetRelCoordsRsp(&DeltaX, &DeltaY, &GetRelRet))
                {
                    LOGD(FPTAG"Get Relative Centroid X:%d, Y:%d, finger status:%d", DeltaX, DeltaY, GetRelRet);
                    Ret = 0;

                    if (0 != GetRelRet)
                    {
                        LOGD(FPTAG"change status: gesture finger lost!!!");
                        process_navi_event(0, 0, FNGR_ST_LOST, pGpioHal);
                        tac_impl_instance->notify_sensor_event(FP_SENSOR_EVENT_FINGER_UP);

                        sumX = 0;
                        sumY = 0;
                        nav_time_update(0);
                        finger_lost = true;
                    }
                }
            }
        }

        sumX += DeltaX;
        sumY += DeltaY;

        jiffiess =  get_jiffiess_ms();
        diffTime = abs(jiffiess - nav_time_get());
//        LOGD(FPTAG"diffTime:%d", diffTime);
        if (diffTime > 0)
        {
            if (diffTime >= FPSENSOR_INPUT_POLL_INTERVAL)
            {
                process_navi_event(sumX, sumY, FNGR_ST_MOVING, pGpioHal);
                LOGD(FPTAG"[rickon] nav finger moving. sumX = %d, sumY = %d\n", sumX, sumY);
                sumX = 0;
                sumY = 0;
                jiffiess = get_jiffiess_ms();
                nav_time_update(jiffiess);
            }
        }
        //usleep(10*1000);
        start_flag = 0;
    }
nav_loop_end:
    return Ret;
}

int32_t fpTaEntryProxy::get_image_quality(int32_t *p1, int32_t *p2, int32_t *p3)
{
    int32_t Ret = 0;
    if (fpTaProxy::get_image_quality(p1, p2, p3) != 0)
    {
        return -ENOENT;
    }

    if (pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_GET_IMAGE_QUALITY) == 0)
    {
        if (pCmdProcessor->processGetImageQualityRsp(p1, p2, p3) == 0)
        {
            Ret = 0;
        }
    }
    if (0 != Ret)
    {
        LOGE(FPTAG"do_get_image_quality_cmd ERROR!!");
    }

    LOGD(FPTAG"do_get_image_quality_cmd End area:%d, condition:%d, quality:%d", *p1, *p2, *p3);

    return Ret;
}

int32_t fpTaEntryProxy::get_image_size(int32_t *width,int32_t *height)
{
    int32_t Ret = 0;
    if (fpTaProxy::get_image_size(width, height) != 0)
    {
        return -ENOENT;
    }

//we has already get the size, just return
    if(sensor_width != 0 && sensor_height != 0)
    {
        *width = sensor_width;
        *height = sensor_height;
        Ret = 0;
    }
    else
    {
        if (pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_GET_IMGAGE_DIMENTION) == 0)
        {
            if (pCmdProcessor->processGetImgFormatRsp(width, height) == 0)
            {
                sensor_width = *width;
                sensor_height = *height;
                Ret = 0;
            }
        }
    }

    if (0 != Ret)
    {
        LOGE(FPTAG"do_get_size_cmd ERROR!!");
    }
    return Ret;
}

int32_t fpTaEntryProxy::print_system_info(void)
{
    if (fpTaProxy::print_system_info() != 0)
    {
        return -ENOENT;
    }
    //now we should call send get system info cmd to get system info
    LOGI(FPTAG"------------TEE system info BEGIN---------------------");
    if (0 == pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_GET_TEE_INFO))
    {
        if (0 == pCmdProcessor->process_get_tee_info_rsp(&chip_revision, &chip_hardware_id))
        {
            LOGI(FPTAG"------------TEE system info OVER---------------------");
        }
    }

    return 0;
}

int32_t fpTaEntryProxy::hal_configuration(int32_t tag, int32_t value)
{
    int32_t rsp = 0;
    if (fpTaProxy::hal_configuration(tag, value) != 0)
    {
        return -ENOENT;
    }

    if (pCmdProcessor->constructSetProperityCmd(tag, value) == 0)
    {
        if (pCmdProcessor->sendCmd() == 0)
        {
            pCmdProcessor->process_one_par_rsp((uint32_t *)&rsp);
            LOGD(FPTAG"hal_configuration tag = %d value = %d rsp = %d", tag, value, rsp);
        }
        else
        {
            LOGE(FPTAG"send cmd error");
            return -EIO;
        }
    }
    else
    {
        LOGE(FPTAG"constructSetProperityCmd error");
        return -ENOMEM;
    }
    return rsp;
}

fpTestIntf *fpTaEntryProxy::get_test_intf()
{
    fpTestIntfTaImpl *pIntf = new fpTestIntfTaImpl();
    pIntf->setTaProxy(this);
    return pIntf;
}

int32_t fpTaEntryProxy::check_board(void)
{
    int32_t result = -ENOENT;
    pGpioHal->ChipGpioReset();

    result = pCmdProcessor->send_cmd_no_payload(TCI_FP_CMD_CHECKBOARD_TEST);

    if (result != 0)
    {
        LOGE(FPTAG"do_check_board_cmd ERROR!!");
    }
    else
    {
        LOGD(FPTAG"do_check_board_cmd OK!!");
    }

    return result;
}

int32_t fpTaEntryProxy::service_control(int32_t ipara1, int32_t ipara2)
{
    LOGD(FPTAG"fpTaEntryProxy::service_control invoked,para1= %d,para2 = %d", ipara1, ipara2);
    if (fpTaProxy::service_control( ipara1, ipara2) != 0)
    {
        return -ENOENT;
    }

    if (ipara1 == FP_SERVICE_CONTROL_CMD_SETENROLL_IMGCNT) //set enrollment cnt
    {
        if (ipara2 >= 4 && ipara2 <= 24)
        {
            LOGD(FPTAG"service_control set enroll num:%d", ipara2);
        }
        else
        {
            LOGE(FPTAG"service_control set enroll num:%d,para invalid,should between[4~24]!!", ipara2);
        }
        return 0;
    }

    if (ipara1 == FP_SERVICE_CONTROL_CMD_INJECTION_IMG_IDX)
    {
        LOGD(FPTAG"service_control reset inject image idx to %d", ipara2);
        g_inject_img_idx = ipara2;
        return 0;
    }

    if (ipara1 == FP_SERVICE_CONTROL_CMD_INJECTION_CONTROL)
    {
        LOGD(FPTAG"service_control set image inject status to:%d", ipara2);
        g_inject_enabled = ipara2;
        if (g_inject_enabled)
        {
            //start a new statistical folder
            if (customer_callback.generate_debug_base_dir)
            {
                customer_callback.generate_debug_base_dir();
            }
        }
        return 0;
    }

    if (ipara1 == FP_SERVICE_CONTROL_CMD_NAVIGATOR_ENABLE_REPORT)
    {
        pGpioHal->pfpInput->config_key_status(ipara2, 1);
        return 0;
    }

    if (ipara1 == FP_SERVICE_CONTROL_CMD_NAVIGATOR_DISABLE_REPORT)
    {
        pGpioHal->pfpInput->config_key_status(ipara2, 0);
        return 0;
    }

    if (ipara1 == FP_SERVICE_CONTROL_CMD_CHECK_BOARD)
    {
        int32_t ret = -ENOENT;
        ret = check_board();
        LOGD(FPTAG"service_control FP_SERVICE_CONTROL_CMD_CHECK_BOARD received,result = %d", ret);
        return ret;
    }

    if (ipara1 == FP_SERVICE_CONTROL_CMD_SELF_TEST)
    {
        int32_t ret = -ENOENT;
        ret = self_test();
        LOGD(FPTAG"service_control FP_SERVICE_CONTROL_CMD_SELF_TEST received,result = %d", ret);
        return ret;
    }

    if(ipara1 == FP_SERVICE_CONTROL_CMD_FINGER_DETECT_TEST)
    {
        int32_t ret = -ENOENT;
        ret = finger_detect_test();
        LOGD(FPTAG"service_control FP_SERVICE_CONTROL_CMD_FINGER_DETECT_TEST received,result = %d",ret);
        return ret; 
    }

    return -EINVAL;
}

int32_t fpTaEntryProxy::finger_detect_test(void)
{
    if (fpTaProxy::finger_detect_test() != 0)
    {
        return -ENOENT;
    }
    return CaptureWaitFingerDown(CAPTURE_STYLE_WAIT_DOWN);
}

int32_t fpTaEntryProxy::self_test(void)
{
    int32_t result = -ENOENT;
    pGpioHal->ChipGpioReset();

    if (pCmdProcessor->constructSetProperityCmd(SET_PROPERTY_CMD_SELFTEST, 0) == 0)
    {
        if (pCmdProcessor->sendCmd() == 0)
        {
            pCmdProcessor->process_one_par_rsp((uint32_t *)&result);
            LOGD(FPTAG"SET_PROPERTY_CMD_SELFTEST,result = %d", result);
        }
        else
        {
            result = -EIO;
        }
    }

    if (0 != result)
    {
        LOGD(FPTAG"doSelftestCmd ERROR!! :%d", result);
    }
    return result;
}

#ifdef FP_TEE_QSEE4
int32_t fpTaEntryProxy::qsee_set_auth_token_key(void)
{
    int32_t result = -ENOENT;
    uint8_t* key = NULL;
    uint32_t key_size = 0;

    LOGD(FPTAG"%s invoked", __func__);

    result = fp_qsee_km_get_encapsulated_key(&key, &key_size);
    if (result)
    {
        LOGE(FPTAG"can not get auth token from keymaster, %d\n", result);
        return -ENOENT;
    }

    if (0 == pCmdProcessor->constructSetAuthTokenKeyCmd(key, key_size))
    {
        if (pCmdProcessor->sendCmd() == 0)
        {
            pCmdProcessor->process_one_par_rsp((uint32_t*)&result);
            LOGE(FPTAG"SET AUTH TOKEN KEY, result = %d",result);
            result = 0;
        }
        else
        {
            LOGE(FPTAG"SEND QSEE SET AUTH TOKEN KEY CMD failed");
            result = -EIO;
        }
    }
    
    fp_qsee_km_release_encapsulated_key(key);
    return 0;
}
#endif
