package com.autogratuity.ui.delivery;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.DeliveryStats;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.ui.common.BaseViewModel;
import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for the delivery functionality, implementing the repository pattern.
 * Handles data operations and business logic related to deliveries.
 */
public class DeliveryViewModel extends BaseViewModel {
    private static final String TAG = "DeliveryViewModel";
    
    private final DeliveryRepository deliveryRepository;
    private final MutableLiveData<List<Delivery>> deliveriesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Delivery> selectedDeliveryLiveData = new MutableLiveData<>();
    private final MutableLiveData<DeliveryStats> deliveryStatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, DeliveryStats>> deliveryStatsByPeriodLiveData = new MutableLiveData<>();
    
    /**
     * Constructor with repository injection
     * 
     * @param deliveryRepository Repository for delivery operations
     */
    public DeliveryViewModel(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }
    
    /**
     * Get deliveries as LiveData for UI observation
     * 
     * @return LiveData containing a list of deliveries
     */
    public LiveData<List<Delivery>> getDeliveries() {
        return deliveriesLiveData;
    }
    
    /**
     * Get the selected delivery
     * 
     * @return LiveData containing the selected delivery
     */
    public LiveData<Delivery> getSelectedDelivery() {
        return selectedDeliveryLiveData;
    }
    
    /**
     * Get delivery stats as LiveData
     * 
     * @return LiveData containing delivery stats
     */
    public LiveData<DeliveryStats> getDeliveryStats() {
        return deliveryStatsLiveData;
    }
    
    /**
     * Get delivery stats by period as LiveData
     * 
     * @return LiveData containing delivery stats by period
     */
    public LiveData<Map<String, DeliveryStats>> getDeliveryStatsByPeriod() {
        return deliveryStatsByPeriodLiveData;
    }
    
    /**
     * Load deliveries with pagination
     * 
     * @param limit Maximum number of deliveries to return
     * @param startAfter Document reference to start after (for pagination)
     */
    public void loadDeliveries(int limit, DocumentReference startAfter) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getDeliveries(limit, startAfter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading deliveries", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Load deliveries with default limit (50)
     */
    public void loadDeliveries() {
        loadDeliveries(50, null);
    }
    
    /**
     * Set up real-time observation of deliveries
     * Updates LiveData whenever changes occur in the repository
     */
    public void observeDeliveries() {
        disposables.add(
            deliveryRepository.observeDeliveries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                    },
                    error -> {
                        Log.e(TAG, "Error observing deliveries", error);
                        setError(error);
                    }
                )
        );
    }
    
    /**
     * Get a delivery by its ID
     * 
     * @param deliveryId The delivery ID
     */
    public void getDeliveryById(String deliveryId) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getDeliveryById(deliveryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    delivery -> {
                        selectedDeliveryLiveData.setValue(delivery);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading delivery by ID", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Get deliveries for a specific address
     * 
     * @param addressId The address ID
     */
    public void getDeliveriesByAddress(String addressId) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getDeliveriesByAddress(addressId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading deliveries by address", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Get deliveries within a date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     */
    public void getDeliveriesByTimeRange(Date startDate, Date endDate) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getDeliveriesByTimeRange(startDate, endDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading deliveries by time range", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Get recent deliveries
     * 
     * @param count Maximum number of recent deliveries to return
     */
    public void getRecentDeliveries(int count) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getRecentDeliveries(count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading recent deliveries", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Get today's deliveries
     */
    public void getTodaysDeliveries() {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getTodaysDeliveries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading today's deliveries", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Get deliveries from the last week
     */
    public void getLastWeekDeliveries() {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getLastWeekDeliveries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading last week's deliveries", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Get deliveries from the last month
     */
    public void getLastMonthDeliveries() {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getLastMonthDeliveries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading last month's deliveries", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Get deliveries that have tips
     */
    public void getTippedDeliveries() {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getTippedDeliveries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading tipped deliveries", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Get deliveries that don't have tips
     */
    public void getUntippedDeliveries() {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getUntippedDeliveries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading untipped deliveries", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Add a new delivery
     * 
     * @param delivery The delivery to add
     */
    public void addDelivery(Delivery delivery) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.addDelivery(delivery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    documentReference -> {
                        setLoading(false);
                        showToast("Delivery added successfully");
                        loadDeliveries(); // Refresh delivery list
                    },
                    error -> {
                        Log.e(TAG, "Error adding delivery", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Update an existing delivery
     * 
     * @param delivery The updated delivery
     */
    public void updateDelivery(Delivery delivery) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.updateDelivery(delivery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        setLoading(false);
                        showToast("Delivery updated successfully");
                    },
                    error -> {
                        Log.e(TAG, "Error updating delivery", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Update just the tip amount for a delivery
     * 
     * @param deliveryId The delivery ID
     * @param tipAmount The new tip amount
     */
    public void updateDeliveryTip(String deliveryId, double tipAmount) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.updateDeliveryTip(deliveryId, tipAmount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        setLoading(false);
                        showToast("Tip updated successfully");
                        // Refresh the selected delivery if it matches the updated one
                        if (selectedDeliveryLiveData.getValue() != null &&
                            selectedDeliveryLiveData.getValue().getId().equals(deliveryId)) {
                            getDeliveryById(deliveryId);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error updating tip", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Delete a delivery
     * 
     * @param deliveryId The delivery ID to delete
     */
    public void deleteDelivery(String deliveryId) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.deleteDelivery(deliveryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        setLoading(false);
                        showToast("Delivery deleted successfully");
                        loadDeliveries(); // Refresh delivery list
                    },
                    error -> {
                        Log.e(TAG, "Error deleting delivery", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Set the selected delivery
     * 
     * @param delivery Delivery to select
     */
    public void selectDelivery(Delivery delivery) {
        selectedDeliveryLiveData.setValue(delivery);
    }
    
    /**
     * Mark a delivery as completed
     * 
     * @param deliveryId The delivery ID
     * @param completionTime The completion time (null for current time)
     */
    public void markDeliveryCompleted(String deliveryId, Date completionTime) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.markDeliveryCompleted(deliveryId, completionTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        setLoading(false);
                        showToast("Delivery marked as completed");
                        // Refresh the selected delivery if it matches the updated one
                        if (selectedDeliveryLiveData.getValue() != null &&
                            selectedDeliveryLiveData.getValue().getId().equals(deliveryId)) {
                            getDeliveryById(deliveryId);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error marking delivery as completed", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Load delivery statistics for different time periods
     */
    public void loadDeliveryStats() {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getDeliveryStats()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    stats -> {
                        deliveryStatsByPeriodLiveData.setValue(stats);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading delivery stats", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Load today's delivery statistics
     */
    public void loadTodayStats() {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getTodayStats()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    stats -> {
                        deliveryStatsLiveData.setValue(stats);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading today's stats", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Load delivery statistics for the last 7 days
     */
    public void loadLastWeekStats() {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getLastWeekStats()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    stats -> {
                        deliveryStatsLiveData.setValue(stats);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading last week's stats", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Load delivery statistics for the last 30 days
     */
    public void loadLastMonthStats() {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getLastMonthStats()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    stats -> {
                        deliveryStatsLiveData.setValue(stats);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading last month's stats", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
}
