#include <stdio.h>
#include "fp_extension.h"
#include <sys/types.h>
#include <sys/socket.h>
#include <poll.h>
#include <unistd.h>
#include <errno.h>
#include <sys/stat.h>
#include "fp_thread_task.h"
#include "fp_tac_impl.h"
#include "fp_extension_intf.h"
#include "fp_daemon_impl.h"
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>

#include "fp_log.h"
#include "fp_common.h"
#define FPTAG "fp_extension.cpp "


#define SOCKET_RESP_OK 12598534
static void *extension_worker(void *data)
{
    LOGD(FPTAG"extension_worker running . . . ");

    fpExtension *extension_this = (fpExtension *)data;
    socket_connection_t fp_recv_send_socket = {0};
    fp_recv_send_socket.para = extension_this;

    //create the /data/fpsensor dir
    if (access((char *)"/data/fpsensor", 0) == -1)
    {
        LOGE(FPTAG"/data/fpsensor dir is not exist, create it");
        mkdir((char *)"/data/fpsensor", 0770);
        if (access((char *)"/data/fpsensor", 0) != -1)
        {
            LOGE(FPTAG" dir /data/fpsensor create failed, can't use extension service");
        }
    }

    pthread_mutex_lock(&extension_this->socket_lock);
    extension_this->fpe_connection = &fp_recv_send_socket;
    extension_this->the_socket = extension_socket_create("/data/fpsensor/socket");

    int32_t chmod_result = chmod("/data/fpsensor/socket",
                                 S_IROTH | S_IWOTH | S_IRWXU | S_IRGRP | S_IWGRP | S_ISUID );
    LOGD(FPTAG"/data/fpsensor/socket chmodResult=%d ", chmod_result);

    pthread_mutex_unlock(&extension_this->socket_lock);
    if (extension_this->the_socket == NULL)
    {
        LOGE("create the_socket fail");
        goto err;
    }
    while (1)
    {
        if (extension_this->try_exit())
        {
            LOGE(FPTAG"try_exit ");
            goto err;
        }

        if (poll_extension_socket(extension_this->the_socket, POLLIN) != POLLIN)
        {
            LOGE(FPTAG"poll_extension_socket error,retry ");
            goto retry1;
        }

        if (extension_this->try_exit())
        {
            LOGE(FPTAG"try_exit ");
            goto err;
        }

        pthread_mutex_lock(&extension_this->socket_lock);
        fp_recv_send_socket.cmd_socket = extension_socket_accept(extension_this->the_socket);
        pthread_mutex_unlock(&extension_this->socket_lock);

        if (!fp_recv_send_socket.cmd_socket)
        {
            LOGE(FPTAG"create cmd socket error, retry ");
            goto retry1;
        }

        int32_t fd_pair[2];
        if (socketpair(AF_UNIX, SOCK_STREAM, 0, fd_pair))
        {
            LOGE(FPTAG"socketpair error,retry ");
            goto retry1;
        }

        pthread_mutex_lock(&extension_this->socket_lock);
        fp_recv_send_socket.data_socket = extension_socket_from_sockfd(fd_pair[0]);
        pthread_mutex_unlock(&extension_this->socket_lock);
        if (!fp_recv_send_socket.data_socket)
        {
            LOGE(FPTAG"data_socket create error,retry ");
            goto retry1;
        }

        if (extension_socket_send_fd(fp_recv_send_socket.cmd_socket, fd_pair[1]))
        {
            close(fd_pair[0]);
            LOGE(FPTAG"send fd to cmd_socket error,retry ");
            goto retry1;
        }

        if (extension_this->socket_handler(&fp_recv_send_socket) == -FP_ERROR_USER_CANCEL)
        {
            goto err;
        }
    retry1:
        pthread_mutex_lock(&extension_this->socket_lock);
        remove_extension_socket(fp_recv_send_socket.cmd_socket);
        fp_recv_send_socket.cmd_socket = NULL;
        remove_extension_socket(fp_recv_send_socket.data_socket);
        fp_recv_send_socket.data_socket = NULL;
        close(fd_pair[0]);
        close(fd_pair[1]);
//        pfpExtension->fpe_connection = NULL;
        pthread_mutex_unlock(&extension_this->socket_lock);
    }
err:
    LOGE(FPTAG"Connection lost");
    pthread_mutex_lock(&extension_this->socket_lock);
    remove_extension_socket(fp_recv_send_socket.cmd_socket);
    fp_recv_send_socket.cmd_socket = NULL;
    remove_extension_socket(fp_recv_send_socket.data_socket);
    fp_recv_send_socket.data_socket = NULL;
    remove_extension_socket(extension_this->the_socket);
    extension_this->the_socket = NULL;
    pthread_mutex_unlock(&extension_this->socket_lock);
    return NULL;
}



