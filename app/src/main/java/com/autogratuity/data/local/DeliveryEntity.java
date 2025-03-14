package com.autogratuity.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

/**
 * Room entity for local caching of delivery data
 */
@Entity(tableName = "deliveries")
@TypeConverters(Converters.class)
public class DeliveryEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String documentId;
    private String orderId;
    private String address;
    private double tipAmount;
    private Date deliveryDate;
    private Date tipDate;
    private boolean doNotDeliver;
    private String zone;
    private String store;
    private String location;
    private String source;
    private String userId;
    private String coordinates;
    private Date importDate;
    private boolean isDirty;
    private Date lastSyncTime;
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public double getTipAmount() {
        return tipAmount;
    }
    
    public void setTipAmount(double tipAmount) {
        this.tipAmount = tipAmount;
    }
    
    public Date getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public Date getTipDate() {
        return tipDate;
    }
    
    public void setTipDate(Date tipDate) {
        this.tipDate = tipDate;
    }
    
    public boolean isDoNotDeliver() {
        return doNotDeliver;
    }
    
    public void setDoNotDeliver(boolean doNotDeliver) {
        this.doNotDeliver = doNotDeliver;
    }
    
    public String getZone() {
        return zone;
    }
    
    public void setZone(String zone) {
        this.zone = zone;
    }
    
    public String getStore() {
        return store;
    }
    
    public void setStore(String store) {
        this.store = store;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }
    
    public Date getImportDate() {
        return importDate;
    }
    
    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }
    
    public boolean isDirty() {
        return isDirty;
    }
    
    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }
    
    public Date getLastSyncTime() {
        return lastSyncTime;
    }
    
    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
}
