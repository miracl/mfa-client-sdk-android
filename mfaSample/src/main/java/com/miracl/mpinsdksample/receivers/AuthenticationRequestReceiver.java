package com.miracl.mpinsdksample.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.miracl.mpinsdksample.util.StorageAuthenticationBroadcastObserver;

public class AuthenticationRequestReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        StorageAuthenticationBroadcastObserver.getInstance().change(intent.getAction());
    }
}
