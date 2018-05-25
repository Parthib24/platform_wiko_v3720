
ifdef MAKE_FPEVENT

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, .)

MY_DIR := ../../../../../../
LOCAL_SRC_FILES += \
     $(MY_DIR)/vendor/tinno/Fingerprint-N/em_framework/framework/base/core/java/android/hardware/fingerprint/FingerprintManager.java

LOCAL_CERTIFICATE := platform
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#LOCAL_PROGUARD_ENABLED:= disabled

LOCAL_JACK_ENABLED := disabled

#LOCAL_JAVA_LIBRARIES += framework
LOCAL_MODULE := hide_deps

include $(BUILD_STATIC_JAVA_LIBRARY)
include $(call all-makefiles-under,$(LOCAL_PATH))

endif

