# Add new src files
MY_DIR := $(call my-dir)
my_source_files :=
my_source_files += $(call all-java-files-under, ../../$(MY_DIR)/new_add)
LOCAL_SRC_FILES += $(my_source_files)

#$(warning $(LOCAL_PATH) : LOCAL_SRC_FILES += $(my_source_files))


