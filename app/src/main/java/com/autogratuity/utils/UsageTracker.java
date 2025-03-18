package com.autogratuity.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.preference.PreferenceRepository;
import com.autogratuity.data.repository.subscription.SubscriptionRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Utility class for tracking app usage metrics
 * Updated to use domain-specific repositories
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
    private final FirebaseAuth mAuth;
    private final PreferenceRepository preferenceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    private int mappingCount = 0;
    private boolean isPro = false;
    private UsageUpdateListener listener;
    
    /**
     * Private constructor for singleton pattern
     */
    private UsageTracker(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.mAuth = FirebaseAuth.getInstance();
        
        // Initialize repositories from RepositoryProvider
        this.preferenceRepository = RepositoryProvider.getPreferenceRepository();
        this.subscriptionRepository = RepositoryProvider.getSubscriptionRepository();
        
        // Load local count from SharedPreferences
        mappingCount = prefs.getInt(KEY_MAPPING_COUNT, 0);
        
        // Check if user is Pro using SubscriptionRepository
        checkProStatus();
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
     * Check if the user has pro status by querying the SubscriptionRepository
     */
    private void checkProStatus() {
        disposables.add(
            subscriptionRepository.isProUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        isPro = result;
                        notifyListener();
                    },
                    error -> {
                        Log.e(TAG, "Error checking pro status", error);
                        isPro = false;
                        notifyListener();
                    }
                )
        );
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
     * Load usage data from the repositories
     */
    public void loadUsageData(UsageDataCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError(new Exception("User not logged in"));
            }
            return;
        }
        
        // Check pro status first
        disposables.add(
            subscriptionRepository.isProUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    proStatus -> {
                        isPro = proStatus;
                        
                        // Then get the user profile
                        disposables.add(
                            preferenceRepository.getUserProfile()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    profile -> {
                                        if (profile != null && profile.getUsageStats() != null) {
                                            mappingCount = profile.getUsageStats().getMappingCount();
                                            
                                            // Update local preferences
                                            prefs.edit().putInt(KEY_MAPPING_COUNT, mappingCount).apply();
                                            
                                            // Notify listener
                                            notifyListener();
                                            
                                            if (callback != null) {
                                                callback.onDataLoaded(mappingCount, isPro);
                                            }
                                        } else {
                                            // Create user profile with default values if it doesn't exist
                                            UserProfile newProfile = new UserProfile();
                                            
                                            // Create usage stats
                                            UserProfile.UsageStats usageStats = new UserProfile.UsageStats();
                                            usageStats.setMappingCount(mappingCount);
                                            newProfile.setUsageStats(usageStats);
                                            
                                            // Update preferences
                                            disposables.add(
                                                preferenceRepository.updateUserProfile(newProfile)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(
                                                        () -> {
                                                            if (callback != null) {
                                                                callback.onDataLoaded(mappingCount, isPro);
                                                            }
                                                        },
                                                        error -> {
                                                            Log.e(TAG, "Error creating profile", error);
                                                            if (callback != null) {
                                                                callback.onError(error);
                                                            }
                                                        }
                                                    )
                                            );
                                        }
                                    },
                                    error -> {
                                        Log.e(TAG, "Error loading user profile", error);
                                        if (callback != null) {
                                            callback.onError(error);
                                        }
                                    }
                                )
                        );
                    },
                    error -> {
                        Log.e(TAG, "Error checking pro status", error);
                        if (callback != null) {
                            callback.onError(error);
                        }
                    }
                )
        );
    }
    
    /**
     * Record a mapping action
     */
    public void recordMapping() {
        // Don't increment if user is Pro
        if (isPro) {
            return;
        }
        
        // Increment the local count
        mappingCount++;
        
        // Update SharedPreferences
        prefs.edit().putInt(KEY_MAPPING_COUNT, mappingCount).apply();
        
        // Update user profile in repository
        disposables.add(
            preferenceRepository.getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    profile -> {
                        if (profile != null) {
                            // Update usage stats
                            UserProfile.UsageStats usageStats = profile.getUsageStats();
                            if (usageStats == null) {
                                usageStats = new UserProfile.UsageStats();
                            }
                            usageStats.setMappingCount(mappingCount);
                            profile.setUsageStats(usageStats);
                            
                            // Update profile
                            disposables.add(
                                preferenceRepository.updateUserProfile(profile)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                        () -> {
                                            Log.d(TAG, "Mapping count updated successfully");
                                            notifyListener();
                                        },
                                        error -> Log.e(TAG, "Error updating mapping count", error)
                                    )
                            );
                        }
                    },
                    error -> Log.e(TAG, "Error getting user profile", error)
                )
        );
    }
    
    /**
     * Reset mapping count to 0
     */
    public void resetMappingCount() {
        mappingCount = 0;
        
        // Update SharedPreferences
        prefs.edit().putInt(KEY_MAPPING_COUNT, mappingCount).apply();
        
        // Update user profile in repository
        disposables.add(
            preferenceRepository.getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    profile -> {
                        if (profile != null) {
                            // Update usage stats
                            UserProfile.UsageStats usageStats = profile.getUsageStats();
                            if (usageStats == null) {
                                usageStats = new UserProfile.UsageStats();
                            }
                            usageStats.setMappingCount(0);
                            profile.setUsageStats(usageStats);
                            
                            // Update profile
                            disposables.add(
                                preferenceRepository.updateUserProfile(profile)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                        () -> {
                                            Log.d(TAG, "Mapping count reset successfully");
                                            notifyListener();
                                        },
                                        error -> Log.e(TAG, "Error resetting mapping count", error)
                                    )
                            );
                        }
                    },
                    error -> Log.e(TAG, "Error getting user profile", error)
                )
        );
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
     * Check if the user has Pro status
     */
    public boolean isPro() {
        return isPro;
    }
    
    /**
     * Update the Pro status of the user
     */
    public void setProStatus(boolean isPro) {
        this.isPro = isPro;
        
        // Create map with subscription data
        Map<String, Object> subscriptionRecord = new HashMap<>();
        subscriptionRecord.put("isPro", isPro);
        subscriptionRecord.put("timestamp", System.currentTimeMillis());
        subscriptionRecord.put("source", "manual_update");
        
        // Add subscription record to repository
        disposables.add(
            subscriptionRepository.addSubscriptionRecord(subscriptionRecord)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    ref -> {
                        Log.d(TAG, "Pro status updated successfully");
                        notifyListener();
                    },
                    error -> Log.e(TAG, "Error updating Pro status", error)
                )
        );
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
     * Clean up resources when no longer needed
     */
    public void dispose() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
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