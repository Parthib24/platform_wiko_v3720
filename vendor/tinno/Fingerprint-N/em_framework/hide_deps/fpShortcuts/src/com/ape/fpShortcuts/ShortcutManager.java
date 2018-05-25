package com.ape.fpShortcuts;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
//import android.support.annotation.RequiresApi;

import com.ape.shortcutsUtils.PermissionUtils;
import com.ape.shortcutsUtils.ResourceUtils;
import com.ape.shortcutsUtils.StringUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import com.ape.fpShortcuts.R;
import android.text.TextUtils;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.res.Resources;
import android.content.ComponentName;
import java.io.FileNotFoundException;
import java.io.IOException;
/**
 * Created by christopher ney on 10/10/2016.
 */

public class ShortcutManager {

    private static final String TAG = ShortcutManager.class.getName();

    private static final boolean CHECK_SHORTCUT_INTENT = true;

    private static final String SHORTCUT_FILENAME = "shortcuts";
    private static final String SHORTCUT_NAMESPACE = "http://schemas.android.com/apk/res/android";

    private static final String NODE_SHORTCUT = "shortcut";
    private static final String NODE_INTENT = "intent";
    private static final String NODE_EXTRA = "extra";
    private static final String NODE_CATEGORIES = "categories";
    private static final String ATTRIBUTE_CLASS = "class";
    private static final String ATTRIBUTE_ENABLED = "enabled";
    private static final String ATTRIBUTE_SHORTCUT_ID = "shortcutId";
    private static final String ATTRIBUTE_ICON = "icon";
    private static final String ATTRIBUTE_SHORT_LABEL = "shortcutShortLabel";
    private static final String ATTRIBUTE_LONG_LABEL = "shortcutLongLabel";
    private static final String ATTRIBUTE_DISABLED_MESSAGE = "shortcutDisabledMessage";
    private static final String ATTRIBUTE_TARGET_PACKAGE = "targetPackage";
    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_TARGET_CLASS = "targetClass";
    private static final String ATTRIBUTE_DATA = "data";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_MIME_TYPE = "mimeType";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static ArrayList<Shortcut> mShortcuts = null;


    private static final String BEGIN_TAG = "shortcuts";
    private static final String META_KEY = "android.app.shortcuts";
    private static final String SHORTCUT_TAG = "shortcut";

    private static final String[] black_list = new String[]{
            "com.android.chrome",
            "com.google.android.youtube",
            "com.google.android.talk",
            "com.google.android.apps.photos",
            "com.google.android.gm",
            "com.myos.camera",
           };
			

    private static PackageManager getPackageManager(Context context) {
        return context.getPackageManager();
    }

    public static ArrayList<Shortcut> getShortcutsFromApplication(Context context, UserHandle user, String packageName) {
        ArrayList<Shortcut> shortcuts = new ArrayList<>();
        try {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {

                XmlResourceParser xml = null;

                int shortcutsResID = AndroidXMLDecompress.getAppShortcutsResourceID(context, packageName);
                if (shortcutsResID != 0)
                    xml = ResourceUtils.getXmlFromApplication(context, packageName, shortcutsResID);

                if (xml == null)
                    xml = ResourceUtils.getXmlFromApplication(context, packageName, SHORTCUT_FILENAME);

                shortcuts = getShortcutsFromXml(context, xml, SHORTCUT_NAMESPACE, packageName);

            } else {

                shortcuts = getAppShortcuts(context, user, packageName);
            }

            if (shortcuts == null || shortcuts.size() == 0) {
                loadDefaultShortcuts(context);
                shortcuts = getDefaultShortcuts(packageName);
            }

        } catch (Exception ex) {
            //TrackingUtils.logException(ex);
        }
        return shortcuts;
    }

    private static ArrayList<Shortcut> getAppShortcuts(Context context, UserHandle user, String packageName) {
	 Log.d(TAG, "getAppShortcuts >>>>>> packageName = " + packageName);	
        ArrayList<Shortcut> shortcuts = new ArrayList<>();
        try {

            LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);

            if (user == null) {
                UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);
                List<UserHandle> users = userManager.getUserProfiles();
                user = (users != null && users.size() > 0) ? users.get(0) : null;
            }

