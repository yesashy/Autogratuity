package com.autogratuity.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.DeliveryStats;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.ui.common.BaseViewModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for DashboardFragment
 * Manages data for statistics, recent activity, and best tipping areas
 */
public class DashboardViewModel extends BaseViewModel {
    
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    
    // LiveData for stats
    private final MutableLiveData<DeliveryStats> todayStatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<DeliveryStats> weekStatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<DeliveryStats> monthStatsLiveData = new MutableLiveData<>();
    
    // LiveData for recent activity
    private final MutableLiveData<List<Delivery>> recentDeliveriesLiveData = new MutableLiveData<>();
    
    // LiveData for best tipping areas
    private final MutableLiveData<List<Address>> bestTippingAreasLiveData = new MutableLiveData<>();
    
    /**
     * Constructor - initializes repositories and loads data
     */
    public DashboardViewModel() {
        // Initialize repositories from RepositoryProvider
        deliveryRepository = RepositoryProvider.getDeliveryRepository();
        addressRepository = RepositoryProvider.getAddressRepository();
        
        // Load initial data
        loadData();
    }
    
    /**
     * Load all data for the dashboard
     */
    public void loadData() {
        loadTodayStats();
        loadWeekStats();
        loadMonthStats();
        loadRecentActivity();
        loadBestTippingAreas();
    }
    
    /**
     * Load today's statistics
     */
    private void loadTodayStats() {
        disposables.add(
            deliveryRepository.getTodayStats()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> setLoading(true))
                .doAfterTerminate(() -> setLoading(false))
                .subscribe(
                    stats -> todayStatsLiveData.setValue(stats),
                    error -> {
                        setError(error);
                        // Set empty stats on error
                        todayStatsLiveData.setValue(new DeliveryStats());
                    }
                )
        );
    }
    
    /**
     * Load week statistics
     */
    private void loadWeekStats() {
        disposables.add(
            deliveryRepository.getLastWeekStats()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    stats -> weekStatsLiveData.setValue(stats),
                    error -> {
                        setError(error);
                        // Set empty stats on error
                        weekStatsLiveData.setValue(new DeliveryStats());
                    }
                )
        );
    }
    
    /**
     * Load month statistics
     */
    private void loadMonthStats() {
        disposables.add(
            deliveryRepository.getLastMonthStats()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    stats -> monthStatsLiveData.setValue(stats),
                    error -> {
                        setError(error);
                        // Set empty stats on error
                        monthStatsLiveData.setValue(new DeliveryStats());
                    }
                )
        );
    }
    
    /**
     * Load recent activity
     */
    private void loadRecentActivity() {
        disposables.add(
            deliveryRepository.getRecentDeliveries(5)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> recentDeliveriesLiveData.setValue(deliveries),
                    error -> {
                        setError(error);
                        // Set empty list on error
                        recentDeliveriesLiveData.setValue(null);
                    }
                )
        );
    }
    
    /**
     * Load best tipping areas
     */
    private void loadBestTippingAreas() {
        disposables.add(
            addressRepository.getBestTippingAddresses(5)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    addresses -> bestTippingAreasLiveData.setValue(addresses),
                    error -> {
                        setError(error);
                        // Set empty list on error
                        bestTippingAreasLiveData.setValue(null);
                    }
                )
        );
    }
    
    /**
     * Get today's stats as LiveData
     */
    public LiveData<DeliveryStats> getTodayStats() {
        return todayStatsLiveData;
    }
    
    /**
     * Get week stats as LiveData
     */
    public LiveData<DeliveryStats> getWeekStats() {
        return weekStatsLiveData;
    }
    
    /**
     * Get month stats as LiveData
     */
    public LiveData<DeliveryStats> getMonthStats() {
        return monthStatsLiveData;
    }
    
    /**
     * Get recent deliveries as LiveData
     */
    public LiveData<List<Delivery>> getRecentDeliveries() {
        return recentDeliveriesLiveData;
    }
    
    /**
     * Get best tipping areas as LiveData
     */
    public LiveData<List<Address>> getBestTippingAreas() {
        return bestTippingAreasLiveData;
    }
    
    /**
     * Refresh all data
     */
    public void refreshData() {
        loadData();
    }
}
