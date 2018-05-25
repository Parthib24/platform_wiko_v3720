ifneq ($(strip $(findstring msm8937, $(TARGET_BOARD_PLATFORM))),)
  include $(call all-subdir-makefiles)
endif

