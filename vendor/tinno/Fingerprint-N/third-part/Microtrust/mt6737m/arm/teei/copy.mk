#

soter := $(microtrust_dir)/arm/teei/soter.raw
ifneq ($(wildcard $(soter)),)
    PRODUCT_COPY_FILES += $(soter):$(TARGET_COPY_OUT_VENDOR)/thh/soter.raw:mtk
    $(warning PRODUCT_COPY_FILES += $(soter):$(TARGET_COPY_OUT_VENDOR)/thh/soter.raw:mtk)
else
    $(error not found : $(soter))
endif


init_thh := $(microtrust_dir)/arm/teei/init_thh
ifneq ($(wildcard $(init_thh)),)
    PRODUCT_COPY_FILES += $(init_thh):$(TARGET_COPY_OUT_VENDOR)/thh/init_thh:mtk
    $(warning PRODUCT_COPY_FILES += $(init_thh):$(TARGET_COPY_OUT_VENDOR)/thh/init_thh:mtk)
else
    $(error not found : $(init_thh))
endif





