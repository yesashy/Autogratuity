# Autogratuity Phase 2 Implementation Plan

## Overview

This document outlines the detailed implementation plan for Phase 2 of the Autogratuity architectural overhaul. Phase 2 involves implementing domain-specific repositories according to the domain-based repository pattern established in Phase 1.

The implementation follows a systematic approach for each domain repository:
1. Create the domain-specific repository interface extending DataRepository
2. Implement the domain repository using functionality from the fragmented files
3. Extract and consolidate relevant methods from the original files
4. Update the RepositoryProvider to provide access to the new domain repository

## Implementation Schedule

| Repository | Time Estimate | Priority |
|------------|---------------|----------|
| Preference Repository | Week 1 | 1 - High |
| Delivery Repository | Week 1-2 | 2 - High |
| Subscription Repository | Week 2 | 3 - Medium |
| Sync Repository | Week 2-3 | 4 - Medium |
| Address Repository | Week 3 | 5 - Medium |

## 1. Preference Repository Implementation

### 1.1 File Structure

```
repository/
└── preference/
    ├── PreferenceRepository.java (Interface)
    └── PreferenceRepositoryImpl.java (Implementation)
```

### 1.2 Interface Definition

Create `PreferenceRepository.java` with the following structure:

```java
package com.autogratuity.data.repository.preference;

import com.autogratuity.data.repository.core.DataRepository;
import com.autogratuity.models.UserProfile;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Map;

/**
 * Repository interface for managing user preferences and profile data.
 * Extends the core DataRepository for preference-specific operations.
 */
public interface PreferenceRepository extends DataRepository {
    
    /**
     * Retrieves the current user profile.
     *
     * @return Single emitting the user profile or error
     */
    Single<UserProfile> getUserProfile();
    
    /**
     * Updates the entire user profile.
     *
     * @param userProfile The updated user profile
     * @return Completable indicating success or error
     */
    Completable updateUserProfile(UserProfile userProfile);
    
    /**
     * Updates specific fields in the user profile.
     *
     * @param fieldUpdates Map of field names to new values
     * @return Completable indicating success or error
     */
    Completable updateUserProfileFields(Map<String, Object> fieldUpdates);
    
    /**
     * Observes changes to the user profile in real-time.
     *
     * @return Observable emitting updated user profile when changes occur
     */
    Observable<UserProfile> observeUserProfile();
    
    /**
     * Sets default tip percentage preference.
     *
     * @param percentage The default tip percentage
     * @return Completable indicating success or error
     */
    Completable setDefaultTipPercentage(double percentage);
    
    /**
     * Gets the default tip percentage preference.
     *
     * @return Single emitting the default tip percentage
     */
    Single<Double> getDefaultTipPercentage();
    
    /**
     * Sets the preferred currency format.
     *
     * @param currencyCode ISO currency code (e.g., "USD")
     * @return Completable indicating success or error
     */
    Completable setPreferredCurrency(String currencyCode);
    
    /**
     * Gets the preferred currency format.
     *
     * @return Single emitting the preferred currency code
     */
    Single<String> getPreferredCurrency();
    
    /**
     * Sets the user's display name.
     *
     * @param displayName User's display name
     * @return Completable indicating success or error
     */
    Completable setDisplayName(String displayName);
    
    /**
     * Sets the user's preferred theme.
     *
     * @param themeMode Theme mode (e.g., "light", "dark", "system")
     * @return Completable indicating success or error
     */
    Completable setThemePreference(String themeMode);
    
    /**
     * Gets the user's preferred theme.
     *
     * @return Single emitting the theme preference
     */
    Single<String> getThemePreference();
}
```

### 1.3 Implementation Details

Create `PreferenceRepositoryImpl.java` with the following structure:

```java
package com.autogratuity.data.repository.preference;

import com.autogratuity.data.repository.core.FirestoreRepository;
import com.autogratuity.models.UserProfile;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of PreferenceRepository interface.
 * Extends FirestoreRepository for Firestore-based preference storage.
 */
public class PreferenceRepositoryImpl extends FirestoreRepository implements PreferenceRepository {
    
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_PREFERENCES = "preferences";
    private static final String FIELD_DISPLAY_NAME = "displayName";
    private static final String FIELD_DEFAULT_TIP = "defaultTipPercentage";
    private static final String FIELD_CURRENCY = "preferredCurrency";
    private static final String FIELD_THEME = "themePreference";
    
    private BehaviorSubject<UserProfile> userProfileSubject = BehaviorSubject.create();
    
    public PreferenceRepositoryImpl() {
        super();
        initializeUserProfileListener();
    }
    
    private void initializeUserProfileListener() {
        if (getCurrentUser() == null) return;
        
        getUserDocumentReference()
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    logError("Error listening to user profile", error);
                    return;
                }
                
                if (snapshot != null && snapshot.exists()) {
                    UserProfile profile = snapshot.toObject(UserProfile.class);
                    if (profile != null) {
                        userProfileSubject.onNext(profile);
                    }
                }
            });
    }
    
    private DocumentReference getUserDocumentReference() {
        return db.collection(COLLECTION_USERS)
            .document(getCurrentUserId());
    }
    
    private DocumentReference getUserPreferencesDocumentReference() {
        return getUserDocumentReference()
            .collection(COLLECTION_PREFERENCES)
            .document("userPreferences");
    }
    
    @Override
    public Single<UserProfile> getUserProfile() {
        if (!isNetworkAvailable() && userProfileSubject.hasValue()) {
            return Single.just(userProfileSubject.getValue());
        }
        
        return Single.create(emitter -> {
            getUserDocumentReference().get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        UserProfile profile = snapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            userProfileSubject.onNext(profile);
                            emitter.onSuccess(profile);
                        } else {
                            emitter.onError(new Exception("Failed to parse user profile"));
                        }
                    } else {
                        // Create a default profile if none exists
                        UserProfile defaultProfile = createDefaultUserProfile();
                        emitter.onSuccess(defaultProfile);
                    }
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    private UserProfile createDefaultUserProfile() {
        UserProfile defaultProfile = new UserProfile();
        defaultProfile.setUserId(getCurrentUserId());
        defaultProfile.setDefaultTipPercentage(15.0);
        defaultProfile.setPreferredCurrency("USD");
        defaultProfile.setThemePreference("system");
        
        // Save the default profile
        updateUserProfile(defaultProfile).subscribe();
        
        return defaultProfile;
    }
    
    @Override
    public Completable updateUserProfile(UserProfile userProfile) {
        return Completable.create(emitter -> {
            if (userProfile == null) {
                emitter.onError(new IllegalArgumentException("User profile cannot be null"));
                return;
            }
            
            // Ensure the user ID is set correctly
            userProfile.setUserId(getCurrentUserId());
            
            getUserDocumentReference()
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    userProfileSubject.onNext(userProfile);
                    emitter.onComplete();
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Completable updateUserProfileFields(Map<String, Object> fieldUpdates) {
        return Completable.create(emitter -> {
            if (fieldUpdates == null || fieldUpdates.isEmpty()) {
                emitter.onError(new IllegalArgumentException("Field updates cannot be null or empty"));
                return;
            }
            
            getUserDocumentReference()
                .set(fieldUpdates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // After updating, get the updated profile to emit to observers
                    getUserProfile().subscribe(
                        updatedProfile -> userProfileSubject.onNext(updatedProfile),
                        error -> logError("Error fetching updated profile", error)
                    );
                    emitter.onComplete();
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Observable<UserProfile> observeUserProfile() {
        // If no value is present, trigger a fetch
        if (!userProfileSubject.hasValue()) {
            getUserProfile().subscribe(
                profile -> {}, // Already handled by subject
                error -> logError("Error fetching user profile for observation", error)
            );
        }
        
        return userProfileSubject;
    }
    
    @Override
    public Completable setDefaultTipPercentage(double percentage) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_DEFAULT_TIP, percentage);
        return updateUserProfileFields(updates);
    }
    
    @Override
    public Single<Double> getDefaultTipPercentage() {
        return getUserProfile().map(UserProfile::getDefaultTipPercentage);
    }
    
    @Override
    public Completable setPreferredCurrency(String currencyCode) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_CURRENCY, currencyCode);
        return updateUserProfileFields(updates);
    }
    
    @Override
    public Single<String> getPreferredCurrency() {
        return getUserProfile().map(UserProfile::getPreferredCurrency);
    }
    
    @Override
    public Completable setDisplayName(String displayName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_DISPLAY_NAME, displayName);
        return updateUserProfileFields(updates);
    }
    
    @Override
    public Completable setThemePreference(String themeMode) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_THEME, themeMode);
        return updateUserProfileFields(updates);
    }
    
    @Override
    public Single<String> getThemePreference() {
        return getUserProfile().map(UserProfile::getThemePreference);
    }
}
```

