package com.autogratuity.models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for tip data
 */
public class TipData extends FirestoreModel {
    private String id;
    private double amount;
    private String source;
    private long timestamp;
    private String orderId;
    private boolean verified;

    /**
     * Default constructor
     */
    public TipData() {
        this.id = "";
        this.amount = 0.0;
        this.source = "";
        this.timestamp = System.currentTimeMillis();
        this.orderId = "";
        this.verified = false;
    }

    /**
     * Create TipData from a Firestore document
     *
     * @param document Firestore document
     * @return TipData object
     */
    public static TipData fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        TipData tipData = new TipData();
        tipData.setId(document.getId());

        if (document.contains("amount") && document.get("amount") != null) {
            tipData.setAmount(document.getDouble("amount"));
        }

        if (document.contains("source") && document.get("source") != null) {
            tipData.setSource(document.getString("source"));
        }

        if (document.contains("timestamp") && document.get("timestamp") != null) {
            tipData.setTimestamp(document.getLong("timestamp"));
        }

        if (document.contains("orderId") && document.get("orderId") != null) {
            tipData.setOrderId(document.getString("orderId"));
        }

        if (document.contains("verified") && document.get("verified") != null) {
            tipData.setVerified(document.getBoolean("verified"));
        }

        return tipData;
    }

    /**
     * Convert to Firestore data
     *
     * @return Map of data
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("amount", amount);
        map.put("source", source);
        map.put("timestamp", timestamp);
        map.put("orderId", orderId);
        map.put("verified", verified);
        return map;
    }
    
    @Override
    public Map<String, Object> toDocument() {
        return toMap();
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}