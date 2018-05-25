package com.ape.fpShortcuts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

//import com.wiko.tracking.TrackingUtils;

/**
 * Created by christopher ney on 18/10/2016.
 */
public class ShortcutVoiceMail extends Shortcut {

    public void init(Context context) {
        try {
            super.init(context);
            String voiceMailNumber = "";
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                voiceMailNumber = telephonyManager.getVoiceMailNumber();
                if (voiceMailNumber == null) voiceMailNumber = "";
            }
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("tel:" + voiceMailNumber));
            this.intent = intent;
        } catch (Exception ex) {
            //TrackingUtils.logException(ex);
        }
    }

    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE
        };
    }

}
