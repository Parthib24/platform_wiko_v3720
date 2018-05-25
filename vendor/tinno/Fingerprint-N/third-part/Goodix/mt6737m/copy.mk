
ifneq ($(strip $(findstring goodix_gf5216, $(TINNO_FINGERPRINT_SUPPORT))),)
COPY_PATH := $(goodix_dir)/arm/goodix_gf5216

copy_files += \
    $(COPY_PATH)/fingerprint.goodix.default.so:system/lib/hw/fingerprint.goodix.default.so \
    $(COPY_PATH)/libgf_algo.so:system/lib/libgf_algo.so \
    $(COPY_PATH)/libgf_ca.so:system/lib/libgf_ca.so \
    $(COPY_PATH)/libgf_hal.so:system/lib/libgf_hal.so \
    $(COPY_PATH)/libgoodixfingerprintd_binder.so:system/lib/libgoodixfingerprintd_binder.so \
    $(COPY_PATH)/gx_fpd:system/bin/gx_fpd

$(warning $(copy_files))
PRODUCT_COPY_FILES += $(copy_files)
endif