fpExtension::fpExtension(thread_task *worker_impl, fpTacImpl *tac_impl, fpDameonImpl *daemon_impl)
{
    LOGD(FPTAG"constructure invoked");
    pthread_mutex_init(&socket_lock, NULL);

    worker_instance = worker_impl;
    extension_impl_instance = tac_impl->get_test_intf();
    daemon_instance = daemon_impl;
    should_exit_status = 0;
    pthread_create(&extension_thread, NULL, &extension_worker, this);

    return;
}
fpExtension::~fpExtension()
{
    LOGD(FPTAG"~fpExtension invoked");
    pthread_mutex_lock(&socket_lock);
    should_exit_status = 1;
    if (the_socket != NULL)
    {
        extension_socket_cancel(the_socket);
    }

    if (fpe_connection != NULL && fpe_connection->cmd_socket != NULL)
    {
        extension_socket_cancel(fpe_connection->cmd_socket);
    }

    pthread_mutex_unlock(&socket_lock);
    pthread_join(extension_thread, NULL);
    pthread_mutex_destroy(&socket_lock);

    if (extension_impl_instance)
    {
        delete extension_impl_instance;
        extension_impl_instance = NULL;
    }
}

int32_t fpExtension::try_exit()
{
    int32_t result = 0;
    pthread_mutex_lock(&socket_lock);
    result = should_exit_status;
    pthread_mutex_unlock(&socket_lock);
    return result;
}

int32_t fpExtension::do_start_enroll_cmd(socket_connection_t *socket_connection)
{
    int32_t response = -1;
    int32_t ret = 0;

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }
    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_start_enroll_cmd();
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

    response = SOCKET_RESP_OK;
    if (0 != ret)
    {
        LOGE(FPTAG"extension_start_enroll status error: %d", ret);
    }

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
    }

    return ret;

}

int32_t fpExtension::do_finish_enroll_cmd(socket_connection_t *socket_connection)
{
    int32_t response = -1;
    int32_t ret = 0;

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_finish_enroll_cmd();
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

    response = SOCKET_RESP_OK;
    if (0 != ret)
    {
        LOGE(FPTAG"extension_finish_enroll error: %d", ret);
    }
    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
    }

    return ret;
}

int32_t fpExtension::do_enroll_image_cmd(socket_connection_t *socket_connection)
{
    int32_t response = -1;
    int32_t ret = 0;

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_enroll_image_cmd();
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

    response = SOCKET_RESP_OK;
    if (0 != ret)
    {
        LOGE(FPTAG"do_enroll_image_cmd error: %d", ret);
    }
    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
    }

    return ret;
}

int32_t fpExtension::do_get_finger_rect_cnt_cmd(socket_connection_t *socket_connection)
{
    int32_t ret = -1;
    int32_t response = 0;
    int32_t rect_cnt = 0;

    LOGD(FPTAG"extension_get_finger_rect_cnt");


    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_get_finger_rect_cnt_cmd(&rect_cnt);
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

    response = SOCKET_RESP_OK;

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if ((0 != ret) )
    {
        LOGE(FPTAG"extension_socket_put 0 error: %d", ret);
        goto out;
    }

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &rect_cnt, sizeof(rect_cnt));
    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
    }
