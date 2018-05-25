package com.ape.encryptmanager.utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.net.Uri;

import com.android.internal.widget.LockPatternView;
import com.android.settings.R;
import com.ape.encryptmanager.provider.FingerPrintProvider;

import com.ape.emFramework.Log;
//import com.ape.fingerprint.WrapperManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.UserHandle;
import android.content.Intent;

public class EncryUtil {
    
public static final String TAG = "EncryUtil";

public static final String _ID = FingerPrintProvider.AppLockColumn.ID;
public static final String APP_NAME = FingerPrintProvider.AppLockColumn.APP_NAME;
public static final String PACKAGE_NAME = FingerPrintProvider.AppLockColumn.PACKAGE_NAME;
public static final String CLASS_NAME = FingerPrintProvider.AppLockColumn.CLASS_NAME;

public static final String ENCRYPT_ONOFF = FingerPrintProvider.AppLockEnableColumn.ENCRYPT_ONOFF;
public static final String APPLOCK_ONOFF = FingerPrintProvider.AppLockEnableColumn.APPLOCK_ONOFF;
public static final String FILELOCK_ONOFF = FingerPrintProvider.AppLockEnableColumn.FILELOCK_ONOFF;
public static final String GALLERY_ONOFF = FingerPrintProvider.AppLockEnableColumn.GALLERY_ONOFF;

public static final String TOUCH_CONTROL_ID = FingerPrintProvider.TouchControlColumn.ID;
public static final String FUNC_ONOFF = FingerPrintProvider.TouchControlColumn.FUNC_ONOFF;
public static final String TOUCH_LEFT = FingerPrintProvider.TouchControlColumn.TOUCH_LEFT;
public static final String TOUCH_RIGHT = FingerPrintProvider.TouchControlColumn.TOUCH_RIGHT;
public static final String TOUCH_DOWN = FingerPrintProvider.TouchControlColumn.TOUCH_DOWN;
public static final String TOUCH_PRESS_ONCE = FingerPrintProvider.TouchControlColumn.TOUCH_PRESS_ONCE;
public static final String TOUCH_HEAVY_PRESS_ONCE = FingerPrintProvider.TouchControlColumn.TOUCH_HEAVY_PRESS_ONCE;

public static final String TOUCH_LONGPRESS = FingerPrintProvider.TouchControlColumn.TOUCH_LONGPRESS;
public static final String TOUCH_DOUBLE_CLICK = FingerPrintProvider.TouchControlColumn.TOUCH_DOUBLE_CLICK;

public static final String FINGER_ENABLE_ID = FingerPrintProvider.FingerEnableColumn.ID;
public static final String FINGER_ENABLE_SCREEN_LOCK_ONOFF = FingerPrintProvider.FingerEnableColumn.SCREEN_LOCK_ONOFF;
public static final String FINGER_ENABLE_QUICK_BOOT_ONOFF = FingerPrintProvider.FingerEnableColumn.QUICK_BOOT_ONOFF;

public static final Uri URI_APPLOCK = Uri
		.parse("content://"+FingerPrintProvider.AUTHORITY+ "/applock");
public static final Uri URI_APPLOCK_ENABLE = Uri
		.parse("content://"+FingerPrintProvider.AUTHORITY+ "/applockenable");
public static final Uri URI_PASSWORD = Uri
		.parse("content://"+FingerPrintProvider.AUTHORITY+ "/password");
public static final Uri URI_TOUCH_CONTROL = Uri
		.parse("content://"+FingerPrintProvider.AUTHORITY+ "/touchcontrol");
public static final Uri URI_FINGERPRINT = Uri
		.parse("content://"+FingerPrintProvider.AUTHORITY+ "/fingerprint");
public static final Uri URI_FINGERPRINT_ENABLE = Uri
		.parse("content://"+FingerPrintProvider.AUTHORITY+ "/fingerprintenable");

// for quick boot
public static final int FINGER_FOR_QUICK_BOOT_RESULT_CODE = 1010;
public static final int CONTACT_FOR_QUICK_BOOT_RESULT_CODE = 1020;
public static final int APP_FOR_NORMAL_CUST_RESULT_CODE = 1030;
public static final int APP_FOR_QUICK_BOOT_RESULT_CODE = 1031;
public static final int FINGER_ENROLL_FOR_APP_LOCK_RESULT_CODE = 1032;
public static final int FINGER_ENROLL_FOR_SCREEN_LOCK_RESULT_CODE = 1033;

public static final int FINGER_ENROLL_FOR_QUICK_BOOT_RESULT_CODE = 1034;

public static final String KEY_QUICK_BOOT_FINGER_PRINT_NAME = "finger_print_name";
public static final String KEY_QUICK_BOOT_FINGER_PRINT_ID = "finger_print_id";

public static String KEY_QUICK_BOOT_CONTACT_NAME = "quick_boot_contact_name";
public static String KEY_QUICK_BOOT_CONTACT_NUMBER = "quick_boot_contact_number";
public static String KEY_QUICK_BOOT_CONTACT_PHOTO = "quick_boot_contact_photo";
public static String KEY_QUICK_BOOT_CONTACT_ID = "quick_boot_contact_ID";	

public static final String KEY_QUICK_BOOT_APP_NAME = "quick_boot_app_name";
public static final String KEY_QUICK_BOOT_PACKAGE_NAME = "quick_boot_package_name";
public static final String KEY_QUICK_BOOT_CLASS_NAME = "quick_boot_class_name";
public static final String KEY_QUICK_BOOT_APP_CATEGORY = "quick_boot_app_category";
public static final String KEY_QUICK_BOOT_APP_ACTION = "quick_boot_app_action";
public static final String KEY_QUICK_BOOT_APP_DATA = "quick_boot_app_data";


public static final String KEY_QUICK_BOOT_FINGERID = "quick_boot_fingerid";

public static final String KEY_APP_LOCK_FINGER_PRINT_NAME = "finger_name_for_app_lock";
public static final String KEY_APP_LOCK_FINGER_PRINT_ID = "finger_id_for_app_lock";

public static final String KEY_SCREEN_LOCK_FINGER_PRINT_NAME = "finger_name_for_screen_lock";
public static final String KEY_SCREEN_LOCK_FINGER_PRINT_ID = "finger_id_for_screen_lock";

public static final String KEY_QUICK_BOOT_DIALOG_FINGER_PRINT_NAME = "finger_name_for_quick_boot_dialog";
public static final String KEY_QUICK_BOOT_DIALOG_FINGER_PRINT_ID = "finger_id_for_quick_boot_dialog";	
public static final String KEY_QUICK_BOOT_DIALOG_QUICK_BOOT_POSITION = "quick_position_for_quick_boot_dialog";	

public static final String REQUEST_INTENT_FROM = "request_intent_from";

public static final String TAG_LOG = "TinnoEncryptService";

public static final int MSG_ENROLL_CREDENTIAL_RSP = 100;
public static final int MSG_VERIFY_CREDENTIAL_RSP = 101;
public static final int MSG_VKEY_CALLBACK = 102;

public static final int SL_TOUCH_TOO_FAST = -1005;
public static final int SL_RSP_SESSION_BUSY_IN_VERI = -4;
public static final int SL_RSP_SESSION_BUSY_IN_ENRO = -5;
public static final int SL_RSP_SESSION_BUSY_IN_WAKEUP_VERI = -6;

/*** Enroll START ****/
public static final int ENROLL_SUCCESS = 0;
public static final int ENROLL_CANCLED = -2;
public static final int ENROLL_NOT_SUPPORT = -3;
public static final int ENROLL_ERROR = -4;
public static final int ENROLL_FAIL = -105;
public static final int ENROLL_CHECK_ERROR = -108;

public static final int ENROLLING = 0;
public static final int REENROLL = 5;
public static final int ENROLL_INDEX = 1;
public static final int ENROLL_CREDENTIAL_RSP = 1;
/** for current single enroll failure due to image is not suitable */
public static final int SL_ENROLL_CURR_ENR_FAIL = -106;
/** for image is not good when finger is putting on chip */
public static final int SL_ENROLL_CURR_IMG_BAD = -107;

/*** Enroll END ****/

/***** IDENTIFY START ****/
public static final int IDENTIFY = 2;
public static final int IDENTIFY_SUCCESS = 0;
public static final int IDENTIFY_TMEOUT = -1;
public static final int IDENTIFY_CANCELED = -2;
public static final int IDENTIFY_ERR_MATCH = -3;
public static final int IDENTIFY_ERROR = -4;
public static final int IDENTIFY_FAIL = -5;
public static final int IDENTIFY_MAX = 5;

public static final int IDENTIFY_INDEX = 2;
public static final int IDENTIFY_CREDENTIAL_RSP = 0;
public static final int IDENTIFY_WAKEUP_NOT_MATCHED = -206;
/** success to wake by verify */
public static final int IDENTIFY_WAKEUP_MATCHED = -207;
public static final int IDENTIFY_WAKEUP_BAD_IMG = -208;
/** for image is not good when finger is put on chip */
public static final int IDENTIFY_CURR_IMG_BAD = -209;

/****** IDENTIFY END *******/

public final static String ERROR_NOTIFY_DIALOG_FINISH_ACTION = "com.ape.encryptmanager.fp.lockscreen.action.finish";
public final static String ACTION_NONSUPPORT_LOCKSCREEN_ACTIVE = "com.ape.encryptmanager.fp.Settings.NONSUPPORT_LOCKSCREEN_ACTIVE";
/** go to FpAppsManagerActivity action */
public final static String APPS_MANAGER_ACTION = "com.ape.encryptmanager.fp.action.VIEW";
/** go to FpAppsManagerActivity category */
public final static String APPS_MANAGER_CATEGORY = "com.ape.encryptmanager.fp.category.appsmanager";

public final static String ACTION_FP_TOUCH_EVENT = "action.fingerprint.touch.event";

public final static String ACTION_FP_ENTRY_EVENT = "action.fingerprint.entry.event";



public static final String QUERY_ENCRYPT_ONOFF[] = { _ID, ENCRYPT_ONOFF,
		APPLOCK_ONOFF, FILELOCK_ONOFF, GALLERY_ONOFF, };

public static final String QUERY_APPS_DATA[] = { _ID, APP_NAME,
		PACKAGE_NAME, CLASS_NAME, };

public static final String QUERY_TOUCH_CONTROL_DATA[] = { TOUCH_CONTROL_ID,
		FUNC_ONOFF, TOUCH_LEFT, TOUCH_RIGHT, TOUCH_DOWN, TOUCH_PRESS_ONCE,TOUCH_HEAVY_PRESS_ONCE,
		TOUCH_LONGPRESS, TOUCH_DOUBLE_CLICK, };

public static final String QUERY_FINGER_ENABLE_ONOFF_ITEMS[] = {
		FINGER_ENABLE_ID, FINGER_ENABLE_SCREEN_LOCK_ONOFF,
		FINGER_ENABLE_QUICK_BOOT_ONOFF,

};

public final static String REQ_KEY = "REQ_KEY";
public final static String RESULT_KEY = "RESULT_KEY";
public final static String TOKEN_KEY = "TOKEN_KEY";
public final static String FORGET_PASSWORD_KEY = "FORGET_PASSWORD_KEY";

/*
 * PASSWORD_CHECK_TYPE 0 is number password ,1 is Pattern password
 */
public final static String PASSWORD_CHECK_TYPE_KEY = "PASSWORD_CHECK_TYPE_KEY";
public final static String PASSWORD_SETTING_TYPE_KEY = "PASSWORD_SETTING_TYPE_KEY";
public static final int NUMBER_PASSWORD = 0;
public static final int PATTERN_PASSWORD = 1;

public static final String PRE_APP_NAME = "PRE_APP_NAME";
public static final String PRE_PACKAGE_NAME = "PRE_PACKAGE_NAME";
public static final String PRE_CLASS_NAME = "PRE_CLASS_NAME";

/*
 * fingerprint ic Suppliers HUIDING
 */
public static final int FINGERPRINT_IC_TYPE_GOODIX = 0;
public static final int FINGERPRINT_IC_TYPE_SILEAD = 1;
public static final int FINGERPRINT_IC_TYPE_ELAN = 2;


public static final int FINGER_MAX_NUMBER =5;

/*
 * fingerprint register type
 */
public static final int FINGERPRINT_REGISTER_VERIFY_TYPE = 0;
public static final int FINGERPRINT_REGISTER_ENROLL_TYPE = 1;

// get fingerprint chip info.
public static final String FINGERPRINT_GOODIX_DEV_NAME = "goodix_fp";
public static final String FINGERPRINT_SILEAD_DEV_NAME = "silead_fp_dev";
public static final String FINGERPRINT_ELAN_DEV_NAME = "elan_fp";
public static String fingerPrintName = getFp_DrvInfo();
private static final String FP_DRV_INFO = "/sys/devices/platform/fp_drv/fp_drv_info";

public static String getFp_DrvInfo() {
    try {
        final String info = readLine(FP_DRV_INFO);
        Log.i(TAG, "getFp_DrvInfo:"+ info);
        return info;
    } catch (IOException e) {
        Log.e(TAG, "IO Exception when getting FP_DEV_INFO:"+ e);
        return "Unavailable";
    }
}

private static String readLine(String filename) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
    try {
       return reader.readLine();
    } finally {
       reader.close();
    }
}

