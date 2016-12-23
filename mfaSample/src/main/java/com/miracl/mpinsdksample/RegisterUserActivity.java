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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

import static android.view.View.GONE;

public class RegisterUserActivity extends AppCompatActivity implements View.OnClickListener, EnterPinDialog.EventListener {

    private EditText       mEmailInput;
    private Button         mSubmitButton;
    private View           mConfirmControls;
    private Button         mConfirmButton;
    private Button         mResendButton;
    private EnterPinDialog mEnterPinDialog;

    private User mCurrentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mEnterPinDialog = new EnterPinDialog(RegisterUserActivity.this, RegisterUserActivity.this);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEnterPinDialog.dismiss();
        // In order not to clutter the sample app with users, remove the current user if it is not registered on
        // navigating away from the app
        if (mCurrentUser != null && mCurrentUser.getState() != User.State.REGISTERED) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    SampleApplication.getMfaSdk().deleteUser(mCurrentUser);
                    return null;
                }
            }.execute();
        }
    }

    @Override
    public void onPinEntered(final String pin) {
        if (mCurrentUser == null) {
            return;
        }

        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
                // Once we have the user's pin we can finish the registration process
                return SampleApplication.getMfaSdk().finishRegistration(mCurrentUser, pin);
            }


            @Override
            protected void onPostExecute(com.miracl.mpinsdk.model.Status status) {
                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    // The registration for the user is complete
                    startActivity(new Intent(RegisterUserActivity.this, RegistrationSuccessfulActivity.class));
                    finish();
                } else {
                    // Finishing registration has failed
                    Toast.makeText(RegisterUserActivity.this,
                      "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(), Toast.LENGTH_SHORT)
                      .show();
                    enableControls();
                }
            }
        }.execute();
    }

    @Override
    public void onPinCanceled() {
        enableControls();
    }

    private void onConfirmClick() {
        if (mCurrentUser == null) {
            return;
        }

        disableControls();
        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
                // After the user has followed the steps in the verification mail, it must be confirmed from the SDK
                // in order to proceed with the registration process
                return SampleApplication.getMfaSdk().confirmRegistration(mCurrentUser);
            }

            @Override
            protected void onPostExecute(com.miracl.mpinsdk.model.Status status) {
                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    mEnterPinDialog.show();
                } else {
                    // Confirmation has failed
                    Toast.makeText(RegisterUserActivity.this,
                      "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(), Toast.LENGTH_SHORT)
                      .show();
                    enableControls();
                }
            }
        }.execute();
    }

    private void onResendClick() {
        if (mCurrentUser == null) {
            return;
        }

        disableControls();
        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
                // If for some reason we need to resend the verification mail, the registration process for the user must be
                // restarted
                return SampleApplication.getMfaSdk().restartRegistration(mCurrentUser);
            }

            @Override
            protected void onPostExecute(com.miracl.mpinsdk.model.Status status) {
                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    // If restarting the registration process is successful a new verification mail is sent
                    Toast
                      .makeText(RegisterUserActivity.this, "Email has been sent to " + mCurrentUser.getId(), Toast.LENGTH_SHORT)
                      .show();
                } else {
                    // Restarting registration has failed
                    Toast.makeText(RegisterUserActivity.this,
                      "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(), Toast.LENGTH_SHORT)
                      .show();
                }

                enableControls();
            }
        }.execute();
    }

    private void onSubmitClick() {
        final String email = mEmailInput.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address entered", Toast.LENGTH_SHORT).show();
            return;
        }

        disableControls();
        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
                // Obtain a user object from the SDK. The id of the user is an email and while it is not mandatory to provide
                // device name note that some backends may require it
                mCurrentUser = SampleApplication.getMfaSdk().makeNewUser(email, "Android Sample App");

                // After we have a user, we can start the registration process for it. If successful this will trigger sending a
                // confirmation email from the current backend
                return SampleApplication.getMfaSdk().startRegistration(mCurrentUser, SampleApplication.getCurrentAccessCode());
            }

            @Override
            protected void onPostExecute(com.miracl.mpinsdk.model.Status status) {
                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    // When the registration process is started successfully for a user, it is stored in the SDK and is
                    // associated with the current backend at the time.
                    mSubmitButton.setVisibility(GONE);
                    mConfirmControls.setVisibility(View.VISIBLE);
                    Toast
                      .makeText(RegisterUserActivity.this, "Email has been sent to " + mCurrentUser.getId(), Toast.LENGTH_SHORT)
                      .show();
                } else {
                    // Starting registration has failed
                    Toast.makeText(RegisterUserActivity.this,
                      "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(), Toast.LENGTH_SHORT)
                      .show();
                }
                enableControls();
            }
        }.execute();
    }

    private void onEmailChanged(CharSequence textInput) {
        if (mSubmitButton.getVisibility() != View.VISIBLE) {
            disableControls();

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    // If the email is changed after the registration is started, we delete the identity (because it will get
                    // stored otherwise) and effectively restart the registration process. This is solely not to clutter the
                    // sample app with users
                    SampleApplication.getMfaSdk().deleteUser(mCurrentUser);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    mCurrentUser = null;
                    mConfirmControls.setVisibility(View.GONE);
                    mSubmitButton.setVisibility(View.VISIBLE);
                    enableControls();
                }
            }.execute();
        } else {
            if (textInput.length() == 0 && mSubmitButton.isEnabled()) {
                mSubmitButton.setEnabled(false);
            } else {
                mSubmitButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_submit_button:
                onSubmitClick();
                return;
            case R.id.register_confirm_button:
                onConfirmClick();
                return;
            case R.id.register_resend_button:
                onResendClick();
                return;
            default:
                break;
        }
    }

    private void disableControls() {
        mSubmitButton.setEnabled(false);
        mConfirmButton.setEnabled(false);
        mResendButton.setEnabled(false);
        mEmailInput.setEnabled(false);
    }

    private void enableControls() {
        mSubmitButton.setEnabled(true);
        mConfirmButton.setEnabled(true);
        mResendButton.setEnabled(true);
        mEmailInput.setEnabled(true);
    }

    private void initViews() {
        mEmailInput = (EditText) findViewById(R.id.register_email_input);
        mConfirmControls = findViewById(R.id.register_confirm_controls);

        mSubmitButton = (Button) findViewById(R.id.register_submit_button);
        mSubmitButton.setOnClickListener(this);
        mConfirmButton = (Button) findViewById(R.id.register_confirm_button);
        mConfirmButton.setOnClickListener(this);
        mResendButton = (Button) findViewById(R.id.register_resend_button);
        mResendButton.setOnClickListener(this);

        mEmailInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onEmailChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void resetViews() {
        mEmailInput.setText("");
        mSubmitButton.setEnabled(false);
        mSubmitButton.setVisibility(View.VISIBLE);
        mConfirmControls.setVisibility(GONE);
    }
}