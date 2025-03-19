package com.autogratuity.data.repository.config;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.autogratuity.data.model.AppConfig;
import com.autogratuity.data.model.ErrorInfo;
import com.autogratuity.data.model.SyncOperation;
import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.repository.core.FirestoreRepository;
import com.autogratuity.data.repository.core.RepositoryEventBus;
import com.autogratuity.data.repository.utils.RepositoryConstants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Implementation of the ConfigRepository interface that provides
 * configuration management and device registration functionality.
 * 
 * This class demonstrates the standardized architectural patterns for:
 * - Consistent caching using CacheStrategy
 * - Standardized error handling using RepositoryErrorHandler
 * - Consistent RxJava patterns for repository operations
 */
public class ConfigRepositoryImpl extends FirestoreRepository implements ConfigRepository, RepositoryEventBus.EventListener {
    
    private static final String TAG = "ConfigRepository";
    
    // Event bus for cross-repository communication
    private final RepositoryEventBus eventBus;
    
    /**
     * Constructor for ConfigRepositoryImpl
     * 
     * @param context Android context for SharedPreferences and connectivity
     */
    public ConfigRepositoryImpl(Context context) {
        super(context);
        
        // Initialize event bus and register as listener
        this.eventBus = RepositoryEventBus.getInstance();
        this.eventBus.register(RepositoryEventBus.EventType.CONFIG_REPOSITORY, this);
    }
    
    //-----------------------------------------------------------------------------------
    // Implementation of App Config operations
    //-----------------------------------------------------------------------------------
    
    @Override
    public Single<AppConfig> getAppConfig() {
        final String entityType = RepositoryConstants.EntityType.APP_CONFIG;
        final String operationName = RepositoryConstants.operationName(
                RepositoryConstants.OperationName.GET, entityType, null);
        final String cacheKey = RepositoryConstants.CachePrefix.APP_CONFIG;
        
        // Use standardized cache-then-source pattern
        return getWithCache(cacheKey, ignored -> fetchAppConfig())
                .compose(applyReadTransformer(entityType, operationName));
    }
    
