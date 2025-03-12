package com.autogratuity;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.os.Bundle;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShiptNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "ShiptNotifListener";
    private static final String SHIPT_PACKAGE = "com.shipt.shopper";

    // Regex patterns to extract Order ID and tip amount
    private static final Pattern TIP_PATTERN = Pattern.compile("You received a (\\$[0-9.]+) tip for order #([A-Z0-9]+)");
    private static final Pattern ALTERNATE_TIP_PATTERN = Pattern.compile("Order #([A-Z0-9]+).*tipped (\\$[0-9.]+)");

    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Notification listener service created");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getPackageName() == null) return;

        // Only process Shipt notifications
        if (SHIPT_PACKAGE.equals(sbn.getPackageName())) {
            try {
                Log.d(TAG, "Shipt notification received");

                Notification notification = sbn.getNotification();
                if (notification == null) return;

                Bundle extras = notification.extras;
                if (extras == null) return;

                String title = extras.getString(Notification.EXTRA_TITLE, "");
                String text = extras.getString(Notification.EXTRA_TEXT, "");

                if (title == null) title = "";
                if (text == null) text = "";

                Log.d(TAG, "Title: " + title);
                Log.d(TAG, "Text: " + text);

                // Check if this is a tip notification
                if (title.contains("Tip") || text.contains("tip") || text.contains("tipped")) {
                    String orderId = null;
                    String tipAmountStr = null;

                    // Try to extract using the first pattern
                    Matcher matcher = TIP_PATTERN.matcher(text);
                    if (matcher.find()) {
                        tipAmountStr = matcher.group(1);
                        orderId = matcher.group(2);
                    } else {
                        // Try alternate pattern
                        matcher = ALTERNATE_TIP_PATTERN.matcher(text);
                        if (matcher.find()) {
                            orderId = matcher.group(1);
                            tipAmountStr = matcher.group(2);
                        }
                    }

                    if (orderId != null && tipAmountStr != null) {
                        // Remove $ from tip amount and convert to double
                        String cleanTipAmount = tipAmountStr.replace("$", "");
                        try {
                            double tipAmount = Double.parseDouble(cleanTipAmount);
                            // Process the tip information
                            processTip(orderId, tipAmount);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing tip amount: " + cleanTipAmount, e);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing notification", e);
            }
        }
    }