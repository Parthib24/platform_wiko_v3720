# Del default src files.
src_path := src/com/android/settings/fingerprint

patsubst_src_files := \
$(src_path)/FingerprintSettings.java \
$(src_path)/FingerprintUiHelper.java \
$(src_path)/SetupFingerprintEnrollFinish.java \
$(src_path)/FingerprintLocationAnimationVideoView.java \
$(src_path)/SetupFingerprintEnrollEnrolling.java \
$(src_path)/FingerprintFindSensorAnimation.java \
$(src_path)/FingerprintEnrollEnrolling.java \
$(src_path)/FingerprintEnrollFinish.java \
$(src_path)/FingerprintEnrollIntroduction.java \
$(src_path)/FingerprintEnrollBase.java \
$(src_path)/FingerprintEnrollSidecar.java \
$(src_path)/SetupFingerprintEnrollIntroduction.java \
$(src_path)/FingerprintEnrollFindSensor.java \
$(src_path)/SetupFingerprintEnrollFindSensor.java \
$(src_path)/FingerprintLocationAnimationView.java \
$(src_path)/SetupSkipDialog.java 

$(foreach f, $(patsubst_src_files), \
   $(eval LOCAL_SRC_FILES := \
   $(patsubst $(f),,$(LOCAL_SRC_FILES))) \
)

# Add new src files
MY_DIR := $(call my-dir)

my_source_files := 
my_source_files += $(call all-java-files-under, ../../../$(MY_DIR)/$(PLATFORM_VERSION))
my_source_files += $(call all-java-files-under, ../../../$(MY_DIR)/new_add)
#$(warning $(LOCAL_PATH) : LOCAL_SRC_FILES += $(my_source_files))
LOCAL_SRC_FILES += $(my_source_files)

# Add new res files
ifeq ($(strip $(PLATFORM_VERSION)),7.1.1)
        LOCAL_RESOURCE_DIR += $(MY_DIR)/new_add/myos-res
endif
LOCAL_RESOURCE_DIR += $(MY_DIR)/new_add/res 
#$(warning $(LOCAL_PATH) : LOCAL_RESOURCE_DIR += $(LOCAL_RESOURCE_DIR))

LOCAL_STATIC_JAVA_LIBRARIES += fpShortcuts




