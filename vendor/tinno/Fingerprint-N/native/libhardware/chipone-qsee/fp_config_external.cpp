#include <inttypes.h>
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <errno.h>
#include <time.h>
#include <unistd.h>
#include "fp_log.h"
#include "fp_common_external.h"

#define FPTAG "fp_config_external.cpp "

//the type of the following value should be int32_t

#define FAE_VERSION_SUB  0
#define FAE_CUSTOMER_ID  0
#define FAE_PRODUCT_ID   0

extern "C" {

    fp_config_item_t fp_system_config_table[] =
    {
        {"default_config_item",                                         0},        //1lzk do not modify this line
//----------------------------fp_config----------------------------------------------------
        {FP_CONFIG_REPORT_DETAIL_MSG,                                   0},         //(0,1) do not use this feature in 6.0 platform, enable on 5.X
        {FP_CONFIG_FEATURE_NAVIGATOR,                                   0},         //(0,1) according to user requirements
        {FP_CONFIG_FORBIDDEN_DUPLICATED_FINGER_ENROLL,                  0},         //(0,1) if do not want to enroll same finger, enable this feature
        {FP_CONFIG_MAX_ENROLL_FINGER_SLOT_NUMBER,                       5},         //[5~20] how many finger can be enrolled in the database
        {FP_CONFIG_FEATURE_AUTH_FIRST_ENTER_NO_WAIT_UP,                 1},         //(0,1) on first time enter authenticate funcation, do not wait finger up
        {FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY,                          1},         //(0,1) when authentication unmatch, do not wait finger up and retry
            {FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY_TIMES,                2},         //[0~3] when authenticate failed, retry how many times, configurable when FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY = 1
            {FP_CONFIG_FEATURE_AUTH_RETRY_ADDITIONAL_2S,                1},         //(0,1) after auth failed and report unmatch msg, allow to retry additional 2s, configurable when FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY = 1

        {FP_CONFIG_FEATURE_AUTH_HAL_CONTROL_UPDATE_TPLT,                0},         //(0,1) enable or disable hal control update templates when authentication matched
//---------------------------project-------------------------------------------------------
        {FP_CONFIG_FEATURE_AUTH_SUCCESS_CONTINUOUS,                     0},         //(0,1) for ZTE yude this feature enable continuous capture even authentication successes
//---------------------------dev_test------------------------------------------------------
        {FP_CONFIG_RECORD_STATISTICAL,                                  0},         //(0,1) control whether to enable record authentication statistical info
        {FP_CONFIG_STORE_CAPTURED_IMG,                                  0},         //(0,1) test purpose control whether to store the captured image , the default name is /sdcard/fpimg.bmp
        {FP_CONFIG_FEATURE_ENHANCEMENT_BMP_BEFORE_SAVE,                 0},         //(0,1) enable or disable enhancement bmp before save to file, contact "tczhai" for more info
//---------------------------fae-----------------------------------------------------------
        {FP_CONFIG_VALUE_FAE_VERSION_SUB,                 FAE_VERSION_SUB},         //sub  version code for FAE
        {FP_CONFIG_VALUE_FAE_CUSTOMER_ID,                 FAE_CUSTOMER_ID},         //customer id for FAE
        {FP_CONFIG_VALUE_FAE_PRODUCT_ID,                   FAE_PRODUCT_ID},         //product id for FAE

        {NULL,                                                          0},         //do not remove this line, and make it be the last line of this table
    };
    extern void rename_hal(const char *hal_new_name);
}

static char fp_debug_base_dir[PATH_MAX];

void customer_generate_base_dir(void)
{
    char startup_time[22];
    memset(fp_debug_base_dir, 0, sizeof(fp_debug_base_dir));
    snprintf(fp_debug_base_dir,sizeof(fp_debug_base_dir),"/data/fp_data/fp_debug_%s",
             get_current_timestamp(startup_time, sizeof(startup_time)));
    LOGD(FPTAG"customer_on_fp_init invoked,fp_debug_base_dir->%s", fp_debug_base_dir);
}

void customer_on_fp_init(void *user_data)
{
    customer_callback.size = sizeof(fp_event_callback_t);
    customer_callback.priv_user_data = user_data;
    customer_generate_base_dir();
//for FAE additional info print
    LOGI(FPTAG"FAE additional info output:");
    LOGI(FPTAG"H-hal FAE BUILD INFO   sub  version:0x%x",FAE_VERSION_SUB);
    LOGI(FPTAG"H-hal FAE BUILD INFO   customer id :0x%x",FAE_CUSTOMER_ID);
    LOGI(FPTAG"H-hal FAE BUILD INFO   product  id :0x%x",FAE_PRODUCT_ID);

    return;
}

//report_code: the msg code report to UI
//report_type: 0, acquire msg,continue enroll; !=0, error msg, exit from enroll
int32_t customer_on_enroll_duplicate(int32_t *report_code, int32_t *report_type)
{
    if (!report_code || !report_type)
    {
        return -EINVAL;
    }

//    *report_code = ;
//    *report_type = ;
    return 0;
}

//caution:feature depond on algorithm configuration, even the report_code set to no zero
int32_t customer_on_enroll_finger_same_area(int32_t *report_code)
{
    if (!report_code)
    {
        return -EINVAL;
    }

//Hisense need to report this value 1006
//otherwise set to 0
    *report_code = 0;
    return 0;
}

//do not change this part-----------------------------------------------------------------
const char *customer_get_fp_debug_base_dir(void)
{
    return fp_debug_base_dir;
}

fp_event_callback_t customer_callback =
{
    .version = 0,
    .size = 0,
    0, 0,
    .priv_user_data = 0,
    .on_fp_init = customer_on_fp_init,

    .on_enroll_duplicate = customer_on_enroll_duplicate,
    .on_enroll_finger_same_area = customer_on_enroll_finger_same_area,
    .on_fp_deinit = NULL,
    .on_generate_img_file_name = NULL,
    .get_fp_debug_base_dir = customer_get_fp_debug_base_dir,
    .generate_debug_base_dir = customer_generate_base_dir,
    0,
};

#define FINGERPRINT_HAL_NAME  "fpsensor_fingerprint"
static __attribute__((constructor(101))) void init_function_for_fae(void){
    LOGD(FPTAG"#force set hal name to %s",FINGERPRINT_HAL_NAME);
    rename_hal(FINGERPRINT_HAL_NAME);
}

void dummy_for_link(void)
{
    extern int register_android_server_fingerprint_FingerprintService(JNIEnv * env);
    register_android_server_fingerprint_FingerprintService(NULL);
}
//do not change this part-----------------------------------------------------------------
