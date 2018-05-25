package com.android.server.fingerprint;
import android.os.Handler;
import com.ape.emFramework.Log;
import com.android.internal.policy.IKeyguardService;
import android.os.RemoteException;

public class PlatfomDeps {

static final String TAG = "PlatfomDeps";

public static void dismiss(IKeyguardService service, boolean allowWhileOccluded) throws RemoteException {
    try {
        service.dismiss();
    }
    catch (RemoteException e) {
        Log.e(TAG, "RemoteException :"+e);
    }
}

}
