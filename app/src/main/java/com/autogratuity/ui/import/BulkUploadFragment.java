package com.autogratuity.ui.import_;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.ProSubscribeActivity;
import com.autogratuity.R;
import com.autogratuity.ui.map.MapFragment;
import com.autogratuity.utils.UsageTracker;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Fragment for bulk uploading delivery data from external sources
 * Updated to use ViewModel architecture pattern
 */
public class BulkUploadFragment extends Fragment {
    private static final String TAG = "BulkUploadFragment";
    private static final int REQUEST_KML_FILE = 101;
    private static final int REQUEST_GEOJSON_FILE = 102;
    private static final int REQUEST_CSV_FILE = 103;
    
    // ViewModel
    private BulkUploadViewModel viewModel;
    
    // UI elements
    private Button importKmlButton;
    private Button importGeoJsonButton;
    private Button importCsvButton;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private LinearLayout resultsContainer;
    private MaterialCardView instructionsCard;
    
    /**
     * Create a new instance of the fragment
     */
    public static BulkUploadFragment newInstance() {
        return new BulkUploadFragment();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(BulkUploadViewModel.class);
        
        // Get reference to map fragment to update after imports
        MapFragment mapFragment = null;
        try {
            mapFragment = (MapFragment) getParentFragmentManager()
                    .findFragmentByTag("map_fragment");
        } catch (Exception e) {
            // Ignore if map fragment not found
        }
        
        // Initialize managers in ViewModel
        viewModel.initializeManagers(requireContext(), mapFragment);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bulk_upload, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        importKmlButton = view.findViewById(R.id.import_kml_button);
        importGeoJsonButton = view.findViewById(R.id.import_geojson_button);
        importCsvButton = view.findViewById(R.id.import_csv_button);
        statusTextView = view.findViewById(R.id.status_text);
        progressBar = view.findViewById(R.id.progress_bar);
        resultsContainer = view.findViewById(R.id.results_container);
        instructionsCard = view.findViewById(R.id.instructions_card);
        
        // Set up UI event handlers
        setupUI();
        
        // Observe ViewModel
        observeViewModel();
        
        // Initialize with empty status
        updateStatus("", false);
    }
    
    /**
     * Set up UI event handlers
     */
    private void setupUI() {
        // Set up KML import button
        importKmlButton.setOnClickListener(v -> {
            // Check if user has available mappings
            if (!viewModel.canAddMapping()) {
                showFreeTierLimitReachedDialog();
                return;
            }
            
            openFileChooser(REQUEST_KML_FILE, "application/vnd.google-earth.kml+xml", "application/vnd.google-earth.kmz");
        });
        
        // Set up GeoJSON import button
        importGeoJsonButton.setOnClickListener(v -> {
            // Check if user has available mappings
            if (!viewModel.canAddMapping()) {
                showFreeTierLimitReachedDialog();
                return;
            }
            
            openFileChooser(REQUEST_GEOJSON_FILE, "application/json", "application/geo+json");
        });
        
        // Set up CSV import button
        if (importCsvButton != null) {
            importCsvButton.setOnClickListener(v -> {
                // Check if user has available mappings
                if (!viewModel.canAddMapping()) {
                    showFreeTierLimitReachedDialog();
                    return;
                }
                
                openFileChooser(REQUEST_CSV_FILE, "text/csv", "application/vnd.ms-excel");
            });
        }
    }
    
