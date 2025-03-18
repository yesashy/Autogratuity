package com.autogratuity.utils;

import android.util.Log;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.data.repository.address.AddressRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * System for validating delivery data before saving to repositories
 * Updated to use domain repositories with RxJava
 */
public class DataValidationSystem {
    private static final String TAG = "DataValidationSystem";
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * Create a new DataValidationSystem with domain repositories
     *
     * @param deliveryRepository The repository for delivery operations
     * @param addressRepository The repository for address operations
     */
    public DataValidationSystem(DeliveryRepository deliveryRepository, AddressRepository addressRepository) {
        this.deliveryRepository = deliveryRepository;
        this.addressRepository = addressRepository;
    }

    /**
     * Validate a list of deliveries
     *
     * @param deliveries List of deliveries to validate
     * @param skipDuplicates Whether to skip duplicate records
     * @param updateExisting Whether to update existing records
     * @return ValidationResult with statistics and validated deliveries
     */
    public ValidationResult validateDeliveries(List<Delivery> deliveries, boolean skipDuplicates, boolean updateExisting) {
        ValidationResult result = new ValidationResult();
        List<Delivery> validatedDeliveries = new ArrayList<>();

        for (Delivery delivery : deliveries) {
            try {
                // Validate address
                if (delivery.getAddress() == null || 
                    delivery.getAddress().getLocation() == null || 
                    delivery.getAddress().getFullAddress() == null || 
                    delivery.getAddress().getFullAddress().isEmpty()) {
                    
                    Log.w(TAG, "Invalid delivery - missing address data");
                    result.addInvalid();
                    result.addWarning("Invalid delivery - missing address data");
                    continue;
                }

                // Check for duplicates if needed
                if (skipDuplicates || updateExisting) {
                    boolean isDuplicate = checkForDuplicateDelivery(delivery);
                    
                    if (isDuplicate) {
                        if (skipDuplicates) {
                            Log.i(TAG, "Skipping duplicate delivery: " + delivery.getAddress().getFullAddress());
                            result.addDuplicate();
                            continue;
                        } else if (updateExisting) {
                            Log.i(TAG, "Updating existing delivery: " + delivery.getAddress().getFullAddress());
                            result.addUpdated();
                        }
                    } else {
                        result.addNew();
                    }
                } else {
                    result.addNew();
                }

                // Add to valid deliveries list
                validatedDeliveries.add(delivery);
            } catch (Exception e) {
                Log.e(TAG, "Error validating delivery: " + e.getMessage());
                result.addInvalid();
                result.addWarning("Error validating delivery: " + e.getMessage());
            }
        }

        result.setValidatedDeliveries(validatedDeliveries);
        return result;
    }

    /**
     * Check if delivery is a duplicate - public method that can be used from ImportManager
     * 
     * @param delivery The delivery to check
     * @return true if it's a duplicate, false otherwise
     */
    public boolean checkForDuplicateDelivery(Delivery delivery) {
        try {
            // Try to find a delivery with the same address
            if (delivery.getAddress() != null && 
                delivery.getAddress().getFullAddress() != null) {
                
                String normalizedAddress = addressRepository.normalizeAddress(
                        delivery.getAddress().getFullAddress());
                
                Address existingAddress = addressRepository.findAddressByNormalizedAddress(normalizedAddress)
                        .blockingGet();
                
                if (existingAddress != null) {
                    // Check if there's a delivery with this address
                    List<Delivery> existingDeliveries = deliveryRepository.getDeliveriesByAddress(
                            existingAddress.getAddressId())
                            .blockingGet();
                    
                    return !existingDeliveries.isEmpty();
                }
            }
            
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking for duplicate delivery: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Clean up resources when no longer needed
     */
    public void dispose() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    /**
     * Class to hold validation results
     */
    public static class ValidationResult {
        private int newCount = 0;
        private int updatedCount = 0;
        private int duplicateCount = 0;
        private int invalidCount = 0;
        private List<String> warnings = new ArrayList<>();
        private List<Delivery> validatedDeliveries = new ArrayList<>();

        // Getters
        public int getNewCount() {
            return newCount;
        }

        public int getUpdatedCount() {
            return updatedCount;
        }

        public int getDuplicateCount() {
            return duplicateCount;
        }

        public int getInvalidCount() {
            return invalidCount;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public List<Delivery> getValidatedDeliveries() {
            return validatedDeliveries;
        }

        // Adders and setters
        public void addNew() {
            newCount++;
        }

        public void addUpdated() {
            updatedCount++;
        }

        public void addDuplicate() {
            duplicateCount++;
        }

        public void addInvalid() {
            invalidCount++;
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public void setValidatedDeliveries(List<Delivery> deliveries) {
            this.validatedDeliveries = deliveries;
        }
    }
}