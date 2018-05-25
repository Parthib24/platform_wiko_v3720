# msm89xx
ifneq ($(strip $(findstring msm89, $(TARGET_BOARD_PLATFORM))),)

$(warning Start compile factory: $(TARGET_BOARD_PLATFORM))

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_OWNER := qti
LOCAL_PROPRIETARY_MODULE := true

LOCAL_SRC_FILES := \
fingerprint_ffbm.cpp \
goodix_ffbm.cpp \
elan_ffbm.cpp \
silead_ffbm.cpp \
chipone_ffbm.cpp


LOCAL_MODULE := libmmi_fingerprint
LOCAL_CLANG := false
LOCAL_MODULE_TAGS := optional

#LOCAL_CFLAGS := -Wall

LOCAL_C_INCLUDES := \
external/libcxx/include \
$(QC_PROP_ROOT)/fastmmi/libmmi \
$(QC_PROP_ROOT)/diag/include \
$(QC_PROP_ROOT)/diag/src/ \
$(TARGET_OUT_HEADERS)/common/inc 


LOCAL_SHARED_LIBRARIES := \
libmmi \
libcutils \
libc++ \
libutils \
libdiag


LOCAL_C_INCLUDES += $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr/include
ifeq ($(TARGET_COMPILE_WITH_MSM_KERNEL),true)
  LOCAL_ADDITIONAL_DEPENDENCIES := $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr
endif

include $(BUILD_SHARED_LIBRARY)

endif #TARGET_BOARD_PLATFORM
