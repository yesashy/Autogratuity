package com.autogratuity.utils;

import android.content.Context;
import android.util.Log;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Address.Location;
import com.autogratuity.data.model.Address.SearchFields;
import com.autogratuity.data.model.Coordinates;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.Amounts;
import com.autogratuity.data.model.Metadata;
import com.autogratuity.data.model.Reference;
import com.autogratuity.data.model.Times;
import com.autogratuity.data.model.Status;
import com.autogratuity.data.model.converter.ModelConverters;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.data.repository.sync.SyncRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Utility class for importing GeoJSON data
 * Supports extracting location data from Google Maps exports
 * Updated to use domain repositories
 */
public class GeoJsonImportUtil {
    private static final String TAG = "GeoJsonImportUtil";
    private final AddressRepository addressRepository;
    private final DeliveryRepository deliveryRepository;
    private final SyncRepository syncRepository;
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
     * Create a new GeoJsonImportUtil with domain repositories
     * 
     * @param addressRepository Repository for address operations
     * @param deliveryRepository Repository for delivery operations
     * @param syncRepository Repository for sync operations
     */
    public GeoJsonImportUtil(AddressRepository addressRepository, 
                            DeliveryRepository deliveryRepository,
                            SyncRepository syncRepository) {
        this.addressRepository = addressRepository;
        this.deliveryRepository = deliveryRepository;
        this.syncRepository = syncRepository;
        this.skipDuplicates = false;
        this.updateExisting = true;
    }
    
    /**
     * Create a new GeoJsonImportUtil with domain repositories and validation options
     * 
     * @param addressRepository Repository for address operations
     * @param deliveryRepository Repository for delivery operations
     * @param syncRepository Repository for sync operations
     * @param skipDuplicates Whether to skip duplicate records
     * @param updateExisting Whether to update existing records
     */
    public GeoJsonImportUtil(AddressRepository addressRepository, 
                            DeliveryRepository deliveryRepository,
                            SyncRepository syncRepository,
                            boolean skipDuplicates, boolean updateExisting) {
        this.addressRepository = addressRepository;
        this.deliveryRepository = deliveryRepository;
        this.syncRepository = syncRepository;
        this.skipDuplicates = skipDuplicates;
        this.updateExisting = updateExisting;
    }
    
    /**
     * Import GeoJSON data from an input stream
     * 
     * @param context The application context
     * @param inputStream The input stream containing GeoJSON data
     * @return Single that emits true if import was successful, false otherwise
     */
    public Single<Boolean> importFromGeoJson(Context context, InputStream inputStream) {
        return Single.fromCallable(() -> {
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
                    return processFeatures(features);
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
        });
    }
    
    /**
     * Process features from GeoJSON data
     * 
     * @param features Array of GeoJSON features
     * @return true if processing was successful, false otherwise
     */
    private boolean processFeatures(JsonArray features) {
        // Create a final list to store all deliveries
        final List<Delivery> deliveries = new ArrayList<>();
        
        // Use AtomicInteger for counters that need to be modified in lambdas
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);
        
