package com.autogratuity.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for address data
 */
public class Address {
    private String id;
    private String fullAddress;
    private String normalizedAddress;
    private Coordinates coordinates;
    private int deliveryCount;
    private double averageTip;
    private boolean doNotDeliver;
    private long lastDeliveryTimestamp;
    private String notes;

    /**
     * Default constructor
     */
    public Address() {
        this.id = "";
        this.fullAddress = "";
        this.normalizedAddress = "";
        this.coordinates = new Coordinates();
        this.deliveryCount = 0;
        this.averageTip = 0.0;
        this.doNotDeliver = false;
        this.lastDeliveryTimestamp = System.currentTimeMillis();
        this.notes = "";
    }

    /**
     * Create Address from a Firestore document
     *
     * @param document Firestore document
     * @return Address object
     */
    public static Address fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        Address address = new Address();
        address.setId(document.getId());

        if (document.contains("fullAddress") && document.get("fullAddress") != null) {
            address.setFullAddress(document.getString("fullAddress"));
        }

        if (document.contains("normalizedAddress") && document.get("normalizedAddress") != null) {
            address.setNormalizedAddress(document.getString("normalizedAddress"));
        }

        if (document.contains("geoPoint") && document.get("geoPoint") != null) {
            GeoPoint geoPoint = document.getGeoPoint("geoPoint");
            if (geoPoint != null) {
                address.setCoordinates(Coordinates.fromGeoPoint(geoPoint));
            }
        } else if (document.contains("coordinates") && document.get("coordinates") != null) {
            String coordsStr = document.getString("coordinates");
            if (coordsStr != null && coordsStr.contains(",")) {
                String[] parts = coordsStr.split(",");
                try {
                    double lat = Double.parseDouble(parts[0]);
                    double lng = Double.parseDouble(parts[1]);
                    address.setCoordinates(new Coordinates(lat, lng));
                } catch (NumberFormatException e) {
                    // Invalid coordinates
                }
            }
        }

        if (document.contains("deliveryCount") && document.get("deliveryCount") != null) {
            address.setDeliveryCount(document.getLong("deliveryCount").intValue());
        }

        if (document.contains("averageTip") && document.get("averageTip") != null) {
            address.setAverageTip(document.getDouble("averageTip"));
        }

        if (document.contains("doNotDeliver") && document.get("doNotDeliver") != null) {
            address.setDoNotDeliver(document.getBoolean("doNotDeliver"));
        }

        if (document.contains("lastDeliveryTimestamp") && document.get("lastDeliveryTimestamp") != null) {
            address.setLastDeliveryTimestamp(document.getLong("lastDeliveryTimestamp"));
        }

        if (document.contains("notes") && document.get("notes") != null) {
            address.setNotes(document.getString("notes"));
        }

        return address;
    }

    /**
     * Convert to Firestore data
     *
     * @return Map of data
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fullAddress", fullAddress);
        map.put("normalizedAddress", normalizedAddress);
        
        // Add GeoPoint if coordinates exist
        if (coordinates != null) {
            map.put("geoPoint", coordinates.toGeoPoint());
            map.put("coordinates", coordinates.toString());
        }
        
        map.put("deliveryCount", deliveryCount);
        map.put("averageTip", averageTip);
        map.put("doNotDeliver", doNotDeliver);
        map.put("lastDeliveryTimestamp", lastDeliveryTimestamp);
        map.put("notes", notes);
        return map;
    }

    /**
     * Get GeoPoint representation of coordinates
     *
     * @return GeoPoint
     */
    public GeoPoint getGeoPoint() {
        return (coordinates != null) ? coordinates.toGeoPoint() : null;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getNormalizedAddress() {
        return normalizedAddress;
    }

    public void setNormalizedAddress(String normalizedAddress) {
        this.normalizedAddress = normalizedAddress;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public int getDeliveryCount() {
        return deliveryCount;
    }

    public void setDeliveryCount(int deliveryCount) {
        this.deliveryCount = deliveryCount;
    }

    public double getAverageTip() {
        return averageTip;
    }

    public void setAverageTip(double averageTip) {
        this.averageTip = averageTip;
    }

    public boolean isDoNotDeliver() {
        return doNotDeliver;
    }

    public void setDoNotDeliver(boolean doNotDeliver) {
        this.doNotDeliver = doNotDeliver;
    }

    public long getLastDeliveryTimestamp() {
        return lastDeliveryTimestamp;
    }

    public void setLastDeliveryTimestamp(long lastDeliveryTimestamp) {
        this.lastDeliveryTimestamp = lastDeliveryTimestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Increment delivery count
     */
    public void incrementDeliveryCount() {
        this.deliveryCount++;
    }

    /**
     * Update average tip with a new tip amount
     *
     * @param newTipAmount New tip amount to include in average
     */
    public void updateAverageTip(double newTipAmount) {
        if (deliveryCount <= 0) {
            averageTip = newTipAmount;
        } else {
            double total = averageTip * deliveryCount;
            total += newTipAmount;
            averageTip = total / (deliveryCount + 1);
        }
    }
}