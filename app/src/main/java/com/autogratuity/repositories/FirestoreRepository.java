package com.autogratuity.repositories;

import android.util.Log;

import com.autogratuity.models.Address;
import com.autogratuity.models.Delivery;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
    
    // Maximum batch size for Firestore
    private static final int MAX_BATCH_SIZE = 500;
    
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
     * Get the Firestore instance
     */
    @Override
    public FirebaseFirestore getFirestore() {
        return db;
    }
    
    /**
     * Add a new delivery to Firestore
     */
    @Override
    public Task<DocumentReference> addDelivery(Delivery delivery) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot add delivery: User not authenticated");
            return Tasks.forException(new Exception("User not authenticated"));
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
            return Tasks.forException(new Exception("User not authenticated"));
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
            return Tasks.forException(new Exception("User not authenticated"));
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
            return Tasks.forException(new Exception("User not authenticated"));
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
            return Tasks.forException(new Exception("User not authenticated"));
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
            return Tasks.forException(new Exception("User not authenticated"));
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
            return Tasks.forException(new Exception("User not authenticated"));
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
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot search addresses: User not authenticated");
            return Tasks.forException(new Exception("User not authenticated"));
        }
        
        // Normalize the search term
        String normalizedTerm = searchTerm.toLowerCase().trim();
        
        return db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .whereArrayContains("searchTerms", normalizedTerm)
                .limit(limit)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Address> addresses = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult()) {
                            Address address = Address.fromDocument(doc);
                            if (address != null) {
                                addresses.add(address);
                            }
                        }
                        return addresses;
                    } else {
                        throw new Exception("Error getting addresses: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }
    
    /**
     * Update the "Do Not Deliver" flag for an address
     */
    @Override
    public Task<Void> updateAddressDoNotDeliver(String addressId, boolean doNotDeliver) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot update address flag: User not authenticated");
            return Tasks.forException(new Exception("User not authenticated"));
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("doNotDeliver", doNotDeliver);
        updates.put("lastUpdated", Timestamp.now());
        
        return db.collection(COLLECTION_ADDRESSES)
                .document(addressId)
                .update(updates);
    }
    
    /**
     * Batch add multiple deliveries
     */
    @Override
    public Task<Void> batchAddDeliveries(List<Delivery> deliveries) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot batch add deliveries: User not authenticated");
            return Tasks.forException(new Exception("User not authenticated"));
        }
        
        if (deliveries == null || deliveries.isEmpty()) {
            return Tasks.forResult(null);
        }
        
        // Split into batches of MAX_BATCH_SIZE
        List<List<Delivery>> batches = new ArrayList<>();
        for (int i = 0; i < deliveries.size(); i += MAX_BATCH_SIZE) {
            batches.add(deliveries.subList(i, Math.min(i + MAX_BATCH_SIZE, deliveries.size())));
        }
        
        List<Task<Void>> batchTasks = new ArrayList<>();
        
        for (List<Delivery> batch : batches) {
            WriteBatch writeBatch = db.batch();
            
            for (Delivery delivery : batch) {
                // Ensure all deliveries have the current user ID
                delivery.setUserId(userId);
                
                // Add to batch
                DocumentReference docRef = db.collection(COLLECTION_DELIVERIES).document();
                writeBatch.set(docRef, delivery.toDocument());
            }
            
            batchTasks.add(writeBatch.commit());
        }
        
        return Tasks.whenAll(batchTasks);
    }
    
    /**
     * Batch add multiple addresses
     */
    @Override
    public Task<Void> batchAddAddresses(List<Address> addresses) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot batch add addresses: User not authenticated");
            return Tasks.forException(new Exception("User not authenticated"));
        }
        
        if (addresses == null || addresses.isEmpty()) {
            return Tasks.forResult(null);
        }
        
        // Split into batches of MAX_BATCH_SIZE
        List<List<Address>> batches = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i += MAX_BATCH_SIZE) {
            batches.add(addresses.subList(i, Math.min(i + MAX_BATCH_SIZE, addresses.size())));
        }
        
        List<Task<Void>> batchTasks = new ArrayList<>();
        
        for (List<Address> batch : batches) {
            WriteBatch writeBatch = db.batch();
            
            for (Address address : batch) {
                // Ensure all addresses have the current user ID
                address.setUserId(userId);
                
                // Add to batch
                DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document();
                writeBatch.set(docRef, address.toDocument());
            }
            
            batchTasks.add(writeBatch.commit());
        }
        
        return Tasks.whenAll(batchTasks);
    }
    
    /**
     * Get addresses near a location within a radius
     */
    @Override
    public Task<QuerySnapshot> getAddressesNearLocation(double latitude, double longitude, double radiusKm) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot get addresses near location: User not authenticated");
            return Tasks.forException(new Exception("User not authenticated"));
        }
        
        // This is a simplistic approach; for a proper geospatial query, you'd need a
        // more sophisticated solution like Firestore's GeoPoint with geohashing
        // or a third-party library like GeoFirestore
        
        // For now, we'll just get all addresses for the user and filter them client-side
        return db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .get();
        
        // Note: You would then filter these results client-side based on distance
        // from the provided coordinates, which is not ideal for large datasets
    }
    
    /**
     * Get recent deliveries with limit
     */
    @Override
    public Task<QuerySnapshot> getRecentDeliveries(int limit) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot get recent deliveries: User not authenticated");
            return Tasks.forException(new Exception("User not authenticated"));
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
            return Tasks.forException(new Exception("User not authenticated"));
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
            return Tasks.forException(new Exception("User not authenticated"));
        }
        
        return db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("deliveryCount", minDeliveries)
                .get();
    }
    
    // Cache-specific operations - not implemented in base repository
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
