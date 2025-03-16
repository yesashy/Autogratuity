package com.autogratuity.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

/**
 * Model class representing a user profile in the Autogratuity app.
 * Maps to documents in the user_profiles collection in Firestore.
 */
public class UserProfile {
    
    @DocumentId
    private String userId;
    
    private String email;
    private String displayName;
    private String defaultAddressId;
    private String accountStatus;
    private String timezone;
    
    @ServerTimestamp
    private Timestamp createdAt;
    
    private Timestamp lastLoginAt;
    private Timestamp privacyPolicyAccepted;
    private Timestamp termsAccepted;
    
    // Nested objects
    private Subscription subscription;
    private Preferences preferences;
    private Permissions permissions;
    private Usage usage;
    private SyncInfo syncInfo;
    private AppSettings appSettings;
    private Communication communication;
    
    // Version for conflict resolution
    private long version;
    
    // Default constructor required for Firestore
    public UserProfile() {
    }
    
    /**
     * Nested class for subscription information
     */
    public static class Subscription {
        private String status;
        private String level;
        private Timestamp startDate;
        private Timestamp expiryDate;
        private boolean isLifetime;
        private String provider;
        private String orderId;
        private Timestamp lastVerified;
        
        public Subscription() {
        }
        
        // Getters and setters
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getLevel() {
            return level;
        }
        
        public void setLevel(String level) {
            this.level = level;
        }
        
        public Date getStartDate() {
            return startDate != null ? startDate.toDate() : null;
        }
        
        public void setStartDate(Date startDate) {
            this.startDate = startDate != null ? new Timestamp(startDate) : null;
        }
        
        public Date getExpiryDate() {
            return expiryDate != null ? expiryDate.toDate() : null;
        }
        
        public void setExpiryDate(Date expiryDate) {
            this.expiryDate = expiryDate != null ? new Timestamp(expiryDate) : null;
        }
        
        public boolean isLifetime() {
            return isLifetime;
        }
        
        public void setLifetime(boolean lifetime) {
            isLifetime = lifetime;
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
    }
    
    /**
     * Nested class for user preferences
     */
    public static class Preferences {
        private boolean notificationsEnabled;
        private String theme;
        private boolean useLocation;
        private int defaultTipPercentage;
        
        public Preferences() {
        }
        
        // Getters and setters
        public boolean isNotificationsEnabled() {
            return notificationsEnabled;
        }
        
        public void setNotificationsEnabled(boolean notificationsEnabled) {
            this.notificationsEnabled = notificationsEnabled;
        }
        
        public String getTheme() {
            return theme;
        }
        
        public void setTheme(String theme) {
            this.theme = theme;
        }
        
        public boolean isUseLocation() {
            return useLocation;
        }
        
        public void setUseLocation(boolean useLocation) {
            this.useLocation = useLocation;
        }
        
        public int getDefaultTipPercentage() {
            return defaultTipPercentage;
        }
        
        public void setDefaultTipPercentage(int defaultTipPercentage) {
            this.defaultTipPercentage = defaultTipPercentage;
        }
    }
    
    /**
     * Nested class for user permissions
     */
    public static class Permissions {
        private String level;
        private boolean bypassLimits;
        private int maxUploads;
        
        public Permissions() {
        }
        
        // Getters and setters
        public String getLevel() {
            return level;
        }
        
        public void setLevel(String level) {
            this.level = level;
        }
        
        public boolean isBypassLimits() {
            return bypassLimits;
        }
        
        public void setBypassLimits(boolean bypassLimits) {
            this.bypassLimits = bypassLimits;
        }
        
        public int getMaxUploads() {
            return maxUploads;
        }
        
        public void setMaxUploads(int maxUploads) {
            this.maxUploads = maxUploads;
        }
    }
    
    /**
     * Nested class for usage statistics
     */
    public static class Usage {
        private int mappingCount;
        private int deliveryCount;
        private int addressCount;
        private Timestamp lastUsageUpdate;
        
        public Usage() {
        }
        
        // Getters and setters
        public int getMappingCount() {
            return mappingCount;
        }
        
        public void setMappingCount(int mappingCount) {
            this.mappingCount = mappingCount;
        }
        
        public int getDeliveryCount() {
            return deliveryCount;
        }
        
        public void setDeliveryCount(int deliveryCount) {
            this.deliveryCount = deliveryCount;
        }
        
        public int getAddressCount() {
            return addressCount;
        }
        
        public void setAddressCount(int addressCount) {
            this.addressCount = addressCount;
        }
        
        public Date getLastUsageUpdate() {
            return lastUsageUpdate != null ? lastUsageUpdate.toDate() : null;
        }
        
        public void setLastUsageUpdate(Date lastUsageUpdate) {
            this.lastUsageUpdate = lastUsageUpdate != null ? new Timestamp(lastUsageUpdate) : null;
        }
    }
    
    /**
     * Nested class for synchronization information
     */
    public static class SyncInfo {
        private Timestamp lastSyncTime;
        private List<String> deviceIds;
        private long version;
        
