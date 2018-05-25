/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ape.encryptmanager;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.Fingerprint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import com.android.settings.R;

import java.util.List;

//import android.util.Log;

import android.hardware.fingerprint.FingerprintManager;
import com.android.settings.fingerprint.FingerprintSettings;
import com.android.settings.fingerprint.FingerprintEnrollIntroduction;
import com.ape.encryptmanager.utils.EncryUtil;
import com.ape.emFramework.Log;
import com.ape.encryptmanager.service.EncryptServiceUtil;
import com.ape.encryptmanager.privacylock.EncryptCheckActivity;
import com.ape.encryptmanager.privacylock.WikoEncryptSettingActivity;
import com.ape.encryptmanager.quickaction.QuickActionFingerListActivity;
import com.ape.emFramework.EmFrameworkStatic;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.app.ActivityManager;
import android.content.pm.ResolveInfo;

public class FingerprintMainScreen extends PreferenceActivity implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "FingerprintMainScreen-cls";
    private static final String KEY_FINGERPRINT_ITEM = "fingerprint_item";
    private static final String KEY_APP_FILE_LOCK = "app_and_file_lock";
    private static final String KEY_QUICK_BOOT = "quick_boot";
    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String KEY_SENSOR_BUTTON = "sensor_button";
    private static final String KEY_FINGERPRINT_GATEGORY = "fingerprint_category";
    private static final String KEY_NAVIGATION_GATEGORY = "navigation_category";
    private static final String KEY_NAVIGATION_BAR_DISPLAY = "navigation_bar_display";
    private static final String KEY_NAVIGATION_BAR_TYPE = "navigation_bar_type";
    private static final String NAV_BAR_DIAPLAY_TYPE = "statusbar_nav_bar_type";
    private static final String NAVIGATION_BAR_STATUS = "navigation_bar_status";
    public static final String ADD_NAVIGATIONBAR_ACTION = "com.add.nar";
    public static final String REMOVE_NAVIGATIONBAR_ACTION = "com.remove.nar";

    public static final String KEY_PREFERENCE_APPLOCK = "pref_key_applock";
    public static final String KEY_PREFERENCE_XLOCKER = "pref_key_xlocker";
    private static final String XLOCKER_APP_PACKAGE_NAME = "com.qihoo360.mobilesafe.applock";

    private static final String KEY_FINGERPRINT_BACKTOUCH_ITEM = "fingerprint_backtouch_category";
    private static final String KEY_SENSOR_CAPTURE = EmFrameworkStatic.KEY_SENSOR_CAPTURE;
    private static final String KEY_SENSOR_CONTINUOUS_CAPTURE = EmFrameworkStatic.KEY_SENSOR_CONTINUOUS_CAPTURE;
    private static final String KEY_SENSOR_ANSWER_CALL = EmFrameworkStatic.KEY_SENSOR_ANSWER_CALL;
    private static final String KEY_SENSOR_STOP_ALARM = EmFrameworkStatic.KEY_SENSOR_STOP_ALARM;
    private static final String KEY_SENSOR_DISPLAY_NOTIFICATION = EmFrameworkStatic.KEY_SENSOR_DISPLAY_NOTIFICATION;
    private static final String KEY_SENSOR_SWITCH_PAGE = EmFrameworkStatic.KEY_SENSOR_SWITCH_PAGE;

    private static final int BACK_HOME_RECENT = 0;
    private static final int RECENT_HOME_BACK = 1;

    private Preference mPreferenceNavigationType;
    private SwitchPreference mPreferenceNavigationDisplay;
    public static final int MSG_APP_FILELOCK_REQ = 1;
    public static final int MSG_NAVIGATION_DISPLAY_ENABLED = 2;
    private Preference mFingerprintItem;
    private Preference mAppFileLock;
    private Preference mAppLcok;
    private Preference mXLcoker;
    private Preference mPreferenceQuickActions;

    private Preference mNavigationBar;

    PreferenceGroup mFingerCategory;
    private SwitchPreference mSensor_button;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    public static final String PRE_FILES = "pre_fingerprint";
    public static final String PRE_SENSOR_BUTTON_ONOFF = "pre_sensor_button_onoff";

    private SwitchPreference mSensor_capture;
    private SwitchPreference mSensor_shoot;
    private SwitchPreference mSensor_call;
    private SwitchPreference mSensor_alarm;
    private SwitchPreference mSensor_notification;
    private SwitchPreference mSensor_page;
    private boolean isSupportFpBacktouch = false;

    //private EncryptServiceUtil mEncryptServiceUtil;
    private Context mContext;
    //public  UpgradeManager mUpgradeManager;
    private boolean isCurActivityFront;
    //public static String APE_FINGERPRINT_APP_KEY="com.ape.encryptmanager";
    //public static String  APE_FINGERPRINT_APP_NAME="ApeEmail";
    private PackageManager mPackageManager;
    public static Context mContextExt;

    private final static boolean debug_6901 = false;

   private Thread mServiceInitThread;

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;
    mContextExt = this.getApplicationContext();
    addPreferencesFromResource(R.xml.fingerprint_main_screen);
    mPackageManager = getPackageManager();
    mFingerprintItem = (Preference) findPreference(KEY_FINGERPRINT_ITEM);
    mAppFileLock = (Preference) findPreference(KEY_APP_FILE_LOCK);
    mAppLcok = (Preference) findPreference(KEY_PREFERENCE_APPLOCK);
    mXLcoker = (Preference) findPreference(KEY_PREFERENCE_XLOCKER);
    mPreferenceQuickActions = (Preference) findPreference(KEY_QUICK_BOOT);
    mNavigationBar=(Preference)findPreference(KEY_NAVIGATION_GATEGORY);
    mSensor_button = (SwitchPreference) findPreference(KEY_SENSOR_BUTTON);
    mSensor_button.setOnPreferenceChangeListener(this);

    ActionBar actionBar=getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    boolean b = EmFrameworkStatic.getFpTouchState();
    mSensor_button.setChecked(b);
  
    initFpBtMenu();

    if (!isSupportFpBacktouch) {
        getPreferenceScreen().removePreference(findPreference(KEY_FINGERPRINT_BACKTOUCH_ITEM));  
    }

    PreferenceScreen root = getPreferenceScreen();
    mFingerCategory = (PreferenceGroup)root.findPreference(KEY_FINGERPRINT_GATEGORY);

    //restartEncryptServiceIfNeed();	
    if(!EmFrameworkStatic.isServiceRunning(mContext, EmFrameworkStatic.BINDER_CLASS)) {
     EmFrameworkStatic.startEncryptServiceForQB(mContext);
    } 

    if (!isFilelockExist()) {
        mFingerCategory.removePreference(mAppFileLock);
    }   
	
}

    private boolean isFilelockExist(){
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> packageInfos = getPackageManager().queryIntentActivities(intent, 0);

        for (int i = 0; i < packageInfos.size(); i++) {
            String launcherActivityName = packageInfos.get(i).activityInfo.name;
            String packageName = packageInfos.get(i).activityInfo.packageName;
            if ("com.ape.secrecy.EncryptBoxMain".equals(launcherActivityName) && "com.ape.filemanager".equals(packageName)) {
                Log.i(TAG, "isHaveFilelockExist is true");
                return true;
            }
        }
        return false;
    }

    //ymc add
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    //ymc end
    
    private void restartEncryptServiceIfNeed() {
        Log.i("EncryptService-class", "----restartEncryptServiceIfNeed----");		
        mServiceInitThread = new Thread(new Runnable() {
            @Override
            public void run() {
	         EmFrameworkStatic.startEncryptServiceForQB(mContext);
            }
        });		
        if(!EmFrameworkStatic.isServiceRunning(mContext, EmFrameworkStatic.BINDER_CLASS)) {
            mServiceInitThread.start();
	 }
    }	

    private void updateFingerPreferenceStatus(){
        int fingerCount = getFingerprintCount();
        Intent intent = new Intent();
        final String clazz;

        if(mFingerprintItem!=null){
            if(fingerCount > 0)
            {
                mFingerprintItem.setSummary(getResources().getQuantityString(
                        R.plurals.security_settings_fingerprint_preference_summary,
                        fingerCount, fingerCount));
                clazz = FingerprintSettings.class.getName();
                Log.i(TAG, "----FingerprintSettings----");
            }
            else
            {
                mFingerprintItem.setSummary(
                        R.string.security_settings_fingerprint_preference_summary_none);
                clazz = FingerprintEnrollIntroduction.class.getName();
                Log.i(TAG, "----FingerprintEnrollIntroduction----");
            }

            intent.setClassName("com.android.settings", clazz);
            mFingerprintItem.setIntent(intent);
        }

        if(mAppFileLock != null) {
            mAppFileLock.setEnabled(fingerCount > 0 ? true : false);
        }

        if(mPreferenceQuickActions != null) {
            mPreferenceQuickActions.setEnabled(fingerCount > 0 ? true : false);
        }
    }

    

