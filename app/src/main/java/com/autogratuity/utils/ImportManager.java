package com.autogratuity.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Address.Location;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.SyncOperation;
import com.autogratuity.data.model.converter.ModelConverters;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.data.repository.sync.SyncRepository;
import com.autogratuity.ui.map.MapFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Manager class for handling bulk imports from various sources
 * Coordinates the import process and updates the UI afterward
 * 
 * Updated to use domain-specific repositories with the new architecture
 */
public class ImportManager {
    private static final String TAG = "ImportManager";
    
    private final Context context;
    private final AddressRepository addressRepository;
    private final DeliveryRepository deliveryRepository;
    private final SyncRepository syncRepository;
    private final MapFragment mapFragment;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    /**
     * Create a new ImportManager with domain repositories
     *
     * @param context The application context
     * @param addressRepository The address repository for saving address data
     * @param deliveryRepository The delivery repository for saving delivery data
     * @param syncRepository The sync repository for offline operations
     * @param mapFragment The map fragment to update (can be null)
     */
    public ImportManager(Context context, 
                        AddressRepository addressRepository, 
                        DeliveryRepository deliveryRepository,
                        SyncRepository syncRepository,
                        MapFragment mapFragment) {
        this.context = context;
        this.addressRepository = addressRepository;
        this.deliveryRepository = deliveryRepository;
        this.syncRepository = syncRepository;
        this.mapFragment = mapFragment;
    }
    
