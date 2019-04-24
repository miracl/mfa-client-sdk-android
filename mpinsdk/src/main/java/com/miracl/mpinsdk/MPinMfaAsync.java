/* **************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ***************************************************************/
package com.miracl.mpinsdk;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.miracl.mpinsdk.model.Expiration;
import com.miracl.mpinsdk.model.OTP;
import com.miracl.mpinsdk.model.RegCode;
import com.miracl.mpinsdk.model.ServiceDetails;
import com.miracl.mpinsdk.model.SessionDetails;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.Signature;
import com.miracl.mpinsdk.model.User;
import com.miracl.mpinsdk.util.Hex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Wrapper of {@link MPinMFA} that executes operations in a background thread.
 */
public class MPinMfaAsync {

    /**
     * Client parameter for whether a backend requires a device name to be specified when registering users.
     */
    public static final String CLIENT_PARAM_DEVICE_NAME = "setDeviceName";

    private static final String DEFAULT_SHARED_PREFS = "mpin-shared-prefs";

    private MPinMFA       mMfaSdk;
    private Handler       mWorkerHandler;
    private HandlerThread mWorkerThread;
    private MfaInfoCache  mMfaInfoCache;

    static {
        System.loadLibrary("AndroidMpinSDK");
    }


    /**
     * Callback for SDK methods
     *
     * @param <T>
     *   The type of the result
     */
    public static abstract class Callback<T> {

        /**
         * Called when a SDK operation completes
         *
         * @param status
         *   The resulting {@link Status} of the operation
         * @param result
         *   The optional result of the operation
         */
        protected void onResult(@NonNull Status status, @Nullable T result) {
            if (status.getStatusCode() == Status.Code.OK) {
                onSuccess(result);
            } else {
                onFail(status);
            }
        }


        /**
         * Utility method for when a operation returns with {@link Status.Code#OK}.
         *
         * @param result
         *   The optional result
         * @see com.miracl.mpinsdk.MPinMfaAsync.Callback#onResult(Status, Object)
         */
        protected void onSuccess(@Nullable T result) {

        }


        /**
         * Utility method for when a operation returns with code different than {@link Status.Code#OK}.
         *
         * @param status
         *   The error status
         * @see com.miracl.mpinsdk.MPinMfaAsync.Callback#onResult(Status, Object)
         */
        protected void onFail(@NonNull Status status) {

        }
    }


    /**
     * Create a new not initialized instance. Use of the init methods in order for the SDK methods to be usable.
     *
     * @see #init(Context, Callback)
     * @see #init(Context, String, String[], Callback)
     */
    public MPinMfaAsync(@NonNull Context context) {
        mMfaSdk = new MPinMFA();
        mMfaInfoCache = new MfaInfoCache(
          context.getApplicationContext().getSharedPreferences(DEFAULT_SHARED_PREFS, Context.MODE_PRIVATE));
        initWorkerThread();
    }


    /**
     * Initialize the {@link MPinMFA}.
     *
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     */
    public void init(@NonNull Context context, @Nullable final Callback<Void> callback) {
        init(context, null, null, callback);
    }

    /**
     * Initialize the {@link MPinMFA} with the provided cid and trusted domains.
     *
     * @param cid
     *   The customer ID associated with the mobile application. Can be <code>null</code> and can be set or changed later with
     *   {@link #setCid(String, Callback)}.
     * @param trustedDomains
     *   An array of domains (without specifying scheme), that the SDK can communicate with (this includes the acceptable
     *   backends that can be set). For example <code>{"mpin.io"}</code>. Can be <code>null</code> and later the SDK will accept
     *   any domain. Additional domains can later be added with {@link #addTrustedDomains(String[], Callback)}.
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     */
    public void init(@NonNull Context context, @Nullable String cid, @Nullable String[] trustedDomains,
                     @Nullable final Callback<Void> callback) {

        Status status = mMfaSdk.init(null, context);
        if (status.getStatusCode() == Status.Code.OK) {
            if (trustedDomains != null) {
                for (String domain : trustedDomains) {
                    mMfaSdk.addTrustedDomain(domain);
                }
            }

            if (cid != null) {
                mMfaSdk.setCid(cid);
            }
        }

        if (callback != null) {
            callback.onResult(status, null);
        }
    }

