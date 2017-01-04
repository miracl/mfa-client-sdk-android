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
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

import java.io.Closeable;
import java.util.List;
import java.util.Map;


public class MPinSDK implements Closeable {

    public static final String CONFIG_BACKEND = "backend";
    private long mPtr;

    public MPinSDK() {
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


    public Status startRegistration(User user) {
        return nStartRegistration(mPtr, user, "", "");
    }

    public Status startRegistration(User user, String activateCode) {
        return nStartRegistration(mPtr, user, activateCode, "");
    }

    public Status startRegistration(User user, String activateCode, String userData) {
        return nStartRegistration(mPtr, user, activateCode, userData);
    }

    public Status restartRegistration(User user) {
        return nRestartRegistration(mPtr, user, "");
    }

    public Status restartRegistration(User user, String userData) {
        return nRestartRegistration(mPtr, user, userData);
    }

    public Status confirmRegistration(User user) {
        return nConfirmRegistration(mPtr, user, "");
    }

    public Status confirmRegistration(User user, String pushToken) {
        return nConfirmRegistration(mPtr, user, pushToken);
    }

    public Status finishRegistration(User user, String pin) {
        return nFinishRegistration(mPtr, user, pin);
    }


    public Status startAuthentication(User user) {
        return nStartAuthentication(mPtr, user);
    }

    public Status checkAccessNumber(String accessNumber) {
        return nCheckAccessNumber(mPtr, accessNumber);
    }

    public Status finishAuthentication(User user, String pin) {
        return nFinishAuthentication(mPtr, user, pin);
    }

    public Status finishAuthentication(User user, String pin, StringBuilder authResultData) {
        return nFinishAuthenticationResultData(mPtr, user, pin, authResultData);
    }

    public Status finishAuthenticationOTP(User user, String pin, OTP otp) {
        return nFinishAuthenticationOTP(mPtr, user, pin, otp);
    }

    public Status finishAuthenticationAN(User user, String pin, String accessNumber) {
        return nFinishAuthenticationAN(mPtr, user, pin, accessNumber);
    }

    public Status listUsers(List<User> users) {
        return nListUsers(mPtr, users);
    }

    public Status listAllUsers(List<User> users) {
        return nListAllUsers(mPtr, users);
    }

    public Status listUsers(List<User> users, String backend) {
        return nListUsersForBackend(mPtr, users, backend);
    }

    public Status listBackends(List<String> backends) {
        return nListBackends(mPtr, backends);
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

    private native Status nSetBackend(long ptr, String server);

    private native Status nSetBackendRPS(long ptr, String server, String rpsPrefix);


    private native User nMakeNewUser(long ptr, String id, String deviceName);

    private native void nDeleteUser(long ptr, User user);

    private native void nClearUsers(long ptr);


    private native boolean nCanLogout(long ptr, User user);

    private native boolean nLogout(long ptr, User user);

    private native String nGetClientParam(long ptr, String key);

    // Native methods from MPinSDK

    private native Status nStartRegistration(long ptr, User user, String activateCode, String userData);

    private native Status nRestartRegistration(long ptr, User user, String userData);

    private native Status nConfirmRegistration(long ptr, User user, String pushToken);

    private native Status nFinishRegistration(long ptr, User user, String pin);


    private native Status nStartAuthentication(long ptr, User user);

    private native Status nCheckAccessNumber(long ptr, String accessNumber);

    private native Status nFinishAuthentication(long ptr, User user, String pin);

    private native Status nFinishAuthenticationResultData(long ptr, User user, String pin, StringBuilder authResultData);

    private native Status nFinishAuthenticationOTP(long ptr, User user, String pin, OTP otp);

    private native Status nFinishAuthenticationAN(long ptr, User user, String pin, String accessNumber);


    private native Status nListUsers(long ptr, List<User> users);

    private native Status nListUsersForBackend(long ptr, List<User> users, String backend);

    private native Status nListAllUsers(long ptr, List<User> users);

    private native Status nListBackends(long ptr, List<String> backends);
}
