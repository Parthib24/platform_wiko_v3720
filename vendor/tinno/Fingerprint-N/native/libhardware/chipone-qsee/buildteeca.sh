#!/bin/bash

# run NDK build
source setup.sh
ANDROID=${ANDROID:-"7"}

export SUPPORT_TEE_INTERNAL_STORAGE="true"
export SUPPORT_NAV_REPORT_IOCTL="false"
export TEE=${TEE:-"qsee"}

$NDK_PATH/ndk-build -B \
        NDK_APPLICATION_MK=Application.mk \
        APP_BUILD_SCRIPT=Android_a.mk \
        NDK_PROJECT_PATH=./ \
        NDK_OUT=./out/obj  \
        NDK_LIBS_OUT=./out/libs \
        COMPILE_FINGERPRINT_ENV="ENV_TEE" \
        COMPILE_FINGERPRINT_MODE="HAL" \
        TARGET_ANDRIOD_VERSION=$ANDROID
