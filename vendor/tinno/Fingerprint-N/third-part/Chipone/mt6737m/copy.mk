# Copy CA library
so_name := fingerprint.chipone.default.so
so_lib_dir := $(chipone_dir)/arm
PRODUCT_COPY_FILES += $(so_lib_dir)/$(so_name):system/lib/hw/$(so_name)

