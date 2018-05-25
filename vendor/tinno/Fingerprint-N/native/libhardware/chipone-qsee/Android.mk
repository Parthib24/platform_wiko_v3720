ifeq ($(strip $(FP_OPEN_SOURCE_CHIPONE)),yes)
ifneq ($(strip $(findstring chipone_7152qsee, $(TINNO_FINGERPRINT_SUPPORT))),)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#LOCAL_SRC_FILES := $(call all-cpp-files-under, .)

LOCAL_SRC_FILES := \
	fp_hal.cpp \
	fp_extension/fp_extension.cpp \
	fp_daemon_native.cpp \
	fp_daemon_impl.cpp \
	fp_tac_impl.cpp \
	jni_util.cpp \
	fp_thread_task.cpp \
	fp_config.cpp\
	fp_auth_statistical.cpp \
	fp_bmp.cpp \
	fp_json.cpp \
	fp_algo_p_lib_wrapper.cpp \
	fp_xml_parser.cpp \
	fp_extension/extension_socket.cpp 

LOCAL_SRC_FILES += \
	ta_entry/fp_input.cpp \
	ta_entry/fpTa_entryproxy.cpp \
	ta_entry/fpTa_commandprocessor.cpp \
	ta_entry/fpGpioHal.cpp \
	ta_entry/fp_nav.cpp \
	ta_entry/fpTestIntfTaImpl.cpp

LOCAL_SRC_FILES += \
	ca/Locals/Code/ta_entry/fp_tee_qsee.cpp \
	ca/Locals/Code/fp_config_external_qsee.cpp

LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE) \
	$(LOCAL_PATH)/include \
	$(LOCAL_PATH)/ta_ree_fake \
	$(LOCAL_PATH)/ta_proxy_base \
	$(LOCAL_PATH)/fp_extension \
	$(LOCAL_PATH)/ta_entry \
	$(LOCAL_PATH)

LOCAL_C_INCLUDES += \
	$(LOCAL_PATH)/qsee_client \
	$(LOCAL_PATH)/ca/Locals/Code

LOCAL_CFLAGS += \
	-Wall -std=c++11 -fexceptions \
	-DLOG_TAG='"fpCoreHalJni"' \
	-DWHOCOMPILE='"$(WHOAMI)"' \
	-DGIT_BRANCH_FROM_BUILD='"$(SO_GIT_BRANCH)"' \
	-DCOMMIT_ID_FROM_BUILD='"$(SO_COMMIT_ID)"'

LOCAL_CFLAGS += \
	-D__DATE__="\"$(BUILD_DATETIME_C_DATE)\"" \
	-D__TIME__=\"$(BUILD_DATETIME_C_TIME)\"

$(warning --LOCAL_SRC_FILES-- $(LOCAL_SRC_FILES) --LOCAL_C_INCLUDES-- $(LOCAL_C_INCLUDES) --LOCAL_CFLAGS-- $(LOCAL_CFLAGS))

LOCAL_CFLAGS += \
    -DENV_TEE -DTEE_NAME='"$(TEE)"' \
    -DFEATURE_TEE_STORAGE \
    -DTARGET_ANDROID=7 \
    -DFP_TEE_QSEE4=1

LOCAL_CFLAGS += -DDEBUG_ENABLE

LOCAL_SHARED_LIBRARIES += libQSEEComAPI
LOCAL_SHARED_LIBRARIES += liblog libcutils

LOCAL_MODULE := fingerprint.chipone.default

LOCAL_MODULE_RELATIVE_PATH := hw
include $(BUILD_SHARED_LIBRARY)

endif

