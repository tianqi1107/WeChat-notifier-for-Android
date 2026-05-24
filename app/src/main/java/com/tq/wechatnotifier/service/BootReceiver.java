package com.tq.wechatnotifier.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.tq.wechatnotifier.util.PrefsManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            PrefsManager prefs = new PrefsManager(context);
            if (prefs.isServiceEnabled()) {
                Intent serviceIntent = new Intent(context, KeepAliveService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }
    }
}
