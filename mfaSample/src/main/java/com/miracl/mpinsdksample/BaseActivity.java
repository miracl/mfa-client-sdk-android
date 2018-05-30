package com.miracl.mpinsdksample;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.Status;

import java.util.Observable;
import java.util.Observer;

public class BaseActivity extends AppCompatActivity implements Observer {

    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;
    public static final String USER_SHOULD_AUTHENTICATE = "com.miracl.mpinsdk.intent.USER_SHOULD_AUTHENTICATE";
    public static final String CONTENT_ENCRYPTION_KEY_PERMANENTLY_INVALIDATED = "com.miracl.mpinsdk.intent.CONTENT_ENCRYPTION_KEY_PERMANENTLY_INVALIDATED";
    private int authenticationRequests;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void update(Observable o, Object arg) {
        if (arg == null) {
            return;
        }
        switch (arg.toString()) {
            case USER_SHOULD_AUTHENTICATE:
                showAuthenticationScreen();
                break;
            case CONTENT_ENCRYPTION_KEY_PERMANENTLY_INVALIDATED:
                SampleApplication.getMfaSdk().init(this, getString(R.string.mpin_cid), null, new MPinMfaAsync.Callback<Void>() {
                    @Override
                    protected void onResult(@NonNull Status status, @Nullable Void result) {
                        super.onResult(status, result);
                        startActivity(new Intent(BaseActivity.this, EncryptionKeyExpired.class));
                        finish();
                        return;
                    }
                });
                break;
        }

    }

    private void showAuthenticationScreen() {
        if(this.authenticationRequests == 0) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            Intent intent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? keyguardManager.createConfirmDeviceCredentialIntent(null, null) : null;
            boolean authenticateBeforeDecrypt = ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && keyguardManager.isDeviceSecure())
                    || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && keyguardManager.isKeyguardSecure()))
                    && intent != null;
            if (authenticateBeforeDecrypt) {
                startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
                synchronized (this) {
                    this.authenticationRequests++;
                }
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            } else {
                SampleApplication.getMfaSdk().init(this, getString(R.string.mpin_cid), null, new MPinMfaAsync.Callback<Void>() {
                    @Override
                    protected void onResult(@NonNull Status status, @Nullable Void result) {
                        super.onResult(status, result);
                    }
                });
            }
            synchronized (this) {
                this.authenticationRequests = 0;
            }
        }
    }
}
