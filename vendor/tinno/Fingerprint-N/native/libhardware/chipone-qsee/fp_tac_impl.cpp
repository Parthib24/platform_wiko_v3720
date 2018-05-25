#include "fp_tac_impl.h"
#include "fp_daemon_impl.h"
#include <endian.h>
#include <time.h>
#include <string.h>
#include <stdlib.h>
#include "fp_log.h"
#include "fp_extension_intf.h"
#include "fp_common.h"
#include "fp_extension.h"
#define FPTAG "fpTac_Impl.cpp "

void LOGD_HAT(const hw_auth_token_t *hat)
{
/*    LOGD(FPTAG"hat->challenge %" PRIu64 "\n"
         "hat->authenticator_id %" PRIu64 "\n"
         "hat->authenticator_type %u" "\n"
         "hat->timestamp %" PRIu64 "\n"
         "hat->user_id %" PRIu64 "\n"
         "hat->version %hhu" "\n",
         hat->challenge,
         hat->authenticator_id,
         hat->authenticator_type,
         hat->timestamp,
         hat->user_id,
         hat->version);
    int32_t *p = (int32_t *)hat->hmac;
    LOGD(FPTAG"hmac[32]:0x%8x, 0x%8x, 0x%8x, 0x%8x, 0x%8x, 0x%8x, 0x%8x, 0x%8x \n",
         p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
*/
}

bool is_64bit_system(void)
{
    long int_bits = (((long)((long *)0 + 1)) << 3);
    LOGI(FPTAG" is_64bit_system = %d", (int_bits == 64));
    return (int_bits == 64);
}

fpTacImpl::fpTacImpl(fpDameonImpl *daemon_impl)
{
    LOGD(FPTAG"fpTacImpl constructor invoked");
    daemon_instance = daemon_impl;
    ta_proxy_instance = create_ta_proxy(this);
    active_gid = -1;
};

fpTacImpl::~fpTacImpl()
{
    LOGD(FPTAG"fpTacImpl destructor invoked");
    daemon_instance = NULL;

    if (ta_proxy_instance)
    {
        delete ta_proxy_instance;
        ta_proxy_instance = NULL;
    }
    active_gid = -1;
};

void fpTacImpl::notify_sensor_event(fp_sensor_event_t event)
{
#ifdef FPSENSOR_CONFIG_REPORT_FINGER_UPDOWN_EVENT
    fingerprint_msg_t msg;
    if ( FP_SENSOR_EVENT_FINGER_DOWN == event ||  FP_SENSOR_EVENT_FINGER_UP == event )
    {
        int32_t acquire_code = (FP_SENSOR_EVENT_FINGER_DOWN == event) ?
                               FINGERPRINT_ACQUIRED_VENDOR_CAPTURE_FINGERDOWN : FINGERPRINT_ACQUIRED_VENDOR_CAPTURE_FINGERUP;
        construct_acquire_msg(msg, acquire_code);
        daemon_instance->notify(&msg);
    }
#endif
}

void fpTacImpl::print_system_info(void)
{
    int32_t para1 = 0;
    int32_t para2 = 0;
    LOGI(FPTAG"H-hal FAE BUILD  INFO   mian version:%s", FPDAEMON_VERSION);
    LOGI(FPTAG"git branch    :%s", GIT_BRANCH_FROM_BUILD);
    LOGI(FPTAG"commit id     :%s", COMMIT_ID_FROM_BUILD);
    LOGI(FPTAG"Android Ver   :%d", TARGET_ANDROID);
    LOGI(FPTAG"Who compile .a:%s", WHOCOMPILE);

#ifdef DEBUG_ENABLE
    LOGI(FPTAG"Mode          :Debug");
#else
    LOGI(FPTAG"Mode          :Release");
#endif

    is_64bit_system();
    print_fp_config_table();

#ifdef FPSENSOR_CONFIG_REPORT_FINGER_UPDOWN_EVENT
    LOGI(FPTAG"support FPSENSOR_CONFIG_REPORT_FINGER_UPDOWN_EVENT: true");
#else
    LOGI(FPTAG"support FPSENSOR_CONFIG_REPORT_FINGER_UPDOWN_EVENT: false");
#endif

#ifdef ENV_TEE
    LOGI(FPTAG"support ENV_TEE: true, TEE_NAME:%s",TEE_NAME);
    #ifdef FEATURE_TEE_STORAGE
        LOGI(FPTAG"support FEATURE_TEE_STORAGE: true");
    #else
        LOGI(FPTAG"support FEATURE_TEE_STORAGE: false");
    #endif
#elif defined(ENV_REE)
    LOGI(FPTAG"support ENV_REE: true");
#else
    #error: please define ENV_TEE or ENV_REE
#endif

    para1 = para2 = 0;
    if(fp_global_env.fp_internal_callback.on_enroll_duplicate)
    {
        fp_global_env.fp_internal_callback.on_enroll_duplicate(&para1,&para2);
    }
    LOGI(FPTAG"on_enroll_duplicate code:%d type:%d",para1,para2);

    para1 = 0;
    if(fp_global_env.fp_internal_callback.on_enroll_finger_same_area)
    {
        fp_global_env.fp_internal_callback.on_enroll_finger_same_area(&para1);
    }
    LOGI(FPTAG"on_enroll_finger_same_area code:%d",para1);

    if(ta_proxy_instance)
    {
        ta_proxy_instance->print_system_info();
    }
    
    return;
}

uint64_t fpTacImpl::open_hal()
{
    uint64_t ret = 0;
    ret = ta_proxy_instance->open_hal();
    if(ret != 0)
    {
        configure_sensor_after_hal_open();
        LOGI(FPTAG"open_hal invoked,system config information:");
        print_system_info();
    }
    else
    {
        LOGE(FPTAG"open_hal invoked,but open failed");
    }
    return ret;
}

void fpTacImpl::configure_sensor_after_hal_open(void)
{
    //configure the sensor
    int report_same_finger_area_msg = 0;

    if(fp_global_env.fp_internal_callback.on_enroll_finger_same_area)
    {
        fp_global_env.fp_internal_callback.on_enroll_finger_same_area(&report_same_finger_area_msg);
        if(report_same_finger_area_msg)
            ta_proxy_instance->hal_configuration(SET_PROPERTY_CMD_ALGO_FINGER_SAME_EREA_DETECT,report_same_finger_area_msg != 0 ? 1 : 0);
    }

    return;
}

int32_t fpTacImpl::close_hal()
{
    LOGD(FPTAG"close_hal invoked");
    if (ta_proxy_instance)
    {
        ta_proxy_instance->close_hal();
    }
    return 0;
}

