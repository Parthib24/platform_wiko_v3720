# CopyRight Silead
# By Warren Zhao

ifeq ($(SL_FPSYS_DISABLED),)

LOCAL_PATH := $(call my-dir)

include $(call all-subdir-makefiles)
#include $(call all-makefiles-under,$(LOCAL_PATH))
#include $(call first-makefiles-under, $(LOCAL_PATH))

endif

