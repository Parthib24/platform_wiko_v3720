#include "fp_daemon_impl.h"
#include "fp_tac_impl.h"
#include <assert.h>
#include <string.h>
#include "fp_thread_task.h"
#include "fp_extension.h"
#include "fp_common.h"
#define FPTAG "fpdaemon_Impl.cpp "

//default do not show logd info in release mode
int32_t enable_logd_in_release_mode = 0;
int32_t extension_service_status = 1;
int32_t g_inject_enabled = 0;
int32_t g_inject_img_idx = 0;
int32_t g_enable_report_auth_result = 0;

fpDameonImpl::fpDameonImpl(JavaVM *pVm)
{
    notify_cb_func = (fingerprint_notify_t)NULL;
    tac_instance = NULL;
    extension_instance = NULL;
    pthread_mutex_init(&mutex, NULL);
    worker_instance = new thread_task(pVm);
    worker_instance->set_thread_task(NULL, NULL);
    worker_instance->set_thread_default_task(NULL, NULL);
    LOGD(FPTAG"fpDameonJniImpl constructor invoked,DaemonVersion:%s", FPDAEMON_VERSION);
    set_tac_impl(new fpTacImpl(this));
};

fpDameonImpl::~fpDameonImpl()
{
    LOGD(FPTAG"~fpDameonImpl destructure invoked");

    if (extension_instance)
    {
        delete extension_instance;
        extension_instance = NULL;
    }

    if (worker_instance)
    {
        worker_instance->set_thread_default_task(NULL, NULL);
        goto_idle();
        delete worker_instance;
        worker_instance = NULL;
    }
    pthread_mutex_destroy(&mutex);
    delete tac_instance;
    tac_instance = NULL;
};

void fpDameonImpl::lock_mutex(void)
{
    pthread_mutex_lock(&mutex);
}
void fpDameonImpl::unlock_mutex(void)
{
    pthread_mutex_unlock(&mutex);
}
void fpDameonImpl::goto_idle(void)
{
    LOGD(FPTAG"goto_idle ");
//    if(!worker_instance->is_thread_idle()){

    tac_instance->set_stop_status(true);
    worker_instance->pause_thread();
//    }
    tac_instance->set_stop_status(false);
}

int32_t fpDameonImpl::set_active_group(int32_t gid, char *path)
{
    LOGI(FPTAG"set_active_group");

    lock_mutex();

    goto_idle();

    int32_t ret = tac_instance->set_active_group(gid, path);
    worker_instance->resume_thread();
    unlock_mutex();

    return ret;
}

int32_t fpDameonImpl::notify(const fingerprint_msg_t *msg)
{
    if(notify_cb_func)
    {
        notify_cb_func(msg, (JNIEnv *)worker_instance->get_jni_env());
    }
    return 0;
}

uint64_t fpDameonImpl::open_hal(void)
{
    LOGD(FPTAG"open_hal invoked");
    lock_mutex();
    int32_t ret = tac_instance->open_hal();
    if (ret != 0)
    {
        if (extension_service_status)
        {
            extension_instance = new fpExtension(worker_instance, tac_instance, this);
        }
    }
    unlock_mutex();
    return ret;
}


int32_t fpDameonImpl::close_hal(void)
{
    LOGD(FPTAG"close_hal invoked");
    lock_mutex();
    goto_idle();

    int32_t ret = tac_instance->close_hal();
    worker_instance->resume_thread();
    unlock_mutex();
    return ret;
}

uint64_t fpDameonImpl::get_authenticator_id(void)
{
    LOGD(FPTAG"get_authenticator_id invoked");
    uint64_t ret = 0;

    lock_mutex();
    goto_idle();

    ret = tac_instance->get_authenticator_id();
    worker_instance->resume_thread();
    unlock_mutex();
    return ret;
}


static void remove_function(void *data)
{
    fpDameonImpl *daemon_instance = (fpDameonImpl *)data;
    daemon_instance->tac_instance->delete_fid(daemon_instance->mgid, daemon_instance->mfid,true);
    LOGD(FPTAG"remove_function exit");
    return;
}

int32_t fpDameonImpl::delete_fid(int32_t gid, int32_t fid)
{
    LOGD(FPTAG"fpDameonJniImpl delete_fid");
    int32_t ret = 0;
    lock_mutex();

    goto_idle();

    mfid = fid;
    mgid = gid;
    worker_instance->set_thread_task(remove_function, (void *)this);
    worker_instance->resume_thread();
    unlock_mutex();
    return ret;
}

int32_t fpDameonImpl::cancel(void)
{
    LOGD(FPTAG"enter cancel");

    lock_mutex();
#if (TARGET_ANDROID >= 7)
    fingerprint_msg_t msg;
    tac_instance->construct_error_msg(msg,FINGERPRINT_ERROR_CANCELED);
    notify(&msg);
#endif
    goto_idle();
    // after cancel, resume worker to handle navigation
    worker_instance->resume_thread();
    unlock_mutex();

    return 0;
}


static void enroll_function(void *data)
{
    fpDameonImpl *daemon_instance = (fpDameonImpl *)data;
    daemon_instance->tac_instance->enroll(&daemon_instance->mhat, daemon_instance->mgid,
                                          daemon_instance->mtimeout);
    LOGD(FPTAG"enroll_function exit");
    return;
}

