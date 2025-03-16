package com.autogratuity.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.List;

/**
 * Model class representing a delivery in the Autogratuity app.
 * Maps to documents in the deliveries collection in Firestore.
 */
public class Delivery {
    
    @DocumentId
    private String deliveryId;
    
    private String userId;
    private String orderId;
    
    // Nested objects
    private Reference reference;
    private Address.SimpleAddress address;
    private Amounts amounts;
    private Times times;
    private Status status;
    private Platform platform;
    private Customer customer;
    private Items items;
    private DisputeInfo disputeInfo;
    private Metadata metadata;
    
    private String notes;
    private List<String> tags;
    
    // Default constructor required for Firestore
    public Delivery() {
    }
    
    /**
     * Nested class for references to other entities
     */
    public static class Reference {
        private String addressId;
        private String platformOrderId;
        
        public Reference() {
        }
        
        // Getters and setters
        public String getAddressId() {
            return addressId;
        }
        
        public void setAddressId(String addressId) {
            this.addressId = addressId;
        }
        
        public String getPlatformOrderId() {
            return platformOrderId;
        }
        
        public void setPlatformOrderId(String platformOrderId) {
            this.platformOrderId = platformOrderId;
        }
    }
    
    /**
     * Nested class for monetary amounts
     */
    public static class Amounts {
        private double baseAmount;
        private double estimatedPay;
        private double finalPay;
        private double tipAmount;
        private Double tipPercentage;
        private double distanceMiles;
        private String currency;
        
        public Amounts() {
        }
        
        // Getters and setters
        public double getBaseAmount() {
            return baseAmount;
        }
        
        public void setBaseAmount(double baseAmount) {
            this.baseAmount = baseAmount;
        }
        
        public double getEstimatedPay() {
            return estimatedPay;
        }
        
        public void setEstimatedPay(double estimatedPay) {
            this.estimatedPay = estimatedPay;
        }
        
        public double getFinalPay() {
            return finalPay;
        }
        
        public void setFinalPay(double finalPay) {
            this.finalPay = finalPay;
        }
        
        public double getTipAmount() {
            return tipAmount;
        }
        
        public void setTipAmount(double tipAmount) {
            this.tipAmount = tipAmount;
        }
        
        public Double getTipPercentage() {
            return tipPercentage;
        }
        
        public void setTipPercentage(Double tipPercentage) {
            this.tipPercentage = tipPercentage;
        }
        
        public double getDistanceMiles() {
            return distanceMiles;
        }
        
        public void setDistanceMiles(double distanceMiles) {
            this.distanceMiles = distanceMiles;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
    
    /**
     * Nested class for timestamps of delivery events
     */
    public static class Times {
        private Timestamp orderedAt;
        private Timestamp acceptedAt;
        private Timestamp pickedUpAt;
        private Timestamp completedAt;
        private Timestamp tippedAt;
        private int estimatedDuration;
        
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
    
    /**
     * Nested class for delivery status
     */
    public static class Status {
        private String state;  // created, accepted, completed, canceled
        private boolean isTipped;
        private boolean isCompleted;
        private boolean isVerified;
        private String cancellationReason;
        private String verificationSource;
        private Timestamp verificationTimestamp;
        
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
    
    /**
     * Nested class for delivery platform information
     */
    public static class Platform {
        private String name;
        private String displayName;
        private String iconUrl;
        
        public Platform() {
        }
        
        // Getters and setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        public String getIconUrl() {
            return iconUrl;
        }
        
        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }
    }
    
    /**
     * Nested class for customer information
     */
    public static class Customer {
        private String name;
        private String contactInfo;
        private float rating;
        
        public Customer() {
        }
        
        // Getters and setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getContactInfo() {
            return contactInfo;
        }
        
        public void setContactInfo(String contactInfo) {
            this.contactInfo = contactInfo;
        }
        
        public float getRating() {
            return rating;
        }
        
        public void setRating(float rating) {
            this.rating = rating;
        }
    }
    