int32_t fpTacImpl::set_stop_status(bool new_status)
{
    LOGD(FPTAG"set_stop_status invoked new_status= %d", new_status);
    return ta_proxy_instance->set_stop_status(new_status);
}

int32_t fpTacImpl::delete_fid(int32_t gid, int32_t fid,bool report_rm_result)
{
    int32_t ret = 0;
    int32_t ids[FP_CONFIG_MAX_ENROLL_SLOTS]; //FIXME
    int32_t size = FP_CONFIG_MAX_ENROLL_SLOTS; //FIXME
    int32_t idx = 0;
    fingerprint_msg_t msg;

    LOGD(FPTAG"delete_fid igid:%d, ifid: 0x%x invoked", gid, fid);

    ret = ta_proxy_instance->get_enrolled_fids(ids, FP_CONFIG_MAX_ENROLL_SLOTS, &size);
    if (ret)
    {
        LOGE(FPTAG"get_enrolled_fids error");
        ret = -EIO;
        goto out;
    }

    if (fid == 0)
    {
        LOGI(FPTAG"delete_fid all fids invoked");
        for (idx = 0; idx < size; ++idx)
        {
            LOGD(FPTAG"delete_fid fid: 0x%x", ids[idx]);
            if (ta_proxy_instance->delete_fid(gid, ids[idx]))
            {
                ret = -EIO;
                LOGE(FPTAG"Remove fid:%d failed!!", ids[idx]);
                goto out;
            }
            log_delete_fid_statistical_info(ids[idx]);
            construct_remove_msg(msg, gid, ids[idx]);
            daemon_instance->notify(&msg);
        }

        //call delete fid = 0 to clear db, do not care the return value
        ta_proxy_instance->delete_fid(gid, 0);
    }
    else
    {
        ret = 0;
        for(idx = 0; idx < size; idx++)
        {
            if(ids[idx] == fid)
            {
                ret = ta_proxy_instance->delete_fid(gid, fid);
                break;
            }
        }

        if (ret != 0)
        {
            ret = -EIO;
            goto out;
        }
        log_delete_fid_statistical_info(fid);

        if(report_rm_result)
        {
            construct_remove_msg(msg, gid, fid);
            daemon_instance->notify(&msg);
        }

        if(size == 1)
        {
            //the last fid in db,then clear db
            ta_proxy_instance->delete_fid(gid, 0);
        }
    }

out:
    if (0 != ret && report_rm_result)
    {
        construct_error_msg(msg, FINGERPRINT_ERROR_UNABLE_TO_REMOVE);
        daemon_instance->notify(&msg);
    }
//for android 7, after remove fid, we should send a fid = 0 msg to finish the process
#if (TARGET_ANDROID>=7)
    if(0 == ret && report_rm_result)
    {
        construct_remove_msg(msg, gid, 0);
        daemon_instance->notify(&msg);
    }
#endif
    return ret;
}

int32_t fpTacImpl::duplicated_finger_check(int32_t gid)
{
    int32_t ret = ta_proxy_instance->begin_authenticate(0);
    int32_t match_result = 0;
    if (ret == 0)
    {
        int32_t match_finger = -1;
        hw_auth_token_t auth_token = {0};
        ret = ta_proxy_instance->authenticate(gid, &match_result, &match_finger, true, &auth_token);
        int32_t dummy = 0;
        int32_t img_quality = 0;
        if(get_fp_config_feature_record_statistical())
        {
            int32_t d1;
            img_quality = 0;
            ta_proxy_instance->get_image_quality(&d1,&d1,&img_quality);
        }
        ta_proxy_instance->end_authenticate(&dummy,0);
    }

    LOGD(FPTAG"fpTacImpl::duplicated_finger_check invoked,match_result=%d,iRet=%d", match_result, ret);
    if(ret < 0)
        return ret;
    else
        return match_result > 0;
}

//return value is: 0~100
int32_t fpTacImpl::get_img_ratio(int32_t img_area)
{
    int32_t sensor_width;
    int32_t sensor_height;
    int32_t ret = ta_proxy_instance->get_image_size(&sensor_width,&sensor_height);
    if(ret == 0)
    {
        float sensor_area = ( (sensor_width * sensor_height) / 400.0f); //20pixel per mm
        int img_ratio = ( (img_area * 100.0f) / sensor_area);
        LOGD(FPTAG" sensor width:%d, height=%d, sensor area = %f,img area:%d, img ratio:%d",
            sensor_width,sensor_height,sensor_area,img_area,img_ratio);

        return img_ratio;
    }

    return 0;
}

uint64_t fpTacImpl::get_authenticator_id()
{
    uint64_t ret = ta_proxy_instance->get_authenticator_id();//authenticatid;
    LOGD(FPTAG" get_authenticator_id invoked iRet=%" PRIu64 " ", ret);
    return ret;
}

