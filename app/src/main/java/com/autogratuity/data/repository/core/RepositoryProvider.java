package com.autogratuity.data.repository.core;

import android.content.Context;
import android.util.Log;

import com.autogratuity.data.repository.core.DataRepository;
import com.autogratuity.data.repository.core.TracingRepositoryDecorator;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.address.AddressRepositoryImpl;
import com.autogratuity.data.repository.config.ConfigRepository;
import com.autogratuity.data.repository.config.ConfigRepositoryImpl;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.data.repository.delivery.DeliveryRepositoryImpl;
import com.autogratuity.data.repository.preference.PreferenceRepository;
import com.autogratuity.data.repository.preference.PreferenceRepositoryImpl;
import com.autogratuity.data.repository.subscription.SubscriptionRepository;
import com.autogratuity.data.repository.subscription.SubscriptionRepositoryImpl;
import com.autogratuity.data.repository.sync.SyncRepository;
import com.autogratuity.data.repository.sync.SyncRepositoryImpl;
import com.autogratuity.data.util.NetworkMonitor;

/**
 * Singleton provider for all repositories in the application.
 * This class follows the service locator pattern to provide a centralized way to access
 * the data repositories throughout the application.
 * 
 * It maintains instances of:
 * - Core DataRepository
 * - Domain-specific repositories (ConfigRepository, etc.)
 * - Network monitoring utilities
 */
public class RepositoryProvider {
    
    private static final String TAG = "RepositoryProvider";
    
    private static volatile DataRepository coreRepository;
    private static volatile ConfigRepository configRepository;
    private static volatile PreferenceRepository preferenceRepository;
    private static volatile DeliveryRepository deliveryRepository;
    private static volatile SubscriptionRepository subscriptionRepository;
    private static volatile AddressRepository addressRepository;
    private static volatile SyncRepository syncRepository;
    private static NetworkMonitor networkMonitor;
    private static volatile boolean isInitialized = false;
    
    // Flag to enable repository method tracing
    private static volatile boolean tracingEnabled = false;
    
    // Flag to enable detailed logging for method parameters and results
    private static volatile boolean detailedLoggingEnabled = false;
    
    private RepositoryProvider() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Check if the RepositoryProvider has been initialized
     * 
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * Initialize all repositories with application context.
     * Must be called before getRepository() can be used, typically in Application.onCreate()
     * 
     * @param context Application context
     */
    public static void initialize(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        
        // Use application context to prevent leaks
        Context applicationContext = context.getApplicationContext();
        
        // Initialize network monitor
        networkMonitor = new NetworkMonitor(applicationContext);
        
        // Initialize repositories if needed
        if (coreRepository == null) {
            synchronized (RepositoryProvider.class) {
                if (coreRepository == null) {
                    Log.d(TAG, "Initializing domain repositories");
                    
                    // Core repository
                    coreRepository = new FirestoreRepository(applicationContext);
                    
                    // Config repository
                    ConfigRepository baseConfigRepo = new ConfigRepositoryImpl(applicationContext);
                    configRepository = tracingEnabled ? 
                            TracingRepositoryDecorator.create(baseConfigRepo, ConfigRepository.class, detailedLoggingEnabled) : 
                            baseConfigRepo;
                    
                    // Preference repository
                    PreferenceRepository basePrefRepo = new PreferenceRepositoryImpl(applicationContext);
                    preferenceRepository = tracingEnabled ? 
                            TracingRepositoryDecorator.create(basePrefRepo, PreferenceRepository.class, detailedLoggingEnabled) : 
                            basePrefRepo;
                    
                    // Delivery repository
                    DeliveryRepository baseDeliveryRepo = new DeliveryRepositoryImpl(applicationContext);
                    deliveryRepository = tracingEnabled ? 
                            TracingRepositoryDecorator.create(baseDeliveryRepo, DeliveryRepository.class, detailedLoggingEnabled) : 
                            baseDeliveryRepo;
                    
                    // Subscription repository
                    SubscriptionRepository baseSubRepo = new SubscriptionRepositoryImpl(applicationContext);
                    subscriptionRepository = tracingEnabled ? 
                            TracingRepositoryDecorator.create(baseSubRepo, SubscriptionRepository.class, detailedLoggingEnabled) : 
                            baseSubRepo;
                    
                    // Address repository
                    AddressRepository baseAddressRepo = new AddressRepositoryImpl(applicationContext);
                    addressRepository = tracingEnabled ? 
                            TracingRepositoryDecorator.create(baseAddressRepo, AddressRepository.class, detailedLoggingEnabled) : 
                            baseAddressRepo;
                    
                    // Sync repository
                    SyncRepository baseSyncRepo = new SyncRepositoryImpl(applicationContext);
                    syncRepository = tracingEnabled ? 
                            TracingRepositoryDecorator.create(baseSyncRepo, SyncRepository.class, detailedLoggingEnabled) : 
                            baseSyncRepo;
                    
                    isInitialized = true;
                    Log.d(TAG, "Domain repositories initialized successfully");
                }
            }
        }
    }
    
