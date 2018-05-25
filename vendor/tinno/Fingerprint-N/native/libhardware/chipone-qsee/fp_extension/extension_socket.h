#ifndef EXTENSION_SOCKET_H
#define EXTENSION_SOCKET_H
#include <unistd.h>
#include <sys/types.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct
{
    int fd;
    int cancel_pipe_fd[2];
} extension_socket_t;

typedef enum
{
    EXTENSION_CMD_UNKNOWN  = 1,
    EXTENSION_CMD_IMAGECAPTURETOOL_STARTENROLL = 20,
    EXTENSION_CMD_IMAGECAPTURETOOL_FINISHENROLL,
    EXTENSION_CMD_IMAGECAPTURETOOL_ENROLLIMAGE,
    EXTENSION_CMD_IMAGECAPTURETOOL_GETFINGERRECTCNT,
    EXTENSION_CMD_IMAGECAPTURETOOL_GETSIZE,
    EXTENSION_CMD_IMAGECAPTURETOOL_SETPROPERTY,
    EXTENSION_CMD_IMAGECAPTURETOOL_GETIMAGEQUALITY,
    EXTENSION_CMD_IMAGECAPTURETOOL_GETTEMLATEIDS,
    EXTENSION_CMD_IMAGECAPTURETOOL_GETFINGERRECT,
    EXTENSION_CMD_IMAGECAPTURETOOL_CAPTURERAWIMAGE,
    EXTENSION_CMD_IMAGECAPTURETOOL_CONTROL,

    EXTENSION_CMD_FPSENSOR_SELFTEST,
    EXTENSION_CMD_FPSENSOR_CHECEKBOARD,

    EXTENSION_CMD_FPSENSOR_NAVIGATOR_ENABLE, //this cmd is replaced by toolcontrol,cmdid= 100
    EXTENSION_CMD_FPSENSOR_NAVIGATOR_CONFIG, //this cmd is replaced by toolcontrol, cmdid = 102,103 enable or disable a key
    EXTENSION_CMD_FPSENSOR_NAVIGATOR_ADDKEY,
    EXTENSION_CMD_FPSENSOR_DETECT_FINGER,
} socket_command_t;


typedef enum
{
    EXTENSION_CB_UNKNOWN = 1,
    EXTENSION_CB_CAPTUREIMGAETOOL_DATA_RECVED = 12,
    EXTENSION_CB_CAPTURE_FINGER_EVENT_RECVED = 13,
    EXTENSION_CB_REPORT_AUTH_RESULT_RECVED = 14,
} socket_callback_t;


extension_socket_t *extension_socket_create(const char *path);
extension_socket_t *extension_socket_accept(extension_socket_t *ext_socket);
int poll_extension_socket(extension_socket_t *sock, int poll_event);
extension_socket_t *extension_socket_from_sockfd(int fd);
void remove_extension_socket(extension_socket_t *ext_socket);
int extension_socket_get(extension_socket_t *ext_socket, void *buf, size_t length);
int extension_socket_put(extension_socket_t *ext_socket, const void *buf, size_t length);
int extension_socket_cancel(extension_socket_t *ext_socket);
int extension_socket_write_byte_array(extension_socket_t *ext_socket, const uint8_t *msg,
                                     uint32_t size);
int extension_socket_send_fd(extension_socket_t *ext_socket, int fd);

#ifdef __cplusplus
}
#endif

#endif
