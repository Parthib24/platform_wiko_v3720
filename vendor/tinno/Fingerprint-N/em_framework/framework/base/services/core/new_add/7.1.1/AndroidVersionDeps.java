package com.android.server.fingerprint;
import android.app.ActivityManagerNative;
import android.os.Handler;
import com.ape.emFramework.Log;
import android.app.IUserSwitchObserver;
import android.os.RemoteException;

public class AndroidVersionDeps {

static final String TAG = "AndroidVersionDeps";

// Android 7.1.1
public static void registerUserSwitchObserver(IUserSwitchObserver observer, String name) throws RemoteException {
    try {
        ActivityManagerNative.getDefault().registerUserSwitchObserver(observer, name);
    }
    catch (RemoteException e) {
        Log.e(TAG, "Failed to listen for user switching event");
    }
}

}
