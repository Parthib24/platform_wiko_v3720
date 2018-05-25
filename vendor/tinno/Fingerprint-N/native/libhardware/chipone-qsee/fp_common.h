#ifndef FP_COMMON_H
#define FP_COMMON_H

#include <time.h>
#include <stdio.h>
#include "hardware.h"
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include "fp_common_external.h"
#include "fp_tac_impl.h"
#include "fp_property.h"
#include <errno.h>

#define FPDAEMON_VERSION "V1.3.11-M170511-" __DATE__ " " __TIME__

#define TEMPLATE_ID_MAX_SIZE            (62)

#define MATCH_RESULT_SUCCEED_UPDATE         1
#define MATCH_RESULT_SUCCEED_NO_UPDATE      2
#define MATCH_RESULT_FAILED                 0
#define MATCH_RESULT_FAILED_POOR_QUALITY    -2

#ifndef VOID_CALLBACK
#define VOID_CALLBACK
typedef void (*void_callback)(int);
#endif

#define AUTHENTICATE_REPORT_MSG_TYPE_UNREPORT   0
#define AUTHENTICATE_REPORT_MSG_TYPE_MATCH      1
#define AUTHENTICATE_REPORT_MSG_TYPE_UNMATCH    2
#define AUTHENTICATE_REPORT_MSG_TYPE_ERROR      3

typedef struct identify_result
{
    unsigned long long timestamp;
    char timestamp_str[28];
    int template_id;//fid
    int result;     //MATCH_RESULT_SUCCEED_UPDATE ; MATCH_RESULT_SUCCEED_NO_UPDATE ; else for failed for authentication
    int score;
    int updated_template_size;
    int homekey_down;
    int quality;
    char operation; //'A' :authenticate 'E': enroll 'D' :delete
    unsigned char report_msg_type;
    unsigned char unmatch_times;
    unsigned char cur_enroll_cnt;
    unsigned char total_enroll_cnt;
    unsigned char finger_status; //0 up, 1 first down, 2 retry
    unsigned char reserved1;
    unsigned char reserved2;
    int predict_result; //just for authentiate
    int esd_exception;
    int reserved[1]; //for future use
    char image_file_name[128];
    identify_result *next;
} identify_result_t;


typedef struct fp_global_insatnce
{
    fp_event_callback_t fp_internal_callback; //callback 
    FILE *log_fp; //internla log file fd
    char captured_image_file_name[PATH_MAX];

} fp_global_insatnce_t;


//error code
#define FP_ERROR_USER_CANCEL 2016


#define FP_CONFIG_MAX_ENROLL_SLOTS 20


//func declaration
extern int32_t swap_bytes(uint8_t *buf, int32_t size);
extern int32_t fp_random(uint8_t *buf, int32_t size);
extern void fp_random_seed(void);
extern struct timeval ca_timer_start(const char *info);
extern int ca_timer_end(const char *fmt, struct timeval time_start);
extern int get_malloc_cnt(void);
extern void *fp_malloc(size_t size);
extern void fp_free(void *buf);
extern void log_statistical_result(identify_result_t *result);
extern void create_log_file(void);
extern int32_t recursive_create_path(const char *tmp_path);
extern fpTaProxy *create_ta_proxy(fpTacImpl *tac_impl);
extern int32_t save_bmp (const char *file_name, char *img_buffer, int img_width, int img_height);
extern bool is_64bit_system(void);
extern int fp_hw_get_module(const char *id, const struct hw_module_t **module);

extern bool get_fp_config_report_detail_msg(void);
extern bool get_fp_config_feature_navigator(void);
extern bool get_fp_config_forbidden_duplicated_finger_enroll(void);
extern bool get_fp_config_feature_auth_success_continuous(void);
extern bool get_fp_config_feature_record_statistical(void);
extern bool get_fp_config_feature_store_captured_img(void);
extern int32_t get_fp_config_enroll_finger_slot_number(void);
extern bool get_fp_config_feature_auth_first_enter_no_waitup(void);
extern bool get_fp_config_feature_auth_unmatch_retry(void);
extern int32_t get_fp_config_feature_auth_unmatch_retry_times(void);
extern bool get_fp_config_feature_auth_hal_contorl_update_tplt(void);
extern bool get_fp_config_feature_auth_retry_additional_2s(void);
extern bool get_fp_config_feature_enhancement_bmp_before_save(void);
extern void print_fp_config_table(void);
extern const char *get_authenticate_msg_desc(int32_t report_msg);
extern const char *get_capture_img_store_file_full_name(void);
extern void bmp_enhancement(char * img_buffer, int32_t w, int32_t h);

extern void log_delete_fid_statistical_info(uint32_t finger_id);
extern void log_authenticate_statistical_info(int32_t isTemplateUpdated,int32_t match_score,int32_t match_result,int32_t match_fid,
                                              int32_t tplt_len, int32_t extra, int32_t predict_result);
extern void reset_global_identify_result(void);
extern identify_result_t& get_cur_identify_result(void);
extern void init_json_file_header(int fd);
extern void append_json_item(int fd,identify_result_t *result,int32_t is_first_line);

extern fp_global_insatnce_t fp_global_env;
extern int32_t g_inject_enabled;
extern int32_t g_inject_img_idx;
extern int32_t g_enable_report_auth_result;

#endif
