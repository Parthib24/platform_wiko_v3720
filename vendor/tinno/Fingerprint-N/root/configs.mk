# init.rc
PRODUCT_COPY_FILES += $(TINNO_FINGERPRINT_PATH)/root/init.fingerprint.rc:root/init.fingerprint.rc

ifneq ($(strip $(MTK_TARGET_PROJECT)),)
PRODUCT_COPY_FILES += $(TINNO_FINGERPRINT_PATH)/root/fingerprint_ftm.rc:root/fingerprint_ftm.rc
endif