public static boolean isElanChip() {
    return FINGERPRINT_ELAN_DEV_NAME.equals(fingerPrintName);
}
public static boolean isGoodixChip() {
    return FINGERPRINT_GOODIX_DEV_NAME.equals(fingerPrintName);
}
public static boolean isSileadChip() {
    return FINGERPRINT_SILEAD_DEV_NAME.equals(fingerPrintName);
}


public static void setLockpatternViewAttrsValue(Context context,LockPatternView lockPatternView){
    //if(WrapperManager.isMtkPlateform())
    if(true)
        return;
    try{
        Field mRegularColor = lockPatternView.getClass().getDeclaredField("mRegularColor");
        Field mErrorColor = lockPatternView.getClass().getDeclaredField("mErrorColor");
        Field mSuccessColor = lockPatternView.getClass().getDeclaredField("mSuccessColor");
        Log.d(TAG, "mRegularColor --->"+mRegularColor + "| lockPatternView.getClass() = " + lockPatternView.getClass());
        mRegularColor.setAccessible(true);
        mRegularColor.setInt(lockPatternView, context.getResources().getColor(R.color.setup_lock_pattern_view_regular_color_light));
        mErrorColor.setAccessible(true);
        mErrorColor.setInt(lockPatternView, context.getResources().getColor(R.color.setup_lock_pattern_view_error_color_light));
        mSuccessColor.setAccessible(true);
        mSuccessColor.setInt(lockPatternView, context.getResources().getColor(R.color.setup_lock_pattern_view_success_color_ext));
    } catch (Exception e){
        // TODO Auto-generated catch block
        Log.d(TAG, "lockPatternView Exception--->" + e.toString());
    }
}


