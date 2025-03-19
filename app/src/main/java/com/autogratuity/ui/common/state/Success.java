package com.autogratuity.ui.common.state;

/**
 * Represents a success state with data.
 * This is used when an operation has completed successfully and data is available.
 * 
 * @param <T> The type of data this state contains
 */
public class Success<T> extends ViewState<T> {
    
    private final T data;
    
    /**
     * Create a success state with data
     * 
     * @param data The data to include in the state
     */
    public Success(T data) {
        this.data = data;
    }
    
    /**
     * Get the data from this state
     * 
     * @return The data
     */
    @Override
    public T getData() {
        return data;
    }
    
    /**
     * Get the error from this state
     * Success states have no error, so this returns null
     * 
     * @return null since no error is available
     */
    @Override
    public Throwable getError() {
        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Success)) {
            return false;
        }
        
        Success<?> other = (Success<?>) obj;
        if (data == null) {
            return other.data == null;
        }
        
        return data.equals(other.data);
    }
    
    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("Success[%s]", data);
    }
}
