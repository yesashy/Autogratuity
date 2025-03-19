package com.autogratuity.data.repository.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.autogratuity.data.model.ErrorInfo;
import com.autogratuity.data.repository.core.CacheStrategy;
import com.autogratuity.data.repository.core.RepositoryErrorHandler;
import com.autogratuity.data.util.RxSchedulers;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import io.reactivex.functions.Function;

/**
 * Utility class providing standardized RxJava patterns for repositories.
 * 
 * This class establishes clear guidelines for when to use each reactive type,
 * provides standard transformers for common repository operations,
 * and integrates with the RepositoryErrorHandler and CacheStrategy components
 * for consistent error handling and caching.
 *
 * <h2>Reactive Type Guidelines</h2>
 * <ul>
 *     <li><b>Single</b>: Use for operations that return exactly one item (e.g., getById, getConfig)</li>
 *     <li><b>Observable</b>: Use for operations that emit a stream of items over time (e.g., observeChanges)</li>
 *     <li><b>Completable</b>: Use for operations with no return value (e.g., update, delete)</li>
 *     <li><b>Maybe</b>: Use for operations that might not return a value (e.g., findBy, getByCondition)</li>
 *     <li><b>Flowable</b>: Use for operations that need backpressure handling (e.g., large data streams)</li>
 * </ul>
 */
public class RxJavaRepositoryExtensions {

    /**
     * Creates a standardized transformer for repository read operations that return a single item.
     * This transformer applies proper threading, error handling, and caching.
     *
     * @param <T> The type of item being returned
     * @param errorHandler The repository error handler
     * @param operationName Human-readable name of the operation (for error reporting)
     * @param entityType Type of entity being operated on (for error reporting)
     * @return SingleTransformer that applies standard repository patterns
     */
    public static <T> SingleTransformer<T, T> applyStandardReadTransformer(
            @NonNull RepositoryErrorHandler errorHandler,
            @NonNull String operationName,
            @Nullable String entityType) {
        
        return single -> single
                .subscribeOn(RxSchedulers.io())
                .observeOn(RxSchedulers.io())
                .doOnError(throwable -> errorHandler.handleError(throwable, operationName, entityType));
    }
    
    /**
     * Creates a standardized transformer for repository read operations that return a single item,
     * with caching support.
     *
     * @param <T> The type of item being returned
     * @param errorHandler The repository error handler
     * @param operationName Human-readable name of the operation (for error reporting)
     * @param entityType Type of entity being operated on (for error reporting)
     * @param cacheStrategy The cache strategy to use
     * @param cacheKey The key to use for caching
     * @param ttl Time-to-live for the cache entry
     * @param ttlUnit Time unit for the ttl
     * @return SingleTransformer that applies standard repository patterns with caching
     */
    public static <K, T> SingleTransformer<T, T> applyStandardReadTransformerWithCache(
            @NonNull RepositoryErrorHandler errorHandler,
            @NonNull String operationName,
            @Nullable String entityType,
            @NonNull CacheStrategy<K, T> cacheStrategy,
            @NonNull K cacheKey,
            long ttl,
            @NonNull TimeUnit ttlUnit) {
        
        return single -> single
                .subscribeOn(RxSchedulers.io())
                .observeOn(RxSchedulers.io())
                .doOnSuccess(value -> cacheStrategy.put(cacheKey, value, ttl, ttlUnit))
                .doOnError(throwable -> errorHandler.handleError(throwable, operationName, entityType));
    }
    
    /**
     * Creates a standardized transformer for repository read operations returning a stream of items.
     * This transformer applies proper threading and error handling.
     *
     * @param <T> The type of items being emitted
     * @param errorHandler The repository error handler
     * @param operationName Human-readable name of the operation (for error reporting)
     * @param entityType Type of entity being operated on (for error reporting)
     * @return ObservableTransformer that applies standard repository patterns
     */
    public static <T> ObservableTransformer<T, T> applyStandardObserveTransformer(
            @NonNull RepositoryErrorHandler errorHandler,
            @NonNull String operationName,
            @Nullable String entityType) {
        
        return observable -> observable
                .subscribeOn(RxSchedulers.io())
                .observeOn(RxSchedulers.io())
                .doOnError(throwable -> errorHandler.handleError(throwable, operationName, entityType));
    }
    
