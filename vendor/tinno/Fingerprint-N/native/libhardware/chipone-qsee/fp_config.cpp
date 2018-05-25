#include <unistd.h>
#include "fp_common.h"
#include "fp_log.h"
#include "string.h"


#define FPTAG "fp_config.cpp "

extern fp_config_item_t fp_system_config_table[];

static int32_t get_fp_config_item_idx(const char *config_desc)
{
    int32_t idx = 0;
    fp_config_item_t *item_ptr = fp_system_config_table;
    do
    {
        if (item_ptr->item_desc == NULL)
        {
            break;
        }

        if (strcmp(item_ptr->item_desc, config_desc) == 0)
        {
            return idx;
        }
        ++idx;
        ++item_ptr;
    }
    while (true);

    return 0;
}

#define GET_CONFIG_VALUE(config_item)                               \
    static int32_t config_item##_idx = -1;                          \
    int32_t config_item##_value = 0;                                \
    if(config_item##_idx == -1)                                     \
    {                                                               \
        config_item##_idx = get_fp_config_item_idx(config_item);    \
    }                                                               \
    config_item##_value = fp_system_config_table[config_item##_idx].config_value;


bool get_fp_config_report_detail_msg(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_REPORT_DETAIL_MSG);
    return FP_CONFIG_REPORT_DETAIL_MSG_value;
}

bool get_fp_config_feature_navigator(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_FEATURE_NAVIGATOR);
    return FP_CONFIG_FEATURE_NAVIGATOR_value;
}

bool get_fp_config_forbidden_duplicated_finger_enroll(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_FORBIDDEN_DUPLICATED_FINGER_ENROLL);
    return FP_CONFIG_FORBIDDEN_DUPLICATED_FINGER_ENROLL_value;
}

bool get_fp_config_feature_auth_success_continuous(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_FEATURE_AUTH_SUCCESS_CONTINUOUS);
    return FP_CONFIG_FEATURE_AUTH_SUCCESS_CONTINUOUS_value;
}

bool get_fp_config_feature_record_statistical(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_RECORD_STATISTICAL);
    return FP_CONFIG_RECORD_STATISTICAL_value;
}

bool get_fp_config_feature_auth_unmatch_retry(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY);
    return FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY_value;
}

int32_t get_fp_config_feature_auth_unmatch_retry_times(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY_TIMES);
    int32_t tmp_value = FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY_TIMES_value;
    if(get_fp_config_feature_auth_unmatch_retry())
    {
        if(tmp_value <= 0)
        {
            LOGI(FPTAG"caution configuratin item FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY_TIMES and \
                 FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY is misconfigured, check it");
            tmp_value = 1;
        }
        else if(tmp_value > 3)
        {
            LOGI(FPTAG"caution configuratin item FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY_TIMES and \
                 FP_CONFIG_FEATURE_AUTH_UNMATCH_RETRY is misconfigured, check it");
            tmp_value = 3;
        }
    }

    return tmp_value;
}

bool get_fp_config_feature_store_captured_img(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_STORE_CAPTURED_IMG);
    return FP_CONFIG_STORE_CAPTURED_IMG_value;
}

bool get_fp_config_feature_auth_first_enter_no_waitup(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_FEATURE_AUTH_FIRST_ENTER_NO_WAIT_UP);
    return FP_CONFIG_FEATURE_AUTH_FIRST_ENTER_NO_WAIT_UP_value;
}

bool get_fp_config_feature_auth_hal_contorl_update_tplt(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_FEATURE_AUTH_HAL_CONTROL_UPDATE_TPLT);
    return FP_CONFIG_FEATURE_AUTH_HAL_CONTROL_UPDATE_TPLT_value;
}

bool get_fp_config_feature_auth_retry_additional_2s(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_FEATURE_AUTH_RETRY_ADDITIONAL_2S);
    return FP_CONFIG_FEATURE_AUTH_RETRY_ADDITIONAL_2S_value;
}

bool get_fp_config_feature_enhancement_bmp_before_save(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_FEATURE_ENHANCEMENT_BMP_BEFORE_SAVE);
    return FP_CONFIG_FEATURE_ENHANCEMENT_BMP_BEFORE_SAVE_value;
}

int32_t get_fp_config_enroll_finger_slot_number(void)
{
    GET_CONFIG_VALUE(FP_CONFIG_MAX_ENROLL_FINGER_SLOT_NUMBER);
    int slot_number = FP_CONFIG_MAX_ENROLL_FINGER_SLOT_NUMBER_value;
    if (slot_number < 5 || slot_number > 20)
    {
        LOGE(FPTAG"\n\n\n\n fp config ERROR %s invoked, but number should be [5,20], current is:%d, force return 5 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n",
             __func__, slot_number);
        return 5;
    }
    return slot_number;
}

void print_fp_config_table(void)
{
    LOGI(FPTAG"fp_system_config_table: ");
    fp_config_item_t *item_ptr = fp_system_config_table;
    do
    {
        if (item_ptr->item_desc == NULL)
        {
            break;
        }

        LOGI(FPTAG"		%s : %d ", item_ptr->item_desc, item_ptr->config_value);
        ++item_ptr;
    }
    while (true);
    return;
}

const char *get_capture_img_store_file_full_name(void)
{
    if(fp_global_env.captured_image_file_name[0] != 0)
        return fp_global_env.captured_image_file_name;
    else
        return "/sdcard/fp_img.bmp";
}

static char *get_log_file_name()
{
    static char log_file_name[PATH_MAX];
    char time_stamp[25];
    memset(log_file_name,0,sizeof(log_file_name));
    snprintf(log_file_name,PATH_MAX,"%s/%s.log",
        fp_global_env.fp_internal_callback.get_fp_debug_base_dir(),
        get_current_timestamp(time_stamp,sizeof(time_stamp)));

    return log_file_name;
}

