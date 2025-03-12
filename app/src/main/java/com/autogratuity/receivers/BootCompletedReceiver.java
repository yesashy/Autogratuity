package com.autogratuity.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.autogratuity.services.NotificationPersistenceService;
import com.autogratuity.services.ShiptCaptureBackgroundService;
import com.autogratuity.utils.SubscriptionManager;

/**
 * Receiver that starts our services when the device boots up
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, starting services");

            // Always start notification service (available to all users)
            Intent notificationService = new Intent(context, NotificationPersistenceService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(notificationService);
            } else {
                context.startService(notificationService);
            }

            // Only start pro service if user has pro access
            SubscriptionManager subscriptionManager = SubscriptionManager.getInstance(context);
            if (subscriptionManager.isProUser()) {
                Intent captureService = new Intent(context, ShiptCaptureBackgroundService.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(captureService);
                } else {
                    context.startService(captureService);
                }
            }
        }
    }
}