package com.ape.emFramework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
//import android.util.Log;
import com.ape.emFramework.Log;
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

import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;  
import android.hardware.fingerprint.FingerprintManager;
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
import com.ape.emFramework.FingerprintFeatureOptions;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.hardware.fingerprint.ApeFpData;
import android.hardware.fingerprint.IFingerprintService;

public class EmFrameworkStatic {

    private static final String TAG = "EmFrameworkStatic";

    public static final String FINGERPRINT_GOODIX_DEV_NAME = "goodix_fp" ;
    public static final String FINGERPRINT_SILEAD_DEV_NAME = "silead_fp_dev" ;
    public static final String FINGERPRINT_ELAN_DEV_NAME = "elan_fp" ;
    public static final String FINGERPRINT_TEE_DEV_NAME = "tee_fp" ;
    public static final String FINGERPRINT_CHIPONE_DEV_NAME = "chipone_fp" ;
    private static final String FP_DRV_INFO = "/sys/devices/platform/fp_drv/fp_drv_info";
    private static final String FINGERPRINTD_ATTR = "/sys/devices/platform/fp_drv/fingerrpintd";


    public static final int FINGERPRINT_ACQUIRED_ENROLL_DUPLICATE= 6; 
    public static final int FINGERPRINT_ACQUIRED_ENROLL_TOO_NEARBY = 7;   

    public static final boolean isTnFpSupport = SystemProperties.get("ro.tinno.fingerprint.support","0").equals("1");

    public static String BINDER_PACKAGE = "com.android.settings";
    public static String EM_PACKAGE = "com.ape.encryptmanager";
    public static String EM_MAIN_ACTIVITY = "com.ape.encryptmanager.FingerprintMainScreen";
    public static String EM_SETUP_ACTIVITY = "com.ape.encryptmanager.fingerprint.SetupFingerprintEnrollIntroduction";

    public static final String BINDER_CLASS = "com.ape.encryptmanager.service.EncryptService";  

    private static final String drv_info = getDrvInfo();

    public static String INTENT_ACTION_TOUCHSTATE_CHANGE = "fingerprint.intent.action.TOUCHSTATE_CHANGE";

    public static final String KEY_SENSOR_CAPTURE = "sensor_capture";
    public static final String KEY_SENSOR_CONTINUOUS_CAPTURE = "sensor_continous_capture";
    public static final String KEY_SENSOR_ANSWER_CALL = "sensor_answer_call";
    public static final String KEY_SENSOR_STOP_ALARM = "sensor_stop_alarm";
    public static final String KEY_SENSOR_DISPLAY_NOTIFICATION = "sensor_display_notification";
    public static final String KEY_SENSOR_SWITCH_PAGE = "sensor_switch_page";
    public static final int MSG_CHECK_QBSTATE = 2101;

    public static final String getFp_DrvInfo() {
        if(!isTnFpSupport) { return null; }

        if(FINGERPRINT_GOODIX_DEV_NAME.equals(drv_info) 
                ||FINGERPRINT_SILEAD_DEV_NAME.equals(drv_info)
                ||FINGERPRINT_ELAN_DEV_NAME.equals(drv_info)  
                ||FINGERPRINT_CHIPONE_DEV_NAME.equals(drv_info)
                ||FINGERPRINT_TEE_DEV_NAME.equals(drv_info)) {  
            return drv_info;
                }
        return null;
    }

    private static final String getDrvInfo() {
        if(!isTnFpSupport) { return null; }

        try {
            return readLine(FP_DRV_INFO);
        } 
        catch (IOException e) {
            Log.e(TAG, "IO Exception when getting FP_DEV_INFO");
            return "Unavailable";
        }
    }
    private static final String getReadLine(String filename) {
        if(!isTnFpSupport) { return null; }

        try {
            return readLine(filename);
        } 
        catch (IOException e) {
            Log.e(TAG, "IO Exception when getting FP_DEV_INFO");
            return "Unavailable";
        }
    }

    private static final String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public static final boolean startFingerprintDaemon() {
        /*
           if (!isTnFpSupport) {
           Log.i(TAG, "This prj not support fingerprint !");
           return false; 
           }

           boolean min_framework = SystemProperties.get("vold.decrypt").equals("trigger_restart_min_framework");
           boolean encrypted = SystemProperties.get("ro.crypto.state").equals("encrypted");

           if (min_framework) {
           Log.i(TAG, "Now, at trigger_restart_min_framework, do not start fingerprintd!");
           return false;
           }

           Log.i(TAG, "Start daemon:[system/bin/fingerprintd]");
           SystemProperties.set("sys.fingerprintd","normal-boot");

           Log.i(TAG, "Start daemon:[system/bin/gx_fpd]");
           SystemProperties.set("persist.sys.fp.goodix", "1");
           */
        return true;
    }


