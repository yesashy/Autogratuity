package com.autogratuity.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RobustShiptAccessibilityService extends AccessibilityService {
    private static final String TAG = "ShiptAccess";

    // Simple pattern to match order IDs - adapts to different formats
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("(?:Order|order|#)\\s*(?:#)?(\\d{5,12})");

    // Pattern to match location information
    private static final Pattern ZONE_PATTERN = Pattern.compile("(?:Zone|zone)\\s*(.+)");
    private static final Pattern STORE_PATTERN = Pattern.compile("(?:Store|store)\\s*(.+)");

    // Pattern to detect delivery completion
    private static final Pattern DELIVERY_COMPLETE_PATTERN = Pattern.compile("(?:Delivery Complete|delivery complete|delivered|Delivered|Slide to complete)");

    // These stores are known Shipt partners - used to confirm we're looking at a delivery screen
    private static final String[] KNOWN_STORES = {
            "target", "hy-vee", "meijer", "cvs", "petco", "kroger", "vons", "publix",
            "h-e-b", "safeway", "shoprite", "pavilions"
    };

    // Session-level tracking variables
    private String lastCapturedOrderId = null;
    private String lastCapturedZone = null;
    private String lastCapturedStore = null;
    private String lastCapturedAddress = null;
    private long lastCaptureTimestamp = 0;
    private static final long CAPTURE_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

    // Delivery flow tracking
    private boolean inDeliveryFlow = false;
    private String currentOrderId = null;
    private boolean deliveryCompleted = false;

    // Database reference
    private FirebaseFirestore db = null;
    private FirebaseAuth mAuth = null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getSource() == null) return;

        try {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) return;

            // Get all text from the screen
            List<String> screenText = collectAllText(rootNode);
            String fullText = TextUtils.join(" ", screenText);

            // Log the text for debugging (you can remove this in production)
            Log.d(TAG, "Screen text: " + fullText.substring(0, Math.min(200, fullText.length())));

            // Check if this appears to be a Shipt order-related screen
            if (isLikelyShiptOrderScreen(screenText)) {
                // Process order claim dialog
                if (fullText.contains("Claim this order") || fullText.contains("claim this order")) {
                    processClaimDialog(screenText);
                }
                // Process order details screens
                else if (containsStoreReference(screenText)) {
                    processOrderScreen(screenText);
                }

                // Check for delivery completion screens
                detectDeliveryCompletion(screenText);
            }

            rootNode.recycle();

        } catch (Exception e) {
            Log.e(TAG, "Error processing accessibility event", e);
        }
    }

    /**
     * Check if the screen is likely related to Shipt orders
     */
    private boolean isLikelyShiptOrderScreen(List<String> screenText) {
        String fullText = TextUtils.join(" ", screenText).toLowerCase();

        // Check for key shipt-related phrases
        if (fullText.contains("shipt") ||
                fullText.contains("claim order") ||
                fullText.contains("est pay") ||
                fullText.contains("available orders") ||
                (fullText.contains("zone") && fullText.contains("pay") && fullText.contains("store"))) {
            return true;
        }

        // Check for known store names
        if (containsStoreReference(screenText)) {
            return true;
        }

        // Check for delivery-related keywords
        if (fullText.contains("slide to complete") ||
                fullText.contains("delivery complete") ||
                fullText.contains("delivered")) {
            return true;
        }

        return false;
    }

    /**
     * Check if the text contains references to known Shipt partner stores
     */
    private boolean containsStoreReference(List<String> screenText) {
        String fullText = TextUtils.join(" ", screenText).toLowerCase();

        for (String store : KNOWN_STORES) {
            if (fullText.contains(store)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Process the order claim dialog which contains the Order ID
     */
    private void processClaimDialog(List<String> screenText) {
        String fullText = TextUtils.join(" ", screenText);

        // Try to extract the order ID
        Matcher orderMatcher = ORDER_ID_PATTERN.matcher(fullText);
        if (orderMatcher.find()) {
            lastCapturedOrderId = orderMatcher.group(1);
            currentOrderId = lastCapturedOrderId; // Track current order ID for delivery completion
            lastCaptureTimestamp = System.currentTimeMillis();

            Log.d(TAG, "Captured Order ID: " + lastCapturedOrderId);

            // If we have sufficient context, store the capture
            if (lastCapturedZone != null || lastCapturedStore != null) {
                storeCapture();
            }
        }
    }

    /**
     * Process order screens to extract Zone, Store, and possibly address info
     */
    private void processOrderScreen(List<String> screenText) {
        String fullText = TextUtils.join(" ", screenText);

        // Try to extract zone information
        for (String line : screenText) {
            // Check for zone info
            if (line.toLowerCase().contains("zone")) {
                Matcher zoneMatcher = ZONE_PATTERN.matcher(line);
                if (zoneMatcher.find()) {
                    lastCapturedZone = zoneMatcher.group(1).trim();
                }
                // Alternative pattern: "Zone South Des Moines"
                else if (line.contains("Zone")) {
                    String[] parts = line.split("Zone");
                    if (parts.length > 1) {
                        lastCapturedZone = parts[1].trim();
                    }
                }
                // Another common pattern: "Zone: South Des Moines"
                else if (line.contains("Zone:")) {
                    String[] parts = line.split("Zone:");
                    if (parts.length > 1) {
                        lastCapturedZone = parts[1].trim();
                    }
                }

                if (lastCapturedZone != null) {
                    Log.d(TAG, "Captured Zone: " + lastCapturedZone);
                    lastCaptureTimestamp = System.currentTimeMillis();
                }
            }

            // Check for store info
            if (line.toLowerCase().contains("store")) {
                Matcher storeMatcher = STORE_PATTERN.matcher(line);
                if (storeMatcher.find()) {
                    lastCapturedStore = storeMatcher.group(1).trim();
                }
                // Alternative pattern: "Store Target - West Des Moines"
                else if (line.contains("Store")) {
                    String[] parts = line.split("Store");
                    if (parts.length > 1) {
                        lastCapturedStore = parts[1].trim();
                    }
                }
                // Another common pattern: "Store: Target - West Des Moines"
                else if (line.contains("Store:")) {
                    String[] parts = line.split("Store:");
                    if (parts.length > 1) {
                        lastCapturedStore = parts[1].trim();
                    }
                }

                if (lastCapturedStore != null) {
                    Log.d(TAG, "Captured Store: " + lastCapturedStore);
                    lastCaptureTimestamp = System.currentTimeMillis();
                }
            }

            // Check for address patterns (typically after claiming)
            if (containsAddressPattern(line)) {
                lastCapturedAddress = line.trim();
                Log.d(TAG, "Possibly captured Address: " + lastCapturedAddress);
                lastCaptureTimestamp = System.currentTimeMillis();
            }
        }

        // If we recently captured an order ID and have new context, update the record
        if (lastCapturedOrderId != null &&
                System.currentTimeMillis() - lastCaptureTimestamp < CAPTURE_TIMEOUT_MS) {
            storeCapture();
        }
    }

    /**
     * Detect delivery completion screens
     */
    private void detectDeliveryCompletion(List<String> screenText) {
        String fullText = TextUtils.join(" ", screenText).toLowerCase();

        // Check for common delivery completion phrases
        boolean containsDeliveryComplete =
                fullText.contains("slide to complete") ||
                        fullText.contains("delivery complete") ||
                        fullText.contains("delivered") ||
                        fullText.contains("mark as delivered") ||
                        fullText.contains("confirm delivery");

        // Check for "slide to" + action buttons (common in Shipt UI)
        boolean containsSlideAction = false;
        for (String line : screenText) {
            if (line.toLowerCase().contains("slide to") ||
                    line.toLowerCase().contains("swipe to")) {
                containsSlideAction = true;
                break;
            }
        }

        // Check for success confirmation after delivery
        boolean containsConfirmation =
                fullText.contains("delivery marked as complete") ||
                        fullText.contains("delivery successful") ||
                        fullText.contains("thank you for delivering");

        // If we detect a delivery flow screen
        if (containsDeliveryComplete || containsSlideAction) {
            inDeliveryFlow = true;

            // Look for order ID on the screen if we don't have it already
            if (currentOrderId == null || lastCapturedOrderId == null) {
                Matcher orderMatcher = ORDER_ID_PATTERN.matcher(fullText);
                if (orderMatcher.find()) {
                    currentOrderId = orderMatcher.group(1);
                    lastCapturedOrderId = currentOrderId;
                    Log.d(TAG, "Found order ID during delivery: " + currentOrderId);
                }
            }
        }

        // If we detect a confirmation screen and we're in delivery flow
        if ((containsConfirmation || (containsDeliveryComplete && !containsSlideAction)) &&
                inDeliveryFlow && !deliveryCompleted) {

            Log.d(TAG, "Detected delivery completion confirmation");
            deliveryCompleted = true;

            // Record delivery completion if we have the order ID
            if (currentOrderId != null) {
                recordDeliveryCompletion(currentOrderId);
            }

            // Reset flow tracking after a short delay (next event will reset it)
            new Thread(() -> {
                try {
                    Thread.sleep(10000); // 10 seconds
                    inDeliveryFlow = false;
                    deliveryCompleted = false;
                    currentOrderId = null;
                } catch (InterruptedException e) {
                    // Ignore
                }
            }).start();
        }
    }

    /**
     * Record when a delivery is completed
     */
    private void recordDeliveryCompletion(String orderId) {
        if (orderId == null) return;

        // Initialize Firestore if needed
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }

        // Only proceed if logged in
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        Timestamp completionTime = Timestamp.now();

        Log.d(TAG, "Recording delivery completion for Order #" + orderId);

        // Check if the delivery exists in our database
        db.collection("deliveries")
                .whereEqualTo("orderId", orderId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Update existing delivery
                        DocumentReference deliveryRef = queryDocumentSnapshots.getDocuments().get(0).getReference();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("deliveryCompletedDate", completionTime);

                        deliveryRef.update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Updated delivery completion date for order: " + orderId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating delivery completion date", e);
                                });
                    } else {
                        // Create new delivery record if it doesn't exist yet
                        Map<String, Object> delivery = new HashMap<>();
                        delivery.put("orderId", orderId);
                        delivery.put("userId", userId);
                        delivery.put("deliveryDate", Timestamp.now()); // Fallback to now for acceptance date
                        delivery.put("deliveryCompletedDate", completionTime);
                        delivery.put("importDate", Timestamp.now());
                        delivery.put("doNotDeliver", false);
                        delivery.put("source", "auto_capture");

                        if (lastCapturedStore != null) {
                            delivery.put("store", lastCapturedStore);
                        }

                        if (lastCapturedZone != null) {
                            delivery.put("zone", lastCapturedZone);
                        }

                        if (lastCapturedAddress != null) {
                            delivery.put("address", lastCapturedAddress);
                        } else if (lastCapturedZone != null && lastCapturedStore != null) {
                            delivery.put("location", lastCapturedStore + " - " + lastCapturedZone);
                        }

                        db.collection("deliveries")
                                .add(delivery)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Created new delivery with completion date: " + orderId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating delivery with completion date", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for existing delivery", e);
                });
    }

    /**
     * Check if text likely contains a US address
     */
    private boolean containsAddressPattern(String text) {
        // Common US address patterns
        // This checks for street numbers followed by common street suffixes
        return text.matches(".*\\d+\\s+[A-Za-z]+\\s+(St|St\\.|Street|Ave|Ave\\.|Avenue|Rd|Rd\\.|Road|Dr|Dr\\.|Drive|Ln|Ln\\.|Lane|Blvd|Blvd\\.|Boulevard|Way|Ct|Ct\\.|Court|Cir|Cir\\.|Circle|Pl|Pl\\.|Place).*");
    }

    /**
     * Store the captured information to Firestore
     */
    private void storeCapture() {
        if (lastCapturedOrderId == null) return;

        // Initialize Firestore if needed
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }

        // Only proceed if logged in
        if (mAuth.getCurrentUser() == null) return;

        // Create the capture data
        Map<String, Object> captureData = new HashMap<>();
        captureData.put("orderId", lastCapturedOrderId);
        captureData.put("captureTime", Timestamp.now());
        captureData.put("userId", mAuth.getCurrentUser().getUid());

        if (lastCapturedZone != null) {
            captureData.put("zone", lastCapturedZone);
        }

        if (lastCapturedStore != null) {
            captureData.put("store", lastCapturedStore);
        }

        if (lastCapturedAddress != null) {
            captureData.put("address", lastCapturedAddress);
        }

        // Combine zone and store for a location reference if no address
        if (lastCapturedAddress == null && lastCapturedZone != null && lastCapturedStore != null) {
            captureData.put("location", lastCapturedStore + " - " + lastCapturedZone);
        }

        // Add delivery data
        captureData.put("deliveryDate", Timestamp.now()); // Order acceptance date
        captureData.put("processed", false);

        // Check if we already have this order ID
        db.collection("shipt_captures")
                .whereEqualTo("orderId", lastCapturedOrderId)
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // New capture
                        db.collection("shipt_captures")
                                .add(captureData)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "New capture stored: " + documentReference.getId());

                                    // Create a delivery record too
                                    createOrUpdateDeliveryRecord(lastCapturedOrderId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error storing capture", e);
                                });
                    } else {
                        // Update existing capture with any new information
                        DocumentReference docRef = querySnapshot.getDocuments().get(0).getReference();
                        docRef.update(captureData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Updated existing capture");

                                    // Update delivery record too
                                    createOrUpdateDeliveryRecord(lastCapturedOrderId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating capture", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for existing capture", e);
                });
    }

    /**
     * Create or update a delivery record in the main deliveries collection
     */
    private void createOrUpdateDeliveryRecord(String orderId) {
        if (db == null || mAuth == null || mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        // Check if delivery already exists
        db.collection("deliveries")
                .whereEqualTo("orderId", orderId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Create new delivery
                        Map<String, Object> delivery = new HashMap<>();
                        delivery.put("orderId", orderId);
                        delivery.put("userId", userId);
                        delivery.put("deliveryDate", Timestamp.now());
                        delivery.put("importDate", Timestamp.now());
                        delivery.put("doNotDeliver", false);
                        delivery.put("source", "auto_capture");

                        if (lastCapturedAddress != null) {
                            delivery.put("address", lastCapturedAddress);
                        } else if (lastCapturedZone != null && lastCapturedStore != null) {
                            delivery.put("location", lastCapturedStore + " - " + lastCapturedZone);
                            // Also store individual components
                            delivery.put("zone", lastCapturedZone);
                            delivery.put("store", lastCapturedStore);
                        }

                        db.collection("deliveries")
                                .add(delivery)
                                .addOnSuccessListener(docRef -> {
                                    Log.d(TAG, "Created new delivery record for: " + orderId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating delivery record", e);
                                });
                    } else {
                        // Update existing delivery
                        DocumentReference docRef = querySnapshot.getDocuments().get(0).getReference();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("lastUpdated", Timestamp.now());

                        // Only update address if we have a better one
                        if (lastCapturedAddress != null) {
                            String existingAddress = querySnapshot.getDocuments().get(0).getString("address");
                            if (existingAddress == null || existingAddress.isEmpty() ||
                                    (lastCapturedAddress.length() > existingAddress.length())) {
                                updates.put("address", lastCapturedAddress);
                            }
                        }

                        // Update store and zone if we have them and they're not already set
                        if (lastCapturedStore != null && !querySnapshot.getDocuments().get(0).contains("store")) {
                            updates.put("store", lastCapturedStore);
                        }

                        if (lastCapturedZone != null && !querySnapshot.getDocuments().get(0).contains("zone")) {
                            updates.put("zone", lastCapturedZone);
                        }

                        if (!updates.isEmpty()) {
                            docRef.update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Updated delivery record for: " + orderId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error updating delivery record", e);
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for existing delivery record", e);
                });
    }

    private List<String> collectAllText(AccessibilityNodeInfo node) {
        List<String> textList = new ArrayList<>();
        if (node == null) return textList;

        if (node.getText() != null) {
            String text = node.getText().toString().trim();
            if (!text.isEmpty()) {
                textList.add(text);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                textList.addAll(collectAllText(child));
                child.recycle();
            }
        }

        return textList;
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "Accessibility service connected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 300; // 0.3 seconds
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;

        this.setServiceInfo(info);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
}