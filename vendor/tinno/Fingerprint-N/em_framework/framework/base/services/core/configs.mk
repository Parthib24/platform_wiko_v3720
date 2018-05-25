# Del default src files.
src_path := java/com/android/server/fingerprint

patsubst_src_files := \
$(src_path)/FingerprintUtils.java \
$(src_path)/EnrollClient.java \
$(src_path)/FingerprintsUserState.java \
$(src_path)/AuthenticationClient.java \
$(src_path)/ClientMonitor.java \
$(src_path)/EnumerateClient.java \
$(src_path)/RemovalClient.java \
$(src_path)/FingerprintService.java 

$(foreach f, $(patsubst_src_files), \
   $(eval LOCAL_SRC_FILES := \
   $(patsubst $(f),, $(LOCAL_SRC_FILES))) \
)

# Add new src files
MY_DIR := $(call my-dir)
my_source_files :=
my_source_files += $(call all-java-files-under, ../../../../$(MY_DIR)/new_add/common)
my_source_files += $(call all-java-files-under, ../../../../$(MY_DIR)/new_add/$(PLATFORM_VERSION))
my_source_files += $(call all-java-files-under, ../../../../$(MY_DIR)/java)

ifneq ($(strip $(MTK_TARGET_PROJECT)),)
    my_source_files += $(call all-java-files-under, ../../../../$(MY_DIR)/platform-depends/mtk)
else
    my_source_files += $(call all-java-files-under, ../../../../$(MY_DIR)/platform-depends/default)
endif

LOCAL_SRC_FILES += $(my_source_files)

#$(warning $(LOCAL_PATH) : LOCAL_SRC_FILES += $(my_source_files))




