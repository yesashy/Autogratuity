package com.autogratuity.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class for address data
 */
public class Address extends FirestoreModel {
    private String id;
    private String fullAddress;
    private String normalizedAddress;
    private List<String> orderIds;
    private double totalTips;
    private int deliveryCount;
    private double averageTip;
    private String userId;
    private boolean doNotDeliver;
    private List<String> searchTerms;
    private Timestamp lastUpdated;
    private GeoPoint geoPoint;
    
    // Empty constructor for Firestore
    public Address() {
        this.orderIds = new ArrayList<>();
        this.searchTerms = new ArrayList<>();
        this.totalTips = 0.0;
        this.deliveryCount = 0;
        this.averageTip = 0.0;
        this.doNotDeliver = false;
        this.lastUpdated = Timestamp.now();
    }
    
    /**
     * Create Address from Firestore document
     */
    public static Address fromDocument(DocumentSnapshot doc) {
        if (doc == null) return null;
        
        Address address = new Address();
        address.id = doc.getId();
        
        // Extract basic fields
        address.fullAddress = doc.getString("fullAddress");
        address.normalizedAddress = doc.getString("normalizedAddress");
        address.userId = doc.getString("userId");
        
        // Extract analytics
        if (doc.contains("totalTips")) {
            address.totalTips = doc.getDouble("totalTips") != null ? doc.getDouble("totalTips") : 0.0;
        }
        
        if (doc.contains("deliveryCount")) {
            Object deliveryCountObj = doc.get("deliveryCount");
            if (deliveryCountObj instanceof Long) {
                address.deliveryCount = ((Long) deliveryCountObj).intValue();
            } else if (deliveryCountObj instanceof Integer) {
                address.deliveryCount = (Integer) deliveryCountObj;
            } else {
                address.deliveryCount = 0;
            }
        }
        
        if (doc.contains("averageTip")) {
            address.averageTip = doc.getDouble("averageTip") != null ? doc.getDouble("averageTip") : 0.0;
        }
        
        // Extract flags
        if (doc.contains("doNotDeliver")) {
            address.doNotDeliver = Boolean.TRUE.equals(doc.getBoolean("doNotDeliver"));
        }
        
        // Extract lists
        if (doc.contains("orderIds")) {
            Object orderIdsObj = doc.get("orderIds");
            if (orderIdsObj instanceof List) {
                address.orderIds = (List<String>) orderIdsObj;
            } else {
                address.orderIds = new ArrayList<>();
            }
        } else {
            address.orderIds = new ArrayList<>();
        }
        
        if (doc.contains("searchTerms")) {
            Object searchTermsObj = doc.get("searchTerms");
            if (searchTermsObj instanceof List) {
                address.searchTerms = (List<String>) searchTermsObj;
            } else {
                address.searchTerms = new ArrayList<>();
            }
        } else {
            address.searchTerms = new ArrayList<>();
        }
        
        // Extract timestamp
        address.lastUpdated = doc.getTimestamp("lastUpdated");
        if (address.lastUpdated == null) {
            address.lastUpdated = Timestamp.now();
        }
        
        // Extract geo point
        if (doc.contains("geoPoint")) {
            address.geoPoint = doc.getGeoPoint("geoPoint");
        }
        
        return address;
    }
    
    /**
     * Convert to Firestore document
     */
    public Map<String, Object> toDocument() {
        Map<String, Object> doc = new HashMap<>();
        
        // Basic fields
        doc.put("fullAddress", fullAddress);
        doc.put("normalizedAddress", normalizedAddress);
        doc.put("userId", userId);
        
        // Analytics
        doc.put("totalTips", totalTips);
        doc.put("deliveryCount", deliveryCount);
        doc.put("averageTip", averageTip);
        
        // Flags
        doc.put("doNotDeliver", doNotDeliver);
        
        // Lists
        doc.put("orderIds", orderIds != null ? orderIds : new ArrayList<>());
        doc.put("searchTerms", searchTerms != null ? searchTerms : new ArrayList<>());
        
        // Timestamp
        doc.put("lastUpdated", lastUpdated != null ? lastUpdated : Timestamp.now());
        
        // Geo point
        if (geoPoint != null) {
            doc.put("geoPoint", geoPoint);
        }
        
        return doc;
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
    
    public List<String> getOrderIds() {
        return orderIds;
    }
    
    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }
    
    public double getTotalTips() {
        return totalTips;
    }
    
    public void setTotalTips(double totalTips) {
        this.totalTips = totalTips;
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
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public boolean isDoNotDeliver() {
        return doNotDeliver;
    }
    
    public void setDoNotDeliver(boolean doNotDeliver) {
        this.doNotDeliver = doNotDeliver;
    }
    
    public List<String> getSearchTerms() {
        return searchTerms;
    }
    
    public void setSearchTerms(List<String> searchTerms) {
        this.searchTerms = searchTerms;
    }
    
    public Timestamp getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public GeoPoint getGeoPoint() {
        return geoPoint;
    }
    
    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
    
    /**
     * Add an order ID to the list
     */
    public void addOrderId(String orderId) {
        if (orderIds == null) {
            orderIds = new ArrayList<>();
        }
        
        if (!orderIds.contains(orderId)) {
            orderIds.add(orderId);
        }
    }
    
    /**
     * Update the statistics with a new tip
     */
    public void updateWithTip(double tipAmount) {
        this.totalTips += tipAmount;
        this.deliveryCount += 1;
        this.averageTip = this.totalTips / this.deliveryCount;
        this.lastUpdated = Timestamp.now();
    }
}
