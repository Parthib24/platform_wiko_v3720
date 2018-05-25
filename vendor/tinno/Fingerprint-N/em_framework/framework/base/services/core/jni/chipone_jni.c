/*
 **
 ** Copyright 2008, The Android Open Source Project
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
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

#include "fp.h"

/************************************************************************************/
#undef LOG_TAG
#define LOG_TAG "[Tinnofingerprint-chipone] "
#define TAG LOG_TAG

#define DRV_CHIPONE 	 "/dev/chipone_fp"

#define FPSENSOR_IOC_MAGIC    0xf0    //CHIP

#define FPSENSOR_IOC_FTM_SET_FINGER_STATE       _IOWR(FPSENSOR_IOC_MAGIC,15,unsigned int)
#define FPSENSOR_IOC_FTM_GET_FINGER_STATE       _IOWR(FPSENSOR_IOC_MAGIC,16,unsigned int)
#define IOCTL_FINGER_STATE_INFO	                FPSENSOR_IOC_FTM_GET_FINGER_STATE

/************************************************************************************/

static int _dev_fd = -1;
static int fp_exit;
static sem_t wk_sem;
/************************************************************************************/
int chipone_dev_init(void);
int chipone_dev_uninit(void);
int chipone_update_finger_state(int *s);

/************************************************************************************/
static void fasync_input_handler(int signum)  
{
	LOGD(TAG "[%s] : %d \n", __func__, signum);
	sem_post(&wk_sem);		 
}

static void register_fasync_notify(int fd)
{
	int flag; 

	LOGD(TAG"[%s]\n", __func__);
	signal(SIGIO, fasync_input_handler);
	flag = fcntl(fd, F_GETFL);
	flag |= FASYNC;
	fcntl(fd, F_SETFL, flag);
	fcntl(fd, F_SETOWN, getpid());
} 

int chipone_dev_init(void)
{
	int fd;

	LOGD(TAG "-->[%s]\n",__func__);  

	if((fd = open(DRV_CHIPONE, O_RDWR)) < 0)
	{
		LOGD(TAG "open:%s err!\n", DRV_CHIPONE);  
		return -1;
	}

	_dev_fd = fd;  
	fp_exit = 0;
	
	register_fasync_notify(fd);
	return 0;
}

int chipone_dev_uninit(void)
{
	int fd = _dev_fd;
	LOGD(TAG "-->[%s]\n",__func__);  
	
	if(fd > 0)
	{
		close(fd);
		_dev_fd = -1;
		fp_exit = 1;
		sem_post(&wk_sem);
		return 0;
	}
	
	return -1;
}

int chipone_update_finger_state(int *s) 
{
	int state = 0;
	int ret = 0;

	sem_wait(&wk_sem);

	if(fp_exit) 
	{ 
		LOGD(TAG "[%s]: Exit~\n",__func__);  
		return -1; 
	}
	
	if((ret = ioctl(_dev_fd, IOCTL_FINGER_STATE_INFO, &state)) < 0)
	{
		*s = 0;
		LOGD(TAG "--ioctl--  err!  ret  = [%d]\n",ret);  
		return -1;
	}
	
	*s = state == 1 ? 1 : 2;

	LOGD(TAG "state = [%d]\n",state);  

	return 0;
}

