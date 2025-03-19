package com.autogratuity.ui.address;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.ui.common.BaseViewModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for the address functionality, implementing the repository pattern.
 * Handles data operations and business logic related to addresses.
 */
public class AddressViewModel extends BaseViewModel {
    private static final String TAG = "AddressViewModel";
    
    private final AddressRepository addressRepository;
    private final MutableLiveData<List<Address>> addressesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Address> selectedAddressLiveData = new MutableLiveData<>();
    
    /**
     * Constructor with repository injection
     * 
     * @param addressRepository Repository for address operations
     */
    public AddressViewModel(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }
    
    /**
     * Get addresses as LiveData for UI observation
     * 
     * @return LiveData containing a list of addresses
     */
    public LiveData<List<Address>> getAddresses() {
        return addressesLiveData;
    }
    
    /**
     * Get the selected address
     * 
     * @return LiveData containing the selected address
     */
    public LiveData<Address> getSelectedAddress() {
        return selectedAddressLiveData;
    }
    
    /**
     * Load all addresses
     * Fetches addresses from the repository and updates the LiveData
     */
    public void loadAddresses() {
        setLoading(true);
        clearError();
        
        addDisposable("loadAddresses",
            addressRepository.getAddresses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    addresses -> {
                        addressesLiveData.setValue(addresses);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading addresses", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Set up real-time observation of addresses
     * Updates LiveData whenever changes occur in the repository
     */
    public void observeAddresses() {
        addDisposable("observeAddresses",
            addressRepository.observeAddresses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    addresses -> {
                        addressesLiveData.setValue(addresses);
                    },
                    error -> {
                        Log.e(TAG, "Error observing addresses", error);
                        setError(error);
                    }
                )
        );
    }
    
    /**
     * Get favorite addresses
     */
    public void loadFavoriteAddresses() {
        setLoading(true);
        clearError();
        
        disposables.add(
            addressRepository.getFavoriteAddresses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    addresses -> {
                        addressesLiveData.setValue(addresses);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading favorite addresses", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Get recently used addresses
     * 
     * @param limit Maximum number of addresses to return
     */
    public void loadRecentlyUsedAddresses(int limit) {
        setLoading(true);
        clearError();
        
        disposables.add(
            addressRepository.getRecentlyUsedAddresses(limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    addresses -> {
                        addressesLiveData.setValue(addresses);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading recent addresses", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Set the selected address
     * 
     * @param address Address to select
     */
    public void selectAddress(Address address) {
        selectedAddressLiveData.setValue(address);
    }
    
    /**
     * Get address by ID
     * 
     * @param addressId ID of the address to retrieve
     */
    public void getAddressById(String addressId) {
        setLoading(true);
        clearError();
        
        disposables.add(
            addressRepository.getAddressById(addressId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    address -> {
                        selectedAddressLiveData.setValue(address);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading address by ID", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Add a new address
     * 
     * @param address Address to add
     */
    public void addAddress(Address address) {
        setLoading(true);
        clearError();
        
        addDisposable("addAddress",
            addressRepository.addAddress(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    documentReference -> {
                        setLoading(false);
                        showToast("Address added successfully");
                        loadAddresses(); // Refresh address list
                    },
                    error -> {
                        Log.e(TAG, "Error adding address", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Update an address
     * 
     * @param address Address to update
     */
    public void updateAddress(Address address) {
        setLoading(true);
        clearError();
        
        disposables.add(
            addressRepository.updateAddress(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        setLoading(false);
                        showToast("Address updated successfully");
                    },
                    error -> {
                        Log.e(TAG, "Error updating address", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Delete an address
     * 
     * @param addressId ID of the address to delete
     */
    public void deleteAddress(String addressId) {
        setLoading(true);
        clearError();
        
        disposables.add(
            addressRepository.deleteAddress(addressId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        setLoading(false);
                        showToast("Address deleted successfully");
                        loadAddresses(); // Refresh address list
                    },
                    error -> {
                        Log.e(TAG, "Error deleting address", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Toggle favorite status for an address
     * 
     * @param addressId ID of the address
     * @param isFavorite Whether to mark as favorite
     */
    public void setAddressFavorite(String addressId, boolean isFavorite) {
        disposables.add(
            addressRepository.setAddressFavorite(addressId, isFavorite)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        showToast(isFavorite ? "Added to favorites" : "Removed from favorites");
                    },
                    error -> {
                        Log.e(TAG, "Error toggling favorite", error);
                        setError(error);
                    }
                )
        );
    }
    
    /**
     * Search addresses by query
     * 
     * @param query Search query
     */
    public void searchAddresses(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadAddresses(); // Load all addresses if query is empty
            return;
        }
        
        setLoading(true);
        clearError();
        
        disposables.add(
            addressRepository.searchAddresses(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    addresses -> {
                        addressesLiveData.setValue(addresses);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error searching addresses", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Get addresses near a location
     * 
     * @param latitude Latitude of the center point
     * @param longitude Longitude of the center point
     * @param radiusKm Radius in kilometers
     */
    public void getAddressesNearLocation(double latitude, double longitude, double radiusKm) {
        setLoading(true);
        clearError();
        
        disposables.add(
            addressRepository.getAddressesNearLocation(latitude, longitude, radiusKm)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    addresses -> {
                        addressesLiveData.setValue(addresses);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error finding nearby addresses", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
}