    /**
     * Observe LiveData from ViewModel
     */
    private void observeViewModel() {
        // Observe importing state
        viewModel.isImporting().observe(getViewLifecycleOwner(), isImporting -> {
            updateStatus(
                viewModel.getStatusMessage().getValue() != null ? 
                viewModel.getStatusMessage().getValue() : "", 
                isImporting
            );
        });
        
        // Observe status message
        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            updateStatus(message, viewModel.isImporting().getValue() != null && viewModel.isImporting().getValue());
        });
        
        // Observe import result
        viewModel.getImportResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    showEnhancedImportResults(
                        result.getNewCount(), 
                        result.getUpdatedCount(),
                        result.getDuplicateCount(),
                        result.getInvalidCount(),
                        result.getWarnings()
                    );
                } else {
                    showError("Import failed: " + (result.getErrors() != null && !result.getErrors().isEmpty() ? 
                                                result.getErrors().get(0) : "Unknown error"));
                }
            }
        });
        
        // Observe error state
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError("Error: " + error.getMessage());
            }
        });
        
        // Observe toast messages
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Open file chooser with specific MIME types
     */
    private void openFileChooser(int requestCode, String... mimeTypes) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, requestCode);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        
        Uri fileUri = data.getData();
        if (fileUri == null) {
            showError("Could not open file");
            return;
        }
        
        // Get persistent permission to access this file
        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    fileUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (SecurityException e) {
            // Continue anyway as we might not need persistent access
        }
        
        switch (requestCode) {
            case REQUEST_KML_FILE:
                handleKmlImport(fileUri);
                break;
                
            case REQUEST_GEOJSON_FILE:
                showDuplicateCheckDialog(fileUri);
                break;
                
            case REQUEST_CSV_FILE:
                handleCsvImport(fileUri);
                break;
        }
    }
    
    /**
     * Show dialog to choose how to handle duplicates
     */
    private void showDuplicateCheckDialog(Uri fileUri) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Duplicate Management")
                .setMessage("How would you like to handle potential duplicate entries?")
                .setPositiveButton("Update Existing Records", (dialog, which) -> {
                    // Import and update existing records
                    startGeoJsonImport(fileUri, false, true);
                })
                .setNegativeButton("Skip Duplicates", (dialog, which) -> {
                    // Import and skip duplicates
                    startGeoJsonImport(fileUri, true, false);
                })
                .setNeutralButton("Cancel", null)
                .show();
    }
    
    /**
     * Handle KML/KMZ file import
     */
    private void handleKmlImport(Uri fileUri) {
        // Show import confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Import from KML/KMZ")
                .setMessage("Import delivery data from this file? This will add locations to your Autogratuity database.")
                .setPositiveButton("Import", (dialog, which) -> {
                    // Clear results container
                    resultsContainer.removeAllViews();
                    
                    // Start import process using ViewModel
                    viewModel.importFromKml(fileUri);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Start GeoJSON import with duplicate handling options
     */
    private void startGeoJsonImport(Uri fileUri, boolean skipDuplicates, boolean updateExisting) {
        // Clear results container
        resultsContainer.removeAllViews();
        
        // Start import process using ViewModel
        viewModel.importFromGeoJson(fileUri, skipDuplicates, updateExisting);
    }
    
    /**
     * Handle CSV file import
     */
    private void handleCsvImport(Uri fileUri) {
        // Show import confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Import from CSV")
                .setMessage("Import delivery data from this CSV file? This will add locations to your Autogratuity database.")
                .setPositiveButton("Import", (dialog, which) -> {
                    // Clear results container
                    resultsContainer.removeAllViews();
                    
                    // Start import process using ViewModel
                    viewModel.importFromCsv(fileUri);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Update the status text and progress bar
     */
    private void updateStatus(String message, boolean showProgress) {
        if (message.isEmpty()) {
            statusTextView.setVisibility(View.GONE);
        } else {
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText(message);
        }
        
        progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
    }
    
    /**
     * Show enhanced import results with detailed statistics
     */
    private void showEnhancedImportResults(int newCount, int updatedCount, 
                                          int duplicateCount, int invalidCount,
                                          List<String> warnings) {
        // Create result summary
        View resultView = getLayoutInflater().inflate(R.layout.item_import_result, resultsContainer, false);
        TextView titleTextView = resultView.findViewById(R.id.result_title);
        TextView detailsTextView = resultView.findViewById(R.id.result_details);
        
        titleTextView.setText("Import Results");
        
        StringBuilder details = new StringBuilder();
        details.append("New records: ").append(newCount).append("\n");
        details.append("Updated records: ").append(updatedCount).append("\n");
        details.append("Skipped duplicates: ").append(duplicateCount).append("\n");
        details.append("Invalid records: ").append(invalidCount).append("\n");
        
        // Add warnings if any
        if (warnings != null && !warnings.isEmpty()) {
            details.append("\nWarnings:\n");
            for (int i = 0; i < Math.min(5, warnings.size()); i++) {
                details.append("- ").append(warnings.get(i)).append("\n");
            }
            
            if (warnings.size() > 5) {
                details.append("- And ").append(warnings.size() - 5).append(" more...\n");
            }
        }
        
        detailsTextView.setText(details.toString());
        resultsContainer.addView(resultView);
        
        // Hide instructions card
        instructionsCard.setVisibility(View.GONE);
        
        // Show toast with summary
        int totalProcessed = newCount + updatedCount;
        Toast.makeText(
                requireContext(),
                String.format("Processed %d locations (%d new, %d updated)", 
                             totalProcessed, newCount, updatedCount),
                Toast.LENGTH_LONG
        ).show();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        View errorView = getLayoutInflater().inflate(R.layout.item_import_error, resultsContainer, false);
        TextView errorTextView = errorView.findViewById(R.id.error_text);
        errorTextView.setText(message);
        resultsContainer.addView(errorView);
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Show dialog when free tier limit is reached
     */
    private void showFreeTierLimitReachedDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Free Tier Limit Reached")
                .setMessage("You've reached the " + UsageTracker.FREE_TIER_MAPPING_LIMIT +
                        " delivery mapping limit for the free tier. Upgrade to Pro for unlimited " +
                        "mappings and automatic order capture!")
                .setPositiveButton("Upgrade to Pro", (dialog, which) -> {
                    // Navigate to subscription activity
                    Intent intent = new Intent(requireContext(), ProSubscribeActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}