package com.miracl.mpinsdksample.util;

import java.util.Observable;

public class StorageAuthenticationBroadcastObserver extends Observable {
    private static StorageAuthenticationBroadcastObserver instance = new StorageAuthenticationBroadcastObserver();

    public static StorageAuthenticationBroadcastObserver getInstance() {
        return instance;
    }

    private StorageAuthenticationBroadcastObserver() {
    }

    public void change(String action) {
        synchronized (this) {
            setChanged();
            notifyObservers(action);
        }
    }
}