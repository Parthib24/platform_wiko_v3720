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
#include <sys/poll.h>
#include <sys/system_properties.h>
#include "fp.h"

/************************************************************************************/
#undef LOG_TAG

#define LOG_TAG "[Tinnofingerprint-goodix_jni] "
#define TAG LOG_TAG

#define DRV_GOODIX 	 "/dev/goodix_fp"

#define  GFX1XM_IOC_MAGIC    'g' 
#define  GFX1XM_IOC_FTM	_IOW(GFX1XM_IOC_MAGIC, 101, int)
#define  GFX1XM_IOC_SET_MODE	_IOW(GFX1XM_IOC_MAGIC, 102, int)


#define CMP_PLF(x) \
    ( strcasecmp(_platform_info,x) == 0 )

#define CMP_PRD(x) \
    ( strcasecmp(_product_info,x) == 0 )
    
/************************************************************************************/
static int _dev_fd = -1;
static int fp_exit = 0;
static sem_t wk_sem;
static char _product_info[512] = {0x00};
static char _platform_info[512] = {0x00};
/************************************************************************************/

/*
void fasync_input_handler(int signum)  
{
	sem_post(&wk_sem);		 
}

void register_fasync_notify(int fd)
{
	int flag;

	LOGD(TAG"[%s]\n", __func__);
	signal(SIGIO, fasync_input_handler);
	flag = fcntl(fd, F_GETFL);
	flag |= FASYNC;
	fcntl(fd, F_SETFL, flag);
	fcntl(fd, F_SETOWN, getpid());
}
*/

int goodix_set_mod(void)
{
	int ret;
	int mode = 0x01;
	
	ret = ioctl(_dev_fd, GFX1XM_IOC_SET_MODE, &mode);	
	if(ret < 0)
	{
		LOGD(TAG "%s:ioctl err!\n", __func__);  
		return -1;
	}

	LOGD(TAG "[%s]: set key mode ok!\n",__func__);  
	return 0;
}

void *thread_do_poll(void * arg) 
{
	struct pollfd pfd;
	int ret = -1;
	pfd.fd = _dev_fd ;
	pfd.events = POLLIN;

	LOGD(TAG "-->[%s], _dev_fd = %d.\n",__func__, _dev_fd);

	do {
		ret = poll(&pfd, 1, -1);
		if (ret ) {
			sem_post(&wk_sem);
		}
	} while (!fp_exit);
	
	pthread_exit(NULL);
	return NULL;
}

int goodix_dev_init(void)
{
    int fd;
    int ret = 0;
    int err;
    pthread_t ntid;
    
    LOGD(TAG "-->[%s]\n",__func__);
    
    __system_property_get("ro.target",_product_info);
    __system_property_get("ro.board.platform", _platform_info);
    
    LOGD(TAG "[%s]--->product_info:[%s]!\n",__func__,_product_info);
    LOGD(TAG "[%s]--->platform_info:[%s]!\n",__func__,_platform_info);
    
    if ((fd = open(DRV_GOODIX, O_RDWR)) < 0) {
    	LOGD(TAG "open:%s err!\n", DRV_GOODIX);  
    	return -1;
    }
    
    _dev_fd = fd;
    fp_exit = 0;
    
    goodix_set_mod();
    err = pthread_create(&ntid, NULL, thread_do_poll, NULL);
    if (err) {
    	LOGD(TAG "[%s]:pthread_create error!!!\n",__func__);
    }

    return ret;
}

int goodix_update_finger_state(int *s) 
{
    int state = 1;
    int ret = 0;
    
    sem_wait(&wk_sem);
    
    if (fp_exit) { 
        LOGD(TAG "[%s]: Exit~\n",__func__);  
        return -1; 
    }
    
    if ((ret = ioctl(_dev_fd, GFX1XM_IOC_FTM, &state)) < 0) {
        LOGE(TAG "--ioctl--  err!, error = %s.\n",strerror(errno));  
        *s = 0;
        return -1;
    }
    
    *s = state;
    return ret;
}

int goodix_dev_uninit(void)
{
	int fd = _dev_fd;
	LOGD(TAG "-->[%s]\n",__func__);  
	
	if (fd > 0) {
		close(fd);
		_dev_fd = -1;
		fp_exit = 1;
		sem_post(&wk_sem);
		return 0;
	}
	
	return -1;
}

