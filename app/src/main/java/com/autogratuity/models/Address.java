package com.autogratuity.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Address extends FirestoreModel {
    private String id;
    private String fullAddress;
    private String normalizedAddress;
    private List<String> orderIds;
    private double totalTips;
    private int deliveryCount;
    private double averageTip;
    private boolean doNotDeliver;
    private String coordinates;
    private List<String> searchTerms;
    private Timestamp lastUpdated;
    private String userId;

    // Empty constructor required for Firestore
    public Address() {
        orderIds = new ArrayList<>();
        searchTerms = new ArrayList<>();
    }

    public Address(String fullAddress) {
        this.fullAddress = fullAddress;
        this.normalizedAddress = fullAddress.toLowerCase().trim();
        this.orderIds = new ArrayList<>();
        this.searchTerms = new ArrayList<>();
        this.totalTips = 0;
        this.deliveryCount = 0;
        this.averageTip = 0;
        this.doNotDeliver = false;
    }

    // Static factory method to create from DocumentSnapshot
    public static Address fromDocument(DocumentSnapshot doc) {
        if (doc == null) return null;

        Address address = new Address();
        address.id = doc.getId();
        address.userId = doc.getString("userId");
        address.fullAddress = doc.getString("fullAddress");
        address.normalizedAddress = doc.getString("normalizedAddress");

        // Handle coordinates and geoPoint
        address.coordinates = doc.getString("coordinates");
        Object geoObj = doc.get("geoPoint");
        if (geoObj instanceof GeoPoint && address.coordinates == null) {
            GeoPoint geoPoint = (GeoPoint) geoObj;
            address.coordinates = geoPoint.getLatitude() + "," + geoPoint.getLongitude();
        }

        // Handle order IDs
        if (doc.contains("orderIds") && doc.get("orderIds") instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> orderIds = (List<String>) doc.get("orderIds");
            address.orderIds = orderIds;
        } else {
            address.orderIds = new ArrayList<>();
        }

        // Handle numeric values
        if (doc.contains("totalTips")) {
            if (doc.getDouble("totalTips") != null) {
                address.totalTips = doc.getDouble("totalTips");
            } else if (doc.getLong("totalTips") != null) {
                address.totalTips = doc.getLong("totalTips").doubleValue();
            }
        }

        if (doc.contains("deliveryCount")) {
            if (doc.getLong("deliveryCount") != null) {
                address.deliveryCount = doc.getLong("deliveryCount").intValue();
            }
        }

        if (doc.contains("averageTip")) {
            if (doc.getDouble("averageTip") != null) {
                address.averageTip = doc.getDouble("averageTip");
            } else if (doc.getLong("averageTip") != null) {
                address.averageTip = doc.getLong("averageTip").doubleValue();
            }
        }

        address.doNotDeliver = doc.getBoolean("doNotDeliver") != null && doc.getBoolean("doNotDeliver");

        // Search terms
        if (doc.contains("searchTerms") && doc.get("searchTerms") instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> searchTerms = (List<String>) doc.get("searchTerms");
            address.searchTerms = searchTerms;
        } else {
            // Generate search terms if not present
            address.searchTerms = generateSearchTerms(address.normalizedAddress);
        }

        address.lastUpdated = doc.getTimestamp("lastUpdated");

        return address;
    }

    // Convert to Firestore document
    public Map<String, Object> toDocument() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("userId", userId);
        doc.put("fullAddress", fullAddress);
        doc.put("normalizedAddress", normalizedAddress);

        // Convert coordinates to GeoPoint if available
        if (coordinates != null && !coordinates.isEmpty()) {
            String[] parts = coordinates.split(",");
            if (parts.length >= 2) {
                try {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lng = Double.parseDouble(parts[1].trim());
                    doc.put("geoPoint", new GeoPoint(lat, lng));
                } catch (NumberFormatException ignored) {
                    // Ignore parsing errors
                }
            }
        }

        // For backward compatibility
        doc.put("coordinates", coordinates);

        doc.put("orderIds", orderIds);
        doc.put("totalTips", totalTips);
        doc.put("deliveryCount", deliveryCount);
        doc.put("averageTip", averageTip);
        doc.put("doNotDeliver", doNotDeliver);

        // Generate search terms if needed
        if (searchTerms == null || searchTerms.isEmpty()) {
            searchTerms = generateSearchTerms(normalizedAddress);
        }
        doc.put("searchTerms", searchTerms);

        doc.put("lastUpdated", lastUpdated != null ? lastUpdated : Timestamp.now());

        return doc;
    }

    // Generate search terms for fuzzy matching
    private static List<String> generateSearchTerms(String address) {
        List<String> terms = new ArrayList<>();

        if (address == null || address.isEmpty()) {
            return terms;
        }

        // Add the full normalized address
        terms.add(address);

        // Add individual words for partial matching
        String[] words = address.split("\\s+");
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

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getNormalizedAddress() { return normalizedAddress; }
    public void setNormalizedAddress(String normalizedAddress) { this.normalizedAddress = normalizedAddress; }

    public List<String> getOrderIds() { return orderIds; }
    public void setOrderIds(List<String> orderIds) { this.orderIds = orderIds; }

    public double getTotalTips() { return totalTips; }
    public void setTotalTips(double totalTips) { this.totalTips = totalTips; }

    public int getDeliveryCount() { return deliveryCount; }
    public void setDeliveryCount(int deliveryCount) { this.deliveryCount = deliveryCount; }

    public double getAverageTip() { return averageTip; }
    public void setAverageTip(double averageTip) { this.averageTip = averageTip; }

    public boolean isDoNotDeliver() { return doNotDeliver; }
    public void setDoNotDeliver(boolean doNotDeliver) { this.doNotDeliver = doNotDeliver; }

    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

    public List<String> getSearchTerms() { return searchTerms; }
    public void setSearchTerms(List<String> searchTerms) { this.searchTerms = searchTerms; }

    public Timestamp getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Timestamp lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public void addOrderId(String orderId) {
        if (orderIds == null) {
            orderIds = new ArrayList<>();
        }
        if (!orderIds.contains(orderId)) {
            orderIds.add(orderId);
            deliveryCount++;
        }
    }

    public void addTip(double tipAmount) {
        totalTips += tipAmount;
        averageTip = totalTips / deliveryCount;
    }
}