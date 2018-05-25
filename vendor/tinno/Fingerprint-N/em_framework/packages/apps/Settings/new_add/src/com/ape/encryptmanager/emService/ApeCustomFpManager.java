package com.ape.emFramework.emService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.R.bool;
import android.R.integer;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;

import com.ape.emFramework.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.ape.encryptmanager.utils.MessageType;
import com.ape.encryptmanager.utils.EncryUtil;
import com.ape.encryptmanager.utils.LogUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.PowerManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import java.lang.reflect.Method;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.os.SystemClock;
import android.app.KeyguardManager;
import com.ape.encryptmanager.service.TinnoFingerprintData;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.pm.ActivityInfo;
import com.ape.encryptmanager.QuickBootData;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.net.Uri;
import android.os.UserHandle;
import com.ape.emFramework.EmFrameworkStatic;

public class ApeCustomFpManager {

public static final String TAG = "ApeCustomfingerprintManger-cls";

/************ slw start ***********************/
// public static final int MSG_ENROLL_CREDENTIAL_RSP = 100;
// public static final int MSG_VERIFY_CREDENTIAL_RSP = 101;
// public static final int MSG_VKEY_CALLBACK = 102;
/************ slw end ***********************/

private Context mContext;
private Handler mHandler;
public static long verifyStartTime = 0;

private static boolean mRegisterFlag = false;
private static boolean mFpKeyFlag = false;
private boolean isElanOptionCancel = false;
public static final int FINGER_MAX_NUM = 5;

private PowerManager mPowerManager;


private int mEnrollmentSteps = -1;
private int mEnrollmentRemaining = 0;
private double mEnrollmentProgress = 0;
private Listener mListener;
private boolean mEnrolling;
private CancellationSignal mEnrollmentCancel;
private boolean mDone;
//private FingerprintManagerEM mFingerprintManager;
private FingerprintManager mFingerprintManager;

private static int fingerVerifyCount = 0;
private static CancellationSignal mFingerprintCancelSignal = null;

public static final int FINGER_ENROLL_TOTAL_TIMES = 10;
public static final long FINGER_LOCKOUT_DURATION = 1000;

private static boolean mTinnoFingerprintLockout = false;
private static int mTinnoFingerprintLockCount = 0;

private final static int MSG_FINGER_AUTHENTICATE_START = 1;
private final static int MSG_FINGER_AUTHENTICATE_CANCEL = 2;

private static final long[] FP_ERROR_VIBRATE_PATTERN = new long[] {0, 30, 100, 30};
private static final long[] FP_SUCCESS_VIBRATE_PATTERN = new long[] {0, 30};
private static final long[] FP_LOCKOUT_VIBRATE_PATTERN = new long[] {0, 60};

public static String  APE_FINGERPRINT_APP_NAME="com.ape.encryptmanager";
public static String  APE_FINGERPRINT_CLASS_NAME="com.ape.encryptmanager.MainActivity";

public static int lastVerifyTag = 0;
private boolean mOccluded = false;

private final String KEY_PACKAGE_NAME = "packageName";
private final String KEY_CLASS_NAME = "className";
private final String KEY_PHONE_NUMBER="phoneNumber";
private final int QUICK_BOOT_MSG_SENED_DELAY = 300;

private static ApeCustomFpManager sApeCustomFpManager;


private ApeCustomFpManager(Context context) {
    Log.d(TAG, "<<<<<<<<Create ApeCustomFpManager>>>>>>>>, currentUserId = " + context.getUserId());
    mContext = context;
    mFingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
}


private ApeCustomFpManager(Context context, Handler handler) {
    Log.d(TAG, "new ApeCustomFpManager");
    mContext = context;
    mHandler = handler;
    mFingerprintManager = (FingerprintManager) context.getSystemService(
        Context.FINGERPRINT_SERVICE);
    if(mFingerprintManager != null){
        Message m = new Message();
        m.what = MessageType.TINNO_MSG_SERVICE_CONNECTED;
        mHandler.sendMessage(m);
    }
}

public void setHandler(Handler handler){
    Log.d(TAG, "setHandler -->" + handler);        
    mHandler = handler;
};

public static ApeCustomFpManager getInstance(Context context){
    Log.d(TAG, "getInstance context.userId -->" + context.getUserId());            
    if (sApeCustomFpManager == null) {
        sApeCustomFpManager = new ApeCustomFpManager(context);      
    }
    return sApeCustomFpManager;
}


public static ApeCustomFpManager ReCreateInstance(Context context){
    Log.d(TAG, "ReCreateInstance context.userId -->" + context.getUserId());
    if (sApeCustomFpManager != null) {
        sApeCustomFpManager = null;
    }
    sApeCustomFpManager = new ApeCustomFpManager(context);      
    return sApeCustomFpManager;
}

public Context getContextByPakcage(String name) {
       try {    
           Context context = mContext.createPackageContext(name, Context.CONTEXT_INCLUDE_CODE    
                   | Context.CONTEXT_IGNORE_SECURITY);
           return context;
       } catch (Exception e) {
           Log.e(TAG, "getContextByPakcage ->context is null ");
           e.printStackTrace();
       }
       return null;
}


private final Runnable mTimeoutRunnable = new Runnable() {
	@Override
	public void run() {
		cancelEnrollment();
	}
};

private void cancelEnrollment() {
	mHandler.removeCallbacks(mTimeoutRunnable);
	if (mEnrolling) {
		mEnrollmentCancel.cancel();
		mEnrolling = false;
		mEnrollmentSteps = -1;
	}
}

public interface Listener {
	void onEnrollmentHelp(CharSequence helpString);
	void onEnrollmentError(CharSequence errString);
	void onEnrollmentProgressChange(int steps, int remaining);
}

public Fingerprint  getFingerPrintById(int fingerId){
     final List<Fingerprint> items = mFingerprintManager.getEnrolledFingerprints();
     final int fpCount = items.size();

     for(int i=0; i<fpCount; i++){
        if(fingerId == items.get(i).getFingerId())
            return items.get(i);
     }
     return null;
}

public void showAppIcon(Context context){
    //Log.d(TAG, "----showAppIcon---" + MainFeatureOptions.isNeedShowAppIconSupported(context));
    PackageManager p = context.getPackageManager();
    ComponentName cName = new ComponentName(APE_FINGERPRINT_APP_NAME,APE_FINGERPRINT_CLASS_NAME);
    //if(MainFeatureOptions.isNeedShowAppIconSupported(context)){
    if(true){
        p.setComponentEnabledSetting(cName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }else{
        p.setComponentEnabledSetting(cName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}


private void deleteFingerPrint(Fingerprint fingerPrint, int userId) {
    mFingerprintManager.remove(fingerPrint, userId, mRemoveCallback);
}


private FingerprintManager.RemovalCallback mRemoveCallback = new FingerprintManager.RemovalCallback() {
    @Override
    public void onRemovalSucceeded(Fingerprint fingerprint) {

    }
    @Override
    public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {

    }
};


public void resetFingerFailedAttemps(){
    Log.d(TAG, "resetFingerFailedAttemps ####");

    if (mFingerprintManager != null) {
        byte[] token = null;
        mFingerprintManager.resetTimeout(token);
        mTinnoFingerprintLockout = false;
    }

}


private FingerprintManager.AuthenticationCallback mAuthenticationCallback = new FingerprintManager.AuthenticationCallback() {

    @Override
    public void onAuthenticationFailed() {
        Log.d(TAG, "onAuthenticationFailed");
        vibrateFingerprintError(mContext);

        TinnoFingerprintData MessageData = new TinnoFingerprintData(-1, "verify failed");
        mHandler.sendMessage(mHandler.obtainMessage(
        MessageType.TINNO_MSG_VERIFY_FAILED, MessageData));
    };

    @Override
    public void onAuthenticationSucceeded(AuthenticationResult result) {
        int fingerid = result.getFingerprint().getFingerId();
        //result.getCryptoObject();
        TinnoFingerprintData MessageData = new TinnoFingerprintData(
        fingerid, 0);

        vibrateFingerprintSuccess(mContext);

        Log.d(TAG, "onAuthenticationSucceeded,  fingerid = " + fingerid);
        if(isScreenON(mContext)){
            mHandler.sendMessage(mHandler.obtainMessage(
                 MessageType.TINNO_MSG_VERIFY_SUCCESS, MessageData));
        }else{
            mHandler.sendMessage(mHandler.obtainMessage(
                 MessageType.TINNO_MSG_VERIFY_IDENTIFY_WAKEUP_MATCHED, MessageData));
        }

        cancelOperation(EncryUtil.FINGERPRINT_REGISTER_VERIFY_TYPE,EncryUtil.TAG_VERIFY_SUCCESS);
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Log.d(TAG, "onAuthenticationHelp , helpMsgId = " + helpMsgId + " | helpString = " + helpString);
        TinnoFingerprintData MessageData = new TinnoFingerprintData(
            helpMsgId, (String)helpString);
        mHandler.sendMessage(mHandler.obtainMessage(
            MessageType.TINNO_MSG_VERIFY_HELP, MessageData));
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Log.d(TAG, "onAuthenticationError , errMsgId = " + errMsgId + " | errString = " + errString);
        TinnoFingerprintData MessageData = new TinnoFingerprintData(
            errMsgId, (String)errString);

        if(errMsgId == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT){
             mTinnoFingerprintLockout = true;
             mHandler.sendMessage(mHandler.obtainMessage(
                 MessageType.TINNO_MSG_VERIFY_ERROR_LOCKOUT, MessageData));
             vibrateFingerprintLockOut(mContext);
        }else{ 
            mHandler.sendMessage(mHandler.obtainMessage(
                   MessageType.TINNO_MSG_SYSTEM_ERROR, MessageData));
        }
    }

    @Override
    public void onAuthenticationAcquired(int acquireInfo) {
        Log.d(TAG, "onAuthenticationAcquired , acquireInfo = " + acquireInfo);
    }
};

public void cancelOperation(int type, final int tag) {
    Log.i(TAG, "Entry cancelOperation.");
    switch(type){
        case EncryUtil.FINGERPRINT_REGISTER_VERIFY_TYPE:
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doCancel(tag);
                }
            }, 0);
        	break;
        case EncryUtil.FINGERPRINT_REGISTER_ENROLL_TYPE:
        	cancelEnrollment();
        	break;
    }
}

public int verifyStart(final int delay, final int tag, int userId) {
    if(isEnrolledFingerprints(userId)){
        //final int userId = ActivityManagerEM.getCurrentUser();
        //final int userId = 0;
        if (isUnlockWithFingerPrintPossible(userId)) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doAuthenticateListening(tag, userId);
                }
            }, delay);
        }
        return 0;
    }
    Log.i(TAG, "verifyStart fail:no enrolled fingerprint!");
    return -1;
}

