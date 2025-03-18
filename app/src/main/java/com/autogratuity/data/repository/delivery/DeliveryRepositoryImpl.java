package com.autogratuity.data.repository.delivery;

import android.content.Context;
import android.util.Log;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.DeliveryStats;
import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.repository.core.FirestoreRepository;
import com.autogratuity.data.serialization.DeliverySerializer;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Implementation of DeliveryRepository interface.
 * Responsible for managing delivery-related data operations.
 */
public class DeliveryRepositoryImpl extends FirestoreRepository implements DeliveryRepository {
    
    private static final String TAG = "DeliveryRepository";
    
    // Collection and field names
    private static final String COLLECTION_DELIVERIES = "deliveries";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_TIMESTAMP = "times.completedAt";
    private static final String FIELD_TIP_AMOUNT = "amounts.tipAmount";
    private static final String FIELD_ADDRESS_ID = "reference.addressId";
    
    // Cache keys
    private static final String CACHE_DELIVERIES_PREFIX = "deliveries_";
    private static final String CACHE_DELIVERY_PREFIX = "delivery_";
    private static final String CACHE_STATS_PREFIX = "delivery_stats_";
    
    // Subject for real-time updates
    private final BehaviorSubject<List<Delivery>> deliveriesSubject = BehaviorSubject.create();
    private final Map<String, BehaviorSubject<Delivery>> deliverySubjects = new HashMap<>();
    
    /**
     * Constructor for DeliveryRepositoryImpl
     * 
     * @param context Android context for SharedPreferences and connectivity
     */
    public DeliveryRepositoryImpl(Context context) {
        super(context);
        setupDeliveriesListener();
    }
    
    /**
     * Sets up real-time listener for deliveries updates
     */
    private void setupDeliveriesListener() {
        if (getCurrentUser() == null) return;
        
        // Set up listener for recent deliveries
        String listenerKey = "deliveries_" + getCurrentUserId() + "_listener";
        ListenerRegistration existingListener = activeListeners.get(listenerKey);
        
        if (existingListener != null) {
            existingListener.remove();
        }
        
        ListenerRegistration listener = db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo(FIELD_USER_ID, getCurrentUserId())
                .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                .limit(50) // Limit the number of deliveries to avoid performance issues
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening to deliveries", e);
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        List<Delivery> deliveries = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Delivery delivery = doc.toObject(Delivery.class);
                            if (delivery != null) {
                                // Ensure ID is set
                                if (delivery.getDeliveryId() == null) {
                                    delivery.setDeliveryId(doc.getId());
                                }
                                deliveries.add(delivery);
                                
                                // Update individual delivery subjects if they exist
                                updateDeliverySubject(delivery);
                            }
                        }
                        
                        // Cache the result
                        putInCache(CACHE_DELIVERIES_PREFIX + getCurrentUserId() + "_50_start", deliveries);
                        
