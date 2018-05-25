package com.ape.backtouch.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by linzhiqin on 9/19/16.
 */
public class SharePreferenceUtil {
    private final static String TAG = "SharePreferenceUtil";

    private final static String BT_MENU_PATH = "bt_menu";
    public final static String BT = "bt";
    public final static String BT_CALL = "bt_call";
    public final static String BT_ROTATE = "bt_rotate";
    public final static String BT_PHOTOS = "bt_photos";
    public final static String BT_CAMERA = "bt_camera";
    public Context mContext;

    public SharePreferenceUtil(Context mContext){
        this.mContext = mContext;
    }

    public void printMenuLog(){
        Log.d(TAG,"BT = " + getSharePreference(BT) + "\n"
                + "BT_CAMERA = " + getSharePreference(BT_CAMERA) + "\n"
                + "BT_PHOTOS = " + getSharePreference(BT_PHOTOS) + "\n"
                + "BT_ROTATE = " + getSharePreference(BT_ROTATE) + "\n"
                + "BT_CALL = " + getSharePreference(BT_CALL) + "\n");
    }

    public void putSharePreferencce(String menuItem,boolean value){
        SharedPreferences mSharedPreferences =  mContext.getSharedPreferences(BT_MENU_PATH,Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(menuItem, value);
        editor.commit();
    }

    public boolean getSharePreference(String menuItem){
        SharedPreferences mSharedPreferences= mContext.getSharedPreferences(BT_MENU_PATH, Activity.MODE_PRIVATE);
        boolean value =mSharedPreferences.getBoolean(menuItem, true);
        return value;
    }
}
