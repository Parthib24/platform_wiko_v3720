/**
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.server.fingerprint;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.IBinder;
import android.os.RemoteException;
import android.system.ErrnoException;
import android.util.Slog;

import android.os.Handler;

// TINNO BEGIN
// add for fingerprint support.
import com.ape.emFramework.Log;
import android.content.Intent;
import android.os.Bundle;
import android.app.KeyguardManager;
import com.ape.emFramework.EmFrameworkStatic;
import android.os.UserHandle;
// TINNO END

/**
 * A class to keep track of the authentication state for a given client.
 */
public abstract class AuthenticationClient extends ClientMonitor {
    private long mOpId;
    private static final String TAG = "AuthenticationClient-cls";
    public static final boolean DEBUG = FingerprintManager.DEBUG;
	
    public abstract boolean handleFailedAttempt();
    public abstract void resetFailedAttempts();

    private Handler mHandler = new Handler();



    public AuthenticationClient(Context context, long halDeviceId, IBinder token,
            IFingerprintServiceReceiver receiver, int targetUserId, int groupId, long opId,
            boolean restricted, String owner) {
        super(context, halDeviceId, token, receiver, targetUserId, groupId, restricted, owner);
        mOpId = opId;
    }

    @Override
    public boolean onAuthenticated(int fingerId, int groupId) {
        boolean result = false;
        boolean authenticated = fingerId != 0;

        IFingerprintServiceReceiver receiver = getReceiver();
        if (receiver != null) {
            try {
                MetricsLogger.action(getContext(), MetricsEvent.ACTION_FINGERPRINT_AUTH,
                        authenticated);
                if (!authenticated) {
                    receiver.onAuthenticationFailed(getHalDeviceId());
                } else {
                    if (true) {
                        Log.v(TAG, "onAuthenticated(owner=" + getOwnerString()
                                + ", id=" + fingerId + ", gp=" + groupId + ")");
                    }
                    
                    //TINNO BEGIN, modified by wenguangyu, for fingerprint
                    Fingerprint fp = (!getIsRestricted()||getOwnerString().equals("com.android.systemui"))
                            ? new Fingerprint("" /* TODO */, groupId, fingerId, getHalDeviceId())
                            : null;
                    if (true) {
                        Log.v(TAG, "onAuthenticated(owner=" + getOwnerString()
                                + ", fp=" + fp + ", gp=" + groupId + ")@@@@@@@@@@@@@@@@");
                    }                      
                    receiver.onAuthenticationSucceeded(getHalDeviceId(), fp, getTargetUserId());
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to notify Authenticated:", e);
                result = true; // client failed
            }
        } else {
            result = true; // client not listening
        }
        if (!authenticated) {
	     if (DEBUG) Log.i(TAG, "onAuthenticated failed:"+receiver);
            if (receiver != null) {
                FingerprintUtils.vibrateFingerprintError(getContext());
            }
            // allow system-defined limit of number of attempts before giving up
            boolean inLockoutMode =  handleFailedAttempt();
            // send lockout event in case driver doesn't enforce it.
            if (inLockoutMode) {
                try {
                    Slog.w(TAG, "Forcing lockout (fp driver code should do this!)");
                    receiver.onError(getHalDeviceId(),
                            FingerprintManager.FINGERPRINT_ERROR_LOCKOUT);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to notify lockout:", e);
                }
            }
            result |= inLockoutMode;
        } else {
            if (DEBUG) Log.i(TAG, "onAuthenticated success!:"+receiver);
            if (receiver != null) {
                // guomingyi add for quick wake support.
                if (FingerprintManager.isFpQuickWakeUpSupport(1)) {
                    mHandler.postDelayed(new Runnable() {  
                        @Override  
                        public void run() {
                            FingerprintUtils.vibrateFingerprintSuccess(getContext());
                        }
                    }, 40);
                } else {
                    FingerprintUtils.vibrateFingerprintSuccess(getContext());
                }
            }
            result |= true; // we have a valid fingerprint, done
            resetFailedAttempts();
        }
        return result;
    }

    /**
     * Start authentication
     */
    @Override
    public int start() {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w(TAG, "start authentication: no fingeprintd!");
            return ERROR_ESRCH;
        }
        try {
	     Log.i(TAG, "daemon.authenticate()");
            final int result = daemon.authenticate(mOpId, getGroupId());
            if (result != 0) {
                Slog.w(TAG, "startAuthentication failed, result=" + result);
                MetricsLogger.histogram(getContext(), "fingeprintd_auth_start_error", result);
                onError(FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE);
                return result;
            }
            if (DEBUG) Log.w(TAG, "client " + getOwnerString() + " is authenticating...");
        } catch (RemoteException e) {
            Slog.e(TAG, "startAuthentication failed", e);
            return ERROR_ESRCH;
        }
        return 0; // success
    }


    @Override
    public int stop(boolean initiatedByClient) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w(TAG, "stopAuthentication: no fingeprintd!");
            return ERROR_ESRCH;
        }
        try {
	     Log.i(TAG, "daemon.cancelAuthentication()");
            final int result = daemon.cancelAuthentication();
            if (result != 0) {
                Slog.w(TAG, "stopAuthentication failed, result=" + result);
                return result;
            }
            if (DEBUG) Log.w(TAG, "client " + getOwnerString() + " is no longer authenticating");
        } catch (RemoteException e) {
            Slog.e(TAG, "stopAuthentication failed", e);
            return ERROR_ESRCH;
        }
        return 0; // success
    }

    @Override
    public boolean onEnrollResult(int fingerId, int groupId, int rem) {
        if (DEBUG) Slog.w(TAG, "onEnrollResult() called for authenticate!");
        return true; // Invalid for Authenticate
    }

    @Override
    public boolean onRemoved(int fingerId, int groupId) {
        if (DEBUG) Slog.w(TAG, "onRemoved() called for authenticate!");
        return true; // Invalid for Authenticate
    }

    @Override
    public boolean onEnumerationResult(int fingerId, int groupId) {
        if (DEBUG) Slog.w(TAG, "onEnumerationResult() called for authenticate!");
        return true; // Invalid for Authenticate
    }
}
