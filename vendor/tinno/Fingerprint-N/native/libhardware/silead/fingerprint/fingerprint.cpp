/*
 * Copyright (C) 2014 The Android Open Source Project
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
#define LOG_TAG "FingerprintHal"

#include <errno.h>
#include <malloc.h>
#include <string.h>
#include <cutils/log.h>
#include <hardware/hardware.h>
#include "fingerprint.h"
#include "SileadFingerprint.h"


static int fingerprint_close(hw_device_t *dev)
{
    if (dev) {
        free(dev);
        return 0;
    } else {
        return -1;
    }
}


static uint64_t fingerprint_pre_enroll(struct fingerprint_device __unused *dev) {
    return FINGERPRINT_ERROR;
}

static int fingerprint_enroll(struct fingerprint_device __unused *dev,
                                const hw_auth_token_t __unused *hat,
                                uint32_t __unused gid,
                                uint32_t __unused timeout_sec) {
    return FINGERPRINT_ERROR;
}

static uint64_t fingerprint_get_auth_id(struct fingerprint_device __unused *dev) {
    return FINGERPRINT_ERROR;
}

static int fingerprint_cancel(struct fingerprint_device __unused *dev) {
    return FINGERPRINT_ERROR;
}

static int fingerprint_remove(struct fingerprint_device __unused *dev,
                                uint32_t __unused gid, uint32_t __unused fid) {
    return FINGERPRINT_ERROR;
}

static int fingerprint_set_active_group(struct fingerprint_device __unused *dev,
                                        uint32_t __unused gid, const char __unused *store_path) {
    return FINGERPRINT_ERROR;
}

static int fingerprint_authenticate(struct fingerprint_device __unused *dev,
                                    uint64_t __unused operation_id, __unused uint32_t gid) {
    return FINGERPRINT_ERROR;
}

static int set_notify_callback(struct fingerprint_device *dev,
                                fingerprint_notify_t notify) {
    /* Decorate with locks */
    dev->notify = notify;
    return FINGERPRINT_ERROR;
}

// Add by sileadinc begin
static int fingerprint_post_enroll(struct fingerprint_device __unused *dev) {
    return FINGERPRINT_ERROR;
}

static int32_t SetFPScreenStatus(int32_t screenStatus)
{
    return FINGERPRINT_ERROR;
}

static int32_t setFPEnableCredential(int32_t index, int32_t enable)
{
    return FINGERPRINT_ERROR;
}

static int32_t getFPEnableCredential(int32_t index)
{
    return FINGERPRINT_ERROR;
}

static int32_t getFPVirtualKeyCode()
{
    return FINGERPRINT_ERROR;
}

static int32_t setFPVirtualKeyCode(int VirtualKeyCode)
{
    return FINGERPRINT_ERROR;
}

static int32_t getFPLongPressVirtualKeyCode()
{
    return FINGERPRINT_ERROR;
}

static int32_t setFPLongPressVirtualKeyCode(int VirtualKeyCode)
{
    return FINGERPRINT_ERROR;
}

static int32_t getFPDouClickVirtualKeyCode()
{
    return FINGERPRINT_ERROR;
}

static int32_t setFPDouClickVirtualKeyCode(int VirtualKeyCode)
{
    return FINGERPRINT_ERROR;
}

static int32_t getFPVirtualKeyState()
{
    return FINGERPRINT_ERROR;
}

static int32_t setFPVirtualKeyState(int VirtualKeyState)
{
    return FINGERPRINT_ERROR;
}

static int32_t getFPWakeUpState()
{
    return FINGERPRINT_ERROR;
}

static int32_t setFPWakeUpState(int WakeUpState)
{
    return FINGERPRINT_ERROR;
}

static int32_t getFingerPrintState()
{
    return FINGERPRINT_ERROR;
}

static int32_t setFingerPrintState(int32_t FingerPrintState)
{
    return FINGERPRINT_ERROR;
}

static int32_t setFPPowerFuncKeyState(int32_t FuncKeyState)
{
    return FINGERPRINT_ERROR;
}

static int32_t getFPPowerFuncKeyState()
{
    return FINGERPRINT_ERROR;
}

static int32_t setFPIdleFuncKeyState(int32_t FuncKeyState)
{
    return FINGERPRINT_ERROR;
}

static int32_t getFPIdleFuncKeyState()
{
    return FINGERPRINT_ERROR;
}

static int32_t setFPWholeFuncKeyState(int FuncKeyState)
{
    return FINGERPRINT_ERROR;
}

static int32_t setFPFunctionKeyState(int32_t index, int32_t enable)
{
    return FINGERPRINT_ERROR;
}

static int32_t getFPFunctionKeyState(int32_t index)
{
    return FINGERPRINT_ERROR;
}
// Add by sileadinc end

