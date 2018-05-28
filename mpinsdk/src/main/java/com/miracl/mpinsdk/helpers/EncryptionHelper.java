package com.miracl.mpinsdk.helpers;

import android.app.KeyguardManager;
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
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class EncryptionHelper {

    public static final String STORAGE_AES_KEY = "STORAGE_AES_KEY";
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    public static final String SHARED_PREFERENCES_FILE_KEY = "ContextSharedPreferences";
    public static final String SHARED_PREFERENCES_IV_KEY = "IV_KEY";
    private static final String TAG = "EncryptionHelper";


    private SecretKey key;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public EncryptionHelper(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        try {
            this.key = extractKeyFromKeyStore();
            if(this.key == null) {
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
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, e.toString());
        } catch (NoSuchProviderException e) {
            Log.e(TAG, e.toString());
        }
    }

    public Boolean userIsAuthenticated() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return encrypt("dummy").status == CryptographicStatus.OK;
            } catch (NoSuchPaddingException e) {
                Log.e(TAG, e.toString());
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, e.toString());
            } catch (InvalidKeyException e) {
                Log.e(TAG, e.toString());
            } catch (IllegalBlockSizeException e) {
                Log.e(TAG, e.toString());
            } catch (BadPaddingException e) {
                Log.e(TAG, e.toString());
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.toString());
            }

        }
        //Strongly reconsider or document this logic.
        return true;
    }

    public static Boolean encryptionHelperApplicable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getEncryptedBase64Payload(String payload) {
        try {
            CryptographicResult result = encrypt(payload);
            return Base64.encodeToString(result.payload, Base64.DEFAULT);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.toString());
        } catch (InvalidKeyException e) {
            Log.e(TAG, e.toString());
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, e.toString());
        } catch (BadPaddingException e) {
            Log.e(TAG, e.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateSecureKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        final KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(STORAGE_AES_KEY,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                //This one is quite useful towards UX, but requires Android 7.0 at least
                .setUserAuthenticationValidityDurationSeconds(40)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build();
        keyGenerator.init(keyGenParameterSpec);
        this.key = keyGenerator.generateKey();
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
    public CryptographicResult encrypt(String payload) throws InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        try {
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, this.key);
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
                return new String(decryptedData, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.toString());
                return null;
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public CryptographicResult decrypt(byte[] data) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec spec = new GCMParameterSpec(128, getIV());
            cipher.init(Cipher.DECRYPT_MODE, this.key, spec);
            String decryptedPayload = new String(cipher.doFinal(data), "UTF-8");
            return new CryptographicResult(cipher.doFinal(data), CryptographicStatus.USER_NOT_AUTHENTICATED);
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
        } catch (BadPaddingException e) {
            Log.e(TAG, e.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.toString());
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, e.toString());
        }
        return new CryptographicResult(null, CryptographicStatus.ERROR);
    }

    public void resetKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        keyStore.deleteEntry(STORAGE_AES_KEY);
    }

    public byte[] getIV() {
        String encodedIv = sharedPreferences.getString(SHARED_PREFERENCES_IV_KEY, "");
        return android.util.Base64.decode(encodedIv, android.util.Base64.DEFAULT);
    }

    public void setIV(byte[] IV) {
        String encoded = Base64.encodeToString(IV, Base64.DEFAULT);
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_IV_KEY, encoded);
        sharedPreferencesEditor.commit();
    }
    public enum CryptographicStatus {
        OK, ERROR, KEY_PERMANENTLY_INVALIDATED, USER_NOT_AUTHENTICATED
    }
    public  class CryptographicResult {

        private byte[] payload;
        private CryptographicStatus status;

        public CryptographicResult(byte[] payload, CryptographicStatus status) {
            this.payload = payload;
            this.status = status;
        }

        public byte[] getPayload() {
            return payload;
        }

        public CryptographicStatus getStatus() {
            return status;
        }
    }
}
