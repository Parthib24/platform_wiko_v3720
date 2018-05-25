/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.server.fingerprint;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;

import com.android.internal.annotations.GuardedBy;

import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.hardware.fingerprint.ApeFpData;
import android.os.Bundle;

/**
 * Class managing the set of ApeFpData per user across device reboots.
 */
class ApeFpDatasUserState {

    private static final String TAG = "ApeFpDatasUserState";
    private static final String APE_FPDATA_FILE = "apefpdata.xml";

    private static final String TAG_APEFPDATAS = "apefpdatas";
    private static final String TAG_APEFPDATA = "apefpdata";

    private static final String ATTR_FINGER_ID = "fingerId";

    private static final String ATTR_APP_NAME = "appName";
    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_CLASS_NAME = "className";
    private static final String ATTR_APP_CATEGORY = "category";
    private static final String ATTR_APP_ACTION = "action";
    private static final String ATTR_APP_DATA = "data";
	

    private static final String ATTR_PHONE_NAME = "phoneName";
    private static final String ATTR_PHONE_NUMBER = "phoneNumber";
    private static final String ATTR_CONTACT_PHOTO_ID = "contactPhotoId";
    private static final String ATTR_CONTACT_ID = "contactId";

    private static final String ATTR_ALLOW_UNLOCK_APP = "allowUnlockApp";

    private final File mFile;

    @GuardedBy("this")
    private final ArrayList<ApeFpData> mApeFpDatas = new ArrayList<ApeFpData>();
    private final Context mCtx;

    public ApeFpDatasUserState(Context ctx, int userId) {
        mFile = getFileForUser(userId);
        mCtx = ctx;
        synchronized (this) {
            readStateSyncLocked();
        }
    }

    public void addApeFpData(int fingerId) {
        synchronized (this) {
            mApeFpDatas.add(new ApeFpData(fingerId));
            scheduleWriteStateLocked();
        }
    }

    public void removeApeFpData(int fingerId) {
        synchronized (this) {
            for (int i = 0; i < mApeFpDatas.size(); i++) {
                if (mApeFpDatas.get(i).getFingerId() == fingerId) {
                    mApeFpDatas.remove(i);
                    scheduleWriteStateLocked();
                    break;
                }
            }
        }
    }
    public void updateApeFpDataQBPackageInfo(int fingerId, String appName, String packageName, String className, String category, String action, String data) {
        synchronized (this) {
            for (int i = 0; i < mApeFpDatas.size(); i++) {
                if (mApeFpDatas.get(i).getFingerId() == fingerId) {
                    ApeFpData old = mApeFpDatas.get(i);
		      if (!packageName.isEmpty()) {
                        mApeFpDatas.set(i, new ApeFpData(old.getFingerId(), 
			    appName, packageName, className, category, action, data,
                            "", "", -1, -1,
                            old.getAllowUnlockAppValue()));
		      } else {
                        mApeFpDatas.set(i, new ApeFpData(old.getFingerId(),
			    appName, packageName, className, category, action, data,
                            old.getQBPhoneName(), old.getQBPhoneNumber(), old.getQBContactPhotoId(), old.getQBContactId(),
                            old.getAllowUnlockAppValue()));
		      }			
                    scheduleWriteStateLocked();
                    break;
                }
            }
        }
    }

    public void updateApeFpDataQBContactsInfo(int fingerId, String phoneName, String phoneNumber, int contactPhotoId, int contactId) {
        synchronized (this) {
            for (int i = 0; i < mApeFpDatas.size(); i++) {
                if (mApeFpDatas.get(i).getFingerId() == fingerId) {
                    ApeFpData old = mApeFpDatas.get(i);
		      if (!phoneNumber.isEmpty()) {
                        mApeFpDatas.set(i, new ApeFpData(old.getFingerId(),
		              "", "", "", "", "", "",
                            phoneName, phoneNumber, contactPhotoId, contactId,
                            old.getAllowUnlockAppValue()));
		      } else {
                        mApeFpDatas.set(i, new ApeFpData(old.getFingerId(),
		              old.getQBAppName(), old.getQBPackageName(), old.getQBClassName(), old.getQBCategory(), old.getQBAction(), old.getQBData(),
                            phoneName, phoneNumber, contactPhotoId, contactId,
                            old.getAllowUnlockAppValue()));
		     }	
                    scheduleWriteStateLocked();
                    break;
                }
            }
        }
    }

