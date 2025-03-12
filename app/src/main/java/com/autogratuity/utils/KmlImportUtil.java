package com.autogratuity.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class KmlImportUtil {
    private static final String TAG = "KmlImportUtil";

    // Regex pattern to extract order ID (numeric sequence at the beginning of name)
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^(\\d+)");

    // Extract tip amount if already present in the label
    private static final Pattern TIP_PATTERN = Pattern.compile("\\$(\\d+\\.\\d+)");

    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    // Keep track of imports
    private int totalPlacemarks = 0;
    private AtomicInteger processedPlacemarks = new AtomicInteger(0);

    public KmlImportUtil(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Import data from a KML or KMZ file
     * @param uri The URI of the KML or KMZ file
     * @return true if import started successfully, false otherwise
     */
    public boolean importFromKmlKmz(Uri uri) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
            return false;
        }

        final String userId = currentUser.getUid();

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(context, "Couldn't open file", Toast.LENGTH_SHORT).show();
                return false;
            }

            String fileName = uri.getLastPathSegment();
            if (fileName != null && fileName.toLowerCase().endsWith(".kmz")) {
                // For KMZ files, we need to unzip them first
                ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                ZipEntry zipEntry;

                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (zipEntry.getName().toLowerCase().endsWith(".kml")) {
                        // Found the KML file inside the KMZ
                        parseKml(zipInputStream, userId);
                        break;
                    }
                }

                zipInputStream.close();
            } else {
                // For KML files, parse directly
                parseKml(inputStream, userId);
                inputStream.close();
            }

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error reading file", e);
            Toast.makeText(context, "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void parseKml(InputStream inputStream, String userId) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

            // Count total placemarks for progress tracking
            countPlacemarks(parser);

            // Reset stream and parser for second pass
            try {
                inputStream.reset();
            } catch (IOException e) {
                Log.e(TAG, "Cannot reset stream, using counting only", e);
                // If we can't reset, just proceed with counting
            }

            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            String currentTag = "";

            String name = null;
            String description = null;
            String coordinates = null;
            String styleUrl = null;

            // Start parsing
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if ("Placemark".equals(currentTag)) {
                            // Reset values for new placemark
                            name = null;
                            description = null;
                            coordinates = null;
                            styleUrl = null;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        String text = parser.getText().trim();
                        if (!text.isEmpty()) {
                            if ("name".equals(currentTag)) {
                                name = text;
                            } else if ("description".equals(currentTag)) {
                                description = text;
                            } else if ("coordinates".equals(currentTag)) {
                                coordinates = text;
                            } else if ("styleUrl".equals(currentTag)) {
                                styleUrl = text;
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("Placemark".equals(parser.getName()) && name != null) {
                            // Process completed placemark
                            processPlacemark(name, description, coordinates, styleUrl, userId);
                        }
                        currentTag = "";
                        break;
                }
                eventType = parser.next();
            }

            Toast.makeText(context, "Started import of " + totalPlacemarks + " locations", Toast.LENGTH_SHORT).show();

        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error parsing KML", e);
            Toast.makeText(context, "Error parsing KML: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void countPlacemarks(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        totalPlacemarks = 0;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && "Placemark".equals(parser.getName())) {
                totalPlacemarks++;
            }
            eventType = parser.next();
        }

        processedPlacemarks.set(0);
        Log.d(TAG, "Found " + totalPlacemarks + " placemarks in KML");
    }

    private void processPlacemark(String name, String description, String coordinates, String styleUrl, String userId) {
        Log.d(TAG, "Processing placemark: " + name);

        // Extract order ID from name
        String orderId = extractOrderId(name);
        if (orderId == null || orderId.isEmpty()) {
            Log.w(TAG, "Skipping placemark without order ID: " + name);
            processedPlacemarks.incrementAndGet();
            return;
        }

        // Extract tip amount if present in the name
        double tipAmount = extractTipAmount(name);

        // Use address from name as that's where it is in Google Maps pins
        String address = name;
        if (orderId.equals(name)) {
            // If name is just the order ID, try to use coordinates or description
            address = "Location at " + (coordinates != null ? formatCoordinates(coordinates) : "Unknown");
        }

        // Create delivery document
        Map<String, Object> delivery = new HashMap<>();
        delivery.put("orderId", orderId);
        delivery.put("address", address);
        delivery.put("deliveryDate", new Timestamp(new Date()));
        delivery.put("userId", userId);
        delivery.put("importDate", new Timestamp(new Date()));

        if (coordinates != null) {
            delivery.put("coordinates", coordinates.trim());
        }

        if (tipAmount > 0) {
            delivery.put("tipAmount", tipAmount);
            delivery.put("tipDate", new Timestamp(new Date()));
        }

        if (styleUrl != null) {
            String color = styleUrl.replace("#", "").trim();
            delivery.put("mapColor", color);
        }

        // Create final copies of variables for use in lambda
        final String finalOrderId = orderId;
        final String finalAddress = address;
        final double finalTipAmount = tipAmount;
        final String finalCoordinates = coordinates;

        // Save to Firestore
        db.collection("deliveries")
                .add(delivery)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Added delivery: " + finalOrderId);
                    updateAddress(finalAddress, finalOrderId, finalTipAmount, finalCoordinates, userId);

                    // Update counter and check if import is complete
                    int processed = processedPlacemarks.incrementAndGet();
                    if (processed == totalPlacemarks) {
                        // All placemarks processed
                        Toast.makeText(context, "Import complete: " + processed + " locations imported",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding delivery", e);
                    processedPlacemarks.incrementAndGet();
                });
    }

    private String formatCoordinates(String coordinates) {
        // Turn "-93.760542,41.684084,0" into a more readable format
        String[] parts = coordinates.split(",");
        if (parts.length >= 2) {
            try {
                double longitude = Double.parseDouble(parts[0]);
                double latitude = Double.parseDouble(parts[1]);
                return String.format("%.6f, %.6f", latitude, longitude);
            } catch (NumberFormatException e) {
                return coordinates;
            }
        }
        return coordinates;
    }

    private String extractOrderId(String placemark) {
        if (placemark == null) return null;

        // Extract numeric ID from the beginning of the name
        Matcher matcher = ORDER_ID_PATTERN.matcher(placemark);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private double extractTipAmount(String name) {
        if (name == null) return 0.0;

        // Try to extract tip amount using pattern ($XX.XX)
        Matcher matcher = TIP_PATTERN.matcher(name);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        return 0.0;
    }

    private void updateAddress(@NonNull String fullAddress, @NonNull String orderId,
                               double tipAmount, String coordinates, @NonNull String userId) {
        // Use coordinates as a unique identifier for this address if available
        String normalizedAddress = fullAddress.toLowerCase().trim();
        String addressKey = coordinates != null ? coordinates.trim() : normalizedAddress;

        // Check if address exists
        db.collection("addresses")
                .whereEqualTo("coordinatesKey", addressKey)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Address exists, update it
                        String addressId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Update order IDs array
                        List<String> orderIds = new ArrayList<>();
                        if (queryDocumentSnapshots.getDocuments().get(0).contains("orderIds")) {
                            Object orderIdsObj = queryDocumentSnapshots.getDocuments().get(0).get("orderIds");
                            if (orderIdsObj instanceof List) {
                                try {
                                    @SuppressWarnings("unchecked")
                                    List<String> existingOrderIds = (List<String>) orderIdsObj;
                                    orderIds.addAll(existingOrderIds);
                                } catch (ClassCastException e) {
                                    Log.e(TAG, "Error casting orderIds", e);
                                }
                            }
                        }

                        if (!orderIds.contains(orderId)) {
                            orderIds.add(orderId);
                        }

                        // Update delivery count
                        Long deliveryCount = queryDocumentSnapshots.getDocuments().get(0).getLong("deliveryCount");
                        long newDeliveryCount = (deliveryCount != null) ? deliveryCount + 1 : 1;

                        // Update tips if applicable
                        Double totalTips = queryDocumentSnapshots.getDocuments().get(0).getDouble("totalTips");
                        double newTotalTips = (totalTips != null) ? totalTips : 0.0;

                        if (tipAmount > 0) {
                            newTotalTips += tipAmount;
                        }

                        double avgTip = newTotalTips / newDeliveryCount;

                        // Update address document
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("orderIds", orderIds);
                        updateData.put("deliveryCount", newDeliveryCount);
                        updateData.put("totalTips", newTotalTips);
                        updateData.put("averageTip", avgTip);

                        db.collection("addresses").document(addressId)
                                .update(updateData)
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error updating address", e)
                                );
                    } else {
                        // Create new address
                        List<String> orderIds = new ArrayList<>();
                        orderIds.add(orderId);

                        Map<String, Object> addressData = new HashMap<>();
                        addressData.put("fullAddress", fullAddress);
                        addressData.put("normalizedAddress", normalizedAddress);
                        addressData.put("coordinatesKey", addressKey);
                        addressData.put("orderIds", orderIds);
                        addressData.put("totalTips", tipAmount);
                        addressData.put("deliveryCount", 1);
                        addressData.put("averageTip", tipAmount);
                        addressData.put("userId", userId);

                        if (coordinates != null) {
                            addressData.put("coordinates", coordinates);
                        }

                        db.collection("addresses").add(addressData)
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error adding address", e)
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error querying addresses", e)
                );
    }
}