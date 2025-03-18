package com.autogratuity.data.repository.sync;

import com.autogratuity.data.model.SyncOperation;
import com.autogratuity.data.model.SyncStatus;
import com.autogratuity.data.repository.core.DataRepository;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Repository interface for data synchronization operations.
 * Extends the core DataRepository for sync-specific operations.
 */
public interface SyncRepository extends DataRepository {
    
    //-----------------------------------------------------------------------------------
    // Sync Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Synchronize all data between local storage and Firestore.
     * 
     * @return Completable that completes when sync is finished
     */
    @Override
    Completable syncData();
    
    /**
     * Get the current sync status.
     * 
     * @return Single that emits the sync status
     */
    @Override
    Single<SyncStatus> getSyncStatus();
    
    /**
     * Enqueue a sync operation for processing.
     * Will be processed immediately if online, or queued for later if offline.
     * 
     * @param operation The sync operation to enqueue
     * @return Completable that completes when enqueuing is finished
     */
    @Override
    Completable enqueueSyncOperation(SyncOperation operation);
    
    /**
     * Get all pending sync operations.
     * 
     * @return Single that emits a list of pending operations
     */
    @Override
    Single<List<SyncOperation>> getPendingSyncOperations();
    
    /**
     * Process all pending sync operations.
     * 
     * @return Completable that completes when processing is finished
     */
    @Override
    Completable processPendingSyncOperations();
    
    /**
     * Update the sync status for a device.
     * 
     * @param deviceId The device ID
     * @param syncStatus The updated sync status
     * @return Completable that completes when update is finished
     */
    @Override
    Completable updateDeviceSyncStatus(String deviceId, SyncStatus syncStatus);
    
    /**
     * Observe changes to the sync status in real-time.
     * 
     * @return Observable that emits sync status updates
     */
    @Override
    Observable<SyncStatus> observeSyncStatus();
    
    //-----------------------------------------------------------------------------------
    // Convenience Methods
    //-----------------------------------------------------------------------------------
    
    /**
     * Create a sync operation for creating an entity.
     * 
     * @param entityType Entity type (e.g., "address", "delivery")
     * @param entityId Entity ID
     * @param data Entity data
     * @return Completable that completes when operation is enqueued
     */
    Completable createEntity(String entityType, String entityId, Map<String, Object> data);
    
    /**
     * Create a sync operation for updating an entity.
     * 
     * @param entityType Entity type (e.g., "address", "delivery")
     * @param entityId Entity ID
     * @param data Entity data
     * @return Completable that completes when operation is enqueued
     */
    Completable updateEntity(String entityType, String entityId, Map<String, Object> data);
    
    /**
     * Create a sync operation for deleting an entity.
     * 
     * @param entityType Entity type (e.g., "address", "delivery")
     * @param entityId Entity ID
     * @return Completable that completes when operation is enqueued
     */
    Completable deleteEntity(String entityType, String entityId);
    
    /**
     * Get failed sync operations.
     * 
     * @return Single that emits a list of failed operations
     */
    Single<List<SyncOperation>> getFailedSyncOperations();
    
    /**
     * Retry a failed sync operation.
     * 
     * @param operationId Operation ID
     * @return Completable that completes when retry is finished
     */
    Completable retryOperation(String operationId);
    
    /**
     * Retry all failed sync operations.
     * 
     * @return Completable that completes when all retries are finished
     */
    Completable retryAllFailedOperations();
    
    /**
     * Cancel a pending sync operation.
     * 
     * @param operationId Operation ID
     * @return Completable that completes when cancellation is finished
     */
    Completable cancelOperation(String operationId);
    
    /**
     * Get sync history by entity.
     * 
     * @param entityType Entity type (e.g., "address", "delivery")
     * @param entityId Entity ID
     * @return Single that emits a list of sync operations for the entity
     */
    Single<List<SyncOperation>> getSyncHistoryForEntity(String entityType, String entityId);
    
    /**
     * Get the number of pending sync operations.
     * 
     * @return Single that emits the number of pending operations
     */
    Single<Integer> getPendingOperationCount();
    
    /**
     * Check if there are any pending sync operations.
     * 
     * @return Single that emits true if there are pending operations
     */
    Single<Boolean> hasPendingOperations();
    
    /**
     * Check if a specific entity has pending sync operations.
     * 
     * @param entityType Entity type (e.g., "address", "delivery")
     * @param entityId Entity ID
     * @return Single that emits true if the entity has pending operations
     */
    Single<Boolean> hasEntityPendingOperations(String entityType, String entityId);
    
    /**
     * Enable or disable background synchronization.
     * 
     * @param enabled Whether background sync should be enabled
     * @return Completable that completes when update is finished
     */
    Completable setBackgroundSyncEnabled(boolean enabled);
    
    /**
     * Check if background synchronization is enabled.
     * 
     * @return Single that emits true if background sync is enabled
     */
    Single<Boolean> isBackgroundSyncEnabled();
    
    /**
     * Schedule a sync to occur in the background.
     * 
     * @param delaySeconds Delay in seconds before sync should occur
     * @return Completable that completes when scheduling is finished
     */
    Completable scheduleBackgroundSync(int delaySeconds);
    
    /**
     * Cancel any scheduled background syncs.
     * 
     * @return Completable that completes when cancellation is finished
     */
    Completable cancelScheduledSync();
}
