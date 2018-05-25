#ifndef FP_TA_PROXY_H
#define FP_TA_PROXY_H


#include <inttypes.h>
#include "fingerprint_sensor_demonnative.h"
#include "fp_extension_intf.h"

#define CAPTURE_STYLE_NO_WAIT 0
#define CAPTURE_STYLE_WAIT_UP 1
#define CAPTURE_STYLE_WAIT_DOWN 2

class fpTacImpl;

class fpTaProxy
{
  public:
    fpTaProxy(fpTacImpl *rtac_instance)
    {
        fp_initilized = false;
        tac_impl_instance = rtac_instance;

        sensor_width = 0;
        sensor_height = 0;
    };
    virtual ~fpTaProxy() {};
    virtual uint64_t open_hal(void)
    {
        return 0;
    };
    virtual int32_t close_hal(void)
    {
        return 0;
    };
    virtual uint64_t pre_enroll(void)
    {
        return init_check();
    };
    virtual int32_t post_enroll(void)
    {
        return init_check();
    };
    virtual int32_t set_active_group(int32_t gid, char *path)
    {
        return init_check();
    };
    virtual int32_t start_enroll(int32_t gid, int32_t *fid)
    {
        return init_check();
    };
    virtual int32_t authorize_enrol(const hw_auth_token_t *hat)
    {
        return init_check();
    };
    virtual int32_t finish_enroll(void)
    {
        return init_check();
    };
    virtual int32_t enroll_img(int32_t *last_progress, int32_t *total_enroll_cnt,
                               int32_t *cur_enroll_cnt, int32_t *enroll_fail_reason, int32_t *fill_part)
    {
        return init_check();
    };

    virtual int32_t enrol_store_template(int32_t gid, int32_t *fid)
    {
        return init_check();
    };

    virtual int32_t capture_img(int32_t captyre_style)
    {
        return init_check();
    };
    virtual int32_t begin_authenticate(uint64_t challenge_id)
    {
        return init_check();
    };
    //if this function return 0, then must call end_authenticate() after this
    virtual int32_t authenticate( int32_t gid, int32_t *match_result, int32_t *match_finger,
                                  bool for_enroll_match_image, hw_auth_token_t *token)
    {
        return init_check();
    };
    virtual int32_t end_authenticate(int32_t *is_template_updated, int32_t allow_update_template)
    {
        return init_check();
    };

    virtual int32_t delete_fid(int32_t gid, int32_t fid)
    {
        return init_check();
    };
    virtual uint64_t get_authenticator_id(void)
    {
        return init_check();
    };
    virtual int32_t get_enrolled_fids(int32_t *enrolled_fids, int32_t array_len, int32_t *fid_cnt)
    {
        return init_check();
    };
    virtual int32_t nav_loop(void)
    {
        return init_check();
    };
    virtual int32_t set_stop_status(bool new_status)
    {
        stop_status = new_status;
        return init_check();
    };
    virtual bool get_stop_status(void)
    {
        return stop_status;
    };
    virtual bool get_init_status(void)
    {
        return fp_initilized;
    };
    virtual fpTestIntf *get_test_intf(void)
    {
        return NULL;
    };
    virtual int32_t service_control(int32_t tag, int32_t alue)
    {
        return init_check();
    }
    virtual void notify_sensor_event(fp_sensor_event_t event)
    {
        return;
    };
    virtual int32_t print_system_info(void)
    {
        return init_check();
    }

    virtual int32_t get_image_quality(int32_t *p1,int32_t *p2,int32_t *p3)
    {
        return init_check();
    }

    virtual int32_t get_image_size(int32_t *width,int32_t *height)
    {
        return init_check();
    }

    virtual int32_t hal_configuration(int32_t tag,int32_t value)
    {
        return init_check();
    }

    virtual int32_t finger_detect_test(void)
    {
        return init_check();
    }

    int32_t init_check(void)
    {
        if (!fp_initilized)
        {
            LOGE("error fpTaProxy is not inited");
        }
        return fp_initilized ? 0 : FINGERPRINT_ERROR_HW_UNAVAILABLE;
    };
  protected:
    fpTacImpl *tac_impl_instance;
    bool stop_status;
    bool fp_initilized;
    uint64_t mChallenge;
    uint64_t mAuthenticator_id;
    int32_t sensor_width;
    int32_t sensor_height;

};
#endif
