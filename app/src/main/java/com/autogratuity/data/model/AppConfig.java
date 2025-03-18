package com.autogratuity.data.model;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing application configuration from Firestore.
 * Maps to documents in the system_config collection in Firestore.
 */
public class AppConfig {
    
    private Versions versions;
    private Features features;
    private Limits limits;
    private Sync sync;
    private Maintenance maintenance;
    private Timestamp updatedAt;
    private long version;
    private Map<String, Object> customData;
    
    // Default constructor required for Firestore
    public AppConfig() {
    }
    
    /**
     * Nested class for version information
     */
    public static class Versions {
        private String minimum;
        private String recommended;
        private String latest;
        
        public Versions() {
        }
        
        // Getters and setters
        public String getMinimum() {
            return minimum;
        }
        
        public void setMinimum(String minimum) {
            this.minimum = minimum;
        }
        
        public String getRecommended() {
            return recommended;
        }
        
        public void setRecommended(String recommended) {
            this.recommended = recommended;
        }
        
        public String getLatest() {
            return latest;
        }
        
        public void setLatest(String latest) {
            this.latest = latest;
        }
    }
    
    /**
     * Nested class for feature flags
     */
    public static class Features {
        private boolean useNewSyncSystem;
        private boolean enableOfflineMode;
        private boolean enableAnalytics;
        private boolean enableBackgroundSync;
        private boolean enforceVersionCheck;
        
        public Features() {
        }
        
        // Getters and setters
        public boolean isUseNewSyncSystem() {
            return useNewSyncSystem;
        }
        
        public void setUseNewSyncSystem(boolean useNewSyncSystem) {
            this.useNewSyncSystem = useNewSyncSystem;
        }
        
        public boolean isEnableOfflineMode() {
            return enableOfflineMode;
        }
        
        public void setEnableOfflineMode(boolean enableOfflineMode) {
            this.enableOfflineMode = enableOfflineMode;
        }
        
        public boolean isEnableAnalytics() {
            return enableAnalytics;
        }
        
        public void setEnableAnalytics(boolean enableAnalytics) {
            this.enableAnalytics = enableAnalytics;
        }
        
        public boolean isEnableBackgroundSync() {
            return enableBackgroundSync;
        }
        
        public void setEnableBackgroundSync(boolean enableBackgroundSync) {
            this.enableBackgroundSync = enableBackgroundSync;
        }
        
        public boolean isEnforceVersionCheck() {
            return enforceVersionCheck;
        }
        
        public void setEnforceVersionCheck(boolean enforceVersionCheck) {
            this.enforceVersionCheck = enforceVersionCheck;
        }
    }
    
    /**
     * Nested class for user limits based on subscription tier
     */
    public static class Limits {
        private TierLimits freeTier;
        private TierLimits proTier;
        
        public Limits() {
        }
        
        // Nested class for tier-specific limits
        public static class TierLimits {
            private int mappingLimit;
            private int importLimit;
            private int exportLimit;
            
            public TierLimits() {
            }
            
            // Getters and setters
            public int getMappingLimit() {
                return mappingLimit;
            }
            
            public void setMappingLimit(int mappingLimit) {
                this.mappingLimit = mappingLimit;
            }
            
            public int getImportLimit() {
                return importLimit;
            }
            
            public void setImportLimit(int importLimit) {
                this.importLimit = importLimit;
            }
            
            public int getExportLimit() {
                return exportLimit;
            }
            
            public void setExportLimit(int exportLimit) {
                this.exportLimit = exportLimit;
            }
        }
        
        // Getters and setters
        public TierLimits getFreeTier() {
            return freeTier;
        }
        
        public void setFreeTier(TierLimits freeTier) {
            this.freeTier = freeTier;
        }
        
        public TierLimits getProTier() {
            return proTier;
        }
        
        public void setProTier(TierLimits proTier) {
            this.proTier = proTier;
        }
    }
    
    /**
     * Nested class for sync configuration
     */
    public static class Sync {
        private int interval;
        private int backgroundInterval;
        private int maxBatchSize;
        private String conflictStrategy;
        
        public Sync() {
        }
        
        // Getters and setters
        public int getInterval() {
            return interval;
        }
        
        public void setInterval(int interval) {
            this.interval = interval;
        }
        
        public int getBackgroundInterval() {
            return backgroundInterval;
        }
        
        public void setBackgroundInterval(int backgroundInterval) {
            this.backgroundInterval = backgroundInterval;
        }
        
        public int getMaxBatchSize() {
            return maxBatchSize;
        }
        
        public void setMaxBatchSize(int maxBatchSize) {
            this.maxBatchSize = maxBatchSize;
        }
        
        public String getConflictStrategy() {
            return conflictStrategy;
        }
        
        public void setConflictStrategy(String conflictStrategy) {
            this.conflictStrategy = conflictStrategy;
        }
    }
    
    /**
     * Nested class for maintenance information
     */
    public static class Maintenance {
        private boolean isInMaintenance;
        private String maintenanceMessage;
        private Timestamp estimatedEndTime;
        
        public Maintenance() {
        }
        
        // Getters and setters
        public boolean isInMaintenance() {
            return isInMaintenance;
        }
        
        public void setInMaintenance(boolean inMaintenance) {
            isInMaintenance = inMaintenance;
        }
        
        public String getMaintenanceMessage() {
            return maintenanceMessage;
        }
        
        public void setMaintenanceMessage(String maintenanceMessage) {
            this.maintenanceMessage = maintenanceMessage;
        }
        
        public Date getEstimatedEndTime() {
            return estimatedEndTime != null ? estimatedEndTime.toDate() : null;
        }
        
        public void setEstimatedEndTime(Date estimatedEndTime) {
            this.estimatedEndTime = estimatedEndTime != null ? new Timestamp(estimatedEndTime) : null;
        }
    }
    
    /**
     * Get custom data map for additional configuration values
     * @return Map of custom configuration data
     */
    public Map<String, Object> getCustomData() {
        if (customData == null) {
            customData = new HashMap<>();
        }
        return customData;
    }
    
    /**
     * Set custom data map
     * @param customData Map of custom configuration data
     */
    public void setCustomData(Map<String, Object> customData) {
        this.customData = customData;
    }
    
    // Getters and setters
    
    public Versions getVersions() {
        return versions;
    }
    
    public void setVersions(Versions versions) {
        this.versions = versions;
    }
    
    public Features getFeatures() {
        return features;
    }
    
    public void setFeatures(Features features) {
        this.features = features;
    }
    
    public Limits getLimits() {
        return limits;
    }
    
    public void setLimits(Limits limits) {
        this.limits = limits;
    }
    
    public Sync getSync() {
        return sync;
    }
    
    public void setSync(Sync sync) {
        this.sync = sync;
    }
    
    public Maintenance getMaintenance() {
        return maintenance;
    }
    
    public void setMaintenance(Maintenance maintenance) {
        this.maintenance = maintenance;
    }
    
    public Date getUpdatedAt() {
        return updatedAt != null ? updatedAt.toDate() : null;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt != null ? new Timestamp(updatedAt) : null;
    }
    
    public long getVersion() {
        return version;
    }
    
    public void setVersion(long version) {
        this.version = version;
    }
}
