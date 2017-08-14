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
import com.miracl.mpinsdk.model.ServiceDetails;
import com.miracl.mpinsdk.model.SessionDetails;
import com.miracl.mpinsdk.model.Signature;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

public class MPinMFA implements Closeable {

    public static final String CONFIG_BACKEND = "backend";

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

    public Status testBackend(String server, String rpsPrefix) {
        return nTestBackendRPS(mPtr, server, rpsPrefix);
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

    public Status setBackend(String server, String rpsPrefix) {
        return nSetBackendRPS(mPtr, server, rpsPrefix);
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

    public Status restartRegistration(User user) {
        return nRestartRegistration(mPtr, user);
    }

    public Status confirmRegistration(User user) {
        return nConfirmRegistration(mPtr, user);
    }

    public Status finishRegistration(User user, String pin) {
        return nFinishRegistration(mPtr, user, pin);
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

    public Status finishAuthentication(User user, String pin, String accessCode) {
        return nFinishAuthentication(mPtr, user, pin, accessCode);
    }

    public Status finishAuthentication(User user, String pin, String accessCode, StringBuilder authCode) {
        return nFinishAuthenticationAuthCode(mPtr, user, pin, accessCode, authCode);
    }

    public Status finishAuthenticationOtp(User user, String pin, OTP otp) {
        return nFinishAuthenticationOTP(mPtr, user, pin, otp);
    }

    public boolean verifyDocumentHash(String document, byte[] hash) {
        return nVerifyDocumentHash(mPtr, document, hash);
    }

    public Status sign(User user, byte[] documentHash, String pin, int epochTime, Signature signature) {
        return nSign(mPtr, user, documentHash, pin, epochTime, signature);
    }

    public Status listUsers(List<User> users) {
        return nListUsers(mPtr, users);
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

    private native Status nTestBackendRPS(long ptr, String server, String rpsPrefix);

    private native boolean nIsUserExisting(long ptr, String id, String customerId, String appId);

    private native Status nSetBackend(long ptr, String server);

    private native Status nSetBackendRPS(long ptr, String server, String rpsPrefix);

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

    private native Status nRestartRegistration(long ptr, User user);

    private native Status nConfirmRegistration(long ptr, User user);

    private native Status nFinishRegistration(long ptr, User user, String pin);


    private native Status nGetAccessCode(long ptr, String authUrl, StringBuilder accessCode);

    private native Status nStartAuthentication(long ptr, User user, String accessCode);

    private native Status nStartAuthenticationOTP(long ptr, User user);

    private native Status nFinishAuthentication(long ptr, User user, String pin, String accessCode);

    private native Status nFinishAuthenticationOTP(long ptr, User user, String pin, OTP otp);

    private native Status nFinishAuthenticationAuthCode(long ptr, User user, String pin, String accessCode,
                                                        StringBuilder authCode);


    private native boolean nVerifyDocumentHash(long ptr, String document, byte[] hash);

    private native Status nSign(long ptr, User user, byte[] documentHash, String pin, int epochTime, Signature signature);


    private native Status nListUsers(long ptr, List<User> users);
}
