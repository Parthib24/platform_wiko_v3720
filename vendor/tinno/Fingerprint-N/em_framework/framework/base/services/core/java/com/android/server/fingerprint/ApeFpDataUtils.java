package com.android.server.fingerprint;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import android.os.Bundle;

public class ApeFpDataUtils {

    private final String TAG = "ApeFpDataUtils";
	
    private static final Object sInstanceLock = new Object();
    private static ApeFpDataUtils sInstance;

    @GuardedBy("this")
    private final SparseArray<ApeFpDatasUserState> mUsers = new SparseArray<>();

    public static ApeFpDataUtils getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new ApeFpDataUtils();
            }
        }
        return sInstance;
    }

    private ApeFpDataUtils() {
    }

    public void addApeFpDataForUser(Context ctx, int fingerId, int userId) {
        getStateForUser(ctx, userId).addApeFpData(fingerId);
    }

    public void removeApeFpDataForUser(Context ctx, int fingerId, int userId) {
        getStateForUser(ctx, userId).removeApeFpData(fingerId);
    }

    public void updateFingerprintQBPackageInfoForUser(Context ctx, int fingerId, int userId,
        String appName, String packageName, String className, String category, String action, String data) {     
        getStateForUser(ctx, userId).updateApeFpDataQBPackageInfo(fingerId, appName, packageName, className, category, action, data);

    }	
    public void updateFingerprintQBContactsInfoForUser(Context ctx, int fingerId, int userId, 
        String phoneName, String phoneNumber, int contactPhotoId, int contactId) {     
        getStateForUser(ctx, userId).updateApeFpDataQBContactsInfo(fingerId, phoneName, phoneNumber, contactPhotoId, contactId);
    }

    public void updateFingerprintPrivLockValueForUser(Context ctx, int fingerId, int userId, 
        int allowUnlockApp) { 
        if (allowUnlockApp < 0) {
            return;
        }
        getStateForUser(ctx, userId).updateApeFpDataPrivLockValue(fingerId, allowUnlockApp);
    }
	
    public Bundle getApeFpDataItemForUser(Context ctx, int userId, int fingerId) {
         return getStateForUser(ctx, userId).getApeFpDataItem(fingerId);
    }

    private ApeFpDatasUserState getStateForUser(Context ctx, int userId) {
        synchronized (this) {
            ApeFpDatasUserState state = mUsers.get(userId);
            if (state == null) {
                state = new ApeFpDatasUserState(ctx, userId);
                mUsers.put(userId, state);
            }
            return state;
        }
    }	

}