    /**
     * Creates a standardized transformer for repository write operations (no return value).
     * This transformer applies proper threading and error handling.
     *
     * @param errorHandler The repository error handler
     * @param operationName Human-readable name of the operation (for error reporting)
     * @param entityType Type of entity being operated on (for error reporting)
     * @return CompletableTransformer that applies standard repository patterns
     */
    public static CompletableTransformer applyStandardWriteTransformer(
            @NonNull RepositoryErrorHandler errorHandler,
            @NonNull String operationName,
            @Nullable String entityType) {
        
        return completable -> completable
                .subscribeOn(RxSchedulers.io())
                .observeOn(RxSchedulers.io())
                .doOnError(throwable -> errorHandler.handleError(throwable, operationName, entityType));
    }
    
    /**
     * Creates a standardized transformer for repository operations that might not return a value.
     * This transformer applies proper threading and error handling.
     *
     * @param <T> The type of item that might be returned
     * @param errorHandler The repository error handler
     * @param operationName Human-readable name of the operation (for error reporting)
     * @param entityType Type of entity being operated on (for error reporting)
     * @return MaybeTransformer that applies standard repository patterns
     */
    public static <T> MaybeTransformer<T, T> applyStandardMaybeTransformer(
            @NonNull RepositoryErrorHandler errorHandler,
            @NonNull String operationName,
            @Nullable String entityType) {
        
        return maybe -> maybe
                .subscribeOn(RxSchedulers.io())
                .observeOn(RxSchedulers.io())
                .doOnError(throwable -> errorHandler.handleError(throwable, operationName, entityType));
    }
    
    /**
     * Creates a standardized transformer for repository operations that emit a stream with backpressure.
     * This transformer applies proper threading, error handling, and backpressure strategy.
     *
     * @param <T> The type of items being emitted
     * @param errorHandler The repository error handler
     * @param operationName Human-readable name of the operation (for error reporting)
     * @param entityType Type of entity being operated on (for error reporting)
     * @return FlowableTransformer that applies standard repository patterns
     */
    public static <T> FlowableTransformer<T, T> applyStandardFlowableTransformer(
            @NonNull RepositoryErrorHandler errorHandler,
            @NonNull String operationName,
            @Nullable String entityType) {
        
        return flowable -> flowable
                .subscribeOn(RxSchedulers.io())
                .observeOn(RxSchedulers.io())
                .onBackpressureBuffer(1000, false, true)
                .doOnError(throwable -> errorHandler.handleError(throwable, operationName, entityType));
    }
    
    /**
     * Creates a standardized transformer for repository operations that need retry capabilities.
     * This transformer applies proper threading, error handling, and retry with exponential backoff.
     *
     * @param <T> The type of items being emitted
     * @param errorHandler The repository error handler
     * @param operationName Human-readable name of the operation (for error reporting)
     * @param entityType Type of entity being operated on (for error reporting)
     * @param maxRetries Maximum number of retries before giving up
     * @return ObservableTransformer that applies standard repository patterns with retry
     */
    public static <T> ObservableTransformer<T, T> applyStandardRetryTransformer(
            @NonNull RepositoryErrorHandler errorHandler,
            @NonNull String operationName,
            @Nullable String entityType,
            int maxRetries) {
        
        return observable -> observable
                .subscribeOn(RxSchedulers.io())
                .observeOn(RxSchedulers.io())
                .retry((retryCount, throwable) -> {
                    // Only retry if we haven't exceeded max retries and error handler says we should retry
                    ErrorInfo errorInfo = errorHandler.handleError(throwable, operationName, entityType);
                    return retryCount < maxRetries && errorInfo.shouldRetry();
                })
                .doOnError(throwable -> errorHandler.handleError(throwable, operationName, entityType));
    }
    
    /**
     * Creates a standardized transformer for repository operations that need retry capabilities.
     * This transformer applies proper threading, error handling, and retry with exponential backoff.
     *
     * @param errorHandler The repository error handler
     * @param operationName Human-readable name of the operation (for error reporting)
     * @param entityType Type of entity being operated on (for error reporting)
     * @param maxRetries Maximum number of retries before giving up
     * @return CompletableTransformer that applies standard repository patterns with retry
     */
    public static CompletableTransformer applyStandardRetryTransformerForCompletable(
            @NonNull RepositoryErrorHandler errorHandler,
            @NonNull String operationName,
            @Nullable String entityType,
            int maxRetries) {
        
        return completable -> completable
                .subscribeOn(RxSchedulers.io())
                .observeOn(RxSchedulers.io())
                .retry((retryCount, throwable) -> {
                    // Only retry if we haven't exceeded max retries and error handler says we should retry
                    ErrorInfo errorInfo = errorHandler.handleError(throwable, operationName, entityType);
                    return retryCount < maxRetries && errorInfo.shouldRetry();
                })
                .doOnError(throwable -> errorHandler.handleError(throwable, operationName, entityType));
    }
    
