package com.ape.backtouch.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ape.backtouch.event.BtEventManager;

/**
 * Created by linzhiqin on 9/19/16.
 */
public class BtService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        BtEventManager.getInsance(this).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