int32_t fpTacImpl::enroll(const hw_auth_token_t *hat, int32_t gid, int32_t timeout)
{
    int32_t cur_progress = 0;
//the init value of iTotalEnrollCnt should > iCurEnrollCnt, to ensoure the start of the loop below
    int32_t total_enroll_cnt = 10;
    int32_t last_enroll_cnt = 0;
    int32_t cur_enroll_cnt = 0;
    int32_t enroll_fail_reason = 0;
    int32_t fill_part_indication = 0;
    int32_t fid = 0;
    LOGD(FPTAG" enroll() invoked");

    LOGD_HAT(hat);
/*
    struct timeval sync_start_time = ca_timer_start(NULL);
    sync_fp_with_xml();
    ca_timer_end("sync fp list time:",sync_start_time);
*/
    fingerprint_msg_t msg;
    int32_t ret = 0;

    ret = ta_proxy_instance->authorize_enrol(hat);

#ifdef DEBUG_ENABLE
    if(g_inject_enabled && ret != 0)
    {
        LOGD(FPTAG" authorize_enrol ingore error in img inject mode,force set ret = 0,g_inject_enabled = %d",g_inject_enabled);
        ret = 0;
    }
#endif

    if(ret)
    {
#ifdef ENV_TEE
        LOGI(FPTAG" authorize_enrol failed!!!!!!!");
        if (-ETIMEDOUT == ret) {
            construct_error_msg(msg, FINGERPRINT_ERROR_TIMEOUT);
        }
        else
        {
            construct_error_msg(msg, FINGERPRINT_ERROR_HW_UNAVAILABLE);
        }
        daemon_instance->notify(&msg);
        ret = -EINVAL;
        goto out;
#endif
    }

    ret = ta_proxy_instance->start_enroll(gid, &fid);
    if (ret < 0)
    {
        construct_error_msg(msg, FINGERPRINT_ERROR_NO_SPACE);
        daemon_instance->notify(&msg);

        ret = -ENOSPC;
        goto finishenroll;
    }

    //for rongqi only
    enroll_state = true;
    //for rongqi only

    while (cur_enroll_cnt < total_enroll_cnt )
    {
CAPTURE_RETRY:
        ret = capture_img(CAPTURE_STYLE_WAIT_UP | CAPTURE_STYLE_WAIT_DOWN);
        LOGD(FPTAG"after capture_img mallocCnt=%d", get_malloc_cnt());

        if (ret != 0)
        {
            LOGE(FPTAG"capture_img error,exit enroll procedure errcode=%d", ret);
            if (ret == -FP_ERROR_USER_CANCEL)
            {
                break;
            }
            else
            {
                usleep(20 * 1000);
                goto CAPTURE_RETRY;
            }
        }

        if (get_fp_config_forbidden_duplicated_finger_enroll())
        {
            ret = duplicated_finger_check(gid);
            if (ret < 0)
            {
                //error
                construct_error_msg(msg, FINGERPRINT_ERROR_UNABLE_TO_PROCESS);
                daemon_instance->notify(&msg);
                break;
            }
            else if (ret > 0)
            {
                int32_t report_code = FINGERPRINT_ACQUIRED_VENDOR_ALGO_IMAGEENROLLED;
                int32_t report_type = 0;
                if(fp_global_env.fp_internal_callback.on_enroll_duplicate)
                {
                    fp_global_env.fp_internal_callback.on_enroll_duplicate(&report_code,&report_type);
                }
                LOGD(FPTAG"duplicated_finger_check find a exist finger, report_code=%d, report_type=%d", report_code,report_type);

                if(report_type)
                {
                    construct_error_msg(msg, report_code);
                    daemon_instance->notify(&msg);
                    break;
                }
                else
                {
                    construct_acquire_msg(msg, report_code);
                    daemon_instance->notify(&msg);
                    continue;
                }
            }
            else
            {
                LOGD(FPTAG"duplicated_finger_check no same finger, it's ok continue enroll");
                ;//do nothing, call enrol image
            }
            LOGD(FPTAG"duplicated_finger_check finished result = %d", ret);
        }

//for presdo-iphone only
        if (!enroll_state)
        {
            LOGD(FPTAG"enroll is paused, just capture image and wait to resume");
            usleep(50 * 1000);
            goto CAPTURE_RETRY;
        }
//for presdo-iphone only

        identify_result_t& identify_result = get_cur_identify_result();
        reset_global_identify_result();

        struct timeval enroll_start_time = ca_timer_start(NULL);
        ret = ta_proxy_instance->enroll_img(&cur_progress, &total_enroll_cnt, &cur_enroll_cnt,
                                            &enroll_fail_reason, &fill_part_indication);
        ca_timer_end(" KPI enrollimage time", enroll_start_time);

        //send progress msg
        if (ret == 0)
        {
            if (get_fp_config_feature_record_statistical())
            {
                int32_t tmp_img_quality = 0;
                int32_t tmpd1;
                ta_proxy_instance->get_image_quality(&tmpd1,&tmpd1,&tmp_img_quality);

                get_current_timestamp(identify_result.timestamp_str,sizeof(identify_result.timestamp_str));
                identify_result.operation = 'E';
                identify_result.cur_enroll_cnt = cur_enroll_cnt;
                identify_result.total_enroll_cnt = total_enroll_cnt;
                identify_result.template_id = 0;
                identify_result.quality = tmp_img_quality;
                identify_result.unmatch_times = 0;
                identify_result.report_msg_type = AUTHENTICATE_REPORT_MSG_TYPE_UNREPORT;

                snprintf(identify_result.image_file_name,sizeof(identify_result.image_file_name),"%s",get_capture_img_store_file_full_name());
                LOGD(FPTAG"statical-> %s %c 0x%x %d %d %d %d %s", identify_result.timestamp_str, identify_result.operation,
                    identify_result.template_id,identify_result.result ,identify_result.score,0,
                    identify_result.updated_template_size,identify_result.image_file_name);

                log_statistical_result(&identify_result);
            }

            if (cur_enroll_cnt >= last_enroll_cnt)
            {
                if (cur_enroll_cnt == total_enroll_cnt)
                {
                    ret = ta_proxy_instance->enrol_store_template(gid, &fid);
                    if(ret == 0)
                    {
                        if(fid == 0)
                        {
                            LOGE(FPTAG"before store template,but found fid = 0, can't process this situation,exit enroll function with error");
                            construct_error_msg(msg, FINGERPRINT_ERROR_NO_SPACE);
                            daemon_instance->notify(&msg);
                        }
                        else
                        {
                            if(ta_proxy_instance->get_stop_status())
                            {
                               LOGI(FPTAG"after store template,but receive stop enroll command,exit enroll function and delete fid=0x%x",fid); 
                               delete_fid(gid,fid,false);
                            }
                            else
                            {
                                construct_enrolling_msg(msg, gid, fid, cur_enroll_cnt, total_enroll_cnt);
                                daemon_instance->notify(&msg); 
                            }
                        }
                    }
                    else //enroll store error
                    {
                        LOGE(FPTAG"store template error:%d",ret);
                        construct_error_msg(msg, FINGERPRINT_ERROR_NO_SPACE);
                        daemon_instance->notify(&msg);
                    }
                }
                else
                {
                    if((cur_enroll_cnt == last_enroll_cnt))
                    {
                        int32_t report_code = 0;
                        if((enroll_fail_reason == 61441 || enroll_fail_reason == 4103))
                        {
                            if(fp_global_env.fp_internal_callback.on_enroll_finger_same_area)
                            {
                                fp_global_env.fp_internal_callback.on_enroll_finger_same_area(&report_code);
                            }
                        }
                        else if( enroll_fail_reason == 4444)
                        {
                            LOGI(FPTAG"enroll finger image quality too bad");
                            // TINNO need reported error code : 1 = PARTIAL
                            report_code = FINGERPRINT_ACQUIRED_PARTIAL;
                        }

                        if(report_code != 0)
                        {
                            LOGI(FPTAG"enroll progress not changed,enroll_fail_reason=%d, report to UI:%d",enroll_fail_reason,report_code);
                            construct_acquire_msg(msg, report_code);
                            daemon_instance->notify(&msg);
                        }

                        goto CAPTURE_RETRY;
                    }

                    //normal process
                    construct_enrolling_msg(msg, gid, fid, cur_enroll_cnt, total_enroll_cnt);
                    daemon_instance->notify(&msg);
                }

                if (get_fp_config_report_detail_msg())
                {
                    //the last second times, indicate user how to fill templates,
                    if (total_enroll_cnt - cur_enroll_cnt == 1)
                    {
                        int32_t fill_indication = FINGERPRINT_ACQUIRED_VENDOR_ALGO_FILL_IND_UNKNOWN;
                        if(fill_part_indication >= 1 && fill_part_indication <= 4)
                        {
                            fill_indication = FINGERPRINT_ACQUIRED_VENDOR_ALGO_FILL_IND_UP + (fill_part_indication - 1);
                            construct_acquire_msg(msg, fill_indication);
                            daemon_instance->notify(&msg);
                        }
                        LOGD(FPTAG" fill_part_indication=%d,fill_indication = %d", fill_part_indication, fill_indication);
                    }
                    if (enroll_fail_reason == 4104)
                    {
                        LOGD(FPTAG" enroll_img 4104");
                    }
                }
                last_enroll_cnt = cur_enroll_cnt;
            }
/*            else
            {
                //progress not changed
                int32_t acquire_info = FINGERPRINT_ACQUIRED_IMAGER_DIRTY;
                if (get_fp_config_report_detail_msg())
                {
                    acquire_info = FINGERPRINT_ACQUIRED_VENDOR_ALGO_UNKNOWN;
                    if (enroll_fail_reason == 4103 || enroll_fail_reason == 61441)
                    {
                        acquire_info = FINGERPRINT_ACQUIRED_VENDOR_ALGO_4103;
                    }
                }
                construct_acquire_msg(msg, acquire_info);
                daemon_instance->notify(&msg);
            }*/
        }
        else
        {
            construct_error_msg(msg, FINGERPRINT_ERROR_UNABLE_TO_PROCESS);
            daemon_instance->notify(&msg);
            break;  //error process
        }
    }

finishenroll:
    ta_proxy_instance->finish_enroll();
out:
    LOGD(FPTAG"tac enroll finished");
    return ret;
}
//now this function is used by enroll and verify
//the combination of bRetry and eCaptyreStyle is not test fully, be caution of this
int32_t fpTacImpl::capture_img(int32_t capture_style)
{
    fingerprint_msg_t msg;
    int32_t ret = 0;
    LOGD(FPTAG"capture_img,capturestyle = %d", capture_style);
    ret = ta_proxy_instance->capture_img(capture_style);

    LOGD(FPTAG" after capture_img,captureRet = %d", ret);
    if (ret == 0)
    {
        //send acquire msg
        construct_acquire_msg(msg, FINGERPRINT_ACQUIRED_GOOD);
        daemon_instance->notify(&msg);
    }
    else if (ret == -FP_ERROR_USER_CANCEL)
    {
        ;// do nothing
    }
    else
    {
        if ( (CAPTURE_STYLE_WAIT_UP & capture_style) )
        {
            switch (ret)
            {
//TA or L-hal dor not report this return value
/*                case -MSG_TYPE_FINGER_LOW_COVERAGE:
                    construct_acquire_msg(msg, FINGERPRINT_ACQUIRED_PARTIAL);
                    daemon_instance->notify(&msg);
                    break;*/
                case -MSG_TYPE_FINGER_MOVE:
                    construct_acquire_msg(msg, FINGERPRINT_ACQUIRED_TOO_FAST);
                    daemon_instance->notify(&msg);
                    break;
                case -MSG_TYPE_FINGER_FAST:
                    /*                    construct_acquire_msg(msg,FINGERPRINT_ACQUIRED_TOO_FAST);
                                        daemon_instance->notify(&msg);*/
                    break;

                default:
                    /*                    construct_acquire_msg(msg,FINGERPRINT_ACQUIRED_VENDOR_CAPTURE_UNKNOWN);
                                        daemon_instance->notify(&msg);*/
                    break;
            }
        }
    }

    return ret;
}

