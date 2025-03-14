package com.autogratuity.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.autogratuity.models.Address;
import com.autogratuity.models.Delivery;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class for handling all Firestore database operations
 * with support for the nested document structure
 */
public class FirestoreRepository {
    private static final String TAG = "FirestoreRepository";

    // Collection names
    private static final String COLLECTION_DELIVERIES = "deliveries";
    private static final String COLLECTION_ADDRESSES = "addresses";
    private static final String COLLECTION_USER_USAGE = "user_usage";
    private static final String COLLECTION_USER_SUBSCRIPTIONS = "user_subscriptions";
    private static final String COLLECTION_PENDING_TIPS = "pending_tips";

    // Singleton instance
    private static FirestoreRepository instance;

    // Firebase instances
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    /**
     * Private constructor for singleton pattern
     */
    private FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Get singleton instance of the repository
     * @return FirestoreRepository instance
     */
    public static synchronized FirestoreRepository getInstance() {
        if (instance == null) {
            instance = new FirestoreRepository();
        }
        return instance;
    }

    /**
     * Get current user ID or null if not logged in
     * @return User ID string or null
     */
    @Nullable
    private String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    // ==================== DELIVERY OPERATIONS ====================

    /**
     * Add a new delivery with nested document structure
     * @param delivery Delivery object to add
     * @return Task with DocumentReference result
     */
    public Task<DocumentReference> addDelivery(Delivery delivery) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to add deliveries");
        }

        delivery.setUserId(userId);

        // Ensure we have timestamps for required date fields
        if (delivery.getDeliveryDate() == null) {
            delivery.setDeliveryDate(Timestamp.now());
        }

        if (delivery.getImportDate() == null) {
            delivery.setImportDate(Timestamp.now());
        }

        // Convert to document with nested structure
        Map<String, Object> document = delivery.toDocument();
        Log.d(TAG, "Adding delivery: " + delivery.getOrderId());

        return db.collection(COLLECTION_DELIVERIES).add(document);
    }

    /**
     * Update an existing delivery
     * @param deliveryId Document ID of the delivery
     * @param updates Map of fields to update (supports nested paths with dots)
     * @return Task with Void result
     */
    public Task<Void> updateDelivery(String deliveryId, Map<String, Object> updates) {
        Log.d(TAG, "Updating delivery: " + deliveryId);
        return db.collection(COLLECTION_DELIVERIES).document(deliveryId).update(updates);
    }

    /**
     * Update delivery with tip information
     * @param deliveryId Document ID of the delivery
     * @param tipAmount Tip amount to set
     * @return Task with Void result
     */
    public Task<Void> updateDeliveryWithTip(String deliveryId, double tipAmount) {
        Map<String, Object> updates = new HashMap<>();

        // Create nested updates
        Map<String, Object> amounts = new HashMap<>();
        amounts.put("tipAmount", tipAmount);
        updates.put("amounts", amounts);

        Map<String, Object> status = new HashMap<>();
        status.put("isTipped", true);
        updates.put("status", status);

        Map<String, Object> dates = new HashMap<>();
        dates.put("tipped", Timestamp.now());
        updates.put("dates", dates);

        Log.d(TAG, "Updating delivery with tip: " + deliveryId + ", amount: $" + tipAmount);
        return updateDelivery(deliveryId, updates);
    }

    /**
     * Mark a delivery as "Do Not Deliver"
     * @param deliveryId Document ID of the delivery
     * @return Task with Void result
     */
    public Task<Void> markDeliveryAsDoNotDeliver(String deliveryId) {
        Map<String, Object> updates = new HashMap<>();
        Map<String, Object> status = new HashMap<>();
        status.put("doNotDeliver", true);
        updates.put("status", status);

        Log.d(TAG, "Marking delivery as Do Not Deliver: " + deliveryId);
        return updateDelivery(deliveryId, updates);
    }

    /**
     * Get a delivery by ID
     * @param deliveryId Document ID of the delivery
     * @return Task with DocumentSnapshot result
     */
    public Task<DocumentSnapshot> getDelivery(String deliveryId) {
        return db.collection(COLLECTION_DELIVERIES).document(deliveryId).get();
    }

    /**
     * Get all deliveries for current user
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getAllDeliveries() {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to get deliveries");
        }

        return db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .orderBy("dates.accepted", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get recent deliveries for current user
     * @param limit Maximum number of deliveries to retrieve
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getRecentDeliveries(int limit) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to get deliveries");
        }

        return db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .orderBy("dates.accepted", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }

    /**
     * Get deliveries with pending tips (no tip amount set)
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getPendingTipDeliveries() {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to get deliveries");
        }

        return db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status.isTipped", false)
                .orderBy("dates.accepted", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get deliveries within a date range
     * @param startDate Start date
     * @param endDate End date
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getDeliveriesInDateRange(Date startDate, Date endDate) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to get deliveries");
        }

        return db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("dates.accepted", new Timestamp(startDate))
                .whereLessThanOrEqualTo("dates.accepted", new Timestamp(endDate))
                .orderBy("dates.accepted", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Delete a delivery
     * @param deliveryId Document ID of the delivery
     * @return Task with Void result
     */
    public Task<Void> deleteDelivery(String deliveryId) {
        Log.d(TAG, "Deleting delivery: " + deliveryId);
        return db.collection(COLLECTION_DELIVERIES).document(deliveryId).delete();
    }

    // ==================== ADDRESS OPERATIONS ====================

    /**
     * Add a new address
     * @param address Address object to add
     * @return Task with DocumentReference result
     */
    public Task<DocumentReference> addAddress(Address address) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to add addresses");
        }

        address.setUserId(userId);

        if (address.getLastUpdated() == null) {
            address.setLastUpdated(Timestamp.now());
        }

        Log.d(TAG, "Adding address: " + address.getFullAddress());
        return db.collection(COLLECTION_ADDRESSES).add(address.toDocument());
    }

    /**
     * Update an existing address
     * @param addressId Document ID of the address
     * @param updates Map of fields to update
     * @return Task with Void result
     */
    public Task<Void> updateAddress(String addressId, Map<String, Object> updates) {
        // Always update the lastUpdated timestamp
        updates.put("lastUpdated", Timestamp.now());

        Log.d(TAG, "Updating address: " + addressId);
        return db.collection(COLLECTION_ADDRESSES).document(addressId).update(updates);
    }

    /**
     * Update address with tip statistics
     * @param addressId Document ID of the address
     * @param tipAmount New tip amount to add
     * @return Task with Void result
     */
    public Task<Void> updateAddressTipStatistics(String addressId, double tipAmount) {
        // First get the current address data
        return db.collection(COLLECTION_ADDRESSES).document(addressId).get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw new Exception("Failed to retrieve address data");
                    }

                    DocumentSnapshot document = task.getResult();
                    double currentTotalTips = document.getDouble("totalTips") != null ?
                            document.getDouble("totalTips") : 0.0;
                    long deliveryCount = document.getLong("deliveryCount") != null ?
                            document.getLong("deliveryCount") : 0;

                    if (deliveryCount == 0) {
                        deliveryCount = 1; // Prevent division by zero
                    }

                    double newTotalTips = currentTotalTips + tipAmount;
                    double newAverageTip = newTotalTips / deliveryCount;

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("totalTips", newTotalTips);
                    updates.put("averageTip", newAverageTip);
                    updates.put("lastUpdated", Timestamp.now());

                    return updateAddress(addressId, updates);
                });
    }

    /**
     * Add an order ID to an address
     * @param addressId Document ID of the address
     * @param orderId Order ID to add
     * @return Task with Void result
     */
    public Task<Void> addOrderIdToAddress(String addressId, String orderId) {
        return db.collection(COLLECTION_ADDRESSES).document(addressId).get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw new Exception("Failed to retrieve address data");
                    }

                    DocumentSnapshot document = task.getResult();
                    List<String> orderIds = new ArrayList<>();

                    // Get existing order IDs
                    if (document.contains("orderIds") && document.get("orderIds") instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> existingOrderIds = (List<String>) document.get("orderIds");
                        orderIds.addAll(existingOrderIds);
                    }

                    // Add new order ID if not already present
                    if (!orderIds.contains(orderId)) {
                        orderIds.add(orderId);
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("orderIds", orderIds);

                    // Update delivery count
                    long deliveryCount = document.getLong("deliveryCount") != null ?
                            document.getLong("deliveryCount") : 0;
                    updates.put("deliveryCount", deliveryCount + 1);

                    return updateAddress(addressId, updates);
                });
    }

    /**
     * Mark an address as "Do Not Deliver"
     * @param addressId Document ID of the address
     * @return Task with Void result
     */
    public Task<Void> markAddressAsDoNotDeliver(String addressId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("doNotDeliver", true);

        Log.d(TAG, "Marking address as Do Not Deliver: " + addressId);
        return updateAddress(addressId, updates);
    }

    /**
     * Get an address by ID
     * @param addressId Document ID of the address
     * @return Task with DocumentSnapshot result
     */
    public Task<DocumentSnapshot> getAddress(String addressId) {
        return db.collection(COLLECTION_ADDRESSES).document(addressId).get();
    }

    /**
     * Get address by normalized address string
     * @param normalizedAddress Normalized address string (lowercase, trimmed)
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getAddressByNormalizedAddress(String normalizedAddress) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to get addresses");
        }

        return db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("normalizedAddress", normalizedAddress)
                .limit(1)
                .get();
    }

    /**
     * Get all addresses for current user
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getAllAddresses() {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to get addresses");
        }

        return db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get best tipping addresses
     * @param limit Maximum number of addresses to retrieve
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getBestTippingAddresses(int limit) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to get addresses");
        }

        return db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .whereGreaterThan("averageTip", 0)
                .orderBy("averageTip", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }

    /**
     * Search addresses by term
     * @param searchTerm Term to search for
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> searchAddresses(String searchTerm) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to search addresses");
        }

        return db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .whereArrayContains("searchTerms", searchTerm.toLowerCase().trim())
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .get();
    }

    // ==================== BATCHED OPERATIONS ====================

    /**
     * Process a group of deliveries in a batch
     * @param deliveries List of deliveries to process
     * @return Task with Void result
     */
    public Task<Void> processBatchDeliveries(List<Delivery> deliveries) {
        WriteBatch batch = db.batch();
        List<DocumentReference> refs = new ArrayList<>();

        for (Delivery delivery : deliveries) {
            DocumentReference ref = db.collection(COLLECTION_DELIVERIES).document();
            batch.set(ref, delivery.toDocument());
            refs.add(ref);
        }

        Log.d(TAG, "Processing batch of " + deliveries.size() + " deliveries");
        return batch.commit();
    }

    /**
     * Update a set of deliveries with "Do Not Deliver" status
     * @param deliveryIds List of delivery document IDs
     * @return Task with Void result
     */
    public Task<Void> batchMarkAsDoNotDeliver(List<String> deliveryIds) {
        WriteBatch batch = db.batch();

        Map<String, Object> updates = new HashMap<>();
        Map<String, Object> status = new HashMap<>();
        status.put("doNotDeliver", true);
        updates.put("status", status);

        for (String id : deliveryIds) {
            DocumentReference ref = db.collection(COLLECTION_DELIVERIES).document(id);
            batch.update(ref, updates);
        }

        Log.d(TAG, "Batch marking " + deliveryIds.size() + " deliveries as Do Not Deliver");
        return batch.commit();
    }

    // ==================== DASHBOARD DATA OPERATIONS ====================

    /**
     * Get statistics for recent deliveries
     * @param daysAgo Number of days to look back
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getDeliveryStatistics(int daysAgo) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in to get statistics");
        }

        // Calculate date range
        Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo);
        Date startDate = calendar.getTime();

        return db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("dates.accepted", new Timestamp(startDate))
                .orderBy("dates.accepted", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get pending tip deliveries older than a certain threshold
     * @param thresholdDays Number of days threshold
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getOldPendingTipDeliveries(int thresholdDays) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in");
        }

        // Calculate threshold date
        Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -thresholdDays);
        Date thresholdDate = calendar.getTime();
        Timestamp thresholdTimestamp = new Timestamp(thresholdDate);

        return db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .whereLessThan("dates.accepted", thresholdTimestamp)
                .whereEqualTo("status.isTipped", false)
                .whereEqualTo("status.doNotDeliver", false)
                .get();
    }

    // ==================== PENDING TIPS OPERATIONS ====================

    /**
     * Store a pending tip when delivery not found
     * @param orderId Order ID
     * @param tipAmount Tip amount
     * @return Task with DocumentReference result
     */
    public Task<DocumentReference> storePendingTip(String orderId, double tipAmount) {
        Map<String, Object> pendingTip = new HashMap<>();
        pendingTip.put("orderId", orderId);

        Map<String, Object> amounts = new HashMap<>();
        amounts.put("tipAmount", tipAmount);
        pendingTip.put("amounts", amounts);

        pendingTip.put("timestamp", Timestamp.now());
        pendingTip.put("processed", false);

        if (getCurrentUserId() != null) {
            pendingTip.put("userId", getCurrentUserId());
        }

        Log.d(TAG, "Storing pending tip: Order #" + orderId + ", Amount: $" + tipAmount);
        return db.collection(COLLECTION_PENDING_TIPS).add(pendingTip);
    }

    /**
     * Get all unprocessed pending tips
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getUnprocessedPendingTips() {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in");
        }

        return db.collection(COLLECTION_PENDING_TIPS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("processed", false)
                .get();
    }

    /**
     * Mark a pending tip as processed
     * @param pendingTipId Document ID of the pending tip
     * @return Task with Void result
     */
    public Task<Void> markPendingTipAsProcessed(String pendingTipId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("processed", true);
        updates.put("processedAt", Timestamp.now());

        return db.collection(COLLECTION_PENDING_TIPS).document(pendingTipId).update(updates);
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Update user usage data
     * @param mappingCount Number of mappings used
     * @return Task with Void result
     */
    public Task<Void> updateUserUsage(int mappingCount) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in");
        }

        Map<String, Object> usage = new HashMap<>();
        usage.put("mappingCount", mappingCount);
        usage.put("userId", userId);

        if (auth.getCurrentUser().getEmail() != null) {
            usage.put("email", auth.getCurrentUser().getEmail());
        }

        usage.put("lastUpdated", Timestamp.now());

        return db.collection(COLLECTION_USER_USAGE).document(userId).set(usage);
    }

    /**
     * Get user usage data
     * @return Task with DocumentSnapshot result
     */
    public Task<DocumentSnapshot> getUserUsage() {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in");
        }

        return db.collection(COLLECTION_USER_USAGE).document(userId).get();
    }

    /**
     * Get user subscription data
     * @return Task with DocumentSnapshot result
     */
    public Task<DocumentSnapshot> getUserSubscription() {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in");
        }

        return db.collection(COLLECTION_USER_SUBSCRIPTIONS).document(userId).get();
    }

    /**
     * Update user subscription
     * @param subscriptionLevel Subscription level (free, trial, pro)
     * @param expiryDate Expiry date for subscription
     * @return Task with Void result
     */
    public Task<Void> updateUserSubscription(String subscriptionLevel, Date expiryDate) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User must be logged in");
        }

        Map<String, Object> subscription = new HashMap<>();
        subscription.put("userId", userId);
        subscription.put("subscriptionLevel", subscriptionLevel);
        subscription.put("startDate", Timestamp.now());

        if (expiryDate != null) {
            subscription.put("expiryDate", new Timestamp(expiryDate));
        }

        subscription.put("lastUpdated", Timestamp.now());

        return db.collection(COLLECTION_USER_SUBSCRIPTIONS).document(userId).set(subscription);
    }

    // Helper method to get the current Calendar instance
    private java.util.Calendar Calendar() {
        return java.util.Calendar.getInstance();
    }
}