private void doAuthenticateListening(int tag,int userId){
    if (isDeviceLocked(mContext) 
		|| isKeyguardLocked(mContext) 
		|| !isScreenON(mContext)) {
        Log.i(TAG, "Keyguard is locked ignore doAuthenticateListening!!!!!!!!!!!");
        return;
    }
//    if(fingerVerifyCount == 1) {
 //       Log.i(TAG, "Warning: Please call doCancel first!");
  //      return;
//    }
    synchronized (ApeCustomFpManager.this) {
        //final int uid = ActivityManagerEM.getCurrentUser();
        final int uid = 0;
        fingerVerifyCount = 1;
        lastVerifyTag = tag;
        Log.i(TAG, "doAuthenticateListening():"+EncryUtil.parseTag(tag));

        mFingerprintCancelSignal = new CancellationSignal();
        mFingerprintManager.authenticate(null, mFingerprintCancelSignal, 0 /* flags */,
                        mAuthenticationCallback, null, userId);
    }
    Log.i(TAG, "doAuthenticateListening exit.");
}
private void doCancel(int tag){
    /* //Removed by yinglong.tang
    if(fingerVerifyCount == 0){
        Log.i(TAG, "Warning: Authenticate alreadly cancel!");
        return;
    }
    */
    if(!checkCancelPolicy(tag, lastVerifyTag)) {
        Log.i(TAG, "checkCancelPermission:fail:" + EncryUtil.parseTag(tag) + " cannot stop:" + EncryUtil.parseTag(lastVerifyTag));
        return;
    }
	
    if (isDeviceLocked(mContext) 
		|| isKeyguardLocked(mContext) 
		|| !isScreenON(mContext)) {
        Log.i(TAG, "Keyguard is locked ignore doCancel!!!!!!!!!!!");
        return;
    }
	
    synchronized (ApeCustomFpManager.this) {
        Log.i(TAG, "doCancel():"+EncryUtil.parseTag(tag));
        if(mFingerprintCancelSignal != null){
            mFingerprintCancelSignal.cancel();
            mFingerprintCancelSignal = null;
        }
        fingerVerifyCount = 0;

    }
    Log.i(TAG, "doCancel exit.");
}

