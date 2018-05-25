package com.ape.backtouch.event;

import android.content.Context;

/**
 * Created by zhiqin.lin on 9/5/16.
 */
public class BtEventManager implements bt_native.OnReportListerner {

    public static final String TAG = "BtEventManager";
    public static BtEventManager instance;
    private Context  mContext;
    private BtTouchEventHandle mBtTouchEventHandle;

    public BtEventManager(Context c) {
        mContext = c;
        mBtTouchEventHandle = new BtTouchEventHandle(c);
    }

    public static BtEventManager getInsance(Context c) {
        if(instance == null) {
            instance = new BtEventManager(c);
        }
        return instance;
    }

    public boolean start() {
        bt_native fn = new bt_native();
        fn.init(1, BtEventManager.this);
        return true;
    }

    @Override
    public boolean OnReport(int event) {
        mBtTouchEventHandle.handleTouchEvent(event);
        return false;
    }
}
