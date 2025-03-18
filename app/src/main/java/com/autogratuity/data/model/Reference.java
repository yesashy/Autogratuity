package com.autogratuity.data.model;

/**
 * Model class representing references to other entities in the Autogratuity app.
 * This is a standalone version of the inner class in Delivery.
 */
public class Reference {
    private String addressId;
    private String platformOrderId;
    private String userId;
    private String deliveryId;
    
    // Default constructor required for Firestore
    public Reference() {
    }
    
    // Getters and setters
    
    public String getAddressId() {
        return addressId;
    }
    
    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }
    
    public String getPlatformOrderId() {
        return platformOrderId;
    }
    
    public void setPlatformOrderId(String platformOrderId) {
        this.platformOrderId = platformOrderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getDeliveryId() {
        return deliveryId;
    }
    
    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }
}
