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
#include "cutils/properties.h"
#include "fp.h"

#undef LOG_TAG
#define LOG_TAG "[Tinnobacktouch-bt.c] "
#define TAG LOG_TAG

/*****************************************************************/
//static int fp_vendor = 0; 
/*****************************************************************/

 int bt_dev_init(void) 
{
	int ret = -1;

	LOGD(TAG"[guomingyi][%s]\n", __func__);
        ret = tp_backtouch_dev_init();

/*	getFpInfo();

	switch(fp_vendor) 
	{
		case VENDOR_GOODIX:
			ret = goodix_dev_init();
			break;
		case VENDOR_ELAN:
			ret = elan_dev_init();
			break;
		case VENDOR_SILEAD:
			ret = silead_dev_init();
			break;
		default:
			LOGD("Err:[%s]/n", __func__); 
			break;
	}
*/	
	return ret;
}

 int bt_dev_uninit(void)
{
	int ret = -1;

	LOGD(TAG"[guomingyi][%s]\n", __func__);
        ret = tp_backtouch_dev_uninit();
	
	/*switch(fp_vendor) 
	{
		case VENDOR_GOODIX:
			ret = goodix_dev_uninit();
			break;
		case VENDOR_ELAN:
			ret = elan_dev_uninit();
			break;
		case VENDOR_SILEAD:
			ret = silead_dev_uninit();
			break;
		default:
			LOGD("Err:[%s] what the fuck ? /n", __func__); 
			break;
	}*/
	
	return ret;
}


 int tp_update_finger_state(int *s)
{
	int ret = -1;

       
       ret = bt_update_finger_state(s);
       LOGD(TAG "tp_update_finger_state--------->%d\n",ret);
       /* switch(fp_vendor) 
	{
		case VENDOR_GOODIX:
			ret = goodix_update_finger_state(s);
			break;
		case VENDOR_ELAN:
			ret = elan_update_finger_state(s);
			break;
		case VENDOR_SILEAD:
			ret = silead_update_finger_state(s);
			break;
		default:
			LOGD(" Err:[%s] what the fuck ? /n", __func__); 
			break;
	}*/

	return ret;
}

/*int getFpInfo(void)
{
    char buf[50] = {0};
    int fd = -1;
    int v = UNKNOW;
    int ret = 0;

    LOGD( "%s \n", __func__);

    if(fp_vendor != UNKNOW)
    {
        LOGD( "[%s]:fp_vendor:%d\n", __func__, fp_vendor);
        return fp_vendor;
    }
 
{
    if ((fd = open(FP_DEV_ATTR, O_RDONLY)) > 0) {
        ret = read(fd, buf, sizeof(buf));
        
        if(strcmp(buf, FP_DRV_GOODIX) == 0) {
            LOGD( "match: /drv/%s Ok!\n",FP_DRV_GOODIX);
            v = VENDOR_GOODIX;
        } else if(strcmp(buf, FP_DRV_SILEAD) == 0) {
            LOGD( "match: /drv/%s Ok!\n",FP_DRV_SILEAD);
            v = VENDOR_SILEAD;
        } else if (strcmp(buf, FP_DRV_ELAN) == 0) {
            LOGD( "match: /drv/%s Ok!\n",FP_DRV_ELAN);
            v = VENDOR_ELAN;
        }
        else {
            LOGD( "%s: Not match: any fp dev !\n", buf);
        }
        close(fd);
    }
}            

    fp_vendor = v;
    return v;
}*/

/*****************************************************************/
