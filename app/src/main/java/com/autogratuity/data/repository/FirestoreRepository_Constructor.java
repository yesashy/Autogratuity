package com.autogratuity.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.autogratuity.data.local.PreferenceManager;
import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.AppConfig;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.DeliveryStats;
import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.model.SyncOperation;
import com.autogratuity.data.model.SyncStatus;
import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.util.NetworkMonitor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Implementation of the DataRepository interface that uses Firebase Firestore
 * as the primary data source with local caching through SharedPreferences.
 */
public class FirestoreRepository implements DataRepository {
    private static final String TAG = "FirestoreRepository";
    
    // SharedPreferences keys
    private static final String PREFS_NAME = "com.autogratuity.data";
    private static final String KEY_USER_PROFILE = "user_profile";
    private static final String KEY_SUBSCRIPTION_STATUS = "subscription_status";
    private static final String KEY_APP_CONFIG = "app_config";
    private static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    private static final String KEY_DEVICE_ID = "device_id";
    
    // Firestore collection names
    private static final String COLLECTION_USER_PROFILES = "user_profiles";
    private static final String COLLECTION_SUBSCRIPTION_RECORDS = "subscription_records";
    private static final String COLLECTION_ADDRESSES = "addresses";
    private static final String COLLECTION_DELIVERIES = "deliveries";
    private static final String COLLECTION_SYNC_OPERATIONS = "sync_operations";
    private static final String COLLECTION_USER_DEVICES = "user_devices";
    private static final String COLLECTION_SYSTEM_CONFIG = "system_config";
    
    // Firebase instances
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final String userId;
    
    // Local storage
    private final PreferenceManager preferenceManager;
    private final Context context;
    
    // Device information
    private final String deviceId;
    
    // Network monitoring
    private final NetworkMonitor networkMonitor;
    
    // In-memory cache
    private final Map<String, Object> memoryCache;
    private final Map<String, Long> cacheTimestamps;
    private final Map<String, ListenerRegistration> activeListeners;
    
    // Cache TTL in milliseconds
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    
    // Sync status tracking
    private final BehaviorSubject<SyncStatus> syncStatusSubject;
    private final BehaviorSubject<NetworkStatus> networkStatusSubject;
    
    // Internal network status tracking
    private enum NetworkStatus {
        CONNECTED,
        DISCONNECTED
    }
    
    /**
     * Constructor for FirestoreRepository
     * 
     * @param context Android context for SharedPreferences and connectivity
     */
    public FirestoreRepository(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        
        // Enable offline persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        
        // Get current user ID
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            this.userId = user.getUid();
        } else {
            throw new IllegalStateException("User must be authenticated before using FirestoreRepository");
        }
        
        // Initialize SharedPreferences with our wrapper
        this.preferenceManager = new PreferenceManager(context, PREFS_NAME);
        
        // Create or retrieve device ID
        String existingDeviceId = preferenceManager.getString(KEY_DEVICE_ID, null);
        if (existingDeviceId != null) {
            this.deviceId = existingDeviceId;
        } else {
            this.deviceId = UUID.randomUUID().toString();
            preferenceManager.saveString(KEY_DEVICE_ID, this.deviceId);
        }
        
        // Initialize network monitor
        this.networkMonitor = new NetworkMonitor(context);
        
        // Initialize cache
        this.memoryCache = new ConcurrentHashMap<>();
        this.cacheTimestamps = new ConcurrentHashMap<>();
        this.activeListeners = new ConcurrentHashMap<>();
        
        // Initialize status tracking
        this.syncStatusSubject = BehaviorSubject.createDefault(new SyncStatus());
        this.networkStatusSubject = BehaviorSubject.createDefault(
                networkMonitor.isConnected() ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED);
        
        // Start monitoring network status
        startNetworkMonitoring();
    }
    
    /**
     * Start monitoring network status changes
     */
    private void startNetworkMonitoring() {
        networkMonitor.observe()
                .observeOn(Schedulers.io())
                .subscribe(isConnected -> {
                    NetworkStatus currentStatus = isConnected ? 
                            NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED;
                    
                    // Only emit if status changed
                    if (networkStatusSubject.getValue() != currentStatus) {
                        networkStatusSubject.onNext(currentStatus);
                        
                        // Update sync status
                        SyncStatus syncStatus = syncStatusSubject.getValue();
                        syncStatus.setOnline(isConnected);
                        syncStatusSubject.onNext(syncStatus);
                        
                        // If we just came online, trigger sync
                        if (isConnected && syncStatus.getPendingOperations() > 0) {
                            processPendingSyncOperations().subscribe();
                        }
                    }
                }, throwable -> {
                    Log.e(TAG, "Error monitoring network status", throwable);
                });
    }
    
    /**
     * Check if network is available using the NetworkMonitor
     * 
     * @return true if connected, false otherwise
     */
    private boolean isNetworkAvailable() {
        return networkMonitor.isConnected();
    }
