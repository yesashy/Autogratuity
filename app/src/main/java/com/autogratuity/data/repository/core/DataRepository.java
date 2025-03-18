package com.autogratuity.data.repository.core;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.AppConfig;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.DeliveryStats;
import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.model.SyncOperation;
import com.autogratuity.data.model.SyncStatus;
import com.autogratuity.data.model.UserProfile;
import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Primary interface for all data operations in the application.
 * This interface serves as the single entry point for accessing and manipulating
 * data across the application, implementing the repository pattern.
 * 
 * It provides methods for:
 * - User profile management
 * - Subscription management
 * - Address management
 * - Delivery tracking
 * - Application configuration
 * - Synchronization operations
 * - Device management
 * 
 * All methods return RxJava types to support asynchronous operations.
 */
public interface DataRepository {
    
    //-----------------------------------------------------------------------------------
    // User Profile Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Get the current user's profile.
     * Will return from cache if available, otherwise fetches from Firestore.
     * 
     * @return Single that emits the user profile
     */
    Single<UserProfile> getUserProfile();
    
    /**
     * Get the current user's profile with option to force refresh from Firestore.
     * 
     * @param forceRefresh If true, bypasses cache and fetches from Firestore
     * @return Single that emits the user profile
     */
    Single<UserProfile> getUserProfile(boolean forceRefresh);
    
    /**
     * Update the entire user profile.
     * 
     * @param profile The updated profile
     * @return Completable that completes when update is finished
     */
    Completable updateUserProfile(UserProfile profile);
    
    /**
     * Update specific fields of the user profile.
     * 
     * @param fields Map of field names to values to update
     * @return Completable that completes when update is finished
     */
    Completable updateUserProfileFields(Map<String, Object> fields);
    
    /**
     * Observe changes to the user profile in real-time.
     * 
     * @return Observable that emits user profile updates
     */
    Observable<UserProfile> observeUserProfile();
    
    //-----------------------------------------------------------------------------------
    // Subscription Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Get the current user's subscription status.
     * 
     * @return Single that emits the subscription status
     */
    Single<SubscriptionStatus> getSubscriptionStatus();
    
    /**
     * Update the user's subscription status.
     * 
     * @param status The updated subscription status
     * @return Completable that completes when update is finished
     */
    Completable updateSubscriptionStatus(SubscriptionStatus status);
    
    /**
     * Add a new subscription record.
     * 
     * @param subscriptionRecord Map containing subscription details
     * @return Single that emits the document reference
     */
    Single<DocumentReference> addSubscriptionRecord(Map<String, Object> subscriptionRecord);
    
    /**
     * Observe changes to the subscription status in real-time.
     * 
     * @return Observable that emits subscription status updates
     */
    Observable<SubscriptionStatus> observeSubscriptionStatus();
    
    /**
     * Verify the current subscription with the payment provider.
     * 
     * @return Completable that completes when verification is finished
     */
    Completable verifySubscription();
    
    /**
     * Check if the current user has an active pro subscription.
     * 
     * @return Single that emits true if user has pro subscription, false otherwise
     */
    Single<Boolean> isProUser();
    
    //-----------------------------------------------------------------------------------
    // Address Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Get all addresses for the current user.
     * 
     * @return Single that emits a list of addresses
     */
    Single<List<Address>> getAddresses();
    
    /**
     * Get an address by its ID.
     * 
     * @param addressId The address ID
     * @return Single that emits the address
     */
    Single<Address> getAddressById(String addressId);
    
    /**
     * Find an address by its normalized form.
     * 
     * @param normalizedAddress The normalized address string
     * @return Single that emits the address if found, or null if not
     */
    Single<Address> findAddressByNormalizedAddress(String normalizedAddress);
    
    /**
     * Add a new address. Will check for duplicates using normalized address.
     * 
     * @param address The address to add
     * @return Single that emits the document reference
     */
    Single<DocumentReference> addAddress(Address address);
    
    /**
     * Update an existing address.
     * 
     * @param address The updated address
     * @return Completable that completes when update is finished
     */
    Completable updateAddress(Address address);
    
    /**
     * Delete an address.
     * 
     * @param addressId The address ID to delete
     * @return Completable that completes when deletion is finished
     */
    Completable deleteAddress(String addressId);
    
    /**
     * Observe changes to all addresses in real-time.
     * 
     * @return Observable that emits updates to the address list
     */
    Observable<List<Address>> observeAddresses();
    
    /**
     * Observe changes to a specific address in real-time.
     * 
     * @param addressId The address ID to observe
     * @return Observable that emits updates to the address
     */
    Observable<Address> observeAddress(String addressId);
    
