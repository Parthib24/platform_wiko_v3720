# include $(BUILD_TINNO_EM_FRAMEWORK)
# tp back touch support.
#
MY_DIR := $(call my-dir)

$(warning ---LOCAL_PATH---$(LOCAL_PATH)---MY_DIR--$(MY_DIR))

ifeq ($(strip $(LOCAL_PATH)),frameworks/base)
include $(MY_DIR)/framework/base/configs.mk
endif

ifeq ($(strip $(LOCAL_PATH)),frameworks/base/services)
include $(MY_DIR)/framework/base/services/configs.mk
endif

ifeq ($(strip $(LOCAL_PATH)),frameworks/base/services/core)
include $(MY_DIR)/framework/base/services/core/configs.mk
endif

ifeq ($(strip $(LOCAL_PATH)),packages/apps/Settings)
include $(MY_DIR)/packages/apps/Settings/configs.mk
endif




