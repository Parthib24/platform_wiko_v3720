/*
 * Copyright (c) 2014-2015, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
#include <string.h> 
#include <fcntl.h>  
#include <stdlib.h> 
#include <stdio.h> 
#include <errno.h> 
#include <stdint.h>
#include <sys/statfs.h>
#include <sys/mount.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <linux/input.h>
#include <poll.h>

#include <semaphore.h>
#include <signal.h>
#include <utils/Log.h>
#include "mmi_module.h"


#define ELAN_FP_DEV "/dev/elan_fp"

#define TAG "[Tinnofingerprint-fingerprint-elan] "
#undef ALOGE
#define ALOGE(...) __android_log_print(ANDROID_LOG_FATAL  , TAG, __VA_ARGS__)
#define ALOGD ALOGE
#define ALOGI ALOGE
#define LOGD ALOGE

#define ELAN_IOCTLID 0x80
#define IOCTL_READ_KEY_STATUS    _IOW(ELAN_IOCTLID, 10, int)

static int elan_fd = 0;

static int fp_exit = 0;
static sem_t wk_sem;
static pthread_t thd_id =0;
static int finger_state = 0;

static sem_t msg_sem;
static bool first_enter = false;

void elan_input_handler(int signum)  
{
	int state;
	finger_state = ioctl(elan_fd, IOCTL_READ_KEY_STATUS, &state);
	ALOGE(  "[elan_ffbm] elan_input_handler:[%d]\n", finger_state);		 
}

void elan_fasync_notify()
{
	int oflags;
	ALOGE( " [elan_ffbm] elan_fasync_notify#####"); 		
	signal(SIGIO, elan_input_handler);
	fcntl(elan_fd, F_SETOWN, getpid());
	oflags = fcntl(elan_fd, F_GETFL);
	fcntl(elan_fd, F_SETFL, oflags | FASYNC);
}

int elan_dev_init(void) 
{
	int ret = 0;
	
	elan_fd = open(ELAN_FP_DEV, O_RDWR);
	if(elan_fd <= 0){
		ALOGE(" [elan_ffbm] open: %s err: %d", ELAN_FP_DEV, elan_fd);	
		return -1;
	}
	else{
		ALOGE(" [elan_ffbm] open elan_fd ok \r\n");	
	}
	elan_fasync_notify();
	first_enter = true;  
	fp_exit = 0;
	return ret;
}

int elan_update_finger_state(int *s)
{
        if (first_enter) {
            *s = 0;
	     first_enter = false;
             return 0;
        }
 
        *s = (finger_state== 1 ? 1 : 0);    
        usleep(5*1000);
        return 0;	
}

int elan_dev_uninit(void)
{
	if(elan_fd > 0)
	{
		close(elan_fd);
		elan_fd = -1;
		fp_exit = 1;
		return 0;
	}
	return -1;
}


