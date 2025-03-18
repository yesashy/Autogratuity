package com.autogratuity.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a sync operation in the Autogratuity app.
 * Maps to documents in the sync_operations collection in Firestore.
 */
public class SyncOperation {
    
    // Status constants
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_RETRYING = "retrying";
    
    // Conflict resolution constants
    public static final String CONFLICT_RESOLUTION_SERVER_WINS = "server_wins";
    public static final String CONFLICT_RESOLUTION_CLIENT_WINS = "client_wins";

    @DocumentId
    private String operationId;
    
    private String userId;
    private String deviceId;
    private String type;  // create, update, delete
    private String entityType;  // userProfile, address, delivery, etc.
    private String entityId;
    private Map<String, Object> data;
    private boolean completed;
    private boolean failed;
    private String error;
    private int attempts;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String authToken;
    private String status;
    private Timestamp completedAt;
    private Timestamp lastAttemptTime;
    private Timestamp nextAttemptTime;
    private int retryCount;
    private int maxRetries = 3;
    private String conflictResolution;
    private Map<String, Object> previousVersion;
    
    // Default constructor required for Firestore
    public SyncOperation() {
        this.data = new HashMap<>();
        this.completed = false;
        this.failed = false;
        this.attempts = 0;
    }
    
    /**
     * Constructor with essential fields
     */
    public SyncOperation(String userId, String type, String entityType, String entityId, Map<String, Object> data) {
        this();
        this.userId = userId;
        this.type = type;
        this.entityType = entityType;
        this.entityId = entityId;
        this.data = data != null ? data : new HashMap<>();
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
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
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
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data != null ? data : new HashMap<>();
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public boolean isFailed() {
        return failed;
    }
    
    public void setFailed(boolean failed) {
        this.failed = failed;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public int getAttempts() {
        return attempts;
    }
    
    public void setAttempts(int attempts) {
        this.attempts = attempts;
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
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    /**
     * Increment the attempt counter
     */
    public void incrementAttempts() {
        this.attempts++;
    }
    
    /**
     * Get the operation type.
     * @return String operation type
     */
    public String getOperationType() {
        return type;
    }
    
    /**
     * Check if the operation can be retried.
     * @return boolean indicating if retry is possible
     */
    public boolean canRetry() {
        return retryCount < maxRetries && (STATUS_FAILED.equals(status) || STATUS_RETRYING.equals(status));
    }
    
    /**
     * Get the conflict resolution strategy.
     * @return String conflict resolution strategy
     */
    public String getConflictResolution() {
        return conflictResolution;
    }
    
    /**
     * Get the previous version data.
     * @return Map previous version data
     */
    public Map<String, Object> getPreviousVersion() {
        return previousVersion;
    }
    
    /**
     * Get the next attempt time.
     * @return Timestamp next attempt time
     */
    public Timestamp getNextAttemptTime() {
        return nextAttemptTime;
    }
    
    /**
     * Set the operation status.
     * @param status New status
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Set the completion timestamp.
     * @param completedAt Completion timestamp
     */
    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt != null ? new Timestamp(completedAt) : null;
    }
    
    /**
     * Set the last attempt timestamp.
     * @param lastAttemptTime Last attempt timestamp
     */
    public void setLastAttemptTime(Date lastAttemptTime) {
        this.lastAttemptTime = lastAttemptTime != null ? new Timestamp(lastAttemptTime) : null;
    }
    
    /**
     * Get the current status.
     * @return String status
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Get error code.
     * @return String error code
     */
    public String getErrorCode() {
        return error != null ? "error" : null;
    }
    
    /**
     * Get error message.
     * @return String error message
     */
    public String getErrorMessage() {
        return error;
    }
    
    /**
     * Get error timestamp.
     * @return String error timestamp
     */
    public String getErrorTimestamp() {
        return updatedAt != null ? updatedAt.toString() : null;
    }
}
