
# include $(BUILD_TINNO_EM_FRAMEWORK)

build_em_path := frameworks/../$(TINNO_FINGERPRINT_PATH)/em_framework
BUILD_TINNO_EM_FRAMEWORK := $(build_em_path)/common.mk

ifneq ($(strip $(TINNO_FINGERPRINT_SUPPORT)),)
# Verno.

ifeq ($(strip $(PLATFORM_VERSION)),7.0)
    fp_buildverno := $(PLATFORM_VERSION).05
else
    fp_buildverno := $(PLATFORM_VERSION).01
endif

# Build time.
fp_buildtime := $(shell $(DATE) +%Y%m%d_%H:%M)
$(warning verno: $(fp_buildverno)_$(fp_buildtime))

PRODUCT_PROPERTY_OVERRIDES += \
    ro.fp.framework.verno=$(fp_buildverno)_$(fp_buildtime)

endif# TINNO_FINGERPRINT_SUPPORT

# Resource overlay.
res_Overlay := $(TINNO_FINGERPRINT_PATH)/em_framework/overlay_res/$(PLATFORM_VERSION)
ifneq ($(wildcard $(res_Overlay)),)
    PRODUCT_PACKAGE_OVERLAYS += $(res_Overlay)
else
    $(error PLATFORM_VERSION == ?)
endif

