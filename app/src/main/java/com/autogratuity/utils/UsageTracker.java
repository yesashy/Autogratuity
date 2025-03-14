package com.autogratuity.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.autogratuity.repositories.FirestoreRepository;
import com.autogratuity.repositories.IFirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Utility class for tracking app usage metrics
 */
public class UsageTracker {
    private static final String TAG = "UsageTracker";
    private static final String PREFS_NAME = "autogratuity_usage";
    private static final String KEY_MAPPING_COUNT = "mapping_count";
    
    // Free tier limits
    public static final int FREE_TIER_MAPPING_LIMIT = 100;
    
    private static UsageTracker instance;
    
    private final Context context;
    private final SharedPreferences prefs;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final IFirestoreRepository repository;
    
    private int mappingCount = 0;
    private boolean isPro = false;
    private SubscriptionManager subscriptionManager;
    private UsageUpdateListener listener;
    
    /**
     * Private constructor for singleton pattern
     */
    private UsageTracker(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.repository = FirestoreRepository.getInstance();
        
        // Load local count from SharedPreferences
        mappingCount = prefs.getInt(KEY_MAPPING_COUNT, 0);
        
        // Get subscription manager
        subscriptionManager = SubscriptionManager.getInstance(context);
    }
    
    /**
     * Get the singleton instance
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
        
        // Immediately notify with current data
        notifyListener();
    }
    
    /**
     * Load usage data from Firestore
     */
    public void loadUsageData(UsageDataCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError(new Exception("User not logged in"));
            }
            return;
        }
        
        String userId = currentUser.getUid();
        
        // First check if the user is a Pro user
        isPro = subscriptionManager.isProUser();
        
        // Get the mapping count from Firestore
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("mappingCount")) {
                        Long count = documentSnapshot.getLong("mappingCount");
                        if (count != null) {
                            mappingCount = count.intValue();
                            
                            // Update local count
                            prefs.edit().putInt(KEY_MAPPING_COUNT, mappingCount).apply();
                            
                            // Notify listener
                            notifyListener();
                            
                            if (callback != null) {
                                callback.onDataLoaded(mappingCount, isPro);
                            }
                        }
                    } else {
                        // Create the user document if it doesn't exist
                        db.collection("users")
                                .document(userId)
                                .set(new UsageData(mappingCount, isPro));
                        
                        if (callback != null) {
                            callback.onDataLoaded(mappingCount, isPro);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading usage data", e);
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
    }
    
    /**
     * Record a mapping action
     */
    public void recordMapping() {
        // Increment the local count
        mappingCount++;
        
        // Update SharedPreferences
        prefs.edit().putInt(KEY_MAPPING_COUNT, mappingCount).apply();
        
        // Update Firestore
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users")
                    .document(userId)
                    .update("mappingCount", mappingCount)
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating mapping count", e));
        }
        
        // Notify listener
        notifyListener();
    }
    
    /**
     * Reset mapping count to 0
     */
    public void resetMappingCount() {
        mappingCount = 0;
        
        // Update SharedPreferences
        prefs.edit().putInt(KEY_MAPPING_COUNT, mappingCount).apply();
        
        // Update Firestore
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users")
                    .document(userId)
                    .update("mappingCount", mappingCount)
                    .addOnFailureListener(e -> Log.e(TAG, "Error resetting mapping count", e));
        }
        
        // Notify listener
        notifyListener();
    }
    
    /**
     * Check if the user can add a new mapping (within free tier limits or Pro)
     */
    public boolean canAddMapping() {
        // Pro users can always add mappings
        if (isPro) {
            return true;
        }
        
        // Free users are limited
        return mappingCount < FREE_TIER_MAPPING_LIMIT;
    }
    
    /**
     * Get the remaining mappings for free tier users
     */
    public int getRemainingMappings() {
        if (isPro) {
            return Integer.MAX_VALUE;
        }
        
        return Math.max(0, FREE_TIER_MAPPING_LIMIT - mappingCount);
    }
    
    /**
     * Update the Pro status of the user
     */
    public void setProStatus(boolean isPro) {
        this.isPro = isPro;
        
        // Update Firestore
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users")
                    .document(userId)
                    .update("isPro", isPro)
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating Pro status", e));
        }
        
        // Notify listener
        notifyListener();
    }
    
    /**
     * Notify the listener about updated usage data
     */
    private void notifyListener() {
        if (listener != null) {
            listener.onUsageUpdated(mappingCount, getRemainingMappings(), isPro);
        }
    }
    
    /**
     * Data class for storing usage information
     */
    private static class UsageData {
        private final int mappingCount;
        private final boolean isPro;
        
        public UsageData(int mappingCount, boolean isPro) {
            this.mappingCount = mappingCount;
            this.isPro = isPro;
        }
        
        public int getMappingCount() {
            return mappingCount;
        }
        
        public boolean isPro() {
            return isPro;
        }
    }
    
    /**
     * Callback interface for loading usage data
     */
    public interface UsageDataCallback {
        void onDataLoaded(int mappingCount, boolean isPro);
        void onError(Exception e);
    }
    
    /**
     * Listener interface for usage updates
     */
    public interface UsageUpdateListener {
        void onUsageUpdated(int mappingCount, int remainingMappings, boolean isPro);
    }
}
