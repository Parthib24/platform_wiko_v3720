# Copyright Silead
# By Warren Zhao

ifeq ($(SL_FPSYS_DISABLED),)

# Add by sileadinc begin
###both|64|empty
SL_LIB_COMBO:=
###64|empty
SL_BIN_COMBO:=
###both|64|empty
SL_TST_COMBO:=
#tbase gp
SL_TZ_COMBO:=beanpod
SL_LIB_CP_HWLIB:=false
# Add by sileadinc end

SL_INSTALL_FILES_PATH := $(SILEAD_DEST_PATH)
$(warning ----$(SL_INSTALL_FILES_PATH)----)

SL_INSTALL_DIR := libs

## Has moved to device.mk
###both|64|empty
#SL_LIB_COMBO:=both
###64|empty
#SL_BIN_COMBO:=64
###both|64|empty
#SL_TST_COMBO:=both
#tbase gp 
#SL_TZ_COMBO:=tbase

#SL_BOARD_COMBO:=mtk
#SL_LIB_CP_HWLIB :=false

ifeq (1,0)
ifeq ($(SL_LIB_COMBO),both)

ifeq ($(TARGET_2ND_ARCH),)
$(error wrong SL_LIB_COMBO:$(SL_LIB_COMBO) values)
endif

else ifeq ($(SL_LIB_COMBO),64)

ifeq ($(filter arm64,$(TARGET_ARCH) $(TARGET_2ND_ARCH)),)
$(error wrong SL_LIB_COMBO:$(SL_LIB_COMBO) values)
endif

else

ifeq ($(filter arm,$(TARGET_ARCH) $(TARGET_2ND_ARCH)),)
$(error wrong SL_LIB_COMBO:$(SL_LIB_COMBO) values)
endif

endif


ifneq ($(SL_LIB_COMBO),both)

ifeq ($(SL_TST_COMBO),|both)
$(error wrong SL_TST_COMBO:$(SL_TST_COMBO) values)
else ifeq ($(SL_LIB_COMBO)|$(SL_TST_COMBO),64|)
$(error wrong SL_TST_COMBO:$(SL_TST_COMBO) values)
else ifeq ($(SL_LIB_COMBO)|$(SL_TST_COMBO),|64)
$(error wrong SL_TST_COMBO:$(SL_TST_COMBO) values)
endif

ifeq ($(SL_LIB_COMBO)|$(SL_BIN_COMBO),64|)
$(error wrong SL_BIN_COMBO:$(SL_BIN_COMBO) values)
else ifeq ($(SL_LIB_COMBO)|$(SL_BIN_COMBO),|64)
$(error wrong SL_BIN_COMBO:$(SL_BIN_COMBO) values)
endif

endif#SL_LIB_COMBO

endif

WZ_LOCAL_EXTERNAL_TZ_LIBS := \
				libMcClient.so \
				libc.so \
				libm.so \
				libstdc++.so \
				libdl.so \
				libcrypto.so \
				libcutils.so \
				libc++.so \
                libQSEEComAPI.so \
                libteec.so

ifeq ($(SL_LIB_COMBO),both)
slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/lib64)
slfp_intall_files := $(filter-out $(WZ_LOCAL_EXTERNAL_TZ_LIBS),   $(slfp_intall_files))
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/lib64/$(f):system/lib64/$(f))
slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/lib)
slfp_intall_files := $(filter-out $(WZ_LOCAL_EXTERNAL_TZ_LIBS),   $(slfp_intall_files))
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/lib/$(f):system/lib/$(f))
else
slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/lib$(SL_LIB_COMBO))
slfp_intall_files := $(filter-out $(WZ_LOCAL_EXTERNAL_TZ_LIBS),   $(slfp_intall_files))
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/lib$(SL_LIB_COMBO)/$(f):system/lib$(SL_LIB_COMBO)/$(f))
endif

slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/bin$(SL_BIN_COMBO))
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/bin$(SL_BIN_COMBO)/$(f):system/bin/$(f))

slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/etc/silead)
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/etc/silead/$(f):system/etc/silead/$(f))

