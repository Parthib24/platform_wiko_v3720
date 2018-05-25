#include <unistd.h>
#include <errno.h>
#include <poll.h>
#include <string.h>
#include <fcntl.h>
#include "fp_common.h"
#include "fp_log.h"
#include "extension_socket.h"
#include <sys/socket.h>
#include <sys/un.h>


#define FPTAG " extension_socket "

static extension_socket_t *create_extension_socket()
{
    extension_socket_t *sock = (extension_socket_t *)fp_malloc(sizeof(extension_socket_t));
    if (sock == NULL)
    {
        errno = -ENOMEM;
        return NULL;
    }
    else
    {
        sock->fd = sock->cancel_pipe_fd[0]  = sock->cancel_pipe_fd[1] = -1;
        return sock;
    }
}

int extension_socket_send_fd(extension_socket_t *ext_sock, int fd)
{
    struct msghdr msg;
    struct cmsghdr *ctl_message;

    char control_buffer[CMSG_SPACE(sizeof(fd))];

    struct iovec dummy;
    dummy.iov_len = 1;
    dummy.iov_base = &dummy;
    msg.msg_iovlen = 1;
    msg.msg_iov = &dummy;
    msg.msg_namelen = 0;
    msg.msg_name = NULL;

    msg.msg_control = control_buffer;
    msg.msg_controllen = sizeof(control_buffer);
    ctl_message = CMSG_FIRSTHDR(&msg);
    ctl_message->cmsg_level = SOL_SOCKET;
    ctl_message->cmsg_type = SCM_RIGHTS;
    ctl_message->cmsg_len = CMSG_LEN(sizeof(fd));

    memcpy(CMSG_DATA(ctl_message), &fd, sizeof(fd));

    msg.msg_controllen = ctl_message->cmsg_len;

    if (sendmsg(ext_sock->fd, &msg, 0) == -1)
    {
        return -errno;
    }

    return 0;
}

extension_socket_t *extension_socket_accept(extension_socket_t *ext_sock)
{
    struct sockaddr_un peer;
    socklen_t size = sizeof(peer);
    
    extension_socket_t *extension_client = create_extension_socket();
    if (!extension_client) return NULL;

    extension_client->fd = accept(ext_sock->fd, (struct sockaddr *) &peer,&size);                        

    if (extension_client->fd == -1)
    {
        LOGE(FPTAG"accept error\n");
        goto err;
    }

    if (pipe(extension_client->cancel_pipe_fd)) goto err;

    return extension_client;
err:
    remove_extension_socket(extension_client);
    return NULL;
}


void remove_extension_socket(extension_socket_t *ext_sock)
{
    if (ext_sock != NULL)
    {
        if (ext_sock->fd != -1) close(ext_sock->fd);
        if (ext_sock->cancel_pipe_fd[0] != -1) close(ext_sock->cancel_pipe_fd[0]);
        if (ext_sock->cancel_pipe_fd[1] != -1) close(ext_sock->cancel_pipe_fd[1]);
        fp_free(ext_sock);
    }
}

int extension_socket_get(extension_socket_t *ext_sock, void *buffer, size_t length)
{
    int ret = 0;
    uint8_t *tmp = (uint8_t *) buffer;
    ssize_t count = 0;
    size_t readed = 0;
    try
    {
        while (length)
        {
            ret = poll_extension_socket(ext_sock, POLLIN);
            
            if (ret != POLLIN) goto err;

            count = read(ext_sock->fd, tmp, length);
            if (count == 0)
            {
                ret  = 1;
                goto err;
            }
            else if (count < 0)
            {
                if (EAGAIN == errno )
                {
                    continue;
                }
                else
                {
                    ret = -errno;
                    goto err;
                }
            }
            tmp += count;
            readed += count;
            length -= count;
        }
    }
    catch (...)
    {
        LOGE(FPTAG"socket_read error, catched! return -EIO");
        return -EIO;
    }
    LOGD(FPTAG"socket_read %zu, first_int:0x%x", readed, *((int *)tmp));
    return 0;
err:
    LOGD(FPTAG"socket_read error %d", ret);
    return ret;
}

