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
 * Data Access Object for Room database operations with AddressEntity
 */
@Dao
public interface AddressDao {
    
    /**
     * Insert a new address entity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AddressEntity address);
    
    /**
     * Update an existing address entity
     */
    @Update
    void update(AddressEntity address);
    
    /**
     * Delete an address entity
     */
    @Delete
    void delete(AddressEntity address);
    
    /**
     * Get all addresses for a specific user
     */
    @Query("SELECT * FROM addresses WHERE userId = :userId")
    List<AddressEntity> getAllForUser(String userId);
    
    /**
     * Get address by normalized address for a specific user
     */
    @Query("SELECT * FROM addresses WHERE normalizedAddress = :normalizedAddress AND userId = :userId LIMIT 1")
    AddressEntity getByNormalizedAddress(String normalizedAddress, String userId);
    
    /**
     * Get addresses with multiple deliveries for a specific user
     */
    @Query("SELECT * FROM addresses WHERE userId = :userId AND deliveryCount >= :minDeliveries")
    List<AddressEntity> getAddressesWithMultipleDeliveries(String userId, int minDeliveries);
    
    /**
     * Get all dirty addresses that need to be synced
     */
    @Query("SELECT * FROM addresses WHERE isDirty = 1")
    List<AddressEntity> getAllDirty();
    
    /**
     * Search addresses by search terms for a specific user
     */
    @Query("SELECT * FROM addresses WHERE userId = :userId AND fullAddress LIKE '%' || :searchTerm || '%' LIMIT :limit")
    List<AddressEntity> searchByTerm(String userId, String searchTerm, int limit);
    
    /**
     * Delete all addresses for a specific user
     */
    @Query("DELETE FROM addresses WHERE userId = :userId")
    void deleteAllForUser(String userId);
    
    /**
     * Update statistics for an address
     */
    @Query("UPDATE addresses SET totalTips = totalTips + :tipAmount, deliveryCount = deliveryCount + 1, " +
           "averageTip = (totalTips + :tipAmount) / (deliveryCount + 1), lastUpdated = :updateTime, " +
           "isDirty = 1, lastSyncTime = :lastSyncTime WHERE documentId = :documentId")
    void updateStatistics(String documentId, double tipAmount, Date updateTime, Date lastSyncTime);
    
    /**
     * Mark an address as a "Do Not Deliver" location
     */
    @Query("UPDATE addresses SET doNotDeliver = 1, isDirty = 1, lastSyncTime = :lastSyncTime WHERE documentId = :documentId")
    void markDoNotDeliver(String documentId, Date lastSyncTime);
    
    /**
     * Mark an address as synced (not dirty)
     */
    @Query("UPDATE addresses SET isDirty = 0, lastSyncTime = :lastSyncTime WHERE id = :id")
    void markSynced(long id, Date lastSyncTime);
}
