# Fingerprint support on/off
PRODUCT_PROPERTY_OVERRIDES += ro.tinno.fingerprint.support=1

# Fingerprint XML configs
include $(TINNO_FINGERPRINT_PATH)/etc/finger_config/configs.mk

# Fingerprint permission
pers_src := $(TINNO_FINGERPRINT_PATH)/etc/permissions/android.hardware.fingerprint.xml
pers_dest := system/etc/permissions/android.hardware.fingerprint.xml
ifneq ($(wildcard $(pers_src)),)
    PRODUCT_COPY_FILES += $(pers_src):$(pers_dest)
else
    $(error $(pers_src) not exist!)
endif




