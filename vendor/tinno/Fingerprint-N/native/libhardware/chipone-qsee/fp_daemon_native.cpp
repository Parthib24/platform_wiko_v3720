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
#include "errno.h"
#include <jni.h>
#include "jni_util.h"
#include "fingerprint_sensor_demonnative.h"
#include <stdio.h>
#include <limits.h>
#include <unistd.h>
#include "fp_daemon_impl.h"
#include "fp_common.h"
#include <sys/types.h>

#define FPTAG "FingerprintDaemonNative.cpp "

static const char *FINGERPRINT_DAEMON_SERVICE = "com/fp/FingerprintDaemon/FingerprintDaemonNative";
static struct
{
    jclass clazz;
    jmethodID notify;
    jobject daemonObj;
} gFingerprintDaemonNativeClassInfo;

JavaVM *gvm = NULL;
fpDameonImpl *daemon_jni_instance = NULL;

static void notifyKeystore(uint8_t *auth_token, size_t auth_token_length)
{
    /*    if (auth_token != NULL && auth_token_length > 0) {
       // TODO: cache service?
      sp<IServiceManager> sm = defaultServiceManager();
       sp<IBinder> binder = sm->getService(String16("android.security.keystore"));
       sp<IKeystoreService> service = interface_cast<IKeystoreService>(binder);
       if (service != NULL) {
           status_t ret = service->addAuthToken(auth_token, auth_token_length);
           if (ret != ResponseCode::NO_ERROR) {
               ALOGE(FPTAG"Falure sending auth token to KeyStore: %d", ret);
           }
       } else {
           ALOGE(FPTAG"Unable to communicate with KeyStore");
       }
    }*/
}

// Called by the HAL to notify us of fingerprint events
static void hal_notify_callback(const fingerprint_msg_t *msg, void *env)
{
    JNIEnv *lenv = (JNIEnv *)env;
    jobject thiz = gFingerprintDaemonNativeClassInfo.daemonObj;
    uint32_t arg1 = 0;
    uint32_t arg2 = 0;
    uint32_t arg3 = 0;

    if(lenv== NULL || thiz == NULL || msg == NULL)
    {
        LOGE("%s invalid para",__func__);
        return;
    }

    switch (msg->type)
    {
        case FINGERPRINT_ERROR:
            arg1 = msg->data.error;
            break;
        case FINGERPRINT_ACQUIRED:
            arg1 = msg->data.acquired.acquired_info;
            break;
        case FINGERPRINT_AUTHENTICATED:
            arg1 = msg->data.authenticated.finger.fid;
            arg2 = msg->data.authenticated.finger.gid;
            if (arg1 != 0)
            {
                notifyKeystore((uint8_t *)(&msg->data.authenticated.hat),
                               sizeof(msg->data.authenticated.hat));
            }
            break;
        case FINGERPRINT_TEMPLATE_ENROLLING:
            arg1 = msg->data.enroll.finger.fid;
            arg2 = msg->data.enroll.finger.gid;
            arg3 = msg->data.enroll.samples_remaining;
            break;
        case FINGERPRINT_TEMPLATE_REMOVED:
            arg1 = msg->data.removed.finger.fid;
            arg2 = msg->data.removed.finger.gid;
            break;
        default:
            LOGE(FPTAG"fingerprint: invalid msg: %d", msg->type);
            return;
    }
    // CallbackHandler object is reference-counted, so no cleanup necessary.
    LOGD(FPTAG"msg in jni layer[type:%d,arg1:%d,arg2:%d,arg3:%d]", msg->type, arg1, arg2, arg3);
//    LOGD(FPTAG"lenv = 0x%x thiz = 0x%x notify = 0x%x ",lenv,thiz,gFingerprintDaemonNativeClassInfo.notify);
    lenv->CallVoidMethod(thiz, gFingerprintDaemonNativeClassInfo.notify, msg->type, arg1, arg2, arg3);
}

static void nativeInit(JNIEnv *env, jobject clazz, jobject daemonObj)
{
    gFingerprintDaemonNativeClassInfo.daemonObj = env->NewGlobalRef(daemonObj);
    if (!daemon_jni_instance)
    {
        LOGD(FPTAG"nativeInit create daemon_jni_instance instance\n");
        daemon_jni_instance = new fpDameonImpl(gvm);
        daemon_jni_instance->set_notify_callback(hal_notify_callback);
    }
    else
    {
        LOGE(FPTAG"nativeInit, but daemon_jni_instance is not NULL ");
    }
}

