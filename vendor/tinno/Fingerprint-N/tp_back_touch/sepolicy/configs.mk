
# SELinux Policy File Configuration
# mediatek/common/BoardConfig.mk
# -include $(TINNO_FINGERPRINT_SELINUX_SEPOLICY)

# TINNO_TP_BACKTOUCH_SUPPORT
selinux_policy_backtouch := $(TINNO_FINGERPRINT_PATH)/tp_back_touch/sepolicy/mt6737m

ifneq ($(wildcard $(selinux_policy_backtouch)/*.te),)
    BOARD_SEPOLICY_DIRS += $(selinux_policy_backtouch)
endif








