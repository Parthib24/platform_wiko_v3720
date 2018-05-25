#ifndef FP_TAC_IMPL_H
#define FP_TAC_IMPL_H


#include <inttypes.h>
#include "fp_ta_proxy.h"

class fpDameonImpl;
class fpTaProxy;
class fpTestIntf;

#ifndef VOID_CALLBACK
#define VOID_CALLBACK
typedef void (*void_callback)(int);
#endif

class fpTacImpl
{
  public:
    fpTacImpl(fpDameonImpl *daemon_impl);

    virtual ~fpTacImpl();
    uint64_t open_hal(void);
    int32_t close_hal(void);
    int32_t set_active_group(int32_t gid, char *path);
    uint64_t pre_enroll(void);
    int32_t post_enroll(void);
    int32_t delete_fid(int32_t gid, int32_t fid,bool report_rm_result);
    int32_t enroll(const hw_auth_token_t *hat, int32_t gid, int32_t timeout);
    int32_t authenticate(uint64_t challange_id, int32_t gid);
    uint64_t get_authenticator_id();
    int32_t get_enrolled_fids(int32_t *enrolled_fids_array, int32_t array_len, int32_t *fids_cnt);
    int32_t navigation_loop(void);
    int32_t set_stop_status(bool new_status);
    int32_t service_control(int32_t tag, int32_t value);
    void notify_sensor_event(fp_sensor_event_t event);
    void print_system_info(void);
    void configure_sensor_after_hal_open(void);

    //injection test. return 1: match 0: unmatch, -1: error
    int32_t injection_authenticate(void);
    void finger_detect(void_callback cb);
    void sync_fp_with_xml(void);
#if (TARGET_ANDROID>=7)
    int32_t enumerate(void);
#endif
    fpTestIntf *get_test_intf(void);
//  private:
    int32_t duplicated_finger_check(int32_t gid);
    int32_t get_img_ratio(int32_t img_area);
    int32_t report_auth_result(int32_t fid, int32_t auth_time);
    static const char *get_acquire_msg_desc(int32_t acquire_code);
    int32_t capture_img(int32_t capture_style);
    static void construct_enrolling_msg(fingerprint_msg_t &msg, int32_t gid, int32_t fid, int32_t enrolled_cnt,
                                 int32_t total_cnt);
    static void construct_remove_msg(fingerprint_msg_t &msg, int32_t gid, int32_t fid);
    static void construct_error_msg(fingerprint_msg_t &msg, int32_t error_code);
    static void construct_acquire_msg(fingerprint_msg_t &msg, int32_t acquire_code);
    static void construct_match_msg(fingerprint_msg_t &msg, int32_t gid, int32_t fid, hw_auth_token_t &token);
#if (TARGET_ANDROID>=7)
    static void construct_enumerating_msg(fingerprint_msg_t &msg, int32_t gid, int32_t fid, int32_t remaining);
#endif
    fpDameonImpl *daemon_instance;
    fpTaProxy *ta_proxy_instance;

//for rongqi only
    bool enroll_state; //true is resumed, false is pasued
//for rongqi only
    int32_t active_gid;
};

#endif
