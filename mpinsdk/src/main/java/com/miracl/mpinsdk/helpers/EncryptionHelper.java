package com.miracl.mpinsdk.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class EncryptionHelper {

    private static final String STORAGE_AES_KEY = "STORAGE_AES_KEY";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String SHARED_PREFERENCES_FILE_KEY = "ContextSharedPreferences";
    private static final String SHARED_PREFERENCES_IV_KEY = "IV_KEY";
    private static final String TAG = "EncryptionHelper";


    private SharedPreferences.Editor sharedPreferencesEditor;
    private SharedPreferences sharedPreferences;

    @SuppressLint("CommitPrefEdits")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public EncryptionHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        try {
            SecretKey key = extractKeyFromKeyStore();
            if(key == null) {
                generateSecureKey();
            }
        } catch (KeyStoreException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.toString());
        } catch (CertificateException e) {
            Log.e(TAG, e.toString());
        } catch (UnrecoverableEntryException e) {
            Log.e(TAG, e.toString());
        }
    }

    public CryptographicStatus getKeyAuthenticationState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return encrypt("dummy").getStatus();
        }
        return CryptographicStatus.NOT_SUPPORTED;
    }

    public static Boolean encryptionHelperApplicable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getEncryptedBase64Payload(String payload) {
        CryptographicResult result = encrypt(payload);
        if(result.getStatus() == CryptographicStatus.KEY_PERMANENTLY_INVALIDATED) {
            resetKey();
            generateSecureKey();
        }
        return Base64.encodeToString(result.payload, Base64.DEFAULT);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateSecureKey() {
        final KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(STORAGE_AES_KEY,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    //This one is quite useful towards UX, but requires Android 7.0 at least
                    .setUserAuthenticationValidityDurationSeconds(600)
                    .setUserAuthenticationRequired(true)
                    .setRandomizedEncryptionRequired(false)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build();
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            setIV(iv);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.toString());
        } catch (NoSuchProviderException e) {
            Log.e(TAG, e.toString());
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, e.toString());
        }
    }
    private SecretKey extractKeyFromKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
                .getEntry(STORAGE_AES_KEY, null);
        if(secretKeyEntry == null) {
            return null;
        }
        return secretKeyEntry.getSecretKey();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private CryptographicResult encrypt(String payload) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, extractKeyFromKeyStore(), new GCMParameterSpec(128, getIV()));
            byte[] encryptedPayload = cipher.doFinal(payload.getBytes("UTF-8"));
            this.setIV(cipher.getIV());
            return new CryptographicResult(encryptedPayload, CryptographicStatus.OK);
        } catch (UserNotAuthenticatedException e) {
            return new CryptographicResult(null, CryptographicStatus.USER_NOT_AUTHENTICATED);
        } catch (KeyPermanentlyInvalidatedException e) {
            return new CryptographicResult(null, CryptographicStatus.KEY_PERMANENTLY_INVALIDATED);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return new CryptographicResult(null, CryptographicStatus.ERROR);
        }
    }

    public String decryptBase64EncodedPayload(String payload)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            byte[] decryptedData = decrypt(Base64.decode(payload, Base64.DEFAULT)).payload;
            try {
                if(decryptedData == null) {
                    return "";
                }
                return new String(decryptedData, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.toString());
            }
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private CryptographicResult decrypt(byte[] data) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec spec = new GCMParameterSpec(128, getIV());
            cipher.init(Cipher.DECRYPT_MODE, extractKeyFromKeyStore(), spec);
            return new CryptographicResult(cipher.doFinal(data), CryptographicStatus.OK);
        } catch (UserNotAuthenticatedException e) {
            return new CryptographicResult(null, CryptographicStatus.USER_NOT_AUTHENTICATED);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.toString());
        } catch (InvalidKeyException e) {
            Log.e(TAG, e.toString());
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, e.toString());
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, e.toString());
        } catch (AEADBadTagException e) {
            Log.e(TAG, e.toString());
        } catch (BadPaddingException e) {
            Log.e(TAG, e.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.toString());
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, e.toString());
        } catch (CertificateException e) {
            Log.e(TAG, e.toString());
        } catch (KeyStoreException e) {
            Log.e(TAG, e.toString());
        } catch (UnrecoverableEntryException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        return new CryptographicResult(null, CryptographicStatus.ERROR);
    }

    public void resetKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            keyStore.deleteEntry(STORAGE_AES_KEY);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.toString());
        } catch (CertificateException e) {
            Log.e(TAG, e.toString());
        } catch (KeyStoreException e) {
            Log.e(TAG, e.toString());
        }
    }

    private byte[] getIV() {
        String encodedIv = sharedPreferences.getString(SHARED_PREFERENCES_IV_KEY, "");
        return android.util.Base64.decode(encodedIv, android.util.Base64.DEFAULT);
    }

    private void setIV(byte[] IV) {
        String encoded = Base64.encodeToString(IV, Base64.DEFAULT);
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_IV_KEY, encoded);
        sharedPreferencesEditor.commit();
    }
    public enum CryptographicStatus {
        OK, ERROR, NOT_SUPPORTED, NOT_ACTIVATED, KEY_PERMANENTLY_INVALIDATED, USER_NOT_AUTHENTICATED
    }
    public class CryptographicResult {

        private byte[] payload;
        private CryptographicStatus status;

        CryptographicResult(byte[] payload, CryptographicStatus status) {
            this.payload = payload;
            this.status = status;
        }

        public CryptographicStatus getStatus() {
            return status;
        }
    }
}
