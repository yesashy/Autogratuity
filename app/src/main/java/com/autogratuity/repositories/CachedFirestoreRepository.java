package com.autogratuity.repositories;

import android.content.Context;
import android.util.Log;
import android.util.LruCache;

import com.autogratuity.models.Address;
import com.autogratuity.models.Delivery;
import com.autogratuity.models.DeliveryData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Cached implementation of IFirestoreRepository
 * Provides in-memory caching of repository operations for better performance
 */
public class CachedFirestoreRepository implements IFirestoreRepository {
    private static final String TAG = "CachedFirestoreRepo";
    
    // Base repository for delegating operations
    private final FirestoreRepository baseRepository;
    
    // In-memory cache for frequently accessed data
    private final LruCache<String, Object> cache;
    
    // Background executor for async operations
    private final Executor executor;
    
    // Cache keys
    private static final String KEY_RECENT_DELIVERIES = "recent_deliveries";
    private static final String KEY_ADDRESS_PREFIX = "address_";
    private static final String KEY_DELIVERY_PREFIX = "delivery_";
    
    // Cache configuration
    private static final int CACHE_SIZE = 100; // Maximum number of entries
    private static final long CACHE_TTL = 5 * 60 * 1000; // 5 minutes in ms
    
    // Cache metadata
    private final Map<String, Date> cacheTimestamps = new HashMap<>();
    private final Map<String, Boolean> dirtyFlags = new HashMap<>();
    
    // Application context
    private final Context context;
    
    /**
     * Create a new cached repository
     */
    public CachedFirestoreRepository(Context context) {
        this.context = context.getApplicationContext();
        this.baseRepository = FirestoreRepository.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        
        // Initialize cache
        this.cache = new LruCache<>(CACHE_SIZE);
    }
    
    /**
     * Check if a cache entry is still valid
     */
    private boolean isCacheValid(String key) {
        Object cacheEntry = cache.get(key);
        if (cacheEntry == null) {
            return false;
        }
        
        Date timestamp = cacheTimestamps.get(key);
        if (timestamp == null) {
            return false;
        }
        
        Boolean isDirty = dirtyFlags.get(key);
        if (isDirty != null && isDirty) {
            return false;
        }
        
        // Check if the cache entry has expired
        long age = System.currentTimeMillis() - timestamp.getTime();
        return age <= CACHE_TTL;
    }
    
    /**
     * Store a value in the cache
     */
    private void cacheValue(String key, Object value) {
        cache.put(key, value);
        cacheTimestamps.put(key, new Date());
        dirtyFlags.put(key, false);
    }
    
    /**
     * Mark a cache entry as dirty
     */
    @Override
    public void setDirty(boolean isDirty) {
        // Implementation depends on context - need to specify which key to dirty
    }
    
    /**
     * Mark a specific cache entry as dirty
     */
    @Override
    public void setDirty(boolean isDirty, String id) {
        dirtyFlags.put(id, isDirty);
        
        // If marking clean and entry doesn't exist, remove the dirty flag
        if (!isDirty && !cache.snapshot().containsKey(id)) {
            dirtyFlags.remove(id);
        }
    }
    
    /**
     * Set the ID for a cached object
     */
    @Override
    public void setId(String id) {
        // Implementation depends on context - need more specific method
    }
    
    /**
     * Set the ID for a cached delivery
     */
    @Override
    public void setId(Delivery delivery, String id) {
        delivery.setId(id);
        String key = KEY_DELIVERY_PREFIX + delivery.getOrderId();
        cacheValue(key, delivery);
    }
    
    /**
     * Set the user ID for cached objects
     */
    @Override
    public void setUserId(String userId) {
        // May require clearing cache for previous user
    }
    
    /**
     * Set the order ID for a cached object
     */
    @Override
    public void setOrderId(String orderId) {
        // Context-dependent implementation
    }
    
    /**
     * Set the address for a cached object
     */
    @Override
    public void setAddress(String address) {
        // Context-dependent implementation
    }
    
    /**
     * Set the tip amount for a cached object
     */
    @Override
    public void setTipAmount(double tipAmount) {
        // Context-dependent implementation
    }
    
    /**
     * Set the last sync time for the cache
     */
    @Override
    public void setLastSyncTime(Date lastSyncTime) {
        // Implementation could track last sync time for cache refresh
    }
    
    /**
     * Invalidate a cache entry
     */
    @Override
    public void invalidateCache(String key) {
        cache.remove(key);
        cacheTimestamps.remove(key);
        dirtyFlags.remove(key);
    }
    
    /**
     * Add a delivery with caching
     */
    @Override
    public Task<DocumentReference> addDelivery(Delivery delivery) {
        // Don't cache writes - pass through to base repository
        return baseRepository.addDelivery(delivery);
    }
    
