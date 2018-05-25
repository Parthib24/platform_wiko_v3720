package com.ape.encryptmanager.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemProperties;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.ape.emFramework.Log;
import android.widget.Toast;
import com.android.settings.R;

import com.android.internal.policy.IKeyguardService;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Vibrator;
import android.hardware.fingerprint.FingerprintManager;

import com.ape.encryptmanager.parse.*;
import com.ape.encryptmanager.*;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.pm.ActivityInfo;
import android.os.UserManager;
import com.ape.encryptmanager.service.*;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import android.provider.Settings;
import com.ape.emFramework.EmFrameworkStatic;
import android.content.pm.PackageInfo;
import com.ape.encryptmanager.service.AppData;
import java.util.Collection;
import java.util.Collections;
import com.ape.fpShortcuts.Shortcut;
import com.ape.fpShortcuts.ShortcutManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.ServiceManager;
import com.ape.encryptmanager.utils.EncryUtil;


public class EncryptService extends Service {
    private static final String TAG = "EncryptService-class";
    private static final boolean DEBUG = false;

    //private EncryptServiceUtil mUtil;
    private EncryptServiceWrapper encryptBinder;
    //private ITinnoFigurePrintCallback  mCallback;
    public Context mContext;
    public final static int MSG_FINGERPRINTD_STATE = 3001;
    private boolean isBootReceiver = false;
    private boolean isUnLockReceiver = false;
    private boolean isQuickBootReceiver = false;
    private static boolean isStartApeFileManager = false;
    //private final static String INTENT_ACTION_FINGER_QUICKBOOT = "com.ape.fingerprint.quickboot"; 

    private static List<AppData> mQuickBootAppDataList = new ArrayList<AppData>();  
    private static List<String> mAppDataNeedToAdd = new ArrayList<String>();	
    private final Object mLock = new Object();
    Handler mHandler = new Handler();
    private boolean LOG_DEBUG = false;