out:
    return ret;
}

int32_t fpExtension::do_get_size_cmd(socket_connection_t *socket_connection)
{
    int32_t ret = -1;
    int32_t width = 0;
    int32_t height = 0;
    int32_t response = 0;

    LOGD(FPTAG"extension_get_image_size");

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_get_size_cmd(&width, &height);
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }


    response = SOCKET_RESP_OK;

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if ((0 != ret))
    {
        LOGE(FPTAG"extension_socket_put 0 error: %d", ret);
        goto out;
    }

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &width, sizeof(width));
    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put 1 error: %d", ret);
        goto out;
    }

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &height, sizeof(height));
    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put 2 error: %d", ret);
    }
out:

    return ret;
}
int32_t fpExtension::do_set_property_cmd(socket_connection_t *socket_connection)
{
    int32_t ret = -1;
    int32_t response = 0;
    int32_t properity_tag = 0;
    int32_t properity_value = 0;

    LOGD(FPTAG"extension_set_properity");


    ret = extension_socket_get(socket_connection->cmd_socket,
                             &properity_tag, sizeof(properity_tag));
    if (ret)
    {
        LOGE(FPTAG"extension_set_properity fpe_socket_read error: %d", ret);
        goto out;
    }
    ret = extension_socket_get(socket_connection->cmd_socket,
                             &properity_value, sizeof(properity_value));
    if (ret)
    {
        LOGE(FPTAG"extension_set_properity fpe_socket_read error: %d", ret);
        goto out;
    }

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_set_property_cmd(properity_tag, properity_value);
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

out:
    response = SOCKET_RESP_OK;
    if (0 != ret)
    {
        LOGE(FPTAG"set_properity error: %d", ret);
    }
    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
    }

    return ret;
}

int32_t fpExtension::do_get_image_quality_cmd(socket_connection_t *socket_connection)
{
    int32_t ret = -1;
    int32_t response = 0;
    int32_t area = 0;
    int32_t codition = 0;
    int32_t quality = 0;

    LOGD(FPTAG"extension_get_image_quality");


    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_get_image_quality_cmd(&area, &codition, &quality);
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

    response = SOCKET_RESP_OK;
    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if ((0 != ret))
    {
        LOGE(FPTAG"extension_socket_put 0 error: %d", ret);
        goto out;
    }

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &area, sizeof(area));
    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put 1 error: %d", ret);
        goto out;
    }

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &codition, sizeof(codition));
    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put 2 error: %d", ret);
        goto out;
    }

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &quality, sizeof(quality));
    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put 3 error: %d", ret);
    }

out:
    return ret;
}

#define TEMPLATE_ID_MAX_SIZE (62)

int32_t fpExtension::do_get_template_ids_cmd(socket_connection_t *socket_connection)
{
    int32_t ret = 0;
    int32_t ids_size = 0;
    int32_t response = 0;
    static int32_t ids[TEMPLATE_ID_MAX_SIZE] = {0};

    LOGD(FPTAG"extension_get_template_ids");

    memset(ids, 0, TEMPLATE_ID_MAX_SIZE * sizeof(int32_t));

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_get_templateIds_cmd(ids, &ids_size);
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

    //out:
    response = SOCKET_RESP_OK;

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if ((0 != ret) )//|| (SOCKET_RESP_OK != response)
    {
        LOGE(FPTAG"extension_socket_put 0 error: %d", ret);
        goto out;
    }

    ret = extension_socket_put(socket_connection->cmd_socket, &ids_size, sizeof(ids_size));

    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put 1 error: %d", ret);
        goto out;
    }
    ret = extension_socket_put(socket_connection->cmd_socket,
                              (const uint8_t *)ids, TEMPLATE_ID_MAX_SIZE * sizeof(int32_t));
    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put 2 error: %d", ret);
    }
out:
    return ret;
}

