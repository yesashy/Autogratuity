package com.autogratuity.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.autogratuity.models.Address;
import com.autogratuity.models.Coordinates;
import com.autogratuity.models.DeliveryData;
import com.autogratuity.models.TipData;
import com.autogratuity.repositories.IFirestoreRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for importing GeoJSON data
 * Supports extracting location data from Google Maps exports
 */
public class GeoJsonImportUtil {
    private static final String TAG = "GeoJsonImportUtil";
    private final IFirestoreRepository firestoreRepository;
    private final boolean skipDuplicates;
    private final boolean updateExisting;
    
    // Pattern for extracting order IDs
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("(\\d{8,10})");
    
    // Stats for tracking import progress
    private int newCount = 0;
    private int updatedCount = 0;
    private int duplicateCount = 0;
    private int invalidCount = 0;
    private final List<String> warnings = new ArrayList<>();
    
    /**
     * Create a new GeoJsonImportUtil with a Firestore repository
     * 
     * @param firestoreRepository The repository to use for saving data
     */
    public GeoJsonImportUtil(IFirestoreRepository firestoreRepository) {
        this.firestoreRepository = firestoreRepository;
        this.skipDuplicates = false;
        this.updateExisting = true;
    }
    
    /**
     * Create a new GeoJsonImportUtil with a Firestore repository and validation options
     * 
     * @param firestoreRepository The repository to use for saving data
     * @param skipDuplicates Whether to skip duplicate records
     * @param updateExisting Whether to update existing records
     */
    public GeoJsonImportUtil(IFirestoreRepository firestoreRepository, boolean skipDuplicates, boolean updateExisting) {
        this.firestoreRepository = firestoreRepository;
        this.skipDuplicates = skipDuplicates;
        this.updateExisting = updateExisting;
    }
    
