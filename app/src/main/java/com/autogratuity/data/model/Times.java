package com.autogratuity.data.model;

import com.google.firebase.Timestamp;

import java.util.Date;

/**
 * Model class representing time-related fields in the Autogratuity app.
 * This is a standalone version of the inner class in Delivery.
 */
public class Times {
    private Timestamp orderedAt;
    private Timestamp acceptedAt;
    private Timestamp pickedUpAt;
    private Timestamp completedAt;
    private Timestamp tippedAt;
    private int estimatedDuration;
    
    // Default constructor required for Firestore
    public Times() {
    }
    
    // Getters and setters
    
    public Date getOrderedAt() {
        return orderedAt != null ? orderedAt.toDate() : null;
    }
    
    public void setOrderedAt(Date orderedAt) {
        this.orderedAt = orderedAt != null ? new Timestamp(orderedAt) : null;
    }
    
    public Date getAcceptedAt() {
        return acceptedAt != null ? acceptedAt.toDate() : null;
    }
    
    public void setAcceptedAt(Date acceptedAt) {
        this.acceptedAt = acceptedAt != null ? new Timestamp(acceptedAt) : null;
    }
    
    public Date getPickedUpAt() {
        return pickedUpAt != null ? pickedUpAt.toDate() : null;
    }
    
    public void setPickedUpAt(Date pickedUpAt) {
        this.pickedUpAt = pickedUpAt != null ? new Timestamp(pickedUpAt) : null;
    }
    
    public Date getCompletedAt() {
        return completedAt != null ? completedAt.toDate() : null;
    }
    
    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt != null ? new Timestamp(completedAt) : null;
    }
    
    public Date getTippedAt() {
        return tippedAt != null ? tippedAt.toDate() : null;
    }
    
    public void setTippedAt(Date tippedAt) {
        this.tippedAt = tippedAt != null ? new Timestamp(tippedAt) : null;
    }
    
    public int getEstimatedDuration() {
        return estimatedDuration;
    }
    
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }
}
