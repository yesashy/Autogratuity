package com.autogratuity;

import android.app.Notification;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.os.Bundle;

import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.data.repository.preference.PreferenceRepository;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.Single;
import io.reactivex.Completable;

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
    private DeliveryRepository deliveryRepository;
    private PreferenceRepository preferenceRepository;
    private CompositeDisposable disposables;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize repositories
        if (RepositoryProvider.isInitialized()) {
            deliveryRepository = RepositoryProvider.getDeliveryRepository();
            preferenceRepository = RepositoryProvider.getPreferenceRepository();
        } else {
            Log.e(TAG, "RepositoryProvider not initialized, service may not function correctly");
            // Initialize RepositoryProvider if possible, otherwise service will be limited
            try {
                RepositoryProvider.initialize(getApplicationContext());
                deliveryRepository = RepositoryProvider.getDeliveryRepository();
                preferenceRepository = RepositoryProvider.getPreferenceRepository();
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize repositories", e);
            }
        }
        
        // Initialize CompositeDisposable
        disposables = new CompositeDisposable();

        // Load any saved custom patterns
        loadCustomPatterns();

        Log.d(TAG, "Notification listener service created");
    }

    private void loadCustomPatterns() {
        if (preferenceRepository != null) {
            // Load saved patterns using PreferenceRepository
            disposables.add(
                preferenceRepository.getPreferenceSetting("shipt_packages", "")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(savedPackages -> {
                        if (savedPackages != null && !savedPackages.isEmpty()) {
                            shiptPackages = savedPackages.split(",");
                        }
                    }, throwable -> {
                        Log.e(TAG, "Error loading shipt packages", throwable);
                    })
            );
            
            disposables.add(
                preferenceRepository.getPreferenceSetting("tip_patterns", "")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(savedTipPatterns -> {
                        if (savedTipPatterns != null && !savedTipPatterns.isEmpty()) {
                            tipPatterns = savedTipPatterns.split("\\|\\|");
                            compiledPatterns.clear();
                        }
                    }, throwable -> {
                        Log.e(TAG, "Error loading tip patterns", throwable);
                    })
            );
            
            disposables.add(
                preferenceRepository.getPreferenceSetting("order_patterns", "")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(savedOrderPatterns -> {
                        if (savedOrderPatterns != null && !savedOrderPatterns.isEmpty()) {
                            orderIdPatterns = savedOrderPatterns.split("\\|\\|");
                            compiledPatterns.clear();
                        }
                    }, throwable -> {
                        Log.e(TAG, "Error loading order patterns", throwable);
                    })
            );
        } else {
            // Fallback to SharedPreferences if repository is not available
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
    
    @Override
    public void onDestroy() {
        // Dispose of all subscriptions to prevent memory leaks
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
        
        super.onDestroy();
    }

    private void saveNotificationForAnalysis(String packageName, String title, String text) {
        if (preferenceRepository != null) {
            // Create unique key based on timestamp
            String key = "notification_" + System.currentTimeMillis();
            
            // Store notification data using PreferenceRepository
            disposables.add(
                preferenceRepository.setPreferenceSetting(key + "_package", packageName)
                    .andThen(preferenceRepository.setPreferenceSetting(key + "_title", title))
                    .andThen(preferenceRepository.setPreferenceSetting(key + "_text", text))
                    .andThen(preferenceRepository.getPreferenceSetting("notification_count", 0))
                    .flatMapCompletable(count -> {
                        int newCount = count + 1;
                        if (newCount > 50) {
                            // Limit number of stored notifications to 50
                            return preferenceRepository.setPreferenceSetting("notification_count", 1);
                        } else {
                            return preferenceRepository.setPreferenceSetting("notification_count", newCount);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        () -> Log.d(TAG, "Notification saved for analysis"),
                        throwable -> Log.e(TAG, "Error saving notification for analysis", throwable)
                    )
            );
        } else {
            // Fallback to old SharedPreferences method if repository is not available
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
    }

    private void processTip(final String orderId, final double tipAmount) {
        if (orderId == null) return;

        Log.d(TAG, "Processing tip: Order #" + orderId + ", Amount: $" + tipAmount);

        if (deliveryRepository == null) {
            Log.e(TAG, "Repository not initialized, cannot process tip");
            return;
        }

        // Find delivery by order ID
        disposables.add(
            findDeliveryByOrderId(orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    delivery -> {
                        // Found the delivery, update it with the tip
                        updateDeliveryWithTip(delivery.getDeliveryId(), tipAmount);
                    },
                    throwable -> {
                        // No matching delivery found, save as pending tip
                        Log.d(TAG, "No matching delivery found for Order #" + orderId + ", storing as pending tip");
                        disposables.add(
                            storePendingTip(orderId, tipAmount)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    () -> Log.d(TAG, "Stored pending tip for Order #" + orderId),
                                    error -> Log.e(TAG, "Error storing pending tip", error)
                                )
                        );
                    }
                )
        );
    }
    
    /**
     * Find a delivery by its order ID
     * @param orderId The order ID to search for
     * @return Single that emits the found delivery or an error if not found
     */
    private Single<Delivery> findDeliveryByOrderId(String orderId) {
        if (deliveryRepository == null) {
            return Single.error(new IllegalStateException("Repository not initialized"));
        }
        
        return deliveryRepository.getAllDeliveries()
            .flatMap(deliveries -> {
                for (Delivery delivery : deliveries) {
                    if (delivery.getReference() != null && 
                        orderId.equals(delivery.getReference().getOrderId())) {
                        return Single.just(delivery);
                    }
                }
                return Single.error(new Exception("Delivery not found"));
            });
    }
    
    /**
     * Store a pending tip for later processing
     * @param orderId The order ID
     * @param tipAmount The tip amount
     * @return Completable that completes when the tip is stored
     */
    private Completable storePendingTip(String orderId, double tipAmount) {
        if (preferenceRepository == null) {
            return Completable.error(new IllegalStateException("Repository not initialized"));
        }
        
        // Store in preferences with a prefix to identify pending tips
        String key = "pending_tip_" + orderId;
        return preferenceRepository.setPreferenceSetting(key, tipAmount);
    }
    
    /**
     * Update a delivery with a tip amount
     * @param deliveryId The delivery ID
     * @param tipAmount The tip amount
     */
    private void updateDeliveryWithTip(String deliveryId, double tipAmount) {
        if (deliveryRepository == null) {
            Log.e(TAG, "Repository not initialized, cannot update delivery");
            return;
        }
        
        disposables.add(
            deliveryRepository.updateDeliveryTip(deliveryId, tipAmount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> Log.d(TAG, "Successfully updated delivery with tip"),
                    throwable -> Log.e(TAG, "Error updating delivery with tip", throwable)
                )
        );
    }
}