int32_t fpTacImpl::injection_authenticate(void)
{
    int32_t is_updated = 0;
    int32_t ret = 0;
    int32_t match_result = 0;
    int32_t match_finger = -1;
    int32_t capture_style = CAPTURE_STYLE_WAIT_DOWN;
    int32_t img_quality = 0;
    hw_auth_token_t token = {0};

    LOGD(FPTAG"injection_authenticate invoked");

    ret = ta_proxy_instance->begin_authenticate(0x11111111);

    if (ret != 0)
    {
        LOGE(FPTAG"injection_authenticate begin_authenticate error:%d",ret);
    }
    else
    {
        match_result = 0;
        match_finger = -1;

        identify_result_t& identify_result = get_cur_identify_result();
        reset_global_identify_result();
        identify_result.operation = 'A';
        ret = capture_img(capture_style);

        get_current_timestamp(identify_result.timestamp_str, sizeof(identify_result.timestamp_str));
        identify_result.timestamp = atol(identify_result.timestamp_str);

        if (ret == 0)
        {
            ret = ta_proxy_instance->authenticate(0, &match_result, &match_finger, false, &token);
            if(get_fp_config_feature_record_statistical())
            {
                int32_t d1,d2;
                img_quality = 0;
                ta_proxy_instance->get_image_quality(&d1,&d2,&img_quality);
            }

            LOGD(FPTAG"authenticate iRet = %d ,matchResult =%d" , ret, match_result);
            if (ret == 0)
            {
                if (match_result > 0)   //match    :match_result == 1, update template, match_result == 2  do no update template
                {
                    if(!ta_proxy_instance->get_stop_status())
                    {
                        LOGI(FPTAG"Inject_Authenticate,discard match notify msg");
                    }
                    else
                    {
                        LOGI(FPTAG"Authenticate finihsed, but canceled");
                    }

                    int32_t allow_update_template = 1; //default allow update templates
                    if(match_result == MATCH_RESULT_SUCCEED_NO_UPDATE) allow_update_template = 0;

                    ta_proxy_instance->end_authenticate(&is_updated,allow_update_template);
                    identify_result.finger_status = 1;
                    identify_result.result = is_updated ? MATCH_RESULT_SUCCEED_UPDATE : MATCH_RESULT_SUCCEED_NO_UPDATE;
                    identify_result.quality = img_quality;
                    identify_result.unmatch_times = 0;
                    identify_result.report_msg_type = AUTHENTICATE_REPORT_MSG_TYPE_MATCH;
                    log_statistical_result(&identify_result);
                    ret = 1;
                }
                else     //unmatch
                {
                    if(!ta_proxy_instance->get_stop_status())
                    {
                        LOGD(FPTAG"injection_authentication discard unmatch notify msg");
                    }

                    ta_proxy_instance->end_authenticate(&is_updated,0);
                    identify_result.result = match_result;// match result should <= 0
                    identify_result.quality = img_quality;
                    identify_result.unmatch_times = 0;
                    identify_result.report_msg_type = (match_result != MATCH_RESULT_FAILED_POOR_QUALITY) ?
                                                         AUTHENTICATE_REPORT_MSG_TYPE_UNMATCH :
                                                         AUTHENTICATE_REPORT_MSG_TYPE_UNREPORT;
                    identify_result.finger_status = 1;
                    log_statistical_result(&identify_result);

                    ret = 0;
                }
            }
            else     //error
            {
                ta_proxy_instance->end_authenticate(&is_updated,0);
                identify_result.result = MATCH_RESULT_FAILED;
                identify_result.quality = img_quality;
                identify_result.unmatch_times = 0;
                identify_result.report_msg_type = AUTHENTICATE_REPORT_MSG_TYPE_ERROR;
                identify_result.finger_status = 1;
                log_statistical_result(&identify_result);
            }
        }
        else
        {
            if(ret == -ENOENT)
            {
                LOGE(FPTAG"inject authenticate,no inject file");
            }
            else
            {
                LOGE(FPTAG"inject authenticate,other error:%d",ret);
            }
        }
    }

    LOGD(FPTAG"authenticate leave");
    return ret;
}