### 1.4 RepositoryProvider Update

Update `RepositoryProvider.java` with the following:

```java
// Add this field
private static PreferenceRepository preferenceRepository;

// Add this method
public static PreferenceRepository getPreferenceRepository() {
    if (preferenceRepository == null) {
        preferenceRepository = new PreferenceRepositoryImpl();
    }
    return preferenceRepository;
}
```

## 2. Delivery Repository Implementation

### 2.1 File Structure

```
repository/
└── delivery/
    ├── DeliveryRepository.java (Interface)
    └── DeliveryRepositoryImpl.java (Implementation)
```

### 2.2 Interface Definition

Create `DeliveryRepository.java` with the following structure:

```java
package com.autogratuity.data.repository.delivery;

import com.autogratuity.data.repository.core.DataRepository;
import com.autogratuity.models.Delivery;
import com.autogratuity.models.DeliveryStats;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Repository interface for managing delivery data.
 * Extends the core DataRepository for delivery-specific operations.
 */
public interface DeliveryRepository extends DataRepository {
    
    /**
     * Retrieves all deliveries for the current user.
     *
     * @return Single emitting a list of deliveries or error
     */
    Single<List<Delivery>> getDeliveries();
    
    /**
     * Retrieves deliveries with pagination support.
     *
     * @param limit Maximum number of deliveries to retrieve
     * @param lastDeliveryId ID of the last delivery in the previous page (null for first page)
     * @return Single emitting a list of deliveries or error
     */
    Single<List<Delivery>> getDeliveriesWithPagination(int limit, String lastDeliveryId);
    
    /**
     * Retrieves deliveries filtered by date range.
     *
     * @param startDate Start date for filtering
     * @param endDate End date for filtering
     * @return Single emitting filtered list of deliveries or error
     */
    Single<List<Delivery>> getDeliveriesByDateRange(Date startDate, Date endDate);
    
    /**
     * Retrieves a delivery by its ID.
     *
     * @param deliveryId ID of the delivery to retrieve
     * @return Single emitting the delivery or error
     */
    Single<Delivery> getDeliveryById(String deliveryId);
    
    /**
     * Adds a new delivery.
     *
     * @param delivery Delivery to add
     * @return Single emitting the reference to the new delivery or error
     */
    Single<String> addDelivery(Delivery delivery);
    
    /**
     * Updates an existing delivery.
     *
     * @param delivery Updated delivery
     * @return Completable indicating success or error
     */
    Completable updateDelivery(Delivery delivery);
    
    /**
     * Updates specific fields of a delivery.
     *
     * @param deliveryId ID of the delivery to update
     * @param fieldUpdates Map of field names to new values
     * @return Completable indicating success or error
     */
    Completable updateDeliveryFields(String deliveryId, Map<String, Object> fieldUpdates);
    
    /**
     * Updates the tip amount for a delivery.
     *
     * @param deliveryId ID of the delivery
     * @param tipAmount New tip amount
     * @return Completable indicating success or error
     */
    Completable updateDeliveryTip(String deliveryId, double tipAmount);
    
    /**
     * Deletes a delivery.
     *
     * @param deliveryId ID of the delivery to delete
     * @return Completable indicating success or error
     */
    Completable deleteDelivery(String deliveryId);
    
    /**
     * Retrieves delivery statistics for the current user.
     *
     * @return Single emitting delivery statistics or error
     */
    Single<DeliveryStats> getDeliveryStats();
    
    /**
     * Retrieves delivery statistics for a specific time period.
     *
     * @param startDate Start date for statistics
     * @param endDate End date for statistics
     * @return Single emitting delivery statistics for the period or error
     */
    Single<DeliveryStats> getDeliveryStatsForPeriod(Date startDate, Date endDate);
    
    /**
     * Observes all deliveries for the current user in real-time.
     *
     * @return Observable emitting updated delivery lists when changes occur
     */
    Observable<List<Delivery>> observeDeliveries();
    
    /**
     * Observes a specific delivery in real-time.
     *
     * @param deliveryId ID of the delivery to observe
     * @return Observable emitting updated delivery when changes occur
     */
    Observable<Delivery> observeDelivery(String deliveryId);
}
```

### 2.3 Implementation Details

Create `DeliveryRepositoryImpl.java` with the following structure:

