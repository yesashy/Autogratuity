package com.autogratuity.models;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for delivery data
 * Used for bulk import operations
 */
public class DeliveryData {
    private String id;
    private Address address;
    private TipData tipData;
    private String notes;
    private boolean importedFromGeoJson;
    private long timestamp;
    private String userId;
    private ImportVerification verification;
    private String orderId;

    /**
     * Default constructor
     */
    public DeliveryData() {
        this.address = new Address();
        this.tipData = new TipData();
        this.notes = "";
        this.importedFromGeoJson = false;
        this.timestamp = System.currentTimeMillis();
        this.verification = new ImportVerification();
    }

    /**
     * Convert to Firestore document
     *
     * @return Map with delivery data fields
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        
        if (address != null) {
            map.put("address", address.toDocument());
        }
        
        if (tipData != null) {
            map.put("tipData", tipData.toMap());
        }
        
        if (verification != null) {
            map.put("verification", verification.toMap());
        }
        
        map.put("notes", notes);
        map.put("importedFromGeoJson", importedFromGeoJson);
        map.put("timestamp", timestamp);
        map.put("userId", userId);
        
        if (orderId != null && !orderId.isEmpty()) {
            map.put("orderId", orderId);
        }
        
        return map;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public TipData getTipData() {
        return tipData;
    }

    public void setTipData(TipData tipData) {
        this.tipData = tipData;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isImportedFromGeoJson() {
        return importedFromGeoJson;
    }

    public void setImportedFromGeoJson(boolean importedFromGeoJson) {
        this.importedFromGeoJson = importedFromGeoJson;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public ImportVerification getVerification() {
        return verification;
    }

    public void setVerification(ImportVerification verification) {
        this.verification = verification;
    }
    
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}