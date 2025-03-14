package com.autogratuity;

import android.app.Notification;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.os.Bundle;

import com.autogratuity.repositories.IFirestoreRepository;
import com.autogratuity.repositories.FirestoreRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShiptNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "ShiptNotifListener";

    // Default package patterns
    private String[] shiptPackages = {
            "com.shipt.shopper", // Shipt shopper app
            "com.shipt", // Main Shipt app
            "com.shipt.user", // Possible Shipt user app
            "com.shipt.consumer", // Another possible variant
            "shipt.test" // For testing purposes
    };

    // Enhanced set of regex patterns for tip notifications
    private String[] tipPatterns = {
            "You got a \\$(\\d+\\.\\d+) tip for an order delivered on .+ \\(#([A-Z0-9]+)\\)",
            "You received a \\$(\\d+\\.\\d+) tip for order #([A-Z0-9]+)",
            "Order #([A-Z0-9]+).*tipped \\$(\\d+\\.\\d+)",
            "([A-Z0-9]+).*tipped you \\$(\\d+\\.\\d+)",
            "Your customer left a \\$(\\d+\\.\\d+) tip.*#([A-Z0-9]+)",
            "You've received a \\$(\\d+\\.\\d+) tip.*([A-Z0-9]+)",
            "\\$(\\d+\\.\\d+) tip.*order.*([A-Z0-9]+)",
            "New tip.*\\$(\\d+\\.\\d+).*#([A-Z0-9]+)"
    };

    // Generic patterns to extract order IDs
    private String[] orderIdPatterns = {
            "#([A-Z0-9]+)",
            "Order ([A-Z0-9]+)",
            "order ([A-Z0-9]+)",
            "\\(#([A-Z0-9]+)\\)",
            "ID\\s*[:#]?\\s*([A-Z0-9]+)",
            "([A-Z0-9]{8,12})" // Most Shipt order IDs are 8-12 alphanumeric chars
    };

    // Compiled patterns cache
    private final Map<String, Pattern> compiledPatterns = new HashMap<>();
    private IFirestoreRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        repository = FirestoreRepository.getInstance();

        // Load any saved custom patterns
        loadCustomPatterns();

        Log.d(TAG, "Notification listener service created");
    }

    private void loadCustomPatterns() {
        // Load saved patterns from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("notification_patterns", MODE_PRIVATE);
        String savedPackages = prefs.getString("shipt_packages", null);
        String savedTipPatterns = prefs.getString("tip_patterns", null);
        String savedOrderPatterns = prefs.getString("order_patterns", null);

        if (savedPackages != null && !savedPackages.isEmpty()) {
            shiptPackages = savedPackages.split(",");
        }

        if (savedTipPatterns != null && !savedTipPatterns.isEmpty()) {
            tipPatterns = savedTipPatterns.split("\\|\\|");
            compiledPatterns.clear();
        }

        if (savedOrderPatterns != null && !savedOrderPatterns.isEmpty()) {
            orderIdPatterns = savedOrderPatterns.split("\\|\\|");
            compiledPatterns.clear();
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getPackageName() == null) return;

        // Check if notification is from any of the Shipt packages
        boolean isShiptNotification = isShiptPackage(sbn.getPackageName());

        if (isShiptNotification) {
            try {
                Log.d(TAG, "Shipt notification received from: " + sbn.getPackageName());

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

                // Save this notification for later analysis
                saveNotificationForAnalysis(sbn.getPackageName(), title, text);

                Log.d(TAG, "Processing notification - Title: " + title);
                Log.d(TAG, "Processing notification - Text: " + text);

                // Check if this contains tip information using multiple patterns
                Map<String, String> extractedData = extractDataFromNotification(fullText);

                String orderId = extractedData.get("orderId");
                String tipAmountStr = extractedData.get("tipAmount");

                if (orderId != null && tipAmountStr != null) {
                    try {
                        double tipAmount = Double.parseDouble(tipAmountStr);
                        Log.d(TAG, "Successfully parsed tip: $" + tipAmount + " for order #" + orderId);
                        processTip(orderId, tipAmount);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing tip amount: " + tipAmountStr, e);
                    }
                } else {
                    Log.d(TAG, "No matching pattern found in notification");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing notification", e);
            }
        }
    }

    private boolean isShiptPackage(String packageName) {
        for (String shiptPackage : shiptPackages) {
            if (shiptPackage.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> extractDataFromNotification(String fullText) {
        Map<String, String> data = new HashMap<>();

        // Try all tip patterns first
        for (String patternStr : tipPatterns) {
            Pattern pattern = getCompiledPattern(patternStr);
            Matcher matcher = pattern.matcher(fullText);

            if (matcher.find()) {
                try {
                    if (matcher.groupCount() >= 2) {
                        // Most patterns have tipAmount as group 1, orderId as group 2
                        data.put("tipAmount", matcher.group(1));
                        data.put("orderId", matcher.group(2));
                    } else if (matcher.groupCount() == 1) {
                        // Some patterns might just have one group - try to detect what it is
                        String match = matcher.group(1);
                        if (match.matches("\\d+\\.\\d+")) {
                            data.put("tipAmount", match);
                        } else {
                            data.put("orderId", match);
                        }
                    }

                    // If we found both, return immediately
                    if (data.containsKey("tipAmount") && data.containsKey("orderId")) {
                        return data;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error extracting data from match: " + e.getMessage());
                }
            }
        }

        // If we still need orderId, try specific patterns
        if (!data.containsKey("orderId")) {
            for (String patternStr : orderIdPatterns) {
                Pattern pattern = getCompiledPattern(patternStr);
                Matcher matcher = pattern.matcher(fullText);

                if (matcher.find() && matcher.groupCount() >= 1) {
                    data.put("orderId", matcher.group(1));
                    break;
                }
            }
        }

        // If we still need tipAmount, try to find any dollar amount
        if (!data.containsKey("tipAmount")) {
            Pattern dollarPattern = getCompiledPattern("\\$(\\d+\\.\\d+)");
            Matcher matcher = dollarPattern.matcher(fullText);

            if (matcher.find() && matcher.groupCount() >= 1) {
                data.put("tipAmount", matcher.group(1));
            }
        }

        return data;
    }

    private Pattern getCompiledPattern(String patternStr) {
        if (!compiledPatterns.containsKey(patternStr)) {
            compiledPatterns.put(patternStr, Pattern.compile(patternStr));
        }
        return compiledPatterns.get(patternStr);
    }

    private void saveNotificationForAnalysis(String packageName, String title, String text) {
        // Save notification content to SharedPreferences for later analysis
        SharedPreferences prefs = getSharedPreferences("notification_analysis", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Create unique key based on timestamp
        String key = "notification_" + System.currentTimeMillis();

        editor.putString(key + "_package", packageName);
        editor.putString(key + "_title", title);
        editor.putString(key + "_text", text);
        editor.apply();

        // Limit number of stored notifications to 50
        int count = prefs.getInt("notification_count", 0);
        if (count > 50) {
            // Remove oldest notifications - simplified implementation
            editor.putInt("notification_count", 1);
            editor.apply();
        } else {
            editor.putInt("notification_count", count + 1);
            editor.apply();
        }
    }

    private void processTip(final String orderId, final double tipAmount) {
        if (orderId == null) return;

        Log.d(TAG, "Processing tip: Order #" + orderId + ", Amount: $" + tipAmount);

        // Find the delivery matching this order ID using repository
        repository.findDeliveryByOrderId(orderId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No matching delivery found, save as pending tip
                        repository.storePendingTip(orderId, tipAmount)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Stored pending tip for Order #" + orderId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error storing pending tip", e);
                                });
                        return;
                    }

                    // Update existing delivery with the tip information
                    String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    repository.updateDeliveryWithTip(documentId, tipAmount)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Successfully updated delivery with tip");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating delivery with tip", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding delivery for tip", e);
                    repository.storePendingTip(orderId, tipAmount);
                });
    }
}