                        // Emit to subscribers
                        deliveriesSubject.onNext(deliveries);
                    }
                });
        
        // Store listener for cleanup
        activeListeners.put(listenerKey, listener);
    }
    
    /**
     * Updates the subject for a specific delivery
     * 
     * @param delivery The delivery to update
     */
    private void updateDeliverySubject(Delivery delivery) {
        String deliveryId = delivery.getDeliveryId();
        if (deliveryId != null && deliverySubjects.containsKey(deliveryId)) {
            deliverySubjects.get(deliveryId).onNext(delivery);
        }
    }
    
    /**
     * Gets a query for the current user's deliveries
     * 
     * @return Query object for user's deliveries
     */
    private Query getUserDeliveriesQuery() {
        return db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo(FIELD_USER_ID, getCurrentUserId())
                .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING);
    }
    
    /**
     * Gets a document reference for a delivery
     * 
     * @param deliveryId Delivery ID
     * @return DocumentReference for the delivery
     */
    private DocumentReference getDeliveryReference(String deliveryId) {
        return db.collection(COLLECTION_DELIVERIES).document(deliveryId);
    }
    
    @Override
    public Single<List<Delivery>> getDeliveries(int limit, DocumentReference startAfter) {
        String cacheKey = CACHE_DELIVERIES_PREFIX + getCurrentUserId() + "_" + limit + "_" + 
                (startAfter != null ? startAfter.getId() : "start");
        
        return Single.create(emitter -> {
            // First try memory cache if not paginating
            if (startAfter == null) {
                List<Delivery> cached = getFromCache(cacheKey);
                if (cached != null) {
                    emitter.onSuccess(cached);
                    return;
                }
            }
            
            // Build query
            Query query = getUserDeliveriesQuery().limit(limit);
            
            // Add pagination if needed
            if (startAfter != null) {
                query = query.startAfter(startAfter);
            }
            
            // Execute query
            query.get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Delivery> deliveries = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Delivery delivery = doc.toObject(Delivery.class);
                            if (delivery != null) {
                                // Ensure ID is set
                                if (delivery.getDeliveryId() == null) {
                                    delivery.setDeliveryId(doc.getId());
                                }
                                deliveries.add(delivery);
                            }
                        }
                        
                        // Cache the result if not paginating
                        if (startAfter == null) {
                            putInCache(cacheKey, deliveries);
                        }
                        
                        emitter.onSuccess(deliveries);
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "Error getting deliveries");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<List<Delivery>> getDeliveriesByTimeRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return Single.error(new IllegalArgumentException("Start and end dates are required"));
        }
        
        String cacheKey = CACHE_DELIVERIES_PREFIX + "timerange_" + startDate.getTime() + "_" + endDate.getTime();
        
        return Single.create(emitter -> {
            // First try memory cache
            List<Delivery> cached = getFromCache(cacheKey);
            if (cached != null) {
                emitter.onSuccess(cached);
                return;
            }
            
            // Query Firestore
            getUserDeliveriesQuery()
                    .whereGreaterThanOrEqualTo(FIELD_TIMESTAMP, startDate)
                    .whereLessThanOrEqualTo(FIELD_TIMESTAMP, endDate)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Delivery> deliveries = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Delivery delivery = doc.toObject(Delivery.class);
                            if (delivery != null) {
                                // Ensure ID is set
                                if (delivery.getDeliveryId() == null) {
                                    delivery.setDeliveryId(doc.getId());
                                }
                                deliveries.add(delivery);
                            }
                        }
                        
                        // Cache the result
                        putInCache(cacheKey, deliveries);
                        
                        emitter.onSuccess(deliveries);
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "Error getting deliveries by time range");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<List<Delivery>> getDeliveriesByAddress(String addressId) {
        if (addressId == null || addressId.isEmpty()) {
            return Single.error(new IllegalArgumentException("Address ID is required"));
        }
        
        String cacheKey = CACHE_DELIVERIES_PREFIX + "address_" + addressId;
        
        return Single.create(emitter -> {
            // First try memory cache
            List<Delivery> cached = getFromCache(cacheKey);
            if (cached != null) {
                emitter.onSuccess(cached);
                return;
            }
            
            // Query Firestore
            getUserDeliveriesQuery()
                    .whereEqualTo(FIELD_ADDRESS_ID, addressId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Delivery> deliveries = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Delivery delivery = doc.toObject(Delivery.class);
                            if (delivery != null) {
                                // Ensure ID is set
                                if (delivery.getDeliveryId() == null) {
                                    delivery.setDeliveryId(doc.getId());
                                }
                                deliveries.add(delivery);
                            }
                        }
                        
                        // Cache the result
                        putInCache(cacheKey, deliveries);
                        
                        emitter.onSuccess(deliveries);
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "Error getting deliveries by address");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<Delivery> getDeliveryById(String deliveryId) {
        if (deliveryId == null || deliveryId.isEmpty()) {
            return Single.error(new IllegalArgumentException("Delivery ID is required"));
        }
        
        String cacheKey = CACHE_DELIVERY_PREFIX + deliveryId;
        
        return Single.create(emitter -> {
            // First try memory cache
            Delivery cached = getFromCache(cacheKey);
            if (cached != null) {
                emitter.onSuccess(cached);
                return;
            }
            
            // Query Firestore
            getDeliveryReference(deliveryId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Delivery delivery = documentSnapshot.toObject(Delivery.class);
                            if (delivery != null) {
                                // Ensure ID is set
                                if (delivery.getDeliveryId() == null) {
                                    delivery.setDeliveryId(documentSnapshot.getId());
                                }
                                
                                // Verify that this delivery belongs to the current user
                                if (getCurrentUserId().equals(delivery.getUserId())) {
                                    // Cache the result
                                    putInCache(cacheKey, delivery);
                                    
                                    // Update the subject if it exists
                                    updateDeliverySubject(delivery);
                                    
                                    emitter.onSuccess(delivery);
                                } else {
                                    emitter.onError(new SecurityException("Delivery does not belong to current user"));
                                }
                            } else {
                                emitter.onError(new Exception("Failed to parse delivery"));
                            }
                        } else {
                            emitter.onError(new Exception("Delivery not found: " + deliveryId));
                        }
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "Error getting delivery by ID");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<DocumentReference> addDelivery(Delivery delivery) {
        // Validate delivery
        if (delivery == null) {
            return Single.error(new IllegalArgumentException("Delivery cannot be null"));
        }
        
        // Generate document ID if needed
        String deliveryId = delivery.getDeliveryId();
        if (deliveryId == null || deliveryId.isEmpty()) {
            deliveryId = UUID.randomUUID().toString();
            delivery.setDeliveryId(deliveryId);
        }
        
        // Set user ID
        delivery.setUserId(getCurrentUserId());
        
        // Initialize metadata if needed
        if (delivery.getMetadata() == null) {
            Delivery.Metadata metadata = new Delivery.Metadata();
            metadata.setCreatedAt(new Date());
            metadata.setUpdatedAt(new Date());
            metadata.setVersion(1);
            delivery.setMetadata(metadata);
        } else {
            delivery.getMetadata().setUpdatedAt(new Date());
        }
        
        // Initialize status if needed
        if (delivery.getStatus() == null) {
            Delivery.Status status = new Delivery.Status();
            status.setState("created");
            status.setCompleted(false);
            status.setTipped(false);
            status.setVerified(false);
            delivery.setStatus(status);
        }
        
        // Initialize times if needed
        if (delivery.getTimes() == null) {
            Delivery.Times times = new Delivery.Times();
            times.setOrderedAt(new Date());
            delivery.setTimes(times);
        }
        
        DocumentReference docRef = getDeliveryReference(deliveryId);
        final String finalDeliveryId = deliveryId;
        final Delivery finalDelivery = delivery;
        
        return Single.create(emitter -> {
            docRef.set(delivery)
                    .addOnSuccessListener(aVoid -> {
                        // Cache the delivery
                        putInCache(CACHE_DELIVERY_PREFIX + finalDeliveryId, finalDelivery);
                        
                        // Invalidate deliveries cache
                        invalidateCache(CACHE_DELIVERIES_PREFIX + getCurrentUserId() + "_*");
                        
                        // Update address statistics if addressId is provided
                        if (finalDelivery.getReference() != null && finalDelivery.getReference().getAddressId() != null) {
                            updateAddressDeliveryStats(finalDelivery.getReference().getAddressId(), finalDelivery)
                                    .subscribe(() -> {}, throwable -> {
                                        Log.e(TAG, "Error updating address delivery stats", throwable);
                                    });
                        }
                        
                        // Update user profile with delivery count
                        updateDeliveryCountInUserProfile(1)
                                .subscribe(() -> {}, throwable -> {
                                    Log.e(TAG, "Error updating delivery count", throwable);
                                });
                        
                        emitter.onSuccess(docRef);
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // Create sync operation for offline
                            Map<String, Object> data = DeliverySerializer.toMap(finalDelivery);
                            enqueueOperation("create", "delivery", finalDeliveryId, data)
                                    .subscribe(
                                            () -> emitter.onSuccess(docRef),
                                            emitter::onError
                                    );
                        } else {
                            handleFirestoreError(e, "Error adding delivery");
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Completable updateDelivery(Delivery delivery) {
        // Validate delivery
        if (delivery == null || delivery.getDeliveryId() == null || delivery.getDeliveryId().isEmpty()) {
            return Completable.error(new IllegalArgumentException("Delivery ID is required"));
        }
        
        DocumentReference docRef = getDeliveryReference(delivery.getDeliveryId());
        
        // Update metadata
        if (delivery.getMetadata() == null) {
            Delivery.Metadata metadata = new Delivery.Metadata();
            metadata.setUpdatedAt(new Date());
            metadata.setVersion(1);
            delivery.setMetadata(metadata);
        } else {
            delivery.getMetadata().setUpdatedAt(new Date());
        }
        
        return Completable.create(emitter -> {
            // First check if the delivery exists and belongs to the user
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Delivery existingDelivery = documentSnapshot.toObject(Delivery.class);
                            if (existingDelivery != null && getCurrentUserId().equals(existingDelivery.getUserId())) {
                                // Update the delivery
                                docRef.set(delivery)
                                        .addOnSuccessListener(aVoid -> {
                                            // Update cache
                                            putInCache(CACHE_DELIVERY_PREFIX + delivery.getDeliveryId(), delivery);
                                            
                                            // Invalidate deliveries cache
                                            invalidateCache(CACHE_DELIVERIES_PREFIX + getCurrentUserId() + "_*");
                                            
                                            // Check if tip status changed
                                            boolean wasTipped = existingDelivery.getStatus() != null && 
                                                    existingDelivery.getStatus().isTipped();
                                            boolean isTipped = delivery.getStatus() != null && 
                                                    delivery.getStatus().isTipped();
                                            
                                            // If the delivery was marked as tipped, update address stats
                                            if (!wasTipped && isTipped && 
                                                    delivery.getReference() != null && 
                                                    delivery.getReference().getAddressId() != null) {
                                                updateAddressDeliveryStats(
                                                        delivery.getReference().getAddressId(), delivery)
                                                        .subscribe(() -> {}, throwable -> {
                                                            Log.e(TAG, "Error updating address delivery stats", throwable);
                                                        });
                                            }
                                            
                                            // Update the subject if it exists
                                            updateDeliverySubject(delivery);
                                            
                                            emitter.onComplete();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (!isNetworkAvailable()) {
                                                // Create sync operation for offline
                                                Map<String, Object> data = DeliverySerializer.toMap(delivery);
                                                enqueueOperation("update", "delivery", delivery.getDeliveryId(), data)
                                                        .subscribe(
                                                                emitter::onComplete,
                                                                emitter::onError
                                                        );
                                            } else {
                                                handleFirestoreError(e, "Error updating delivery");
                                                emitter.onError(e);
                                            }
                                        });
                            } else {
                                emitter.onError(new SecurityException("Delivery does not belong to current user"));
                            }
                        } else {
                            emitter.onError(new Exception("Delivery not found: " + delivery.getDeliveryId()));
                        }
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "Error checking delivery before update");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Completable updateDeliveryTip(String deliveryId, double tipAmount) {
        if (deliveryId == null || deliveryId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Delivery ID is required"));
        }
        
        if (tipAmount < 0) {
            return Completable.error(new IllegalArgumentException("Tip amount cannot be negative"));
        }
        
        DocumentReference docRef = getDeliveryReference(deliveryId);
        
        return Completable.create(emitter -> {
            // First get the current delivery
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Delivery delivery = documentSnapshot.toObject(Delivery.class);
                            if (delivery != null && getCurrentUserId().equals(delivery.getUserId())) {
                                // Prepare updates
                                Map<String, Object> updates = new HashMap<>();
                                
                                // Update amounts
                                if (delivery.getAmounts() == null) {
                                    Delivery.Amounts amounts = new Delivery.Amounts();
                                    amounts.setTipAmount(tipAmount);
                                    
                                    // Calculate tip percentage if baseAmount is available
                                    if (amounts.getBaseAmount() > 0) {
                                        double percentage = (tipAmount / amounts.getBaseAmount()) * 100;
                                        amounts.setTipPercentage(percentage);
                                    }
                                    
                                    Map<String, Object> deliveryMap = DeliverySerializer.toMap(delivery);
                                    Object amountsObj = deliveryMap.get("amounts");
                                    Map<String, Object> amountsMap = new HashMap<>();
                                    
                                    // Safely convert the object to the correct Map type
                                    if (amountsObj instanceof Map) {
                                        // Cast with type safety
                                        Map<?, ?> originalMap = (Map<?, ?>) amountsObj;
                                        // Copy all entries to ensure proper type conversion
                                        for (Map.Entry<?, ?> entry : originalMap.entrySet()) {
                                            if (entry.getKey() instanceof String) {
                                                amountsMap.put((String) entry.getKey(), entry.getValue());
                                            }
                                        }
                                    }
                                    // Serialize amounts to a Map
                                    updates.put("amounts", amountsMap);
                                } else {
                                    updates.put("amounts.tipAmount", tipAmount);
                                    
                                    // Calculate tip percentage if baseAmount is available
                                    if (delivery.getAmounts().getBaseAmount() > 0) {
                                        double percentage = (tipAmount / delivery.getAmounts().getBaseAmount()) * 100;
                                        updates.put("amounts.tipPercentage", percentage);
                                    }
                                }
                                
                                // Update status
                                boolean wasTipped = delivery.getStatus() != null && delivery.getStatus().isTipped();
                                boolean shouldBeTipped = tipAmount > 0;
                                
                                if (wasTipped != shouldBeTipped) {
                                    updates.put("status.isTipped", shouldBeTipped);
                                }
                                
                                // Update tipped timestamp if newly tipped
                                if (!wasTipped && shouldBeTipped) {
                                    updates.put("times.tippedAt", new Date());
                                }
                                
                                // Update metadata
                                updates.put("metadata.updatedAt", new Date());
                                
                                // Apply updates
                                docRef.update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            // Invalidate caches
                                            invalidateCache(CACHE_DELIVERY_PREFIX + deliveryId);
                                            invalidateCache(CACHE_DELIVERIES_PREFIX + getCurrentUserId() + "_*");
                                            
                                            // If tip was added, update address stats
                                            if (!wasTipped && shouldBeTipped && 
                                                    delivery.getReference() != null && 
                                                    delivery.getReference().getAddressId() != null) {
                                                // Create updated delivery for stats calculation
                                                Delivery updatedDelivery = delivery;
                                                if (updatedDelivery.getAmounts() == null) {
                                                    updatedDelivery.setAmounts(new Delivery.Amounts());
                                                }
                                                updatedDelivery.getAmounts().setTipAmount(tipAmount);
                                                
                                                updateAddressDeliveryStats(
                                                        delivery.getReference().getAddressId(), updatedDelivery)
                                                        .subscribe(() -> {}, throwable -> {
                                                            Log.e(TAG, "Error updating address delivery stats", throwable);
                                                        });
                                            }
                                            
                                            emitter.onComplete();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (!isNetworkAvailable()) {
                                                // Create sync operation for offline
                                                // Create a simplified map with just the tip update info
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("tipAmount", tipAmount);
                                data.put("deliveryId", deliveryId);
                                enqueueOperation("updateTip", "delivery", deliveryId, data)
                                                        .subscribe(
                                                                emitter::onComplete,
                                                                emitter::onError
                                                        );
                                            } else {
                                                handleFirestoreError(e, "Error updating delivery tip");
                                                emitter.onError(e);
                                            }
                                        });
                            } else {
                                emitter.onError(new SecurityException("Delivery does not belong to current user"));
                            }
                        } else {
                            emitter.onError(new Exception("Delivery not found: " + deliveryId));
                        }
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "Error checking delivery before updating tip");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Completable deleteDelivery(String deliveryId) {
        if (deliveryId == null || deliveryId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Delivery ID is required"));
        }
        
        DocumentReference docRef = getDeliveryReference(deliveryId);
        
        return Completable.create(emitter -> {
            // First check if delivery exists and belongs to user
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Delivery delivery = documentSnapshot.toObject(Delivery.class);
                            if (delivery != null && getCurrentUserId().equals(delivery.getUserId())) {
                                // Get address ID for stats update
                                String addressId = delivery.getReference() != null ? 
                                        delivery.getReference().getAddressId() : null;
                                        
                                // Check if this delivery has a tip
                                boolean hasTip = delivery.getStatus() != null && 
                                        delivery.getStatus().isTipped() && 
                                        delivery.getAmounts() != null && 
                                        delivery.getAmounts().getTipAmount() > 0;
                                
                                // Delete the delivery
                                docRef.delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Remove from cache
                                            invalidateCache(CACHE_DELIVERY_PREFIX + deliveryId);
                                            invalidateCache(CACHE_DELIVERIES_PREFIX + getCurrentUserId() + "_*");
                                            
                                            // Update user profile with delivery count
                                            updateDeliveryCountInUserProfile(-1)
                                                    .subscribe(() -> {}, throwable -> {
                                                        Log.e(TAG, "Error updating delivery count", throwable);
                                                    });
                                                    
                                            // Update address stats if needed
                                            if (addressId != null) {
                                                decrementAddressDeliveryStats(addressId, hasTip, 
                                                        hasTip ? delivery.getAmounts().getTipAmount() : 0)
                                                        .subscribe(() -> {}, throwable -> {
                                                            Log.e(TAG, "Error updating address stats", throwable);
                                                        });
                                            }
                                            
                                            emitter.onComplete();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (!isNetworkAvailable()) {
                                                // Create sync operation for offline
                                                enqueueOperation("delete", "delivery", deliveryId, null)
                                                        .subscribe(
                                                                emitter::onComplete,
                                                                emitter::onError
                                                        );
                                            } else {
                                                handleFirestoreError(e, "Error deleting delivery");
                                                emitter.onError(e);
                                            }
                                        });
                            } else {
                                emitter.onError(new SecurityException("Delivery does not belong to current user"));
                            }
                        } else {
                            // Delivery already deleted
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "Error checking delivery before delete");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Observable<List<Delivery>> observeDeliveries() {
        // If not yet subscribed, set up real-time listener
        if (!deliveriesSubject.hasValue()) {
            getRecentDeliveries(50).subscribe(
                    deliveries -> {}, // deliveries will be emitted by the listener
                    throwable -> Log.e(TAG, "Error loading initial deliveries", throwable)
            );
        }
        
        return deliveriesSubject;
    }
    
    @Override
    public Observable<Delivery> observeDelivery(String deliveryId) {
        if (deliveryId == null || deliveryId.isEmpty()) {
            return Observable.error(new IllegalArgumentException("Delivery ID is required"));
        }
        
        // Create or get subject for this delivery
        BehaviorSubject<Delivery> subject = deliverySubjects.computeIfAbsent(
                deliveryId, k -> BehaviorSubject.create());
        
        // Set up listener if not already
        String listenerKey = "delivery_" + deliveryId + "_listener";
        
        if (!activeListeners.containsKey(listenerKey)) {
            // Set up listener
            ListenerRegistration listener = getDeliveryReference(deliveryId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Error listening to delivery", e);
                            return;
                        }
                        
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            Delivery delivery = documentSnapshot.toObject(Delivery.class);
                            if (delivery != null) {
                                // Ensure ID is set
                                if (delivery.getDeliveryId() == null) {
                                    delivery.setDeliveryId(documentSnapshot.getId());
                                }
                                
                                // Verify that this delivery belongs to the current user
                                if (getCurrentUserId().equals(delivery.getUserId())) {
                                    // Update cache
                                    putInCache(CACHE_DELIVERY_PREFIX + deliveryId, delivery);
                                    
                                    // Emit to subscribers
                                    subject.onNext(delivery);
                                }
                            }
                        }
                    });
            
            // Store listener for cleanup
            activeListeners.put(listenerKey, listener);
            
            // Initially load from cache or Firestore
            Delivery cached = getFromCache(CACHE_DELIVERY_PREFIX + deliveryId);
            if (cached != null) {
                subject.onNext(cached);
            } else {
                getDeliveryById(deliveryId)
                        .subscribe(
                                subject::onNext,
                                throwable -> Log.e(TAG, "Error loading initial delivery", throwable)
                        );
            }
        }
        
        return subject;
    }
    
    @Override
    public Single<Map<String, DeliveryStats>> getDeliveryStats() {
        String cacheKey = CACHE_STATS_PREFIX + getCurrentUserId();
        
        return Single.create(emitter -> {
            // First try memory cache
            Map<String, DeliveryStats> cached = getFromCache(cacheKey);
            if (cached != null) {
                emitter.onSuccess(cached);
                return;
            }
            
            // Calculate time periods
            Calendar cal = Calendar.getInstance();
            Date now = cal.getTime();
            
            // Today - start of day to now
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfToday = cal.getTime();
            
            // Last 7 days - 7 days ago to now
            cal.add(Calendar.DAY_OF_YEAR, -7);
            Date sevenDaysAgo = cal.getTime();
            
            // Last 30 days - 30 days ago to now
            cal.setTime(now); // Reset to now
            cal.add(Calendar.DAY_OF_YEAR, -30);
            Date thirtyDaysAgo = cal.getTime();
            
            // Query Firestore for all deliveries in the last 30 days
            getUserDeliveriesQuery()
                    .whereGreaterThanOrEqualTo(FIELD_TIMESTAMP, thirtyDaysAgo)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        // Initialize stats objects
                        DeliveryStats todayStats = new DeliveryStats();
                        DeliveryStats sevenDayStats = new DeliveryStats();
                        DeliveryStats thirtyDayStats = new DeliveryStats();
                        
                        // Process deliveries
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Delivery delivery = doc.toObject(Delivery.class);
                            if (delivery != null && 
                                    delivery.getTimes() != null && 
                                    delivery.getTimes().getCompletedAt() != null) {
                                
                                Date completedAt = delivery.getTimes().getCompletedAt();
                                boolean isToday = !completedAt.before(startOfToday);
                                boolean isWithinSevenDays = !completedAt.before(sevenDaysAgo);
                                // All deliveries are within 30 days due to the query
                                
                                // Update appropriate stats
                                if (isToday) {
                                    updateDeliveryStat(todayStats, delivery);
                                }
                                
                                if (isWithinSevenDays) {
                                    updateDeliveryStat(sevenDayStats, delivery);
                                }
                                
                                updateDeliveryStat(thirtyDayStats, delivery);
                            }
                        }
                        
                        // Create result map
                        Map<String, DeliveryStats> statsMap = new HashMap<>();
                        statsMap.put("today", todayStats);
                        statsMap.put("sevenDays", sevenDayStats);
                        statsMap.put("thirtyDays", thirtyDayStats);
                        
                        // Cache the result
                        putInCache(cacheKey, statsMap);
                        
                        emitter.onSuccess(statsMap);
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "Error calculating delivery stats");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<List<Delivery>> getAllDeliveries() {
        return getDeliveries(1000, null);
    }
    
    @Override
    public Single<List<Delivery>> getRecentDeliveries(int count) {
        return getDeliveries(count, null);
    }
    
    @Override
    public Single<List<Delivery>> getTodaysDeliveries() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfToday = cal.getTime();
        
        return getDeliveriesByTimeRange(startOfToday, now);
    }
    
    @Override
    public Single<List<Delivery>> getLastWeekDeliveries() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        
        cal.add(Calendar.DAY_OF_YEAR, -7);
        Date sevenDaysAgo = cal.getTime();
        
        return getDeliveriesByTimeRange(sevenDaysAgo, now);
    }
    
    @Override
    public Single<List<Delivery>> getLastMonthDeliveries() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        
        cal.add(Calendar.DAY_OF_YEAR, -30);
        Date thirtyDaysAgo = cal.getTime();
        
        return getDeliveriesByTimeRange(thirtyDaysAgo, now);
    }
    
    @Override
    public Single<List<Delivery>> getTippedDeliveries() {
        return Single.create(emitter -> {
            getUserDeliveriesQuery()
                    .whereEqualTo("status.isTipped", true)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Delivery> deliveries = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Delivery delivery = doc.toObject(Delivery.class);
                            if (delivery != null) {
                                // Ensure ID is set
                                if (delivery.getDeliveryId() == null) {
                                    delivery.setDeliveryId(doc.getId());
                                }
                                deliveries.add(delivery);
                            }
                        }
                        
                        emitter.onSuccess(deliveries);
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "Error getting tipped deliveries");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<List<Delivery>> getUntippedDeliveries() {
        return Single.create(emitter -> {
            getUserDeliveriesQuery()
                    .whereEqualTo("status.isCompleted", true)
                    .whereEqualTo("status.isTipped", false)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Delivery> deliveries = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Delivery delivery = doc.toObject(Delivery.class);
                            if (delivery != null) {
                                // Ensure ID is set
                                if (delivery.getDeliveryId() == null) {
                                    delivery.setDeliveryId(doc.getId());
                                }
                                deliveries.add(delivery);
                            }
                        }
                        
                        emitter.onSuccess(deliveries);
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(e, "Error getting untipped deliveries");
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Completable markDeliveryCompleted(String deliveryId, Date completionTime) {
        if (deliveryId == null || deliveryId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Delivery ID is required"));
        }
        
        Date timestamp = completionTime != null ? completionTime : new Date();
        
        return getDeliveryById(deliveryId)
                .flatMapCompletable(delivery -> {
                    // Update status
                    if (delivery.getStatus() == null) {
                        delivery.setStatus(new Delivery.Status());
                    }
                    delivery.getStatus().setCompleted(true);
                    delivery.getStatus().setState("completed");
                    
                    // Update timestamp
                    if (delivery.getTimes() == null) {
                        delivery.setTimes(new Delivery.Times());
                    }
                    delivery.getTimes().setCompletedAt(timestamp);
                    
                    // Update the delivery
                    return updateDelivery(delivery);
                });
    }
    
    @Override
    public Single<DeliveryStats> getTodayStats() {
        return getDeliveryStats()
                .map(statsMap -> statsMap.get("today"));
    }
    
    @Override
    public Single<DeliveryStats> getLastWeekStats() {
        return getDeliveryStats()
                .map(statsMap -> statsMap.get("sevenDays"));
    }
    
    @Override
    public Single<DeliveryStats> getLastMonthStats() {
        return getDeliveryStats()
                .map(statsMap -> statsMap.get("thirtyDays"));
    }
    
    @Override
    public DeliveryStats calculateStats(List<Delivery> deliveries) {
        DeliveryStats stats = new DeliveryStats();
        
        if (deliveries == null || deliveries.isEmpty()) {
            return stats;
        }
        
        // Process deliveries
        for (Delivery delivery : deliveries) {
            updateDeliveryStat(stats, delivery);
        }
        
        return stats;
    }
    
    /**
     * Helper method to update delivery statistics with a delivery
     * 
     * @param stats Stats to update
     * @param delivery Delivery to include
     */
    private void updateDeliveryStat(DeliveryStats stats, Delivery delivery) {
        // Increment delivery count
        stats.setCount(stats.getCount() + 1);
        
        // Update tip stats if applicable
        if (delivery.getStatus() != null && 
                delivery.getStatus().isTipped() && 
                delivery.getAmounts() != null && 
                delivery.getAmounts().getTipAmount() > 0) {
            
            double tipAmount = delivery.getAmounts().getTipAmount();
            stats.setTipCount(stats.getTipCount() + 1);
            stats.setTotalTips(stats.getTotalTips() + tipAmount);
            
            // Update highest tip if applicable
            if (tipAmount > stats.getHighestTip()) {
                stats.setHighestTip(tipAmount);
            }
        }
        
        // Update pending count
        if (delivery.getStatus() != null && 
                !delivery.getStatus().isCompleted()) {
            stats.setPendingCount(stats.getPendingCount() + 1);
        }
        
        // Update average time if applicable
        if (delivery.getTimes() != null && 
                delivery.getTimes().getAcceptedAt() != null && 
                delivery.getTimes().getCompletedAt() != null) {
            
            int durationMinutes = delivery.getActualDurationMinutes();
            if (durationMinutes > 0) {
                // Calculate new average (weighted)
                int currentCount = stats.getCount() - 1; // Exclude this delivery from count
                double currentTotal = stats.getAverageTimeMinutes() * currentCount;
                double newTotal = currentTotal + durationMinutes;
                double newAverage = currentCount > 0 ? newTotal / stats.getCount() : durationMinutes;
                
                stats.setAverageTimeMinutes(newAverage);
            }
        }
        
        // Recalculate average tip
        if (stats.getTipCount() > 0) {
            stats.setAverageTip(stats.getTotalTips() / stats.getTipCount());
        }
    }
    
    /**
     * Helper method to update address delivery statistics
     * 
     * @param addressId Address ID
     * @param delivery New delivery to include in stats
     * @return Completable that completes when update is finished
     */
    private Completable updateAddressDeliveryStats(String addressId, Delivery delivery) {
        return getAddressById(addressId)
                .flatMapCompletable(address -> {
                    if (address.getDeliveryStats() == null) {
                        address.setDeliveryStats(new Address.DeliveryStats());
                    }
                    
                    Address.DeliveryStats stats = address.getDeliveryStats();
                    stats.setDeliveryCount(stats.getDeliveryCount() + 1);
                    
                    // Update tip stats if this delivery has a tip
                    if (delivery.getAmounts() != null && delivery.getAmounts().getTipAmount() > 0) {
                        double tipAmount = delivery.getAmounts().getTipAmount();
                        stats.setTipCount(stats.getTipCount() + 1);
                        stats.setTotalTips(stats.getTotalTips() + tipAmount);
                        
                        // Recalculate average
                        if (stats.getTipCount() > 0) {
                            stats.setAverageTip(stats.getTotalTips() / stats.getTipCount());
                        }
                        
                        // Update highest tip if applicable
                        if (tipAmount > stats.getHighestTip()) {
                            stats.setHighestTip(tipAmount);
                        }
                    }
                    
                    // Update last delivery date
                    if (delivery.getTimes() != null && delivery.getTimes().getCompletedAt() != null) {
                        stats.setLastDeliveryDate(delivery.getTimes().getCompletedAt());
                    } else {
                        stats.setLastDeliveryDate(new Date());
                    }
                    
                    // Update the address
                    return updateAddress(address);
                });
    }
    
    /**
     * Helper method to decrement address delivery statistics after a delivery is deleted
     * 
     * @param addressId Address ID
     * @param hasTip Whether the deleted delivery had a tip
     * @param tipAmount The tip amount of the deleted delivery
     * @return Completable that completes when update is finished
     */
    private Completable decrementAddressDeliveryStats(String addressId, boolean hasTip, double tipAmount) {
        return getAddressById(addressId)
                .flatMapCompletable(address -> {
                    if (address.getDeliveryStats() == null) {
                        // Nothing to update
                        return Completable.complete();
                    }
                    
                    Address.DeliveryStats stats = address.getDeliveryStats();
                    
                    // Decrement delivery count
                    stats.setDeliveryCount(Math.max(0, stats.getDeliveryCount() - 1));
                    
                    // Update tip stats if needed
                    if (hasTip && stats.getTipCount() > 0) {
                        stats.setTipCount(Math.max(0, stats.getTipCount() - 1));
                        stats.setTotalTips(Math.max(0, stats.getTotalTips() - tipAmount));
                        
                        // Recalculate average
                        if (stats.getTipCount() > 0) {
                            stats.setAverageTip(stats.getTotalTips() / stats.getTipCount());
                        } else {
                            stats.setAverageTip(0);
                        }
                        
                        // Note: We don't update highestTip as that would require recalculating from all deliveries
                    }
                    
                    // Update the address
                    return updateAddress(address);
                });
    }
    
    /**
     * Helper method to update delivery count in user profile
     * 
     * @param delta Change in delivery count (1 for add, -1 for delete)
     * @return Completable that completes when update is finished
     */
    private Completable updateDeliveryCountInUserProfile(int delta) {
        return getUserProfile()
                .flatMapCompletable(profile -> {
                    if (profile.getUsage() == null) {
                        profile.setUsage(new UserProfile.Usage());
                    }
                    
                    int currentCount = profile.getUsage().getDeliveryCount();
                    profile.getUsage().setDeliveryCount(Math.max(0, currentCount + delta));
                    profile.getUsage().setLastUsageUpdate(new Date());
                    
                    return updateUserProfile(profile);
                });
    }
    
    /**
     * Clean up resources when repository is no longer needed
     */
    public void cleanup() {
        // Remove all listeners
        for (Map.Entry<String, ListenerRegistration> entry : activeListeners.entrySet()) {
            if (entry.getKey().startsWith("delivery_") || entry.getKey().startsWith("deliveries_")) {
                entry.getValue().remove();
            }
        }
        
        // Clear subjects
        deliverySubjects.clear();
    }
}