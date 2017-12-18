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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.miracl.mpinsdk.model.Expiration;
import com.miracl.mpinsdk.model.ServiceDetails;
import com.miracl.mpinsdk.model.User;

import java.util.HashMap;
import java.util.Locale;

class MfaInfoCache {

    private static final String KEY_SERVICE_INFO       = "service-info";
    private static final String KEY_CUSTOMER_INFO      = "customer-info";
    private static final String KEY_LAST_LOGGED        = "last-logged-users";
    private static final String KEY_LAST_OTP_USER      = "last-otp-user";
    private static final String KEY_REG_OTT_EXPIRATION = "reg-ott-expiration";

    private static final String KEY_FORMAT_PAIR = "%s@%s";

    private SharedPreferences mSharedPrefs;

    // key=backend, value=serviceDetails
    private HashMap<String, ServiceDetails>   mServiceInfo;
    // key=customerId, value=customerInfo
    private HashMap<String, CustomerInfo>     mCustomerInfo;
    // key=customerId@backend, value=userId
    private HashMap<String, String>           mLastLoggedUsers;
    // first=userId, second=customerId
    private Pair<String, String>              mLastOtpUser;
    // key=userId@customerId, value=expiration
    private HashMap<String, RegOttExpiration> mExpiration;

    private Gson mGson;

    MfaInfoCache(SharedPreferences sharedPreferences) {
        mSharedPrefs = sharedPreferences;
        mGson = new Gson();
        initCustomerInfo();
        initServiceInfo();
        initLastLoggedUsers();
        initLastOtpUser();
        initExpiration();
    }


    void putServiceDetails(@NonNull ServiceDetails serviceDetails) {
        mServiceInfo.put(getBackend(serviceDetails.backendUrl), serviceDetails);

        updateServiceInfoSharedPrefs();
    }

    void putLastLoggedInUser(@NonNull User user) {
        String key = getLastLoggedKey(user.getBackend(), user.getCustomerId());
        mLastLoggedUsers.put(key, user.getId());
        updateLastLoggedUsersSharedPrefs();
    }

    void removeFromLastLoggedInUsers(@NonNull User user) {
        String key = getLastLoggedKey(user.getBackend(), user.getCustomerId());
        String lastLoggedInUserId = mLastLoggedUsers.get(key);
        if (lastLoggedInUserId != null && lastLoggedInUserId.equals(user.getId())) {
            mLastLoggedUsers.remove(key);
            updateLastLoggedUsersSharedPrefs();
        }
    }

    void putCustomerInfo(String id, String name, String logoUrl) {
        mCustomerInfo.put(id, new CustomerInfo(id, name, logoUrl));
        updateCustomerInfoSharedPrefs();
    }

    void putLastOtpUser(@NonNull User user) {
        mLastOtpUser = new Pair<>(user.getId(), user.getCustomerId());
        updateLastOtpUser();
    }

    void removeFromLastOtpUser(@NonNull User user) {
        if (mLastOtpUser != null && mLastOtpUser.first != null && mLastOtpUser.second != null) {
            if (mLastOtpUser.first.equals(user.getId()) && mLastOtpUser.second.equals(user.getCustomerId())) {
                mLastOtpUser = null;
                updateLastOtpUser();
            }
        }
    }

    void putExpiration(@NonNull User user) {
        String key = getExpirationKey(user);
        mExpiration.put(key, new RegOttExpiration(user.getRegistationExpiration(), false));
        updateExpiration();
    }

    void removeExpiration(@NonNull User user) {
        mExpiration.remove(getExpirationKey(user));
        updateExpiration();
    }

    void invalidateExpiration(User user, boolean isCancelled) {
        String key = getExpirationKey(user);
        RegOttExpiration expiration = mExpiration.get(key);
        if (expiration != null) {
            expiration.expireTimeSeconds = 0;
            expiration.isCancelled = isCancelled;
        }
        mExpiration.put(key, expiration);
        updateExpiration();
    }

