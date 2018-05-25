package com.android.server.fingerprint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Instrumentation;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.os.SystemClock;
import android.os.Binder;
import android.os.PowerManager;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.pm.ActivityInfo;
import java.util.List;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import java.util.Timer;
import java.util.TimerTask;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.app.Service;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;

import com.ape.emFramework.EmFrameworkStatic;
import com.ape.emFramework.FingerprintFeatureOptions;
import com.ape.emFramework.Log;
import android.os.Bundle;
import com.android.server.fingerprint.FingerprintService;
import java.util.Timer;
import java.util.TimerTask;

import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.content.pm.ActivityInfo;
import android.os.PowerManager;

import com.ape.emFramework.BtFrameworkStatic;
import com.ape.emFramework.BtFeatureOptions;

public class TpEventReport implements TpNative.OnReportListerner {

public static final String TAG = "TpEventReport";
private static final boolean DEBUG = true;

private static final int FINGER_CLICK_MIN_TIME = 100;
private static final int FINGER_CLICK_TIME = 600;
private static final int FINGER_LONG_PRESS_TIME = 600;
private static final int FINGER_DOUBLE_CLICK_TIME = 300;

// HW event.
private final static int HW_EVT_DOWN = 1;
private final static int HW_EVT_UP = 2;
private final static int HW_EVT_LIGHT_SCREEN = 3;

// SW event.
private static final int SW_EVT_DOUBLE_CLICK = 10;
private static final int SW_EVT_LONG_PRESS = 11;
private static final int SW_EVT_SINGLE_CLICK = 12;

private static final String MYOS_CAEMRA_PACKAGE = "com.myos.camera";
private static final String MYOS_DIALER_PACKAGE = "com.android.dialer";

private static final int VIBRATE_TIME = 80;

private boolean longPress = false;
private Timer mTimer = null;
private long lastFingerDownTime = 0;
private long lastFingerUpTime = 0;

private Timer mDoubleTimer = null;
private boolean doubleClick = false;

private static TpEventReport instance;
private Context  mContext;
private String fpCurrAction = null; 

public boolean isScreenOn = true;


public TpEventReport(Context c) {
    mContext = c;
    registerScreenActionReceiver();
}

public static TpEventReport getInstance(Context c) {
    if(instance == null) {
        instance = new TpEventReport(c);
    }
    return instance;
}

public boolean start(int s) {
    if(s != 0) {
        TpNative.start(1, TpEventReport.this);
        Log.i(TAG, "TpNative : init success!");
    }
    else {
        Log.e(TAG, "TpNative : init failed!");
    }
    return  true;
}

public void setFpAction(String action) {

}

// Implements OnReportListerner
public boolean OnReport(int e) {	
    if(DEBUG) Log.i(TAG, "OnReport: "+e);

    //add for ApeFTM APK
    backTouchKey(e);

    if (!BtFrameworkStatic.getBackTouchState(BtFrameworkStatic.KEY_BACK_TOUCH)){
        Log.i(TAG, "Back touch is close !!!");
        return false;
    }

    parseKeyEvents(e);     

    return true;
}

private void parseKeyEvents(int e) {
    switch (e) {
        case HW_EVT_DOWN : 
        {
            long downTime = SystemClock.uptimeMillis();
            lastFingerDownTime = downTime;

            longPress = false;
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                    longPress = true;
                    go_go_go(SW_EVT_LONG_PRESS);
                }
            }, FINGER_LONG_PRESS_TIME);
        }
        break;
        case HW_EVT_UP: 
        {
            if (mTimer != null) { 
                mTimer.cancel(); 
                mTimer = null;
            }
            long upTime = SystemClock.uptimeMillis();
            if (upTime -lastFingerDownTime < FINGER_CLICK_TIME) {
                if (doubleClick && upTime - lastFingerUpTime < FINGER_DOUBLE_CLICK_TIME) {
                   if (mDoubleTimer != null) { 
                       mDoubleTimer.cancel(); 
                       mDoubleTimer = null;
                   }
               	go_go_go(SW_EVT_DOUBLE_CLICK);
                   doubleClick = false;
                   lastFingerUpTime = 0;
                } else {
                    mDoubleTimer= new Timer();
                    mDoubleTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (mDoubleTimer != null) {
                                mDoubleTimer.cancel();
                                mDoubleTimer = null;
                            }
                             
                            if (upTime - lastFingerDownTime > FINGER_CLICK_MIN_TIME) {
                                go_go_go(SW_EVT_SINGLE_CLICK);
                            } else {
                                Log.d(TAG,"click is too quickly | time = " + (upTime -lastFingerDownTime));
                            }
                            doubleClick = false;
                            lastFingerUpTime = 0;
                        }
                    }, FINGER_DOUBLE_CLICK_TIME);  
                } 

                doubleClick = true;
                lastFingerUpTime = upTime;
            } else {
                Log.d(TAG,"time = " + (upTime -lastFingerDownTime));
            }
        }
        break;
        case HW_EVT_LIGHT_SCREEN:
        {
            go_go_go(SW_EVT_DOUBLE_CLICK);
        }
        break; 
        default:
        {
            go_go_go(SW_EVT_SINGLE_CLICK);
        }
        break;
    }
}