int32_t fpTacImpl::authenticate(uint64_t challenge_id, int32_t gid)
{
    fingerprint_msg_t msg;
    int32_t is_updated = 0;
    hw_auth_token_t token = {0};
    int32_t ret = 0;
    int32_t unmatch_times = 0;
    int32_t match_result = 0;
    int32_t match_finger = -1;
    int32_t auth_config_unmatch_retry_value = get_fp_config_feature_auth_unmatch_retry();
    int32_t configure_retry_additional_2s_value = get_fp_config_feature_auth_retry_additional_2s();
    int32_t auth_first_enter_no_wait_up = get_fp_config_feature_auth_first_enter_no_waitup();
    int32_t additional_2s_retry_processing = 0;
    int32_t capture_style = CAPTURE_STYLE_WAIT_DOWN | CAPTURE_STYLE_WAIT_UP;
    int32_t img_quality = 0;
    static struct timeval previous_authentication_time = {0};
    struct timeval retry_additional_2s_start_time = {0};
    int32_t unmatch_msg_pending = 0;
    int32_t report_unmatch_throshold = 1;
    int32_t unmatch_minus2_times = 0;
    int32_t finger_status = 0;
    if(auth_config_unmatch_retry_value)
        report_unmatch_throshold = get_fp_config_feature_auth_unmatch_retry_times();

    if(auth_first_enter_no_wait_up)
        capture_style = CAPTURE_STYLE_WAIT_DOWN;

    LOGD(FPTAG"authenticate invoked,auth_config_unmatch_retry_value=%d,auth_first_enter_no_wait_up = %d,configure_retry_additional_2s_value=%d",
                auth_config_unmatch_retry_value,auth_first_enter_no_wait_up,configure_retry_additional_2s_value);

    ret = ta_proxy_instance->begin_authenticate(challenge_id);

    if (ret != 0)
    {
        construct_error_msg(msg, FINGERPRINT_ERROR_UNABLE_TO_PROCESS);
        daemon_instance->notify(&msg);
    }
    else
    {
        int32_t MAX_RETRY_TIME = 1;
        int32_t authenticate_remain_times = MAX_RETRY_TIME;

RETRY_CAPTURE:
//        iAuthenticateRemainRetry--;
        match_result = 0;
        match_finger = -1;
        LOGD(FPTAG"mallocCnt =%d", get_malloc_cnt());
        ret = capture_img(capture_style);
        finger_status++;
        identify_result_t& identify_result = get_cur_identify_result();
        reset_global_identify_result();
        identify_result.operation = 'A';
        get_current_timestamp(identify_result.timestamp_str, sizeof(identify_result.timestamp_str));
        identify_result.timestamp = atol(identify_result.timestamp_str);
        snprintf(identify_result.image_file_name, sizeof(identify_result.image_file_name), "%s",
                 get_capture_img_store_file_full_name());

//if the capture style is no wait  and  capture img failed due to no finger
        if( ret == -MSG_TYPE_FINGER_MOVE )
        {
            finger_status = 0;
            if(auth_config_unmatch_retry_value && unmatch_msg_pending)
            {
                construct_match_msg(msg, gid, 0, token);
                daemon_instance->notify(&msg);
                report_auth_result(0,0);
                unmatch_msg_pending = 0;
                identify_result.finger_status = finger_status; //up
                identify_result.result = MATCH_RESULT_FAILED;
                identify_result.predict_result = -1;
                identify_result.unmatch_times = report_unmatch_throshold - 1;
                identify_result.report_msg_type = AUTHENTICATE_REPORT_MSG_TYPE_UNMATCH;
                log_statistical_result(&identify_result);
                LOGD(FPTAG"defer fingerup detected, will report unmatch msg");
            }
            unmatch_times = 0;
            unmatch_minus2_times = 0;
            capture_style = CAPTURE_STYLE_WAIT_DOWN;
            LOGD(FPTAG"fingerup detected,unmatch_times=%d,capture_style=%d",unmatch_times,capture_style);

            if(additional_2s_retry_processing)
            {
                additional_2s_retry_processing = 0;
                LOGD(FPTAG"fingerup detected, additional_2s_retry_processing = 0, stop additonal 2s retry");
            }

            goto RETRY_CAPTURE;
        }

        if (ret == 0)
        {
            struct timeval authenticate_start_time = ca_timer_start(NULL);
            ret = ta_proxy_instance->authenticate(gid, &match_result, &match_finger, false, &token);
            ca_timer_end(FPTAG" KPI authenticate time", authenticate_start_time);

            if(get_fp_config_feature_record_statistical())
            {
                int32_t d1,d2;
                img_quality = 0;
                ta_proxy_instance->get_image_quality(&d1,&d2,&img_quality);
            }

            LOGD(FPTAG"authenticate iRet = %d ,matchResult =%d" , ret, match_result);
            if (ret == 0)
            {
                if (match_result > 0)   //match    :match_result == 1, update template, match_result == 2  do no update template
                {
                    if(!ta_proxy_instance->get_stop_status())
                    {
                        construct_match_msg(msg, gid, match_finger, token);
                        daemon_instance->notify(&msg);
                        report_auth_result(match_finger,0);
                    }
                    else
                    {
                        LOGI(FPTAG"Authenticate finihsed, but canceled");
                    }

                    int32_t time_elpased_from_last_auth = ca_timer_end(NULL, previous_authentication_time);
                    previous_authentication_time = ca_timer_start(NULL);

                    int32_t allow_update_template = 1; //default allow update templates
                    if(get_fp_config_feature_auth_hal_contorl_update_tplt())
                    {
                        //if elapsed time > 1s, then allow update templates
                        allow_update_template = (time_elpased_from_last_auth > 1000);
                    }
                    if(match_result == MATCH_RESULT_SUCCEED_NO_UPDATE) allow_update_template = 0;

                    LOGD(FPTAG"time_elpased_from_last_auth = %d,match_result = %d, allow_update_template value = %d",
                        time_elpased_from_last_auth,match_result,allow_update_template);

                    ta_proxy_instance->end_authenticate(&is_updated,allow_update_template);
                    identify_result.finger_status = finger_status;
                    identify_result.result = is_updated ? MATCH_RESULT_SUCCEED_UPDATE : MATCH_RESULT_SUCCEED_NO_UPDATE;
                    identify_result.quality = img_quality;
                    identify_result.unmatch_times = 0;
                    identify_result.report_msg_type = AUTHENTICATE_REPORT_MSG_TYPE_MATCH;
                    log_statistical_result(&identify_result);

                    if (get_fp_config_feature_auth_success_continuous())
                    {
                        LOGD(FPTAG"fpTacImpl:authenticate match, retry");
                        unmatch_times = 0;
                        unmatch_minus2_times = 0;
                        finger_status = 0;
                        capture_style = CAPTURE_STYLE_WAIT_UP | CAPTURE_STYLE_WAIT_DOWN;
                        goto RETRY_CAPTURE;
                    }
                }
                else     //unmatch
                {
                    int32_t tmp_unmatch_times = unmatch_times;
                    if(match_result != MATCH_RESULT_FAILED_POOR_QUALITY)
                    {
                        ++unmatch_times;
                    }
                    else
                    {
                        ++unmatch_minus2_times;
                    }

                    int32_t need_report_msg = ( (!auth_config_unmatch_retry_value && match_result != MATCH_RESULT_FAILED_POOR_QUALITY) || 
                                        ( unmatch_times == report_unmatch_throshold && (tmp_unmatch_times == (unmatch_times - 1))) );
                    unmatch_msg_pending = auth_config_unmatch_retry_value &&
                                        (unmatch_times < report_unmatch_throshold ) &&
                                        (match_result != MATCH_RESULT_FAILED_POOR_QUALITY);

                    LOGD(FPTAG"authenticate unmatch mr=%d,tmp_times=%d,ut=%d,nrm=%d,msg_pending=%d,unmatch_minus2_times=%d",
                        match_result,tmp_unmatch_times,unmatch_times,need_report_msg,unmatch_msg_pending,unmatch_minus2_times);

                    if(need_report_msg) //when unmatch_times first time change to report_unmatch_throshold, then report unmatch msg
                    {
                        if(!ta_proxy_instance->get_stop_status())
                        {
                            construct_match_msg(msg, gid, 0, token); //fid = 0 means no match
                            daemon_instance->notify(&msg);
                            report_auth_result(0,0);
                        }
                    }
                    LOGD(FPTAG"make statistical info img_quality = %d ,unmatch_times =%d,report_msg = %d" ,
                        img_quality, unmatch_times,need_report_msg ? 2 : 0);

                    ta_proxy_instance->end_authenticate(&is_updated,0);
                    identify_result.result = match_result;// match result should <= 0
                    identify_result.quality = img_quality;
                    identify_result.unmatch_times = tmp_unmatch_times;
                    identify_result.report_msg_type = (need_report_msg ? AUTHENTICATE_REPORT_MSG_TYPE_UNMATCH : AUTHENTICATE_REPORT_MSG_TYPE_UNREPORT);
                    identify_result.finger_status = finger_status;
                    log_statistical_result(&identify_result);

                    if (authenticate_remain_times > 0)
                    {
                        if(auth_config_unmatch_retry_value)
                        {
                            if(need_report_msg) //if msg reported, then wait finger up and down
                            {
                                if(configure_retry_additional_2s_value)
                                {
                                    LOGD(FPTAG"additional_2s_retry_processing = 1, start additonal 2s retry");
                                    retry_additional_2s_start_time = ca_timer_start(NULL);
                                    additional_2s_retry_processing = 1;
                                }
                                else
                                {
                                    capture_style = CAPTURE_STYLE_WAIT_UP | CAPTURE_STYLE_WAIT_DOWN;
                                    unmatch_times = 0;
                                    unmatch_minus2_times = 0;
                                    finger_status = 0;
                                }
                            }
                            else
                            {
                                capture_style = CAPTURE_STYLE_NO_WAIT;
                                if(configure_retry_additional_2s_value && additional_2s_retry_processing)
                                {
                                    if(ca_timer_end(NULL,retry_additional_2s_start_time) > 2 * 1000)
                                    {
                                        additional_2s_retry_processing = 0;
                                        capture_style = CAPTURE_STYLE_WAIT_UP | CAPTURE_STYLE_WAIT_DOWN;
                                        unmatch_times = 0;
                                        unmatch_minus2_times = 0;
                                        finger_status = 0;
                                        LOGD(FPTAG"additional_2s_retry_processing = 0, stop additonal 2s retry");
                                    }
                                }

                                if(unmatch_minus2_times > 10)
                                {
                                    additional_2s_retry_processing = 0;
                                    capture_style = CAPTURE_STYLE_WAIT_UP | CAPTURE_STYLE_WAIT_DOWN;
                                    unmatch_times = 0;
                                    unmatch_minus2_times = 0;
                                    finger_status = 0;
                                    LOGD(FPTAG"unmatch result = -2 accumulate > 10,retry and wait fingerup");
                                }
                            }
                        }
                        else
                        {
                            unmatch_times = 0;
                            unmatch_minus2_times = 0;
                            finger_status = 0;
                            capture_style = CAPTURE_STYLE_WAIT_UP | CAPTURE_STYLE_WAIT_DOWN;
                        }
                        LOGD(FPTAG" authenticate unmatch retry unmatch_times=%d,capture_style= %d",unmatch_times,capture_style);
                        goto RETRY_CAPTURE;
                    }
                }
            }
            else if (-FP_ERROR_USER_CANCEL == ret)
            {
                LOGE(FPTAG"authenticate canceld ,exit authenticate process");
            }
            else     //error
            {
                ta_proxy_instance->end_authenticate(&is_updated,0);
                identify_result.result = MATCH_RESULT_FAILED;
                identify_result.quality = img_quality;
                identify_result.unmatch_times = unmatch_times;
                identify_result.report_msg_type = AUTHENTICATE_REPORT_MSG_TYPE_ERROR;
                identify_result.finger_status = finger_status;
                log_statistical_result(&identify_result);

                construct_error_msg(msg, FINGERPRINT_ERROR_UNABLE_TO_PROCESS);
                daemon_instance->notify(&msg);
            }
        }
        else
        {
            if (ret == -FP_ERROR_USER_CANCEL)
            {
                LOGE(FPTAG"fpTacImpl:authenticate canceld ,exit authenticate process");
            }
            else if(ret == -ENOENT)
            {
                construct_error_msg(msg, FINGERPRINT_ERROR_HW_UNAVAILABLE);
                daemon_instance->notify(&msg);
            }
            else if (authenticate_remain_times > 0)
            {
                LOGD(FPTAG"fpTacImpl:authenticate capture image acquired retry");
                usleep(20 * 1000);
                goto RETRY_CAPTURE;
            }
            else
            {
                LOGE(FPTAG"fpTacImpl: capture 3 times all failed, notify user FINGERPRINT_ERROR_HW_UNAVAILABLE error");
                construct_error_msg(msg, FINGERPRINT_ERROR_HW_UNAVAILABLE);
                daemon_instance->notify(&msg);
            }
        }
    }
//OUT:
    if (!is_updated)
    {
        //lzk I don't know why, but if i remove this line the framework has some bugs, it can't start the next authentate process
        usleep(15 * 1000);
    }

    LOGD(FPTAG"authenticate leave");
    return 0;//iRet;
}

