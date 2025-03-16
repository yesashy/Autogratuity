package com.autogratuity.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.Date;

/**
 * Model class for subscription status.
 * This is a simplified version of the subscription record for UI and business logic use.
 */
public class SubscriptionStatus {
    
    private String userId;
    private String status; // "free", "pro", "lifetime"
    private String level;  // More specific level details
    private boolean isActive;
    private boolean isLifetime;
    private Timestamp expiryDate;
    private Timestamp startDate;
    private String provider;
    private String orderId;
    private Timestamp lastVerified;
    private String verificationStatus;
    private String verificationError;
    
    // Default constructor required for Firestore
    public SubscriptionStatus() {
    }
    
    /**
     * Convenience constructor to create a free subscription status
     * @param userId The user ID
     */
    public SubscriptionStatus(String userId) {
        this.userId = userId;
        this.status = "free";
        this.isActive = true;
        this.isLifetime = false;
    }
    
    /**
     * Convenience constructor to create a specific subscription status
     * @param userId The user ID
     * @param status The subscription status
     */
    public SubscriptionStatus(String userId, String status) {
        this.userId = userId;
        this.status = status;
        this.isActive = true;
        this.isLifetime = "lifetime".equals(status);
    }
    
    // Getters and setters
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.isLifetime = "lifetime".equals(status);
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isLifetime() {
        return isLifetime;
    }
    
    public void setLifetime(boolean lifetime) {
        isLifetime = lifetime;
        if (lifetime) {
            status = "lifetime";
        }
    }
    
    public Date getExpiryDate() {
        return expiryDate != null ? expiryDate.toDate() : null;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate != null ? new Timestamp(expiryDate) : null;
    }
    
    public Date getStartDate() {
        return startDate != null ? startDate.toDate() : null;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate != null ? new Timestamp(startDate) : null;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public Date getLastVerified() {
        return lastVerified != null ? lastVerified.toDate() : null;
    }
    
    public void setLastVerified(Date lastVerified) {
        this.lastVerified = lastVerified != null ? new Timestamp(lastVerified) : null;
    }
    
    public String getVerificationStatus() {
        return verificationStatus;
    }
    
    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
    
    public String getVerificationError() {
        return verificationError;
    }
    
    public void setVerificationError(String verificationError) {
        this.verificationError = verificationError;
    }
    
    /**
     * Convenience method to check if the user is a pro user
     * @return True if the user has an active pro or lifetime subscription
     */
    public boolean isPro() {
        if (!isActive) {
            return false;
        }
        
        if (isLifetime || "lifetime".equals(status)) {
            return true;
        }
        
        if ("pro".equals(status)) {
            // Check if expired
            if (expiryDate != null) {
                Date now = new Date();
                Date expiry = expiryDate.toDate();
                return !now.after(expiry);
            }
            return true; // No expiry date means indefinite
        }
        
        return false;
    }
}
