package com.autogratuity.data.repository.preference;

import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.repository.core.DataRepository;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Repository interface for managing user preferences and profile data.
 * Extends the core DataRepository interface for preference-specific operations.
 */
public interface PreferenceRepository extends DataRepository {
    
    //-----------------------------------------------------------------------------------
    // User Profile Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Get the current user's profile.
     * Will return from cache if available, otherwise fetches from Firestore.
     * 
     * @return Single that emits the user profile
     */
    @Override
    Single<UserProfile> getUserProfile();
    
    /**
     * Get the current user's profile with option to force refresh from Firestore.
     * 
     * @param forceRefresh If true, bypasses cache and fetches from Firestore
     * @return Single that emits the user profile
     */
    @Override
    Single<UserProfile> getUserProfile(boolean forceRefresh);
    
    /**
     * Update the entire user profile.
     * 
     * @param profile The updated profile
     * @return Completable that completes when update is finished
     */
    @Override
    Completable updateUserProfile(UserProfile profile);
    
    /**
     * Update specific fields of the user profile.
     * 
     * @param fields Map of field names to values to update
     * @return Completable that completes when update is finished
     */
    @Override
    Completable updateUserProfileFields(Map<String, Object> fields);
    
    /**
     * Observe changes to the user profile in real-time.
     * 
     * @return Observable that emits user profile updates
     */
    @Override
    Observable<UserProfile> observeUserProfile();
    
    //-----------------------------------------------------------------------------------
    // Convenience methods for common preferences
    //-----------------------------------------------------------------------------------
    
    /**
     * Set the display name for the user.
     * 
     * @param displayName The display name to set
     * @return Completable that completes when update is finished
     */
    Completable setDisplayName(String displayName);
    
    /**
     * Get preference setting as a specific type.
     * 
     * @param key Preference key
     * @param defaultValue Default value if preference not found
     * @param <T> Type of preference value
     * @return Single that emits the preference value
     */
    <T> Single<T> getPreferenceSetting(String key, T defaultValue);
    
    /**
     * Set preference setting.
     * 
     * @param key Preference key
     * @param value Preference value
     * @param <T> Type of preference value
     * @return Completable that completes when update is finished
     */
    <T> Completable setPreferenceSetting(String key, T value);
    
    /**
     * Get the default tip percentage.
     * 
     * @return Single that emits the default tip percentage
     */
    Single<Integer> getDefaultTipPercentage();
    
    /**
     * Set the default tip percentage.
     * 
     * @param percentage The default tip percentage to set
     * @return Completable that completes when update is finished
     */
    Completable setDefaultTipPercentage(int percentage);
    
    /**
     * Get the user's preferred theme.
     * 
     * @return Single that emits the theme preference
     */
    Single<String> getThemePreference();
    
    /**
     * Set the user's preferred theme.
     * 
     * @param theme The theme preference to set
     * @return Completable that completes when update is finished
     */
    Completable setThemePreference(String theme);
    
    /**
     * Get notification enabled preference.
     * 
     * @return Single that emits whether notifications are enabled
     */
    Single<Boolean> getNotificationsEnabled();
    
    /**
     * Set notification enabled preference.
     * 
     * @param enabled Whether notifications should be enabled
     * @return Completable that completes when update is finished
     */
    Completable setNotificationsEnabled(boolean enabled);
    
    /**
     * Get the default address ID.
     * 
     * @return Single that emits the default address ID
     */
    Single<String> getDefaultAddressId();
    
    /**
     * Set the default address ID.
     * 
     * @param addressId The address ID to set as default
     * @return Completable that completes when update is finished
     */
    Completable setDefaultAddressId(String addressId);
    
    /**
     * Get whether the user has completed onboarding.
     * 
     * @return Single that emits whether onboarding is completed
     */
    Single<Boolean> isOnboardingCompleted();
    
    /**
     * Set whether the user has completed onboarding.
     * 
     * @param completed Whether onboarding is completed
     * @return Completable that completes when update is finished
     */
    Completable setOnboardingCompleted(boolean completed);
    
    /**
     * Get whether the user has opted in to data collection.
     * 
     * @return Single that emits whether data collection is opted in
     */
    Single<Boolean> isDataCollectionOptedIn();
    
    /**
     * Set whether the user has opted in to data collection.
     * 
     * @param optedIn Whether data collection is opted in
     * @return Completable that completes when update is finished
     */
    Completable setDataCollectionOptedIn(boolean optedIn);
}
