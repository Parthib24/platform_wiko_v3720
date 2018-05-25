
ifneq ($(strip $(findstring goodix_316m, $(TINNO_FINGERPRINT_SUPPORT))),)
COPY_PATH := $(goodix_dir)/arm64/goodix_316m

copy_files += \
    $(COPY_PATH)/fingerprint.goodix.default.so:system/lib64/hw/fingerprint.goodix.default.so \
    $(COPY_PATH)/gxfingerprint.default.so:system/lib64/hw/gxfingerprint.default.so \
    $(COPY_PATH)/libfp_client.so:system/lib64/libfp_client.so \
    $(COPY_PATH)/libfpservice.so:system/lib64/libfpservice.so \
    $(COPY_PATH)/libfpnav.so:system/lib64/libfpnav.so \
    $(COPY_PATH)/gx_fpd:system/bin/gx_fpd

$(warning $(copy_files))
PRODUCT_COPY_FILES += $(copy_files)
endif

ifneq ($(strip $(findstring goodix_3208qsee, $(TINNO_FINGERPRINT_SUPPORT))),)
COPY_PATH := $(goodix_dir)/arm64/goodix_3208qsee

copy_files += \
    $(COPY_PATH)/fingerprint.goodix.default.so:system/lib64/hw/fingerprint.goodix.default.so \
    $(COPY_PATH)/libgoodixfingerprintd_binder.so:system/lib64/libgoodixfingerprintd_binder.so \
    $(COPY_PATH)/libgf_algo.so:system/lib64/libgf_algo.so \
    $(COPY_PATH)/libgf_ca.so:system/lib64/libgf_ca.so \
    $(COPY_PATH)/libgf_hal.so:system/lib64/libgf_hal.so \
    $(COPY_PATH)/gx_fpd:system/bin/gx_fpd

$(warning $(copy_files))
PRODUCT_COPY_FILES += $(copy_files)
endif

