
ifneq ($(strip $(findstring goodix_gf5216, $(TINNO_FINGERPRINT_SUPPORT))),)
COPY_PATH := $(goodix_dir)/arm64/goodix_gf5216

copy_files += \
    $(COPY_PATH)/fingerprint.goodix.default.so:system/lib64/hw/fingerprint.goodix.default.so \
    $(COPY_PATH)/gxfingerprint.default.so:system/lib64/hw/gxfingerprint.default.so \
    $(COPY_PATH)/libfp_client.so:system/lib64/libfp_client.so \
    $(COPY_PATH)/libfpservice.so:system/lib64/libfpservice.so \
    $(COPY_PATH)/gx_fpd:system/bin/gx_fpd

$(warning $(copy_files))
PRODUCT_COPY_FILES += $(copy_files)
endif


