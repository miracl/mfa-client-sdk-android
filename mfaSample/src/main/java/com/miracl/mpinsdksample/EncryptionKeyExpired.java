package com.miracl.mpinsdksample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.miracl.mpinsdk.helpers.EncryptionHelper;

public class EncryptionKeyExpired extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption_key_expired);
    }

    public void continueOnClick(View view) {
        startActivity(new Intent(EncryptionKeyExpired.this, QrReaderActivity.class));
        finish();
        return;
    }
}
