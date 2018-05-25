#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <errno.h>
#include <unistd.h>
#include "fp_log.h"
#include "fp_common.h"

#define FPTAG "fp_xml_paser.cpp  "

#define USER_LIST_FILE_MAX_LINE 1024
char line_buf[USER_LIST_FILE_MAX_LINE];
#define FID_TAG "fingerId="

static int32_t get_fid_from_line(char *id_str,int32_t *fid)
{
    if(NULL == id_str || NULL == fid)
    {
        return -EINVAL;
    }

    LOGD(FPTAG"id_string:%s",id_str);

    char *str_parse = id_str + strlen(FID_TAG);
    int char_idx = 0;
    char cur_char = 0;
    int first_quote_found = 0;
    int sign = 0;
    int found_fid = 0;
    while(1)
    {
        cur_char = str_parse[char_idx];
        if(cur_char == '\"')
        {
            if(!first_quote_found)
            {
                first_quote_found = 1;
            }
            else
            {
                if(char_idx <= 2)
                {
                    LOGE(FPTAG "fid string too short:%s",*id_str);
                    return -EINVAL;
                }
                else
                {
                    *fid = sign ? -found_fid : found_fid;
                    LOGD(FPTAG "find fid:%d",*fid);
                    return 0;
                }
            }
        }
        else if(cur_char == '-')
        {
            if(char_idx == 1)
            {
                sign = 1;
            }
            else
            {
                LOGE(FPTAG "invalid sign position");
                return -EINVAL;
            }
        }
        else if(cur_char <= '9' && cur_char >= '0')
        {
            int num = cur_char - '0';
            found_fid = found_fid * 10 + num;
        }
        else
        {
            LOGE(FPTAG "found invalid char");
            return -EINVAL;
        }

        char_idx++;

        if(char_idx > 15)
        {
            LOGE(FPTAG "fid is too long");
            return -EINVAL;
        }
    }

    return -EINVAL;
}

int32_t get_fp_list_from_xml(char *xml_file,int32_t *fp_array,int32_t *fp_array_size)
{
    int real_fid_cnt = 0;
    int input_array_size = *fp_array_size;
    int32_t result = 0;
    if(NULL == xml_file || NULL == fp_array || NULL == fp_array_size || *fp_array_size <= 0)
    {
        LOGE(FPTAG" input para invalid");
        return -EINVAL;
    }

    FILE *fp = fopen(xml_file,"r");
    if(NULL == fp)
    {
        LOGE(FPTAG" file open error:%s,err:%d",xml_file,-errno);
        return -ENOENT;
    }

    while(fgets(line_buf,USER_LIST_FILE_MAX_LINE,fp) != NULL)
    {
        char *fid_str = strstr(line_buf,FID_TAG);
        if(fid_str != NULL)
        {
            //find a fid line
            int32_t fid = 0;
            int32_t result = get_fid_from_line(fid_str,&fid);
            if(result == 0)
            {
                fp_array[real_fid_cnt] = fid;
                real_fid_cnt++;
                if(real_fid_cnt >= *fp_array_size )
                {
                    LOGE(FPTAG"fid array is too short");
                    result = -EINVAL;
                    goto out;
                }
            }
            else
            {
                LOGE(FPTAG"parse fid line error:%s",fid_str);
                result = -EINVAL;
                goto out;
            }
        }
    }

out:
    *fp_array_size = real_fid_cnt;
    LOGE(FPTAG"get_fp_list_from_xml:array_size = %d, return value:%d",real_fid_cnt,result);
    if(fp)
    {
        fclose(fp);
    }
    fp = NULL;
    return result;
}