@Override
public void onResume() {
    super.onResume();
    updateData();
     //Context.MODE_WORLD_READABLE ,MODE_WORLD_READABLE no longer supported
     mSharedPreferences = getSharedPreferences(PRE_FILES,
            Context.MODE_MULTI_PROCESS);
    /*if (mSharedPreferences != null) {
        boolean b = mSharedPreferences.getBoolean(PRE_SENSOR_BUTTON_ONOFF, true);
        mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(PRE_SENSOR_BUTTON_ONOFF, b);
        mEditor.commit();

        boolean b = EmFrameworkStatic.getFpTouchState();
        mSensor_button.setChecked(b);
    }*/
    boolean b = EmFrameworkStatic.getFpTouchState();
    mSensor_button.setChecked(b);

    updateFpBtMenu();

    //if(MainFeatureOptions.isNavigationBarSupported()) {
    if(mPreferenceNavigationDisplay != null) {
        if (getNavBarStatus() == 1) {
            mPreferenceNavigationDisplay.setSummary(R.string.switch_on);
            mPreferenceNavigationDisplay.setChecked(true);
            mPreferenceNavigationType.setEnabled(true);
        } else {
            mPreferenceNavigationDisplay.setSummary(R.string.switch_off);
            mPreferenceNavigationDisplay.setChecked(false);
            mPreferenceNavigationType.setEnabled(false);
            setSensorButtonStatus(true);
            mSensor_button.setEnabled(false);
        }

        if (getNavBarDisplayType() == BACK_HOME_RECENT) {
            mPreferenceNavigationType.setSummary(R.string.navigation_default);
        } else {
            mPreferenceNavigationType.setSummary(R.string.navigation_overturn);
        }
        mPreferenceNavigationDisplay.setEnabled(true);
    }
    /*********upgrade start************/
/*
    android.util.Log.d("xiaowen", "onResume----start upgrade");
    if (null == mUpgradeManager) {
        mUpgradeManager = UpgradeManager.newInstance(this, APE_FINGERPRINT_APP_KEY, getString(R.string.app_name));
    }
    //isCurActivityFront = true;

    mUpgradeManager.askForNewVersionBackgroundDelay(new CheckVersionBackgroundListener() {
        @Override
        public void onSuccess(boolean hasUpgrade, long flag) {
            android.util.Log.d("xiaowen","hasUpgrade:"+hasUpgrade);
            android.util.Log.d("xiaowen","isCurActivityFront:"+isCurActivityFront);
            if(hasUpgrade) {
                android.util.Log.d("xiaowen", "onResume----start upgrade--show dialog");
                mUpgradeManager.showBackgroundUpgradeDialog(FingerprintMainScreen.this,flag);
            }
        }
        @Override
        public void onFail(String s) {
            android.util.Log.d("xiaowen","onResume----start upgrade ---fail");
        }

    });
*/
    /**********upgrade end**********/
    if(mAppLcok !=null) {
        try {
            ApplicationInfo info = mPackageManager.getApplicationInfo("com.applock.tinno", 0);
            String name = mPackageManager.getApplicationLabel(info).toString();
            mAppLcok.setTitle(name);
        } catch (Exception e) {
            getPreferenceScreen().removePreference(mAppLcok);
            mAppLcok = null;
        }
            //for hide applock
            if (mAppLcok != null) {
                getPreferenceScreen().removePreference(mAppLcok);
                mAppLcok = null;
            }
    }
    if(mXLcoker != null) {
        try {
            int state= mPackageManager.getApplicationEnabledSetting(XLOCKER_APP_PACKAGE_NAME);
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                getPreferenceScreen().removePreference(mXLcoker);
            } else {
                getPreferenceScreen().addPreference(mXLcoker);
                ApplicationInfo info = mPackageManager.getApplicationInfo(XLOCKER_APP_PACKAGE_NAME, 0);
                String name = mPackageManager.getApplicationLabel(info).toString();
                mXLcoker.setTitle(name);
            }
        } catch (Exception e) {
            getPreferenceScreen().removePreference(mXLcoker);
            mXLcoker = null;
            Log.i(TAG, "onResume mXLcoker Exception:" + e.toString());
        }
    }

