#ifndef FP_TEST_INTF_H
#define FP_TEST_INTF_H

#include "fp_log.h"

typedef struct
{
    int32_t capture_result;
    int32_t image_length;
    char *image_data;
} fp_capture_image_data_t;




class fpTestIntf
{
  public:
    fpTestIntf() {};
    virtual ~fpTestIntf() {};

    virtual int32_t do_start_enroll_cmd()
    {
        return 0;
    };
    virtual int32_t do_finish_enroll_cmd()
    {
        return 0;
    };
    virtual int32_t do_enroll_image_cmd()
    {
        return 0;
    };
    virtual int32_t do_get_finger_rect_cnt_cmd(int32_t *rect_cnt)
    {
        *rect_cnt = 0;
        return 0;
    };
    virtual int32_t do_get_size_cmd(int32_t *width, int32_t *height)
    {
        *width = *height = 160;
        return 0;
    };
    virtual int32_t do_set_property_cmd(int32_t tag, int32_t value)
    {
        return 0;
    };
    virtual int32_t do_get_image_quality_cmd(int32_t *area, int32_t *condition, int32_t *quality)
    {
        return 0;
    };
    virtual int32_t do_get_templateIds_cmd(int32_t *ids_array, int32_t *ids_array_len)
    {
        return 0;
    };
    virtual int32_t do_get_finger_rect_cmd(int32_t idx, int32_t *rect_data)
    {
        return 0;
    };
    virtual int32_t capture_image_func(int32_t mode, fp_capture_image_data_t *image_data )
    {
        return 0;
    };
    virtual int32_t do_tool_control_cmd(int32_t p0, int32_t p1)
    {
        return 0;
    };
    virtual int32_t do_selftest_cmd(int32_t *result)
    {
        return 0;
    };
    virtual int32_t do_checkboard_cmd(int32_t *result)
    {
        return 0;
    };
    virtual int32_t do_navigator_addkey_cmd(int32_t *key_array, int32_t key_cnt)
    {
        return 0;
    }
    virtual int32_t finger_detect_func(int32_t dummy,int32_t *result)
    {
        return 0;
    };
};
#endif
