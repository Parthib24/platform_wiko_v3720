
MY_DIR := $(call my-dir)

ftm_config := $(MY_DIR)/$(TARGET_BOARD_PLATFORM)/configs.mk
ifneq ($(wildcard $(ftm_config)),)
    include $(MY_DIR)/$(TARGET_BOARD_PLATFORM)/configs.mk
else
    include $(MY_DIR)/mtk/configs.mk
endif