// for 6901 test
    if (debug_6901) {
        FingerprintManager mFpm = (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);
        mFpm.registerFpService(getPackageName(), new FingerprintManager.AppClientCallback() {
        
            @Override
            public boolean onFingerprintEvent(int e) {
                /*
                private final static int HW_EVT_MOVE_UP = 3;
                private final static int HW_EVT_MOVE_DOWN = 4;
                private final static int HW_EVT_MOVE_LEFT = 5;
                private final static int HW_EVT_MOVE_RIGHT = 6;
                private static final int SW_EVT_DOUBLE_CLICK = 10;
                private static final int SW_EVT_LONG_PRESS = 11;
                private static final int SW_EVT_GO_BACK = 12;
                */
                Log.i(TAG, "onFingerprintEvent:" + e);
                return true;
            }
        }, 0,1);
    }

    
}

private void updateData() {
    int fingerCount = getFingerprintCount();
    if(mAppFileLock != null) {
        mAppFileLock.setEnabled(fingerCount > 0 ? true : false);
    }
    if(mPreferenceQuickActions != null) { 
        mPreferenceQuickActions.setEnabled(fingerCount > 0 ? true : false); 
    } 

    if(mFingerprintItem != null){
        if(fingerCount > 0) {
            mFingerprintItem.setSummary(getResources().getQuantityString(
                R.plurals.security_settings_fingerprint_preference_summary,
                fingerCount, fingerCount));
        } 
        else {
            mFingerprintItem.setSummary(
                R.string.security_settings_fingerprint_preference_summary_none);
        }
    } 
}



