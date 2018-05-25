/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//#define LOG_TAG "fingerprintd"

#include <cutils/log.h>
#include <utils/Log.h>

#include <binder/IPCThreadState.h>
#include <binder/IServiceManager.h>
#include <binder/PermissionCache.h>
#include <utils/String16.h>

#include <keystore/IKeystoreService.h>
#include <keystore/keystore.h> // for error codes

#include <hardware/hardware.h>
//#include <hardware/fingerprint.h>
#include "fingerprint.h"
#include <hardware/hw_auth_token.h>
#include "FingerprintDaemonProxy.h"



static android::sp<android::FingerprintDaemonProxy> proxy = NULL;
static int chip_name = -1;

static void ftm_open_hal(void);
static int init_compatible(void);
static int remove_old_fpdata_from_androidM(void);
static int is_mini_framework(void);
static void startFpTZLogOutputIfNecessary();

int main(int argc, char *argv[]) {

    ALOGD("[%s:%s,%d] Starting..\n",  __func__, argv[0], argc);
    android::sp<android::IServiceManager> serviceManager = android::defaultServiceManager();
    android::status_t ret;

    proxy = android::FingerprintDaemonProxy::getInstance();
    ret = serviceManager->addService(android::FingerprintDaemonProxy::descriptor, proxy);

    if (ret != android::OK) {
        ALOGE("Couldn't register binder service!");
        return -1;
    }

    ALOGD("---init_compatible..222\n");
    init_compatible();
#ifndef MTK_TARGET_PROJECT	
    startFpTZLogOutputIfNecessary();
#endif	
    //Remove fpdata for first boot solve ota bug.add by yinglong.tang.
    #if (defined(CONFIG_PROJECT_V3991) || defined(CONFIG_PROJECT_P7201) || defined(CONFIG_PROJECT_P7701))
        remove_old_fpdata_from_androidM();
    #endif

    /*
     * We're the only thread in existence, so we're just going to process
     * Binder transaction as a single-threaded program.
     */
    android::IPCThreadState::self()->joinThreadPool();
    ALOGD("fingerprintd start  Done!");
    return 0;
}

int tryToGetChipIdFormTz(void)
{
    int ret = -1;

#ifdef FINGERPRINT_SUPPORT_CHIPONE
    if ((ret = checkFpSensor()) == 0){
        chip_name = CHIPONE;
        return 0;
    }
    ALOGD("%s: not chipone sensor\n",__func__);
#endif

#ifdef FINGERPRINT_SUPPORT_SILEAD
    if (check_silead_sensor() == 0) {
        chip_name = SILEAD;
        return 0;
    }
    ALOGD("%s: not silead sensor\n",__func__);
#endif

    return ret;
}



int getFpChipName(void)
{
    char buf[50] = {0};
    int fd;
    int ret;
    int j = -1;

    if (chip_name > 0) {
        return chip_name;
    }

    if ((fd = open(FP_DEV_ATTR, O_RDONLY)) > 0) {
        ret = read(fd, buf, sizeof(buf));
        ALOGD( "read file result:%d,fp_drv: %s\n", ret, buf);

        if (strcmp(buf, FP_DRV_ELAN) == 0) {
            ALOGD( "match: /drv/%s Ok!\n",FP_DRV_ELAN);
            j = ELAN;
        }
        else
        if (strcmp(buf, FP_DRV_SILEAD) == 0) {
            ALOGD( "match: /drv/%s Ok!\n",FP_DRV_SILEAD);
            j = SILEAD;
        }
        else
        if (strcmp(buf, FP_DRV_GOODIX) == 0) {
            ALOGD( "match: /drv/%s Ok!\n",FP_DRV_GOODIX);
            j = GOODIX;
        }
        else 
        if (strcmp(buf, FP_DRV_CHIPONE) == 0) {
            ALOGD( "match: /drv/%s Ok!\n",FP_DRV_CHIPONE);
            j = CHIPONE;
        }
        else {
            ALOGE( "not mach ANY fp drv !\n");
        }

        close(fd);
        chip_name = j;
        return j;
    }

    ALOGE( "Cannot open: %s\n", FP_DEV_ATTR);
    return -1;
}

#ifdef MTK_TARGET_PROJECT
int get_ipo_state(void)
{
    char value[PROPERTY_VALUE_MAX];

    memset(value, 0, sizeof(value));
    property_get("sys.ipo.shutdown", value, "0");
    return (strcmp(value, "1") == 0);
}
#endif