            if (launcherApps.hasShortcutHostPermission()) {

                LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
                query.setPackage(packageName);

                List<ShortcutInfo> appShortcuts = launcherApps.getShortcuts(query, user);                
                if (appShortcuts != null) {
                    for (ShortcutInfo appShortcut : appShortcuts) {
	                 Log.d(TAG, "getAppShortcuts >>>>>> appShortcut = " + appShortcut);				   			
                        shortcuts.add(new Shortcut(context, launcherApps, appShortcut));
                    }
                }
            }

        } catch (Exception ex) {
            //TrackingUtils.logException(ex);
        }
        return shortcuts;
    }
	
    public static ArrayList<Shortcut> getShortcutsInfo(Context context, UserHandle user, ComponentName comp) {
        Log.d(TAG, "getShortcutsInfo  comp = " + comp);
        ArrayList<Shortcut> result = new ArrayList<>();
        if (true) {//itemInfo instanceof ShortcutInfo) {
           /* if (itemInfo.getIntent() == null || itemInfo.getIntent().getComponent() == null) {
                return result;
            }
            ComponentName componentName = itemInfo.getIntent().getComponent();*/
            for (int i = 0; i < black_list.length; i++) {
                if (black_list[i].equals(comp.getPackageName())) {
                    return result;
                }
            }
            if ("com.google.android.apps.maps".equals(comp.getPackageName())) {
                return getGoogleMapShortcuts(context);
            }
	
            PackageManager mPackManager = context.getPackageManager();
            try {
                ActivityInfo info = mPackManager.getActivityInfo(comp, PackageManager.GET_META_DATA);
                XmlPullParser parser = null;

                if (info != null && info.metaData != null) {
                    parser = info.loadXmlMetaData(mPackManager, META_KEY);
                }
                if (parser == null && info != null && info.applicationInfo != null && info.applicationInfo.metaData != null) {
                    parser = info.applicationInfo.loadXmlMetaData(mPackManager, META_KEY);
                }

               
                if (parser != null) {
                    beginDocument(parser, BEGIN_TAG);
                    int type;
                    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
                        if (type == XmlPullParser.END_TAG)
                            continue;
                        String name = parser.getName();
                        Log.d(TAG, " 111current  tag : " + name);
                        if (SHORTCUT_TAG.equals(name)) {
                        Log.d(TAG, " ############<shortcut> START############" );	   				
                            Resources res = mPackManager.getResourcesForApplication(comp.getPackageName());
                            Shortcut sInfo = new Shortcut();
                            int count = parser.getAttributeCount();
                            Log.d(TAG, " the count of attribute " + count + "| <" + name +">");	
                            for (int i = 0; i < count; i++) {
                                if (parser.getAttributeName(i).equals(ATTRIBUTE_SHORTCUT_ID)) {
                                    sInfo.id = parser.getAttributeValue(i);
                                } else if (parser.getAttributeName(i).equals(ATTRIBUTE_ENABLED)) {
                                    sInfo.enabled = Boolean.valueOf(parser.getAttributeValue(i));
                                } else if (parser.getAttributeName(i).equals(ATTRIBUTE_ICON)) {
                                    String value = parser.getAttributeValue(i).replace("@", "");
                                    sInfo.icon = getDrawableId(value);
                                } else if (parser.getAttributeName(i).equals(ATTRIBUTE_SHORT_LABEL)) {
                                    String value = parser.getAttributeValue(i).replace("@", "");
                                    sInfo.shortLabel= getStringFromRes(res, value);
                                } else if (parser.getAttributeName(i).equals(ATTRIBUTE_LONG_LABEL)) {
                                    String value = parser.getAttributeValue(i).replace("@", "");
                                    sInfo.longLabel = getStringFromRes(res, value);
                                } else if (parser.getAttributeName(i).equals(ATTRIBUTE_DISABLED_MESSAGE)) {
                                    String value = parser.getAttributeValue(i).replace("@", "");
                                    sInfo.disabledMessage= getStringFromRes(res, value);
                                }
                            }

                            parser.nextTag();
				Log.d(TAG, " 222current tag : " + parser.getName());
                            if (parser.getName().equalsIgnoreCase(NODE_INTENT)) {
                                count = parser.getAttributeCount();
                                String action = null;
                                String targetPkg = null;
                                String targetCls = null;
                                String data = null;
                                String typeFinal = null;
                                String mimeType = null;								
                                Log.d(TAG, " the count of attribute " + count + "| <" + name +">");	
                                for (int i = 0; i < count; i++) {
                                    if (parser.getAttributeName(i).equals(ATTRIBUTE_ACTION)) {
                                        action = parser.getAttributeValue(i);
                                        Log.d(TAG,"action: "+action);
                                    } else if (parser.getAttributeName(i).equals(ATTRIBUTE_TARGET_PACKAGE)) {
                                        targetPkg = parser.getAttributeValue(i);
                                        Log.d(TAG,"targetPkg: "+targetPkg);
                                    } else if (parser.getAttributeName(i).equals(ATTRIBUTE_TARGET_CLASS)) {
                                        targetCls = parser.getAttributeValue(i);
                                        Log.d(TAG,"targetCls: "+targetCls);
                                    } else if (parser.getAttributeName(i).equals(ATTRIBUTE_DATA)) {
                                        data = parser.getAttributeValue(i);
                                        Log.d(TAG, "intent data: " + data);
                                    } else if (parser.getAttributeName(i).equals(ATTRIBUTE_TYPE)) {
                                        typeFinal = parser.getAttributeValue(i);
                                        Log.d(TAG, "intent typeFinal: " + typeFinal);
                                    } else if (parser.getAttributeName(i).equals(ATTRIBUTE_MIME_TYPE)) {
                                        mimeType = parser.getAttributeValue(i);
                                        Log.d(TAG, "intent mimeType: " + mimeType);								
                                    }
                                }
                                if (!TextUtils.isEmpty(action) || (!TextUtils.isEmpty(targetPkg) && !TextUtils.isEmpty(targetCls))) {
                                    sInfo.intent = new Intent(action);
                                    if (!TextUtils.isEmpty(data)) {
                                        sInfo.intent.setData(Uri.parse(data));
                                    }
                                    if (!TextUtils.isEmpty(mimeType)) {
                                        sInfo.intent.setType(mimeType);
                                    } else if (!TextUtils.isEmpty(typeFinal)) {
                                        sInfo.intent.setType(typeFinal);
                                    }
                                    if (!TextUtils.isEmpty(targetPkg) && !TextUtils.isEmpty(targetCls)) {
                                        if ("com.google.android.apps.docs.app.PickFilesToUploadActivity".equals(targetCls) ||
                                                "com.google.android.apps.docs.capture.DocScannerActivity".equals(targetCls)) {
                                            continue;
                                        }
                                        sInfo.intent.setComponent(new ComponentName(targetPkg, targetCls));
                                    }
                                    parser.next();
                                    Log.d(TAG, "intent sub tag: " + parser.getName());
                                    while (NODE_EXTRA.equals(parser.getName())) {
                                        if (parser.getEventType() != XmlPullParser.END_TAG) {
                                            int attrCount = parser.getAttributeCount();
                                            String attrKey = "";
                                            String attrValue = "";
                                            for (int i = 0; i < attrCount; i++) {
                                                if ("name".equalsIgnoreCase(parser.getAttributeName(i))) {
                                                    attrKey = parser.getAttributeValue(i);
                                                }else if("value".equalsIgnoreCase(parser.getAttributeName(i))){
                                                    attrValue = parser.getAttributeValue(i);
                                                }
                                            }
                                            if (!TextUtils.isEmpty(attrKey) && !TextUtils.isEmpty(attrValue)) {
                                                Log.d(TAG, "=====intent extra TAG=======");
                                                Log.d(TAG, "attrCount: " + attrCount);
                                                Log.d(TAG, "(key=" + attrKey + " ; value =" + attrValue +")");
                                                try {
                                                    int intValue = Integer.valueOf(attrValue);
                                                    sInfo.intent.putExtra(attrKey, intValue);
                                                    Log.d(TAG, "extra attrValue type is Integer###");
                                                } catch (NumberFormatException e) {
                                                    if ("true".equalsIgnoreCase(attrValue) || "false".equalsIgnoreCase(attrValue)) {
                                                        boolean booleanValue = Boolean.valueOf(attrValue);
                                                        sInfo.intent.putExtra(attrValue, booleanValue);
                                                        Log.d(TAG, "extra attrValue type is Boolean$$$");
                                                    } else {
                                                        sInfo.intent.putExtra(attrKey, attrValue);
                                                        Log.d(TAG, "extra attrValue type is String type or other!!!");
                                                    }
                                                }
                                            }
                                        }
                                        parser.next();
				            Log.d(TAG, " 333current tag : " + parser.getName());			
                                    }
                                    result.add(sInfo);
                                }
                            }	
                            Log.d(TAG, " ############</shortcut> END############" );					
                        }
                    }

                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "getShortcutsInfo e:" + e);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "getShortcutsInfo e:" + e);
            } catch (XmlPullParserException e) {
                Log.d(TAG, "getShortcutsInfo e:" + e);
            } catch (IOException e) {
                Log.d(TAG, "getShortcutsInfo e:" + e);
            }
        }
        return result;
    }

    private static ArrayList<Shortcut> getGoogleMapShortcuts(Context context) {
        ArrayList<Shortcut> result = new ArrayList<>();
        Shortcut sInfoItinerary = new Shortcut();
	 String strItinerary = 	"Itinerary";//context.getResources().getString(R.string.google_map_itinerary);
        sInfoItinerary.enabled = true;
        sInfoItinerary.shortLabel = strItinerary;
        sInfoItinerary.longLabel = strItinerary;
        sInfoItinerary.disabledMessage = strItinerary;
        sInfoItinerary.icon = -1;
        sInfoItinerary.intent = new Intent();
	 sInfoItinerary.packageName="com.google.android.apps.maps";
        sInfoItinerary.intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.driveabout.app.DestinationActivity");
        sInfoItinerary.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        result.add(sInfoItinerary);

        Shortcut sInfoSearch = new Shortcut();
	 String strSearch = "Search";//context.getResources().getString(R.string.search_hint2);	
        sInfoSearch.enabled = true;
        sInfoSearch.shortLabel = strSearch;
        sInfoSearch.longLabel = strSearch;
        sInfoSearch.disabledMessage = strSearch;
	 sInfoSearch.icon = -1;
	 sInfoSearch.packageName="com.google.android.apps.maps";
        sInfoSearch.intent = new Intent();
        sInfoSearch.intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.PlacesActivity");
        sInfoSearch.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        result.add(sInfoSearch);

        return result;
    }

    private static String getStringFromRes(Resources res, String id) {
        String result = null;
        try {
            result = res.getString(Integer.valueOf(id));
        } catch (Resources.NotFoundException e) {
            Log.d(TAG, "getStringFromRes e:" + e);
        } catch (NumberFormatException e) {
            Log.d(TAG, "getStringFromRes e:" + e);
        }
        return result;
    }

    private static int getDrawableId(String id) {
        int result = -1;
        try {
            result = Integer.valueOf(id);
        } catch (NumberFormatException e) {
            Log.d(TAG, "getDrawableId e:" + e);
        }
        return result;
    }
	
    private static void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != parser.START_TAG
                && type != parser.END_DOCUMENT) {
            ;
        }

        if (type != parser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }
	
    private static ArrayList<Shortcut> getDefaultShortcuts(String packageName) {
        ArrayList<Shortcut> shortcuts = new ArrayList<>();
        try {
            for (Shortcut shortcut : mShortcuts) {
                if (shortcut.packageName != null
                        && packageName.equalsIgnoreCase(shortcut.packageName)) {
                    shortcuts.add(shortcut);
                }
            }
        } catch (Exception ex) {
            //TrackingUtils.logException(ex);
        }
        return shortcuts;
    }

    private static void loadDefaultShortcuts(Context context) {
        try {
            if (mShortcuts == null) {

                String packageName = context.getPackageName();

                XmlResourceParser xml = ResourceUtils.getXmlFromApplication(context, packageName, "shortcuts_apps");

                mShortcuts = getShortcutsFromXml(context, xml, null, packageName);
            }
        } catch (Exception ex) {
            //TrackingUtils.logException(ex);
        }
    }

    private static Shortcut instantiateShortcutFromClassName(String className) {
        Shortcut shortcut = null;
        try {
            if (className != null) {
                Class<?> clazz = Class.forName(className);
                Constructor<?> constructor = clazz.getConstructor();
                shortcut = (Shortcut)constructor.newInstance();
            } else {
                shortcut = new Shortcut();
            }
        } catch (Exception ex) {
            //TrackingUtils.logException(ex);
        }
        return shortcut;
    }

    private static ArrayList<Shortcut> getShortcutsFromXml(Context context, XmlResourceParser xml, String namespace, String packageName) {
        ArrayList<Shortcut> shortcuts = new ArrayList<>();
        try {

            Shortcut shortcut = null;

            while (xml != null && xml.getEventType() != XmlResourceParser.END_DOCUMENT) {

                String name = xml.getName();

                if (NODE_SHORTCUT.equals(name)) {

                    addShortcut(context, shortcut, shortcuts);

                    String className = xml.getAttributeValue(namespace, ATTRIBUTE_CLASS);
                    shortcut = instantiateShortcutFromClassName(className);

                    if (shortcut != null) {

                        shortcut.enabled = xml.getAttributeBooleanValue(namespace, ATTRIBUTE_ENABLED, false);

                        String iconValue = xml.getAttributeValue(namespace, ATTRIBUTE_ICON);
                        shortcut.icon = ResourceUtils.getDrawableFromAddress(context, packageName, iconValue);
                        if (shortcut.icon == -1)
                            shortcut.icon = R.drawable.sym_def_app_icon;

                        shortcut.id = xml.getAttributeValue(namespace, ATTRIBUTE_SHORTCUT_ID);

                        String shortLabelValue = xml.getAttributeValue(namespace, ATTRIBUTE_SHORT_LABEL);
                        shortcut.shortLabel = ResourceUtils.getStringFromAddress(context, packageName, shortLabelValue);

                        String shortcutLongLabel = xml.getAttributeValue(namespace, ATTRIBUTE_LONG_LABEL);
                        shortcut.longLabel = ResourceUtils.getStringFromAddress(context, packageName, shortcutLongLabel);

                        String shortcutDisabledMessage = xml.getAttributeValue(namespace, ATTRIBUTE_DISABLED_MESSAGE);
                        shortcut.disabledMessage = ResourceUtils.getStringFromAddress(context, packageName, shortcutDisabledMessage);
                    }

                } else if (NODE_INTENT.equals(name) && shortcut != null && shortcut.intent == null) {

                    String targetPackage = xml.getAttributeValue(namespace, ATTRIBUTE_TARGET_PACKAGE);
                    if (targetPackage == null || targetPackage.length() == 0)
                        targetPackage = packageName;
                    shortcut.packageName = targetPackage;

                    String action = xml.getAttributeValue(namespace, ATTRIBUTE_ACTION);
                    String targetClass = xml.getAttributeValue(namespace, ATTRIBUTE_TARGET_CLASS);
                    String data = xml.getAttributeValue(namespace, ATTRIBUTE_DATA);
                    String type = xml.getAttributeValue(namespace, ATTRIBUTE_MIME_TYPE);
                    if (type == null)
                        type = xml.getAttributeValue(namespace, ATTRIBUTE_TYPE);

                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (action != null && action.length() > 0)
                        intent.setAction(action);

                    String componentPackage = targetPackage;
                    if (targetClass != null && targetClass.length() > 0) {
                        String[] separated = targetClass.split("/");
                        if (separated.length == 2) {
                            componentPackage = separated[0];
                            targetClass = separated[1];
                            intent.setPackage(componentPackage);
                        }
                        intent.setClassName(componentPackage, targetClass);
                    }
                    intent.setPackage(componentPackage);

                    if (data != null && data.length() > 0)
                        intent.setData(Uri.parse(data));

                    if (type != null && type.length() > 0)
                        intent.setType(type);

                    shortcut.intent = intent;
                    shortcut.init(context);

                } else if (NODE_EXTRA.equals(name) && shortcut != null && shortcut.intent != null) {

                    String extraName = xml.getAttributeValue(namespace, ATTRIBUTE_NAME);
                    String extraValue = xml.getAttributeValue(namespace, ATTRIBUTE_VALUE);

                    if (extraName != null && extraValue != null) {
                        if (TRUE.equalsIgnoreCase(extraValue) || FALSE.equalsIgnoreCase(extraValue)) {
                            shortcut.intent.putExtra(extraName, Boolean.parseBoolean(extraValue));
                        } else if (StringUtils.isNumeric(extraValue)) {
                            shortcut.intent.putExtra(extraName, Integer.parseInt(extraValue));
                        } else {
                            shortcut.intent.putExtra(extraName, extraValue);
                        }
                    }

                } else if (NODE_CATEGORIES.equals(name) && shortcut != null && shortcut.intent != null) {

                    String category = xml.getAttributeValue(namespace, ATTRIBUTE_NAME);
                    if (category != null ) {
                        shortcut.intent.addCategory(category);
                    }
                }

                xml.next();
            }

            addShortcut(context, shortcut, shortcuts);

        } catch (Exception ex) {
            //TrackingUtils.logException(ex);
        }
        return shortcuts;
    }

    private static void addShortcut(Context context, Shortcut shortcut, ArrayList<Shortcut> shortcuts) {

        if (context != null && shortcut != null && shortcuts != null && shortcut.intent != null) {

            Intent intent = shortcut.intent;
            String packageName = intent.getPackage();
            String targetClass = (intent.getComponent() != null)
                    ? intent.getComponent().getClassName() : null;

            if ((!CHECK_SHORTCUT_INTENT ||
                    (
                            intent.resolveActivity(getPackageManager(context)) != null)
                            && activityIsExported(context, packageName, targetClass))
                    ) {

                shortcuts.add(shortcut);
                shortcut = null;
            } else {
                shortcut = null;
            }
        }
    }

    private static boolean activityIsExported(Context context, String packageName, String className) {
        boolean exported = true;
        try {
            if (packageName != null && packageName.length() > 0
                    && className != null && className.length() > 0) {
                ComponentName componentName = new ComponentName(packageName, className);
                ActivityInfo activityInfo = getPackageManager(context)
                        .getActivityInfo(componentName, 0);
                if (activityInfo != null) {
                    exported = activityInfo.exported;
                }
            }
        } catch (PackageManager.NameNotFoundException nex) {
            nex.printStackTrace();
        } catch (Exception ex) {
            //TrackingUtils.logException(ex);
        }
        return exported;
    }

    public static void executeShortcut(Activity activity, Shortcut shortcut) {
        try {
            if (shortcut != null) {

                String[] permissions = shortcut.getPermissions();

                if (permissions == null || !PermissionUtils.checkPermissions(activity, permissions, 0)) {
                    shortcut.init(activity.getApplicationContext());
                    Intent intent = shortcut.intent;
                    if (intent != null)
                        activity.startActivity(intent);
                }
            }
        } catch (Exception ex) {
            //TrackingUtils.logException(ex);
        }
    }
}