int32_t fpTacImpl::get_enrolled_fids(int32_t *enrolled_fids_array, int32_t array_len,
                                     int32_t *fids_cnt)
{
    LOGD(FPTAG"get_enrolled_fids invoked");
    return ta_proxy_instance->get_enrolled_fids(enrolled_fids_array, array_len, fids_cnt);
}

int32_t fpTacImpl::set_active_group(int32_t gid, char *path)
{
    int32_t ret = 0;
    active_gid = gid;
    ret = ta_proxy_instance->set_active_group(gid, path);
    return ret;
}

int32_t fpTacImpl::navigation_loop(void)
{
    return ta_proxy_instance->nav_loop();
}

uint64_t fpTacImpl::pre_enroll(void)
{
    return ta_proxy_instance->pre_enroll();
}

int32_t fpTacImpl::post_enroll(void)
{
    return ta_proxy_instance->post_enroll();
}

#if (TARGET_ANDROID>=7)
int32_t fpTacImpl::enumerate(void)
{
    int32_t ret = 0;
    int32_t fid_array[FP_CONFIG_MAX_ENROLL_SLOTS];
    int32_t fid_cnt;
    fingerprint_msg_t msg;
    get_enrolled_fids(fid_array,FP_CONFIG_MAX_ENROLL_SLOTS,&fid_cnt);

    for(int i = 0; i < fid_cnt; i++)
    {
        if(ta_proxy_instance->get_stop_status())
        {
            LOGD(FPTAG"enumerating is interrupted");
            return -FP_ERROR_USER_CANCEL;
        }

        construct_enumerating_msg(msg, active_gid,fid_array[i], fid_cnt - 1 - i);
        daemon_instance->notify(&msg);
    }

    return ret;
}
#endif

