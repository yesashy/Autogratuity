// app/src/main/java/com/autogratuity/utils/ExportManager.java
package com.autogratuity.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.data.repository.preference.PreferenceRepository;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility for exporting delivery data
 * Updated to use domain repositories with RxJava
 */
public class ExportManager {
    private static final String TAG = "ExportManager";
    private final Context context;
    private final DeliveryRepository deliveryRepository;
    private final PreferenceRepository preferenceRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    /**
     * Create a new ExportManager
     *
     * @param context The application context
     * @param deliveryRepository The repository for delivery operations
     * @param preferenceRepository The repository for preferences
     */
    public ExportManager(Context context, DeliveryRepository deliveryRepository, PreferenceRepository preferenceRepository) {
        this.context = context;
        this.deliveryRepository = deliveryRepository;
        this.preferenceRepository = preferenceRepository;
    }

    public interface ExportCallback {
        void onExportComplete(Uri fileUri);
        void onExportError(Exception e);
    }

    /**
     * Export deliveries to CSV file
     * @param startDate Start date for export range (null for all)
     * @param endDate End date for export range (null for all)
     * @param includeTips Whether to include tip amounts
     * @param includeAddresses Whether to include full addresses
     * @param callback Callback for export completion
     */
    public void exportToCSV(Date startDate, Date endDate, boolean includeTips,
                            boolean includeAddresses, ExportCallback callback) {
        
        // Build query based on parameters
        disposables.add(
            (startDate != null && endDate != null ? 
                deliveryRepository.getDeliveriesByTimeRange(startDate, endDate) : 
                deliveryRepository.getAllDeliveries())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    deliveries -> {
                        try {
                            List<String[]> rows = createCSVRows(deliveries, includeTips, includeAddresses);
                            Uri fileUri = writeCSV(rows);
                            callback.onExportComplete(fileUri);
                        } catch (IOException e) {
                            callback.onExportError(e);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error exporting to CSV: " + error.getMessage());
                        callback.onExportError(new Exception("Error exporting data: " + error.getMessage()));
                    }
                )
        );
    }
    
    /**
     * Create CSV rows from delivery data
     * 
     * @param deliveries The list of deliveries to export
     * @param includeTips Whether to include tip data
     * @param includeAddresses Whether to include full addresses
     * @return List of string arrays representing CSV rows
     */
    private List<String[]> createCSVRows(List<Delivery> deliveries, boolean includeTips, boolean includeAddresses) {
        List<String[]> rows = new ArrayList<>();
        
        // Add header row
        List<String> headers = new ArrayList<>();
        headers.add("Order ID");
        if (includeAddresses) {
            headers.add("Address");
        }
        headers.add("Delivery Date");
        headers.add("Completion Date");
        if (includeTips) {
            headers.add("Tip Amount");
            headers.add("Tip Date");
        }
        headers.add("Do Not Deliver");
        
        rows.add(headers.toArray(new String[0]));
        
        // Add data rows
        for (Delivery delivery : deliveries) {
            List<String> rowData = new ArrayList<>();
            
            // Order ID
            String orderId = delivery.getMetadata() != null ? delivery.getMetadata().getOrderId() : "";
            rowData.add(orderId != null ? orderId : "");
            
            // Address
            if (includeAddresses) {
                String address = delivery.getAddress() != null ? delivery.getAddress().getFullAddress() : "";
                rowData.add(address != null ? address : "");
            }
            
            // Delivery Date
            Date deliveryDate = delivery.getTimes() != null ? delivery.getTimes().getOrderedAt() : null;
            rowData.add(deliveryDate != null ? dateFormat.format(deliveryDate) : "");
            
            // Completion Date
            Date completionDate = delivery.getTimes() != null ? delivery.getTimes().getCompletedAt() : null;
            rowData.add(completionDate != null ? dateFormat.format(completionDate) : "");
            
            // Tip info
            if (includeTips) {
                // Tip Amount
                double tipAmount = delivery.getAmounts() != null ? delivery.getAmounts().getTipAmount() : 0.0;
                rowData.add(tipAmount > 0 ? String.format("%.2f", tipAmount) : "");
                
                // Tip Date
                Date tipDate = delivery.getTimes() != null ? delivery.getTimes().getTippedAt() : null;
                rowData.add(tipDate != null ? dateFormat.format(tipDate) : "");
            }
            
            // Do Not Deliver flag
            boolean doNotDeliver = delivery.getAddress() != null && 
                    delivery.getAddress().getFlags() != null && 
                    delivery.getAddress().getFlags().isDoNotDeliver();
            rowData.add(doNotDeliver ? "Yes" : "No");
            
            rows.add(rowData.toArray(new String[0]));
        }
        
        return rows;
    }

    @NonNull
    private Uri writeCSV(List<String[]> rows) throws IOException {
        // Create file in Downloads directory
        String fileName = "autogratuity_export_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".csv";

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outputFile = new File(downloadsDir, fileName);

        // Write CSV data
        try (FileWriter writer = new FileWriter(outputFile)) {
            for (String[] row : rows) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    // Escape commas and quotes
                    String cell = row[i];
                    if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")) {
                        cell = "\"" + cell.replace("\"", "\"\"") + "\"";
                    }
                    sb.append(cell);
                }
                sb.append("\n");
                writer.write(sb.toString());
            }
        }

        // Get content URI via FileProvider
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                outputFile);
    }
    
    /**
     * Clean up resources when no longer needed
     */
    public void dispose() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }
}