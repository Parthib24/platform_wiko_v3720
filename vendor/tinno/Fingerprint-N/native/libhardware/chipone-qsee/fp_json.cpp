#include <unistd.h>
#include <stdio.h>
#include "fp_common.h"
#include "fp_log.h"
#include "string.h"

#define FPTAG "fp_json.cpp "

/*
{ "people": [
{ "firstName": "Brett", "lastName":"McLaughlin", "email": "aaaa" },
{ "firstName": "Jason", "lastName":"Hunter", "email": "bbbb"},
{ "firstName": "Elliotte", "lastName":"Harold", "email": "cccc" }
]}
*/


/*
{ "fp_data": [
*/
#define FILE_TAG "fp_data"

static char json_content[20 * 1024];
static int32_t json_offset = 0;
#define PREPARE_JSON() do { \
                                memset(json_content,0,sizeof(json_content)); \
                                json_offset = 0; \
                            }while(0)
#define END_JSON(fd) do { \
                          write(fd, json_content, json_offset); \
                        }while(0)


char buffer[256];

#define RECORD_BUFFER(fd,buffer,len) do { \
                                            if(json_offset + len > sizeof(json_content)) \
                                            { \
                                                LOGE(FILE_TAG" json write content too long"); \
                                                break; \
                                            } \
                                            memcpy( (json_content + json_offset),buffer,len); \
                                            json_offset += len; \
                                        }while(0)

#define RECORD_INT_ENELEMT(fp,tag,value) do { \
                                                snprintf(buffer,sizeof(buffer),"\"%s\":\"%d\",",#tag,value); \
                                                RECORD_BUFFER(fd, buffer, strlen(buffer)); \
                                            }while(0)
#define RECORD_STRING_ELEMENT(fp,tag,value) do{ \
                                                snprintf(buffer,sizeof(buffer),"\"%s\":\"%s\",",#tag,value); \
                                                RECORD_BUFFER(fd, buffer, strlen(buffer)); \
                                            }while(0)

static void record_string(int fd,char *string)
{
    snprintf(buffer,sizeof(buffer),"%s",string);
    RECORD_BUFFER(fd, buffer, strlen(buffer));
}

void init_json_file_header(int fd){
    PREPARE_JSON();
    snprintf(buffer,sizeof(buffer),(char *)"{\"%s\" :[\n",FILE_TAG);
    RECORD_BUFFER(fd, buffer, strlen(buffer));
    END_JSON(fd);
}

void append_json_item(int fd,identify_result_t *result,int32_t is_first_line)
{
    PREPARE_JSON();
    if(!is_first_line)
    {
        //remove the last three character \n]}, prepare to append new line
        lseek(fd,-3,SEEK_END);
        record_string(fd,(char *)",\n");
    }
    record_string(fd,(char *)"{");
    RECORD_STRING_ELEMENT(fp,time,result->timestamp_str);
    RECORD_INT_ENELEMT(fp,Result,result->result);
    RECORD_INT_ENELEMT(fp,Score,result->score);
    RECORD_INT_ENELEMT(fp,TpltSize,result->updated_template_size);
    RECORD_INT_ENELEMT(fp,Predict_Result,result->predict_result);
    RECORD_STRING_ELEMENT(fp,fileName,result->image_file_name);
    //last item
    snprintf(buffer,sizeof(buffer),(char *)"\"%s\":\"%d\"}","end_item",0);
    RECORD_BUFFER(fd, buffer, strlen(buffer));
    //add file tail
    record_string(fd,(char *)"\n]}");
    END_JSON(fd);
    LOGD(FILE_TAG" json write length = %d",json_offset);
}