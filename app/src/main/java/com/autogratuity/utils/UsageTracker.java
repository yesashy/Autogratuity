package com.autogratuity.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks usage for freemium limitations
 */
public class UsageTracker {
    private static final String TAG = "UsageTracker";

    // Constants
    public static final int FREE_TIER_MAPPING_LIMIT = 100;
    private static final String PREF_NAME = "autogratuity_usage";
    private static final String PREF_KEY_MAPPING_COUNT = "mapping_count";

    // Singleton instance
    private static UsageTracker instance;

    // Dependencies
    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final SubscriptionManager subscriptionManager;

    // Cached values
    private int cachedMappingCount = -1; // -1 indicates not loaded yet
    private UsageUpdateListener listener;

    private UsageTracker(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.subscriptionManager = SubscriptionManager.getInstance(context);
    }

    /**
     * Get singleton instance
     */
    public static synchronized UsageTracker getInstance(Context context) {
        if (instance == null) {
            instance = new UsageTracker(context);
        }
        return instance;
    }

    /**
     * Set a listener for usage updates
     */
    public void setListener(UsageUpdateListener listener) {
        this.listener = listener;

        // If we already have cached values, notify the listener
        if (cachedMappingCount >= 0) {
            notifyListener();
        }
    }

    /**
     * Get the current mapping count
     * This will load from Firestore if needed
     */
    public void loadUsageData(final UsageDataCallback callback) {
        // If we're a pro user, no need to count
        if (subscriptionManager.isProUser()) {
            cachedMappingCount = 0; // Pro users don't have a count
            if (callback != null) {
                callback.onDataLoaded(0, true);
            }
            notifyListener();
            return;
        }

        // If we have cached values and not forcing refresh, use them
        if (cachedMappingCount >= 0) {
            if (callback != null) {
                callback.onDataLoaded(cachedMappingCount, false);
            }
            return;
        }

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user logged in");
            if (callback != null) {
                callback.onError(new Exception("No user logged in"));
            }
            return;
        }

        // First try to load from Firestore
        db.collection("user_usage")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("mappingCount")) {
                        // Get count from Firestore
                        Long count = documentSnapshot.getLong("mappingCount");
                        if (count != null) {
                            cachedMappingCount = count.intValue();

                            // Store in local storage for offline access
                            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                            prefs.edit().putInt(PREF_KEY_MAPPING_COUNT, cachedMappingCount).apply();

                            // Notify caller
                            if (callback != null) {
                                callback.onDataLoaded(cachedMappingCount, false);
                            }

                            // Notify listener
                            notifyListener();
                            return;
                        }
                    }

                    // If no data in Firestore, try local storage
                    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    cachedMappingCount = prefs.getInt(PREF_KEY_MAPPING_COUNT, 0);

                    // Create document in Firestore
                    Map<String, Object> usage = new HashMap<>();
                    usage.put("mappingCount", cachedMappingCount);
                    usage.put("userId", currentUser.getUid());
                    usage.put("email", currentUser.getEmail());
                    usage.put("lastUpdated", System.currentTimeMillis());

                    db.collection("user_usage")
                            .document(currentUser.getUid())
                            .set(usage)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Created usage document in Firestore");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating usage document", e);
                            });

                    // Notify caller
                    if (callback != null) {
                        callback.onDataLoaded(cachedMappingCount, false);
                    }

                    // Notify listener
                    notifyListener();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading usage data", e);

                    // Try local storage as backup
                    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    cachedMappingCount = prefs.getInt(PREF_KEY_MAPPING_COUNT, 0);

                    // Notify caller
                    if (callback != null) {
                        callback.onDataLoaded(cachedMappingCount, false);
                    }

                    // Notify listener
                    notifyListener();
                });
    }

    /**
     * Check if user can add more mappings
     */
    public boolean canAddMapping() {
        // Pro users can always add
        if (subscriptionManager.isProUser()) {
            return true;
        }

        // Load data if needed
        if (cachedMappingCount < 0) {
            // If data isn't loaded yet, be cautious and allow
            // loadUsageData() will update later
            return true;
        }

        // Check against limit
        return cachedMappingCount < FREE_TIER_MAPPING_LIMIT;
    }

    /**
     * Get remaining mappings for free tier
     */
    public int getRemainingMappings() {
        if (subscriptionManager.isProUser()) {
            return Integer.MAX_VALUE;
        }

        if (cachedMappingCount < 0) {
            return FREE_TIER_MAPPING_LIMIT; // Default if not loaded yet
        }

        return Math.max(0, FREE_TIER_MAPPING_LIMIT - cachedMappingCount);
    }

    /**
     * Record a new mapping
     */
    public void recordMapping() {
        // Pro users don't count mappings
        if (subscriptionManager.isProUser()) {
            return;
        }

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user logged in");
            return;
        }

        // Increment local cache
        if (cachedMappingCount < 0) {
            cachedMappingCount = 1;
        } else {
            cachedMappingCount++;
        }

        // Update local storage
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_KEY_MAPPING_COUNT, cachedMappingCount).apply();

        // Update Firestore
        DocumentReference userUsageRef = db.collection("user_usage")
                .document(currentUser.getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("mappingCount", cachedMappingCount);
        updates.put("lastUpdated", System.currentTimeMillis());

        userUsageRef.set(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recorded mapping: " + cachedMappingCount);
                })
                .addOnFailureListener(e -> {
                    // If document doesn't exist yet, create it
                    if (e.getMessage() != null && e.getMessage().contains("No document to update")) {
                        Map<String, Object> usage = new HashMap<>();
                        usage.put("mappingCount", cachedMappingCount);
                        usage.put("userId", currentUser.getUid());
                        usage.put("email", currentUser.getEmail());
                        usage.put("lastUpdated", System.currentTimeMillis());

                        userUsageRef.set(usage);
                    } else {
                        Log.e(TAG, "Error recording mapping", e);
                    }
                });

        // Notify listener
        notifyListener();
    }

    /**
     * Notify the listener if set
     */
    private void notifyListener() {
        if (listener != null) {
            boolean isPro = subscriptionManager.isProUser();
            int remaining = getRemainingMappings();
            listener.onUsageUpdated(cachedMappingCount, remaining, isPro);
        }
    }

    /**
     * Reset mapping count (for new accounts)
     */
    public void resetMappingCount() {
        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user logged in");
            return;
        }

        // Reset local cache
        cachedMappingCount = 0;

        // Update local storage
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_KEY_MAPPING_COUNT, 0).apply();

        // Update Firestore
        Map<String, Object> usage = new HashMap<>();
        usage.put("mappingCount", 0);
        usage.put("userId", currentUser.getUid());
        usage.put("email", currentUser.getEmail());
        usage.put("lastUpdated", System.currentTimeMillis());
        usage.put("reset", true);

        db.collection("user_usage")
                .document(currentUser.getUid())
                .set(usage)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reset mapping count");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error resetting mapping count", e);
                });

        // Notify listener
        notifyListener();
    }

    /**
     * Listener for usage updates
     */
    public interface UsageUpdateListener {
        void onUsageUpdated(int mappingCount, int remainingMappings, boolean isPro);
    }

    /**
     * Callback for loading usage data
     */
    public interface UsageDataCallback {
        void onDataLoaded(int mappingCount, boolean isPro);
        void onError(Exception e);
    }
}