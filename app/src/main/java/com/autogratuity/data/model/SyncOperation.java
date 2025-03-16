package com.autogratuity.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.Map;

/**
 * Model class representing a sync operation in the Autogratuity app.
 * Maps to documents in the sync_operations collection in Firestore.
 */
public class SyncOperation {
    
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_RETRYING = "retrying";
    
    public static final String CONFLICT_RESOLUTION_SERVER_WINS = "server_wins";
    public static final String CONFLICT_RESOLUTION_CLIENT_WINS = "client_wins";
    public static final String CONFLICT_RESOLUTION_MERGE = "merge";
    
    @DocumentId
    private String operationId;
    
    private String userId;
    private String operationType;  // create, update, delete
    private String entityType;     // userProfile, address, delivery, etc.
    private String entityId;
    private String status;
    private int priority;          // Higher numbers mean higher priority
    private String deviceId;
    private int attempts;
    private int maxAttempts;
    private String conflictResolution;
    
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp lastAttemptTime;
    private Timestamp nextAttemptTime;
    private Timestamp completedAt;
    
    private Map<String, Object> data;            // The new data to apply
    private Map<String, Object> previousVersion; // Original data for conflict resolution
    
    private Error error;
    
    // Default constructor required for Firestore
    public SyncOperation() {
    }
    
    /**
     * Create a basic sync operation
     * 
     * @param userId User ID
     * @param operationType Operation type (create, update, delete)
     * @param entityType Entity type (userProfile, address, delivery, etc.)
     * @param entityId Entity ID
     * @param data The data to sync
     */
    public SyncOperation(String userId, String operationType, String entityType, String entityId, Map<String, Object> data) {
        this.userId = userId;
        this.operationType = operationType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.data = data;
        this.status = STATUS_PENDING;
        this.priority = 0;
        this.attempts = 0;
        this.maxAttempts = 5;
        this.conflictResolution = CONFLICT_RESOLUTION_SERVER_WINS;
    }
    
    /**
     * Create a complete sync operation
     * 
     * @param userId User ID
     * @param operationType Operation type (create, update, delete)
     * @param entityType Entity type (userProfile, address, delivery, etc.)
     * @param entityId Entity ID
     * @param data The data to sync
     * @param previousVersion The original data for conflict resolution
     * @param priority Priority level (higher means higher priority)
     * @param conflictResolution Conflict resolution strategy
     */
    public SyncOperation(String userId, String operationType, String entityType, String entityId, 
                         Map<String, Object> data, Map<String, Object> previousVersion, 
                         int priority, String conflictResolution) {
        this.userId = userId;
        this.operationType = operationType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.data = data;
        this.previousVersion = previousVersion;
        this.status = STATUS_PENDING;
        this.priority = priority;
        this.attempts = 0;
        this.maxAttempts = 5;
        this.conflictResolution = conflictResolution;
    }
    
    /**
     * Nested class for error information
     */
    public static class Error {
        private String code;
        private String message;
        private Timestamp timestamp;
        
        public Error() {
        }
        
        public Error(String code, String message) {
            this.code = code;
            this.message = message;
            this.timestamp = new Timestamp(new Date());
        }
        
        // Getters and setters
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public Date getTimestamp() {
            return timestamp != null ? timestamp.toDate() : null;
        }
        
        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp != null ? new Timestamp(timestamp) : null;
        }
    }
    
    // Getters and setters
    
    public String getOperationId() {
        return operationId;
    }
    
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public String getEntityId() {
        return entityId;
    }
    
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public int getAttempts() {
        return attempts;
    }
    
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
    
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    
    public String getConflictResolution() {
        return conflictResolution;
    }
    
    public void setConflictResolution(String conflictResolution) {
        this.conflictResolution = conflictResolution;
    }
    
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
    
    public Date getLastAttemptTime() {
        return lastAttemptTime != null ? lastAttemptTime.toDate() : null;
    }
    
    public void setLastAttemptTime(Date lastAttemptTime) {
        this.lastAttemptTime = lastAttemptTime != null ? new Timestamp(lastAttemptTime) : null;
    }
    
    public Date getNextAttemptTime() {
        return nextAttemptTime != null ? nextAttemptTime.toDate() : null;
    }
    
    public void setNextAttemptTime(Date nextAttemptTime) {
        this.nextAttemptTime = nextAttemptTime != null ? new Timestamp(nextAttemptTime) : null;
    }
    
    public Date getCompletedAt() {
        return completedAt != null ? completedAt.toDate() : null;
    }
    
    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt != null ? new Timestamp(completedAt) : null;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public Map<String, Object> getPreviousVersion() {
        return previousVersion;
    }
    
    public void setPreviousVersion(Map<String, Object> previousVersion) {
        this.previousVersion = previousVersion;
    }
    
    public Error getError() {
        return error;
    }
    
    public void setError(Error error) {
        this.error = error;
    }
    
    /**
     * Convenience method to check if this operation can be retried
     * 
     * @return True if the operation can be retried
     */
    @Exclude
    public boolean canRetry() {
        return (status.equals(STATUS_FAILED) || status.equals(STATUS_RETRYING)) && attempts < maxAttempts;
    }
    
    /**
     * Convenience method to mark an operation as failed
     * 
     * @param errorCode Error code
     * @param errorMessage Error message
     */
    @Exclude
    public void markAsFailed(String errorCode, String errorMessage) {
        this.status = STATUS_FAILED;
        this.attempts++;
        this.lastAttemptTime = new Timestamp(new Date());
        
        if (this.error == null) {
            this.error = new Error();
        }
        
        this.error.setCode(errorCode);
        this.error.setMessage(errorMessage);
        this.error.setTimestamp(new Date());
        
        // Set next retry time based on exponential backoff
        if (this.attempts < this.maxAttempts) {
            long backoffMillis = Math.min(1000 * (long) Math.pow(2, this.attempts), 1000 * 60 * 30); // Cap at 30 minutes
            Date nextRetry = new Date(System.currentTimeMillis() + backoffMillis);
            this.nextAttemptTime = new Timestamp(nextRetry);
        }
    }
    
    /**
     * Convenience method to mark an operation as completed
     */
    @Exclude
    public void markAsCompleted() {
        this.status = STATUS_COMPLETED;
        this.completedAt = new Timestamp(new Date());
        this.updatedAt = new Timestamp(new Date());
    }
}
