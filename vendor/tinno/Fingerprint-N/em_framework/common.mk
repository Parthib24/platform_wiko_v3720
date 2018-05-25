# include $(BUILD_TINNO_EM_FRAMEWORK)

MY_DIR := $(call my-dir)

$(warning ---LOCAL_PATH---$(LOCAL_PATH)---MY_DIR--$(MY_DIR))

ifeq ($(strip $(LOCAL_PATH)),frameworks/base)
include $(MY_DIR)/framework/base/configs.mk

else ifeq ($(strip $(LOCAL_PATH)),frameworks/base/packages/Keyguard)
#include $(MY_DIR)/framework/base/packages/Keyguard/configs.mk

else ifeq ($(strip $(LOCAL_PATH)),frameworks/base/services)
include $(MY_DIR)/framework/base/services/configs.mk

else ifeq ($(strip $(LOCAL_PATH)),frameworks/base/services/core)
include $(MY_DIR)/framework/base/services/core/configs.mk

else ifeq ($(strip $(LOCAL_PATH)),packages/apps/Settings)
include $(MY_DIR)/packages/apps/Settings/configs.mk

else ifeq ($(strip $(LOCAL_PATH)),vendor/mediatek/proprietary/factory)
include $(MY_DIR)/../native/ftm/configs.mk

endif

# TINNO_TP_BACKTOUCH_SUPPORT
include $(build_em_path)/../tp_back_touch/common.mk