    /**
     * Creates a standardized transformer for repository single operations that need retry capabilities.
     * This transformer applies proper threading, error handling, and retry with exponential backoff.
     *
     * @param <T> The type of item being emitted
     * @param errorHandler The repository error handler
     * @param operationName Human-readable name of the operation (for error reporting)
     * @param entityType Type of entity being operated on (for error reporting)
     * @param maxRetries Maximum number of retries before giving up
     * @return SingleTransformer that applies standard repository patterns with retry
     */
    public static <T> SingleTransformer<T, T> applyStandardRetryTransformerForSingle(
            @NonNull RepositoryErrorHandler errorHandler,
            @NonNull String operationName,
            @Nullable String entityType,
            int maxRetries) {
        
        return single -> single
                .subscribeOn(RxSchedulers.io())
                .observeOn(RxSchedulers.io())
                .retry((retryCount, throwable) -> {
                    // Only retry if we haven't exceeded max retries and error handler says we should retry
                    ErrorInfo errorInfo = errorHandler.handleError(throwable, operationName, entityType);
                    return retryCount < maxRetries && errorInfo.shouldRetry();
                })
                .doOnError(throwable -> errorHandler.handleError(throwable, operationName, entityType));
    }
    
    /**
     * Helper method to get an item from cache first, then from a source Single if not in cache.
     *
     * @param <K> The type of cache key
     * @param <T> The type of item being retrieved
     * @param cacheStrategy The cache strategy to use
     * @param cacheKey The key to use for caching
     * @param sourceProducer Function that produces a Single to fetch the item if not in cache
     * @return Single that emits the item from cache or source
     */
    public static <K, T> Single<T> getWithCache(
            @NonNull CacheStrategy<K, T> cacheStrategy,
            @NonNull K cacheKey,
            @NonNull Function<K, Single<T>> sourceProducer) {
        
        return Single.defer(() -> {
            // First try cache
            T cachedValue = cacheStrategy.get(cacheKey);
            if (cachedValue != null) {
                return Single.just(cachedValue);
            }
            
            // If not in cache, get from source and cache it
            return sourceProducer.apply(cacheKey)
                    .doOnSuccess(value -> cacheStrategy.put(cacheKey, value));
        });
    }
    
    /**
     * Helper method to get an item from cache first, then from a source Single if not in cache,
     * with a specific cache TTL.
     *
     * @param <K> The type of cache key
     * @param <T> The type of item being retrieved
     * @param cacheStrategy The cache strategy to use
     * @param cacheKey The key to use for caching
     * @param sourceProducer Function that produces a Single to fetch the item if not in cache
     * @param ttl Time-to-live for the cache entry
     * @param ttlUnit Time unit for the ttl
     * @return Single that emits the item from cache or source
     */
    public static <K, T> Single<T> getWithCache(
            @NonNull CacheStrategy<K, T> cacheStrategy,
            @NonNull K cacheKey,
            @NonNull Function<K, Single<T>> sourceProducer,
            long ttl,
            @NonNull TimeUnit ttlUnit) {
        
        return Single.defer(() -> {
            // First try cache
            T cachedValue = cacheStrategy.get(cacheKey);
            if (cachedValue != null) {
                return Single.just(cachedValue);
            }
            
            // If not in cache, get from source and cache it
            return sourceProducer.apply(cacheKey)
                    .doOnSuccess(value -> cacheStrategy.put(cacheKey, value, ttl, ttlUnit));
        });
    }
    
    /**
     * Helper method to invalidate a cache entry after a write operation completes.
     *
     * @param <K> The type of cache key
     * @param completable The completable representing the write operation
     * @param cacheStrategy The cache strategy to use
     * @param cacheKeys The keys to invalidate
     * @return Completable that invalidates the cache after the operation completes
     */
    @SafeVarargs
    public static <K> Completable invalidateCacheAfterWrite(
            @NonNull Completable completable,
            @NonNull CacheStrategy<K, ?> cacheStrategy,
            @NonNull K... cacheKeys) {
        
        return completable.doOnComplete(() -> {
            for (K key : cacheKeys) {
                cacheStrategy.remove(key);
            }
        });
    }
}
