#ifndef FP_DAEMON_IMPL_H
#define FP_DAEMON_IMPL_H


#include <inttypes.h>
#include "fp_log.h"
#include <jni.h>
#include "fingerprint_sensor_demonnative.h"
#include <unistd.h>
#include <pthread.h>
#include "fp_common.h"

class fpTacImpl;
class thread_task;
class fpExtension;

class fpDameonImpl
{
  public:
    fpDameonImpl(JavaVM *pVm);

    virtual ~fpDameonImpl();
    int32_t set_active_group(int32_t gid, char *path);

    int32_t notify(const fingerprint_msg_t *msg);



    int32_t post_enroll(void);

    int32_t set_notify_callback(fingerprint_notify_t cb_func)
    {
        notify_cb_func = cb_func;
        return 0;
    }
    void set_tac_impl(fpTacImpl *tac_impl)
    {
        tac_instance = tac_impl;
    }

    uint64_t open_hal(void);
    int32_t close_hal(void);
    uint64_t get_authenticator_id(void);
    uint64_t pre_enroll(void);
    int32_t delete_fid(int32_t gid, int32_t fid);
    int32_t cancel(void);
    int32_t enroll(const hw_auth_token_t *hat, int32_t gid, int32_t timeout) ;
    int32_t authenticate(uint64_t session_id, int32_t gid);
    int32_t get_enrolled_fids(int32_t *enrolled_fids, int32_t array_len, int32_t *fids_cnt) ;

#if (TARGET_ANDROID>=7)
    int32_t enumerate(void);
#endif

    int32_t service_control(int32_t ipara1, int32_t ipara2);
    int32_t finger_detect(void_callback cb);
    void lock_mutex(void);
    void unlock_mutex(void);
    void goto_idle(void);
    hw_auth_token_t mhat;
    int32_t mgid;
    int32_t mfid;
    int32_t mtimeout;
    uint64_t msession_id;
    fpTacImpl *tac_instance;
    fingerprint_notify_t notify_cb_func;
    void_callback finger_detect_cb;
    thread_task *worker_instance;
    fpExtension *extension_instance;
    pthread_mutex_t mutex;
};

#endif
