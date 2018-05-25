package com.android.server.fingerprint;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import android.hardware.fingerprint.FlPassword;
import android.os.Bundle;

public class FileLockPasswordUtil {

    private final String TAG = "FileLockPasswordUtil";
	
    private static final Object sInstanceLock = new Object();
    private static FileLockPasswordUtil sInstance;

    @GuardedBy("this")
    private final SparseArray<FileLockPasswordUserState> mUsers = new SparseArray<>();

    public static FileLockPasswordUtil getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FileLockPasswordUtil();
            }
        }
        return sInstance;
    }

    private FileLockPasswordUtil() {
    }
		
    public Bundle getFlPasswordDataForUser(Context context, int userId) {
        return getStateForUser(context, userId).getFlPasswordData();
    }

    public String changePatternPasswordForUser(Context context, int userId, String patternPassword) {
        return getStateForUser(context,userId).changePatternPassword(patternPassword);                              
    }

    public String changeNumberPasswordForUser(Context context, int userId, String numberPassword) {       
        return getStateForUser(context, userId).changeNumberPassword(numberPassword);
    }

    public String changePasswordQuestionForUser(Context context, int userId, int question, String answer) {
        return getStateForUser(context,userId).changePasswordQuestion(question, answer);
    }
    public String setNumberPasswordForUser(Context context, int userId, String numberPassword, int question, String answer) {
        return getStateForUser(context, userId).setNumberPassword(numberPassword, question, answer);
    }

    public String setPatternPasswordForUser(Context context, int userId, String patternPassword, int question, String answer) {
        return getStateForUser(context, userId).setPatternPassword(patternPassword, question, answer);               
    }

    private FileLockPasswordUserState getStateForUser(Context ctx, int userId) {
        synchronized (this) {
            FileLockPasswordUserState state = mUsers.get(userId);
            if (state == null) {
                state = new FileLockPasswordUserState(ctx, userId);
                mUsers.put(userId, state);
            }
            return state;
        }
    }	

}
