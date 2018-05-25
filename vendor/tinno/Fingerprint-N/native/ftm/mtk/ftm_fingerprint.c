/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

#ifdef TINNO_FINGERPRINT_SUPPORT
   
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
#include <math.h>
#include <dlfcn.h>

#include "common.h"
#include "miniui.h"
#include "ftm.h"
#include <cutils/properties.h>



/******************************************************************************
 * MACRO
 *****************************************************************************/
#define TAG "[Tinnofingerprint-fingerprint-main] "
#define mod_to_fingerprint_data(p) (struct fingerprint_data*)((char*)(p) + sizeof(struct ftm_module))

#define FP_DRV_SILEAD  	"silead_fp"
#define FP_DRV_GOODIX 	"goodix_fp"
#define FP_DRV_ELAN  	"elan_fp"
#define FP_DRV_CHIPONE  "chipone_fp"

#define FP_DEV_ATTR	"/sys/devices/platform/fp_drv/fp_drv_info"

#define TIME_OUT_MS   500 // 3s

#if 1//debug
#define LOGD LOGE
#endif
/******************************************************************************
 * FUNCTION
 *****************************************************************************/
extern int g_tee_device_handle;
extern int elan_dev_init(void);
extern int elan_dev_uninit(void);
extern int elan_update_finger_state(int *s);

extern int goodix_dev_init(void);
extern int goodix_dev_uninit(void);
extern int goodix_update_finger_state(int *s);

extern int silead_dev_init(void);
extern int silead_dev_uninit(void);
extern int silead_update_finger_state(int *s);

extern int chipone_dev_init(void);
extern int chipone_dev_uninit(void);
extern int chipone_update_finger_state(int *s);

/******************************************************************************
 * Structure
 *****************************************************************************/
enum {
    ITEM_PASS,
    ITEM_FAIL,   
};
/*---------------------------------------------------------------------------*/
enum {
    UNKNOW = 0,
    VENDOR_SILEAD,
    VENDOR_GOODIX,
    VENDOR_CHIPONE,
    VENDOR_ELAN,   
};

/*---------------------------------------------------------------------------*/
enum{
	 GX_ERR_SUCCESS = 0,
	 GX_ERR_TIMEOUT,
	 GX_ERR_OPEN_DEVICEFAIL,
	 GX_ERR_NEEDTRY,
	 GX_ERR_FAILED,
	 GX_ERR_NO_SUPPORT,
};
/*---------------------------------------------------------------------------*/
enum{
	 RET_OK = 0,
	 RET_FAIL = 1,
};
/*---------------------------------------------------------------------------*/
static char *detect_state[2] = {
"Up",
"Down",
};
static char *result_info[2] = {
"Success",
"Fail.",
};

static char *fp_vendor_info[5] = {
"Unknow",
FP_DRV_SILEAD,
FP_DRV_GOODIX,
FP_DRV_CHIPONE,
FP_DRV_ELAN,
};
/*---------------------------------------------------------------------------*/
static item_t fingerprint_items[] = {
    item(ITEM_PASS,   uistr_pass),
    item(ITEM_FAIL,   uistr_fail),
    item(-1, NULL),
};
/*---------------------------------------------------------------------------*/
struct fingerprint_priv
{
    /*specific data field*/
    int fd;
    int ret;
    int state;
    int vendor;
};
/*---------------------------------------------------------------------------*/
struct fingerprint_data
{
    struct fingerprint_priv fingerprint;

    /*common for each factory mode*/
    char  info[1024];
    //bool  avail;
    bool  exit_thd;

    text_t    title;
    text_t    text;
    text_t    left_btn;
    text_t    center_btn;
    text_t    right_btn;
    
    pthread_t update_thd;
    struct ftm_module *mod;
    //struct textview tv;
    struct itemview *iv;
};

static bool thread_exit = false;
#define BUF_LEN 2

static int fp_vendor_id = -1;

#define MAX_WAIT_TIME 10
static  int pre_init_wait_time = 0;
static int fp_pre_init_complete = 0;

/******************************************************************************
 * Functions 
 *****************************************************************************/
