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

public class BtFeatureOptions {
    public static final String TAG = "BtFeatureOptions";

    public static String FEATURE_SCREEN_ON_OPEN_CAMERA = "feature_screen_on_open_camera";

    public static final String FINGER_CONFIG_FILE = "etc/backtouch_config.xml";
    private static Map mMap = new HashMap<String, String>();

    private static void initLoadOptions() {
        
        if (mMap.size() > 0){
            return ;        
        }
        Log.d(TAG, "BtFeatureOptions start initLoadOptions...");
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
                    Log.d(TAG, "<setting " + "\" name=\"" + key_name + "\" value=\"" + key_value + "\"/>");
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
            return  Integer.parseInt(s);
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

    /**
     * Now, set your feature! 
     */
    public static boolean isScreenOnOpenCamera(){
        String status = getString(FEATURE_SCREEN_ON_OPEN_CAMERA, "0");
        Log.d(TAG,"[isScreenOnOpenCamera] status = " + status);
        if(status != null && status.equals("1") ){
            return true;
        }
        return false;
    }


}