    /**
     * Helper method to fetch app configuration from Firestore
     * 
     * @return Single that emits the app configuration
     */
    private Single<AppConfig> fetchAppConfig() {
        final String entityType = RepositoryConstants.EntityType.APP_CONFIG;
        final String operationName = "fetch app config";
        
        return Single.create(emitter -> {
            DocumentReference docRef = db.collection(COLLECTION_SYSTEM_CONFIG).document("app_config");
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            AppConfig config = documentSnapshot.toObject(AppConfig.class);
                            if (config != null) {
                                // Cache and emit
                                final String cacheKey = RepositoryConstants.CachePrefix.APP_CONFIG;
                                putInCache(cacheKey, config, 30, TimeUnit.MINUTES);
                                emitter.onSuccess(config);
                            } else {
                                emitter.onError(new Exception("Failed to parse app config"));
                            }
                        } else {
                            // If no config exists, create a default one
                            AppConfig defaultConfig = createDefaultAppConfig();
                            emitter.onSuccess(defaultConfig);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Use standardized error handling
                        ErrorInfo errorInfo = handleFirestoreError(e, operationName, entityType);
                        
                        // Provide a fallback config when offline
                        if (!isNetworkAvailable() || errorInfo.isNetworkError()) {
                            AppConfig defaultConfig = createDefaultAppConfig();
                            emitter.onSuccess(defaultConfig);
                        } else {
                            emitter.onError(new Exception(errorInfo.getUserFriendlyMessage(), e));
                        }
                    });
        });
    }
    
    /**
     * Create a default app configuration
     * 
     * @return Default app configuration
     */
    private AppConfig createDefaultAppConfig() {
        AppConfig config = new AppConfig();
        
        // Set up versions
        AppConfig.Versions versions = new AppConfig.Versions();
        versions.setMinimum("1.0.0");
        versions.setRecommended("1.0.0");
        versions.setLatest("1.0.0");
        config.setVersions(versions);
        
        // Set up features
        AppConfig.Features features = new AppConfig.Features();
        features.setUseNewSyncSystem(true);
        features.setEnableOfflineMode(true);
        features.setEnableAnalytics(true);
        features.setEnableBackgroundSync(true);
        features.setEnforceVersionCheck(false);
        config.setFeatures(features);
        
        // Set up tier limits
        AppConfig.Limits limits = new AppConfig.Limits();
        
        AppConfig.Limits.TierLimits freeTier = new AppConfig.Limits.TierLimits();
        freeTier.setMappingLimit(100);
        freeTier.setImportLimit(25);
        freeTier.setExportLimit(100);
        
        AppConfig.Limits.TierLimits proTier = new AppConfig.Limits.TierLimits();
        proTier.setMappingLimit(-1); // Unlimited
        proTier.setImportLimit(-1);
        proTier.setExportLimit(-1);
        
        limits.setFreeTier(freeTier);
        limits.setProTier(proTier);
        config.setLimits(limits);
        
        // Set up sync config
        AppConfig.Sync sync = new AppConfig.Sync();
        sync.setInterval(60); // 60 seconds
        sync.setBackgroundInterval(120); // 2 minutes
        sync.setMaxBatchSize(50);
        sync.setConflictStrategy("SERVER_WINS");
        config.setSync(sync);
        
        // Set up maintenance info
        AppConfig.Maintenance maintenance = new AppConfig.Maintenance();
        maintenance.setInMaintenance(false);
        maintenance.setMaintenanceMessage("");
        config.setMaintenance(maintenance);
        
        return config;
    }
    
    @Override
    public Observable<AppConfig> observeAppConfig() {
        final String entityType = RepositoryConstants.EntityType.APP_CONFIG;
        final String operationName = RepositoryConstants.operationName(
                RepositoryConstants.OperationName.OBSERVE, entityType, null);
        final String cacheKey = RepositoryConstants.CachePrefix.APP_CONFIG;
        final String listenerKey = "app_config_listener";
        
        return Observable.create(emitter -> {
            // Set up listener
            final ListenerRegistration listenerRef = activeListeners.get(listenerKey);
            
            if (listenerRef != null) {
                // Remove old listener
                listenerRef.remove();
            }
            
            // Create new listener
            final DocumentReference docRef = db.collection(COLLECTION_SYSTEM_CONFIG).document("app_config");
            final ListenerRegistration newListener = docRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    handleFirestoreError(e, operationName, entityType);
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                    }
                    return;
                }
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    AppConfig config = documentSnapshot.toObject(AppConfig.class);
                    if (config != null) {
                        // Cache and emit
                        putInCache(cacheKey, config, 30, TimeUnit.MINUTES);
                        
                        // Emit to subscribers
                        if (!emitter.isDisposed()) {
                            emitter.onNext(config);
                        }
                    }
                }
            });
            
            // Store listener for cleanup
            activeListeners.put(listenerKey, newListener);
            
            // Clean up when disposed
            emitter.setCancellable(() -> {
                activeListeners.get(listenerKey).remove();
                activeListeners.remove(listenerKey);
            });
            
            // Initially load from cache or Firestore
            AppConfig cached = getFromCache(cacheKey);
            if (cached != null) {
                emitter.onNext(cached);
            } else {
                getAppConfig()
                        .subscribe(
                                emitter::onNext,
                                throwable -> Log.e(TAG, "Error loading initial app config", throwable)
                        );
            }
        })
        .compose(applyObserveTransformer(entityType, operationName));
    }
    
    //-----------------------------------------------------------------------------------
    // Implementation of Device Management operations
    //-----------------------------------------------------------------------------------
    
    @Override
    public Completable registerDevice(Map<String, Object> deviceInfo) {
        final String entityType = RepositoryConstants.EntityType.DEVICE;
        final String operationName = RepositoryConstants.operationName(
                RepositoryConstants.OperationName.ADD, entityType, null);
        
        if (deviceInfo == null) {
            deviceInfo = new HashMap<>();
        }
        
        // Ensure required fields
        final Map<String, Object> finalDeviceInfo = new HashMap<>(deviceInfo);
        finalDeviceInfo.put("userId", userId);
        finalDeviceInfo.put("deviceId", deviceId);
        
        if (!finalDeviceInfo.containsKey("platform")) {
            finalDeviceInfo.put("platform", "android");
        }
        
        if (!finalDeviceInfo.containsKey("lastActive")) {
            finalDeviceInfo.put("lastActive", new Date());
        }
        
        // Add default settings if not provided
        if (!finalDeviceInfo.containsKey("settings")) {
            Map<String, Object> settings = new HashMap<>();
            settings.put("syncEnabled", true);
            settings.put("notificationsEnabled", true);
            finalDeviceInfo.put("settings", settings);
        }
        
        // Add metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdAt", new Date());
        metadata.put("updatedAt", new Date());
        metadata.put("firstLoginAt", new Date());
        finalDeviceInfo.put("metadata", metadata);
        
        // Add capabilities
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsBackgroundSync", true);
        capabilities.put("supportsNotifications", true);
        capabilities.put("supportsOfflineMode", true);
        finalDeviceInfo.put("capabilities", capabilities);
        
        String docId = userId + "_" + deviceId;
        DocumentReference docRef = db.collection(COLLECTION_USER_DEVICES).document(docId);
        
        return Completable.create(emitter -> {
            // First check if device already registered
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Device already registered, update it
                            docRef.update(
                                    "lastActive", new Date(),
                                    "metadata.updatedAt", new Date()
                            )
                            .addOnSuccessListener(aVoid -> {
                                emitter.onComplete();
                            })
                            .addOnFailureListener(e -> {
                                // Use standardized error handling
                                ErrorInfo errorInfo = handleFirestoreError(e, "updating existing device", entityType);
                                emitter.onError(new Exception(errorInfo.getUserFriendlyMessage(), e));
                            });
                        } else {
                            // Create new device record
                            docRef.set(finalDeviceInfo)
                                    .addOnSuccessListener(aVoid -> {
                                        // Also update user profile with device ID
                                        addDeviceToUserProfile(deviceId)
                                                .subscribe(
                                                        () -> emitter.onComplete(),
                                                        emitter::onError
                                                );
                                    })
                                    .addOnFailureListener(e -> {
                                        // Check if offline
                                        if (!isNetworkAvailable()) {
                                            // Create sync operation for when we're back online through event bus
                                            SyncOperation operation = new SyncOperation(
                                            userId,
                                            "create",
                                            entityType,
                                            docId,
                    finalDeviceInfo);
            
            // Post event to sync repository
            postSyncOperationEvent(operation)
                    .subscribe(
                            () -> emitter.onComplete(),
                            emitter::onError
                    );
                                        } else {
                                            // Use standardized error handling
                                            ErrorInfo errorInfo = handleFirestoreError(e, "registering device", entityType);
                                            emitter.onError(new Exception(errorInfo.getUserFriendlyMessage(), e));
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Use standardized error handling
                        ErrorInfo errorInfo = handleFirestoreError(e, "checking if device exists", entityType);
                        emitter.onError(new Exception(errorInfo.getUserFriendlyMessage(), e));
                    });
        })
        .compose(applyWriteTransformer(entityType, operationName));
    }
    
    /**
     * Helper method to add device ID to user profile
     * 
     * @param deviceId Device ID to add
     * @return Completable that completes when update is finished
     */
    private Completable addDeviceToUserProfile(String deviceId) {
        final String entityType = RepositoryConstants.EntityType.USER_PROFILE;
        final String operationName = "add device to user profile";
        
        return getUserProfile()
                .flatMapCompletable(profile -> {
                    if (profile.getSyncInfo() == null) {
                        profile.setSyncInfo(new UserProfile.SyncInfo());
                    }
                    
                    if (profile.getSyncInfo().getDeviceIds() == null) {
                        profile.getSyncInfo().setDeviceIds(new ArrayList<>());
                    }
                    
                    List<String> deviceIds = profile.getSyncInfo().getDeviceIds();
                    if (!deviceIds.contains(deviceId)) {
                        deviceIds.add(deviceId);
                        return updateUserProfile(profile);
                    } else {
                        return Completable.complete();
                    }
                })
                .compose(applyWriteTransformer(entityType, operationName));
    }
    
    @Override
    public Completable updateDeviceLastActive() {
        final String entityType = RepositoryConstants.EntityType.DEVICE;
        final String operationName = RepositoryConstants.operationName(
                RepositoryConstants.OperationName.UPDATE, entityType, null);
        
        String docId = userId + "_" + deviceId;
        final DocumentReference docRef = db.collection(COLLECTION_USER_DEVICES).document(docId);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastActive", new Date());
        updates.put("metadata.updatedAt", new Date());
        
        return Completable.create(emitter -> {
            docRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        emitter.onComplete();
                    })
                    .addOnFailureListener(e -> {
                        // If the device doesn't exist, register it
                        if (e instanceof com.google.firebase.firestore.FirebaseFirestoreException &&
                                ((com.google.firebase.firestore.FirebaseFirestoreException) e).getCode() == 
                                        com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND) {
                            
                            registerDevice(null)
                                    .subscribe(
                                            emitter::onComplete,
                                            emitter::onError
                                    );
                        } else if (!isNetworkAvailable()) {
                            // Create sync operation for when we're back online through event bus
                            SyncOperation operation = new SyncOperation(
                            userId,
                            "update",
                            entityType,
                            docId,
                    updates);
            
            // Post event to sync repository
            postSyncOperationEvent(operation)
                    .subscribe(
                            () -> emitter.onComplete(),
                            emitter::onError
                    );
                        } else {
                            // Use standardized error handling
                            ErrorInfo errorInfo = handleFirestoreError(e, "updating device last active", entityType);
                            emitter.onError(new Exception(errorInfo.getUserFriendlyMessage(), e));
                        }
                    });
        })
        .compose(applyWriteTransformer(entityType, operationName));
    }
    
    //-----------------------------------------------------------------------------------
    // Implementation of Cache Management operations
    //-----------------------------------------------------------------------------------
    
    @Override
    public Completable clearCaches() {
        // First call super implementation to clear memory cache
        return super.clearCaches();
    }
    
    @Override
    public Completable prefetchCriticalData() {
        final String operationName = "prefetch critical data";
        
        // Prefetch critical data for improved performance
        return Completable.mergeArray(
                // Prefetch user profile
                getUserProfile().ignoreElement(),
                
                // Prefetch subscription status
                getSubscriptionStatus().ignoreElement(),
                
                // Prefetch app config
                getAppConfig().ignoreElement()
        )
        .compose(applyWriteTransformer("all", operationName));
    }

    @Override
    public String getConfigValue(String key, String defaultValue) {
        final String cachePrefixConfigValue = "config_value_";
        final String cacheKey = cachePrefixConfigValue + key;
        
        try {
            // First try cache
            String cachedValue = getFromCache(cacheKey);
            if (cachedValue != null) {
                return cachedValue;
            }
            
            // Get from config
            AppConfig config = getAppConfig().blockingGet();
            if (config != null && config.getCustomData() != null && config.getCustomData().containsKey(key)) {
                Object value = config.getCustomData().get(key);
                String stringValue = value != null ? value.toString() : defaultValue;
                
                // Cache for future use
                putInCache(cacheKey, stringValue, 1, TimeUnit.HOURS);
                
                return stringValue;
            }
            return defaultValue;
        } catch (Exception e) {
            Log.e(TAG, "Error getting config value: " + key, e);
            return defaultValue;
        }
    }
    
    @Override
    public boolean getConfigBoolean(String key, boolean defaultValue) {
        final String cachePrefixConfigBoolean = "config_boolean_";
        final String cacheKey = cachePrefixConfigBoolean + key;
        
        try {
            // First try cache
            Boolean cachedValue = getFromCache(cacheKey);
            if (cachedValue != null) {
                return cachedValue;
            }
            
            // Get from config
            AppConfig config = getAppConfig().blockingGet();
            if (config != null && config.getCustomData() != null && config.getCustomData().containsKey(key)) {
                Object value = config.getCustomData().get(key);
                boolean boolValue;
                
                if (value instanceof Boolean) {
                    boolValue = (Boolean) value;
                } else if (value != null) {
                    boolValue = Boolean.parseBoolean(value.toString());
                } else {
                    boolValue = defaultValue;
                }
                
                // Cache for future use
                putInCache(cacheKey, boolValue, 1, TimeUnit.HOURS);
                
                return boolValue;
            }
            return defaultValue;
        } catch (Exception e) {
            Log.e(TAG, "Error getting config boolean: " + key, e);
            return defaultValue;
        }
    }
    
    @Override
    public Completable incrementCounter(String counterKey) {
        final String entityType = "counter";
        final String operationName = "increment counter";
        
        return Completable.defer(() -> {
            DocumentReference docRef = db.collection(COLLECTION_SYSTEM_CONFIG)
                    .document("counters");
            
            return Completable.create(emitter -> {
                docRef.update(counterKey, com.google.firebase.firestore.FieldValue.increment(1))
                        .addOnSuccessListener(aVoid -> emitter.onComplete())
                        .addOnFailureListener(e -> {
                            if (e instanceof com.google.firebase.firestore.FirebaseFirestoreException &&
                                    ((com.google.firebase.firestore.FirebaseFirestoreException) e).getCode() == 
                                            com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND) {
                                // Create if not exists
                                Map<String, Object> initialData = new HashMap<>();
                                initialData.put(counterKey, 1);
                                docRef.set(initialData)
                                        .addOnSuccessListener(aVoid -> emitter.onComplete())
                                        .addOnFailureListener(innerE -> {
                                            // Use standardized error handling
                                            ErrorInfo errorInfo = handleFirestoreError(
                                                    innerE, "creating counter document", entityType);
                                            emitter.onError(new Exception(errorInfo.getUserFriendlyMessage(), innerE));
                                        });
                            } else if (!isNetworkAvailable()) {
                                // Create sync operation for when we're back online
                                Map<String, Object> updates = new HashMap<>();
                                updates.put(counterKey, 1); // Will be incremented
                                
                                enqueueOperation("update", entityType, "counters", updates)
                                        .subscribe(
                                                () -> emitter.onComplete(),
                                                emitter::onError
                                        );
                            } else {
                                // Use standardized error handling
                                ErrorInfo errorInfo = handleFirestoreError(e, "incrementing counter", entityType);
                                emitter.onError(new Exception(errorInfo.getUserFriendlyMessage(), e));
                            }
                        });
            });
        })
        .compose(applyWriteTransformer(entityType, operationName));
    }
    
    @Override
    public Completable noOpCompletable() {
        return Completable.complete()
                .compose(applyWriteTransformer("none", "no-op operation"));
    }
    
    /**
     * Post a sync operation event to the event bus
     * 
     * @param operation The sync operation to post
     * @return Completable that completes when the event is posted
     */
    private Completable postSyncOperationEvent(SyncOperation operation) {
        return Completable.fromAction(() -> {
            Map<String, Object> data = new HashMap<>();
            data.put("operation", operation);
            
            eventBus.post(
                    RepositoryEventBus.EventType.SYNC_OPERATION_ENQUEUED,
                    RepositoryEventBus.EventType.CONFIG_REPOSITORY,
                    data,
                    RepositoryEventBus.EventType.SYNC_REPOSITORY);
        });
    }
    
    /**
     * Handle events from the event bus
     * 
     * @param event The repository event
     */
    @Override
    public void onEvent(RepositoryEventBus.RepositoryEvent event) {
        if (event == null) {
            return;
        }
        
        Log.d(TAG, "Received event: " + event.getType() + " from " + event.getSource());
        
        switch (event.getType()) {
            case RepositoryEventBus.EventType.SYNC_STATUS_CHANGED:
                // Handle sync status change
                break;
                
            case RepositoryEventBus.EventType.SYNC_OPERATION_COMPLETED:
                // Handle sync operation completion
                break;
                
            case RepositoryEventBus.EventType.SYNC_OPERATION_FAILED:
                // Handle sync operation failure
                break;
                
            default:
                // Ignore other events
                break;
        }
    }
    
    /**
     * Clean up resources when repository is no longer needed
     */
    public void cleanup() {
        // Unregister from event bus
        eventBus.unregister(RepositoryEventBus.EventType.CONFIG_REPOSITORY, this);
        
        // Clean up any other resources
        super.cleanup();
    }
}