void create_log_file(void)
{
    if(!fp_global_env.log_fp)
    {
        char *log_file_name = get_log_file_name();
        if(log_file_name)
        {
            fp_global_env.log_fp = fopen(log_file_name,"w");
            LOGD(FPTAG" open log file :%p, errno:%d",(void*)fp_global_env.log_fp,-errno);
        }
    }
}

const char *get_authenticate_msg_desc(int32_t report_msg)
{
    switch(report_msg)
    {
        case AUTHENTICATE_REPORT_MSG_TYPE_UNREPORT:
            return "Unreport";
        case AUTHENTICATE_REPORT_MSG_TYPE_MATCH:
            return "Match   ";
        case AUTHENTICATE_REPORT_MSG_TYPE_UNMATCH:
            return "Unmatch ";
        case AUTHENTICATE_REPORT_MSG_TYPE_ERROR:
            return "Error   ";
    }
    return "Unknown ";
}

//#define LOG_2_FILE
#define LOG_BUF_SIZE 512
void log_2_file(int log_level, const char * log_tag, const char *fmt, ...)
{
#ifdef LOG_2_FILE

    if(!fp_global_env.log_fp)
    {
        create_log_file();
    }

    if(!fp_global_env.log_fp)
        return;

    char time_stamp[25];
    va_list ap;
    char buf[LOG_BUF_SIZE];
    memset(buf,0,sizeof(buf));
    va_start(ap, fmt);
    vsnprintf(buf, LOG_BUF_SIZE, fmt, ap);
    va_end(ap);

    char level = '0';
    switch(log_level)
    {
        case ANDROID_LOG_DEBUG:
            level = 'D';
            break;
        case ANDROID_LOG_INFO:
            level = 'I';
            break;
        case ANDROID_LOG_ERROR:
            level = 'E';
            break;
        default:
            break;
    }
    get_current_timestamp(time_stamp,sizeof(time_stamp));
    fprintf(fp_global_env.log_fp, "%s: %c %s %s\n",time_stamp, level, log_tag, buf);
#endif

    return;
}

void internal_on_fp_init(void* user_data)
{
    customer_callback.size = sizeof(fp_event_callback_t);
    customer_callback.priv_user_data = user_data;
    LOGD(FPTAG"internal_on_fp_init invoked");
    if(customer_callback.on_fp_init)
        customer_callback.on_fp_init(user_data);


    return;
}


int32_t internal_on_enroll_duplicate(int32_t *report_code,int32_t *report_type)
{
    if(customer_callback.on_enroll_duplicate)
        return customer_callback.on_enroll_duplicate(report_code,report_type);

    return 0;
}

int32_t internal_on_enroll_finger_same_area(int32_t *report_code)
{
    if(customer_callback.on_enroll_finger_same_area)
        return customer_callback.on_enroll_finger_same_area(report_code);

    return 0;
}

void internal_on_fp_deinit(void)
{
    if(fp_global_env.log_fp)
    {
        fclose(fp_global_env.log_fp);
        fp_global_env.log_fp = NULL;
    }
    if(customer_callback.on_fp_deinit)
        customer_callback.on_fp_deinit();

    return;
}

const char* internal_on_generate_img_file_name(void)
{
    #define NEW_DIR_IMAGE_CNT 5000
    static int32_t accu_cnt = 0;
    if(!get_fp_config_feature_store_captured_img())
        return "/sdcard/fpImg.bmp";

    accu_cnt++;
    if(accu_cnt > NEW_DIR_IMAGE_CNT){
        if(customer_callback.generate_debug_base_dir)
            customer_callback.generate_debug_base_dir();
        LOGD(FPTAG"previous dir stroe image > %d, generate new dir",NEW_DIR_IMAGE_CNT);
        accu_cnt = 0;
    }
    memset(fp_global_env.captured_image_file_name,0,sizeof(fp_global_env.captured_image_file_name));

    char time_stamp[22];
    struct timeval ts;
    gettimeofday(&ts, NULL);

    snprintf(fp_global_env.captured_image_file_name, sizeof(fp_global_env.captured_image_file_name),
        "%s/fpimg_%s_%lu.bmp",
        fp_global_env.fp_internal_callback.get_fp_debug_base_dir(),
        get_current_timestamp(time_stamp,sizeof(time_stamp)),ts.tv_usec );

    LOGD("get image name:%s\n", fp_global_env.captured_image_file_name );

    return fp_global_env.captured_image_file_name;
}

const char *internal_get_fp_debug_base_dir(void)
{
    if(customer_callback.get_fp_debug_base_dir)
    {
        const char *base_dir = customer_callback.get_fp_debug_base_dir();
        if (access(base_dir, 0) != 0)
        {
            customer_callback.generate_debug_base_dir();
            base_dir = customer_callback.get_fp_debug_base_dir();

            LOGI(FPTAG"%s dir is not exist, create it", base_dir);
            recursive_create_path(base_dir);
        }
        return base_dir;
    }

    return "/sdcard";
}

//do not change this part
fp_global_insatnce_t fp_global_env = 
{
    {
        .version = 0,
        .size = 0,
        0,0,
        .priv_user_data = 0,
        .on_fp_init = internal_on_fp_init,

        .on_enroll_duplicate = internal_on_enroll_duplicate,
        .on_enroll_finger_same_area = internal_on_enroll_finger_same_area,
        .on_fp_deinit = internal_on_fp_deinit,
        .on_generate_img_file_name = internal_on_generate_img_file_name,
        .get_fp_debug_base_dir = internal_get_fp_debug_base_dir,
        0,
    },
    0,
};
