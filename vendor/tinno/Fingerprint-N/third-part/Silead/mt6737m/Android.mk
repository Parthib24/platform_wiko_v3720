ifneq ($(strip $(findstring mt6737, $(TARGET_BOARD_PLATFORM))),)
  include $(call all-subdir-makefiles)
endif

