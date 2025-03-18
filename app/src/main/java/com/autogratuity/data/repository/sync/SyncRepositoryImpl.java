package com.autogratuity.data.repository.sync;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.model.SyncOperation;
import com.autogratuity.data.model.SyncStatus;
import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.repository.core.FirestoreRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Implementation of SyncRepository interface.
 * Responsible for managing data synchronization.
 */
public class SyncRepositoryImpl extends FirestoreRepository implements SyncRepository {
    
    private static final String TAG = "SyncRepository";
    
    // Collection names
    private static final String COLLECTION_SYNC_OPERATIONS = "sync_operations";
    private static final String COLLECTION_USER_DEVICES = "user_devices";
    private static final String COLLECTION_USER_PROFILES = "user_profiles";
    private static final String COLLECTION_ADDRESSES = "addresses";
    private static final String COLLECTION_DELIVERIES = "deliveries";
    private static final String COLLECTION_SUBSCRIPTION_RECORDS = "subscription_records";
    private static final String COLLECTION_SYSTEM_CONFIG = "system_config";
    
    // Preferences keys
    private static final String PREF_BACKGROUND_SYNC_ENABLED = "background_sync_enabled";
    private static final String PREF_LAST_SYNC_TIME = "last_sync_time";
    
    // Background work names
    private static final String WORK_SYNC_DATA = "sync_data_work";
    
    // Subject for real-time updates
    private final BehaviorSubject<SyncStatus> syncStatusSubject;
    
    /**
     * Constructor for SyncRepositoryImpl
     * 
     * @param context Android context for SharedPreferences and connectivity
     */
    public SyncRepositoryImpl(Context context) {
        super(context);
        
        // Initialize sync status
        SyncStatus initialStatus = new SyncStatus();
        initialStatus.setOnline(isNetworkAvailable());
        syncStatusSubject = BehaviorSubject.createDefault(initialStatus);
        
        // Update pending operation count
        updatePendingOperationCount();
        
        // Start monitoring network status
        startNetworkMonitoring();
    }
    
    /**
     * Start monitoring network status changes
     */
    private void startNetworkMonitoring() {
        Observable.interval(30, TimeUnit.SECONDS)
                .filter(tick -> {
                    boolean currentStatus = isNetworkAvailable();
                    boolean previousStatus = syncStatusSubject.getValue().isOnline();
                    return currentStatus != previousStatus;
                })
                .subscribe(tick -> {
                    boolean isConnected = isNetworkAvailable();
                    
                    // Update sync status
                    SyncStatus syncStatus = syncStatusSubject.getValue();
                    syncStatus.setOnline(isConnected);
                    syncStatusSubject.onNext(syncStatus);
                    
                    // If we just came online, try to process pending operations
                    if (isConnected && syncStatus.getPendingOperations() > 0) {
                        processPendingSyncOperations()
                                .subscribe(
                                        () -> Log.d(TAG, "Processed pending operations after reconnecting"),
                                        error -> Log.e(TAG, "Error processing pending operations", error)
                                );
                    }
                }, throwable -> {
                    Log.e(TAG, "Error monitoring network status", throwable);
                });
    }
    
    /**
     * Update the count of pending operations in the sync status
     */
    private void updatePendingOperationCount() {
        // Only update if we have a subscriber to avoid unnecessary queries
        if (syncStatusSubject.hasObservers()) {
            getPendingOperationCount()
                    .subscribe(
                            count -> {
                                SyncStatus status = syncStatusSubject.getValue();
                                status.setPendingOperations(count);
                                syncStatusSubject.onNext(status);
                            },
                            error -> Log.e(TAG, "Error updating pending operation count", error)
                    );
        }
    }
    
