//-----------------------------------------------------------------------------------
// Implementation of Delivery operations
//-----------------------------------------------------------------------------------

@Override
public Single<List<Delivery>> getDeliveries(int limit, DocumentReference startAfter) {
    String cacheKey = "deliveries_" + userId + "_" + limit + "_" + 
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
        Query query = db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .orderBy("times.completedAt", Query.Direction.DESCENDING)
                .limit(limit);
        
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
                    handleFirestoreError(e, "getting deliveries");
                    emitter.onError(e);
                });
    });
}

@Override
public Single<List<Delivery>> getDeliveriesByTimeRange(Date startDate, Date endDate) {
    if (startDate == null || endDate == null) {
        return Single.error(new IllegalArgumentException("Start and end dates are required"));
    }
    
    String cacheKey = "deliveries_timerange_" + startDate.getTime() + "_" + endDate.getTime();
    
    return Single.create(emitter -> {
        // First try memory cache
        List<Delivery> cached = getFromCache(cacheKey);
        if (cached != null) {
            emitter.onSuccess(cached);
            return;
        }
        
        // Query Firestore
        db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("times.completedAt", startDate)
                .whereLessThanOrEqualTo("times.completedAt", endDate)
                .orderBy("times.completedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Delivery> deliveries = new ArrayList<>();
                    
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Delivery delivery = doc.toObject(Delivery.class);
                        if (delivery != null) {
                            deliveries.add(delivery);
                        }
                    }
                    
                    // Cache the result
                    putInCache(cacheKey, deliveries);
                    
                    emitter.onSuccess(deliveries);
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(e, "getting deliveries by time range");
                    emitter.onError(e);
                });
    });
}

@Override
public Single<List<Delivery>> getDeliveriesByAddress(String addressId) {
    if (addressId == null || addressId.isEmpty()) {
        return Single.error(new IllegalArgumentException("Address ID is required"));
    }
    
    String cacheKey = "deliveries_address_" + addressId;
    
    return Single.create(emitter -> {
        // First try memory cache
        List<Delivery> cached = getFromCache(cacheKey);
        if (cached != null) {
            emitter.onSuccess(cached);
            return;
        }
        
        // Query Firestore
        db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("reference.addressId", addressId)
                .orderBy("times.completedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Delivery> deliveries = new ArrayList<>();
                    
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Delivery delivery = doc.toObject(Delivery.class);
                        if (delivery != null) {
                            deliveries.add(delivery);
                        }
                    }
                    
                    // Cache the result
                    putInCache(cacheKey, deliveries);
                    
                    emitter.onSuccess(deliveries);
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(e, "getting deliveries by address");
                    emitter.onError(e);
                });
    });
}

