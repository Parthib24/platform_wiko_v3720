package com.ape.encryptmanager.utils;

import com.ape.emFramework.Log;
public class LogUtil {
	private static final Boolean DEBUG=true;
	private static final String TAG = "Tinnofingerprint";
	
	
   public static void 	d(String tag , String msg){
	   if(DEBUG){
		   Log.d(TAG, tag+"  :  "+msg);
	   }
   }
   
   public static void 	e(String tag , String msg){
	   if(DEBUG){
		   Log.e(TAG, tag+"  :  "+msg);
	   }
   }
   public static void 	i(String tag , String msg){
	   if(DEBUG){
		   Log.i(TAG, tag+"  :  "+msg);
	   }
   }
   
   public static void 	v(String tag , String msg){
	   if(DEBUG){
		   Log.v(TAG, tag+"  :  "+msg);
	   }
   }
	
   public static void 	w(String tag , String msg){
	   if(DEBUG){
		   Log.w(TAG, tag+"  :  "+msg);
	   }
   }
	
}
