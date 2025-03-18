package com.autogratuity.data.model;

import com.google.firebase.Timestamp;

import java.util.Date;

/**
 * Model class representing status information in the Autogratuity app.
 * This is a standalone version of the inner class in Delivery.
 */
public class Status {
    private String state;  // created, accepted, completed, canceled
    private boolean isTipped;
    private boolean isCompleted;
    private boolean isVerified;
    private String cancellationReason;
    private String verificationSource;
    private Timestamp verificationTimestamp;
    
    // Default constructor required for Firestore
    public Status() {
    }
    
    // Getters and setters
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public boolean isTipped() {
        return isTipped;
    }
    
    public void setTipped(boolean tipped) {
        isTipped = tipped;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    
    public boolean isVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public String getVerificationSource() {
        return verificationSource;
    }
    
    public void setVerificationSource(String verificationSource) {
        this.verificationSource = verificationSource;
    }
    
    public Date getVerificationTimestamp() {
        return verificationTimestamp != null ? verificationTimestamp.toDate() : null;
    }
    
    public void setVerificationTimestamp(Date verificationTimestamp) {
        this.verificationTimestamp = verificationTimestamp != null ? 
                new Timestamp(verificationTimestamp) : null;
    }
}
