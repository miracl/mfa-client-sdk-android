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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.miracl.mpinsdk.MPinSDK;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

import java.util.ArrayList;

public class RegistrationSuccessfulActivity extends AppCompatActivity implements View.OnClickListener,
  EnterPinDialog.EventListener {

    private Button         mLoginButton;
    private EnterPinDialog mEnterPinDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_successful);

        initView();
    }

    @Override
    public void onPinEntered(final String pin) {
        new AsyncTask<Void, Void, Status>() {

            private User mCurrentUser;

            @Override
            protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
                MPinSDK mPinSDK = SampleApplication.getSdk();
                ArrayList<User> users = new ArrayList<>();
                // Check if we have a registered user for the currently set backend
                mPinSDK.ListUsers(users);
                if (!users.isEmpty() && SampleApplication.getCurrentAccessCode() != null) {
                    mCurrentUser = users.get(0);
                    // Start the authentication process with the stored access code and a registered user
                    com.miracl.mpinsdk.model.Status startAuthenticationStatus = mPinSDK
                      .StartAuthentication(mCurrentUser, SampleApplication.getCurrentAccessCode());
                    if (startAuthenticationStatus.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                        // Finish the authentication with the user's pin
                        return mPinSDK.FinishAuthenticationAN(mCurrentUser, pin, SampleApplication.getCurrentAccessCode());
                    } else {
                        return startAuthenticationStatus;
                    }
                } else {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(com.miracl.mpinsdk.model.Status status) {
                if (status == null) {
                    // We don't have a user or an access code
                    Toast.makeText(RegistrationSuccessfulActivity.this, "Can't login right now, try again", Toast.LENGTH_SHORT)
                      .show();
                    mLoginButton.setEnabled(true);
                    return;
                }

                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    // The authentication for the user is successful
                    Toast.makeText(RegistrationSuccessfulActivity.this,
                      "Successfully logged " + mCurrentUser.getId() + " with " + mCurrentUser.getBackend(), Toast.LENGTH_SHORT)
                      .show();
                    finish();
                } else {
                    // Authentication failed
                    Toast.makeText(RegistrationSuccessfulActivity.this,
                      "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(), Toast.LENGTH_SHORT)
                      .show();
                    mLoginButton.setEnabled(true);
                }
            }
        }.execute();
    }

    @Override
    public void onPinCanceled() {
        mLoginButton.setEnabled(true);
    }

    private void initView() {
        mLoginButton = (Button) findViewById(R.id.reg_success_login);
        mLoginButton.setOnClickListener(this);

        mEnterPinDialog = new EnterPinDialog(this, this);
    }

    private void onLoginClick() {
        mLoginButton.setEnabled(false);
        mEnterPinDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reg_success_login:
                onLoginClick();
                break;
            default:
                break;
        }
    }
}