```java
package com.autogratuity.data.repository.delivery;

import com.autogratuity.data.repository.core.FirestoreRepository;
import com.autogratuity.models.Delivery;
import com.autogratuity.models.DeliveryStats;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of DeliveryRepository interface.
 * Extends FirestoreRepository for Firestore-based delivery data storage.
 */
public class DeliveryRepositoryImpl extends FirestoreRepository implements DeliveryRepository {
    
    private static final String COLLECTION_DELIVERIES = "deliveries";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_TIP_AMOUNT = "tipAmount";
    
    private BehaviorSubject<List<Delivery>> deliveriesSubject = BehaviorSubject.create();
    private Map<String, BehaviorSubject<Delivery>> deliverySubjects = new HashMap<>();
    
    public DeliveryRepositoryImpl() {
        super();
        initializeDeliveriesListener();
    }
    
    private void initializeDeliveriesListener() {
        if (getCurrentUser() == null) return;
        
        getUserDeliveriesQuery()
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    logError("Error listening to deliveries", error);
                    return;
                }
                
                if (snapshots != null) {
                    List<Delivery> deliveries = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Delivery delivery = doc.toObject(Delivery.class);
                        if (delivery != null) {
                            delivery.setDeliveryId(doc.getId());
                            deliveries.add(delivery);
                            
                            // Update individual delivery subjects if they exist
                            if (deliverySubjects.containsKey(delivery.getDeliveryId())) {
                                deliverySubjects.get(delivery.getDeliveryId()).onNext(delivery);
                            }
                        }
                    }
                    deliveriesSubject.onNext(deliveries);
                }
            });
    }
    
    private Query getUserDeliveriesQuery() {
        return db.collection(COLLECTION_DELIVERIES)
            .whereEqualTo(FIELD_USER_ID, getCurrentUserId())
            .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING);
    }
    
    private DocumentReference getDeliveryDocumentReference(String deliveryId) {
        return db.collection(COLLECTION_DELIVERIES).document(deliveryId);
    }
    
    @Override
    public Single<List<Delivery>> getDeliveries() {
        if (!isNetworkAvailable() && deliveriesSubject.hasValue()) {
            return Single.just(deliveriesSubject.getValue());
        }
        
        return Single.create(emitter -> {
            getUserDeliveriesQuery().get()
                .addOnSuccessListener(snapshots -> {
                    List<Delivery> deliveries = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Delivery delivery = doc.toObject(Delivery.class);
                        if (delivery != null) {
                            delivery.setDeliveryId(doc.getId());
                            deliveries.add(delivery);
                        }
                    }
                    
                    deliveriesSubject.onNext(deliveries);
                    emitter.onSuccess(deliveries);
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Single<List<Delivery>> getDeliveriesWithPagination(int limit, String lastDeliveryId) {
        return Single.create(emitter -> {
            Query query = getUserDeliveriesQuery().limit(limit);
            
            if (lastDeliveryId != null && !lastDeliveryId.isEmpty()) {
                // Get the last document as a starting point
                getDeliveryDocumentReference(lastDeliveryId).get()
                    .addOnSuccessListener(lastDocSnapshot -> {
                        if (lastDocSnapshot.exists()) {
                            Query paginatedQuery = query.startAfter(lastDocSnapshot);
                            
                            paginatedQuery.get()
                                .addOnSuccessListener(snapshots -> processDeliveryQuerySnapshot(snapshots, emitter))
                                .addOnFailureListener(emitter::onError);
                        } else {
                            emitter.onError(new Exception("Last delivery reference not found"));
                        }
                    })
                    .addOnFailureListener(emitter::onError);
            } else {
                // First page
                query.get()
                    .addOnSuccessListener(snapshots -> processDeliveryQuerySnapshot(snapshots, emitter))
                    .addOnFailureListener(emitter::onError);
            }
        });
    }
    
    private void processDeliveryQuerySnapshot(QuerySnapshot snapshots, io.reactivex.SingleEmitter<List<Delivery>> emitter) {
        List<Delivery> deliveries = new ArrayList<>();
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            Delivery delivery = doc.toObject(Delivery.class);
            if (delivery != null) {
                delivery.setDeliveryId(doc.getId());
                deliveries.add(delivery);
            }
        }
        emitter.onSuccess(deliveries);
    }
    
    @Override
    public Single<List<Delivery>> getDeliveriesByDateRange(Date startDate, Date endDate) {
        return Single.create(emitter -> {
            Timestamp startTimestamp = new Timestamp(startDate);
            Timestamp endTimestamp = new Timestamp(endDate);
            
            getUserDeliveriesQuery()
                .whereGreaterThanOrEqualTo(FIELD_TIMESTAMP, startTimestamp)
                .whereLessThanOrEqualTo(FIELD_TIMESTAMP, endTimestamp)
                .get()
                .addOnSuccessListener(snapshots -> processDeliveryQuerySnapshot(snapshots, emitter))
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Single<Delivery> getDeliveryById(String deliveryId) {
        return Single.create(emitter -> {
            getDeliveryDocumentReference(deliveryId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Delivery delivery = doc.toObject(Delivery.class);
                        if (delivery != null) {
                            delivery.setDeliveryId(doc.getId());
                            emitter.onSuccess(delivery);
                        } else {
                            emitter.onError(new Exception("Failed to parse delivery"));
                        }
                    } else {
                        emitter.onError(new Exception("Delivery not found"));
                    }
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Single<String> addDelivery(Delivery delivery) {
        return Single.create(emitter -> {
            if (delivery == null) {
                emitter.onError(new IllegalArgumentException("Delivery cannot be null"));
                return;
            }
            
            // Ensure the user ID is set correctly
            delivery.setUserId(getCurrentUserId());
            
            // Set timestamp if not already set
            if (delivery.getTimestamp() == null) {
                delivery.setTimestamp(new Timestamp(new Date()));
            }
            
            db.collection(COLLECTION_DELIVERIES)
                .add(delivery)
                .addOnSuccessListener(docRef -> {
                    String deliveryId = docRef.getId();
                    delivery.setDeliveryId(deliveryId);
                    
                    // Update delivery with its ID
                    docRef.update("deliveryId", deliveryId)
                        .addOnSuccessListener(aVoid -> emitter.onSuccess(deliveryId))
                        .addOnFailureListener(emitter::onError);
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Completable updateDelivery(Delivery delivery) {
        return Completable.create(emitter -> {
            if (delivery == null || delivery.getDeliveryId() == null) {
                emitter.onError(new IllegalArgumentException("Delivery and deliveryId cannot be null"));
                return;
            }
            
            getDeliveryDocumentReference(delivery.getDeliveryId())
                .set(delivery)
                .addOnSuccessListener(aVoid -> emitter.onComplete())
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Completable updateDeliveryFields(String deliveryId, Map<String, Object> fieldUpdates) {
        return Completable.create(emitter -> {
            if (deliveryId == null || fieldUpdates == null || fieldUpdates.isEmpty()) {
                emitter.onError(new IllegalArgumentException("DeliveryId and field updates cannot be null or empty"));
                return;
            }
            
            getDeliveryDocumentReference(deliveryId)
                .set(fieldUpdates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> emitter.onComplete())
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Completable updateDeliveryTip(String deliveryId, double tipAmount) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_TIP_AMOUNT, tipAmount);
        return updateDeliveryFields(deliveryId, updates);
    }
    
    @Override
    public Completable deleteDelivery(String deliveryId) {
        return Completable.create(emitter -> {
            if (deliveryId == null) {
                emitter.onError(new IllegalArgumentException("DeliveryId cannot be null"));
                return;
            }
            
            getDeliveryDocumentReference(deliveryId)
                .delete()
                .addOnSuccessListener(aVoid -> emitter.onComplete())
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Single<DeliveryStats> getDeliveryStats() {
        return getDeliveries()
            .map(this::calculateDeliveryStats);
    }
    
    @Override
    public Single<DeliveryStats> getDeliveryStatsForPeriod(Date startDate, Date endDate) {
        return getDeliveriesByDateRange(startDate, endDate)
            .map(this::calculateDeliveryStats);
    }
    
    private DeliveryStats calculateDeliveryStats(List<Delivery> deliveries) {
        DeliveryStats stats = new DeliveryStats();
        
        if (deliveries.isEmpty()) {
            return stats;
        }
        
        double totalEarnings = 0;
        double totalTips = 0;
        double totalDistance = 0;
        double maxTip = Double.MIN_VALUE;
        double minTip = Double.MAX_VALUE;
        
        for (Delivery delivery : deliveries) {
            double tipAmount = delivery.getTipAmount();
            totalEarnings += delivery.getTotalEarnings();
            totalTips += tipAmount;
            totalDistance += delivery.getDistance();
            
            if (tipAmount > maxTip) {
                maxTip = tipAmount;
            }
            
            if (tipAmount < minTip) {
                minTip = tipAmount;
            }
        }
        
        stats.setTotalDeliveries(deliveries.size());
        stats.setTotalEarnings(totalEarnings);
        stats.setTotalTips(totalTips);
        stats.setAverageTip(totalTips / deliveries.size());
        stats.setMaxTip(maxTip);
        stats.setMinTip(minTip == Double.MAX_VALUE ? 0 : minTip);
        stats.setTotalDistance(totalDistance);
        stats.setAverageDistance(totalDistance / deliveries.size());
        
        return stats;
    }
    
    @Override
    public Observable<List<Delivery>> observeDeliveries() {
        // If no value is present, trigger a fetch
        if (!deliveriesSubject.hasValue()) {
            getDeliveries().subscribe(
                deliveries -> {}, // Already handled by subject
                error -> logError("Error fetching deliveries for observation", error)
            );
        }
        
        return deliveriesSubject;
    }
    
    @Override
    public Observable<Delivery> observeDelivery(String deliveryId) {
        // Create or get subject for this delivery
        BehaviorSubject<Delivery> subject = deliverySubjects.computeIfAbsent(
            deliveryId, k -> BehaviorSubject.create());
        
        // Set up document listener if this is a new subject
        if (!subject.hasValue()) {
            getDeliveryDocumentReference(deliveryId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        logError("Error listening to delivery " + deliveryId, error);
                        return;
                    }
                    
                    if (snapshot != null && snapshot.exists()) {
                        Delivery delivery = snapshot.toObject(Delivery.class);
                        if (delivery != null) {
                            delivery.setDeliveryId(snapshot.getId());
                            subject.onNext(delivery);
                        }
                    }
                });
            
            // Also trigger an initial fetch
            getDeliveryById(deliveryId).subscribe(
                subject::onNext,
                error -> logError("Error fetching delivery for observation", error)
            );
        }
        
        return subject;
    }
}
```

