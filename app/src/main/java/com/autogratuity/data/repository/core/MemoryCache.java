package com.autogratuity.data.repository.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of CacheStrategy that stores data in memory.
 * This implementation is thread-safe and supports time-based expiration.
 *
 * @param <K> The type of key used to identify cached items
 * @param <V> The type of value stored in the cache
 */
public class MemoryCache<K, V> implements CacheStrategy<K, V> {

    private final Map<K, CacheEntry<V>> cache;
    private final long defaultExpirationMillis;

    /**
     * Entry in the cache containing the value and metadata
     *
     * @param <V> The type of value stored in the cache
     */
    private static class CacheEntry<V> {
        final V value;
        final long createdAt;
        final long expiresAt;

        CacheEntry(V value, long createdAt, long expiresAt) {
            this.value = value;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
        }
    }

    /**
     * Create a MemoryCache with infinite expiration time
     */
    public MemoryCache() {
        this(0, null);
    }

    /**
     * Create a MemoryCache with the specified default expiration time
     *
     * @param duration The default duration after which cache entries expire
     * @param unit The time unit for the duration
     */
    public MemoryCache(long duration, @Nullable TimeUnit unit) {
        this.cache = new ConcurrentHashMap<>();
        this.defaultExpirationMillis = (unit != null) ? unit.toMillis(duration) : 0;
    }

    /**
     * Convert a duration and time unit to milliseconds
     *
     * @param duration The duration
     * @param unit The time unit
     * @return The duration in milliseconds, or 0 for infinite
     */
    private long toExpirationMillis(long duration, @Nullable TimeUnit unit) {
        if (duration <= 0 || unit == null) {
            return 0; // No expiration
        }
        return System.currentTimeMillis() + unit.toMillis(duration);
    }

    @Override
    public boolean put(@NonNull K key, @NonNull V value) {
        long now = System.currentTimeMillis();
        long expiration = defaultExpirationMillis > 0 ? now + defaultExpirationMillis : 0;
        cache.put(key, new CacheEntry<>(value, now, expiration));
        return true;
    }

    @Override
    public boolean put(@NonNull K key, @NonNull V value, long duration, @NonNull TimeUnit unit) {
        long now = System.currentTimeMillis();
        long expiration = toExpirationMillis(duration, unit);
        cache.put(key, new CacheEntry<>(value, now, expiration));
        return true;
    }

    @Override
    public int putAll(@NonNull Collection<Pair<K, V>> values) {
        int count = 0;
        for (Pair<K, V> pair : values) {
            if (put(pair.getKey(), pair.getValue())) {
                count++;
            }
        }
        return count;
    }

    @Override
    @Nullable
    public V get(@NonNull K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            if (entry != null && entry.isExpired()) {
                cache.remove(key);
            }
            return null;
        }
        return entry.value;
    }

    @Override
    public boolean contains(@NonNull K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            if (entry != null && entry.isExpired()) {
                cache.remove(key);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean remove(@NonNull K key) {
        return cache.remove(key) != null;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int clearExpired() {
        List<K> expiredKeys = new ArrayList<>();
        
        // Find expired entries
        for (Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredKeys.add(entry.getKey());
            }
        }
        
        // Remove expired entries
        for (K key : expiredKeys) {
            cache.remove(key);
        }
        
        return expiredKeys.size();
    }

    @Override
    @Nullable
    public Date getLastUpdated(@NonNull K key) {
        CacheEntry<V> entry = cache.get(key);
        return entry != null ? new Date(entry.createdAt) : null;
    }

    @Override
    @Nullable
    public Date getExpirationTime(@NonNull K key) {
        CacheEntry<V> entry = cache.get(key);
        return (entry != null && entry.expiresAt > 0) ? new Date(entry.expiresAt) : null;
    }

    @Override
    public boolean isExpired(@NonNull K key) {
        CacheEntry<V> entry = cache.get(key);
        return entry == null || entry.isExpired();
    }

    @Override
    public List<K> getAllKeys() {
        return new ArrayList<>(cache.keySet());
    }

    @Override
    public int size() {
        clearExpired(); // Ensure we don't count expired entries
        return cache.size();
    }

    /**
     * Create a map of cache pairs from a map
     *
     * @param map The map to convert
     * @param <K> The key type
     * @param <V> The value type
     * @return A collection of Pair objects
     */
    public static <K, V> Collection<Pair<K, V>> mapToPairs(Map<K, V> map) {
        List<Pair<K, V>> pairs = new ArrayList<>(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            pairs.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        return pairs;
    }

    /**
     * Create a new builder for configuring a MemoryCache
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A new MemoryCacheBuilder
     */
    public static <K, V> MemoryCacheBuilder<K, V> builder() {
        return new MemoryCacheBuilder<>();
    }

    /**
     * Builder class for creating MemoryCache instances with custom configurations
     *
     * @param <K> The key type
     * @param <V> The value type
     */
    public static class MemoryCacheBuilder<K, V> {
        private long duration = 0;
        private TimeUnit timeUnit = null;
        private Map<K, V> initialItems = null;

        /**
         * Set the default expiration time for cache entries
         *
         * @param duration The duration after which entries expire
         * @param timeUnit The time unit for the duration
         * @return This builder for chaining
         */
        public MemoryCacheBuilder<K, V> withExpiration(long duration, TimeUnit timeUnit) {
            this.duration = duration;
            this.timeUnit = timeUnit;
            return this;
        }

        /**
         * Set initial items to populate the cache with
         *
         * @param items The initial items for the cache
         * @return This builder for chaining
         */
        public MemoryCacheBuilder<K, V> withInitialItems(Map<K, V> items) {
            this.initialItems = new HashMap<>(items);
            return this;
        }

        /**
         * Build the MemoryCache with the configured options
         *
         * @return A new MemoryCache instance
         */
        public MemoryCache<K, V> build() {
            MemoryCache<K, V> cache = new MemoryCache<>(duration, timeUnit);
            if (initialItems != null && !initialItems.isEmpty()) {
                cache.putAll(mapToPairs(initialItems));
            }
            return cache;
        }
    }
}
