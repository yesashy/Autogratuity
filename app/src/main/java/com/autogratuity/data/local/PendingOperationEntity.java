package com.autogratuity.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

/**
 * Entity to track operations that need to be performed when online
 * Serves as a queue for pending operations
 */
@Entity(tableName = "pending_operations")
@TypeConverters(Converters.class)
public class PendingOperationEntity {
    
    // Operation types
    public static final String OPERATION_ADD_DELIVERY = "add_delivery";
    public static final String OPERATION_UPDATE_TIP = "update_tip";
    public static final String OPERATION_ADD_ADDRESS = "add_address";
    public static final String OPERATION_UPDATE_ADDRESS = "update_address";
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String operationType;
    private String targetId;
    private String userId;
    private String jsonData;
    private int retryCount;
    private Date createdAt;
    private Date lastAttempt;
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getJsonData() {
        return jsonData;
    }
    
    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getLastAttempt() {
        return lastAttempt;
    }
    
    public void setLastAttempt(Date lastAttempt) {
        this.lastAttempt = lastAttempt;
    }
}