### 2.4 RepositoryProvider Update

Update `RepositoryProvider.java` with the following:

```java
// Add this field
private static DeliveryRepository deliveryRepository;

// Add this method
public static DeliveryRepository getDeliveryRepository() {
    if (deliveryRepository == null) {
        deliveryRepository = new DeliveryRepositoryImpl();
    }
    return deliveryRepository;
}
```

## 3. Subscription Repository Implementation

### 3.1 File Structure

```
repository/
└── subscription/
    ├── SubscriptionRepository.java (Interface)
    └── SubscriptionRepositoryImpl.java (Implementation)
```

### 3.2 Interface Definition

Create `SubscriptionRepository.java` with the following structure:

```java
package com.autogratuity.data.repository.subscription;

import com.autogratuity.data.repository.core.DataRepository;
import com.autogratuity.models.SubscriptionStatus;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Repository interface for managing subscription data.
 * Extends the core DataRepository for subscription-specific operations.
 */
public interface SubscriptionRepository extends DataRepository {
    
    /**
     * Retrieves the current subscription status for the user.
     *
     * @return Single emitting the subscription status or error
     */
    Single<SubscriptionStatus> getSubscriptionStatus();
    
    /**
     * Updates the subscription status with new information.
     *
     * @param subscriptionStatus Updated subscription status
     * @return Completable indicating success or error
     */
    Completable updateSubscriptionStatus(SubscriptionStatus subscriptionStatus);
    
    /**
     * Adds a record of a subscription purchase.
     *
     * @param purchaseToken Purchase token from the payment processor
     * @param productId Identifier for the purchased subscription
     * @param purchaseTime Time of the purchase
     * @return Completable indicating success or error
     */
    Completable addSubscriptionRecord(String purchaseToken, String productId, long purchaseTime);
    
    /**
     * Verifies a subscription purchase with the backend server.
     *
     * @param purchaseToken Purchase token to verify
     * @return Single emitting a boolean indicating if the subscription is valid
     */
    Single<Boolean> verifySubscription(String purchaseToken);
    
    /**
     * Checks if the user has a valid pro subscription.
     *
     * @return Single emitting a boolean indicating if the user is a pro user
     */
    Single<Boolean> isProUser();
    
    /**
     * Retrieves the expiry date of the current subscription.
     *
     * @return Single emitting the expiry timestamp or error
     */
    Single<Long> getSubscriptionExpiryDate();
    
    /**
     * Updates the subscription expiry date.
     *
     * @param expiryTimeMillis New expiry timestamp in milliseconds
     * @return Completable indicating success or error
     */
    Completable updateSubscriptionExpiryDate(long expiryTimeMillis);
    
    /**
     * Sets the auto-renewal status of the subscription.
     *
     * @param autoRenew Whether the subscription auto-renews
     * @return Completable indicating success or error
     */
    Completable setSubscriptionAutoRenewal(boolean autoRenew);
    
    /**
     * Observes the subscription status in real-time.
     *
     * @return Observable emitting updated subscription status when changes occur
     */
    Observable<SubscriptionStatus> observeSubscriptionStatus();
}
```

### 3.3 Implementation Details

Create `SubscriptionRepositoryImpl.java` with the following structure:

