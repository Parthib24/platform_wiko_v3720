#include <sys/time.h>
#include <sys/stat.h>
#include <string.h>
#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include "fp_common.h"
#include "fp_log.h"

#define FPTAG "fp_auth_statistical.cpp "


#define TIMEBUF_SIZE 21

char *get_current_timestamp(char *buf, int len)
{
    time_t local_time;
    struct tm *tm;

    if (buf == NULL || len < TIMEBUF_SIZE)
    {
        LOGE("Invalid timestamp buffer");
        goto get_timestamp_error;
    }

    /* Get current time */
    local_time = time(NULL);
    if (!local_time)
    {
        LOGE("Unable to get timestamp");
        goto get_timestamp_error;
    }

    tm = localtime(&local_time);
    if (!tm)
    {
        LOGE("Unable to get local time");
        goto get_timestamp_error;
    }

    snprintf(buf, TIMEBUF_SIZE,
             "%04d%02d%02d%02d%02d%02d", tm->tm_year + 1900,
             tm->tm_mon + 1, tm->tm_mday, tm->tm_hour, tm->tm_min,
             tm->tm_sec);

    return buf;

get_timestamp_error:
    return NULL;
}

static int find_next_slash(const char *tmp_path, int32_t paht_len, int32_t start_idx)
{
    int found_idx = -1;
    for (int i = start_idx + 1; i < paht_len; i++)
    {
        if (tmp_path[i] == '/')
        {
            found_idx = i;
            break;
        }
    }

    return found_idx;
}

int32_t recursive_create_path(const char *input_path)
{
    if(!input_path)
        return -EINVAL;

    LOGD(FPTAG"input path:%s",input_path);
    char tmp_path[PATH_MAX];

    if(access(input_path,0) == 0 )
    {
        return 0;
    }

//make the path end with '/'
    if(input_path[strlen(input_path) - 1] != '/')
    {
        snprintf(tmp_path,sizeof(tmp_path),"%s/",input_path);
    }
    else
    {
        snprintf(tmp_path,sizeof(tmp_path),"%s",input_path);
    }

    int path_len = strlen(tmp_path);
    int first_slash = 0;

    char create_dir_path[PATH_MAX];
    memset(create_dir_path, 0, sizeof(create_dir_path));

    int second_slash = find_next_slash(tmp_path, path_len, first_slash);
    while (second_slash > 0 && second_slash < path_len)
    {
        strncpy(create_dir_path, tmp_path, second_slash);
        int ret = mkdir(create_dir_path, 0770);
        if(ret < 0 && errno != EEXIST){
            LOGE(FPTAG"create dir failed:%s, err=%d",create_dir_path,-errno);
            return -errno;
        }
//        int error_code = 
//        LOGD(FPTAG"create sub dir :%s,%d,%d", create_dir_path,error_code,-errno);
        first_slash = second_slash;
        second_slash = find_next_slash(tmp_path, path_len, first_slash);
    }
    return 0;
}


char fp_statiscal_file[PATH_MAX];
char fp_statiscal_bak_file[PATH_MAX];

static const char *get_operation_str(int32_t action)
{
    const char *str = NULL;
    switch(action)
    {
        case 'A':
        case 'a':
            str = "Authen";
            break;
        case 'E':
        case 'e':
            str = "Enroll";
            break;
        case 'D':
        case 'd':
            str = "Delete";
            break;
        default:
            str = "unknown";
            break;
    }
    return str;
}

void log_delete_fid_statistical_info(uint32_t finger_id)
{
    if (get_fp_config_feature_record_statistical())
    {
        reset_global_identify_result();
        identify_result_t& identify_result = get_cur_identify_result();
        get_current_timestamp(identify_result.timestamp_str,sizeof(identify_result.timestamp_str));
        identify_result.operation = 'D';
        identify_result.template_id = finger_id;
        snprintf(identify_result.image_file_name,sizeof(identify_result.image_file_name),"%s","unknown");
        LOGD(FPTAG"statical-> %s %c 0x%x %d %d %d %d %s", identify_result.timestamp_str, identify_result.operation,
            identify_result.template_id, identify_result.result ,identify_result.score, 0,
            identify_result.updated_template_size,identify_result.image_file_name);
        log_statistical_result(&identify_result);
    }
}

static identify_result_t g_cur_identify_result;
//identify_result_t *g_identify_result_list_head;

void reset_global_identify_result(void)
{
    int32_t tmp_esd_exception = g_cur_identify_result.esd_exception;
    memset(&g_cur_identify_result,0,sizeof(g_cur_identify_result));
    //retain the esd_exception value;
    g_cur_identify_result.esd_exception = tmp_esd_exception;
}