    public static final boolean getFpTouchState() {
        if (!isTnFpSupport) {
            Log.i(TAG, "This prj not support fingerprint !");
            return false; 
        }   
        boolean touchOnOff = SystemProperties.get("persist.tinno.fp.touchstate", 
                FingerprintFeatureOptions.getTouchDefaultStatus()).equals("1");

        Log.i(TAG, "getFpTouchState:touchOnOff = " + touchOnOff);	
        return touchOnOff;
    }

    public static final void setFpTouchState(boolean b) {
        Log.i(TAG, "setFpTouchState: value = " + b);    
        if (b) {
            SystemProperties.set("persist.tinno.fp.touchstate", "1"); 
        } else {
            SystemProperties.set("persist.tinno.fp.touchstate", "0"); 
        }
    }

    public static final boolean getFpBackTouchState(String key) {
        if (!isTnFpSupport) {
            Log.i(TAG, "This prj not support fingerprint !");
            return false; 
        }   
        boolean touchOnOff = SystemProperties.get(getFpBtPro(key), 
                FingerprintFeatureOptions.getBackNavigationDefaultStatus()).equals("1");

        Log.i(TAG, "getFpBackTouchState:touchOnOff = " + touchOnOff);	
        return touchOnOff;
    }

    public static final void setFpTZLogEnableValue(boolean b) {
        Log.i(TAG, "setFpTZLogEnableValue: value = " + b);    
        if (b) {
            SystemProperties.set("persist.sys.fp.tzlog", "1"); 
        } else {
            SystemProperties.set("persist.sys.fp.tzlog", "0"); 
        }
    }

    public static final boolean getFpTZLogEnableValue() {
        if (!isTnFpSupport) {
            Log.i(TAG, "This prj not support fingerprint !");
            return false; 
        }   
        boolean tz_log_enable= SystemProperties.get("persist.sys.fp.tzlog", 
                FingerprintFeatureOptions.getTZLogEnableDefaultStatus()).equals("1");    
        Log.i(TAG, "getFpTZLogEnableValue:tz_log_enable = " + tz_log_enable);
        return tz_log_enable;
    }


    public static final void setFpBackTouchState(String key,boolean b) {
        Log.i(TAG, "setFpTouchState: value = " + b);  
        if (b) {
            SystemProperties.set(getFpBtPro(key), "1"); 
        } else {
            SystemProperties.set(getFpBtPro(key), "0"); 
        }
    }

    private static final String getFpBtPro(String key) {
        String pro = "";
        switch (key) {
            case KEY_SENSOR_CAPTURE:
                pro = "persist.tinno.fp.bt.capture";
                break;
            case KEY_SENSOR_CONTINUOUS_CAPTURE:
                pro = "persist.tinno.fp.bt.shoot";
                break;
            case KEY_SENSOR_ANSWER_CALL:
                pro = "persist.tinno.fp.bt.call";
                break;
            case KEY_SENSOR_STOP_ALARM:
                pro = "persist.tinno.fp.bt.alarm";
                break;
            case KEY_SENSOR_DISPLAY_NOTIFICATION:
                pro = "persist.tinno.fp.bt.notify";
                break;
            case KEY_SENSOR_SWITCH_PAGE:
                pro = "persist.tinno.fp.bt.page";
                break;
        }
        Log.i(TAG, "getFpBtPro: pro = " + pro);  
        return pro;
    }

    public static String getStartPackageName(Context c){
        final String pkg = "com.android.settings";
        final String pkg2 = EM_PACKAGE;

        final PackageManager pm = c.getPackageManager();
        if(pm != null && pm.isPackageAvailable(pkg2)){
            return pkg2;
        }
        return pkg;
    }

    public static final void startEncryptService(final Context context) {
        if (!isTnFpSupport) { return ; }

        if (getFp_DrvInfo() == null) {
            return;
        }

        try {
            final String BINDER_PACKAGE = getStartPackageName(context);
            Log.i(TAG,"Start: "+BINDER_PACKAGE+"/"+BINDER_CLASS);
            Intent intent = new Intent();
            intent.setClassName(BINDER_PACKAGE, BINDER_CLASS);
            context.startServiceAsUser(intent, UserHandle.OWNER);
        }
        catch (Exception e) {
            Log.e(TAG,"startEncryptService:FAIL!:"+e);
        }
    }

    public static final void startEncryptServiceForQB(final Context context) {
        if (!isTnFpSupport) { return ; }

        if (getFp_DrvInfo() == null) {
            return;
        }

        try {
            final String BINDER_PACKAGE = getStartPackageName(context);
            Log.i(TAG,"Start: "+BINDER_PACKAGE+"/"+BINDER_CLASS);
            Intent intent = new Intent();
            intent.setClassName(BINDER_PACKAGE, BINDER_CLASS);
            intent.setAction("com.ape.action.quickboot");
            context.startServiceAsUser(intent, UserHandle.OWNER);
        }
        catch (Exception e) {
            Log.e(TAG,"startEncryptService:FAIL!:"+e);
        }
    }