static int fingerprint_init_priv(struct fingerprint_priv *fingerprint)
{
    memset(fingerprint, 0x00, sizeof(*fingerprint));
    fingerprint->fd = -1;
    fingerprint->state = -1;
    fingerprint->vendor = UNKNOW;
    fingerprint->ret = -1;
    return 0;
}
/*---------------------------------------------------------------------------*/
static int fingerprint_detect_id(void)
{
    char buf[128] = {0};
    int fd = -1;
    int id = -1;
    int ret;
    
    if (fp_vendor_id > 0) return fp_vendor_id;
    
    LOGD(TAG "%s - init\n", __FUNCTION__);
    
    if ((fd = open(FP_DEV_ATTR, O_RDONLY)) > 0) {
        ret = read(fd, buf, sizeof(buf));
        LOGD(TAG "read file result:%d,fp_drv: %s\n", ret, buf);
        if (strcmp(buf, FP_DRV_ELAN) == 0) {
            LOGD(TAG "match: /drv/%s Ok!\n",FP_DRV_ELAN);
            id = VENDOR_ELAN;
        }
        else
        if (strcmp(buf, FP_DRV_SILEAD) == 0) {
            LOGD(TAG "match: /drv/%s Ok!\n",FP_DRV_SILEAD);
            id = VENDOR_SILEAD;
        }
        else
        if (strcmp(buf, FP_DRV_GOODIX) == 0) {
            LOGD(TAG "match: /drv/%s Ok!\n",FP_DRV_GOODIX);
            id = VENDOR_GOODIX;
        }
        else
        if (strcmp(buf, FP_DRV_CHIPONE) == 0) {
            LOGD(TAG "match: /drv/%s Ok!\n",FP_DRV_CHIPONE);
            id = VENDOR_CHIPONE;
        }
        else {
            LOGD(TAG "not mach ANY fp drv !\n");
            id = UNKNOW;
        }
        close(fd);
        fp_vendor_id = id;
        return id;
    }
    
    LOGE(TAG "Cannot open: %s\n", FP_DEV_ATTR);
    return -1;
}


static int fingerprint_open(struct fingerprint_priv *fingerprint)
{
    int id = 0;
    int ret;
    
    if ((id = fingerprint_detect_id()) > 0) {
        fingerprint->vendor = id;
        switch(id) {
        case VENDOR_GOODIX:  
            ret = goodix_dev_init();
            break;
        case VENDOR_ELAN:
            ret = elan_dev_init();    
            break;
        case VENDOR_SILEAD:
            ret = silead_dev_init();    
            break;
        case VENDOR_CHIPONE:
            ret = chipone_dev_init();
            break;
        }
        LOGD(TAG "%s:%d\n", __func__, ret);
        return ret;
    }
    LOGE(TAG "%s : failed!\n", __FUNCTION__);
    return -1;
}
/*---------------------------------------------------------------------------*/
static int fingerprint_close(struct fingerprint_priv *fingerprint)
{
    LOGD(TAG "%s \n", __FUNCTION__);
    
    if(VENDOR_GOODIX == fingerprint->vendor) {
        goodix_dev_uninit();
    }
    
    if(VENDOR_ELAN == fingerprint->vendor) {
        elan_dev_uninit();
    }

    if(VENDOR_SILEAD == fingerprint->vendor) {
        silead_dev_uninit();
    }

    if(VENDOR_CHIPONE == fingerprint->vendor) {
        chipone_dev_uninit();
    }
       
    return 0;
}

/*---------------------------------------------------------------------------*/
static int fingerprint_update_info(struct fingerprint_priv *fingerprint)
{
    int ret = 0;
    int state = -1;
    int up = 0;
    int down = 0;
    int v = fingerprint->vendor;
    switch (v) {  
    case VENDOR_GOODIX:
        ret = goodix_update_finger_state(&fingerprint->state);
        break;
    
    case VENDOR_ELAN:  
        ret = elan_update_finger_state(&fingerprint->state);
        break;

    case VENDOR_SILEAD:  
        ret = silead_update_finger_state(&fingerprint->state);
        break;

    case VENDOR_CHIPONE:  
        ret = chipone_update_finger_state(&fingerprint->state);
        break;
    }

    return ret;
}

