//-----------------------------------------------------------------------------------
// Implementation of Sync operations
//-----------------------------------------------------------------------------------

@Override
public Completable syncData() {
    // Update sync status
    SyncStatus status = syncStatusSubject.getValue();
    status.setStatus(SyncStatus.STATUS_SYNCING);
    syncStatusSubject.onNext(status);
    
    // Check network availability
    if (!isNetworkAvailable()) {
        status.setStatus(SyncStatus.STATUS_OFFLINE);
        syncStatusSubject.onNext(status);
        return Completable.error(new Exception("Cannot sync while offline"));
    }
    
    // Define sync operations
    return Completable.mergeArray(
            // Sync user profile
            getUserProfile(true).ignoreElement(),
            
            // Sync subscription status
            getSubscriptionStatus().ignoreElement(),
            
            // Sync addresses
            getAddresses().ignoreElement(),
            
            // Sync deliveries (last 30 days)
            getDeliveriesByTimeRange(
                    new Date(System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)), 
                    new Date())
                    .ignoreElement(),
            
            // Process any pending sync operations
            processPendingSyncOperations()
    )
    .doOnComplete(() -> {
        // Update sync status on completion
        SyncStatus updatedStatus = syncStatusSubject.getValue();
        updatedStatus.setStatus(SyncStatus.STATUS_IDLE);
        updatedStatus.setLastSyncTime(new Date());
        syncStatusSubject.onNext(updatedStatus);
        
        // Update the last sync time in preferences
        prefs.edit()
                .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                .apply();
                
        // Update device sync status in Firestore
        updateDeviceSyncStatus(deviceId, updatedStatus)
                .subscribe(
                        () -> Log.d(TAG, "Device sync status updated"),
                        error -> Log.e(TAG, "Error updating device sync status", error)
                );
    })
    .doOnError(error -> {
        // Update sync status on error
        SyncStatus updatedStatus = syncStatusSubject.getValue();
        updatedStatus.setStatus(SyncStatus.STATUS_ERROR);
        updatedStatus.setLastError(error.getMessage());
        updatedStatus.setLastFailedSyncTime(new Date());
        syncStatusSubject.onNext(updatedStatus);
    });
}

@Override
public Completable enqueueSyncOperation(SyncOperation operation) {
    // Validate operation
    if (operation == null) {
        return Completable.error(new IllegalArgumentException("Operation cannot be null"));
    }
    
    // Set user ID if not already set
    if (operation.getUserId() == null || operation.getUserId().isEmpty()) {
        operation.setUserId(userId);
    }
    
    // Set device ID
    operation.setDeviceId(deviceId);
    
    // Set timestamps
    Date now = new Date();
    operation.setCreatedAt(now);
    operation.setUpdatedAt(now);
    
    // Generate operation ID if not already set
    String operationId = operation.getOperationId();
    if (operationId == null || operationId.isEmpty()) {
        operationId = UUID.randomUUID().toString();
        operation.setOperationId(operationId);
    }
    
    DocumentReference docRef = db.collection(COLLECTION_SYNC_OPERATIONS).document(operationId);
    
    return Completable.create(emitter -> {
        docRef.set(operation)
                .addOnSuccessListener(aVoid -> {
                    // Update sync status
                    SyncStatus status = syncStatusSubject.getValue();
                    status.setPendingOperations(status.getPendingOperations() + 1);
                    syncStatusSubject.onNext(status);
                    
                    // Process immediately if online
                    if (isNetworkAvailable() && !"pending".equals(status.getStatus())) {
                        processSyncOperation(operation)
                                .subscribe(
                                        () -> emitter.onComplete(),
                                        emitter::onError
                                );
                    } else {
                        emitter.onComplete();
                    }
                })
                .addOnFailureListener(e -> {
                    // If we're offline, store locally
                    if (!isNetworkAvailable()) {
                        // This would store the operation locally for later processing
                        // For simplicity, we'll just update the sync status
                        SyncStatus status = syncStatusSubject.getValue();
                        status.setPendingOperations(status.getPendingOperations() + 1);
                        syncStatusSubject.onNext(status);
                        
                        // For a real implementation, we would store in a local database
                        emitter.onComplete();
                    } else {
                        handleFirestoreError(e, "enqueueing sync operation");
                        emitter.onError(e);
                    }
                });
    });
}

