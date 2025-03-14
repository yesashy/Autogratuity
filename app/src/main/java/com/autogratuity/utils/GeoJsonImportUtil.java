package com.autogratuity.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.autogratuity.models.Address;
import com.autogratuity.models.Delivery;
import com.autogratuity.repositories.FirestoreRepository;
import com.autogratuity.repositories.IFirestoreRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for importing delivery and tip data from Google Maps GeoJSON export
 */
public class GeoJsonImportUtil {
    private static final String TAG = "GeoJsonImportUtil";
    
    // Regex patterns for extracting order IDs and tip information
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^(\\d{8,10})");
    private static final Pattern ORDER_ID_WITH_TEXT_PATTERN = Pattern.compile("(\\d{8,10})\\s*[-–]?\\s*(.*)");
    private static final Pattern TIP_AMOUNT_PATTERN = Pattern.compile("\\$([0-9]+(?:\\.[0-9]{1,2})?)");
    private static final Pattern DO_NOT_DELIVER_PATTERN = Pattern.compile("(?i)(do not deliver|don'?t deliver)");
    
    private final Context context;
    private final IFirestoreRepository repository;
    private final FirebaseAuth auth;
    
    // Import statistics
    private int totalFeatures = 0;
    private int successfulImports = 0;
    private int failedImports = 0;
    private int duplicateEntries = 0;
    private final List<String> importErrors = new ArrayList<>();
    
    public GeoJsonImportUtil(Context context) {
        this.context = context;
        this.repository = FirestoreRepository.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    
    /**
     * Parses a GeoJSON file from a URI and imports the deliveries
     *
     * @param fileUri The URI of the GeoJSON file
     * @param callback Callback for handling import completion and statistics
     */
    public void importFromGeoJson(Uri fileUri, ImportCallback callback) {
        // Reset statistics
        totalFeatures = 0;
        successfulImports = 0;
        failedImports = 0;
        duplicateEntries = 0;
        importErrors.clear();
        
        // Ensure the user is logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onImportFailed("User not logged in");
            }
            return;
        }
        
        String userId = currentUser.getUid();
        
