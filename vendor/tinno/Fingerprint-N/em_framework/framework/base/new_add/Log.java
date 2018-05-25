package com.ape.emFramework;
import android.os.UserHandle;
//import android.util.Log;

public  class Log {
    private static final Boolean DEBUG = true;
    private static final Boolean DEBUG_multi_user = true;
    private static final String TAG = "Tinnofingerprint:"+(DEBUG_multi_user ? UserHandle.myUserId() : "");
	
   public static void d(String tag , String msg){
	   if(DEBUG){
		   android.util.Log.d(TAG, ""+tag+": "+msg+" ");
	   }
   }
   
   public static void e(String tag , String msg){
	   if(DEBUG){
		   android.util.Log.e(TAG, ""+tag+": "+msg+" ");
	   }
   }
   public static void i(String tag , String msg){
	   if(DEBUG){
		   android.util.Log.i(TAG, ""+tag+": "+msg+" ");
	   }
   }
   
   public static void v(String tag , String msg){
	   if(DEBUG){
		   android.util.Log.v(TAG, ""+tag+": "+msg+" ");
	   }
   }
	
   public static void w(String tag , String msg){
	   if(DEBUG){
		   android.util.Log.w(TAG, ""+tag+": "+msg+" ");
	   }
   }
	
}
