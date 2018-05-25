package com.ape.encryptmanager.testtool;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import com.ape.emFramework.Log;

public class FingerprintTestTool {
        public static final String TAG = "FingerprintTestTool";
        public static final String FINGERPRINT_TEST_TOOL_BROADCAST = "fingerprint.test.tool.broadcast";
        public static final String TOUCH_TIME_KEY = "touch_time";
        public static final String MSG_TYPE_KEY = "message_type";
        public static final String MSG_START_UNLOCKSCREEN_TIME_KEY = "start_unlockscreen_time";
        public static final String MSG_WAKEUP_LOCK_KEY = "wakeup_lock";
        

        public static final int MSG_FINGERPRINT_VERIFY_SUCCESS = 1;
        public static final int MSG_FINGERPRINT_VERIFY_FAILED = 2;

        private static long mTouchTime = -1;

        public static void setTouchTime() {
                mTouchTime = new Date().getTime();
                Log.d(TAG, "setTouchTime  ouchTime = " + mTouchTime);
        }

        public static void setTouchTime(long touchTime) {

                mTouchTime = touchTime;
                Log.d(TAG, "setTouchTime  ouchTime = " + mTouchTime);
        }

        public static void sendBroadcastTimeData(Context context, int MsgType, long unlockScreenTime , Boolean wakeupLock) {

                Log.d(TAG, "sendBroadcastTimeData   touchTime = " + mTouchTime+ " -----unlockScreenTime="+unlockScreenTime);
                Log.d(TAG, "sendBroadcastTimeData   MsgType = " + MsgType);
                
                Intent intent = new Intent(FINGERPRINT_TEST_TOOL_BROADCAST);
                intent.putExtra(TOUCH_TIME_KEY, mTouchTime);
                intent.putExtra(MSG_START_UNLOCKSCREEN_TIME_KEY, unlockScreenTime);
                intent.putExtra(MSG_TYPE_KEY, MsgType);
                intent.putExtra(MSG_WAKEUP_LOCK_KEY, wakeupLock);
                context.sendBroadcast(intent);
        }

        public static void sendBroadcastTimeData(Context context, int MsgType,Boolean wakeupLock) {
                Log.d(TAG, "sendBroadcastTimeData   touchTime = " + mTouchTime+ " -----unlockScreenTime="+ new Date().getTime());
                Log.d(TAG, "sendBroadcastTimeData   MsgType = " + MsgType);
                
                Intent intent = new Intent(FINGERPRINT_TEST_TOOL_BROADCAST);
                intent.putExtra(MSG_START_UNLOCKSCREEN_TIME_KEY, new Date().getTime());
                intent.putExtra(TOUCH_TIME_KEY, mTouchTime);
                intent.putExtra(MSG_TYPE_KEY, MsgType);
                intent.putExtra(MSG_WAKEUP_LOCK_KEY, wakeupLock);
                context.sendBroadcast(intent);
        }

}
