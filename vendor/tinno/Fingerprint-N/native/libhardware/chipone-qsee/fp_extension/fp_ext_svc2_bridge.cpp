#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <poll.h>
#include <unistd.h>
#include <errno.h>
#include <sys/stat.h>
#include <dlfcn.h>
#include <sys/stat.h>

#include "fp_ext_svc2_bridge.h"
#include "fp_thread_task.h"
#include "fp_tac_impl.h"
#include "fp_extension_intf.h"
#include "fp_daemon_impl.h"
#include "fp_log.h"
#include "fp_common.h"

#define FPTAG "fp_ext_svc2_bridge.cpp "


fpExtSvc2Api fp_ext_svc2_api_inst;

int32_t ext_svc_ctrl(int32_t p1, int32_t p2)
{
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge *)fp_ext_svc2_api_inst.bridge_inst;
    if(bridge_inst != NULL)
    {
        return bridge_inst->svc_ctrl(p1,p2);
    }
    return -ENOENT;
}

int32_t ext_set_property(int32_t tag,int32_t value)
{
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge *)fp_ext_svc2_api_inst.bridge_inst;
    if(bridge_inst != NULL)
    {
        return bridge_inst->set_property(tag,value);
    }
    return -ENOENT;
}

int32_t ext_get_size(int32_t *width, int32_t *height)
{
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge *)fp_ext_svc2_api_inst.bridge_inst;
    if(bridge_inst != NULL)
    {
        return bridge_inst->get_size(width,height);
    }
    return -ENOENT;
}

int32_t ext_sensor_self_test()
{
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge *)fp_ext_svc2_api_inst.bridge_inst;
    if(bridge_inst != NULL)
    {
        return bridge_inst->sensor_self_test();
    }
    return -ENOENT;
}

int32_t ext_sensor_check_board()
{
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge *)fp_ext_svc2_api_inst.bridge_inst;
    if(bridge_inst != NULL)
    {
        return bridge_inst->sensor_check_board();
    }
    return -ENOENT;
}

int32_t ext_finger_detect_test()
{
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge *)fp_ext_svc2_api_inst.bridge_inst;
    if(bridge_inst != NULL)
    {
        return bridge_inst->finger_detect_test();
    }
    return -ENOENT;
}

int32_t ext_cancel()
{
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge *)fp_ext_svc2_api_inst.bridge_inst;
    if(bridge_inst != NULL)
    {
        return bridge_inst->cancel();
    }
    return -ENOENT;
}

int32_t ext_capture_raw_image(int32_t mode)
{
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge *)fp_ext_svc2_api_inst.bridge_inst;
    if(bridge_inst != NULL)
    {
        return bridge_inst->capture_raw_image(mode);
    }
    return -ENOENT;
}

int32_t ext_get_img_quality(int32_t *area,int32_t *condition,int32_t *quality)
{
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge *)fp_ext_svc2_api_inst.bridge_inst;
    if(bridge_inst != NULL)
    {
        return bridge_inst->get_img_quality(area,condition,quality);
    }
    return -ENOENT;
}

int32_t ext_ext_cmd(int8_t *cmd_buf,int32_t cmd_len)
{
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge *)fp_ext_svc2_api_inst.bridge_inst;
    if(bridge_inst != NULL)
    {
        return bridge_inst->ext_cmd(cmd_buf,cmd_len);
    }
    return -ENOENT;
}

void navigation(void *arg)
{
    fpDameonImpl *pDaemonImpl = (fpDameonImpl *)arg;
    pDaemonImpl->tac_instance->navigation_loop();
    return ;
}

int32_t fpExtSvc2Bridge::load_ext_svc2_lib(fpExtSvc2Api *inst)
{
    LOGD(FPTAG"load_ext_svc2_lib invoked");
    int32_t ret = -1;
    char *final_fp_ext_svc2_path = (char *)"/system/lib64/fp_ext_svc2.so";
    if(!is_64bit_system()){
        LOGD(FPTAG"32bit system\n");
        final_fp_ext_svc2_path = (char *)"/system/lib/fp_ext_svc2.so";
    }
    void* lib_handle = dlopen(final_fp_ext_svc2_path, RTLD_NOW);
    if(lib_handle == NULL){
        LOGE(FPTAG" dlopen failed can't find hal so: %s, errno= %d\n",final_fp_ext_svc2_path,errno);
        ret = -errno;
        return ret;
    }
    LOGD( FPTAG" dlopen for fp_ext_svc2 lib success \n");

    int (*add_fp_ext_svc2)(void) = (int (*)(void))dlsym(lib_handle, "add_fp_ext_svc2");
    int32_t (*init_fp_ext_svc2_api_impl)(void *) = (int32_t (*)(void *) )dlsym(lib_handle, "init_fp_ext_svc2_api_impl");
    if( NULL == add_fp_ext_svc2 || NULL == init_fp_ext_svc2_api_impl){
            LOGE(FPTAG" dlopen can't get funcptr\n");
            LOGD(FPTAG" add_fp_ext_svc2=%p\n",add_fp_ext_svc2);
            LOGD(FPTAG" init_fp_ext_svc2_api_impl=%p\n",init_fp_ext_svc2_api_impl);
            ret = -EINVAL;
            goto out ;
    }
    if(add_fp_ext_svc2() == 0)
    {
        LOGD(FPTAG " add_fp_ext_svc2 ok\n");
        init_fp_ext_svc2_api_impl((void *)inst);
        return 0;
    }
    else
    {
        LOGE(FPTAG " load_ext_svc2_lib error\n");
    }

out:
    if (lib_handle){
        dlclose(lib_handle);
    }
    LOGD(FPTAG " factory_test exit ret = %d\n",ret);
    return ret;
}