    @Override
    public Completable syncData() {
        // Update sync status
        SyncStatus status = syncStatusSubject.getValue();
        status.setSyncing();
        syncStatusSubject.onNext(status);
        
        // Check network availability
        if (!isNetworkAvailable()) {
            status.setOnline(false);
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
            updatedStatus.setCompleted();
            syncStatusSubject.onNext(updatedStatus);
            
            // Update the last sync time in preferences
            getPrefs().edit()
                    .putLong(PREF_LAST_SYNC_TIME, System.currentTimeMillis())
                    .apply();
                    
            // Update device sync status in Firestore
            updateDeviceSyncStatus(getDeviceId(), updatedStatus)
                    .subscribe(
                            () -> Log.d(TAG, "Device sync status updated"),
                            error -> Log.e(TAG, "Error updating device sync status", error)
                    );
        })
        .doOnError(error -> {
            // Update sync status on error
            SyncStatus updatedStatus = syncStatusSubject.getValue();
            updatedStatus.setError(error.getMessage());
            syncStatusSubject.onNext(updatedStatus);
        });
    }
    
    @Override
    public Single<SyncStatus> getSyncStatus() {
        return Single.just(syncStatusSubject.getValue());
    }
    
    @Override
    public Completable enqueueSyncOperation(SyncOperation operation) {
        // Validate operation
        if (operation == null) {
            return Completable.error(new IllegalArgumentException("Operation cannot be null"));
        }
        
        // Set user ID if not already set
        if (operation.getUserId() == null || operation.getUserId().isEmpty()) {
            operation.setUserId(getUserId());
        }
        
        // Set device ID
        operation.setDeviceId(getDeviceId());
        
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
        final String finalOperationId = operationId;
        
        return Completable.create(emitter -> {
            docRef.set(operation)
                    .addOnSuccessListener(aVoid -> {
                        // Update sync status
                        SyncStatus status = syncStatusSubject.getValue();
                        status.setPendingOperations(status.getPendingOperations() + 1);
                        syncStatusSubject.onNext(status);
                        
                        // Process immediately if online
                        if (isNetworkAvailable() && !SyncStatus.STATUS_SYNCING.equals(status.getStatus())) {
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
                            Log.e(TAG, "Error enqueueing sync operation", e);
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
                    .whereEqualTo("userId", getUserId())
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
                        Log.e(TAG, "Error getting pending sync operations", e);
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
                                    SyncStatus status = syncStatusSubject.getValue();
                                    status.setFailedOperations(status.getFailedOperations() + 1);
                                    syncStatusSubject.onNext(status);
                                    
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
                        Log.e(TAG, "Error processing create operation", e);
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
                                // For simplicity, we'll just update with transaction
                                db.runTransaction((Transaction.Function<Void>) transaction -> {
                                    DocumentSnapshot snapshot = transaction.get(docRef);
                                    
                                    if (snapshot.exists()) {
                                        // Check for version conflict
                                        Long localVersion = (Long) data.get("version");
                                        Long serverVersion = snapshot.getLong("version");
                                        
                                        if (localVersion != null && serverVersion != null && localVersion < serverVersion) {
                                            // Conflict detected
                                            if (SyncOperation.CONFLICT_RESOLUTION_SERVER_WINS.equals(conflictStrategy)) {
                                                // Server wins - no update
                                                return null;
                                            } else if (SyncOperation.CONFLICT_RESOLUTION_CLIENT_WINS.equals(conflictStrategy)) {
                                                // Client wins - force update
                                                data.put("version", serverVersion + 1);
                                                transaction.update(docRef, data);
                                            } else {
                                                // Merge strategy - would implement field-by-field merge
                                                // For brevity, just increment version and update
                                                data.put("version", serverVersion + 1);
                                                transaction.update(docRef, data);
                                            }
                                        } else {
                                            // No conflict or local is newer
                                            if (serverVersion != null) {
                                                data.put("version", serverVersion + 1);
                                            } else {
                                                data.put("version", localVersion != null ? localVersion + 1 : 1);
                                            }
                                            transaction.update(docRef, data);
                                        }
                                    } else {
                                        // Document doesn't exist, create it
                                        data.put("version", 1);
                                        transaction.set(docRef, data);
                                    }
                                    
                                    return null;
                                })
                                .addOnSuccessListener(result -> {
                                    // Invalidate relevant caches
                                    invalidateCachesForEntity(entityType, entityId);
                                    
                                    emitter.onComplete();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error in update transaction", e);
                                    emitter.onError(e);
                                });
                            } else {
                                // No version tracking, just update
                                docRef.update(data)
                                        .addOnSuccessListener(aVoid -> {
                                            // Invalidate relevant caches
                                            invalidateCachesForEntity(entityType, entityId);
                                            
                                            emitter.onComplete();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error processing update operation", e);
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
                                        Log.e(TAG, "Error processing update operation (create)", e);
                                        emitter.onError(e);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking document for update operation", e);
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
                        Log.e(TAG, "Error processing delete operation", e);
                        emitter.onError(e);
                    });
        });
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
                        operation.setUpdatedAt(new Date());
                        
                        if (SyncOperation.STATUS_COMPLETED.equals(status)) {
                            operation.setCompletedAt(new Date());
                            
                            // Update sync status
                            SyncStatus syncStatus = syncStatusSubject.getValue();
                            syncStatus.setPendingOperations(Math.max(0, syncStatus.getPendingOperations() - 1));
                            syncStatusSubject.onNext(syncStatus);
                        } else if (SyncOperation.STATUS_IN_PROGRESS.equals(status)) {
                            operation.setLastAttemptTime(new Date());
                            operation.setAttempts(operation.getAttempts() + 1);
                        }
                        
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
                invalidateCache("userProfile_" + getUserId());
                break;
            case "subscriptionRecord":
                invalidateCache("subscriptionStatus_" + getUserId());
                break;
            case "address":
                invalidateCache("address_" + entityId);
                invalidateCache("addresses_" + getUserId());
                break;
            case "delivery":
                invalidateCache("delivery_" + entityId);
                invalidateCache("deliveries_" + getUserId() + "_*");
                invalidateCache("delivery_stats_" + getUserId());
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
        
        String docId = getUserId() + "_" + deviceId;
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
                            deviceData.put("userId", getUserId());
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
                                        Log.e(TAG, "Error creating device document", error);
                                        emitter.onError(error);
                                    });
                        } else {
                            Log.e(TAG, "Error updating device sync status", e);
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Observable<SyncStatus> observeSyncStatus() {
        return syncStatusSubject;
    }
    
    @Override
    public Completable createEntity(String entityType, String entityId, Map<String, Object> data) {
        if (entityType == null || entityType.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Entity type cannot be empty"));
        }
        
        if (entityId == null || entityId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Entity ID cannot be empty"));
        }
        
        if (data == null || data.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Entity data cannot be empty"));
        }
        
        // Add current timestamp
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());
        
        // Create sync operation
        SyncOperation operation = new SyncOperation(
                getUserId(),
                "create",
                entityType,
                entityId,
                data);
        
        return enqueueSyncOperation(operation);
    }
    
    @Override
    public Completable updateEntity(String entityType, String entityId, Map<String, Object> data) {
        if (entityType == null || entityType.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Entity type cannot be empty"));
        }
        
        if (entityId == null || entityId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Entity ID cannot be empty"));
        }
        
        if (data == null || data.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Entity data cannot be empty"));
        }
        
        // Add current timestamp
        data.put("updatedAt", FieldValue.serverTimestamp());
        
        // Create sync operation
        SyncOperation operation = new SyncOperation(
                getUserId(),
                "update",
                entityType,
                entityId,
                data);
        
        return enqueueSyncOperation(operation);
    }
    
    @Override
    public Completable deleteEntity(String entityType, String entityId) {
        if (entityType == null || entityType.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Entity type cannot be empty"));
        }
        
        if (entityId == null || entityId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Entity ID cannot be empty"));
        }
        
        // Create sync operation
        SyncOperation operation = new SyncOperation(
                getUserId(),
                "delete",
                entityType,
                entityId,
                null);
        
        return enqueueSyncOperation(operation);
    }
    
    @Override
    public Single<List<SyncOperation>> getFailedSyncOperations() {
        return Single.create(emitter -> {
            db.collection(COLLECTION_SYNC_OPERATIONS)
                    .whereEqualTo("userId", getUserId())
                    .whereEqualTo("status", SyncOperation.STATUS_FAILED)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<SyncOperation> operations = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            SyncOperation operation = doc.toObject(SyncOperation.class);
                            if (operation != null) {
                                operations.add(operation);
                            }
                        }
                        
                        emitter.onSuccess(operations);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting failed sync operations", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Completable retryOperation(String operationId) {
        if (operationId == null || operationId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Operation ID cannot be empty"));
        }
        
        return Single.create(emitter -> {
            DocumentReference docRef = db.collection(COLLECTION_SYNC_OPERATIONS).document(operationId);
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            SyncOperation operation = documentSnapshot.toObject(SyncOperation.class);
                            if (operation != null && getUserId().equals(operation.getUserId())) {
                                emitter.onSuccess(operation);
                            } else {
                                emitter.onError(new SecurityException("Operation does not belong to current user"));
                            }
                        } else {
                            emitter.onError(new Exception("Operation not found: " + operationId));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting operation for retry", e);
                        emitter.onError(e);
                    });
        })
        .flatMapCompletable(operation -> {
            // Reset operation status
            operation.setStatus(SyncOperation.STATUS_PENDING);
            operation.setUpdatedAt(new Date());
            
            // Update operation in Firestore
            return Completable.create(emitter -> {
                DocumentReference docRef = db.collection(COLLECTION_SYNC_OPERATIONS).document(operationId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", SyncOperation.STATUS_PENDING);
                updates.put("updatedAt", new Date());
                
                docRef.update(updates)
                        .addOnSuccessListener(aVoid -> {
                            // Update sync status
                            SyncStatus status = syncStatusSubject.getValue();
                            status.setPendingOperations(status.getPendingOperations() + 1);
                            status.setFailedOperations(Math.max(0, status.getFailedOperations() - 1));
                            syncStatusSubject.onNext(status);
                            
                            // Process the operation if online
                            if (isNetworkAvailable()) {
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
                            Log.e(TAG, "Error updating operation for retry", e);
                            emitter.onError(e);
                        });
            });
        });
    }
    
    @Override
    public Completable retryAllFailedOperations() {
        return getFailedSyncOperations()
                .flatMapCompletable(operations -> {
                    if (operations.isEmpty()) {
                        return Completable.complete();
                    }
                    
                    // Create a batch to update all operations
                    WriteBatch batch = db.batch();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", SyncOperation.STATUS_PENDING);
                    updates.put("updatedAt", new Date());
                    
                    for (SyncOperation operation : operations) {
                        DocumentReference docRef = 
                                db.collection(COLLECTION_SYNC_OPERATIONS).document(operation.getOperationId());
                        batch.update(docRef, updates);
                    }
                    
                    return Completable.create(emitter -> {
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    // Update sync status
                                    SyncStatus status = syncStatusSubject.getValue();
                                    status.setPendingOperations(status.getPendingOperations() + operations.size());
                                    status.setFailedOperations(Math.max(0, status.getFailedOperations() - operations.size()));
                                    syncStatusSubject.onNext(status);
                                    
                                    // Process operations if online
                                    if (isNetworkAvailable()) {
                                        processPendingSyncOperations()
                                                .subscribe(
                                                        () -> emitter.onComplete(),
                                                        emitter::onError
                                                );
                                    } else {
                                        emitter.onComplete();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error retrying all failed operations", e);
                                    emitter.onError(e);
                                });
                    });
                });
    }
    
    @Override
    public Completable cancelOperation(String operationId) {
        if (operationId == null || operationId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Operation ID cannot be empty"));
        }
        
        return Completable.create(emitter -> {
            DocumentReference docRef = db.collection(COLLECTION_SYNC_OPERATIONS).document(operationId);
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            SyncOperation operation = documentSnapshot.toObject(SyncOperation.class);
                            if (operation != null && getUserId().equals(operation.getUserId())) {
                                // Check if operation is pending or retrying
                                if (SyncOperation.STATUS_PENDING.equals(operation.getStatus()) ||
                                        SyncOperation.STATUS_RETRYING.equals(operation.getStatus()) ||
                                        SyncOperation.STATUS_FAILED.equals(operation.getStatus())) {
                                    
                                    // Delete the operation
                                    docRef.delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Update sync status
                                                SyncStatus status = syncStatusSubject.getValue();
                                                if (SyncOperation.STATUS_PENDING.equals(operation.getStatus()) ||
                                                        SyncOperation.STATUS_RETRYING.equals(operation.getStatus())) {
                                                    status.setPendingOperations(Math.max(0, status.getPendingOperations() - 1));
                                                } else if (SyncOperation.STATUS_FAILED.equals(operation.getStatus())) {
                                                    status.setFailedOperations(Math.max(0, status.getFailedOperations() - 1));
                                                }
                                                syncStatusSubject.onNext(status);
                                                
                                                emitter.onComplete();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error deleting operation", e);
                                                emitter.onError(e);
                                            });
                                } else {
                                    emitter.onError(new IllegalStateException(
                                            "Cannot cancel operation with status: " + operation.getStatus()));
                                }
                            } else {
                                emitter.onError(new SecurityException("Operation does not belong to current user"));
                            }
                        } else {
                            // Operation already deleted
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting operation for cancellation", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<List<SyncOperation>> getSyncHistoryForEntity(String entityType, String entityId) {
        if (entityType == null || entityType.isEmpty()) {
            return Single.error(new IllegalArgumentException("Entity type cannot be empty"));
        }
        
        if (entityId == null || entityId.isEmpty()) {
            return Single.error(new IllegalArgumentException("Entity ID cannot be empty"));
        }
        
        return Single.create(emitter -> {
            db.collection(COLLECTION_SYNC_OPERATIONS)
                    .whereEqualTo("userId", getUserId())
                    .whereEqualTo("entityType", entityType)
                    .whereEqualTo("entityId", entityId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<SyncOperation> operations = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            SyncOperation operation = doc.toObject(SyncOperation.class);
                            if (operation != null) {
                                operations.add(operation);
                            }
                        }
                        
                        emitter.onSuccess(operations);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting sync history for entity", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<Integer> getPendingOperationCount() {
        return Single.create(emitter -> {
            db.collection(COLLECTION_SYNC_OPERATIONS)
                    .whereEqualTo("userId", getUserId())
                    .whereEqualTo("status", SyncOperation.STATUS_PENDING)
                    .count()
                    .get()
                    .addOnSuccessListener(countQuery -> {
                        Integer count = 0;
                        if (countQuery != null) {
                            count = (int) countQuery.getCount();
                        }
                        
                        // Update sync status
                        SyncStatus status = syncStatusSubject.getValue();
                        status.setPendingOperations(count);
                        syncStatusSubject.onNext(status);
                        
                        emitter.onSuccess(count);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting pending operation count", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<Boolean> hasPendingOperations() {
        return getPendingOperationCount()
                .map(count -> count > 0);
    }
    
    @Override
    public Single<Boolean> hasEntityPendingOperations(String entityType, String entityId) {
        if (entityType == null || entityType.isEmpty()) {
            return Single.error(new IllegalArgumentException("Entity type cannot be empty"));
        }
        
        if (entityId == null || entityId.isEmpty()) {
            return Single.error(new IllegalArgumentException("Entity ID cannot be empty"));
        }
        
        return Single.create(emitter -> {
            db.collection(COLLECTION_SYNC_OPERATIONS)
                    .whereEqualTo("userId", getUserId())
                    .whereEqualTo("status", SyncOperation.STATUS_PENDING)
                    .whereEqualTo("entityType", entityType)
                    .whereEqualTo("entityId", entityId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        emitter.onSuccess(!querySnapshot.isEmpty());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking pending operations for entity", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Completable setBackgroundSyncEnabled(boolean enabled) {
        return Completable.fromAction(() -> {
            // Save preference
            getPrefs().edit()
                    .putBoolean(PREF_BACKGROUND_SYNC_ENABLED, enabled)
                    .apply();
            
            // Update sync status
            SyncStatus status = syncStatusSubject.getValue();
            status.setBackgroundSyncEnabled(enabled);
            syncStatusSubject.onNext(status);
            
            // If enabling, schedule background sync if needed
            if (enabled && status.getPendingOperations() > 0) {
                scheduleBackgroundSync(30)
                        .subscribe(
                                () -> Log.d(TAG, "Background sync scheduled after enabling"),
                                error -> Log.e(TAG, "Error scheduling background sync", error)
                        );
            }
        });
    }
    
    @Override
    public Single<Boolean> isBackgroundSyncEnabled() {
        return Single.fromCallable(() -> 
                getPrefs().getBoolean(PREF_BACKGROUND_SYNC_ENABLED, true));
    }
    
    @Override
    public Completable scheduleBackgroundSync(int delaySeconds) {
        return isBackgroundSyncEnabled()
                .flatMapCompletable(isEnabled -> {
                    if (!isEnabled) {
                        return Completable.complete();
                    }
                    
                    if (WorkManager.getInstance() == null) {
                        return Completable.error(new IllegalStateException("WorkManager not initialized"));
                    }
                    
                    // Set up constraints (require network)
                    Constraints constraints = new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build();
                    
                    // Create work request
                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                            .setConstraints(constraints)
                            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                            .build();
                    
                    // Enqueue work
                    WorkManager.getInstance().enqueueUniqueWork(
                            WORK_SYNC_DATA,
                            ExistingWorkPolicy.REPLACE,
                            workRequest);
                    
                    return Completable.complete();
                });
    }
    
    @Override
    public Completable cancelScheduledSync() {
        if (WorkManager.getInstance() == null) {
            return Completable.error(new IllegalStateException("WorkManager not initialized"));
        }
        
        WorkManager.getInstance().cancelUniqueWork(WORK_SYNC_DATA);
        return Completable.complete();
    }
    
    /**
     * Clean up resources when the repository is no longer needed
     */
    public void cleanup() {
        // Complete the sync status subject
        if (!syncStatusSubject.hasComplete()) {
            syncStatusSubject.onComplete();
        }
    }
    
    /**
     * WorkManager worker class for background synchronization
     */
    public static class SyncWorker extends androidx.work.Worker {
        
        public SyncWorker(Context context, androidx.work.WorkerParameters params) {
            super(context, params);
        }
        
        @Override
        public Result doWork() {
            // Get repository instance
            SyncRepository syncRepo = (SyncRepository) RepositoryProvider.getRepository();
            
            try {
                // Perform sync
                syncRepo.syncData()
                        .blockingAwait();
                
                return Result.success();
            } catch (Exception e) {
                Log.e(TAG, "Error in background sync", e);
                return Result.retry();
            }
        }
    }
}