@Override
public Single<List<SyncOperation>> getPendingSyncOperations() {
    return Single.create(emitter -> {
        // Query Firestore for pending operations
        db.collection(COLLECTION_SYNC_OPERATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", SyncOperation.STATUS_PENDING)
                .orderBy("priority", Query.Direction.DESCENDING) // Higher priority first
                .orderBy("createdAt", Query.Direction.ASCENDING) // Oldest first
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SyncOperation> operations = new ArrayList<>();
                    
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        SyncOperation operation = doc.toObject(SyncOperation.class);
                        if (operation != null) {
                            operations.add(operation);
                        }
                    }
                    
                    // Update sync status
                    SyncStatus status = syncStatusSubject.getValue();
                    status.setPendingOperations(operations.size());
                    syncStatusSubject.onNext(status);
                    
                    emitter.onSuccess(operations);
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(e, "getting pending sync operations");
                    emitter.onError(e);
                });
    });
}

@Override
public Completable processPendingSyncOperations() {
    // Check network availability
    if (!isNetworkAvailable()) {
        return Completable.error(new Exception("Cannot process sync operations while offline"));
    }
    
    return getPendingSyncOperations()
            .flatMapCompletable(operations -> {
                if (operations.isEmpty()) {
                    return Completable.complete();
                }
                
                List<Completable> completables = new ArrayList<>();
                for (SyncOperation operation : operations) {
                    completables.add(processSyncOperation(operation));
                }
                
                return Completable.concat(completables);
            });
}

/**
 * Process a single sync operation
 * 
 * @param operation The operation to process
 * @return Completable that completes when the operation is processed
 */
private Completable processSyncOperation(SyncOperation operation) {
    // Mark operation as in progress
    return updateSyncOperationStatus(operation, SyncOperation.STATUS_IN_PROGRESS)
            .andThen(Completable.defer(() -> {
                // Process based on type
                Completable processCompletable;
                
                switch (operation.getOperationType()) {
                    case "create":
                        processCompletable = processCreateOperation(operation);
                        break;
                    case "update":
                        processCompletable = processUpdateOperation(operation);
                        break;
                    case "delete":
                        processCompletable = processDeleteOperation(operation);
                        break;
                    case "updateTip":
                        processCompletable = processUpdateTipOperation(operation);
                        break;
                    default:
                        return Completable.error(
                                new UnsupportedOperationException("Unknown operation type: " + 
                                        operation.getOperationType()));
                }
                
                return processCompletable
                        .andThen(updateSyncOperationStatus(operation, SyncOperation.STATUS_COMPLETED))
                        .onErrorResumeNext(error -> {
                            // Mark as failed and increment attempts
                            operation.markAsFailed(
                                    error.getClass().getSimpleName(), 
                                    error.getMessage());
                            
                            if (operation.canRetry()) {
                                return updateSyncOperationStatus(operation, SyncOperation.STATUS_RETRYING);
                            } else {
                                // No more retries, mark as failed
                                return updateSyncOperationStatus(operation, SyncOperation.STATUS_FAILED);
                            }
                        });
            }));
}

/**
 * Process a create operation
 * 
 * @param operation The operation to process
 * @return Completable that completes when the operation is processed
 */
private Completable processCreateOperation(SyncOperation operation) {
    String entityType = operation.getEntityType();
    String entityId = operation.getEntityId();
    Map<String, Object> data = operation.getData();
    
    if (data == null) {
        return Completable.error(new IllegalArgumentException("Operation data cannot be null"));
    }
    
    DocumentReference docRef = db.collection(entityTypeToCollection(entityType)).document(entityId);
    
    return Completable.create(emitter -> {
        docRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    // Invalidate relevant caches
                    invalidateCachesForEntity(entityType, entityId);
                    
                    emitter.onComplete();
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(e, "processing create operation");
                    emitter.onError(e);
                });
    });
}

/**
 * Process an update operation
 * 
 * @param operation The operation to process
 * @return Completable that completes when the operation is processed
 */
