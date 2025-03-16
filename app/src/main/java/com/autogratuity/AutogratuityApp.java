package com.autogratuity;

import android.app.Application;
import android.util.Log;

import com.autogratuity.data.repository.RepositoryProvider;
import com.google.firebase.FirebaseApp;

/**
 * Application class for Autogratuity.
 * Handles global initialization of Firebase, data repository, and other components.
 */
public class AutogratuityApp extends Application {
    
    private static final String TAG = "AutogratuityApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Initialize Repository
        try {
            RepositoryProvider.initialize(this);
            
            // Start prefetching critical data
            RepositoryProvider.getRepository().prefetchCriticalData()
                    .subscribe(
                            () -> Log.d(TAG, "Critical data prefetched successfully"),
                            error -> Log.e(TAG, "Error prefetching data", error)
                    );
        } catch (Exception e) {
            Log.e(TAG, "Error initializing repository", e);
        }
        
        // Any other global initializations can go here
    }
}