```java
package com.autogratuity.data.repository.subscription;

import com.autogratuity.data.repository.core.FirestoreRepository;
import com.autogratuity.models.SubscriptionStatus;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of SubscriptionRepository interface.
 * Extends FirestoreRepository for Firestore-based subscription data storage.
 */
public class SubscriptionRepositoryImpl extends FirestoreRepository implements SubscriptionRepository {
    
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_SUBSCRIPTIONS = "subscriptions";
    private static final String DOCUMENT_SUBSCRIPTION = "currentSubscription";
    private static final String COLLECTION_PURCHASE_HISTORY = "purchaseHistory";
    
    private static final String FIELD_SUBSCRIPTION_TYPE = "subscriptionType";
    private static final String FIELD_EXPIRY_DATE = "expiryDate";
    private static final String FIELD_AUTO_RENEW = "autoRenew";
    private static final String FIELD_PURCHASE_TOKEN = "purchaseToken";
    private static final String FIELD_PRODUCT_ID = "productId";
    private static final String FIELD_PURCHASE_TIME = "purchaseTime";
    private static final String FIELD_VERIFIED = "verified";
    
    private BehaviorSubject<SubscriptionStatus> subscriptionSubject = BehaviorSubject.create();
    
    public SubscriptionRepositoryImpl() {
        super();
        initializeSubscriptionListener();
    }
    
    private void initializeSubscriptionListener() {
        if (getCurrentUser() == null) return;
        
        getUserSubscriptionDocumentReference()
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    logError("Error listening to subscription status", error);
                    return;
                }
                
                if (snapshot != null && snapshot.exists()) {
                    SubscriptionStatus status = snapshot.toObject(SubscriptionStatus.class);
                    if (status != null) {
                        subscriptionSubject.onNext(status);
                    }
                }
            });
    }
    
    private DocumentReference getUserSubscriptionDocumentReference() {
        return db.collection(COLLECTION_USERS)
            .document(getCurrentUserId())
            .collection(COLLECTION_SUBSCRIPTIONS)
            .document(DOCUMENT_SUBSCRIPTION);
    }
    
    @Override
    public Single<SubscriptionStatus> getSubscriptionStatus() {
        if (!isNetworkAvailable() && subscriptionSubject.hasValue()) {
            return Single.just(subscriptionSubject.getValue());
        }
        
        return Single.create(emitter -> {
            getUserSubscriptionDocumentReference().get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        SubscriptionStatus status = snapshot.toObject(SubscriptionStatus.class);
                        if (status != null) {
                            subscriptionSubject.onNext(status);
                            emitter.onSuccess(status);
                        } else {
                            emitter.onError(new Exception("Failed to parse subscription status"));
                        }
                    } else {
                        // Create a default subscription status if none exists
                        SubscriptionStatus defaultStatus = createDefaultSubscriptionStatus();
                        emitter.onSuccess(defaultStatus);
                    }
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    private SubscriptionStatus createDefaultSubscriptionStatus() {
        SubscriptionStatus defaultStatus = new SubscriptionStatus();
        defaultStatus.setUserId(getCurrentUserId());
        defaultStatus.setSubscriptionType("free");
        defaultStatus.setAutoRenew(false);
        
        // Save the default status
        updateSubscriptionStatus(defaultStatus).subscribe();
        
        return defaultStatus;
    }
    
    @Override
    public Completable updateSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        return Completable.create(emitter -> {
            if (subscriptionStatus == null) {
                emitter.onError(new IllegalArgumentException("Subscription status cannot be null"));
                return;
            }
            
            // Ensure the user ID is set correctly
            subscriptionStatus.setUserId(getCurrentUserId());
            
            getUserSubscriptionDocumentReference()
                .set(subscriptionStatus)
                .addOnSuccessListener(aVoid -> {
                    subscriptionSubject.onNext(subscriptionStatus);
                    emitter.onComplete();
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Completable addSubscriptionRecord(String purchaseToken, String productId, long purchaseTime) {
        return Completable.create(emitter -> {
            if (purchaseToken == null || productId == null) {
                emitter.onError(new IllegalArgumentException("Purchase token and product ID cannot be null"));
                return;
            }
            
            Map<String, Object> purchaseRecord = new HashMap<>();
            purchaseRecord.put(FIELD_PURCHASE_TOKEN, purchaseToken);
            purchaseRecord.put(FIELD_PRODUCT_ID, productId);
            purchaseRecord.put(FIELD_PURCHASE_TIME, purchaseTime);
            purchaseRecord.put(FIELD_VERIFIED, false);
            
            db.collection(COLLECTION_USERS)
                .document(getCurrentUserId())
                .collection(COLLECTION_PURCHASE_HISTORY)
                .add(purchaseRecord)
                .addOnSuccessListener(docRef -> {
                    // After adding the record, proceed to verify it
                    verifySubscription(purchaseToken)
                        .subscribe(
                            isValid -> {
                                // Update the record with verification status
                                docRef.update(FIELD_VERIFIED, isValid)
                                    .addOnSuccessListener(aVoid -> {
                                        if (isValid) {
                                            // If valid, update the subscription status
                                            updateSubscriptionFromPurchase(productId, purchaseTime)
                                                .subscribe(
                                                    () -> emitter.onComplete(),
                                                    emitter::onError
                                                );
                                        } else {
                                            emitter.onComplete();
                                        }
                                    })
                                    .addOnFailureListener(emitter::onError);
                            },
                            emitter::onError
                        );
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    private Completable updateSubscriptionFromPurchase(String productId, long purchaseTime) {
        // Determine subscription type and expiry based on product ID
        String subscriptionType;
        long expiryTimeMillis;
        
        // This logic would depend on your subscription offerings
        switch (productId) {
            case "monthly_pro_subscription":
                subscriptionType = "pro";
                expiryTimeMillis = purchaseTime + (30L * 24 * 60 * 60 * 1000); // 30 days
                break;
            case "yearly_pro_subscription":
                subscriptionType = "pro";
                expiryTimeMillis = purchaseTime + (365L * 24 * 60 * 60 * 1000); // 365 days
                break;
            default:
                subscriptionType = "free";
                expiryTimeMillis = 0;
                break;
        }
        
        // Create updated subscription status
        SubscriptionStatus updatedStatus = new SubscriptionStatus();
        updatedStatus.setUserId(getCurrentUserId());
        updatedStatus.setSubscriptionType(subscriptionType);
        updatedStatus.setExpiryDate(new Timestamp(new Date(expiryTimeMillis)));
        updatedStatus.setAutoRenew(true);
        
        return updateSubscriptionStatus(updatedStatus);
    }
    
    @Override
    public Single<Boolean> verifySubscription(String purchaseToken) {
        // In a real implementation, this would verify with Google Play or Apple App Store
        // For this example, we'll simulate a verification process
        return Single.create(emitter -> {
            // Simulated network delay for verification
            try {
                Thread.sleep(1000);
                
                // For demonstration, assume all tokens starting with "valid" are valid
                boolean isValid = purchaseToken != null && purchaseToken.startsWith("valid");
                emitter.onSuccess(isValid);
            } catch (InterruptedException e) {
                emitter.onError(e);
            }
        });
    }
    
    @Override
    public Single<Boolean> isProUser() {
        return getSubscriptionStatus()
            .map(status -> {
                if (status == null) return false;
                
                // Check if user has a pro subscription
                boolean isPro = "pro".equals(status.getSubscriptionType());
                
                // Check if subscription has expired
                if (isPro && status.getExpiryDate() != null) {
                    Date expiryDate = status.getExpiryDate().toDate();
                    Date now = new Date();
                    
                    // If expired and not set to auto-renew, user is no longer pro
                    if (now.after(expiryDate) && !status.isAutoRenew()) {
                        return false;
                    }
                }
                
                return isPro;
            });
    }
    
    @Override
    public Single<Long> getSubscriptionExpiryDate() {
        return getSubscriptionStatus()
            .map(status -> {
                if (status == null || status.getExpiryDate() == null) {
                    return 0L;
                }
                return status.getExpiryDate().toDate().getTime();
            });
    }
    
    @Override
    public Completable updateSubscriptionExpiryDate(long expiryTimeMillis) {
        return getSubscriptionStatus()
            .flatMapCompletable(currentStatus -> {
                currentStatus.setExpiryDate(new Timestamp(new Date(expiryTimeMillis)));
                return updateSubscriptionStatus(currentStatus);
            });
    }
    
    @Override
    public Completable setSubscriptionAutoRenewal(boolean autoRenew) {
        return getSubscriptionStatus()
            .flatMapCompletable(currentStatus -> {
                currentStatus.setAutoRenew(autoRenew);
                return updateSubscriptionStatus(currentStatus);
            });
    }
    
    @Override
    public Observable<SubscriptionStatus> observeSubscriptionStatus() {
        // If no value is present, trigger a fetch
        if (!subscriptionSubject.hasValue()) {
            getSubscriptionStatus().subscribe(
                status -> {}, // Already handled by subject
                error -> logError("Error fetching subscription status for observation", error)
            );
        }
        
        return subscriptionSubject;
    }
}
```

