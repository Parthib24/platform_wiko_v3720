#
# Elan.
ifneq ($(strip $(findstring elan, $(TINNO_FINGERPRINT_SUPPORT))),)
    elan_dir := $(TINNO_FINGERPRINT_PATH)/third-part/Elan/$(TARGET_BOARD_PLATFORM)
    $(warning $(elan_dir)/copy.mk)
    include $(elan_dir)/copy.mk
endif

# Goodix.
ifneq ($(strip $(findstring goodix, $(TINNO_FINGERPRINT_SUPPORT))),)
    goodix_dir := $(TINNO_FINGERPRINT_PATH)/third-part/Goodix/$(TARGET_BOARD_PLATFORM)
    $(warning $(goodix_dir)/copy.mk)
    include $(goodix_dir)/copy.mk
endif

# Silead.
ifneq ($(strip $(findstring silead, $(TINNO_FINGERPRINT_SUPPORT))),)
    silead_dir := $(TINNO_FINGERPRINT_PATH)/third-part/Silead/$(TARGET_BOARD_PLATFORM)
    $(warning $(silead_dir)/copy.mk)
    include $(silead_dir)/copy.mk
endif

# microtrust TEE.
ifeq ($(strip $(MICROTRUST_TEE_SUPPORT)),yes)
ifneq ($(strip $(TINNO_FINGERPRINT_SUPPORT)),)
    microtrust_dir := $(TINNO_FINGERPRINT_PATH)/third-part/Microtrust/$(TARGET_BOARD_PLATFORM)
    $(warning $(microtrust_dir)/copy.mk)
    include $(microtrust_dir)/arm/trust-app/copy.mk
    include $(microtrust_dir)/arm/teei/copy.mk
endif
endif

# CHIPONE.
ifneq ($(strip $(findstring chipone, $(TINNO_FINGERPRINT_SUPPORT))),)
    chipone_dir := $(TINNO_FINGERPRINT_PATH)/third-part/Chipone/$(TARGET_BOARD_PLATFORM)
    $(warning $(chipone_dir)/copy.mk)
    include $(chipone_dir)/copy.mk
endif


