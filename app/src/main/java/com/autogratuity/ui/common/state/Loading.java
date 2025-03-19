package com.autogratuity.ui.common.state;

/**
 * Represents a loading state with no data.
 * This is used when an operation is in progress and data is being fetched.
 * 
 * @param <T> The type of data that will eventually be loaded
 */
public class Loading<T> extends ViewState<T> {
    
    /**
     * Get the data from this state
     * Loading states have no data, so this returns null
     * 
     * @return null since no data is available
     */
    @Override
    public T getData() {
        return null;
    }
    
    /**
     * Get the error from this state
     * Loading states have no error, so this returns null
     * 
     * @return null since no error is available
     */
    @Override
    public Throwable getError() {
        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Loading;
    }
    
    @Override
    public int hashCode() {
        return Loading.class.hashCode();
    }
    
    @Override
    public String toString() {
        return "Loading";
    }
}