@Override
public boolean onPreferenceChange(Preference preference, Object objValue) {
    final String key = preference.getKey();
    Log.i(TAG, "----onPreferenceChange:"+key);
    
    if(KEY_SENSOR_BUTTON.equals(key)){
        Log.i(TAG, "----onPreferenceChange:"+key + " | objValue = " + objValue);
        EmFrameworkStatic.setFpTouchState((boolean)objValue); 
        
        Intent intent=new Intent(EmFrameworkStatic.INTENT_ACTION_TOUCHSTATE_CHANGE);          
        this.sendBroadcastAsUser(intent, UserHandle.ALL, null);
    }
    else if(KEY_NAVIGATION_BAR_DISPLAY.equals(key)){
        setNavBarStatus((boolean)objValue ? 1:0);
        mPreferenceNavigationDisplay.setEnabled(false);
        mHandler.removeMessages(MSG_NAVIGATION_DISPLAY_ENABLED);
        Message msg = new Message();
        msg.what = MSG_NAVIGATION_DISPLAY_ENABLED;
        msg.obj = objValue;
        mHandler.sendMessageDelayed(msg, 500);
        if((boolean)objValue){
             mPreferenceNavigationDisplay.setSummary(R.string.switch_on);
             mPreferenceNavigationType.setEnabled(true);
             mSensor_button.setEnabled(true);
        }else{
            mPreferenceNavigationDisplay.setSummary(R.string.switch_off);
            mPreferenceNavigationType.setEnabled(false);
            setSensorButtonStatus(true);
            mSensor_button.setEnabled(false);
        }
    
    }

    switch (key) {
        case KEY_SENSOR_CAPTURE:
        case KEY_SENSOR_CONTINUOUS_CAPTURE:
        case KEY_SENSOR_ANSWER_CALL:
        case KEY_SENSOR_STOP_ALARM:
        case KEY_SENSOR_DISPLAY_NOTIFICATION:
        case KEY_SENSOR_SWITCH_PAGE:
             EmFrameworkStatic.setFpBackTouchState(key,(boolean)objValue);
             break;
    }
    
    return true;
}

