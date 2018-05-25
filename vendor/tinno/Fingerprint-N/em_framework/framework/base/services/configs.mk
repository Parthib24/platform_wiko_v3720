
MY_DIR := $(call my-dir)

ifneq ($(strip $(TINNO_FINGERPRINT_SUPPORT)),)

# Native source code.
my_source_files :=
my_source_files += $(call all-cpp-files-under, ../../../$(MY_DIR)/core/jni)
my_source_files += $(call all-c-files-under, ../../../$(MY_DIR)/core/jni)

LOCAL_SRC_FILES += $(my_source_files)
LOCAL_C_INCLUDES += ../../../$(MY_DIR)/core/jni

LOCAL_SHARED_LIBRARIES += libcutils libutils 
LOCAL_CFLAGS += -DTINNO_FINGERPRINT_SUPPORT

#$(warning $(LOCAL_PATH) : LOCAL_SRC_FILES += $(my_source_files))

endif

