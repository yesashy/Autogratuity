package com.autogratuity.data.repository.core;

import com.autogratuity.data.model.ErrorInfo;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Repository Contract Interface
 * 
 * This interface defines the standardized contract for all repositories in the application.
 * It establishes consistent method naming conventions, return types, error handling,
 * caching behaviors, and documentation requirements.
 * 
 * Domain-specific repositories should implement the relevant sub-interfaces based on
 * their functionality requirements.
 */
public interface RepositoryContract {

    /**
     * Read Operations Interface
     * 
     * Defines contract for read operations in repositories.
     * 
     * Naming Conventions:
     * - get*(): For direct entity retrieval
     * - get*ById(): For retrieval by ID
     * - get*s(): For retrieving collections of entities
     * - find*By*(): For conditional retrieval
     * 
     * Return Types:
     * - Single<T>: For single entity retrieval
     * - Single<List<T>>: For collections of entities
     * 
     * Error Handling:
     * - All methods must use standardized error handling through RepositoryErrorHandler
     * - Network errors must be properly mapped to appropriate ErrorInfo instances
     * 
     * Caching:
     * - Should use standardized caching methods such as getWithCache()
     * - Cache keys must follow RepositoryConstants patterns
     * 
     * @param <T> The entity type
     */
    interface ReadOperations<T> {
        /**
         * Retrieve a single entity.
         * 
         * @return Single emitting the entity
         */
        Single<T> get();
        
        /**
         * Retrieve a single entity with cache control.
         * 
         * @param forceRefresh If true, bypasses cache and fetches from source
         * @return Single emitting the entity
         */
        Single<T> get(boolean forceRefresh);
        
        /**
         * Retrieve an entity by its ID.
         * 
         * @param id Entity ID
         * @return Single emitting the entity
         */
        Single<T> getById(String id);
        
        /**
         * Retrieve all entities of this type.
         * 
         * @return Single emitting a list of entities
         */
        Single<List<T>> getAll();
        
        /**
         * Retrieve entities with pagination.
         * 
         * @param limit Maximum number of entities to retrieve
         * @param startAfter Reference to start after (for pagination)
         * @return Single emitting a list of entities
         */
        Single<List<T>> getAll(int limit, DocumentReference startAfter);
    }
    
    /**
     * Write Operations Interface
     * 
     * Defines contract for write operations in repositories.
     * 
     * Naming Conventions:
     * - add*(): For creating new entities
     * - update*(): For updating existing entities
     * - delete*(): For removing entities
     * - set*(): For setting specific flags or properties
     * 
     * Return Types:
     * - Single<DocumentReference>: For add operations
     * - Completable: For update and delete operations
     * 
     * Error Handling:
     * - All methods must use standardized error handling through RepositoryErrorHandler
     * - Write errors must be properly mapped to appropriate ErrorInfo instances
     * - Conflict resolution strategies must be documented
     * 
     * Synchronization:
     * - All write operations should use enqueueSyncOperation for offline support
     * - Operations must include proper metadata (timestamps, user ID)
     * 
     * @param <T> The entity type
     */
    interface WriteOperations<T> {
        /**
         * Add a new entity.
         * 
         * @param entity The entity to add
         * @return Single emitting the document reference
         */
        Single<DocumentReference> add(T entity);
        
        /**
         * Update an existing entity.
         * 
         * @param entity The entity to update
         * @return Completable that completes when update is finished
         */
        Completable update(T entity);
        
        /**
         * Update specific fields of an entity.
         * 
         * @param id Entity ID
         * @param fields Map of field names to values to update
         * @return Completable that completes when update is finished
         */
        Completable updateFields(String id, Map<String, Object> fields);
        
        /**
         * Delete an entity.
         * 
         * @param id Entity ID to delete
         * @return Completable that completes when deletion is finished
         */
        Completable delete(String id);
    }
    
    /**
     * Observe Operations Interface
     * 
     * Defines contract for observe operations in repositories.
     * 
     * Naming Conventions:
     * - observe*(): For observing a single entity
     * - observe*s(): For observing collections of entities
     * 
     * Return Types:
     * - Observable<T>: For single entity observation
     * - Observable<List<T>>: For collections of entities
     * 
     * Error Handling:
     * - All methods must use standardized error handling through RepositoryErrorHandler
     * - Observation errors must not terminate the Observable
     * 
     * Lifecycle Management:
     * - Proper Firestore listener cleanup must be implemented
     * - Backpressure handling must be documented
     * 
     * @param <T> The entity type
     */
    interface ObserveOperations<T> {
        /**
         * Observe changes to a single entity in real-time.
         * 
         * @return Observable emitting updates to the entity
         */
        Observable<T> observe();
        
