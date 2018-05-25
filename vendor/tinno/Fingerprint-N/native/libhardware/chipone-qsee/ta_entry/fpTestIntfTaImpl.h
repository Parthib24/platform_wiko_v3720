#ifndef FP_TESTINTF_TAIMPL_H
#define FP_TESTINTF_TAIMPL_H

#include "fp_extension_intf.h"

class fpTaEntryProxy;

class fpTestIntfTaImpl : public fpTestIntf
{
  public:
    fpTestIntfTaImpl();
    virtual ~fpTestIntfTaImpl();
    virtual int32_t do_start_enroll_cmd();
    virtual int32_t do_finish_enroll_cmd();
    virtual int32_t do_enroll_image_cmd();
    virtual int32_t do_get_finger_rect_cnt_cmd(int32_t *rect_cnt);
    virtual int32_t do_get_size_cmd(int32_t *width, int32_t *height);
    virtual int32_t do_set_property_cmd(int32_t tag, int32_t value);
    virtual int32_t do_get_image_quality_cmd(int32_t *area, int32_t *condition, int32_t *quality);
    virtual int32_t do_get_templateIds_cmd(int32_t *ids_array, int32_t *ids_array_len);
    virtual int32_t do_get_finger_rect_cmd(int32_t idx, int32_t *rect_data);
    virtual int32_t capture_image_func(int32_t mode, fp_capture_image_data_t *image_data);
    virtual int32_t do_tool_control_cmd(int32_t p0, int32_t p1);
    virtual int32_t do_selftest_cmd(int32_t *result);
    virtual int32_t do_checkboard_cmd(int32_t *result);
    virtual int32_t finger_detect_func(int32_t dummy,int32_t *result);
    void setTaProxy(fpTaEntryProxy *pfpTaEntryProxy);

  private:
    int captureImgWaitFingerLost();
    int captureImgWaitFingerDown();
    int captureImgExecution(int *pLen, char *pData);

    fpTaEntryProxy *mpfpTaEntryProxy;
};
#endif
