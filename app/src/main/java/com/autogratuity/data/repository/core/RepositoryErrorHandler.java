package com.autogratuity.data.repository.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.autogratuity.data.model.ErrorInfo;
import com.autogratuity.data.model.SyncStatus;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Standard error handling utility for repositories that provides consistent
 * error handling, logging, propagation, and recovery across the application.
 */
public class RepositoryErrorHandler {
    private static final String TAG = "RepoErrorHandler";

    // Last error tracking
    private final BehaviorSubject<ErrorInfo> lastErrorSubject;
    
    // Sync status tracking
    private final BehaviorSubject<SyncStatus> syncStatusSubject;
    
    // Retry configuration
    private final int maxRetries;
    private final long initialRetryDelayMs;
    private final float retryBackoffFactor;
    
    /**
     * Constructor with default retry settings
     * 
     * @param syncStatusSubject Subject for tracking sync status
     */
    public RepositoryErrorHandler(@NonNull BehaviorSubject<SyncStatus> syncStatusSubject) {
        this(syncStatusSubject, 3, 1000, 2.0f);
    }
    
    /**
     * Constructor with custom retry settings
     * 
     * @param syncStatusSubject Subject for tracking sync status
     * @param maxRetries Maximum number of retries before giving up
     * @param initialRetryDelayMs Initial delay in milliseconds before first retry
     * @param retryBackoffFactor Factor by which to increase delay for each retry
     */
    public RepositoryErrorHandler(
            @NonNull BehaviorSubject<SyncStatus> syncStatusSubject,
            int maxRetries,
            long initialRetryDelayMs,
            float retryBackoffFactor) {
        this.syncStatusSubject = syncStatusSubject;
        this.lastErrorSubject = BehaviorSubject.create();
        this.maxRetries = maxRetries;
        this.initialRetryDelayMs = initialRetryDelayMs;
        this.retryBackoffFactor = retryBackoffFactor;
    }
    
    /**
     * Handle an error by converting it to an ErrorInfo, logging it, and updating
     * the sync status.
     * 
     * @param error The error to handle
     * @param operationName Description of the operation that failed
     * @param entityType Type of entity being operated on (e.g., "user", "delivery")
     * @return ErrorInfo representing the handled error
     */
    public ErrorInfo handleError(@NonNull Throwable error, @NonNull String operationName, @Nullable String entityType) {
        // Create standardized error info
        ErrorInfo errorInfo = createErrorInfo(error, operationName, entityType);
        
        // Log the error
        logError(errorInfo, error);
        
        // Update the sync status
        updateSyncStatus(errorInfo);
        
        // Track as last error
        lastErrorSubject.onNext(errorInfo);
        
        return errorInfo;
    }
    
    /**
     * Create a standardized ErrorInfo from an exception
     * 
     * @param error The exception to convert
     * @param operationName Description of the operation that failed
     * @param entityType Type of entity being operated on
     * @return Standardized ErrorInfo object
     */
    public ErrorInfo createErrorInfo(@NonNull Throwable error, @NonNull String operationName, @Nullable String entityType) {
        // Convert the error to a standardized ErrorInfo
        ErrorInfo errorInfo;
        
        if (error instanceof FirebaseNetworkException || 
            error instanceof UnknownHostException || 
            error instanceof ConnectException) {
            // Network errors
            errorInfo = ErrorInfo.createNetworkError("Network error during " + operationName);
        } else if (error instanceof FirebaseAuthException) {
            // Authentication errors
            errorInfo = ErrorInfo.createAuthError("Authentication error during " + operationName);
        } else if (error instanceof FirebaseFirestoreException) {
            // Firestore errors
            errorInfo = ErrorInfo.createServerError("Database error during " + operationName);
        } else if (error instanceof SocketTimeoutException || 
                  error instanceof TimeoutException) {
            // Timeout errors
            errorInfo = ErrorInfo.createTimeoutError("Operation timed out: " + operationName);
        } else if (error instanceof IllegalArgumentException) {
            // Validation errors
            errorInfo = ErrorInfo.createValidationError("Invalid input for " + operationName);
        } else {
            // Other errors
            errorInfo = ErrorInfo.fromThrowable(error);
        }
        
        // Add additional details
        if (entityType != null) {
            errorInfo.addDetail("entityType", entityType);
        }
        errorInfo.addDetail("operation", operationName);
        
        return errorInfo;
    }
    
    /**
     * Log an error with appropriate formatting and details
     * 
     * @param errorInfo Standardized error info
     * @param originalError Original exception
     */
    private void logError(@NonNull ErrorInfo errorInfo, @NonNull Throwable originalError) {
        String errorPrefix = "[" + errorInfo.getCode() + "] ";
        String entityType = (String) errorInfo.getDetails().get("entityType");
        String operation = (String) errorInfo.getDetails().get("operation");
        
        StringBuilder logMessage = new StringBuilder(errorPrefix);
        if (entityType != null) {
            logMessage.append(entityType).append(": ");
        }
        logMessage.append(operation).append(" - ");
        logMessage.append(errorInfo.getMessage());
        
        if (errorInfo.isCritical()) {
            Log.e(TAG, logMessage.toString(), originalError);
        } else {
            Log.w(TAG, logMessage.toString(), originalError);
        }
    }
    