        // Process each feature
        for (JsonElement featureElement : features) {
            try {
                final JsonObject feature = featureElement.getAsJsonObject();
                final Delivery delivery = convertFeatureToDelivery(feature);
                
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
        List<Delivery> validatedDeliveries = validateDeliveries(deliveries);
        
        // Save all processed deliveries to repositories
        if (!validatedDeliveries.isEmpty()) {
            return saveDeliveriesToRepositories(validatedDeliveries);
        } else {
            Log.w(TAG, "No valid deliveries to save after validation");
            warnings.add("No valid deliveries to save after validation");
            return false;
        }
    }
    
    /**
     * Convert a GeoJSON feature to a Delivery object
     * 
     * @param feature The GeoJSON feature
     * @return Delivery object, or null if conversion failed
     */
    private Delivery convertFeatureToDelivery(JsonObject feature) {
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
            address.setLocation(ModelConverters.toLocation(coordinates));
            
            // Normalize address and create search terms
            String normalizedAddress = addressRepository.normalizeAddress(name);
            address.setNormalizedAddress(normalizedAddress);
            
            // Parse address components
            Address.Components components = addressRepository.parseAddressComponents(name);
            address.setComponents(components);
            
            // Create search terms for improved searching
            List<String> searchTerms = new ArrayList<>();
            String[] addressParts = normalizedAddress.split("\\s+");
            for (String part : addressParts) {
                if (!part.isEmpty()) {
                    searchTerms.add(part);
                }
            }
            SearchFields searchFields = new SearchFields();
            searchFields.setSearchTerms(searchTerms);
            address.setSearchFields(searchFields);
            
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
            
            // Create a delivery with proper structure
            final Delivery delivery = new Delivery();
            
            // Set address as the reference
            Reference reference = new Reference();
            reference.setAddressId(null); // Will be set when address is saved
            reference.setPlatformOrderId(orderId); // Setting platform order ID
            delivery.setReference(ModelConverters.toDeliveryReference(reference)); // Convert using ModelConverters
            delivery.setAddress(ModelConverters.toSimpleAddress(address)); // Convert using ModelConverters
            
            // Set amounts
            Amounts amounts = new Amounts();
            amounts.setTipAmount(tipAmount);
            amounts.setBaseAmount(0.0); // Unknown from GeoJSON
            amounts.setEstimatedPay(0.0); // Unknown from GeoJSON
            delivery.setAmounts(ModelConverters.toDeliveryAmounts(amounts)); // Convert using ModelConverters
            
            // Set metadata
            Metadata metadata = new Metadata();
            metadata.setCreatedAt(new Date());
            metadata.setSource("geojson_import");
            if (orderId != null) {
                metadata.setImportId(orderId);
            }
            delivery.setMetadata(ModelConverters.toDeliveryMetadata(metadata)); // Convert using ModelConverters
            
            // Set times
            Times times = new Times();
            times.setOrderedAt(new Date());
            delivery.setTimes(ModelConverters.toDeliveryTimes(times)); // Convert using ModelConverters
            
            // Set status
            Status status = new Status();
            status.setTipped(tipAmount > 0);
            status.setCompleted(true);
            delivery.setStatus(ModelConverters.toDeliveryStatus(status)); // Convert using ModelConverters
            
            // Set notes
            delivery.setNotes(description);
            
            return delivery;
        } catch (Exception e) {
            Log.e(TAG, "Error converting feature to delivery: " + e.getMessage());
            warnings.add("Error converting feature to delivery: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract order ID from text using regex
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
     * Validate deliveries before saving
     * 
     * @param deliveries List of Delivery objects to validate
     * @return List of validated Delivery objects
     */
    private List<Delivery> validateDeliveries(final List<Delivery> deliveries) {
        List<Delivery> validated = new ArrayList<>();
        
        for (Delivery delivery : deliveries) {
            // Check required fields
            if (delivery.getAddress() == null || delivery.getAddress().getFullAddress() == null) {
                invalidCount++;
                warnings.add("Skipping delivery with no address");
                continue;
            }
            
            if (delivery.getAddress().getLocation() == null || 
                    (delivery.getAddress().getLocation().getLatitude() == 0 && 
                     delivery.getAddress().getLocation().getLongitude() == 0)) {
                invalidCount++;
                warnings.add("Skipping delivery with invalid coordinates: " + delivery.getAddress().getFullAddress());
                continue;
            }
            
            // Check for duplicates if needed
            if (skipDuplicates) {
                try {
                    String normalizedAddress = addressRepository.normalizeAddress(delivery.getAddress().getFullAddress());
                    Address existingAddress = addressRepository.findAddressByNormalizedAddress(normalizedAddress)
                            .blockingGet();
                    
                    if (existingAddress != null) {
                        duplicateCount++;
                        
                        if (updateExisting) {
                            // Update existing address with new data if needed
                            delivery.getAddress().setAddressId(existingAddress.getAddressId());
                            validated.add(delivery);
                        } else {
                            warnings.add("Skipping duplicate address: " + delivery.getAddress().getFullAddress());
                        }
                        
                        continue;
                    }
                } catch (Exception e) {
                    // Address not found, which is fine for new records
                }
            }
            
            // Add valid deliveries
            validated.add(delivery);
        }
        
        return validated;
    }
    
    /**
     * Save deliveries to repositories with proper error handling
     * 
     * @param deliveries List of Delivery objects to save
     * @return true if saving was successful, false otherwise
     */
    private boolean saveDeliveriesToRepositories(final List<Delivery> deliveries) {
        if (deliveries.isEmpty()) {
            Log.w(TAG, "No deliveries to save");
            return false;
        }
        
        for (Delivery delivery : deliveries) {
            try {
                // First try to find existing address
                Address existingAddress = null;
                String normalizedAddress = addressRepository.normalizeAddress(delivery.getAddress().getFullAddress());
                
                try {
                    existingAddress = addressRepository.findAddressByNormalizedAddress(normalizedAddress)
                            .blockingGet();
                } catch (Exception e) {
                    // Address not found, will create new one
                }
                
                // Handle the address
                if (existingAddress != null) {
                    // Use existing address
                    Reference reference = delivery.getReference();
                    reference.setAddressId(existingAddress.getAddressId());
                    delivery.setReference(reference);
                    
                    if (updateExisting) {
                        // Update the address if needed
                        addressRepository.updateAddress(delivery.getAddress())
                                .blockingAwait();
                        updatedCount++;
                    } else {
                        // Use the existing address
                        delivery.setAddress(existingAddress);
                    }
                } else {
                    // Create new address
                    DocumentReference addressRef = addressRepository.addAddress(delivery.getAddress())
                            .blockingGet();
                    
                    // Update reference with new address ID
                    String addressId = addressRef.getId();
                    delivery.getAddress().setAddressId(addressId);
                    Reference reference = delivery.getReference();
                    reference.setAddressId(addressId);
                    delivery.setReference(reference);
                    
                    newCount++;
                }
                
                // Save the delivery
                deliveryRepository.addDelivery(delivery)
                        .blockingGet();
                
            } catch (Exception e) {
                Log.e(TAG, "Error saving delivery: " + e.getMessage());
                warnings.add("Error saving delivery: " + e.getMessage());
                
                // If offline, queue for later sync
                try {
                    // Convert to map for sync
                    Map<String, Object> deliveryMap = new HashMap<>();
                    deliveryMap.put("address", delivery.getAddress());
                    deliveryMap.put("amounts", delivery.getAmounts());
                    deliveryMap.put("reference", delivery.getReference());
                    deliveryMap.put("notes", delivery.getNotes());
                    deliveryMap.put("metadata", delivery.getMetadata());
                    deliveryMap.put("times", delivery.getTimes());
                    deliveryMap.put("status", delivery.getStatus());
                    
                    // Enqueue operation for later sync
                    syncRepository.createEntity("delivery", null, deliveryMap)
                            .blockingAwait();
                    
                    warnings.add("Delivery queued for offline sync: " + delivery.getAddress().getFullAddress());
                } catch (Exception syncError) {
                    Log.e(TAG, "Error queueing for sync: " + syncError.getMessage());
                    warnings.add("Failed to queue for sync: " + syncError.getMessage());
                    return false;
                }
            }
        }
        
        Log.i(TAG, String.format(
                "Import complete. New: %d, Updated: %d, Duplicates: %d, Invalid: %d",
                newCount, updatedCount, duplicateCount, invalidCount));
        
        return true;
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