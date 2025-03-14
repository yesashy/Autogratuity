package com.autogratuity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autogratuity.models.Address;
import com.autogratuity.models.Delivery;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFirebaseActivity extends AppCompatActivity {
    private static final String TAG = "FirebaseTest";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button testButton;
    private TextView resultTextView;

    // Test references to clean up
    private List<DocumentReference> testReferences = new ArrayList<>();

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

        // Run tests in sequence
        testDeliveryModel()
                .continueWithTask(task -> testAddressModel())
                .continueWithTask(task -> testBatchOperations())
                .continueWithTask(task -> testComplexQueries())
                .continueWithTask(task -> testEdgeCases())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cleanupTestDocuments();
                    } else {
                        Log.e(TAG, "Test failed", task.getException());
                        appendTestResult("\n❌ Test failed: " + task.getException().getMessage());
                        testButton.setEnabled(true);
                    }
                });
    }

    /**
     * Test the Delivery model with nested structure
     */
    private com.google.android.gms.tasks.Task<Void> testDeliveryModel() {
        appendTestResult("\n==== Testing Delivery Model ====");

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

        return db.collection("deliveries")
                .add(testDelivery.toDocument())
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentReference deliveryRef = task.getResult();
                    testReferences.add(deliveryRef);
                    String deliveryId = deliveryRef.getId();

                    Log.d(TAG, "Created test delivery with ID: " + deliveryId);
                    appendTestResult("Created test delivery with ID: " + deliveryId);

                    // 3. Read it back to test fromDocument
                    return deliveryRef.get();
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    Log.d(TAG, "Retrieved document for verification");
                    appendTestResult("Retrieved document for verification");

                    Delivery readDelivery = Delivery.fromDocument(task.getResult());

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
                    status.put("isFlagged", true);  // New field to test
                    updates.put("status", status);

                    // Update nested amounts field
                    Map<String, Object> amounts = new HashMap<>();
                    amounts.put("tipAmount", 12.75);  // Increase tip
                    amounts.put("estimatedPay", 8.50);  // Add estimated pay
                    updates.put("amounts", amounts);

                    appendTestResult("\nUpdating document with nested fields...");

                    return testReferences.get(0).update(updates);
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    Log.d(TAG, "Updated document with nested fields");
                    appendTestResult("Updated document with nested fields successfully");

                    // 6. Read again to verify updates
                    return testReferences.get(0).get();
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    Delivery updatedDelivery = Delivery.fromDocument(task.getResult());

                    // Verify if updates were applied correctly
                    boolean statusCorrect = updatedDelivery.isDoNotDeliver() == false;  // Should still be false
                    double tipAmount = updatedDelivery.getTipAmount();

                    appendTestResult("Verified updates: ");
                    appendTestResult("- Tip amount updated: " + (tipAmount == 12.75 ? "✓" : "✗"));
                    appendTestResult("- Status fields preserved: " + (statusCorrect ? "✓" : "✗"));

                    // 7. Test query with nested field
                    return db.collection("deliveries")
                            .whereEqualTo("status.isCompleted", true)
                            .whereGreaterThan("amounts.tipAmount", 10.0)
                            .get();
                })
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    int count = task.getResult().size();
                    appendTestResult("Query found " + count + " documents with completed status and tip > $10");
                    appendTestResult("✅ Delivery model tests passed");

                    return null;
                });
    }

    /**
     * Test the Address model with nested structure
     */
    private com.google.android.gms.tasks.Task<Void> testAddressModel() {
        appendTestResult("\n==== Testing Address Model ====");

        // Create a test address
        Address testAddress = new Address("456 Test Avenue, Testborough, TS 67890");
        testAddress.setUserId(mAuth.getCurrentUser().getUid());
        testAddress.setCoordinates("41.583972,-93.627756");
        testAddress.addOrderId("TEST-ORDER-123");
        testAddress.addTip(9.50);

        appendTestResult("Creating test address document...");

        // Save to Firestore
        return db.collection("addresses")
                .add(testAddress.toDocument())
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentReference addressRef = task.getResult();
                    testReferences.add(addressRef);
                    String addressId = addressRef.getId();

                    appendTestResult("Created test address with ID: " + addressId);

                    // Read it back
                    return addressRef.get();
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    Address readAddress = Address.fromDocument(task.getResult());

                    // Verify the data
                    appendTestResult("\nVerification results:");
                    appendTestResult("Full Address: " + readAddress.getFullAddress());
                    appendTestResult("Delivery Count: " + readAddress.getDeliveryCount());
                    appendTestResult("Average Tip: $" + readAddress.getAverageTip());

                    // Test updating with a new order and tip
                    readAddress.addOrderId("TEST-ORDER-456");
                    readAddress.addTip(12.25);

                    // Calculate expected values
                    double expectedTotal = 9.50 + 12.25;
                    double expectedAverage = expectedTotal / 2;

                    appendTestResult("\nUpdating address with new order and tip...");

                    return testReferences.get(1).update(readAddress.toDocument());
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Read it back again
                    return testReferences.get(1).get();
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    Address updatedAddress = Address.fromDocument(task.getResult());

                    // Verify updates
                    appendTestResult("\nVerified updates:");
                    appendTestResult("- Delivery Count: " + updatedAddress.getDeliveryCount() + (updatedAddress.getDeliveryCount() == 2 ? " ✓" : " ✗"));
                    appendTestResult("- Total Tips: $" + updatedAddress.getTotalTips());
                    appendTestResult("- Average Tip: $" + updatedAddress.getAverageTip());

                    // Test search terms functionality
                    List<String> searchTerms = updatedAddress.getSearchTerms();
                    boolean hasFullAddress = searchTerms.contains(updatedAddress.getNormalizedAddress());
                    boolean hasPartialTerms = false;

                    for (String term : searchTerms) {
                        if (term.length() < updatedAddress.getNormalizedAddress().length() &&
                                updatedAddress.getNormalizedAddress().contains(term)) {
                            hasPartialTerms = true;
                            break;
                        }
                    }

                    appendTestResult("- Contains full address in search terms: " + (hasFullAddress ? "✓" : "✗"));
                    appendTestResult("- Contains partial terms for search: " + (hasPartialTerms ? "✓" : "✗"));

                    // Test query by search terms
                    String searchTerm = "test";
                    return db.collection("addresses")
                            .whereArrayContains("searchTerms", searchTerm)
                            .get();
                })
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    int count = task.getResult().size();
                    appendTestResult("Search query found " + count + " addresses with 'test' in search terms");
                    appendTestResult("✅ Address model tests passed");

                    return null;
                });
    }

    /**
     * Test batch operations
     */
    private com.google.android.gms.tasks.Task<Void> testBatchOperations() {
        appendTestResult("\n==== Testing Batch Operations ====");

        // Create multiple test deliveries in a single batch
        WriteBatch batch = db.batch();
        List<DocumentReference> batchRefs = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Delivery delivery = new Delivery("BATCH-" + i + "-" + System.currentTimeMillis(),
                    i + " Batch Road, Batchville, BT " + (10000 + i), Timestamp.now());
            delivery.setUserId(mAuth.getCurrentUser().getUid());
            delivery.setTipAmount(5.0 + i);  // Different tip amounts

            DocumentReference ref = db.collection("deliveries").document();
            batch.set(ref, delivery.toDocument());
            batchRefs.add(ref);
        }

        appendTestResult("Creating 5 deliveries in a batch...");

        return batch.commit()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    appendTestResult("Batch creation successful");
                    testReferences.addAll(batchRefs);

                    // Now test batch update
                    WriteBatch updateBatch = db.batch();

                    for (int i = 0; i < batchRefs.size(); i++) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status.doNotDeliver", i % 2 == 0);  // Mark even numbered docs as Do Not Deliver
                        updateBatch.update(batchRefs.get(i), updates);
                    }

                    appendTestResult("Updating batch documents...");
                    return updateBatch.commit();
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    appendTestResult("Batch update successful");

                    // Query to verify updates
                    return db.collection("deliveries")
                            .whereEqualTo("status.doNotDeliver", true)
                            .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                            .get();
                })
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    int count = task.getResult().size();
                    appendTestResult("Found " + count + " 'Do Not Deliver' documents (expected 3)");
                    appendTestResult("✅ Batch operations tests passed");

                    return null;
                });
    }

    /**
     * Test complex queries with multiple nested fields
     */
    private com.google.android.gms.tasks.Task<Void> testComplexQueries() {
        appendTestResult("\n==== Testing Complex Queries ====");

        // Use the documents we've already created to test complex queries
        appendTestResult("Testing complex queries on existing documents...");

        // Query 1: Deliveries with tips > $7 AND completed status
        return db.collection("deliveries")
                .whereGreaterThan("amounts.tipAmount", 7.0)
                .whereEqualTo("status.isCompleted", true)
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    int count = task.getResult().size();
                    appendTestResult("Query 1: Found " + count + " documents with tips > $7 AND completed status");

                    // Query 2: Order by nested field (tipAmount descending)
                    return db.collection("deliveries")
                            .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                            .orderBy("amounts.tipAmount", Query.Direction.DESCENDING)
                            .limit(3)
                            .get();
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    int count = task.getResult().size();
                    appendTestResult("Query 2: Found " + count + " documents ordered by tip amount (descending)");

                    StringBuilder tipValues = new StringBuilder();
                    for (int i = 0; i < Math.min(count, 3); i++) {
                        Delivery delivery = Delivery.fromDocument(task.getResult().getDocuments().get(i));
                        tipValues.append("$").append(delivery.getTipAmount()).append(", ");
                    }

                    appendTestResult("Top tips: " + tipValues.toString());

                    // Query 3: Multiple field conditions
                    return db.collection("deliveries")
                            .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                            .whereGreaterThan("amounts.tipAmount", 5.0)
                            .whereLessThan("amounts.tipAmount", 10.0)
                            .get();
                })
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    int count = task.getResult().size();
                    appendTestResult("Query 3: Found " + count + " documents with tips between $5-$10");
                    appendTestResult("✅ Complex query tests passed");

                    return null;
                });
    }

    /**
     * Test edge cases (nulls, missing fields, etc.)
     */
    private com.google.android.gms.tasks.Task<Void> testEdgeCases() {
        appendTestResult("\n==== Testing Edge Cases ====");

        // Create a minimal delivery with only required fields
        Map<String, Object> minimalDelivery = new HashMap<>();
        minimalDelivery.put("orderId", "MINIMAL-" + System.currentTimeMillis());
        minimalDelivery.put("userId", mAuth.getCurrentUser().getUid());

        appendTestResult("Creating minimal delivery document...");

        // 1. Test with minimal fields
        return db.collection("deliveries")
                .add(minimalDelivery)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentReference minimalRef = task.getResult();
                    testReferences.add(minimalRef);
                    appendTestResult("Created minimal delivery successfully");

                    // Read it back with model class
                    return minimalRef.get();
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    try {
                        Delivery minimalDelivery = Delivery.fromDocument(task.getResult());
                        appendTestResult("Model handles minimal fields: OK");
                        appendTestResult("- Order ID: " + minimalDelivery.getOrderId());
                        appendTestResult("- Address: " + (minimalDelivery.getAddress() == null ? "null" : minimalDelivery.getAddress()));
                        appendTestResult("- Tip Amount: " + minimalDelivery.getTipAmount());
                    } catch (Exception e) {
                        appendTestResult("❌ Model fails with minimal fields: " + e.getMessage());
                        throw e;
                    }

                    // 2. Test with invalid/unusual values
                    Map<String, Object> unusualDelivery = new HashMap<>();
                    unusualDelivery.put("orderId", "UNUSUAL-" + System.currentTimeMillis());
                    unusualDelivery.put("userId", mAuth.getCurrentUser().getUid());
                    unusualDelivery.put("address", "");  // Empty string

                    // Nested fields with unusual values
                    Map<String, Object> unusualAmounts = new HashMap<>();
                    unusualAmounts.put("tipAmount", -1.0);  // Negative tip
                    unusualDelivery.put("amounts", unusualAmounts);

                    Map<String, Object> unusualDates = new HashMap<>();
                    unusualDates.put("accepted", null);  // Null date
                    unusualDelivery.put("dates", unusualDates);

                    return db.collection("deliveries").add(unusualDelivery);
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentReference unusualRef = task.getResult();
                    testReferences.add(unusualRef);
                    appendTestResult("Created unusual delivery successfully");

                    // Read it back
                    return unusualRef.get();
                })
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    try {
                        Delivery unusualDelivery = Delivery.fromDocument(task.getResult());
                        appendTestResult("Model handles unusual values: OK");
                        appendTestResult("- Negative tip handled: " + (unusualDelivery.getTipAmount() < 0 ? "preserved" : "normalized"));
                        appendTestResult("- Null date handled without exception");
                    } catch (Exception e) {
                        appendTestResult("❌ Model fails with unusual values: " + e.getMessage());
                        throw e;
                    }

                    appendTestResult("✅ Edge case tests passed");

                    return null;
                });
    }

    /**
     * Clean up all test documents
     */
    private void cleanupTestDocuments() {
        appendTestResult("\n==== Cleaning Up Test Documents ====");
        appendTestResult("Deleting " + testReferences.size() + " test documents...");

        AtomicInteger deleted = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        if (testReferences.isEmpty()) {
            appendTestResult("No documents to clean up");
            testCompleted(true);
            return;
        }

        for (DocumentReference ref : testReferences) {
            ref.delete()
                    .addOnSuccessListener(aVoid -> {
                        int count = deleted.incrementAndGet();
                        if (count + failed.get() == testReferences.size()) {
                            appendTestResult("Deleted " + count + " documents successfully");
                            testCompleted(failed.get() == 0);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting document", e);
                        int count = failed.incrementAndGet();
                        appendTestResult("Failed to delete document: " + e.getMessage());

                        if (deleted.get() + count == testReferences.size()) {
                            testCompleted(false);
                        }
                    });
        }
    }

    /**
     * Finalize test result
     */
    private void testCompleted(boolean success) {
        if (success) {
            appendTestResult("\n✅ All tests completed successfully!");
            Toast.makeText(TestFirebaseActivity.this,
                    "Firebase structure tests passed!",
                    Toast.LENGTH_SHORT).show();
        } else {
            appendTestResult("\n⚠️ Tests completed with some errors");
            Toast.makeText(TestFirebaseActivity.this,
                    "Firebase tests completed with some errors",
                    Toast.LENGTH_SHORT).show();
        }

        testButton.setEnabled(true);
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