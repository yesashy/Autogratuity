package com.autogratuity.fragments;

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

import com.autogratuity.R;
import com.autogratuity.utils.GeoJsonImportUtil;
import com.autogratuity.utils.KmlImportUtil;
import com.autogratuity.utils.UsageTracker;
import com.google.android.material.card.MaterialCardView;

/**
 * Fragment for bulk uploading delivery data from external sources
 */
public class BulkUploadFragment extends Fragment {
    private static final String TAG = "BulkUploadFragment";
    private static final int REQUEST_KML_FILE = 101;
    private static final int REQUEST_GEOJSON_FILE = 102;
    
    private Button importKmlButton;
    private Button importGeoJsonButton;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private LinearLayout resultsContainer;
    private MaterialCardView instructionsCard;
    
    private KmlImportUtil kmlImportUtil;
    private GeoJsonImportUtil geoJsonImportUtil;
    private UsageTracker usageTracker;
    
    /**
     * Create a new instance of the fragment
     */
    public static BulkUploadFragment newInstance() {
        return new BulkUploadFragment();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        kmlImportUtil = new KmlImportUtil(requireContext());
        geoJsonImportUtil = new GeoJsonImportUtil(requireContext());
        usageTracker = UsageTracker.getInstance(requireContext());
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
        statusTextView = view.findViewById(R.id.status_text);
        progressBar = view.findViewById(R.id.progress_bar);
        resultsContainer = view.findViewById(R.id.results_container);
        instructionsCard = view.findViewById(R.id.instructions_card);
        
        // Set up KML import button
        importKmlButton.setOnClickListener(v -> {
            // Check if user has available mappings
            if (!usageTracker.canAddMapping() && !usageTracker.isPro()) {
                showFreeTierLimitReachedDialog();
                return;
            }
            
            openFileChooser(REQUEST_KML_FILE, "application/vnd.google-earth.kml+xml", "application/vnd.google-earth.kmz");
        });
        
        // Set up GeoJSON import button
        importGeoJsonButton.setOnClickListener(v -> {
            // Check if user has available mappings
            if (!usageTracker.canAddMapping() && !usageTracker.isPro()) {
                showFreeTierLimitReachedDialog();
                return;
            }
            
            openFileChooser(REQUEST_GEOJSON_FILE, "application/json", "application/geo+json");
        });
        
        // Initialize with empty status
        updateStatus("", false);
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
                handleGeoJsonImport(fileUri);
                break;
        }
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
                    // Start import process
                    updateStatus("Importing KML/KMZ data...", true);
                    resultsContainer.removeAllViews();
                    
                    // Perform the import
                    boolean success = kmlImportUtil.importFromKmlKmz(fileUri);
                    
                    if (success) {
                        updateStatus("Processing KML/KMZ data...", true);
                        // The KmlImportUtil handles the completion asynchronously
                        // So we don't update the UI here
                    } else {
                        showError("Failed to import KML/KMZ data");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Handle GeoJSON file import
     */
    private void handleGeoJsonImport(Uri fileUri) {
        // Show import confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Import from GeoJSON")
                .setMessage("Import delivery data from this file? This will add locations with order IDs and tip information to your Autogratuity database.")
                .setPositiveButton("Import", (dialog, which) -> {
                    // Start import process
                    updateStatus("Importing GeoJSON data...", true);
                    resultsContainer.removeAllViews();
                    
                    // Perform the import
                    geoJsonImportUtil.importFromGeoJson(fileUri, new GeoJsonImportUtil.ImportCallback() {
                        @Override
                        public void onImportCompleted(GeoJsonImportUtil.ImportStatistics statistics) {
                            // Update the UI on the main thread
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    showImportResults(statistics);
                                });
                            }
                        }
                        
                        @Override
                        public void onImportFailed(String errorMessage) {
                            // Show error on the main thread
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    showError("Import failed: " + errorMessage);
                                });
                            }
                        }
                    });
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
     * Show import results
     */
    private void showImportResults(GeoJsonImportUtil.ImportStatistics stats) {
        // Update status
        updateStatus("Import completed", false);
        
        // Create result summary
        View resultView = getLayoutInflater().inflate(R.layout.item_import_result, resultsContainer, false);
        TextView titleTextView = resultView.findViewById(R.id.result_title);
        TextView detailsTextView = resultView.findViewById(R.id.result_details);
        
        titleTextView.setText("Import Results");
        
        StringBuilder details = new StringBuilder();
        details.append("Total locations: ").append(stats.getTotalFeatures()).append("\n");
        details.append("Successfully imported: ").append(stats.getSuccessfulImports()).append("\n");
        details.append("Failed imports: ").append(stats.getFailedImports()).append("\n");
        details.append("Duplicate entries: ").append(stats.getDuplicateEntries()).append("\n");
        
        // Add errors if any
        if (!stats.getErrors().isEmpty()) {
            details.append("\nErrors:\n");
            for (String error : stats.getErrors()) {
                details.append("- ").append(error).append("\n");
            }
        }
        
        detailsTextView.setText(details.toString());
        resultsContainer.addView(resultView);
        
        // Record the successful imports in usage tracker
        for (int i = 0; i < stats.getSuccessfulImports(); i++) {
            usageTracker.recordMapping();
        }
        
        // Hide instructions card
        instructionsCard.setVisibility(View.GONE);
        
        // Show toast with summary
        Toast.makeText(
                requireContext(),
                "Imported " + stats.getSuccessfulImports() + " out of " + stats.getTotalFeatures() + " locations",
                Toast.LENGTH_LONG
        ).show();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        updateStatus("Import failed", false);
        
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
                    // TODO: Direct to subscription options
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
