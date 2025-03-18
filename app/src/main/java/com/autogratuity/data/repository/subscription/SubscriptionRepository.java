package com.autogratuity.data.repository.subscription;

import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.repository.core.DataRepository;
import com.google.firebase.firestore.DocumentReference;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Repository interface for managing subscription data.
 * Extends the core DataRepository for subscription-specific operations.
 */
public interface SubscriptionRepository extends DataRepository {
    
    //-----------------------------------------------------------------------------------
    // Subscription Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Get the current user's subscription status.
     * 
     * @return Single that emits the subscription status
     */
    @Override
    Single<SubscriptionStatus> getSubscriptionStatus();
    
    /**
     * Update the user's subscription status.
     * 
     * @param status The updated subscription status
     * @return Completable that completes when update is finished
     */
    @Override
    Completable updateSubscriptionStatus(SubscriptionStatus status);
    
    /**
     * Add a new subscription record.
     * 
     * @param subscriptionRecord Map containing subscription details
     * @return Single that emits the document reference
     */
    @Override
    Single<DocumentReference> addSubscriptionRecord(Map<String, Object> subscriptionRecord);
    
    /**
     * Observe changes to the subscription status in real-time.
     * 
     * @return Observable that emits subscription status updates
     */
    @Override
    Observable<SubscriptionStatus> observeSubscriptionStatus();
    
    /**
     * Verify the current subscription with the payment provider.
     * 
     * @return Completable that completes when verification is finished
     */
    @Override
    Completable verifySubscription();
    
    /**
     * Check if the current user has an active pro subscription.
     * 
     * @return Single that emits true if user has pro subscription, false otherwise
     */
    @Override
    Single<Boolean> isProUser();
    
    //-----------------------------------------------------------------------------------
    // Convenience Methods
    //-----------------------------------------------------------------------------------
    
    /**
     * Get the expiration date of the current subscription.
     * Returns null for free or lifetime subscriptions.
     * 
     * @return Single that emits the expiration date or null
     */
    Single<java.util.Date> getSubscriptionExpiryDate();
    
    /**
     * Check if the subscription has expired.
     * Always returns false for free or lifetime subscriptions.
     * 
     * @return Single that emits true if expired, false otherwise
     */
    Single<Boolean> isSubscriptionExpired();
    
    /**
     * Check if the subscription is a lifetime subscription.
     * 
     * @return Single that emits true if lifetime, false otherwise
     */
    Single<Boolean> isLifetimeSubscription();
    
    /**
     * Get the subscription level (tier).
     * 
     * @return Single that emits the subscription level
     */
    Single<String> getSubscriptionLevel();
    
    /**
     * Upgrade the subscription to a pro plan.
     * 
     * @param durationMonths Duration in months (0 for lifetime)
     * @param paymentDetails Map containing payment details
     * @return Completable that completes when upgrade is finished
     */
    Completable upgradeSubscription(int durationMonths, Map<String, Object> paymentDetails);
    
    /**
     * Cancel the current subscription.
     * 
     * @param immediate If true, subscription ends immediately; if false, it ends at expiry
     * @return Completable that completes when cancellation is finished
     */
    Completable cancelSubscription(boolean immediate);
    
    /**
     * Get the subscription history.
     * 
     * @return Single that emits a list of subscription records
     */
    Single<java.util.List<Map<String, Object>>> getSubscriptionHistory();
}
