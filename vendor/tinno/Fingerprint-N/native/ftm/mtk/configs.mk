
MY_DIR := $(call my-dir)

my_source_files :=
my_source_files += $(call all-cpp-files-under, ../../../../$(MY_DIR)/)
my_source_files += $(call all-c-files-under, ../../../../$(MY_DIR)/)

LOCAL_SRC_FILES += $(my_source_files)
LOCAL_C_INCLUDES += $(MY_DIR)

LOCAL_SHARED_LIBRARIES += libcutils libutils 
LOCAL_CFLAGS += -DTINNO_FINGERPRINT_SUPPORT

$(warning --add ftm fingerprint source--$(my_source_files))




