#define RECT_DATA_LEN    (12)
int32_t fpExtension::do_get_finger_rect_cmd(socket_connection_t *socket_connection)
{
    int32_t ret = -1;
    int32_t rect_data[RECT_DATA_LEN] = {0};
    int32_t idx = 0;
    int32_t response = 0;


    LOGD(FPTAG"extension_get_finger_rect");

    ret = extension_socket_get(socket_connection->cmd_socket,
                             &idx, sizeof(idx));
    if (ret)
    {
        LOGE(FPTAG"extension_get_finger_rect fpe_socket_read error: %d", ret);
        goto arg_err;
    }

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_get_finger_rect_cmd(idx, rect_data);
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }


arg_err:
    response = SOCKET_RESP_OK;

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if ((0 != ret))
    {
        LOGE(FPTAG"extension_socket_put 0 error: %d", ret);
        goto out;
    }


    ret = extension_socket_put(socket_connection->cmd_socket,
                              (const uint8_t *)rect_data, RECT_DATA_LEN * sizeof(int32_t));
    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put 2 error: %d", ret);
    }
out:
    return ret;
}

void capture_image_func(void *para)
{
    //do image capture
    int32_t ret = -1;
    socket_connection_t *socket_connection = (socket_connection_t *)para;
    fpExtension *extension_this = (fpExtension *)socket_connection->para;
    int32_t mode = socket_connection->mode;

    fp_capture_image_data_t capture_image_data;
    capture_image_data.capture_result = -EBUSY;
    capture_image_data.image_data = (char *)malloc(160 * 160 );
    memset(capture_image_data.image_data, 0, 160 * 160  );

    if (extension_this->extension_impl_instance)
    {
        ret = extension_this->extension_impl_instance->capture_image_func(mode, &capture_image_data);
    }

    socket_callback_t cmd = EXTENSION_CB_CAPTUREIMGAETOOL_DATA_RECVED;
    ret = extension_socket_put(socket_connection->data_socket, &cmd, sizeof(cmd));

    if (ret)
    {
        goto out;
    }

    ret = extension_socket_put(socket_connection->data_socket,
                              &capture_image_data.capture_result,
                              sizeof(capture_image_data.capture_result));

    if (ret || capture_image_data.capture_result)
    {
        LOGE(FPTAG"capture_result error:%d", capture_image_data.capture_result);
        goto out;
    }
    ret = extension_socket_write_byte_array(socket_connection->data_socket,
                                              (const uint8_t *)capture_image_data.image_data,
                                              capture_image_data.image_length);
out:
    free(capture_image_data.image_data);
    if (ret)
    {
        LOGE(FPTAG"socket_write failed for %s %d", __func__, ret);
    }
}

int32_t fpExtension::do_capture_raw_image_cmd(socket_connection_t *socket_connection)
{
    uint32_t data = 0;
    int32_t ret = -1;
    int32_t response = 0;

    LOGD(FPTAG"extension_captureimage");

    ret = extension_socket_get(socket_connection->cmd_socket,
                             &data, sizeof(data));

    if (ret)
    {
        goto out;
    }


    LOGD(FPTAG "wait for finger mode:%d", data);

    socket_connection->mode = data;

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (worker_instance)
    {
        worker_instance->set_thread_task(capture_image_func, socket_connection);
    }

    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

out:
    response = SOCKET_RESP_OK;
    if (0 != ret)
    {
        LOGE(FPTAG"extension_captureimage error: %d", ret);
    }
    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
    }

    return ret;
}

void navigation(void *arg)
{
    fpDameonImpl *pDaemonImpl = (fpDameonImpl *)arg;
    pDaemonImpl->tac_instance->navigation_loop();
    return ;
}

