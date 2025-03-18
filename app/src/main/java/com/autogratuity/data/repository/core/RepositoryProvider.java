package com.autogratuity.data.repository.core;

import android.content.Context;
import android.util.Log;

import com.autogratuity.data.repository.core.DataRepository;
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
                    configRepository = new ConfigRepositoryImpl(applicationContext);
                    
                    // Preference repository
                    preferenceRepository = new PreferenceRepositoryImpl(applicationContext);
                    
                    // Delivery repository
                    deliveryRepository = new DeliveryRepositoryImpl(applicationContext);
                    
                    // Subscription repository
                    subscriptionRepository = new SubscriptionRepositoryImpl(applicationContext);
                    
                    // Address repository
                    addressRepository = new AddressRepositoryImpl(applicationContext);
                    
                    // Sync repository
                    syncRepository = new SyncRepositoryImpl(applicationContext);
                    
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
}
