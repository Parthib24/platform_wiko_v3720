MY_DIR := $(TINNO_FINGERPRINT_PATH)/tp_back_touch/etc/
TEMP_PRJ_DIR := $(strip $(subst TINNO_PRJ_DIR =,,$(shell cat $(PWD)/build.ini |grep TINNO_PRJ_DIR)))
TEMP_TARGET_NAME := $(strip $(subst ro.target =,,$(shell cat $(PWD)/build.ini |grep ro.target)))

$(warning --TEMP_PRJ_DIR=$(TEMP_PRJ_DIR)---TEMP_TARGET_NAME=$(TEMP_TARGET_NAME)-----)
backtouch_config_file := backtouch_config.xml

ifneq ($(strip $(backtouch_config_file)),)
    ifneq ($(wildcard $(PWD)/$(TEMP_PRJ_DIR)/etc/$(backtouch_config_file)),)   
        backtouch_config_path := $(TEMP_PRJ_DIR)/etc/$(backtouch_config_file)
    else 
        backtouch_config_path := $(MY_DIR)/$(backtouch_config_file)
    endif
endif

$(warning backtouch_config_path---$(backtouch_config_path)-----)

ifneq ($(wildcard $(backtouch_config_path)),) 
    PRODUCT_COPY_FILES += $(backtouch_config_path):system/etc/backtouch_config.xml
else
    $(warning --err! finger_config.xml not found!-----)
endif