### 3.4 RepositoryProvider Update

Update `RepositoryProvider.java` with the following:

```java
// Add this field
private static SubscriptionRepository subscriptionRepository;

// Add this method
public static SubscriptionRepository getSubscriptionRepository() {
    if (subscriptionRepository == null) {
        subscriptionRepository = new SubscriptionRepositoryImpl();
    }
    return subscriptionRepository;
}
```

## 4. Sync Repository Implementation

### 4.1 File Structure

```
repository/
└── sync/
    ├── SyncRepository.java (Interface)
    └── SyncRepositoryImpl.java (Implementation)
```

### 4.2 Interface Definition

Create `SyncRepository.java` with the following structure:

```java
package com.autogratuity.data.repository.sync;

import com.autogratuity.data.repository.core.DataRepository;
import com.autogratuity.models.SyncOperation;
import com.autogratuity.models.SyncStatus;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.List;

/**
 * Repository interface for managing data synchronization.
 * Extends the core DataRepository for sync-specific operations.
 */
public interface SyncRepository extends DataRepository {
    
    /**
     * Synchronizes all data for the current user.
     *
     * @return Completable indicating success or error
     */
    Completable syncData();
    
    /**
     * Retrieves the current synchronization status.
     *
     * @return Single emitting the sync status or error
     */
    Single<SyncStatus> getSyncStatus();
    
    /**
     * Adds a sync operation to the queue for processing.
     *
     * @param operation Sync operation to enqueue
     * @return Completable indicating success or error
     */
    Completable enqueueSyncOperation(SyncOperation operation);
    
    /**
     * Retrieves all pending sync operations.
     *
     * @return Single emitting a list of pending operations or error
     */
    Single<List<SyncOperation>> getPendingSyncOperations();
    
    /**
     * Processes all pending sync operations.
     *
     * @return Completable indicating success or error
     */
    Completable processPendingSyncOperations();
    
    /**
     * Updates the sync status for the current device.
     *
     * @param lastSyncTime Timestamp of the last successful sync
     * @param syncSuccess Whether the sync was successful
     * @param errorMessage Error message if sync failed (null if successful)
     * @return Completable indicating success or error
     */
    Completable updateDeviceSyncStatus(long lastSyncTime, boolean syncSuccess, String errorMessage);
    
    /**
     * Observes the sync status in real-time.
     *
     * @return Observable emitting updated sync status when changes occur
     */
    Observable<SyncStatus> observeSyncStatus();
    
    /**
     * Schedules automatic background synchronization.
     *
     * @param intervalMinutes Interval between syncs in minutes
     * @param requiresWifi Whether sync should only occur on WiFi
     * @param requiresCharging Whether sync should only occur when charging
     * @return Completable indicating success or error
     */
    Completable schedulePeriodicSync(int intervalMinutes, boolean requiresWifi, boolean requiresCharging);
    
    /**
     * Cancels scheduled background synchronization.
     *
     * @return Completable indicating success or error
     */
    Completable cancelPeriodicSync();
}
```

### 4.3 Implementation Details

Create `SyncRepositoryImpl.java` with the following structure:

