package com.autogratuity.data.repository.utils;

import androidx.annotation.NonNull;

/**
 * Standard constants for repository implementations.
 * Provides consistent naming conventions, cache key formats, and other constants
 * to enforce standardization across repository implementations.
 */
public class RepositoryConstants {

    /**
     * Cache key prefixes for different entity types.
     * These prefixes should be used when generating cache keys to ensure consistency.
     */
    public static class CachePrefix {
        public static final String USER_PROFILE = "user_profile";
        public static final String APP_CONFIG = "app_config";
        public static final String ADDRESS = "address";
        public static final String ADDRESSES = "addresses";
        public static final String DELIVERY = "delivery";
        public static final String DELIVERIES = "deliveries";
        public static final String SUBSCRIPTION = "subscription";
        public static final String SYNC_STATUS = "sync_status";
        public static final String SYNC_OPERATIONS = "sync_operations";
    }
    
    /**
     * Entity type identifiers for error reporting and logging.
     * These identifiers should be used when calling error handling methods.
     */
    public static class EntityType {
        public static final String USER_PROFILE = "user_profile";
        public static final String APP_CONFIG = "app_config";
        public static final String ADDRESS = "address";
        public static final String DELIVERY = "delivery";
        public static final String SUBSCRIPTION = "subscription";
        public static final String SYNC_OPERATION = "sync_operation";
        public static final String DEVICE = "device";
    }
    
    /**
     * Operation name templates for error reporting and logging.
     * These should be used when calling error handling methods.
     */
    public static class OperationName {
        public static final String GET = "get %s";
        public static final String GET_ALL = "get all %ss";
        public static final String GET_BY_ID = "get %s by id";
        public static final String FIND_BY = "find %s by %s";
        public static final String ADD = "add %s";
        public static final String UPDATE = "update %s";
        public static final String DELETE = "delete %s";
        public static final String OBSERVE = "observe %s";
        public static final String OBSERVE_ALL = "observe all %ss";
        public static final String SYNC = "sync %s";
    }
    
    /**
     * Method name conventions for repository implementations.
     * These name patterns should be followed when implementing repository interfaces.
     */
    public static class MethodName {
        // Read operations - Single
        public static final String GET_FORMAT = "get%s";              // Example: getAppConfig(), getUserProfile()
        public static final String GET_BY_ID_FORMAT = "get%sById";    // Example: getAddressById(), getDeliveryById()
        public static final String GET_ALL_FORMAT = "get%ss";         // Example: getAddresses(), getDeliveries()
        public static final String FIND_BY_FORMAT = "find%sBy%s";     // Example: findAddressByNormalizedAddress()
        
        // Read operations - Observable
        public static final String OBSERVE_FORMAT = "observe%s";      // Example: observeAppConfig(), observeUserProfile()
        public static final String OBSERVE_BY_ID_FORMAT = "observe%sById"; // Example: observeAddressById()
        public static final String OBSERVE_ALL_FORMAT = "observe%ss"; // Example: observeAddresses(), observeDeliveries()
        
        // Write operations - Single<DocumentReference>
        public static final String ADD_FORMAT = "add%s";              // Example: addAddress(), addDelivery()
        
        // Write operations - Completable
        public static final String UPDATE_FORMAT = "update%s";        // Example: updateAddress(), updateDelivery()
        public static final String DELETE_FORMAT = "delete%s";        // Example: deleteAddress(), deleteDelivery()
        public static final String SET_FORMAT = "set%s";              // Example: setDefaultAddress(), setAddressFlags()
    }
    
    /**
     * Generates a standardized cache key for a single entity.
     *
     * @param prefix The entity type prefix
     * @param id The entity ID
     * @param userId The user ID (optional, can be null)
     * @return A standardized cache key
     */
    @NonNull
    public static String singleEntityCacheKey(@NonNull String prefix, @NonNull String id, String userId) {
        return userId != null 
            ? String.format("%s_%s_%s", prefix, userId, id)
            : String.format("%s_%s", prefix, id);
    }
    
    /**
     * Generates a standardized cache key for a collection of entities.
     *
     * @param prefix The entity type prefix
     * @param userId The user ID (optional, can be null)
     * @return A standardized cache key
     */
    @NonNull
    public static String collectionCacheKey(@NonNull String prefix, String userId) {
        return userId != null 
            ? String.format("%s_collection_%s", prefix, userId)
            : String.format("%s_collection", prefix);
    }
    
    /**
     * Generates a standardized operation name for error reporting.
     *
     * @param template The operation name template
     * @param entityType The entity type
     * @param criteria The search criteria (optional, can be null)
     * @return A standardized operation name
     */
    @NonNull
    public static String operationName(@NonNull String template, @NonNull String entityType, String criteria) {
        return criteria != null 
            ? String.format(template, entityType, criteria)
            : String.format(template, entityType);
    }
}
