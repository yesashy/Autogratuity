package com.autogratuity.repositories;

import android.util.Log;

import com.autogratuity.models.Address;
import com.autogratuity.models.Delivery;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of IFirestoreRepository for managing Firestore data operations
 * Implements the singleton pattern for consistent application access
 */
public class FirestoreRepository implements IFirestoreRepository {
    private static final String TAG = "FirestoreRepository";
    
    // Firestore collection names
    private static final String COLLECTION_DELIVERIES = "deliveries";
    private static final String COLLECTION_ADDRESSES = "addresses";
    private static final String COLLECTION_PENDING_TIPS = "pending_tips";
    
    // Singleton instance
    private static FirestoreRepository instance;
    
    // Firestore reference
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    
    // Private constructor for singleton pattern
    private FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    
    /**
     * Get the singleton instance of the repository
     */
    public static synchronized FirestoreRepository getInstance() {
        if (instance == null) {
            instance = new FirestoreRepository();
        }
        return instance;
    }
    
    /**
     * Get the current user ID, or null if not authenticated
     */
    private String getCurrentUserId() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
    
    /**
     * Add a new delivery to Firestore
     */
    @Override
    public Task<DocumentReference> addDelivery(Delivery delivery) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot add delivery: User not authenticated");
            return null;
        }
        
        // Ensure the delivery has the current user ID
        delivery.setUserId(userId);
        
        // Convert to Firestore document
        Map<String, Object> deliveryData = delivery.toDocument();
        
        // Add to Firestore
        return db.collection(COLLECTION_DELIVERIES).add(deliveryData);
    }
    
    /**
     * Find deliveries with the given order ID
     */
    @Override
    public Task<QuerySnapshot> findDeliveryByOrderId(String orderId) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot find delivery: User not authenticated");
            return null;
        }
        
        return db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("orderId", orderId)
                .whereEqualTo("userId", userId)
                .get();
    }
    
    /**
     * Update a delivery with tip information
     */
    @Override
    public Task<Void> updateDeliveryWithTip(String documentId, double tipAmount) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot update delivery: User not authenticated");
            return null;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("tipAmount", tipAmount);
        updates.put("tipDate", Timestamp.now());
        
        return db.collection(COLLECTION_DELIVERIES)
                .document(documentId)
                .update(updates);
    }
    
    /**
     * Store a pending tip when the delivery record isn't found
     */
    @Override
    public Task<DocumentReference> storePendingTip(String orderId, double tipAmount) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot store pending tip: User not authenticated");
            return null;
        }
        
        Map<String, Object> tipData = new HashMap<>();
        tipData.put("orderId", orderId);
        tipData.put("tipAmount", tipAmount);
        tipData.put("userId", userId);
        tipData.put("timestamp", Timestamp.now());
        tipData.put("processed", false);
        
        return db.collection(COLLECTION_PENDING_TIPS).add(tipData);
    }
    
    /**
     * Find an address by its normalized form
     */
    @Override
    public Task<QuerySnapshot> findAddressByNormalizedAddress(String normalizedAddress) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot find address: User not authenticated");
            return null;
        }
        
        return db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("normalizedAddress", normalizedAddress)
                .whereEqualTo("userId", userId)
                .get();
    }
    
    /**
     * Update address statistics when a new tip is received
     */
    @Override
    public Task<Void> updateAddressStatistics(String addressId, double tipAmount, String orderId) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot update address stats: User not authenticated");
            return null;
        }
        
        DocumentReference addressRef = db.collection(COLLECTION_ADDRESSES).document(addressId);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("totalTips", FieldValue.increment(tipAmount));
        updates.put("deliveryCount", FieldValue.increment(1));
        updates.put("lastUpdated", Timestamp.now());
        
        if (orderId != null) {
            updates.put("orderIds", FieldValue.arrayUnion(orderId));
        }
        
        return addressRef.update(updates);
    }
    
    /**
     * Add a new address to Firestore
     */
    @Override
    public Task<DocumentReference> addAddress(Address address) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot add address: User not authenticated");
            return null;
        }
        
        // Ensure the address has the current user ID
        address.setUserId(userId);
        
        // Convert to Firestore document
        Map<String, Object> addressData = address.toDocument();
        
        // Add to Firestore
        return db.collection(COLLECTION_ADDRESSES).add(addressData);
    }
    
    /**
     * Search for addresses matching a search term
     */
    @Override
    public Task<List<Address>> getAddressesBySearchTerm(String searchTerm, int limit) {
        // This would be implemented as a custom query
        // For now, return a placeholder
        return null;
    }
    
    /**
     * Get recent deliveries with limit
     */
    @Override
    public Task<QuerySnapshot> getRecentDeliveries(int limit) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot get recent deliveries: User not authenticated");
            return null;
        }
        
        return db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .orderBy("deliveryDate", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }
    
    /**
     * Get deliveries without tips older than cutoff date
     */
    @Override
    public Task<QuerySnapshot> getDeliveriesWithoutTips(Timestamp cutoffDate) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot get deliveries without tips: User not authenticated");
            return null;
        }
        
        return db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("tipAmount", 0)
                .whereLessThan("deliveryDate", cutoffDate)
                .get();
    }
    
    /**
     * Get addresses with multiple deliveries
     */
    @Override
    public Task<QuerySnapshot> getAddressesWithMultipleDeliveries(int minDeliveries) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot get addresses: User not authenticated");
            return null;
        }
        
        return db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("deliveryCount", minDeliveries)
                .get();
    }
    
    // Cache-specific operations - no implementation needed in base class
    @Override
    public void setDirty(boolean isDirty) {
        // Not implemented in base repository
    }

    @Override
    public void setId(String id) {
        // Not implemented in base repository
    }

    @Override
    public void setId(Delivery delivery, String id) {
        // Not implemented in base repository
    }

    @Override
    public void setDirty(boolean isDirty, String id) {
        // Not implemented in base repository
    }

    @Override
    public void setUserId(String userId) {
        // Not implemented in base repository
    }

    @Override
    public void setOrderId(String orderId) {
        // Not implemented in base repository
    }

    @Override
    public void setAddress(String address) {
        // Not implemented in base repository
    }

    @Override
    public void setTipAmount(double tipAmount) {
        // Not implemented in base repository
    }

    @Override
    public void setLastSyncTime(Date lastSyncTime) {
        // Not implemented in base repository
    }

    @Override
    public void invalidateCache(String key) {
        // Not implemented in base repository
    }
}
