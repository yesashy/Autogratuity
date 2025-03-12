package com.autogratuity.models;

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

    // Empty constructor required for Firestore
    public Address() {
        orderIds = new ArrayList<>();
    }

    public Address(String fullAddress) {
        this.fullAddress = fullAddress;
        this.normalizedAddress = fullAddress.toLowerCase().trim();
        this.orderIds = new ArrayList<>();
        this.totalTips = 0;
        this.deliveryCount = 0;
        this.averageTip = 0;
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

    public void addOrderId(String orderId) {
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