#ifndef FP_EXT_SVC2_BRIDGE_H
#define FP_EXT_SVC2_BRIDGE_H


#include <pthread.h>
#include "FpExtSvc2Api.h"

class thread_task;
class fpTacImpl;
class fpTestIntf;
class fpDameonImpl;

class fpExtSvc2Bridge
{
  public:
    fpExtSvc2Bridge(thread_task *worker_impl, fpTacImpl *tac_impl, fpDameonImpl *daemon_impl);
    ~fpExtSvc2Bridge();

    int32_t svc_ctrl(int32_t p1,int32_t p2);
    int32_t set_property(int32_t tag,int32_t value);
    int32_t get_size(int32_t *width,int32_t *height);
    int32_t get_img_quality(int32_t *area,int32_t *codition,int32_t *quality);
    int32_t sensor_self_test();
    int32_t sensor_check_board();
    int32_t finger_detect_test();
    int32_t cancel();
    int32_t capture_raw_image(int32_t mode);
    int32_t ext_cmd(int8_t *cmd_buf, int32_t cmd_len);
    int32_t load_ext_svc2_lib(fpExtSvc2Api *inst);

    thread_task *worker_instance;
    fpTestIntf *extension_impl_instance;
    fpDameonImpl *daemon_instance;
    int32_t capture_mode;
    void* lib_handle;
};
#endif