int32_t fpExtension::do_tool_control_cmd(socket_connection_t *socket_connection)
{
    int32_t ret = -1;
    int32_t response = 0;
    int32_t tag = 0;
    int32_t value = 0;
    int32_t result = -1;

    LOGD(FPTAG"extension_tool_control");


    ret = extension_socket_get(socket_connection->cmd_socket,
                             &tag, sizeof(tag));
    if (ret)
    {
        LOGE(FPTAG"extension_tool_control fpe_socket_read error: %d", ret);
        goto out;
    }
    ret = extension_socket_get(socket_connection->cmd_socket,
                             &value, sizeof(value));
    if (ret)
    {
        LOGE(FPTAG"extension_tool_control fpe_socket_read error: %d", ret);
        goto out;
    }

    LOGD(FPTAG"extension_tool_control tag: %d, value: %d", tag, value);

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
            result = 0;
        }
        else if (FP_SERVICE_CONTROL_CMD_NAVIGATOR_GET_STATUS == tag )
        {
            result = (worker_instance->def_thread_work.function_ptr != 0);
        }
        else if (FP_SERVICE_CONTROL_CMD_MISC_PRINT_SYSTEM_INFO == tag)
        {
            daemon_instance->goto_idle();
            if(daemon_instance && daemon_instance->tac_instance)
            {
                daemon_instance->tac_instance->print_system_info();
                result = 0;
            }
            else
            {
                LOGI(FPTAG"Print system info error,some instance is null");
                result = -ENOENT;
            }
            worker_instance->resume_thread();
        }
        else if(FP_SERVICE_CONTROL_CMD_RELEASE_MODE_ENABLE_LOGD == tag)
        {
            enable_logd_in_release_mode = value;
            LOGI(FPTAG"FP_SERVICE_CONTROL_CMD_RELEASE_MODE_ENABLE_LOGD enable_logd_in_release_mode = %d",enable_logd_in_release_mode);
            result = 0;
        }
        else if(FP_SERVICE_CONTROL_CMD_INJECTION_AUTHENTICATE_TEST == tag)
        {
            LOGI(FPTAG"FP_SERVICE_CONTROL_CMD_INJECTION_AUTHENTICATE_TEST received");
#ifdef DEBUG_ENABLE
            result = daemon_instance->tac_instance->injection_authenticate();
#endif
            LOGI(FPTAG"FP_SERVICE_CONTROL_CMD_INJECTION_AUTHENTICATE_TEST result = %d",result);
        }
        else if(FP_SERVICE_CONTROL_CMD_MISC_CONTROL_AGING_TEST == tag)
        {
            g_enable_report_auth_result = value;
            LOGI(FPTAG"FP_SERVICE_CONTROL_CMD_MISC_CONTROL_AGING_TEST g_enable_report_auth_result = %d",g_enable_report_auth_result);
            result = 0;
        }
        else
        {
            daemon_instance->goto_idle();
            result = extension_impl_instance->do_tool_control_cmd( tag, value);
            worker_instance->resume_thread();
        }
    }

out:
    response = SOCKET_RESP_OK;
    if (0 != ret)
    {
        LOGE(FPTAG"extension_tool_control error: %d", ret);
    }
    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
    }

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &result, sizeof(result));

    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
    }

    return ret;
}

int32_t fpExtension::do_self_test_cmd(socket_connection_t *socket_connection)
{
    int32_t ret = -1;
    int32_t test_result = 0;
    int32_t response = 0;


    LOGD(FPTAG"extension_selftest");

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_selftest_cmd( &test_result);
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

    response = SOCKET_RESP_OK;

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if ((0 != ret) )
    {
        LOGE(FPTAG"extension_socket_put 0 error: %d", ret);
        goto out;
    }

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &test_result, sizeof(test_result));
    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put 2 error: %d", ret);
    }
out:
    return ret;
}
int32_t fpExtension::do_check_board_cmd(socket_connection_t *socket_connection)
{
    int32_t ret = -1;
    int32_t test_result = -1;
    int32_t response = 0;


    LOGD(FPTAG"extension_checkboard");


    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_checkboard_cmd( &test_result);
    }
    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

    response = SOCKET_RESP_OK;

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if ((0 != ret) )
    {
        LOGE(FPTAG"extension_socket_put 0 error: %d", ret);
        goto out;
    }

    ret = extension_socket_put(socket_connection->cmd_socket,
                              &test_result, sizeof(test_result));
    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put 2 error: %d", ret);
    }
