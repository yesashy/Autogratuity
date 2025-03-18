package com.autogratuity.ui.dialog;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.ui.common.BaseViewModel;

import java.util.Date;
import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel specifically designed for Delivery Dialog operations.
 * Handles the interactions between dialogs and repositories, separating UI logic from data operations.
 */
public class DeliveryDialogViewModel extends BaseViewModel {
    private static final String TAG = "DeliveryDialogViewModel";
    
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    
    private final MutableLiveData<Delivery> deliveryLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccessLiveData = new MutableLiveData<>();
    
    /**
     * Constructor with repository injection
     * 
     * @param deliveryRepository Repository for delivery operations
     * @param addressRepository Repository for address operations
     */
    public DeliveryDialogViewModel(DeliveryRepository deliveryRepository, AddressRepository addressRepository) {
        this.deliveryRepository = deliveryRepository;
        this.addressRepository = addressRepository;
    }
    
    /**
     * Get delivery LiveData for UI observation
     * 
     * @return LiveData containing the current delivery
     */
    public LiveData<Delivery> getDelivery() {
        return deliveryLiveData;
    }
    
    /**
     * Get operation success LiveData
     * 
     * @return LiveData indicating if the operation was successful
     */
    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccessLiveData;
    }
    
    /**
     * Get delivery by ID
     * 
     * @param deliveryId ID of the delivery to retrieve
     */
    public void loadDelivery(String deliveryId) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.getDeliveryById(deliveryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    delivery -> {
                        deliveryLiveData.setValue(delivery);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading delivery", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Update delivery tip amount
     * 
     * @param deliveryId ID of the delivery to update
     * @param tipAmount New tip amount
     */
    public void updateTipAmount(String deliveryId, double tipAmount) {
        setLoading(true);
        clearError();
        
        disposables.add(
            deliveryRepository.updateDeliveryTip(deliveryId, tipAmount)
                .andThen(deliveryRepository.getDeliveryById(deliveryId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    updatedDelivery -> {
                        deliveryLiveData.setValue(updatedDelivery);
                        operationSuccessLiveData.setValue(true);
                        showToast("Tip amount updated successfully");
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error updating tip amount", error);
                        operationSuccessLiveData.setValue(false);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Delete a delivery
     * 
     * @param deliveryId ID of the delivery to delete
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
                        operationSuccessLiveData.setValue(true);
                        showToast("Delivery deleted successfully");
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error deleting delivery", error);
                        operationSuccessLiveData.setValue(false);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Find an address by normalized address string or create a new one if not found.
     * Then create a delivery with that address.
     * 
     * @param orderId Order ID for the delivery
     * @param addressText Full address text
     * @param tipAmount Tip amount for the delivery
     */
    public void createDeliveryWithAddress(String orderId, String addressText, double tipAmount) {
        setLoading(true);
        clearError();
        
        // Normalize the address for consistent lookup
        String normalizedAddress = addressRepository.normalizeAddress(addressText);
        
        disposables.add(
            addressRepository.findAddressByNormalizedAddress(normalizedAddress)
                .flatMap(existingAddress -> {
                    if (existingAddress != null) {
                        // Use existing address
                        return Single.just(existingAddress);
                    } else {
                        // Create new address
                        Address newAddress = createAddressObject(addressText, normalizedAddress);
                        
                        // Save the new address
                        return addressRepository.addAddress(newAddress)
                                .flatMap(docRef -> addressRepository.getAddressById(docRef.getId()));
                    }
                })
                .flatMap(address -> {
                    // Create delivery with the address
                    Delivery delivery = createDeliveryObject(orderId, address, tipAmount);
                    
                    // Save the delivery
                    return deliveryRepository.addDelivery(delivery)
                            .flatMap(docRef -> deliveryRepository.getDeliveryById(docRef.getId()));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    delivery -> {
                        deliveryLiveData.setValue(delivery);
                        operationSuccessLiveData.setValue(true);
                        showToast("Delivery added successfully");
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error creating delivery with address", error);
                        operationSuccessLiveData.setValue(false);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Create a new address object
     * 
     * @param fullAddress Full address text
     * @param normalizedAddress Normalized address for consistent lookup
     * @return New Address object
     */
    private Address createAddressObject(String fullAddress, String normalizedAddress) {
        Address address = new Address();
        address.setFullAddress(fullAddress);
        address.setNormalizedAddress(normalizedAddress);
        
        // Set up address components if needed
        Address.Components components = new Address.Components();
        // Parse components from the address text
        // This would be more sophisticated in a real implementation
        address.setComponents(components);
        
        // Create address metadata
        Address.Metadata metadata = new Address.Metadata();
        metadata.setCreatedAt(new Date());
        metadata.setUpdatedAt(new Date());
        metadata.setVersion(1);
        address.setMetadata(metadata);
        
        return address;
    }
    
    /**
     * Create a new delivery object
     * 
     * @param orderId Order ID (can be null or empty for auto-generated ID)
     * @param address Associated address
     * @param tipAmount Tip amount
     * @return New Delivery object
     */
    private Delivery createDeliveryObject(String orderId, Address address, double tipAmount) {
        Delivery delivery = new Delivery();
        
        // Set basic fields
        if (orderId == null || orderId.trim().isEmpty()) {
            // Generate a random order ID if not provided
            orderId = "AUTO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        delivery.setOrderId(orderId);
        
        // Set address reference
        Delivery.Reference reference = new Delivery.Reference();
        reference.setAddressId(address.getAddressId());
        delivery.setReference(reference);
        
        // Set SimpleAddress for the delivery
        Address.SimpleAddress simpleAddress = address.toSimpleAddress();
        delivery.setAddress(simpleAddress);
        
        // Set status
        Delivery.Status status = new Delivery.Status();
        status.setCompleted(true); // Mark as completed since we're adding it after the fact
        status.setTipped(tipAmount > 0); // Mark as tipped if tip amount is positive
        status.setState("completed");
        delivery.setStatus(status);
        
        // Set amounts
        Delivery.Amounts amounts = new Delivery.Amounts();
        amounts.setTipAmount(tipAmount);
        // Other amount fields would be set here in a real implementation
        delivery.setAmounts(amounts);
        
        // Set timestamps
        Delivery.Times times = new Delivery.Times();
        Date now = new Date();
        times.setOrderedAt(now);
        times.setAcceptedAt(now);
        times.setCompletedAt(now);
        if (tipAmount > 0) {
            times.setTippedAt(now);
        }
        delivery.setTimes(times);
        
        // Set metadata
        Delivery.Metadata metadata = new Delivery.Metadata();
        metadata.setCreatedAt(now);
        metadata.setUpdatedAt(now);
        metadata.setVersion(1);
        delivery.setMetadata(metadata);
        
        return delivery;
    }
}
