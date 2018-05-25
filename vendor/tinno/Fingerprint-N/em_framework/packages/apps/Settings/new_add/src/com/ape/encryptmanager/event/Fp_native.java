package com.ape.encryptmanager.event;

import com.ape.emFramework.Log;

public class Fp_native {

	private static final String TAG = "Fp_native-cls";
	private OnReportListerner mOnReportListerner = null;

	static
	{
		System.loadLibrary("fp_dev_jni");
	}

	public static native int native_init(int set);
	public static native int native_update();
	public static native int native_setObj(Fp_native f);

	public static boolean init(int s, OnReportListerner l) {
		if(native_init(s) < 0) {
			Log.i(TAG, "native_init:fail");
			return false;
		}

		Fp_native fn = new Fp_native();
		fn.native_setObj(fn);
		fn.setOnReportListerner(l);
		return true;
	}

	//native call
	public int onEventReport(int e) {
		mOnReportListerner.OnReport(e);
		return 0;
	}

	public interface OnReportListerner {
		boolean OnReport(int event);
	}

	public void setOnReportListerner(OnReportListerner l) {
		mOnReportListerner = l;
	}

}
