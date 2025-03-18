package com.autogratuity.data.serialization;

import android.content.Context;

import com.autogratuity.data.local.JsonSerializer;
import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.security.EncryptionUtils;
import com.autogratuity.data.security.ValidationUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for serializing/deserializing UserProfile objects to/from Firestore maps.
 * This class includes encryption for sensitive user data and validation for inputs.
 */
public class UserProfileSerializer {
    
    private static EncryptionUtils encryptionUtils;
    
    /**
     * Initialize the serializer with encryption support
     * 
     * @param context Android context for encryption
     */
    public static void init(Context context) {
        encryptionUtils = EncryptionUtils.getInstance(context);
    }
    
    /**
     * Convert a UserProfile object to a Map for Firestore storage
     * 
     * @param profile The user profile to convert
     * @param context Optional context for encryption (can be null if initialized)
     * @return Map representation of the user profile
     */
    public static Map<String, Object> toMap(UserProfile profile, Context context) {
        if (profile == null) {
            return null;
        }
        
        // Ensure encryption is available
        if (encryptionUtils == null && context != null) {
            encryptionUtils = EncryptionUtils.getInstance(context);
        }
        
        Map<String, Object> map = new HashMap<>();
        
        // Add base fields
        map.put("userId", profile.getUserId());
        map.put("displayName", profile.getDisplayName());
        map.put("defaultAddressId", profile.getDefaultAddressId());
        map.put("accountStatus", profile.getAccountStatus());
        map.put("timezone", profile.getTimezone());
        map.put("version", profile.getVersion());
        
        // Timestamp fields
        if (profile.getCreatedAt() != null) {
            map.put("createdAt", new Timestamp(profile.getCreatedAt()));
        }
        
        if (profile.getLastLoginAt() != null) {
            map.put("lastLoginAt", new Timestamp(profile.getLastLoginAt()));
        }
        
        if (profile.getPrivacyPolicyAccepted() != null) {
            map.put("privacyPolicyAccepted", new Timestamp(profile.getPrivacyPolicyAccepted()));
        }
        
        if (profile.getTermsAccepted() != null) {
            map.put("termsAccepted", new Timestamp(profile.getTermsAccepted()));
        }
        
        // Encrypt sensitive fields
        if (encryptionUtils != null) {
            // Email is PII and should be encrypted
            map.put("email", encryptionUtils.encrypt(profile.getEmail()));
        } else {
            map.put("email", profile.getEmail());
        }
        
        // Add nested objects
        if (profile.getSubscription() != null) {
            map.put("subscription", subscriptionToMap(profile.getSubscription()));
        }
        
        if (profile.getPreferences() != null) {
            map.put("preferences", preferencesToMap(profile.getPreferences()));
        }
        
        if (profile.getPermissions() != null) {
            map.put("permissions", permissionsToMap(profile.getPermissions()));
        }
        
        if (profile.getUsage() != null) {
            map.put("usage", usageToMap(profile.getUsage()));
        }
        
        if (profile.getSyncInfo() != null) {
            map.put("syncInfo", syncInfoToMap(profile.getSyncInfo()));
        }
        
        if (profile.getAppSettings() != null) {
            map.put("appSettings", appSettingsToMap(profile.getAppSettings()));
        }
        
        if (profile.getCommunication() != null) {
            map.put("communication", communicationToMap(profile.getCommunication()));
        }
        
        return map;
    }
    
    /**
     * Convert a DocumentSnapshot to a UserProfile object
     * 
     * @param snapshot Firestore document snapshot
     * @param context Optional context for decryption (can be null if initialized)
     * @return UserProfile object
     */
    public static UserProfile fromDocumentSnapshot(DocumentSnapshot snapshot, Context context) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        
        // Ensure encryption is available
        if (encryptionUtils == null && context != null) {
            encryptionUtils = EncryptionUtils.getInstance(context);
        }
        
        // First get the base UserProfile from Firestore
        UserProfile profile = snapshot.toObject(UserProfile.class);
        
