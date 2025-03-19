package com.autogratuity.data.repository.sync;

import android.util.Log;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Implements exponential backoff retry mechanism for sync operations.
 * <p>
 * This class provides a standardized approach to retrying failed operations with
 * an exponential backoff strategy, which increases the delay between retry attempts
 * to prevent overwhelming the system during periods of high load or transient failures.
 */
public class RetryWithBackoff {
    private static final String TAG = "RetryWithBackoff";
    
    // Default values
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final double DEFAULT_BACKOFF_FACTOR = 2.0;
    private static final long DEFAULT_INITIAL_DELAY_MS = 1000; // 1 second
    private static final long DEFAULT_MAX_DELAY_MS = 3600000;  // 1 hour
    
    // Configuration
    private final int maxRetries;
    private final double backoffFactor;
    private final long initialDelayMs;
    private final long maxDelayMs;
    
    /**
     * Creates a new RetryWithBackoff instance with default values.
     */
    public RetryWithBackoff() {
        this(DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_FACTOR, DEFAULT_INITIAL_DELAY_MS, DEFAULT_MAX_DELAY_MS);
    }
    
    /**
     * Creates a new RetryWithBackoff instance with custom values.
     *
     * @param maxRetries     Maximum number of retry attempts
     * @param backoffFactor  Factor by which to increase delay between retries (e.g., 2.0 for doubling)
     * @param initialDelayMs Initial delay in milliseconds before first retry
     * @param maxDelayMs     Maximum delay in milliseconds between retries
     */
    public RetryWithBackoff(int maxRetries, double backoffFactor, long initialDelayMs, long maxDelayMs) {
        this.maxRetries = maxRetries;
        this.backoffFactor = backoffFactor;
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs = maxDelayMs;
    }
    
    /**
     * Determines if another retry attempt should be made based on the current retry count.
     *
     * @param retryCount Current retry count
     * @return true if another retry should be attempted, false otherwise
     */
    public boolean shouldRetry(int retryCount) {
        return retryCount < maxRetries;
    }
    
    /**
     * Calculates the next retry delay using exponential backoff.
     *
     * @param retryCount Current retry count
     * @return Delay in milliseconds before next retry attempt
     */
    public long getNextDelayMs(int retryCount) {
        if (retryCount <= 0) {
            return initialDelayMs;
        }
        
        // Calculate exponential backoff delay
        long delay = (long) (initialDelayMs * Math.pow(backoffFactor, retryCount - 1));
        
        // Add some jitter (± 15%) to prevent synchronized retries
        double jitterFactor = 0.85 + (Math.random() * 0.3); // 0.85 to 1.15
        delay = (long) (delay * jitterFactor);
        
        // Ensure delay doesn't exceed max delay
        return Math.min(delay, maxDelayMs);
    }
    
    /**
     * Calculates the next retry time.
     *
     * @param retryCount Current retry count
     * @return Date representing when the next retry should occur
     */
    public Date getNextRetryTime(int retryCount) {
        return new Date(System.currentTimeMillis() + getNextDelayMs(retryCount));
    }
    
    /**
     * Logs information about the retry attempt.
     *
     * @param operationId Operation identifier
     * @param retryCount  Current retry count
     * @param error       Error that caused the retry
     */
    public void logRetryAttempt(String operationId, int retryCount, Throwable error) {
        long delayMs = getNextDelayMs(retryCount);
        Log.d(TAG, String.format("Scheduling retry %d/%d for operation %s in %d ms due to: %s",
                retryCount + 1, maxRetries, operationId, delayMs, error.getMessage()));
    }
    
    /**
     * Gets the maximum number of retries.
     *
     * @return Maximum number of retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }
    
    /**
     * Determines if the error is retryable based on error characteristics.
     * <p>
     * Some errors should not be retried (e.g., permission errors, invalid data),
     * while others are good candidates for retry (e.g., network timeouts, server overload).
     *
     * @param error Error to evaluate
     * @return true if the error should be retried, false otherwise
     */
    public boolean isRetryableError(Throwable error) {
        if (error == null) {
            return false;
        }
        
        // Network connectivity errors (generally retryable)
        if (error instanceof java.net.UnknownHostException || 
            error instanceof java.net.SocketTimeoutException ||
            error instanceof java.net.ConnectException ||
            error instanceof java.io.InterruptedIOException ||
            error instanceof java.net.SocketException) {
            return true;
        }
        
        // Check Firebase Firestore errors
        if (error instanceof com.google.firebase.firestore.FirebaseFirestoreException) {
            com.google.firebase.firestore.FirebaseFirestoreException firestoreError = 
                    (com.google.firebase.firestore.FirebaseFirestoreException) error;
            
            // Get the error code
            com.google.firebase.firestore.FirebaseFirestoreException.Code code = firestoreError.getCode();
            
            // These errors are retryable
            return code == com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE ||
                   code == com.google.firebase.firestore.FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ||
                   code == com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ||
                   code == com.google.firebase.firestore.FirebaseFirestoreException.Code.INTERNAL ||
                   code == com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED;
        }
        
        // Generic timeout errors
        String message = error.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            if (lowerMessage.contains("timeout") || 
                lowerMessage.contains("timed out") ||
                lowerMessage.contains("unavailable") ||
                lowerMessage.contains("throttl") ||
                lowerMessage.contains("too many requests") ||
                lowerMessage.contains("temporary failure") ||
                lowerMessage.contains("deadline exceeded") ||
                lowerMessage.contains("transient") ||
                lowerMessage.contains("overloaded") ||
                lowerMessage.contains("busy")) {
                return true;
            }
        }
        
        // Default to not retrying unknown errors
        return false;
    }
    
    /**
     * Sleeps for the calculated delay time. Useful for synchronous retry loops.
     *
     * @param retryCount Current retry count
     * @throws InterruptedException if the sleep is interrupted
     */
    public void sleep(int retryCount) throws InterruptedException {
        Thread.sleep(getNextDelayMs(retryCount));
    }
    
    /**
     * Creates an RxJava backoff delay function suitable for use with retryWhen.
     *
     * @return A function that returns an Observable that delays emission based on retry count
     */
    public io.reactivex.functions.Function<io.reactivex.Observable<Throwable>, io.reactivex.Observable<?>> asRxBackoffFunction() {
        return attempts -> attempts
                .zipWith(io.reactivex.Observable.range(1, maxRetries), (error, retryCount) -> {
                    // Check if error is retryable
                    if (!isRetryableError(error)) {
                        throw io.reactivex.exceptions.Exceptions.propagate(error);
                    }
                    
                    // Log retry attempt
                    logRetryAttempt("RxOperation", retryCount - 1, error);
                    
                    // Return retry count for delay calculation
                    return retryCount;
                })
                .flatMap(retryCount -> {
                    long delay = getNextDelayMs(retryCount - 1);
                    Log.d(TAG, "RxJava retry " + retryCount + " with delay " + delay + "ms");
                    return io.reactivex.Observable.timer(delay, TimeUnit.MILLISECONDS);
                });
    }
}