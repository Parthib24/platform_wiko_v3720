# Copyright (C) 2013 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# Modified by sileadinc begin
#LOCAL_MODULE := fingerprint.default
LOCAL_MODULE := fingerprint.silead.default
# Modified by sileadinc end

LOCAL_MODULE_RELATIVE_PATH := hw

# Modified by sileadinc begin
#LOCAL_SRC_FILES := fingerprint.c
LOCAL_SRC_FILES := fingerprint.cpp
# Modified by sileadinc end

LOCAL_SHARED_LIBRARIES := liblog

# Add by sileadinc begin
LOCAL_SRC_FILES += ainffpsvcfpapkrelayerCB.cpp SileadFingerprint.cpp
LOCAL_STATIC_LIBRARIES := libfpsvcd_remoteapi libslbase
LOCAL_STATIC_LIBRARIES += libfpfslockd_remoteapi libalog libaslxml libslos

LOCAL_C_INCLUDES := $(LOCAL_PATH) \
$(LOCAL_PATH)/../../../third-part/Silead/msm8937/arm64/silead_6163/prebuiltlibs/libs/inc

$(warning $(LOCAL_C_INCLUDES))

LOCAL_LDFLAGS := -Wl,--no-undefined #-Wl,--no-allow-shlib-undefined
LOCAL_CFLAGS_64:=-D__GCC_BUILTIN_ARCH64__
LOCAL_CPPFLAGS_64:=-D__GCC_BUILTIN_ARCH64__
LOCAL_SHARED_LIBRARIES += libcutils
# Add by sileadinc end

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
