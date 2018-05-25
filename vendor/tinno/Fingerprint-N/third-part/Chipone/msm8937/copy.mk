
ifneq ($(strip $(findstring chipone_7152qsee, $(TINNO_FINGERPRINT_SUPPORT))),)

so_name := fingerprint.chipone.default.so
so_lib_dir := $(chipone_dir)/arm64/chipone_7152qsee

ifeq ($(strip $(FP_OPEN_SOURCE_CHIPONE)),yes)
PRODUCT_PACKAGES += fingerprint.chipone.default
else
copy_files += \
    $(so_lib_dir)/$(so_name):system/lib64/hw/$(so_name)
endif

$(warning $(copy_files))
PRODUCT_COPY_FILES += $(copy_files)
endif
