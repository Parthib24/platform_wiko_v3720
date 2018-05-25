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
#define LOG_TAG "[Tinno-backtouch_jni] "
#define TAG LOG_TAG

#define DRV_BACKTOUCH 	 "/dev/touch"
#define  GFX1XM_IOC_MAGIC    'A'
#define  GFX1XM_IOC_FTM	_IOW(GFX1XM_IOC_MAGIC, 101, int)
#define  GFX1XM_IOC_SET_MODE	_IOW(GFX1XM_IOC_MAGIC, 102, int)


/************************************************************************************/
static int _dev_fd = -1;
static int fp_exit;
static sem_t wk_sem;
/************************************************************************************/

void *thread_do_bk_poll(void * arg) 
{
	struct pollfd pfd;
	int ret = -1;
	pfd.fd = _dev_fd ;
	pfd.events = POLLIN;

	LOGD(TAG "-->[%s], _dev_fd = %d.\n",__func__, _dev_fd);

	while (1)
	{
		ret = poll(&pfd, 1, -1);
		LOGD(TAG " ret = %d.\n", ret);
		if (ret == 0)
		{
			LOGD(TAG "-->[%s],   ret == 0.\n",__func__);
		}
		else
		{
			sem_post(&wk_sem);
		}
	}
	pthread_exit(NULL);
	return NULL;
}

int tp_backtouch_dev_init(void)
{
	int fd;
	int err;
	pthread_t ntid;

	LOGD(TAG "-->[%s]\n",__func__);

	if((fd = open(DRV_BACKTOUCH, O_RDWR)) < 0)
	{
		LOGD(TAG "open:%s err!\n", DRV_BACKTOUCH);  
		return -1;
	}

	_dev_fd = fd;
	fp_exit = 0;

	err = pthread_create(&ntid, NULL, thread_do_bk_poll, NULL);
	if (err != 0) {
		LOGD(TAG "[%s]:pthread_create error!!!\n",__func__);
	}

	return 0;
}

int bt_update_finger_state(int *s) 
{
	int state = 1;
	int ret = 0;

	sem_wait(&wk_sem);

	if((ret = ioctl(_dev_fd, GFX1XM_IOC_FTM, &state)) < 0)
	{
		*s = 0;
		LOGD(TAG "--ioctl--  err!, error = %s.\n",strerror(errno));  
		return -1;
	}

	LOGD(TAG " state = %d.\n", state);

	if(state != 0) {
		*s = state;
	}
	else 
	{
		*s = 0;
	}

	return 0;
}

int tp_backtouch_dev_uninit(void)
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

/*****************************************************************/
