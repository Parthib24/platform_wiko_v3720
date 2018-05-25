/**
 * Copyright (C) 2014 The Android Open Source Project
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

package android.hardware.fingerprint;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.keystore.AndroidKeyStoreProvider;
import android.util.Log;
import android.util.Slog;
import android.app.KeyguardManager;
import java.security.Signature;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.Mac;

import static android.Manifest.permission.INTERACT_ACROSS_USERS;
import static android.Manifest.permission.USE_FINGERPRINT;
import static android.Manifest.permission.MANAGE_FINGERPRINT;

//TINNO BEGIN
import com.ape.emFramework.EmFrameworkStatic;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
//import com.ape.emFramework.Log;
import com.android.internal.widget.LockPatternUtils;
import android.os.SystemProperties;
//TINNO END


/**
 * A class that coordinates access to the fingerprint hardware.
 * <p>
 * Use {@link android.content.Context#getSystemService(java.lang.String)}
 * with argument {@link android.content.Context#FINGERPRINT_SERVICE} to get
 * an instance of this class.
 */

public class FingerprintManager {
    private static final String TAG = "FingerprintManager";
    private static final boolean USER_DEBUG = !("user".equals(android.os.Build.TYPE));
    public static final boolean DEBUG = ((SystemProperties.getInt("persist.tinno.fp.d", 1) > 0) || USER_DEBUG);

    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_ERROR = 104;
    private static final int MSG_REMOVED = 105;

    
    /**
     * @hide
     */
    public static final int FINGERPRINT_NAVIGATION_SWITCH_PAGE_TYPE = 0;

    /**
     * Fingerprint sensor navigation type
     * @hide
     */
    public static final int FINGERPRINT_NAVIGATION_DISPLAY_NOTIFICATION_TYPE = 1;

    /**
     * Fingerprint sensor navigation type
     * @hide
     */
    public static final int FINGERPRINT_NAVIGATION_STOP_ALARM_TYPE = 2;

    /**
     * Fingerprint sensor navigation type
     * @hide
     */
    public static final int FINGERPRINT_NAVIGATION_ANSWER_CALL_TYPE = 3;

    /**
     * Fingerprint sensor navigation type
     * @hide
     */
    public static final int FINGERPRINT_NAVIGATION_CONTINOUS_CAPTURE_TYPE = 4;
	
    //TINNO END

    //
    // Error messages from fingerprint hardware during initilization, enrollment, authentication or
    // removal. Must agree with the list in fingerprint.h
    //

    /**
     * The hardware is unavailable. Try again later.
     */
    public static final int FINGERPRINT_ERROR_HW_UNAVAILABLE = 1;

    /**
     * Error state returned when the sensor was unable to process the current image.
     */
    public static final int FINGERPRINT_ERROR_UNABLE_TO_PROCESS = 2;

    /**
     * Error state returned when the current request has been running too long. This is intended to
     * prevent programs from waiting for the fingerprint sensor indefinitely. The timeout is
     * platform and sensor-specific, but is generally on the order of 30 seconds.
     */
    public static final int FINGERPRINT_ERROR_TIMEOUT = 3;

    /**
     * Error state returned for operations like enrollment; the operation cannot be completed
     * because there's not enough storage remaining to complete the operation.
     */
    public static final int FINGERPRINT_ERROR_NO_SPACE = 4;

    /**
     * The operation was canceled because the fingerprint sensor is unavailable. For example,
     * this may happen when the user is switched, the device is locked or another pending operation
     * prevents or disables it.
     */
    public static final int FINGERPRINT_ERROR_CANCELED = 5;

    /**
     * The {@link FingerprintManager#remove(Fingerprint, RemovalCallback)} call failed. Typically
     * this will happen when the provided fingerprint id was incorrect.
     *
     * @hide
     */
    public static final int FINGERPRINT_ERROR_UNABLE_TO_REMOVE = 6;

   /**
     * The operation was canceled because the API is locked out due to too many attempts.
     */
    public static final int FINGERPRINT_ERROR_LOCKOUT = 7;

    /**
     * Hardware vendors may extend this list if there are conditions that do not fall under one of
     * the above categories. Vendors are responsible for providing error strings for these errors.
     * @hide
     */
    public static final int FINGERPRINT_ERROR_VENDOR_BASE = 1000;

    //
    // Image acquisition messages. Must agree with those in fingerprint.h
    //

    /**
     * The image acquired was good.
     */
    public static final int FINGERPRINT_ACQUIRED_GOOD = 0;

    /**
     * Only a partial fingerprint image was detected. During enrollment, the user should be
     * informed on what needs to happen to resolve this problem, e.g. "press firmly on sensor."
     */
    public static final int FINGERPRINT_ACQUIRED_PARTIAL = 1;

    /**
     * The fingerprint image was too noisy to process due to a detected condition (i.e. dry skin) or
     * a possibly dirty sensor (See {@link #FINGERPRINT_ACQUIRED_IMAGER_DIRTY}).
     */
    public static final int FINGERPRINT_ACQUIRED_INSUFFICIENT = 2;

    /**
     * The fingerprint image was too noisy due to suspected or detected dirt on the sensor.
     * For example, it's reasonable return this after multiple
     * {@link #FINGERPRINT_ACQUIRED_INSUFFICIENT} or actual detection of dirt on the sensor
     * (stuck pixels, swaths, etc.). The user is expected to take action to clean the sensor
     * when this is returned.
     */
    public static final int FINGERPRINT_ACQUIRED_IMAGER_DIRTY = 3;

    /**
     * The fingerprint image was unreadable due to lack of motion. This is most appropriate for
     * linear array sensors that require a swipe motion.
     */
    public static final int FINGERPRINT_ACQUIRED_TOO_SLOW = 4;

    /**
     * The fingerprint image was incomplete due to quick motion. While mostly appropriate for
     * linear array sensors,  this could also happen if the finger was moved during acquisition.
     * The user should be asked to move the finger slower (linear) or leave the finger on the sensor
     * longer.
     */
    public static final int FINGERPRINT_ACQUIRED_TOO_FAST = 5;