    Thread mServiceInitThread = new Thread(new Runnable() {
        @Override
        public void run() {
            structureQuickBootAppDatas(EncryptService.this, null);
        }
    });	


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, " ************* onCreate (EncryptService) ************* ");
        mContext = this;
        FingerprintManager mFingerprintManager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);

        encryptBinder = new EncryptServiceWrapper();
        registerBootReceiver();
        registerUnLockReceiver();
        registerPackageStateReceiver();

        //add filelock for multi-user
        startApeFileManager();

        /*try {
          Log.d(TAG, "Add EncryptService Ibinder obj(encryptBinder) to ServiceManager!");
          ServiceManager.addService("EncryptService", encryptBinder); 
        }
        catch (java.lang.SecurityException e) {
        Log.e(TAG, "Add EncryptService exception :"+e);
        stopSelf();
        return;
        }*/


        /*mHandler.postDelayed(new Runnable() {  
          @Override  
          public void run() {
          structureQuickBootAppDatas(EncryptService.this);
          }  
          }, 2000);*/	  	
        //mUtil = EncryptServiceUtil.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();
        if (DEBUG) Log.d(TAG, "onBind:"+action);
        return encryptBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (DEBUG) Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        if (DEBUG) Log.d(TAG, "onStart");
        super.onStart(intent, startId);
    }

    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (DEBUG) Log.d(TAG, "onTaskRemoved");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "onStartCommand intent =" + intent + "| flags =" + flags + " | startId =" +startId);
        if (intent == null) {
	     if (mQuickBootAppDataList.size() <= 0 || mQuickBootAppDataList == null) {
		  mQuickBootAppDataList.clear();
                structureQuickBootAppDatas(this, null);
            }	
        } else if (intent != null && intent.getAction() != null && intent.getAction().equals("com.ape.action.quickboot")){	 
            /*mHandler.postDelayed(new Runnable() {  
              @Override
              public void run() {
              structureQuickBootAppDatas(EncryptService.this, null);
              }  
              }, 500);*/
            mServiceInitThread.start();
        }
        return START_STICKY;	
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy(); 
        if (isBootReceiver) {
            unregisterReceiver(bootReceiver);
            isBootReceiver = false;
        }

        if (isUnLockReceiver) {
            unregisterReceiver(unlockdReceiver);
            isUnLockReceiver = false;
        }

        unregisterReceiver(mPackageStateReceiver);

        /*if (isQuickBootReceiver) {
          unregisterReceiver(mQuickBootReceiver);
          isQuickBootReceiver = false;
          }*/
    }

    public final class EncryptServiceWrapper extends MEncryptService.Stub {

        @Override
        public boolean isMustSetPassword() {
            return EncryptServiceUtil.getInstance(EncryptService.this).isMustSetPassword();
        }

        @Override
        public boolean checkToken(String token) {
            return EncryptServiceUtil.getInstance(EncryptService.this).checkToken(token);
        }

        @Override
        public String checkPassword(String password) {
            return EncryptServiceUtil.getInstance(EncryptService.this).checkPassword(password);
        }	

        @Override
        public boolean getAppFingerprint() {
            return EncryptServiceUtil.getInstance(EncryptService.this).getAppFingerprint();
        }

        @Override
        public int  getPasswordType(){
            return EncryptServiceUtil.getInstance(EncryptService.this).getPasswordType();
        }


        @Override
        public String setNumberPassword(String password,int question ,String answer) {
            return EncryptServiceUtil.getInstance(EncryptService.this).setNumberPassword( password, question ,  answer);
        }

        @Override
        public String setPatternPassword(String password,int question ,String answer) {
            return EncryptServiceUtil.getInstance(EncryptService.this).setPatternPassword(password, question, answer);
        }


        @Override
        public String changePatternPassword(String password) {
            return  EncryptServiceUtil.getInstance(EncryptService.this).changePatternPassword(password);
        }

        @Override
        public String changeNumberPassword(String password) {
            return EncryptServiceUtil.getInstance(EncryptService.this).changeNumberPassword(password);
        }

        @Override
        public String changePasswordQuestion(int question, String answer) {
            return EncryptServiceUtil.getInstance(EncryptService.this).changePasswordQuestion(question, answer);
        }

        @Override
        public int getPasswordLength() {
            return EncryptServiceUtil.getInstance(EncryptService.this).getPasswordLength();
        }


        @Override
        public String getToken() {
            return EncryptServiceUtil.getInstance(EncryptService.this).getToken();
        }

        @Override
        public int getPasswordQuestionID() {
            return EncryptServiceUtil.getInstance(EncryptService.this).getPasswordQuestionID();
        }

        @Override
        public boolean checkPasswordQuestion(String security ) {
            return EncryptServiceUtil.getInstance(EncryptService.this).checkPasswordQuestion(security);
        }

        @Override
        public String[] getQuestionList() {
            return EncryptServiceUtil.getInstance(EncryptService.this).getQuestionList();
        }

        @Override
        public boolean getFileFingerprintLock() {
            return EncryptServiceUtil.getInstance(EncryptService.this).getFileLock();
        }

        @Override
        public boolean getGalleryFingerprintLock() {
            return EncryptServiceUtil.getInstance(EncryptService.this).getGalleryLock();
        }

        @Override
        public boolean getLeftScrollSwitch() { 
            return false;
        }

        @Override
        public boolean getRightScrollSwitch() {
            return false;
        }

        @Override
        public boolean getDownScrollSwitch() {
            return false;
        }

        @Override
        public boolean geClickSwitch() {
            return false;
        }

        @Override
        public boolean getLongClickSwitch() {
            return false;
        }

        @Override
        public boolean getDoubleClickSwitch() {
            return false;
        }

        @Override
        public Map<String, String> getLongClickData() {
            return null;
        }

        @Override
        public boolean registerCallback(ITinnoFigurePrintCallback  Callback) {
            if (DEBUG) Log.d(TAG, "applock: registerCallback");
            return true;
        }

        @Override
        public boolean unregisterCallback() {
            if (DEBUG) Log.d(TAG, "applock: unregisterCallback");
            return true;
        }

        @Override
        public boolean verifyStart() {
            if (DEBUG) Log.d(TAG, "mingyi.guo: verifyStart");
            return true;
        }

        @Override
        public boolean isSupportFingerprint() {
            //return MainFeatureOptions.isTnFpSupport; //Features.isTnFpSupport;
            return EmFrameworkStatic.isTnFpSupport;
            //  return true ;
        }

        @Override
        public List<AppData> getQuickBootAppDatas() {
            int appDataSize = 0;
            if (mQuickBootAppDataList != null) {
                appDataSize = mQuickBootAppDataList.size();
                if (DEBUG) Log.d(TAG, "getQuickBootAppDatas --->appDataSize =" + appDataSize);		
                if (appDataSize > 0) {
                    updateQuickBootAppDatas();
                }
            }
            return mQuickBootAppDataList;
        }	
    }


    private void registerBootReceiver() {
        if (DEBUG) Log.d(TAG, "registerBootReceiver");
        if (!isBootReceiver) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BOOT_COMPLETED);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);		
            filter.addAction(EncryUtil.ACTION_FP_ENTRY_EVENT);		
            registerReceiver(bootReceiver, filter);
            isBootReceiver = true;
        }
    }


    public void structureQuickBootAppDatas(Context context, String packageName) {
        synchronized (mLock) {
            AppDataCompartor mAppDataCompartor = new AppDataCompartor();

            List<ResolveInfo> resolveInfos;  

            PackageManager packageManager = mContext.getPackageManager();  
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);  
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            if (packageName != null) {
                mainIntent.setPackage(packageName);
            }		
            resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);	
            if (DEBUG) Log.d(TAG, " structureQuickBootAppDatas--------resolveInfos.size()----->" + resolveInfos.size());

            if (resolveInfos == null || resolveInfos.size() <= 0) {
                return;
            }

            for (int i = 0; i < resolveInfos.size(); i++) {
                ResolveInfo s = resolveInfos.get(i);
                AppData tempData = new AppData();
                if (LOG_DEBUG) {
                    if (DEBUG) Log.d(TAG, "structureQuickBootAppDatas--->s.activityInfo.packageName="+s.activityInfo.packageName +
                            " |s.activityInfo.name="+s.activityInfo.name +
                            " |s.activityInfo.label="+s.loadLabel(context.getPackageManager()).toString() +
                            " |s.loadIcon ="+ s.loadIcon(context.getPackageManager()) +
                            " |s.getIconResource() ="+ s.getIconResource());
                }	 


                tempData.SetAppName(s.loadLabel(context.getPackageManager())
                        .toString());
                tempData.SetPackageName(s.activityInfo.packageName);
                tempData.SetClassName(s.activityInfo.name);
                tempData.setAppIcon(s.getIconResource());
                tempData.setAppLetters(AppDataListChoiceActivity.setSortLetters(s.loadLabel(context.getPackageManager()).toString()));

                List<Shortcut> shortcutListOrigin = new ArrayList<Shortcut>();
                shortcutListOrigin = ShortcutManager.getShortcutsInfo(this, UserHandle.of(this.getUserId()), new ComponentName(s.activityInfo.packageName, s.activityInfo.name));
                List<Shortcut> shortcutListNew = new ArrayList<Shortcut>();
                shortcutListNew.add(getDefaultAppLaunchShortcutItemForQuickBoot(s.activityInfo.packageName, s.activityInfo.name)); 	 
                if (DEBUG) Log.d(TAG, "--------structureQuickBootAppDatas--->shortcutListOrigin=" + shortcutListOrigin);

                if (shortcutListOrigin != null) {
                    if (shortcutListOrigin.size() > 0) {
                        for (int j=0; j<shortcutListOrigin.size(); j++) {
                            Shortcut shortcut = shortcutListOrigin.get(j);
                            if (LOG_DEBUG) {
                                if (DEBUG) Log.d(TAG, "structureQuickBootAppDatas shortcut[" + j + "] =" + shortcut +
                                        "| shortcut.id=" + shortcut.id +
                                        "| shortcut.shortLabel=" + shortcut.shortLabel +
                                        "| shortcut.longLabel=" + shortcut.longLabel +
                                        "| shortcut.disabledMessage=" + shortcut.disabledMessage +
                                        "| shortcut.icon=" + shortcut.icon +
                                        "| shortcut.enabled=" + shortcut.enabled +
                                        "| shortcut.intent=" + shortcut.intent +
                                        "| shortcut.id=" + shortcut.id +
                                        "| shortcut.packageName=" + shortcut.packageName);
                            }                     
                            if (shortcut.packageName != null 
                                    && shortcut.packageName.equals("com.google.android.apps.maps")) {
                                if (shortcut.shortLabel.equals("Itinerary")) {
                                    shortcut.shortLabel = context.getString(R.string.google_map_itinerary);
                                } else if (shortcut.shortLabel.equals("Search")) {
                                    shortcut.shortLabel = context.getString(R.string.search_hint2);
                                }
                                    }
                            //if (shortcut.enabled) {
                            shortcutListNew.add(shortcut);
                            //}
                        }
                    }
                }

                tempData.setAppActionsList(shortcutListNew);

                if (AppDataListChoiceActivity.isNotSupportedApp(s.activityInfo.name)) {
                    mQuickBootAppDataList.remove(tempData);
                } else {
                    Drawable appIcon = AppDataListChoiceActivity.getAppInfoIcon(this, 
                            tempData.getPackageName(), tempData.getClassName());
                    if (tempData.getAppIcon() > 0 && appIcon != null) {
                        mQuickBootAppDataList.add(tempData);
                    }
                }
            }

            Collections.sort(mQuickBootAppDataList, mAppDataCompartor);
        }
    }

    public void removeQuickBootAppData(Context context, String packageName) {
        if (DEBUG) Log.d(TAG, " removeQuickBootAppData----packageName = " + packageName);   	
        synchronized (mLock) { 	
            AppDataCompartor mAppDataCompartor = new AppDataCompartor();
            if (mQuickBootAppDataList == null || mQuickBootAppDataList.size() <= 0) {
                return;
            }
            if (DEBUG) Log.d(TAG, " removeQuickBootAppData----before List size  = " + mQuickBootAppDataList.size());   				
            for (int i=0; i<mQuickBootAppDataList.size(); i++) {
                AppData appData = mQuickBootAppDataList.get(i);
                String appData_packageName = appData.getPackageName();
                if (appData_packageName.equals(packageName)) {
                    if (LOG_DEBUG) {
                        Log.d(TAG, " removeQuickBootAppData----remove index  = " + i);
                    }		
                    mQuickBootAppDataList.remove(i);
                }
            }
            if (DEBUG) Log.d(TAG, " removeQuickBootAppData----after List size  = " + mQuickBootAppDataList.size());   						
            Collections.sort(mQuickBootAppDataList, mAppDataCompartor);
        }
    }	

    public Shortcut getDefaultAppLaunchShortcutItemForQuickBoot(String packageName, String className) {
        Shortcut init_shortcutItem ;
        Intent intent = new Intent();
        intent.setPackage(packageName);
        intent.setClassName(packageName, className);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        init_shortcutItem = new Shortcut();
        init_shortcutItem.shortLabel = this.getResources().getString(R.string.quick_boot_shortcuts_item_default);
        init_shortcutItem.packageName = packageName;
        init_shortcutItem.intent = intent;
        return init_shortcutItem;
    }

    private void registerPackageStateReceiver() {
        if (DEBUG) Log.d(TAG, "registerPackageStateReceiver");
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");		
        registerReceiver(mPackageStateReceiver, filter);
    }

    private final BroadcastReceiver mPackageStateReceiver = new BroadcastReceiver() {
        @Override  
        public void onReceive(Context context, Intent intent){
            if (DEBUG) Log.d(TAG, "PackageStateReceiver --> intent =" + intent + "!!!");
            String packageName = null;			
            String dataStr = intent.getDataString();
            int startIndex = dataStr.indexOf(":");
            if (startIndex > 0) {
                packageName = dataStr.substring(startIndex+1, dataStr.length());
                if (DEBUG) Log.d(TAG, "PackageStateReceiver --> packageName =" + packageName); 
            }	
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                //if (mQuickBootAppDataList.size() > 0) {
                //structureQuickBootAppDatas(context, packageName);
                //}		     
                if (packageName != null) {
                    int size = mAppDataNeedToAdd.size();
                    if (size > 0) {
                        for (int i=0; i<size; i++) {
                            String itemName = mAppDataNeedToAdd.get(i);
                            if (packageName.equals(itemName)) {
                                return;
                            }			   
                        }
                    }
                    mAppDataNeedToAdd.add(packageName);
                }
            } else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                if (mQuickBootAppDataList.size() > 0) {
                    removeQuickBootAppData(context,packageName);
                }                  
            }
        }
    };

    private void updateQuickBootAppDatas(){		
        int newDataSize = mAppDataNeedToAdd.size();	
        if (DEBUG) Log.d(TAG, "updateQuickBootAppDatas --newDataSize=" + newDataSize + "!!!"); 
        if (newDataSize <= 0) {
            return;
        }

        for (int i=0; i<newDataSize; i++) {
            String packageName = mAppDataNeedToAdd.get(i);
            structureQuickBootAppDatas(EncryptService.this, packageName);		
        }
        mAppDataNeedToAdd.clear();
    }

    /*
       private void registerUserChangeReceiver() {
// Intents for all users
Log.d(TAG, "registerUserChangeReceiver");      
IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
filter.addAction(Intent.ACTION_SCREEN_ON);
filter.addAction(Intent.ACTION_USER_PRESENT);
filter.addAction(Intent.ACTION_USER_SWITCHED);
filter.addAction(INTENT_ACTION_FINGER_QUICKBOOT);
mContext.registerReceiverAsUser(mUserChangeReceiver, UserHandle.ALL, filter, null, null);
       }

       private void registerQuickBootReceiver() {
// Intents for all users
if (!isQuickBootReceiver) {
Log.d(TAG, "registerQuickBootReceiver");      
IntentFilter filter = new IntentFilter(INTENT_ACTION_FINGER_QUICKBOOT);
filter.setPriority(10000);
mContext.registerReceiverAsUser(mQuickBootReceiver, UserHandle.ALL, filter, null, null);
isQuickBootReceiver = true;
}
       }    
       */
