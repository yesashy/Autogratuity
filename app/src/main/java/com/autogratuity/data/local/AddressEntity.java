package com.autogratuity.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.List;

/**
 * Room entity for local caching of address data
 */
@Entity(tableName = "addresses")
@TypeConverters({Converters.class, ListConverter.class})
public class AddressEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String documentId;
    private String fullAddress;
    private String normalizedAddress;
    private List<String> orderIds;
    private double totalTips;
    private int deliveryCount;
    private double averageTip;
    private String userId;
    private boolean doNotDeliver;
    private List<String> searchTerms;
    private Date lastUpdated;
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
    
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
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
