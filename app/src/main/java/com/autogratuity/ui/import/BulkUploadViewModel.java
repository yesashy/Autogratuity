// Replace the package line only
package com.autogratuity.ui.import_;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.data.repository.sync.SyncRepository;
import com.autogratuity.ui.common.BaseViewModel;
import com.autogratuity.utils.ImportManager;
import com.autogratuity.utils.UsageTracker;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for BulkUploadFragment
 * Manages data for bulk import operations
 */
public class BulkUploadViewModel extends BaseViewModel {
    
    // Repositories
    private final AddressRepository addressRepository;
    private final DeliveryRepository deliveryRepository;
    private final SyncRepository syncRepository;
    
    // Import manager and usage tracker
    private ImportManager importManager;
    private UsageTracker usageTracker;
    
    // Import status
    private final MutableLiveData<Boolean> importingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> statusMessageLiveData = new MutableLiveData<>("");
    
    // Import results
    private final MutableLiveData<ImportResult> importResultLiveData = new MutableLiveData<>();
    
    /**
     * Constructor - initializes repositories
     */
    public BulkUploadViewModel() {
        // Initialize repositories from RepositoryProvider
        addressRepository = RepositoryProvider.getAddressRepository();
        deliveryRepository = RepositoryProvider.getDeliveryRepository();
        syncRepository = RepositoryProvider.getSyncRepository();
    }
    
    /**
     * Initialize ImportManager and UsageTracker
     * 
     * @param context Application context
     * @param mapFragment Optional MapFragment for refreshing map data
     */
    public void initializeManagers(Context context, Object mapFragment) {
        // Initialize import manager
        importManager = new ImportManager(context, addressRepository, deliveryRepository, syncRepository, mapFragment);
        
        // Initialize usage tracker
        usageTracker = UsageTracker.getInstance(context);
    }
    
    /**
     * Check if the user can add new mapping
     * 
     * @return true if the user can add mapping, false otherwise
     */
    public boolean canAddMapping() {
        return usageTracker != null && (usageTracker.canAddMapping() || usageTracker.isPro());
    }
    
