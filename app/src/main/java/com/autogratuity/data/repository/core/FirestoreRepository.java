package com.autogratuity.data.repository.core;

import com.autogratuity.data.security.AuthenticationManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.AppConfig;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.DeliveryStats;
import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.model.SyncOperation;
import com.autogratuity.data.model.SyncStatus;
import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.util.NetworkMonitor;
import com.autogratuity.data.local.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Core implementation of the DataRepository interface that contains
 * shared functionality and base infrastructure needed by all domain repositories.
 * 
 * This class provides:
 * - Shared constants for collections and preferences
 * - Firebase initialization and configuration
 * - Cache and network management
 * - Core utilities for Firestore operations
 * - Common error handling and synchronization utilities
 * 
 * Domain-specific functionality is delegated to specialized repositories.
 */
public class FirestoreRepository implements DataRepository {
    protected static final String TAG = "FirestoreRepository";
    
    // SharedPreferences keys
    protected static final String PREFS_NAME = "com.autogratuity.data";
    protected static final String KEY_USER_PROFILE = "user_profile";
    protected static final String KEY_SUBSCRIPTION_STATUS = "subscription_status";
    protected static final String KEY_APP_CONFIG = "app_config";
    protected static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    protected static final String KEY_DEVICE_ID = "device_id";
    
    // Firestore collection names
    protected static final String COLLECTION_USER_PROFILES = "user_profiles";
    protected static final String COLLECTION_SUBSCRIPTION_RECORDS = "subscription_records";
    protected static final String COLLECTION_ADDRESSES = "addresses";
    protected static final String COLLECTION_DELIVERIES = "deliveries";
    protected static final String COLLECTION_SYNC_OPERATIONS = "sync_operations";
    protected static final String COLLECTION_USER_DEVICES = "user_devices";
    protected static final String COLLECTION_SYSTEM_CONFIG = "system_config";
    
    // Firebase instances
    protected final FirebaseFirestore db;
    protected final FirebaseAuth auth;
    protected final String userId;
    protected final AuthenticationManager authManager;
    
    // Local storage
    protected final SharedPreferences prefs;
    protected final Context context;
    
    // Device information
    protected final String deviceId;
    
    // In-memory cache
    protected final Map<String, Object> memoryCache;
    protected final Map<String, Long> cacheTimestamps;
    protected final Map<String, ListenerRegistration> activeListeners;
    
    // Cache TTL in milliseconds
    protected static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    
    // Sync status tracking
    protected final BehaviorSubject<SyncStatus> syncStatusSubject;
    protected final BehaviorSubject<NetworkStatus> networkStatusSubject;
    
    // Network monitoring
    protected final NetworkMonitor networkMonitor;
    
    // Preference manager for typed preferences access
    protected PreferenceManager prefManager;
    
    // Internal network status tracking
    protected enum NetworkStatus {
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
        this.authManager = AuthenticationManager.getInstance(context);
        
        // Enable offline persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        
        // Get current user ID without throwing exception if user is not authenticated
        FirebaseUser user = auth.getCurrentUser();
        this.userId = (user != null) ? user.getUid() : null;
        
        // Initialize SharedPreferences
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Create or retrieve device ID
        String existingDeviceId = prefs.getString(KEY_DEVICE_ID, null);
        if (existingDeviceId != null) {
            this.deviceId = existingDeviceId;
        } else {
            this.deviceId = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_DEVICE_ID, this.deviceId).apply();
        }
        
        // Initialize cache
        this.memoryCache = new ConcurrentHashMap<>();
        this.cacheTimestamps = new ConcurrentHashMap<>();
        this.activeListeners = new ConcurrentHashMap<>();
        
        // Initialize network monitor
        this.networkMonitor = new NetworkMonitor(context);
        
        // Initialize status tracking
        this.syncStatusSubject = BehaviorSubject.createDefault(new SyncStatus());
        this.networkStatusSubject = BehaviorSubject.createDefault(
                isNetworkAvailable() ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED);
        
