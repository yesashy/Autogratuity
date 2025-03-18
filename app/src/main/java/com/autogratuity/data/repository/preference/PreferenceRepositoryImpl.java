package com.autogratuity.data.repository.preference;

import android.content.Context;
import android.util.Log;

import com.autogratuity.data.model.AppConfig;
import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.repository.core.FirestoreRepository;
import com.autogratuity.data.serialization.UserProfileSerializer;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Implementation of PreferenceRepository interface.
 * Responsible for managing user preferences and profile data.
 */
public class PreferenceRepositoryImpl extends FirestoreRepository implements PreferenceRepository {
    
    private static final String TAG = "PreferenceRepository";
    
    // Collection and field names
    private static final String COLLECTION_USER_PROFILES = "user_profiles";
    private static final String FIELD_DISPLAY_NAME = "displayName";
    private static final String FIELD_DEFAULT_ADDRESS_ID = "defaultAddressId";
    
    // Nested field paths
    private static final String PATH_PREFERENCES = "preferences";
    private static final String PATH_PREFERENCES_THEME = "preferences.theme";
    private static final String PATH_PREFERENCES_DEFAULT_TIP = "preferences.defaultTipPercentage";
    private static final String PATH_PREFERENCES_NOTIFICATIONS = "preferences.notificationsEnabled";
    private static final String PATH_APP_SETTINGS = "appSettings";
    private static final String PATH_APP_SETTINGS_ONBOARDING = "appSettings.onboardingCompleted";
    private static final String PATH_APP_SETTINGS_DATA_COLLECTION = "appSettings.dataCollectionOptIn";
    
    // In-memory cache keys
    private static final String CACHE_USER_PROFILE = "user_profile";
    
    // Subject for real-time updates
    private final BehaviorSubject<UserProfile> userProfileSubject = BehaviorSubject.create();
    
    /**
     * Constructor for PreferenceRepositoryImpl
     * 
     * @param context Android context for SharedPreferences and connectivity
     */
    public PreferenceRepositoryImpl(Context context) {
        super(context);
        
        // Initialize the UserProfileSerializer
        UserProfileSerializer.init(context);
        
        setupProfileListener();
    }
    
    /**
     * Sets up real-time listener for user profile updates
     */
    private void setupProfileListener() {
        DocumentReference userProfileRef = db.collection(COLLECTION_USER_PROFILES).document(userId);
        
        // Store the listener registration for cleanup
        activeListeners.put(CACHE_USER_PROFILE, userProfileRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "Error listening to user profile", e);
                return;
            }
            
