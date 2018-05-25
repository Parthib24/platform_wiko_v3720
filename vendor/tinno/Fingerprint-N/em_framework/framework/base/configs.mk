# Del default src files.
src_path := core/java/android/hardware/fingerprint

patsubst_src_files := \
$(src_path)/IFingerprintServiceLockoutResetCallback.aidl \
$(src_path)/Fingerprint.aidl \
$(src_path)/IFingerprintServiceReceiver.aidl \
$(src_path)/FingerprintManager.java \
$(src_path)/IFingerprintService.aidl \
$(src_path)/Fingerprint.java \
$(src_path)/IFingerprintDaemonCallback.aidl \
$(src_path)/IFingerprintDaemon.aidl 

$(foreach f, $(patsubst_src_files), \
   $(eval LOCAL_SRC_FILES := \
   $(patsubst $(f),, $(LOCAL_SRC_FILES))) \
)

# Add new src files
MY_DIR := $(call my-dir)
my_source_files :=
my_source_files += $(call all-java-files-under, ../../$(MY_DIR)/new_add)
my_source_files += $(call all-java-files-under, ../../$(MY_DIR)/core)
my_source_files += $(call all-Iaidl-files-under, ../../$(MY_DIR)/core)
LOCAL_SRC_FILES += $(my_source_files)

#$(warning $(LOCAL_PATH) : LOCAL_SRC_FILES += $(my_source_files))




