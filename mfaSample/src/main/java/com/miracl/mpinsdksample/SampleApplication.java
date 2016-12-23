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
package com.miracl.mpinsdksample;

import android.app.Application;
import android.os.AsyncTask;

import com.miracl.mpinsdk.MPinMFA;
import com.miracl.mpinsdk.model.Status;

import java.util.HashMap;
import java.util.Map;

public class SampleApplication extends Application {

    static {
        // We need to load the MPinSDK lib
        System.loadLibrary("AndroidMpinSDK");
    }

    private static MPinMFA sMPinMfa;
    private static String  sAccessCode;

    @Override
    public void onCreate() {
        super.onCreate();

        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
                // Init the MPinMfa without additional configuration
                sMPinMfa = new MPinMFA();
                com.miracl.mpinsdk.model.Status status = sMPinMfa.init(null, SampleApplication.this);
                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    //TODO change with actual User-Agent for the sample
                    Map<String, String> headers = new HashMap<>(1);
                    headers.put("User-Agent", "com.miracl.android.tcbmfa/1.1.1 (android/6.0.1) build/101");
                    sMPinMfa.addCustomHeaders(headers);
                }

                return status;
            }
        }.execute();
    }

    public static MPinMFA getMfaSdk() {
        return sMPinMfa;
    }

    public static String getCurrentAccessCode() {
        return sAccessCode;
    }

    public static void setCurrentAccessCode(String accessCode) {
        sAccessCode = accessCode;
    }
}
