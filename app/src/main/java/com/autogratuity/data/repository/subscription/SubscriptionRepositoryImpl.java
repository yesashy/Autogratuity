package com.autogratuity.data.repository.subscription;

import android.content.Context;
import android.util.Log;

import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.repository.core.FirestoreRepository;
import com.autogratuity.data.security.EncryptionUtils;
import com.autogratuity.data.security.ValidationUtils;
import com.autogratuity.data.serialization.SubscriptionSerializer;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Implementation of SubscriptionRepository interface.
 * Responsible for managing subscription data and operations.
 */
public class SubscriptionRepositoryImpl extends FirestoreRepository implements SubscriptionRepository {
    
    private static final String TAG = "SubscriptionRepository";
    
    // Collection and field names
    private static final String COLLECTION_SUBSCRIPTION_RECORDS = "subscription_records";
    private static final String COLLECTION_USER_PROFILES = "user_profiles";
    
    // Subscription status fields
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_LEVEL = "subscriptionLevel";
    private static final String FIELD_IS_ACTIVE = "isActive";
    private static final String FIELD_IS_LIFETIME = "isLifetime";
    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_EXPIRY_DATE = "expiryDate";
    private static final String FIELD_PAYMENT_PROVIDER = "paymentProvider";
    private static final String FIELD_ORDER_ID = "orderId";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_UPDATED_AT = "updatedAt";
    private static final String FIELD_VERIFICATION_DATA = "verificationData";
    
    // Cache keys
    private static final String CACHE_SUBSCRIPTION_STATUS = "subscription_status";
    private static final String PREFS_SUBSCRIPTION_STATUS = "subscription_status";
    
    // Subject for real-time updates
    private final BehaviorSubject<SubscriptionStatus> subscriptionStatusSubject = BehaviorSubject.create();
    
    /**
     * Constructor for SubscriptionRepositoryImpl
     * 
     * @param context Android context for SharedPreferences and connectivity
     */
    public SubscriptionRepositoryImpl(Context context) {
        super(context);
        
        // Initialize encryption for subscription data
        EncryptionUtils.getInstance(context);
        SubscriptionSerializer.init(context);
        
        setupSubscriptionListener();
    }
    