    /**
     * Import data from a GeoJSON file
     *
     * @param uri URI of the GeoJSON file
     * @param skipDuplicates Whether to skip duplicate records
     * @param updateExisting Whether to update existing records
     * @param listener Listener for import completion
     */
    public void importFromGeoJson(Uri uri, boolean skipDuplicates, boolean updateExisting, ImportListener listener) {
        if (uri == null) {
            notifyFailure(listener, "Invalid file URI");
            return;
        }

        InputStream inputStream = null;
        try {
            // Open input stream from URI
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                notifyFailure(listener, "Could not open file");
                return;
            }
            
            // Create a new GeoJSON import utility using domain repositories
            GeoJsonImportUtil importUtil = new GeoJsonImportUtil(addressRepository, deliveryRepository, 
                    syncRepository, skipDuplicates, updateExisting);
            
            disposables.add(
                importUtil.importFromGeoJson(context, inputStream)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        success -> {
                            if (success) {
                                // Update UI
                                updateMapAfterImport();
                                
                                // Notify listener
                                if (listener != null) {
                                    listener.onImportCompleted(
                                            importUtil.getNewCount(), 
                                            importUtil.getUpdatedCount(),
                                            importUtil.getDuplicateCount(),
                                            importUtil.getInvalidCount(),
                                            importUtil.getWarnings());
                                }
                            } else {
                                List<String> errors = new ArrayList<>();
                                errors.add("GeoJSON import failed");
                                notifyFailure(listener, "Import failed", errors);
                            }
                        },
                        error -> {
                            Log.e(TAG, "Error during import", error);
                            List<String> errors = new ArrayList<>();
                            errors.add(error.getMessage());
                            notifyFailure(listener, "Error: " + error.getMessage(), errors);
                        }
                    )
            );
        } catch (Exception e) {
            Log.e(TAG, "Error during import", e);
            List<String> errors = new ArrayList<>();
            errors.add(e.getMessage());
            notifyFailure(listener, "Error: " + e.getMessage(), errors);
        } finally {
            // Always close the input stream
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing input stream", e);
                }
            }
        }
    }
    
    /**
     * Import data from a KML file
     *
     * @param uri URI of the KML file
     * @param listener Listener for import completion
     */
    public void importFromKml(Uri uri, ImportListener listener) {
        // Placeholder - implement KML import with domain repositories
        // This would follow a similar pattern to GeoJSON import
        
        // For now, enqueue an offline operation as an example
        if (uri != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fileUri", uri.toString());
            metadata.put("importType", "kml");
            
            disposables.add(
                syncRepository.createEntity("import_request", null, metadata)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        () -> {
                            // Notify that operation is queued
                            List<String> warnings = new ArrayList<>();
                            warnings.add("KML import queued for processing when implementation is complete");
                            if (listener != null) {
                                listener.onImportCompleted(0, 0, 0, 0, warnings);
                            }
                        },
                        error -> {
                            List<String> errors = new ArrayList<>();
                            errors.add("KML import not implemented yet");
                            errors.add(error.getMessage());
                            notifyFailure(listener, "KML import not implemented yet", errors);
                        }
                    )
            );
        } else {
            List<String> errors = new ArrayList<>();
            errors.add("Invalid KML file URI");
            notifyFailure(listener, "Invalid KML file URI", errors);
        }
    }
    
    /**
     * Import data from a CSV file
     *
     * @param uri URI of the CSV file
     * @param listener Listener for import completion
     */
    public void importFromCsv(Uri uri, ImportListener listener) {
        // Placeholder - implement CSV import with domain repositories
        // This would follow a similar pattern to GeoJSON import
        
        // For now, enqueue an offline operation as an example
        if (uri != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fileUri", uri.toString());
            metadata.put("importType", "csv");
            
            disposables.add(
                syncRepository.createEntity("import_request", null, metadata)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        () -> {
                            // Notify that operation is queued
                            List<String> warnings = new ArrayList<>();
                            warnings.add("CSV import queued for processing when implementation is complete");
                            if (listener != null) {
                                listener.onImportCompleted(0, 0, 0, 0, warnings);
                            }
                        },
                        error -> {
                            List<String> errors = new ArrayList<>();
                            errors.add("CSV import not implemented yet");
                            errors.add(error.getMessage());
                            notifyFailure(listener, "CSV import not implemented yet", errors);
                        }
                    )
            );
        } else {
            List<String> errors = new ArrayList<>();
            errors.add("Invalid CSV file URI");
            notifyFailure(listener, "Invalid CSV file URI", errors);
        }
    }
    
    /**
     * Process a list of deliveries ensuring they are properly geocoded and saved
     * 
     * @param deliveries The list of deliveries to process
     * @return Single that emits the list of processed deliveries
     */
    public Single<List<Delivery>> processDeliveries(List<Delivery> deliveries) {
        return Single.fromCallable(() -> {
            List<Delivery> processedDeliveries = new ArrayList<>();
            
            for (Delivery delivery : deliveries) {
                if (delivery.getAddress() != null) {
                    // Ensure the address is geocoded
                    if (ModelConverters.getLocation(delivery.getAddress()) == null ||
                            (ModelConverters.getLocation(delivery.getAddress()).getLatitude() == 0 &&
                             ModelConverters.getLocation(delivery.getAddress()).getLongitude() == 0)) {
                        try {
                            // Geocode the address - first convert SimpleAddress to full Address
                            Address fullAddress = ModelConverters.fromSimpleAddress(delivery.getAddress());
                            Address geocodedAddress = addressRepository.geocodeAddress(fullAddress)
                                    .blockingGet();
                            // Convert geocoded address back to SimpleAddress
                            delivery.setAddress(ModelConverters.toSimpleAddress(geocodedAddress));
                        } catch (Exception e) {
                            Log.e(TAG, "Error geocoding address: " + e.getMessage());
                        }
                    }
                    
                    processedDeliveries.add(delivery);
                }
            }
            
            return processedDeliveries;
        });
    }
    
    /**
     * Save a list of deliveries with proper offline handling
     * 
     * @param deliveries The list of deliveries to save
     * @return Single that emits the number of saved deliveries
     */
    public Single<Integer> saveDeliveries(List<Delivery> deliveries) {
        return Single.fromCallable(() -> {
            int savedCount = 0;
            
            for (Delivery delivery : deliveries) {
                try {
                    // Save the address first if it doesn't exist
                    if (delivery.getAddress() != null) {
                        String normalizedAddress = addressRepository.normalizeAddress(
                                delivery.getAddress().getFullAddress());
                        
                        // Try to find if address already exists
                        Address existingAddress = null;
                        try {
                            existingAddress = addressRepository.findAddressByNormalizedAddress(normalizedAddress)
                                    .blockingGet();
                        } catch (Exception e) {
                            Log.d(TAG, "Address doesn't exist, will create new: " + e.getMessage());
                        }
                        
                        if (existingAddress == null) {
                            // Convert SimpleAddress to Address for creation
                            Address fullAddress = ModelConverters.fromSimpleAddress(delivery.getAddress());
                            // Create new address
                            addressRepository.addAddress(fullAddress)
                                    .blockingGet();
                        } else {
                            // Use existing address - convert to SimpleAddress
                            delivery.setAddress(ModelConverters.toSimpleAddress(existingAddress));
                        }
                    }
                    
                    // Save the delivery
                    deliveryRepository.addDelivery(delivery)
                            .blockingGet();
                    
                    savedCount++;
                } catch (Exception e) {
                    Log.e(TAG, "Error saving delivery: " + e.getMessage());
                    
                    // If network error, enqueue for later sync
                    try {
                        // Convert delivery to map
                        Map<String, Object> deliveryMap = new HashMap<>();
                        // Add delivery details to map
                        // Convert Address.SimpleAddress to Address and save (compatible with sync)
                        Address fullAddress = ModelConverters.fromSimpleAddress(delivery.getAddress());
                        deliveryMap.put("address", fullAddress);
                        deliveryMap.put("amounts", ModelConverters.toAmounts(delivery.getAmounts())); // Convert to compatible type
                        deliveryMap.put("notes", delivery.getNotes());
                        // Add more fields as needed
                        
                        // Create sync operation
                        syncRepository.createEntity("delivery", null, deliveryMap)
                                .blockingAwait();
                                
                        savedCount++;
                    } catch (Exception syncError) {
                        Log.e(TAG, "Error queueing delivery for sync: " + syncError.getMessage());
                    }
                }
            }
            
            return savedCount;
        });
    }
    
    /**
     * Update the map after an import
     */
    private void updateMapAfterImport() {
        // Update map if available
        if (mapFragment != null) {
            mapFragment.refreshMap();
        } else {
            Log.d(TAG, "Map fragment not available for refresh");
        }
    }
    
    /**
     * Notify listener of import failure
     *
     * @param listener The listener to notify
     * @param message The error message
     */
    private void notifyFailure(ImportListener listener, String message) {
        List<String> errors = new ArrayList<>();
        errors.add(message);
        notifyFailure(listener, message, errors);
    }
    
    /**
     * Notify listener of import failure with detailed errors
     *
     * @param listener The listener to notify
     * @param message The error message
     * @param errors List of detailed error messages
     */
    private void notifyFailure(ImportListener listener, String message, List<String> errors) {
        Log.e(TAG, message);
        
        // Show toast to user if context is available
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
        
        // Notify listener
        if (listener != null) {
            listener.onImportFailed(message, errors);
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
     * Listener interface for import operations
     */
    public interface ImportListener {
        /**
         * Called when import is completed successfully
         * 
         * @param newCount Number of new records added
         * @param updatedCount Number of existing records updated
         * @param duplicateCount Number of duplicate records skipped
         * @param invalidCount Number of invalid records
         * @param warnings List of warnings
         */
        void onImportCompleted(int newCount, int updatedCount, int duplicateCount, int invalidCount, List<String> warnings);
        
        /**
         * Called when import fails
         * 
         * @param message Error message
         * @param errors List of detailed error messages
         */
        void onImportFailed(String message, List<String> errors);
    }
}