package com.autogratuity.data.repository.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Interface defining a standardized caching strategy for repositories.
 * Provides a contract for storing, retrieving, and managing cached data
 * with support for time-based expiration and metadata.
 *
 * @param <K> The type of key used to identify cached items
 * @param <V> The type of value stored in the cache
 */
public interface CacheStrategy<K, V> {

    /**
     * Store a value in the cache with the specified key
     *
     * @param key The key to identify the cached value
     * @param value The value to store in the cache
     * @return true if the value was successfully stored, false otherwise
     */
    boolean put(@NonNull K key, @NonNull V value);

    /**
     * Store a value in the cache with the specified key and expiration time
     *
     * @param key The key to identify the cached value
     * @param value The value to store in the cache
     * @param duration The duration after which the cache entry expires
     * @param unit The time unit for the duration
     * @return true if the value was successfully stored, false otherwise
     */
    boolean put(@NonNull K key, @NonNull V value, long duration, @NonNull TimeUnit unit);

    /**
     * Store multiple values in the cache at once
     *
     * @param values A collection of key-value pairs to store in the cache
     * @return The number of values successfully stored
     */
    int putAll(@NonNull Collection<Pair<K, V>> values);

    /**
     * Retrieve a value from the cache by its key
     *
     * @param key The key of the value to retrieve
     * @return The cached value, or null if not found or expired
     */
    @Nullable
    V get(@NonNull K key);

    /**
     * Check if a key exists in the cache and is not expired
     *
     * @param key The key to check
     * @return true if the key exists and is not expired, false otherwise
     */
    boolean contains(@NonNull K key);

    /**
     * Remove a value from the cache by its key
     *
     * @param key The key of the value to remove
     * @return true if the value was successfully removed, false if it didn't exist
     */
    boolean remove(@NonNull K key);

    /**
     * Remove all values from the cache
     */
    void clear();

    /**
     * Remove all expired values from the cache
     *
     * @return The number of expired values removed
     */
    int clearExpired();

    /**
     * Get the time when a cached value was last updated
     *
     * @param key The key of the cached value
     * @return The time when the value was last updated, or null if the key doesn't exist
     */
    @Nullable
    Date getLastUpdated(@NonNull K key);

    /**
     * Get the expiration time for a cached value
     *
     * @param key The key of the cached value
     * @return The expiration time for the value, or null if the key doesn't exist or doesn't expire
     */
    @Nullable
    Date getExpirationTime(@NonNull K key);

    /**
     * Check if a cached value has expired
     *
     * @param key The key of the cached value
     * @return true if the value has expired, false otherwise
     */
    boolean isExpired(@NonNull K key);

    /**
     * Get all keys in the cache
     *
     * @return A list of all keys in the cache
     */
    List<K> getAllKeys();

    /**
     * Get the number of items in the cache
     *
     * @return The number of items in the cache
     */
    int size();

    /**
     * Simple class to hold key-value pairs for bulk operations
     *
     * @param <K> The key type
     * @param <V> The value type
     */
    class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
