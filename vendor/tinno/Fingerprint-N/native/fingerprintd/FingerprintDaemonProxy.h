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

#ifndef FINGERPRINT_DAEMON_PROXY_H_
#define FINGERPRINT_DAEMON_PROXY_H_

#include "IFingerprintDaemon.h"
#include "IFingerprintDaemonCallback.h"

namespace android {

class FingerprintDaemonProxy : public BnFingerprintDaemon {
    public:
        static FingerprintDaemonProxy* getInstance() {
            if (sInstance == NULL) {
                sInstance = new FingerprintDaemonProxy();
            }
            return sInstance;
        }

        // These reflect binder methods.
        virtual void init(const sp<IFingerprintDaemonCallback>& callback);
        virtual int32_t enroll(const uint8_t* token, ssize_t tokenLength, int32_t groupId, int32_t timeout);
        virtual uint64_t preEnroll();
        virtual int32_t postEnroll();
        virtual int32_t stopEnrollment();
        virtual int32_t authenticate(uint64_t sessionId, uint32_t groupId);
        virtual int32_t stopAuthentication();
        virtual int32_t remove(int32_t fingerId, int32_t groupId);
        virtual uint64_t getAuthenticatorId();
        virtual int32_t setActiveGroup(int32_t groupId, const uint8_t* path, ssize_t pathLen);
        virtual int64_t openHal();
        virtual int32_t closeHal();

        //guomingyi add start.
        virtual void setMsg(int32_t msg);
        virtual int get(int32_t arg);
        //guomingyi add end.
		
    private:
        FingerprintDaemonProxy();
        virtual ~FingerprintDaemonProxy();
        void binderDied(const wp<IBinder>& who);
        void notifyKeystore(const uint8_t *auth_token, const size_t auth_token_length);
        static void hal_notify_callback(const fingerprint_msg_t *msg);

        static FingerprintDaemonProxy* sInstance;
        fingerprint_module_t const* mModule;
        fingerprint_device_t* mDevice;
        sp<IFingerprintDaemonCallback> mCallback;
};

} // namespace android

//guomingyi add start.
/************************************************************/
#include <fcntl.h>
#include <utils/Log.h>
#include <cutils/klog.h>
#include <cutils/properties.h>
#include "external.h"

#undef LOG_TAG
#define LOG_TAG  "[Tinnofingerprint-fingerprintd]"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_FATAL  , LOG_TAG,__VA_ARGS__)

#define KLOGD(...)  KLOG_DEBUG(LOG_TAG, __VA_ARGS__)
#define KLOGE(...)  KLOG_ERROR(LOG_TAG, __VA_ARGS__)


#define FINGERPRINT_MODULE_GOODIX  "fingerprint.goodix"  	/* fingerprint.goodix.default.so */
#define FINGERPRINT_MODULE_ELAN  "fingerprint.elan"    		/* fingerprint.elan.default.so */
#define FINGERPRINT_MODULE_SILEAD  "fingerprint.silead"   	/* fingerprint.silead.default.so */
#define FINGERPRINT_MODULE_CHIPONE  "fingerprint.chipone"   	/* fingerprint.chipone.default.so */

#define FP_DEV_ATTR	  "/sys/devices/platform/fp_drv/fp_drv_info"
#define FP_DRV_SILEAD  	"silead_fp"
#define FP_DRV_GOODIX 	"goodix_fp"
#define FP_DRV_ELAN  	"elan_fp"
#define FP_DRV_CHIPONE	"chipone_fp"
#define FP_DRV_TEE          "tee_fp"



#if (defined(CONFIG_PROJECT_P7201) || defined(CONFIG_PROJECT_I9051) || defined(CONFIG_PROJECT_P7701) )
#define USE_HAL_DYNAMIC_COMPATIBLE 
#endif


enum 
{
    UNKNOW = 0,
    SILEAD,
    GOODIX,
    ELAN,
    TEE,
    CHIPONE,
};
extern int getFpChipName(void);
extern int get_ipo_state(void);

#define  MSG_SCREEN_OFF 	0
#define  MSG_SCREEN_ON 	1
#define  MSG_USER_PRESENT 	2

#define  MSG_FINGERPRINTD_CANCEL 	1001
#define  MSG_FINGERPRINT_ENABLE 	1002
#define  MSG_FINGERPRINT_DISABLE 	1003

#define  MSG_FINGERPRINT_STATE 	    3001

#define SILEAD_CHIP_ID (1633923091)


//ONLY FOR GOODIX.
typedef enum fingerprint_gx_cmd_type {
    FINGERPRINT_GX_CMD_INVALID = -1,
    FINGERPRINT_GX_CMD_FP_ENABLE = 3,
    FINGERPRINT_GX_CMD_FP_DISABLE = 4
} fingerprint_gx_cmd_type_t;


enum 
{
    ACT_CANCEL = 0,
    ACT_ENROLL,
    ACT_AUTHENTICAT,
    ACT_REMOVE, 
};

/************************************************************/
//guomingyi add end.

#endif // FINGERPRINT_DAEMON_PROXY_H_
