package com.ape.backtouch.event;

import android.util.Log;

/**
 * Created by linzhiqin on 9/7/16.
 */
public class bt_native {

    private static final String TAG = "bt_native";
    private OnReportListerner mOnReportListerner = null;

    static
    {
        System.loadLibrary("bt_dev_jni");
    }

    public static native int native_init(int set);
    public static native int native_update();
    public static native int native_setObj(bt_native f);

    public static boolean init(int s, OnReportListerner l) {
        Log.d(TAG,"init ");
        if(native_init(s) < 0) {
            Log.i(TAG, "native_init:fail");
            return false;
        }
        bt_native fn = new bt_native();
        fn.native_setObj(fn);
        fn.setOnReportListerner(l);
        return true;
    }

    //native call
    public int onEventReport(int e) {
        mOnReportListerner.OnReport(e);
        Log.d(TAG,"e = " + e);
        return 0;
    }

    public interface OnReportListerner {
        boolean OnReport(int event);
    }

    public void setOnReportListerner(OnReportListerner l) {
        mOnReportListerner = l;
    }
}
