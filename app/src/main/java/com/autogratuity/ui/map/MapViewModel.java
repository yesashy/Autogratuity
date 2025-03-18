package com.autogratuity.ui.map;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.ui.common.BaseViewModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for MapFragment
 * Manages data for map markers and operations
 */
public class MapViewModel extends BaseViewModel {
    
    private final AddressRepository addressRepository;
    private final DeliveryRepository deliveryRepository;
    
    // LiveData for addresses and deliveries
    private final MutableLiveData<List<Address>> addressesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Delivery>> deliveriesLiveData = new MutableLiveData<>();
    
    // Map state
    private final MutableLiveData<Boolean> showingRecentDeliveriesLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<LatLng> mapCenterLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> mapZoomLiveData = new MutableLiveData<>(14.0f);
    
    /**
     * Constructor - initializes repositories
     */
    public MapViewModel() {
        // Initialize repositories from RepositoryProvider
        addressRepository = RepositoryProvider.getAddressRepository();
        deliveryRepository = RepositoryProvider.getDeliveryRepository();
    }
    
    /**
     * Load all addresses
     * 
     * @param limit Maximum number of addresses to load
     */
    public void loadAddresses(int limit) {
        disposables.add(
            addressRepository.getAddresses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> setLoading(true))
                .doAfterTerminate(() -> setLoading(false))
                .subscribe(
                    addresses -> {
                        // Limit the number of addresses if needed
                        if (addresses.size() > limit) {
                            addressesLiveData.setValue(addresses.subList(0, limit));
                        } else {
                            addressesLiveData.setValue(addresses);
                        }
                        showingRecentDeliveriesLiveData.setValue(false);
                        showToast("Showing all locations");
                    },
                    error -> {
                        setError(error);
                        addressesLiveData.setValue(null);
                        showToast("Error loading addresses");
                    }
                )
        );
    }
    
    /**
     * Load recent deliveries
     * 
     * @param limit Maximum number of deliveries to load
     */
    public void loadRecentDeliveries(int limit) {
        disposables.add(
            deliveryRepository.getRecentDeliveries(limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> setLoading(true))
                .doAfterTerminate(() -> setLoading(false))
                .subscribe(
                    deliveries -> {
                        deliveriesLiveData.setValue(deliveries);
                        showingRecentDeliveriesLiveData.setValue(true);
                        showToast("Showing recent deliveries");
                    },
                    error -> {
                        setError(error);
                        deliveriesLiveData.setValue(null);
                        showToast("Error loading deliveries");
                    }
                )
        );
    }
    
    /**
     * Refresh the map data based on current view mode
     */
    public void refreshMapData() {
        Boolean showingRecentDeliveries = showingRecentDeliveriesLiveData.getValue();
        if (showingRecentDeliveries != null && showingRecentDeliveries) {
            loadRecentDeliveries(50);
        } else {
            loadAddresses(100);
        }
    }
    
    /**
     * Set map center position
     * 
     * @param latLng Center position
     */
    public void setMapCenter(LatLng latLng) {
        mapCenterLiveData.setValue(latLng);
    }
    
    /**
     * Set map zoom level
     * 
     * @param zoom Zoom level
     */
    public void setMapZoom(float zoom) {
        mapZoomLiveData.setValue(zoom);
    }
    
    /**
     * Get addresses as LiveData
     */
    public LiveData<List<Address>> getAddresses() {
        return addressesLiveData;
    }
    
    /**
     * Get deliveries as LiveData
     */
    public LiveData<List<Delivery>> getDeliveries() {
        return deliveriesLiveData;
    }
    
    /**
     * Get showing recent deliveries flag as LiveData
     */
    public LiveData<Boolean> isShowingRecentDeliveries() {
        return showingRecentDeliveriesLiveData;
    }
    
    /**
     * Get map center as LiveData
     */
    public LiveData<LatLng> getMapCenter() {
        return mapCenterLiveData;
    }
    
    /**
     * Get map zoom as LiveData
     */
    public LiveData<Float> getMapZoom() {
        return mapZoomLiveData;
    }
}