private void registerUnLockReceiver() {
    if (DEBUG) Log.d(TAG, "registerUnLockReceiver");
    if (!isUnLockReceiver) {
        registerReceiver(unlockdReceiver, new IntentFilter(Intent.ACTION_USER_UNLOCKED));
        isUnLockReceiver = true;
    }
}

/*
   private final BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() {
   @Override
   public void onReceive(Context context, Intent intent) {
   String action = intent.getAction();
   Log.d(TAG, "BroadcastReceiver mUserChangeReceiver  action =" + action + 
   " | mContext.getUserId() =" + mContext.getUserId() + "context.getUserId() =" + context.getUserId());            
   if (action.equals(Intent.ACTION_USER_SWITCHED)) {
   int userId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, 0);
   mApeCustomFpManager = ApeCustomFpManager.ReCreateInstance(mContext);      
   }
   }
   };





*/




private BroadcastReceiver unlockdReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (DEBUG) Log.d(TAG, "BroadcastReceiver unlockdReceiver");
        if (action !=null || action.equals(Intent.ACTION_USER_UNLOCKED)) {
            startApeFileManager();
        }
    }
};

private final BroadcastReceiver bootReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (DEBUG) Log.d(TAG, "BroadcastReceiver bootReceiver action =" + action);
        if (action !=null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            startApeFileManager();
            structureQuickBootAppDatas(EncryptService.this, null);
        } else if (action !=null && action.equals(EncryUtil.ACTION_FP_ENTRY_EVENT)) {
            if (mQuickBootAppDataList.size() <=0 || mQuickBootAppDataList == null) {
	         structureQuickBootAppDatas(EncryptService.this, null);
            }
        } else if (action !=null && action.equals(Intent.ACTION_LOCALE_CHANGED)) {
	     if (mQuickBootAppDataList.size() <= 0 || mQuickBootAppDataList == null) {
	         structureQuickBootAppDatas(EncryptService.this, null);
	     }
        }
    }
};


