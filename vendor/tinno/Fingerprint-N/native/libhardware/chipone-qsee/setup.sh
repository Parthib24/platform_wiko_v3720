#!/bin/bash

rm -rf out
rm -rf fp_lib_5
rm -rf fp_lib_6
rm -rf fp_lib_7
export SO_GIT_BRANCH=`git describe --contains --all HEAD`
export SO_COMMIT_ID=`git rev-parse --short HEAD`
export MODE=${MODE:-"Debug"}
export WHOAMI=`whoami`
export NDK_PATH=/opt/android-ndk-r10e