    /**
     * Get stored information about a customer by its id
     *
     * @param id
     *   The id of the customer
     * @return The stored info or <code>null</code> if there is no information for this id
     * @see CustomerInfo
     */
    public
    @Nullable
    CustomerInfo getCustomerInfo(String id) {
        if (mCustomerInfo.containsKey(id)) {
            return mCustomerInfo.get(id);
        } else {
            return null;
        }
    }

    /**
     * Get stored service details for a backend.
     *
     * @param backend
     *   The backend for which to get details as defined in {@link ServiceDetails#backendUrl}
     * @return The details for the service or <code>null</code> if there are no stored details for this backend
     * @see ServiceDetails
     */
    public
    @Nullable
    ServiceDetails getServiceDetails(@NonNull String backend) {
        if (mServiceInfo.containsKey(backend)) {
            return mServiceInfo.get(backend);
        } else {
            return null;
        }
    }

    /**
     * Obtain the id of the last user that has successfully authenticated to a backend of a customer.
     *
     * @param backend
     *   The backend used for authentication. Should be passed as it is returned from
     *   {@link User#getBackend()} (not containing scheme).
     * @param customerId
     *   The customer id to which the backend is associated
     * @return The user's id. Could be <code>null</code> if there hasn't been a successful authentication to that
     * backend/customerId pair.
     */
    public
    @Nullable
    String getLastLoggedInUserId(@NonNull String backend, @NonNull String customerId) {
        String key = getLastLoggedKey(getBackend(backend), customerId);
        if (mLastLoggedUsers.containsKey(key)) {
            return mLastLoggedUsers.get(key);
        } else {
            return null;
        }
    }

    /**
     * Obtain a pair of the last successfully authenticated user for a one time password and the customer id
     *
     * @return Pair.first is the id of the user,
     * Pair.second is the customer id. Could be <code>null</code> if there hasn't been a successful OTP authentication.
     */
    public
    @Nullable
    Pair<String, String> getLastOtpUserAndCustomerId() {
        return mLastOtpUser;
    }

    /**
     * Get RegOtt expiration for a user. Could be null if the user is registered or has not started registration.
     *
     * @param user
     *   A user with previously started registration process
     * @return The RegOtt expiration if available
     * @see RegOttExpiration
     */
    public
    @Nullable
    RegOttExpiration getRegOttExpiration(User user) {
        String key = getExpirationKey(user);
        if (mExpiration.containsKey(key)) {
            return mExpiration.get(key);
        } else {
            return null;
        }
    }

    private String getExpirationKey(User user) {
        return String.format(KEY_FORMAT_PAIR, user.getId(), user.getCustomerId());
    }

    private String getLastLoggedKey(String backend, String customerId) {
        return String.format(Locale.US, KEY_FORMAT_PAIR, customerId, backend);
    }

    private void updateServiceInfoSharedPrefs() {
        String serviceInfoJson = mGson.toJson(mServiceInfo);
        mSharedPrefs.edit().putString(KEY_SERVICE_INFO, serviceInfoJson).apply();
    }

    private void updateCustomerInfoSharedPrefs() {
        String customerInfoJson = mGson.toJson(mCustomerInfo);
        mSharedPrefs.edit().putString(KEY_CUSTOMER_INFO, customerInfoJson).apply();
    }

    private void updateLastLoggedUsersSharedPrefs() {
        String lastLoggedJson = mGson.toJson(mLastLoggedUsers);
        mSharedPrefs.edit().putString(KEY_LAST_LOGGED, lastLoggedJson).apply();
    }

    private void updateLastOtpUser() {
        String lastOtpJson = mGson.toJson(mLastOtpUser);
        mSharedPrefs.edit().putString(KEY_LAST_OTP_USER, lastOtpJson).apply();
    }

