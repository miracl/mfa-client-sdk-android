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
package com.miracl.mpinsdk.model;


import java.io.Closeable;


public class User implements Closeable {

    public enum State {
        INVALID, STARTED_REGISTRATION, ACTIVATED, REGISTERED, BLOCKED
    }

    private long mPtr;


    public String getId() {
        return nGetId(mPtr);
    }

    public Expiration getRegistationExpiration() {
        Expiration expiration = new Expiration();
        nGetRegistrationExpiration(mPtr, expiration);
        return expiration;
    }

    public State getState() {
        switch (nGetState(mPtr)) {
            case 1:
                return State.STARTED_REGISTRATION;
            case 2:
                return State.ACTIVATED;
            case 3:
                return State.REGISTERED;
            case 4:
                return State.BLOCKED;
            default:
                return State.INVALID;
        }
    }

    public String getBackend() {
        return nGetBackend(mPtr);
    }

    public String getCustomerId() {
        return nGetCustomerId(mPtr);
    }

    public String getAppId() {
        return nGetAppId(mPtr);
    }

    public boolean canSign() {
        return nCanSign(mPtr);
    }

    /**
     * Gets the required length for the user PIN, as configured in the MFA platform
     * @return
     * The required length for the user PIN
     */
    public int getPinLength() {
        return nGetPinLength(mPtr);
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

    @Override
    public String toString() {
        return getId();
    }


    private User(long ptr) {
        mPtr = ptr;
    }

    private native void nDestruct(long ptr);

    private native String nGetId(long ptr);

    private native void nGetRegistrationExpiration(long ptr, Expiration expiration);

    private native int nGetState(long ptr);

    private native String nGetBackend(long ptr);

    private native String nGetCustomerId(long ptr);

    private native String nGetAppId(long ptr);

    private native boolean nCanSign(long ptr);

    private native int nGetPinLength(long ptr);
}
