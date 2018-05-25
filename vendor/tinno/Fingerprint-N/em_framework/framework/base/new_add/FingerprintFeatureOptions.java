package com.ape.emFramework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.android.internal.util.XmlUtils;


import android.os.Environment;
import android.os.UserHandle;
import com.ape.emFramework.Log;
import android.util.Xml;
import java.util.HashMap;
import java.util.Map;

public class FingerprintFeatureOptions {
    public static final String TAG = "FingerprintFeatureOptions";
    public static final boolean DEBUG = false;
    public static String FEATURE_FINGERPRINT_TOUCH_DEFAULT_STATUS = "feature_fingerprint_touch_default_status";
    public static String FEATURE_FINGERPRINT_BACK_NAVIGATION_DEFAULT_STATUS = "feature_fingerprint_back_navigation_default_status";
    public static String FEATURE_FINGERPRINT_IS_BACK_SENSOR = "feature_fingerprint_is_back_sensor";
    public static String FEATURE_FINGERPRINT_IS_CAMERA_CAPTURE = "feature_fingerprint_is_camera_capture";
    public static String FEATURE_FINGERPRINT_TZ_LOG_ENABLE_DEFAULT_STATUS = "feature_fingerprint_tz_log_enable_default_status";

    public static String FEATURE_FINGERPRINT_QWK_BL_TRIGGER_DELAY = "feature_fingerprint_qwk_bl_trigger_delay";
    public static String FEATURE_FINGERPRINT_QWK_WAKEUP_FINISH_DELAY = "feature_fingerprint_qwk_wakeup_finish_delay";
    public static String FEATURE_FINGERPRINT_BACK_KEY_VIB_TIME = "feature_fingerprint_back_key_vib_time";
    public static String FEATURE_FINGERPRINT_AUTH_SUCCESS_VIB_TIME = "feature_fingerprint_auth_success_vib_time";


    public static final String FINGER_CONFIG_FILE = "etc/finger_config.xml";
    private static Map mMap = new HashMap<String, String>();


    private static void initLoadOptions() {
        if (mMap.size() > 0) {
            return ;        
        }

        if (DEBUG) Log.d(TAG, "FingerprintFeatureOptions start initLoadOptions...");
        FileReader settingsReader;
        final File settingsFile = new File(Environment.getRootDirectory(), FINGER_CONFIG_FILE);
        try {
            settingsReader = new FileReader(settingsFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + Environment.getRootDirectory() + "/" + FINGER_CONFIG_FILE);
            return;
        }

        try { 
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(settingsReader);
                XmlUtils.beginDocument(parser, "resources");
                while (true) {
                    XmlUtils.nextElement(parser);
                    String name = parser.getName();
                    if (!"setting".equals(name)) {
                        break;
                    }

                    String key_name  = parser.getAttributeValue(null, "name");
                    String key_value = parser.getAttributeValue(null, "value");
                    if (DEBUG) Log.d(TAG, "<setting " + "\" name=\"" + key_name + "\" value=\"" + key_value + "\"/>");
                    if(mMap != null && key_name !=null && key_value != null){
                        mMap.put(key_name, key_value);
                    }

                }

            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in fingerprint-conf parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in fingerprint-conf parser " + e);
            }
        } finally {

            try{
                if(settingsReader != null) settingsReader.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception in fingerprint-conf parser " + e);
            }

        }

    }

    public static boolean getBoolean(String key ,boolean defValue){
        initLoadOptions();
        String s = (String)mMap.get(key);
        if(s == null || s.length() == 0){
            return defValue;
        }
        try{
            return Boolean.parseBoolean(s);
        }catch (Exception e) {
            Log.w(TAG, "Exception in parseBoolean: " + e);
            return defValue;
        }
    }

    public static String getString(String key ,String defValue){
        initLoadOptions();
        String s = (String)mMap.get(key);
        if(s == null || s.length() == 0){
            return defValue;
        }
        return s;    

    }

    public static int getInt(String key ,int defValue){
        initLoadOptions();
        String s = (String)mMap.get(key);
        if(s == null || s.length() == 0){
            return defValue;
        }
        try{
            return Integer.parseInt(s);
        }catch (Exception e) {
            Log.w(TAG, "Exception in parseInt: " + e);
            return defValue;
        }
    }

    public static long getLong(String key ,long defValue){
        initLoadOptions();
        String s = (String)mMap.get(key);
        if(s == null || s.length() == 0){
            return defValue;
        }
        try{
            return  Long.parseLong(s);
        }catch (Exception e) {
            Log.w(TAG, "Exception in parseLong: " + e);
            return defValue;
        }
    }

    public static double getDouble(String key ,double defValue){
        initLoadOptions();
        String s = (String)mMap.get(key);
        if(s == null || s.length() == 0){
            return defValue;
        }
        try{
            return Double.parseDouble(s);
        }catch (Exception e) {
            Log.w(TAG, "Exception in parseDouble: " + e);
            return defValue;
        }
    }




    public static String getTouchDefaultStatus(){
        String status = getString(FEATURE_FINGERPRINT_TOUCH_DEFAULT_STATUS, "0");
        if (DEBUG) Log.d(TAG,"[getTouchDefaultStatus] status = " + status);
        return status;
    }

    public static String getTZLogEnableDefaultStatus(){
        String status = getString(FEATURE_FINGERPRINT_TZ_LOG_ENABLE_DEFAULT_STATUS, "0");
        if (DEBUG) Log.d(TAG,"[getTZLogEnableDefaultStatus] status = " + status);
        return status;
    }

    public static String getBackNavigationDefaultStatus(){
        String status = getString(FEATURE_FINGERPRINT_BACK_NAVIGATION_DEFAULT_STATUS, "0");
        if (DEBUG) Log.d(TAG,"[getBackNavigationDefaultStatus] status = " + status);
        return status;
    }

    public static boolean isBackFingerprint(){
        String status = getString(FEATURE_FINGERPRINT_IS_BACK_SENSOR, "0");
        if (DEBUG) Log.d(TAG,"[isBackFingerprint] status = " + status);
        if(status != null && status.equals("1") ){
            return true;
        }
        return false;
    }

    public static boolean isCameraCapture(){
        String status = getString(FEATURE_FINGERPRINT_IS_CAMERA_CAPTURE, "0");
        if (DEBUG) Log.d(TAG,"[isCameraCapture] status = " + status);
        if(status != null && status.equals("1") ){
            return true;
        }
        return false;
    }

    public static int getFpQwkBlTriggerdelay() {
        int delay = getInt(FEATURE_FINGERPRINT_QWK_BL_TRIGGER_DELAY, 0);
        if (DEBUG) Log.d(TAG,"getFpQwkBlTriggerdelay(): " + delay);
        return delay;
    }

    public static int getFpQwkWakeupFinishdelay() {
        int delay = getInt(FEATURE_FINGERPRINT_QWK_WAKEUP_FINISH_DELAY, 0);
        if (DEBUG) Log.d(TAG,"getFpQwkWakeupFinishdelay(): " + delay);
        return delay;
    }

    public static int getBackKeyVibTime() {
        int t = getInt(FEATURE_FINGERPRINT_BACK_KEY_VIB_TIME, 40);
        if (DEBUG) Log.d(TAG,"getBackKeyVibTime(): " + t);
        return t;
    }

    public static int getAuthSuccessVibTime() {
        int t = getInt(FEATURE_FINGERPRINT_AUTH_SUCCESS_VIB_TIME, 40);
        if (DEBUG) Log.d(TAG,"getAuthSuccessVibTime(): " + t);
        return t;
    }
}