void fpTacImpl::finger_detect(void_callback cb)
{
    LOGD(FPTAG"finger_detect invoked");
    int32_t ret = ta_proxy_instance->finger_detect_test();

    if(cb)
    {
        try
        {   if(ret != -FP_ERROR_USER_CANCEL)
            {
                cb(ret);
            }
            else
            {
                LOGD(FPTAG"finger_detect canceled by user");
            }
        }
        catch(...)
        {
            LOGE(FPTAG"finger_detected ,exception when notify to UI");
        }
    }
    else
    {
        LOGE(FPTAG"finger_detected ,but cb is null, can't report to UI");
    }
    return;
}

void fpTacImpl::sync_fp_with_xml(void)
{
    extern int32_t get_fp_list_from_xml(char *xml_file,int32_t *fp_array,int32_t *fp_array_size);

    int32_t xml_fid_array[FP_CONFIG_MAX_ENROLL_SLOTS];
    int32_t xml_fid_array_size = FP_CONFIG_MAX_ENROLL_SLOTS;

    int32_t fp_tplt_array[FP_CONFIG_MAX_ENROLL_SLOTS];
    int32_t fp_tplt_array_size = FP_CONFIG_MAX_ENROLL_SLOTS;

    int32_t fp_sync_mark[FP_CONFIG_MAX_ENROLL_SLOTS];

    char xml_file[PATH_MAX];//="/data/system/users/0/settings_fingerprints.xml";
/*    if(active_gid != 0)
    {
        LOGE(FPTAG"current gid:%d, abort sync", active_gid);
        return;
    }
*/
    snprintf(xml_file,PATH_MAX,"/data/system/users/%d/settings_fingerprint.xml",active_gid);
    LOGD(FPTAG"current user fp setting xml:%s",xml_file);

    if(1)
    {
        int32_t result = get_fp_list_from_xml(xml_file,xml_fid_array,&xml_fid_array_size);
        if(result != 0)
        {
            LOGE(FPTAG"get_fp_list_from_xml error:%d", result);
            return;
        }

        result = get_enrolled_fids(fp_tplt_array,FP_CONFIG_MAX_ENROLL_SLOTS,&fp_tplt_array_size);
        if(result != 0)
        {
            LOGE(FPTAG"get_enrolled_fids error:%d", result);
            return;
        }

        memset(fp_sync_mark,0,sizeof(fp_sync_mark));
        for(int idx = 0; idx < fp_tplt_array_size; idx++)
        {
            int cur_fid = fp_tplt_array[idx];
            for(int xml_idx = 0; xml_idx < xml_fid_array_size; xml_idx++)
            {
                if(xml_fid_array[xml_idx] == cur_fid)
                {
                    fp_sync_mark[idx] = 1;
                    break;
                }
            }
        }

        LOGD(FPTAG" fp list in db, total:%d",fp_tplt_array_size);
        for(int idx = 0; idx < fp_tplt_array_size; idx++)
        {
            LOGD(FPTAG"          :%x",fp_tplt_array[idx]);
        }
        LOGD(FPTAG" fp list in xml, total:%d",xml_fid_array_size);
        for(int idx = 0; idx < xml_fid_array_size; idx++)
        {
            LOGD(FPTAG"          :%x",xml_fid_array[idx]);
        }

        for(int idx = 0; idx < fp_tplt_array_size; idx++)
        {
            if(fp_sync_mark[idx] == 0)
            {
                LOGE(FPTAG"found a misSync fid:%x,how to process it? TBD",fp_tplt_array[idx]);
            }
        }
    }
    return;
}

