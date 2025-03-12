package com.autogratuity.models;

import com.google.firebase.Timestamp;

public class Delivery {
    private String id;
    private String orderId;
    private String address;
    private double tipAmount;
    private Timestamp deliveryDate;
    private Timestamp tipDate;

    // Empty constructor required for Firestore
    public Delivery() {}

    public Delivery(String orderId, String address, Timestamp deliveryDate) {
        this.orderId = orderId;
        this.address = address;
        this.deliveryDate = deliveryDate;
        this.tipAmount = 0.0;
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

    public Timestamp getTipDate() { return tipDate; }
    public void setTipDate(Timestamp tipDate) { this.tipDate = tipDate; }

    public boolean isTipped() {
        return tipAmount > 0;
    }
}