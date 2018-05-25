# copy TA to system.img
#
copy_path := $(microtrust_dir)/arm/trust-app

TA := fp_server_elan
IC := 614r
trust_app := $(copy_path)/$(strip $(findstring elan_$(IC), $(TINNO_FINGERPRINT_SUPPORT)))/$(TA)
ifneq ($(wildcard $(trust_app)),)
    PRODUCT_COPY_FILES += $(trust_app):$(TARGET_COPY_OUT_VENDOR)/thh/$(TA):mtk
else
    $(warning PRODUCT_COPY_FILES += $(trust_app):$(TARGET_COPY_OUT_VENDOR)/thh/$(TA):mtk)
endif


TA := fp_server_elan
IC := 96sa
trust_app := $(copy_path)/$(strip $(findstring elan_$(IC), $(TINNO_FINGERPRINT_SUPPORT)))/$(TA)
ifneq ($(wildcard $(trust_app)),)
    PRODUCT_COPY_FILES += $(trust_app):$(TARGET_COPY_OUT_VENDOR)/thh/$(TA):mtk
else
    $(warning PRODUCT_COPY_FILES += $(trust_app):$(TARGET_COPY_OUT_VENDOR)/thh/$(TA):mtk)
endif


TA := fp_server_goodix
IC := gf5216
trust_app := $(copy_path)/$(strip $(findstring goodix_$(IC), $(TINNO_FINGERPRINT_SUPPORT)))/$(TA)
ifneq ($(wildcard $(trust_app)),)
    PRODUCT_COPY_FILES += $(trust_app):$(TARGET_COPY_OUT_VENDOR)/thh/$(TA):mtk
else
    $(warning PRODUCT_COPY_FILES += $(trust_app):$(TARGET_COPY_OUT_VENDOR)/thh/$(TA):mtk)
endif

TA := fp_server_chipone
IC := 7152
trust_app := $(copy_path)/$(strip $(findstring chipone_$(IC), $(TINNO_FINGERPRINT_SUPPORT)))/$(TA)
ifneq ($(wildcard $(trust_app)),)
    PRODUCT_COPY_FILES += $(trust_app):$(TARGET_COPY_OUT_VENDOR)/thh/$(TA):mtk
else
    $(warning PRODUCT_COPY_FILES += $(trust_app):$(TARGET_COPY_OUT_VENDOR)/thh/$(TA):mtk)
endif

