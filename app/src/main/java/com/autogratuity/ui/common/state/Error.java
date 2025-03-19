package com.autogratuity.ui.common.state;

import com.autogratuity.data.model.ErrorInfo;

/**
 * Represents an error state.
 * This is used when an operation has failed and an error is available.
 * 
 * @param <T> The type of data that was being loaded
 */
public class Error<T> extends ViewState<T> {
    
    private final Throwable error;
    
    /**
     * Create an error state with an exception
     * 
     * @param error The exception representing the error
     */
    public Error(Throwable error) {
        this.error = error;
    }
    
    /**
     * Get the data from this state
     * Error states have no data, so this returns null
     * 
     * @return null since no data is available
     */
    @Override
    public T getData() {
        return null;
    }
    
    /**
     * Get the error from this state
     * 
     * @return The error
     */
    @Override
    public Throwable getError() {
        return error;
    }
    
    /**
     * Get a user-friendly error message
     * 
     * @return User-friendly error message
     */
    public String getErrorMessage() {
        if (error == null) {
            return "Unknown error";
        }
        
        // Check if error message is from ErrorInfo
        if (error.getCause() instanceof Exception && 
            error.getCause().getMessage() != null && 
            !error.getCause().getMessage().isEmpty()) {
            return error.getCause().getMessage();
        }
        
        return error.getMessage() != null ? error.getMessage() : "Unknown error";
    }
    
    /**
     * Check if the error is recoverable
     * 
     * @return true if the error is recoverable
     */
    public boolean isRecoverable() {
        // Network errors and timeouts are usually recoverable
        if (error instanceof java.net.UnknownHostException || 
            error instanceof java.net.SocketTimeoutException ||
            error instanceof java.io.InterruptedIOException) {
            return true;
        }
        
        // Check if the error contains ErrorInfo with recovery action
        if (error.getCause() instanceof Exception && 
            error.getCause().getMessage() != null && 
            error.getCause().getMessage().contains(ErrorInfo.RECOVERY_RETRY)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Error)) {
            return false;
        }
        
        Error<?> other = (Error<?>) obj;
        if (error == null) {
            return other.error == null;
        }
        
        return error.equals(other.error);
    }
    
    @Override
    public int hashCode() {
        return error != null ? error.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("Error[%s]", getErrorMessage());
    }
}
