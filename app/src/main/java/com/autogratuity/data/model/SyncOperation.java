package com.autogratuity.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import com.autogratuity.data.model.ErrorInfo;
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
    private ErrorInfo errorInfo;
    private int attempts;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String authToken;
    private String status;
    private Timestamp completedAt;
    private Timestamp lastAttemptTime;
    private Timestamp nextAttemptTime;
    private int retryCount;
    private int maxRetries = 5; // Increased from 3 to 5 for more retry attempts
    private String conflictResolution;
    private Map<String, Object> previousVersion;
    private String errorType; // Type of error for better retry handling
    private boolean retryable; // Flag indicating if operation is retryable
    private boolean hasConflict; // Flag indicating if a conflict was detected
    private String conflictType; // Type of conflict detected
    private Map<String, Object> conflictDetails; // Detailed information about the conflict
    
    // Default constructor required for Firestore
    public SyncOperation() {
        this.data = new HashMap<>();
        this.completed = false;
        this.failed = false;
        this.attempts = 0;
        this.hasConflict = false;
        this.conflictDetails = new HashMap<>();
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
    
    /**
     * Get the error message.
     * @return String error message
     * @deprecated Use getErrorInfo() instead for more structured error information
     */
    @Deprecated
    public String getError() {
        return error;
    }
    
    /**
     * Get detailed error information.
     * @return ErrorInfo object or null if no error
     */
    public ErrorInfo getErrorInfo() {
        if (error == null) return null;
        
        // Use existing errorInfo if available, otherwise create one
        if (errorInfo != null) {
            return errorInfo;
        }
        
        return new ErrorInfo("error", error, updatedAt != null ? updatedAt.toDate() : null);
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
        // Use RetryWithBackoff to determine if we should retry
        com.autogratuity.data.repository.sync.RetryWithBackoff retryWithBackoff = 
                new com.autogratuity.data.repository.sync.RetryWithBackoff(maxRetries, 2.0, 1000, 3600000);
                
        boolean underMaxRetries = retryWithBackoff.shouldRetry(retryCount);
        boolean hasAppropriateStatus = STATUS_FAILED.equals(status) || STATUS_RETRYING.equals(status);
        
        return underMaxRetries && hasAppropriateStatus && retryable;
    }
    
    /**
     * Get the conflict resolution strategy.
     * @return String conflict resolution strategy
     */
    public String getConflictResolution() {
        return conflictResolution;
    }
    
    /**
     * Set the conflict resolution strategy.
     * @param strategy Conflict resolution strategy to use
     */
    public void setConflictResolution(String strategy) {
        this.conflictResolution = strategy;
    }
    
    /**
     * Get the previous version data.
     * @return Map previous version data
     */
    public Map<String, Object> getPreviousVersion() {
        return previousVersion;
    }
    
    /**
     * Set the previous version data.
     * @param previousVersion Previous version data to store
     */
    public void setPreviousVersion(Map<String, Object> previousVersion) {
        this.previousVersion = previousVersion;
    }
    
    /**
     * Get the next attempt time.
     * @return Timestamp next attempt time
     */
    public Timestamp getNextAttemptTime() {
        return nextAttemptTime;
    }
    
    /**
     * Calculate and get the delay until next retry in milliseconds
     * @return long milliseconds until next retry or -1 if not retryable
     */
    public long getDelayUntilNextRetryMs() {
        if (!canRetry() || nextAttemptTime == null) {
            return -1;
        }
        
        long nextTimeMs = nextAttemptTime.toDate().getTime();
        long currentTimeMs = System.currentTimeMillis();
        
        return Math.max(0, nextTimeMs - currentTimeMs);
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
     * Get the retry count for this operation.
     * @return int retry count
     */
    public int getRetryCount() {
        return retryCount;
    }
    
    /**
     * Get error code.
     * @return String error code
     */
    public String getErrorCode() {
        return error != null ? "error" : null;
    }
    
    /**
     * Get error timestamp.
     * @return String error timestamp
     */
    public String getErrorTimestamp() {
        return updatedAt != null ? updatedAt.toString() : null;
    }
    
    /**
     * Mark operation as failed with error details
     * @param errorCode Error code
     * @param errorMessage Error message
     * @param isRetryable Whether this failure is retryable
     */
    /**
     * Check if this operation has a conflict.
     * @return true if a conflict has been detected
     */
    public boolean hasConflict() {
        return hasConflict;
    }
    
    /**
     * Set the conflict status flag.
     * @param hasConflict true if a conflict has been detected
     */
    public void setHasConflict(boolean hasConflict) {
        this.hasConflict = hasConflict;
    }
    
    /**
     * Get the conflict type.
     * @return String conflict type identifier
     */
    public String getConflictType() {
        return conflictType;
    }
    
    /**
     * Set the conflict type.
     * @param conflictType Type of conflict detected
     */
    public void setConflictType(String conflictType) {
        this.conflictType = conflictType;
    }
    
    /**
     * Get detailed information about the conflict.
     * @return Map with conflict details
     */
    public Map<String, Object> getConflictDetails() {
        return conflictDetails;
    }
    
    /**
     * Set detailed information about the conflict.
     * @param conflictDetails Map with conflict details
     */
    public void setConflictDetails(Map<String, Object> conflictDetails) {
        this.conflictDetails = conflictDetails != null ? conflictDetails : new HashMap<>();
    }
    
    /**
     * Mark an operation as having a conflict.
     * @param conflictType Type of conflict detected
     * @param details Detailed information about the conflict
     * @param recommendedResolution Recommended resolution strategy
     */
    public void markAsConflicted(String conflictType, Map<String, Object> details, String recommendedResolution) {
        this.hasConflict = true;
        this.conflictType = conflictType;
        this.conflictDetails = details != null ? details : new HashMap<>();
        this.conflictResolution = recommendedResolution;
        
        // Add conflict information to the error info if it exists
        if (this.errorInfo != null) {
            this.errorInfo.setCode("CONFLICT_" + conflictType);
            this.errorInfo.setRecoveryAction(ErrorInfo.RECOVERY_MANUAL_RESOLVE);
            this.errorInfo.addDetail("conflictType", conflictType);
            this.errorInfo.addDetail("recommendedResolution", recommendedResolution);
            
            // Add number of conflicting fields if available
            if (details != null && details.containsKey("conflictingFields")) {
                Object conflictingFields = details.get("conflictingFields");
                if (conflictingFields instanceof Map) {
                    this.errorInfo.addDetail("conflictingFieldCount", ((Map) conflictingFields).size());
                }
            }
        }
    }
    
    public void markAsFailed(String errorCode, String errorMessage, boolean isRetryable) {
        this.failed = true;
        this.completed = false;
        this.error = errorMessage;
        this.errorType = errorCode;
        this.retryable = isRetryable;
        this.attempts++;
        this.retryCount++;
        
        // Use RetryWithBackoff to calculate next retry time with standardized parameters
        com.autogratuity.data.repository.sync.RetryWithBackoff retryWithBackoff = 
                new com.autogratuity.data.repository.sync.RetryWithBackoff(maxRetries, 2.0, 1000, 3600000);
        
        // Log retry attempt if operation is retryable
        if (isRetryable && canRetry()) {
            Throwable error = new Exception(errorMessage);
            retryWithBackoff.logRetryAttempt(this.operationId, this.retryCount, error);
            Date nextAttempt = retryWithBackoff.getNextRetryTime(this.retryCount);
            this.nextAttemptTime = new Timestamp(nextAttempt);
        }
        
        // Create error info object with retry information
        this.errorInfo = new ErrorInfo(errorCode, errorMessage, new Date());
        if (isRetryable && canRetry()) {
            this.errorInfo.setRecoveryAction(ErrorInfo.RECOVERY_RETRY);
            // Include the next retry time in error info for better visibility
            this.errorInfo.addDetail("nextRetryTime", this.nextAttemptTime);
            this.errorInfo.addDetail("retryCount", this.retryCount);
            this.errorInfo.addDetail("maxRetries", this.maxRetries);
        }
    }
    
    /**
     * Mark operation as failed with error details (backward compatibility)
     * @param errorCode Error code
     * @param errorMessage Error message
     */
    public void markAsFailed(String errorCode, String errorMessage) {
        // Use RetryWithBackoff to determine if error is retryable
        com.autogratuity.data.repository.sync.RetryWithBackoff retryWithBackoff = 
                new com.autogratuity.data.repository.sync.RetryWithBackoff();
        
        // Create exception to use RetryWithBackoff's error detection
        Exception exception = new Exception(errorMessage) {
            @Override
            public String toString() {
                return errorCode + ": " + getMessage();
            }
        };
        
        boolean isRetryable = retryWithBackoff.isRetryableError(exception);
        
        // If not detected as retryable, use the basic heuristic as fallback
        if (!isRetryable && errorCode != null) {
            isRetryable = errorCode.contains("UNAVAILABLE") ||
                errorCode.contains("RESOURCE_EXHAUSTED") ||
                errorCode.contains("DEADLINE_EXCEEDED") ||
                errorCode.contains("INTERNAL") ||
                errorCode.contains("ABORTED") ||
                errorCode.contains("NETWORK") ||
                errorCode.contains("TIMEOUT");
        }
        
        markAsFailed(errorCode, errorMessage, isRetryable);
    }
}