@Override
public void onPause() {
    super.onPause();
    Log.i(TAG, "----onPause----");
    mHandler.removeMessages(MSG_NAVIGATION_DISPLAY_ENABLED);
    //isCurActivityFront = false;

    // for 6901 test
    if (debug_6901) {
        FingerprintManager mFpm = (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);
        mFpm.unregisterFpService(getPackageName());
    }
}

@Override
public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
     final String key = preference.getKey();
    Log.i(TAG, "----onPreferenceTreeClick:"+key);

    if(KEY_FINGERPRINT_ITEM.equals(key)){
        Intent intent = new Intent();
        final String clazz;
        if(getFingerprintCount() > 0) {
            clazz = FingerprintSettings.class.getName();
        } 
        else {
            clazz = FingerprintEnrollIntroduction.class.getName();
        }

        intent.setClassName(getPackageName(), clazz);
        startActivity(intent);
    }

    else if(KEY_APP_FILE_LOCK.equals(key)){
        Intent appIntent = new Intent();
        if(EncryptServiceUtil.getInstance(this).isMustSetPassword()) {
            appIntent.setClassName(getPackageName(), WikoEncryptSettingActivity.class.getName());     
            Bundle data = new Bundle();
            data.putInt(EncryUtil.REQ_KEY, EncryptCheckActivity.Other);
            appIntent.putExtras(data);
            startActivityForResult(appIntent,MSG_APP_FILELOCK_REQ);
        } 
        else {
            appIntent.setClassName(getPackageName(), EncryptCheckActivity.class.getName()); 
            Bundle data = new Bundle();
            data.putInt(EncryUtil.REQ_KEY, EncryptCheckActivity.Other);
            appIntent.putExtras(data);
            startActivityForResult(appIntent,MSG_APP_FILELOCK_REQ);
        }
    }
    else if(KEY_QUICK_BOOT.equals(key)){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
        intent.setClassName(getPackageName(), QuickActionFingerListActivity.class.getName());
        startActivity(intent);
    } else if(KEY_NAVIGATION_BAR_TYPE.equals(key)){
        MyAlertDialogView dialog = new MyAlertDialogView(mContext);
        dialog.showSelectDialog();

    }

    return super.onPreferenceTreeClick(preferenceScreen, preference);
}

@Override
public boolean onPreferenceClick(Preference preference) {
    return false;
}


@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (MSG_APP_FILELOCK_REQ == requestCode && data != null) {
		Bundle data2 = data.getExtras();
		if (data2 != null && data2.getString("RESULT_KEY") != null) {
			Intent intent = new Intent();
			intent.setClassName("com.android.settings",
					"com.ape.encryptmanager.privacylock.PrivacyLockActivity");
			startActivity(intent);
		}
	}

}

 private void setNavBarDisplayType(int type) {
        Settings.System.putInt(getContentResolver(), NAV_BAR_DIAPLAY_TYPE, type);
    }

    private int getNavBarDisplayType() {
        int type = Settings.System.getInt(getContentResolver(), NAV_BAR_DIAPLAY_TYPE, BACK_HOME_RECENT);
        return type;
    }

    private void setNavBarStatus(int type) {
        Settings.System.putInt(getContentResolver(), NAVIGATION_BAR_STATUS, type);
        if(type == 1){
            mContext.sendBroadcast(new Intent(ADD_NAVIGATIONBAR_ACTION));
        }else{
            mContext.sendBroadcast(new Intent(REMOVE_NAVIGATIONBAR_ACTION));
        }
    }

    private int getNavBarStatus() {
        int type = Settings.System.getInt(getContentResolver(), NAVIGATION_BAR_STATUS, 1);
        return type;
    }

    private void setSensorButtonStatus(boolean status){

        if(status == mSensor_button.isChecked()){
            return;
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(PRE_SENSOR_BUTTON_ONOFF, status);
        editor.commit();
        mSensor_button.setChecked(status);
    }

    public static void setSensorButtonAutoOpen(Context context){
        //Context.MODE_WORLD_READABLE ,MODE_WORLD_READABLE no longer supported
        SharedPreferences sharedPreferences = context.getSharedPreferences(PRE_FILES,
                Context.MODE_MULTI_PROCESS);
        boolean b = sharedPreferences.getBoolean(PRE_SENSOR_BUTTON_ONOFF, false);
        if(!b){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PRE_SENSOR_BUTTON_ONOFF, true);
            editor.commit();
        }

    }