        public SyncInfo() {
        }
        
        // Getters and setters
        public Date getLastSyncTime() {
            return lastSyncTime != null ? lastSyncTime.toDate() : null;
        }
        
        public void setLastSyncTime(Date lastSyncTime) {
            this.lastSyncTime = lastSyncTime != null ? new Timestamp(lastSyncTime) : null;
        }
        
        public List<String> getDeviceIds() {
            return deviceIds;
        }
        
        public void setDeviceIds(List<String> deviceIds) {
            this.deviceIds = deviceIds;
        }
        
        public long getVersion() {
            return version;
        }
        
        public void setVersion(long version) {
            this.version = version;
        }
    }
    
    /**
     * Nested class for app settings
     */
    public static class AppSettings {
        private boolean dataCollectionOptIn;
        private String lastVersion;
        private boolean onboardingCompleted;
        
        public AppSettings() {
        }
        
        // Getters and setters
        public boolean isDataCollectionOptIn() {
            return dataCollectionOptIn;
        }
        
        public void setDataCollectionOptIn(boolean dataCollectionOptIn) {
            this.dataCollectionOptIn = dataCollectionOptIn;
        }
        
        public String getLastVersion() {
            return lastVersion;
        }
        
        public void setLastVersion(String lastVersion) {
            this.lastVersion = lastVersion;
        }
        
        public boolean isOnboardingCompleted() {
            return onboardingCompleted;
        }
        
        public void setOnboardingCompleted(boolean onboardingCompleted) {
            this.onboardingCompleted = onboardingCompleted;
        }
    }
    
    /**
     * Nested class for communication preferences
     */
    public static class Communication {
        private boolean emailOptIn;
        private boolean marketingOptIn;
        private boolean pushNotificationsEnabled;
        
        public Communication() {
        }
        
        // Getters and setters
        public boolean isEmailOptIn() {
            return emailOptIn;
        }
        
        public void setEmailOptIn(boolean emailOptIn) {
            this.emailOptIn = emailOptIn;
        }
        
        public boolean isMarketingOptIn() {
            return marketingOptIn;
        }
        
        public void setMarketingOptIn(boolean marketingOptIn) {
            this.marketingOptIn = marketingOptIn;
        }
        
        public boolean isPushNotificationsEnabled() {
            return pushNotificationsEnabled;
        }
        
        public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) {
            this.pushNotificationsEnabled = pushNotificationsEnabled;
        }
    }
    
    // Getter and setter methods
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDefaultAddressId() {
        return defaultAddressId;
    }
    
    public void setDefaultAddressId(String defaultAddressId) {
        this.defaultAddressId = defaultAddressId;
    }
    
    public String getAccountStatus() {
        return accountStatus;
    }
    
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public Date getCreatedAt() {
        return createdAt != null ? createdAt.toDate() : null;
    }
    
    @PropertyName("createdAt")
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getLastLoginAt() {
        return lastLoginAt != null ? lastLoginAt.toDate() : null;
    }
    
    public void setLastLoginAt(Date lastLoginAt) {
        this.lastLoginAt = lastLoginAt != null ? new Timestamp(lastLoginAt) : null;
    }
    
    public Date getPrivacyPolicyAccepted() {
        return privacyPolicyAccepted != null ? privacyPolicyAccepted.toDate() : null;
    }
    
    public void setPrivacyPolicyAccepted(Date privacyPolicyAccepted) {
        this.privacyPolicyAccepted = privacyPolicyAccepted != null ? 
                new Timestamp(privacyPolicyAccepted) : null;
    }
    
    public Date getTermsAccepted() {
        return termsAccepted != null ? termsAccepted.toDate() : null;
    }
    
    public void setTermsAccepted(Date termsAccepted) {
        this.termsAccepted = termsAccepted != null ? new Timestamp(termsAccepted) : null;
    }
    
    public Subscription getSubscription() {
        return subscription;
    }
    
    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
    
    public Preferences getPreferences() {
        return preferences;
    }
    
    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }
    
    public Permissions getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }
    
    public Usage getUsage() {
        return usage;
    }
    
    public void setUsage(Usage usage) {
        this.usage = usage;
    }
    
    public SyncInfo getSyncInfo() {
        return syncInfo;
    }
    
    public void setSyncInfo(SyncInfo syncInfo) {
        this.syncInfo = syncInfo;
    }
    
    public AppSettings getAppSettings() {
        return appSettings;
    }
    
    public void setAppSettings(AppSettings appSettings) {
        this.appSettings = appSettings;
    }
    
    public Communication getCommunication() {
        return communication;
    }
    
    public void setCommunication(Communication communication) {
        this.communication = communication;
    }
    
    public long getVersion() {
        return version;
    }
    
    public void setVersion(long version) {
        this.version = version;
    }
    
    @Exclude
    public boolean isProUser() {
        return subscription != null && 
               ("pro".equals(subscription.getStatus()) || 
                "lifetime".equals(subscription.getStatus()) ||
                subscription.isLifetime());
    }
}