        // Start monitoring network status
        startNetworkMonitoring();
    }
    
    /**
     * Start monitoring network status changes
     */
    private void startNetworkMonitoring() {
        // This would ideally use NetworkCallback for API 21+ but for simplicity,
        // we'll just do periodic checking in this example
        Observable.interval(30, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribe(ignored -> {
                    boolean isConnected = isNetworkAvailable();
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
    
    // Note: Methods for user and device access are defined in the helper methods section below
    
    /**
     * Check if network is available
     * 
     * @return true if connected, false otherwise
     */
    protected boolean isNetworkAvailable() {
        return networkMonitor.isNetworkAvailable();
    }
    
    // Methods moved to the helper section below
    
    //-----------------------------------------------------------------------------------
    // Helper methods for user and device management
    //-----------------------------------------------------------------------------------
    
    /**
     * Get the current Firebase authenticated user.
     * @return The current FirebaseUser or null if not authenticated
     */
    protected FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
    
    /**
     * Get the current user's ID.
     * @return String user ID or null if not authenticated
     */
    protected String getCurrentUserId() {
        // Use stored userId if available, otherwise get from current user
        if (userId != null) {
            return userId;
        }
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    /**
     * Get the device's unique identifier.
     * @return String device ID
     */
    protected String getDeviceId() {
        // Use the existing deviceId field
        return deviceId;
    }
    
    /**
     * Get SharedPreferences instance.
     * @return SharedPreferences
     */
    protected SharedPreferences getPrefs() {
        return prefs;
    }
    
    /**
     * Get preference manager for type-safe preference access.
     * @return PreferenceManager instance
     */
    protected PreferenceManager getPreferenceManager() {
        if (prefManager == null) {
            prefManager = new PreferenceManager(context, PREFS_NAME);
        }
        return prefManager;
    }
    
    //-----------------------------------------------------------------------------------
    // Helper methods for cache management
    //-----------------------------------------------------------------------------------
    
    /**
     * Check if cached data is still valid
     * 
     * @param key Cache key
     * @return true if cache is valid, false otherwise
     */
    protected boolean isCacheValid(String key) {
        Long timestamp = cacheTimestamps.get(key);
        if (timestamp == null) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        return (now - timestamp) < CACHE_TTL_MS;
    }
    
    /**
     * Get data from cache
     * 
     * @param key Cache key
     * @param <T> Type of cached data
     * @return Cached data or null if not found
     */
    @SuppressWarnings("unchecked")
    protected <T> T getFromCache(String key) {
        if (isCacheValid(key)) {
            return (T) memoryCache.get(key);
        }
        return null;
    }
    
    /**
     * Store data in cache
     * 
     * @param key Cache key
     * @param data Data to cache
     * @param <T> Type of data
     */
    protected <T> void putInCache(String key, T data) {
        memoryCache.put(key, data);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }
    
    /**
     * Invalidate a specific cache entry
     * 
     * @param key Cache key
     */
    protected void invalidateCache(String key) {
        memoryCache.remove(key);
        cacheTimestamps.remove(key);
    }
    
    /**
     * Get data from SharedPreferences
     * 
     * @param key SharedPreferences key
     * @param classOfT Class type for deserialization
     * @param <T> Type of data
     * @return Deserialized data or null if not found
     */
    protected <T> T getFromPrefs(String key, Class<T> classOfT) {
        String json = prefs.getString(key, null);
        if (json != null) {
            try {
                // This would normally use Gson.fromJson but for brevity, 
                // we're not implementing the actual serialization here
                return null; // Placeholder for deserialization
            } catch (Exception e) {
                Log.e(TAG, "Error deserializing data from SharedPreferences: " + key, e);
            }
        }
        return null;
    }
    
    /**
     * Store data in SharedPreferences
     * 
     * @param key SharedPreferences key
     * @param data Data to store
     * @param <T> Type of data
     */
    protected <T> void saveToPrefs(String key, T data) {
        try {
            // This would normally use Gson.toJson but for brevity,
            // we're not implementing the actual serialization here
            String json = "{}"; // Placeholder for serialization
            prefs.edit().putString(key, json).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error serializing data to SharedPreferences: " + key, e);
        }
    }
    
    //-----------------------------------------------------------------------------------
    // Helper methods for Firestore operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Create a document reference for a user-specific document
     * 
     * @param collection Collection name
     * @param documentId Document ID (optional, will be generated if null)
     * @return DocumentReference
     */
    protected DocumentReference getDocumentReference(String collection, String documentId) {
        if (documentId == null) {
            return db.collection(collection).document();
        } else {
            return db.collection(collection).document(documentId);
        }
    }
    
    /**
     * Create a collection reference for user-specific documents
     * 
     * @param collection Collection name
     * @return CollectionReference with user filter
     */
    protected Query getUserCollectionReference(String collection) {
        return db.collection(collection).whereEqualTo("userId", userId);
    }
    
    /**
     * Handle a Firestore error and update sync status
     * 
     * @param error The exception
     * @param operation Description of the operation that failed
     */
    protected void handleFirestoreError(Throwable error, String operation) {
        Log.e(TAG, "Firestore error during " + operation, error);
        
        // Update sync status
        SyncStatus currentStatus = syncStatusSubject.getValue();
        currentStatus.setError("Error: " + error.getMessage());
        syncStatusSubject.onNext(currentStatus);
        
        // If we're offline, queue a sync operation for later
        if (!isNetworkAvailable()) {
            currentStatus.setOnline(false);
            syncStatusSubject.onNext(currentStatus);
        }
    }
    
    /**
     * Merge updates for conflict resolution
     * This is a simplified implementation - a real one would do field-by-field merging
     * based on importance and update timestamp
     * 
     * @param serverData Server data
     * @param clientUpdates Client updates
     * @return Merged data
     */
    protected Map<String, Object> mergeUpdates(Map<String, Object> serverData, Map<String, Object> clientUpdates) {
        Map<String, Object> result = new HashMap<>(serverData);
        
        for (Map.Entry<String, Object> entry : clientUpdates.entrySet()) {
            String key = entry.getKey();
            Object clientValue = entry.getValue();
            
            // Skip special fields
            if ("version".equals(key) || "updatedAt".equals(key) || "createdAt".equals(key)) {
                continue;
            }
            
            // Add or overwrite with client value
            result.put(key, clientValue);
        }
        
        return result;
    }
    
    /**
     * Enqueue an operation for offline support
     * 
     * @param operationType Operation type (create, update, delete)
     * @param entityType Entity type (userProfile, address, delivery, etc.)
     * @param entityId Entity ID
     * @param data Data to apply
     * @return Completable that completes when operation is enqueued
     */
    protected Completable enqueueOperation(String operationType, String entityType, 
                                        String entityId, Map<String, Object> data) {
        // Get a fresh token for authentication when sync happens later
        return authManager.getAuthToken(false)
                .flatMapCompletable(token -> {
                    // Create sync operation with authentication token
                    SyncOperation operation = new SyncOperation(userId, operationType, entityType, entityId, data);
                    operation.setAuthToken(token);
                    return enqueueSyncOperation(operation);
                });
    }
    
    //-----------------------------------------------------------------------------------
    // The following methods provide minimal implementation that will be overridden
    // by domain-specific repositories. Here we just return "not implemented"
    // but ensure proper method signatures to match the interface.
    //-----------------------------------------------------------------------------------
    
    @Override
    public Single<UserProfile> getUserProfile() {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<UserProfile> getUserProfile(boolean forceRefresh) {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable updateUserProfile(UserProfile profile) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable updateUserProfileFields(Map<String, Object> fields) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Observable<UserProfile> observeUserProfile() {
        return Observable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<SubscriptionStatus> getSubscriptionStatus() {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable updateSubscriptionStatus(SubscriptionStatus status) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<DocumentReference> addSubscriptionRecord(Map<String, Object> subscriptionRecord) {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Observable<SubscriptionStatus> observeSubscriptionStatus() {
        return Observable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable verifySubscription() {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<Boolean> isProUser() {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<List<Address>> getAddresses() {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<Address> getAddressById(String addressId) {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<Address> findAddressByNormalizedAddress(String normalizedAddress) {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<DocumentReference> addAddress(Address address) {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable updateAddress(Address address) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable deleteAddress(String addressId) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Observable<List<Address>> observeAddresses() {
        return Observable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Observable<Address> observeAddress(String addressId) {
        return Observable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<List<Delivery>> getDeliveries(int limit, DocumentReference startAfter) {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<List<Delivery>> getDeliveriesByTimeRange(Date startDate, Date endDate) {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<List<Delivery>> getDeliveriesByAddress(String addressId) {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<Delivery> getDeliveryById(String deliveryId) {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<DocumentReference> addDelivery(Delivery delivery) {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable updateDelivery(Delivery delivery) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable updateDeliveryTip(String deliveryId, double tipAmount) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable deleteDelivery(String deliveryId) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Observable<List<Delivery>> observeDeliveries() {
        return Observable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Observable<Delivery> observeDelivery(String deliveryId) {
        return Observable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<Map<String, DeliveryStats>> getDeliveryStats() {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable syncData() {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<SyncStatus> getSyncStatus() {
        return Single.just(syncStatusSubject.getValue());
    }
    
    @Override
    public Completable enqueueSyncOperation(SyncOperation operation) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Single<List<SyncOperation>> getPendingSyncOperations() {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable processPendingSyncOperations() {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable updateDeviceSyncStatus(String deviceId, SyncStatus syncStatus) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Observable<SyncStatus> observeSyncStatus() {
        return syncStatusSubject;
    }
    
    @Override
    public Single<AppConfig> getAppConfig() {
        return Single.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Observable<AppConfig> observeAppConfig() {
        return Observable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable registerDevice(Map<String, Object> deviceInfo) {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable updateDeviceLastActive() {
        return Completable.error(new UnsupportedOperationException("Not implemented in core repository"));
    }
    
    @Override
    public Completable clearCaches() {
        // Clear all memory caches
        memoryCache.clear();
        cacheTimestamps.clear();
        return Completable.complete();
    }
    
    @Override
    public Completable prefetchCriticalData() {
        // Skip prefetching if user is not authenticated
        if (userId == null) {
            Log.d(TAG, "Skipping prefetchCriticalData: User not authenticated");
            return Completable.complete();
        }
        
        // Prefetch app configuration
        return getAppConfig()
                .flatMapCompletable(config -> {
                    Log.d(TAG, "App configuration prefetched");
                    return Completable.complete();
                })
                .onErrorResumeNext(error -> {
                    Log.e(TAG, "Error prefetching critical data", error);
                    return Completable.complete(); // Complete without error to prevent app crashes
                });
    }
}