    /**
     * Get the core repository instance.
     * initialize() must be called before this method.
     * 
     * @return DataRepository instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static DataRepository getRepository() {
        if (coreRepository == null) {
            throw new IllegalStateException("RepositoryProvider not initialized. Call initialize() first.");
        }
        return coreRepository;
    }
    
    /**
     * Get the config repository instance.
     * initialize() must be called before this method.
     * 
     * @return ConfigRepository instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static ConfigRepository getConfigRepository() {
        if (configRepository == null) {
            throw new IllegalStateException("RepositoryProvider not initialized. Call initialize() first.");
        }
        return configRepository;
    }
    
    /**
     * Get the network monitor instance.
     * 
     * @return NetworkMonitor instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static NetworkMonitor getNetworkMonitor() {
        if (networkMonitor == null) {
            throw new IllegalStateException("RepositoryProvider not initialized. Call initialize() first.");
        }
        return networkMonitor;
    }
    
    /**
     * Get the preference repository instance.
     * initialize() must be called before this method.
     * 
     * @return PreferenceRepository instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static PreferenceRepository getPreferenceRepository() {
        if (preferenceRepository == null) {
            throw new IllegalStateException("RepositoryProvider not initialized. Call initialize() first.");
        }
        return preferenceRepository;
    }
    
    /**
     * Get the delivery repository instance.
     * initialize() must be called before this method.
     * 
     * @return DeliveryRepository instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static DeliveryRepository getDeliveryRepository() {
        if (deliveryRepository == null) {
            throw new IllegalStateException("RepositoryProvider not initialized. Call initialize() first.");
        }
        return deliveryRepository;
    }
    
    /**
     * Get the subscription repository instance.
     * initialize() must be called before this method.
     * 
     * @return SubscriptionRepository instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static SubscriptionRepository getSubscriptionRepository() {
        if (subscriptionRepository == null) {
            throw new IllegalStateException("RepositoryProvider not initialized. Call initialize() first.");
        }
        return subscriptionRepository;
    }
    
    /**
     * Get the address repository instance.
     * initialize() must be called before this method.
     * 
     * @return AddressRepository instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static AddressRepository getAddressRepository() {
        if (addressRepository == null) {
            throw new IllegalStateException("RepositoryProvider not initialized. Call initialize() first.");
        }
        return addressRepository;
    }
    
    /**
     * Get the sync repository instance.
     * initialize() must be called before this method.
     * 
     * @return SyncRepository instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static SyncRepository getSyncRepository() {
        if (syncRepository == null) {
            throw new IllegalStateException("RepositoryProvider not initialized. Call initialize() first.");
        }
        return syncRepository;
    }
    
    /**
     * Reset all repositories (mainly for testing purposes).
     */
    public static void reset() {
        coreRepository = null;
        configRepository = null;
        preferenceRepository = null;
        deliveryRepository = null;
        subscriptionRepository = null;
        addressRepository = null;
        syncRepository = null;
        networkMonitor = null;
        isInitialized = false;
    }
    
    /**
     * Enable or disable repository method tracing.
     * This setting affects newly created repository instances after the next initialize() call.
     * 
     * @param enabled true to enable tracing, false to disable
     * @param detailedLogging true to enable detailed parameter and result logging
     */
    public static void setTracingEnabled(boolean enabled, boolean detailedLogging) {
        tracingEnabled = enabled;
        detailedLoggingEnabled = detailedLogging;
        
        Log.d(TAG, "Repository method tracing " + (enabled ? "enabled" : "disabled") + 
                (enabled && detailedLogging ? " with detailed logging" : ""));
    }
    
    /**
     * Check if repository method tracing is enabled.
     * 
     * @return true if tracing is enabled, false otherwise
     */
    public static boolean isTracingEnabled() {
        return tracingEnabled;
    }
    
    /**
     * Check if detailed logging is enabled for repository method tracing.
     * 
     * @return true if detailed logging is enabled, false otherwise
     */
    public static boolean isDetailedLoggingEnabled() {
        return detailedLoggingEnabled;
    }
    
    /**
     * Get repository method tracing statistics.
     * 
     * @return Map of method names to their statistics
     */
    public static java.util.Map<String, TracingRepositoryDecorator.MethodStats> getTracingStatistics() {
        return TracingRepositoryDecorator.getStatistics();
    }
    
    /**
     * Reset repository method tracing statistics.
     */
    public static void resetTracingStatistics() {
        TracingRepositoryDecorator.resetStatistics();
        Log.d(TAG, "Repository method tracing statistics reset");
    }
    
    /**
     * Log a summary of repository method tracing statistics.
     */
    public static void logTracingStatistics() {
        if (!tracingEnabled) {
            Log.d(TAG, "Repository method tracing is not enabled");
            return;
        }
        
        java.util.Map<String, TracingRepositoryDecorator.MethodStats> stats = getTracingStatistics();
        if (stats.isEmpty()) {
            Log.d(TAG, "No repository method tracing statistics available");
            return;
        }
        
        Log.d(TAG, "==== Repository Method Tracing Statistics ====");
        for (java.util.Map.Entry<String, TracingRepositoryDecorator.MethodStats> entry : stats.entrySet()) {
            String methodName = entry.getKey();
            TracingRepositoryDecorator.MethodStats methodStats = entry.getValue();
            
            Log.d(TAG, String.format("%s: count=%d, avg=%dms, min=%dms, max=%dms, errors=%d",
                    methodName,
                    methodStats.getCount(),
                    methodStats.getAvgTimeMs(),
                    methodStats.getMinTimeMs(),
                    methodStats.getMaxTimeMs(),
                    methodStats.getErrorCount()));
        }
        Log.d(TAG, "===============================================");
    }
}
