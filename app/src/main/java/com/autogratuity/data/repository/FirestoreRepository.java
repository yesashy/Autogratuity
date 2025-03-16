package com.autogratuity.data.repository;

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
    private final SharedPreferences prefs;
    private final Context context;
    
    // Device information
    private final String deviceId;
    
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
    
    /**
     * Check if network is available
     * 
     * @return true if connected, false otherwise
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        
        return false;
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
    private boolean isCacheValid(String key) {
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
    private <T> T getFromCache(String key) {
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
    private <T> void putInCache(String key, T data) {
        memoryCache.put(key, data);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }
    
    /**
     * Invalidate a specific cache entry
     * 
     * @param key Cache key
     */
    private void invalidateCache(String key) {
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
    private <T> T getFromPrefs(String key, Class<T> classOfT) {
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
    private <T> void saveToPrefs(String key, T data) {
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
    private DocumentReference getDocumentReference(String collection, String documentId) {
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
    private Query getUserCollectionReference(String collection) {
        return db.collection(collection).whereEqualTo("userId", userId);
    }
    
    /**
     * Handle a Firestore error and update sync status
     * 
     * @param error The exception
     * @param operation Description of the operation that failed
     */
    private void handleFirestoreError(Exception error, String operation) {
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
     * Update document with transaction to handle conflicts
     * 
     * @param documentRef Document reference
     * @param updates Updates to apply
     * @param conflictStrategy Conflict resolution strategy
     * @return Completable that completes when update is finished
     */
    private Completable updateWithTransaction(DocumentReference documentRef, 
                                             Map<String, Object> updates, 
                                             String conflictStrategy) {
        return Completable.create(emitter -> {
            db.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(documentRef);
                
                if (snapshot.exists()) {
                    // Check for version conflict
                    Long localVersion = (Long) updates.get("version");
                    Long serverVersion = snapshot.getLong("version");
                    
                    if (localVersion != null && serverVersion != null && localVersion < serverVersion) {
                        // Conflict detected
                        if (SyncOperation.CONFLICT_RESOLUTION_SERVER_WINS.equals(conflictStrategy)) {
                            // Server wins - no update
                            return null;
                        } else if (SyncOperation.CONFLICT_RESOLUTION_CLIENT_WINS.equals(conflictStrategy)) {
                            // Client wins - force update
                            updates.put("version", serverVersion + 1);
                            transaction.update(documentRef, updates);
                        } else {
                            // Merge strategy - would implement field-by-field merge
                            // For brevity, not implemented here
                            Map<String, Object> merged = mergeUpdates(snapshot.getData(), updates);
                            merged.put("version", serverVersion + 1);
                            transaction.update(documentRef, merged);
                        }
                    } else {
                        // No conflict or local is newer
                        updates.put("version", serverVersion != null ? serverVersion + 1 : 1);
                        transaction.update(documentRef, updates);
                    }
                } else {
                    // Document doesn't exist, create it
                    updates.put("version", 1);
                    transaction.set(documentRef, updates);
                }
                
                return null;
            }).addOnSuccessListener(result -> {
                emitter.onComplete();
            }).addOnFailureListener(error -> {
                handleFirestoreError(error, "updating document: " + documentRef.getPath());
                emitter.onError(error);
            });
        });
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
    private Map<String, Object> mergeUpdates(Map<String, Object> serverData, Map<String, Object> clientUpdates) {
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
    private Completable enqueueOperation(String operationType, String entityType, 
                                        String entityId, Map<String, Object> data) {
        return enqueueSyncOperation(
                new SyncOperation(userId, operationType, entityType, entityId, data));
    }
    
    //-----------------------------------------------------------------------------------
    // Implementation of UserProfile operations
    //-----------------------------------------------------------------------------------
    
    @Override
    public Single<UserProfile> getUserProfile() {
        return getUserProfile(false);
    }
    
    @Override
    public Single<UserProfile> getUserProfile(boolean forceRefresh) {
        String cacheKey = "userProfile_" + userId;
        
        return Single.create(emitter -> {
            // First try memory cache if not forcing refresh
            if (!forceRefresh) {
                UserProfile cached = getFromCache(cacheKey);
                if (cached != null) {
                    emitter.onSuccess(cached);
                    return;
                }
            }
            
            // Then try Firestore
            DocumentReference docRef = db.collection(COLLECTION_USER_PROFILES).document(userId);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                    if (profile != null) {
                        // Cache the result
                        putInCache(cacheKey, profile);
                        saveToPrefs(KEY_USER_PROFILE, profile);
                        
                        emitter.onSuccess(profile);
                    } else {
                        emitter.onError(new Exception("Failed to parse user profile"));
                    }
                } else {
                    // Create a new profile
                    UserProfile newProfile = createDefaultUserProfile();
                    saveUserProfile(newProfile)
                            .subscribe(
                                    () -> emitter.onSuccess(newProfile),
                                    emitter::onError
                            );
                }
            }).addOnFailureListener(e -> {
                // If offline, try SharedPreferences
                if (!isNetworkAvailable()) {
                    UserProfile localProfile = getFromPrefs(KEY_USER_PROFILE, UserProfile.class);
                    if (localProfile != null) {
                        emitter.onSuccess(localProfile);
                    } else {
                        emitter.onError(e);
                    }
                } else {
                    handleFirestoreError(e, "getting user profile");
                    emitter.onError(e);
                }
            });
        });
    }
    
    /**
     * Create a default user profile for new users
     * 
     * @return A new UserProfile with default values
     */
    private UserProfile createDefaultUserProfile() {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setCreatedAt(new Date());
        profile.setLastLoginAt(new Date());
        profile.setAccountStatus("active");
        
        // Set up default subscription as free
        UserProfile.Subscription subscription = new UserProfile.Subscription();
        subscription.setStatus("free");
        subscription.setLevel("");
        subscription.setLifetime(false);
        profile.setSubscription(subscription);
        
        // Set up default preferences
        UserProfile.Preferences preferences = new UserProfile.Preferences();
        preferences.setNotificationsEnabled(true);
        preferences.setTheme("system");
        preferences.setUseLocation(true);
        preferences.setDefaultTipPercentage(15);
        profile.setPreferences(preferences);
        
        // Set up default permissions
        UserProfile.Permissions permissions = new UserProfile.Permissions();
        permissions.setLevel("user");
        permissions.setBypassLimits(false);
        permissions.setMaxUploads(100);
        profile.setPermissions(permissions);
        
        // Set up usage tracking
        UserProfile.Usage usage = new UserProfile.Usage();
        usage.setMappingCount(0);
        usage.setDeliveryCount(0);
        usage.setAddressCount(0);
        profile.setUsage(usage);
        
        // Set up sync info
        UserProfile.SyncInfo syncInfo = new UserProfile.SyncInfo();
        syncInfo.setVersion(1);
        List<String> deviceIds = new ArrayList<>();
        deviceIds.add(deviceId);
        syncInfo.setDeviceIds(deviceIds);
        profile.setSyncInfo(syncInfo);
        
        // Set up app settings
        UserProfile.AppSettings appSettings = new UserProfile.AppSettings();
        appSettings.setDataCollectionOptIn(true);
        appSettings.setOnboardingCompleted(false);
        profile.setAppSettings(appSettings);
        
        // Set up communication preferences
        UserProfile.Communication communication = new UserProfile.Communication();
        communication.setEmailOptIn(true);
        communication.setMarketingOptIn(false);
        communication.setPushNotificationsEnabled(true);
        profile.setCommunication(communication);
        
        return profile;
    }
    
    /**
     * Helper method to save a user profile to Firestore
     * 
     * @param profile The profile to save
     * @return Completable that completes when save is finished
     */
    private Completable saveUserProfile(UserProfile profile) {
        DocumentReference docRef = db.collection(COLLECTION_USER_PROFILES).document(userId);
        
        return Completable.create(emitter -> {
            docRef.set(profile)
                    .addOnSuccessListener(aVoid -> {
                        // Update cache
                        putInCache("userProfile_" + userId, profile);
                        saveToPrefs(KEY_USER_PROFILE, profile);
                        
                        emitter.onComplete();
                    })
                    .addOnFailureListener(e -> {
                        // If offline, save to queue
                        if (!isNetworkAvailable()) {
                            // Save locally anyway
                            saveToPrefs(KEY_USER_PROFILE, profile);
                            
                            // Create sync operation
                            Map<String, Object> data = new HashMap<>(); // Convert profile to map
                            enqueueOperation("update", "userProfile", userId, data)
                                    .subscribe(
                                            emitter::onComplete,
                                            emitter::onError
                                    );
                        } else {
                            handleFirestoreError(e, "saving user profile");
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Completable updateUserProfile(UserProfile profile) {
        DocumentReference docRef = db.collection(COLLECTION_USER_PROFILES).document(userId);
        
        return Completable.create(emitter -> {
            // Update timestamp and increment version
            Date now = new Date();
            profile.setLastLoginAt(now);
            
            docRef.set(profile)
                    .addOnSuccessListener(aVoid -> {
                        // Update cache
                        putInCache("userProfile_" + userId, profile);
                        saveToPrefs(KEY_USER_PROFILE, profile);
                        
                        emitter.onComplete();
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // Save locally anyway
                            saveToPrefs(KEY_USER_PROFILE, profile);
                            
                            // Create sync operation
                            Map<String, Object> data = new HashMap<>(); // Convert profile to map
                            enqueueOperation("update", "userProfile", userId, data)
                                    .subscribe(
                                            emitter::onComplete,
                                            emitter::onError
                                    );
                        } else {
                            handleFirestoreError(e, "updating user profile");
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Completable updateUserProfileFields(Map<String, Object> fields) {
        DocumentReference docRef = db.collection(COLLECTION_USER_PROFILES).document(userId);
        
        // Add timestamp
        fields.put("lastLoginAt", FieldValue.serverTimestamp());
        
        return Completable.create(emitter -> {
            docRef.update(fields)
                    .addOnSuccessListener(aVoid -> {
                        // Invalidate cache to force refresh
                        invalidateCache("userProfile_" + userId);
                        
                        emitter.onComplete();
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // Create sync operation
                            enqueueOperation("update", "userProfile", userId, fields)
                                    .subscribe(
                                            emitter::onComplete,
                                            emitter::onError
                                    );
                        } else {
                            handleFirestoreError(e, "updating user profile fields");
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Observable<UserProfile> observeUserProfile() {
        return Observable.create(emitter -> {
            // Set up listener
            String listenerKey = "userProfile_" + userId + "_listener";
            ListenerRegistration listener = activeListeners.get(listenerKey);
            
            if (listener != null) {
                // Remove old listener
                listener.remove();
            }
            
            // Create new listener
            DocumentReference docRef = db.collection(COLLECTION_USER_PROFILES).document(userId);
            listener = docRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                    }
                    return;
                }
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                    if (profile != null) {
                        // Update cache
                        putInCache("userProfile_" + userId, profile);
                        saveToPrefs(KEY_USER_PROFILE, profile);
                        
                        // Emit to subscribers
                        if (!emitter.isDisposed()) {
                            emitter.onNext(profile);
                        }
                    }
                }
            });
            
            // Store listener for cleanup
            activeListeners.put(listenerKey, listener);
            
            // Clean up when disposed
            emitter.setCancellable(() -> {
                if (listener != null) {
                    listener.remove();
                    activeListeners.remove(listenerKey);
                }
            });
            
            // Initially load from cache or Firestore
            UserProfile cached = getFromCache("userProfile_" + userId);
            if (cached != null) {
                emitter.onNext(cached);
            } else {
                getUserProfile()
                        .subscribe(
                                emitter::onNext,
                                throwable -> Log.e(TAG, "Error loading initial user profile", throwable)
                        );
            }
        });
    }
    
    //-----------------------------------------------------------------------------------
    // Implementation of Subscription operations
    //-----------------------------------------------------------------------------------
    
    @Override
    public Single<SubscriptionStatus> getSubscriptionStatus() {
        String cacheKey = "subscriptionStatus_" + userId;
        
        return Single.create(emitter -> {
            // First try memory cache
            SubscriptionStatus cached = getFromCache(cacheKey);
            if (cached != null) {
                emitter.onSuccess(cached);
                return;
            }
            
            // Then try Firestore - look for active subscription records
            db.collection(COLLECTION_SUBSCRIPTION_RECORDS)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isActive", true)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Find the most relevant subscription (e.g., lifetime first, then by expiry date)
                            SubscriptionStatus status = new SubscriptionStatus(userId);
                            boolean foundLifetime = false;
                            Date latestExpiry = null;
                            DocumentSnapshot latestSubscription = null;
                            
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                // Check for lifetime subscription first
                                Boolean isLifetime = doc.getBoolean("isLifetime");
                                if (isLifetime != null && isLifetime) {
                                    // Found lifetime subscription, use it immediately
                                    foundLifetime = true;
                                    latestSubscription = doc;
                                    break;
                                }
                                
                                // Otherwise check expiry date
                                Date expiryDate = doc.getDate("expiryDate");
                                if (expiryDate != null && (latestExpiry == null || expiryDate.after(latestExpiry))) {
                                    latestExpiry = expiryDate;
                                    latestSubscription = doc;
                                }
                            }
                            
                            // Use the most relevant subscription
                            if (latestSubscription != null) {
                                // Convert to SubscriptionStatus
                                status.setUserId(userId);
                                status.setStatus(foundLifetime ? "lifetime" : "pro");
                                status.setLevel(latestSubscription.getString("subscriptionLevel"));
                                status.setLifetime(foundLifetime);
                                status.setActive(true);
                                status.setExpiryDate(latestExpiry);
                                status.setStartDate(latestSubscription.getDate("startDate"));
                                status.setProvider(latestSubscription.getString("paymentProvider"));
                                status.setOrderId(latestSubscription.getString("orderId"));
                                
                                // Get verification data if available
                                Map<String, Object> verificationData = 
                                        (Map<String, Object>) latestSubscription.get("verificationData");
                                if (verificationData != null) {
                                    status.setLastVerified((Date) verificationData.get("lastVerified"));
                                    status.setVerificationStatus((String) verificationData.get("verificationStatus"));
                                    status.setVerificationError((String) verificationData.get("verificationError"));
                                }
                                
                                // Cache the result
                                putInCache(cacheKey, status);
                                saveToPrefs(KEY_SUBSCRIPTION_STATUS, status);
                                
                                emitter.onSuccess(status);
                            } else {
                                // No valid subscription found, create free status
                                SubscriptionStatus freeStatus = new SubscriptionStatus(userId);
                                putInCache(cacheKey, freeStatus);
                                saveToPrefs(KEY_SUBSCRIPTION_STATUS, freeStatus);
                                
                                emitter.onSuccess(freeStatus);
                            }
                        } else {
                            // No active subscriptions found, create free status
                            SubscriptionStatus freeStatus = new SubscriptionStatus(userId);
                            putInCache(cacheKey, freeStatus);
                            saveToPrefs(KEY_SUBSCRIPTION_STATUS, freeStatus);
                            
                            emitter.onSuccess(freeStatus);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If offline, try SharedPreferences
                        if (!isNetworkAvailable()) {
                            SubscriptionStatus localStatus = 
                                    getFromPrefs(KEY_SUBSCRIPTION_STATUS, SubscriptionStatus.class);
                            if (localStatus != null) {
                                emitter.onSuccess(localStatus);
                            } else {
                                // Create a free status if nothing found
                                SubscriptionStatus freeStatus = new SubscriptionStatus(userId);
                                emitter.onSuccess(freeStatus);
                            }
                        } else {
                            handleFirestoreError(e, "getting subscription status");
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Completable updateSubscriptionStatus(SubscriptionStatus status) {
        // This is a bit different - we don't directly update subscription status
        // Instead, we create or update a subscription record
        
        // Generate a record ID if this is a new subscription
        String recordId = userId + "_" + (status.isLifetime() ? "lifetime" : System.currentTimeMillis());
        DocumentReference docRef = db.collection(COLLECTION_SUBSCRIPTION_RECORDS).document(recordId);
        
        // Convert SubscriptionStatus to subscription record
        Map<String, Object> record = new HashMap<>();
        record.put("userId", userId);
        record.put("subscriptionLevel", status.getLevel() != null ? status.getLevel() : status.getStatus());
        record.put("isActive", status.isActive());
        record.put("isLifetime", status.isLifetime());
        record.put("startDate", status.getStartDate());
        record.put("expiryDate", status.getExpiryDate());
        record.put("paymentProvider", status.getProvider());
        record.put("orderId", status.getOrderId());
        record.put("type", "purchase");
        record.put("updatedAt", FieldValue.serverTimestamp());
        
        // Add verification data
        Map<String, Object> verificationData = new HashMap<>();
        verificationData.put("lastVerified", status.getLastVerified());
        verificationData.put("verificationStatus", 
                status.getVerificationStatus() != null ? status.getVerificationStatus() : "pending");
        verificationData.put("verificationError", status.getVerificationError());
        record.put("verificationData", verificationData);
        
        return Completable.create(emitter -> {
            docRef.set(record)
                    .addOnSuccessListener(aVoid -> {
                        // Cache the status
                        putInCache("subscriptionStatus_" + userId, status);
                        saveToPrefs(KEY_SUBSCRIPTION_STATUS, status);
                        
                        // Also update user profile with subscription info
                        updateUserProfileWithSubscription(status)
                                .subscribe(
                                        emitter::onComplete,
                                        emitter::onError
                                );
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // Save locally anyway
                            saveToPrefs(KEY_SUBSCRIPTION_STATUS, status);
                            
                            // Create sync operation
                            enqueueOperation("create", "subscriptionRecord", recordId, record)
                                    .subscribe(
                                            () -> {
                                                // Also update user profile with subscription info locally
                                                updateUserProfileWithSubscription(status)
                                                        .subscribe(
                                                                emitter::onComplete,
                                                                emitter::onError
                                                        );
                                            },
                                            emitter::onError
                                    );
                        } else {
                            handleFirestoreError(e, "updating subscription status");
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    /**
     * Helper method to update user profile with subscription information
     * 
     * @param status The subscription status
     * @return Completable that completes when update is finished
     */
    private Completable updateUserProfileWithSubscription(SubscriptionStatus status) {
        Map<String, Object> updates = new HashMap<>();
        
        Map<String, Object> subscription = new HashMap<>();
        subscription.put("status", status.getStatus());
        subscription.put("level", status.getLevel());
        subscription.put("isLifetime", status.isLifetime());
        subscription.put("startDate", status.getStartDate());
        subscription.put("expiryDate", status.getExpiryDate());
        subscription.put("provider", status.getProvider());
        subscription.put("orderId", status.getOrderId());
        subscription.put("lastVerified", status.getLastVerified());
        
        updates.put("subscription", subscription);
        
        return updateUserProfileFields(updates);
    }
    
    @Override
    public Single<DocumentReference> addSubscriptionRecord(Map<String, Object> subscriptionRecord) {
        // Generate document ID
        String recordId = (String) subscriptionRecord.get("recordId");
        if (recordId == null || recordId.isEmpty()) {
            recordId = userId + "_" + System.currentTimeMillis();
            subscriptionRecord.put("recordId", recordId);
        }
        
        // Make sure we have required fields
        subscriptionRecord.put("userId", userId);
        subscriptionRecord.put("createdAt", FieldValue.serverTimestamp());
        subscriptionRecord.put("updatedAt", FieldValue.serverTimestamp());
        
        // Version for conflict resolution
        subscriptionRecord.put("version", 1);
        
        DocumentReference docRef = db.collection(COLLECTION_SUBSCRIPTION_RECORDS).document(recordId);
        
        return Single.create(emitter -> {
            docRef.set(subscriptionRecord)
                    .addOnSuccessListener(aVoid -> {
                        // Invalidate cache
                        invalidateCache("subscriptionStatus_" + userId);
                        
                        emitter.onSuccess(docRef);
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // Create sync operation
                            enqueueOperation("create", "subscriptionRecord", recordId, subscriptionRecord)
                                    .subscribe(
                                            () -> emitter.onSuccess(docRef),
                                            emitter::onError
                                    );
                        } else {
                            handleFirestoreError(e, "adding subscription record");
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Observable<SubscriptionStatus> observeSubscriptionStatus() {
        return Observable.create(emitter -> {
            // Set up listener for subscription records
            String listenerKey = "subscriptionRecords_" + userId + "_listener";
            ListenerRegistration listener = activeListeners.get(listenerKey);
            
            if (listener != null) {
                // Remove old listener
                listener.remove();
            }
            
            // Create new listener
            listener = db.collection(COLLECTION_SUBSCRIPTION_RECORDS)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isActive", true)
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            if (!emitter.isDisposed()) {
                                emitter.onError(e);
                            }
                            return;
                        }
                        
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Process the same way as getSubscriptionStatus
                            SubscriptionStatus status = new SubscriptionStatus(userId);
                            boolean foundLifetime = false;
                            Date latestExpiry = null;
                            DocumentSnapshot latestSubscription = null;
                            
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                // Check for lifetime subscription first
                                Boolean isLifetime = doc.getBoolean("isLifetime");
                                if (isLifetime != null && isLifetime) {
                                    foundLifetime = true;
                                    latestSubscription = doc;
                                    break;
                                }
                                
                                // Otherwise check expiry date
                                Date expiryDate = doc.getDate("expiryDate");
                                if (expiryDate != null && (latestExpiry == null || expiryDate.after(latestExpiry))) {
                                    latestExpiry = expiryDate;
                                    latestSubscription = doc;
                                }
                            }
                            
                            // Use the most relevant subscription
                            if (latestSubscription != null) {
                                // Convert to SubscriptionStatus
                                status.setUserId(userId);
                                status.setStatus(foundLifetime ? "lifetime" : "pro");
                                status.setLevel(latestSubscription.getString("subscriptionLevel"));
                                status.setLifetime(foundLifetime);
                                status.setActive(true);
                                status.setExpiryDate(latestExpiry);
                                status.setStartDate(latestSubscription.getDate("startDate"));
                                status.setProvider(latestSubscription.getString("paymentProvider"));
                                status.setOrderId(latestSubscription.getString("orderId"));
                                
                                // Get verification data if available
                                Map<String, Object> verificationData = 
                                        (Map<String, Object>) latestSubscription.get("verificationData");
                                if (verificationData != null) {
                                    status.setLastVerified((Date) verificationData.get("lastVerified"));
                                    status.setVerificationStatus((String) verificationData.get("verificationStatus"));
                                    status.setVerificationError((String) verificationData.get("verificationError"));
                                }
                                
                                // Cache the result
                                putInCache("subscriptionStatus_" + userId, status);
                                saveToPrefs(KEY_SUBSCRIPTION_STATUS, status);
                                
                                // Emit to subscribers
                                if (!emitter.isDisposed()) {
                                    emitter.onNext(status);
                                }
                            } else {
                                // No valid subscription found, use free status
                                SubscriptionStatus freeStatus = new SubscriptionStatus(userId);
                                putInCache("subscriptionStatus_" + userId, freeStatus);
                                saveToPrefs(KEY_SUBSCRIPTION_STATUS, freeStatus);
                                
                                if (!emitter.isDisposed()) {
                                    emitter.onNext(freeStatus);
                                }
                            }
                        } else {
                            // No active subscriptions found, use free status
                            SubscriptionStatus freeStatus = new SubscriptionStatus(userId);
                            putInCache("subscriptionStatus_" + userId, freeStatus);
                            saveToPrefs(KEY_SUBSCRIPTION_STATUS, freeStatus);
                            
                            if (!emitter.isDisposed()) {
                                emitter.onNext(freeStatus);
                            }
                        }
                    });
            
            // Store listener for cleanup
            activeListeners.put(listenerKey, listener);
            
            // Clean up when disposed
            emitter.setCancellable(() -> {
                if (listener != null) {
                    listener.remove();
                    activeListeners.remove(listenerKey);
                }
            });
            
            // Initially load from cache or Firestore
            SubscriptionStatus cached = getFromCache("subscriptionStatus_" + userId);
            if (cached != null) {
                emitter.onNext(cached);
            } else {
                getSubscriptionStatus()
                        .subscribe(
                                emitter::onNext,
                                throwable -> Log.e(TAG, "Error loading initial subscription status", throwable)
                        );
            }
        });
    }
    
    @Override
    public Completable verifySubscription() {
        // This would call the appropriate payment provider API
        // For brevity, we'll just mark the subscription as verified
        
        return getSubscriptionStatus()
                .flatMapCompletable(status -> {
                    // Skip verification for free/lifetime
                    if (status.isLifetime() || "free".equals(status.getStatus())) {
                        return Completable.complete();
                    }
                    
                    // Set verification status to verified and timestamp
                    status.setVerificationStatus("verified");
                    status.setLastVerified(new Date());
                    
                    // If expired, mark as inactive
                    if (status.getExpiryDate() != null && status.getExpiryDate().before(new Date())) {
                        status.setActive(false);
                    }
                    
                    return updateSubscriptionStatus(status);
                });
    }
    
    @Override
    public Single<Boolean> isProUser() {
        return getSubscriptionStatus()
                .map(SubscriptionStatus::isPro);
    }
    
    //-----------------------------------------------------------------------------------
    // Implementation of Address operations
    //-----------------------------------------------------------------------------------
    
    @Override
    public Single<List<Address>> getAddresses() {
        String cacheKey = "addresses_" + userId;
        
        return Single.create(emitter -> {
            // First try memory cache
            List<Address> cached = getFromCache(cacheKey);
            if (cached != null) {
                emitter.onSuccess(cached);
                return;
            }
            
            // Then try Firestore
            getUserCollectionReference(COLLECTION_ADDRESSES)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Address> addresses = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Address address = doc.toObject(Address.class);
                            if (address != null) {
                                addresses.add(address);
                            }
                        }
                        
                        // Cache the result
                        putInCache(cacheKey, addresses);
                        
                        emitter.onSuccess(addresses);
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "getting addresses");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<Address> getAddressById(String addressId) {
        String cacheKey = "address_" + addressId;
        
        return Single.create(emitter -> {
            // First try memory cache
            Address cached = getFromCache(cacheKey);
            if (cached != null) {
                emitter.onSuccess(cached);
                return;
            }
            
            // Then try Firestore
            DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document(addressId);
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Address address = documentSnapshot.toObject(Address.class);
                            if (address != null) {
                                // Cache the result
                                putInCache(cacheKey, address);
                                
                                emitter.onSuccess(address);
                            } else {
                                emitter.onError(new Exception("Failed to parse address"));
                            }
                        } else {
                            emitter.onError(new Exception("Address not found: " + addressId));
                        }
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "getting address by ID");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<Address> findAddressByNormalizedAddress(String normalizedAddress) {
        if (normalizedAddress == null || normalizedAddress.isEmpty()) {
            return Single.error(new IllegalArgumentException("Normalized address cannot be empty"));
        }
        
        return Single.create(emitter -> {
            // Query Firestore for the address
            db.collection(COLLECTION_ADDRESSES)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("normalizedAddress", normalizedAddress)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            Address address = querySnapshot.getDocuments().get(0).toObject(Address.class);
                            if (address != null) {
                                // Cache the result
                                putInCache("address_" + address.getAddressId(), address);
                                
                                emitter.onSuccess(address);
                            } else {
                                emitter.onError(new Exception("Failed to parse address"));
                            }
                        } else {
                            // Address not found
                            emitter.onSuccess(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "finding address by normalized address");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<DocumentReference> addAddress(Address address) {
        if (address.getNormalizedAddress() == null || address.getNormalizedAddress().isEmpty()) {
            return Single.error(new IllegalArgumentException("Normalized address is required"));
        }
        
        return findAddressByNormalizedAddress(address.getNormalizedAddress())
                .flatMap(existingAddress -> {
                    if (existingAddress != null) {
                        // Already exists, return existing reference
                        return Single.just(
                                db.collection(COLLECTION_ADDRESSES).document(existingAddress.getAddressId()));
                    } else {
                        // Create new address
                        return createNewAddress(address);
                    }
                });
    }
    
    /**
     * Helper method to create a new address
     * 
     * @param address The address to create
     * @return Single that emits the document reference
     */
    private Single<DocumentReference> createNewAddress(Address address) {
        // Generate document ID if needed
        String addressId = address.getAddressId();
        if (addressId == null || addressId.isEmpty()) {
            addressId = UUID.randomUUID().toString();
            address.setAddressId(addressId);
        }
        
        // Set user ID
        address.setUserId(userId);
        
        // Initialize metadata if needed
        if (address.getMetadata() == null) {
            Address.Metadata metadata = new Address.Metadata();
            metadata.setCreatedAt(new Date());
            metadata.setUpdatedAt(new Date());
            metadata.setVersion(1);
            address.setMetadata(metadata);
        } else {
            address.getMetadata().setUpdatedAt(new Date());
        }
        
        // Initialize stats if needed
        if (address.getDeliveryStats() == null) {
            Address.DeliveryStats stats = new Address.DeliveryStats();
            stats.setDeliveryCount(0);
            stats.setTipCount(0);
            stats.setTotalTips(0);
            stats.setAverageTip(0);
            address.setDeliveryStats(stats);
        }
        
        DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document(addressId);
        
        return Single.create(emitter -> {
            docRef.set(address)
                    .addOnSuccessListener(aVoid -> {
                        // Cache the address
                        putInCache("address_" + addressId, address);
                        
                        // Invalidate addresses cache
                        invalidateCache("addresses_" + userId);
                        
                        // Update user profile with address count
                        updateAddressCountInUserProfile(1)
                                .subscribe(() -> {}, throwable -> {
                                    Log.e(TAG, "Error updating address count", throwable);
                                });
                        
                        emitter.onSuccess(docRef);
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // Create sync operation
                            Map<String, Object> data = new HashMap<>(); // Convert address to map
                            enqueueOperation("create", "address", addressId, data)
                                    .subscribe(
                                            () -> emitter.onSuccess(docRef),
                                            emitter::onError
                                    );
                        } else {
                            handleFirestoreError(e, "adding address");
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    /**
     * Helper method to update address count in user profile
     * 
     * @param delta Change in address count (1 for add, -1 for delete)
     * @return Completable that completes when update is finished
     */
    private Completable updateAddressCountInUserProfile(int delta) {
        return getUserProfile()
                .flatMapCompletable(profile -> {
                    if (profile.getUsage() == null) {
                        profile.setUsage(new UserProfile.Usage());
                    }
                    
                    int currentCount = profile.getUsage().getAddressCount();
                    profile.getUsage().setAddressCount(Math.max(0, currentCount + delta));
                    profile.getUsage().setLastUsageUpdate(new Date());
                    
                    return updateUserProfile(profile);
                });
    }
    
    @Override
    public Completable updateAddress(Address address) {
        if (address.getAddressId() == null || address.getAddressId().isEmpty()) {
            return Completable.error(new IllegalArgumentException("Address ID is required"));
        }
        
        DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document(address.getAddressId());
        
        // Update metadata
        if (address.getMetadata() == null) {
            Address.Metadata metadata = new Address.Metadata();
            metadata.setUpdatedAt(new Date());
            metadata.setVersion(1);
            address.setMetadata(metadata);
        } else {
            address.getMetadata().setUpdatedAt(new Date());
        }
        
        return Completable.create(emitter -> {
            docRef.set(address)
                    .addOnSuccessListener(aVoid -> {
                        // Update cache
                        putInCache("address_" + address.getAddressId(), address);
                        
                        // Invalidate addresses cache
                        invalidateCache("addresses_" + userId);
                        
                        emitter.onComplete();
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // Create sync operation
                            Map<String, Object> data = new HashMap<>(); // Convert address to map
                            enqueueOperation("update", "address", address.getAddressId(), data)
                                    .subscribe(
                                            emitter::onComplete,
                                            emitter::onError
                                    );
                        } else {
                            handleFirestoreError(e, "updating address");
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Completable deleteAddress(String addressId) {
        DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document(addressId);
        
        return Completable.create(emitter -> {
            // First check if address exists and belongs to user
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Address address = documentSnapshot.toObject(Address.class);
                            if (address != null && userId.equals(address.getUserId())) {
                                // Delete the address
                                docRef.delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Remove from cache
                                            invalidateCache("address_" + addressId);
                                            invalidateCache("addresses_" + userId);
                                            
                                            // Update user profile with address count
                                            updateAddressCountInUserProfile(-1)
                                                    .subscribe(() -> {}, throwable -> {
                                                        Log.e(TAG, "Error updating address count", throwable);
                                                    });
                                            
                                            emitter.onComplete();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (!isNetworkAvailable()) {
                                                // Create sync operation
                                                enqueueOperation("delete", "address", addressId, null)
                                                        .subscribe(
                                                                emitter::onComplete,
                                                                emitter::onError
                                                        );
                                            } else {
                                                handleFirestoreError(e, "deleting address");
                                                emitter.onError(e);
                                            }
                                        });
                            } else {
                                emitter.onError(new SecurityException("Address does not belong to current user"));
                            }
                        } else {
                            // Address already deleted
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "checking address before delete");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Observable<List<Address>> observeAddresses() {
        return Observable.create(emitter -> {
            // Set up listener
            String listenerKey = "addresses_" + userId + "_listener";
            ListenerRegistration listener = activeListeners.get(listenerKey);
            
            if (listener != null) {
                // Remove old listener
                listener.remove();
            }
            
            // Create new listener
            listener = getUserCollectionReference(COLLECTION_ADDRESSES)
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            if (!emitter.isDisposed()) {
                                emitter.onError(e);
                            }
                            return;
                        }
                        
                        if (querySnapshot != null) {
                            List<Address> addresses = new ArrayList<>();
                            
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Address address = doc.toObject(Address.class);
                                if (address != null) {
                                    addresses.add(address);
                                }
                            }
                            
                            // Cache the result
                            putInCache("addresses_" + userId, addresses);
                            
                            // Emit to subscribers
                            if (!emitter.isDisposed()) {
                                emitter.onNext(addresses);
                            }
                        }
                    });
            
            // Store listener for cleanup
            activeListeners.put(listenerKey, listener);
            
            // Clean up when disposed
            emitter.setCancellable(() -> {
                if (listener != null) {
                    listener.remove();
                    activeListeners.remove(listenerKey);
                }
            });
            
            // Initially load from cache or Firestore
            List<Address> cached = getFromCache("addresses_" + userId);
            if (cached != null) {
                emitter.onNext(cached);
            } else {
                getAddresses()
                        .subscribe(
                                emitter::onNext,
                                throwable -> Log.e(TAG, "Error loading initial addresses", throwable)
                        );
            }
        });
    }
    
    @Override
    public Observable<Address> observeAddress(String addressId) {
        return Observable.create(emitter -> {
            // Set up listener
            String listenerKey = "address_" + addressId + "_listener";
            ListenerRegistration listener = activeListeners.get(listenerKey);
            
            if (listener != null) {
                // Remove old listener
                listener.remove();
            }
            
            // Create new listener
            DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document(addressId);
            listener = docRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                    }
                    return;
                }
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Address address = documentSnapshot.toObject(Address.class);
                    if (address != null) {
                        // Update cache
                        putInCache("address_" + addressId, address);
                        
                        // Emit to subscribers
                        if (!emitter.isDisposed()) {
                            emitter.onNext(address);
                        }
                    }
                }
            });
            
            // Store listener for cleanup
            activeListeners.put(listenerKey, listener);
            
            // Clean up when disposed
            emitter.setCancellable(() -> {
                if (listener != null) {
                    listener.remove();
                    activeListeners.remove(listenerKey);
                }
            });
            
            // Initially load from cache or Firestore
            Address cached = getFromCache("address_" + addressId);
            if (cached != null) {
                emitter.onNext(cached);
            } else {
                getAddressById(addressId)
                        .subscribe(
                                emitter::onNext,
                                throwable -> Log.e(TAG, "Error loading initial address", throwable)
                        );
            }
        });
    }
    
    //-----------------------------------------------------------------------------------
    // Implementing additional DataRepository methods would follow the same pattern
    // For brevity, we'll omit the full implementation of all methods
    //-----------------------------------------------------------------------------------
    
    @Override
    public Single<List<Delivery>> getDeliveries(int limit, DocumentReference startAfter) {
        // Implementation would be similar to getAddresses but with pagination
        return Single.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Single<List<Delivery>> getDeliveriesByTimeRange(Date startDate, Date endDate) {
        // Implementation would query deliveries within the time range
        return Single.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Single<List<Delivery>> getDeliveriesByAddress(String addressId) {
        // Implementation would query deliveries for a specific address
        return Single.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Single<Delivery> getDeliveryById(String deliveryId) {
        // Implementation would be similar to getAddressById
        return Single.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Single<DocumentReference> addDelivery(Delivery delivery) {
        // Implementation would be similar to addAddress
        return Single.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Completable updateDelivery(Delivery delivery) {
        // Implementation would be similar to updateAddress
        return Completable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Completable updateDeliveryTip(String deliveryId, double tipAmount) {
        // Implementation would update just the tip amount for a delivery
        return Completable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Completable deleteDelivery(String deliveryId) {
        // Implementation would be similar to deleteAddress
        return Completable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Observable<List<Delivery>> observeDeliveries() {
        // Implementation would be similar to observeAddresses
        return Observable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Observable<Delivery> observeDelivery(String deliveryId) {
        // Implementation would be similar to observeAddress
        return Observable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Single<Map<String, DeliveryStats>> getDeliveryStats() {
        // Implementation would calculate stats for different time periods
        return Single.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Completable syncData() {
        // Implementation would trigger sync of all data
        return Completable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Single<SyncStatus> getSyncStatus() {
        // Return the current sync status
        return Single.just(syncStatusSubject.getValue());
    }
    
    @Override
    public Completable enqueueSyncOperation(SyncOperation operation) {
        // Implementation would add operation to sync queue
        return Completable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Single<List<SyncOperation>> getPendingSyncOperations() {
        // Implementation would return pending operations from Firestore
        return Single.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Completable processPendingSyncOperations() {
        // Implementation would process pending operations
        return Completable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Completable updateDeviceSyncStatus(String deviceId, SyncStatus syncStatus) {
        // Implementation would update device sync status in Firestore
        return Completable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Observable<SyncStatus> observeSyncStatus() {
        // Return the sync status subject as an Observable
        return syncStatusSubject;
    }
    
    @Override
    public Single<AppConfig> getAppConfig() {
        // Implementation would get app config from Firestore
        return Single.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Observable<AppConfig> observeAppConfig() {
        // Implementation would be similar to observeUserProfile but for app config
        return Observable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Completable registerDevice(Map<String, Object> deviceInfo) {
        // Implementation would register device in Firestore
        return Completable.error(new UnsupportedOperationException("Not implemented yet"));
    }
    
    @Override
    public Completable updateDeviceLastActive() {
        // Implementation would update device last active timestamp
        return Completable.error(new UnsupportedOperationException("Not implemented yet"));
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
        // Implementation would prefetch essential data
        return Completable.error(new UnsupportedOperationException("Not implemented yet"));
    }
}
