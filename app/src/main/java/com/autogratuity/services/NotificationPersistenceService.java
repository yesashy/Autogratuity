package com.autogratuity.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.autogratuity.MainActivity;
import com.autogratuity.R;

/**
 * A foreground service to ensure our NotificationListenerService stays alive
 */
public class NotificationPersistenceService extends Service {
    private static final String TAG = "NotificationPersistence";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "autogratuity_persistence_channel";
    private static final long CHECK_INTERVAL_MS = 30 * 60 * 1000; // 30 minutes

    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Persistence service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Persistence service started");

        if (!isRunning) {
            // Create notification channel for Android O and above
            createNotificationChannel();

            // Start as a foreground service
            startForeground(NOTIFICATION_ID, createNotification());

            // Ensure notification service is enabled
            ensureNotificationListenerEnabled();

            isRunning = true;
        }

        // If killed, restart
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Autogratuity Service",
                    NotificationManager.IMPORTANCE_MIN); // Minimum importance to be less intrusive

            channel.setDescription("Keeps Autogratuity running to capture tip notifications");
            channel.setShowBadge(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        // Create an intent that opens the main activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Autogratuity Active")
                .setContentText("Monitoring for Shipt tip notifications")
                .setSmallIcon(R.drawable.ic_package)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MIN) // Minimum priority
                .setOngoing(true);

        return builder.build();
    }

    private void ensureNotificationListenerEnabled() {
        // Check if notification access is granted
        String enabledListeners = Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners");

        if (enabledListeners == null || !enabledListeners.contains(getPackageName())) {
            Log.w(TAG, "Notification listener service is not enabled");
            // Cannot programmatically enable this, user must do it manually
        } else {
            Log.d(TAG, "Notification listener service is enabled");
        }
    }
}