```java
package com.autogratuity.data.repository.sync;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.autogratuity.data.repository.core.FirestoreRepository;
import com.autogratuity.models.SyncOperation;
import com.autogratuity.models.SyncStatus;
import com.autogratuity.workers.SyncWorker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of SyncRepository interface.
 * Extends FirestoreRepository for Firestore-based sync data storage.
 */
public class SyncRepositoryImpl extends FirestoreRepository implements SyncRepository {
    
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_SYNC = "sync";
    private static final String DOCUMENT_SYNC_STATUS = "syncStatus";
    private static final String COLLECTION_PENDING_OPERATIONS = "pendingOperations";
    
    private static final String FIELD_LAST_SYNC_TIME = "lastSyncTime";
    private static final String FIELD_SYNC_SUCCESS = "syncSuccess";
    private static final String FIELD_ERROR_MESSAGE = "errorMessage";
    private static final String FIELD_DEVICE_ID = "deviceId";
    private static final String FIELD_OPERATION_TYPE = "operationType";
    private static final String FIELD_TARGET_COLLECTION = "targetCollection";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_DATA = "data";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_RETRY_COUNT = "retryCount";
    
    private static final String WORK_NAME_PERIODIC_SYNC = "periodicSync";
    
    private BehaviorSubject<SyncStatus> syncStatusSubject = BehaviorSubject.create();
    private Context applicationContext;
    
    public SyncRepositoryImpl(Context context) {
        super();
        this.applicationContext = context.getApplicationContext();
        initializeSyncStatusListener();
    }
    
    private void initializeSyncStatusListener() {
        if (getCurrentUser() == null) return;
        
        getUserSyncStatusDocumentReference()
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    logError("Error listening to sync status", error);
                    return;
                }
                
                if (snapshot != null && snapshot.exists()) {
                    SyncStatus status = snapshot.toObject(SyncStatus.class);
                    if (status != null) {
                        syncStatusSubject.onNext(status);
                    }
                }
            });
    }
    
    private DocumentReference getUserSyncStatusDocumentReference() {
        return db.collection(COLLECTION_USERS)
            .document(getCurrentUserId())
            .collection(COLLECTION_SYNC)
            .document(DOCUMENT_SYNC_STATUS);
    }
    
    @Override
    public Completable syncData() {
        return Completable.create(emitter -> {
            // Start with processing any pending operations
            processPendingSyncOperations()
                .andThen(Completable.defer(() -> {
                    // Then perform full data sync
                    // This would typically involve fetching latest data from server
                    // and updating local cache
                    
                    // For demonstration, we'll just update the sync status
                    long syncTime = System.currentTimeMillis();
                    return updateDeviceSyncStatus(syncTime, true, null);
                }))
                .subscribe(
                    emitter::onComplete,
                    emitter::onError
                );
        });
    }
    
    @Override
    public Single<SyncStatus> getSyncStatus() {
        if (!isNetworkAvailable() && syncStatusSubject.hasValue()) {
            return Single.just(syncStatusSubject.getValue());
        }
        
        return Single.create(emitter -> {
            getUserSyncStatusDocumentReference().get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        SyncStatus status = snapshot.toObject(SyncStatus.class);
                        if (status != null) {
                            syncStatusSubject.onNext(status);
                            emitter.onSuccess(status);
                        } else {
                            emitter.onError(new Exception("Failed to parse sync status"));
                        }
                    } else {
                        // Create a default sync status if none exists
                        SyncStatus defaultStatus = createDefaultSyncStatus();
                        emitter.onSuccess(defaultStatus);
                    }
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    private SyncStatus createDefaultSyncStatus() {
        SyncStatus defaultStatus = new SyncStatus();
        defaultStatus.setUserId(getCurrentUserId());
        defaultStatus.setDeviceId(getDeviceId());
        defaultStatus.setLastSyncTime(0L);
        defaultStatus.setSyncSuccess(false);
        
        // Save the default status
        updateDeviceSyncStatus(0L, false, "Initial status").subscribe();
        
        return defaultStatus;
    }
    
    @Override
    public Completable enqueueSyncOperation(SyncOperation operation) {
        return Completable.create(emitter -> {
            if (operation == null) {
                emitter.onError(new IllegalArgumentException("Sync operation cannot be null"));
                return;
            }
            
            // Set default values if not provided
            if (operation.getTimestamp() == null) {
                operation.setTimestamp(new Timestamp(new Date()));
            }
            
            if (operation.getStatus() == null) {
                operation.setStatus("pending");
            }
            
            if (operation.getRetryCount() == null) {
                operation.setRetryCount(0);
            }
            
            db.collection(COLLECTION_USERS)
                .document(getCurrentUserId())
                .collection(COLLECTION_PENDING_OPERATIONS)
                .add(operation)
                .addOnSuccessListener(docRef -> {
                    // Update the operation with its ID
                    docRef.update("operationId", docRef.getId())
                        .addOnSuccessListener(aVoid -> emitter.onComplete())
                        .addOnFailureListener(emitter::onError);
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Single<List<SyncOperation>> getPendingSyncOperations() {
        return Single.create(emitter -> {
            db.collection(COLLECTION_USERS)
                .document(getCurrentUserId())
                .collection(COLLECTION_PENDING_OPERATIONS)
                .whereEqualTo(FIELD_STATUS, "pending")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<SyncOperation> operations = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        SyncOperation operation = doc.toObject(SyncOperation.class);
                        if (operation != null) {
                            operation.setOperationId(doc.getId());
                            operations.add(operation);
                        }
                    }
                    emitter.onSuccess(operations);
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Completable processPendingSyncOperations() {
        return getPendingSyncOperations()
            .flatMapCompletable(operations -> {
                if (operations.isEmpty()) {
                    return Completable.complete();
                }
                
                List<Completable> processingCompletables = new ArrayList<>();
                
                for (SyncOperation operation : operations) {
                    processingCompletables.add(processSyncOperation(operation));
                }
                
                return Completable.concat(processingCompletables);
            });
    }
    
    private Completable processSyncOperation(SyncOperation operation) {
        return Completable.create(emitter -> {
            if (operation == null || operation.getOperationId() == null) {
                emitter.onError(new IllegalArgumentException("Invalid sync operation"));
                return;
            }
            
            // In a real implementation, this would handle different operation types
            // For this example, we'll simulate processing with a delay
            try {
                Thread.sleep(500);
                
                // Update operation status to completed
                DocumentReference operationRef = db.collection(COLLECTION_USERS)
                    .document(getCurrentUserId())
                    .collection(COLLECTION_PENDING_OPERATIONS)
                    .document(operation.getOperationId());
                
                Map<String, Object> updates = new HashMap<>();
                updates.put(FIELD_STATUS, "completed");
                updates.put("completedAt", new Timestamp(new Date()));
                
                operationRef.set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(error -> {
                        // If we fail to update the status, increment retry count
                        operation.setRetryCount(operation.getRetryCount() + 1);
                        operationRef.update(FIELD_RETRY_COUNT, operation.getRetryCount())
                            .addOnCompleteListener(task -> emitter.onError(error));
                    });
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
    
    @Override
    public Completable updateDeviceSyncStatus(long lastSyncTime, boolean syncSuccess, String errorMessage) {
        return Completable.create(emitter -> {
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put(FIELD_LAST_SYNC_TIME, lastSyncTime);
            statusUpdate.put(FIELD_SYNC_SUCCESS, syncSuccess);
            statusUpdate.put(FIELD_ERROR_MESSAGE, errorMessage);
            statusUpdate.put(FIELD_DEVICE_ID, getDeviceId());
            statusUpdate.put("userId", getCurrentUserId());
            
            getUserSyncStatusDocumentReference()
                .set(statusUpdate, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Create a SyncStatus object to emit to observers
                    SyncStatus status = new SyncStatus();
                    status.setUserId(getCurrentUserId());
                    status.setDeviceId(getDeviceId());
                    status.setLastSyncTime(lastSyncTime);
                    status.setSyncSuccess(syncSuccess);
                    status.setErrorMessage(errorMessage);
                    
                    syncStatusSubject.onNext(status);
                    emitter.onComplete();
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Observable<SyncStatus> observeSyncStatus() {
        // If no value is present, trigger a fetch
        if (!syncStatusSubject.hasValue()) {
            getSyncStatus().subscribe(
                status -> {}, // Already handled by subject
                error -> logError("Error fetching sync status for observation", error)
            );
        }
        
        return syncStatusSubject;
    }
    
    @Override
    public Completable schedulePeriodicSync(int intervalMinutes, boolean requiresWifi, boolean requiresCharging) {
        return Completable.fromAction(() -> {
            // Create WorkManager constraints
            Constraints.Builder constraintsBuilder = new Constraints.Builder();
            
            if (requiresWifi) {
                constraintsBuilder.setRequiredNetworkType(NetworkType.UNMETERED);
            } else {
                constraintsBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
            }
            
            if (requiresCharging) {
                constraintsBuilder.setRequiresCharging(true);
            }
            
            Constraints constraints = constraintsBuilder.build();
            
            // Create the periodic work request
            PeriodicWorkRequest syncWorkRequest =
                new PeriodicWorkRequest.Builder(SyncWorker.class, intervalMinutes, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build();
            
            // Schedule the work
            WorkManager.getInstance(applicationContext)
                .enqueueUniquePeriodicWork(
                    WORK_NAME_PERIODIC_SYNC,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    syncWorkRequest
                );
        });
    }
    
    @Override
    public Completable cancelPeriodicSync() {
        return Completable.fromAction(() -> {
            WorkManager.getInstance(applicationContext)
                .cancelUniqueWork(WORK_NAME_PERIODIC_SYNC);
        });
    }
}
```