private class MyAlertDialogView implements View.OnClickListener {
        private View mView;
        private Context mContext;
        private View mLayout1;
        private View mLayout2;
        private RadioButton mRadio1;
        private RadioButton mRadio2;
        private AlertDialog mAlertDialog;

        public MyAlertDialogView(Context context) {
            mContext = context;
            createAlertDialogView();
        }

        private View createAlertDialogView() {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            mView = inflater.inflate(R.xml.navigation_bar_select, null);
            mLayout1 = mView.findViewById(R.id.key_back_home_recent);
            mLayout2 = mView.findViewById(R.id.key_recent_home_back);

            mLayout1.setOnClickListener(this);
            mLayout2.setOnClickListener(this);
            mRadio1 = (RadioButton) mView.findViewById(R.id.key_back_home_recent_rb);
            mRadio2 = (RadioButton) mView.findViewById(R.id.key_recent_home_back_rb);
            mRadio1.setOnClickListener(this);
            mRadio2.setOnClickListener(this);
            if (getNavBarDisplayType() == BACK_HOME_RECENT) {
                mRadio1.setChecked(true);
                mRadio2.setChecked(false);
            } else {
                mRadio1.setChecked(false);
                mRadio2.setChecked(true);
            }

            return mView;
        }

        public void showSelectDialog() {
            mAlertDialog = new AlertDialog.Builder(mContext)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int button) {
                            if(mRadio1.isChecked()){
                                setNavBarDisplayType(BACK_HOME_RECENT);
                                mPreferenceNavigationType.setSummary(R.string.navigation_default);
                            }else if(mRadio2.isChecked()){
                                setNavBarDisplayType(RECENT_HOME_BACK);
                                mPreferenceNavigationType.setSummary(R.string.navigation_overturn);
                            }else{
                                setNavBarDisplayType(BACK_HOME_RECENT);
                                mPreferenceNavigationType.setSummary(R.string.navigation_default);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setTitle(R.string.navgation_bar_display).setView(mView).create();
            mAlertDialog.show();
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.key_back_home_recent:
                case R.id.key_back_home_recent_rb:
                    mRadio1.setChecked(true);
                    mRadio2.setChecked(false);
                    //setNavBarDisplayType(BACK_HOME_RECENT);
                    //mPreferenceNavigationType.setSummary(R.string.navigation_default);
                    //mView.postDelayed(mRunnable, 50);
                    break;
                case R.id.key_recent_home_back:
                case R.id.key_recent_home_back_rb:
                    mRadio1.setChecked(false);
                    mRadio2.setChecked(true);
                    //setNavBarDisplayType(RECENT_HOME_BACK);
                    //mPreferenceNavigationType.setSummary(R.string.navigation_overturn);
                    //mView.postDelayed(mRunnable, 50);
                    break;

                default:
                    break;
            }
        }

        private Runnable mRunnable = new Runnable() {
            public void run() {
                mAlertDialog.dismiss();
            }
        };

    }


