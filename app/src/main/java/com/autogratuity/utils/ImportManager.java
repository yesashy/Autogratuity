package com.autogratuity.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.autogratuity.fragments.MapFragment;
import com.autogratuity.models.DeliveryData;
import com.autogratuity.repositories.IFirestoreRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for handling bulk imports from various sources
 * Coordinates the import process and updates the UI afterward
 */
public class ImportManager {
    private static final String TAG = "ImportManager";
    
    private final Context context;
    private final IFirestoreRepository repository;
    private final MapFragment mapFragment;
    
    /**
     * Create a new ImportManager
     *
     * @param context The application context
     * @param repository The repository for saving data
     * @param mapFragment The map fragment to update (can be null)
     */
    public ImportManager(Context context, IFirestoreRepository repository, MapFragment mapFragment) {
        this.context = context;
        this.repository = repository;
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
            
            // Create the import utility with validation options
            GeoJsonImportUtil importUtil = new GeoJsonImportUtil(repository, skipDuplicates, updateExisting);
            boolean success = importUtil.importFromGeoJson(context, inputStream);
            
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
        // Similar to GeoJSON import but using KmlImportUtil
        // This is a placeholder - you'd need to implement KmlImportUtil
        List<String> errors = new ArrayList<>();
        errors.add("KML import not implemented yet");
        notifyFailure(listener, "KML import not implemented yet", errors);
    }
    
    /**
     * Import data from a CSV file
     *
     * @param uri URI of the CSV file
     * @param listener Listener for import completion
     */
    public void importFromCsv(Uri uri, ImportListener listener) {
        // CSV import logic would go here
        List<String> errors = new ArrayList<>();
        errors.add("CSV import not implemented yet");
        notifyFailure(listener, "CSV import not implemented yet", errors);
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