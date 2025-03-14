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
 * Data Access Object for Room database operations with DeliveryEntity
 */
@Dao
public interface DeliveryDao {
    
    /**
     * Insert a new delivery entity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DeliveryEntity delivery);
    
    /**
     * Update an existing delivery entity
     */
    @Update
    void update(DeliveryEntity delivery);
    
    /**
     * Delete a delivery entity
     */
    @Delete
    void delete(DeliveryEntity delivery);
    
    /**
     * Get all deliveries for a specific user
     */
    @Query("SELECT * FROM deliveries WHERE userId = :userId")
    List<DeliveryEntity> getAllForUser(String userId);
    
    /**
     * Get delivery by order ID for a specific user
     */
    @Query("SELECT * FROM deliveries WHERE orderId = :orderId AND userId = :userId LIMIT 1")
    DeliveryEntity getByOrderId(String orderId, String userId);
    
    /**
     * Get recent deliveries for a specific user
     */
    @Query("SELECT * FROM deliveries WHERE userId = :userId ORDER BY deliveryDate DESC LIMIT :limit")
    List<DeliveryEntity> getRecentDeliveries(String userId, int limit);
    
    /**
     * Get all dirty deliveries that need to be synced
     */
    @Query("SELECT * FROM deliveries WHERE isDirty = 1")
    List<DeliveryEntity> getAllDirty();
    
    /**
     * Get deliveries that haven't been tipped after a cutoff date
     */
    @Query("SELECT * FROM deliveries WHERE userId = :userId AND tipAmount = 0 AND deliveryDate < :cutoffDate")
    List<DeliveryEntity> getDeliveriesWithoutTips(String userId, Date cutoffDate);
    
    /**
     * Get deliveries by address containing a search term
     */
    @Query("SELECT * FROM deliveries WHERE userId = :userId AND address LIKE '%' || :searchTerm || '%'")
    List<DeliveryEntity> searchByAddress(String userId, String searchTerm);
    
    /**
     * Delete all deliveries for a specific user
     */
    @Query("DELETE FROM deliveries WHERE userId = :userId")
    void deleteAllForUser(String userId);
    
    /**
     * Update the tip amount for a delivery
     */
    @Query("UPDATE deliveries SET tipAmount = :tipAmount, tipDate = :tipDate, isDirty = 1, lastSyncTime = :lastSyncTime WHERE documentId = :documentId")
    void updateTip(String documentId, double tipAmount, Date tipDate, Date lastSyncTime);
    
    /**
     * Mark a delivery as synced (not dirty)
     */
    @Query("UPDATE deliveries SET isDirty = 0, lastSyncTime = :lastSyncTime WHERE id = :id")
    void markSynced(long id, Date lastSyncTime);
}