public static void sendFingerprintEntryBroadcast(Context context) {
    Intent intent=new Intent(ACTION_FP_ENTRY_EVENT);  
    context.sendBroadcastAsUser(intent, UserHandle.CURRENT, null);        
}
public static final int TAG_OTHER = 0;
public static final int TAG_PRIVATE_FMGR = 1;
public static final int TAG_KEYGUARD_BINDER_CALL = 2;
public static final int TAG_USER_PRESENT = 3;
public static final int TAG_FINGERPRINT_LIST = 4;
public static final int TAG_APPLOCK = 5;
public static final int TAG_SIM_STATE_CHANGE = 6;
public static final int TAG_VERIFY_SUCCESS = 7;
public static final int TAG_VERIFY_ERR = 8;
public static final int TAG_SCREEN_ON_OFF = 9;
public static final int TAG_AIRPLANE_MODE_CHANGED = 10;
public static final int TAG_COUNTDOWN_TIMER_ON_FINISH = 11;
public static final int TAG_DO_UNLOCK_SCREEN = 12;
public static final int TAG_VERIFY_ERR_LOCKOUT = 13;



public static String parseTag(int tag) {
    switch (tag) {
        case EncryUtil.TAG_OTHER:
            return "TAG_OTHER";
        case EncryUtil.TAG_PRIVATE_FMGR:
            return "TAG_PRIVATE_FMGR";
        case EncryUtil.TAG_KEYGUARD_BINDER_CALL:
            return "TAG_KEYGUARD_BINDER_CALL";
        case EncryUtil.TAG_USER_PRESENT:
            return "TAG_USER_PRESENT";
        case EncryUtil.TAG_FINGERPRINT_LIST:
            return "TAG_FINGERPRINT_LIST";
        case EncryUtil.TAG_APPLOCK:
            return "TAG_APPLOCK";
        case EncryUtil.TAG_SIM_STATE_CHANGE:
            return "TAG_SIM_STATE_CHANGE";
        case EncryUtil.TAG_VERIFY_SUCCESS:
            return "TAG_VERIFY_SUCCESS";
        case EncryUtil.TAG_VERIFY_ERR:
            return "TAG_VERIFY_ERR";
        case EncryUtil.TAG_SCREEN_ON_OFF:
            return "TAG_SCREEN_ON_OFF";
        case EncryUtil.TAG_AIRPLANE_MODE_CHANGED:
            return "TAG_AIRPLANE_MODE_CHANGED";
        case EncryUtil.TAG_COUNTDOWN_TIMER_ON_FINISH:
            return "TAG_COUNTDOWN_TIMER_ON_FINISH";
        case EncryUtil.TAG_DO_UNLOCK_SCREEN:
            return "TAG_DO_UNLOCK_SCREEN";
        case EncryUtil.TAG_VERIFY_ERR_LOCKOUT:
            return "TAG_VERIFY_ERR_LOCKOUT";
    }

    return tag+"";
}

}
