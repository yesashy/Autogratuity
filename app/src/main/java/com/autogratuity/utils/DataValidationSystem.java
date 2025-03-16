package com.autogratuity.utils;

import android.util.Log;

import com.autogratuity.models.Address;
import com.autogratuity.models.DeliveryData;
import com.autogratuity.repositories.FirestoreRepository;
import com.autogratuity.repositories.IFirestoreRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * System for validating delivery data before saving to Firestore
 */
public class DataValidationSystem {
    private static final String TAG = "DataValidationSystem";
    private final IFirestoreRepository repository;

    /**
     * Create a new DataValidationSystem with a Firestore repository
     *
     * @param repository The repository to use for checking duplicates
     */
    public DataValidationSystem(IFirestoreRepository repository) {
        this.repository = repository;
    }

    /**
     * Validate a list of deliveries
     *
     * @param deliveries List of deliveries to validate
     * @param skipDuplicates Whether to skip duplicate records
     * @param updateExisting Whether to update existing records
     * @return ValidationResult with statistics and validated deliveries
     */
    public ValidationResult validateDeliveries(List<DeliveryData> deliveries, boolean skipDuplicates, boolean updateExisting) {
        ValidationResult result = new ValidationResult();
        List<DeliveryData> validatedDeliveries = new ArrayList<>();

        for (DeliveryData delivery : deliveries) {
            try {
                // Validate address
                if (delivery.getAddress() == null || 
                    delivery.getAddress().getCoordinates() == null || 
                    delivery.getAddress().getFullAddress() == null || 
                    delivery.getAddress().getFullAddress().isEmpty()) {
                    
                    Log.w(TAG, "Invalid delivery - missing address data");
                    result.addInvalid();
                    result.addWarning("Invalid delivery - missing address data");
                    continue;
                }

                // Check for duplicates if needed - use direct repository cast for access to implementation
                if (skipDuplicates || updateExisting) {
                    // Use repository method to check for duplicates if available
                    boolean isDuplicate = false;
                    if (repository instanceof FirestoreRepository) {
                        isDuplicate = ((FirestoreRepository) repository).checkForDuplicateDelivery(delivery);
                    }
                    
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
    public boolean checkForDuplicateDelivery(DeliveryData delivery) {
        if (repository instanceof FirestoreRepository) {
            return ((FirestoreRepository) repository).checkForDuplicateDelivery(delivery);
        }
        return false;
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
        private List<DeliveryData> validatedDeliveries = new ArrayList<>();

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

        public List<DeliveryData> getValidatedDeliveries() {
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

        public void setValidatedDeliveries(List<DeliveryData> deliveries) {
            this.validatedDeliveries = deliveries;
        }
    }
}