    /**
     * Sets up real-time listener for subscription status changes
     */
    private void setupSubscriptionListener() {
        String listenerKey = "subscriptionRecords_" + userId + "_listener";
        
        // Create new listener
        ListenerRegistration listener = db.collection(COLLECTION_SUBSCRIPTION_RECORDS)
                .whereEqualTo(FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_IS_ACTIVE, true)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening to subscription records", e);
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        processSubscriptionRecords(querySnapshot);
                    }
                });
        
        // Store listener for cleanup
        activeListeners.put(listenerKey, listener);
    }
    
    /**
     * Process subscription records from a query snapshot
     * 
     * @param querySnapshot The query snapshot containing subscription records
     */
    private void processSubscriptionRecords(QuerySnapshot querySnapshot) {
        if (querySnapshot.isEmpty()) {
            // No active subscriptions, emit a free status
            SubscriptionStatus freeStatus = new SubscriptionStatus(userId);
            updateSubscriptionCache(freeStatus);
            subscriptionStatusSubject.onNext(freeStatus);
            return;
        }
        
        // Find the most relevant subscription (lifetime first, then latest expiry)
        SubscriptionStatus status = new SubscriptionStatus(userId);
        boolean foundLifetime = false;
        Date latestExpiry = null;
        DocumentSnapshot latestSubscription = null;
        
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            // Check for lifetime subscription first
            Boolean isLifetime = doc.getBoolean(FIELD_IS_LIFETIME);
            if (isLifetime != null && isLifetime) {
                foundLifetime = true;
                latestSubscription = doc;
                break;
            }
            
            // Otherwise check expiry date
            Date expiryDate = doc.getDate(FIELD_EXPIRY_DATE);
            if (expiryDate != null && (latestExpiry == null || expiryDate.after(latestExpiry))) {
                latestExpiry = expiryDate;
                latestSubscription = doc;
            }
        }
        
        // Use the most relevant subscription
        if (latestSubscription != null) {
            status = convertDocumentToSubscriptionStatus(latestSubscription);
            updateSubscriptionCache(status);
            subscriptionStatusSubject.onNext(status);
        } else {
            // Shouldn't reach here, but just in case
            SubscriptionStatus freeStatus = new SubscriptionStatus(userId);
            updateSubscriptionCache(freeStatus);
            subscriptionStatusSubject.onNext(freeStatus);
        }
    }
    
    /**
     * Convert a Firestore document to a SubscriptionStatus object
     * 
     * @param doc The document snapshot
     * @return The subscription status
     */
    private SubscriptionStatus convertDocumentToSubscriptionStatus(DocumentSnapshot doc) {
        // Use the serializer to convert the document to a subscription status
        // This handles decryption of sensitive fields
        return SubscriptionSerializer.fromDocumentSnapshot(doc, context);
    }
    
    /**
     * Update subscription cache
     * 
     * @param status The subscription status to cache
     */
    private void updateSubscriptionCache(SubscriptionStatus status) {
        putInCache(CACHE_SUBSCRIPTION_STATUS + "_" + userId, status);
        saveToPrefs(PREFS_SUBSCRIPTION_STATUS, status);
    }
    
    @Override
    public Single<SubscriptionStatus> getSubscriptionStatus() {
        return Single.create(emitter -> {
            // First try memory cache
            SubscriptionStatus cached = getFromCache(CACHE_SUBSCRIPTION_STATUS + "_" + userId);
            if (cached != null) {
                emitter.onSuccess(cached);
                return;
            }
            
            // Then try SharedPreferences
            if (!isNetworkAvailable()) {
                SubscriptionStatus prefsStatus = getFromPrefs(PREFS_SUBSCRIPTION_STATUS, SubscriptionStatus.class);
                if (prefsStatus != null) {
                    updateSubscriptionCache(prefsStatus);
                    emitter.onSuccess(prefsStatus);
                    return;
                }
            }
            
            // Finally, query Firestore for active subscription records
            db.collection(COLLECTION_SUBSCRIPTION_RECORDS)
                    .whereEqualTo(FIELD_USER_ID, userId)
                    .whereEqualTo(FIELD_IS_ACTIVE, true)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            // No active subscriptions, return a free status
                            SubscriptionStatus freeStatus = new SubscriptionStatus(userId);
                            updateSubscriptionCache(freeStatus);
                            emitter.onSuccess(freeStatus);
                            return;
                        }
                        
                        // Find the most relevant subscription (lifetime first, then latest expiry)
                        SubscriptionStatus status = new SubscriptionStatus(userId);
                        boolean foundLifetime = false;
                        Date latestExpiry = null;
                        DocumentSnapshot latestSubscription = null;
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            // Check for lifetime subscription first
                            Boolean isLifetime = doc.getBoolean(FIELD_IS_LIFETIME);
                            if (isLifetime != null && isLifetime) {
                                foundLifetime = true;
                                latestSubscription = doc;
                                break;
                            }
                            
                            // Otherwise check expiry date
                            Date expiryDate = doc.getDate(FIELD_EXPIRY_DATE);
                            if (expiryDate != null && (latestExpiry == null || expiryDate.after(latestExpiry))) {
                                latestExpiry = expiryDate;
                                latestSubscription = doc;
                            }
                        }
                        
                        if (latestSubscription != null) {
                            status = convertDocumentToSubscriptionStatus(latestSubscription);
                            updateSubscriptionCache(status);
                            emitter.onSuccess(status);
                        } else {
                            // Shouldn't reach here, but just in case
                            SubscriptionStatus freeStatus = new SubscriptionStatus(userId);
                            updateSubscriptionCache(freeStatus);
                            emitter.onSuccess(freeStatus);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If offline, try to create a free status
                        if (!isNetworkAvailable()) {
                            SubscriptionStatus freeStatus = new SubscriptionStatus(userId);
                            emitter.onSuccess(freeStatus);
                        } else {
                            Log.e(TAG, "Error getting subscription status", e);
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Completable updateSubscriptionStatus(SubscriptionStatus status) {
        if (status == null) {
            return Completable.error(new IllegalArgumentException("Subscription status cannot be null"));
        }
        
        // Ensure user ID is set
        status.setUserId(userId);
        
        // Generate a record ID if this is a new subscription
        String recordId = userId + "_" + (status.isLifetime() ? "lifetime" : System.currentTimeMillis());
        DocumentReference docRef = db.collection(COLLECTION_SUBSCRIPTION_RECORDS).document(recordId);
        
        // Convert SubscriptionStatus to subscription record using the serializer
        // This handles encryption of sensitive fields
        Map<String, Object> record = SubscriptionSerializer.toMap(status, context);
        
        // Add additional fields not included in the standard serialization
        record.put(FIELD_TYPE, "purchase");
        record.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        
        return Completable.create(emitter -> {
            docRef.set(record)
                    .addOnSuccessListener(aVoid -> {
                        // Cache the status
                        updateSubscriptionCache(status);
                        
                        // Also update user profile with subscription info
                        updateUserProfileWithSubscription(status)
                                .subscribe(
                                        emitter::onComplete,
                                        emitter::onError
                                );
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // Save locally anyway
                            updateSubscriptionCache(status);
                            
                            // Create sync operation
                            enqueueOperation("create", COLLECTION_SUBSCRIPTION_RECORDS, recordId, record)
                                    .subscribe(
                                            () -> {
                                                // Also update user profile with subscription info locally
                                                updateUserProfileWithSubscription(status)
                                                        .subscribe(
                                                                emitter::onComplete,
                                                                emitter::onError
                                                        );
                                            },
                                            emitter::onError
                                    );
                        } else {
                            Log.e(TAG, "Error updating subscription status", e);
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    /**
     * Helper method to update user profile with subscription information
     * 
     * @param status The subscription status
     * @return Completable that completes when update is finished
     */
    private Completable updateUserProfileWithSubscription(SubscriptionStatus status) {
        Map<String, Object> updates = new HashMap<>();
        
        Map<String, Object> subscription = new HashMap<>();
        subscription.put("status", status.getStatus());
        subscription.put("level", status.getLevel());
        subscription.put("isLifetime", status.isLifetime());
        subscription.put("startDate", status.getStartDate());
        subscription.put("expiryDate", status.getExpiryDate());
        subscription.put("provider", status.getProvider());
        subscription.put("orderId", status.getOrderId());
        subscription.put("lastVerified", status.getLastVerified());
        
        updates.put("subscription", subscription);
        
        return updateUserProfileFields(updates);
    }
    
    @Override
    public Single<DocumentReference> addSubscriptionRecord(Map<String, Object> subscriptionRecord) {
        if (subscriptionRecord == null) {
            return Single.error(new IllegalArgumentException("Subscription record cannot be null"));
        }
        
        // Validate sensitive fields
        String orderId = (String) subscriptionRecord.get("orderId");
        String provider = (String) subscriptionRecord.get("provider");
        String payload = (String) subscriptionRecord.get("verificationPayload");
        
        if (orderId != null) {
            subscriptionRecord.put("orderId", ValidationUtils.sanitizeString(orderId));
        }
        
        if (provider != null) {
            subscriptionRecord.put("provider", ValidationUtils.sanitizeString(provider));
        }
        
        if (payload != null && !ValidationUtils.isValidSubscriptionPayload(payload)) {
            return Single.error(new IllegalArgumentException("Invalid subscription verification payload"));
        }
        
        // Generate document ID
        String recordId = (String) subscriptionRecord.get("recordId");
        if (recordId == null || recordId.isEmpty()) {
            recordId = userId + "_" + System.currentTimeMillis();
            subscriptionRecord.put("recordId", recordId);
        }
        
        // Make sure we have required fields
        subscriptionRecord.put(FIELD_USER_ID, userId);
        subscriptionRecord.put("createdAt", FieldValue.serverTimestamp());
        subscriptionRecord.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        
        // Version for conflict resolution
        subscriptionRecord.put("version", 1);
        
        DocumentReference docRef = db.collection(COLLECTION_SUBSCRIPTION_RECORDS).document(recordId);
        
        return Single.create(emitter -> {
            docRef.set(subscriptionRecord)
                    .addOnSuccessListener(aVoid -> {
                        // Invalidate cache
                        invalidateCache(CACHE_SUBSCRIPTION_STATUS + "_" + userId);
                        
                        emitter.onSuccess(docRef);
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // Create sync operation
                            enqueueOperation("create", COLLECTION_SUBSCRIPTION_RECORDS, recordId, subscriptionRecord)
                                    .subscribe(
                                            () -> emitter.onSuccess(docRef),
                                            emitter::onError
                                    );
                        } else {
                            Log.e(TAG, "Error adding subscription record", e);
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Observable<SubscriptionStatus> observeSubscriptionStatus() {
        // Ensure we have initial value
        if (!subscriptionStatusSubject.hasValue()) {
            getSubscriptionStatus()
                .subscribe(
                    status -> {}, // Will be emitted through the subject by the listener
                    error -> Log.e(TAG, "Error getting initial subscription status", error)
                );
        }
        
        return subscriptionStatusSubject;
    }
    
    @Override
    public Completable verifySubscription() {
        return getSubscriptionStatus()
                .flatMapCompletable(status -> {
                    // Skip verification for free/lifetime
                    if (status.isLifetime() || "free".equals(status.getStatus())) {
                        return Completable.complete();
                    }
                    
                    // Set verification status to verified and timestamp
                    status.setVerificationStatus("verified");
                    status.setLastVerified(new Date());
                    
                    // If expired, mark as inactive
                    if (status.getExpiryDate() != null && status.getExpiryDate().before(new Date())) {
                        status.setActive(false);
                    }
                    
                    return updateSubscriptionStatus(status);
                });
    }
    
    @Override
    public Single<Boolean> isProUser() {
        return getSubscriptionStatus()
                .map(SubscriptionStatus::isPro);
    }
    
    @Override
    public Single<Date> getSubscriptionExpiryDate() {
        return getSubscriptionStatus()
                .map(status -> {
                    if ("free".equals(status.getStatus()) || status.isLifetime()) {
                        return null;
                    }
                    return status.getExpiryDate();
                });
    }
    
    @Override
    public Single<Boolean> isSubscriptionExpired() {
        return getSubscriptionStatus()
                .map(status -> {
                    if ("free".equals(status.getStatus()) || status.isLifetime()) {
                        return false;
                    }
                    
                    Date expiryDate = status.getExpiryDate();
                    if (expiryDate == null) {
                        return false;
                    }
                    
                    return expiryDate.before(new Date());
                });
    }
    
    @Override
    public Single<Boolean> isLifetimeSubscription() {
        return getSubscriptionStatus()
                .map(status -> status.isLifetime() || "lifetime".equals(status.getStatus()));
    }
    
    @Override
    public Single<String> getSubscriptionLevel() {
        return getSubscriptionStatus()
                .map(status -> {
                    String level = status.getLevel();
                    if (level == null || level.isEmpty()) {
                        return status.getStatus();
                    }
                    return level;
                });
    }
    
    @Override
    public Completable upgradeSubscription(int durationMonths, Map<String, Object> paymentDetails) {
        return Single.fromCallable(() -> {
            boolean isLifetime = durationMonths <= 0;
            
            SubscriptionStatus status = new SubscriptionStatus(userId);
            status.setStatus(isLifetime ? "lifetime" : "pro");
            status.setLifetime(isLifetime);
            status.setActive(true);
            
            // Set provider and order ID if provided
            if (paymentDetails != null) {
                status.setProvider((String) paymentDetails.get("provider"));
                status.setOrderId((String) paymentDetails.get("orderId"));
            }
            
            // Set dates
            Date now = new Date();
            status.setStartDate(now);
            
            if (!isLifetime) {
                // Calculate expiry date
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(now);
                calendar.add(Calendar.MONTH, durationMonths);
                status.setExpiryDate(calendar.getTime());
            }
            
            // Set verification status to pending
            status.setVerificationStatus("pending");
            
            return status;
        }).flatMapCompletable(this::updateSubscriptionStatus);
    }
    
    @Override
    public Completable cancelSubscription(boolean immediate) {
        return getSubscriptionStatus()
                .flatMapCompletable(status -> {
                    // Skip for free
                    if ("free".equals(status.getStatus())) {
                        return Completable.complete();
                    }
                    
                    if (immediate) {
                        // Immediately cancel by setting to inactive
                        status.setActive(false);
                        return updateSubscriptionStatus(status);
                    } else {
                        // Add a cancellation record but leave current subscription active
                        Map<String, Object> cancelRecord = new HashMap<>();
                        cancelRecord.put(FIELD_USER_ID, userId);
                        cancelRecord.put(FIELD_TYPE, "cancellation");
                        cancelRecord.put("cancelledAt", new Date());
                        cancelRecord.put("effectiveAt", status.getExpiryDate());
                        cancelRecord.put("reasonCode", "user_initiated");
                        
                        return addSubscriptionRecord(cancelRecord)
                                .ignoreElement();
                    }
                });
    }
    
    @Override
    public Single<List<Map<String, Object>>> getSubscriptionHistory() {
        return Single.create(emitter -> {
            db.collection(COLLECTION_SUBSCRIPTION_RECORDS)
                    .whereEqualTo(FIELD_USER_ID, userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Map<String, Object>> history = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Map<String, Object> record = doc.getData();
                            if (record != null) {
                                // Add ID for reference
                                record.put("id", doc.getId());
                                history.add(record);
                            }
                        }
                        
                        emitter.onSuccess(history);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting subscription history", e);
                        emitter.onError(e);
                    });
        });
    }
    
    /**
     * Clean up resources when the repository is no longer needed
     */
    public void cleanup() {
        // Remove subscription listener
        String listenerKey = "subscriptionRecords_" + userId + "_listener";
        if (activeListeners.containsKey(listenerKey)) {
            activeListeners.get(listenerKey).remove();
            activeListeners.remove(listenerKey);
        }
    }
}
