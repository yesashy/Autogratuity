package com.autogratuity.data.model;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing metadata in the Autogratuity app.
 * This is a standalone version of the inner class in various model classes.
 */
public class Metadata {
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String source;
    private String importId;
    private String captureId;
    private long version;
    private Map<String, Object> customData;
    
    // Default constructor required for Firestore
    public Metadata() {
        this.customData = new HashMap<>();
    }
    
    // Getters and setters
    
    public Date getCreatedAt() {
        return createdAt != null ? createdAt.toDate() : null;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt != null ? new Timestamp(createdAt) : null;
    }
    
    public Date getUpdatedAt() {
        return updatedAt != null ? updatedAt.toDate() : null;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt != null ? new Timestamp(updatedAt) : null;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getImportId() {
        return importId;
    }
    
    public void setImportId(String importId) {
        this.importId = importId;
    }
    
    public String getCaptureId() {
        return captureId;
    }
    
    public void setCaptureId(String captureId) {
        this.captureId = captureId;
    }
    
    public long getVersion() {
        return version;
    }
    
    public void setVersion(long version) {
        this.version = version;
    }
    
    public Map<String, Object> getCustomData() {
        return customData;
    }
    
    public void setCustomData(Map<String, Object> customData) {
        this.customData = customData != null ? customData : new HashMap<>();
    }
    
    public void addCustomData(String key, Object value) {
        if (customData == null) {
            customData = new HashMap<>();
        }
        customData.put(key, value);
    }
    
    public Object getCustomDataValue(String key) {
        return customData != null ? customData.get(key) : null;
    }
}