out:
    return ret;
}

int32_t fpExtension::do_navigator_addkey_cmd(socket_connection_t *socket_connection)
{
    int32_t ret = -1;
    int32_t key_cnt = 0;
    int32_t response = 0;
    int32_t *key_array = NULL;
    size_t key_array_size = 0;

    LOGD(FPTAG"do_navigator_addkey_cmd invoked");

    ret = extension_socket_get(socket_connection->cmd_socket, &key_cnt, sizeof(key_cnt));

    if (ret)
    {
        LOGE(FPTAG"extension_tool_control fpe_socket_read error: %d", ret);
        goto out;
    }

    key_array_size = (sizeof(int32_t) * key_cnt);
    key_array = (int32_t *)fp_malloc(key_array_size);
    if (!key_array)
    {
        ret = -ENOMEM;
        LOGE(FPTAG"extension_tool_control fpe_socket_read error: %d", ret);
        goto out;
    }

    ret = extension_socket_get(socket_connection->cmd_socket, key_array, key_array_size);

    if (ret)
    {
        LOGE(FPTAG"extension_tool_control fpe_socket_read error: %d", ret);
        goto out;
    }

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (extension_impl_instance)
    {
        ret = extension_impl_instance->do_navigator_addkey_cmd(key_array, key_cnt);
    }

    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

    response = SOCKET_RESP_OK;

    ret = extension_socket_put(socket_connection->cmd_socket, &response, sizeof(response));

    if ((0 != ret) )
    {
        LOGE(FPTAG"extension_socket_put 0 error: %d", ret);
        goto out;
    }

out:
    if (key_array)
    {
        fp_free(key_array);
    }

    return ret;
}

void finger_detect_func(void *para)
{
    //do image capture
    int32_t ret = -1;
    int32_t finger_detect_result = -1;
    socket_connection_t *socket_connection = (socket_connection_t *)para;
    fpExtension *extension_this = (fpExtension *)socket_connection->para;

    if (extension_this->extension_impl_instance)
    {
        extension_this->extension_impl_instance->finger_detect_func(0,&finger_detect_result);
    }

    socket_callback_t cmd = EXTENSION_CB_CAPTURE_FINGER_EVENT_RECVED;
    ret = extension_socket_put(socket_connection->data_socket, &cmd, sizeof(cmd));

    if (ret)
    {
        return;
    }

    extension_socket_put(socket_connection->data_socket,&finger_detect_result,sizeof(finger_detect_result));
}

int32_t fpExtension::do_finger_detect_test(socket_connection_t *socket_connection)
{
    uint32_t data = 0;
    int32_t ret = -1;
    int32_t response = 0;

    LOGD(FPTAG"do_finger_detect_test");

    ret = extension_socket_get(socket_connection->cmd_socket,
                             &data, sizeof(data));

    if (ret)
    {
        goto out;
    }

    LOGD(FPTAG "dummy value:%d", data);

    if (daemon_instance)
    {
        daemon_instance->goto_idle();
    }

    if (worker_instance)
    {
        worker_instance->set_thread_task(finger_detect_func, socket_connection);
    }

    if (worker_instance)
    {
        worker_instance->resume_thread();
    }

out:
    response = SOCKET_RESP_OK;
    if (0 != ret)
    {
        LOGE(FPTAG"extension_captureimage error: %d", ret);
    }
    ret = extension_socket_put(socket_connection->cmd_socket,
                              &response, sizeof(response));

    if (0 != ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
    }

    return ret;
}

