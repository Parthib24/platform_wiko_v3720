package com.ape.encryptmanager.event;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Vibrator;
import android.view.KeyEvent;

import com.ape.emFramework.Log;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.app.Instrumentation;
import android.os.Binder;
import android.view.KeyCharacterMap;
import android.view.InputDevice;
import android.view.InputEventConsistencyVerifier;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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

//MTK Project support only.
//import com.mediatek.audioprofile.AudioProfileManager;
//import com.ape.fingerprint.WrapperManager;
//import com.apefinger.util.MainFeatureOptions;
//import com.mediatek.common.audioprofile.IAudioProfileService;

import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.app.Service;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.app.Instrumentation;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.os.ServiceManager;
import com.ape.encryptmanager.utils.EncryUtil;
import com.ape.encryptmanager.parse.*;
import android.os.RemoteException;
import com.ape.encryptmanager.service.*;
//import com.ape.emFramework.EmCommon.ActivityManagerEM;
//import com.ape.emFramework.EmCommon.UserHandleEM;

public class FpTouchEventHandle {

private static final String TAG = "FpTouchEventHandle";

private static int HW_EVT_DOWN = 1;
private static int HW_EVT_UP = 2;

private static final int EVT_ONCE_TOUCH = 0;
private static final int EVT_DOUBLE_TOUCH = 1;
private static final int EVT_LONG_TOUCH = 2;

private static long lastFingerDownTime=0;
private static long lastFingerUpTime = 0;
private static final int FINGER_CLICK_TIME = 300;
private static final int FINGER_LONG_PRESS_TIME = 600;
private static final int FINGER_DOUBLE_CLICK_TIME = 700;

private Context  mContext;
private Vibrator mVb;
private static FpTouchEventHandle instance;
private Timer mTimer;

private final static String ACTION_LOCK_SCREEN = "tinno.intent.action.DO_KEYGUARD_LOCKED";
private final static String ACTION_CAM_OPEN = "android.intent.action.ACTION_SHUTDOWN_FLASH";
private TelephonyManager mTelemanager;
//private AudioProfileManager mProfileManager;
private final static String PROFILE_MEETING = "mtk_audioprofile_meeting";
private final static String PROFILE_SILENT = "mtk_audioprofile_silent";

private boolean mDisable = false;
private boolean has_down;
private static boolean is_Open;
private boolean is_CamOpen = false;
//private boolean isLockScreen = false;

//public static final boolean DEFAULT_VALUE = MainFeatureOptions.getTouchBackDefaultStatus();
public static final boolean DEFAULT_VALUE = true;

public static final String PRE_FILES = "pre_fingerprint";
public static final String PRE_SENSOR_BUTTON_ONOFF = "pre_sensor_button_onoff";

private static final String AUDIO_PROFILE_SERVICE = "audioprofile";
//private IAudioProfileService mIAudioProfileService;

private boolean mOccluded = false;



public FpTouchEventHandle(Context c) {
	mContext = c;
	mVb = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
	//mProfileManager = (AudioProfileManager) c.getSystemService(Context.AUDIO_PROFILE_SERVICE);
	mTelemanager = (TelephonyManager) c.getSystemService(Service.TELEPHONY_SERVICE);
	updatePreference(c);
	registerScreenActionReceiver();
	//getAudioProfileService();
}
/*
public IAudioProfileService getAudioProfileService() 
{
	if(mIAudioProfileService == null) {
		mIAudioProfileService = IAudioProfileService.Stub.asInterface(
			ServiceManager.getService(AUDIO_PROFILE_SERVICE));
		if(mIAudioProfileService == null) {
			Log.e(TAG, "-----getAudioProfileService fail!-----");
		}
	}
	return mIAudioProfileService;
}
*/

public static FpTouchEventHandle getInsance(Context c) {
	if(instance == null) {
		instance = new FpTouchEventHandle(c);
	}
	return instance;
}

public void disable(boolean b) {
    mDisable = b;
}

public void setOccluded(boolean occ) {
	mOccluded = occ;
}

public void fp_handleKeyEvent( int evt) {	
	if(!is_Open) {
		Log.i(TAG, "Sensor button has been closed!");
		return;
	}
	
	final boolean isCall = (mTelemanager.getCallState() 
		== TelephonyManager.CALL_STATE_OFFHOOK);

	if(isDeviceLocked()) {
		if(mOccluded && !isCall){
			sendDownAndUpKeyEvents(evt);    
            return;
		}
        Log.i(TAG, "isLockScreen mOccluded:"+mOccluded);
		return;
	}

	if(mDisable) {
        Log.i(TAG, "Disable!");
		return;
	}

	if(isFingerprintEnrolling()){
		Log.i(TAG, "isFingerprintEnrolling...!");
		return;
	}
	
	if(isCall) {
		final String cls = getTop();
		if(cls.contains("com.android.incallui.InCallActivity")) {
			Log.i(TAG, "Incall, so,return!");
			return;
		}
	}

	if(isDeviceLocked()) {
		Log.i(TAG, "isDeviceLocked!");
		return;
	}

	sendDownAndUpKeyEvents(evt);                                                     
}

private void sendDownAndUpKeyEvents(int msgtype) {
	//Log.i(TAG, " " + "HW msgtype = "+ msgtype);     
	
	if(msgtype == HW_EVT_DOWN)
	{          
		final long downTime = SystemClock.uptimeMillis();
		lastFingerDownTime = downTime;
	}
	else if(msgtype == HW_EVT_UP) 
	{
		final long upTime = SystemClock.uptimeMillis();
		if(upTime -lastFingerDownTime < FINGER_CLICK_TIME) 
		{
			String cls = null;
			if(is_CamOpen){
				cls = getTop();
			}
			
			if(("com.myos.camera.CameraLauncher2".equals(cls) 
				||"com.myos.camera.SecureCameraActivity".equals(cls))) 
			{
				Log.i(TAG, "do capture!");
				simulateKeyStroke(KeyEvent.KEYCODE_VOLUME_DOWN);
				vibrateForUser(30);
			}
			else 
			{
				if(is_CamOpen) 
				{
					Log.i(TAG, "Camera has exit!");
					is_CamOpen = false;
				}

				if(is_Open) {
					simulateKeyStroke(KeyEvent.KEYCODE_BACK);
					if(isEnableVib())
					{
						//vibrateForUser(MainFeatureOptions.getFingerVibTime());
						vibrateForUser(30);
					}
				}
			}
		}
       }
}

private boolean isEnableVib() 
{
	//final boolean hf = EmCommon.getIntForUserEM(mContext.getContentResolver(),
	//	Settings.System.HAPTIC_FEEDBACK_ENABLED, 1, UserHandleEM.USER_CURRENT) != 0;
        final boolean hf = false;

	//if(!WrapperManager.isMtkPlateform()/*Features.is_mtk*/) {
        if(false/*Features.is_mtk*/) {
		return hf;
	}

	try {
/*
		if(getAudioProfileService() != null) {
			final String key = getAudioProfileService().getActiveProfileKey();
			final boolean enable = !(PROFILE_MEETING.equals(key) || PROFILE_SILENT.equals(key));
			return hf && enable;
		}
*/
		return  hf;
	}
	catch (Exception e) {
		e.printStackTrace();
	}
	
	return false;
}

private void simulateKeyStroke(final int KeyCode) {
	new Thread(new Runnable() {
		@Override
		public void run() {
			try {
				Instrumentation in = new Instrumentation();
				in.sendKeyDownUpSync(KeyCode);
			} catch (Exception e) {
				Log.e(TAG, "simulateKeyStroke:"+e);
			}
		}
	}).start();
}

private boolean isScreenOn() {
	PowerManager  mPowerManagerEx = 
		(PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
	boolean isScreenOn = false;
	isScreenOn = mPowerManagerEx.isScreenOn();
	return isScreenOn;
}    

private boolean isDeviceLocked(){
	return isKeyguardLocked(mContext);
}

public static boolean isKeyguardLocked(Context c){
    if(c == null) { return false; }
    KeyguardManager km = (KeyguardManager)c.getSystemService(Context.KEYGUARD_SERVICE);
    if(km != null){
        return km.isKeyguardLocked();
    }
    return false;
}
private boolean isFingerprintEnrolling(){
	String currentStackTop = getTop();
	return currentStackTop.contains("FingerprintEnrollEnrolling");

}

public void vibrateForUser(int times) {
	Vibrator vibrator = mContext.getSystemService(Vibrator.class);
	if (vibrator != null) {
		final long[] pattern = new long[] {0, times};
		vibrator.vibrate(pattern, -1);
	}
}

private String getTop() {   
	final ActivityManager am = 
		(ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
	ActivityInfo aInfo = null;
	List<RunningTaskInfo> list = am.getRunningTasks(1);
	if (list.size() != 0) {
		RunningTaskInfo topRunningTask = list.get(0);
		final String top = topRunningTask.topActivity.getClassName();
		//Log.d(TAG,"Top:"+top);
		return top;
	} else {
		return "";
	}
}

private void registerScreenActionReceiver() {
	final IntentFilter filter = new IntentFilter();
	filter.addAction(Intent.ACTION_SCREEN_OFF);
	filter.addAction(Intent.ACTION_SCREEN_ON);
	//filter.addAction(ACTION_LOCK_SCREEN);
	filter.addAction(Intent.ACTION_USER_PRESENT);
	filter.addAction(ACTION_CAM_OPEN);
	filter.setPriority(1002);
	mContext.registerReceiver(receiver, filter);
}

private final BroadcastReceiver receiver = new BroadcastReceiver() {
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        //Log.d(TAG,"--[onReceive]--: "+action);
        //final int uid = ActivityManagerEM.getCurrentUser(); 
        final int uid = 0;
        
        if(Intent.ACTION_USER_PRESENT.equals(action)) {
        	disable(false);
        	//isLockScreen = false;
        }
        else if(Intent.ACTION_SCREEN_OFF.equals(action)) {
            if(uid != 0){ return; }
        	disable(true);
        }
        else if(Intent.ACTION_SCREEN_ON.equals(action)) {
        	final String cls = getTop();
        	Log.i(TAG, "getTop:"+cls);
        	if(cls.contains("com.google.android.setupwizard") 
        		|| cls.contains("com.android.settings.wifi.WifiSetupActivity")) {
        		disable(false);
        	}
        	else if(!isDeviceLocked()) {
        		disable(false);
        	}
        	else {
                if(uid != 0){ return; }
        		disable(true);
        	}
        }
        /*else if(ACTION_LOCK_SCREEN.equals(action)) {
        	//disable(true);
        	isLockScreen = true;
        }*/
        else if(ACTION_CAM_OPEN.equals(action)) {
        	if(true) {
        		is_CamOpen = true;
        	}
        }
	}
};


public static boolean updatePreference(Context c){
	Context useCtx = null;
	
	try {
		useCtx = c.createPackageContext("com.ape.encryptmanager", 
			Context.CONTEXT_IGNORE_SECURITY);
	}
	catch (NameNotFoundException e) {
		Log.i(TAG, "NameNotFoundException:"+e);
		return false;
	}

	SharedPreferences mPreferences = useCtx.getSharedPreferences(PRE_FILES, 
		(Context.MODE_WORLD_READABLE |Context.MODE_MULTI_PROCESS));
	
	is_Open = mPreferences.getBoolean(PRE_SENSOR_BUTTON_ONOFF, DEFAULT_VALUE);
	Log.i(TAG, "updatePreference-is_Open:"+is_Open);
	return true;
}


}
