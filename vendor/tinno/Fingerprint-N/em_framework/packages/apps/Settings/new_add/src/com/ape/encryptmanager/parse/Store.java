package com.ape.encryptmanager.parse;

import java.util.ArrayList;
import com.ape.encryptmanager.utils.EncryUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.ape.emFramework.Log;
import android.content.pm.PackageManager.NameNotFoundException;
import com.ape.encryptmanager.*;

public class Store {
private static final String TAG ="Store";

private Context mContext;
private SharedPreferences mSharedPreferences;
private SharedPreferences.Editor mEditor;   
private boolean need_commit = false;

public static final String PRE_FILES = "preferences_fingerprint_configs";
public static final String PRE_PREFIX = "";

Store(Context c) {
	mContext = c;
	mSharedPreferences = mContext.getSharedPreferences(PRE_FILES, 
		Context.MODE_MULTI_PROCESS |Context.MODE_WORLD_READABLE);
	mEditor = mSharedPreferences.edit();
}

public boolean isFristBoot() {
	String value = mSharedPreferences.getString("fristboot", null);
	Log.i(TAG, "isFristBoot:"+value);
	
	if(value == null) {
		mEditor.putString("fristboot", "!frist");
		mEditor.commit();
		return true;
	}
	return false;
}

public void put(final String id, final String value) {
	if(id == null || value == null) {
		Log.e(TAG, "put : err!");
		return;
	}
	
	final String key = PRE_PREFIX+id;
	Log.i(TAG, "put -->> id:"+key+" | value:"+value);
	mEditor.putString(key, value);
	need_commit = true;
}

public String get(final String id) {
	if(id == null) {
		Log.e(TAG, "get : err!");
		return null;
	}
	final String key = PRE_PREFIX+id;	
	final String value = mSharedPreferences.getString(key, null);
	Log.i(TAG, "get : "+key+" : "+value);
	return value;
}

public void commit() {
	if(need_commit) {
		mEditor.commit();
		need_commit = false;
	}
}
	
}
