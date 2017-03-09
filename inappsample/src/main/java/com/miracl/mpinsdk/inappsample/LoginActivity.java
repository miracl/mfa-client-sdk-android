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
package com.miracl.mpinsdk.inappsample;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.miracl.mpinsdk.MPinMFA;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements EnterPinDialog.EventListener {

    private User           mCurrentUser;
    private EnterPinDialog mEnterPinDialog;
    private View           mUserInfo;
    private MessageDialog  mMessageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEnterPinDialog = new EnterPinDialog(this, this);
        mMessageDialog = new MessageDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentUserAndInit();
    }

    @Override
    public void onPinEntered(final String pin) {
        new AsyncTask<Void, Void, Status>() {

            // The auth code given from the sdk on successful authentication
            private StringBuilder authCode;

            @Override
            protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
                authCode = new StringBuilder();
                MPinMFA mPinMfa = SampleApplication.getMfaSdk();
                String accessCode = SampleApplication.getCurrentAccessCode();
                if (mCurrentUser != null && accessCode != null) {
                    // Start the authentication process with the scanned access code and a registered user
                    com.miracl.mpinsdk.model.Status startAuthenticationStatus = mPinMfa
                      .startAuthentication(mCurrentUser, accessCode);
                    if (startAuthenticationStatus.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                        // Finish the authentication with the user's pin
                        return mPinMfa.finishAuthentication(mCurrentUser, pin, accessCode, authCode);
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
                    // We don't have a current user or an access code
                    mMessageDialog.show("Can't login right now, try again");
                    return;
                }

                if (mCurrentUser != null && mCurrentUser.getState() == User.State.BLOCKED) {
                    // If the user's identity gets blocked, we delete it
                    new AlertDialog.Builder(LoginActivity.this).setMessage(
                      "Identity has been blocked because of too many wrong PIN entries. You will need to create it again.")
                      .setPositiveButton("OK", null).setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            onDeleteClick();
                        }
                    }).show();
                    return;
                }

                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK && authCode != null) {
                    // The authentication for the user is successful
                    validateLogin(authCode.toString());
                } else {
                    // Authentication failed
                    mMessageDialog.show(status);
                }
            }
        }.execute();
    }

    private void validateLogin(String authCode) {
        // We use the auth code from the sdk to validate the login with a demo service
        final String clientService = getString(R.string.mpin_access_code_service_base_url);
        if (!clientService.isEmpty() && mCurrentUser != null && authCode != null) {
            new ValidateLoginTask(clientService, authCode, mCurrentUser.getId()) {

                @Override
                protected void onPostExecute(Boolean isSuccessful) {
                    if (isSuccessful) {
                        mMessageDialog.show(
                          "Successfully logged " + mCurrentUser.getId() + " to " + clientService + " with " + mCurrentUser
                            .getBackend());
                    } else {
                        mMessageDialog
                          .show("Failed to validate login to " + clientService + " with " + mCurrentUser.getBackend());
                    }
                }
            }.execute();
        } else {
            mMessageDialog.show("Failed to validate login");
        }
    }

    @Override
    public void onPinCanceled() {

    }

    private void getCurrentUserAndInit() {
        new AsyncTask<Void, Void, Pair<Status, List<User>>>() {

            @Override
            protected Pair<com.miracl.mpinsdk.model.Status, List<User>> doInBackground(Void... voids) {
                MPinMFA sdk = SampleApplication.getMfaSdk();
                // Get the list of stored users in order to check if there is
                // an existing user that can be logged in with
                List<User> users = new ArrayList<>();
                List<User> registeredUsers = new ArrayList<>();
                com.miracl.mpinsdk.model.Status listUsersStatus = sdk.listUsers(users);
                for (User user : users) {
                    if (user.getState() == User.State.REGISTERED) {
                        registeredUsers.add(user);
                    } else {
                        // delete users that are not registered, because the sample does not handle such cases
                        sdk.deleteUser(user);
                    }
                }

                return new Pair<>(listUsersStatus, registeredUsers);
            }

            @Override
            protected void onPostExecute(Pair<com.miracl.mpinsdk.model.Status, List<User>> statusUsersPair) {
                com.miracl.mpinsdk.model.Status status = statusUsersPair.first;

                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    // Filter the registered users for the current backend
                    String backendUrl = getString(R.string.mpin_backend);
                    Uri currentBackend = Uri.parse(backendUrl);

                    if (currentBackend != null && currentBackend.getAuthority() != null) {
                        List<User> currentBackendRegisteredUsers = new ArrayList<>();
                        for (User user : statusUsersPair.second) {
                            if (user.getBackend().equalsIgnoreCase(currentBackend.getAuthority())) {
                                currentBackendRegisteredUsers.add(user);
                            }
                        }

                        if (currentBackendRegisteredUsers.isEmpty()) {
                            // If there are no users, we need to register a new one
                            startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
                            finish();
                        } else {
                            // If there is a registered user show info
                            mCurrentUser = currentBackendRegisteredUsers.get(0);
                            initView();
                        }
                    }
                } else {
                    // Listing user for the current backend failed
                    mMessageDialog.show(status);
                }
            }
        }.execute();
    }

    private void startLogin() {
        mEnterPinDialog.setTitle(mCurrentUser.getId());
        mEnterPinDialog.show();
    }

    private void onDeleteClick() {
        if (mCurrentUser != null) {
            mUserInfo.setVisibility(View.GONE);
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    // After we delete the current user, start the registration flow again
                    SampleApplication.getMfaSdk().deleteUser(mCurrentUser);
                    startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
                    finish();
                    return null;
                }
            }.execute();
        }
    }

    private void onLoginClick() {
        if (mCurrentUser != null) {
            new AccessCodeObtainingTask(getString(R.string.mpin_access_code_service_base_url),
              new AccessCodeObtainingTask.Callback() {

                  @Override
                  public void onSuccess() {
                      startLogin();
                  }

                  @Override
                  public void onFail(Status status) {
                      mMessageDialog.show(status);
                  }
              }).execute();
        }
    }

    private void initView() {
        if (mCurrentUser != null) {
            ((TextView) findViewById(R.id.user_id)).setText(mCurrentUser.getId());
            ((TextView) findViewById(R.id.user_backend)).setText(mCurrentUser.getBackend());
            ((TextView) findViewById(R.id.user_state)).setText(mCurrentUser.getState().toString());
            ((TextView) findViewById(R.id.user_cid)).setText(mCurrentUser.getCustomerId());
            findViewById(R.id.delete_user_button).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    onDeleteClick();
                }
            });
            findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    onLoginClick();
                }
            });
            mUserInfo = findViewById(R.id.user_info);
            mUserInfo.setVisibility(View.VISIBLE);
        }
    }
}