private boolean go_go_go(int event) {
    switch (event) {
        case SW_EVT_SINGLE_CLICK : 
        {
            Log.d(TAG,"######SW_EVT_SINGLE_CLICK#####");
            backTouchSingleClick("up");
            //vibrateForUser(VIBRATE_TIME);
        }
        break;
        case SW_EVT_DOUBLE_CLICK: 
        {
            Log.d(TAG,"######SW_EVT_DOUBLE_CLICK#####");
            backTouchDoubleClick();
            //vibrateForUser(VIBRATE_TIME);
        }
        break;
        case SW_EVT_LONG_PRESS:
        {
            Log.d(TAG,"######SW_EVT_LONG_PRESS#####");
            backTouchLongPress("down");
            //vibrateForUser(VIBRATE_TIME);
        }
        break;  
        default:
        {
            if(DEBUG) Log.i(TAG, "go_go_go: omg, unknow msg :"+event);
            return false;
        }
    }
    if(DEBUG) Log.i(TAG, "go_go_go: event :"+event);
    return true;
}

TelephonyManager getTelecommService() {
        return (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

private void backTouchSingleClick(String action) {
        TelephonyManager tm = getTelecommService();

        if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) //telephone is normal
        {
            if (!isScreenOn){
                backTouchOpenCamera();
            } else if (isCameraActivity()) {
                backTouchCameraCaptureBroadCast(action);
            }
        }else if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING){ //telephone is ring
            Log.d(TAG, "telephone is ring");
        }
    }

    private void backTouchDoubleClick() {
        TelephonyManager tm = getTelecommService();

        if (tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK){
            //telephone is calling
        } else if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING){
            // telephone is ring
            if (isScreenOn && !isKeyguardOn()) {
                backTouchCall("popcall");
            }else{
                backTouchCall("dialercall");
            }
        } else if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
            //telephone is normal
            backTouchOpenCamera();
        }
    }

    private void backTouchLongPress(String action) {
        TelephonyManager tm = getTelecommService();
        if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) //telephone is normal
        {
            backTouchCameraRoateBroadCast(action);
        }
    }

   private void backTouchKey(int key) {
        Intent intent_key = new Intent();
        intent_key.setAction("com.system.backtouch.key");
        if (HW_EVT_DOWN == key) {
            intent_key.putExtra("action", "down");
        } else if (HW_EVT_UP == key) {
            intent_key.putExtra("action", "up");
        }
        mContext.sendBroadcastAsUser(intent_key,UserHandle.ALL);
    }

   private void backTouchCall(String type) {
        if (!BtFrameworkStatic.getBackTouchState(BtFrameworkStatic.KEY_BACK_TOUCH_CALL)){
            Log.d(TAG,"Call function is close !!!");
            return;
        }

        vibrateForUser(VIBRATE_TIME);

        Intent intent_answer = new Intent();
        intent_answer.setAction("com.incallui.backtouch.call");
        intent_answer.putExtra("type", type);
        mContext.sendBroadcastAsUser(intent_answer,UserHandle.ALL);
    }

    private void backTouchCameraRoateBroadCast(String action) {
        if (!BtFrameworkStatic.getBackTouchState(BtFrameworkStatic.KEY_BACK_TOUCH_CHANGE_SCREEN)){
            Log.d(TAG,"Roate function is close !!!");
            return;
        }

        if (!isCameraActivity()) {
            Log.d(TAG,"Here is not camera,We coudn't change screen!!!");
            return;
        }
        
        vibrateForUser(VIBRATE_TIME);

        Intent intent_Roate = new Intent();
        intent_Roate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent_Roate.setAction("com.camera.bk.function");
        intent_Roate.putExtra("type", "longpress");
        intent_Roate.putExtra("action", action);
        mContext.sendBroadcastAsUser(intent_Roate,UserHandle.ALL);
    }

    private void backTouchCameraCaptureBroadCast(String action) {
        if (!BtFrameworkStatic.getBackTouchState(BtFrameworkStatic.KEY_BACK_TOUCH_TAKE_PHOTOS)){
            Log.d(TAG,"Capture function is close !!!");
            return;
        }
        
        vibrateForUser(VIBRATE_TIME);

        Intent intent_Capture = new Intent();
        intent_Capture.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent_Capture.setAction("com.camera.bk.function");
        intent_Capture.putExtra("type", "click");
        intent_Capture.putExtra("action", action);
        mContext.sendBroadcastAsUser(intent_Capture,UserHandle.ALL);
    }

    private void backTouchOpenCamera(){
    	  Log.d(TAG, "isKeyguardOn = " + isKeyguardOn() + "| isCameraActivity() = " + isCameraActivity() + " | isScreenOn = " + isScreenOn);
    	  
        if (!BtFrameworkStatic.getBackTouchState(BtFrameworkStatic.KEY_BACK_TOUCH_OPEN_CAMERA)){
            Log.d(TAG,"OpenCamera function is close !!!");
            return;
        }

        if (isScreenOn) {
            if (!BtFeatureOptions.isScreenOnOpenCamera()) {
              Log.d(TAG,"Screen is bright,it's not support to open camera !!!");
              return;
            }
        }
        
        vibrateForUser(VIBRATE_TIME);

        wakeUp();
        if (isKeyguardOn()) {
            Intent intent_openCamera = new Intent();
            intent_openCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent_openCamera.setAction("com.backtouch.open.camera");
            mContext.sendBroadcastAsUser(intent_openCamera,UserHandle.ALL);
        }else {
            Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivityAsUser(intent,UserHandle.CURRENT);
        }
    }

    private boolean isCameraActivity(){
        if (getTop().contains(MYOS_CAEMRA_PACKAGE)){
            return true;
        }
        return false;
    }

    private boolean isDialerActivity(){
        if (getTop().contains(MYOS_DIALER_PACKAGE)){
            return true;
        }
        return false;
    }

    private String getTop() {
        final ActivityManager am =
                (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityInfo aInfo = null;
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list.size() != 0) {
            ActivityManager.RunningTaskInfo topRunningTask = list.get(0);
            final String top = topRunningTask.topActivity.getClassName();
            Log.d(TAG,"Top:"+top);
            return top;
        } else {
            return "";
        }
    }

    private void registerScreenActionReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG,"action = " + action);
            if (Intent.ACTION_SCREEN_ON.equals(action)){
                isScreenOn = true;
            }else if(Intent.ACTION_SCREEN_OFF.equals(action)) {
                isScreenOn = false;
            }
        }
    };

    private boolean isKeyguardOn(){
        final KeyguardManager mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    private void wakeUp(){
        PowerManager pm=(PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,TAG);
        wl.acquire();
        wl.release();
    }

    public void vibrateForUser(int times) {
        Vibrator vibrator = mContext.getSystemService(Vibrator.class);
        if (vibrator != null) {
            final long[] pattern = new long[] {0, times};
            vibrator.vibrate(pattern, -1);
        }
   }  


}