    public static boolean isServiceRunning(Context context,String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList 
            = activityManager.getRunningServices(30);
        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        Log.i("EncryptService-class", "-isServiceRunning---->isRunning =" + isRunning);		   
        return isRunning;
    }


    public static boolean isScreenON(Context c) {
        if(c == null) { return false; }
        PowerManager pm = (PowerManager)c.getSystemService(Context.POWER_SERVICE);
        if(pm == null) { return false; }
        boolean isScreenOn;
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT_WATCH) {
            isScreenOn = pm.isInteractive();
        } else {
            isScreenOn = pm.isScreenOn();
        }
        return isScreenOn;
    }    


    public static boolean isSupportShowFingerprintMenu(Context c) {
        FingerprintManager fpm = (FingerprintManager) c.getSystemService(Context.FINGERPRINT_SERVICE);
        UserManager um = (UserManager) c.getSystemService(Context.USER_SERVICE);

        if(fpm == null || um == null){
            return false ;
        }
        if(fpm.isHardwareDetected()
                &&um.isAdminUser() ) {
            return true;
                }
        return false;
    }


    public static boolean setEmEntry(Preference pf, PreferenceGroup pfg) {
        if(pf == null || pfg == null) { return false; }
        final PackageManager pm = pfg.getContext().getPackageManager();
        if(pm != null && pm.isPackageAvailable(EM_PACKAGE)) {
            Intent intent = new Intent();
            intent.setClassName(EM_PACKAGE, EM_MAIN_ACTIVITY);
            pf.setSummary("");
            pf.setIntent(intent);
            pfg.addPreference(pf); 
            return true;
        }
        Log.i(TAG, "setEmEntry:default"); 
        return false;
    }

    public static boolean setEmEntry(Context c) {
        if(c == null) { return false; }
        final PackageManager pm = c.getPackageManager();
        if(pm != null && pm.isPackageAvailable(EM_PACKAGE)) {
            Intent cIntent = null;
            try{
                cIntent = ((Activity)c).getIntent();    
            }catch (Exception e){

            }
            Intent i = new Intent();
            if(cIntent !=null)  {
                boolean useImmersiveMode = cIntent.getBooleanExtra("useImmersiveMode", false);
                if(useImmersiveMode){
                    i.putExtra("useImmersiveMode",useImmersiveMode);
                }
            }         
            i.setClassName(EM_PACKAGE, EM_SETUP_ACTIVITY);
            c.startActivity(i);
            return true;
        }
        Log.i(TAG, "setEmEntry:default setup"); 
        return false;
    }


    public static boolean isPackageAvailableExt(String packageName, Context c) {
        final PackageManager pm = c.getPackageManager();
        if(pm != null && pm.isPackageAvailable(packageName)) {
            return true;
        }
        return false;
    }

    public static String getTop(Context c) {   
        final ActivityManager am = 
            (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityInfo aInfo = null;
        List<RunningTaskInfo> list = am.getRunningTasks(1);
        if (list.size() != 0) {
            RunningTaskInfo topRunningTask = list.get(0);
            final String top = topRunningTask.topActivity.getClassName();
            return top;
        } else {
            return "";
        }
    }

    public static boolean isDeviceLocked(Context c){
        if (c == null) { 
            Log.e(TAG,"isDeviceLocked Context err!:");
            return false; 
        }

        KeyguardManager km = (KeyguardManager)c.getSystemService(Context.KEYGUARD_SERVICE);
        if (km != null){
            return km.isDeviceLocked(ActivityManager.getCurrentUser());
        }
        return false;
    }

    public static boolean isKeyguardLocked(Context c){
        if (c == null) { 
            Log.e(TAG,"isKeyguardLocked Context err!:");
            return false; 
        }

        KeyguardManager km = (KeyguardManager)c.getSystemService(Context.KEYGUARD_SERVICE);
        if (km != null){
            return km.isKeyguardLocked();
        }
        return false;
    }

    public static boolean isScreenOn(Context c) {
        PowerManager  mPowerManagerEx = 
            (PowerManager)c.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        isScreenOn = mPowerManagerEx.isScreenOn();
        return isScreenOn;
    }    

    public static int getIntForUser(ContentResolver cr, String name, int def, int userHandle) {
        return Settings.System.getIntForUser(cr, name, def, userHandle);
    }



    public static boolean LunchQuickBootByFp(int fingerId, Context context, int userId)
    {
        Log.i(TAG, "LunchQuickBootByFp ->fingerId = " + fingerId + "| userId = " + userId);

        FingerprintManager fingerprintManager = (FingerprintManager)context.getSystemService(context.FINGERPRINT_SERVICE);
        ApeFpData fpItem = fingerprintManager.getApeFpDataItem(userId,fingerId);

        String packageName,className;
        String phoneNumber;

        if(fpItem == null) {
            Log.e(TAG, " LunchQuickBootByFp -fpItem is null! " );
            return false;
        }

        if(fpItem.isQuickBootDataEmpty()){
            Log.e(TAG, "QuickBootData is null!");
            return false;
        }

        if(!fpItem.isPackageInfoEmpty()){  	  
            className = fpItem.getQBClassName();
            packageName = fpItem.getQBPackageName(); 
            Intent intent = fpItem.getQBAppIntent();

            //startAppFromQuickBoot(context, packageName, className, userId);
            startAppFromQuickBoot(context, intent, userId);        
            return true;
        }

        if(!fpItem.isContactInfoEmpty()){
            phoneNumber = fpItem.getQBPhoneNumber();
            makeCallFromQuickBoot(context, phoneNumber, userId);		  
            return true;
        }	

        Log.i(TAG, "Shit! may be has same error ???");
        return false;
    }

    //private static void startAppFromQuickBoot(Context context, String packageName, String className, int userId) {
    private static void startAppFromQuickBoot(Context context, Intent intent, int userId) {
        // start app
        Log.i(TAG, " begin to startAppFromQuickBoot---> intent: " +intent);
        callStartAppFromFingerLaunch(context, intent, userId);
    }


    //public static void callStartAppFromFingerLaunch(Context context, 
    //String pkgName, String clsName, int userId) {
    public static void callStartAppFromFingerLaunch(Context context, 
            Intent intent, int userId) {        
        Log.d(TAG, "callStartAppFromFingerLaunch:intent = " + intent + " ||userId = " + userId);
        final String top = getTop(context);
        Log.d(TAG, "call start app|getTop:" + top);
        UserHandle user = UserHandle.of(userId);
        try {
            /*Intent intent = new Intent(Intent.ACTION_MAIN,null);
              intent.setClassName(pkgName, clsName);
              intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
              Intent.FLAG_ACTIVITY_SINGLE_TOP);
              intent.addCategory(Intent.CATEGORY_LAUNCHER);
              */    
            if(intent.getPackage().equals("com.android.browser")){
                intent.setAction("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.BROWSABLE");
                Log.d(TAG, "---android.intent.category.BROWSABLE---");
            }
            if (intent.getPackage().equals("com.fineclouds.privatesystem.sym")) {
                Log.d(TAG, "launchPrivateAppMMX!!!");
                //EmFramework.launchPrivateSpaceApp(mContext);
            } else {
                context.startActivityAsUser(intent, user);
            }

        } catch (Exception e) {
            Log.e(TAG, "shit!:" + e);
        }
    }

    private static void makeCallFromQuickBoot(Context context, String phoneNumber, int userId) {
        //make call
        Log.i(TAG, " begin to makeCallFromQuickBoot! : " +phoneNumber);
        //callMakeCallFromFingerLaunch(context, phoneNumber, userId);
        IFingerprintService fpService = IFingerprintService.Stub.asInterface(
                ServiceManager.getService("fingerprint"));
        try {
            fpService.makeCallByService(phoneNumber);
        } catch (Exception e) {
            Log.e(TAG,"make call err:"+e);
            e.printStackTrace();
        }

    }

    public static void callMakeCallFromFingerLaunch(Context context, String phoneNumber, int userId) {
        Intent intent;
        UserHandle user = UserHandle.of(userId);

        if(PhoneNumberUtils.isEmergencyNumber(phoneNumber)){
            intent = new Intent("android.intent.action.CALL_EMERGENCY", Uri.parse("tel:" +  phoneNumber));
        }else{
            intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" +  phoneNumber));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//FLAG_ACTIVITY_CLEAR_TASK//FLAG_ACTIVITY_CLEAR_TOP
        context.startActivityAsUser(intent,user);//AsUser(intent, UserHandleEM.CURRENT_OR_SELF);
    }
    public static boolean checkQuickBootState(Context context, Handler handler, int fingerId, int userId) {
        boolean isScreenOn = isScreenON(context);
        boolean isKeyguardLocked = isKeyguardLocked(context);
        //Log.d(TAG, "checkQuickBootState : isScreenOn = " + isScreenOn + "| isKeyguardLocked =" + isKeyguardLocked);
        if(!isScreenOn|| isKeyguardLocked){
            handler.sendMessageDelayed(handler.obtainMessage(MSG_CHECK_QBSTATE, fingerId, userId), 75);	 
            return false;
        }
        return true;
    }

}
