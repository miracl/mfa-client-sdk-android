/***************************************************************
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

import com.miracl.mpinsdk.model.OTP;
import com.miracl.mpinsdk.model.RegCode;
import com.miracl.mpinsdk.model.ServiceDetails;
import com.miracl.mpinsdk.model.SessionDetails;
import com.miracl.mpinsdk.model.Signature;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;
import com.miracl.mpinsdk.model.VerificationResult;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

public class MPinMFA implements Closeable {

    public static final String CONFIG_BACKEND = "backend";

    /**
     * MPin Authentication notification channel ID
     */
    public static final String AUTHENTICATION_NOTIFICATION_CHANNEL_ID = "mpin_authentication_notification_channel";

    private long mPtr;

    public MPinMFA() {
        mPtr = nConstruct();
    }

    @Override
    public void close() {
        synchronized (this) {
            nDestruct(mPtr);
            mPtr = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public Status init(Map<String, String> config, Context context) {
        return nInit(mPtr, config, context);
    }

    public void addCustomHeaders(Map<String, String> headers) {
        nAddCustomHeaders(mPtr, headers);
    }

    public void clearCustomHeaders() {
        nClearCustomHeaders(mPtr);
    }

    public void addTrustedDomain(String domain) {
        nAddTrustedDomain(mPtr, domain);
    }

    public void clearTrustedDomains() {
        nClearTrustedDomains(mPtr);
    }

    public Status testBackend(String server) {
        return nTestBackend(mPtr, server);
    }

    public boolean isUserExisting(String id) {
        return nIsUserExisting(mPtr, id, "", "");
    }

    public boolean isUserExisting(String id, String customerId) {
        return nIsUserExisting(mPtr, id, customerId, "");
    }

    public boolean isUserExisting(String id, String customerId, String appId) {
        return nIsUserExisting(mPtr, id, customerId, appId);
    }

    public Status setBackend(String server) {
        return nSetBackend(mPtr, server);
    }

    public User makeNewUser(String id) {
        return nMakeNewUser(mPtr, id, "");
    }

    public User makeNewUser(String id, String deviceName) {
        return nMakeNewUser(mPtr, id, deviceName);
    }

    public void deleteUser(User user) {
        nDeleteUser(mPtr, user);
    }

    public void clearUsers() {
        nClearUsers(mPtr);
    }

    public boolean canLogout(User user) {
        return nCanLogout(mPtr, user);
    }

    public boolean logout(User user) {
        return nLogout(mPtr, user);
    }

    public String getClientParam(String key) {
        return nGetClientParam(mPtr, key);
    }


    public Status getServiceDetails(String serviceUrl, ServiceDetails serviceDetails) {
        return nGetServiceDetails(mPtr, serviceUrl, serviceDetails);
    }

    public Status getSessionDetails(String accessCode, SessionDetails sessionDetails) {
        return nGetSessionDetails(mPtr, accessCode, sessionDetails);
    }

    public void setCid(String cid) {
        nSetCID(mPtr, cid);
    }

    public Status abortSession(String accessCode) {
        return nAbortSession(mPtr, accessCode);
    }


    public Status startRegistration(User user, String accessCode) {
        return nStartRegistration(mPtr, user, accessCode, "");
    }

    public Status startRegistration(User user, String accessCode, String pushToken) {
        return nStartRegistration(mPtr, user, accessCode, pushToken);
    }

    public Status startRegistration(User user, String accessCode, String pushToken, String regCode) {
        return nStartRegistrationRegCode(mPtr, user, accessCode, pushToken, regCode);
    }

    public Status restartRegistration(User user) {
        return nRestartRegistration(mPtr, user);
    }

    public Status confirmRegistration(User user) {
        return nConfirmRegistration(mPtr, user);
    }

    public Status finishRegistration(User user, String secret) {
        return nFinishRegistration(mPtr, user, secret);
    }

    public Status finishRegistration(User user, String[] multiFactor) {
        return nFinishRegistrationMultiFactor(mPtr, user, multiFactor);
    }

    public Status startRegistrationDvs(User user, String[] multiFactor) {
        return nStartRegistrationDVS(mPtr, user, multiFactor);
    }

    public Status finishRegistrationDvs(User user, String[] multiFactor) {
        return nFinishRegistrationDVS(mPtr, user, multiFactor);
    }

    public Status getAccessCode(String authUrl, StringBuilder accessCode) {
        return nGetAccessCode(mPtr, authUrl, accessCode);
    }

    public Status startAuthentication(User user, String accessCode) {
        return nStartAuthentication(mPtr, user, accessCode);
    }

    public Status startAuthenticationOtp(User user) {
        return nStartAuthenticationOTP(mPtr, user);
    }

    public Status startAuthenticationRegCode(User user) {
        return nStartAuthenticationRegCode(mPtr, user);
    }

    public Status finishAuthentication(User user, String secret, String accessCode) {
        return nFinishAuthentication(mPtr, user, secret, accessCode);
    }

    public Status finishAuthentication(User user, String[] multiFactor, String accessCode) {
        return nFinishAuthenticationMultiFactor(mPtr, user, multiFactor, accessCode);
    }

    public Status finishAuthentication(User user, String secret, String accessCode, StringBuilder authCode) {
        return nFinishAuthenticationAuthCode(mPtr, user, secret, accessCode, authCode);
    }

    public Status finishAuthentication(User user, String[] multiFactor, String accessCode, StringBuilder authCode) {
        return nFinishAuthenticationAuthCodeMultiFactor(mPtr, user, multiFactor, accessCode, authCode);
    }

    public Status finishAuthenticationOtp(User user, String secret, OTP otp) {
        return nFinishAuthenticationOTP(mPtr, user, secret, otp);
    }

    public Status finishAuthenticationOtp(User user, String[] multiFactor, OTP otp) {
        return nFinishAuthenticationOTPMultiFactor(mPtr, user, multiFactor, otp);
    }

    public Status finishAuthenticationRegCode(User user, String[] multiFactor, RegCode regCode) {
        return nFinishAuthenticationRegCode(mPtr, user, multiFactor, regCode);
    }

    public boolean isRegistrationTokenSet(User user) {
        return nIsRegistrationTokenSet(mPtr, user);
    }

    public Status sign(User user, byte[] documentHash, String secret, int epochTime, Signature signature) {
        return nSign(mPtr, user, documentHash, secret, epochTime, signature);
    }

    public Status sign(User user, byte[] documentHash, String[] multiFactor, int epochTime, Signature signature) {
        return nSignMultiFactor(mPtr, user, documentHash, multiFactor, epochTime, signature);
    }

    public Status listUsers(List<User> users) {
        return nListUsers(mPtr, users);
    }

    public Status setRegistrationToken(User user, String regToken) {
        return nSetRegistrationToken(mPtr, user, regToken);
    }

    public byte[] hashDocument(byte[] document) {
        return nHashDocument(mPtr, document);
    }

    public Status startVerification(User user, String clientId, String accessCode) {
        return nStartVerification(mPtr, user, clientId, accessCode);
    }

    public Status finishVerification(User user, String verificationCode, VerificationResult verificationResult) {
        return nFinishVerification(mPtr, user, verificationCode, verificationResult);
    }


    // Native methods from MPinSDKBase

    private native long nConstruct();

    private native void nDestruct(long ptr);

    private native Status nInit(long ptr, Map<String, String> config, Context context);

    private native void nAddCustomHeaders(long ptr, Map<String, String> customHeaders);

    private native void nClearCustomHeaders(long ptr);

    private native void nAddTrustedDomain(long ptr, String domain);

    private native void nClearTrustedDomains(long ptr);

    private native Status nTestBackend(long ptr, String server);


    private native boolean nIsUserExisting(long ptr, String id, String customerId, String appId);

    private native Status nSetBackend(long ptr, String server);

    private native User nMakeNewUser(long ptr, String id, String deviceName);

    private native void nDeleteUser(long ptr, User user);

    private native void nClearUsers(long ptr);

    private native boolean nCanLogout(long ptr, User user);

    private native boolean nLogout(long ptr, User user);

    private native String nGetClientParam(long ptr, String key);

    // Native methods from MPinMFA

    private native Status nGetServiceDetails(long ptr, String url, ServiceDetails serviceDetails);

    private native Status nGetSessionDetails(long ptr, String accessCode, SessionDetails sessionDetails);

    private native void nSetCID(long ptr, String cid);

    private native Status nAbortSession(long ptr, String accessCode);

    private native Status nStartRegistration(long ptr, User user, String accessCode, String pushToken);

    private native Status nStartRegistrationRegCode(long ptr, User user, String accessCode, String pushToken, String regCode);

    private native Status nRestartRegistration(long ptr, User user);

    private native Status nConfirmRegistration(long ptr, User user);

    private native Status nFinishRegistration(long ptr, User user, String secret);

    private native Status nFinishRegistrationMultiFactor(long ptr, User user, String[] multiFactor);

    private native Status nStartRegistrationDVS(long ptr, User user, String[] multiFactor);

    private native Status nFinishRegistrationDVS(long ptr, User user, String[] multiFactor);

    private native Status nGetAccessCode(long ptr, String authUrl, StringBuilder accessCode);

    private native Status nStartAuthentication(long ptr, User user, String accessCode);

    private native Status nStartAuthenticationOTP(long ptr, User user);

    private native Status nStartAuthenticationRegCode(long mPtr, User user);

    private native Status nFinishAuthentication(long ptr, User user, String secret, String accessCode);

    private native Status nFinishAuthenticationMultiFactor(long ptr, User user, String[] multiFactor, String accessCode);

    private native Status nFinishAuthenticationOTP(long ptr, User user, String secret, OTP otp);

    private native Status nFinishAuthenticationOTPMultiFactor(long ptr, User user, String[] multiFactor, OTP otp);

    private native Status nFinishAuthenticationAuthCode(long ptr, User user, String secret, String accessCode,
                                                        StringBuilder authCode);

    private native Status nFinishAuthenticationAuthCodeMultiFactor(long ptr, User user, String[] multiFactor, String accessCode, StringBuilder authCode);

    private native Status nFinishAuthenticationRegCode(long mPtr, User user, String[] multiFactor, RegCode regCode);

    private native Status nSign(long ptr, User user, byte[] documentHash, String secret, int epochTime, Signature signature);

    private native Status nSignMultiFactor(long ptr, User user, byte[] documentHash, String[] multiFactor, int epochTime, Signature signature);

    private native Status nListUsers(long ptr, List<User> users);

    private native Status nSetRegistrationToken(long ptr, User user, String regToken);

    private native boolean nIsRegistrationTokenSet(long ptr, User user);

    private native byte[] nHashDocument(long ptr, byte[] document);

    private native Status nStartVerification(long ptr, User user, String clientId, String accessCode);

    private native Status nFinishVerification(long ptr, User user, String verificationCode, VerificationResult verificationResult);
}
