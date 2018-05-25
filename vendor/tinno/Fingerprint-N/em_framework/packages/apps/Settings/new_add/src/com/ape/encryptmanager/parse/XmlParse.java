package com.ape.encryptmanager.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.Environment;
import com.ape.emFramework.Log;
import android.util.Xml;


public class XmlParse {
private static final String TAG ="XmlParse";
private static final boolean debug = false;

private static final String cfg_file_path ="/system/etc/fingerprint_config.xml";
private static final String key_1 ="fingerprint";
private static final String key_2 ="id";
private static final String key_3 ="value";

private Context mContext;
private static XmlParse mXmlParse;
private static ArrayList<fpItem> mList = new ArrayList<fpItem>();

public XmlParse(Context c) {
	mContext = c;
	parse(c);
}

public static XmlParse getInstance(Context c) {
	if(mXmlParse == null) {
		mXmlParse = new XmlParse(c);
	}
	return mXmlParse;
}


public static String getValueFromId(String id) {
	for(int i = 0; i < mList.size(); i++) {
		if(mList.get(i).getId().equals(id)) {
			return mList.get(i).getValue();
		}
	}
	return null;
}

private static void parse(final Context c) {
	File xmlFile = new File(cfg_file_path);  
	FileReader xmlReader;

	try {
		xmlReader = new FileReader(xmlFile);
	} catch (FileNotFoundException e) {
		Log.e(TAG, "Can't open :" + cfg_file_path);
		return;
	}

	try {  
		FileInputStream is = new FileInputStream(xmlFile);  
		mList = readXml(is);  
		is.close();  
	} catch (Exception e) {  
		Log.e(TAG, "readXml :exception!" );
		return;
	}  


	int count = mList.size();
	Log.i(TAG, "[parse count:"+count+"]");
	
	Store mStore = new Store(c);
	if(debug ||mStore.isFristBoot())
	{
		for(int i = 0; i < count; i++) {
			final String id = mList.get(i).getId();
			final String value = mList.get(i).getValue();
			mStore.put(id, value);
		}
		mStore.commit();
	}
	else
	{
		for(int i = 0; i < count; i++) {
			final String id = mList.get(i).getId();
			final String defvalue = mList.get(i).getValue();
			final String newValue = mStore.get(id);
			if(newValue != null) {
				if(!newValue.equals(defvalue) && !newValue.equals("")){
					mList.get(i).setValue(newValue);
				}
			}
			else if(defvalue != null){
				mStore.put(id, defvalue);
				mStore.commit();
			}
		}
	}
}

private static ArrayList<fpItem> readXml(FileInputStream is) {
	if(mList != null && mList.size() > 0) {
		//Log.i(TAG, "readXml:has init!");
		//return mList;
	}
	
	Log.i(TAG, "Start parse xml...");
	ArrayList<fpItem> list = new ArrayList<fpItem>();
	XmlPullParser parser = Xml.newPullParser();  

	try {  
		parser.setInput(is, "UTF-8");
		int eventType = parser.getEventType();
		
		while (eventType != XmlPullParser.END_DOCUMENT)
		{  
			switch (eventType) 
			{  
				case XmlPullParser.START_DOCUMENT:				
				case XmlPullParser.END_TAG:
				break;  

				case XmlPullParser.START_TAG:  
					String name = parser.getName();  
					if(key_1.equals(name)) 
					{
						String id  = parser.getAttributeValue(null, key_2);
						String value = parser.getAttributeValue(null, key_3);
						if(id == null || value == null) {
							continue;
						}
						list.add(new fpItem(id, value));
					}
				break;  
			}  
			eventType = parser.next();  
		}  
		is.close();  
	}
	catch (Exception e) {  
		Log.e(TAG, " readXml err!...");
		e.printStackTrace();  
		return null;
	}  
	return list;
}


public static int readFromXml() {
	final File xmlFile = new File("/system/etc/fingerprint_config.xml");  
	int ret = -1;
	
	try {  
		final FileInputStream is = new FileInputStream(xmlFile);
		final XmlPullParser parser = Xml.newPullParser();  
		parser.setInput(is, "UTF-8");
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT){  
			switch (eventType) {  
				case XmlPullParser.START_TAG:  
					String name = parser.getName();  
					if("fingerprint".equals(name)) {
						String id  = parser.getAttributeValue(null, "id");
						String value = parser.getAttributeValue(null, "value");
						if("ro.tinno.em2".equals(id)) {
							if("0".equals(value)){
								ret = 0;
								break;
							}
							else if("1".equals(value)){
								ret = 1;
								break;
							}
						}
					}
				break;  
			}  
			eventType = parser.next();  
		}  
		is.close();  
	}
	catch (Exception e) {
		e.printStackTrace();  
	}
	Log.i(TAG, "##readFromXml use :"+ret);
	return ret;
}



}
