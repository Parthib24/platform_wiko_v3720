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

#define bool int
#define false (0)
#define true (1)

#define elan_debug 0

#define Base_Average 4

#define WRITE_REG_HEAD 0x80
//#define ELAN_FP_DEV "/dev/fingerprint"
#define ELAN_FP_DEV "/dev/elan_fp"


#define TAG "[Tinnofingerprint-fingerprint-elan] "
#define ALOGE(...) __android_log_print(ANDROID_LOG_FATAL  , TAG,__VA_ARGS__)

#define ELAN_IOCTLID               0x80
#define IOCTL_RESET	_IOW(ELAN_IOCTLID,  1, int)
#define IOCTL_IRQ_MASK _IOW(ELAN_IOCTLID, 2, int)
#define IOCTL_READ_MODE _IOW(ELAN_IOCTLID, 3, int)
#define IOCTL_WORK_MODE _IOW(ELAN_IOCTLID, 4, int)
#define IOCTL_SET_XY _IOW(ELAN_IOCTLID, 5, int)
#define IOCTL_SET_SCAN_FLAG _IOW(ELAN_IOCTLID, 6, int)
#define IOCTL_POWER_KEY _IOW(ELAN_IOCTLID, 7, int)
#define IOCTL_SPI_CONFIG _IOW(ELAN_IOCTLID, 8, int)
#define IOCTL_DEBUG_LOG_SWITCH _IOW(ELAN_IOCTLID, 9, int)
#define IOCTL_READ_KEY_STATUS    _IOW(ELAN_IOCTLID, 10, int)
#define IOCTL_WRITE_KEY_STATUS _IOR(ELAN_IOCTLID, 11, int)
#define IOCTL_WAKE_UP_SUSPEND _IOW(ELAN_IOCTLID, 12, int)
#define IOCTL_REMALLOC_IMAGE_BUFFER _IOW(ELAN_IOCTLID, 13, int)

#define CHECK_NEGATIVE_CNT 100
#define CHECK_NEGATIVE_LIMIT 800

//for IRQ MODE setting
#define IRQ_MODE 0x0A


#define WRITE_REG_HEAD 0x80
#define READ_REG_HEAD 0xC0

#define ELAN_CHIP_519R

#ifdef ELAN_CHIP_519R
	#define MAX_PIX_X 56
	#define MAX_PIX_Y 192	
#endif

static int elan_fd = 0;

//print message buffer
static char print_buf[4096];


static const int max_width_pix = MAX_PIX_X;
static const int max_heigh_pix = MAX_PIX_Y;
static const int pix_byte = MAX_PIX_X * MAX_PIX_Y;
static const int total_byte = MAX_PIX_X * MAX_PIX_Y * 2;

static uint16_t image_base[MAX_PIX_X * MAX_PIX_Y * 2];
static uint16_t image_raw[MAX_PIX_X * MAX_PIX_Y * 2];
static uint16_t image_alg[MAX_PIX_X * MAX_PIX_Y * 2];


static int fp_exit = 0;
static sem_t wk_sem;
static pthread_t thd_id =0;
static int finger_state = 0;
int adc_mem_back = 0;
int base_mem_back = 0;

//TINNO BEGIN, modified by wenguangyu, for new elan ftm
static sem_t msg_sem;
static bool first_enter = false;
//int g_cnt = 10;
void elan_input_handler(int signum)  
{
	int state;
	finger_state = ioctl(elan_fd, IOCTL_READ_KEY_STATUS, &state);
	ALOGE(  "[elan_ffbm] elan_input_handler:[%d]\n", finger_state);
	//g_cnt = 10;
	//sem_post(&msg_sem);		 
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

#if 0
void *thread_do_detect(void * arg) 
{
      int ret = -1;
      int state;
      while (1)
      {
		sem_wait(&msg_sem);
		finger_state = ioctl(elan_fd, IOCTL_READ_KEY_STATUS, &state);
		ALOGE(  " [elan_ffbm] thread_do_detect#####:[%d]\n", finger_state); 	
		//finger_state = state;
      }
      pthread_exit(NULL);  
      return NULL;
}
#endif

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
       /*
	if(pthread_create(&thd_id, NULL, thread_do_detect, NULL) )
	{
		ALOGE(  "[elan_ffbm]  thd_id thread create failed.!\n");
		return -1;
	}*/
	first_enter = true;  
	fp_exit = 0;
	return ret;
}

int elan_update_finger_state(int *s)
{        
        *s = (finger_state== 1 ? 1 : 0);
        ALOGE(  "[elan_ffbm] elan_update_finger_state:[%d]\n", *s);
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
                finger_state = -1;
		//sem_post(&msg_sem);
		return 0;
	}
	return -1;
}



