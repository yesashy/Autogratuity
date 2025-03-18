package com.autogratuity.data.model;

import java.util.Date;

/**
 * Model class representing the synchronization status of the application.
 * This is not a direct mapping to a Firestore collection but is used to track
 * and report sync status.
 */
public class SyncStatus {
    
    public static final String STATUS_IDLE = "idle";
    public static final String STATUS_SYNCING = "syncing";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_OFFLINE = "offline";
    
    private String status;
    private boolean isOnline;
    private Date lastSyncTime;
    private Date lastFailedSyncTime;
    private String lastError;
    private int pendingOperations;
    private int failedOperations;
    private boolean backgroundSyncEnabled;
    
    // Default constructor
    public SyncStatus() {
        this.status = STATUS_IDLE;
        this.isOnline = true;
        this.pendingOperations = 0;
        this.failedOperations = 0;
        this.backgroundSyncEnabled = true;
    }
    
    /**
     * Constructor with initial status
     * @param status The initial sync status
     */
    public SyncStatus(String status) {
        this.status = status;
        this.isOnline = true;
        this.pendingOperations = 0;
        this.failedOperations = 0;
        this.backgroundSyncEnabled = true;
    }
    
    // Getters and setters
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        isOnline = online;
        if (!online) {
            status = STATUS_OFFLINE;
        } else if (status.equals(STATUS_OFFLINE)) {
            status = STATUS_IDLE;
        }
    }
    
    public Date getLastSyncTime() {
        return lastSyncTime;
    }
    
    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
    
    public Date getLastFailedSyncTime() {
        return lastFailedSyncTime;
    }
    
    public void setLastFailedSyncTime(Date lastFailedSyncTime) {
        this.lastFailedSyncTime = lastFailedSyncTime;
    }
    
    public String getLastError() {
        return lastError;
    }
    
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
    
    public int getPendingOperations() {
        return pendingOperations;
    }
    
    public void setPendingOperations(int pendingOperations) {
        this.pendingOperations = pendingOperations;
    }
    
    public int getFailedOperations() {
        return failedOperations;
    }
    
    public void setFailedOperations(int failedOperations) {
        this.failedOperations = failedOperations;
    }
    
    public boolean isBackgroundSyncEnabled() {
        return backgroundSyncEnabled;
    }
    
    public void setBackgroundSyncEnabled(boolean backgroundSyncEnabled) {
        this.backgroundSyncEnabled = backgroundSyncEnabled;
    }
    
    /**
     * Convenience method to update status with error information
     * 
     * @param errorMessage The error message
     */
    public void setError(String errorMessage) {
        this.status = STATUS_ERROR;
        this.lastError = errorMessage;
        this.lastFailedSyncTime = new Date();
    }
    
    /**
     * Convenience method to mark sync as started
     */
    public void setSyncing() {
        this.status = STATUS_SYNCING;
    }
    
    /**
     * Convenience method to mark sync as completed successfully
     */
    public void setCompleted() {
        this.status = STATUS_IDLE;
        this.lastSyncTime = new Date();
    }
    
    /**
     * Check if sync is currently in progress
     * @return true if sync is in progress
     */
    public boolean isInProgress() {
        return STATUS_SYNCING.equals(status);
    }

    /**
     * Check if sync has an error
     * @return true if sync has an error
     */
    public boolean isError() {
        return STATUS_ERROR.equals(status) || (lastError != null && !lastError.isEmpty());
    }
    
    /**
     * Get a human-readable status message
     * 
     * @return A user-friendly status message
     */
    public String getStatusMessage() {
        switch (status) {
            case STATUS_SYNCING:
                return "Syncing data...";
            case STATUS_ERROR:
                return "Sync error: " + (lastError != null ? lastError : "Unknown error");
            case STATUS_OFFLINE:
                return "You're offline. Changes will sync when you reconnect.";
            case STATUS_IDLE:
            default:
                if (lastSyncTime != null) {
                    long diffMinutes = (new Date().getTime() - lastSyncTime.getTime()) / (60 * 1000);
                    if (diffMinutes < 1) {
                        return "Data synced just now";
                    } else if (diffMinutes < 60) {
                        return "Data synced " + diffMinutes + " minute" + (diffMinutes > 1 ? "s" : "") + " ago";
                    } else if (diffMinutes < 1440) { // Less than a day
                        long diffHours = diffMinutes / 60;
                        return "Data synced " + diffHours + " hour" + (diffHours > 1 ? "s" : "") + " ago";
                    } else {
                        long diffDays = diffMinutes / 1440;
                        return "Data synced " + diffDays + " day" + (diffDays > 1 ? "s" : "") + " ago";
                    }
                } else {
                    return "Not synced yet";
                }
        }
    }
}