   public void updateApeFpDataPrivLockValue(int fingerId, int allowUnlockApp) {
        synchronized (this) {
            for (int i = 0; i < mApeFpDatas.size(); i++) {
                if (mApeFpDatas.get(i).getFingerId() == fingerId) {
                    ApeFpData old = mApeFpDatas.get(i);
                    mApeFpDatas.set(i, new ApeFpData(old.getFingerId(),
                         old.getQBAppName(), old.getQBPackageName(), old.getQBClassName(), old.getQBCategory(), old.getQBAction(), old.getQBData(),
                         old.getQBPhoneName(), old.getQBPhoneNumber(), old.getQBContactPhotoId(), old.getQBContactId(),
                         allowUnlockApp));
                    scheduleWriteStateLocked();
                    break;
                }
            }
        }
   }
    public Bundle getApeFpDataItem(int fingerId) {
        synchronized (this) {
	     for (int i = 0; i < mApeFpDatas.size(); i++) {
                if (mApeFpDatas.get(i).getFingerId() == fingerId) {
                    ApeFpData apeFpDataItem = mApeFpDatas.get(i);
			Bundle b = new Bundle();
			b.putInt("ape_FingerId", apeFpDataItem.getFingerId());
			b.putString("ape_QBAppName", apeFpDataItem.getQBAppName());
			b.putString("ape_QBPackageName", apeFpDataItem.getQBPackageName());
			b.putString("ape_QBClassName", apeFpDataItem.getQBClassName());
			b.putString("ape_QBCategory", apeFpDataItem.getQBCategory());
			b.putString("ape_QBAction", apeFpDataItem.getQBAction());
			b.putString("ape_QBData", apeFpDataItem.getQBData());


			b.putString("ape_QBPhoneName", apeFpDataItem.getQBPhoneName());
			b.putString("ape_QBPhoneNumber", apeFpDataItem.getQBPhoneNumber());
			b.putInt("ape_QBContactPhotoId", apeFpDataItem.getQBContactPhotoId());
			b.putInt("ape_QBContactId", apeFpDataItem.getQBContactId());

			b.putInt("ape_AllowUnlockApp", apeFpDataItem.getAllowUnlockAppValue());
			return b;

		  }
	     }
	     Slog.i(TAG, "getApeFpDataItem failed, No ApeFpData match fingerId");
            return null;
	}
    }
	
    public List<ApeFpData> getApeFpDatas() {
        synchronized (this) {
            return getCopy(mApeFpDatas);
        }
    }



