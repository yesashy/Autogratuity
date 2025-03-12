package com.autogratuity.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes captured Shipt data and integrates it with main app database
 */
public class ShiptCaptureProcessor {
    private static final String TAG = "ShiptDataProcessor";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context context;

    public ShiptCaptureProcessor(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Process all unprocessed captures
     */
    public void processCaptures(ProcessCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onError(new Exception("User not logged in"));
            return;
        }

        // Get all unprocessed captures
        db.collection("shipt_captures")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("processed", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        if (callback != null) callback.onComplete(0);
                        return;
                    }

                    final int[] processedCount = {0};
                    final int[] errorCount = {0};
                    final int totalCount = queryDocumentSnapshots.size();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        processSingleCapture(document, new ProcessCallback() {
                            @Override
                            public void onComplete(int count) {
                                processedCount[0]++;
                                checkCompletion();
                            }

                            @Override
                            public void onError(Exception e) {
                                errorCount[0]++;
                                Log.e(TAG, "Error processing capture: " + document.getId(), e);
                                checkCompletion();
                            }

                            private void checkCompletion() {
                                if (processedCount[0] + errorCount[0] >= totalCount) {
                                    if (callback != null) {
                                        callback.onComplete(processedCount[0]);
                                    }
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting captures", e);
                    if (callback != null) callback.onError(e);
                });
    }

    /**
     * Process a single capture
     */
    private void processSingleCapture(DocumentSnapshot document, ProcessCallback callback) {
        // Skip invalid captures
        if (!document.contains("orderId")) {
            markAsProcessed(document.getReference(), false);
            if (callback != null) callback.onError(new Exception("Missing order ID"));
            return;
        }

        String orderId = document.getString("orderId");
        String location = "";

        // Determine the location string to use
        if (document.contains("address") && document.getString("address") != null) {
            location = document.getString("address");
        } else if (document.contains("location") && document.getString("location") != null) {
            location = document.getString("location");
        } else {
            // Try to construct from zone and store
            String zone = document.getString("zone");
            String store = document.getString("store");

            if (zone != null && store != null) {
                location = store + " - " + zone;
            } else if (zone != null) {
                location = zone;
            } else if (store != null) {
                location = store;
            }
        }

        // If we still don't have a location, mark as invalid
        if (location.isEmpty()) {
            markAsProcessed(document.getReference(), false);
            if (callback != null) callback.onError(new Exception("Missing location information"));
            return;
        }

        // Create or update the delivery
        addOrUpdateDelivery(orderId, location, document.getReference(), callback);
    }

    /**
     * Add or update a delivery in the main database
     */
    private void addOrUpdateDelivery(String orderId, String location, DocumentReference sourceRef, ProcessCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onError(new Exception("User not logged in"));
            return;
        }

        // Check if delivery already exists
        db.collection("deliveries")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("orderId", orderId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Update existing delivery
                        DocumentReference deliveryRef = queryDocumentSnapshots.getDocuments().get(0).getReference();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("lastUpdated", Timestamp.now());

                        // Only update address if it might be more specific (longer)
                        DocumentSnapshot existing = queryDocumentSnapshots.getDocuments().get(0);
                        String existingAddress = existing.getString("address");

                        if (existingAddress == null || (location.length() > existingAddress.length())) {
                            updates.put("address", location);
                        }

                        deliveryRef.update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    markAsProcessed(sourceRef, true);
                                    if (callback != null) callback.onComplete(1);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating delivery", e);
                                    if (callback != null) callback.onError(e);
                                });
                    } else {
                        // Create new delivery
                        Map<String, Object> delivery = new HashMap<>();
                        delivery.put("orderId", orderId);
                        delivery.put("address", location);
                        delivery.put("deliveryDate", Timestamp.now());
                        delivery.put("importDate", Timestamp.now());
                        delivery.put("userId", currentUser.getUid());
                        delivery.put("doNotDeliver", false);
                        delivery.put("source", "auto_capture");

                        db.collection("deliveries")
                                .add(delivery)
                                .addOnSuccessListener(documentReference -> {
                                    updateAddress(location, orderId);
                                    markAsProcessed(sourceRef, true);
                                    if (callback != null) callback.onComplete(1);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding delivery", e);
                                    if (callback != null) callback.onError(e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for existing delivery", e);
                    if (callback != null) callback.onError(e);
                });
    }

    /**
     * Mark a capture as processed
     */
    private void markAsProcessed(DocumentReference reference, boolean success) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("processed", true);
        updates.put("processedAt", Timestamp.now());
        updates.put("processSuccess", success);

        reference.update(updates)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking capture as processed", e);
                });
    }

    /**
     * Update or create an address entry
     */
    private void updateAddress(String fullAddress, String orderId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String normalizedAddress = normalizeAddress(fullAddress);
        String userId = currentUser.getUid();

        // Check if address exists
        db.collection("addresses")
                .whereEqualTo("userId", userId)
                .whereArrayContains("searchTerms", normalizedAddress)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Address exists, update it
                        DocumentReference addressRef = queryDocumentSnapshots.getDocuments().get(0).getReference();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("lastUpdated", Timestamp.now());
                        updates.put("orderIds", FieldValue.arrayUnion(orderId));
                        updates.put("deliveryCount", FieldValue.increment(1));

                        addressRef.update(updates)
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating address", e);
                                });
                    } else {
                        // Create new address
                        Map<String, Object> address = new HashMap<>();
                        address.put("fullAddress", fullAddress);
                        address.put("normalizedAddress", normalizedAddress);
                        address.put("orderIds", new ArrayList<String>() {{ add(orderId); }});
                        address.put("totalTips", 0.0);
                        address.put("deliveryCount", 1);
                        address.put("averageTip", 0.0);
                        address.put("userId", userId);
                        address.put("doNotDeliver", false);
                        address.put("createdAt", Timestamp.now());
                        address.put("searchTerms", generateSearchTerms(fullAddress));

                        db.collection("addresses")
                                .add(address)
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding address", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for existing address", e);
                });
    }

    /**
     * Normalize an address for better matching
     */
    private String normalizeAddress(String address) {
        return address.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")  // Remove punctuation
                .replaceAll("\\s+", " ")        // Normalize whitespace
                .trim();
    }

    /**
     * Generate search terms for fuzzy matching
     */
    private List<String> generateSearchTerms(String address) {
        List<String> terms = new ArrayList<>();
        String normalized = normalizeAddress(address);

        // Add the full normalized address
        terms.add(normalized);

        // Add individual words for partial matching
        String[] words = normalized.split("\\s+");
        for (String word : words) {
            if (word.length() > 3) {  // Only add meaningful words
                terms.add(word);
            }
        }

        // Extract house/building number if present
        if (words.length > 0 && words[0].matches("\\d+")) {
            terms.add(words[0]);
        }

        return terms;
    }

    /**
     * Callback interface for processing
     */
    public interface ProcessCallback {
        void onComplete(int count);
        void onError(Exception e);
    }
}