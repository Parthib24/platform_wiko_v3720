package com.ape.backtouch.event;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.ape.backtouch.model.ActionModel;
import com.ape.backtouch.model.EventModel;
import com.ape.backtouch.util.ScreenUtils;

/**
 * Created by linzhiqin on 9/8/16.
 */
public class BtTouchEventHandle {
    private static final String TAG = "BtTouchEventHandle";
    private ActionModel mCurActionModel;
    private EventModel mEventModel;

    private static final int SINGLE_CLICK_DOWN_EVENT = 10000;
    private static final int SINGLE_CLICK_UP_EVENT = 10001;
    private static final int LONG_CLICK_DOWN_EVENT = 10002;
    private static final int LONG_CLICK_UP_EVENT = 10003;
    private static final int DOUBLE_CLICK_EVENT = 10004;

    public static final int LONG_CLICK_TIME = 700;
    public static final int DOUBLE_CLICK_TIME =300;
    public static final int COUNT_TIME = 100;

    private boolean isPreSingleClick = false;
    private long mPreSingleClickUptime = 0;
    private Context mContext;
    private ScreenUtils mScreenUtils;
    private BtConfig mBtConfig;

    public BtTouchEventHandle(Context c){
        mEventModel = new EventModel();
        mContext = c;
        mScreenUtils = new ScreenUtils(c);
        mBtConfig = new BtConfig(c);
    }