    /**
     * Import GeoJSON data from an input stream
     * 
     * @param context The application context
     * @param inputStream The input stream containing GeoJSON data
     * @return true if import was successful, false otherwise
     */
    public boolean importFromGeoJson(Context context, InputStream inputStream) {
        try {
            // Reset statistics
            newCount = 0;
            updatedCount = 0;
            duplicateCount = 0;
            invalidCount = 0;
            warnings.clear();
            
            // Parse the GeoJSON file
            JsonParser parser = new JsonParser();
            JsonObject geoJson = parser.parse(new InputStreamReader(inputStream)).getAsJsonObject();
            
            // Extract features array
            if (geoJson.has("features")) {
                final JsonArray features = geoJson.getAsJsonArray("features");
                processFeatures(features);
                return true;
            } else {
                Log.e(TAG, "Invalid GeoJSON format: missing 'features' array");
                warnings.add("Invalid GeoJSON format: missing 'features' array");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error importing GeoJSON: " + e.getMessage());
            warnings.add("Error importing GeoJSON: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Process features from GeoJSON data
     * 
     * @param features Array of GeoJSON features
     */
    private void processFeatures(JsonArray features) {
        // Create a final list to store all deliveries
        final List<DeliveryData> deliveries = new ArrayList<>();
        
        // Use AtomicInteger for counters that need to be modified in lambdas
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);
        
        // Process each feature outside of lambda to avoid "effectively final" issues
        for (JsonElement featureElement : features) {
            try {
                final JsonObject feature = featureElement.getAsJsonObject();
                final DeliveryData delivery = convertFeatureToDelivery(feature);
                
                if (delivery != null) {
                    deliveries.add(delivery);
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing feature: " + e.getMessage());
                warnings.add("Error processing feature: " + e.getMessage());
                failCount.incrementAndGet();
            }
        }
        
        // Validate deliveries before saving
        DataValidationSystem validationSystem = new DataValidationSystem(firestoreRepository);
        DataValidationSystem.ValidationResult validationResult = 
                validationSystem.validateDeliveries(deliveries, skipDuplicates, updateExisting);
        
        // Store validation statistics
        newCount = validationResult.getNewCount();
        updatedCount = validationResult.getUpdatedCount();
        duplicateCount = validationResult.getDuplicateCount();
        invalidCount = validationResult.getInvalidCount();
        warnings.addAll(validationResult.getWarnings());
        
        List<DeliveryData> validatedDeliveries = validationResult.getValidatedDeliveries();
        
        // Save all processed deliveries to Firestore
        if (!validatedDeliveries.isEmpty()) {
            saveDeliveriesToFirestore(validatedDeliveries);
        } else {
            Log.w(TAG, "No valid deliveries to save after validation");
            warnings.add("No valid deliveries to save after validation");
        }
        
        Log.i(TAG, String.format(
                "Import complete. New: %d, Updated: %d, Duplicates: %d, Invalid: %d, Total: %d",
                newCount, updatedCount, duplicateCount, invalidCount, 
                successCount.get() + failCount.get()));
    }
    
    /**
     * Convert a GeoJSON feature to a DeliveryData object
     * 
     * @param feature The GeoJSON feature
     * @return DeliveryData object, or null if conversion failed
     */
    private DeliveryData convertFeatureToDelivery(JsonObject feature) {
        try {
            // Extract properties
            final JsonObject properties = feature.getAsJsonObject("properties");
            final String name = properties.has("name") ? 
                    properties.get("name").getAsString() : "Unknown";
            final String description = properties.has("description") ? 
                    properties.get("description").getAsString() : "";
            
            // Extract geometry
            final JsonObject geometry = feature.getAsJsonObject("geometry");
            if (!geometry.has("type") || !geometry.get("type").getAsString().equals("Point")) {
                Log.w(TAG, "Skipping non-Point feature: " + name);
                warnings.add("Skipping non-Point feature: " + name);
                return null;
            }
            
            // Extract coordinates
            final JsonArray coords = geometry.getAsJsonArray("coordinates");
            final double longitude = coords.get(0).getAsDouble();
            final double latitude = coords.get(1).getAsDouble();
            
            // Create coordinates
            final Coordinates coordinates = new Coordinates(latitude, longitude);
            
            // Create address
            final Address address = new Address();
            address.setFullAddress(name);
            address.setCoordinates(coordinates);
            // Normalize address and create search terms
            String normalizedAddress = name.toLowerCase().trim();
            address.setNormalizedAddress(normalizedAddress);
            
            // Create search terms for improved searching
            List<String> searchTerms = new ArrayList<>();
            String[] addressParts = normalizedAddress.split("\\s+");
            for (String part : addressParts) {
                if (!part.isEmpty()) {
                    searchTerms.add(part);
                }
            }
            address.setSearchTerms(searchTerms);
            
            // Try to extract order ID from name or description
            String orderId = extractOrderId(name);
            if (orderId == null) {
                orderId = extractOrderId(description);
            }
            
            // Extract tip amount from description if available
            double tipAmount = 0.0;
            try {
                // Simple extraction for demo - can be enhanced with regex
                if (description.contains("$")) {
                    final String[] descParts = description.split("\\$");
                    if (descParts.length > 1) {
                        final String amountStr = descParts[1].split("\\s+")[0].trim();
                        tipAmount = Double.parseDouble(amountStr);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not parse tip amount from description: " + description);
                warnings.add("Could not parse tip amount from '" + description + "'");
            }
            
            // Create tip data
            final TipData tipData = new TipData();
            tipData.setAmount(tipAmount);
            tipData.setSource("geojson_import");
            
            // Create delivery data
            final DeliveryData deliveryData = new DeliveryData();
            deliveryData.setAddress(address);
            deliveryData.setTipData(tipData);
            deliveryData.setNotes(description);
            deliveryData.setImportedFromGeoJson(true);
            deliveryData.setTimestamp(System.currentTimeMillis());
            
            if (orderId != null) {
                deliveryData.setOrderId(orderId);
            }
            
            return deliveryData;
        } catch (Exception e) {
            Log.e(TAG, "Error converting feature to delivery: " + e.getMessage());
            warnings.add("Error converting feature to delivery: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract order ID from text using regex with improved error handling
     * 
     * @param text Text to extract from
     * @return Order ID if found, null otherwise
     */
    private String extractOrderId(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        
        try {
            Matcher matcher = ORDER_ID_PATTERN.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error extracting order ID from text: " + e.getMessage());
            warnings.add("Error extracting order ID from text: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Save deliveries to Firestore in a batch
     * 
     * @param deliveries List of DeliveryData objects to save
     */
    private void saveDeliveriesToFirestore(final List<DeliveryData> deliveries) {
        if (deliveries.isEmpty()) {
            Log.w(TAG, "No deliveries to save");
            return;
        }
        
        firestoreRepository.batchSaveDeliveries(deliveries)
            .addOnSuccessListener(aVoid -> {
                Log.i(TAG, "Successfully saved " + deliveries.size() + " deliveries to Firestore");
                // Refresh UI by notifying the repository listeners
                firestoreRepository.notifyDataChanged();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save deliveries: " + e.getMessage());
                warnings.add("Failed to save deliveries: " + e.getMessage());
            });
    }
    
    /**
     * Get the count of new records from the last import
     */
    public int getNewCount() {
        return newCount;
    }
    
    /**
     * Get the count of updated records from the last import
     */
    public int getUpdatedCount() {
        return updatedCount;
    }
    
    /**
     * Get the count of duplicate records from the last import
     */
    public int getDuplicateCount() {
        return duplicateCount;
    }
    
    /**
     * Get the count of invalid records from the last import
     */
    public int getInvalidCount() {
        return invalidCount;
    }
    
    /**
     * Get warnings from the last import
     */
    public List<String> getWarnings() {
        return warnings;
    }
}