private Completable processUpdateOperation(SyncOperation operation) {
    String entityType = operation.getEntityType();
    String entityId = operation.getEntityId();
    Map<String, Object> data = operation.getData();
    
    if (data == null) {
        return Completable.error(new IllegalArgumentException("Operation data cannot be null"));
    }
    
    DocumentReference docRef = db.collection(entityTypeToCollection(entityType)).document(entityId);
    
    return Completable.create(emitter -> {
        // First check if the document exists
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Check for conflicts and resolve
                        String conflictStrategy = operation.getConflictResolution();
                        Map<String, Object> previousVersion = operation.getPreviousVersion();
                        
                        // Handle conflict resolution
                        if (previousVersion != null && !previousVersion.isEmpty()) {
                            // This would compare versions and merge data
                            // For simplicity, we'll just use the update with transaction helper
                            updateWithTransaction(docRef, data, conflictStrategy)
                                    .subscribe(
                                            emitter::onComplete,
                                            emitter::onError
                                    );
                        } else {
                            // No version tracking, just update
                            docRef.update(data)
                                    .addOnSuccessListener(aVoid -> {
                                        // Invalidate relevant caches
                                        invalidateCachesForEntity(entityType, entityId);
                                        
                                        emitter.onComplete();
                                    })
                                    .addOnFailureListener(e -> {
                                        handleFirestoreError(e, "processing update operation");
                                        emitter.onError(e);
                                    });
                        }
                    } else {
                        // Document doesn't exist, create it
                        docRef.set(data)
                                .addOnSuccessListener(aVoid -> {
                                    // Invalidate relevant caches
                                    invalidateCachesForEntity(entityType, entityId);
                                    
                                    emitter.onComplete();
                                })
                                .addOnFailureListener(e -> {
                                    handleFirestoreError(e, "processing update operation (create)");
                                    emitter.onError(e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(e, "checking document for update operation");
                    emitter.onError(e);
                });
    });
}

/**
 * Process a delete operation
 * 
 * @param operation The operation to process
 * @return Completable that completes when the operation is processed
 */
private Completable processDeleteOperation(SyncOperation operation) {
    String entityType = operation.getEntityType();
    String entityId = operation.getEntityId();
    
    DocumentReference docRef = db.collection(entityTypeToCollection(entityType)).document(entityId);
    
    return Completable.create(emitter -> {
        docRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Invalidate relevant caches
                    invalidateCachesForEntity(entityType, entityId);
                    
                    emitter.onComplete();
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(e, "processing delete operation");
                    emitter.onError(e);
                });
    });
}

/**
 * Process an updateTip operation
 * 
 * @param operation The operation to process
 * @return Completable that completes when the operation is processed
 */
private Completable processUpdateTipOperation(SyncOperation operation) {
    String deliveryId = operation.getEntityId();
    Map<String, Object> data = operation.getData();
    
    if (data == null || !data.containsKey("tipAmount")) {
        return Completable.error(
                new IllegalArgumentException("Operation data must contain tipAmount"));
    }
    
    double tipAmount = ((Number) data.get("tipAmount")).doubleValue();
    
    return updateDeliveryTip(deliveryId, tipAmount);
}

/**
 * Update a sync operation's status
 * 
 * @param operation The operation to update
 * @param status The new status
 * @return Completable that completes when the update is finished
 */
private Completable updateSyncOperationStatus(SyncOperation operation, String status) {
    DocumentReference docRef = 
            db.collection(COLLECTION_SYNC_OPERATIONS).document(operation.getOperationId());
    
    Map<String, Object> updates = new HashMap<>();
    updates.put("status", status);
    updates.put("updatedAt", new Date());
    
    if (SyncOperation.STATUS_COMPLETED.equals(status)) {
        updates.put("completedAt", new Date());
    } else if (SyncOperation.STATUS_IN_PROGRESS.equals(status)) {
        updates.put("lastAttemptTime", new Date());
        updates.put("attempts", operation.getAttempts() + 1);
    } else if (SyncOperation.STATUS_FAILED.equals(status) || SyncOperation.STATUS_RETRYING.equals(status)) {
        updates.put("lastAttemptTime", new Date());
        updates.put("attempts", operation.getAttempts());
        
        if (operation.getError() != null) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("code", operation.getError().getCode());
            errorMap.put("message", operation.getError().getMessage());
            errorMap.put("timestamp", operation.getError().getTimestamp());
            updates.put("error", errorMap);
        }
        
        if (operation.getNextAttemptTime() != null) {
            updates.put("nextAttemptTime", operation.getNextAttemptTime());
        }
    }
    
    return Completable.create(emitter -> {
        docRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update in-memory operation object
                    operation.setStatus(status);
                    
                    // Update sync status
                    SyncStatus syncStatus = syncStatusSubject.getValue();
                    if (SyncOperation.STATUS_COMPLETED.equals(status)) {
                        syncStatus.setPendingOperations(Math.max(0, syncStatus.getPendingOperations() - 1));
                    } else if (SyncOperation.STATUS_FAILED.equals(status)) {
                        syncStatus.setFailedOperations(syncStatus.getFailedOperations() + 1);
                    }
                    syncStatusSubject.onNext(syncStatus);
                    
                    emitter.onComplete();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating sync operation status", e);
                    emitter.onError(e);
                });
    });
}

