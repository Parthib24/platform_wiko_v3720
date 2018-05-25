
# SELinux Policy File Configuration
# mediatek/common/BoardConfig.mk
# -include $(TINNO_FINGERPRINT_SELINUX_SEPOLICY)

ifneq ($(strip $(TINNO_FINGERPRINT_SUPPORT)),)

    ifeq ($(strip $(TARGET_PRJ)),p6901)
        selinux_policy_finger := $(TINNO_FINGERPRINT_PATH)/sepolicy/msm8940
        $(warning ---selinux_policy_finger---$(selinux_policy_finger)---)
    else ifeq ($(strip $(TARGET_PRJ)),i9051)
        selinux_policy_finger := $(TINNO_FINGERPRINT_PATH)/sepolicy/msm8953
        $(warning ---selinux_policy_finger---$(selinux_policy_finger)---)
    else
        selinux_policy_finger := $(TINNO_FINGERPRINT_PATH)/sepolicy/$(TARGET_BOARD_PLATFORM)
        $(warning ---selinux_policy_finger---$(selinux_policy_finger)---)
    endif

    ifneq ($(wildcard $(selinux_policy_finger)/*.te),)
        BOARD_SEPOLICY_DIRS += $(selinux_policy_finger)
    endif

    BOARD_SEPOLICY_DIRS += $(TINNO_FINGERPRINT_PATH)/sepolicy/common

    $(warning ---BOARD_SEPOLICY_DIRS---$(BOARD_SEPOLICY_DIRS)---)

endif