    //-----------------------------------------------------------------------------------
    // Delivery Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Get deliveries with pagination.
     * 
     * @param limit Maximum number of deliveries to return
     * @param startAfter Document reference to start after (for pagination)
     * @return Single that emits a list of deliveries
     */
    Single<List<Delivery>> getDeliveries(int limit, DocumentReference startAfter);
    
    /**
     * Get deliveries within a specific time range.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Single that emits a list of deliveries
     */
    Single<List<Delivery>> getDeliveriesByTimeRange(Date startDate, Date endDate);
    
    /**
     * Get deliveries for a specific address.
     * 
     * @param addressId The address ID
     * @return Single that emits a list of deliveries
     */
    Single<List<Delivery>> getDeliveriesByAddress(String addressId);
    
    /**
     * Get a delivery by its ID.
     * 
     * @param deliveryId The delivery ID
     * @return Single that emits the delivery
     */
    Single<Delivery> getDeliveryById(String deliveryId);
    
    /**
     * Add a new delivery.
     * 
     * @param delivery The delivery to add
     * @return Single that emits the document reference
     */
    Single<DocumentReference> addDelivery(Delivery delivery);
    
    /**
     * Update an existing delivery.
     * 
     * @param delivery The updated delivery
     * @return Completable that completes when update is finished
     */
    Completable updateDelivery(Delivery delivery);
    
    /**
     * Update just the tip amount for a delivery.
     * 
     * @param deliveryId The delivery ID
     * @param tipAmount The new tip amount
     * @return Completable that completes when update is finished
     */
    Completable updateDeliveryTip(String deliveryId, double tipAmount);
    
    /**
     * Delete a delivery.
     * 
     * @param deliveryId The delivery ID to delete
     * @return Completable that completes when deletion is finished
     */
    Completable deleteDelivery(String deliveryId);
    
    /**
     * Observe changes to deliveries in real-time.
     * 
     * @return Observable that emits updates to the delivery list
     */
    Observable<List<Delivery>> observeDeliveries();
    
    /**
     * Observe changes to a specific delivery in real-time.
     * 
     * @param deliveryId The delivery ID to observe
     * @return Observable that emits updates to the delivery
     */
    Observable<Delivery> observeDelivery(String deliveryId);
    
    /**
     * Get delivery statistics for different time periods.
     * 
     * @return Single that emits a map of time period keys to delivery statistics
     */
    Single<Map<String, DeliveryStats>> getDeliveryStats();
    
    //-----------------------------------------------------------------------------------
    // Sync Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Synchronize all data between local storage and Firestore.
     * 
     * @return Completable that completes when sync is finished
     */
    Completable syncData();
    
    /**
     * Get the current sync status.
     * 
     * @return Single that emits the sync status
     */
    Single<SyncStatus> getSyncStatus();
    
    /**
     * Enqueue a sync operation for processing.
     * Will be processed immediately if online, or queued for later if offline.
     * 
     * @param operation The sync operation to enqueue
     * @return Completable that completes when enqueuing is finished
     */
    Completable enqueueSyncOperation(SyncOperation operation);
    
    /**
     * Get all pending sync operations.
     * 
     * @return Single that emits a list of pending operations
     */
    Single<List<SyncOperation>> getPendingSyncOperations();
    
    /**
     * Process all pending sync operations.
     * 
     * @return Completable that completes when processing is finished
     */
    Completable processPendingSyncOperations();
    
    /**
     * Update the sync status for a device.
     * 
     * @param deviceId The device ID
     * @param syncStatus The updated sync status
     * @return Completable that completes when update is finished
     */
    Completable updateDeviceSyncStatus(String deviceId, SyncStatus syncStatus);
    
    /**
     * Observe changes to the sync status in real-time.
     * 
     * @return Observable that emits sync status updates
     */
    Observable<SyncStatus> observeSyncStatus();
    
    //-----------------------------------------------------------------------------------
    // Config Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Get the application configuration.
     * 
     * @return Single that emits the app configuration
     */
    Single<AppConfig> getAppConfig();
    
    /**
     * Observe changes to the app configuration in real-time.
     * 
     * @return Observable that emits app configuration updates
     */
    Observable<AppConfig> observeAppConfig();
    
    //-----------------------------------------------------------------------------------
    // Device Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Register the current device with the system.
     * 
     * @param deviceInfo Map containing device information
     * @return Completable that completes when registration is finished
     */
    Completable registerDevice(Map<String, Object> deviceInfo);
    
    /**
     * Update the last active timestamp for the current device.
     * 
     * @return Completable that completes when update is finished
     */
    Completable updateDeviceLastActive();
    
    //-----------------------------------------------------------------------------------
    // Cache Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Clear all in-memory caches.
     * 
     * @return Completable that completes when caches are cleared
     */
    Completable clearCaches();
    
    /**
     * Prefetch critical data for improved performance.
     * 
     * @return Completable that completes when prefetching is finished
     */
    Completable prefetchCriticalData();
}