#############################
# Copyright Silead
# By Warren Zhao
#############################
LOCAL_PATH := $(call my-dir)

#############################################
##FOR LOCAL EXTERNAL LIBS
#############################################
#local dependent libs
#module, including the path relative to root

define wz_include_lib_rule

include $(CLEAR_VARS)

LOCAL_MODULE       := $(basename $(1))
LOCAL_SRC_FILES	   := libs/lib/$(1)
LOCAL_SRC_FILES_64 := libs/lib64/$(1)
LOCAL_MODULE_CLASS := $(2)
LOCAL_MODULE_TAGS  := eng
ifeq ($(2),STATIC_LIBRARIES)
LOCAL_MODULE_SUFFIX := .a
else
LOCAL_MODULE_SUFFIX := .so
endif
#LOCAL_UNINSTALLABLE_MODULE := true
LOCAL_MULTILIB     := both
LOCAL_EXPORT_C_INCLUDE_DIRS := $(LOCAL_PATH)/libs/inc

#$(LOCAL_PATH)/libs/lib/$(1)	:	
#						$(SL_ROOT_DIR)/build/bin/mksmake -C $(SL_ROOT_DIR) all

#$(LOCAL_PATH)/libs/lib64/$(1):	
#						$(SL_ROOT_DIR)/build/bin/mksmake -C $(SL_ROOT_DIR) all

include $(BUILD_PREBUILT)

#$(basename $(1)) : $(1)
   
endef

define wz_add_lib_rule

    $(eval $(call wz_include_lib_rule,$(1),$(2)))
  
endef

#input is relative path to LOCAL_PATH
define wz_add_libs_rules
  
    $(foreach m,$(1),$(call wz_add_lib_rule,$m,$(2)))

endef

#############################

#WZ_LOCAL_EXTERNAL_SHARED_LIBS := libfpsvcd_remoteapi.a libslbase.a libfpcal.so libfactorylib.so 

WZ_LOCAL_EXTERNAL_TZ_LIBS := \
				libMcClient.so \
				libc.so \
				libm.so \
				libstdc++.so \
				libdl.so \
				libcrypto.so \
				libcutils.so \
				libc++.so \
                libQSEEComAPI.so \
                libteec.so
#add by wells begin
WZ_LOCAL_EXTERNAL_STATIC_LIBS :=$(notdir $(shell ls $(LOCAL_PATH)/libs/lib/*.a))
$(eval $(call wz_add_libs_rules,$(WZ_LOCAL_EXTERNAL_STATIC_LIBS),STATIC_LIBRARIES))
#add by wells end 

WZ_LOCAL_EXTERNAL_SHARED_LIBS := $(notdir $(shell ls $(LOCAL_PATH)/libs/lib/*.so))
WZ_LOCAL_EXTERNAL_SHARED_LIBS := $(filter-out $(WZ_LOCAL_EXTERNAL_TZ_LIBS),   $(WZ_LOCAL_EXTERNAL_SHARED_LIBS))
$(eval $(call wz_add_libs_rules,$(WZ_LOCAL_EXTERNAL_SHARED_LIBS),SHARED_LIBRARIES))

