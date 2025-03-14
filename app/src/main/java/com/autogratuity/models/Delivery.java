package com.autogratuity.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Delivery extends FirestoreModel {
    private String id;
    private String orderId;
    private String address;
    private double tipAmount;
    private Timestamp deliveryDate;
    private Timestamp deliveryCompletedDate;
    private Timestamp tipDate;
    private boolean doNotDeliver;
    private String zone;
    private String store;
    private String location;
    private String source;
    private String userId;
    private String coordinates;
    private Timestamp importDate;

    // Empty constructor required for Firestore
    public Delivery() {}

    public Delivery(String orderId, String address, Timestamp deliveryDate) {
        this.orderId = orderId;
        this.address = address;
        this.deliveryDate = deliveryDate;
        this.tipAmount = 0.0;
        this.doNotDeliver = false;
        this.importDate = Timestamp.now();
    }

    // Static factory method to create from DocumentSnapshot
    public static Delivery fromDocument(DocumentSnapshot doc) {
        if (doc == null) return null;

        Delivery delivery = new Delivery();
        delivery.id = doc.getId();
        delivery.orderId = getString(doc, "orderId");
        delivery.userId = getString(doc, "userId");

        // Address data - support both flat and nested structures
        if (doc.contains("address") && doc.get("address") instanceof Map) {
            delivery.address = getString(doc, "address.fullAddress");

            // Extract coordinates from GeoPoint if available
            Object geoObj = doc.get("address.geoPoint");
            if (geoObj instanceof GeoPoint) {
                GeoPoint geoPoint = (GeoPoint) geoObj;
                delivery.coordinates = geoPoint.getLatitude() + "," + geoPoint.getLongitude();
            }
        } else {
            delivery.address = doc.getString("address");
            delivery.coordinates = doc.getString("coordinates");
        }

        // Dates - support both flat and nested structures
        if (doc.contains("dates") && doc.get("dates") instanceof Map) {
            delivery.deliveryDate = doc.getTimestamp("dates.accepted");
            delivery.deliveryCompletedDate = doc.getTimestamp("dates.completed");
            delivery.tipDate = doc.getTimestamp("dates.tipped");
            delivery.importDate = doc.getTimestamp("dates.created");
        } else {
            delivery.deliveryDate = doc.getTimestamp("deliveryDate");
            delivery.deliveryCompletedDate = doc.getTimestamp("deliveryCompletedDate");
            delivery.tipDate = doc.getTimestamp("tipDate");
            delivery.importDate = doc.getTimestamp("importDate");
        }

        // Status - support both flat and nested structures
        if (doc.contains("status") && doc.get("status") instanceof Map) {
            delivery.doNotDeliver = getBoolean(doc, "status.doNotDeliver");
        } else {
            delivery.doNotDeliver = doc.getBoolean("doNotDeliver") != null && doc.getBoolean("doNotDeliver");
        }

        // Amounts - support both flat and nested structures
        if (doc.contains("amounts") && doc.get("amounts") instanceof Map) {
            delivery.tipAmount = getDouble(doc, "amounts.tipAmount");
        } else {
            delivery.tipAmount = doc.getDouble("tipAmount") != null ? doc.getDouble("tipAmount") : 0.0;
        }

        // Other fields
        delivery.zone = doc.getString("zone");
        delivery.store = doc.getString("store");
        delivery.location = doc.getString("location");

        // Metadata - support both flat and nested structures
        if (doc.contains("metadata") && doc.get("metadata") instanceof Map) {
            delivery.source = getString(doc, "metadata.source");
        } else {
            delivery.source = doc.getString("source");
        }

        return delivery;
    }

    // Convert to Firestore document with nested structure
    public Map<String, Object> toDocument() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("orderId", orderId);
        doc.put("userId", userId);

        // Address data
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("fullAddress", address);
        addressMap.put("normalized", address != null ? address.toLowerCase().trim() : "");
        addressMap.put("searchKey", address != null ? address.toLowerCase().replaceAll("[^a-z0-9]", "") : "");

        // Convert coordinates to GeoPoint if available
        if (coordinates != null && !coordinates.isEmpty()) {
            String[] parts = coordinates.split(",");
            if (parts.length >= 2) {
                try {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lng = Double.parseDouble(parts[1].trim());
                    addressMap.put("geoPoint", new GeoPoint(lat, lng));
                } catch (NumberFormatException ignored) {}
            }
        }
        doc.put("address", addressMap);

        // Dates
        Map<String, Object> dates = new HashMap<>();
        dates.put("created", importDate != null ? importDate : Timestamp.now());
        dates.put("accepted", deliveryDate);
        dates.put("completed", deliveryCompletedDate);
        dates.put("tipped", tipDate);
        doc.put("dates", dates);

        // Status
        Map<String, Object> status = new HashMap<>();
        status.put("isCompleted", deliveryCompletedDate != null);
        status.put("isTipped", tipAmount > 0);
        status.put("doNotDeliver", doNotDeliver);
        doc.put("status", status);

        // Amounts
        Map<String, Object> amounts = new HashMap<>();
        amounts.put("tipAmount", tipAmount);
        amounts.put("estimatedPay", 0.0); // Default to 0 if not set
        doc.put("amounts", amounts);

        // Other fields (for backward compatibility)
        if (zone != null) doc.put("zone", zone);
        if (store != null) doc.put("store", store);
        if (location != null) doc.put("location", location);

        // Metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", source != null ? source : "manual");
        metadata.put("platform", "android");
        metadata.put("captureId", null);
        metadata.put("version", 1);
        doc.put("metadata", metadata);

        return doc;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getTipAmount() { return tipAmount; }
    public void setTipAmount(double tipAmount) { this.tipAmount = tipAmount; }

    public Timestamp getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Timestamp deliveryDate) { this.deliveryDate = deliveryDate; }

    public Timestamp getDeliveryCompletedDate() { return deliveryCompletedDate; }
    public void setDeliveryCompletedDate(Timestamp deliveryCompletedDate) { this.deliveryCompletedDate = deliveryCompletedDate; }

    public Timestamp getTipDate() { return tipDate; }
    public void setTipDate(Timestamp tipDate) { this.tipDate = tipDate; }

    public boolean isDoNotDeliver() { return doNotDeliver; }
    public void setDoNotDeliver(boolean doNotDeliver) { this.doNotDeliver = doNotDeliver; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

    public Timestamp getImportDate() { return importDate; }
    public void setImportDate(Timestamp importDate) { this.importDate = importDate; }

    public boolean isTipped() {
        return tipAmount > 0;
    }
}