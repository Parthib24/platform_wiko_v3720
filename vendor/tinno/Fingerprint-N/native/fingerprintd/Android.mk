#
# Copyright (C) 2015 The Android Open Source Project
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
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#LOCAL_CFLAGS := -Wall -Wextra -Werror -Wunused
ifneq ($(strip $(MTK_TARGET_PROJECT)),)
LOCAL_CFLAGS += -DMTK_TARGET_PROJECT
TARGET_PRJ := $(MTK_TARGET_PROJECT)
else
TARGET_PRJ := $(TARGET_PRODUCT)
endif 

TARGET_PRJ := $(strip $(TARGET_PRJ))

ifneq ($(strip $(TARGET_PRJ)),)
PROJECT_FLAGS := $(shell echo CONFIG_PROJECT_$(TARGET_PRJ)|tr a-z A-Z)
LOCAL_CFLAGS += -D$(PROJECT_FLAGS)
$(warning --LOCAL_CFLAGS += -D$(PROJECT_FLAGS)--)
endif


#Just for mtk microtrust tee.add by yinglong.tang
ifeq ($(strip $(MICROTRUST_TEE_SUPPORT)),yes)
LOCAL_CFLAGS += -DTINNO_MICROTRUST_TEE_SUPPORT
endif

LOCAL_C_INCLUDES := $(LOCAL_PATH) 

LOCAL_SRC_FILES := \
	FingerprintDaemonProxy.cpp \
	IFingerprintDaemon.cpp \
	IFingerprintDaemonCallback.cpp \
	fingerprintd.cpp

LOCAL_SRC_FILES += \
    external.cpp

LOCAL_MODULE := fingerprintd
LOCAL_SHARED_LIBRARIES := \
	libbinder \
	liblog \
	libhardware \
	libutils \
	libkeystore_binder \
	libcutils

ifneq ($(strip $(findstring silead, $(TINNO_FINGERPRINT_SUPPORT))),)
  ifneq ($(strip $(MICROTRUST_TEE_SUPPORT)),yes)
    LOCAL_SHARED_LIBRARIES += libhardware_detect_ca_qsee
    LOCAL_CFLAGS += -DFINGERPRINT_SUPPORT_SILEAD
  endif
endif

ifneq ($(strip $(findstring chipone, $(TINNO_FINGERPRINT_SUPPORT))),)
    ifneq ($(strip $(MICROTRUST_TEE_SUPPORT)),yes)
        LOCAL_CFLAGS += -DFINGERPRINT_SUPPORT_CHIPONE
    endif
endif

include $(BUILD_EXECUTABLE)
