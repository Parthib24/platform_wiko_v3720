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
import android.hardware.fingerprint.FlPassword;
import android.os.Bundle;

/**
 * Class managing the set of fingerprint per user across device reboots.
 */
class FileLockPasswordUserState {

    private static final String TAG = "FileLockPasswordUserState";
    private static final String FILELOCK_PASSWORD_FILE = "fileslock_password.xml";

    private static final String TAG_PASSWORDS = "passwords";
    private static final String TAG_PASSWORD = "password";

    public static final String ATTR_PASSWORK_ID = "passwordId";
    public static final String ATTR_CODE = "code";
    public static final String ATTR_TOKEN = "token";
    public static final String ATTR_PASSWORD_LENGTH  = "passwordLength";
    public static final String ATTR_QUESTION  = "question";
    public static final String ATTR_ANSWER  = "answer";
    public static final String ATTR_PATTERN_PASSWORD   = "patternPassword";
    public static final String ATTR_CURRENT_PASSWORD_TYPE = "currentPasswordType";
	
    public static final int NUMBER_PASSWORD = 0;
    public static final int PATTERN_PASSWORD = 1;	

    private final File mFile;

    @GuardedBy("this")
    private final ArrayList<FlPassword> mFlPasswords = new ArrayList<FlPassword>();
    private final Context mCtx;

    public FileLockPasswordUserState(Context ctx, int userId) {
        mFile = getFileForUser(userId);
        mCtx = ctx;
        synchronized (this) {
            readStateSyncLocked();
        }
    }
    
    public List<FlPassword> getFlPasswords() {
        synchronized (this) {
            return getCopy(mFlPasswords);
        }
    }

    private String getSpaceFilter(String original) {
        if (original == null || original.length() <= 0) {
            return original;
        }
        String startSpace = "^[  ]*";
        String endSpace = "[  ]*$";
        String buffer = original.replaceAll(startSpace, "").replaceAll(endSpace, "");
        return buffer;
    }
	   
    private int getFindStringCount(String from, String to) {
            if (from == null || to == null) {
                    return 0;
            }
            int count = 0;
            for (int i = 0; i < from.length();) {

                    int index = from.indexOf(to, i);
                    if (index >= 0) {
                            count++;
                            i = index + 1;
                    } else {
                            return count;
                    }
            }
            return count;

    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }	   
	   
    public String setNumberPassword(String passwordValue, int questionId, String answerValue) {
        synchronized (this) {

            if (passwordValue == null || questionId < 0 || answerValue == null || passwordValue.length() <= 0 || answerValue.length() <= 0) {
                return null;
            }

            String numberPasswordMd5 = md5(passwordValue);
            String answermd5 = md5(getSpaceFilter(answerValue));

            int passwordId = 0;
            String code = numberPasswordMd5;
            String token = numberPasswordMd5;
            int passwordLength = passwordValue.length();
            String question = Integer.toString(questionId);
            String answer = answermd5;
            String patternPassword ="";
            int currentPasswordType = NUMBER_PASSWORD;

            if (mFlPasswords.size() <= 0) {
                mFlPasswords.add(new FlPassword(passwordId, code, token, passwordLength, question, answer, patternPassword, currentPasswordType));
            } else {
                mFlPasswords.set(0, new FlPassword(passwordId, code, token, passwordLength, question, answer, patternPassword, currentPasswordType));
            } 
            scheduleWriteStateLocked();
	     return numberPasswordMd5;		
        }
    }


    public String changeNumberPassword(String passwordValue) {
        synchronized (this) {

            if (passwordValue == null ||passwordValue.length() <= 0) {
                return null;
            }
			
	     FlPassword flPassword;		
            if(mFlPasswords.size() <= 0) {
	         return null;	     			
            }		
	     FlPassword oldPassword = mFlPasswords.get(0);
		 
            String md5 = md5(passwordValue);

            int passwordId = 0;
            String code = md5;
            String token = md5;
            int passwordLength = passwordValue.length();
            String question = oldPassword.getQuestion();
            String answer = oldPassword.getAnswer();
            String patternPassword ="";
            int currentPasswordType = NUMBER_PASSWORD;

            mFlPasswords.set(0, new FlPassword(passwordId, code, token, passwordLength, question, answer, patternPassword, currentPasswordType));
	     
            scheduleWriteStateLocked();
	     return md5;		
        }
    }

