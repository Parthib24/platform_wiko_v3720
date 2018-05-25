package com.android.server.fingerprint;
import android.os.Handler;
import com.ape.emFramework.Log;

public class FingerprintNative {

private static final String TAG = "FingerprintNative";
private OnReportListerner mOnReportListerner = null;
private Handler mHandler = new Handler();

public static native int native_init(int set);
public static native int native_setObj(FingerprintNative f);
public static native int native_bl_ctl(int val, int brightness);
public static native int native_get_bl();

public static FingerprintNative start(int s, OnReportListerner l) {
	if(native_init(s) < 0) {
		Log.i(TAG, "native_init:fail");
		return null;
	}

	FingerprintNative fn = new FingerprintNative();
	fn.native_setObj(fn);
	fn.setOnReportListerner(l);
	return fn;
}

//Native call
public int onEventReport(int e) {
   mHandler.post(new Runnable() {
      @Override
      public void run() {
          if (mOnReportListerner != null)
              mOnReportListerner.OnReport(e);
      }
   });
   return 0;
}

public interface OnReportListerner {
	boolean OnReport(int event);
}

public void setOnReportListerner(OnReportListerner l) {
	mOnReportListerner = l;
}

}
