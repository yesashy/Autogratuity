//-----------------------------------------------------------------------------------
// Helper methods for Shared Preferences
//-----------------------------------------------------------------------------------

/**
 * Get data from SharedPreferences
 * 
 * @param key SharedPreferences key
 * @param classOfT Class type for deserialization
 * @param <T> Type of data
 * @return Deserialized data or null if not found
 */
private <T> T getFromPrefs(String key, Class<T> classOfT) {
    return preferenceManager.getObject(key, classOfT);
}

/**
 * Store data in SharedPreferences
 * 
 * @param key SharedPreferences key
 * @param data Data to store
 * @param <T> Type of data
 */
private <T> void saveToPrefs(String key, T data) {
    preferenceManager.saveObject(key, data);
}

/**
 * Invalidate a cache entry by pattern
 * 
 * @param keyPattern Cache key pattern (supports trailing * wildcard)
 */
private void invalidateCache(String keyPattern) {
    if (keyPattern.endsWith("*")) {
        String prefix = keyPattern.substring(0, keyPattern.length() - 1);
        List<String> keysToRemove = new ArrayList<>();
        
        // Find all keys that match the pattern
        for (String key : memoryCache.keySet()) {
            if (key.startsWith(prefix)) {
                keysToRemove.add(key);
            }
        }
        
        // Remove matching keys
        for (String key : keysToRemove) {
            memoryCache.remove(key);
            cacheTimestamps.remove(key);
        }
    } else {
        // Simple key removal
        memoryCache.remove(keyPattern);
        cacheTimestamps.remove(keyPattern);
    }
}
