package com.autogratuity.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class for managing SharedPreferences with JSON serialization.
 * Provides type-safe methods for storing and retrieving objects.
 */
public class PreferenceManager {
    private static final String TAG = "PreferenceManager";
    
    private final SharedPreferences preferences;
    
    /**
     * Constructor with SharedPreferences name.
     * 
     * @param context Application context
     * @param name Name of the SharedPreferences file
     */
    public PreferenceManager(Context context, String name) {
        this.preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }
    
    /**
     * Constructor with existing SharedPreferences.
     * 
     * @param preferences SharedPreferences instance
     */
    public PreferenceManager(SharedPreferences preferences) {
        this.preferences = preferences;
    }
    
    /**
     * Save an object to SharedPreferences.
     * 
     * @param key Preference key
     * @param object Object to save
     * @param <T> Type of object
     * @return true if saved successfully, false otherwise
     */
    public <T> boolean saveObject(String key, T object) {
        if (key == null || object == null) {
            return false;
        }
        
        String json = JsonSerializer.toJson(object);
        if (json == null) {
            return false;
        }
        
        return preferences.edit().putString(key, json).commit();
    }
    
    /**
     * Get an object from SharedPreferences.
     * 
     * @param key Preference key
     * @param classOfT Class of the object
     * @param <T> Type of object
     * @return The object, or null if not found or deserialization fails
     */
    public <T> T getObject(String key, Class<T> classOfT) {
        if (key == null || !preferences.contains(key)) {
            return null;
        }
        
        String json = preferences.getString(key, null);
        return JsonSerializer.fromJson(json, classOfT);
    }
    
    /**
     * Save a value to SharedPreferences.
     * 
     * @param key Preference key
     * @param value Value to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveString(String key, String value) {
        if (key == null) {
            return false;
        }
        
        return preferences.edit().putString(key, value).commit();
    }
    
    /**
     * Get a string from SharedPreferences.
     * 
     * @param key Preference key
     * @param defaultValue Default value to return if not found
     * @return The string value, or defaultValue if not found
     */
    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }
    
    /**
     * Save a boolean value to SharedPreferences.
     * 
     * @param key Preference key
     * @param value Value to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveBoolean(String key, boolean value) {
        if (key == null) {
            return false;
        }
        
        return preferences.edit().putBoolean(key, value).commit();
    }
    
    /**
     * Get a boolean from SharedPreferences.
     * 
     * @param key Preference key
     * @param defaultValue Default value to return if not found
     * @return The boolean value, or defaultValue if not found
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }
    
    /**
     * Save a long value to SharedPreferences.
     * 
     * @param key Preference key
     * @param value Value to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveLong(String key, long value) {
        if (key == null) {
            return false;
        }
        
        return preferences.edit().putLong(key, value).commit();
    }
    
    /**
     * Get a long from SharedPreferences.
     * 
     * @param key Preference key
     * @param defaultValue Default value to return if not found
     * @return The long value, or defaultValue if not found
     */
    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }
    
    /**
     * Check if a key exists in SharedPreferences.
     * 
     * @param key Preference key
     * @return true if the key exists, false otherwise
     */
    public boolean contains(String key) {
        return preferences.contains(key);
    }
    
    /**
     * Remove a key from SharedPreferences.
     * 
     * @param key Preference key
     * @return true if removed successfully, false otherwise
     */
    public boolean remove(String key) {
        if (key == null) {
            return false;
        }
        
        return preferences.edit().remove(key).commit();
    }
    
    /**
     * Clear all preferences.
     * 
     * @return true if cleared successfully, false otherwise
     */
    public boolean clear() {
        return preferences.edit().clear().commit();
    }
    
    /**
     * Get the underlying SharedPreferences instance.
     * 
     * @return SharedPreferences instance
     */
    public SharedPreferences getPreferences() {
        return preferences;
    }
}
