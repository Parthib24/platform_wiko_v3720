/**
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.hardware.fingerprint;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.keystore.AndroidKeyStoreProvider;
//import android.util.Log;
import android.util.Slog;
import android.app.KeyguardManager;
import java.security.Signature;
import android.hardware.fingerprint.FingerprintManager;


public class FpmWarpper {
    private static final String TAG = "FingerprintManager";
 	

public static class AppClientCallback {
public boolean onFingerprintEvent(int e) { return false; }
};

public static void registerFpService(Context c, AppClient client, int event , int type) {

        FingerprintManager mFpm = (FingerprintManager) c.getSystemService(Context.FINGERPRINT_SERVICE);
        mFpm.registerFpService(c.getPackageName(), new FingerprintManager.AppClientCallback() {
            @Override
            public boolean onFingerprintEvent(int e) {
                /*
                private final static int HW_EVT_MOVE_UP = 3;
                private final static int HW_EVT_MOVE_DOWN = 4;
                private final static int HW_EVT_MOVE_LEFT = 5;
                private final static int HW_EVT_MOVE_RIGHT = 6;
                private static final int SW_EVT_DOUBLE_CLICK = 10;
                private static final int SW_EVT_LONG_PRESS = 11;
                private static final int SW_EVT_GO_BACK = 12;
                */
                //Log.i(TAG, "onFingerprintEvent:" + e);
		if (client == null) 
  			return false;

                return client.onCb(e);
            }
        }, event, type);
}

public static void unregisterFpService(Context c) {
        FingerprintManager mFpm = (FingerprintManager) c.getSystemService(Context.FINGERPRINT_SERVICE);
        mFpm.unregisterFpService(c.getPackageName());
}


}








