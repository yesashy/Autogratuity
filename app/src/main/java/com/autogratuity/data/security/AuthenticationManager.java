package com.autogratuity.data.security;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Utility class for handling authentication tokens and session management.
 * Ensures secure access to data by providing fresh authentication tokens.
 */
public class AuthenticationManager {
    
    private static final String TAG = "AuthenticationManager";
    private static final long TOKEN_EXPIRY_MARGIN_MS = 5 * 60 * 1000; // 5 minutes margin
    
    private static AuthenticationManager instance;
    private final FirebaseAuth firebaseAuth;
    private final Map<String, TokenInfo> tokenCache = new HashMap<>();
    
    /**
     * Token information class
     */
    private static class TokenInfo {
        String token;
        long expiryTimeMs;
        
        TokenInfo(String token, long expiryTimeMs) {
            this.token = token;
            this.expiryTimeMs = expiryTimeMs;
        }
        
        boolean isValid() {
            return token != null && 
                   expiryTimeMs > System.currentTimeMillis() + TOKEN_EXPIRY_MARGIN_MS;
        }
    }
    
    /**
     * Get singleton instance of AuthenticationManager
     * 
     * @param context Android context
     * @return AuthenticationManager instance
     */
    public static synchronized AuthenticationManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthenticationManager(context);
        }
        return instance;
    }
    
    /**
     * Private constructor
     * 
     * @param context Android context
     */
    private AuthenticationManager(Context context) {
        firebaseAuth = FirebaseAuth.getInstance();
    }
    
    /**
     * Get the current user's ID
     * 
     * @return User ID or null if not authenticated
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    /**
     * Check if a user is currently authenticated
     * 
     * @return True if user is authenticated
     */
    public boolean isAuthenticated() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    /**
     * Get a fresh authentication token for API calls
     * 
     * @param forceRefresh Whether to force a refresh or use cached token
     * @return Single that emits the auth token
     */
    public Single<String> getAuthToken(boolean forceRefresh) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Single.error(new IllegalStateException("User is not authenticated"));
        }
        
        // Check cache if not forcing refresh
        if (!forceRefresh) {
            TokenInfo tokenInfo = tokenCache.get(userId);
            if (tokenInfo != null && tokenInfo.isValid()) {
                return Single.just(tokenInfo.token);
            }
        }
        
        // Fetch fresh token
        return Single.create(emitter -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                emitter.onError(new IllegalStateException("User is not authenticated"));
                return;
            }
            
            user.getIdToken(forceRefresh)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            GetTokenResult result = task.getResult();
                            String token = result.getToken();
                            
                            // Extract expiry time from token claims
                            long expiryTimeMs = System.currentTimeMillis() + 3600000; // Default 1 hour
                            if (result.getExpirationTimestamp() > 0) {
                                expiryTimeMs = result.getExpirationTimestamp() * 1000;
                            }
                            
                            // Cache the token
                            tokenCache.put(userId, new TokenInfo(token, expiryTimeMs));
                            
                            emitter.onSuccess(token);
                        } else {
                            Log.e(TAG, "Error getting auth token", task.getException());
                            emitter.onError(task.getException() != null ? 
                                    task.getException() : 
                                    new Exception("Unknown error getting auth token"));
                        }
                    });
        });
    }
    
    /**
     * Attach authentication token to headers for API calls
     * 
     * @param headers Headers map to attach token to
     * @return Single that emits the updated headers
     */
    public Single<Map<String, String>> attachAuthHeaders(Map<String, String> headers) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        
        final Map<String, String> finalHeaders = headers;
        
        return getAuthToken(false)
                .map(token -> {
                    finalHeaders.put("Authorization", "Bearer " + token);
                    return finalHeaders;
                });
    }
    
    /**
     * Refresh the current user's token
     * 
     * @return Completable that completes when refresh is done
     */
    public Completable refreshToken() {
        return getAuthToken(true).ignoreElement();
    }
    
    /**
     * Clear all cached tokens
     */
    public void clearTokenCache() {
        tokenCache.clear();
    }
    
    /**
     * Get token expiration time
     * 
     * @return Single that emits the expiration date
     */
    public Single<Date> getTokenExpirationTime() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Single.error(new IllegalStateException("User is not authenticated"));
        }
        
        TokenInfo tokenInfo = tokenCache.get(userId);
        if (tokenInfo != null && tokenInfo.isValid()) {
            return Single.just(new Date(tokenInfo.expiryTimeMs));
        }
        
        // No valid token in cache, fetch new one and return its expiry
        return getAuthToken(true)
                .map(token -> {
                    TokenInfo info = tokenCache.get(userId);
                    return new Date(info.expiryTimeMs);
                });
    }
}