//Authenticate cancel policy.
private boolean checkCancelPolicy(int cancel_tag,int auth_tag) {
    if(cancel_tag == EncryUtil.TAG_KEYGUARD_BINDER_CALL
            || cancel_tag == EncryUtil.TAG_VERIFY_SUCCESS
            || cancel_tag == EncryUtil.TAG_SIM_STATE_CHANGE
            || cancel_tag == EncryUtil.TAG_VERIFY_ERR_LOCKOUT) {
        return true;
    }

    if(cancel_tag == auth_tag) {
        return true;
    }
    else {
        switch (auth_tag) {
            case EncryUtil.TAG_KEYGUARD_BINDER_CALL:
            case EncryUtil.TAG_SCREEN_ON_OFF:
            case EncryUtil.TAG_AIRPLANE_MODE_CHANGED:
            case EncryUtil.TAG_DO_UNLOCK_SCREEN:
            case EncryUtil.TAG_COUNTDOWN_TIMER_ON_FINISH: {
                switch (cancel_tag) {
                    case EncryUtil.TAG_USER_PRESENT:
                        return true;
                }
            }
        }
    }

    return false;
}

public boolean isUnlockWithFingerPrintPossible(int userId) {
    return mFingerprintManager != null && !isFingerprintDisabled(userId);
}

