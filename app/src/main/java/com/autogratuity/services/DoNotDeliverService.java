package com.autogratuity.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Job service that marks orders without tips as "Do Not Deliver" after 14 days
 */
public class DoNotDeliverService extends JobService {
    private static final String TAG = "DoNotDeliverService";

    // Number of days after which to mark as "Do Not Deliver"
    private static final int DAYS_THRESHOLD = 14;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isRunning = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        isRunning = true;
        Log.d(TAG, "Starting Do Not Deliver service");

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Run the update in the background
        new Thread(() -> {
            updateOldOrders(params);
        }).start();

        // Return true to indicate the job is still running in the background
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        isRunning = false;
        Log.d(TAG, "Do Not Deliver service stopped");

        // Return true to reschedule the job if it's stopped prematurely
        return true;
    }

    private void updateOldOrders(JobParameters params) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No user logged in, skipping update");
            jobFinished(params, false);
            return;
        }

        String userId = currentUser.getUid();

        // Calculate the threshold date (14 days ago)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -DAYS_THRESHOLD);
        Date thresholdDate = calendar.getTime();
        Timestamp thresholdTimestamp = new Timestamp(thresholdDate);

        // Query for orders older than threshold with no tip
        db.collection("deliveries")
                .whereEqualTo("userId", userId)
                .whereLessThan("importDate", thresholdTimestamp)
                .whereEqualTo("doNotDeliver", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No old orders to update");
                        jobFinished(params, false);
                        return;
                    }

                    AtomicInteger pendingUpdates = new AtomicInteger(queryDocumentSnapshots.size());

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // Check if this order has a tip
                        if (document.contains("tipAmount")) {
                            // This order has a tip, so don't mark it
                            pendingUpdates.decrementAndGet();
                            continue;
                        }

                        // This order has no tip after 14 days, mark it as "Do Not Deliver"
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("doNotDeliver", true);

                        // Update the order
                        db.collection("deliveries")
                                .document(document.getId())
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Order " + document.getString("orderId") + " marked as Do Not Deliver");

                                    // Also update the associated address
                                    updateAddressForOrder(document, userId);

                                    // Check if all updates are complete
                                    if (pendingUpdates.decrementAndGet() == 0) {
                                        jobFinished(params, false);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating order", e);
                                    if (pendingUpdates.decrementAndGet() == 0) {
                                        jobFinished(params, true); // Retry on failure
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying orders", e);
                    jobFinished(params, true); // Retry on failure
                });
    }

    private void updateAddressForOrder(DocumentSnapshot orderDocument, String userId) {
        // Get the address from the order
        String address = orderDocument.getString("address");
        if (address == null) return;

        // Check if there's a coordinates key to use
        String coordinates = orderDocument.getString("coordinates");
        String addressKey = coordinates != null ? coordinates.trim() : address.toLowerCase().trim();

        // Query for the address
        db.collection("addresses")
                .whereEqualTo("userId", userId)
                .whereEqualTo("coordinatesKey", addressKey)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) return;

                    // Update the address to mark it as "Do Not Deliver"
                    DocumentSnapshot addressDocument = queryDocumentSnapshots.getDocuments().get(0);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("doNotDeliver", true);

                    db.collection("addresses")
                            .document(addressDocument.getId())
                            .update(updates)
                            .addOnSuccessListener(aVoid ->
                                    Log.d(TAG, "Address marked as Do Not Deliver: " + address)
                            )
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Error updating address", e)
                            );
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error querying address", e)
                );
    }
}