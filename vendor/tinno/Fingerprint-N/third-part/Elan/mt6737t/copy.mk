so_name := fingerprint.elan.default.so
so_lib_dir := $(elan_dir)/arm/IC

ifneq ($(strip $(findstring elan_614r, $(TINNO_FINGERPRINT_SUPPORT))),)
$(warning PRODUCT_COPY_FILES += $(so_lib_dir)/elan_614r/$(so_name):system/lib/hw/$(so_name))
PRODUCT_COPY_FILES += $(so_lib_dir)/elan_614r/$(so_name):system/lib/hw/$(so_name)
endif

ifneq ($(strip $(findstring elan_96sa, $(TINNO_FINGERPRINT_SUPPORT))),)
$(warning PRODUCT_COPY_FILES += $(so_lib_dir)/elan_96sa/$(so_name):system/lib/hw/$(so_name))
PRODUCT_COPY_FILES += $(so_lib_dir)/elan_96sa/$(so_name):system/lib/hw/$(so_name)
endif










