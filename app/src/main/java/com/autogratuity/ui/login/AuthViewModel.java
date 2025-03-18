package com.autogratuity.ui.login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.repository.config.ConfigRepository;
import com.autogratuity.data.repository.preference.PreferenceRepository;
import com.autogratuity.data.security.AuthenticationManager;
import com.autogratuity.ui.common.BaseViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for authentication-related activities, implementing the repository pattern.
 * Handles login, registration, and authentication state management.
 */
public class AuthViewModel extends BaseViewModel {
    private static final String TAG = "AuthViewModel";
    
    private final PreferenceRepository preferenceRepository;
    private final ConfigRepository configRepository;
    private final FirebaseAuth firebaseAuth;
    
    // LiveData fields for UI state
    private final MutableLiveData<Boolean> isAuthenticatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> userIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> userEmailLiveData = new MutableLiveData<>();
    private final MutableLiveData<FirebaseAuth.AuthStateListener> authListenerLiveData = new MutableLiveData<>();
    
    /**
     * Constructor with repository injection
     * 
     * @param preferenceRepository Repository for app preferences
     * @param configRepository Repository for app configuration
     */
    public AuthViewModel(PreferenceRepository preferenceRepository, ConfigRepository configRepository) {
        this.preferenceRepository = preferenceRepository;
        this.configRepository = configRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
        
        // Set up auth state listener
        FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            isAuthenticatedLiveData.postValue(user != null);
            if (user != null) {
                userIdLiveData.postValue(user.getUid());
                userEmailLiveData.postValue(user.getEmail());
            } else {
                userIdLiveData.postValue(null);
                userEmailLiveData.postValue(null);
            }
        };
        
        // Register the listener
        firebaseAuth.addAuthStateListener(authStateListener);
        authListenerLiveData.setValue(authStateListener);
    }
    
    /**
     * Get authentication status as LiveData
     * 
     * @return LiveData containing authentication status
     */
    public LiveData<Boolean> isAuthenticated() {
        return isAuthenticatedLiveData;
    }
    
    /**
     * Get user ID as LiveData
     * 
     * @return LiveData containing user ID
     */
    public LiveData<String> getUserId() {
        return userIdLiveData;
    }
    
    /**
     * Get user email as LiveData
     * 
     * @return LiveData containing user email
     */
    public LiveData<String> getUserEmail() {
        return userEmailLiveData;
    }
    
    /**
     * Login with email and password
     * 
     * @param email User email
     * @param password User password
     * @return LiveData that will be updated with the result
     */
    public void login(String email, String password) {
        setLoading(true);
        clearError();
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                setLoading(false);
                showToast("Login successful");
            })
            .addOnFailureListener(e -> {
                setLoading(false);
                setError(e);
                Log.e(TAG, "Login failed", e);
                showToast("Login failed: " + e.getMessage());
            });
    }
    
    /**
     * Register new user with email and password
     * 
     * @param email User email
     * @param password User password
     */
    public void register(String email, String password) {
        setLoading(true);
        clearError();
        
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                setLoading(false);
                showToast("Registration successful");
                
                // Save user preferences
                saveInitialUserPreferences(authResult.getUser());
            })
            .addOnFailureListener(e -> {
                setLoading(false);
                setError(e);
                Log.e(TAG, "Registration failed", e);
                showToast("Registration failed: " + e.getMessage());
            });
    }
    
    /**
     * Sign out current user
     */
    public void signOut() {
        firebaseAuth.signOut();
    }
    
    /**
     * Save initial user preferences after registration
     * 
     * @param user Firebase user
     */
    private void saveInitialUserPreferences(FirebaseUser user) {
        if (user == null) return;
        
        // Get default preferences from config
        disposables.add(
            configRepository.getConfigValue("default_preferences", "{}")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    defaultPrefs -> {
                        // Save user registration timestamp
                        preferenceRepository.setLongPreference("user_registered_at", System.currentTimeMillis())
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                        
                        // Save user email for future reference
                        preferenceRepository.setStringPreference("user_email", user.getEmail())
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                    },
                    error -> Log.e(TAG, "Error saving initial preferences", error)
                )
        );
    }
    
    /**
     * Check if current password is valid
     * 
     * @param email User email
     * @param password Current password to check
     * @return LiveData that will be updated with the result
     */
    public void verifyPassword(String email, String password) {
        setLoading(true);
        clearError();
        
        // Re-authenticate with current credentials
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                setLoading(false);
                showToast("Password verified");
            })
            .addOnFailureListener(e -> {
                setLoading(false);
                setError(e);
                Log.e(TAG, "Password verification failed", e);
                showToast("Password verification failed");
            });
    }
    
    /**
     * Reset password for user email
     * 
     * @param email User email
     */
    public void resetPassword(String email) {
        setLoading(true);
        clearError();
        
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener(aVoid -> {
                setLoading(false);
                showToast("Password reset email sent");
            })
            .addOnFailureListener(e -> {
                setLoading(false);
                setError(e);
                Log.e(TAG, "Password reset failed", e);
                showToast("Password reset failed: " + e.getMessage());
            });
    }
    
    /**
     * Check if a user is currently authenticated
     * 
     * @return True if authenticated, false otherwise
     */
    public boolean checkAuthenticationState() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        boolean isAuthenticated = user != null;
        isAuthenticatedLiveData.setValue(isAuthenticated);
        
        if (isAuthenticated) {
            userIdLiveData.setValue(user.getUid());
            userEmailLiveData.setValue(user.getEmail());
        }
        
        return isAuthenticated;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        
        // Remove the auth state listener
        FirebaseAuth.AuthStateListener listener = authListenerLiveData.getValue();
        if (listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
    }
}
