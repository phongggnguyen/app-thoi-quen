package com.example.myhabits.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.myhabits.services.NotificationService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                "android.intent.action.QUICKBOOT.POWERON".equals(intent.getAction()) ||
                "com.htc.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {

            try {
                Log.d(TAG, "Device boot completed, rescheduling notifications");

                // Dùng startService thay vì startForegroundService
                Intent serviceIntent = new Intent(context, NotificationService.class)
                        .setAction("RESCHEDULE_NOTIFICATIONS");
                context.startService(serviceIntent);

                Log.d(TAG, "Notification service started successfully");

            } catch (Exception e) {
                Log.e(TAG, "Error starting notification service: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}