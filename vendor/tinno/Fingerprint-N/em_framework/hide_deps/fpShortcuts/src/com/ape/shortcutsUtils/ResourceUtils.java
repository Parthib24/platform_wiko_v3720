package com.ape.shortcutsUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;


import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by christopher ney on 13/01/16.
 */
public class ResourceUtils {

    public static final String TAG = ResourceUtils.class.getName();

    public static XmlResourceParser getXmlFromApplication(Context context, String packageName, int mXmlResID) {
        XmlResourceParser xmlResourceParser = null;

        try {

            PackageManager manager = context.getPackageManager();
            Resources apkResources = manager.getResourcesForApplication(packageName);

            if (mXmlResID != 0)
                xmlResourceParser = apkResources.getXml(mXmlResID);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return xmlResourceParser;
    }

    public static XmlResourceParser getXmlFromApplication(Context context, String packageName, String resourceName) {

        XmlResourceParser xmlResourceParser = null;

        try {

            PackageManager manager = context.getPackageManager();
            Resources apkResources = manager.getResourcesForApplication(packageName);

            int mXmlResID = apkResources.getIdentifier(resourceName, "xml", packageName);
            if (mXmlResID != 0)
                xmlResourceParser = apkResources.getXml(mXmlResID);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return xmlResourceParser;
    }

    public static Drawable getDrawableFromApplication(Context context, String packageName, String resourceName) {

        Drawable drawable = null;

        try {

            PackageManager manager = context.getPackageManager();
            Resources apkResources = manager.getResourcesForApplication(packageName);

            int mDrawableResID = apkResources.getIdentifier(resourceName, "drawable", packageName);
            drawable = apkResources.getDrawable(mDrawableResID);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return drawable;
    }

    /**
     *
     * @param context
     * @param packageName Package name
     * @param address Resource Integer Address @2130838026
     * @return
     */
    public static int getDrawableFromAddress(Context context, String packageName, String address) {

        if (context == null || address == null || address == null)
            return -1;//null;
        Drawable drawable = null;
        int drawableResID;
        try {

            PackageManager manager = context.getPackageManager();
            Resources apkResources = manager.getResourcesForApplication(packageName);
            if (address.startsWith("@drawable/")) {
                drawableResID = apkResources.getIdentifier(address.replace("@drawable/", ""), "drawable", packageName);
            } else if (address.startsWith("@")) {
                drawableResID = Integer.parseInt(address.replace("@", ""));
            } else {
                drawableResID = Integer.parseInt(address);
            }
            //drawable = apkResources.getDrawable(drawableResID);

        } catch (Exception e) {
            e.printStackTrace();
	     return -1;		
        }

        return drawableResID;//drawable;
    }

    public static String getStringFromAddress(Context context, String packageName, String address) {

        if (context == null || address == null || address == null)
            return null;
        String text = null;

        try {

            PackageManager manager = context.getPackageManager();
            Resources apkResources = manager.getResourcesForApplication(packageName);

            int stringID;
            if (address.startsWith("@string/")) {
                stringID = apkResources.getIdentifier(address.replace("@string/", ""), "string", packageName);
                text = apkResources.getString(stringID);
            } else if (address.startsWith("@")) {
                stringID = Integer.parseInt(address.replace("@", ""));
                text = apkResources.getString(stringID);
            } else {
                text = address;
            }

        } catch (Exception e) {
            e.printStackTrace();
	     return null;		
        }

        return text;
    }

    public static String getStringFromAddress(Context context, String packageName, int stringID) {

        if (context == null || stringID == 0)
            return null;
        String text = null;

        try {

            PackageManager manager = context.getPackageManager();
            Resources apkResources = manager.getResourcesForApplication(packageName);
            text = apkResources.getString(stringID);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }


    public static Drawable getDrawableFromApplication(Context context, String packageName, int resourceId) {

        Drawable drawable = null;

        try {

            PackageManager manager = context.getPackageManager();
            Resources apkResources = manager.getResourcesForApplication(packageName);

            drawable = apkResources.getDrawable(resourceId);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return drawable;
    }

    public static Drawable changeBitmapColor(Drawable source, int color) {
        source.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return source;
    }

    public static Drawable getApplicationIcon(Context context, String packageName) {
        Drawable icon = null;
        try {
            if (packageName != null) icon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }
        return icon;
    }

    /**
     * Return an Bitmap Image form SD Card.
     * @param filePath File path
     * @return Bitmap if exists
     */
    public static Bitmap getBitmapFromSdCard(String filePath) {
        Bitmap bmp = null;
        try {
            File f = new File(filePath);
            bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bmp;
    }

    public static String getJSONFromFile(String filePath) {

        FileInputStream stream = null;
        String jsonStr = null;
        try {

            File yourFile = new File(Environment.getExternalStorageDirectory() + "/" + filePath);

            if (yourFile.exists()) {
                stream = new FileInputStream(yourFile);
                FileChannel fc = stream.getChannel();
                MappedByteBuffer mappedByteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                jsonStr = Charset.defaultCharset().decode(mappedByteBuffer).toString();
                stream.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonStr;
    }

    /*public static String getJSONFromRaw(int rawId, Context context) {
        String jsonString = null;
        try {
            InputStream jsonStream = context.getResources().openRawResource(rawId);
            jsonString = Utils.stringWithInputStream(jsonStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonString;
    }*/

    public static Uri getDrawableUri(Context context, int resourceId) {
        Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(resourceId)
                + '/' + context.getResources().getResourceTypeName(resourceId)
                + '/' + context.getResources().getResourceEntryName(resourceId));
        return drawableUri;
    }

    /**
     * Retrieve the height of the system navigation bar
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        if (resources != null) {
            int id = resources.getIdentifier(
                    "navigation_bar_height",
                    "dimen", "android");
            if (id > 0) {
                return resources.getDimensionPixelSize(id);
            }
        }
        return 0;
    }

    /**
     * Check if current device has physical system Navigation bar
     * @param context
     * @return true if has physical Navigation bar
     */
    public static boolean isPermanentKeyPresent(Context context){
        try {
            if(context != null) {
                boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
                boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
                return hasBackKey && hasMenuKey;
            }

        }catch (Exception ex){
            //TrackingUtils.logException(ex);
        }
        return false;
    }


    public static int getDimensionPixelSizeByName(String name, Context context, int dimenDefault) {
        int dimenId = context.getResources().getIdentifier(name, "dimen", context.getPackageName());
        if (dimenId != 0) {
            return context.getResources().getDimensionPixelSize(dimenId);
        } else if (dimenDefault != 0) {
            return context.getResources().getDimensionPixelSize(dimenDefault);
        } else {
            return 0;
        }
    }

}
