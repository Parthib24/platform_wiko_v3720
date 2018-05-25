package com.ape.backtouch.event;

import android.content.Context;
import android.util.Log;

import com.ape.backtouch.util.SharePreferenceUtil;

/**
 * Created by linzhiqin on 9/19/16.
 */
public class BtConfig {
    private final static String TAG = "BtConfig";
    private SharePreferenceUtil mSharePreferenceUtil;

    public BtConfig(Context mContext){
        mSharePreferenceUtil = new SharePreferenceUtil(mContext);
    }

    public void printBtConfigLog(){
        Log.d(TAG,"isBtEnable = " + isBtEnable() + "\n"
        + "isBtCallEnable = " + isBtCallEnable() + "\n"
        + "isBtCameraEnable = " + isBtCameraEnable() + "\n"
        + "isBtRoateEnable = " + isBtRoateEnable() + "\n"
        + "isBtPhotosEnable = " + isBtPhotosEnable() + "\n");
    }

    public boolean isBtEnable(){
        return mSharePreferenceUtil.getSharePreference(mSharePreferenceUtil.BT);
    }

    public boolean isBtCallEnable(){
        return mSharePreferenceUtil.getSharePreference(mSharePreferenceUtil.BT_CALL);
    }

    public boolean isBtCameraEnable(){
        return mSharePreferenceUtil.getSharePreference(mSharePreferenceUtil.BT_CAMERA);
    }

    public boolean isBtRoateEnable(){
        return mSharePreferenceUtil.getSharePreference(mSharePreferenceUtil.BT_ROTATE);
    }

    public boolean isBtPhotosEnable(){
        return mSharePreferenceUtil.getSharePreference(mSharePreferenceUtil.BT_PHOTOS);
    }
}
