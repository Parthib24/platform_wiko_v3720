#include <string.h>
#include <fcntl.h>  
#include <stdlib.h> 
#include <stdio.h> 
#include <errno.h> 
#include <stdint.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <linux/input.h>
#include <poll.h>

#include <semaphore.h>
#include <signal.h>
#include <pthread.h>

#include "fp.h"

#undef LOG_TAG
#define LOG_TAG "[Tinnofingerprint-elan_jni] "
#define TAG LOG_TAG

#define ELAN_FP_DEV "/dev/elan_fp"
#define ELAN_IOCTLID    0x80
#define IOCTL_READ_KEY_STATUS    _IOW(ELAN_IOCTLID, 10, int)


static int elan_fd = 0;
static sem_t msg_sem;

static int finger_state = 0;

void elan_input_handler(int signum)  
{	
    finger_state = ioctl(elan_fd, IOCTL_READ_KEY_STATUS, 0);
    sem_post(&msg_sem);		 
}

void elan_fasync_notify()
{
    int oflags;
    
    signal(SIGIO, elan_input_handler);
    fcntl(elan_fd, F_SETOWN, getpid());
    oflags = fcntl(elan_fd, F_GETFL);
    fcntl(elan_fd, F_SETFL, oflags | FASYNC);
}

int elan_dev_init(void) 
{
    int ret = 0;
    
    elan_fd = open(ELAN_FP_DEV, O_RDWR);
    if (elan_fd <= 0) {
        LOGD(TAG " open: %s failed!", ELAN_FP_DEV);	
        return -1;
    }
    else {
        LOGD(TAG"open elan_fd ok \r\n");	
    }
    
    elan_fasync_notify();
    return ret;
}

int elan_update_finger_state(int *s)
{
    sem_wait(&msg_sem);
    *s = finger_state;
    return 0;
}

int elan_dev_uninit(void)
{
    if (elan_fd > 0) {
        close(elan_fd);
        elan_fd = -1;
        return 0;
    }
    return -1;
}
/*****************************************************************/