private void startApeFileManager() {
    if (DEBUG) Log.i(TAG, "start--com.ape.secrecy.service.EncryptFileService---isStartApeFileManager="+isStartApeFileManager);
    try {
        if (!isStartApeFileManager) {
            Intent intent = new Intent();
            intent.setClassName("com.ape.filemanager", "com.ape.secrecy.service.EncryptFileService");
            ComponentName name = startService(intent);
            if (DEBUG) Log.i(TAG, "start--com.ape.secrecy.service.EncryptFileService---name="+name);
            if (name != null) {
                isStartApeFileManager = true;
            }
        }
    }
    catch (Exception ex) {
        ex.printStackTrace();
    }
}

public static void XmlParse(final Context c) {
    new Thread(new Runnable() {
        @Override
        public void run() {
            XmlParse.getInstance(c);
        }
    }).start();
}

public static String getTop(final Context c) {   
    final ActivityManager am = 
        (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityInfo aInfo = null;
    List<RunningTaskInfo> list = am.getRunningTasks(1);
    if (list.size() != 0) {
        RunningTaskInfo topRunningTask = list.get(0);
        return topRunningTask.topActivity.getClassName(); 
    } else {
        return "";
    }
}

public static void sendMsgToFingerprintd(int msg) {
    //Utils.sendMsgToFingerprintd(msg);
}

public static void cancelAuthenticationEx() {
    //Utils.cancelAuthenticationEx();
}


public static int getFingerprintdState(int id) {
    //return Utils.get(id);
    return 0;
}

}