identify_result_t& get_cur_identify_result(void)
{
    return g_cur_identify_result;
}

static void log_statical_bigdata(identify_result_t *result)
{
    const char *data_path = fp_global_env.fp_internal_callback.get_fp_debug_base_dir();
    if (data_path == NULL)
    {
        LOGE(FPTAG"%s get statistical path is null,can't record statiscal info!!", __func__);
        return ;
    }

    char tmp_path[PATH_MAX];
    int str_len = strlen(data_path);
    if (str_len >= PATH_MAX - 10)
    {
        LOGE(FPTAG"%s file path length is too long", __func__);
        return ;
    }

    memset(tmp_path, 0, sizeof(tmp_path));
    strncpy(tmp_path, data_path, str_len);

    if (access(tmp_path, 0) == -1)
    {
        LOGI(FPTAG"%s dir is not exist, create it", tmp_path);
        if(recursive_create_path(tmp_path) != 0)
        {
            LOGE(FPTAG"dir %s create failed", tmp_path);
            return; 
        }
    }

    memset(fp_statiscal_file, 0, sizeof(fp_statiscal_file));
    memset(fp_statiscal_bak_file, 0, sizeof(fp_statiscal_bak_file));

    snprintf(fp_statiscal_file, sizeof(fp_statiscal_file), "%s/1.dat", tmp_path);
    snprintf(fp_statiscal_bak_file, sizeof(fp_statiscal_bak_file), "%s/2.dat", tmp_path);

    struct stat statbuf;
    stat(fp_statiscal_file, &statbuf);
    int size = statbuf.st_size;

    if (size > 5 * 1000* 1000)
    {
        LOGD(FPTAG"%s backup fp statistical file", __func__);
        remove(fp_statiscal_bak_file);
        rename(fp_statiscal_file, fp_statiscal_bak_file);
    }

    FILE* fd = fopen(fp_statiscal_file,"a+");//open(fp_statiscal_file, O_CREAT | O_WRONLY | O_APPEND, 0777);

    if (fd > 0)
    {
        fseek(fd,0,SEEK_END);
        /* LOGD("%s open file success", __func__); */
        fprintf(fd, "%s  \t %s  \t ", result->timestamp_str,get_operation_str(result->operation));
        fprintf(fd, "fid:%8x \t ", result->template_id);

        int32_t quality = result->quality;
        int32_t unmatch_times = result->unmatch_times;
        int32_t report_msg = result->report_msg_type;

        const char *fmt = NULL;
        switch(result->operation)
        {
            case 'A':
            case 'a':
                fmt = (const char *)"Result:%-4d \t Score:%-4d \t Quality:%-4d \t  RetryCnt:%-4d \t Msg:%s \t  FingerStatus:%d \t  esd_exception:%-4d \t ";
                fprintf(fd,fmt,result->result, result->score,quality,unmatch_times,get_authenticate_msg_desc(report_msg),result->finger_status,result->esd_exception);
                break;
            case 'E':
            case 'e':
                fmt = (const char *)"Cur   :%-4d \t Total:%-4d \t Quality:%-4d \t           %-4d \t     %s \t  esd_exception:%-4d \t ";
                fprintf(fd,fmt,result->cur_enroll_cnt, result->total_enroll_cnt,quality,unmatch_times,get_authenticate_msg_desc(report_msg),result->esd_exception);
                break;
            default:
                break;
        }

        fprintf(fd, "Tplt_Size:%-6d \t Predict_Result:%-4d \t %s\n", result->updated_template_size,result->predict_result,result->image_file_name);
        fclose(fd);
    }
    else
    {
        LOGE(FPTAG"%s open file failed", __func__);
    }
}

