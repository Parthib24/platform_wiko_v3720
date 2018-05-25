package com.android.server.fingerprint;
import android.os.Handler;
import com.ape.emFramework.Log;

public class FingerprintWrapperImlp {

private static final String TAG = "FingerprintWrapperImlp";


public static boolean canUseFingerprint(String opPackageName, boolean requireForeground, int uid, int pid) {

    if ("com.android.settings".equals(opPackageName)) {
        return true;
    }
    
    if ("com.ape.applock".equals(opPackageName)) {
        return true;
    }
    
    if ("com.sugar.applock".equals(opPackageName)) {
        return true;
    }

    if ("com.myos.applock".equals(opPackageName)) {
        return true;
    }

    return false;
}


}
