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
import android.os.PowerManager.WakeLock;
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

import android.os.Handler;
import android.os.SystemProperties;
import android.content.ServiceConnection;
import com.android.internal.policy.IKeyguardService;
import android.hardware.fingerprint.FingerprintManager;

import com.android.server.fingerprint.PlatfomDeps;
import android.os.IBinder;
import android.hardware.usb.UsbManager;
import android.telephony.TelephonyManager; 
import android.content.Intent;

public class FingerprintEventReport implements FingerprintNative.OnReportListerner {

    public static final String TAG = "FingerprintEventReport";
    public static final boolean DEBUG = FingerprintManager.DEBUG;

    private final static String ACTION_CAM_OPEN = "android.intent.action.ACTION_SHUTDOWN_FLASH";

    // HW event.
    private final static int HW_EVT_DOWN = 1;
    private final static int HW_EVT_UP = 2;
    private final static int HW_EVT_MOVE_UP = 3;
    private final static int HW_EVT_MOVE_DOWN = 4;
    private final static int HW_EVT_MOVE_LEFT = 5;
    private final static int HW_EVT_MOVE_RIGHT = 6;
    private static final int HW_EVT_DOUBLE_CLICK = 7;
    private static final int HW_EVT_LONG_PRESS = 8;
    private static final int HW_EVT_GO_BACK = 9;

    // Add for fp quick wakeup & lock bl support.
    private static final int HW_EVT_QUICK_WAKEUP = 99;
    private static final int HW_EVT_PWR = 116;
    private static final int HW_EVT_SCREEN_OFF = 10;
    private static final int HW_EVT_SCREEN_ON = 11;


    // SW event.
    private static final int SW_EVT_DOUBLE_CLICK = 10;
    private static final int SW_EVT_LONG_PRESS = 11;
    private static final int SW_EVT_GO_BACK = 12;


    private static long lastFingerDownTime=0;
    private static long lastFingerUpTime = 0;
    private static final int FINGER_CLICK_TIME = 300;
    private static final int FINGER_LONG_PRESS_TIME = 600;
    private static final int FINGER_DOUBLE_CLICK_TIME = 700;

    private static final int VIBRATE_TIME = FingerprintFeatureOptions.getBackKeyVibTime();

    private static FingerprintEventReport instance;
    private TelephonyManager mTelemanager;
    private boolean mEnable = true;
    private String changeReason = "";
    private boolean mOccluded = false;
    private static boolean is_Open = EmFrameworkStatic.getFpTouchState(); //Set sensor button default on.

