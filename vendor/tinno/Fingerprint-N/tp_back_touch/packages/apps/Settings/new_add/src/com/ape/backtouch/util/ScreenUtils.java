package com.ape.backtouch.util;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.PowerManager;
import android.util.Log;
import java.util.List;

/**
 * Created by linzhiqin on 9/18/16.
 */
public class ScreenUtils {
    private static final String TAG = "ScreenUtils";
    private Context mContext;
    private static final String MYOS_CAEMRA_PACKAGE = "com.myos.camera";
    private static final String MYOS_DIALER_PACKAGE = "com.android.dialer";

    public boolean isScreenOn = true;

    public ScreenUtils(Context context){
        this.mContext = context;
        registerScreenActionReceiver();
    }

    public boolean isCameraActivity(){
        if (getTop().contains(MYOS_CAEMRA_PACKAGE)){
            return true;
        }
        return false;
    }

    public boolean isDialerActivity(){
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

    public boolean isKeyguardOn(){
        final KeyguardManager mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    public void wakeUp(){
        PowerManager pm=(PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,TAG);
        wl.acquire();
        wl.release();
    }
}