    public String setPatternPassword(String passwordValue, int questionId, String answerValue) {
        synchronized (this) {

            if (passwordValue == null || questionId < 0 || answerValue == null || passwordValue.length() <= 0 || answerValue.length() <= 0) {
                return null;
            }

            String patternPasswordMd5 = md5(passwordValue);
            String answermd5 = md5(getSpaceFilter(answerValue));

            int passwordId = 0;
            String code = "";
            String token = patternPasswordMd5;
            int passwordLength = getFindStringCount(passwordValue, "row=");
            String question = Integer.toString(questionId);
            String answer = answermd5;
            String patternPassword =patternPasswordMd5;
            int currentPasswordType = PATTERN_PASSWORD;		

            if (mFlPasswords.size() <= 0) {
                mFlPasswords.add(new FlPassword(passwordId, code, token, passwordLength, question, answer, patternPassword, currentPasswordType));
            } else {
                mFlPasswords.set(0, new FlPassword(passwordId, code, token, passwordLength, question, answer, patternPassword, currentPasswordType));
            } 
            scheduleWriteStateLocked();
	     return patternPasswordMd5;		
        }
    }

    public String changePatternPassword(String passwordValue) {
        synchronized (this) {

            if (passwordValue == null || passwordValue.length() <= 0) {
                return null;
            }
	     FlPassword flPassword;		
            if(mFlPasswords.size() <= 0) {
	         return null;	     			
            }		
	     FlPassword oldPassword = mFlPasswords.get(0);
		  
            String patternPasswordMd5 = md5(passwordValue);

            int passwordId = 0;
            String code = "";
            String token = patternPasswordMd5;
            int passwordLength = getFindStringCount(passwordValue, "row=");
            String question = oldPassword.getQuestion();
            String answer = oldPassword.getAnswer();
            String patternPassword =patternPasswordMd5;
            int currentPasswordType = PATTERN_PASSWORD;
            mFlPasswords.set(0, new FlPassword(passwordId, code, token, passwordLength, question, answer, patternPassword, currentPasswordType)); 		
            scheduleWriteStateLocked();		
	     return patternPasswordMd5;		
        }
    }



    public String changePasswordQuestion(int questionId, String answerValue) {
        synchronized (this) {
            if (questionId < 0 || answerValue == null || answerValue.length() <= 0) {
                return null;
            }
	     FlPassword flPassword;		
            if(mFlPasswords.size() <= 0) {
	         return null;	     			
            }		
	     FlPassword oldPassword = mFlPasswords.get(0);
		  
            String answermd5 = md5(getSpaceFilter(answerValue));;

            int passwordId = 0;
            String code = oldPassword.getToken();
            String token = oldPassword.getToken();
            int passwordLength = oldPassword.getPasswordLength();
            String question =Integer.toString(questionId);;
            String answer = answermd5;
            String patternPassword =oldPassword.getPatternPassword();
            int currentPasswordType = oldPassword.getCurrentPasswordType();
			
            mFlPasswords.set(0, new FlPassword(passwordId, code, token, passwordLength, question, answer, patternPassword, currentPasswordType)); 		
            scheduleWriteStateLocked();		
	     return oldPassword.getToken();		
        }
    }
		
    public Bundle getFlPasswordData() {
        synchronized (this) {
            if(mFlPasswords.size() > 0) {
                FlPassword flPassword = mFlPasswords.get(0);
	        Bundle b = new Bundle();
		b.putInt("passwordId", flPassword.getPasswordId());
	        b.putString("code", flPassword.getCode());
	        b.putString("token", flPassword.getToken());
		b.putInt("passwordLength", flPassword.getPasswordLength());
	        b.putString("question", flPassword.getQuestion());
	        b.putString("answer", flPassword.getAnswer());
	        b.putString("patternPassword", flPassword.getPatternPassword());
	        b.putInt("currentPasswordType", flPassword.getCurrentPasswordType());
		return b;
	     }		  	
	 }
	 Slog.i(TAG, "getFlPasswordData failed, No flPassword data");
        return null;
    }	