    /**
     * Find delivery by order ID with caching
     */
    @Override
    public Task<QuerySnapshot> findDeliveryByOrderId(String orderId) {
        // Generate cache key
        final String cacheKey = KEY_DELIVERY_PREFIX + orderId;
        
        // Check if we have a valid cached result
        if (isCacheValid(cacheKey)) {
            Log.d(TAG, "Cache hit for delivery: " + orderId);
            TaskCompletionSource<QuerySnapshot> source = new TaskCompletionSource<>();
            source.setResult((QuerySnapshot) cache.get(cacheKey));
            return source.getTask();
        }
        
        // Cache miss - fetch from network
        Log.d(TAG, "Cache miss for delivery: " + orderId);
        Task<QuerySnapshot> networkTask = baseRepository.findDeliveryByOrderId(orderId);
        
        // Cache the result when it completes
        networkTask.addOnSuccessListener(result -> {
            Log.d(TAG, "Caching delivery query result for: " + orderId);
            cacheValue(cacheKey, result);
        });
        
        return networkTask;
    }
    
    /**
     * Update delivery with tip - invalidates cache
     */
    @Override
    public Task<Void> updateDeliveryWithTip(String documentId, double tipAmount) {
        // Pass through to base repository
        Task<Void> task = baseRepository.updateDeliveryWithTip(documentId, tipAmount);
        
        // Invalidate related caches when the update completes
        task.addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Invalidating caches after delivery tip update");
            invalidateCache(KEY_RECENT_DELIVERIES);
            // Would need delivery details to invalidate specific delivery cache
        });
        
        return task;
    }
    
    /**
     * Store pending tip - pass through
     */
    @Override
    public Task<DocumentReference> storePendingTip(String orderId, double tipAmount) {
        // Don't cache writes - pass through to base repository
        return baseRepository.storePendingTip(orderId, tipAmount);
    }
    
    /**
     * Find address by normalized form with caching
     */
    @Override
    public Task<QuerySnapshot> findAddressByNormalizedAddress(String normalizedAddress) {
        // Generate cache key
        final String cacheKey = KEY_ADDRESS_PREFIX + normalizedAddress;
        
        // Check if we have a valid cached result
        if (isCacheValid(cacheKey)) {
            Log.d(TAG, "Cache hit for address: " + normalizedAddress);
            TaskCompletionSource<QuerySnapshot> source = new TaskCompletionSource<>();
            source.setResult((QuerySnapshot) cache.get(cacheKey));
            return source.getTask();
        }
        
        // Cache miss - fetch from network
        Log.d(TAG, "Cache miss for address: " + normalizedAddress);
        Task<QuerySnapshot> networkTask = baseRepository.findAddressByNormalizedAddress(normalizedAddress);
        
        // Cache the result when it completes
        networkTask.addOnSuccessListener(result -> {
            Log.d(TAG, "Caching address query result for: " + normalizedAddress);
            cacheValue(cacheKey, result);
        });
        
        return networkTask;
    }
    
    /**
     * Update address statistics - invalidates cache
     */
    @Override
    public Task<Void> updateAddressStatistics(String addressId, double tipAmount, String orderId) {
        // Pass through to base repository
        Task<Void> task = baseRepository.updateAddressStatistics(addressId, tipAmount, orderId);
        
        // Invalidate related caches when the update completes
        task.addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Invalidating address caches after statistics update");
            // Invalidate specific address cache
            invalidateCache(KEY_ADDRESS_PREFIX + addressId);
        });
        
        return task;
    }
    
    /**
     * Add address - pass through
     */
    @Override
    public Task<DocumentReference> addAddress(Address address) {
        // Don't cache writes - pass through to base repository
        return baseRepository.addAddress(address);
    }
    
    /**
     * Get addresses by search term with caching
     */
    @Override
    public Task<List<Address>> getAddressesBySearchTerm(String searchTerm, int limit) {
        // Implementation would depend on base repository
        return baseRepository.getAddressesBySearchTerm(searchTerm, limit);
    }
    
    /**
     * Get recent deliveries with caching
     */
    @Override
    public Task<QuerySnapshot> getRecentDeliveries(int limit) {
        // Generate cache key
        final String cacheKey = KEY_RECENT_DELIVERIES + "_" + limit;
        
        // Check if we have a valid cached result
        if (isCacheValid(cacheKey)) {
            Log.d(TAG, "Cache hit for recent deliveries");
            TaskCompletionSource<QuerySnapshot> source = new TaskCompletionSource<>();
            source.setResult((QuerySnapshot) cache.get(cacheKey));
            return source.getTask();
        }
        
        // Cache miss - fetch from network
        Log.d(TAG, "Cache miss for recent deliveries");
        Task<QuerySnapshot> networkTask = baseRepository.getRecentDeliveries(limit);
        
        // Cache the result when it completes
        networkTask.addOnSuccessListener(result -> {
            Log.d(TAG, "Caching recent deliveries result");
            cacheValue(cacheKey, result);
        });
        
        return networkTask;
    }
    
    /**
     * Get deliveries without tips - pass through
     */
    @Override
    public Task<QuerySnapshot> getDeliveriesWithoutTips(Timestamp cutoffDate) {
        // This is likely used for background processing, so don't cache
        return baseRepository.getDeliveriesWithoutTips(cutoffDate);
    }
    
    /**
     * Get addresses with multiple deliveries - pass through
     */
    @Override
    public Task<QuerySnapshot> getAddressesWithMultipleDeliveries(int minDeliveries) {
        // Could implement caching for this, but for simplicity pass through
        return baseRepository.getAddressesWithMultipleDeliveries(minDeliveries);
    }
    
    /**
     * Implement missing method from IFirestoreRepository interface
     * @return The Firestore instance
     */
    @Override
    public FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }
    
    /**
     * Update the "Do Not Deliver" flag for an address
     */
    @Override
    public Task<Void> updateAddressDoNotDeliver(String addressId, boolean doNotDeliver) {
        // Pass through to base repository
        Task<Void> task = baseRepository.updateAddressDoNotDeliver(addressId, doNotDeliver);
        
        // Invalidate related caches when the update completes
        task.addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Invalidating address caches after doNotDeliver update");
            // Invalidate specific address cache
            invalidateCache(KEY_ADDRESS_PREFIX + addressId);
        });
        
        return task;
    }
    
    /**
     * Save multiple deliveries in a batch
     */
    @Override
    public Task<Void> batchAddDeliveries(List<Delivery> deliveries) {
        // Pass through to base repository
        return baseRepository.batchAddDeliveries(deliveries);
    }
    
    /**
     * Save multiple addresses in a batch
     */
    @Override
    public Task<Void> batchAddAddresses(List<Address> addresses) {
        // Pass through to base repository
        return baseRepository.batchAddAddresses(addresses);
    }
    
    /**
     * Find addresses near a location
     */
    @Override
    public Task<QuerySnapshot> getAddressesNearLocation(double latitude, double longitude, double radiusKm) {
        // This is a geospatial query that may be expensive - don't cache for simplicity
        return baseRepository.getAddressesNearLocation(latitude, longitude, radiusKm);
    }
    
    /**
     * Batch save DeliveryData objects
     * Required for GeoJsonImportUtil
     */
    @Override
    public Task<Void> batchSaveDeliveries(List<DeliveryData> deliveries) {
        // Pass through to base repository
        return baseRepository.batchSaveDeliveries(deliveries);
    }
    
    /**
     * Notify listeners that data has changed
     */
    @Override
    public void notifyDataChanged() {
        baseRepository.notifyDataChanged();
    }
    
    /**
     * Mark a delivery as verified
     */
    @Override
    public Task<Void> markAsVerified(String deliveryId, boolean verified) {
        // Create update data
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("tipData.verified", verified);
        
        if (verified) {
            updateData.put("verification.verifiedByPro", true);
            updateData.put("verification.verificationTimestamp", new Timestamp(new Date()));
        }
        
        // Get Firestore reference
        FirebaseFirestore db = getFirestore();
        
        // Update document
        return db.collection("deliveries")
                .document(deliveryId)
                .set(updateData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully updated verification status for " + deliveryId);
                    // Invalidate cache for this delivery
                    invalidateCache(KEY_DELIVERY_PREFIX + deliveryId);
                })
                .addOnFailureListener(e -> 
                        Log.e(TAG, "Error updating verification status: " + e.getMessage()));
    }
    
    /**
     * Update verification status with source and notes
     */
    @Override
    public Task<Void> updateVerificationStatus(String deliveryId, String source, String notes) {
        // Create update data
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("verification.verifiedByPro", true);
        updateData.put("verification.verificationTimestamp", new Timestamp(new Date()));
        updateData.put("verification.verificationSource", source);
        
        if (notes != null && !notes.isEmpty()) {
            updateData.put("verification.verificationNotes", notes);
        }
        
        // Get Firestore reference
        FirebaseFirestore db = getFirestore();
        
        // Update document
        return db.collection("deliveries")
                .document(deliveryId)
                .set(updateData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully updated verification details for " + deliveryId);
                    // Invalidate cache for this delivery
                    invalidateCache(KEY_DELIVERY_PREFIX + deliveryId);
                })
                .addOnFailureListener(e -> 
                        Log.e(TAG, "Error updating verification details: " + e.getMessage()));
    }
}