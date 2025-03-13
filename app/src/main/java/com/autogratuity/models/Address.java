// app/src/main/java/com/autogratuity/models/Address.java
package com.autogratuity.models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Address {
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