static void log_statical_xiaomi(identify_result_t *result)
{
//fp_global_env.fp_internal_callback.get_fp_debug_base_dir();
    const char *data_path = "/sdcard/MIUI/debug_log";
    if (data_path == NULL)
    {
        LOGE(FPTAG"%s get statistical path is null,can't record statiscal info!!", __func__);
        return ;
    }

    char tmp_path[PATH_MAX];
    int str_len = strlen(data_path);
    if (str_len >= PATH_MAX - 10)
    {
        LOGE(FPTAG"%s file path length is too long", __func__);
        return ;
    }

    memset(tmp_path, 0, sizeof(tmp_path));
    strncpy(tmp_path, data_path, str_len);

    if (access(tmp_path, 0) == -1)
    {
        LOGI(FPTAG"%s dir is not exist, create it", tmp_path);
        if(recursive_create_path(tmp_path) != 0)
        {
            LOGE(FPTAG"dir %s create failed", tmp_path);
            return;
        }
    }

    memset(fp_statiscal_file, 0, sizeof(fp_statiscal_file));
    memset(fp_statiscal_bak_file, 0, sizeof(fp_statiscal_bak_file));

    snprintf(fp_statiscal_file, sizeof(fp_statiscal_file), "%s/1.dat", tmp_path);
    snprintf(fp_statiscal_bak_file, sizeof(fp_statiscal_bak_file), "%s/2.dat", tmp_path);

    struct stat statbuf;
    stat(fp_statiscal_file, &statbuf);
    int size = statbuf.st_size;

    if (size > 1000000)
    {
        LOGD("%s backup fp statistical file", __func__);
        remove(fp_statiscal_bak_file);
        rename(fp_statiscal_file, fp_statiscal_bak_file);
    }

    int fd = open(fp_statiscal_file, O_CREAT | O_WRONLY | O_APPEND, 0777);

    if (fd > 0)
    {
        /* LOGD("%s open file success", __func__); */
        write(fd, &result->timestamp, sizeof(result->timestamp));
        write(fd, &result->result, sizeof(result->result));
        write(fd, &result->score, sizeof(result->score));
        write(fd, &result->template_id, sizeof(result->template_id));
        write(fd, &result->updated_template_size, sizeof(result->updated_template_size));
        write(fd, &result->homekey_down, sizeof(result->homekey_down));
        close(fd);
    }
    else
    {
        LOGE(FPTAG"%s open file failed", __func__);
    }
}

static void log_huawei_json(identify_result_t *result)
{
    const char *data_path = fp_global_env.fp_internal_callback.get_fp_debug_base_dir();
    if (data_path == NULL)
    {
        LOGE(FPTAG"%s get statistical path is null,can't record statiscal info!!", __func__);
        return ;
    }

    char tmp_path[PATH_MAX];
    int str_len = strlen(data_path);
    if (str_len >= PATH_MAX - 10)
    {
        LOGE(FPTAG"%s file path length is too long", __func__);
        return ;
    }

    memset(tmp_path, 0, sizeof(tmp_path));
    strncpy(tmp_path, data_path, str_len);

    if (access(tmp_path, 0) == -1)
    {
        LOGI(FPTAG"%s dir is not exist, create it", tmp_path);
        if(recursive_create_path(tmp_path) != 0)
        {
            LOGE(FPTAG"dir %s create failed", tmp_path);
            return;
        }
    }

    memset(fp_statiscal_file, 0, sizeof(fp_statiscal_file));
    memset(fp_statiscal_bak_file, 0, sizeof(fp_statiscal_bak_file));

    snprintf(fp_statiscal_file, sizeof(fp_statiscal_file), "%s/1.txt", tmp_path);
    snprintf(fp_statiscal_bak_file, sizeof(fp_statiscal_bak_file), "%s/2.txt", tmp_path);

    int is_file_first_line = 0;
    struct stat statbuf;
    int ret = stat(fp_statiscal_file, &statbuf);
    if(ret < 0)
    {
        if(errno != ENOENT)
        {
            LOGE(FPTAG"file %s error,errno= %d", fp_statiscal_file,-errno);
            return;
        }
        //file is not exist
        is_file_first_line = 1;
    }

    int size = statbuf.st_size;

    if (size > 1000000)
    {
        LOGD("%s backup fp statistical file", __func__);
        remove(fp_statiscal_bak_file);
        rename(fp_statiscal_file, fp_statiscal_bak_file);
        is_file_first_line = 1;
    }

    int fd = open(fp_statiscal_file, O_CREAT | O_WRONLY, 0666);

    if (fd > 0)
    {
        if(is_file_first_line)
        {
            init_json_file_header(fd);
        }
        append_json_item(fd,result,is_file_first_line);
        close(fd);
    }
    else
    {
        LOGE(FPTAG"%s open file failed", __func__);
    }
    return;
}

void log_statistical_result(identify_result_t *result)
{
    if(!result)
    {
        return;
    }
    if(!get_fp_config_feature_record_statistical())
    {
        return;
    }

#if 0 //log for xiaomi system
    if(result->operation == 'A')
    {
        int32_t extra = result->extra_info;
        int32_t report_msg = ( extra & 0xFF );
        if(report_msg == 1 || report_msg == 2){
            LOGD(FPTAG"authenicate statistical info:%d", report_msg);
            log_statical_xiaomi(result);
        }
    }
#elif 0 //huawei json
    log_huawei_json(result);

#else //for bigdata
    log_statical_bigdata(result);
#endif
    return;
}