private boolean isFingerprintDisabled(int userId) {
    final DevicePolicyManager dpm =
            (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
    return dpm != null && (dpm.getKeyguardDisabledFeatures(null/*, userId*/)
                & DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT) != 0;
}


public int[] getFpIndexList() {
    try {
    	int[] data = null;
    	List<Integer> fpIndexList = new ArrayList<Integer>();

                 List<Fingerprint> list = mFingerprintManager.getEnrolledFingerprints();
    	if (list != null && list.size() > 0) {
    		for (int i = 0; i < list.size(); i++) {
    			fpIndexList.add(list.get(i).getFingerId());
    		}
    	}

    	if (fpIndexList != null && fpIndexList.size() > 0) {
    		data = new int[fpIndexList.size()];
    		for (int i = 0; i < fpIndexList.size(); i++) {
    			data[i] = fpIndexList.get(i);
    		}
    	}
    	return data;
    } catch (Exception e) {
    	LogUtil.d(TAG, "Exception :" + e.toString());
    	return null;
    }
}

public int removeCredential(int enrollIndex, int userId) {
    try{
        Fingerprint  currentFinger = getFingerPrintById(enrollIndex);
        deleteFingerPrint(currentFinger, userId);
        return 0;
    }catch(Exception e){
        LogUtil.d(TAG, "Exception :" + e.toString());
        return -1;
    }
}




public boolean isFingerprintLockout(){
    return mTinnoFingerprintLockout == true;
}

public void setFingerprintLockoutValue(boolean isLockout){
    mTinnoFingerprintLockout = isLockout;
}

public static void vibrateFingerprintError(Context c) {
    if(c == null) { return; }
	Vibrator vibrator = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
	if (vibrator != null) {
		vibrator.vibrate(FP_ERROR_VIBRATE_PATTERN, -1);
	}
    else {
        Log.d(TAG, "vibrateFingerprintError vibrator is null!");
    }
}

public static void vibrateFingerprintSuccess(Context c) {
    if(c == null) { return; }
	Vibrator vibrator = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
	if (vibrator != null) {
		vibrator.vibrate(FP_SUCCESS_VIBRATE_PATTERN, -1);
	}
    else {
        Log.d(TAG, "vibrateFingerprintSuccess vibrator is null!");
    }
}

public static void vibrateFingerprintLockOut(Context c) {
    if(c == null) { return; }
	Vibrator vibrator = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
	if (vibrator != null) {
		vibrator.vibrate(FP_LOCKOUT_VIBRATE_PATTERN, -1);
	}
    else {
        Log.d(TAG, "vibrateFingerprintLockOut vibrator is null!");
    }
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
    Log.i(TAG," isScreenON = " + isScreenOn);
    return isScreenOn;
}

public static boolean isDeviceLocked(Context c){
    if(c == null) { return false; }
    KeyguardManager km = (KeyguardManager)c.getSystemService(Context.KEYGUARD_SERVICE);
    if(km != null){
        Log.d(TAG, "km.isDeviceLocked() = " + km.isDeviceLocked());       
        return km.isDeviceLocked();//ActivityManagerEM.getCurrentUser()
    }
    return false;
}

public static boolean isKeyguardLocked(Context c){
    if(c == null) { return false; }
    KeyguardManager km = (KeyguardManager)c.getSystemService(Context.KEYGUARD_SERVICE);
    if(km != null){
        return km.isKeyguardLocked();
    }
    return false;
}

public boolean isEnrolledFingerprints(int userId) {
    Log.d(TAG, "isEnrolledFingerprints userId= " + userId);   
    List<Fingerprint> enrolled_items =
        mFingerprintManager.getEnrolledFingerprints(userId);
    return (enrolled_items.size() > 0);
}

public void setOccluded(boolean isOccluded) {
    mOccluded = isOccluded;
}

public boolean isOccluded() {
    return mOccluded;
}

}