private int getFingerprintCount() {
    FingerprintManager fpm = (FingerprintManager) mContext.getSystemService(
                Context.FINGERPRINT_SERVICE);
    if (fpm == null || !fpm.isHardwareDetected()) {
            Log.v(TAG, "No fingerprint hardware detected!!");
            return -1;
    }

    final List<Fingerprint> items = fpm.getEnrolledFingerprints(mContext.getUserId());
    final int fingerprintCount = items != null ? items.size() : 0;
    Log.d(TAG, "getFingerprintCount:" + fingerprintCount);
    return fingerprintCount;
}

    private void maybeAddFingerprintPreference(PreferenceGroup fingerCategory, Preference mPreference) {
        int fingerprintCount = 0;

        if((fingerprintCount = getFingerprintCount()) < 0) {
            return;
        }

        Intent intent = new Intent();
        final String clazz;
        if(fingerprintCount > 0)
        {
            mPreference.setSummary(getResources().getQuantityString(
                    R.plurals.security_settings_fingerprint_preference_summary,
                    fingerprintCount, fingerprintCount));
            clazz = FingerprintSettings.class.getName();
            Log.i(TAG, "----FingerprintSettings----");
        }
        else
        {
            mPreference.setSummary(
                    R.string.security_settings_fingerprint_preference_summary_none);
            clazz = FingerprintEnrollIntroduction.class.getName();
            Log.i(TAG, "----FingerprintEnrollIntroduction----");
        }

        intent.setClassName("com.android.settings", clazz);
        mPreference.setIntent(intent);
        fingerCategory.addPreference(mPreference);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean msgData =  (boolean)msg.obj;
            switch (msg.what) {
                case MSG_NAVIGATION_DISPLAY_ENABLED:
                    if(mPreferenceNavigationDisplay != null) {
                        if(msgData){
                            mPreferenceNavigationDisplay.setSummary(R.string.switch_on);
                            mPreferenceNavigationType.setEnabled(true);
                            mSensor_button.setEnabled(true);
                        }
                        mPreferenceNavigationDisplay.setEnabled(true);
                    }
                    break;
                default:
            }
        }
    };

    public static Context getContext(){
        return mContextExt;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "--------onBackPressed-------");
        //System.exit(0);
        finish();
    }

    private void initFpBtMenu() {
        mSensor_capture = (SwitchPreference) findPreference(KEY_SENSOR_CAPTURE);
        mSensor_shoot = (SwitchPreference) findPreference(KEY_SENSOR_CONTINUOUS_CAPTURE);
        mSensor_call = (SwitchPreference) findPreference(KEY_SENSOR_ANSWER_CALL);
        mSensor_alarm = (SwitchPreference) findPreference(KEY_SENSOR_STOP_ALARM);
        mSensor_notification = (SwitchPreference) findPreference(KEY_SENSOR_DISPLAY_NOTIFICATION);
        mSensor_page = (SwitchPreference) findPreference(KEY_SENSOR_SWITCH_PAGE);

        mSensor_capture.setOnPreferenceChangeListener(this);
        mSensor_shoot.setOnPreferenceChangeListener(this);
        mSensor_call.setOnPreferenceChangeListener(this);
        mSensor_alarm.setOnPreferenceChangeListener(this);
        mSensor_notification.setOnPreferenceChangeListener(this);
        mSensor_page.setOnPreferenceChangeListener(this);

        updateFpBtMenu();
    }

    private void updateFpBtMenu() {
        mSensor_capture.setChecked(EmFrameworkStatic.getFpBackTouchState(KEY_SENSOR_CAPTURE));
        mSensor_shoot.setChecked(EmFrameworkStatic.getFpBackTouchState(KEY_SENSOR_CONTINUOUS_CAPTURE));
        mSensor_call.setChecked(EmFrameworkStatic.getFpBackTouchState(KEY_SENSOR_ANSWER_CALL));
        mSensor_alarm.setChecked(EmFrameworkStatic.getFpBackTouchState(KEY_SENSOR_STOP_ALARM));
        mSensor_notification.setChecked(EmFrameworkStatic.getFpBackTouchState(KEY_SENSOR_DISPLAY_NOTIFICATION));
        mSensor_page.setChecked(EmFrameworkStatic.getFpBackTouchState(KEY_SENSOR_SWITCH_PAGE));
    }
}
