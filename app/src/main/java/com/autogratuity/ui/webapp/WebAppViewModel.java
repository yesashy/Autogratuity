package com.autogratuity.ui.webapp;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.repository.config.ConfigRepository;
import com.autogratuity.data.repository.preference.PreferenceRepository;
import com.autogratuity.data.security.AuthenticationManager;
import com.autogratuity.ui.common.BaseViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for WebAppActivity, implementing the repository pattern.
 * Handles data operations and business logic related to the web app interface.
 */
public class WebAppViewModel extends BaseViewModel {
    private static final String TAG = "WebAppViewModel";
    
    private final PreferenceRepository preferenceRepository;
    private final ConfigRepository configRepository;
    private final AuthenticationManager authManager;
    
    // LiveData fields for UI state
    private final MutableLiveData<String> userIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAuthenticatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> webAppContentLiveData = new MutableLiveData<>();
    
    /**
     * Constructor with repository injection
     * 
     * @param preferenceRepository Repository for app preferences
     * @param configRepository Repository for app configuration
     */
    public WebAppViewModel(PreferenceRepository preferenceRepository, ConfigRepository configRepository) {
        this.preferenceRepository = preferenceRepository;
        this.configRepository = configRepository;
        this.authManager = AuthenticationManager.getInstance(null); // Context will be set when used
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
     * Get authentication status as LiveData
     * 
     * @return LiveData containing authentication status
     */
    public LiveData<Boolean> isAuthenticated() {
        return isAuthenticatedLiveData;
    }
    
    /**
     * Get web app content as LiveData
     * 
     * @return LiveData containing web app content
     */
    public LiveData<String> getWebAppContent() {
        return webAppContentLiveData;
    }
    
    /**
     * Load user information and update LiveData
     */
    public void loadUserInfo() {
        // Use AuthenticationManager to check if user is authenticated
        boolean isAuthenticated = authManager.isAuthenticated();
        isAuthenticatedLiveData.setValue(isAuthenticated);
        
        if (isAuthenticated) {
            String userId = authManager.getCurrentUserId();
            userIdLiveData.setValue(userId);
        } else {
            userIdLiveData.setValue("Not logged in");
        }
    }
    
    /**
     * Load web app content from configuration
     */
    public void loadWebAppContent() {
        setLoading(true);
        
        // Load content from ConfigRepository
        disposables.add(
            configRepository.getConfigValue("webapp_content", "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    content -> {
                        if (content != null && !content.isEmpty()) {
                            webAppContentLiveData.setValue(content);
                        } else {
                            // If no custom content is found, load default content
                            loadDefaultWebAppContent();
                        }
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading web app content", error);
                        setError(error);
                        loadDefaultWebAppContent();
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Load default web app content when custom content is not available
     */
    private void loadDefaultWebAppContent() {
        // Create default HTML content
        String defaultHtml = "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Autogratuity</title>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }"
                + "h1 { color: #6200EE; }"
                + ".card { background: #f9f9f9; border-radius: 8px; padding: 15px; margin-bottom: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }"
                + "button { background: #6200EE; color: white; border: none; padding: 10px 15px; border-radius: 4px; margin-top: 10px; }"
                + "</style>"
                + "</head><body>"
                + "<h1>Autogratuity</h1>"
                + "<div class='card'>"
                + "<h2>Welcome to Autogratuity</h2>"
                + "<p>Your Shipt tip tracking app is running!</p>"
                + "<p>This is a placeholder for the full web app interface.</p>"
                + "<button onclick='Android.showToast(\"Button clicked!\")'>Test Android Bridge</button>"
                + "</div>"
                + "<div class='card'>"
                + "<h3>Notification Status</h3>"
                + "<p>Listening for Shipt notifications...</p>"
                + "</div>"
                + "</body></html>";
        
        webAppContentLiveData.setValue(defaultHtml);
    }
    
    /**
     * Save a user preference
     * 
     * @param key Preference key
     * @param value Preference value
     */
    public void savePreference(String key, String value) {
        disposables.add(
            preferenceRepository.setStringPreference(key, value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> Log.d(TAG, "Preference saved: " + key),
                    error -> {
                        Log.e(TAG, "Error saving preference: " + key, error);
                        setError(error);
                    }
                )
        );
    }
}
