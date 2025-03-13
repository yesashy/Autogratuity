// app/src/main/java/com/autogratuity/models/Delivery.java
package com.autogratuity.models;

import com.google.firebase.Timestamp;

public class Delivery {
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

    // Empty constructor required for Firestore
    public Delivery() {}

    public Delivery(String orderId, String address, Timestamp deliveryDate) {
        this.orderId = orderId;
        this.address = address;
        this.deliveryDate = deliveryDate;
        this.tipAmount = 0.0;
        this.doNotDeliver = false;
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

    public boolean isTipped() {
        return tipAmount > 0;
    }
}