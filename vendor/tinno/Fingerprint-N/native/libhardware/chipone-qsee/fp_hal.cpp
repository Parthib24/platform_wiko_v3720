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

#include <inttypes.h>
#include "fp_log.h"
#include "hw_auth_token.h"
#include "errno.h"
#include <jni.h>
#include "jni_util.h"
#include <stdio.h>
#include <limits.h>
#include <unistd.h>
#include "fp_daemon_impl.h"
#include <sys/types.h>
#include <string.h>
#include "fp_common.h"
#include "fingerprint_sensor_demonnative.h"


#define FPTAG "fp_hal.cpp [guomingyi]"

typedef struct fp_fingerprint_hal_device
{
    fingerprint_device_t device; //inheritance
} fp_fingerprint_hal_device_t;


fpDameonImpl *daemon_hal_instance = NULL;

extern "C" {
    void fp_init(void)
    {
        if (daemon_hal_instance)
        {
            LOGE(FPTAG"nativeInit, but daemon_hal_instance is not NULL, destory instance and recreate again");
            delete daemon_hal_instance;
            daemon_hal_instance = NULL;
        }

        LOGD(FPTAG"nativeInit create daemon_hal_instance instance\n");
        daemon_hal_instance = new fpDameonImpl(NULL);
    }

    int fp_enroll(struct fingerprint_device *device, const hw_auth_token_t *token, uint32_t groupId,
                  uint32_t timeout)
    {
        if (daemon_hal_instance)
        {
            return  daemon_hal_instance->enroll((hw_auth_token_t *)token, groupId,  timeout);
        }
        else
        {
            LOGE(FPTAG"daemon_hal_instance is not initilized");
            return -ENODEV;
        }
    }
    uint64_t fp_preEnroll(struct fingerprint_device *device)
    {
        if (daemon_hal_instance)
        {
            return daemon_hal_instance->pre_enroll();
        }
        else
        {
            LOGE(FPTAG"daemon_hal_instance is not initilized");
            return -ENODEV;
        }
    }
    int fp_postEnroll(struct fingerprint_device *device)
    {
        if (daemon_hal_instance)
        {
            return daemon_hal_instance->post_enroll();
        }
        else
        {
            LOGE(FPTAG"daemon_hal_instance is not initilized");
            return -ENODEV;
        }
    }

    int fp_cancel(struct fingerprint_device *device)
    {
        if (daemon_hal_instance)
        {
            return daemon_hal_instance->cancel();
        }
        else
        {
            LOGE(FPTAG"daemon_hal_instance is not initilized");
            return -ENODEV;
        }
    }

    int fp_authenticate(struct fingerprint_device *device, uint64_t sessionId, uint32_t groupId)
    {
        if (daemon_hal_instance)
        {
            return daemon_hal_instance->authenticate(sessionId, groupId);
        }
        else
        {
            LOGE(FPTAG"daemon_hal_instance is not initilized");
            return -ENODEV;
        }
    }

    int fp_remove(struct fingerprint_device *device, uint32_t groupId , uint32_t fingerId)
    {
        if (daemon_hal_instance)
        {
            return daemon_hal_instance->delete_fid(groupId, fingerId);
        }
        else
        {
            LOGE(FPTAG"daemon_hal_instance is not initilized");
            return -ENODEV;
        }
    }

    uint64_t fp_getAuthenticatorId(struct fingerprint_device *device)
    {
        if (daemon_hal_instance)
        {
            return daemon_hal_instance->get_authenticator_id();
        }
        else
        {
            LOGE(FPTAG"daemon_hal_instance is not initilized");
            return -ENODEV;
        }
    }

    int fp_setActiveGroup(struct fingerprint_device *device, uint32_t groupId, const char *path)
    {
        if (daemon_hal_instance)
        {
            return daemon_hal_instance->set_active_group(groupId, (char *)path);
        }
        else
        {
            LOGE(FPTAG"daemon_hal_instance is not initilized");
            return -ENODEV;
        }
    }

    int64_t fp_openHal()
    {
        int64_t ret = 0;    

        if (daemon_hal_instance)
        {
            ret  = daemon_hal_instance->open_hal();
            if (0 == ret)
            {
                LOGD(FPTAG"fp_openHal return 0, fpdfpsensor suiside");
                delete daemon_hal_instance;
                daemon_hal_instance = NULL;
            }
            else
            {
                if (get_fp_config_feature_navigator())
                {
                    LOGD(FPTAG" enable navgigator task");
                    daemon_hal_instance->service_control(FP_SERVICE_CONTROL_CMD_NAVIGATOR_ENABLE, 1);
                }
            }
        }
        else
        {
            LOGE(FPTAG"daemon_hal_instance is not initilized");
        }
        LOGD(FPTAG"###fp_openHal ret = %" PRId64 "\n", ret);
        return ret;
    }

    int fp_closeHal()
    {
        LOGD(FPTAG"fp_closeHal delete daemon_hal_instance ");
        if (daemon_hal_instance)
        {
            delete daemon_hal_instance;
            daemon_hal_instance = NULL;
        }
        LOGD(FPTAG"fp_closeHal finished");
        return 0; // TODO
    }
    int32_t fp_getEnrolledFids(int32_t *pArray, int32_t iArraySize, int32_t *pFidsCnt)
    {
        if (daemon_hal_instance)
        {
            return daemon_hal_instance->get_enrolled_fids(pArray, iArraySize, pFidsCnt);
        }
        else
        {
            LOGE(FPTAG"daemon_hal_instance is not initilized");
            return -ENODEV;
        }
    }

    extern int32_t extension_service_status;
    int32_t fp_set_extension_status(int32_t new_status)
    {
        extension_service_status = new_status;
        return extension_service_status;
    }

    int32_t fp_service_control(int32_t tag, int32_t value)
    {
        int32_t ret = -ENOENT;
        if (daemon_hal_instance)
        {
            return daemon_hal_instance->service_control(tag,value);
        }
        return ret;
    }
//add more test API for spreadtrum
    int32_t factory_init(void)
    {
        fp_init();
        fp_set_extension_status(0);
        if (!fp_openHal())
        {
            return -1;
        }
        //disable navigator
        fp_service_control(FP_SERVICE_CONTROL_CMD_NAVIGATOR_ENABLE, 0);
        return 0;
    }
    int32_t factory_exit(void)
    {
        fp_closeHal();
        return 0;
    }
    int32_t finger_detect(void_callback cb)
    {
        LOGD(FPTAG"finger_detect test invoked");
        int32_t ret = daemon_hal_instance->finger_detect(cb);
        return ret == 0 ? 0 : -1;
    }
    int32_t deadpixel_test(void)
    {
        LOGD(FPTAG"deadpixel_test test invoked");
        int32_t ret = fp_service_control(FP_SERVICE_CONTROL_CMD_CHECK_BOARD, 0);
        LOGD(FPTAG"deadpixel_test test ret = %d\n",ret);
        return ret == 0 ? 0 : -1;
    }
    int32_t interrupt_test(void)
    {
        LOGD(FPTAG"interrupt_test test invoked");
        int32_t ret = fp_service_control(FP_SERVICE_CONTROL_CMD_SELF_TEST, 0);
        LOGD(FPTAG"interrupt_test test ret = %d\n",ret);
        return ret == 0 ? 0 : -1;
    }
    int32_t spi_test(void)
    {
        LOGD(FPTAG"spi_test test invoked");
        int32_t ret = fp_service_control(FP_SERVICE_CONTROL_CMD_SELF_TEST, 0);
        LOGD(FPTAG"spi_test test ret = %d\n",ret);
        return ret == 0 ? 0 : -1;
    }

    static int fp_set_notify(struct fingerprint_device *device,
                             fingerprint_notify_t notify)
    {
        LOGD(FPTAG"%s", __func__);

        device->notify = notify;//no used just dummy.
        daemon_hal_instance->set_notify_callback(notify);
        return 0;
    }

#if (TARGET_ANDROID >= 7)
    static int fp_enumerate(struct fingerprint_device *dev)
    {
        if(daemon_hal_instance)
        {
            return daemon_hal_instance->enumerate();
        }

        LOGE(FPTAG"daemon_hal_instance is not initilized");
        return -ENOENT;
    }

#else
    static int fp_enumerate(struct fingerprint_device *device,
                            fingerprint_finger_id_t *results,
                            uint32_t *max_size)
    {
        LOGD(FPTAG"%s", __func__);

        int32_t fids_array[FP_CONFIG_MAX_ENROLL_SLOTS];
        int32_t fids_array_size =  FP_CONFIG_MAX_ENROLL_SLOTS;
        int32_t fids_len = 0;
        fp_getEnrolledFids(fids_array, fids_array_size, &fids_len);

        if (results == NULL)
        {
            *max_size = fids_len;
            return fids_len;
        }

        int32_t copy_len = fids_len;
        if ((int32_t)(*max_size) < fids_len)
        {
            LOGE(FPTAG"fp_enumerate buffer size too small, cur = %d, need = %d", *max_size, fids_len);
            copy_len = *max_size;
        }

        for (int i = 0; i < copy_len; i++)
        {
            results->fid = fids_array[i];
            results->gid = 1;
        }

        return copy_len;
    }
#endif

    static int fp_module_close(hw_device_t *device)
    {
        LOGD(FPTAG"%s", __func__);

        if (!device)
        {
            return 0;
        }
        fp_closeHal();

        fp_free(device);

        return 0;
    }

    int fp_module_open(const hw_module_t *module, const char *name,
                              hw_device_t **device)
    {
        LOGD(FPTAG"%s", __func__);


        *device = NULL;

        fp_fingerprint_hal_device_t *dev = (fp_fingerprint_hal_device_t *)
                                           fp_malloc(sizeof(fp_fingerprint_hal_device_t));

        if (!dev)
        {
            return -ENOMEM;
        }

        memset(dev, 0, sizeof(fp_fingerprint_hal_device_t));

        dev->device.common.tag = HARDWARE_DEVICE_TAG;
        dev->device.common.version = FINGERPRINT_MODULE_API_VERSION_2_0;
        dev->device.common.module = (struct hw_module_t *) module;
        dev->device.common.close = fp_module_close;
        dev->device.enroll = fp_enroll;
        dev->device.cancel = fp_cancel;
        dev->device.remove = fp_remove;
        dev->device.set_notify = fp_set_notify;
        dev->device.notify = NULL;
        dev->device.authenticate = fp_authenticate;
        dev->device.pre_enroll = fp_preEnroll;
        dev->device.enumerate = fp_enumerate;
        dev->device.get_authenticator_id = fp_getAuthenticatorId;
        dev->device.set_active_group = fp_setActiveGroup;
        dev->device.post_enroll = fp_postEnroll;

        fp_init();
        if (!fp_openHal())
        {
            goto err;
        }
        *device = (hw_device_t *) dev;

        return 0;
    err:
        LOGE(FPTAG"%s failed\n", __func__);
        fp_module_close((hw_device_t *) dev);
       
        return -1;
    }


    static struct hw_module_methods_t fp_module_methods =
    {
        fp_module_open
    };

    fingerprint_module_t HAL_MODULE_INFO_SYM =
    {
        {
            HARDWARE_MODULE_TAG,                /* TAG */
            FINGERPRINT_MODULE_API_VERSION_2_0, /* Module API version*/
            0,                                  /* HW API version */
            FPSENSOR_FINGERPRINT_HARDWARE_MODULE_ID,     /* ID */
            "FPSENSOR Fingrprint HAL",               /* Module name */
            "Fingerprint",             /* Module author */
            &fp_module_methods,                /* Module methods */
            0,                                  /* dso */
            {0,},                               /* reserved */
        },
    };

    void rename_hal(const char *hal_new_name)
    {
        if(strcmp(HAL_MODULE_INFO_SYM.common.id,hal_new_name) != 0)
        {
            LOGD(FPTAG"[guomingyi]hal old name:%s,new name %s",HAL_MODULE_INFO_SYM.common.id,hal_new_name);
            HAL_MODULE_INFO_SYM.common.id = hal_new_name;
        }
    }
};

