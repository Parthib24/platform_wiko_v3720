# fingerprintd.
PRODUCT_PACKAGES += fingerprintd

# Qcom ftm, msm89xx.
ifneq ($(strip $(findstring msm89, $(TARGET_BOARD_PLATFORM))),)
PRODUCT_PACKAGES += libmmi_fingerprint
endif



















