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

#include <binder/IServiceManager.h>
#include <hardware/hardware.h>
//#include <hardware/fingerprint.h>
#include "fingerprint.h"
#include <hardware/hw_auth_token.h>
#include <keystore/IKeystoreService.h>
#include <keystore/keystore.h> // for error codes
#include <utils/Log.h>

#include "FingerprintDaemonProxy.h"

namespace android {

FingerprintDaemonProxy* FingerprintDaemonProxy::sInstance = NULL;

// Supported fingerprint HAL version
static const uint16_t kVersion = HARDWARE_MODULE_API_VERSION(2, 0);

static int fingerprintd_state = 0;

FingerprintDaemonProxy::FingerprintDaemonProxy() : mModule(NULL), mDevice(NULL), mCallback(NULL) {

}

FingerprintDaemonProxy::~FingerprintDaemonProxy() {
    closeHal();
}

void FingerprintDaemonProxy::hal_notify_callback(const fingerprint_msg_t *msg) {
    FingerprintDaemonProxy* instance = FingerprintDaemonProxy::getInstance();
    const sp<IFingerprintDaemonCallback> callback = instance->mCallback;
    if (callback == NULL) {
        ALOGE("Invalid callback object");
        return;
    }

#ifdef MTK_TARGET_PROJECT
    if(get_ipo_state())  {
        ALOGD("is ipo state, so return!\n");
        return;
    }
#endif
	
    const int64_t device = (int64_t) instance->mDevice;
    switch (msg->type) {
        case FINGERPRINT_ERROR:
            ALOGD("onError(%d)", msg->data.error);
            callback->onError(device, msg->data.error);
            break;
        case FINGERPRINT_ACQUIRED:
            ALOGD("onAcquired(%d)", msg->data.acquired.acquired_info);
	    //if(msg->data.acquired.acquired_info != 3)//remove dirty msg
            	callback->onAcquired(device, msg->data.acquired.acquired_info);
            break;
        case FINGERPRINT_AUTHENTICATED:
            ALOGD("onAuthenticated(fid=%d, gid=%d)",
                    msg->data.authenticated.finger.fid,
                    msg->data.authenticated.finger.gid);
            if (msg->data.authenticated.finger.fid != 0) {
                const uint8_t* hat = reinterpret_cast<const uint8_t *>(&msg->data.authenticated.hat);
                instance->notifyKeystore(hat, sizeof(msg->data.authenticated.hat));
            }
            callback->onAuthenticated(device,
                    msg->data.authenticated.finger.fid,
                    msg->data.authenticated.finger.gid);
            break;
        case FINGERPRINT_TEMPLATE_ENROLLING:
            ALOGD("onEnrollResult(fid=%d, gid=%d, rem=%d)",
                    msg->data.enroll.finger.fid,
                    msg->data.enroll.finger.gid,
                    msg->data.enroll.samples_remaining);
            callback->onEnrollResult(device,
                    msg->data.enroll.finger.fid,
                    msg->data.enroll.finger.gid,
                    msg->data.enroll.samples_remaining);
            break;
        case FINGERPRINT_TEMPLATE_REMOVED:
            ALOGD("onRemove(fid=%d, gid=%d)",
                    msg->data.removed.finger.fid,
                    msg->data.removed.finger.gid);
            callback->onRemoved(device,
                    msg->data.removed.finger.fid,
                    msg->data.removed.finger.gid);
            break;
        default:
            ALOGE("invalid msg type: %d", msg->type);
            return;
    }
}

void FingerprintDaemonProxy::notifyKeystore(const uint8_t *auth_token, const size_t auth_token_length) {
    if (auth_token != NULL && auth_token_length > 0) {
        // TODO: cache service?
        sp < IServiceManager > sm = defaultServiceManager();
        sp < IBinder > binder = sm->getService(String16("android.security.keystore"));
        sp < IKeystoreService > service = interface_cast < IKeystoreService > (binder);
        if (service != NULL) {
            status_t ret = service->addAuthToken(auth_token, auth_token_length);
            if (ret != ResponseCode::NO_ERROR) {
                ALOGE("Falure sending auth token to KeyStore: %d", ret);
            }
        } else {
            ALOGE("Unable to communicate with KeyStore");
        }
    }
}

void FingerprintDaemonProxy::init(const sp<IFingerprintDaemonCallback>& callback) {
    if (mCallback != NULL && IInterface::asBinder(callback) != IInterface::asBinder(mCallback)) {
        IInterface::asBinder(mCallback)->unlinkToDeath(this);
    }
    IInterface::asBinder(callback)->linkToDeath(this);
    mCallback = callback;
}

int32_t FingerprintDaemonProxy::enroll(const uint8_t* token, ssize_t tokenSize, int32_t groupId,
        int32_t timeout) {
    ALOG(LOG_VERBOSE, LOG_TAG, "enroll(gid=%d, timeout=%d)\n", groupId, timeout);
    if (tokenSize != sizeof(hw_auth_token_t) ) {
        ALOG(LOG_VERBOSE, LOG_TAG, "enroll() : invalid token size %zu\n", tokenSize);
        return -1;
    }
    const hw_auth_token_t* authToken = reinterpret_cast<const hw_auth_token_t*>(token);
    fingerprintd_state = ACT_ENROLL;
    return mDevice->enroll(mDevice, authToken, groupId, timeout);
}

uint64_t FingerprintDaemonProxy::preEnroll() {
    return mDevice->pre_enroll(mDevice);
}

int32_t FingerprintDaemonProxy::postEnroll() {
    return mDevice->post_enroll(mDevice);
}

int32_t FingerprintDaemonProxy::stopEnrollment() {
    ALOG(LOG_VERBOSE, LOG_TAG, "stopEnrollment()\n");
    fingerprintd_state = ACT_CANCEL;
    return mDevice->cancel(mDevice);
}

int32_t FingerprintDaemonProxy::authenticate(uint64_t sessionId, uint32_t groupId) {
    ALOG(LOG_VERBOSE, LOG_TAG, "authenticate(sid=%" PRId64 ", gid=%d)\n", sessionId, groupId);
    fingerprintd_state = ACT_AUTHENTICAT;
    return mDevice->authenticate(mDevice, sessionId, groupId);
}

int32_t FingerprintDaemonProxy::stopAuthentication() {
    ALOG(LOG_VERBOSE, LOG_TAG, "stopAuthentication()\n");
    fingerprintd_state = ACT_CANCEL;
    return mDevice->cancel(mDevice);
}

int32_t FingerprintDaemonProxy::remove(int32_t fingerId, int32_t groupId) {
    ALOG(LOG_VERBOSE, LOG_TAG, "remove(fid=%d, gid=%d)\n", fingerId, groupId);
    if(fingerId == 0){  
      ALOGE("lockmethod is changed can't delete fingerprints!");
	return -1;
    }
    fingerprintd_state = ACT_REMOVE;
    return mDevice->remove(mDevice, groupId, fingerId);
}

uint64_t FingerprintDaemonProxy::getAuthenticatorId() {
    return mDevice->get_authenticator_id(mDevice);
}

int32_t FingerprintDaemonProxy::setActiveGroup(int32_t groupId, const uint8_t* path,
        ssize_t pathlen) {
    if (pathlen >= PATH_MAX || pathlen <= 0) {
        ALOGE("Bad path length: %zd", pathlen);
        return -1;
    }
    // Convert to null-terminated string
    char path_name[PATH_MAX];
    memcpy(path_name, path, pathlen);
    path_name[pathlen] = '\0';
    ALOG(LOG_VERBOSE, LOG_TAG, "setActiveGroup(%d, %s, %zu)", groupId, path_name, pathlen);
    return mDevice->set_active_group(mDevice, groupId, path_name);
}

int64_t FingerprintDaemonProxy::openHal() {
    ALOG(LOG_VERBOSE, LOG_TAG, "nativeOpenHal()\n");
    int err;
    const hw_module_t *hw_module = NULL;
    hw_device_t *device = NULL;

#if defined(USE_HAL_DYNAMIC_COMPATIBLE)
    char prop_value[ PROPERTY_VALUE_MAX] = {0};
    int prop_ret = 0;

    ALOGD("Start to mach goodix fp hw..");

#if defined(CONFIG_PROJECT_I9051)
#else
    prop_ret = property_get("sys.fp.onstart", prop_value, NULL);

    if (0 != strcmp(prop_value, "1")) { 
        ALOGE("NOT GOODIX HW, GOTO NEXT..");
        goto next_module;
    }
#endif	
    
    if(0 == (err = hw_get_module(FINGERPRINT_MODULE_GOODIX, &hw_module))){		
        if (NULL == hw_module) {
            ALOGE("No valid fingerprint module: /system/lib/hw/%s.default.so\n", FINGERPRINT_MODULE_GOODIX);
            goto next_module;
        }
		
        mModule = reinterpret_cast<const fingerprint_module_t*>(hw_module);
        if (mModule->common.methods->open == NULL) {
            ALOGE("No valid open method");
            goto next_module;
        }
		
        if (0 == (err = mModule->common.methods->open(hw_module, NULL, &device))) {    
            goto open_hal_success;
        } 
        else {
            ALOGE("Can't open goodix fingerprint methods, error: %d", err);
            goto next_module;		          
        }
    } 
    else {
        ALOGE("Can't open goodix fingerprint HW Module, error: %d", err);
        goto next_module;
    }

next_module:
	
    ALOGD("Start to mach elan fp hw..");
    if (0 == (err = hw_get_module(FINGERPRINT_MODULE_ELAN, &hw_module))) {		
        if (NULL == hw_module) {
            ALOGE("No valid fingerprint module: /system/lib/hw/%s.default.so\n", FINGERPRINT_MODULE_ELAN);
            goto open_hal_error;
        }
		
        mModule = reinterpret_cast<const fingerprint_module_t*>(hw_module);
        if (mModule->common.methods->open == NULL) {
            ALOGE("No valid elan open method");
            goto open_hal_error;
        }
		
        if (0 == (err = mModule->common.methods->open(hw_module, NULL, &device))) {		
            goto open_hal_success;
        } 
        else {
            ALOGE("Can't open elan fingerprint methods, error: %d", err);
            goto open_hal_error;		
        }
    }
    else{
        ALOGE("Can't open elan fingerprint HW Module, error: %d", err);
        goto open_hal_error;	  
    }	
	
#else /*USE_HAL_DYNAMIC_COMPATIBLE*/

    int chip = getFpChipName();	
    if (chip == GOODIX) {
        if (0 != (err = hw_get_module(FINGERPRINT_MODULE_GOODIX, &hw_module))) {
            ALOGE("Can't open fingerprint HW Module:%s.default.so, error: %d\n", FINGERPRINT_MODULE_GOODIX, err);
            goto open_hal_error;
        }
    }
    else if (chip == ELAN) {
        if (0 != (err = hw_get_module(FINGERPRINT_MODULE_ELAN, &hw_module))) {
            ALOGE("Can't open fingerprint HW Module:%s.default.so, error: %d\n", FINGERPRINT_MODULE_ELAN, err);
            goto open_hal_error;
        }
    }
    else if (chip == SILEAD) {
        if (0 != (err = hw_get_module(FINGERPRINT_MODULE_SILEAD, &hw_module))) {
            ALOGE("Can't open fingerprint HW Module:%s.default.so, error: %d\n", FINGERPRINT_MODULE_SILEAD, err);
            goto open_hal_error;
        }
    }
    else if (chip == CHIPONE) {
        if (0 != (err = hw_get_module(FINGERPRINT_MODULE_CHIPONE, &hw_module))) {
            ALOGE("Can't open fingerprint HW Module:%s.default.so, error: %d\n", FINGERPRINT_MODULE_CHIPONE, err);
            goto open_hal_error;
        }
    }
    else {
        ALOGE("!!!!! ... What happened ? -: %d\n", chip);
        goto open_hal_error;
    }

    if (NULL == hw_module) {
        ALOGE("No valid fingerprint module");
        goto open_hal_error;
    }

    mModule = reinterpret_cast<const fingerprint_module_t*>(hw_module);

    if (mModule->common.methods->open == NULL) {
        ALOGE("No valid open method");
        goto open_hal_error;
    }

    if (0 != (err = mModule->common.methods->open(hw_module, NULL, &device))) {
        ALOGE("Can't open fingerprint methods, error: %d", err);
        goto open_hal_error;
    }
	
#endif /*USE_HAL_DYNAMIC_COMPATIBLE*/

open_hal_success:

    mDevice = reinterpret_cast<fingerprint_device_t*>(device);
    err = mDevice->set_notify(mDevice, hal_notify_callback);
    if (err < 0) {
        ALOGE("Failed in call to set_notify(), err=%d", err);
        goto open_hal_error;
    }

    // Sanity check - remove
    if (mDevice->notify != hal_notify_callback) {
        ALOGE("NOTIFY not set properly: %p != %p", mDevice->notify, hal_notify_callback);
    }

    ALOGD("fingerprint HAL successfully initialized!\n");
    return reinterpret_cast<int64_t>(mDevice); // This is just a handle


open_hal_error:
	
    ALOGE("-----fingerprint HAL init failed!------\n");
	
     // Kill vendor daemon.
    property_set("sys.fp.goodix", "0");
    property_set("sys.fp.silead", "0");
    return 0;
}

int32_t FingerprintDaemonProxy::closeHal() {
    ALOG(LOG_VERBOSE, LOG_TAG, "nativeCloseHal()\n");
    if (mDevice == NULL) {
        ALOGE("No valid device");
        return -ENOSYS;
    }
    int err;
    if (0 != (err = mDevice->common.close(reinterpret_cast<hw_device_t*>(mDevice)))) {
        ALOGE("Can't close fingerprint module, error: %d", err);
        return err;
    }
    mDevice = NULL;
    return 0;
}

void FingerprintDaemonProxy::binderDied(const wp<IBinder>& who) {
    ALOGD("binder died");
    int err;
    if (0 != (err = closeHal())) {
        ALOGE("Can't close fingerprint device, error: %d", err);
    }
    if (IInterface::asBinder(mCallback) == who) {
        mCallback = NULL;
    }
}

void FingerprintDaemonProxy::setMsg(int32_t msg) {

}

int FingerprintDaemonProxy::get(int32_t arg) {
    return 0;
}


}
