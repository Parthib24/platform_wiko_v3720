ifneq ($(strip $(findstring silead_6163, $(TINNO_FINGERPRINT_SUPPORT))),)
  include $(call all-subdir-makefiles)
endif