static jint nativeEnroll(JNIEnv *env, jobject clazz, jbyteArray token, jint groupId, jint timeout)
{
    LOGD(FPTAG"nativeEnroll(gid=%d, timeout=%d)\n", groupId, timeout);
    if(env == NULL || daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s invalid para",__func__);
        return -EINVAL;
    }
    const int tokenSize = env->GetArrayLength(token);
    jbyte *tokenData = env->GetByteArrayElements(token, 0);
    hw_auth_token_t aFakeToken = {0, 0, 0, 0, 1};
    hw_auth_token_t *pToken = (hw_auth_token_t *)tokenData;
    if (tokenSize != sizeof(hw_auth_token_t))
    {
        LOGD(FPTAG"nativeEnroll() : invalid token size %d\n", tokenSize);
//        return -1;
        pToken = &aFakeToken;
    }
    int ret = daemon_jni_instance->enroll(pToken, groupId, timeout);
    env->ReleaseByteArrayElements(token, tokenData, 0);
    return ret;
}

static jlong nativePreEnroll(JNIEnv *env, jobject clazz)
{
    LOGD(FPTAG"nativePreEnroll()\n");
    if(daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s daemon is not inited",__func__);
        return -ENOENT;
    }
    uint64_t ret = daemon_jni_instance->pre_enroll();
    return (jlong)((int64_t)ret);
}

static jint nativePostEnroll(JNIEnv *env, jobject clazz)
{
    LOGD(FPTAG"nativePostEnroll()\n");
    if(daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s daemon is not inited",__func__);
        return -ENOENT;
    }
    int ret = daemon_jni_instance->post_enroll();
    return ret;
}

static jint nativeStopEnrollment(JNIEnv *env, jobject clazz)
{
    LOGD(FPTAG"nativeStopEnrollment()\n");
    if(daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s daemon is not inited",__func__);
        return -ENOENT;
    }
    int ret = daemon_jni_instance->cancel();
    return ret;
}

static jint nativeAuthenticate(JNIEnv *env, jobject clazz, jlong sessionId, jint groupId)
{
    LOGD(FPTAG"nativeAuthenticate(sid=%lld, gid=%d", sessionId, groupId);
    if(daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s daemon is not inited",__func__);
        return -ENOENT;
    }
    int ret = daemon_jni_instance->authenticate(sessionId, groupId);
    return ret;
}

static jint nativeStopAuthentication(JNIEnv *env, jobject clazz)
{
    LOGD( FPTAG"nativeStopAuthentication()\n");
    if(daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s daemon is not inited",__func__);
        return -ENOENT;
    }
    int ret = daemon_jni_instance->cancel();
    return ret;
}

static jint nativeRemove(JNIEnv *env, jobject clazz, jint fingerId, jint groupId)
{
    LOGD(FPTAG"nativeRemove(fid=%d, gid=%d)\n", fingerId, groupId);
    if(daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s daemon is not inited",__func__);
        return -ENOENT;
    }
    int ret = daemon_jni_instance->delete_fid(groupId, fingerId);
    return ret;
}

static jlong nativeGetAuthenticatorId(JNIEnv *, jobject clazz)
{
    if(daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s daemon is not inited",__func__);
        return -ENOENT;
    }
    return daemon_jni_instance->get_authenticator_id();
}

static jint nativeSetActiveGroup(JNIEnv *env, jobject clazz, jint gid, jbyteArray path)
{
    if(path== NULL || daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s invalid para",__func__);
        return -EINVAL;
    }

    const int pathSize = env->GetArrayLength(path);
    jbyte *pathData = env->GetByteArrayElements(path, 0);
    if (pathSize >= PATH_MAX)
    {
        LOGE(FPTAG"Path name is too long\n");
        return -EINVAL;
    }
    char path_name[PATH_MAX] = {0};
    memcpy(path_name, pathData, pathSize);
    LOGD(FPTAG"nativeSetActiveGroup() path: %s, gid: %d\n", path_name, gid);
    int result = daemon_jni_instance->set_active_group(gid, path_name);
    env->ReleaseByteArrayElements(path, pathData, 0);
    return result;
}

static jint nativeOpenHal(JNIEnv *env, jobject clazz)
{
    LOGD(FPTAG"nativeOpenHal invoked");
    int iRet = 0;
    if (daemon_jni_instance)
    {
        iRet = daemon_jni_instance->open_hal();
        if (iRet == 0)
        {
            LOGD(FPTAG"fp_openHal return 0, fpdfpsensor try suiside");
            daemon_jni_instance->close_hal();

            usleep(10 * 1000);

            delete daemon_jni_instance;
            daemon_jni_instance = NULL;
        }
        else
        {
            if (get_fp_config_feature_navigator())
            {
                daemon_jni_instance->service_control(FP_SERVICE_CONTROL_CMD_NAVIGATOR_ENABLE, 1);
            }
        }
    }
    else
    {
        return 0; //open error
    }
    LOGD(FPTAG"open_hal ret = %d\n", iRet);
    return iRet;
}