### 4.4 RepositoryProvider Update

Update `RepositoryProvider.java` with the following:

```java
// Add this field
private static SyncRepository syncRepository;

// Add this method
public static SyncRepository getSyncRepository(Context context) {
    if (syncRepository == null) {
        syncRepository = new SyncRepositoryImpl(context);
    }
    return syncRepository;
}
```

## 5. Address Repository Implementation

### 5.1 File Structure

```
repository/
└── address/
    ├── AddressRepository.java (Interface)
    └── AddressRepositoryImpl.java (Implementation)
```

### 5.2 Interface Definition

Create `AddressRepository.java` with the following structure:

```java
package com.autogratuity.data.repository.address;

import com.autogratuity.data.repository.core.DataRepository;
import com.autogratuity.models.Address;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.List;
import java.util.Map;

/**
 * Repository interface for managing address data.
 * Extends the core DataRepository for address-specific operations.
 */
public interface AddressRepository extends DataRepository {
    
    /**
     * Retrieves all addresses for the current user.
     *
     * @return Single emitting a list of addresses or error
     */
    Single<List<Address>> getAddresses();
    
    /**
     * Retrieves an address by its ID.
     *
     * @param addressId ID of the address to retrieve
     * @return Single emitting the address or error
     */
    Single<Address> getAddressById(String addressId);
    
    /**
     * Finds an address by its normalized form.
     *
     * @param normalizedAddress Normalized address string
     * @return Single emitting the address or null if not found
     */
    Single<Address> findAddressByNormalizedAddress(String normalizedAddress);
    
    /**
     * Adds a new address.
     *
     * @param address Address to add
     * @return Single emitting the reference to the new address or error
     */
    Single<String> addAddress(Address address);
    
    /**
     * Updates an existing address.
     *
     * @param address Updated address
     * @return Completable indicating success or error
     */
    Completable updateAddress(Address address);
    
    /**
     * Updates specific fields of an address.
     *
     * @param addressId ID of the address to update
     * @param fieldUpdates Map of field names to new values
     * @return Completable indicating success or error
     */
    Completable updateAddressFields(String addressId, Map<String, Object> fieldUpdates);
    
    /**
     * Deletes an address.
     *
     * @param addressId ID of the address to delete
     * @return Completable indicating success or error
     */
    Completable deleteAddress(String addressId);
    
    /**
     * Sets an address as the favorite.
     *
     * @param addressId ID of the address to set as favorite
     * @return Completable indicating success or error
     */
    Completable setAddressAsFavorite(String addressId);
    
    /**
     * Observes all addresses for the current user in real-time.
     *
     * @return Observable emitting updated address lists when changes occur
     */
    Observable<List<Address>> observeAddresses();
    
    /**
     * Observes a specific address in real-time.
     *
     * @param addressId ID of the address to observe
     * @return Observable emitting updated address when changes occur
     */
    Observable<Address> observeAddress(String addressId);
    
    /**
     * Normalizes an address string for consistent comparison.
     *
     * @param addressString Raw address string
     * @return Normalized address string
     */
    String normalizeAddress(String addressString);
}
```

### 5.3 Implementation Details

Create `AddressRepositoryImpl.java` with the following structure:

```java
package com.autogratuity.data.repository.address;

import com.autogratuity.data.repository.core.FirestoreRepository;
import com.autogratuity.models.Address;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementation of AddressRepository interface.
 * Extends FirestoreRepository for Firestore-based address data storage.
 */
public class AddressRepositoryImpl extends FirestoreRepository implements AddressRepository {
    
    private static final String COLLECTION_ADDRESSES = "addresses";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_NORMALIZED_ADDRESS = "normalizedAddress";
    private static final String FIELD_IS_FAVORITE = "isFavorite";
    
    private BehaviorSubject<List<Address>> addressesSubject = BehaviorSubject.create();
    private Map<String, BehaviorSubject<Address>> addressSubjects = new HashMap<>();
    
    public AddressRepositoryImpl() {
        super();
        initializeAddressesListener();
    }
    
    private void initializeAddressesListener() {
        if (getCurrentUser() == null) return;
        
        getUserAddressesQuery()
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    logError("Error listening to addresses", error);
                    return;
                }
                
                if (snapshots != null) {
                    List<Address> addresses = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Address address = doc.toObject(Address.class);
                        if (address != null) {
                            address.setAddressId(doc.getId());
                            addresses.add(address);
                            
                            // Update individual address subjects if they exist
                            if (addressSubjects.containsKey(address.getAddressId())) {
                                addressSubjects.get(address.getAddressId()).onNext(address);
                            }
                        }
                    }
                    addressesSubject.onNext(addresses);
                }
            });
    }
    
    private Query getUserAddressesQuery() {
        return db.collection(COLLECTION_ADDRESSES)
            .whereEqualTo(FIELD_USER_ID, getCurrentUserId());
    }
    
    private DocumentReference getAddressDocumentReference(String addressId) {
        return db.collection(COLLECTION_ADDRESSES).document(addressId);
    }
    
    @Override
    public Single<List<Address>> getAddresses() {
        if (!isNetworkAvailable() && addressesSubject.hasValue()) {
            return Single.just(addressesSubject.getValue());
        }
        
        return Single.create(emitter -> {
            getUserAddressesQuery().get()
                .addOnSuccessListener(snapshots -> {
                    List<Address> addresses = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Address address = doc.toObject(Address.class);
                        if (address != null) {
                            address.setAddressId(doc.getId());
                            addresses.add(address);
                        }
                    }
                    
                    addressesSubject.onNext(addresses);
                    emitter.onSuccess(addresses);
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Single<Address> getAddressById(String addressId) {
        return Single.create(emitter -> {
            getAddressDocumentReference(addressId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Address address = doc.toObject(Address.class);
                        if (address != null) {
                            address.setAddressId(doc.getId());
                            
                            // Update address subject if it exists
                            if (addressSubjects.containsKey(addressId)) {
                                addressSubjects.get(addressId).onNext(address);
                            }
                            
                            emitter.onSuccess(address);
                        } else {
                            emitter.onError(new Exception("Failed to parse address"));
                        }
                    } else {
                        emitter.onError(new Exception("Address not found"));
                    }
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Single<Address> findAddressByNormalizedAddress(String normalizedAddress) {
        return Single.create(emitter -> {
            db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo(FIELD_USER_ID, getCurrentUserId())
                .whereEqualTo(FIELD_NORMALIZED_ADDRESS, normalizedAddress)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        DocumentSnapshot doc = snapshots.getDocuments().get(0);
                        Address address = doc.toObject(Address.class);
                        if (address != null) {
                            address.setAddressId(doc.getId());
                            emitter.onSuccess(address);
                        } else {
                            emitter.onSuccess(null);
                        }
                    } else {
                        emitter.onSuccess(null);
                    }
                })
                .addOnFailureListener(emitter::onError);
        });
    }
    
    @Override
    public Single<String>