    /**
     * Set the customer ID.
     *
     * @param cid
     *   The customer ID associated with the mobile application.
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     */
    public void setCid(@NonNull final String cid, @Nullable final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                mMfaSdk.setCid(cid);

                if (callback != null) {
                    callback.onSuccess(null);
                }
            }
        });
    }

    /**
     * Add an array of domains (without specifying scheme), that the SDK can communicate with.
     * These will be the domains that can later be set as backends and have users associated with them.
     * For example <code>{"mpin.io"}</code>.
     *
     * @param domains
     *   The array of domains that will be added as trusted.
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     * @see #setBackend(ServiceDetails, Callback)
     */
    public void addTrustedDomains(@NonNull final String[] domains, @Nullable final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                for (String domain : domains) {
                    mMfaSdk.addTrustedDomain(domain);
                }
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }
        });
    }

    /**
     * Add key-value pairs as map that will be added as headers for every request the SDK makes.
     *
     * @param customHeaders
     *   A map of header name and value, that will be added
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     */
    public void addCustomHeaders(@NonNull final Map<String, String> customHeaders, @Nullable final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                mMfaSdk.addCustomHeaders(customHeaders);
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }
        });
    }

    /**
     * Check whether a user is existing in the SDK.
     *
     * @param id
     *   The user ID that will be checked for existence.
     * @param callback
     *   Callback containing the result of the check.
     */
    public void isUserExisting(@NonNull final String id, @NonNull final Callback<Boolean> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                callback.onSuccess(mMfaSdk.isUserExisting(id));
            }
        });
    }

    /**
     * Check whether a user with a specific customer ID is existing in the SDK.
     *
     * @param id
     *   The user ID that will be checked for existence.
     * @param customerId
     *   The customer ID associated with the user ID
     * @param callback
     *   Callback containing the result of the check.
     */
    public void isUserExisting(@NonNull final String id, @NonNull final String customerId,
                               @NonNull final Callback<Boolean> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                callback.onSuccess(mMfaSdk.isUserExisting(id, customerId));
            }
        });
    }

    /**
     * Check whether a user with a specific customer ID and app ID is existing in the SDK.
     *
     * @param id
     *   The user ID that will be checked for existence.
     * @param customerId
     *   The customer ID associated with the user ID.
     * @param appId
     *   The app ID associated with the user ID.
     * @param callback
     *   Callback containing the result of the check.
     */
    public void isUserExisting(@NonNull final String id, @NonNull final String customerId, @NonNull final String appId,
                               @NonNull final Callback<Boolean> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                callback.onSuccess(mMfaSdk.isUserExisting(id, customerId, appId));
            }
        });
    }

    // /**
    //  * Set the backend for the sdk from the specified user
    //  *
    //  * @param user
    //  *   The user for which to set the backend
    //  * @param callback
    //  *   Callback for the operation. Can be <code>null</code>.
    //  */
    // public void setBackend(@NonNull final User user, @Nullable final Callback<Void> callback) {
    //     mWorkerHandler.post(new Runnable() {

    //         @Override
    //         public void run() {

    //             Status status = mMfaSdk.setBackend("https://" + user.getBackend());
    //             if (callback != null) {
    //                 callback.onResult(status, null);
    //             }
    //         }
    //     });
    // }


    /**
     * Set the backend for the SDK from a service details.
     *
     * @param serviceDetails
     *   The service details describing the backend
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     */
    public void setBackend(@NonNull final ServiceDetails serviceDetails, @Nullable final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.setBackend(serviceDetails.backendUrl);

                if (callback != null) {
                    callback.onResult(status, null);
                }
            }
        });
    }


    /**
     * Create a new user for the current backend. The user needs to be registered before it can be used for authentication.
     *
     * @param userId
     *   The id of the user
     * @param deviceName
     *   Optional device name
     * @param callback
     *   The callback for the newly created user. Can
     *   be <code>null</code> and the operation will still be executed.
     * @see #startRegistration(String, User, Callback)
     */
    public void makeNewUser(final @NonNull String userId, final @Nullable String deviceName,
                            @Nullable final Callback<User> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                User user;
                if (deviceName != null) {
                    user = mMfaSdk.makeNewUser(userId, deviceName);
                } else {
                    user = mMfaSdk.makeNewUser(userId);
                }
                if (callback != null) {
                    callback.onResult(new Status(Status.Code.OK, null), user);
                }
            }
        });
    }


    /**
     * Delete a user from the SDK. After this operation the provided {@link User} is no longer valid.
     *
     * @param user
     *   The user to remove
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     */
    public void deleteUser(@NonNull final User user, @Nullable final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                mMfaInfoCache.removeFromLastLoggedInUsers(user);
                mMfaInfoCache.removeFromLastOtpUser(user);
                mMfaInfoCache.removeExpiration(user);
                mMfaSdk.deleteUser(user);
                if (callback != null) {
                    callback.onResult(new Status(Status.Code.OK, null), null);
                }
            }
        });
    }

    /**
     * Fetch an access code without scanning QR code
     *
     * @param authUrl
     *   A valid platform authorize URL
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     */
    public void getAccessCode(@NonNull final String authUrl, @Nullable final Callback<String> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                StringBuilder code = new StringBuilder();
                Status status = mMfaSdk.getAccessCode(authUrl, code);

                if (callback != null) {
                    callback.onResult(status, code.toString());
                }
            }
        });
    }

    /**
     * Start the registration process for a previously created {@link User}. For the registration process to be completed a
     * user's registration needs to be confirmed and then finished.
     *
     * @param accessCode
     *   A valid access code
     * @param user
     *   A {@link User} object in non-registered state
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     * @see #confirmRegistration(User, Callback)
     * @see #finishRegistration(User, String[], Callback)
     */
    public void startRegistration(@NonNull final String accessCode, final @NonNull User user,
                                  @Nullable final Callback<Void> callback) {
        startRegistration(accessCode, user, null, null, callback);
    }

    /**
     * Start the registration process for a previously created {@link User}. For the registration process to be completed a
     * user's registration needs to be confirmed and then finished.
     *
     * @param accessCode
     *   A valid access code
     * @param user
     *   A {@link User} object in non-registered state
     * @param pushToken
     *   The unique token for sending push notifications on the device
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     * @see #confirmRegistration(User, Callback)
     * @see #finishRegistration(User, String[], Callback)
     */
    public void startRegistration(@NonNull final String accessCode, final @NonNull User user,
                                  @Nullable final String pushToken, @Nullable final Callback<Void> callback) {
        startRegistration(accessCode, user, pushToken, null, callback);
    }

    /**
     * Start the registration process for a previously created {@link User}. For the registration process to be completed a
     * user's registration needs to be confirmed and then finished.
     *
     * @param accessCode
     *   A valid access code
     * @param user
     *   A {@link User} object in non-registered state
     * @param pushToken
     *   The unique token for sending push notifications on the device
     * @param regCode
     *   A valid registration code used for identity verification
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     * @see #confirmRegistration(User, Callback)
     * @see #finishRegistration(User, String[], Callback)
     */
    public void startRegistration(@NonNull final String accessCode, final @NonNull User user,
                                  @Nullable final String pushToken, @Nullable final String regCode,
                                  @Nullable final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                String pToken = pushToken;
                if (pToken == null) {
                    pToken = "";
                }

                final Status status;
                if (regCode != null) {
                    status = mMfaSdk.startRegistration(user, accessCode, pToken, regCode);
                } else {
                    status = mMfaSdk.startRegistration(user, accessCode, pToken);
                }

                if (status.getStatusCode() == Status.Code.OK) {
                    mMfaInfoCache.putExpiration(user);
                }
                if (callback != null) {
                    callback.onResult(status, null);
                }
            }
        });
    }

    /**
     * Start the registration process for a new user, that will be created with the specified user ID and device name. For the
     * registration process to be completed a user's registration needs to be confirmed and then finished.
     *
     * @param accessCode
     *   A valid access code
     * @param userId
     *   The user ID for the user that will be created
     * @param deviceName
     *   Optional device name for the user that will be created
     * @param callback
     *   Callback with the newly created user
     * @see #confirmRegistration(User, Callback)
     * @see #finishRegistration(User, String[], Callback)
     */
    public void startRegistration(@NonNull final String accessCode, @NonNull final String userId,
                                  @Nullable final String deviceName, @NonNull final Callback<User> callback) {
        startRegistration(accessCode, userId, deviceName, null, null, callback);
    }

    /**
     * Start the registration process for a new user, that will be created with the specified user ID and device name. For the
     * registration process to be completed a user's registration needs to be confirmed and then finished.
     *
     * @param accessCode
     *   A valid access code
     * @param userId
     *   The user ID for the user that will be created
     * @param deviceName
     *   Optional device name for the user that will be created
     * @param pushToken
     *   The unique token for sending push notifications on the device
     * @param callback
     *   Callback with the newly created user
     * @see #confirmRegistration(User, Callback)
     * @see #finishRegistration(User, String[], Callback)
     */
    public void startRegistration(@NonNull final String accessCode, @NonNull final String userId,
                                  @Nullable final String deviceName, @Nullable final String pushToken,
                                  @NonNull final Callback<User> callback) {
        startRegistration(accessCode, userId, deviceName, pushToken, null, callback);
    }

    /**
     * Start the registration process for a new user, that will be created with the specified user ID and device name. For the
     * registration process to be completed a user's registration needs to be confirmed and then finished.
     *
     * @param accessCode
     *   A valid access code
     * @param userId
     *   The user ID for the user that will be created
     * @param deviceName
     *   Optional device name for the user that will be created
     * @param pushToken
     *   The unique token for sending push notifications on the device
     * @param regCode
     *   A valid registration code used for identity verification
     * @param callback
     *   Callback with the newly created user
     * @see #confirmRegistration(User, Callback)
     * @see #finishRegistration(User, String[], Callback)
     */
    public void startRegistration(@NonNull final String accessCode, @NonNull final String userId,
                                  @Nullable final String deviceName, @Nullable final String pushToken,
                                  @Nullable final String regCode, @NonNull final Callback<User> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                final User user;

                if (!TextUtils.isEmpty(deviceName)) {
                    user = mMfaSdk.makeNewUser(userId, deviceName);
                } else {
                    user = mMfaSdk.makeNewUser(userId);
                }

                startRegistration(accessCode, user, pushToken, regCode, new Callback<Void>() {

                    @Override
                    protected void onResult(Status status, Void result) {
                        callback.onResult(status, user);
                    }
                });
            }
        });
    }

    /**
     * Restart a previously started registration for a user. This will result in the user being in started registration state.
     * Any verification steps (such as sending a verification email) will be executed again.
     *
     * @param user
     *   The user to restart the registration for
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     * @see #startRegistration(String, String, String, Callback)
     * @see #startRegistration(String, User, Callback)
     * @see #confirmRegistration(User, Callback)
     * @see #finishRegistration(User, String[], Callback)
     */
    public void restartRegistration(@NonNull final User user, final @Nullable Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.restartRegistration(user);
                Status.Code confirmStatusCode = status.getStatusCode();
                if (confirmStatusCode == Status.Code.REGISTRATION_EXPIRED) {
                    mMfaInfoCache.invalidateExpiration(user, false);
                }

                if (callback != null) {
                    callback.onResult(status, null);
                }
            }
        });
    }

    /**
     * Verify that a user has confirmed the previously started registration. This method should be called after
     * startRegistration/restartRegistration and before finishRegistration.
     *
     * @param user
     *   The user for which a registration process is started
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     * @see #startRegistration(String, String, String, Callback)
     * @see #startRegistration(String, User, Callback)
     * @see #restartRegistration(User, Callback)
     * @see #finishRegistration(User, String[], Callback)
     */
    public void confirmRegistration(@NonNull final User user, final @Nullable Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.confirmRegistration(user);
                Status.Code confirmStatusCode = status.getStatusCode();
                if (confirmStatusCode == Status.Code.REGISTRATION_EXPIRED || confirmStatusCode == Status.Code.OK) {
                    // if the confirmation of the mail is successful we should remove expiration data, because the regOTT
                    // for this identity is no longer valid
                    mMfaInfoCache.invalidateExpiration(user, false);
                }

                if (callback != null) {
                    callback.onResult(status, null);
                }
            }
        });
    }

    /**
     * Finish the registration process for a user with confirmed registration. The provided array of strings will be used to
     * authenticate the user and cannot be changed later.
     *
     * @param user
     *   The user with confirmed registration
     * @param factors
     *   An array of strings that form the user's authentication. It will be associated with the user and cannot be changed.
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     * @see #startRegistration(String, String, String, Callback)
     * @see #startRegistration(String, User, Callback)
     * @see #restartRegistration(User, Callback)
     * @see #confirmRegistration(User, Callback)
     */
    public void finishRegistration(@NonNull final User user, @NonNull final String[] factors,
                                   @Nullable final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.finishRegistration(user, factors);
                if (status.getStatusCode() == Status.Code.OK) {
                    mMfaInfoCache.removeExpiration(user);
                }
                if (callback != null) {
                    callback.onResult(status, null);
                }
            }
        });
    }

    /**
     * Start a new registration with an existing user. The existing user will be deleted and a registration for a new one with
     * the same id will be started and the backend for the existing user will be set as current one for the SDK. Can be used to
     * to make a new registration for a previously blocked user or to change a user's factors.
     *
     * @param accessCode
     *   The access code for the registration
     * @param user
     *   The user to delete
     * @param callback
     *   Callback with the newly created user or the old one if changing the backend failed
     * @see #confirmRegistration(User, Callback)
     * @see #finishRegistration(User, String[], Callback)
     */
    public void startNewRegistration(@NonNull final String accessCode, @NonNull final User user,
                                     @Nullable final String deviceName, @Nullable final Callback<User> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                // The backend url stored in the user does not have a scheme
                Status setBackendStatus = mMfaSdk.setBackend("https://" + user.getBackend());
                if (setBackendStatus != null && setBackendStatus.getStatusCode() == Status.Code.OK) {
                    mMfaInfoCache.removeFromLastLoggedInUsers(user);

                    mMfaSdk.deleteUser(user);
                    //We assume user is not existing, as it was just deleted.
                    User newUser = mMfaSdk.makeNewUser(user.getId(), deviceName);
                    Status startRegistrationStatus = mMfaSdk.startRegistration(newUser, accessCode);

                    if (startRegistrationStatus.getStatusCode() == Status.Code.OK) {
                        mMfaInfoCache.putExpiration(newUser);
                    }

                    if (callback != null) {
                        callback.onResult(startRegistrationStatus, newUser);
                    }
                } else {
                    if (callback != null) {
                        callback.onResult(setBackendStatus, user);
                    }
                }
            }
        });
    }


    /**
     * Start the registration process for DVS capabilities of a {@link User user}. The user should already be in
     * {@link User.State#REGISTERED registered state} and valid dvs registering token should be provided. Check if a user is
     * already registered for DVS with {@link User#canSign()}.
     *
     * @param user
     *   The User object in registered state
     * @param multiFactor
     *   The DVS registration token
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     * @see #finishRegistrationDvs(User, String[], Callback)
     * @see User#canSign()
     */
    public void startRegistrationDvs(@NonNull final User user, @NonNull final String[] multiFactor,
                                     @Nullable final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.startRegistrationDvs(user, multiFactor);
                if (callback != null) {
                    callback.onResult(status, null);
                }
            }
        });
    }

    /**
     * Finish the registration process for DVS capabilities of a {@link User user}. The user should already be in
     * {@link User.State#REGISTERED registered state} and {@link #startRegistrationDvs(User, String[], Callback)} should be
     * previously called for it. Check if a user is already registered for DVS with {@link User#canSign()}.
     *
     * @param user
     *   The User object in registered state
     * @param multiFactor
     *   The array of string factors for the User's signing
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     * @see #startRegistrationDvs(User, String[], Callback)
     * @see User#canSign()
     */
    public void finishRegistrationDvs(@NonNull final User user, @NonNull final String[] multiFactor,
                                      @Nullable final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.finishRegistrationDvs(user, multiFactor);
                if (callback != null) {
                    callback.onResult(status, null);
                }
            }
        });
    }

    /**
     * Sign a document using an identity that has DVS capabilities.
     * Check if a user is already registered for DVS with {@link User#canSign()}.
     *
     * @param user
     *   The User object in registered state
     * @param documentHash
     *   The hash of the document for signing
     * @param multiFactor
     *   The array of string factors for the User's signing
     * @param timestamp
     *   Timestamp of when the document was created
     * @param callback
     *   The callback for the operation
     * @see User#canSign()
     */
    public void sign(@NonNull final User user, @NonNull final byte[] documentHash, @NonNull final String[] multiFactor,
                     final int timestamp, @NonNull final Callback<Signature> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Signature signature = new Signature();

                try {
                    callback.onResult(mMfaSdk.sign(user, documentHash, multiFactor, timestamp, signature), signature);
                } catch (Exception e) {
                    callback.onResult(new Status(Status.Code.RESPONSE_PARSE_ERROR, e.getMessage()), signature);
                }
            }
        });
    }

    /**
     * Check if the provided document hash properly corresponds to the document
     *
     * @param document
     *   The document for signing
     * @param documentHash
     *   The hash of the document for signing
     * @param callback
     *   The callback for the operation
     */
    public void verifyDocumentHash(@NonNull final byte[] document, @NonNull final byte[] documentHash,
                                   final @NonNull Callback<Boolean> callback) {
        final String documentHashString = new String(documentHash).toLowerCase(Locale.getDefault());
        final String hashedDocument = Hex.encode(mMfaSdk.hashDocument(document));

        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onResult(new Status(Status.Code.OK, ""), documentHashString.equals(hashedDocument));
            }
        });
    }

    /**
     * Get {@link SessionDetails details} (app and customer info) for a session with access code.
     *
     * @param accessCode
     *   A valid access code
     * @param callback
     *   Callback with the retrieved session details
     * @see SessionDetails
     */
    public void getSessionDetails(@NonNull final String accessCode, @NonNull final Callback<SessionDetails> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                SessionDetails sessionDetails = new SessionDetails();
                Status status = mMfaSdk.getSessionDetails(accessCode, sessionDetails);
                if (status.getStatusCode() == Status.Code.OK) {
                    mMfaInfoCache
                      .putCustomerInfo(sessionDetails.customerId, sessionDetails.customerName, sessionDetails.customerIconUrl);
                }
                callback.onResult(status, sessionDetails);
            }
        });
    }

    /**
     * Get {@link ServiceDetails details} (name, logo, etc) for a service.
     *
     * @param serviceUrl
     *   The service's url
     * @param callback
     *   Callback with the retrieved details
     * @see ServiceDetails
     */
    public void getServiceDetails(@NonNull final String serviceUrl, @NonNull final Callback<ServiceDetails> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                ServiceDetails serviceDetails = new ServiceDetails();
                Status status = mMfaSdk.getServiceDetails(serviceUrl, serviceDetails);
                if (status.getStatusCode() == Status.Code.OK) {
                    mMfaInfoCache.putServiceDetails(serviceDetails);
                }
                callback.onResult(status, serviceDetails);
            }
        });
    }


    /**
     * Start an authentication process for a user with a valid access code.
     *
     * @param user
     *   The user which will be authenticated
     * @param accessCode
     *   A valid access code
     * @param callback
     *   Callback for the operation
     * @see #finishAuthentication(User, String[], String, Callback)
     */
    public void startAuthentication(@NonNull final User user, @NonNull final String accessCode,
                                    @NonNull final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.startAuthentication(user, accessCode);

                callback.onResult(status, null);
            }
        });
    }

    /**
     * Finish the authentication process for a user that has started authentication. Should be called after
     * {@link #startAuthentication(User, String, Callback)} with the same access code. If the same access code is not valid,
     * the authentication process should be started again with a new valid access code.
     *
     * @param user
     *   The user for which an authentication is started
     * @param factors
     *   An array of strings that form the user's authentication
     * @param accessCode
     *   A valid access code, the same one used for starting the authentication
     * @param callback
     *   Callback for the operation
     * @see #startAuthentication(User, String, Callback)
     */
    public void finishAuthentication(@NonNull final User user, @NonNull final String[] factors, @NonNull final String accessCode,
                                     @NonNull final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.finishAuthentication(user, factors, accessCode);
                if (status.getStatusCode() == Status.Code.OK) {
                    mMfaInfoCache.putLastLoggedInUser(user);
                }
                callback.onResult(status, null);
            }
        });
    }

    /**
     * Finish the authentication process for a user that has started authentication and obtain authentication code for it.
     * Should be called after {@link #startAuthentication(User, String, Callback)} with the same access code. If the same
     * access code is not valid, the authentication process should be started again with a new valid access code.
     *
     * @param user
     *   The user for which an authentication is started
     * @param factors
     *   An array of strings that form the user's authentication
     * @param accessCode
     *   A valid access code, the same one used for starting the authentication
     * @param callback
     *   Callback for the operation with the authentication code
     * @see #startAuthentication(User, String, Callback)
     */
    public void finishAuthenticationAuthCode(@NonNull final User user, @NonNull final String[] factors,
                                             @NonNull final String accessCode, @NonNull final Callback<String> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                StringBuilder authCode = new StringBuilder();
                Status status = mMfaSdk.finishAuthentication(user, factors, accessCode, authCode);
                if (status.getStatusCode() == Status.Code.OK) {
                    mMfaInfoCache.putLastLoggedInUser(user);
                }
                callback.onResult(status, authCode.toString());
            }
        });
    }

    /**
     * Start an authentication process for a user to receive a one time password.
     *
     * @param user
     *   The user which will be authenticated
     * @param callback
     *   Callback for the operation
     * @see #finishAuthenticationOtp(User, String[], Callback)
     */
    public void startAuthenticationOtp(@NonNull final User user, @NonNull final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.startAuthenticationOtp(user);

                callback.onResult(status, null);
            }
        });
    }

    /**
     * Finish the OTP authentication process for a user that has started a OTP authentication. Should be called after
     * {@link #startAuthenticationOtp(User, Callback)}. One time password is obtained once the authentication is successful.
     *
     * @param user
     *   The user for which a OTP authentication is started
     * @param factors
     *   An array of strings that form the user's authentication
     * @param callback
     *   Callback with the generated OTP
     * @see #startAuthenticationOtp(User, Callback)
     * @see OTP
     */
    public void finishAuthenticationOtp(@NonNull final User user, @NonNull final String[] factors,
                                        @NonNull final Callback<OTP> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                OTP otp = new OTP();
                Status status = mMfaSdk.finishAuthenticationOtp(user, factors, otp);
                if (status.getStatusCode() == Status.Code.OK) {
                    mMfaInfoCache.putLastOtpUser(user);
                }
                callback.onResult(status, otp);
            }
        });
    }

    /**
     * Start an authentication process for a user to receive a registration code.
     *
     * @param user
     *   The user which will be authenticated
     * @param callback
     *   Callback for the operation
     * @see #finishAuthenticationRegCode(User, String[], Callback)
     */
    public void startAuthenticationRegCode(@NonNull final User user, @NonNull final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.startAuthenticationRegCode(user);
                callback.onResult(status, null);
            }
        });
    }

    /**
     * Finish the registration code authentication process for a user that has started a regCode authentication. Should be called after
     * {@link #startAuthenticationRegCode(User, Callback)}. Registration code is obtained once the authentication is successful.
     *
     * @param user
     *   The user for which a registration code authentication is started
     * @param factors
     *   An array of strings that form the user's authentication
     * @param callback
     *   Callback with the generated registration code
     * @see #startAuthenticationRegCode(User, Callback)
     */
    public void finishAuthenticationRegCode(@NonNull final User user, @NonNull final String[] factors,
                                        @NonNull final Callback<RegCode> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                RegCode regCode = new RegCode();
                Status status = mMfaSdk.finishAuthenticationRegCode(user, factors, regCode);
                callback.onResult(status, regCode);
            }
        });
    }

    /**
     * Abort the session associated with the specified access code.
     *
     * @param accessCode
     *   The access code for the session
     * @param callback
     *   The callback for the operation. Can be <code>null</code> and the operation will still be executed.
     */
    public void abortSession(@NonNull final String accessCode, @Nullable final Callback<Void> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Status status = mMfaSdk.abortSession(accessCode);
                if (callback != null) {
                    callback.onResult(status, null);
                }
            }
        });
    }

    /**
     * Get a list of all the users.
     *
     * @param callback
     *   The callback for the operation
     */
    public void getUsers(@NonNull final Callback<List<User>> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                List<User> users = new ArrayList<>();
                Status status = mMfaSdk.listUsers(users);
                callback.onResult(status, users);
            }
        });
    }

    /**
     * Get a list of all the users for a backend.
     *
     * @param backendUrl
     *   The backend with which the users are associated with
     * @param customerId
     *   The customer id with which the users are associated with
     * @param callback
     *   The callback for the operation
     */
    public void getUsers(@NonNull final String backendUrl, @Nullable final String customerId,
                         @NonNull final Callback<List<User>> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                Uri backend = Uri.parse(backendUrl);
                if (backend != null && backend.getAuthority() != null) {
                    List<User> users = new ArrayList<>();
                    Status status = mMfaSdk.listUsers(users);
                    if (status.getStatusCode() == Status.Code.OK) {
                        Iterator<User> iterator = users.iterator();

                        while (iterator.hasNext()) {
                            User user = iterator.next();
                            if (user.getBackend() == null || !user.getBackend().equalsIgnoreCase(backend.getAuthority())) {
                                iterator.remove();
                            } else if (customerId != null && user.getCustomerId() != null) {

                                if (!user.getCustomerId().equals(customerId)) {
                                    iterator.remove();
                                }
                            }
                        }
                    }

                    callback.onResult(status, users);
                } else {
                    callback.onResult(new Status(Status.Code.FLOW_ERROR, "Invalid backend url for listing users"), null);
                }
            }
        });
    }


    /**
     * Get a client parameter for the currently set backend.
     *
     * @param clientParam
     *   The key to check the client parameter with. For example {@link #CLIENT_PARAM_DEVICE_NAME}
     * @param callback
     *   The callback with the result.
     */
    public void getClientParam(@NonNull final String clientParam, final @NonNull Callback<String> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                callback.onResult(new Status(Status.Code.OK, ""), mMfaSdk.getClientParam(clientParam));
            }
        });
    }

    /**
     * Performs a check whether a registration token is set during a user registration.
     * @param user
     * A {@link User} object in a non confirmed state
     * @param callback
     * The callback returning true or false depending on whether a token has been submitted during registration.
     */
    public void IsRegistrationTokenSet(@NonNull final User user, final @NonNull Callback<Boolean> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
            callback.onResult(new Status(Status.Code.OK, ""), mMfaSdk.isRegistrationTokenSet(user));
            }
        });
    }
    /**
     * Get storage with cached information for the SDK such as cached {@link ServiceDetails} or information about who the last
     * successfully logged-in user is.
     */
    public MfaInfoCache getCachedInfo() {
        return mMfaInfoCache;
    }


    /**
     * Obtain a reference to the {@link MPinMFA} associated with this instance. The callback's onResult is called on a
     * background thread and can be used to call SDK methods safely.
     *
     * @param callback
     *   The callback to receive the SDK.
     */
    public void doInBackground(@NonNull final Callback<MPinMFA> callback) {
        mWorkerHandler.post(new Runnable() {

            @Override
            public void run() {
                callback.onResult(new Status(Status.Code.OK, ""), mMfaSdk);
            }
        });
    }

    /**
     * Obtain a reference to the {@link MPinMFA} associated with this instance.
     */
    public MPinMFA getMfaSdk() {
        return mMfaSdk;
    }

    /**
     * Release the SDK. After this method is called all other SDK related methods will fail.
     */
    public void release() {
        mWorkerHandler.removeCallbacksAndMessages(null);
        mWorkerThread.quit();
        mMfaSdk.close();
    }


    private void initWorkerThread() {
        mWorkerThread = new HandlerThread("SDK Worker Thread");
        mWorkerThread.start();
        mWorkerHandler = new Handler(mWorkerThread.getLooper());
    }

    private boolean isValidExpiration(Expiration expiration) {
        return expiration != null && expiration.expireTimeSeconds != 0 && expiration.nowTimeSeconds != 0;
    }
}