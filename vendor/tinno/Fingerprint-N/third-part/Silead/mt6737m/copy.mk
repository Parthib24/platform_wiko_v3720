
ifneq ($(strip $(findstring silead_6163, $(TINNO_FINGERPRINT_SUPPORT))),)

SILEAD_DEST_PATH := $(silead_dir)/arm/silead_6163/prebuiltlibs
include $(SILEAD_DEST_PATH)/slfpinstall.mk
$(warning ----include $(SILEAD_DEST_PATH)/slfpinstall.mk----)

PRODUCT_PACKAGES += \
    fingerprint.silead.default

endif


