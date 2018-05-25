package com.ape.shortcutsUtils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;

public class PermissionUtils {

    public static final String TAG = PermissionUtils.class.getName();

    /**
     * Notification Listeners settings Key.
     */
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private static final String PREFERENCE_NAME = "notification_listener_preference";
    private static final String KEY_NOTIFICATION_CHECKED = "notification_checked";

    public static ArrayList<String> getGrantedPermissions(Context context, String packageName) {
        ArrayList<String> grantedPermissions = new ArrayList<>();
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);

            // Get Permissions
            String[] requestedPermissions = packageInfo.requestedPermissions;
            int[] requestedPermissionsFlags = packageInfo.requestedPermissionsFlags;

            if(requestedPermissions != null) {
                for (int i = 0; i < requestedPermissions.length; i++) {

                    int newReqFlags = requestedPermissionsFlags[i];
                    boolean isGranted =
                            ((newReqFlags&PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0);

                    if (isGranted) grantedPermissions.add(requestedPermissions[i]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return grantedPermissions;
    }

    public static boolean checkPermissions(Activity activity, String[] permissions, int requestCode) {

        ArrayList<String> missingPermissions = new ArrayList<>();

        for (String permission : permissions) {

            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (missingPermissions.size() > 0) {
            String[] perms = missingPermissions.toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(activity, perms, requestCode);
            return true;
        } else {
            return false;
        }
    }



    /**
     * Enable Notification Listener into the secure settings.
     * @param context Application Context.
     * @param packageName Package Name of the Notification Listener Service.
     * @param servicePackageAndClassName Class Name of the Notification Listener Service (with package name).
     */
    public static void enableNotificationListener(Context context, String packageName, String servicePackageAndClassName) {

        String notificationService = packageName + "/" + servicePackageAndClassName;
        String pkgName = context.getPackageName();
        ContentResolver contentResolver = context.getContentResolver();

        final String flat = Settings.Secure.getString(contentResolver, ENABLED_NOTIFICATION_LISTENERS);
        boolean isEnable = false;

        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        isEnable = true;
                        break;
                    }
                }
            }
        }
        if(!isEnable) {
            StringBuilder sb = new StringBuilder();
            if(!TextUtils.isEmpty(flat)) {
                sb.append(flat);
                sb.append(":");
            }
            sb.append(notificationService);
            try {
                Settings.Secure.putString(contentResolver,ENABLED_NOTIFICATION_LISTENERS, sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if the application has the Notification Service Listener enabled or not.
     * @param context Application Context
     * @param packageName Application Package Name
     * @return True if enabled
     */
    public static boolean isNotificationListenerEnabled(Context context, String packageName) {
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(packageName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Open the notification listener settings screen.
     * @param context Application Context
     * @param descriptionText Description text (optional)
     */
    public static void openNotificationAccess(Context context, String descriptionText) {

        if (descriptionText != null)
            Toast.makeText(context, descriptionText, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_NOTIFICATION_CHECKED, true);
        editor.commit();
    }

    /**
     * Indicate if the notification settings has been already prompt.
     * @param context Application Context
     * @return True if already prompt.
     */
    public static boolean hasNotificationSettingChecked(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_NOTIFICATION_CHECKED, false);
    }

    public static boolean forcePermission(Context context) {

        try {
            /*
            ContentResolver contentResolver = context.getContentResolver();
            Object value = Settings.Secure.getString(contentResolver, Manifest.permission.READ_CONTACTS);
            LogUtil.i(TAG, "" + value);
            Settings.Secure.putInt(contentResolver, Manifest.permission.READ_CONTACTS, 1);
            */

            PackageManager packageManager = context.getPackageManager();
            PermissionInfo permission = packageManager.getPermissionInfo(Manifest.permission.READ_CONTACTS, 0);
            packageManager.addPermission(permission);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