        if (profile != null) {
            // Ensure userId is set
            if (profile.getUserId() == null) {
                profile.setUserId(snapshot.getId());
            }
            
            // Decrypt sensitive fields if encrypted
            if (encryptionUtils != null) {
                String email = profile.getEmail();
                if (email != null && encryptionUtils.isEncrypted(email)) {
                    profile.setEmail(encryptionUtils.decrypt(email));
                }
                
                // If there are other sensitive fields that need decryption, they would be handled here
            }
        }
        
        return profile;
    }
    
    /**
     * Convert a Map to a UserProfile object
     * 
     * @param map Map representation of a user profile
     * @param context Optional context for decryption (can be null if initialized)
     * @return UserProfile object
     */
    public static UserProfile fromMap(Map<String, Object> map, Context context) {
        if (map == null) {
            return null;
        }
        
        // Ensure encryption is available
        if (encryptionUtils == null && context != null) {
            encryptionUtils = EncryptionUtils.getInstance(context);
        }
        
        // Manual deserialization for better control and handling of nested objects
        UserProfile profile = new UserProfile();
        
        // Set basic fields
        profile.setUserId((String) map.get("userId"));
        profile.setDisplayName((String) map.get("displayName"));
        profile.setDefaultAddressId((String) map.get("defaultAddressId"));
        profile.setAccountStatus((String) map.get("accountStatus"));
        profile.setTimezone((String) map.get("timezone"));
        profile.setVersion(map.get("version") instanceof Number ? ((Number) map.get("version")).longValue() : 0);
        
        // Handle email with decryption if needed
        String email = (String) map.get("email");
        if (email != null && encryptionUtils != null && encryptionUtils.isEncrypted(email)) {
            profile.setEmail(encryptionUtils.decrypt(email));
        } else {
            profile.setEmail(email);
        }
        
        // Handle timestamp fields
        Object createdAt = map.get("createdAt");
        if (createdAt instanceof Timestamp) {
            profile.setCreatedAt((Timestamp) createdAt);
        } else if (createdAt instanceof Map) {
            // Handle serialized timestamp format
            try {
                Map<String, Object> timestampMap = (Map<String, Object>) createdAt;
                long seconds = ((Number) timestampMap.get("seconds")).longValue();
                int nanoseconds = ((Number) timestampMap.get("nanoseconds")).intValue();
                profile.setCreatedAt(new Timestamp(seconds, nanoseconds));
            } catch (Exception e) {
                Log.e("UserProfileSerializer", "Error parsing createdAt timestamp", e);
            }
        }
        
        // Handle other timestamp fields similarly
        Object lastLoginAt = map.get("lastLoginAt");
        if (lastLoginAt instanceof Timestamp) {
            profile.setLastLoginAt(((Timestamp) lastLoginAt).toDate());
        } else if (lastLoginAt instanceof Map) {
            try {
                Map<String, Object> timestampMap = (Map<String, Object>) lastLoginAt;
                long seconds = ((Number) timestampMap.get("seconds")).longValue();
                int nanoseconds = ((Number) timestampMap.get("nanoseconds")).intValue();
                profile.setLastLoginAt(new Timestamp(seconds, nanoseconds).toDate());
            } catch (Exception e) {
                Log.e("UserProfileSerializer", "Error parsing lastLoginAt timestamp", e);
            }
        }
        
        Object privacyPolicyAccepted = map.get("privacyPolicyAccepted");
        if (privacyPolicyAccepted instanceof Timestamp) {
            profile.setPrivacyPolicyAccepted(((Timestamp) privacyPolicyAccepted).toDate());
        } else if (privacyPolicyAccepted instanceof Map) {
            try {
                Map<String, Object> timestampMap = (Map<String, Object>) privacyPolicyAccepted;
                long seconds = ((Number) timestampMap.get("seconds")).longValue();
                int nanoseconds = ((Number) timestampMap.get("nanoseconds")).intValue();
                profile.setPrivacyPolicyAccepted(new Timestamp(seconds, nanoseconds).toDate());
            } catch (Exception e) {
                Log.e("UserProfileSerializer", "Error parsing privacyPolicyAccepted timestamp", e);
            }
        }
        
        Object termsAccepted = map.get("termsAccepted");
        if (termsAccepted instanceof Timestamp) {
            profile.setTermsAccepted(((Timestamp) termsAccepted).toDate());
        } else if (termsAccepted instanceof Map) {
            try {
                Map<String, Object> timestampMap = (Map<String, Object>) termsAccepted;
                long seconds = ((Number) timestampMap.get("seconds")).longValue();
                int nanoseconds = ((Number) timestampMap.get("nanoseconds")).intValue();
                profile.setTermsAccepted(new Timestamp(seconds, nanoseconds).toDate());
            } catch (Exception e) {
                Log.e("UserProfileSerializer", "Error parsing termsAccepted timestamp", e);
            }
        }
        
        // Handle nested objects
        Map<String, Object> subscriptionMap = (Map<String, Object>) map.get("subscription");
        if (subscriptionMap != null) {
            profile.setSubscription(subscriptionFromMap(subscriptionMap));
        }
        
        Map<String, Object> preferencesMap = (Map<String, Object>) map.get("preferences");
        if (preferencesMap != null) {
            profile.setPreferences(preferencesFromMap(preferencesMap));
        }
        
        Map<String, Object> permissionsMap = (Map<String, Object>) map.get("permissions");
        if (permissionsMap != null) {
            profile.setPermissions(permissionsFromMap(permissionsMap));
        }
        
        Map<String, Object> usageMap = (Map<String, Object>) map.get("usage");
        if (usageMap != null) {
            profile.setUsage(usageFromMap(usageMap));
        }
        
        Map<String, Object> syncInfoMap = (Map<String, Object>) map.get("syncInfo");
        if (syncInfoMap != null) {
            profile.setSyncInfo(syncInfoFromMap(syncInfoMap));
        }
        
        Map<String, Object> appSettingsMap = (Map<String, Object>) map.get("appSettings");
        if (appSettingsMap != null) {
            profile.setAppSettings(appSettingsFromMap(appSettingsMap));
        }
        
        Map<String, Object> communicationMap = (Map<String, Object>) map.get("communication");
        if (communicationMap != null) {
            profile.setCommunication(communicationFromMap(communicationMap));
        }
        
        return profile;
    }
    
    /**
     * Create a default user profile when none exists
     * 
     * @param userId User ID for the new profile
     * @return The default user profile
     */
    public static UserProfile createDefaultUserProfile(String userId) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setCreatedAt(new Date());
        profile.setAccountStatus("active");
        
        // Initialize nested objects
        UserProfile.Preferences preferences = new UserProfile.Preferences();
        preferences.setDefaultTipPercentage(15);
        preferences.setTheme("system");
        preferences.setNotificationsEnabled(true);
        profile.setPreferences(preferences);
        
        UserProfile.AppSettings appSettings = new UserProfile.AppSettings();
        appSettings.setOnboardingCompleted(false);
        appSettings.setDataCollectionOptIn(true);
        appSettings.setLastVersion("1.0.0");
        profile.setAppSettings(appSettings);
        
        UserProfile.Communication communication = new UserProfile.Communication();
        communication.setPushNotificationsEnabled(true);
        profile.setCommunication(communication);
        
        return profile;
    }
    
    /**
     * Validate a UserProfile object
     * 
     * @param profile The profile to validate
     * @return True if the profile is valid
     */
    public static boolean validateUserProfile(UserProfile profile) {
        if (profile == null) {
            return false;
        }
        
        // Validate userId (required field)
        if (profile.getUserId() == null || profile.getUserId().isEmpty()) {
            Log.e("UserProfileSerializer", "Invalid profile: userId is required");
            return false;
        }
        
        // Validate email if present
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
            if (!ValidationUtils.isValidEmail(profile.getEmail())) {
                Log.e("UserProfileSerializer", "Invalid profile: email format is invalid");
                return false;
            }
        }
        
        // Validate accountStatus if present
        if (profile.getAccountStatus() != null) {
            String status = profile.getAccountStatus().toLowerCase();
            if (!status.equals("active") && !status.equals("inactive") && 
                !status.equals("suspended") && !status.equals("deleted")) {
                Log.e("UserProfileSerializer", "Invalid profile: accountStatus is invalid");
                return false;
            }
        }
        
        // Validate version (must be non-negative)
        if (profile.getVersion() < 0) {
            Log.e("UserProfileSerializer", "Invalid profile: version must be non-negative");
            return false;
        }
        
        // Validate preferences if present
        if (profile.getPreferences() != null) {
            UserProfile.Preferences prefs = profile.getPreferences();
            
            // Validate default tip percentage (must be between 0 and 100)
            if (prefs.getDefaultTipPercentage() < 0 || prefs.getDefaultTipPercentage() > 100) {
                Log.e("UserProfileSerializer", "Invalid profile: defaultTipPercentage must be between 0 and 100");
                return false;
            }
            
            // Validate theme if present
            if (prefs.getTheme() != null) {
                String theme = prefs.getTheme().toLowerCase();
                if (!theme.equals("light") && !theme.equals("dark") && !theme.equals("system")) {
                    Log.e("UserProfileSerializer", "Invalid profile: theme must be 'light', 'dark', or 'system'");
                    return false;
                }
            }
        }
        
        // Validate subscription if present
        if (profile.getSubscription() != null) {
            UserProfile.Subscription sub = profile.getSubscription();
            
            // Validate status if present
            if (sub.getStatus() != null) {
                String status = sub.getStatus().toLowerCase();
                if (!status.equals("free") && !status.equals("pro") && 
                    !status.equals("trial") && !status.equals("expired") && 
                    !status.equals("lifetime")) {
                    Log.e("UserProfileSerializer", "Invalid profile: subscription status is invalid");
                    return false;
                }
            }
            
            // Validate dates if present
            if (sub.getExpiryDate() != null && sub.getStartDate() != null) {
                if (sub.getExpiryDate().before(sub.getStartDate())) {
                    Log.e("UserProfileSerializer", "Invalid profile: subscription expiryDate cannot be before startDate");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    //-----------------------------------------------------------------------------------
    // Helper methods for converting nested objects to maps
    //-----------------------------------------------------------------------------------
    
    private static Map<String, Object> subscriptionToMap(UserProfile.Subscription subscription) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", subscription.getStatus());
        map.put("level", subscription.getLevel());
        map.put("isLifetime", subscription.isLifetime());
        
        // Time-related fields
        if (subscription.getStartDate() != null) {
            map.put("startDate", new Timestamp(subscription.getStartDate()));
        }
        
        if (subscription.getExpiryDate() != null) {
            map.put("expiryDate", new Timestamp(subscription.getExpiryDate()));
        }
        
        if (subscription.getLastVerified() != null) {
            map.put("lastVerified", new Timestamp(subscription.getLastVerified()));
        }
        
        // Encrypt sensitive fields
        if (encryptionUtils != null) {
            // Only encrypt if values exist
            if (subscription.getProvider() != null) {
                map.put("provider", encryptionUtils.encrypt(subscription.getProvider()));
            }
            if (subscription.getOrderId() != null) {
                map.put("orderId", encryptionUtils.encrypt(subscription.getOrderId()));
            }
        } else {
            map.put("provider", subscription.getProvider());
            map.put("orderId", subscription.getOrderId());
        }
        
        return map;
    }
    
    /**
     * Deserialize a subscription from a map
     * 
     * @param map The map containing subscription data
     * @return The deserialized Subscription object
     */
    private static UserProfile.Subscription subscriptionFromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        UserProfile.Subscription subscription = new UserProfile.Subscription();
        
        subscription.setStatus((String) map.get("status"));
        subscription.setLevel((String) map.get("level"));
        
        Boolean isLifetime = (Boolean) map.get("isLifetime");
        if (isLifetime != null) {
            subscription.setLifetime(isLifetime);
        }
        
        // Handle timestamp fields
        Object startDate = map.get("startDate");
        if (startDate instanceof Timestamp) {
            subscription.setStartDate(((Timestamp) startDate).toDate());
        } else if (startDate instanceof Map) {
            try {
                Map<String, Object> timestampMap = (Map<String, Object>) startDate;
                long seconds = ((Number) timestampMap.get("seconds")).longValue();
                int nanoseconds = ((Number) timestampMap.get("nanoseconds")).intValue();
                subscription.setStartDate(new Timestamp(seconds, nanoseconds).toDate());
            } catch (Exception e) {
                Log.e("UserProfileSerializer", "Error parsing startDate timestamp", e);
            }
        }
        
        Object expiryDate = map.get("expiryDate");
        if (expiryDate instanceof Timestamp) {
            subscription.setExpiryDate(((Timestamp) expiryDate).toDate());
        } else if (expiryDate instanceof Map) {
            try {
                Map<String, Object> timestampMap = (Map<String, Object>) expiryDate;
                long seconds = ((Number) timestampMap.get("seconds")).longValue();
                int nanoseconds = ((Number) timestampMap.get("nanoseconds")).intValue();
                subscription.setExpiryDate(new Timestamp(seconds, nanoseconds).toDate());
            } catch (Exception e) {
                Log.e("UserProfileSerializer", "Error parsing expiryDate timestamp", e);
            }
        }
        
        Object lastVerified = map.get("lastVerified");
        if (lastVerified instanceof Timestamp) {
            subscription.setLastVerified(((Timestamp) lastVerified).toDate());
        } else if (lastVerified instanceof Map) {
            try {
                Map<String, Object> timestampMap = (Map<String, Object>) lastVerified;
                long seconds = ((Number) timestampMap.get("seconds")).longValue();
                int nanoseconds = ((Number) timestampMap.get("nanoseconds")).intValue();
                subscription.setLastVerified(new Timestamp(seconds, nanoseconds).toDate());
            } catch (Exception e) {
                Log.e("UserProfileSerializer", "Error parsing lastVerified timestamp", e);
            }
        }
        
        // Handle encrypted fields
        String provider = (String) map.get("provider");
        if (provider != null && encryptionUtils != null && encryptionUtils.isEncrypted(provider)) {
            subscription.setProvider(encryptionUtils.decrypt(provider));
        } else {
            subscription.setProvider(provider);
        }
        
        String orderId = (String) map.get("orderId");
        if (orderId != null && encryptionUtils != null && encryptionUtils.isEncrypted(orderId)) {
            subscription.setOrderId(encryptionUtils.decrypt(orderId));
        } else {
            subscription.setOrderId(orderId);
        }
        
        return subscription;
    }
    
    private static Map<String, Object> preferencesToMap(UserProfile.Preferences preferences) {
        Map<String, Object> map = new HashMap<>();
        map.put("notificationsEnabled", preferences.isNotificationsEnabled());
        map.put("theme", preferences.getTheme());
        map.put("useLocation", preferences.isUseLocation());
        map.put("defaultTipPercentage", preferences.getDefaultTipPercentage());
        return map;
    }
    
    /**
     * Deserialize preferences from a map
     * 
     * @param map The map containing preferences data
     * @return The deserialized Preferences object
     */
    private static UserProfile.Preferences preferencesFromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        UserProfile.Preferences preferences = new UserProfile.Preferences();
        
        Boolean notificationsEnabled = (Boolean) map.get("notificationsEnabled");
        if (notificationsEnabled != null) {
            preferences.setNotificationsEnabled(notificationsEnabled);
        }
        
        preferences.setTheme((String) map.get("theme"));
        
        Boolean useLocation = (Boolean) map.get("useLocation");
        if (useLocation != null) {
            preferences.setUseLocation(useLocation);
        }
        
        Number defaultTipPercentage = (Number) map.get("defaultTipPercentage");
        if (defaultTipPercentage != null) {
            preferences.setDefaultTipPercentage(defaultTipPercentage.intValue());
        }
        
        return preferences;
    }
    
    private static Map<String, Object> permissionsToMap(UserProfile.Permissions permissions) {
        Map<String, Object> map = new HashMap<>();
        map.put("level", permissions.getLevel());
        map.put("bypassLimits", permissions.isBypassLimits());
        map.put("maxUploads", permissions.getMaxUploads());
        return map;
    }
    
    /**
     * Deserialize permissions from a map
     * 
     * @param map The map containing permissions data
     * @return The deserialized Permissions object
     */
    private static UserProfile.Permissions permissionsFromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        UserProfile.Permissions permissions = new UserProfile.Permissions();
        
        permissions.setLevel((String) map.get("level"));
        
        Boolean bypassLimits = (Boolean) map.get("bypassLimits");
        if (bypassLimits != null) {
            permissions.setBypassLimits(bypassLimits);
        }
        
        Number maxUploads = (Number) map.get("maxUploads");
        if (maxUploads != null) {
            permissions.setMaxUploads(maxUploads.intValue());
        }
        
        return permissions;
    }
    
    private static Map<String, Object> usageToMap(UserProfile.Usage usage) {
        Map<String, Object> map = new HashMap<>();
        map.put("mappingCount", usage.getMappingCount());
        map.put("deliveryCount", usage.getDeliveryCount());
        map.put("addressCount", usage.getAddressCount());
        
        if (usage.getLastUsageUpdate() != null) {
            map.put("lastUsageUpdate", new Timestamp(usage.getLastUsageUpdate()));
        }
        
        return map;
    }
    
    /**
     * Deserialize usage from a map
     * 
     * @param map The map containing usage data
     * @return The deserialized Usage object
     */
    private static UserProfile.Usage usageFromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        UserProfile.Usage usage = new UserProfile.Usage();
        
        Number mappingCount = (Number) map.get("mappingCount");
        if (mappingCount != null) {
            usage.setMappingCount(mappingCount.intValue());
        }
        
        Number deliveryCount = (Number) map.get("deliveryCount");
        if (deliveryCount != null) {
            usage.setDeliveryCount(deliveryCount.intValue());
        }
        
        Number addressCount = (Number) map.get("addressCount");
        if (addressCount != null) {
            usage.setAddressCount(addressCount.intValue());
        }
        
        Object lastUsageUpdate = map.get("lastUsageUpdate");
        if (lastUsageUpdate instanceof Timestamp) {
            usage.setLastUsageUpdate(((Timestamp) lastUsageUpdate).toDate());
        } else if (lastUsageUpdate instanceof Map) {
            try {
                Map<String, Object> timestampMap = (Map<String, Object>) lastUsageUpdate;
                long seconds = ((Number) timestampMap.get("seconds")).longValue();
                int nanoseconds = ((Number) timestampMap.get("nanoseconds")).intValue();
                usage.setLastUsageUpdate(new Timestamp(seconds, nanoseconds).toDate());
            } catch (Exception e) {
                Log.e("UserProfileSerializer", "Error parsing lastUsageUpdate timestamp", e);
            }
        }
        
        return usage;
    }
    
    private static Map<String, Object> syncInfoToMap(UserProfile.SyncInfo syncInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceIds", syncInfo.getDeviceIds());
        map.put("version", syncInfo.getVersion());
        
        if (syncInfo.getLastSyncTime() != null) {
            map.put("lastSyncTime", new Timestamp(syncInfo.getLastSyncTime()));
        }
        
        return map;
    }
    
    /**
     * Deserialize sync info from a map
     * 
     * @param map The map containing sync info data
     * @return The deserialized SyncInfo object
     */
    private static UserProfile.SyncInfo syncInfoFromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        UserProfile.SyncInfo syncInfo = new UserProfile.SyncInfo();
        
        @SuppressWarnings("unchecked")
        List<String> deviceIds = (List<String>) map.get("deviceIds");
        syncInfo.setDeviceIds(deviceIds);
        
        Number version = (Number) map.get("version");
        if (version != null) {
            syncInfo.setVersion(version.longValue());
        }
        
        Object lastSyncTime = map.get("lastSyncTime");
        if (lastSyncTime instanceof Timestamp) {
            syncInfo.setLastSyncTime(((Timestamp) lastSyncTime).toDate());
        } else if (lastSyncTime instanceof Map) {
            try {
                Map<String, Object> timestampMap = (Map<String, Object>) lastSyncTime;
                long seconds = ((Number) timestampMap.get("seconds")).longValue();
                int nanoseconds = ((Number) timestampMap.get("nanoseconds")).intValue();
                syncInfo.setLastSyncTime(new Timestamp(seconds, nanoseconds).toDate());
            } catch (Exception e) {
                Log.e("UserProfileSerializer", "Error parsing lastSyncTime timestamp", e);
            }
        }
        
        return syncInfo;
    }
    
    private static Map<String, Object> appSettingsToMap(UserProfile.AppSettings appSettings) {
        Map<String, Object> map = new HashMap<>();
        map.put("dataCollectionOptIn", appSettings.isDataCollectionOptIn());
        map.put("lastVersion", appSettings.getLastVersion());
        map.put("onboardingCompleted", appSettings.isOnboardingCompleted());
        return map;
    }
    
    /**
     * Deserialize app settings from a map
     * 
     * @param map The map containing app settings data
     * @return The deserialized AppSettings object
     */
    private static UserProfile.AppSettings appSettingsFromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        UserProfile.AppSettings appSettings = new UserProfile.AppSettings();
        
        Boolean dataCollectionOptIn = (Boolean) map.get("dataCollectionOptIn");
        if (dataCollectionOptIn != null) {
            appSettings.setDataCollectionOptIn(dataCollectionOptIn);
        }
        
        appSettings.setLastVersion((String) map.get("lastVersion"));
        
        Boolean onboardingCompleted = (Boolean) map.get("onboardingCompleted");
        if (onboardingCompleted != null) {
            appSettings.setOnboardingCompleted(onboardingCompleted);
        }
        
        return appSettings;
    }
    
    private static Map<String, Object> communicationToMap(UserProfile.Communication communication) {
        Map<String, Object> map = new HashMap<>();
        map.put("emailOptIn", communication.isEmailOptIn());
        map.put("marketingOptIn", communication.isMarketingOptIn());
        map.put("pushNotificationsEnabled", communication.isPushNotificationsEnabled());
        return map;
    }
    
    /**
     * Deserialize communication settings from a map
     * 
     * @param map The map containing communication settings data
     * @return The deserialized Communication object
     */
    private static UserProfile.Communication communicationFromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        UserProfile.Communication communication = new UserProfile.Communication();
        
        Boolean emailOptIn = (Boolean) map.get("emailOptIn");
        if (emailOptIn != null) {
            communication.setEmailOptIn(emailOptIn);
        }
        
        Boolean marketingOptIn = (Boolean) map.get("marketingOptIn");
        if (marketingOptIn != null) {
            communication.setMarketingOptIn(marketingOptIn);
        }
        
        Boolean pushNotificationsEnabled = (Boolean) map.get("pushNotificationsEnabled");
        if (pushNotificationsEnabled != null) {
            communication.setPushNotificationsEnabled(pushNotificationsEnabled);
        }
        
        return communication;
    }
    
    /**
     * Helper method to convert Date to Timestamp
     */
    private static Timestamp dateToTimestamp(Date date) {
        return date != null ? new Timestamp(date) : null;
    }
    
    /**
     * Serialize a user profile to JSON for shared preferences storage.
     * This includes encryption of sensitive fields.
     * 
     * @param profile The profile to serialize
     * @param context Context for encryption
     * @return JSON string with encrypted sensitive data
     */
    public static String toJson(UserProfile profile, Context context) {
        if (profile == null) {
            return null;
        }
        
        // Convert to map with encryption
        Map<String, Object> map = toMap(profile, context);
        
        // Convert map to JSON
        return JsonSerializer.toJson(map);
    }
    
    /**
     * Deserialize a user profile from JSON for shared preferences retrieval.
     * This includes decryption of sensitive fields.
     * 
     * @param json JSON string with encrypted sensitive data
     * @param context Context for decryption
     * @return Deserialized UserProfile object
     */
    public static UserProfile fromJson(String json, Context context) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        try {
            // Convert JSON to map
            @SuppressWarnings("unchecked")
            Map<String, Object> map = JsonSerializer.fromJson(json, Map.class);
            
            // Convert map to profile with decryption
            return fromMap(map, context);
        } catch (Exception e) {
            Log.e("UserProfileSerializer", "Error deserializing profile from JSON", e);
            return null;
        }
    }
}