int32_t fpDameonImpl::enroll(const hw_auth_token_t *hat, int32_t gid, int32_t timeout)
{
    LOGD(FPTAG"[guomingyi]enroll");
    int32_t ret = 0;
    lock_mutex();
    if (hat != NULL)
    {
        memcpy(&mhat, hat, sizeof(hw_auth_token_t));
    }

    goto_idle();

    mgid = gid;
    mtimeout = timeout;
    worker_instance->set_thread_task(enroll_function, (void *)this);
    worker_instance->resume_thread();
    unlock_mutex();
    return ret;
}

uint64_t fpDameonImpl::pre_enroll(void)
{
    LOGD(FPTAG"pre_enroll");
    lock_mutex();

    goto_idle();

    uint64_t ret = tac_instance->pre_enroll();
    worker_instance->resume_thread();
    unlock_mutex();
    return ret;
}
int32_t fpDameonImpl::post_enroll(void)
{
    LOGD(FPTAG"post_enroll");
/*    lock_mutex();

    goto_idle();

    uint32_t ret = tac_instance->post_enroll();
    worker_instance->resume_thread();
    unlock_mutex();
    return ret;*/
    return 0;
}


void authenticate_function(void *data)
{
    fpDameonImpl *daemon_instance = (fpDameonImpl *)data;
    daemon_instance->tac_instance->authenticate( daemon_instance->msession_id, daemon_instance->mgid);
    LOGD(FPTAG"authenticate_function exit");
    return;
}

int32_t fpDameonImpl::authenticate(uint64_t session_id, int32_t gid)
{
    LOGD(FPTAG"authenticate invoked");
    lock_mutex();

    int32_t ret = 0;

    goto_idle();

    msession_id = session_id;
    mgid = gid;
    worker_instance->set_thread_task(authenticate_function, (void *)this);
    worker_instance->resume_thread();
    unlock_mutex();

    return ret;
}

int32_t fpDameonImpl::get_enrolled_fids(int32_t *enrolled_fids_array, int32_t array_len,
                                        int32_t *fid_cnt)
{
    LOGD(FPTAG"enter get_enrolled_fids");
    lock_mutex();

    goto_idle();

    int32_t ret = tac_instance->get_enrolled_fids(enrolled_fids_array, array_len, fid_cnt);
    worker_instance->resume_thread();
    unlock_mutex();
    return ret;
}

#if (TARGET_ANDROID>=7)
void enumerate_function(void *data)
{
    fpDameonImpl *daemon_instance = (fpDameonImpl *)data;
    daemon_instance->tac_instance->enumerate();
    LOGD(FPTAG"enumerate_function exit");
    return;
}

int32_t fpDameonImpl::enumerate(void)
{
    LOGD(FPTAG"enumerate invoked");
    lock_mutex();

    int32_t ret = 0;

    goto_idle();

    worker_instance->set_thread_task(enumerate_function, (void *)this);
    worker_instance->resume_thread();
    unlock_mutex();

    return ret;
}
#endif

extern void navigation(void *arg);


void finger_detect_function(void *data)
{
    fpDameonImpl *daemon_instance = (fpDameonImpl *)data;
    daemon_instance->tac_instance->finger_detect(daemon_instance->finger_detect_cb);
    LOGD(FPTAG"finger_detect_function exit");
    return;
}

int32_t fpDameonImpl::finger_detect(void_callback cb)
{
    LOGD(FPTAG"finger_detect invoked");
    lock_mutex();

    int32_t ret = 0;

    goto_idle();
    finger_detect_cb = cb;
    worker_instance->set_thread_task(finger_detect_function, (void *)this);
    worker_instance->resume_thread();
    unlock_mutex();

    return ret;
}

int32_t fpDameonImpl::service_control(int32_t tag, int32_t value)
{
    LOGD(FPTAG"enter service_control,tag=%d,ipara2=%d", tag, value);
    int32_t ret = 0;
//for some para command,  just set variable value, do not interrupt current process
//for rongqi project only
    if (tag == FP_SERVICE_CONTROL_CMD_ENROLL_PAUSE || tag == FP_SERVICE_CONTROL_CMD_ENROLL_RESUME)
    {
        LOGD(FPTAG"receive FP_SERVICE_CONTROL_CMD_ENROLL_PAUSE or FP_SERVICE_CONTROL_CMD_ENROLL_RESUME, just set value and return");
        ret = tac_instance->service_control(tag, value);
        return 0;
    }
////for rongqi project only  end

    lock_mutex();
    goto_idle();
    if (FP_SERVICE_CONTROL_CMD_NAVIGATOR_ENABLE == tag)
    {
        if (get_fp_config_feature_navigator())
        {
            if (value)
            {
                worker_instance->set_thread_default_task(navigation, (void *)this);
            }
            else
            {
                worker_instance->set_thread_default_task(NULL, NULL);
            }
        }
        else
        {
            LOGI(FPTAG"navigator is not enable, but received FP_SERVICE_CONTROL_CMD_NAVIGATOR_ENABLE cmd");
        }
    }
    else
    {
        ret = tac_instance->service_control(tag, value);
    }
    worker_instance->resume_thread();
    unlock_mutex();
    return ret;
}
