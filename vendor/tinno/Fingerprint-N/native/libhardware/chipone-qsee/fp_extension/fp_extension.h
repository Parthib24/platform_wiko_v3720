#ifndef FP_EXTENSION_H
#define FP_EXTENSION_H


#include <pthread.h>
#include "extension_socket.h"

class thread_task;
class fpTacImpl;
class fpTestIntf;
class fpDameonImpl;

struct socket_connection_t
{
    extension_socket_t *cmd_socket;
    extension_socket_t *data_socket;
    int32_t mode;
    void *para;
};

class fpExtension
{
  public:
    fpExtension(thread_task *worker_impl, fpTacImpl *tac_impl, fpDameonImpl *daemon_impl);
    ~fpExtension();

    int32_t try_exit(void);
    int32_t socket_handler(socket_connection_t *fp_extension_socket) ;

    int32_t do_start_enroll_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_finish_enroll_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_enroll_image_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_get_finger_rect_cnt_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_get_size_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_set_property_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_get_image_quality_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_get_template_ids_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_get_finger_rect_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_capture_raw_image_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_tool_control_cmd(socket_connection_t *fp_extension_socket);

    int32_t do_self_test_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_check_board_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_navigator_addkey_cmd(socket_connection_t *fp_extension_socket);
    int32_t do_finger_detect_test(socket_connection_t *socket_connection);

    int32_t report_auth_result(int32_t fid, int32_t auth_time);

    pthread_t extension_thread;
    pthread_mutex_t socket_lock;
    int32_t should_exit_status;
    socket_connection_t *fpe_connection;
    extension_socket_t *the_socket;
    thread_task *worker_instance;
    fpTestIntf *extension_impl_instance;
    fpDameonImpl *daemon_instance;
};
#endif