    /**
     * This Handler class should be static or leaks might occur
     * preference ->Lint ->HandlerLeak,ignore
     */
    Handler mEventHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SINGLE_CLICK_DOWN_EVENT:
                    Log.d(TAG,"######SINGLE_CLICK_DOWN_EVENT#####");
                    break;
                case SINGLE_CLICK_UP_EVENT:
                    Log.d(TAG, "######SINGLE_CLICK_UP_EVENT######");
                    backTouchSingleClick("up");
                    break;
                case LONG_CLICK_DOWN_EVENT:
                    Log.d(TAG, "######LONG_CLICK_DOWN_EVENT######");
                    backTouchLongPress("down");
                    break;
                case LONG_CLICK_UP_EVENT:
                    Log.d(TAG,"######LONG_CLICK_UP_EVENT######");
                    break;
                case DOUBLE_CLICK_EVENT:
                    Log.d(TAG, "######DOUBLE_CLICK_EVENT######");
                    backTouchDoubleClick();
                    break;
            }
        }
    };

    public void handleTouchEvent(int event){
        mCurActionModel = setActionModel(event);
        setEventModel(mCurActionModel);

        if (!mBtConfig.isBtEnable()){
            Log.d(TAG,"Back touch is close");
            return;
        }

        if (mCurActionModel.getType().equals("down")){
            mEventHandler.sendEmptyMessage(SINGLE_CLICK_DOWN_EVENT);
            mHandler.postDelayed(mLongClickRunnable, COUNT_TIME);
        }else if (mCurActionModel.getType().equals("up")){

            if (mEventModel.getDownAction() == null || mEventModel.getUpAction() == null){
                return;
            }

            mHandler.removeCallbacks(mLongClickRunnable);

            if (mEventModel.getUpAction().getTime() - mEventModel.getDownAction().getTime() >= LONG_CLICK_TIME){
                mEventHandler.sendEmptyMessage(LONG_CLICK_UP_EVENT);
                isPreSingleClick = false;
            } else if (mEventModel.getUpAction().getTime() - mEventModel.getDownAction().getTime() < LONG_CLICK_TIME){
                if (isPreSingleClick && (mEventModel.getUpAction().getTime() - mPreSingleClickUptime < DOUBLE_CLICK_TIME)) {
                    mEventHandler.sendEmptyMessage(DOUBLE_CLICK_EVENT);
                    isPreSingleClick = false;
                    mHandler.removeCallbacks(mSingleClickRunnable);
                }else{
                    isPreSingleClick = true;
                    mPreSingleClickUptime = mEventModel.getUpAction().getTime();
                    mHandler.postDelayed(mSingleClickRunnable,DOUBLE_CLICK_TIME);
                }
            }

            mEventModel = new EventModel();
        }
    }

    private ActionModel setActionModel(int event){
        ActionModel mActionModel = new ActionModel();
        if (event == 1)
            mActionModel.setType("down");
        else
            mActionModel.setType("up");
        mActionModel.setTime(SystemClock.uptimeMillis());
        return mActionModel;
    }

    private void setEventModel(ActionModel mActionModel){
        if (mActionModel.getType().equals("down") && mEventModel.getDownAction() == null){
            mEventModel.setDownAction(mActionModel);
            mEventModel.getDownAction().setCount(0);
        }else if (mActionModel.getType().equals("up")) {
            mEventModel.setUpAction(mActionModel);
            mEventModel.getUpAction().setCount(0);
        }
    }

    Handler mHandler = new Handler();
    Runnable mLongClickRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mEventModel.getDownAction().setCount(mEventModel.getDownAction().getCount() + 1);
                if (mEventModel.getDownAction().getCount() > LONG_CLICK_TIME/COUNT_TIME){
                    mEventHandler.sendEmptyMessage(LONG_CLICK_DOWN_EVENT);
                }else {
                    mHandler.postDelayed(this, COUNT_TIME);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    Runnable mSingleClickRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mEventHandler.sendEmptyMessage(SINGLE_CLICK_UP_EVENT);
            }catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    TelephonyManager getTelecommService() {
        return (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    private void backTouchSingleClick(String action) {
        TelephonyManager tm = getTelecommService();

        if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) //telephone is normal
        {
            if (!mScreenUtils.isScreenOn){
                backTouchOpenCamera();
            } else if (mScreenUtils.isCameraActivity()) {
                backTouchCameraCaptureBroadCast(action);
            }
        }else if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING){ //telephone is ring
            Log.d(TAG, "telephone is ring");
        }
    }

    private void backTouchDoubleClick() {
        TelephonyManager tm = getTelecommService();

        if (tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK){
            //telephone is calling
        } else if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING){
            // telephone is ring
            if (mScreenUtils.isScreenOn && !mScreenUtils.isKeyguardOn()) {
                backTouchCall("popcall");
            }else{
                backTouchCall("dialercall");
            }
        } else if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
            //telephone is normal
            backTouchOpenCamera();
        }
    }

    private void backTouchLongPress(String action) {
        TelephonyManager tm = getTelecommService();
        if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) //telephone is normal
        {
            backTouchCameraRoateBroadCast(action);
        }
    }

    /**
     * exported="true" dialer apk.
     * @param type  popcall or dialercall
     */
    private void backTouchCall(String type) {
        if (!mBtConfig.isBtCallEnable()){
            Log.d(TAG,"Call function is close");
            return;
        }

        Intent intent_answer = new Intent();
        intent_answer.setAction("com.incallui.backtouch.call");
        intent_answer.putExtra("type", type);
        mContext.sendBroadcast(intent_answer);
    }

    private void backTouchCameraRoateBroadCast(String action) {
        if (!mBtConfig.isBtRoateEnable()){
            Log.d(TAG,"Roate function is close");
            return;
        }

        Intent intent_Roate = new Intent();
        intent_Roate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent_Roate.setAction("com.camera.bk.function");
        intent_Roate.putExtra("type", "longpress");
        intent_Roate.putExtra("action", action);
        mContext.sendBroadcast(intent_Roate);
    }

    private void backTouchCameraCaptureBroadCast(String action) {
        if (!mBtConfig.isBtPhotosEnable()){
            Log.d(TAG,"Capture function is close");
            return;
        }

        Intent intent_Capture = new Intent();
        intent_Capture.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent_Capture.setAction("com.camera.bk.function");
        intent_Capture.putExtra("type", "click");
        intent_Capture.putExtra("action", action);
        mContext.sendBroadcast(intent_Capture);
    }

    private void backTouchOpenCamera(){
        if (!mBtConfig.isBtCameraEnable()){
            Log.d(TAG,"OpenCamera function is close");
            return;
        }

        mScreenUtils.wakeUp();

        if (mScreenUtils.isKeyguardOn()) {
            Intent intent_openCamera = new Intent();
            intent_openCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent_openCamera.setAction("com.backtouch.open.camera");
            mContext.sendBroadcast(intent_openCamera);
        }else {
            Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
}
