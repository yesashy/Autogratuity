//-----------------------------------------------------------------------------------
// Implementation of App Config operations
//-----------------------------------------------------------------------------------

@Override
public Single<AppConfig> getAppConfig() {
    String cacheKey = "app_config";
    
    return Single.create(emitter -> {
        // First try memory cache
        AppConfig cached = getFromCache(cacheKey);
        if (cached != null) {
            emitter.onSuccess(cached);
            return;
        }
        
        // Check if we have a stored config
        AppConfig storedConfig = getFromPrefs(KEY_APP_CONFIG, AppConfig.class);
        if (storedConfig != null) {
            // Put in memory cache and return
            putInCache(cacheKey, storedConfig);
            emitter.onSuccess(storedConfig);
            
            // Refresh in background
            fetchAppConfig()
                    .subscribe(
                            config -> {
                                // Update cache
                                putInCache(cacheKey, config);
                                saveToPrefs(KEY_APP_CONFIG, config);
                            },
                            error -> Log.e(TAG, "Error fetching app config in background", error)
                    );
            return;
        }
        
        // Fetch from Firestore
        fetchAppConfig()
                .subscribe(
                        config -> {
                            // Cache and return
                            putInCache(cacheKey, config);
                            saveToPrefs(KEY_APP_CONFIG, config);
                            emitter.onSuccess(config);
                        },
                        error -> {
                            handleFirestoreError(error, "getting app config");
                            
                            // If we have a local fallback config, use it
                            if (storedConfig != null) {
                                emitter.onSuccess(storedConfig);
                            } else {
                                // Create a default config
                                AppConfig defaultConfig = createDefaultAppConfig();
                                emitter.onSuccess(defaultConfig);
                            }
                        }
                );
    });
}

/**
 * Helper method to fetch app configuration from Firestore
 * 
 * @return Single that emits the app configuration
 */
private Single<AppConfig> fetchAppConfig() {
    return Single.create(emitter -> {
        DocumentReference docRef = db.collection(COLLECTION_SYSTEM_CONFIG).document("app_config");
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        AppConfig config = documentSnapshot.toObject(AppConfig.class);
                        if (config != null) {
                            emitter.onSuccess(config);
                        } else {
                            emitter.onError(new Exception("Failed to parse app config"));
                        }
                    } else {
                        emitter.onError(new Exception("App config not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(e, "fetching app config");
                    emitter.onError(e);
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
    sync.setConflictStrategy(SyncOperation.CONFLICT_RESOLUTION_SERVER_WINS);
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
    return Observable.create(emitter -> {
        // Set up listener
        String listenerKey = "app_config_listener";
        ListenerRegistration listener = activeListeners.get(listenerKey);
        
        if (listener != null) {
            // Remove old listener
            listener.remove();
        }
        
        // Create new listener
        DocumentReference docRef = db.collection(COLLECTION_SYSTEM_CONFIG).document("app_config");
        listener = docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                if (!emitter.isDisposed()) {
                    emitter.onError(e);
                }
                return;
            }
            
            if (documentSnapshot != null && documentSnapshot.exists()) {
                AppConfig config = documentSnapshot.toObject(AppConfig.class);
                if (config != null) {
                    // Update cache
                    putInCache("app_config", config);
                    saveToPrefs(KEY_APP_CONFIG, config);
                    
                    // Emit to subscribers
                    if (!emitter.isDisposed()) {
                        emitter.onNext(config);
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
        AppConfig cached = getFromCache("app_config");
        if (cached != null) {
            emitter.onNext(cached);
        } else {
            getAppConfig()
                    .subscribe(
                            emitter::onNext,
                            throwable -> Log.e(TAG, "Error loading initial app config", throwable)
                    );
        }
    });
}

//-----------------------------------------------------------------------------------
// Implementation of Device Management operations
//-----------------------------------------------------------------------------------

@Override
public Completable registerDevice(Map<String, Object> deviceInfo) {
    if (deviceInfo == null) {
        deviceInfo = new HashMap<>();
    }
    
    // Ensure required fields
    deviceInfo.put("userId", userId);
    deviceInfo.put("deviceId", deviceId);
    
    if (!deviceInfo.containsKey("platform")) {
        deviceInfo.put("platform", "android");
    }
    
    if (!deviceInfo.containsKey("lastActive")) {
        deviceInfo.put("lastActive", new Date());
    }
    
    // Add default settings if not provided
    if (!deviceInfo.containsKey("settings")) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("syncEnabled", true);
        settings.put("notificationsEnabled", true);
        deviceInfo.put("settings", settings);
    }
    
    // Add metadata
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("createdAt", new Date());
    metadata.put("updatedAt", new Date());
    metadata.put("firstLoginAt", new Date());
    deviceInfo.put("metadata", metadata);
    
    // Add capabilities
    Map<String, Object> capabilities = new HashMap<>();
    capabilities.put("supportsBackgroundSync", true);
    capabilities.put("supportsNotifications", true);
    capabilities.put("supportsOfflineMode", true);
    deviceInfo.put("capabilities", capabilities);
    
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
                            handleFirestoreError(e, "updating existing device");
                            emitter.onError(e);
                        });
                    } else {
                        // Create new device record
                        docRef.set(deviceInfo)
                                .addOnSuccessListener(aVoid -> {
                                    // Also update user profile with device ID
                                    addDeviceToUserProfile(deviceId)
                                            .subscribe(
                                                    () -> emitter.onComplete(),
                                                    emitter::onError
                                            );
                                })
                                .addOnFailureListener(e -> {
                                    handleFirestoreError(e, "registering device");
                                    emitter.onError(e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(e, "checking if device exists");
                    emitter.onError(e);
                });
    });
}

/**
 * Helper method to add device ID to user profile
 * 
 * @param deviceId Device ID to add
 * @return Completable that completes when update is finished
 */
private Completable addDeviceToUserProfile(String deviceId) {
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
            });
}

@Override
public Completable updateDeviceLastActive() {
    String docId = userId + "_" + deviceId;
    DocumentReference docRef = db.collection(COLLECTION_USER_DEVICES).document(docId);
    
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
                    } else {
                        handleFirestoreError(e, "updating device last active");
                        emitter.onError(e);
                    }
                });
    });
}

//-----------------------------------------------------------------------------------
// Implementation of Cache Management operations
//-----------------------------------------------------------------------------------

@Override
public Completable clearCaches() {
    // Clear memory cache
    memoryCache.clear();
    cacheTimestamps.clear();
    
    return Completable.complete();
}

@Override
public Completable prefetchCriticalData() {
    // Prefetch critical data for improved performance
    return Completable.mergeArray(
            // Prefetch user profile
            getUserProfile().ignoreElement(),
            
            // Prefetch subscription status
            getSubscriptionStatus().ignoreElement(),
            
            // Prefetch app config
            getAppConfig().ignoreElement()
    );
}
