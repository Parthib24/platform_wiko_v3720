ifneq ($(strip $(findstring silead, $(TINNO_FINGERPRINT_SUPPORT))),)
  include $(call all-subdir-makefiles)
endif

