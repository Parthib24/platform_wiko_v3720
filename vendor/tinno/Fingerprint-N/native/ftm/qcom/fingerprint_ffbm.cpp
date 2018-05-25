/*
 * Copyright (c) 2014-2015, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
#include <sys/statfs.h>
#include <sys/mount.h>
#include "mmi_module.h"
#include "nv.h"
#include <cutils/properties.h>


#define FP_DRV_SILEAD  	"silead_fp"
#define FP_DRV_GOODIX 	"goodix_fp"
#define FP_DRV_ELAN  	"elan_fp"
#define FP_DRV_CHIPONE  "chipone_fp"

#define FP_DEV_ATTR		"/sys/devices/platform/fp_drv/fp_drv_info"


#define TAG "[Tinnofingerprint-fingerprint-main] "  
#define ALOGE(...) __android_log_print(ANDROID_LOG_FATAL  , TAG, __VA_ARGS__)
#define ALOGD ALOGE
#define ALOGI ALOGE
#define LOGD ALOGE


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


static int fingerprint_open(void);
/*---------------------------------------------------------------------------*/
enum {
    UNKNOW = 0,
    VENDOR_SILEAD,
    VENDOR_GOODIX,
    VENDOR_ELAN,
    VENDOR_CHIPONE,
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
FP_DRV_ELAN,
FP_DRV_CHIPONE,
};
static bool exit_thd;
static int g_fp_vendor = UNKNOW;
static int g_chip_test_flag = -1;

#define MAX_WAIT_TIME 10
static  int pre_init_wait_time = 0;
static int fp_pre_init_complete = 0;

/**
* Defined case run in mmi mode,this mode support UI.
*
*/

/*---------------------------------------------------------------------------*/
static int fingerprint_detect_id(void)
{
	char buf[50] = {0};
	int fd = -1;
	int id = -1;
	int ret;

        if (g_fp_vendor >  0) return g_fp_vendor;

        ALOGD(TAG "%s - init\n", __FUNCTION__);

	if ((fd = open(FP_DEV_ATTR, O_RDONLY)) > 0) {
		ret = read(fd, buf, sizeof(buf));
		ALOGD(TAG "read file result:%d,fp_drv: %s\n", ret, buf);

		if (strcmp(buf, FP_DRV_ELAN) == 0) {
			ALOGD(TAG "match: /drv/%s Ok!\n",FP_DRV_ELAN);
			id = VENDOR_ELAN;
		}
		else
		if (strcmp(buf, FP_DRV_SILEAD) == 0) {
			ALOGD(TAG "match: /drv/%s Ok!\n",FP_DRV_SILEAD);
			id = VENDOR_SILEAD;
		}
		else
		if (strcmp(buf, FP_DRV_GOODIX) == 0) {
			ALOGD(TAG "match: /drv/%s Ok!\n",FP_DRV_GOODIX);
			id = VENDOR_GOODIX;
		}
		else
		if (strcmp(buf, FP_DRV_CHIPONE) == 0) {
			ALOGD(TAG "match: /drv/%s Ok!\n",FP_DRV_CHIPONE);
			id = VENDOR_CHIPONE;
		 }
		else {
			ALOGD(TAG "not mach ANY fp drv !\n");
			id = UNKNOW;
		}
		
		close(fd);
		g_fp_vendor = id;
		return id;
	}
	
	LOGE(TAG "Cannot open: %s\n", FP_DEV_ATTR);
	return -1;
}


static int fingerprint_update_info(char *buf, int size)
{
	char buftemp[128] = { 0x00 };
	int ret = 0;
	int state = 0;

restart:
    memset(buftemp, 0, sizeof(buftemp));
    ALOGI("fp_pre_init_complete -> %d;pre_init_wait_time -> ",fp_pre_init_complete,pre_init_wait_time);
	
    if ((0 == fp_pre_init_complete) && (pre_init_wait_time <= MAX_WAIT_TIME)) {
        char openhal[PROPERTY_VALUE_MAX] = {0};
        property_get("sys.fingerprintd.openhal", openhal, NULL);
        if(strcmp(openhal, "completed") == 0) {
            fp_pre_init_complete = 1;
            g_chip_test_flag = fingerprint_open();
	     ALOGI("g_chip_test_flag : %d\n", g_chip_test_flag);
            goto restart;
        }
        else {
            usleep(1000*1000);
            snprintf(buftemp, sizeof(buftemp), "Wait fingerprintd readly.. %d\n", ++pre_init_wait_time);
            strlcat(buf, buftemp, size);
	     ALOGI("%s\n", buf);
        }
    }
    else if (0 == g_chip_test_flag) {
        switch (g_fp_vendor) {  
            case VENDOR_GOODIX:
                ret = goodix_update_finger_state(&state);
                break;
            case VENDOR_ELAN:  
                ret = elan_update_finger_state(&state);
                break;
            case VENDOR_SILEAD:  
                ret = silead_update_finger_state(&state);
                break;
            case VENDOR_CHIPONE:
                ret = chipone_update_finger_state(&state);
                break;
        }
        snprintf(buftemp, sizeof(buftemp), "Chip info: %s\n", fp_vendor_info[g_fp_vendor]);
        strlcat(buf, buftemp, size);
        snprintf(buftemp, sizeof(buftemp), "Chip init: %s\n", result_info[RET_OK]);
        strlcat(buf, buftemp, size);
        snprintf(buftemp, sizeof(buftemp), "Finger detect: (%d)", state);
        strlcat(buf, buftemp, size);
    }
    else {
        snprintf(buftemp, sizeof(buftemp), "Chip info: %s\n", fp_vendor_info[UNKNOW]);
        strlcat(buf, buftemp, size);
        snprintf(buftemp, sizeof(buftemp), "Chip init: %s\n", result_info[RET_FAIL]);
        strlcat(buf, buftemp, size);
    }
		
    return ret;
}

