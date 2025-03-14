package com.autogratuity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autogratuity.models.Delivery;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TestFirebaseActivity extends AppCompatActivity {
    private static final String TAG = "FirebaseTest";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button testButton;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_firebase);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        testButton = findViewById(R.id.test_firebase_button);
        resultTextView = findViewById(R.id.test_result_text);

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            resultTextView.setText("Please log in first");
            testButton.setEnabled(false);
            return;
        }

        // Set up test button
        testButton.setOnClickListener(v -> testFirebaseStructure());
    }

    /**
     * Tests the new Firebase nested structure and model classes
     */
    private void testFirebaseStructure() {
        // Update UI
        resultTextView.setText("Starting Firebase structure test...");
        testButton.setEnabled(false);

        Log.d(TAG, "Starting Firebase structure test...");
        appendTestResult("Starting Firebase structure test...");

        // 1. Create a test delivery with the new structure
        Delivery testDelivery = new Delivery("TEST-" + System.currentTimeMillis(),
                "123 Test Street, Testville, TS 12345", Timestamp.now());
        testDelivery.setUserId(mAuth.getCurrentUser().getUid());
        testDelivery.setTipAmount(7.50);
        testDelivery.setTipDate(Timestamp.now());
        testDelivery.setCoordinates("41.684084,-93.760542");
        testDelivery.setSource("test");

        // 2. Save to Firestore using the new structure
        Log.d(TAG, "Creating test delivery document...");
        appendTestResult("Creating test delivery document...");

        db.collection("deliveries")
                .add(testDelivery.toDocument())
                .addOnSuccessListener(documentReference -> {
                    String deliveryId = documentReference.getId();
                    Log.d(TAG, "Created test delivery with ID: " + deliveryId);
                    appendTestResult("Created test delivery with ID: " + deliveryId);

                    // 3. Read it back to test fromDocument
                    documentReference.get()
                            .addOnSuccessListener(documentSnapshot -> {
                                Log.d(TAG, "Retrieved document for verification");
                                appendTestResult("Retrieved document for verification");

                                Delivery readDelivery = Delivery.fromDocument(documentSnapshot);

                                // 4. Verify the data was correctly stored and retrieved
                                Log.d(TAG, "Verification results:");
                                Log.d(TAG, "Order ID: " + readDelivery.getOrderId());
                                Log.d(TAG, "Address: " + readDelivery.getAddress());
                                Log.d(TAG, "Tip Amount: " + readDelivery.getTipAmount());
                                Log.d(TAG, "Coordinates: " + readDelivery.getCoordinates());
                                Log.d(TAG, "UserId: " + readDelivery.getUserId());

                                appendTestResult("\nVerification results:");
                                appendTestResult("Order ID: " + readDelivery.getOrderId());
                                appendTestResult("Address: " + readDelivery.getAddress());
                                appendTestResult("Tip Amount: " + readDelivery.getTipAmount());
                                appendTestResult("Coordinates: " + readDelivery.getCoordinates());

                                // 5. Test updating the document
                                Map<String, Object> updates = new HashMap<>();
                                Map<String, Object> status = new HashMap<>();
                                status.put("isCompleted", true);
                                updates.put("status", status);

                                appendTestResult("\nUpdating document with nested field...");

                                documentReference.update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Updated document with nested field");
                                            appendTestResult("Updated document with nested field");

                                            // 6. Test query with nested field
                                            testNestedQuery(documentReference);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error updating document", e);
                                            appendTestResult("Error updating document: " + e.getMessage());
                                            testButton.setEnabled(true);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error retrieving document", e);
                                appendTestResult("Error retrieving document: " + e.getMessage());
                                testButton.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating test document", e);
                    appendTestResult("Error creating test document: " + e.getMessage());
                    testButton.setEnabled(true);
                });
    }

    /**
     * Tests queries with nested fields
     */
    private void testNestedQuery(DocumentReference testDoc) {
        Log.d(TAG, "Testing queries with nested fields...");
        appendTestResult("\nTesting queries with nested fields...");

        // Query for documents where status.isCompleted is true
        db.collection("deliveries")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .whereEqualTo("status.isCompleted", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() +
                            " documents with status.isCompleted=true");

                    appendTestResult("Found " + queryDocumentSnapshots.size() +
                            " documents with status.isCompleted=true");

                    // Test another query with amounts
                    db.collection("deliveries")
                            .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                            .whereGreaterThan("amounts.tipAmount", 5.0)
                            .get()
                            .addOnSuccessListener(tipQuerySnapshot -> {
                                Log.d(TAG, "Found " + tipQuerySnapshot.size() +
                                        " documents with amounts.tipAmount > 5.0");

                                appendTestResult("Found " + tipQuerySnapshot.size() +
                                        " documents with amounts.tipAmount > 5.0");

                                // Clean up test document
                                cleanupTestDocument(testDoc);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error executing tip amount query", e);
                                appendTestResult("Error executing tip amount query: " + e.getMessage());
                                testButton.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error executing status query", e);
                    appendTestResult("Error executing status query: " + e.getMessage());
                    testButton.setEnabled(true);
                });
    }

    /**
     * Clean up the test document after tests complete
     */
    private void cleanupTestDocument(DocumentReference testDoc) {
        // Optional: Delete the test document to clean up
        testDoc.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Test document deleted successfully");
                    appendTestResult("\nTest document deleted successfully");
                    appendTestResult("\n✅ All tests completed successfully!");

                    Toast.makeText(TestFirebaseActivity.this,
                            "Firebase structure tests passed!",
                            Toast.LENGTH_SHORT).show();

                    testButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting test document", e);
                    appendTestResult("Error deleting test document: " + e.getMessage());
                    appendTestResult("\n⚠️ Tests completed with cleanup error");
                    testButton.setEnabled(true);
                });
    }

    /**
     * Append text to the result TextView
     */
    private void appendTestResult(String text) {
        runOnUiThread(() -> {
            resultTextView.append("\n" + text);
            // Scroll to the bottom
            final int scrollAmount = resultTextView.getLayout().getLineTop(resultTextView.getLineCount())
                    - resultTextView.getHeight();
            if (scrollAmount > 0) {
                resultTextView.scrollTo(0, scrollAmount);
            } else {
                resultTextView.scrollTo(0, 0);
            }
        });
    }
}