#ifndef FP_COMMON_EXTERNAL_H
#define FP_COMMON_EXTERNAL_H


#define FP_CONFIG_REPORT_DETAIL_MSG                     "fp_config_report_detail_msg"
#define FP_CONFIG_FEATURE_NAVIGATOR                     "fp_config_feature_navigator"
#define FP_CONFIG_FORBIDDEN_DUPLICATED_FINGER_ENROLL    "fp_config_forbidden_duplicated_finger_enroll"
#define FP_CONFIG_FEATURE_AUTH_SUCCESS_CONTINUOUS       "fp_config_feature_auth_success_continuous"
#define FP_CONFIG_RECORD_STATISTICAL                    "fp_config_record_statistical"
#define FP_CONFIG_STORE_CAPTURED_IMG                    "fp_config_store_captured_img"
#define FP_CONFIG_MAX_ENROLL_FINGER_SLOT_NUMBER         "fp_config_max_enroll_finger_slot_number"
#define FP_CONFIG_FEATURE_AUTH_FIRST_ENTER_NO_WAIT_UP   "fp_config_feature_auth_first_enter_nowait_up"
#define FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY            "fp_config_feature_auth_unmatch_retry"
#define FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY_TIMES      "fp_config_feature_auth_unmatch_retry_times"
#define FP_CONFIG_FEATURE_AUTH_HAL_CONTROL_UPDATE_TPLT  "fp_config_feature_auth_hal_control_update_tplt"
#define FP_CONFIG_FEATURE_AUTH_RETRY_ADDITIONAL_2S      "fp_config_feature_auth_retry_additional_2s"
#define FP_CONFIG_FEATURE_ENHANCEMENT_BMP_BEFORE_SAVE   "fp_config_feature_enhancement_bmp_before_save"
#define FP_CONFIG_VALUE_FAE_VERSION_SUB                 "fp_config_value_fae_version_sub"
#define FP_CONFIG_VALUE_FAE_CUSTOMER_ID                 "fp_config_value_fae_customer_id"
#define FP_CONFIG_VALUE_FAE_PRODUCT_ID                  "fp_config_value_fae_product_id"


typedef struct fp_config_item
{
    const char *item_desc;
    int32_t config_value;
} fp_config_item_t;


typedef struct fp_event_callback
{
    int32_t version;
    int32_t size;
    int32_t dummy[2];
    void *priv_user_data;

    void (*on_fp_init)(void* user_data);

    //report_type: 0 acquire, continue enroll; != 0 means error, exit enroll progress
    int32_t (*on_enroll_duplicate)(int32_t *report_code,int32_t *report_type);
    //report_code: 0 not report to user, otherwise report code to UI
    int32_t (*on_enroll_finger_same_area)(int32_t *report_code);
    void (*on_fp_deinit)(void);
    const char* (*on_generate_img_file_name)(void);
    const char* (*get_fp_debug_base_dir)(void);
    void (*generate_debug_base_dir)(void);
    void (*reserved_function[16])(void);
} fp_event_callback_t;

extern fp_event_callback_t customer_callback;
extern char *get_current_timestamp(char *buf, int len);


#endif