    /**
     * Import from GeoJSON file with options for duplicate handling
     * 
     * @param uri URI of the GeoJSON file
     * @param skipDuplicates Whether to skip duplicate records
     * @param updateExisting Whether to update existing records
     */
    public void importFromGeoJson(Uri uri, boolean skipDuplicates, boolean updateExisting) {
        if (importManager == null) {
            setError(new IllegalStateException("ImportManager not initialized"));
            return;
        }
        
        importingLiveData.setValue(true);
        statusMessageLiveData.setValue("Importing GeoJSON data...");
        
        disposables.add(
            // Execute in a Single to prevent blocking
            io.reactivex.Single.fromCallable(() -> {
                final ImportResult[] result = new ImportResult[1];
                
                // Perform import using ImportManager
                importManager.importFromGeoJson(uri, skipDuplicates, updateExisting, new ImportManager.ImportListener() {
                    @Override
                    public void onImportCompleted(int newCount, int updatedCount, int duplicateCount, int invalidCount, List<String> warnings) {
                        result[0] = new ImportResult(true, newCount, updatedCount, duplicateCount, invalidCount, warnings, null);
                    }
                    
                    @Override
                    public void onImportFailed(String message, List<String> errors) {
                        result[0] = new ImportResult(false, 0, 0, 0, 0, null, errors);
                    }
                });
                
                // Wait for result (this is a simplified approach, in a real app you'd use callbacks properly)
                int tryCount = 0;
                while (result[0] == null && tryCount < 10) {
                    try {
                        Thread.sleep(500); // Wait for callback
                        tryCount++;
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Import interrupted", e);
                    }
                }
                
                if (result[0] == null) {
                    throw new RuntimeException("Import timed out");
                }
                
                return result[0];
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(() -> {
                importingLiveData.setValue(false);
                statusMessageLiveData.setValue("Import completed");
            })
            .subscribe(
                result -> {
                    importResultLiveData.setValue(result);
                    
                    // Record the imports in usage tracker
                    if (result.isSuccess() && usageTracker != null) {
                        int totalProcessed = result.getNewCount() + result.getUpdatedCount();
                        for (int i = 0; i < totalProcessed; i++) {
                            usageTracker.recordMapping();
                        }
                    }
                },
                error -> {
                    setError(error);
                    importResultLiveData.setValue(new ImportResult(false, 0, 0, 0, 0, null, 
                            List.of("Import failed: " + error.getMessage())));
                }
            )
        );
    }
    
    /**
     * Import from KML file
     * 
     * @param uri URI of the KML file
     */
    public void importFromKml(Uri uri) {
        if (importManager == null) {
            setError(new IllegalStateException("ImportManager not initialized"));
            return;
        }
        
        importingLiveData.setValue(true);
        statusMessageLiveData.setValue("Importing KML/KMZ data...");
        
        disposables.add(
            // Execute in a Single to prevent blocking
            io.reactivex.Single.fromCallable(() -> {
                final ImportResult[] result = new ImportResult[1];
                
                // Perform import using ImportManager
                importManager.importFromKml(uri, new ImportManager.ImportListener() {
                    @Override
                    public void onImportCompleted(int newCount, int updatedCount, int duplicateCount, int invalidCount, List<String> warnings) {
                        result[0] = new ImportResult(true, newCount, updatedCount, duplicateCount, invalidCount, warnings, null);
                    }
                    
                    @Override
                    public void onImportFailed(String message, List<String> errors) {
                        result[0] = new ImportResult(false, 0, 0, 0, 0, null, errors);
                    }
                });
                
                // Wait for result (this is a simplified approach, in a real app you'd use callbacks properly)
                int tryCount = 0;
                while (result[0] == null && tryCount < 10) {
                    try {
                        Thread.sleep(500); // Wait for callback
                        tryCount++;
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Import interrupted", e);
                    }
                }
                
                if (result[0] == null) {
                    throw new RuntimeException("Import timed out");
                }
                
                return result[0];
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(() -> {
                importingLiveData.setValue(false);
                statusMessageLiveData.setValue("Import completed");
            })
            .subscribe(
                result -> {
                    importResultLiveData.setValue(result);
                    
                    // Record the imports in usage tracker
                    if (result.isSuccess() && usageTracker != null) {
                        int totalProcessed = result.getNewCount() + result.getUpdatedCount();
                        for (int i = 0; i < totalProcessed; i++) {
                            usageTracker.recordMapping();
                        }
                    }
                },
                error -> {
                    setError(error);
                    importResultLiveData.setValue(new ImportResult(false, 0, 0, 0, 0, null, 
                            List.of("Import failed: " + error.getMessage())));
                }
            )
        );
    }
    
    /**
     * Import from CSV file
     * 
     * @param uri URI of the CSV file
     */
    public void importFromCsv(Uri uri) {
        if (importManager == null) {
            setError(new IllegalStateException("ImportManager not initialized"));
            return;
        }
        
        importingLiveData.setValue(true);
        statusMessageLiveData.setValue("Importing CSV data...");
        
        disposables.add(
            // Execute in a Single to prevent blocking
            io.reactivex.Single.fromCallable(() -> {
                final ImportResult[] result = new ImportResult[1];
                
                // Perform import using ImportManager
                importManager.importFromCsv(uri, new ImportManager.ImportListener() {
                    @Override
                    public void onImportCompleted(int newCount, int updatedCount, int duplicateCount, int invalidCount, List<String> warnings) {
                        result[0] = new ImportResult(true, newCount, updatedCount, duplicateCount, invalidCount, warnings, null);
                    }
                    
                    @Override
                    public void onImportFailed(String message, List<String> errors) {
                        result[0] = new ImportResult(false, 0, 0, 0, 0, null, errors);
                    }
                });
                
                // Wait for result (this is a simplified approach, in a real app you'd use callbacks properly)
                int tryCount = 0;
                while (result[0] == null && tryCount < 10) {
                    try {
                        Thread.sleep(500); // Wait for callback
                        tryCount++;
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Import interrupted", e);
                    }
                }
                
                if (result[0] == null) {
                    throw new RuntimeException("Import timed out");
                }
                
                return result[0];
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(() -> {
                importingLiveData.setValue(false);
                statusMessageLiveData.setValue("Import completed");
            })
            .subscribe(
                result -> {
                    importResultLiveData.setValue(result);
                    
                    // Record the imports in usage tracker
                    if (result.isSuccess() && usageTracker != null) {
                        int totalProcessed = result.getNewCount() + result.getUpdatedCount();
                        for (int i = 0; i < totalProcessed; i++) {
                            usageTracker.recordMapping();
                        }
                    }
                },
                error -> {
                    setError(error);
                    importResultLiveData.setValue(new ImportResult(false, 0, 0, 0, 0, null, 
                            List.of("Import failed: " + error.getMessage())));
                }
            )
        );
    }
    
    /**
     * Get importing state as LiveData
     */
    public LiveData<Boolean> isImporting() {
        return importingLiveData;
    }
    
    /**
     * Get status message as LiveData
     */
    public LiveData<String> getStatusMessage() {
        return statusMessageLiveData;
    }
    
    /**
     * Get import result as LiveData
     */
    public LiveData<ImportResult> getImportResult() {
        return importResultLiveData;
    }
    
    /**
     * Clean up resources
     */
    @Override
    protected void onCleared() {
        if (importManager != null) {
            importManager.dispose();
        }
        
        super.onCleared();
    }
    
    /**
     * Class representing the result of an import operation
     */
    public static class ImportResult {
        private final boolean success;
        private final int newCount;
        private final int updatedCount;
        private final int duplicateCount;
        private final int invalidCount;
        private final List<String> warnings;
        private final List<String> errors;
        
        public ImportResult(boolean success, int newCount, int updatedCount, int duplicateCount, 
                           int invalidCount, List<String> warnings, List<String> errors) {
            this.success = success;
            this.newCount = newCount;
            this.updatedCount = updatedCount;
            this.duplicateCount = duplicateCount;
            this.invalidCount = invalidCount;
            this.warnings = warnings;
            this.errors = errors;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
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
        
        public List<String> getErrors() {
            return errors;
        }
        
        public int getTotalProcessed() {
            return newCount + updatedCount;
        }
    }
}