static jint nativeCloseHal(JNIEnv *env, jobject clazz)
{
    if (daemon_jni_instance)
    {
        return daemon_jni_instance->close_hal();
    }
    return -ENOSYS; // TODO
}
//no need to manually release the new allocated variables return to java
static jintArray nativeGetEnrolledFids(JNIEnv *env, jobject clazz)
{
    LOGD( FPTAG"nativeGetEnrolledFids()\n");
    if(daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s daemon is not inited",__func__);
        return NULL;
    }

    int iEnrollFids[FP_CONFIG_MAX_ENROLL_SLOTS];
    int iEnrollCnts = 0;
    int result = daemon_jni_instance->get_enrolled_fids(iEnrollFids, FP_CONFIG_MAX_ENROLL_SLOTS,
                                                        &iEnrollCnts);

    if (result == 0 && (iEnrollCnts > 0 && iEnrollCnts <= FP_CONFIG_MAX_ENROLL_SLOTS))
    {
        jintArray jia = env->NewIntArray(iEnrollCnts);
        jint *p = env->GetIntArrayElements(jia, 0);
        for (int i = 0; i < iEnrollCnts; i++)
        {
            p[i] = iEnrollFids[i];
        }
        env->ReleaseIntArrayElements(jia, p, 0);
        return jia;
    }

    return NULL;
}

static jint nativeServiceControl(JNIEnv *env, jobject clazz, jint ipara1, int ipara2)
{
    LOGD(FPTAG"nativeServiceControl(ipara1=%d, ipara2=%d)\n", ipara1, ipara2);
    if(daemon_jni_instance == NULL)
    {
        LOGE(FPTAG"%s daemon is not inited",__func__);
        return -ENOENT;
    }
    return daemon_jni_instance->service_control(ipara1, ipara2);
}

// ----------------------------------------------------------------------------


// TODO: clean up void methods
static const JNINativeMethod g_methods[] =
{
    { "nativeAuthenticate", "(JI)I", (void *)nativeAuthenticate },
    { "nativeStopAuthentication", "()I", (void *)nativeStopAuthentication },
    { "nativeEnroll", "([BII)I", (void *)nativeEnroll },
    { "nativeSetActiveGroup", "(I[B)I", (void *)nativeSetActiveGroup },
    { "nativePreEnroll", "()J", (void *)nativePreEnroll },
    { "nativePostEnroll", "()I", (void *)nativePostEnroll },
    { "nativeStopEnrollment", "()I", (void *)nativeStopEnrollment },
    { "nativeRemove", "(II)I", (void *)nativeRemove },
    { "nativeGetAuthenticatorId", "()J", (void *)nativeGetAuthenticatorId },
    { "nativeOpenHal", "()I", (void *)nativeOpenHal },
    { "nativeCloseHal", "()I", (void *)nativeCloseHal },
    { "nativeInit", "(Lcom/fp/FingerprintDaemon/FingerprintDaemonNative;)V", (void *)nativeInit },
    { "nativeGetEnrolledFids", "()[I", (void *)nativeGetEnrolledFids },
    { "nativeServiceControl", "(II)I", (void *)nativeServiceControl },
};

int register_android_server_fingerprint_FingerprintService(JNIEnv *env)
{
    fp::ScopedLocalRef fpDaemonnative_class(env, env->FindClass(FINGERPRINT_DAEMON_SERVICE));
    gFingerprintDaemonNativeClassInfo.clazz = (jclass) env->NewGlobalRef((
                                                                             jclass)fpDaemonnative_class.ref());
    gFingerprintDaemonNativeClassInfo.notify = env->GetMethodID(gFingerprintDaemonNativeClassInfo.clazz,
                                                                "notify", "(IIII)V");
    int result = env->RegisterNatives(gFingerprintDaemonNativeClassInfo.clazz, g_methods,
                                      sizeof(g_methods) / sizeof(g_methods[0]));
    LOGD(FPTAG"FingerprintDaemonNative JNI ready.\n");
    return result;
}

int deregister_service(JNIEnv *env)
{
    env->DeleteGlobalRef(gFingerprintDaemonNativeClassInfo.clazz);
    env->DeleteGlobalRef(gFingerprintDaemonNativeClassInfo.daemonObj);
    if (daemon_jni_instance)
    {
        delete daemon_jni_instance;
    }

    daemon_jni_instance = NULL;

    return 0;
}

extern "C" jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env;
    gvm = vm;
    LOGD(FPTAG"JNI_OnLoad invoked");
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK)
    {
        LOGE(FPTAG"GetEnv failed");
        return -1;
    }

    if (register_android_server_fingerprint_FingerprintService(env))
    {
        LOGE(FPTAG"FingerprintDaemonNative registerMethods failed\n");
        return -1;
    }

    return JNI_VERSION_1_4;
}

void JNI_OnUnload( JavaVM *vm, void   *reserved )
{
    LOGD(FPTAG"JNI_OnUnload invoked");
    return ;
}