    /**
     * Hardware vendors may extend this list if there are conditions that do not fall under one of
     * the above categories. Vendors are responsible for providing error strings for these errors.
     * @hide
     */
    public static final int FINGERPRINT_ACQUIRED_VENDOR_BASE = 1000;

    private IFingerprintService mService;
    private Context mContext;
    private IBinder mToken = new Binder();
    private AuthenticationCallback mAuthenticationCallback;
    private EnrollmentCallback mEnrollmentCallback;
    private RemovalCallback mRemovalCallback;
    private CryptoObject mCryptoObject;
    private Fingerprint mRemovalFingerprint;
    private Handler mHandler;

    LockPatternUtils mLockPatternUtils;//add by wenguangyu, for fingerprint
    
    private class OnEnrollCancelListener implements OnCancelListener {
        @Override
        public void onCancel() {
            cancelEnrollment();
        }
    }

    private class OnAuthenticationCancelListener implements OnCancelListener {
        private CryptoObject mCrypto;

        public OnAuthenticationCancelListener(CryptoObject crypto) {
            mCrypto = crypto;
        }

        @Override
        public void onCancel() {
            cancelAuthentication(mCrypto);
        }
    }

    /**
     * A wrapper class for the crypto objects supported by FingerprintManager. Currently the
     * framework supports {@link Signature}, {@link Cipher} and {@link Mac} objects.
     */
    public static final class CryptoObject {

        public CryptoObject(@NonNull Signature signature) {
            mCrypto = signature;
        }

        public CryptoObject(@NonNull Cipher cipher) {
            mCrypto = cipher;
        }

        public CryptoObject(@NonNull Mac mac) {
            mCrypto = mac;
        }

        /**
         * Get {@link Signature} object.
         * @return {@link Signature} object or null if this doesn't contain one.
         */
        public Signature getSignature() {
            return mCrypto instanceof Signature ? (Signature) mCrypto : null;
        }

        /**
         * Get {@link Cipher} object.
         * @return {@link Cipher} object or null if this doesn't contain one.
         */
        public Cipher getCipher() {
            return mCrypto instanceof Cipher ? (Cipher) mCrypto : null;
        }

        /**
         * Get {@link Mac} object.
         * @return {@link Mac} object or null if this doesn't contain one.
         */
        public Mac getMac() {
            return mCrypto instanceof Mac ? (Mac) mCrypto : null;
        }

        /**
         * @hide
         * @return the opId associated with this object or 0 if none
         */
        public long getOpId() {
            return mCrypto != null ?
                    AndroidKeyStoreProvider.getKeyStoreOperationHandle(mCrypto) : 0;
        }

        private final Object mCrypto;
    };

    /**
     * Container for callback data from {@link FingerprintManager#authenticate(CryptoObject,
     *     CancellationSignal, int, AuthenticationCallback, Handler)}.
     */
    public static class AuthenticationResult {
        private Fingerprint mFingerprint;
        private CryptoObject mCryptoObject;
        private int mUserId;

        /**
         * Authentication result
         *
         * @param crypto the crypto object
         * @param fingerprint the recognized fingerprint data, if allowed.
         * @hide
         */
        public AuthenticationResult(CryptoObject crypto, Fingerprint fingerprint, int userId) {
            mCryptoObject = crypto;
            mFingerprint = fingerprint;
            mUserId = userId;
        }

        /**
         * Obtain the crypto object associated with this transaction
         * @return crypto object provided to {@link FingerprintManager#authenticate(CryptoObject,
         *     CancellationSignal, int, AuthenticationCallback, Handler)}.
         */
        public CryptoObject getCryptoObject() { return mCryptoObject; }

        /**
         * Obtain the Fingerprint associated with this operation. Applications are strongly
         * discouraged from associating specific fingers with specific applications or operations.
         *
         * @hide
         */
        public Fingerprint getFingerprint() { return mFingerprint; }

        /**
         * Obtain the userId for which this fingerprint was authenticated.
         * @hide
         */
        public int getUserId() { return mUserId; }
    };

    /**
     * Callback structure provided to {@link FingerprintManager#authenticate(CryptoObject,
     * CancellationSignal, int, AuthenticationCallback, Handler)}. Users of {@link
     * FingerprintManager#authenticate(CryptoObject, CancellationSignal,
     * int, AuthenticationCallback, Handler) } must provide an implementation of this for listening to
     * fingerprint events.
     */
    public static abstract class AuthenticationCallback {
        /**
         * Called when an unrecoverable error has been encountered and the operation is complete.
         * No further callbacks will be made on this object.
         * @param errorCode An integer identifying the error message
         * @param errString A human-readable error string that can be shown in UI
         */
        public void onAuthenticationError(int errorCode, CharSequence errString) { }

        /**
         * Called when a recoverable error has been encountered during authentication. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it."
         * @param helpCode An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) { }

        /**
         * Called when a fingerprint is recognized.
         * @param result An object containing authentication-related data
         */
        public void onAuthenticationSucceeded(AuthenticationResult result) { }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        public void onAuthenticationFailed() { }

        /**
         * Called when a fingerprint image has been acquired, but wasn't processed yet.
         *
         * @param acquireInfo one of FINGERPRINT_ACQUIRED_* constants
         * @hide
         */
        public void onAuthenticationAcquired(int acquireInfo) {}
    };

    /**
     * Callback structure provided to {@link FingerprintManager#enroll(long, EnrollmentCallback,
     * CancellationSignal, int). Users of {@link #FingerprintManager()}
     * must provide an implementation of this to {@link FingerprintManager#enroll(long,
     * CancellationSignal, int, EnrollmentCallback) for listening to fingerprint events.
     *
     * @hide
     */
    public static abstract class EnrollmentCallback {
        /**
         * Called when an unrecoverable error has been encountered and the operation is complete.
         * No further callbacks will be made on this object.
         * @param errMsgId An integer identifying the error message
         * @param errString A human-readable error string that can be shown in UI
         */
        public void onEnrollmentError(int errMsgId, CharSequence errString) { }