        new Thread(() -> {
            try {
                String jsonContent = readTextFromUri(fileUri);
                
                if (jsonContent == null || jsonContent.isEmpty()) {
                    reportFailure(callback, "File is empty or could not be read");
                    return;
                }
                
                JSONObject geoJson = new JSONObject(jsonContent);
                
                // Validate that this is a GeoJSON FeatureCollection
                if (!geoJson.has("type") || !geoJson.getString("type").equals("FeatureCollection")) {
                    reportFailure(callback, "Invalid GeoJSON format: Not a FeatureCollection");
                    return;
                }
                
                if (!geoJson.has("features")) {
                    reportFailure(callback, "Invalid GeoJSON format: No features found");
                    return;
                }
                
                JSONArray features = geoJson.getJSONArray("features");
                totalFeatures = features.length();
                
                // Process each feature (location)
                for (int i = 0; i < features.length(); i++) {
                    JSONObject feature = features.getJSONObject(i);
                    try {
                        processFeature(feature, userId);
                        successfulImports++;
                    } catch (Exception e) {
                        failedImports++;
                        importErrors.add("Error processing feature " + (i + 1) + ": " + e.getMessage());
                        Log.e(TAG, "Error processing feature " + (i + 1), e);
                    }
                }
                
                // Report success with statistics
                if (callback != null) {
                    ImportStatistics stats = new ImportStatistics(
                            totalFeatures,
                            successfulImports,
                            failedImports,
                            duplicateEntries,
                            importErrors
                    );
                    callback.onImportCompleted(stats);
                }
                
            } catch (JSONException e) {
                reportFailure(callback, "Invalid JSON format: " + e.getMessage());
            } catch (IOException e) {
                reportFailure(callback, "Error reading file: " + e.getMessage());
            } catch (Exception e) {
                reportFailure(callback, "Unexpected error: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Process a single Feature from the GeoJSON
     */
    private void processFeature(JSONObject feature, String userId) throws JSONException {
        // Validate that this is a Feature
        if (!feature.has("type") || !feature.getString("type").equals("Feature")) {
            throw new JSONException("Not a Feature type");
        }
        
        // Extract properties
        JSONObject properties = feature.getJSONObject("properties");
        String name = properties.has("name") ? properties.getString("name") : null;
        String address = properties.has("address") ? properties.getString("address") : null;
        
        // Skip if we don't have the key data
        if (name == null || address == null) {
            throw new JSONException("Missing name or address property");
        }
        
        // Extract geometry
        JSONObject geometry = feature.getJSONObject("geometry");
        String geometryType = geometry.getString("type");
        
        // Only process Point geometries
        if (!"Point".equals(geometryType)) {
            throw new JSONException("Not a Point geometry");
        }
        
        // Extract coordinates [longitude, latitude]
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        double longitude = coordinates.getDouble(0);
        double latitude = coordinates.getDouble(1);
        
        // Skip if coordinates are [0,0] (invalid)
        if (latitude == 0.0 && longitude == 0.0) {
            throw new JSONException("Invalid coordinates [0,0]");
        }
        
        // Extract order ID and tip info from the name
        String orderId = null;
        String additionalInfo = null;
        boolean doNotDeliver = false;
        Double tipAmount = null;
        
        // Try to extract order ID from the name
        Matcher orderIdMatcher = ORDER_ID_PATTERN.matcher(name);
        if (orderIdMatcher.find()) {
            orderId = orderIdMatcher.group(1);
            
            // Check if there's additional info after the order ID
            Matcher infoMatcher = ORDER_ID_WITH_TEXT_PATTERN.matcher(name);
            if (infoMatcher.find()) {
                additionalInfo = infoMatcher.group(2).trim();
                
                // Extract tip amount if present
                Matcher tipMatcher = TIP_AMOUNT_PATTERN.matcher(additionalInfo);
                if (tipMatcher.find()) {
                    try {
                        tipAmount = Double.parseDouble(tipMatcher.group(1));
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Could not parse tip amount: " + tipMatcher.group(1));
                    }
                }
                
                // Check if this is marked as "Do Not Deliver"
                Matcher dndMatcher = DO_NOT_DELIVER_PATTERN.matcher(additionalInfo);
                doNotDeliver = dndMatcher.find();
            }
        } else {
            // If no order ID is found, use a temporary generated one
            // This allows importing locations that don't have order IDs yet
            orderId = "TEMP_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
            additionalInfo = name; // Use the full name as additional info
            
            // Check for tip info and DND flag in the whole name
            Matcher tipMatcher = TIP_AMOUNT_PATTERN.matcher(name);
            if (tipMatcher.find()) {
                try {
                    tipAmount = Double.parseDouble(tipMatcher.group(1));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Could not parse tip amount: " + tipMatcher.group(1));
                }
            }
            
            Matcher dndMatcher = DO_NOT_DELIVER_PATTERN.matcher(name);
            doNotDeliver = dndMatcher.find();
        }
        
        // Create Delivery object
        Delivery delivery = new Delivery(orderId, address, Timestamp.now());
        delivery.setUserId(userId);
        delivery.setImportDate(Timestamp.now());
        delivery.setDoNotDeliver(doNotDeliver);
        delivery.setSource("geojson_import");
        
        // Set coordinates string for backward compatibility
        delivery.setCoordinates(latitude + "," + longitude);
        
        // Set tip amount if found
        if (tipAmount != null && tipAmount > 0) {
            delivery.setTipAmount(tipAmount);
            delivery.setTipDate(Timestamp.now());
        }
        
        // Add to Firestore
        repository.addDelivery(delivery)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Added delivery: " + orderId);
                    
                    // Also update address data
                    updateAddressData(address, orderId, tipAmount, latitude, longitude, doNotDeliver, userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding delivery: " + orderId, e);
                    failedImports++;
                    importErrors.add("Error adding delivery " + orderId + ": " + e.getMessage());
                });
    }
    
    /**
     * Updates or creates the address data with location and statistics
     */
    private void updateAddressData(String fullAddress, String orderId, Double tipAmount, 
                                  double latitude, double longitude, boolean doNotDeliver, 
                                  String userId) {
        // Normalize address for matching
        String normalizedAddress = fullAddress.toLowerCase().trim();
        
        // Search for existing address
        repository.findAddressByNormalizedAddress(normalizedAddress)
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Update existing address
                        String addressId = querySnapshot.getDocuments().get(0).getId();
                        
                        // Only update if tip amount exists
                        if (tipAmount != null && tipAmount > 0) {
                            repository.updateAddressStatistics(addressId, tipAmount, orderId);
                        }
                        
                        // Update "Do Not Deliver" flag if needed
                        if (doNotDeliver) {
                            // This method would need to be added to IFirestoreRepository
                            updateAddressDoNotDeliver(addressId, true);
                        }
                        
                        duplicateEntries++;
                    } else {
                        // Create new address
                        Address address = new Address();
                        address.setFullAddress(fullAddress);
                        address.setNormalizedAddress(normalizedAddress);
                        address.setUserId(userId);
                        address.setDoNotDeliver(doNotDeliver);
                        
                        // Create search terms for better searching
                        List<String> searchTerms = new ArrayList<>();
                        searchTerms.add(normalizedAddress);
                        
                        // Add individual words for partial matching
                        String[] words = normalizedAddress.split("\\s+");
                        for (String word : words) {
                            if (word.length() > 3) {  // Only add meaningful words
                                searchTerms.add(word);
                            }
                        }
                        address.setSearchTerms(searchTerms);
                        
                        // Set location data
                        address.setGeoPoint(new GeoPoint(latitude, longitude));
                        
                        // Set order ID
                        List<String> orderIds = new ArrayList<>();
                        orderIds.add(orderId);
                        address.setOrderIds(orderIds);
                        
                        // Set statistics
                        if (tipAmount != null && tipAmount > 0) {
                            address.setTotalTips(tipAmount);
                            address.setDeliveryCount(1);
                            address.setAverageTip(tipAmount);
                        } else {
                            address.setTotalTips(0.0);
                            address.setDeliveryCount(1);
                            address.setAverageTip(0.0);
                        }
                        
                        // Add to Firestore
                        repository.addAddress(address)
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding address: " + fullAddress, e);
                                    importErrors.add("Error adding address " + fullAddress + ": " + e.getMessage());
                                });
                    }
                });
    }
    
    /**
     * Updates the "Do Not Deliver" flag for an address
     */
    private void updateAddressDoNotDeliver(String addressId, boolean doNotDeliver) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("doNotDeliver", doNotDeliver);
        
        // Add this method to the Firestore repository as well
        FirestoreRepository.getInstance().getFirestore()
                .collection("addresses")
                .document(addressId)
                .update(updates)
                .addOnFailureListener(e -> Log.e(TAG, "Error updating doNotDeliver flag", e));
    }
    
    /**
     * Reads a text file from a URI
     */
    private String readTextFromUri(Uri uri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
            return stringBuilder.toString();
        }
    }
    
    /**
     * Reports an import failure to the callback
     */
    private void reportFailure(ImportCallback callback, String errorMessage) {
        Log.e(TAG, errorMessage);
        if (callback != null) {
            callback.onImportFailed(errorMessage);
        }
    }
    
    /**
     * Callback interface for import operations
     */
    public interface ImportCallback {
        void onImportCompleted(ImportStatistics statistics);
        void onImportFailed(String errorMessage);
    }
    
    /**
     * Class for storing import statistics
     */
    public static class ImportStatistics {
        private final int totalFeatures;
        private final int successfulImports;
        private final int failedImports;
        private final int duplicateEntries;
        private final List<String> errors;
        
        public ImportStatistics(int totalFeatures, int successfulImports, int failedImports, int duplicateEntries, List<String> errors) {
            this.totalFeatures = totalFeatures;
            this.successfulImports = successfulImports;
            this.failedImports = failedImports;
            this.duplicateEntries = duplicateEntries;
            this.errors = new ArrayList<>(errors);
        }
        
        public int getTotalFeatures() {
            return totalFeatures;
        }
        
        public int getSuccessfulImports() {
            return successfulImports;
        }
        
        public int getFailedImports() {
            return failedImports;
        }
        
        public int getDuplicateEntries() {
            return duplicateEntries;
        }
        
        public List<String> getErrors() {
            return errors;
        }
    }
}
