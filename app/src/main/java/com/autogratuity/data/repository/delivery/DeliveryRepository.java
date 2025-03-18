package com.autogratuity.data.repository.delivery;

import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.DeliveryStats;
import com.autogratuity.data.repository.core.DataRepository;
import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Repository interface for managing delivery data.
 * Extends the core DataRepository for delivery-specific operations.
 */
public interface DeliveryRepository extends DataRepository {
    
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
    @Override
    Single<List<Delivery>> getDeliveries(int limit, DocumentReference startAfter);
    
    /**
     * Get deliveries within a specific time range.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Single that emits a list of deliveries
     */
    @Override
    Single<List<Delivery>> getDeliveriesByTimeRange(Date startDate, Date endDate);
    
    /**
     * Get deliveries for a specific address.
     * 
     * @param addressId The address ID
     * @return Single that emits a list of deliveries
     */
    @Override
    Single<List<Delivery>> getDeliveriesByAddress(String addressId);
    
    /**
     * Get a delivery by its ID.
     * 
     * @param deliveryId The delivery ID
     * @return Single that emits the delivery
     */
    @Override
    Single<Delivery> getDeliveryById(String deliveryId);
    
    /**
     * Add a new delivery.
     * 
     * @param delivery The delivery to add
     * @return Single that emits the document reference
     */
    @Override
    Single<DocumentReference> addDelivery(Delivery delivery);
    
    /**
     * Update an existing delivery.
     * 
     * @param delivery The updated delivery
     * @return Completable that completes when update is finished
     */
    @Override
    Completable updateDelivery(Delivery delivery);
    
    /**
     * Update just the tip amount for a delivery.
     * 
     * @param deliveryId The delivery ID
     * @param tipAmount The new tip amount
     * @return Completable that completes when update is finished
     */
    @Override
    Completable updateDeliveryTip(String deliveryId, double tipAmount);
    
    /**
     * Delete a delivery.
     * 
     * @param deliveryId The delivery ID to delete
     * @return Completable that completes when deletion is finished
     */
    @Override
    Completable deleteDelivery(String deliveryId);
    
    /**
     * Observe changes to deliveries in real-time.
     * 
     * @return Observable that emits updates to the delivery list
     */
    @Override
    Observable<List<Delivery>> observeDeliveries();
    
    /**
     * Observe changes to a specific delivery in real-time.
     * 
     * @param deliveryId The delivery ID to observe
     * @return Observable that emits updates to the delivery
     */
    @Override
    Observable<Delivery> observeDelivery(String deliveryId);
    
    /**
     * Get delivery statistics for different time periods.
     * 
     * @return Single that emits a map of time period keys to delivery statistics
     */
    @Override
    Single<Map<String, DeliveryStats>> getDeliveryStats();
    
    //-----------------------------------------------------------------------------------
    // Convenience Methods
    //-----------------------------------------------------------------------------------
    
    /**
     * Get all deliveries (with a default limit).
     * 
     * @return Single that emits a list of deliveries
     */
    Single<List<Delivery>> getAllDeliveries();
    
    /**
     * Get recent deliveries (limited to a specific count).
     * 
     * @param count Maximum number of recent deliveries to return
     * @return Single that emits a list of recent deliveries
     */
    Single<List<Delivery>> getRecentDeliveries(int count);
    
    /**
     * Get today's deliveries.
     * 
     * @return Single that emits a list of today's deliveries
     */
    Single<List<Delivery>> getTodaysDeliveries();
    
    /**
     * Get deliveries from the last week.
     * 
     * @return Single that emits a list of deliveries from the last 7 days
     */
    Single<List<Delivery>> getLastWeekDeliveries();
    
    /**
     * Get deliveries from the last month.
     * 
     * @return Single that emits a list of deliveries from the last 30 days
     */
    Single<List<Delivery>> getLastMonthDeliveries();
    
    /**
     * Get deliveries that have tips.
     * 
     * @return Single that emits a list of deliveries with tips
     */
    Single<List<Delivery>> getTippedDeliveries();
    
    /**
     * Get deliveries that don't have tips.
     * 
     * @return Single that emits a list of deliveries without tips
     */
    Single<List<Delivery>> getUntippedDeliveries();
    
    /**
     * Mark a delivery as completed.
     * 
     * @param deliveryId The delivery ID
     * @param completionTime The completion time (null for current time)
     * @return Completable that completes when update is finished
     */
    Completable markDeliveryCompleted(String deliveryId, Date completionTime);
    
    /**
     * Get today's delivery statistics.
     * 
     * @return Single that emits delivery statistics for today
     */
    Single<DeliveryStats> getTodayStats();
    
    /**
     * Get delivery statistics for the last 7 days.
     * 
     * @return Single that emits delivery statistics for the last 7 days
     */
    Single<DeliveryStats> getLastWeekStats();
    
    /**
     * Get delivery statistics for the last 30 days.
     * 
     * @return Single that emits delivery statistics for the last 30 days
     */
    Single<DeliveryStats> getLastMonthStats();
    
    /**
     * Calculate statistics for the given list of deliveries.
     * 
     * @param deliveries List of deliveries to analyze
     * @return DeliveryStats containing calculated statistics
     */
    DeliveryStats calculateStats(List<Delivery> deliveries);
}