        /**
         * Observe changes to a specific entity by ID in real-time.
         * 
         * @param id Entity ID to observe
         * @return Observable emitting updates to the entity
         */
        Observable<T> observeById(String id);
        
        /**
         * Observe changes to all entities of this type in real-time.
         * 
         * @return Observable emitting updates to the entity list
         */
        Observable<List<T>> observeAll();
    }
    
    /**
     * Cache Operations Interface
     * 
     * Defines contract for cache management in repositories.
     * 
     * Cache Behaviors:
     * - All repositories must implement a standard caching strategy
     * - Cache keys must follow conventions in RepositoryConstants
     * - Cache invalidation must be properly handled during write operations
     */
    interface CacheOperations {
        /**
         * Clear all in-memory caches for this repository.
         * 
         * @return Completable that completes when caches are cleared
         */
        Completable clearCache();
        
        /**
         * Invalidate cache for a specific entity.
         * 
         * @param id Entity ID to invalidate
         * @return Completable that completes when cache is invalidated
         */
        Completable invalidateCache(String id);
        
        /**
         * Prefetch entities for improved performance.
         * 
         * @param limit Maximum number of entities to prefetch
         * @return Completable that completes when prefetching is finished
         */
        Completable prefetch(int limit);
    }
    
    /**
     * Error Handling Interface
     * 
     * Defines contract for error handling in repositories.
     * 
     * Error Handling Requirements:
     * - All repository methods must use standardized error handling
     * - Errors must be mapped to appropriate ErrorInfo instances
     * - Retry strategies must be consistent
     */
    interface ErrorHandling {
        /**
         * Handle a repository error with standard approach.
         * 
         * @param error The exception
         * @param operation Description of the operation that failed
         * @param entityType Type of entity being operated on
         * @return ErrorInfo representing the handled error
         */
        ErrorInfo handleError(Throwable error, String operation, String entityType);
        
        /**
         * Apply standardized retry strategy to operations.
         * 
         * @param maxRetries Maximum number of retry attempts
         * @param backoffFactor Factor to increase delay between retries
         * @return Completable transformer for retry strategy
         */
        Completable.Transformer applyRetryStrategy(int maxRetries, double backoffFactor);
    }
    
    /**
     * Transaction Operations Interface
     * 
     * Defines contract for transaction operations in repositories.
     * 
     * Transaction Requirements:
     * - Must support atomic operations across multiple entities
     * - Must handle rollback scenarios
     * - Must support proper error propagation
     * 
     * @param <T> The result type
     */
    interface TransactionOperations<T> {
        /**
         * Execute a read-write transaction.
         * 
         * @param transaction Function to execute within the transaction
         * @return Single emitting the transaction result
         */
        Single<T> executeTransaction(Function<Transaction, T> transaction);
        
        /**
         * Transaction context for repository operations.
         */
        interface Transaction {
            /**
             * Get an entity within a transaction.
             * 
             * @param id Entity ID
             * @param type Class type for deserialization
             * @param <E> Entity type
             * @return The entity
             * @throws Exception If retrieval fails
             */
            <E> E get(String id, Class<E> type) throws Exception;
            
            /**
             * Set entity data within a transaction.
             * 
             * @param id Entity ID
             * @param data Entity data
             * @throws Exception If write fails
             */
            void set(String id, Object data) throws Exception;
            
            /**
             * Update fields within a transaction.
             * 
             * @param id Entity ID
             * @param fields Map of field names to values
             * @throws Exception If update fails
             */
            void update(String id, Map<String, Object> fields) throws Exception;
            
            /**
             * Delete an entity within a transaction.
             * 
             * @param id Entity ID
             * @throws Exception If deletion fails
             */
            void delete(String id) throws Exception;
        }
        
        /**
         * Function to execute within a transaction.
         * 
         * @param <T> Result type
         */
        interface Function<T> {
            /**
             * Apply the function within a transaction.
             * 
             * @param transaction Transaction context
             * @return Transaction result
             * @throws Exception If transaction fails
             */
            T apply(Transaction transaction) throws Exception;
        }
    }
}
