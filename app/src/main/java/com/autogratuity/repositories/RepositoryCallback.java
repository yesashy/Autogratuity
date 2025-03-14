package com.autogratuity.repositories;

import java.util.List;

/**
 * Generic callback interface for repository operations
 * @param <T> Type of data being returned
 */
public interface RepositoryCallback<T> {
    /**
     * Called when operation completes successfully
     * @param result Result of the operation
     */
    void onSuccess(T result);

    /**
     * Called when operation fails
     * @param exception Exception that occurred
     */
    void onError(Exception exception);
}