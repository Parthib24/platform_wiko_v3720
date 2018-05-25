package com.ape.emFramework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.os.UserHandle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.content.pm.PackageManager;
import android.os.SystemProperties;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;  
import android.os.UserManager;
import android.content.pm.UserInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import android.os.PowerManager;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.pm.ActivityInfo;
import android.os.RemoteException;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import android.view.View;
import android.graphics.Color;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import com.google.android.collect.Lists;
import com.android.internal.widget.LockPatternUtils;
import java.lang.reflect.Method;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.app.Activity;

import java.util.List;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import android.os.UserHandle;
import android.app.Service;
import android.os.RemoteException;
import android.provider.Settings;
import android.content.ContentResolver;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.hardware.fingerprint.ApeFpData;

public class BtFrameworkStatic {
   
private static final String TAG = "BtFrameworkStatic";

public static final String KEY_BACK_TOUCH = "back_touch";
public static final String KEY_BACK_TOUCH_OPEN_CAMERA = "back_touch_open_camera";
public static final String KEY_BACK_TOUCH_TAKE_PHOTOS = "back_touch_take_photos";
public static final String KEY_BACK_TOUCH_CHANGE_SCREEN = "back_touch_change_screen";
public static final String KEY_BACK_TOUCH_CALL = "back_touch_call";

public static final void setBackTouchState(String key,boolean value) {
    Log.i(TAG, "setBackTouchState: key = " + key + " value = " + value);  

    if (value) {
        SystemProperties.set(getBackTouchPro(key), "1"); 
    } else {
        SystemProperties.set(getBackTouchPro(key), "0"); 
    }
}

public static final boolean getBackTouchState(String key) {  
    boolean value = SystemProperties.get(getBackTouchPro(key), "1").equals("1");

    Log.i(TAG, "getBackTouchState: key = " + key + " value = " + value);
	
    return value;
}

private static final String getBackTouchPro(String key) {
    String pro = "";
    switch (key) {
        case KEY_BACK_TOUCH:
             pro = "persist.tinno.bt";
             break;
        case KEY_BACK_TOUCH_OPEN_CAMERA:
             pro = "persist.tinno.bt.opencamera";
             break;
        case KEY_BACK_TOUCH_TAKE_PHOTOS:
             pro = "persist.tinno.bt.capture";
             break;
        case KEY_BACK_TOUCH_CHANGE_SCREEN:
             pro = "persist.tinno.bt.screen";
             break;
        case KEY_BACK_TOUCH_CALL:
             pro = "persist.tinno.bt.call";
             break;
    }  
    return pro;
}
	
}
