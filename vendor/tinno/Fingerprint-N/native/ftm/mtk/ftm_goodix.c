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
#include <sys/mount.h>

#include <sys/poll.h>


/************************************************************************************/
#define TAG "[Tinnofingerprint-fingerprint-goodix] "
#define ALOGE(...) __android_log_print(ANDROID_LOG_FATAL  , TAG,__VA_ARGS__)

#define DRV_GOODIX 	 "/dev/goodix_fp"


/**********************IO Magic**********************/
#define  GF_IOC_MAGIC    'g'  //define magic number
/*read/write GF registers*/
#define  GF_IOC_CMD	_IOWR(GF_IOC_MAGIC, 1, struct gf_ioc_transfer)
#define  GF_IOC_REINIT	_IO(GF_IOC_MAGIC, 0)
#define GF_IOC_ENABLE_IRQ		_IO(GF_IOC_MAGIC, 3)
#define GF_IOC_DISABLE_IRQ		_IO(GF_IOC_MAGIC, 4)
#define  GF_IOC_SENDKEY  _IOW(GF_IOC_MAGIC, 7, struct gf_key)
//LINE<JIRA_ID><DATE20160319><merge from P6601>zenghaihui            
//guomingyi add start.
#define  GFX1XM_IOC_FTM	_IOW(GF_IOC_MAGIC, 101, int)
#define  GFX1XM_IOC_SET_MODE	_IOW(GF_IOC_MAGIC, 102, int)
//guomingyi add end.
//#define  GF_IOC_MAXNR    10
/************************************************************************************/

static int _dev_fd = -1;
static int fp_exit;
static sem_t wk_sem;
static pthread_t thd_id =0;
static int finger_state = 0;
/************************************************************************************/

void poll_thread_exit_handler(int signum)  
{
    ALOGE(TAG "-->[%s]======.\n",__func__);  
    pthread_exit(NULL);	 
}

void *thread_do_poll(void * arg) 
{
    struct pollfd pfd;
    int ret = -1;
    pfd.fd = _dev_fd ;
    int state = 0; 
    pfd.events = POLLIN;
    ALOGE(TAG "-->[%s], _dev_fd = %d.\n",__func__, _dev_fd);
    signal(SIGQUIT, poll_thread_exit_handler);

    while (1) { 
        ret = poll(&pfd, 1, -1);
        //ALOGE(TAG "-->[%s], ret = %d.\n",__func__, ret);  
        if (ret == 0) {
            ALOGE(TAG "-->[%s],   ret == 0.\n",__func__); 
        } else {    
            ALOGE(TAG "-->[%s] else =========.\n",__func__);
            if((ret = ioctl(_dev_fd, GFX1XM_IOC_FTM, &state)) < 0) {
                finger_state = 0;
                ALOGE(  "--ioctl--  err!\n");  
                continue;
            }
		
            finger_state = state;
            ALOGE(  "finger_state:[%d]\n", state);  
        }
    }

    ALOGE(  "thread_do_poll pthread exit!\n");  
    pthread_exit(NULL);  
    return NULL;
}

int goodix_dev_init(void)
{
	int fd;
	int ret;

	ALOGE(  "-->[%s]\n",__func__);  

	if((fd = open(DRV_GOODIX, O_RDWR)) < 0)
	{
		ALOGE(  "open:%s err!\n", DRV_GOODIX);  
		return -1;
	}

	if(pthread_create(&thd_id, NULL, thread_do_poll, NULL) )
	{
		ALOGE(  " thd_id thread create failed.!\n");
		return -1;
	}
      _dev_fd = fd; 
	  
	//add for goodix enable irq by yinglong.tang
	if((ret = ioctl(_dev_fd, GF_IOC_ENABLE_IRQ, 0)) < 0)
	{
            ALOGE(  "--ioctl-GF_IOC_ENABLE_IRQ-  err!\n");  
       }

      fp_exit = 0;
	
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
	int kill_ret;
	ALOGE(  "-->[%s]\n",__func__);  

	//add for thread_do_poll exit by yinglong.tang
	kill_ret = pthread_kill(thd_id,SIGQUIT );
	   
	if(fd > 0)
	{
		close(fd);
		_dev_fd = -1;
		fp_exit = 1;
                finger_state = -1;
		return 0;
	}
	
	return -1;
}