    /**
     * Update the sync status with error information
     * 
     * @param errorInfo The error information
     */
    private void updateSyncStatus(@NonNull ErrorInfo errorInfo) {
        SyncStatus currentStatus = syncStatusSubject.getValue();
        if (currentStatus != null) {
            currentStatus.setError(errorInfo.getMessage());
            currentStatus.setLastErrorTime(errorInfo.getTimestamp());
            syncStatusSubject.onNext(currentStatus);
        }
    }
    
    /**
     * Apply standard error handling to a Completable
     * 
     * @param source The source Completable
     * @param operationName Description of the operation
     * @param entityType Type of entity being operated on
     * @return Completable with standardized error handling
     */
    public Completable handleCompletable(@NonNull Completable source, @NonNull String operationName, @Nullable String entityType) {
        return source.doOnError(error -> handleError(error, operationName, entityType));
    }
    
    /**
     * Apply standard error handling to a Completable with retries
     * 
     * @param source The source Completable
     * @param operationName Description of the operation
     * @param entityType Type of entity being operated on
     * @return Completable with standardized error handling and retries
     */
    public Completable handleCompletableWithRetry(@NonNull Completable source, @NonNull String operationName, @Nullable String entityType) {
        return source
                .retry((retryCount, error) -> shouldRetry(retryCount, error))
                .doOnError(error -> handleError(error, operationName, entityType));
    }
    
    /**
     * Apply standard error handling to a Single
     * 
     * @param source The source Single
     * @param operationName Description of the operation
     * @param entityType Type of entity being operated on
     * @param <T> The type of object emitted by the Single
     * @return Single with standardized error handling
     */
    public <T> Single<T> handleSingle(@NonNull Single<T> source, @NonNull String operationName, @Nullable String entityType) {
        return source.doOnError(error -> handleError(error, operationName, entityType));
    }
    
    /**
     * Apply standard error handling to a Single with retries
     * 
     * @param source The source Single
     * @param operationName Description of the operation
     * @param entityType Type of entity being operated on
     * @param <T> The type of object emitted by the Single
     * @return Single with standardized error handling and retries
     */
    public <T> Single<T> handleSingleWithRetry(@NonNull Single<T> source, @NonNull String operationName, @Nullable String entityType) {
        return source
                .retry((retryCount, error) -> shouldRetry(retryCount, error))
                .doOnError(error -> handleError(error, operationName, entityType));
    }
    
    /**
     * Apply standard error handling to an Observable
     * 
     * @param source The source Observable
     * @param operationName Description of the operation
     * @param entityType Type of entity being operated on
     * @param <T> The type of object emitted by the Observable
     * @return Observable with standardized error handling
     */
    public <T> Observable<T> handleObservable(@NonNull Observable<T> source, @NonNull String operationName, @Nullable String entityType) {
        return source.doOnError(error -> handleError(error, operationName, entityType));
    }
    
    /**
     * Determine if an operation should be retried based on the error and retry count
     * 
     * @param retryCount Current retry count
     * @param error The error that occurred
     * @return true if the operation should be retried, false otherwise
     */
    private boolean shouldRetry(int retryCount, Throwable error) {
        // Don't retry if we've exceeded the maximum retry count
        if (retryCount >= maxRetries) {
            return false;
        }
        
        // Don't retry for client errors (e.g., validation errors)
        if (error instanceof IllegalArgumentException || 
            error instanceof IllegalStateException) {
            return false;
        }
        
        // Don't retry authentication errors
        if (error instanceof FirebaseAuthException) {
            return false;
        }
        
        // Retry for network and server errors
        if (error instanceof FirebaseNetworkException || 
            error instanceof FirebaseFirestoreException ||
            error instanceof UnknownHostException || 
            error instanceof ConnectException ||
            error instanceof SocketTimeoutException ||
            error instanceof TimeoutException) {
            return true;
        }
        
        // Retry for Firebase service interruptions
        if (error instanceof FirebaseException || 
            error instanceof FirebaseApiNotAvailableException) {
            return true;
        }
        
        // Retry for I/O errors
        if (error instanceof IOException) {
            return true;
        }
        
        // Don't retry for other errors
        return false;
    }
    
    /**
     * Calculate the retry delay based on the retry count
     * 
     * @param retryCount Current retry count
     * @return Delay in milliseconds
     */
    public long getRetryDelayForAttempt(int retryCount) {
        return (long) (initialRetryDelayMs * Math.pow(retryBackoffFactor, retryCount));
    }
    
    /**
     * Get the observable of the last error
     * 
     * @return Observable that emits when a new error occurs
     */
    public Observable<ErrorInfo> getLastErrorObservable() {
        return lastErrorSubject.hide();
    }
}