/**
 * Convert entity type to collection name
 * 
 * @param entityType Entity type
 * @return Collection name
 */
private String entityTypeToCollection(String entityType) {
    switch (entityType) {
        case "userProfile":
            return COLLECTION_USER_PROFILES;
        case "subscriptionRecord":
            return COLLECTION_SUBSCRIPTION_RECORDS;
        case "address":
            return COLLECTION_ADDRESSES;
        case "delivery":
            return COLLECTION_DELIVERIES;
        case "userDevice":
            return COLLECTION_USER_DEVICES;
        default:
            throw new IllegalArgumentException("Unknown entity type: " + entityType);
    }
}

/**
 * Invalidate caches for a specific entity
 * 
 * @param entityType Entity type
 * @param entityId Entity ID
 */
private void invalidateCachesForEntity(String entityType, String entityId) {
    switch (entityType) {
        case "userProfile":
            invalidateCache("userProfile_" + userId);
            break;
        case "subscriptionRecord":
            invalidateCache("subscriptionStatus_" + userId);
            break;
        case "address":
            invalidateCache("address_" + entityId);
            invalidateCache("addresses_" + userId);
            break;
        case "delivery":
            invalidateCache("delivery_" + entityId);
            invalidateCache("deliveries_" + userId + "_*");
            invalidateCache("delivery_stats_" + userId);
            break;
        case "userDevice":
            // No specific cache for user devices
            break;
    }
}

@Override
public Completable updateDeviceSyncStatus(String deviceId, SyncStatus syncStatus) {
    if (deviceId == null || deviceId.isEmpty()) {
        return Completable.error(new IllegalArgumentException("Device ID is required"));
    }
    
    String docId = userId + "_" + deviceId;
    DocumentReference docRef = db.collection(COLLECTION_USER_DEVICES).document(docId);
    
    Map<String, Object> updates = new HashMap<>();
    updates.put("lastSyncTime", syncStatus.getLastSyncTime());
    updates.put("lastActive", new Date());
    
    Map<String, Object> syncStatusMap = new HashMap<>();
    syncStatusMap.put("lastSyncStatus", syncStatus.getStatus());
    syncStatusMap.put("lastSyncError", syncStatus.getLastError());
    syncStatusMap.put("pendingOperations", syncStatus.getPendingOperations());
    updates.put("syncStatus", syncStatusMap);
    
    return Completable.create(emitter -> {
        docRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    emitter.onComplete();
                })
                .addOnFailureListener(e -> {
                    // If the document doesn't exist, we need to create it
                    if (e instanceof com.google.firebase.firestore.FirebaseFirestoreException &&
                            ((com.google.firebase.firestore.FirebaseFirestoreException) e).getCode() == 
                                    com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND) {
                        
                        Map<String, Object> deviceData = new HashMap<>();
                        deviceData.put("userId", userId);
                        deviceData.put("deviceId", deviceId);
                        deviceData.put("platform", "android");
                        deviceData.put("lastActive", new Date());
                        deviceData.put("lastSyncTime", syncStatus.getLastSyncTime());
                        deviceData.put("syncStatus", syncStatusMap);
                        deviceData.put("isCurrentDevice", true);
                        
                        Map<String, Object> settings = new HashMap<>();
                        settings.put("syncEnabled", true);
                        settings.put("notificationsEnabled", true);
                        deviceData.put("settings", settings);
                        
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("createdAt", new Date());
                        metadata.put("updatedAt", new Date());
                        deviceData.put("metadata", metadata);
                        
                        docRef.set(deviceData)
                                .addOnSuccessListener(aVoid -> {
                                    emitter.onComplete();
                                })
                                .addOnFailureListener(error -> {
                                    handleFirestoreError(error, "creating device document");
                                    emitter.onError(error);
                                });
                    } else {
                        handleFirestoreError(e, "updating device sync status");
                        emitter.onError(e);
                    }
                });
    });
}

@Override
public Observable<SyncStatus> observeSyncStatus() {
    return syncStatusSubject;
}
