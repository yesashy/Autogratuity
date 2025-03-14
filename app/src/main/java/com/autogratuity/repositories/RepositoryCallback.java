package com.autogratuity.repositories;

/**
 * Generic callback interface for repository operations
 * Provides unified error handling for asynchronous operations
 *
 * @param <T> The type of result expected from the operation
 */
public interface RepositoryCallback<T> {
    
    /**
     * Called when the operation completes successfully
     *
     * @param result The result of the operation
     */
    void onSuccess(T result);
    
    /**
     * Called when the operation fails
     *
     * @param e The exception that caused the failure
     */
    void onError(Exception e);
}
