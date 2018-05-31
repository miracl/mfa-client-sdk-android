package com.miracl.mpinsdk.inappsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class EncryptionKeyExpired extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption_key_expired);
    }

    public void continueOnClick(View view) {
        startActivity(new Intent(EncryptionKeyExpired.this, LoginActivity.class));
        finish();
        return;
    }
}