static void *fingerprint_update_iv_thread(void *priv)
{
    struct fingerprint_data *dat = (struct fingerprint_data *)priv; 
    struct fingerprint_priv *fingerprint = &dat->fingerprint;
    struct itemview *iv = dat->iv;    
    int err = 0, len = 0;
    char *status;
    
    LOGD(TAG "%s: Start--\n", __FUNCTION__);
    
    fingerprint->ret = fingerprint_open(fingerprint);

    do {

    restart:
		
        if ((0 == fp_pre_init_complete) && (pre_init_wait_time <= MAX_WAIT_TIME)) {
            char openhal[PROPERTY_VALUE_MAX] = {0};
            property_get("sys.fingerprintd.openhal", openhal, NULL);
            if(strcmp(openhal, "completed") == 0) {
                fp_pre_init_complete = 1;
                ALOGI("fp_pre_init_complete==1\n");
                goto restart;
            }
            else {
                usleep(1000*1000);
                len = 0;
                len += snprintf(dat->info+len, sizeof(dat->info)-len, "Wait fingerprintd readly.. %d\n\n", ++pre_init_wait_time);  
            }
        }
        else if (fingerprint->ret == RET_OK) {
            if (fingerprint_update_info(fingerprint) < 0) {
                LOGE(TAG "---get fingerprint info err!\n");
                continue;
            }
            
            if (!(fingerprint->vendor >= UNKNOW && fingerprint->vendor <= VENDOR_ELAN)) {
                continue;
            }

            len = 0;
            len += snprintf(dat->info+len, sizeof(dat->info)-len, "Chip info: %s\n\n", fp_vendor_info[fingerprint->vendor]);    		
            len += snprintf(dat->info+len, sizeof(dat->info)-len, "Chip init: %s\n\n", result_info[RET_OK]);  
            len += snprintf(dat->info+len, sizeof(dat->info)-len, "Finger detect: (%d)\n\n", fingerprint->state);  
    if(get_is_ata() == 1)
    {
        if (fingerprint->state == 1)
        {
            dat->mod->test_result = FTM_TEST_PASS;
            dat->exit_thd=true;
        }
    }
        }
        else {
            len = 0;
            len += snprintf(dat->info+len, sizeof(dat->info)-len, "Chip info: %s\n\n", fp_vendor_info[UNKNOW]);  
            len += snprintf(dat->info+len, sizeof(dat->info)-len, "Chip init: %s\n\n", result_info[RET_FAIL]);   
        }

        iv->set_text(iv, &dat->text);
        iv->redraw(iv);
    
    } 
    while(!dat->exit_thd);

    fingerprint_close(fingerprint);
    LOGD(TAG "%s: Exit\n", __FUNCTION__);    
    pthread_exit(NULL);    
    return NULL;
}
/*---------------------------------------------------------------------------*/
static int fingerprint_entry_tinno(struct ftm_param *param, void *priv)
{
    char *ptr;
    int chosen;
    struct fingerprint_data *dat = (struct fingerprint_data *)priv;
    struct textview *tv;
    struct itemview *iv;
    struct statfs stat;
    int err;

    LOGD(TAG "%s\n", __FUNCTION__);

    init_text(&dat->title, param->name, COLOR_YELLOW);
    init_text(&dat->text, &dat->info[0], COLOR_YELLOW);
    init_text(&dat->left_btn, uistr_info_sensor_fail, COLOR_YELLOW);
    init_text(&dat->center_btn, uistr_info_sensor_pass, COLOR_YELLOW);
    init_text(&dat->right_btn, uistr_info_sensor_back, COLOR_YELLOW);
       
    snprintf(dat->info, sizeof(dat->info), uistr_info_sensor_initializing);
    dat->exit_thd = false;  


    if (!dat->iv) {
        iv = ui_new_itemview();
        if (!iv) {
            LOGD(TAG "No memory");
            return -1;
        }
        dat->iv = iv;
    }
    iv = dat->iv;
    iv->set_title(iv, &dat->title);
    iv->set_items(iv, fingerprint_items, 0);
    iv->set_text(iv, &dat->text);
    
    pthread_create(&dat->update_thd, NULL, fingerprint_update_iv_thread, priv);
    if(get_is_ata() == 1)
	{
       pthread_join(dat->update_thd, NULL);

        return 0;
	}
	do {
        chosen = iv->run(iv, &thread_exit);
        switch (chosen) {
        case ITEM_PASS:
        case ITEM_FAIL:
            if (chosen == ITEM_PASS) {
                dat->mod->test_result = FTM_TEST_PASS;
            } else if (chosen == ITEM_FAIL) {
                dat->mod->test_result = FTM_TEST_FAIL;
            }           
            thread_exit = true;          
            break;
        }
        iv->redraw(iv);
        if (thread_exit) {
            dat->exit_thd = true;
            break;
        }        
    } while (1);
    pthread_join(dat->update_thd, NULL);

    return 0;
}
/*---------------------------------------------------------------------------*/
int fingerprint_init_tinno(void)
{
    int ret = 0;
    struct ftm_module *mod;
    struct fingerprint_data *dat;

    LOGD(TAG "%s\n", __FUNCTION__);
    
    mod = ftm_alloc(ITEM_FINGERPRINT_TEST, sizeof(struct fingerprint_data));
    dat  = mod_to_fingerprint_data(mod);

    memset(dat, 0x00, sizeof(*dat));
    fingerprint_init_priv(&dat->fingerprint);  
        
    /*NOTE: the assignment MUST be done, or exception happens when tester press Test Pass/Test Fail*/    
    dat->mod = mod; 
    
    if (!mod)
        return -ENOMEM;

    ret = ftm_register(mod, fingerprint_entry_tinno, (void*)dat);  

    return ret;
}
/*---------------------------------------------------------------------------*/
int fingerprint_daemon_trigger(void)
{
    LOGD("%s: sys.fingerprintd=factory-boot\n", __func__);
    property_set("sys.fingerprintd", "factory-boot");
    return 0;
}


#endif 

