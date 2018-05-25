package com.android.server.fingerprint;
import android.os.Handler;
import com.ape.emFramework.Log;

public class TpNative {

private static final String TAG = "TpNative";
private OnReportListerner mOnReportListerner = null;
private Handler mHandler = new Handler();

public static native int native_init(int set);
public static native int native_setObj(TpNative f);

public static boolean start(int s, OnReportListerner l) {
	if(native_init(s) < 0) {
		Log.i(TAG, "native_init:fail");
		return false;
	}

	TpNative fn = new TpNative();
	fn.native_setObj(fn);
	fn.setOnReportListerner(l);
        Log.i(TAG, "native_init:start success!");
	return true;
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
