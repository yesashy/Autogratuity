package com.autogratuity.data.repository;

import android.content.Context;

import com.autogratuity.data.util.NetworkMonitor;

/**
 * Singleton provider for the DataRepository.
 * This class follows the service locator pattern to provide a centralized way to access
 * the data repository throughout the application.
 */
public class RepositoryProvider {
    
    private static volatile DataRepository instance;
    private static NetworkMonitor networkMonitor;
    
    private RepositoryProvider() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Initialize the repository with application context.
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
        
        // Initialize repository if needed
        if (instance == null) {
            synchronized (RepositoryProvider.class) {
                if (instance == null) {
                    instance = new FirestoreRepository(applicationContext);
                }
            }
        }
    }
    
    /**
     * Get the repository instance.
     * initialize() must be called before this method.
     * 
     * @return DataRepository instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static DataRepository getRepository() {
        if (instance == null) {
            throw new IllegalStateException("RepositoryProvider not initialized. Call initialize() first.");
        }
        return instance;
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
     * Reset the repository (mainly for testing purposes).
     */
    public static void reset() {
        instance = null;
        networkMonitor = null;
    }
}