    private void updateExpiration() {
        String expirationJson = mGson.toJson(mExpiration);
        mSharedPrefs.edit().putString(KEY_REG_OTT_EXPIRATION, expirationJson).apply();
    }

    private void initCustomerInfo() {
        if (mSharedPrefs.contains(KEY_CUSTOMER_INFO)) {
            String customerInfoJson = mSharedPrefs.getString(KEY_CUSTOMER_INFO, "");
            try {
                mCustomerInfo = mGson.fromJson(customerInfoJson, new TypeToken<HashMap<String, CustomerInfo>>() {

                }.getType());
            } catch (JsonParseException e) {
                mCustomerInfo = new HashMap<>();
            }
        } else {
            mCustomerInfo = new HashMap<>();
        }
    }

    private void initServiceInfo() {
        if (mSharedPrefs.contains(KEY_SERVICE_INFO)) {
            String serviceInfoJson = mSharedPrefs.getString(KEY_SERVICE_INFO, "");
            try {
                mServiceInfo = mGson.fromJson(serviceInfoJson, new TypeToken<HashMap<String, ServiceDetails>>() {

                }.getType());
            } catch (JsonParseException e) {
                mServiceInfo = new HashMap<>();
            }
        } else {
            mServiceInfo = new HashMap<>();
        }
    }

    private String getBackend(String backendUrl) {
        Uri uri = Uri.parse(backendUrl);
        if (uri != null) {
            return uri.getAuthority();
        } else {
            return null;
        }
    }

    private void initLastLoggedUsers() {
        if (mSharedPrefs.contains(KEY_LAST_LOGGED)) {
            String serviceInfoJson = mSharedPrefs.getString(KEY_LAST_LOGGED, "");
            try {
                mLastLoggedUsers = mGson.fromJson(serviceInfoJson, new TypeToken<HashMap<String, String>>() {

                }.getType());
            } catch (JsonParseException e) {
                mLastLoggedUsers = new HashMap<>();
            }
        } else {
            mLastLoggedUsers = new HashMap<>();
        }
    }

    private void initLastOtpUser() {
        if (mSharedPrefs.contains(KEY_LAST_OTP_USER)) {
            String lastOtpUser = mSharedPrefs.getString(KEY_LAST_OTP_USER, "");
            try {
                mLastOtpUser = mGson.fromJson(lastOtpUser, new TypeToken<Pair<String, String>>() {

                }.getType());
            } catch (JsonParseException e) {
                mLastOtpUser = null;
            }
        } else {
            mLastOtpUser = null;
        }
    }

    private void initExpiration() {
        if (mSharedPrefs.contains(KEY_REG_OTT_EXPIRATION)) {
            String expirationJson = mSharedPrefs.getString(KEY_REG_OTT_EXPIRATION, "");
            try {
                mExpiration = mGson.fromJson(expirationJson, new TypeToken<HashMap<String, RegOttExpiration>>() {

                }.getType());
            } catch (JsonParseException e) {
                mExpiration = new HashMap<>();
            }
        } else {
            mExpiration = new HashMap<>();
        }
    }

    /**
     * Cached information about a customer.
     */
    public static class CustomerInfo {

        private String id;
        private String name;
        private String logoUrl;

        private CustomerInfo(String id, String name, String logoUrl) {
            this.id = id;
            this.name = name;
            this.logoUrl = logoUrl;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLogoUrl() {
            return logoUrl;
        }
    }

    /**
     * Expiration information about a user's RegOtt
     */
    public static class RegOttExpiration extends Expiration {

        /**
         * Indicates whether a user has canceled the registration process manually.
         */
        boolean isCancelled;

        private RegOttExpiration(Expiration registrationExpiration, boolean isCancelled) {
            if (registrationExpiration != null) {
                expireTimeSeconds = registrationExpiration.expireTimeSeconds;
                nowTimeSeconds = registrationExpiration.expireTimeSeconds;
            }
            this.isCancelled = isCancelled;
        }
    }
}
