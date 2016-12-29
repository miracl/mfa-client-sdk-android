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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.miracl.mpinsdk.MPinMFA;
import com.miracl.mpinsdk.model.ServiceDetails;
import com.miracl.mpinsdk.model.SessionDetails;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;
import com.miracl.mpinsdksample.util.CameraPreview;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.ArrayList;
import java.util.List;

public class QrReaderActivity extends AppCompatActivity implements EnterPinDialog.EventListener {

    static {
        // QR reader lib
        System.loadLibrary("iconv");
    }

    private static final int REQUEST_CAMERA_PERMISSION = 4321;

    private static final int FRAME_TIMEOUT = 300;

    private Camera.PreviewCallback mPreviewCallBack;
    private CameraPreview          mCameraPreview;
    private FrameLayout            mCameraContainer;
    private View                   mNoCameraPermissionView;
    private boolean                mIsCameraPermissionBlocked;

    private ImageScanner  mScanner;
    private boolean       mIsBarCodeProcessing;
    private HandlerThread mImageProcessingWorkerThread;
    private Handler       mImageProcessingHandler;

    private EnterPinDialog mEnterPinDialog;

    private String         mCurrentAccessCode;
    private User           mCurrentUser;
    private ServiceDetails mCurrentServiceDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_reader);

        mCameraContainer = (FrameLayout) findViewById(R.id.qr_reader_camera_container);
        mNoCameraPermissionView = findViewById(R.id.qr_reader_no_camera_permission);

        initImageProcessingHandler();

        mScanner = new ImageScanner();
        mScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);

        mEnterPinDialog = new EnterPinDialog(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNoCameraPermissionView.setVisibility(View.GONE);
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (cameraPermissionCheck == PackageManager.PERMISSION_GRANTED) {
            initCameraPreview();
        } else if (mIsCameraPermissionBlocked) {
            mNoCameraPermissionView.setVisibility(View.VISIBLE);
        } else {
            requestCameraPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCameraPreview();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mImageProcessingHandler != null) {
            mImageProcessingHandler.removeCallbacksAndMessages(null);
            mImageProcessingHandler = null;
        }

        if (mImageProcessingWorkerThread != null) {
            mImageProcessingWorkerThread.quit();
            mImageProcessingWorkerThread.interrupt();
            mImageProcessingWorkerThread = null;
        }

        mScanner = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initCameraPreview();
                } else {
                    mIsCameraPermissionBlocked = !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]);
                    mNoCameraPermissionView.setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }


    private void onCameraFrame(final byte[] data, final int frameWidth, final int frameHeight) {
        if (!mIsBarCodeProcessing) {
            mImageProcessingHandler.post(new Runnable() {

                @Override
                public void run() {
                    mIsBarCodeProcessing = true;
                    Image barcode = new Image(frameWidth, frameHeight, "Y800");
                    barcode.setData(data);
                    int result = mScanner.scanImage(barcode);
                    //if we have meaningful result
                    if (result != 0) {
                        // Clear all pending runnables
                        mImageProcessingHandler.removeCallbacksAndMessages(null);
                        SymbolSet symbolSet = mScanner.getResults();
                        String url = null;
                        for (Symbol sym : symbolSet) {
                            url = sym.getData();
                        }
                        final String readUrl = url;
                        runOnUiThread(new Runnable() {

                            public void run() {
                                onQrUrlReceived(readUrl);
                            }
                        });
                    } else {
                        mIsBarCodeProcessing = false;
                    }
                }
            });
        }
    }

    private void onQrUrlReceived(String url) {
        Uri qrUri = Uri.parse(url);
        stopCameraPreview();

        // Check if the url from the qr has the expected parts
        if (qrUri.getScheme() != null && qrUri.getAuthority() != null && qrUri.getFragment() != null && !qrUri.getFragment()
          .isEmpty()) {

            // Obtain the access code from the qr-read url
            mCurrentAccessCode = qrUri.getFragment();

            final String baseUrl = qrUri.getScheme() + "://" + qrUri.getAuthority();

            // Obtain the service details and set the backend
            new AsyncTask<Void, Void, Status>() {

                @Override
                protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
                    MPinMFA mPinMfa = SampleApplication.getMfaSdk();
                    ServiceDetails serviceDetails = new ServiceDetails();

                    // MPinSDK methods are synchronous, be sure not to call them on the ui thread
                    com.miracl.mpinsdk.model.Status serviceDetailsStatus = mPinMfa.getServiceDetails(baseUrl, serviceDetails);
                    if (serviceDetailsStatus.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {

                        com.miracl.mpinsdk.model.Status backendStatus = mPinMfa.setBackend(serviceDetails.backendUrl);
                        if (backendStatus.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                            mCurrentServiceDetails = serviceDetails;
                            // If the backend is set successfully, we can retrieve the session details using the access code
                            SessionDetails details = new SessionDetails();
                            return mPinMfa.getSessionDetails(mCurrentAccessCode, details);
                        } else {
                            return backendStatus;
                        }
                    } else {
                        return serviceDetailsStatus;
                    }
                }

                @Override
                protected void onPostExecute(com.miracl.mpinsdk.model.Status status) {
                    if (status != null) {
                        if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                            onBackendSet();
                        } else {
                            // The setting of backend or retrieving the session details failed
                            Toast.makeText(QrReaderActivity.this,
                              "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(),
                              Toast.LENGTH_SHORT).show();
                        }
                        // Resume scanning for qr code
                        mIsBarCodeProcessing = false;
                        startCameraPreview();
                    }
                }
            }.execute();
        } else {
            // The url does not have the expected format
            Toast.makeText(this, "Invalid qr url", Toast.LENGTH_SHORT).show();
            //Resume scanning for qr code
            mIsBarCodeProcessing = false;
            startCameraPreview();
        }
    }

    private void onBackendSet() {
        SampleApplication.setCurrentAccessCode(mCurrentAccessCode);
        new AsyncTask<Void, Void, Pair<Status, List<User>>>() {

            @Override
            protected Pair<com.miracl.mpinsdk.model.Status, List<User>> doInBackground(Void... voids) {
                MPinMFA sdk = SampleApplication.getMfaSdk();
                // Get the list of stored users in order to check if there is
                // an existing user that can be logged in
                List<User> users = new ArrayList<>();
                com.miracl.mpinsdk.model.Status listUsersStatus = sdk.listUsers(users);
                return new Pair<>(listUsersStatus, users);
            }

            @Override
            protected void onPostExecute(Pair<com.miracl.mpinsdk.model.Status, List<User>> statusUsersPair) {
                com.miracl.mpinsdk.model.Status status = statusUsersPair.first;

                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    // Filter the users for the current backend
                    Uri currentBackend = Uri.parse(mCurrentServiceDetails.backendUrl);
                    if (currentBackend != null && currentBackend.getAuthority() != null) {
                        List<User> currentBackendUsers = new ArrayList<>();
                        for (User user : statusUsersPair.second) {
                            if (user.getBackend().equalsIgnoreCase(currentBackend.getAuthority())) {
                                currentBackendUsers.add(user);
                            }
                        }

                        if (currentBackendUsers.isEmpty()) {
                            // If there are no users, we need to register a new one
                            startActivity(new Intent(QrReaderActivity.this, RegisterUserActivity.class));
                        } else {
                            // If there is a registered user start the authentication process
                            mCurrentUser = currentBackendUsers.get(0);
                            mEnterPinDialog.setTitle(mCurrentUser.getId());
                            mEnterPinDialog.show();
                        }
                    }
                } else {
                    // Listing user for the current backend failed
                    Toast.makeText(QrReaderActivity.this,
                      "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(), Toast.LENGTH_SHORT)
                      .show();
                }
            }
        }.execute();
    }

    @Override
    public void onPinEntered(final String pin) {
        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
                MPinMFA mPinMfa = SampleApplication.getMfaSdk();
                if (mCurrentUser != null && mCurrentAccessCode != null) {
                    // Start the authentication process with the scanned access code and a registered user
                    com.miracl.mpinsdk.model.Status startAuthenticationStatus = mPinMfa
                      .startAuthentication(mCurrentUser, mCurrentAccessCode);
                    if (startAuthenticationStatus.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                        // Finish the authentication with the user's pin
                        return mPinMfa.finishAuthentication(mCurrentUser, pin, mCurrentAccessCode);
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
                    Toast.makeText(QrReaderActivity.this, "Can't login right now, try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    // The authentication for the user is successful
                    Toast.makeText(QrReaderActivity.this,
                      "Successfully logged " + mCurrentUser.getId() + " with " + mCurrentUser.getBackend(), Toast.LENGTH_SHORT)
                      .show();
                } else {
                    // Authentication failed
                    Toast.makeText(QrReaderActivity.this,
                      "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(), Toast.LENGTH_SHORT)
                      .show();
                }

                // Resume scanning for qr code
                mIsBarCodeProcessing = false;
                startCameraPreview();
            }
        }.execute();
    }

    @Override
    public void onPinCanceled() {
        // Resume scanning for qr code
        mIsBarCodeProcessing = false;
        startCameraPreview();
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    private void initCameraPreview() {
        if (mCameraPreview == null) {
            initPreviewCallBack();
            mCameraPreview = new CameraPreview(this, mPreviewCallBack);
            mCameraContainer.addView(mCameraPreview);
        }

        startCameraPreview();
    }

    private void startCameraPreview() {
        if (mCameraPreview != null) {
            mCameraPreview.startPreviewCallback();
        }
    }

    private void initPreviewCallBack() {
        mPreviewCallBack = new Camera.PreviewCallback() {

            private long mLastScannedTime;

            public void onPreviewFrame(byte[] data, Camera camera) {
                if (mCameraPreview.isPreviewing()) {
                    long currentTime = System.currentTimeMillis();
                    //For optimization reasons we don't process every single frame
                    if (currentTime - mLastScannedTime > FRAME_TIMEOUT) {
                        mLastScannedTime = currentTime;
                        Camera.Size size = camera.getParameters().getPreviewSize();
                        onCameraFrame(data, size.width, size.height);
                    }
                }
            }
        };
    }

    private void initImageProcessingHandler() {
        mImageProcessingWorkerThread = new HandlerThread("Image Processing Worker Thread");
        mImageProcessingWorkerThread.start();
        mImageProcessingHandler = new Handler(mImageProcessingWorkerThread.getLooper());
    }

    private void stopCameraPreview() {
        if (mCameraPreview != null) {
            mCameraPreview.stopPreviewCallback();
        }
        mIsBarCodeProcessing = false;
        mImageProcessingHandler.removeCallbacksAndMessages(null);
    }

    private void removeCameraPreview() {
        releaseCamera();
        if (mCameraPreview != null) {
            mCameraContainer.removeView(mCameraPreview);
            mCameraPreview = null;
        }
    }

    private void releaseCamera() {
        if (mCameraPreview != null) {
            mCameraPreview.releaseCamera();
        }
    }
}