## Fix copy sysparms/silead_config.xml fail start
#slfp_intall_files_dirs := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/etc/silead/sysparms)
#PRODUCT_COPY_FILES += $(foreach d,$(slfp_intall_files_dirs),$(foreach f,$(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/etc/silead/sysparms/$(d)),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/etc/silead/sysparms/$(d)/$(f):system/etc/silead/sysparms/$(d)/$(f)))

slfp_intall_files_tmps := $(shell find $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/etc/silead/sysparms -type f)
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files_tmps),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/etc/silead/sysparms/$(shell echo $(f)|awk -F"sysparms/" '{print $$2}'):system/etc/silead/sysparms/$(shell echo $(f)|awk -F"sysparms/" '{print $$2}'))
## Fix copy sysparms/silead_config.xml fail end

ifeq ($(SL_TST_COMBO),both)
slfp_intall_files_dirs := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test64)
PRODUCT_COPY_FILES += $(foreach d,$(slfp_intall_files_dirs),$(foreach f,$(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test64/$(d)),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test64/$(d)/$(f):system/bin/test64/$(d)/$(f)))
slfp_intall_files_dirs := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test)
PRODUCT_COPY_FILES += $(foreach d,$(slfp_intall_files_dirs),$(foreach f,$(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test/$(d)),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test/$(d)/$(f):system/bin/test/$(d)/$(f)))
else
slfp_intall_files_dirs := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test$(SL_TST_COMBO))
PRODUCT_COPY_FILES += $(foreach d,$(slfp_intall_files_dirs),$(foreach f,$(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test$(SL_TST_COMBO)/$(d)),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test$(SL_TST_COMBO)/$(d)/$(f):system/bin/test$(SL_TST_COMBO)/$(d)/$(f)))
endif

## For cloudtest
ifeq ($(SL_CLOUD_TEST_SUPPORT),yes)
slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test$(SL_BIN_COMBO)/cloudtestsuited)
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/test$(SL_BIN_COMBO)/cloudtestsuited/$(f):system/bin/$(f))
endif

ifeq ($(SL_TZ_COMBO),tbase)
slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)_$(SL_TZ_COMBO)/bin/)
slfp_intall_files := $(filter %.tabin,$(slfp_intall_files))
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)_$(SL_TZ_COMBO)/bin/$(f):system/app/mcRegistry/$(f))
else 
ifeq ($(SL_TZ_COMBO),watch)
slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)_$(SL_TZ_COMBO)/bin/)
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)_$(SL_TZ_COMBO)/bin/$(f):cache/data/tee/$(f))
endif
ifeq ($(SL_TZ_COMBO),beanpod)
PRODUCT_COPY_FILES += $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)_$(SL_TZ_COMBO)/bin/fpsvcd_ta_beanpod.elf:system/vendor/thh/fp_server_silead
#PRODUCT_COPY_FILES += $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)_$(SL_TZ_COMBO)/bin/fpsvcd_ta_beanpod.elf:data/thh/tee_05/tee
endif
ifeq ($(SL_TZ_COMBO),qsee)
slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)_$(SL_TZ_COMBO)/qsee_ta/)
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)_$(SL_TZ_COMBO)/qsee_ta/$(f):system/vendor/firmware/$(f))
endif
endif

ifeq ($(SL_LIB_CP_HWLIB),true)
ifeq ($(SL_LIB_COMBO),both)
slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/hw/lib64)
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/hw/lib64/$(f):system/lib64/hw/$(f))
slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/hw/lib)
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/hw/lib64/$(f):system/lib/hw/$(f))
else
slfp_intall_files := $(shell ls $(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/hw/lib$(SL_LIB_COMBO))
PRODUCT_COPY_FILES += $(foreach f,$(slfp_intall_files),$(SL_INSTALL_FILES_PATH)/$(SL_INSTALL_DIR)/hw/lib$(SL_LIB_COMBO)/$(f):system/lib$(SL_LIB_COMBO)/hw/$(f))
endif
endif

endif