fpExtSvc2Bridge::fpExtSvc2Bridge(thread_task *worker_impl, fpTacImpl *tac_impl, fpDameonImpl *daemon_impl)
{
    LOGD(FPTAG"fpExtSvc2Bridge constructure invoked");

    worker_instance = worker_impl;
    extension_impl_instance = tac_impl->get_test_intf();
    daemon_instance = daemon_impl;
    fp_ext_svc2_api_inst.bridge_inst = (void *)this;
    fp_ext_svc2_api_inst.svc_ctrl = ext_svc_ctrl;
    fp_ext_svc2_api_inst.set_property = ext_set_property;
    fp_ext_svc2_api_inst.get_size = ext_get_size;
    fp_ext_svc2_api_inst.get_img_quality = ext_get_img_quality;
    fp_ext_svc2_api_inst.sensor_self_test = ext_sensor_self_test;
    fp_ext_svc2_api_inst.sensor_check_board = ext_sensor_check_board;
    fp_ext_svc2_api_inst.finger_detect_test = ext_finger_detect_test;
    fp_ext_svc2_api_inst.cancel = ext_cancel;
    fp_ext_svc2_api_inst.ext_cmd = ext_ext_cmd;
    fp_ext_svc2_api_inst.capture_raw_image = ext_capture_raw_image;

    if(load_ext_svc2_lib(&fp_ext_svc2_api_inst) == 0)
    {
        LOGI(FPTAG"fpExtSvc2 add to system manager success, supporting ext service now!!");
    }
    else
    {
        LOGE(FPTAG"fpExtSvc2 add to system manager failed, DO NOT supports ext service!!!");
    }

    return;
}

fpExtSvc2Bridge::~fpExtSvc2Bridge()
{
    LOGD(FPTAG"~fpExtSvc2Bridge invoked");
    fp_ext_svc2_api_inst.bridge_inst = NULL;
    cancel();

    if (extension_impl_instance)
    {
        delete extension_impl_instance;
    }
    extension_impl_instance = NULL;

    if (lib_handle){
        dlclose(lib_handle);
    }
    lib_handle = NULL;
}

