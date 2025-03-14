package com.autogratuity.repositories;

import com.autogratuity.models.Address;
import com.autogratuity.models.Delivery;
import com.autogratuity.models.DeliveryData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Interface for Firestore repository operations
 * Defines the contract for all repository implementations
 */
public interface IFirestoreRepository {
    
    // Delivery operations
    Task<DocumentReference> addDelivery(Delivery delivery);
    Task<QuerySnapshot> findDeliveryByOrderId(String orderId);
    Task<Void> updateDeliveryWithTip(String documentId, double tipAmount);
    Task<DocumentReference> storePendingTip(String orderId, double tipAmount);
    
    // Address operations
    Task<QuerySnapshot> findAddressByNormalizedAddress(String normalizedAddress);
    Task<Void> updateAddressStatistics(String addressId, double tipAmount, String orderId);
    Task<DocumentReference> addAddress(Address address);
    Task<List<Address>> getAddressesBySearchTerm(String searchTerm, int limit);
    Task<Void> updateAddressDoNotDeliver(String addressId, boolean doNotDeliver);
    
    // Batch operations for bulk import
    Task<Void> batchAddDeliveries(List<Delivery> deliveries);
    Task<Void> batchAddAddresses(List<Address> addresses);
    Task<Void> batchSaveDeliveries(List<DeliveryData> deliveries);
    
    // Geo-based queries
    Task<QuerySnapshot> getAddressesNearLocation(double latitude, double longitude, double radiusKm);
    
    // Cache operations
    void setDirty(boolean isDirty);
    void setId(String id);
    void setId(Delivery delivery, String id);
    void setDirty(boolean isDirty, String id);
    void setUserId(String userId);
    void setOrderId(String orderId);
    void setAddress(String address);
    void setTipAmount(double tipAmount);
    void setLastSyncTime(Date lastSyncTime);
    
    // Cache invalidation
    void invalidateCache(String key);
    
    // Query operations
    Task<QuerySnapshot> getRecentDeliveries(int limit);
    Task<QuerySnapshot> getDeliveriesWithoutTips(Timestamp cutoffDate);
    Task<QuerySnapshot> getAddressesWithMultipleDeliveries(int minDeliveries);
    
    // Utility method to access the Firestore instance directly when needed
    FirebaseFirestore getFirestore();
    
    // Notification method for UI updates
    void notifyDataChanged();
    
    // Verification status methods
    Task<Void> markAsVerified(String deliveryId, boolean verified);
    Task<Void> updateVerificationStatus(String deliveryId, String source, String notes);
}