        /**
         * Called when a recoverable error has been encountered during enrollment. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it" or what they need to do next, such as
         * "Touch sensor again."
         * @param helpMsgId An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) { }

        /**
         * Called as each enrollment step progresses. Enrollment is considered complete when
         * remaining reaches 0. This function will not be called if enrollment fails. See
         * {@link EnrollmentCallback#onEnrollmentError(int, CharSequence)}
         * @param remaining The number of remaining steps
         */
        public void onEnrollmentProgress(int remaining) { }
    };

    /**
     * Callback structure provided to {@link FingerprintManager#remove(int). Users of
     * {@link #FingerprintManager()} may optionally provide an implementation of this to
     * {@link FingerprintManager#remove(int, int, RemovalCallback)} for listening to
     * fingerprint template removal events.
     *
     * @hide
     */
    public static abstract class RemovalCallback {
        /**
         * Called when the given fingerprint can't be removed.
         * @param fp The fingerprint that the call attempted to remove
         * @param errMsgId An associated error message id
         * @param errString An error message indicating why the fingerprint id can't be removed
         */
        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) { }

        /**
         * Called when a given fingerprint is successfully removed.
         * @param fingerprint the fingerprint template that was removed.
         */
        public void onRemovalSucceeded(Fingerprint fingerprint) { }
    };

    /**
     * @hide
     */
    public static abstract class LockoutResetCallback {

        /**
         * Called when lockout period expired and clients are allowed to listen for fingerprint
         * again.
         */
        public void onLockoutReset() { }
    };

    /**
     * Request authentication of a crypto object. This call warms up the fingerprint hardware
     * and starts scanning for a fingerprint. It terminates when
     * {@link AuthenticationCallback#onAuthenticationError(int, CharSequence)} or
     * {@link AuthenticationCallback#onAuthenticationSucceeded(AuthenticationResult)} is called, at
     * which point the object is no longer valid. The operation can be canceled by using the
     * provided cancel object.
     *
     * @param crypto object associated with the call or null if none required.
     * @param cancel an object that can be used to cancel authentication
     * @param flags optional flags; should be 0
     * @param callback an object to receive authentication events
     * @param handler an optional handler to handle callback events
     *
     * @throws IllegalArgumentException if the crypto operation is not supported or is not backed
     *         by <a href="{@docRoot}training/articles/keystore.html">Android Keystore
     *         facility</a>.
     * @throws IllegalStateException if the crypto primitive is not initialized.
     */
    @RequiresPermission(USE_FINGERPRINT)
    public void authenticate(@Nullable CryptoObject crypto, @Nullable CancellationSignal cancel,
            int flags, @NonNull AuthenticationCallback callback, @Nullable Handler handler) {
        authenticate(crypto, cancel, flags, callback, handler, UserHandle.myUserId());
    }

    /**
     * Use the provided handler thread for events.
     * @param handler
     */
    private void useHandler(Handler handler) {
        if (handler != null) {
            mHandler = new MyHandler(handler.getLooper());
        } else if (mHandler.getLooper() != mContext.getMainLooper()){
            mHandler = new MyHandler(mContext.getMainLooper());
        }
    }

    /**
     * Per-user version
     * @hide
     */
    @RequiresPermission(USE_FINGERPRINT)
    public void authenticate(@Nullable CryptoObject crypto, @Nullable CancellationSignal cancel,
            int flags, @NonNull AuthenticationCallback callback, Handler handler, int userId) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an authentication callback");
        }

        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "authentication already canceled");
                return;
            } else {
                cancel.setOnCancelListener(new OnAuthenticationCancelListener(crypto));
            }
        }

        if (mService != null) try {
            useHandler(handler);
            mAuthenticationCallback = callback;
            mCryptoObject = crypto;
            long sessionId = crypto != null ? crypto.getOpId() : 0;
	     Log.i(TAG, "authenticate | mContext.getOpPackageName():"+mContext.getOpPackageName()+" userId:"+userId+" callback:"+callback);
		 
            mService.authenticate(mToken, sessionId, userId, mServiceReceiver, flags,
                    mContext.getOpPackageName());
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception while authenticating: ");
            if (callback != null) {
                // Though this may not be a hardware issue, it will cause apps to give up or try
                // again later.
                callback.onAuthenticationError(FINGERPRINT_ERROR_HW_UNAVAILABLE,
                        getErrorString(FINGERPRINT_ERROR_HW_UNAVAILABLE));
            }
        }
    }




    /**
     * Request fingerprint enrollment. This call warms up the fingerprint hardware
     * and starts scanning for fingerprints. Progress will be indicated by callbacks to the
     * {@link EnrollmentCallback} object. It terminates when
     * {@link EnrollmentCallback#onEnrollmentError(int, CharSequence)} or
     * {@link EnrollmentCallback#onEnrollmentProgress(int) is called with remaining == 0, at
     * which point the object is no longer valid. The operation can be canceled by using the
     * provided cancel object.
     * @param token a unique token provided by a recent creation or verification of device
     * credentials (e.g. pin, pattern or password).
     * @param cancel an object that can be used to cancel enrollment
     * @param flags optional flags
     * @param userId the user to whom this fingerprint will belong to
     * @param callback an object to receive enrollment events
     * @hide
     */
    @RequiresPermission(MANAGE_FINGERPRINT)
    public void enroll(byte [] token, CancellationSignal cancel, int flags,
            int userId, EnrollmentCallback callback) {
        if (userId == UserHandle.USER_CURRENT) {
            userId = getCurrentUserId();
        }
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an enrollment callback");
        }

        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "enrollment already canceled");
                return;
            } else {
                cancel.setOnCancelListener(new OnEnrollCancelListener());
            }
        }

        if (mService != null) try {
            mEnrollmentCallback = callback;
            mService.enroll(mToken, token, userId, mServiceReceiver, flags,
                    mContext.getOpPackageName());
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in enroll: ");
            if (callback != null) {
                // Though this may not be a hardware issue, it will cause apps to give up or try
                // again later.
                callback.onEnrollmentError(FINGERPRINT_ERROR_HW_UNAVAILABLE,
                        getErrorString(FINGERPRINT_ERROR_HW_UNAVAILABLE));
            }
        }
    }

    /**
     * Requests a pre-enrollment auth token to tie enrollment to the confirmation of
     * existing device credentials (e.g. pin/pattern/password).
     * @hide
     */
    @RequiresPermission(MANAGE_FINGERPRINT)
    public long preEnroll() {
        long result = 0;
        if (mService != null) try {
            result = mService.preEnroll(mToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
        return result;
    }

    /**
     * Finishes enrollment and cancels the current auth token.
     * @hide
     */
    @RequiresPermission(MANAGE_FINGERPRINT)
    public int postEnroll() {
        int result = 0;
        if (mService != null) try {
            result = mService.postEnroll(mToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
        return result;
    }

    /**
     * Sets the active user. This is meant to be used to select the current profile for enrollment
     * to allow separate enrolled fingers for a work profile
     * @param userId
     * @hide
     */
    @RequiresPermission(MANAGE_FINGERPRINT)
    public void setActiveUser(int userId) {
        if (mService != null) try {
            mService.setActiveUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Remove given fingerprint template from fingerprint hardware and/or protected storage.
     * @param fp the fingerprint item to remove
     * @param userId the user who this fingerprint belongs to
     * @param callback an optional callback to verify that fingerprint templates have been
     * successfully removed. May be null of no callback is required.
     *
     * @hide
     */
    @RequiresPermission(MANAGE_FINGERPRINT)
    public void remove(Fingerprint fp, int userId, RemovalCallback callback) {
        if (mService != null) try {
            mRemovalCallback = callback;
            mRemovalFingerprint = fp;
            mService.remove(mToken, fp.getFingerId(), fp.getGroupId(), userId, mServiceReceiver);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in remove: ");
            if (callback != null) {
                callback.onRemovalError(fp, FINGERPRINT_ERROR_HW_UNAVAILABLE,
                        getErrorString(FINGERPRINT_ERROR_HW_UNAVAILABLE));
            }
        }
    }

    /**
     * Renames the given fingerprint template
     * @param fpId the fingerprint id
     * @param userId the user who this fingerprint belongs to
     * @param newName the new name
     *
     * @hide
     */
    @RequiresPermission(MANAGE_FINGERPRINT)
    public void rename(int fpId, int userId, String newName) {
        // Renames the given fpId
        if (mService != null) {
            try {
                mService.rename(fpId, userId, newName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "rename(): Service not connected!");
        }
    }

    /**
     * Obtain the list of enrolled fingerprints templates.
     * @return list of current fingerprint items
     *
     * @hide
     */
    @RequiresPermission(USE_FINGERPRINT)
    public List<Fingerprint> getEnrolledFingerprints(int userId) {
        if (mService != null) try {
            return mService.getEnrolledFingerprints(userId, mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
        return null;
    }

    /**
     * Obtain the list of enrolled fingerprints templates.
     * @return list of current fingerprint items
     *
     * @hide
     */
    @RequiresPermission(USE_FINGERPRINT)
    public List<Fingerprint> getEnrolledFingerprints() {
        return getEnrolledFingerprints(UserHandle.myUserId());
    }

    /**
     * Determine if there is at least one fingerprint enrolled.
     *
     * @return true if at least one fingerprint is enrolled, false otherwise
     */
    @RequiresPermission(USE_FINGERPRINT)
    public boolean hasEnrolledFingerprints() {
        if (mService != null) try {
            return mService.hasEnrolledFingerprints(
                    UserHandle.myUserId(), mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
        return false;
    }

    /**
     * @hide
     */
    @RequiresPermission(allOf = {
            USE_FINGERPRINT,
            INTERACT_ACROSS_USERS})
    public boolean hasEnrolledFingerprints(int userId) {
        if (mService != null) try {
            return mService.hasEnrolledFingerprints(userId, mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
        return false;
    }

    /**
     * Determine if fingerprint hardware is present and functional.
     *
     * @return true if hardware is present and functional, false otherwise.
     */
    @RequiresPermission(USE_FINGERPRINT)
    public boolean isHardwareDetected() {
        if (mService != null) {
            try {
                long deviceId = 0; /* TODO: plumb hardware id to FPMS */
                return mService.isHardwareDetected(deviceId, mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "isFingerprintHardwareDetected(): Service not connected!");
        }
        return false;
    }

    /**
     * Retrieves the authenticator token for binding keys to the lifecycle
     * of the current set of fingerprints. Used only by internal clients.
     *
     * @hide
     */
    public long getAuthenticatorId() {
        if (mService != null) {
            try {
                return mService.getAuthenticatorId(mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "getAuthenticatorId(): Service not connected!");
        }
        return 0;
    }

    /**
     * Reset the lockout timer when asked to do so by keyguard.
     *
     * @param token an opaque token returned by password confirmation.
     *
     * @hide
     */
    public void resetTimeout(byte[] token) {
        if (mService != null) {
            try {
                mService.resetTimeout(token);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "resetTimeout(): Service not connected!");
        }
    }

    /**
     * @hide
     */
    public void addLockoutResetCallback(final LockoutResetCallback callback) {
        if (mService != null) {
            try {
                final PowerManager powerManager = mContext.getSystemService(PowerManager.class);
                mService.addLockoutResetCallback(
                        new IFingerprintServiceLockoutResetCallback.Stub() {

                    @Override
                    public void onLockoutReset(long deviceId) throws RemoteException {
                        final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                                PowerManager.PARTIAL_WAKE_LOCK, "lockoutResetCallback");
                        wakeLock.acquire();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    callback.onLockoutReset();
                                } finally {
                                    wakeLock.release();
                                }
                            }
                        });
                    }
                });
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "addLockoutResetCallback(): Service not connected!");
        }
    }

    private class MyHandler extends Handler {
        private MyHandler(Context context) {
            super(context.getMainLooper());
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            switch(msg.what) {
                case MSG_ENROLL_RESULT:
                    sendEnrollResult((Fingerprint) msg.obj, msg.arg1 /* remaining */);
                    break;
                case MSG_ACQUIRED:
                    sendAcquiredResult((Long) msg.obj /* deviceId */, msg.arg1 /* acquire info */);
                    break;
                case MSG_AUTHENTICATION_SUCCEEDED:
                    sendAuthenticatedSucceeded((Fingerprint) msg.obj, msg.arg1 /* userId */);
                    break;
                case MSG_AUTHENTICATION_FAILED:
                    sendAuthenticatedFailed();
                    break;
                case MSG_ERROR:
                    sendErrorResult((Long) msg.obj /* deviceId */, msg.arg1 /* errMsgId */);
                    break;
                case MSG_REMOVED:
                    sendRemovedResult((Long) msg.obj /* deviceId */, msg.arg1 /* fingerId */,
                            msg.arg2 /* groupId */);
		   //TINNO BEGIN, added by wenguangyu, for fingerprint
		      break;
                case EmFrameworkStatic.MSG_CHECK_QBSTATE:
		      int fingerId = msg.arg1;
		      int userId = msg.arg2;	  
		      if (EmFrameworkStatic.checkQuickBootState(mContext,mHandler,fingerId,userId)) {
                        EmFrameworkStatic.LunchQuickBootByFp(fingerId, mContext, userId);                            
		      }					
	             break;
		   //TINNO END, added by wenguangyu, for fingerprint
            }
        }

        private void sendRemovedResult(long deviceId, int fingerId, int groupId) {
            if (mRemovalCallback != null) {
                int reqFingerId = mRemovalFingerprint.getFingerId();
                int reqGroupId = mRemovalFingerprint.getGroupId();
                if (reqFingerId != 0 && fingerId != 0  &&  fingerId != reqFingerId) {
                    Log.w(TAG, "Finger id didn't match: " + fingerId + " != " + reqFingerId);
                    return;
                }
                if (groupId != reqGroupId) {
                    Log.w(TAG, "Group id didn't match: " + groupId + " != " + reqGroupId);
                    return;
                }
                mRemovalCallback.onRemovalSucceeded(new Fingerprint(null, groupId, fingerId,
                        deviceId));
            }
        }

        private void sendErrorResult(long deviceId, int errMsgId) {
            if (getErrorString(errMsgId) == null) { 
                Log.e(TAG, "sendErrorResult: getErrorString is null!");
                return; 
            }
			
            if (mEnrollmentCallback != null) {
                mEnrollmentCallback.onEnrollmentError(errMsgId, getErrorString(errMsgId));
            } else if (mAuthenticationCallback != null) {
                mAuthenticationCallback.onAuthenticationError(errMsgId, getErrorString(errMsgId));
            } else if (mRemovalCallback != null) {
                mRemovalCallback.onRemovalError(mRemovalFingerprint, errMsgId,
                        getErrorString(errMsgId));
            }
        }

        private void sendEnrollResult(Fingerprint fp, int remaining) {
            if (mEnrollmentCallback != null) {
                mEnrollmentCallback.onEnrollmentProgress(remaining);
            }
        }

        private void sendAuthenticatedSucceeded(Fingerprint fp, int userId) {
            if (mAuthenticationCallback != null) {
                final AuthenticationResult result =
                        new AuthenticationResult(mCryptoObject, fp, userId);
                mAuthenticationCallback.onAuthenticationSucceeded(result);
                //TINNO BEGIN, add by wenguangyu, for fingerprint
                String opName = mContext.getOpPackageName();
                Slog.v(TAG, "sendAuthenticatedSucceeded:opName = " + opName + "userId = " + userId);
                if (opName != null && opName.equals("com.android.systemui")) {
                    if(result.getFingerprint() != null) {
			   int fingerId = result.getFingerprint().getFingerId();
                        int strongAuth = mLockPatternUtils.getStrongAuthForUser(userId);					
                        Slog.v(TAG, "sendAuthenticatedSucceeded:strongAuth = " + strongAuth);
                        if (strongAuth == 1) {
                            Slog.v(TAG, "sendAuthenticatedSucceeded:the strongAuth state is SinceBoot , no need to check QuickBootState!");							
                            return;
			   } 			   				
		          if (EmFrameworkStatic.checkQuickBootState(mContext,mHandler,fingerId, userId)) {
				mHandler.removeMessages(EmFrameworkStatic.MSG_CHECK_QBSTATE);  	
                            EmFrameworkStatic.LunchQuickBootByFp(fingerId, mContext, userId);                            
		          }
			   /* mHandler.postDelayed(new Runnable() {  
		              @Override  
		              public void run() {
                                EmFrameworkStatic.LunchQuickBootByFp(result.getFingerprint().getFingerId(), mContext, userId);
		              }
		          }, 100);*/
                    }
                }
                //TINNO END, add by wenguangyu, for fingerprint
            }
        }

        private void sendAuthenticatedFailed() {
            if (mAuthenticationCallback != null) {
               mAuthenticationCallback.onAuthenticationFailed();
            }
        }

        private void sendAcquiredResult(long deviceId, int acquireInfo) {
            if (mAuthenticationCallback != null) {
                mAuthenticationCallback.onAuthenticationAcquired(acquireInfo);
            }
            final String msg = getAcquiredString(acquireInfo);
            if (msg == null) {
                return;
            }
            if (mEnrollmentCallback != null) {
                mEnrollmentCallback.onEnrollmentHelp(acquireInfo, msg);
            } else if (mAuthenticationCallback != null) {
                mAuthenticationCallback.onAuthenticationHelp(acquireInfo, msg);
            }
        }
    };

    /**
     * @hide
     */
    public FingerprintManager(Context context, IFingerprintService service) {
        mContext = context;
        Log.i(TAG, "FingerprintManager | context.getOpPackageName():"+context.getOpPackageName()+" service:"+service);
		
        mService = service;
        if (mService == null) {
            Slog.v(TAG, "FingerprintManagerService was null");
        }
        mHandler = new MyHandler(context);
	 mLockPatternUtils = new LockPatternUtils(context);//add by wenguangyu, for fingerprint 
    }

    private int getCurrentUserId() {
        try {
            return ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void cancelEnrollment() {
        if (mService != null) try {
            mService.cancelEnrollment(mToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void cancelAuthentication(CryptoObject cryptoObject) {
        if (mService != null) try {
            mService.cancelAuthentication(mToken, mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private String getErrorString(int errMsg) {
        switch (errMsg) {
            case FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                return mContext.getString(
                    com.android.internal.R.string.fingerprint_error_unable_to_process);
            case FINGERPRINT_ERROR_HW_UNAVAILABLE:
                return mContext.getString(
                    com.android.internal.R.string.fingerprint_error_hw_not_available);
            case FINGERPRINT_ERROR_NO_SPACE:
                return mContext.getString(
                    com.android.internal.R.string.fingerprint_error_no_space);
            case FINGERPRINT_ERROR_TIMEOUT:
                return mContext.getString(com.android.internal.R.string.fingerprint_error_timeout);
            case FINGERPRINT_ERROR_CANCELED:
                return mContext.getString(com.android.internal.R.string.fingerprint_error_canceled);
            case FINGERPRINT_ERROR_LOCKOUT:
                return mContext.getString(com.android.internal.R.string.fingerprint_error_lockout);
            default:
                if (errMsg >= FINGERPRINT_ERROR_VENDOR_BASE) {
                    int msgNumber = errMsg - FINGERPRINT_ERROR_VENDOR_BASE;
                    String[] msgArray = mContext.getResources().getStringArray(
                            com.android.internal.R.array.fingerprint_error_vendor);
                    if (msgNumber < msgArray.length) {
                        return msgArray[msgNumber];
                    }
                }
                return null;
        }
    }

    private String getAcquiredString(int acquireInfo) {
        Log.d("TinnoFingerprint", "getAcquiredString --> acquireInfo " + acquireInfo);        
        switch (acquireInfo) {
            case FINGERPRINT_ACQUIRED_GOOD:
                return null;
            case FINGERPRINT_ACQUIRED_PARTIAL:
                return mContext.getString(
                    com.android.internal.R.string.fingerprint_acquired_partial);
            case FINGERPRINT_ACQUIRED_INSUFFICIENT:
                return mContext.getString(
                    com.android.internal.R.string.fingerprint_acquired_insufficient);
            case FINGERPRINT_ACQUIRED_IMAGER_DIRTY:
                return mContext.getString(
                    com.android.internal.R.string.fingerprint_acquired_imager_dirty);
            case FINGERPRINT_ACQUIRED_TOO_SLOW:
                return mContext.getString(
                    com.android.internal.R.string.fingerprint_acquired_too_slow);
            case FINGERPRINT_ACQUIRED_TOO_FAST:
                return mContext.getString(
                    com.android.internal.R.string.fingerprint_acquired_too_fast);
	    //TinnoFingerprint[xiaowen], start
	     case EmFrameworkStatic.FINGERPRINT_ACQUIRED_ENROLL_DUPLICATE:
		return "This fingerprint is already registered. Please try another one.";//mContext.getString(
		    //com.android.internal.R.string.fingerprint_acquired_enroll_duplicate);
	     case EmFrameworkStatic.FINGERPRINT_ACQUIRED_ENROLL_TOO_NEARBY:
		return "Adjust your finger position to register the edges of your fingerprint";//mContext.getString(
		    //com.android.internal.R.string.fingerprint_acquired_enroll_too_nearby);                
	    //TinnoFingerprint[xiaowen], end
                
            default:
                if (acquireInfo >= FINGERPRINT_ACQUIRED_VENDOR_BASE) {
                    int msgNumber = acquireInfo - FINGERPRINT_ACQUIRED_VENDOR_BASE;
                    String[] msgArray = mContext.getResources().getStringArray(
                            com.android.internal.R.array.fingerprint_acquired_vendor);
                    if (msgNumber < msgArray.length) {
                        return msgArray[msgNumber];
                    }
                }
                return null;
        }
    }

    private IFingerprintServiceReceiver mServiceReceiver = new IFingerprintServiceReceiver.Stub() {

        @Override // binder call
        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
            mHandler.obtainMessage(MSG_ENROLL_RESULT, remaining, 0,
                    new Fingerprint(null, groupId, fingerId, deviceId)).sendToTarget();
        }

        @Override // binder call
        public void onAcquired(long deviceId, int acquireInfo) {
            mHandler.obtainMessage(MSG_ACQUIRED, acquireInfo, 0, deviceId).sendToTarget();
        }

        @Override // binder call
        public void onAuthenticationSucceeded(long deviceId, Fingerprint fp, int userId) {
            mHandler.obtainMessage(MSG_AUTHENTICATION_SUCCEEDED, userId, 0, fp).sendToTarget();
        }

        @Override // binder call
        public void onAuthenticationFailed(long deviceId) {
            mHandler.obtainMessage(MSG_AUTHENTICATION_FAILED).sendToTarget();;
        }

        @Override // binder call
        public void onError(long deviceId, int error) {
            mHandler.obtainMessage(MSG_ERROR, error, 0, deviceId).sendToTarget();
        }

        @Override // binder call
        public void onRemoved(long deviceId, int fingerId, int groupId) {
            mHandler.obtainMessage(MSG_REMOVED, fingerId, groupId, deviceId).sendToTarget();
        }
		
    // TINNO BEGIN
    // add for fingerprint support.
       @Override // binder call
       public boolean onCallback(int e) {
            if (mAppClient != null) {
                mAppClient.onFingerprintEvent(e);
            }
            return false;
        }

       @Override // binder call
           public boolean onCb(int arg0, String reason) {
               if (mFpCallback != null) {
                   mFpCallback.onCb(arg0, reason);
                   return true;
               }
               Log.e(TAG, "onCb failed:mFpCallback == null");
               return false;
           }
       // TINNO END.
    };


    // TINNO BEGIN
    // add for fingerprint support.
    public void notifyFpService(int msgId, Bundle extra)  {
        if (mService != null) {
            try {
                mService.notifyFpService(msgId, extra);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "notifyFpService(): Service not connected!");
        }
    }

    private AppClientCallback mAppClient;	
    public static abstract class AppClientCallback {
        public boolean onFingerprintEvent(int e) { return false; }
    };

    private FingerprintCallback mFpCallback = null;
    public static abstract class FingerprintCallback {
        public boolean onCb(int arg0, String reason) { return false; };
    }

    public boolean addFingerprintCb(FingerprintCallback cb, String reason) {
        if (mService != null) {
            try {
                mFpCallback = cb;
                return mService.addFingerprintCb(mServiceReceiver, reason);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "addFingerprintCb: Service not connected!");
        }
        return false;
    }

    /**
     * @param type  include FINGERPRINT_NAVIGATION_SWITCH_PAGE_TYPE,
     *  FINGERPRINT_NAVIGATION_DISPLAY_NOTIFICATION_TYPE,
     *  FINGERPRINT_NAVIGATION_STOP_ALARM_TYPE,
     *  FINGERPRINT_NAVIGATION_ANSWER_CALL_TYPE,
     *  FINGERPRINT_NAVIGATION_CONTINOUS_CAPTURE_TYPE
     */	
    public void registerFpService(String packageName, AppClientCallback cb, int event , int type)  {
        if (mService != null && cb != null ) {
            try {
                mAppClient = cb;
                mService.registerFpService(packageName, mServiceReceiver, event , type);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "registerFpService(): Service not connected!");
        }
    }

    public void unregisterFpService(String packageName)  {
        if (mService != null) {
            try {
                if (mAppClient != null) {
                    mAppClient = null;
                }
                mService.unregisterFpService(packageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "registerFpService(): Service not connected!");
        }
    }
    // TINNO END.

    //TINNO BEGIN, added by wenguangyu, for fingerprint
    @RequiresPermission(MANAGE_FINGERPRINT)
    public void updateQBPackageInfo(int fpId, int userId, 
        String newAppName, String newPackageName, String newClassName, String newCategory, String newAction, String newData) {
        // updateQBPackageInfo the given fpId
        if (mService != null) {
            try {
                mService.updateQBPackageInfo(fpId, userId, newAppName, newPackageName, newClassName, newCategory, newAction, newData);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "updateQBPackageInfo(): Service not connected!");
        }
    }
	
    @RequiresPermission(MANAGE_FINGERPRINT)
    public void updateQBContactsInfo(int fpId, int userId, 
        String newPhoneName, String newPhoneNumber, int newContactPhotoId, int newContactId) {
        // updateQBContactsInfo the given fpId
        if (mService != null) {
            try {
                mService.updateQBContactsInfo(fpId, userId, newPhoneName, newPhoneNumber, newContactPhotoId, newContactId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "updateQBContactsInfo(): Service not connected!");
        }
    }

    @RequiresPermission(MANAGE_FINGERPRINT)
    public void updatePrivLockValue(int fpId, int userId, int newAllowUnlockApp) {
        // updatePrivLockValue the given fpId
        if (mService != null) {
            try {
                mService.updatePrivLockValue(fpId, userId, newAllowUnlockApp);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "updatePrivLockValue(): Service not connected!");
        }
    }	
	
    @RequiresPermission(USE_FINGERPRINT)
    public ApeFpData getApeFpDataItem(int userId, int fingerId) {
        if (mService != null) try {	  	
	         Bundle b = mService.getApeFpDataItem(userId, mContext.getOpPackageName(), fingerId);
	         if (b == null) {
                    Log.e(TAG, "getApeFpDataItem: the data is NULL!!! ");
                    return null;
	         }
			 
                int apeFingerId = b.getInt("ape_FingerId");
		  String appName = b.getString("ape_QBAppName");
		  String packageName = b.getString("ape_QBPackageName");
		  String className = b.getString("ape_QBClassName");
		  String category = b.getString("ape_QBCategory");
		  String action = b.getString("ape_QBAction");
		  String data = b.getString("ape_QBData");

		  String phoneName = b.getString("ape_QBPhoneName");
		  String phoneNumber = b.getString("ape_QBPhoneNumber");
		  int contactPhotoId = b.getInt("ape_QBContactPhotoId");
		  int contactId = b.getInt("ape_QBContactId");

		  int allowUnlockApp = b.getInt("ape_AllowUnlockApp");

			 
			 
                Log.d(TAG, "getApeFpDataItem: apeFingerId = " + apeFingerId +
					"| appName = " + appName + 
					"| packageName = " + packageName + 
					"| className = " + className +
		                     "| category = " + category + 
		                     "| action = " + action +
		                     "| data = " + data +           
		                     "| phoneName = " + phoneName +            
		                     "| phoneNumber = " + phoneNumber + 
		                     "| contactPhotoId = " + contactPhotoId + 
		                     "| contactId = " + contactId +                      
		                     "| allowUnlockApp = " + allowUnlockApp);
				
		  return new ApeFpData(apeFingerId, 
		  	appName, packageName, className, category, action, data, 
		  	phoneName, phoneNumber, contactPhotoId, contactId, 
		  	allowUnlockApp);		  
		  
        } catch (RemoteException e) {
        
            throw e.rethrowFromSystemServer();

        }
        return null;	
    }


    public String changePatternPassword(int userId, String patternPassword) {
        // changePatternPassword the given fpId
        if (mService != null) {
            try {
                return mService.changePatternPassword(userId, patternPassword);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "changePatternPassword(): Service not connected!");
        }
	 return null;		
    }	

    public String changeNumberPassword(int userId, String numberPassword) {
        // changeNumberPassword the given fpId
        if (mService != null) {
            try {
                return mService.changeNumberPassword(userId, numberPassword);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "changeNumberPassword(): Service not connected!");
        }
	 return null;			
    }	

    public String changePasswordQuestion(int userId, int question, String answer) {
        // changePasswordQuestion the given fpId
        if (mService != null) {
            try {
                return mService.changePasswordQuestion(userId, question, answer);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "changePasswordQuestion(): Service not connected!");
        }
	 return null;			
    }	
	
    public String setNumberPassword(int userId, String numberPassword, int question, String answer) {
        // setNumberPassword the given fpId
        if (mService != null) {
            try {
                return mService.setNumberPassword(userId, numberPassword, question, answer);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "setNumberPassword(): Service not connected!");
        }
	 return null;		
    }

    public String setPatternPassword(int userId, String patternPassword, int question, String answer) {
        // setPatternPassword the given fpId
        if (mService != null) {
            try {
                return mService.setPatternPassword(userId, patternPassword, question, answer);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "setPatternPassword(): Service not connected!");
        }
	 return null;	
    }

    public FlPassword getFlPasswordData(int userId) {		
        if (mService != null) try {	  	
	         Bundle b = mService.getFlPasswordData(userId);
	         if (b == null) {
                    Log.e(TAG, "getFlPasswordData: the data is NULL!!! ");
                    return null;
	         }		 
		  int passwordId = b.getInt("passwordId");
	         String code = b.getString("code");
	         String token = b.getString("token");
		  int passwordLength = b.getInt("passwordLength");
	         String question = b.getString("question");
	         String answer = b.getString("answer");
	         String patternPassword = b.getString("patternPassword");
	         int currentPasswordType = b.getInt("currentPasswordType");
			 
                Log.d(TAG, "getFlPasswordData: passwordId = " + passwordId +
					"| code = " + code + 
					"| token = " + token + 
					"| passwordLength = " + passwordLength +
		                     "| question = " + question + 
		                     "| answer = " + answer +
		                     "| patternPassword = " + patternPassword + 
		                     "| currentPasswordType = " + currentPasswordType);		 
		  return new FlPassword(passwordId, code, token, passwordLength, question, answer, patternPassword, currentPasswordType);	
		  
        } catch (RemoteException e) {
        
            throw e.rethrowFromSystemServer();

        }
        return null;
    }

    public Fingerprint getFingerprintById(int userId, int fingerId) {
        List<Fingerprint> fpList;	   	
        if (mService != null) try {
            fpList = mService.getEnrolledFingerprints(userId, mContext.getOpPackageName());
	     if (fpList != null && fpList.size()>0) {
		  for (int i=0; i<fpList.size(); i++) {
		      Fingerprint fpItem = fpList.get(i); 	
                    if(fingerId == fpItem.getFingerId()) {
                        return fpItem;
		      }                
		  }
	     }		
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }		
        return null;  

	}


    public List<ApeFpData> getApeFpDataList(int userId) {
        List<Fingerprint> fpList;
        List<ApeFpData> apeFpDataList = new ArrayList<ApeFpData>();		
        if (mService != null) try {
            fpList = mService.getEnrolledFingerprints(userId, mContext.getOpPackageName());
	     if (fpList != null && fpList.size()>0) {
		  for (int i=0; i<fpList.size(); i++) {	  	
		      Fingerprint fpItem = fpList.get(i);
		      int fingerId = fpItem.getFingerId();	  
		      ApeFpData apeFpData= FingerprintManager.this.getApeFpDataItem(userId, fingerId);
		      apeFpDataList.add(apeFpData); 
		  }
		  return apeFpDataList;
	     }		
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }		
        return null;      
    }		
//TINNO END, added by wenguangyu, for fingerprint 	


// guomingyi add for fp quick wakeup support.
public static final int FP_DEFAULT_VERSION = 0;
public static final int FP_QWK_VERSION_1 = 1;
public static final int FP_QWK_VERSION_2 = 2;
public static final boolean DEBUG_FP = false;
private static int fp_quick_wake_version = SystemProperties.getInt("persist.tinno.fp.qwk", FP_DEFAULT_VERSION);

public static final int FP_STATE_IDLE = 0;
public static final int FP_SCR_ON_UNLOCK = 1;
public static final int FP_SCR_OFF_UNLOCK = 2;
public static final int FP_TRIGGER_IRQ = 3;

public static final int FLAG_FP_STATE_IDLE = -3000;
public static final int FLAG_FP_SCR_ON_UNLOCK = -3001;
public static final int FLAG_FP_SCR_OFF_UNLOCK = -3002;
public static final int FLAG_POWER_KEY_PRESS = -3003;
public static final int FLAG_SCREEN_ON = -3004;
public static final int FLAG_FP_TRIGGER_IRQ = -3005;
public static final int FLAG_FP_LOCKOUT = -3006;
public static final int FLAG_FP_MAX = -3010; //max .

public static final int CMD_BACKLIGHT_ON = 1;
public static final int CMD_STATE_RESET = 2;

public static final int FLAG_START_WAKEUP = 10;
public static final int FLAG_FINISH_WAKEUP = 11;
public static final int FLAG_GOING_TO_SLEEP = 12;
public static final int FLAG_FINISH_TO_SLEEP = 13;
public static final int FLAG_START_DO_LOCKED = 14;
public static final int FLAG_FINISH_DO_LOCKED = 15;

public static boolean isFpQuickWakeUpSupport(int flag) {
    if (fp_quick_wake_version == FP_QWK_VERSION_1 && flag == FP_QWK_VERSION_1) {
        if (DEBUG_FP) Log.i(TAG, "isFpQuickWakeUpSupport: version:1");
        return true;
    }
    if (fp_quick_wake_version == FP_QWK_VERSION_2 && (flag == FP_QWK_VERSION_1 || flag == FP_QWK_VERSION_2)) {
        if (DEBUG_FP) Log.i(TAG, "isFpQuickWakeUpSupport: version:2");
        return true;
    }

    return false;
}

public int getCurrFingerprintState()  {
    if (mService != null) {
        try {
            return mService.getCurrFingerprintState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    } else {
        Log.w(TAG, "FingerprintService not connected!");
    }
    return FP_STATE_IDLE;
}

public boolean setWorkingFlag(int flag)  {
    if (mService != null) {
        try {
            return mService.setWorkingFlag(flag);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    } else {
        Log.w(TAG, "FingerprintService not connected!");
    }
    return false;
}

public void sendCmdToFingerprintService(int cmd, String reason)  {
    if (mService != null) {
        try {
            mService.sendCmdToFingerprintService(cmd, reason);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    } else {
        Log.w(TAG, "FingerprintService not connected!");
    }
}

public boolean isFingerprintQuickWakeup(int arg0, String reason)  {
    if (mService != null) {
        try {
            return mService.isFingerprintQuickWakeup(arg0, reason);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    } else {
        Log.w(TAG, "FingerprintService not connected!");
    }
    return false;
}

// guomingyi add for fp quick wakeup support.

}