            if (snapshot != null && snapshot.exists()) {
                // Use UserProfileSerializer to deserialize with encryption
                UserProfile userProfile = UserProfileSerializer.fromDocumentSnapshot(snapshot, context);
                if (userProfile != null) {
                    // Update the in-memory cache
                    putInCache(CACHE_USER_PROFILE, userProfile);
                    
                    // Emit to observers
                    userProfileSubject.onNext(userProfile);
                    
                    // Also save to preferences for offline access with encryption
                    String profileJson = UserProfileSerializer.toJson(userProfile, context);
                    prefManager.saveString(KEY_USER_PROFILE, profileJson);
                }
            }
        }));
    }
    
    /**
     * Gets a reference to the user profile document
     * 
     * @return DocumentReference for the user profile
     */
    private DocumentReference getUserProfileDocRef() {
        return db.collection(COLLECTION_USER_PROFILES).document(userId);
    }
    
    /**
     * Creates a default user profile when none exists
     * 
     * @return The created default profile
     */
    private UserProfile createDefaultUserProfile() {
        // Use UserProfileSerializer to create a default profile
        UserProfile profile = UserProfileSerializer.createDefaultUserProfile(userId);
        
        // Save the default profile
        updateUserProfile(profile).subscribe(
            () -> Log.d(TAG, "Default user profile created"),
            error -> Log.e(TAG, "Error creating default user profile", error)
        );
        
        return profile;
    }
    
    @Override
    public Single<UserProfile> getUserProfile() {
        return getUserProfile(false);
    }
    
    @Override
    public Single<UserProfile> getUserProfile(boolean forceRefresh) {
        return Single.create(emitter -> {
            // Check in-memory cache first, unless forcing refresh
            if (!forceRefresh) {
                UserProfile cachedProfile = getFromCache(CACHE_USER_PROFILE);
                if (cachedProfile != null) {
                    emitter.onSuccess(cachedProfile);
                    return;
                }
            }
            
            // If offline and not in memory cache, check shared preferences
            if (!isNetworkAvailable() && !forceRefresh) {
                String profileJson = prefManager.getString(KEY_USER_PROFILE, null);
                if (profileJson != null) {
                    UserProfile prefsProfile = UserProfileSerializer.fromJson(profileJson, context);
                    if (prefsProfile != null) {
                        putInCache(CACHE_USER_PROFILE, prefsProfile);
                        emitter.onSuccess(prefsProfile);
                        return;
                    }
                }
            }
            
            // If forcing refresh or not in cache, fetch from Firestore
            getUserProfileDocRef().get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Use UserProfileSerializer for deserialization with proper encryption
                        UserProfile profile = UserProfileSerializer.fromDocumentSnapshot(documentSnapshot, context);
                        if (profile != null) {
                            // Update cache and shared preferences
                            putInCache(CACHE_USER_PROFILE, profile);
                            
                            // Save to preferences with encryption
                            String profileJson = UserProfileSerializer.toJson(profile, context);
                            prefManager.saveString(KEY_USER_PROFILE, profileJson);
                            
                            // Emit the result
                            emitter.onSuccess(profile);
                        } else {
                            emitter.onError(new Exception("Failed to parse user profile"));
                        }
                    } else {
                        // Create default profile if none exists
                        UserProfile defaultProfile = createDefaultUserProfile();
                        emitter.onSuccess(defaultProfile);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user profile", e);
                    
                    // If offline, try to get from shared preferences as a fallback
                    if (!isNetworkAvailable()) {
                        String profileJson = prefManager.getString(KEY_USER_PROFILE, null);
                        if (profileJson != null) {
                            UserProfile prefsProfile = UserProfileSerializer.fromJson(profileJson, context);
                            if (prefsProfile != null) {
                                emitter.onSuccess(prefsProfile);
                            } else {
                                emitter.onError(e);
                            }
                        } else {
                            emitter.onError(e);
                        }
                    } else {
                        emitter.onError(e);
                    }
                });
        });
    }
    
    @Override
    public Completable updateUserProfile(UserProfile profile) {
        return Completable.create(emitter -> {
            if (profile == null) {
                emitter.onError(new IllegalArgumentException("Profile cannot be null"));
                return;
            }
            
            // Ensure the user ID is set
            profile.setUserId(userId);
            
            // Update version for conflict resolution
            profile.setVersion(profile.getVersion() + 1);
            
            // Validate the profile before saving
            if (!UserProfileSerializer.validateUserProfile(profile)) {
                emitter.onError(new IllegalArgumentException("Invalid user profile data"));
                return;
            }
            
            // Convert profile to map using serializer
            Map<String, Object> profileMap = UserProfileSerializer.toMap(profile, context);
            
            // Update document in Firestore
            getUserProfileDocRef().set(profileMap)
                .addOnSuccessListener(aVoid -> {
                    // Update cache and shared preferences
                    putInCache(CACHE_USER_PROFILE, profile);
                    
                    // Save to preferences with encryption
                    String profileJson = UserProfileSerializer.toJson(profile, context);
                    prefManager.saveString(KEY_USER_PROFILE, profileJson);
                    
                    // Emit the update to observers
                    userProfileSubject.onNext(profile);
                    
                    emitter.onComplete();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user profile", e);
                    
                    // If offline, queue for sync later
                    if (!isNetworkAvailable()) {
                        // Convert profile to Map for sync operation using serializer
                        Map<String, Object> serializedProfileMap = UserProfileSerializer.toMap(profile, context);
                        
                        enqueueOperation("update", COLLECTION_USER_PROFILES, userId, serializedProfileMap)
                            .subscribe(
                                emitter::onComplete,
                                emitter::onError
                            );
                    } else {
                        emitter.onError(e);
                    }
                });
        });
    }
    
    @Override
    public Completable updateUserProfileFields(Map<String, Object> fields) {
        return Completable.create(emitter -> {
            if (fields == null || fields.isEmpty()) {
                emitter.onError(new IllegalArgumentException("Fields map cannot be null or empty"));
                return;
            }
            
            // Ensure the user ID is not being changed
            fields.remove("userId");
            
            // Update version for conflict resolution
            getUserProfile().flatMapCompletable(profile -> {
                    // Get the current version
                    long version = profile.getVersion();
                    fields.put("version", version + 1);
                    
                    // Verify we're not trying to update user ID
                    if (fields.containsKey("userId")) {
                        return Completable.error(new IllegalArgumentException("Cannot change userId"));
                    }
                    
                    return Completable.create(innerEmitter -> {
                        // Validate fields
                        if (fields.containsKey(PATH_PREFERENCES_DEFAULT_TIP)) {
                            Number tipPercentage = (Number) fields.get(PATH_PREFERENCES_DEFAULT_TIP);
                            if (tipPercentage != null && (tipPercentage.intValue() < 0 || tipPercentage.intValue() > 100)) {
                                innerEmitter.onError(new IllegalArgumentException("Tip percentage must be between 0 and 100"));
                                return;
                            }
                        }
                        
                        if (fields.containsKey(PATH_PREFERENCES_THEME)) {
                            String theme = (String) fields.get(PATH_PREFERENCES_THEME);
                            if (theme != null) {
                                String themeValue = theme.toLowerCase();
                                if (!themeValue.equals("light") && !themeValue.equals("dark") && !themeValue.equals("system")) {
                                    innerEmitter.onError(new IllegalArgumentException("Theme must be 'light', 'dark', or 'system'"));
                                    return;
                                }
                            }
                        }
                        
                        // Update document in Firestore using merge option
                        getUserProfileDocRef().set(fields, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                // Refresh the profile after update
                                getUserProfile(true)
                                    .subscribe(
                                        updatedProfile -> {
                                            // Update emitted to observers through profile listener
                                            innerEmitter.onComplete();
                                        },
                                        innerEmitter::onError
                                    );
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating user profile fields", e);
                                
                                // If offline, queue for sync later
                                if (!isNetworkAvailable()) {
                                    enqueueOperation("update", COLLECTION_USER_PROFILES, userId, fields)
                                        .subscribe(
                                            innerEmitter::onComplete,
                                            innerEmitter::onError
                                        );
                                } else {
                                    innerEmitter.onError(e);
                                }
                            });
                    });
                })
                .subscribe(
                    emitter::onComplete,
                    emitter::onError
                );
        });
    }
    
    @Override
    public Observable<UserProfile> observeUserProfile() {
        // Ensure we have the initial value
        if (!userProfileSubject.hasValue()) {
            getUserProfile().subscribe(
                profile -> {}, // Profile will be emitted through the subject
                error -> Log.e(TAG, "Error fetching initial user profile for observation", error)
            );
        }
        
        return userProfileSubject;
    }
    
    @Override
    public Completable setDisplayName(String displayName) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(FIELD_DISPLAY_NAME, displayName);
        return updateUserProfileFields(fields);
    }
    
    @Override
    public <T> Single<T> getPreferenceSetting(String key, T defaultValue) {
        return getUserProfile().map(profile -> {
            if (profile == null) {
                return defaultValue;
            }
            
            // This implementation handles both top-level and nested fields through reflection
            try {
                // Handle nested paths like "preferences.theme"
                if (key.contains(".")) {
                    String[] parts = key.split("\\.");
                    Object currentObject = profile;
                    
                    // Navigate through the object hierarchy
                    for (int i = 0; i < parts.length - 1; i++) {
                        String part = parts[i];
                        String getterMethod = "get" + part.substring(0, 1).toUpperCase() + part.substring(1);
                        currentObject = currentObject.getClass().getMethod(getterMethod).invoke(currentObject);
                        
                        if (currentObject == null) {
                            return defaultValue;
                        }
                    }
                    
                    // Get the final property
                    String lastPart = parts[parts.length - 1];
                    String getterMethod;
                    
                    // Handle boolean properties that use "is" prefix
                    if (lastPart.startsWith("is") && lastPart.length() > 2 && 
                        Character.isUpperCase(lastPart.charAt(2))) {
                        getterMethod = lastPart;
                    } else {
                        getterMethod = "get" + lastPart.substring(0, 1).toUpperCase() + lastPart.substring(1);
                    }
                    
                    @SuppressWarnings("unchecked")
                    T value = (T) currentObject.getClass().getMethod(getterMethod).invoke(currentObject);
                    return value != null ? value : defaultValue;
                } else {
                    // Simple case: top-level property
                    String getterMethod;
                    if (key.startsWith("is") && key.length() > 2 && Character.isUpperCase(key.charAt(2))) {
                        getterMethod = key;
                    } else {
                        getterMethod = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
                    }
                    
                    @SuppressWarnings("unchecked")
                    T value = (T) profile.getClass().getMethod(getterMethod).invoke(profile);
                    return value != null ? value : defaultValue;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting preference: " + key, e);
                return defaultValue;
            }
        });
    }
    
    @Override
    public <T> Completable setPreferenceSetting(String key, T value) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(key, value);
        return updateUserProfileFields(fields);
    }
    
    @Override
    public Single<Integer> getDefaultTipPercentage() {
        return getUserProfile().map(profile -> {
            if (profile != null && profile.getPreferences() != null) {
                return profile.getPreferences().getDefaultTipPercentage();
            }
            return 15; // Default value if not set
        });
    }
    
    @Override
    public Completable setDefaultTipPercentage(int percentage) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(PATH_PREFERENCES_DEFAULT_TIP, percentage);
        return updateUserProfileFields(fields);
    }
    
    @Override
    public Single<String> getThemePreference() {
        return getUserProfile().map(profile -> {
            if (profile != null && profile.getPreferences() != null) {
                return profile.getPreferences().getTheme();
            }
            return "system"; // Default to system theme
        });
    }
    
    @Override
    public Completable setThemePreference(String theme) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(PATH_PREFERENCES_THEME, theme);
        return updateUserProfileFields(fields);
    }
    
    @Override
    public Single<Boolean> getNotificationsEnabled() {
        return getUserProfile().map(profile -> {
            if (profile != null && profile.getPreferences() != null) {
                return profile.getPreferences().isNotificationsEnabled();
            }
            return true; // Default to enabled
        });
    }
    
    @Override
    public Completable setNotificationsEnabled(boolean enabled) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(PATH_PREFERENCES_NOTIFICATIONS, enabled);
        return updateUserProfileFields(fields);
    }
    
    @Override
    public Single<String> getDefaultAddressId() {
        return getUserProfile().map(UserProfile::getDefaultAddressId);
    }
    
    @Override
    public Completable setDefaultAddressId(String addressId) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(FIELD_DEFAULT_ADDRESS_ID, addressId);
        return updateUserProfileFields(fields);
    }
    
    @Override
    public Single<Boolean> isOnboardingCompleted() {
        return getUserProfile().map(profile -> {
            if (profile != null && profile.getAppSettings() != null) {
                return profile.getAppSettings().isOnboardingCompleted();
            }
            return false; // Default to not completed
        });
    }
    
    @Override
    public Completable setOnboardingCompleted(boolean completed) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(PATH_APP_SETTINGS_ONBOARDING, completed);
        return updateUserProfileFields(fields);
    }
    
    @Override
    public Single<Boolean> isDataCollectionOptedIn() {
        return getUserProfile().map(profile -> {
            if (profile != null && profile.getAppSettings() != null) {
                return profile.getAppSettings().isDataCollectionOptIn();
            }
            return true; // Default to opted in
        });
    }
    
    @Override
    public Completable setDataCollectionOptedIn(boolean optedIn) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(PATH_APP_SETTINGS_DATA_COLLECTION, optedIn);
        return updateUserProfileFields(fields);
    }
    
    /**
     * Clean up resources when the repository is no longer needed
     */
    public void cleanup() {
        // Remove Firestore listeners
        if (activeListeners.containsKey(CACHE_USER_PROFILE)) {
            activeListeners.get(CACHE_USER_PROFILE).remove();
            activeListeners.remove(CACHE_USER_PROFILE);
        }
    }
}