@Override
public Single<Delivery> getDeliveryById(String deliveryId) {
    if (deliveryId == null || deliveryId.isEmpty()) {
        return Single.error(new IllegalArgumentException("Delivery ID is required"));
    }
    
    String cacheKey = "delivery_" + deliveryId;
    
    return Single.create(emitter -> {
        // First try memory cache
        Delivery cached = getFromCache(cacheKey);
        if (cached != null) {
            emitter.onSuccess(cached);
            return;
        }
        
        // Query Firestore
        DocumentReference docRef = db.collection(COLLECTION_DELIVERIES).document(deliveryId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Delivery delivery = documentSnapshot.toObject(Delivery.class);
                        if (delivery != null) {
                            // Verify that this delivery belongs to the current user
                            if (userId.equals(delivery.getUserId())) {
                                // Cache the result
                                putInCache(cacheKey, delivery);
                                
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
                    handleFirestoreError(e, "getting delivery by ID");
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
    delivery.setUserId(userId);
    
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
    
    DocumentReference docRef = db.collection(COLLECTION_DELIVERIES).document(deliveryId);
    final String finalDeliveryId = deliveryId;
    
    return Single.create(emitter -> {
        docRef.set(delivery)
                .addOnSuccessListener(aVoid -> {
                    // Cache the delivery
                    putInCache("delivery_" + finalDeliveryId, delivery);
                    
                    // Invalidate deliveries cache
                    invalidateCache("deliveries_" + userId + "_*");
                    
                    // Update address statistics if addressId is provided
                    if (delivery.getReference() != null && delivery.getReference().getAddressId() != null) {
                        updateAddressDeliveryStats(delivery.getReference().getAddressId(), delivery)
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
                        Map<String, Object> data = new HashMap<>(); // Convert delivery to map
                        enqueueOperation("create", "delivery", finalDeliveryId, data)
                                .subscribe(
                                        () -> emitter.onSuccess(docRef),
                                        emitter::onError
                                );
                    } else {
                        handleFirestoreError(e, "adding delivery");
                        emitter.onError(e);
                    }
                });
    });
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

@Override
public Completable updateDelivery(Delivery delivery) {
    // Validate delivery
    if (delivery == null || delivery.getDeliveryId() == null || delivery.getDeliveryId().isEmpty()) {
        return Completable.error(new IllegalArgumentException("Delivery ID is required"));
    }
    
    DocumentReference docRef = db.collection(COLLECTION_DELIVERIES).document(delivery.getDeliveryId());
    
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
                        if (existingDelivery != null && userId.equals(existingDelivery.getUserId())) {
                            // Update the delivery
                            docRef.set(delivery)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update cache
                                        putInCache("delivery_" + delivery.getDeliveryId(), delivery);
                                        
                                        // Invalidate deliveries cache
                                        invalidateCache("deliveries_" + userId + "_*");
                                        
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
                                        
                                        emitter.onComplete();
                                    })
                                    .addOnFailureListener(e -> {
                                        if (!isNetworkAvailable()) {
                                            // Create sync operation for offline
                                            Map<String, Object> data = new HashMap<>(); // Convert delivery to map
                                            enqueueOperation("update", "delivery", delivery.getDeliveryId(), data)
                                                    .subscribe(
                                                            emitter::onComplete,
                                                            emitter::onError
                                                    );
                                        } else {
                                            handleFirestoreError(e, "updating delivery");
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
                    handleFirestoreError(e, "checking delivery before update");
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
    
    DocumentReference docRef = db.collection(COLLECTION_DELIVERIES).document(deliveryId);
    
    return Completable.create(emitter -> {
        // First get the current delivery
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Delivery delivery = documentSnapshot.toObject(Delivery.class);
                        if (delivery != null && userId.equals(delivery.getUserId())) {
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
                                
                                Map<String, Object> amountsMap = new HashMap<>(); // Convert amounts to map
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
                                        invalidateCache("delivery_" + deliveryId);
                                        invalidateCache("deliveries_" + userId + "_*");
                                        
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
                                            Map<String, Object> data = new HashMap<>();
                                            data.put("tipAmount", tipAmount);
                                            enqueueOperation("updateTip", "delivery", deliveryId, data)
                                                    .subscribe(
                                                            emitter::onComplete,
                                                            emitter::onError
                                                    );
                                        } else {
                                            handleFirestoreError(e, "updating delivery tip");
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
                    handleFirestoreError(e, "checking delivery before updating tip");
                    emitter.onError(e);
                });
    });
}

@Override
public Completable deleteDelivery(String deliveryId) {
    if (deliveryId == null || deliveryId.isEmpty()) {
        return Completable.error(new IllegalArgumentException("Delivery ID is required"));
    }
    
    DocumentReference docRef = db.collection(COLLECTION_DELIVERIES).document(deliveryId);
    
    return Completable.create(emitter -> {
        // First check if delivery exists and belongs to user
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Delivery delivery = documentSnapshot.toObject(Delivery.class);
                        if (delivery != null && userId.equals(delivery.getUserId())) {
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
                                        invalidateCache("delivery_" + deliveryId);
                                        invalidateCache("deliveries_" + userId + "_*");
                                        
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
                                            handleFirestoreError(e, "deleting delivery");
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
                    handleFirestoreError(e, "checking delivery before delete");
                    emitter.onError(e);
                });
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

@Override
public Observable<List<Delivery>> observeDeliveries() {
    return Observable.create(emitter -> {
        // Set up listener
        String listenerKey = "deliveries_" + userId + "_listener";
        ListenerRegistration listener = activeListeners.get(listenerKey);
        
        if (listener != null) {
            // Remove old listener
            listener.remove();
        }
        
        // Create new listener
        listener = db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .orderBy("times.completedAt", Query.Direction.DESCENDING)
                .limit(50) // Limit the number of deliveries to avoid performance issues
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        if (!emitter.isDisposed()) {
                            emitter.onError(e);
                        }
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        List<Delivery> deliveries = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Delivery delivery = doc.toObject(Delivery.class);
                            if (delivery != null) {
                                deliveries.add(delivery);
                            }
                        }
                        
                        // Cache the result
                        putInCache("deliveries_" + userId + "_50_start", deliveries);
                        
                        // Emit to subscribers
                        if (!emitter.isDisposed()) {
                            emitter.onNext(deliveries);
                        }
                    }
                });
        
        // Store listener for cleanup
        activeListeners.put(listenerKey, listener);
        
        // Clean up when disposed
        emitter.setCancellable(() -> {
            if (listener != null) {
                listener.remove();
                activeListeners.remove(listenerKey);
            }
        });
        
        // Initially load from cache or Firestore
        List<Delivery> cached = getFromCache("deliveries_" + userId + "_50_start");
        if (cached != null) {
            emitter.onNext(cached);
        } else {
            getDeliveries(50, null)
                    .subscribe(
                            emitter::onNext,
                            throwable -> Log.e(TAG, "Error loading initial deliveries", throwable)
                    );
        }
    });
}

@Override
public Observable<Delivery> observeDelivery(String deliveryId) {
    if (deliveryId == null || deliveryId.isEmpty()) {
        return Observable.error(new IllegalArgumentException("Delivery ID is required"));
    }
    
    return Observable.create(emitter -> {
        // Set up listener
        String listenerKey = "delivery_" + deliveryId + "_listener";
        ListenerRegistration listener = activeListeners.get(listenerKey);
        
        if (listener != null) {
            // Remove old listener
            listener.remove();
        }
        
        // Create new listener
        DocumentReference docRef = db.collection(COLLECTION_DELIVERIES).document(deliveryId);
        listener = docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                if (!emitter.isDisposed()) {
                    emitter.onError(e);
                }
                return;
            }
            
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Delivery delivery = documentSnapshot.toObject(Delivery.class);
                if (delivery != null) {
                    // Verify that this delivery belongs to the current user
                    if (userId.equals(delivery.getUserId())) {
                        // Update cache
                        putInCache("delivery_" + deliveryId, delivery);
                        
                        // Emit to subscribers
                        if (!emitter.isDisposed()) {
                            emitter.onNext(delivery);
                        }
                    }
                }
            }
        });
        
        // Store listener for cleanup
        activeListeners.put(listenerKey, listener);
        
        // Clean up when disposed
        emitter.setCancellable(() -> {
            if (listener != null) {
                listener.remove();
                activeListeners.remove(listenerKey);
            }
        });
        
        // Initially load from cache or Firestore
        Delivery cached = getFromCache("delivery_" + deliveryId);
        if (cached != null) {
            emitter.onNext(cached);
        } else {
            getDeliveryById(deliveryId)
                    .subscribe(
                            emitter::onNext,
                            throwable -> Log.e(TAG, "Error loading initial delivery", throwable)
                    );
        }
    });
}

@Override
public Single<Map<String, DeliveryStats>> getDeliveryStats() {
    String cacheKey = "delivery_stats_" + userId;
    
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
        db.collection(COLLECTION_DELIVERIES)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("times.completedAt", thirtyDaysAgo)
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
                    handleFirestoreError(e, "calculating delivery stats");
                    emitter.onError(e);
                });
    });
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