static int fingerprint_open(const hw_module_t* module, const char __unused *id,
                            hw_device_t** device)
{
    if (device == NULL) {
        ALOGE("NULL device on open");
        return -EINVAL;
    }

    // Modified by sileadinc begin
    //fingerprint_device_t *dev = malloc(sizeof(fingerprint_device_t));
    fingerprint_device_t *dev = (fingerprint_device_t *)malloc(sizeof(fingerprint_device_t));
    // Modified by sileadinc end
    memset(dev, 0, sizeof(fingerprint_device_t));

    dev->common.tag = HARDWARE_DEVICE_TAG;
    dev->common.version = FINGERPRINT_MODULE_API_VERSION_2_0;
    dev->common.module = (struct hw_module_t*) module;
    dev->common.close = fingerprint_close;

    // Modified by sileadinc begin
    #if 0
    dev->pre_enroll = fingerprint_pre_enroll;
    dev->enroll = fingerprint_enroll;
    dev->post_enroll = fingerprint_post_enroll;
    dev->get_authenticator_id = fingerprint_get_auth_id;
    dev->cancel = fingerprint_cancel;
    dev->remove = fingerprint_remove;
    dev->set_active_group = fingerprint_set_active_group;
    dev->authenticate = fingerprint_authenticate;
    dev->set_notify = set_notify_callback;
    dev->notify = NULL;

    dev->SetFPScreenStatus = SetFPScreenStatus;
    dev->setFPEnableCredential = setFPEnableCredential;
    dev->getFPEnableCredential = getFPEnableCredential;
    dev->getFPVirtualKeyCode  = getFPVirtualKeyCode;
    dev->setFPVirtualKeyCode  = setFPVirtualKeyCode;
    dev->getFPLongPressVirtualKeyCode = getFPLongPressVirtualKeyCode;
    dev->setFPLongPressVirtualKeyCode = setFPLongPressVirtualKeyCode;
    dev->getFPDouClickVirtualKeyCode =getFPDouClickVirtualKeyCode;
    dev->setFPDouClickVirtualKeyCode = setFPDouClickVirtualKeyCode;
    dev->getFPVirtualKeyState = getFPVirtualKeyState;
    dev->setFPVirtualKeyState =setFPVirtualKeyState;
    dev->getFPWakeUpState = getFPWakeUpState;
    dev->setFPWakeUpState =setFPWakeUpState;
    dev->getFingerPrintState = getFingerPrintState;
    dev->setFingerPrintState =setFingerPrintState;
    dev->setFPPowerFuncKeyState =setFPPowerFuncKeyState;
    dev->getFPPowerFuncKeyState =getFPPowerFuncKeyState;
    dev->setFPIdleFuncKeyState = setFPIdleFuncKeyState;
    dev->getFPIdleFuncKeyState = getFPIdleFuncKeyState;
    dev->setFPWholeFuncKeyState = setFPWholeFuncKeyState;
    dev->setFPFunctionKeyState = setFPFunctionKeyState;
    dev->getFPFunctionKeyState = getFPFunctionKeyState;
    #else
    dev->pre_enroll = silead_fingerprint_pre_enroll;
    dev->enroll = silead_fingerprint_enroll;
    dev->post_enroll = silead_fingerprint_post_enroll;
    dev->get_authenticator_id = silead_fingerprint_get_auth_id;
    dev->cancel = silead_fingerprint_cancel;
    dev->remove = silead_fingerprint_remove;
    dev->set_active_group = silead_fingerprint_set_active_group;
    dev->authenticate = silead_fingerprint_authenticate;
    dev->set_notify = silead_set_notify_callback;
    /*dev->SetFPScreenStatus = silead_SetFPScreenStatus;
    dev->setFPEnableCredential = silead_setFPEnableCredential;
    dev->getFPEnableCredential = silead_getFPEnableCredential;
    dev->getFPVirtualKeyCode  = silead_getFPVirtualKeyCode;
    dev->setFPVirtualKeyCode  = silead_setFPVirtualKeyCode;
    dev->getFPLongPressVirtualKeyCode = silead_getFPLongPressVirtualKeyCode;
    dev->setFPLongPressVirtualKeyCode = silead_setFPLongPressVirtualKeyCode;
    dev->getFPDouClickVirtualKeyCode = silead_getFPDouClickVirtualKeyCode;
    dev->setFPDouClickVirtualKeyCode = silead_setFPDouClickVirtualKeyCode;
    dev->getFPVirtualKeyState = silead_getFPVirtualKeyState;
    dev->setFPVirtualKeyState = silead_setFPVirtualKeyState;
    dev->getFPWakeUpState = silead_getFPWakeUpState;
    dev->setFPWakeUpState = silead_setFPWakeUpState;
    dev->getFingerPrintState = silead_getFingerPrintState;
    dev->setFingerPrintState = silead_setFingerPrintState;
    dev->setFPPowerFuncKeyState = silead_setFPPowerFuncKeyState;
    dev->getFPPowerFuncKeyState = silead_getFPPowerFuncKeyState;
    dev->setFPIdleFuncKeyState = silead_setFPIdleFuncKeyState;
    dev->getFPIdleFuncKeyState = silead_getFPIdleFuncKeyState;
    dev->setFPWholeFuncKeyState = silead_setFPWholeFuncKeyState;
    dev->setFPFunctionKeyState = silead_setFPFunctionKeyState;
    dev->getFPFunctionKeyState = silead_getFPFunctionKeyState;*/
    #endif
    // Modified by sileadinc end

    *device = (hw_device_t*) dev;
    return 0;
}

static struct hw_module_methods_t fingerprint_module_methods = {
    .open = fingerprint_open,
};

fingerprint_module_t HAL_MODULE_INFO_SYM = {
    .common = {
        .tag                = HARDWARE_MODULE_TAG,
        .module_api_version = FINGERPRINT_MODULE_API_VERSION_2_0,
        .hal_api_version    = HARDWARE_HAL_API_VERSION,
        .id                 = FINGERPRINT_HARDWARE_MODULE_ID,
        .name               = "Demo Fingerprint HAL",
        .author             = "The Android Open Source Project",
        .methods            = &fingerprint_module_methods,
    },
};