int extension_socket_put(extension_socket_t *ext_sock, const void *buffer, size_t length)
{
    int ret = 0;
    size_t wrote = 0;
    ssize_t count = 0;
    uint8_t *tmp = (uint8_t *) buffer;

    try
    {
        while (length)
        {
            ret = poll_extension_socket(ext_sock, POLLOUT);

            if (ret != POLLOUT) goto err;
             
            count = write(ext_sock->fd, tmp, length);
            if (count == 0)
            {
                ret  = 1;
                goto err;
            }
            else if (count < 0)
            {
                if (errno == EAGAIN)
                {
                    continue;
                }
                else
                {
                    ret = -errno;
                    goto err;
                }
            }
            wrote += count;
            tmp += count;
            length -= count;
        }
    }
    catch (...)
    {
        LOGE(FPTAG"socket_write error, catched! return -EIO");
        return -EIO;
    }
    LOGD(FPTAG"socket_write %zu ,first_int: 0x%x \n", wrote, *((int *)buffer));
    return 0;
err:
    LOGD(FPTAG"socket_write error %d", ret);
    return ret;
}

int extension_socket_cancel(extension_socket_t *ext_sock)
{
    char dummy = 1;
    if (sizeof(char) != write(ext_sock->cancel_pipe_fd[1], &dummy, sizeof(char)))
    {
        return -errno;
    }

    return 0;
}

int poll_extension_socket(extension_socket_t *ext_sock, int poll_event)
{
    struct pollfd pfd[2];
    int ret = 0;
    uint8_t dummy;
    
    pfd[0].events = poll_event;
    pfd[0].fd = ext_sock->fd;
    pfd[1].events = POLLIN;
    pfd[1].fd = ext_sock->cancel_pipe_fd[0];

    ret = poll(pfd, 2, -1);

    if (ret == -1) return -errno;

    if (pfd[1].revents)
    {
        if (pfd[1].revents & POLLIN)
        {
            if (read(ext_sock->cancel_pipe_fd[0], &dummy, sizeof(uint8_t)) < 0)
            {
                return -errno;
            }
        }
        return -EINTR;
    }

    if (pfd[0].revents & poll_event)
    {
        return poll_event;
    }

    return POLLHUP;
}

int extension_socket_write_byte_array(extension_socket_t *ext_sock, const uint8_t *msg,
                                     uint32_t size)
{
	int ret = 0;
    try
    {
        ret = extension_socket_put(ext_sock, &size, sizeof(size));

        if (ret) return ret;

        if (size > 0)
        {
            ret = extension_socket_put(ext_sock, msg, size);

            if (ret) return ret;
        }
    }
    catch (...)
    {
        LOGE(FPTAG"extension_socket_write_byte_array error, catched! return -EIO");
        return -EIO;
    }
    return 0;
}


extension_socket_t *extension_socket_create(const char *path)
{
    struct sockaddr_un sock_addr;
    extension_socket_t *ext_sock = create_extension_socket();
    if (!ext_sock) return NULL;

    if (sizeof(sock_addr.sun_path) <= strlen(path))
    {
        LOGE(FPTAG"socket path too long");
        errno = EOVERFLOW;
        goto err;
    }

    ext_sock->fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (ext_sock->fd == -1)
    {
        LOGE(FPTAG"failed to create socket fd\n");
        goto err;
    }

    if (fcntl(ext_sock->fd, F_SETFL, O_NONBLOCK) == -1)
    {
        goto err;
    }

    memset(&sock_addr, 0, sizeof(sock_addr));
    sock_addr.sun_family = AF_UNIX;
    strncpy(sock_addr.sun_path, path, sizeof(sock_addr.sun_path));
    unlink(path);

    if (bind(ext_sock->fd, (struct sockaddr *) &sock_addr,
             sizeof(struct sockaddr_un)))
    {
        LOGE(FPTAG"bind socket error %d", -errno);
        goto err;
    }

    if (listen(ext_sock->fd, 1) != 0)
    {
        LOGE(FPTAG"listen failed\n");
        goto err;
    }

    if (pipe(ext_sock->cancel_pipe_fd))
    {
        goto err;
    }

    return ext_sock;

err:
    remove_extension_socket(ext_sock);
    return NULL;
}

extension_socket_t *extension_socket_from_sockfd(int sock_fd)
{
    extension_socket_t *ext_sock = create_extension_socket();
    if (!ext_sock) return NULL;

    if (pipe(ext_sock->cancel_pipe_fd))
    {
        goto err;
    }

    if (fcntl(sock_fd, F_SETFL, O_NONBLOCK) == -1)
    {
        goto err;
    }
	
    ext_sock->fd = sock_fd;
    return ext_sock;

err:
    remove_extension_socket(ext_sock);
    return NULL;
}