static void *monitor_thread_callback(void *args)
{
    #define MAX_LOOP_COUNT (60*2)
    int loop_count = 0;
    char boot_type[ PROPERTY_VALUE_MAX] = {0};

    // Wait 5 seconds..
    usleep(1000*1000*5);

    do {
        property_get("sys.fingerprintd", boot_type, NULL);
        if(strcmp(boot_type, "normal-boot") == 0) {
            break;
        }
        else if(strcmp(boot_type, "factory-boot") == 0) {
            ftm_open_hal();
            break;
        }
        else {
            ALOGD("%s: wait for boot type :%d\n", __func__, loop_count);
            usleep(1000*1000);
        }
    } while(++loop_count < MAX_LOOP_COUNT);

    pthread_exit(NULL);
    return NULL;
}

static void ftm_open_hal(void) {
    if (proxy != NULL) {
        usleep(1000*1000*5);
        proxy->openHal();
        property_set("sys.fingerprintd.openhal", "completed");
    }
    else {
        ALOGE("factory-boot: err--- proxy is NULL!..\n");
    }
}

//add by wenguangyu, for tz log output, start
#define  TZ_LOG_DEV "/dev/fp_drv"
#define  TZ_LOG_IOC_MAGIC         't'
#define  TZ_LOG_IOC_DISABLE_OUTPUT	_IO(TZ_LOG_IOC_MAGIC, 1)
#define  TZ_LOG_IOC_ENABLE_OUTPUT	_IO(TZ_LOG_IOC_MAGIC, 2)
static int fp_tz_log_fd;
int fp_tz_log_state;

int fp_tz_log_output_init(void) 
{
    int ret = 0;
    
    fp_tz_log_fd = open(TZ_LOG_DEV, O_RDONLY);
    ALOGD("fp_tz_log_fd = %d\r\n", fp_tz_log_fd);	    
    if (fp_tz_log_fd < 0) {
        ALOGE(" open %s failed!", TZ_LOG_DEV);	
        return -1;
    }
    else {
        ALOGE("open %s success \r\n", TZ_LOG_DEV);
    	return 1;
    }    
}

int fp_tz_log_output_enable()  
{
    ALOGD("tz_log_output_enable \r\n");			 
    int init_result; 	
    init_result = fp_tz_log_output_init();
    if (init_result > 0) {
        fp_tz_log_state = ioctl(fp_tz_log_fd, TZ_LOG_IOC_ENABLE_OUTPUT, 0); 
        ALOGD("ioctl fp_tz_log_state = %d!!! \r\n", fp_tz_log_state);
        return 1;			    	      
    } else {
        ALOGE("tz_log_output_enable fail !!! \r\n");			    	
    }
    return 0;	 	
}
static void startFpTZLogOutputIfNecessary(){
       char tz_log_prop_value[16];
	property_get("persist.sys.fp.tzlog", tz_log_prop_value, "0");
       if (strcmp(tz_log_prop_value, "1") == 0) {
           fp_tz_log_output_enable();
	}
}
//add by wenguangyu, for tz log output, end

static int init_compatible(void) {
    int vendor = -1;

#if defined(USE_HAL_DYNAMIC_COMPATIBLE)
    if (is_mini_framework() != 1) {
        ALOGE("init_compatible: set sys.fp.goodix 1.");
        property_set("persist.sys.fp.goodix", "1"); //for history issue.
        property_set("sys.fp.goodix", "1");
    }
#else
    tryToGetChipIdFormTz();
    vendor = getFpChipName();
    if (vendor == SILEAD) {
        property_set("sys.fp.silead", "1");
    }
    else if(vendor == GOODIX) {
        property_set("sys.fp.goodix", "1");
    }
#endif /*USE_HAL_DYNAMIC_COMPATIBLE*/

    pthread_t tid = 0;
    if (pthread_create(&tid, NULL, monitor_thread_callback, NULL)) {
        ALOGI("[%s]:pthread_create failed!\n",__func__);
    }

    return 0;
}

static int remove_old_fpdata_from_androidM(void) {
    char value[PROPERTY_VALUE_MAX];

    memset(value, 0, sizeof(value));
    property_get("persist.sys.fpdata.removed", value, "unknow");
    //ALOGE( "persist.sys.fpdata.removed: %s\n", value);
    if (strcmp(value, "unknow") == 0) {
        property_set("sys.fingerprint.data.remove", "1");//trigger remove fpdata action.
        property_set("persist.sys.fpdata.removed", "1"); //flag for has removed.
    } 

    return 0;
}

static int is_mini_framework(void) {
    char value[PROPERTY_VALUE_MAX];

    memset(value, 0, sizeof(value));
    property_get("vold.decrypt", value, "0");
    ALOGE("is_mini_framework: value =  %s.", value);
    if (strcmp(value, "trigger_restart_min_framework") == 0) {
        return 1;
    }

    return 0;
}






