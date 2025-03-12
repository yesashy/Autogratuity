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
    private static final String[] SHIPT_PACKAGES = {
            "com.shipt.shopper", // Shipt shopper app
            "com.shipt", // Main Shipt app
            "com.shipt.user" // Possible Shipt user app
    };

    // Updated regex patterns to match the actual notification formats
    private static final Pattern TIP_PATTERN = Pattern.compile("You got a \\$(\\d+\\.\\d+) tip for an order delivered on .+ \\(#([A-Z0-9]+)\\)");
    // Alternate patterns to catch variations
    private static final Pattern ALTERNATE_TIP_PATTERN_1 = Pattern.compile("You received a \\$(\\d+\\.\\d+) tip for order #([A-Z0-9]+)");
    private static final Pattern ALTERNATE_TIP_PATTERN_2 = Pattern.compile("Order #([A-Z0-9]+).*tipped \\$(\\d+\\.\\d+)");

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

        // Check if notification is from any of the Shipt packages
        boolean isShiptNotification = false;
        for (String shipPackage : SHIPT_PACKAGES) {
            if (shipPackage.equals(sbn.getPackageName())) {
                isShiptNotification = true;
                break;
            }
        }

        if (isShiptNotification) {
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

                // Combined text for better pattern matching
                String fullText = title + " " + text;

                Log.d(TAG, "Title: " + title);
                Log.d(TAG, "Text: " + text);
                Log.d(TAG, "Full text for matching: " + fullText);

                // Check if this is a tip notification
                if (title.contains("tip") || text.contains("tip") ||
                        title.contains("Tip") || text.contains("Tip")) {

                    String orderId = null;
                    String tipAmountStr = null;
                    boolean matched = false;

                    // Try all patterns
                    Matcher matcher = TIP_PATTERN.matcher(fullText);
                    if (matcher.find()) {
                        tipAmountStr = matcher.group(1);
                        orderId = matcher.group(2);
                        matched = true;
                        Log.d(TAG, "Matched main pattern");
                    }

                    if (!matched) {
                        matcher = ALTERNATE_TIP_PATTERN_1.matcher(fullText);
                        if (matcher.find()) {
                            tipAmountStr = matcher.group(1);
                            orderId = matcher.group(2);
                            matched = true;
                            Log.d(TAG, "Matched alternate pattern 1");
                        }
                    }

                    if (!matched) {
                        matcher = ALTERNATE_TIP_PATTERN_2.matcher(fullText);
                        if (matcher.find()) {
                            orderId = matcher.group(1);
                            tipAmountStr = matcher.group(2);
                            matched = true;
                            Log.d(TAG, "Matched alternate pattern 2");
                        }
                    }

                    // If no pattern matched but contains both "tip" and "#", try to extract from text
                    if (!matched && text.contains("tip") && text.contains("#")) {
                        Log.d(TAG, "Attempting to extract data from unmatched pattern");
                        // Extract the order ID (anything after # until a non-alphanumeric character)
                        Pattern orderPattern = Pattern.compile("#([A-Z0-9]+)");
                        Matcher orderMatcher = orderPattern.matcher(text);
                        if (orderMatcher.find()) {
                            orderId = orderMatcher.group(1);
                        }

                        // Extract the tip amount (any dollar amount)
                        Pattern amountPattern = Pattern.compile("\\$(\\d+\\.\\d+)");
                        Matcher amountMatcher = amountPattern.matcher(text);
                        if (amountMatcher.find()) {
                            tipAmountStr = amountMatcher.group(1);
                        }

                        if (orderId != null && tipAmountStr != null) {
                            matched = true;
                            Log.d(TAG, "Extracted data from unmatched pattern");
                        }
                    }

                    if (matched && orderId != null && tipAmountStr != null) {
                        // Process the tip information
                        try {
                            double tipAmount = Double.parseDouble(tipAmountStr);
                            Log.d(TAG, "Successfully parsed tip: $" + tipAmount + " for order #" + orderId);
                            processTip(orderId, tipAmount);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing tip amount: " + tipAmountStr, e);
                        }
                    } else {
                        Log.d(TAG, "No matching pattern found in tip notification");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing notification", e);
            }
        }
    }

    private void processTip(final String orderId, final double tipAmount) {
        if (orderId == null || db == null) return;

        Log.d(TAG, "Processing tip: Order #" + orderId + ", Amount: $" + tipAmount);

        // Store this tip in the pending tips collection
        Map<String, Object> pendingTip = new HashMap<>();
        pendingTip.put("order_id", orderId);
        pendingTip.put("tip_amount", tipAmount);
        pendingTip.put("timestamp", Timestamp.now());

        db.collection("pending_tips")
                .add(pendingTip)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Pending tip stored with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error storing pending tip", e);
                });
    }
}