int32_t fpExtension::report_auth_result(int32_t fid, int32_t auth_time)
{
    LOGD(FPTAG"report_auth_result invoked,fid:%d, auth_time:%d",fid,auth_time);

    socket_callback_t cmd = EXTENSION_CB_REPORT_AUTH_RESULT_RECVED;
    int32_t ret = extension_socket_put(fpe_connection->data_socket, &cmd, sizeof(cmd));

    if (ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
        return ret;
    }

    int32_t auth_result[2];
    auth_result[0] = fid;
    auth_result[1] = auth_time;
    ret = extension_socket_put(fpe_connection->data_socket,&auth_result,sizeof(auth_result));
    if (ret)
    {
        LOGE(FPTAG"extension_socket_put error: %d", ret);
    }

    return ret;
}

int32_t fpExtension::socket_handler(socket_connection_t *socket_connection)
{
    int32_t ret = 0;
    int32_t command = 0;

    while (1)
    {
        ret = poll_extension_socket(socket_connection->cmd_socket, POLLIN);
        if (ret != POLLIN)
        {
            ret = 0;
            goto err0;
        }

        if (try_exit())
        {
            LOGE(FPTAG"socket_handler shouldExit3");
            ret = -FP_ERROR_USER_CANCEL;
            goto err0;
        }
        ret = extension_socket_get(socket_connection->cmd_socket, &command, sizeof(command));

        if (ret != 0)
        {
            LOGE(FPTAG"extension_socket_get failed with error %d", ret);
            goto err0;
        }

        daemon_instance->lock_mutex();
        switch (command)
        {
            case EXTENSION_CMD_IMAGECAPTURETOOL_STARTENROLL:
            {
                ret  = do_start_enroll_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG"start enroll failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_IMAGECAPTURETOOL_FINISHENROLL:
            {
                ret  = do_finish_enroll_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG"finish enroll failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_IMAGECAPTURETOOL_ENROLLIMAGE:
            {
                ret  = do_enroll_image_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do enroll image failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_IMAGECAPTURETOOL_GETFINGERRECTCNT:
            {
                ret  = do_get_finger_rect_cnt_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do fingerrect cnt failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_IMAGECAPTURETOOL_GETSIZE:
            {
                ret  = do_get_size_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do getsize failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_IMAGECAPTURETOOL_SETPROPERTY:
            {
                ret  = do_set_property_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do getsize failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_IMAGECAPTURETOOL_GETIMAGEQUALITY:
            {
                ret  = do_get_image_quality_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do getImagequality  failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_IMAGECAPTURETOOL_GETTEMLATEIDS:
            {
                ret  = do_get_template_ids_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do gettemplateids  failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_IMAGECAPTURETOOL_GETFINGERRECT:
            {
                ret  = do_get_finger_rect_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do getfingerrect  failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_IMAGECAPTURETOOL_CAPTURERAWIMAGE:
            {
                ret  = do_capture_raw_image_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do capture raw image  failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_IMAGECAPTURETOOL_CONTROL:
            {
                ret  = do_tool_control_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do capture tool control  failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_FPSENSOR_SELFTEST:
            {
                ret  = do_self_test_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do_self_test_cmd failed : %d", ret);
                    goto err;
                }
            }
            break;
            case EXTENSION_CMD_FPSENSOR_CHECEKBOARD:
            {
                ret  = do_check_board_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do_check_board_cmd failed : %d", ret);
                    goto err;
                }
            }
            break;

            case EXTENSION_CMD_FPSENSOR_NAVIGATOR_ADDKEY:
            {
                ret  = do_navigator_addkey_cmd(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do_navigator_addkey_cmd failed : %d", ret);
                    goto err;
                }

            }
            break;

            case EXTENSION_CMD_FPSENSOR_DETECT_FINGER:
            {
                ret  = do_finger_detect_test(socket_connection);
                if (ret)
                {
                    LOGE(FPTAG" do_finger_detect_test failed : %d", ret);
                    goto err;
                }

            }
            break;

            default:
                LOGE("Unknown extension");
                goto err;
        }
        daemon_instance->unlock_mutex();
    }
err:
    daemon_instance->unlock_mutex();
err0:
    return ret;
}

