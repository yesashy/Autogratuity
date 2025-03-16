package com.autogratuity.data.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Utility class for monitoring network connectivity changes.
 * Provides real-time updates on network status using RxJava.
 */
public class NetworkMonitor {
    private static final String TAG = "NetworkMonitor";
    
    private final ConnectivityManager connectivityManager;
    private final BehaviorSubject<Boolean> connectivitySubject = BehaviorSubject.createDefault(false);
    
    /**
     * Constructor that initializes and starts monitoring network connectivity.
     * 
     * @param context Application context
     */
    public NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // Initial connectivity check
        boolean isConnected = isNetworkAvailable();
        connectivitySubject.onNext(isConnected);
        
        // Setup network callback for changes
        setupNetworkCallback();
    }
    
    /**
     * Sets up a callback to monitor network state changes.
     * Uses ConnectivityManager.NetworkCallback which is the recommended approach for API 21+.
     */
    private void setupNetworkCallback() {
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager not available");
            return;
        }
        
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // Network is available
                connectivitySubject.onNext(true);
                Log.d(TAG, "Network is available");
            }
            
            @Override
            public void onLost(Network network) {
                // Network is lost
                // Check if any other networks are available before reporting offline
                boolean isAnyNetworkAvailable = isNetworkAvailable();
                connectivitySubject.onNext(isAnyNetworkAvailable);
                Log.d(TAG, "Network is " + (isAnyNetworkAvailable ? "still available" : "lost"));
            }
            
            @Override
            public void onUnavailable() {
                // Network is unavailable
                connectivitySubject.onNext(false);
                Log.d(TAG, "Network is unavailable");
            }
        };
        
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        } catch (Exception e) {
            Log.e(TAG, "Error registering network callback", e);
        }
    }
    
    /**
     * Checks if the device currently has an active network connection.
     * 
     * @return true if connected to a network, false otherwise
     */
    public boolean isNetworkAvailable() {
        if (connectivityManager == null) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(
                    connectivityManager.getActiveNetwork());
            
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            // For older devices
            Network[] networks = connectivityManager.getAllNetworks();
            for (Network network : networks) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null && (
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Provides an Observable that emits the current network status and subsequent changes.
     * 
     * @return Observable that emits true when connected, false when disconnected
     */
    public Observable<Boolean> observe() {
        return connectivitySubject;
    }
    
    /**
     * Returns the current network connectivity status.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connectivitySubject.getValue() != null && connectivitySubject.getValue();
    }
}
