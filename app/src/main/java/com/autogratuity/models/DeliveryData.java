package com.autogratuity.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class for delivery data
 * Used for bulk import operations
 */
public class DeliveryData extends FirestoreModel {
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
     * Create a DeliveryData object from a Firestore document
     * 
     * @param doc The DocumentSnapshot from Firestore
     * @return A DeliveryData object, or null if the document is invalid
     */
    public static DeliveryData fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return null;
        }
        
        DeliveryData data = new DeliveryData();
        data.id = doc.getId();
        
        // Get data from document
        if (doc.contains("address") && doc.get("address") instanceof Map) {
            Map<String, Object> addressMap = (Map<String, Object>) doc.get("address");
            Address address = new Address();
            // Populate address from map
            if (addressMap.containsKey("fullAddress")) {
                address.setFullAddress((String) addressMap.get("fullAddress"));
            }
            if (addressMap.containsKey("normalizedAddress")) {
                address.setNormalizedAddress((String) addressMap.get("normalizedAddress"));
            }
            data.setAddress(address);
        }
        
        if (doc.contains("tipData") && doc.get("tipData") instanceof Map) {
            Map<String, Object> tipMap = (Map<String, Object>) doc.get("tipData");
            TipData tipData = new TipData();
            // Populate tip data from map
            if (tipMap.containsKey("amount") && tipMap.get("amount") instanceof Number) {
                tipData.setAmount(((Number) tipMap.get("amount")).doubleValue());
            }
            data.setTipData(tipData);
        }
        
        data.notes = getString(doc, "notes", "");
        data.importedFromGeoJson = getBoolean(doc, "importedFromGeoJson", false);
        data.timestamp = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0;
        data.userId = getString(doc, "userId", "");
        data.orderId = getString(doc, "orderId", "");
        
        return data;
    }

    /**
     * Convert to Firestore document
     *
     * @return Map with delivery data fields
     */
    @Override
    public Map<String, Object> toDocument() {
        Map<String, Object> map = new HashMap<>();
        
        // Add address data
        if (address != null) {
            map.put("address", address.toDocument());
        }
        
        // Add tip data
        if (tipData != null) {
            map.put("tipData", tipData.toMap());
        }
        
        // Add verification data
        if (verification != null) {
            map.put("verification", verification.toMap());
        }
        
        // Add basic fields
        map.put("notes", notes);
        map.put("importedFromGeoJson", importedFromGeoJson);
        map.put("timestamp", timestamp);
        map.put("userId", userId);
        map.put("deliveryDate", new Timestamp(new Date()));
        map.put("importDate", new Timestamp(new Date()));
        
        // Add order ID if available
        if (orderId != null && !orderId.isEmpty()) {
            map.put("orderId", orderId);
        }
        
        return map;
    }
    
    /**
     * Convert to map - alias for toDocument for compatibility
     */
    public Map<String, Object> toMap() {
        return toDocument();
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