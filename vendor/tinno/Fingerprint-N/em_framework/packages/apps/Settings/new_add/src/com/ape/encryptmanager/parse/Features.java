package com.ape.encryptmanager.parse;
import com.ape.emFramework.Log;
import com.ape.encryptmanager.parse.XmlParse;
//import com.ape.emFramework.*;


public class Features {
private static final String TAG ="Features";

public static String getString(final String id) {
	return XmlParse.getValueFromId(id);
}

public static boolean get(final String id) {
	return "true".equals(getString(id));
}

public static int getInt(final String id, final int def) {
	final String tmp = getString(id);
	if(tmp != null) {
		int val =  Integer.valueOf(tmp);
		//Log.i(TAG, "getInt:"+val);
		return val;
	}
	return def;
}

private static boolean isEm2() {
	if(!isTnFpSupport){
		return false;
	}
	/*
	int ret = XmlParse.readFromXml();
	if(0 == ret){
		return false;
	}
	else if(1 == ret){
		return true;
	}*/
	//return EmCommon.isEm2;
       return true;
}

/***************************************************************************************************************/
//public static final boolean is_mtk = EmCommon.SystemPropertiesGet("ro.tinno.platform", "mtk_platform").equals("mtk_platform");
//public static final boolean is_qc = EmCommon.SystemPropertiesGet("ro.tinno.platform", "mtk_platform").equals("qc_platform");
//public static final boolean isTnFpSupport = EmCommon.SystemPropertiesGet("ro.tinno.fingerprint.support", "0").equals("1");
public static final boolean is_mtk = true;
public static final boolean is_qc = false;
public static final boolean isTnFpSupport = true;
public static boolean is_em2 = isEm2();
/***************************************************************************************************************/

//Key map.
/***************************************************************************************************************/
public static String TOUCH_SENSOR_CAPTURE = "touch_sensor_capture";
public static String VIB_TIMES = "vib times";

/***************************************************************************************************************/

}
