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

import com.miracl.mpinsdk.MPinSDK;
import com.miracl.mpinsdk.model.SessionDetails;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;
import com.miracl.mpinsdksample.util.CameraPreview;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    private OkHttpClient mOkHttpClient;

    private String mCurrentAccessCode;
    private User   mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_reader);

        mCameraContainer = (FrameLayout) findViewById(R.id.qr_reader_camera_container);
        mNoCameraPermissionView = findViewById(R.id.qr_reader_no_camera_permission);

        initImageProcessingHandler();

        mScanner = new ImageScanner();
        mScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);

        mOkHttpClient = new OkHttpClient();

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

            //Create uri to  qr-read-base-url/service
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(qrUri.getAuthority()).scheme(qrUri.getScheme()).path("service");
            final Uri serviceUri = uriBuilder.build();

            // Obtain the access code from the qr-read url
            mCurrentAccessCode = qrUri.getFragment();

            // Do the network request to /service and MPinSDK session initialization on a separate thread
            new AsyncTask<Void, Void, Status>() {

                @Override
                protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
                    //TODO remove
                    Request getServiceInfoRequest = new Request.Builder().url(serviceUri.toString()).get()
                      .addHeader("User-Agent", "com.miracl.android.tcbmfa/1.1.1 (android/6.0.1) build/101").build();

                    try {
                        Response serviceResponse = mOkHttpClient.newCall(getServiceInfoRequest).execute();

                        String jsonData = serviceResponse.body().string();
                        JSONObject responseJson = new JSONObject(jsonData);

                        MPinSDK mPinSDK = SampleApplication.getSdk();
                        // Retrieve the url of the service from the response
                        String backendUrl = responseJson.getString("url");

                        // MPinSDK methods are synchronous, be sure not to call them on the ui thread
                        com.miracl.mpinsdk.model.Status backendStatus = mPinSDK.SetBackend(backendUrl);
                        if (backendStatus.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                            // If the backend is set successfully, we can retrieve the session details using the access code
                            SessionDetails details = new SessionDetails();
                            return mPinSDK.GetSessionDetails(mCurrentAccessCode, details);
                        } else {
                            return backendStatus;
                        }
                    } catch (IOException e) {
                        // Request to /service failed
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(QrReaderActivity.this, "Could not make request to " + serviceUri.toString(),
                                  Toast.LENGTH_SHORT).show();
                            }
                        });
                        return null;
                    } catch (JSONException e) {
                        // Response from /service has unexpected format
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(QrReaderActivity.this,
                                  "Unexpected format of json returned by " + serviceUri.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        return null;
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
                MPinSDK sdk = SampleApplication.getSdk();
                // Get the list of stored users for the currently set backend in order to check if there is
                // an existing user that can be logged in
                List<User> usersForCurrentBackend = new ArrayList<>();
                com.miracl.mpinsdk.model.Status listUsersStatus = sdk.ListUsers(usersForCurrentBackend);
                return new Pair<>(listUsersStatus, usersForCurrentBackend);
            }

            @Override
            protected void onPostExecute(Pair<com.miracl.mpinsdk.model.Status, List<User>> statusUsersPair) {
                com.miracl.mpinsdk.model.Status status = statusUsersPair.first;

                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    List<User> currentBackendUsers = statusUsersPair.second;

                    if (currentBackendUsers.isEmpty()) {
                        // If there are no users, we need to register a new one
                        startActivity(new Intent(QrReaderActivity.this, RegisterUserActivity.class));
                    } else {
                        // If there is a registered user start the authentication process
                        mCurrentUser = currentBackendUsers.get(0);
                        mEnterPinDialog.setTitle(mCurrentUser.getId());
                        mEnterPinDialog.show();
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
                MPinSDK mPinSDK = SampleApplication.getSdk();
                if (mCurrentUser != null && mCurrentAccessCode != null) {
                    // Start the authentication process with the scanned access code and a registered user
                    com.miracl.mpinsdk.model.Status startAuthenticationStatus = mPinSDK
                      .StartAuthentication(mCurrentUser, mCurrentAccessCode);
                    if (startAuthenticationStatus.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                        // Finish the authentication with the user's pin
                        return mPinSDK.FinishAuthenticationAN(mCurrentUser, pin, mCurrentAccessCode);
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