    private static File getFileForUser(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), APE_FPDATA_FILE);
    }

    private final Runnable mWriteStateRunnable = new Runnable() {
        @Override
        public void run() {
            doWriteState();
        }
    };

    private void scheduleWriteStateLocked() {
        AsyncTask.execute(mWriteStateRunnable);
    }

    private ArrayList<ApeFpData> getCopy(ArrayList<ApeFpData> array) {
        ArrayList<ApeFpData> result = new ArrayList<ApeFpData>(array.size());
        for (int i = 0; i < array.size(); i++) {
            ApeFpData fp = array.get(i);
             result.add(new ApeFpData(fp.getFingerId(),
                 fp.getQBAppName(), fp.getQBPackageName(), fp.getQBClassName(), fp.getQBCategory(), fp.getQBAction(), fp.getQBData(),
                 fp.getQBPhoneName(), fp.getQBPhoneNumber(), fp.getQBContactPhotoId(), fp.getQBContactId(),
                 fp.getAllowUnlockAppValue()));
        }
        return result;
    }

    private void doWriteState() {
        AtomicFile destination = new AtomicFile(mFile);

        ArrayList<ApeFpData> apeFpDatas;

        synchronized (this) {
            apeFpDatas = getCopy(mApeFpDatas);
        }

        FileOutputStream out = null;
        try {
            out = destination.startWrite();

            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, "utf-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, true);
            serializer.startTag(null, TAG_APEFPDATAS);

            final int count = apeFpDatas.size();
            for (int i = 0; i < count; i++) {
                ApeFpData fp = apeFpDatas.get(i);
                serializer.startTag(null, TAG_APEFPDATA);
                serializer.attribute(null, ATTR_FINGER_ID, Integer.toString(fp.getFingerId()));

                serializer.attribute(null, ATTR_APP_NAME, fp.getQBAppName().toString());
                serializer.attribute(null, ATTR_PACKAGE_NAME, fp.getQBPackageName().toString());
                serializer.attribute(null, ATTR_CLASS_NAME, fp.getQBClassName().toString());
                serializer.attribute(null, ATTR_APP_CATEGORY, fp.getQBCategory().toString());
                serializer.attribute(null, ATTR_APP_ACTION, fp.getQBAction().toString());
                serializer.attribute(null, ATTR_APP_DATA, fp.getQBData().toString());

                serializer.attribute(null, ATTR_PHONE_NAME, fp.getQBPhoneName().toString());
                serializer.attribute(null, ATTR_PHONE_NUMBER, fp.getQBPhoneNumber().toString());
                serializer.attribute(null, ATTR_CONTACT_PHOTO_ID, Integer.toString(fp.getQBContactPhotoId()));
                serializer.attribute(null, ATTR_CONTACT_ID, Integer.toString(fp.getQBContactId()));

                serializer.attribute(null, ATTR_ALLOW_UNLOCK_APP, Integer.toString(fp.getAllowUnlockAppValue()));

                serializer.endTag(null, TAG_APEFPDATA);
            }

            serializer.endTag(null, TAG_APEFPDATAS);
            serializer.endDocument();
            destination.finishWrite(out);

            // Any error while writing is fatal.
        } catch (Throwable t) {
            Slog.wtf(TAG, "Failed to write settings, restoring backup", t);
            destination.failWrite(out);
            throw new IllegalStateException("Failed to write ApeFpDatas", t);
        } finally {
            IoUtils.closeQuietly(out);
        }
    }

    private void readStateSyncLocked() {
        FileInputStream in;
        if (!mFile.exists()) {
            return;
        }
        try {
            in = new FileInputStream(mFile);
        } catch (FileNotFoundException fnfe) {
            Slog.i(TAG, "No ApeFpData state");
            return;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);
            parseStateLocked(parser);

        } catch (XmlPullParserException | IOException e) {
            throw new IllegalStateException("Failed parsing settings file: "
                    + mFile , e);
        } finally {
            IoUtils.closeQuietly(in);
        }
    }

    private void parseStateLocked(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        final int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }

            String tagName = parser.getName();
            if (tagName.equals(TAG_APEFPDATAS)) {
                parseApeFpDatasLocked(parser);
            }
        }
    }

    private void parseApeFpDatasLocked(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        final int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }

            String tagName = parser.getName();
            if (tagName.equals(TAG_APEFPDATA)) {
                String fingerId = parser.getAttributeValue(null, ATTR_FINGER_ID);
			
                String appName = parser.getAttributeValue(null, ATTR_APP_NAME);
                String packageName = parser.getAttributeValue(null, ATTR_PACKAGE_NAME);
                String className = parser.getAttributeValue(null, ATTR_CLASS_NAME);
                String category = parser.getAttributeValue(null, ATTR_APP_CATEGORY);
                String action = parser.getAttributeValue(null, ATTR_APP_ACTION);
                String data = parser.getAttributeValue(null, ATTR_APP_DATA);
				

                String phoneName = parser.getAttributeValue(null, ATTR_PHONE_NAME);
                String phoneNumber = parser.getAttributeValue(null, ATTR_PHONE_NUMBER);
                String ContactPhotoId = parser.getAttributeValue(null, ATTR_CONTACT_PHOTO_ID);
                String ContactId = parser.getAttributeValue(null, ATTR_CONTACT_ID);

                String allowUnlockApp = parser.getAttributeValue(null, ATTR_ALLOW_UNLOCK_APP);
  
                mApeFpDatas.add(new ApeFpData(Integer.parseInt(fingerId),
                    appName, packageName, className, category, action, data,
                    phoneName, phoneNumber, Integer.parseInt(ContactPhotoId), Integer.parseInt(ContactId),
                    Integer.parseInt(allowUnlockApp)));			
            }
        }
    }

}