int32_t fpTacImpl::service_control(int32_t tag, int32_t value)
{
    LOGD(FPTAG"service_control invoked tag=%d, value=%d", tag, value);
    //for rongqi only
    if (tag == FP_SERVICE_CONTROL_CMD_ENROLL_PAUSE)
    {
        LOGD(FPTAG"service_control pause enroll");
        enroll_state = false;
        return 0;
    }
    else if (tag == FP_SERVICE_CONTROL_CMD_ENROLL_RESUME)
    {
        LOGD(FPTAG"service_control resume enroll");
        enroll_state = true;
        return 0;
    }
    //for rongqi only


    return ta_proxy_instance->service_control(tag, value);
}

fpTestIntf *fpTacImpl::get_test_intf()
{
    return ta_proxy_instance->get_test_intf();
}

int32_t fpTacImpl::report_auth_result(int32_t fid, int32_t auth_time)
{
    if(g_enable_report_auth_result)
    {
        if(daemon_instance && daemon_instance->extension_instance)
        {
            daemon_instance->extension_instance->report_auth_result(fid,0);
        }
    }

    return 0;
}

const char *fpTacImpl::get_acquire_msg_desc(int32_t acquire_code)
{                
    const char *msg = NULL;
    
#define ASSIGN_STRING( CMD )    \
        case CMD:               \
            msg = #CMD;         \
            break;

    switch (acquire_code)
    {
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_GOOD);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_PARTIAL);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_INSUFFICIENT);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_IMAGER_DIRTY);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_TOO_SLOW);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_TOO_FAST);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_CAPTURE_MOVE);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_CAPTURE_FINGERDOWN);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_CAPTURE_FINGERUP);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_CAPTURE_UNKNOWN);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_ALGO_4103);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_ALGO_4104);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_ALGO_IMAGEENROLLED);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_ALGO_UNKNOWN);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_ALGO_FILL_IND_UP);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_ALGO_FILL_IND_DOWN);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_ALGO_FILL_IND_LEFT);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_ALGO_FILL_IND_RIGHT);
        ASSIGN_STRING(FINGERPRINT_ACQUIRED_VENDOR_ALGO_FILL_IND_UNKNOWN);

        default:
            msg = "FINGERPRINT_ACQUIRED_ERROR_UNKNOWN";
            break;
    }
    return msg;
}

void fpTacImpl::construct_acquire_msg(fingerprint_msg_t &msg, int32_t acquire_code)
{
    LOGI(FPTAG" construct_acquire_msg,Acquirecode=%d->%s",acquire_code, get_acquire_msg_desc(acquire_code));
    memset(&msg, 0, sizeof(fingerprint_msg_t));
    msg.type = FINGERPRINT_ACQUIRED;

    msg.data.acquired.acquired_info = (fingerprint_acquired_info_t)acquire_code;
}

void fpTacImpl::construct_match_msg(fingerprint_msg_t &msg, int32_t gid, int32_t fid,
                                    hw_auth_token_t &token)
{
    LOGI(FPTAG" construct_match_msg,ifid=0x%x", fid);
    memset(&msg, 0, sizeof(fingerprint_msg_t));

    msg.type = FINGERPRINT_AUTHENTICATED;
    msg.data.authenticated.finger.fid = fid;
    msg.data.authenticated.finger.gid = gid;
    msg.data.authenticated.hat   = token;
    LOGD_HAT(&msg.data.authenticated.hat);
}

#if (TARGET_ANDROID>=7)
void fpTacImpl::construct_enumerating_msg(fingerprint_msg_t &msg, int32_t gid, int32_t fid, int32_t remaining)
{
    LOGI(FPTAG" construct_enumerating_msg,gid = %d ,fid=0x%x,remaining=%d", gid,fid,remaining);
    memset(&msg, 0, sizeof(fingerprint_msg_t));

    msg.type = FINGERPRINT_TEMPLATE_ENUMERATING;
    msg.data.enumerated.finger.gid = gid;
    msg.data.enumerated.finger.fid = fid;
    msg.data.enumerated.remaining_templates = remaining;
    return;
}
#endif

void fpTacImpl::construct_error_msg(fingerprint_msg_t &msg, int32_t error_code)
{
    LOGI(FPTAG" construct_error_msg,ErrorCode=%d", error_code);
    memset(&msg, 0, sizeof(fingerprint_msg_t));
    msg.type = FINGERPRINT_ERROR;
    msg.data.error = (fingerprint_error)error_code;
}

void fpTacImpl::construct_remove_msg(fingerprint_msg_t &msg, int32_t gid, int32_t fid)
{
    LOGI(FPTAG" construct_remove_msg,gid=%d, fid = 0x%x", gid, fid);
    memset(&msg, 0, sizeof(fingerprint_msg_t));
    msg.type = FINGERPRINT_TEMPLATE_REMOVED;
    msg.data.removed.finger.gid = gid;
    msg.data.removed.finger.fid = fid;
}
void fpTacImpl::construct_enrolling_msg(fingerprint_msg_t &msg, int32_t gid, int32_t fid,
                                        int32_t enrolled_cnt, int32_t total_cnt)
{
    LOGI(FPTAG" construct_enrolling_msg,gid=%d, fid = 0x%x, iCurEnrolledCnt=%d,iTotalEnrollCnt=%d", gid,
         fid, enrolled_cnt, total_cnt);
    memset(&msg, 0, sizeof(fingerprint_msg_t));
    msg.type = FINGERPRINT_TEMPLATE_ENROLLING;
    msg.data.enroll.finger.gid = gid;
    msg.data.enroll.finger.fid = fid;
    if(enrolled_cnt == 0)
        msg.data.enroll.samples_remaining = (total_cnt - 1);
    else
        msg.data.enroll.samples_remaining = (total_cnt - enrolled_cnt);

    return;
}
