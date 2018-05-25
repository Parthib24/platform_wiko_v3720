/*
 * Copyright (c) 2014-2015, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include <pthread.h>  
#include <sys/mount.h>
#include <sys/statfs.h>
#include <dirent.h>
#include <linux/input.h>
#include <linux/netlink.h>  
#include <sys/socket.h>  
#include <math.h>
#include <dlfcn.h>
#include <semaphore.h>
#include <signal.h>

#include <sys/mount.h>
#include "mmi_module.h"

#include <sys/poll.h>


/************************************************************************************/
#define TAG "[Tinnofingerprint-fingerprint-goodix] "
#define ALOGE(...) __android_log_print(ANDROID_LOG_FATAL  , TAG, __VA_ARGS__)
#define ALOGD ALOGE
#define ALOGI ALOGE
#define LOGD ALOGE

#define DRV_GOODIX 	 "/dev/goodix_fp"

/**********************IO Magic**********************/
#define  GF_IOC_MAGIC    'g'  //define magic number
     
#define  GFX1XM_IOC_FTM	_IOW(GF_IOC_MAGIC, 101, int)
#define  GFX1XM_IOC_SET_MODE	_IOW(GF_IOC_MAGIC, 102, int)
/************************************************************************************/

static int _dev_fd = -1;
static int fp_exit;
static sem_t wk_sem;
static pthread_t thd_id =0;
static int finger_state = 0;
/************************************************************************************/

void *thread_do_poll(void * arg) 
{
    int ret = -1;
    int state = 0; 
    struct pollfd pfd;
	
    pfd.fd = _dev_fd ;
    pfd.events = POLLIN;
	
    while (1) {     
        ret = poll(&pfd, 1, -1);
        if (ret == 0) {
			
        }
        else {    
            //sem_post(&wk_sem);
            if ((ret = ioctl(_dev_fd, GFX1XM_IOC_FTM, &state)) < 0) {
                finger_state = 0;
                ALOGE("--ioctl--  err!\n");  
                continue;
            }
            finger_state = state;
            ALOGI("finger_state:[%d]\n", state);  
        }
    }
    pthread_exit(NULL);  
    return NULL;
}

int goodix_dev_init(void)
{
    int fd;
    int ret;
    
    if ((fd = open(DRV_GOODIX, O_RDWR)) < 0) {
        ALOGE(TAG"open:%s err!\n", DRV_GOODIX);  
        return -1;
    }
    
    if (pthread_create(&thd_id, NULL, thread_do_poll, NULL) ) {
        ALOGE(TAG" thd_id thread create failed.!\n");
        return -1;
    }
    
    _dev_fd = fd;  
    fp_exit = 0;
    ALOGE(TAG"[%s] init success!\n",__func__);  
    return 0;
}

int goodix_update_finger_state(int *s) 
{
	int state = 0;  
	int ret = 0;

	*s = (finger_state == 1) ? 1 : 0;
	usleep(5*1000);
	return 0;
}

int goodix_dev_uninit(void)
{
	int fd = _dev_fd;
	ALOGE("-->[%s]\n",__func__);  
	
	if(fd > 0)
	{
		close(fd);
		_dev_fd = -1;
		fp_exit = 1;
		//sem_post(&wk_sem);
		return 0;
	}
	
	return -1;
}