int32_t fpExtSvc2Bridge::svc_ctrl(int32_t tag,int32_t value)
{
    int32_t ret = -1;

    LOGD(FPTAG"svc_ctrl,tag=%d,value = %d",tag,value);

    daemon_instance->goto_idle();

    if (extension_impl_instance)
    {
        if (tag == FP_SERVICE_CONTROL_CMD_NAVIGATOR_ENABLE)
        {
            if (get_fp_config_feature_navigator())
            {
                if (value)
                {
                    worker_instance->set_thread_default_task(navigation, (void *)daemon_instance);
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
            ret = 0;
        }
        else if (FP_SERVICE_CONTROL_CMD_NAVIGATOR_GET_STATUS == tag )
        {
            ret = (worker_instance->def_thread_work.function_ptr != 0);
        }
        else if (FP_SERVICE_CONTROL_CMD_MISC_PRINT_SYSTEM_INFO == tag)
        {
            if(daemon_instance && daemon_instance->tac_instance)
            {
                daemon_instance->tac_instance->print_system_info();
                ret = 0;
            }
            else
            {
                LOGI(FPTAG"Print system info error,some instance is null");
                ret = -ENOENT;
            }
        }
        else if(FP_SERVICE_CONTROL_CMD_RELEASE_MODE_ENABLE_LOGD == tag)
        {
            enable_logd_in_release_mode = value;
            LOGI(FPTAG"FP_SERVICE_CONTROL_CMD_RELEASE_MODE_ENABLE_LOGD enable_logd_in_release_mode = %d",enable_logd_in_release_mode);
            ret = 0;
        }
        else
        {
            ret = extension_impl_instance->do_tool_control_cmd(tag, value);
        }
    }
    LOGD(FPTAG"svc_ctrl,ret = %d,tag=%d,value = %d",ret,tag,value);
    worker_instance->resume_thread();
    return ret;
}

int32_t fpExtSvc2Bridge::set_property(int32_t tag,int32_t value)
{
    int32_t ret = -1;
    LOGD(FPTAG"set_property,tag=%d,value=%d",tag,value);

    daemon_instance->goto_idle();
    ret = extension_impl_instance->do_set_property_cmd(tag, value);
    LOGD(FPTAG"get_size,ret=%d",ret);
    worker_instance->resume_thread();

    return ret;
}

int32_t fpExtSvc2Bridge::get_img_quality(int32_t *area,int32_t *codition,int32_t *quality)
{
    int32_t ret = -1;
    LOGD(FPTAG"get_img_quality invoked");

    daemon_instance->goto_idle();
    ret = extension_impl_instance->do_get_image_quality_cmd(area, codition, quality);
    LOGD(FPTAG"get_img_quality,ret=%d,area=%d,codition=%d,quality=%d",ret,*area,*codition,*quality);
    worker_instance->resume_thread();

    return ret;
}

int32_t fpExtSvc2Bridge::get_size(int32_t *width,int32_t *height)
{
    int32_t ret = -1;
    LOGD(FPTAG"get_size");
    daemon_instance->goto_idle();
    ret = extension_impl_instance->do_get_size_cmd(width, height);
    LOGD(FPTAG"get_size,ret=%d,width=%d, height=%d",ret,*width,*height);
    worker_instance->resume_thread();

    return ret;
}

int32_t fpExtSvc2Bridge::sensor_self_test()
{
    int32_t test_result = -1;
    LOGD(FPTAG"sensor_self_test");

    daemon_instance->goto_idle();
    extension_impl_instance->do_selftest_cmd( &test_result);
    worker_instance->resume_thread();

    LOGD(FPTAG"sensor_self_test result = %d", test_result);
    return test_result;
}

int32_t fpExtSvc2Bridge::sensor_check_board()
{
    int32_t test_result = -1;
    LOGD(FPTAG"sensor_check_board");

    daemon_instance->goto_idle();
    extension_impl_instance->do_checkboard_cmd( &test_result);
    worker_instance->resume_thread();

    LOGD(FPTAG"sensor_check_board test_result = %d",test_result);
    return test_result;
}

void finger_detect_func(void *para)
{
    //do image capture
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge*)para;
    int32_t finger_detect_result = -1;
    bridge_inst->extension_impl_instance->finger_detect_func(0,&finger_detect_result);
    if(fp_ext_svc2_api_inst.on_finger_detected)
    {
        fp_ext_svc2_api_inst.on_finger_detected(finger_detect_result);
    }

}

int32_t fpExtSvc2Bridge::finger_detect_test()
{
    LOGD(FPTAG"finger_detect_test");
    daemon_instance->goto_idle();
    worker_instance->set_thread_task(finger_detect_func, (void*)this);
    worker_instance->resume_thread();

    return 0;
}

int32_t fpExtSvc2Bridge::cancel()
{
    LOGD(FPTAG"cancel");

    daemon_instance->goto_idle();
    worker_instance->resume_thread();
    return 0;
}

void capture_image_func(void *para)
{
    //do image capture
    int32_t ret = -1;
    fpExtSvc2Bridge *bridge_inst = (fpExtSvc2Bridge*)para;
    int32_t mode = bridge_inst->capture_mode;

    fp_capture_image_data_t capture_image_data;
    capture_image_data.capture_result = -EBUSY;
    capture_image_data.image_data = (char *)malloc(160 * 160 );
    memset(capture_image_data.image_data, 0, 160 * 160  );

    if (bridge_inst->extension_impl_instance)
    {
        ret = bridge_inst->extension_impl_instance->capture_image_func(mode, &capture_image_data);
    }

    if(fp_ext_svc2_api_inst.on_raw_img_captured != NULL)
    {
        LOGD(FPTAG"capture_image_func send result to UI result = %d, img_len=%d",capture_image_data.capture_result,capture_image_data.image_length);
        fp_ext_svc2_api_inst.on_raw_img_captured(capture_image_data.capture_result,capture_image_data.image_data,capture_image_data.image_length);
    }

    free(capture_image_data.image_data);
    capture_image_data.image_data = NULL;
}

int32_t fpExtSvc2Bridge::capture_raw_image(int32_t mode)
{
    LOGD(FPTAG"capture_raw_image,mode = %d",mode);
    capture_mode = mode;
    daemon_instance->goto_idle();
    worker_instance->set_thread_task(capture_image_func, this);
    worker_instance->resume_thread();

    return 0;
}

int32_t fpExtSvc2Bridge::ext_cmd(int8_t *cmd_buf, int32_t cmd_len)
{
    LOGD(FPTAG"ext_cmd invoked,cmd_len = %d, not implemented",cmd_len);

    return -EINVAL;
}