    private static File getFileForUser(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), FILELOCK_PASSWORD_FILE);
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

    private ArrayList<FlPassword> getCopy(ArrayList<FlPassword> array) {
        ArrayList<FlPassword> result = new ArrayList<FlPassword>(array.size());
        for (int i = 0; i < array.size(); i++) {
            FlPassword pw = array.get(i);
             result.add(new FlPassword(pw.getPasswordId(), pw.getCode(), pw.getToken(), pw.getPasswordLength(), 
                 pw.getQuestion(), pw.getAnswer(), pw.getPatternPassword(), pw.getCurrentPasswordType()));                   							
        }
        return result;
    }
	
    private void doWriteState() {
        AtomicFile destination = new AtomicFile(mFile);

        ArrayList<FlPassword> flPasswords;

        synchronized (this) {
            flPasswords = getCopy(mFlPasswords);
        }

        FileOutputStream out = null;
        try {
            out = destination.startWrite();

            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, "utf-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, true);
            serializer.startTag(null, TAG_PASSWORDS);

            final int count = flPasswords.size();
            for (int i = 0; i < count; i++) {
                FlPassword pw = flPasswords.get(i);
                serializer.startTag(null, TAG_PASSWORD);
				
                serializer.attribute(null, ATTR_PASSWORK_ID, Integer.toString(pw.getPasswordId()));
                serializer.attribute(null, ATTR_CODE, pw.getCode());
                serializer.attribute(null, ATTR_TOKEN, pw.getToken());
                serializer.attribute(null, ATTR_PASSWORD_LENGTH, Integer.toString(pw.getPasswordLength()));

                serializer.attribute(null, ATTR_QUESTION, pw.getQuestion());
                serializer.attribute(null, ATTR_ANSWER, pw.getAnswer());
                serializer.attribute(null, ATTR_PATTERN_PASSWORD, pw.getPatternPassword());	
                serializer.attribute(null, ATTR_CURRENT_PASSWORD_TYPE, Integer.toString(pw.getCurrentPasswordType()));
				
                serializer.endTag(null, TAG_PASSWORD);
            }

            serializer.endTag(null, TAG_PASSWORDS);
            serializer.endDocument();
            destination.finishWrite(out);

            // Any error while writing is fatal.
        } catch (Throwable t) {
            Slog.wtf(TAG, "Failed to write settings, restoring backup", t);
            destination.failWrite(out);
            throw new IllegalStateException("Failed to write flPasswords", t);
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
            Slog.i(TAG, "No FlPassword state");
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
            if (tagName.equals(TAG_PASSWORDS)) {
                parsePasswordsLocked(parser);
            }
        }
    }

    private void parsePasswordsLocked(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        final int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }
			
            String tagName = parser.getName();			
            if (tagName.equals(TAG_PASSWORD)) {
				
                String passwordId = parser.getAttributeValue(null, ATTR_PASSWORK_ID);
                String code = parser.getAttributeValue(null, ATTR_CODE);
	         String token = parser.getAttributeValue(null, ATTR_TOKEN);
                String passwordLength = parser.getAttributeValue(null, ATTR_PASSWORD_LENGTH);
	         String question = parser.getAttributeValue(null, ATTR_QUESTION);
	         String answer = parser.getAttributeValue(null, ATTR_ANSWER);			
	         String patternPassword = parser.getAttributeValue(null, ATTR_PATTERN_PASSWORD);			
	         String currentPasswordType = parser.getAttributeValue(null, ATTR_CURRENT_PASSWORD_TYPE);			
  
                mFlPasswords.add(new FlPassword(Integer.parseInt(passwordId), code, token, Integer.parseInt(passwordLength), 
		      question, answer, patternPassword, Integer.parseInt(currentPasswordType)));	
            }
        }
    }

}
