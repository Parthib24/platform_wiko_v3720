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

#define LOG_TAG "Backtouch-JNI"

#include "JNIHelp.h"
#include <inttypes.h>

#include <android_runtime/AndroidRuntime.h>
#include <android_runtime/Log.h>
#include <android_os_MessageQueue.h>
#include <binder/IServiceManager.h>
#include <utils/String16.h>
#include <utils/Looper.h>
#include <keystore/IKeystoreService.h>
#include <keystore/keystore.h> // for error code

#include <hardware/hardware.h>
#include <hardware/fingerprint.h>
#include <hardware/hw_auth_token.h>

#include <utils/Log.h>
#include "core_jni_helpers.h"
#include "fp.h"
#include <sys/system_properties.h>

#undef LOG_TAG
#define LOG_TAG  "[Tinnobacktouch-jni]"
#define TAG LOG_TAG

namespace android {

/*****************************************************************************************/

struct JNI_data {
    JavaVM *g_vm;
    JNIEnv *g_env;
    jobject g_thiz;
    jclass clazz;
    jmethodID cb;
    int loop;
} ;

static JNI_data mJNI_data;
static JNI_data *mJdata_p = &mJNI_data;
static pthread_t mThd;

static const char* TP_BACKTOUCH_NATIVE = "com/android/server/fingerprint/TpNative";
/*****************************************************************************************/

static void *monitor_thread(void *args) {
    int event = 0;
    JNI_data *mJdata = (JNI_data *)args;
    
    LOGD(TAG "[%s]-Entry--\n",__func__);
	
    if (mJdata == NULL) {
        LOGE("err: JNI_data is NULL!--/n");
        goto error;
    }
    
    mJdata->g_vm->AttachCurrentThread(&mJdata->g_env, NULL);
    
    do {
        if (tp_update_finger_state(&event) < 0) {
            LOGE(TAG "[%s] update err!\n", __func__);
            continue;
        }
        mJdata->g_env->CallIntMethod(mJdata->g_thiz, mJdata->cb, event);
    }
    while(mJdata->loop);
	
    mJdata->g_vm->DetachCurrentThread();
    
error:
    
    LOGE(TAG "[%s]-Exit--\n", __func__);
    mJdata->g_env->DeleteGlobalRef(mJdata->g_thiz);
    pthread_exit(NULL);   
    return NULL;
}


static int native_setObj(JNIEnv *env, jobject object,jobject object1) {
    int ret = 0;
    
    if (mJdata_p->loop == 0) {
    	return ret;
    }
    
    if (env->GetJavaVM(&mJdata_p->g_vm) != 0) {
    	LOGE("GetJavaVM NULL!--/n");
    	goto error;
    }
    
    mJdata_p->g_thiz = env->NewGlobalRef(object1);
    if (mJdata_p->g_thiz == NULL) {
    	LOGE("mJdata_p->g_thiz is NULL!--/n");
    	goto error;
    }
    
    mJdata_p->clazz = env->GetObjectClass(mJdata_p->g_thiz);
    if (mJdata_p->clazz == NULL) {
    	LOGE("GetObjectClass is NULL!--/n");
    	goto error;
    }
    
    mJdata_p->cb = env->GetMethodID(mJdata_p->clazz, "onEventReport", "(I)I");
    if (mJdata_p->cb == NULL) {
    	LOGE("GetMethodID is NULL!--/n");
    	goto error;
    }
    
    mJdata_p->g_env = env;
    if(pthread_create(&mThd, NULL, monitor_thread, (void *)mJdata_p)) {
    	LOGE("pthread_create : fail!--/n");
    	goto error;
    }
    
    return ret;
    
error:
    
    LOGE("%s: err!/n",__func__);
    return -1;
}

static int native_init(JNIEnv *env, jobject object, jint set) {
    LOGD(TAG "[%s]-Entry--\n",__func__);
    
    if (mJdata_p == NULL) {
        LOGE(TAG "mJdata_p is NULL\n");
        goto error;
    }
    
    if (mJdata_p->loop) {
        LOGE(TAG "thread-loop runing!--\n");
        goto error;
    }
    
    if (bt_dev_init() < 0) {
        LOGE("fp_dev_init : fail!--/n");
        goto error;
    }
    
    mJdata_p->loop = set;
    return 0;
    
error:
    LOGE("%s: err!/n",__func__);   
    return -1;
}



static const JNINativeMethod g_methods[] = {
    { "native_init", "(I)I", (void *)native_init },
    { "native_setObj", "(Lcom/android/server/fingerprint/TpNative;)I", (void *)native_setObj },
	
};

int register_android_server_fingerprint_BackTouchService(JNIEnv* env) {
    int result = RegisterMethodsOrDie(env, TP_BACKTOUCH_NATIVE, g_methods, NELEM(g_methods));
    ALOG(LOG_VERBOSE, LOG_TAG, "TpNative JNI ready.\n");
    //ALOG(LOG_VERBOSE, LOG_TAG, "sys.fingerprintd=normal-boot\n");
    //__system_property_set("sys.fingerprintd", "normal-boot");
    return result;
}


} // namespace android
