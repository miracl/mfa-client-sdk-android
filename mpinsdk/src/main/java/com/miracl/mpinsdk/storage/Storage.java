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
package com.miracl.mpinsdk.storage;


import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.miracl.mpinsdk.helpers.EncryptionHelper;
import com.miracl.mpinsdk.intent.MPinIntent;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class Storage implements IStorage {

    private static final String MPIN_STORAGE = "MpinStorage";
    private static final String USER_STORAGE = "UserStorage";
    private static final int    CHUNK_SIZE   = 255;

    private final Context mContext;
    private final String  mFileName;
    private       String  mErrorMessage;
    private EncryptionHelper encryptionHelper;

    public Storage(Context context, boolean isMpinType) {
        mContext = context.getApplicationContext();
        mFileName = isMpinType ? MPIN_STORAGE : USER_STORAGE;
        if(EncryptionHelper.encryptionHelperApplicable()) {
            encryptionHelper = new EncryptionHelper(context);
            handleKeyAuthenticationState();
        }
    }

    private EncryptionHelper.CryptographicStatus handleKeyAuthenticationState() {
        if(encryptionHelper == null) {
            return EncryptionHelper.CryptographicStatus.NOT_ACTIVATED;
        }
        EncryptionHelper.CryptographicStatus keyAuthenticationState = encryptionHelper.getKeyAuthenticationState();
        if(encryptionHelper != null && keyAuthenticationState.equals(EncryptionHelper.CryptographicStatus.USER_NOT_AUTHENTICATED)) {
            Intent userShouldAuthenticate;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                userShouldAuthenticate = new Intent(MPinIntent.USER_SHOULD_AUTHENTICATE);
                userShouldAuthenticate.setPackage(mContext.getPackageName());
                mContext.sendBroadcast(userShouldAuthenticate);
            }
        } else if(encryptionHelper != null && keyAuthenticationState.equals(EncryptionHelper.CryptographicStatus.KEY_PERMANENTLY_INVALIDATED)) {
            Intent userShouldAuthenticate;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                encryptionHelper.resetKey();
                userShouldAuthenticate = new Intent(MPinIntent.CONTENT_ENCRYPTION_KEY_PERMANENTLY_INVALIDATED);
                userShouldAuthenticate.setPackage(mContext.getPackageName());
                mContext.sendBroadcast(userShouldAuthenticate);
            }
        }
        return keyAuthenticationState;
    }


    @Override
    public boolean SetData(String data) {
        if(handleKeyAuthenticationState() == EncryptionHelper.CryptographicStatus.OK) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                data = encryptionHelper.getEncryptedBase64Payload(data);
            }
        }
        mErrorMessage = null;
        FileOutputStream fos = null;
        try {
            fos = mContext.openFileOutput(mFileName, Context.MODE_PRIVATE);
            fos.write(data.getBytes());
        } catch (IOException e) {
            mErrorMessage = e.getLocalizedMessage();
        } finally {
            if (fos == null) {
                return false;
            }
            try {
                fos.close();
            } catch (IOException e) {
                mErrorMessage = e.getLocalizedMessage();
            }
        }

        return (mErrorMessage == null);
    }


    @Override
    public String GetData() {
        String data = "";
        mErrorMessage = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            fis = mContext.openFileInput(mFileName);
            byte[] buffer = new byte[CHUNK_SIZE];
            int nbread;
            while ((nbread = fis.read(buffer, 0, CHUNK_SIZE)) > 0) {
                bos.write(buffer, 0, nbread);
            }
            data = new String(bos.toByteArray());
        } catch (IOException e) {
            mErrorMessage = e.getLocalizedMessage();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                    bos.close();
                } catch (IOException e) {
                    mErrorMessage = e.getLocalizedMessage();
                }
            }
        }
        if(handleKeyAuthenticationState() == EncryptionHelper.CryptographicStatus.OK) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && data != null && !data.equals("")) {
                data = encryptionHelper.decryptBase64EncodedPayload(data);

            }
        }
        return data;
    }


    @Override
    public String GetErrorMessage() {
        return mErrorMessage;
    }

    @Override
    public boolean ClearData() {
        boolean isDeleted = mContext.deleteFile(mFileName);
        if (isDeleted) {
            Intent dataClearedIntent = new Intent(MPinIntent.ACTION_MPIN_STORAGE_CLEARED);
            dataClearedIntent.setPackage(mContext.getPackageName());
            mContext.sendBroadcast(dataClearedIntent);
        }

        return isDeleted;
    }
}
