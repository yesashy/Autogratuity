package com.autogratuity.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.autogratuity.MainActivity;
import com.autogratuity.R;
import com.autogratuity.receivers.CaptureProcessReceiver;
import com.autogratuity.utils.ShiptCaptureProcessor;

/**
 * Background service that periodically processes captured Shipt data
 */
public class ShiptCaptureBackgroundService extends Service {
    private static final String TAG = "CaptureService";
    private static final int NOTIFICATION_ID = 1004;
    private static final String CHANNEL_ID = "autogratuity_capture_channel";
    private static final long PROCESSING_INTERVAL_MS = 30 * 60 * 1000; // 30 minutes

    private AlarmManager alarmManager;
    private PendingIntent processingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Shipt capture service created");

        // Setup recurring alarm for processing
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, CaptureProcessReceiver.class);
        processingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Shipt capture service started");

        // Create notification channel for Android O and above
        createNotificationChannel();

        // Start as a foreground service
        startForeground(NOTIFICATION_ID, createNotification("Monitoring Shipt data..."));

        // Schedule the first processing
        scheduleProcessingAlarm();

        // Run initial processing
        processCaptures();

        // If killed, restart
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancel any pending alarms
        if (alarmManager != null && processingIntent != null) {
            alarmManager.cancel(processingIntent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Shipt Capture Service",
                    NotificationManager.IMPORTANCE_LOW); // Low importance to be less intrusive

            channel.setDescription("Processes captured Shipt data");
            channel.setShowBadge(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String contentText) {
        // Create an intent that opens the main activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Autogratuity Shipt Monitor")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_package)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority to be less intrusive
                .setOngoing(true);

        return builder.build();
    }

    private void scheduleProcessingAlarm() {
        // Schedule recurring processing
        if (alarmManager != null) {
            // First processing after 30 minutes
            alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + PROCESSING_INTERVAL_MS,
                    PROCESSING_INTERVAL_MS,
                    processingIntent);

            Log.d(TAG, "Scheduled recurring processing every " + (PROCESSING_INTERVAL_MS / 60000) + " minutes");
        }
    }

    private void processCaptures() {
        Log.d(TAG, "Processing captured Shipt data...");

        // Update notification to show we're processing
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification("Processing Shipt data..."));
        }

        // Process captures in a background thread
        new Thread(() -> {
            ShiptCaptureProcessor processor = new ShiptCaptureProcessor(this);
            processor.processCaptures(new ShiptCaptureProcessor.ProcessCallback() {
                @Override
                public void onComplete(int count) {
                    Log.d(TAG, "Processed " + count + " captures");

                    // Update notification
                    if (notificationManager != null) {
                        String message = count > 0 ?
                                "Processed " + count + " captures" :
                                "Monitoring Shipt data...";

                        notificationManager.notify(NOTIFICATION_ID, createNotification(message));
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error processing captures", e);

                    // Update notification
                    if (notificationManager != null) {
                        notificationManager.notify(NOTIFICATION_ID,
                                createNotification("Error processing Shipt data"));
                    }
                }
            });
        }).start();
    }
}