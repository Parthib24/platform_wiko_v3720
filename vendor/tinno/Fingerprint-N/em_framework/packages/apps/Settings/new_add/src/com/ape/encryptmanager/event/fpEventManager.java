package com.ape.encryptmanager.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.ape.emFramework.Log;

public class fpEventManager implements Fp_native.OnReportListerner {

public static final String TAG = "fpEventManager";
private Context  mContext;
private static fpEventManager instance;
public static final boolean DEFAULT = FpTouchEventHandle.DEFAULT_VALUE;
private FpTouchEventHandle mEh;

public fpEventManager(Context c) {
	mContext = c;
    mEh = FpTouchEventHandle.getInsance(c);
}

public static fpEventManager getInsance(Context c) {
	if(instance == null) {
		instance = new fpEventManager(c);
	}
	return instance;
}

public void disable(boolean b) {
	FpTouchEventHandle.getInsance(mContext).disable(b);
}

public void setOccluded(boolean occ) {
	FpTouchEventHandle.getInsance(mContext).setOccluded(occ);
}

public boolean start() {
	if(Fp_native.native_init(0) < 0) {
		return false;
	}

	new Thread(new Runnable() {
		@Override
		public void run() {
			while(true) {
				try {
					int event = Fp_native.native_update();
					if (event > 0) {
						Log.i(TAG, "event:"+event);
						mEh.fp_handleKeyEvent(event);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}).start();

	return  true;
}

public boolean start(int s) {
	Log.i(TAG, "start:" + s);

	if(s == 0) {
		start();
	}
	else {
		Fp_native fn = new Fp_native();
		fn.init(1, fpEventManager.this);
	}
	return  true;
}

public boolean OnReport(int e) {
	Log.i(TAG, "OnReport:" + e);
	mEh.fp_handleKeyEvent(e);
	return false;
}

public static void updatePreference(Context c){
	FpTouchEventHandle.getInsance(c).updatePreference(c); 
}



}
