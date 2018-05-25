so_name := fingerprint.elan.default.so
so_lib_dir := $(elan_dir)/arm64/IC

ifneq ($(strip $(findstring elan_519c, $(TINNO_FINGERPRINT_SUPPORT))),)
$(warning PRODUCT_COPY_FILES += $(so_lib_dir)/elan_519c/$(so_name):system/lib64/hw/$(so_name))
PRODUCT_COPY_FILES += $(so_lib_dir)/elan_519c/$(so_name):system/lib64/hw/$(so_name)
endif

ifneq ($(strip $(findstring elan_96sa, $(TINNO_FINGERPRINT_SUPPORT))),)
$(warning PRODUCT_COPY_FILES += $(so_lib_dir)/elan_96sa/$(so_name):system/lib64/hw/$(so_name))
PRODUCT_COPY_FILES += $(so_lib_dir)/elan_96sa/$(so_name):system/lib64/hw/$(so_name)
endif
