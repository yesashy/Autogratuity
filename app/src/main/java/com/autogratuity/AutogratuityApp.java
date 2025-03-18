package com.autogratuity;

import android.app.Application;
import android.util.Log;

import com.autogratuity.data.repository.core.RepositoryProvider;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Application class for Autogratuity.
 * Handles global initialization of Firebase, data repository, and other components.
 */
public class AutogratuityApp extends Application {
    
    private static final String TAG = "AutogratuityApp";
    private CompositeDisposable disposables = new CompositeDisposable();
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
        
        // Initialize Repository with new domain-based implementation
        initializeRepositories();
        
        // Set up authentication state listener to handle login/logout
        setupAuthStateListener();
    }
    
    /**
     * Initialize all repositories
     */
    private void initializeRepositories() {
        try {
            if (!RepositoryProvider.isInitialized()) {
                RepositoryProvider.initialize(this);
                Log.d(TAG, "Domain repositories initialized successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing repositories", e);
        }
    }
    
    /**
     * Set up authentication state listener to handle login/logout
     */
    private void setupAuthStateListener() {
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null) {
                // User is signed in, prefetch critical data
                prefetchCriticalData();
            } else {
                // User is signed out, handle cleanup if needed
                Log.d(TAG, "User signed out, repository still available for login functionality");
            }
        });
    }
    
    /**
     * Prefetch critical data from repositories
     * This is called when user is authenticated
     */
    private void prefetchCriticalData() {
        try {
            if (RepositoryProvider.isInitialized()) {
                // Start prefetching critical data (using the core repository)
                disposables.add(
                    RepositoryProvider.getRepository().prefetchCriticalData()
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            () -> Log.d(TAG, "Critical data prefetched successfully"),
                            error -> Log.e(TAG, "Error prefetching data", error)
                        )
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during prefetch data", e);
        }
    }
    
    @Override
    public void onTerminate() {
        // Dispose of all subscriptions to avoid memory leaks
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
        
        super.onTerminate();
    }
}