static int fingerprint_open(void)
{
    int ret = -1;
    int vendor = 0;

    if ((vendor = fingerprint_detect_id()) > 0) {
	 switch (vendor) {
            case VENDOR_ELAN:
                ret = elan_dev_init();
	         break;
            case VENDOR_GOODIX:
                ret = goodix_dev_init();
	         break;
            case VENDOR_SILEAD:
                ret = silead_dev_init();
	         break;
            case VENDOR_CHIPONE:
                ret = chipone_dev_init();
	         break;
	 }
	 ALOGI("%s : %d\n", __func__, ret);
        return ret;
    }
    
    ALOGE(  "Cannot open: %s\n", FP_DEV_ATTR);
    return -1;
}

static int32_t module_init(const mmi_module_t * module, unordered_map < string, string > &params) {
    ALOGI("%s  start ", __FUNCTION__);

    if(module == NULL) {
        ALOGE("%s  NULL point  received ", __FUNCTION__);
        return FAILED;
    }

    return SUCCESS;
}

static int32_t module_deinit(const mmi_module_t * module) {
    ALOGI("%s start.", __FUNCTION__);
    if(module == NULL) {
        ALOGE("%s NULL point  received ", __FUNCTION__);
        return FAILED;
    }
    return SUCCESS;
}

static int32_t module_stop(const mmi_module_t * module) {
    ALOGI("%s start.", __FUNCTION__);
    if(module == NULL) {
        ALOGE("%s NULL point  received ", __FUNCTION__);
        return FAILED;
    }

    pthread_kill(module->run_pid, SIGUSR1);
    
    exit_thd = true;
    pthread_join(module->run_pid, NULL);
    
    return SUCCESS;
}

static void *fingerprint_update_iv_thread(void *mod) {
    char buf[512] = { 0 };

    mmi_module_t *module = (mmi_module_t *) mod;

    ALOGE(  "entry: %s\n", __func__);
	
    if(module == NULL || module->cb_print == NULL) {
        ALOGE("%s NULL for cb function ", __FUNCTION__);
        return NULL;
    }
	
    signal(SIGUSR1, signal_handler);

    while (!exit_thd) {
        memset(buf, 0x00, sizeof(buf));
        if (fingerprint_update_info(buf, sizeof(buf)) < 0) {
            ALOGE(  "---get fingerprint info err!\n");
            continue;
        }
        
        module->cb_print(NULL, SUBCMD_MMI, buf, strlen(buf), PRINT_DATA);
        //usleep(100*1000);
    }

    return NULL;
}


/**
* Before call Run function, caller should call module_init first to initialize the module.
* the "cmd" passd in MUST be defined in cmd_list ,mmi_agent will validate the cmd before run.
*
*/
static int32_t module_run(const mmi_module_t * module, const char *cmd, unordered_map < string, string > &params) {
    ALOGI("%s start:%s", __FUNCTION__);
    int ret = FAILED;

    exit_thd = false;
    
    if(module == NULL) {
        ALOGE("%s NULL point  received ", __FUNCTION__);
        return FAILED;
    }

    ret = pthread_create((pthread_t *) & module->run_pid, NULL, fingerprint_update_iv_thread, (void *) module);
    if(ret < 0) {
        ALOGE("%s:Can't create pthread: %s\n", __FUNCTION__, strerror(errno));
        return FAILED;
    } else {
        pthread_join(module->run_pid, NULL);
    }
    return SUCCESS;
}



/**
* Methods must be implemented by module.
*/
static struct mmi_module_methods_t module_methods = {
    .module_init = module_init,
    .module_deinit = module_deinit,
    .module_run = module_run,
    .module_stop = module_stop,
};

int fingerprint_daemon_start(void) {
    property_set("sys.fingerprintd", "factory-boot");	
    ALOGI("%s: sys.fingerprintd=factory-boot\n", __func__);
    return 0;
}

/**
* Every mmi module must have a data structure named MMI_MODULE_INFO_SYM
* and the fields of this data structure must be initialize in strictly sequence as definition,
* please don't change the sequence as g++ not supported in CPP file.
*/
mmi_module_t MMI_MODULE_INFO_SYM = {
    .version_major = 1,
    .version_minor = 0,
    .name = "Fingerprint",
    .author = "Qualcomm Technologies, Inc.",
    .methods = &module_methods,
    .module_handle = NULL,
    .supported_cmd_list = NULL,
    .supported_cmd_list_size = 0,
    .cb_print = NULL, /**it is initialized by mmi agent*/
    .run_pid = -1,
};