    /**
     * Nested class for order items information
     */
    public static class Items {
        private int count;
        private String description;
        private List<String> itemsList;
        
        public Items() {
        }
        
        // Getters and setters
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public List<String> getItemsList() {
            return itemsList;
        }
        
        public void setItemsList(List<String> itemsList) {
            this.itemsList = itemsList;
        }
    }
    
    /**
     * Nested class for dispute information
     */
    public static class DisputeInfo {
        private boolean hasDispute;
        private String disputeReason;
        private String disputeStatus;
        private String resolution;
        private Timestamp disputeDate;
        
        public DisputeInfo() {
        }
        
        // Getters and setters
        public boolean isHasDispute() {
            return hasDispute;
        }
        
        public void setHasDispute(boolean hasDispute) {
            this.hasDispute = hasDispute;
        }
        
        public String getDisputeReason() {
            return disputeReason;
        }
        
        public void setDisputeReason(String disputeReason) {
            this.disputeReason = disputeReason;
        }
        
        public String getDisputeStatus() {
            return disputeStatus;
        }
        
        public void setDisputeStatus(String disputeStatus) {
            this.disputeStatus = disputeStatus;
        }
        
        public String getResolution() {
            return resolution;
        }
        
        public void setResolution(String resolution) {
            this.resolution = resolution;
        }
        
        public Date getDisputeDate() {
            return disputeDate != null ? disputeDate.toDate() : null;
        }
        
        public void setDisputeDate(Date disputeDate) {
            this.disputeDate = disputeDate != null ? new Timestamp(disputeDate) : null;
        }
    }
    
    /**
     * Nested class for metadata
     */
    public static class Metadata {
        private Timestamp createdAt;
        private Timestamp updatedAt;
        private String source;
        private String importId;
        private String captureId;
        private long version;
        
        public Metadata() {
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
    }
    
    // Getters and setters
    
    public String getDeliveryId() {
        return deliveryId;
    }
    
    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public Reference getReference() {
        return reference;
    }
    
    public void setReference(Reference reference) {
        this.reference = reference;
    }
    
    public Address.SimpleAddress getAddress() {
        return address;
    }
    
    public void setAddress(Address.SimpleAddress address) {
        this.address = address;
    }
    
    public Amounts getAmounts() {
        return amounts;
    }
    
    public void setAmounts(Amounts amounts) {
        this.amounts = amounts;
    }
    
    public Times getTimes() {
        return times;
    }
    
    public void setTimes(Times times) {
        this.times = times;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Platform getPlatform() {
        return platform;
    }
    
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public Items getItems() {
        return items;
    }
    
    public void setItems(Items items) {
        this.items = items;
    }
    
    public DisputeInfo getDisputeInfo() {
        return disputeInfo;
    }
    
    public void setDisputeInfo(DisputeInfo disputeInfo) {
        this.disputeInfo = disputeInfo;
    }
    
    public Metadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    /**
     * Calculate the actual duration of the delivery in minutes
     * 
     * @return Duration in minutes, or -1 if timestamps are incomplete
     */
    @Exclude
    public int getActualDurationMinutes() {
        if (times == null || times.getAcceptedAt() == null || times.getCompletedAt() == null) {
            return -1;
        }
        
        long durationMs = times.getCompletedAt().getTime() - times.getAcceptedAt().getTime();
        return (int) (durationMs / (1000 * 60));
    }
    
    /**
     * Get the display name of the delivery status
     * 
     * @return User-friendly status string
     */
    @Exclude
    public String getStatusDisplayName() {
        if (status == null) {
            return "Unknown";
        }
        
        if (status.isTipped()) {
            return "Tipped";
        }
        
        if (status.isCompleted()) {
            return "Completed";
        }
        
        if ("canceled".equals(status.getState())) {
            return "Canceled";
        }
        
        if ("accepted".equals(status.getState())) {
            return "In Progress";
        }
        
        if ("created".equals(status.getState())) {
            return "Created";
        }
        
        return "Unknown";
    }
}