    private static boolean sensorCapture =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_CAPTURE);
    private static boolean sensorContinuousCapture =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_CONTINUOUS_CAPTURE);
    private static boolean sensorAnswerCall =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_ANSWER_CALL);
    private static boolean sensorStopAlarm =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_STOP_ALARM);
    private static boolean sensorDisplayNotification =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_DISPLAY_NOTIFICATION);
    private static boolean sensorSwitchPage =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_SWITCH_PAGE);

    private boolean is_CamOpen = false;

    private boolean longPress = false;
    private static final boolean feature_slide = true;

    private Context  mContext;
    private Vibrator mVb;

    // Record fp state.
    private String fpCurrAction = null; 

    // Record keyguard state.
    private FingerprintService mService;

    private Timer mTimer = null;
    private Timer mTimerIrq = null;
    private PowerManager mPower;
    private FingerprintNative mfpn;

    private Handler mHandler = new Handler();
    private Runnable mRunnableFpIrq = null, mRunnableFpIrqBl = null, mRunnableFpIrqBl2 = null;
    private Runnable mRunnableFpIrqGotoSleep = null, mRunnableLockOut = null;
    private boolean isPowerState = false;
    private int screen_onoff_count = 0;

    // guomingyi add for fp quick wakeup.
    private static final int DO_KEYGUARD_DISABLE = 0;
    private static final int DO_KEYGUARD_ENABLE = 1;
    private static final int DO_KEYGUARD_TIMEOUT = 2;
    private static final int DO_KEYGUARD_DONE = 3;
    private static final int DO_KEYGUARD_SVC_INIT = 4;
    private static final int DO_KEYGUARD_DISMISS = 5;

    private static boolean fp_quick_wake_up = FingerprintManager.isFpQuickWakeUpSupport(1);
    private static boolean fp_quick_wake_up_v2 = FingerprintManager.isFpQuickWakeUpSupport(2); 

    public static final int FP_BL_RESET = 0;
    public static final int FP_BL_LOCK = 1;
    public static final int FP_BL_UNLOCK_AND_TRIGGER = 2;
    public static final int FP_BL_GOTO_SLEEP = 3;
    public static final int FP_BL_LOCK_OUT = 4;
    public static final int FP_RESET_EVENT = 10;

    private static final int FLAG_FP_STATE_IDLE = FingerprintManager.FLAG_FP_STATE_IDLE;
    private static final int FLAG_FP_SCR_ON_UNLOCK = FingerprintManager.FLAG_FP_SCR_ON_UNLOCK;
    private static final int FLAG_FP_SCR_OFF_UNLOCK = FingerprintManager.FLAG_FP_SCR_OFF_UNLOCK;
    private static final int FLAG_POWER_KEY_PRESS = FingerprintManager.FLAG_POWER_KEY_PRESS;
    private static final int FLAG_SCREEN_ON = FingerprintManager.FLAG_SCREEN_ON;
    private static final int FLAG_FP_TRIGGER_IRQ = FingerprintManager.FLAG_FP_TRIGGER_IRQ;
    private static final int FLAG_FP_LOCKOUT = FingerprintManager.FLAG_FP_LOCKOUT;

    private final String KEYGUARD_PACKAGE = "com.android.systemui";
    private final String KEYGUARD_CLASS = "com.android.systemui.keyguard.KeyguardService";
    private IKeyguardService mKeyguardService = null;

    private int bl_ctl_flag = 0;
    private boolean mFirstEntry = false;
    private boolean mFpQuickWakeup = false;
    private boolean mHasAuthFailed = false;
    private int mLockscreenSoundBak = 0;

    private PowerManager.WakeLock mWakeLock = null;
    private static final Object mWakeSyncLock = new Object();
    private int wakeLock_count = 0;
    private boolean fp_auth_success = false;
    private static final int TIME_OUT = 200;
    private boolean mUsbStateChange = false;
    private static final int mQwkFailedTriggerBlDelay = FingerprintFeatureOptions.getFpQwkBlTriggerdelay();
    private static final int mQwkWakeUpFinishTriggerBlDelay = FingerprintFeatureOptions.getFpQwkWakeupFinishdelay();

    public FingerprintEventReport(Context c) {
        mFirstEntry = true;
        mContext = c;
        mPower =(PowerManager)mContext.getSystemService(Context.POWER_SERVICE);  
        mTelemanager = (TelephonyManager) c.getSystemService(Service.TELEPHONY_SERVICE);
        registerScreenActionReceiver();
        Log.i(TAG, "init :FingerprintManager.isFpQuickWakeUpSupport(1):"+FingerprintManager.isFpQuickWakeUpSupport(1));
    }

    public static FingerprintEventReport getInsance(Context c) {
        if(instance == null) {
            instance = new FingerprintEventReport(c);
        }
        return instance;
    }

    public boolean start(int s) {
        if(s != 0) {
            mfpn = FingerprintNative.start(1, FingerprintEventReport.this);
        }
        else {
            Log.e(TAG, "FingerprintNative : init failed!");
        }
        return  true;
    }

    public void setFpAction(String action) {
        fpCurrAction = action;
        if(DEBUG) Log.i(TAG, "fpCurrAction:"+fpCurrAction);

        if("cancelAuthentication".equals(fpCurrAction)) {
            fp_button_enable(true, "setFpAction: cancelAuthentication");
        }
    }

    public void registerFingerprintService(FingerprintService s) {
        if (mService == null && s != null) {
            mService = s;
        }
    }

    /*
     *  1: keyguard hide;
     *  2: keyguard is occluded by another windows;
     *  3: keyguard show;
     *  4: keyguard dismiss.
     */
    public void keyguardStateChange(int s, Bundle extra) {
        switch(s) {
            case 1:
                fp_button_enable(true, "keyguard: hide");
                if (fp_quick_wake_up) {
                    bl_ctl_enable(FP_BL_RESET,"keyguard: hide");
                    boolean on = isBackLightOn();
                    if (!on) {
                        if(DEBUG) Log.i(TAG, "keyguardStateChange: wakeup!");
                        start_wakeUp("keyguard: hide");
                    }
                    resetFpIrq_ReportFlag(1, "keyguard: hide");
                }
                break;
            case 2:
                if(extra != null) {
                    boolean occ = extra.getBoolean("isOccluded", false);
                    mOccluded = occ;
                    if(occ) {
                        fp_button_enable(true, "keyguard: Occluded");
                    }
                    else {
                        fp_button_enable(false, "keyguard: Not occluded");
                    }
                }
                break;
            case 3:
                fp_button_enable(false, "keyguard: show");
                if (fp_quick_wake_up) {
                    resetFpIrq_ReportFlag(0, "keyguard: show");
                }
                break;
            case 4:
                Log.i(TAG, "keyguard: dismiss");
                break;
            default:
                Log.i(TAG, "keyguard what the fuck state ? :"+s);
                break;
        }
    }

    public boolean removeHanlerCallback(int who, final String reason) {
        switch (who) {
            default:
                {
                    if (mRunnableLockOut != null) {
                        mHandler.removeCallbacks(mRunnableLockOut);
                        mRunnableLockOut = null;
                    }
                }
                break;
        }
        return true;
    }

    public boolean fp_quick_wakeup(int fpId, boolean lo) {
        boolean deviceLocked = EmFrameworkStatic.isDeviceLocked(mContext);
        boolean lockout = mService.inPreLockoutMode();
        boolean on = isBackLightOn();
        boolean screen_on = mPower.isScreenOn();

        if (mOccluded) {
            if(DEBUG) Log.i(TAG, "fp_quick_wakeup: mOccluded,so return!");
            return false;
        }

        removeHanlerCallback(2, "onAuth start fingerId:"+fpId);

        if (fpId != 0) {
            fp_auth_success = true;
            if(DEBUG) Log.i(TAG, "fp_quick_wakeup: auth success.fp_auth_success = true");
        }
        else {
            fp_auth_success = false;
            mHasAuthFailed = true;
            mService.handleAcq(FLAG_FP_STATE_IDLE, "normal.auth failed");
        }

        if (on && screen_on) {
            if(DEBUG) Log.i(TAG, "fp_quick_wakeup: backlight is on ,return!:"+screen_on);
            if (fpId != 0) {
                mService.handleAcq(FLAG_FP_SCR_ON_UNLOCK, "normal.auth success");
            }
            return false;
        }

        if(DEBUG) Log.i(TAG, "fp_quick_wakeup: on:"+on+" bl_ctl_flag:"+bl_ctl_flag+" deviceLocked:"+deviceLocked);
        if (!deviceLocked && !fp_quick_wake_up_v2) {
            if (!on && fpId != 0) {
                if(DEBUG) Log.i(TAG, "fp_quick_wakeup: auth seccuss! screen off ==> wakeup..");
                userActivity("auth seccuss!"); 
                bl_ctl_enable(FP_BL_UNLOCK_AND_TRIGGER, "auth.success.00");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean dlocked = EmFrameworkStatic.isDeviceLocked(mContext);
                        boolean klocked = EmFrameworkStatic.isKeyguardLocked(mContext);
                        if(DEBUG) Log.i(TAG, "fp_quick_wakeup: dismiss!:klocked:"+klocked+" dlocked:"+dlocked);
                        if (klocked) {
                            doKeyguardLock(DO_KEYGUARD_DISMISS, "[device not lock]");
                            start_wakeUp("fp auth success");
                        }
                    }
                }, 500);
            }
            return false;
        }

        /* In general, finger Id > 0, but some vendor fpId maybe negative.  */
        if (fpId != 0) {
            if(DEBUG) Log.i(TAG, "fp_quick_wakeup : auth.success");
            bl_ctl_enable(FP_BL_UNLOCK_AND_TRIGGER, "auth.success");
            return true;
        }
        else {
            // authenticate failed.
            if (lockout) {
                if(DEBUG) Log.i(TAG, "fp_quick_wakeup : lockout - wait..few s to trigger bl!, on:"+on+" deviceLocked:"+deviceLocked);
                if (deviceLocked) {
                    mHandler.postDelayed(mRunnableLockOut = new Runnable() {
                        @Override
                        public void run() {
                            if(DEBUG) Log.i(TAG, "fp_quick_wakeup : lockout..");
                            userActivity("lockout"); 
                            bl_ctl_enable(FP_BL_UNLOCK_AND_TRIGGER, "lockout");
                            /* start_wakeUp("lockout"); */
                        }
                    }, 200); 
                }
            }
        }
        return false;
    }

    public boolean acquireOrReleasePowerWakeLock(boolean acq, final String reason) {
        if (acq) {
            synchronized(mWakeSyncLock) {
                if (mWakeLock == null) {
                    if(DEBUG) Log.i(TAG, "powerWakeLock:acquire reason: "+reason);
                    mWakeLock = mPower.newWakeLock(PowerManager.FULL_WAKE_LOCK, "fingerprint-FULL_WAKE_LOCK");
                    mWakeLock.acquire();
                    wakeLock_count++;
                    return true;
                }
                else {
                    if(DEBUG) Log.i(TAG, "powerWakeLock:acquire failed!: "+reason+ " wakeLock_count:"+wakeLock_count);
                }
            }
        }
        else {
            synchronized(mWakeSyncLock) {
                if (mWakeLock != null) {
                    if(DEBUG) Log.i(TAG, "powerWakeLock:release reason: "+reason);
                    mWakeLock.release();
                    mWakeLock = null;
                    wakeLock_count--;
                    return true;
                }
                else {
                    if(DEBUG) Log.i(TAG, "powerWakeLock:release failed!: "+reason+ " wakeLock_count:"+wakeLock_count);
                }
            }
        }
        return false;
    }

    public void autoRestIrqFlag() {
        if (mTimerIrq != null) {
            mTimerIrq.cancel();
            mTimerIrq = null;
        }

        if(DEBUG) Log.i(TAG, "autoRestIrqFlag: start timer ..:"+TIME_OUT+"ms");
        mTimerIrq = new Timer();
        mTimerIrq.schedule(new TimerTask() {  
            @Override  
            public void run() {  
                mTimerIrq = null;
                if (fp_auth_success) {
                    if(DEBUG) Log.i(TAG, "autoRestIrqFlag: auth success!");
                    removeHanlerCallback(2, "autoRestIrqFlag & fp_auth_success == true");
                    return;
                }
                if(DEBUG) Log.i(TAG, "autoRestIrqFlag: timer out to reset irq flag!");
                resetFpIrq_ReportFlag(0, "autoRestIrqFlag:timer out");
            }
        }, TIME_OUT/*ms*/);
    }

    public void bl_ctl_enable(int e, final String from) {
        if (!fp_quick_wake_up) {
            if(DEBUG) Log.i(TAG, "bl_ctl_enable : pls setprop persist.tinno.fp.qwk 1 to enable !");
            return;
        }

        boolean on = isBackLightOn();
        boolean screen_on = mPower.isScreenOn();
        boolean mLockout = mService.inRealLockoutMode();
        final int brightness = EmFrameworkStatic.getIntForUser(
                mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 127, UserHandle.USER_CURRENT);
        boolean devLock = EmFrameworkStatic.isDeviceLocked(mContext);

        if(DEBUG) Log.i(TAG, "bl_ctl_enable :"
                + " screen_on:" + screen_on 
                + " Backlight: " + on 
                + " set:" + e 
                + " last:" + bl_ctl_flag
                + " brightness:" + brightness
                + " real mLockout:" + mLockout
                + " fp_auth_success:" + fp_auth_success
                + " mHasAuthFailed:" + mHasAuthFailed
                + " from:" + from);

        switch (e) {
            case FP_BL_LOCK:
                if (false == on || false == screen_on) {
                    mFpQuickWakeup = true;
                    removeHanlerCallback(2, "from.fp-irq");
                    mService.handleAcq(FLAG_FP_TRIGGER_IRQ, "fp-irq");
                    mService.setCurrFingerprintState(FingerprintManager.FP_TRIGGER_IRQ);
                    if (fp_auth_success || mService.inPreLockoutMode()) {
                        bl_ctl_enable(FP_BL_RESET, "from.fp-irq");
                        start_wakeUp("fingerprint trigger irq:fp_auth_success ? inPreLockoutMode()");
                        return;
                    }
                    if(DEBUG) Log.i(TAG, "bl_ctl_enable: disable backlight!");
                    mfpn.native_bl_ctl(FP_BL_LOCK, brightness);
                    start_wakeUp("fingerprint trigger irq");
                } else {
                    if(DEBUG) Log.i(TAG, "bl_ctl_enable: FP_BL_LOCK - screen is on!");
                }

                userActivity("fp.irq"); 
                autoRestIrqFlag();
                bl_ctl_flag = e;
                break;
            case FP_BL_UNLOCK_AND_TRIGGER:
                if (!on && devLock) {
                    mService.setCurrFingerprintState(FingerprintManager.FP_SCR_OFF_UNLOCK);
                    mService.handleAcq(FLAG_FP_SCR_OFF_UNLOCK, "FP_BL_UNLOCK_AND_TRIGGER");
                    Runnable r = mTurnOnBacklightRunnable;
                    int d0 = mQwkFailedTriggerBlDelay;
                    if ((d0 > 0) && mHasAuthFailed) {
                        if(DEBUG) Log.i(TAG, "bl_ctl_enable: mQwkFailedTriggerBlDelay:"+d0);
                        mHandler.postDelayed(r, d0);
                        mHasAuthFailed = false;
                    }
                    else {
                        int d1 = mQwkWakeUpFinishTriggerBlDelay;
                        if ((d1 > 0) && mService.notImmediatelyEnableBackLight(r, d1)) {
                            if(DEBUG) Log.i(TAG, "bl_ctl_enable: mQwkWakeUpFinishTriggerBlDelay:"+d1);
                        }
                        else {
                            if(DEBUG) Log.i(TAG, "bl_ctl_enable: immediately turn on backlight no delay..");
                            r.run();
                        }
                    }
                }
                bl_ctl_flag = e;
                break;
            case FP_BL_RESET:
                if(DEBUG) Log.i(TAG, "bl_ctl_enable: FP_BL_RESET");
                mfpn.native_bl_ctl(e, brightness);
                fp_auth_success = false;
                break;
        }
    }

    private final Runnable mTurnOnBacklightRunnable = new Runnable() {
        @Override
        public void run() {
            if(DEBUG) Log.i(TAG, "mTurnOnBacklightRunnable: start. unlock bl and turn on backlight..");
            final int brightness = EmFrameworkStatic.getIntForUser(
                    mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 127, UserHandle.USER_CURRENT);
            mfpn.native_bl_ctl(FP_BL_UNLOCK_AND_TRIGGER, brightness);
            /* mService.setDisplayOn(1, "fp-trigger bl on"); */
        }
    };

    public void resetFpIrq_ReportFlag(int r, final String from) {
        if(DEBUG) Log.i(TAG, "resetFpIrq_ReportFlag: "+r+ " from:"+from);
        mfpn.native_bl_ctl(FP_RESET_EVENT, r == 0 ? 0 : 1);
    }

    private boolean start_wakeUp(final String reason) {
        /* synchronized(mWakeSyncLock) { */
            if(DEBUG) Log.i(TAG, "wakeup system..reason:[ "+reason+" ]");
            mPower.wakeUp(SystemClock.uptimeMillis(),reason);
        /* } */
        return true;
    }

    // Implements OnReportListerner
    public boolean OnReport(int e) {	
        if(DEBUG) Log.i(TAG, "OnReport: "+e);

        if (fp_quick_wake_up) {
            switch (e) {
                case HW_EVT_QUICK_WAKEUP:
                    {
                        boolean enroll = mService.hasEnrolledFingerprints(UserHandle.myUserId());
                        boolean deviceLocked = EmFrameworkStatic.isDeviceLocked(mContext);
                        boolean mLockout = mService.inRealLockoutMode();//mService.inPreLockoutMode();
                        boolean rc = enroll;
                        rc = rc && !mLockout;
                        rc = rc && !mOccluded;
                        rc = rc && deviceLocked;

                        if(DEBUG) Log.i(TAG, "deviceLocked:" +deviceLocked 
                                + " enroll: "+ enroll 
                                + " mLockout: "+mLockout
                                + " mOccluded: "+mOccluded
                                + " rc: "+rc);

                        if (rc) {
                            bl_ctl_enable(FP_BL_LOCK, "fp.trigger.irq");
                        } 
                        return false;
                    }
                case HW_EVT_PWR:
                    {
                        boolean on = isBackLightOn();
                        if(DEBUG) Log.i(TAG, "Power key pressed, screen on#:"+on);
                        if (false == on) { 
                            // screen off ==> on
                            isPowerState = true;
                            mService.handleAcq(FLAG_POWER_KEY_PRESS, "power on");
                        } else { 
                            isPowerState = false;
                        }
                        return false;
                    }
            }
        }

        if(mTelemanager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            final String cls = EmFrameworkStatic.getTop(mContext);
            if(cls.contains("com.android.incallui.InCallActivity")) {
                if(DEBUG) Log.i(TAG, "Incall, so,return!");
                return false;
            }
        }

        if(!mEnable) {
            boolean deviceLocked = EmFrameworkStatic.isDeviceLocked(mContext);
            if (!deviceLocked) 
                Log.i(TAG, "disable by:"+changeReason);
            return false;
        }

        if(isFingerprintSpecScreen()){
            if(DEBUG) Log.i(TAG, "isFingerprintSpecScreen...!");
            return false;
        }

        if(hwEventCheck(e)){
            go_go_go(e);
            return true;
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

                    if (feature_slide) {
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
                        go_go_go(SW_EVT_GO_BACK);
                    }
                }
                break;
            default:
                {
                    if (feature_slide) {
                        go_go_go(e);
                    }
                }
                break;
        }
    }

    private boolean go_go_go(int event) {
        switch (event) {
            case SW_EVT_GO_BACK : 
                {
                    if (FingerprintFeatureOptions.isCameraCapture() && mService.onReport(event)) {
                         if (DEBUG) Log.i(TAG, "There are clients callback!!!");
                         vibrateForUser(VIBRATE_TIME);
                    }
                    else if (is_Open) {
                        if (DEBUG) Log.i(TAG, "simulateKeyStroke:KEYCODE_BACK");
                        simulateKeyStroke(KeyEvent.KEYCODE_BACK);
                        vibrateForUser(VIBRATE_TIME);
                    }
                    else {
                        if (DEBUG) Log.i(TAG, "sorry,There are nothing to do!!!");
                    }
                }
                break;
            case SW_EVT_DOUBLE_CLICK: 
            case SW_EVT_LONG_PRESS: 
                {
                    if (mService.onReport(event)) {
                        vibrateForUser(VIBRATE_TIME);
                    }
                }
                break;
            case HW_EVT_MOVE_UP : 
            case HW_EVT_MOVE_DOWN: 
            case HW_EVT_MOVE_LEFT : 
            case HW_EVT_MOVE_RIGHT: 
                {
                    if (mService.onReport(event)) {
                        vibrateForUser(VIBRATE_TIME);
                    }
                }
                break;
            default:
                {
                    if(DEBUG) Log.i(TAG, "go_go_go: omg, unknow msg :"+event);
                    return false;
                }
        }

        return true;
    }

    private boolean isEnableVib() {
        final boolean hf = EmFrameworkStatic.getIntForUser(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 1, UserHandle.USER_CURRENT) != 0;
        if(DEBUG) Log.i(TAG, "isEnableVib :"+hf);
        return hf;
    }

    private void simulateKeyStroke(final int KeyCode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Instrumentation in = new Instrumentation();
                    in.sendKeyDownUpSync(KeyCode);
                } 
                catch (Exception e) {
                    Log.e(TAG, "simulateKeyStroke:"+e);
                }
            }
        }).start();
    }

    private boolean isFingerprintSpecScreen() {
        String currentStackTop = EmFrameworkStatic.getTop(mContext);
        if (currentStackTop == null || mService == null) 
            return false;
        return (currentStackTop.contains("FingerprintEnrollEnrolling") 
                || (currentStackTop.contains("FingerprintSettings") 
                && mService.getEnrolledFingerprints(ActivityManager.getCurrentUser()).size() > 0));
    }

    public void vibrateForUser(int times) {
        if (!isEnableVib())
            return;

        Vibrator vibrator = mContext.getSystemService(Vibrator.class);
        if (vibrator != null) {
            final long[] pattern = new long[] {0, times};
            vibrator.vibrate(pattern, -1);
        }
    }

    private void registerScreenActionReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(EmFrameworkStatic.INTENT_ACTION_TOUCHSTATE_CHANGE);
        if (fp_quick_wake_up) {
            filter.addAction(Intent.ACTION_USER_PRESENT);
            /* filter.addAction(UsbManager.ACTION_USB_STATE); */
            filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        }
        filter.setPriority(1000);
        mContext.registerReceiverAsUser(receiver, UserHandle.ALL, filter, null, null);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_USER_PRESENT.equals(action)) { 
                if(DEBUG) Log.i(TAG, "onReceive: ACTION_USER_PRESENT"); 
                mService.setCurrFingerprintState(FingerprintManager.FP_STATE_IDLE);
                mFpQuickWakeup = false;
                fp_auth_success = false;
                mHasAuthFailed = false;
                mService.handleAcq(FLAG_FP_STATE_IDLE, "ACTION_USER_PRESENT");
            }
            else if (Intent.ACTION_POWER_CONNECTED.equals(action)
                    || Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                boolean on = isBackLightOn();
                if(DEBUG) Log.i(TAG, "#onReceive: ACTION_POWER_CONNECTED/DISCONNECTED: on:"+ on);
                if (!on) {
                    userActivity("ACTION_POWER_CONNECTED/DISCONNECTED"); 
                    bl_ctl_enable(FP_BL_UNLOCK_AND_TRIGGER, "ACTION_POWER_CONNECTED/DISCONNECTED");
                }
            }
            else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                int state = mTelemanager.getCallState();
                if(DEBUG) Log.i(TAG, "#onReceive: ACTION_PHONE_STATE_CHANGED:"+state);
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    case TelephonyManager.CALL_STATE_IDLE:
                        boolean on = isBackLightOn();
                        if (!on) {
                            userActivity("ACTION_PHONE_STATE_CHANGED"); 
                            bl_ctl_enable(FP_BL_UNLOCK_AND_TRIGGER, "ACTION_PHONE_STATE_CHANGED"); 
                        }
                        break;
                }
            }
            else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                fp_button_enable(false, action);
            }
            else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                int uid = ActivityManager.getCurrentUser();   
                String cls = EmFrameworkStatic.getTop(context);
                /* if(DEBUG) Log.i(TAG, "ACTION_SCREEN_ON - getTop:"+cls+" uid:"+uid); */
                doKeyguardLock(DO_KEYGUARD_SVC_INIT, "ACTION_SCREEN_ON");

                if (cls.contains("com.google.android.setupwizard") 
                        || cls.contains("com.android.settings.wifi.WifiSetupActivity")
                        || cls.contains("com.android.camera.CameraActivity") 
                        || cls.contains("com.android.camera.PhotoCamera")
                        || cls.contains("com.myos.camera.CameraLauncher2")
                        || cls.contains("com.myos.camera.SecureCameraActivity")) {
                    fp_button_enable(true, "setupwizard or Camera open!");
                }
                else if(!EmFrameworkStatic.isDeviceLocked(mContext) 
                        && !EmFrameworkStatic.isKeyguardLocked(mContext)) {
                    fp_button_enable(true, action);
                }
            }
            else if (EmFrameworkStatic.INTENT_ACTION_TOUCHSTATE_CHANGE.equals(action)) {
                is_Open = EmFrameworkStatic.getFpTouchState();
                sensorContinuousCapture =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_CONTINUOUS_CAPTURE);
                sensorAnswerCall =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_ANSWER_CALL);
                sensorStopAlarm =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_STOP_ALARM);
                sensorDisplayNotification =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_DISPLAY_NOTIFICATION);
                sensorSwitchPage =  EmFrameworkStatic.getFpBackTouchState(EmFrameworkStatic.KEY_SENSOR_SWITCH_PAGE);

            }
        }
    };

    private void fp_button_enable(boolean b, String reason) {
        if(mEnable != b) {
            mEnable = b;
            changeReason = reason;
            if(DEBUG) Log.i(TAG, "fp_button_enable:"+b+" reason:"+reason);
        }
    }

    private String getTopPackageName(Context c){
        final ActivityManager am = 
            (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(1);
        if (list.size() != 0) {
            RunningTaskInfo topRunningTask = list.get(0);
            final String top = topRunningTask.topActivity.getClassName();
            return top;
        } else {
            return null;
        }
    }

    private boolean switchCheck(){
        if(is_Open 
                ||sensorContinuousCapture 
                || sensorAnswerCall 
                ||sensorStopAlarm
                ||sensorDisplayNotification
                ||sensorSwitchPage) {
            return true;
                }
        return false;
    }

    private boolean hwEventCheck( int e) {
        if (e == HW_EVT_MOVE_LEFT 
                || e == HW_EVT_MOVE_RIGHT
                || e == HW_EVT_MOVE_RIGHT
                || e == HW_EVT_MOVE_UP
                || e == HW_EVT_MOVE_DOWN
                || e == HW_EVT_LONG_PRESS) {

            return true;
                } 
        return false ;
    }


    public void bindKeyGuardService(Context context) {
        Intent intent = new Intent();
        intent.setClassName(KEYGUARD_PACKAGE, KEYGUARD_CLASS);
        if (!context.bindService(intent, mKeyguardConnection, Context.BIND_AUTO_CREATE)) {
            Log.v(TAG, " ** bindService: can't bind to "+ KEYGUARD_CLASS);
        } else {
            Log.v(TAG, TAG + " ** bindService started");
        }
    }

    private final ServiceConnection mKeyguardConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG) Log.i(TAG, "connection ibinder:"+service);
            mKeyguardService = IKeyguardService.Stub.asInterface(service);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) Log.i(TAG, "disconnected!");
            mKeyguardService = null;
        }
    };

    public void doKeyguardLock(int option, String tag) {
        if (!fp_quick_wake_up) {
            return;
        }

        try {               
            switch (option) {
                case DO_KEYGUARD_DISABLE:
                    if (DEBUG) Log.d(TAG,"[guomingyi] setKeyguard disable :"+tag);
                    mKeyguardService.setKeyguardEnabled(false);
                    break;

                case DO_KEYGUARD_ENABLE:
                    if (DEBUG) Log.d(TAG,"[guomingyi] setKeyguard enable :"+tag);
                    mKeyguardService.setKeyguardEnabled(true);
                    break;

                case DO_KEYGUARD_TIMEOUT:
                    if (DEBUG) Log.d(TAG,"[guomingyi] doKeyguardTimeout() !:"+tag);
                    mKeyguardService.setKeyguardEnabled(true);
                    mKeyguardService.doKeyguardTimeout(null);
                    break;

                case DO_KEYGUARD_DONE:
                    if (DEBUG) Log.d(TAG,"[guomingyi] keyguardDone..:"+tag);
                    mKeyguardService.keyguardDone(true, false);
                    break;

                case DO_KEYGUARD_DISMISS:
                    if (DEBUG) Log.d(TAG,"[guomingyi] dismiss..:"+tag);
                    /* mKeyguardService.dismiss(); */ // mtk platfom
                    // mKeyguardService.dismiss(true); // qcom platfom
                    PlatfomDeps.dismiss(mKeyguardService, true);
                    break;

                case DO_KEYGUARD_SVC_INIT:
                    if (mKeyguardService == null) {
                        bindKeyGuardService(mContext);
                    }
                    break;

                default:
                    Log.e(TAG,"[guomingyi] doKeyguardLock: what the fuck ???:"+tag);
                    break;
            }
        } catch (Exception e) {
            Log.d(TAG,"[guomingyi] doKeyguardLock:...:"+e+" tag:"+tag);
            bindKeyGuardService(mContext);
        }
    }

    public boolean isBackLightOn() {
        int val = mfpn.native_get_bl();
        if (DEBUG) Log.d(TAG,"[guomingyi] isBackLightOn: val:"+val);
        return val > 0;
    }

    public void userActivity(String reason) {
        long now = SystemClock.uptimeMillis();
        if (DEBUG) Log.d(TAG,"userActivity:"+reason+" now:"+now);
        mPower.userActivity(now, PowerManager.USER_ACTIVITY_EVENT_TOUCH, 0);
    }
}
