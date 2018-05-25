# =============================================================================
#
# =============================================================================
LOCAL_PATH	:= $(call my-dir)


#second way----------------------------------prebuild libfp_daemon_impl_ca.a--------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := libfp_daemon_impl_ca
LOCAL_SRC_FILES := fp_lib/$(MODE)/libfp_daemon_impl_ca.a
include $(PREBUILT_STATIC_LIBRARY)



#--------------------------------------------QSEE libs for compile---------------------------------------------------------
include $(CLEAR_VARS)
LOCAL_MODULE := libQSEEComAPI
LOCAL_SRC_FILES := $(qsee_dev_kit)/qsee_client/libQSEEComAPI.so
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
include $(BUILD_PREBUILT)
include $(PREBUILT_SHARED_LIBRARY)



#--------------------------------------------fpsensor_fingerprint.default.so---------------------------------------------------------

include $(CLEAR_VARS)


# Module name
LOCAL_MODULE    := fpsensor_fingerprint.default

LOCAL_CFLAGS := 

LOCAL_C_INCLUDES += $(LOCAL_PATH) \
                    $(PATH_TA_OUT)/Public \
					$(qsee_dev_kit)/qsee_client
					


# Add your source files here (relative paths)
LOCAL_SRC_FILES    := fp_config_external_qsee.cpp \
                	  ta_entry/fp_tee_qsee.cpp

LOCAL_STATIC_LIBRARIES := fp_daemon_impl_ca

# Need the QSEE client library
LOCAL_SHARED_LIBRARIES := libQSEEComAPI 

#lzk add
LOCAL_CFLAGS += -Wall -std=c++11 -fexceptions -DLOG_TAG='"fpCoreHalJni"'
LOCAL_LDLIBS := -llog -ljnigraphics
#lzk add end

LOCAL_CFLAGS += -DFP_TEE_QSEE4=1
ifeq ($(MODE), Debug)
LOCAL_CFLAGS += -DDEBUG_ENABLE=1
else
LOCAL_CFLAGS += -DDEBUG_ENABLE=0
endif

include $(BUILD_SHARED_LIBRARY)

