// app/src/main/java/com/autogratuity/utils/ExportManager.java
package com.autogratuity.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportManager {
    private static final String TAG = "ExportManager";
    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public ExportManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onExportError(new Exception("User not logged in"));
            return;
        }

        // Build query
        Query query = db.collection("deliveries")
                .whereEqualTo("userId", currentUser.getUid());

        // Add date filters if provided
        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("deliveryDate", new Timestamp(startDate));
        }
        if (endDate != null) {
            query = query.whereLessThanOrEqualTo("deliveryDate", new Timestamp(endDate));
        }

        // Execute query
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
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
                for (QueryDocumentSnapshot document : task.getResult()) {
                    List<String> rowData = new ArrayList<>();

                    rowData.add(document.getString("orderId"));

                    if (includeAddresses) {
                        rowData.add(document.getString("address"));
                    }

                    Timestamp deliveryDate = document.getTimestamp("deliveryDate");
                    rowData.add(deliveryDate != null ? dateFormat.format(deliveryDate.toDate()) : "");

                    Timestamp completionDate = document.getTimestamp("deliveryCompletedDate");
                    rowData.add(completionDate != null ? dateFormat.format(completionDate.toDate()) : "");

                    if (includeTips) {
                        Double tipAmount = document.getDouble("tipAmount");
                        rowData.add(tipAmount != null ? String.format("%.2f", tipAmount) : "");

                        Timestamp tipDate = document.getTimestamp("tipDate");
                        rowData.add(tipDate != null ? dateFormat.format(tipDate.toDate()) : "");
                    }

                    Boolean doNotDeliver = document.getBoolean("doNotDeliver");
                    rowData.add(doNotDeliver != null && doNotDeliver ? "Yes" : "No");

                    rows.add(rowData.toArray(new String[0]));
                }

                // Write to file
                try {
                    Uri fileUri = writeCSV(rows);
                    callback.onExportComplete(fileUri);
                } catch (IOException e) {
                    callback.onExportError(e);
                }
            } else {
                callback.onExportError(task.getException());
            }
        });
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
}