# Add new src files
MY_DIR := $(call my-dir)

my_source_files := 

my_source_files += $(call all-java-files-under, ../../../$(MY_DIR)/new_add)
#$(warning $(LOCAL_PATH) : LOCAL_SRC_FILES += $(my_source_files))
LOCAL_SRC_FILES += $(my_source_files)

# Add new res files
LOCAL_RESOURCE_DIR += $(MY_DIR)/new_add/res
#$(warning $(LOCAL_PATH) : LOCAL_RESOURCE_DIR += $(LOCAL_RESOURCE_DIR))





