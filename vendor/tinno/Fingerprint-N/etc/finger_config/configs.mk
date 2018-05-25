MY_DIR := $(TINNO_FINGERPRINT_PATH)/etc/finger_config
TEMP_PRJ_DIR := $(strip $(subst TINNO_PRJ_DIR =,,$(shell cat $(PWD)/build.ini |grep TINNO_PRJ_DIR)))
TEMP_TARGET_NAME := $(strip $(subst ro.target =,,$(shell cat $(PWD)/build.ini |grep ro.target)))


$(warning ---FINGERPRINT_LOCATION=$(FINGERPRINT_LOCATION)--TEMP_PRJ_DIR=$(TEMP_PRJ_DIR)---TEMP_TARGET_NAME=$(TEMP_TARGET_NAME)-----)

ifeq ($(strip $(FINGERPRINT_LOCATION)),front)
    finger_config_file := front_finger_config.xml

else ifeq ($(strip $(FINGERPRINT_LOCATION)),back)
    finger_config_file := back_finger_config.xml
 
else   
     finger_config_file := front_finger_config.xml
     $(warning ---finger_config_file set default front_finger_config.xml ---)
     ifeq ($(strip $(TEMP_TARGET_NAME)),v3971)
         finger_config_file := back_finger_config.xml
     else ifeq ($(strip $(TEMP_TARGET_NAME)),v3973)
         finger_config_file := back_finger_config.xml
     else ifeq ($(strip $(TEMP_TARGET_NAME)),v3961)
         finger_config_file := back_finger_config.xml
     else ifeq ($(strip $(TEMP_TARGET_NAME)),v3963)
         finger_config_file := back_finger_config.xml
     else ifeq ($(strip $(TEMP_TARGET_NAME)),p6901)
         finger_config_file := back_finger_config.xml
     endif  
    
endif

ifneq ($(strip $(finger_config_file)),)

    ifneq ($(wildcard $(PWD)/$(TEMP_PRJ_DIR)/etc/$(finger_config_file)),)   
        finger_config_path := $(TEMP_PRJ_DIR)/etc/$(finger_config_file)
    else 
        finger_config_path := $(MY_DIR)/$(finger_config_file)
    endif
    $(warning ---finger_config_path=$(finger_config_path)------)
endif


ifneq ($(wildcard $(finger_config_path)),) 
    PRODUCT_COPY_FILES += $(finger_config_path):system/etc/finger_config.xml
    $(warning PRODUCT_COPY_FILES += $(finger_config_path):system/etc/finger_config.xml)
else
    $(warning --err! finger_config.xml not found!-----)
endif

