package com.autogratuity.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Room database operations with PendingOperationEntity
 */
@Dao
public interface PendingOperationDao {
    
    /**
     * Insert a new pending operation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PendingOperationEntity operation);
    
    /**
     * Update an existing pending operation
     */
    @Update
    void update(PendingOperationEntity operation);
    
    /**
     * Delete a pending operation
     */
    @Delete
    void delete(PendingOperationEntity operation);
    
    /**
     * Get all pending operations for a specific user, ordered by creation time
     */
    @Query("SELECT * FROM pending_operations WHERE userId = :userId ORDER BY createdAt ASC")
    List<PendingOperationEntity> getAllForUser(String userId);
    
    /**
     * Get all pending operations, ordered by creation time
     */
    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    List<PendingOperationEntity> getAll();
    
    /**
     * Get the next batch of pending operations to process, up to a limit
     */
    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC LIMIT :limit")
    List<PendingOperationEntity> getNextBatch(int limit);
    
    /**
     * Get all operations of a specific type for a user
     */
    @Query("SELECT * FROM pending_operations WHERE operationType = :operationType AND userId = :userId")
    List<PendingOperationEntity> getByType(String operationType, String userId);
    
    /**
     * Get a pending operation for a specific target ID and type
     */
    @Query("SELECT * FROM pending_operations WHERE targetId = :targetId AND operationType = :operationType LIMIT 1")
    PendingOperationEntity getByTargetAndType(String targetId, String operationType);
    
    /**
     * Get all operations that have been retried fewer than a maximum number of times
     */
    @Query("SELECT * FROM pending_operations WHERE retryCount < :maxRetries ORDER BY lastAttempt ASC")
    List<PendingOperationEntity> getRetryableOperations(int maxRetries);
    
    /**
     * Delete all operations for a specific user
     */
    @Query("DELETE FROM pending_operations WHERE userId = :userId")
    void deleteAllForUser(String userId);
    
    /**
     * Update the retry count and last attempt time for an operation
     */
    @Query("UPDATE pending_operations SET retryCount = retryCount + 1, lastAttempt = :attemptTime WHERE id = :id")
    void updateRetryStatus(long id, Date attemptTime);
    
    /**
     * Delete all operations older than a specified date
     */
    @Query("DELETE FROM pending_operations WHERE createdAt < :cutoffDate")
    void deleteOldOperations(Date cutoffDate);
}
