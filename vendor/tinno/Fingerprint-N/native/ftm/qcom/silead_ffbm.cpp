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

#include <utils/Log.h>
#include "mmi_module.h"

/************************************************************************************/
#undef LOG_TAG
#define LOG_TAG "[Tinnofingerprint-silead-ftm] "
#define TAG LOG_TAG


#define DRV_SILEAD 	 "/dev/silead_fp_dev"

#define  SPI_IOC_MAGIC    'k' 

#define  IOCTL_FINGER_STATE_INFO	_IOR(SPI_IOC_MAGIC, 81, __u32)

/************************************************************************************/

static int _dev_fd = -1;
static int fp_exit;
static sem_t wk_sem;
static int state = 0;

/************************************************************************************/
int silead_dev_init(void);
int silead_dev_uninit(void);
int silead_update_finger_state(int *s);

/************************************************************************************/
static void fasync_input_handler(int signum)  
{
	//LOGD(TAG "[%s] : %d \n", __func__, signum);
	//sem_post(&wk_sem);	
	int ret = 0;
	if((ret = ioctl(_dev_fd, IOCTL_FINGER_STATE_INFO, &state)) < 0)
	{
	    ALOGE(TAG "--ioctl--  err!\n");  
	}	 
}

static void register_fasync_notify(int fd)
{
	int flag; 

	ALOGE(TAG"[%s]\n", __func__);
	signal(SIGIO, fasync_input_handler);
	flag = fcntl(fd, F_GETFL);
	flag |= FASYNC;
	fcntl(fd, F_SETFL, flag);
	fcntl(fd, F_SETOWN, getpid());
} 

int silead_dev_init(void)
{
	int fd;

	ALOGE(TAG "-->[%s]\n",__func__);  

	if((fd = open(DRV_SILEAD, O_RDWR)) < 0)
	{
		ALOGE(TAG "open:%s err!\n", DRV_SILEAD);  
		return -1;
	}

	_dev_fd = fd;  
	fp_exit = 0;
	
	register_fasync_notify(fd);
	return 0;
}

int silead_dev_uninit(void)
{
	int fd = _dev_fd;
	ALOGE(TAG "-->[%s]\n",__func__);  
	
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

int silead_update_finger_state(int *s) 
{
	//int state = 0;
	int ret = 0;

	//sem_wait(&wk_sem);

	if(fp_exit) 
	{ 
		ALOGE(TAG "[%s]: Exit~\n",__func__);  
		return -1; 
	}
	/*
	if((ret = ioctl(_dev_fd, IOCTL_FINGER_STATE_INFO, &state)) < 0)
	{
		*s = 0;
		ALOGE(TAG "--ioctl--  err!\n");  
		return -1;
	} 
        */
	
	*s = state == 1 ? 1 : 0;
       
	ALOGE(TAG "state = [%d]\n",state);  
	usleep(5*1000);
	return 0;
}

