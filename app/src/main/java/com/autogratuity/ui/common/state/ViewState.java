package com.autogratuity.ui.common.state;

/**
 * Base class for representing UI state in a standardized way.
 * This enables consistent state management across ViewModels and UI components.
 * 
 * @param <T> The type of data this state wraps
 */
public abstract class ViewState<T> {
    
    /**
     * Create a loading state with no data
     * 
     * @param <T> The type of data that will eventually be loaded
     * @return Loading state instance
     */
    public static <T> ViewState<T> loading() {
        return new Loading<>();
    }
    
    /**
     * Create a success state with data
     * 
     * @param data The data to include in the state
     * @param <T> The type of data
     * @return Success state instance with data
     */
    public static <T> ViewState<T> success(T data) {
        return new Success<>(data);
    }
    
    /**
     * Create an error state with exception
     * 
     * @param error The exception representing the error
     * @param <T> The type of data that was being loaded
     * @return Error state instance
     */
    public static <T> ViewState<T> error(Throwable error) {
        return new Error<>(error);
    }
    
    /**
     * Create an error state with message
     * 
     * @param message The error message
     * @param <T> The type of data that was being loaded
     * @return Error state instance
     */
    public static <T> ViewState<T> error(String message) {
        return new Error<>(new Exception(message));
    }
    
    /**
     * Check if this state is a loading state
     * 
     * @return true if this is a loading state
     */
    public boolean isLoading() {
        return this instanceof Loading;
    }
    
    /**
     * Check if this state is a success state
     * 
     * @return true if this is a success state
     */
    public boolean isSuccess() {
        return this instanceof Success;
    }
    
    /**
     * Check if this state is an error state
     * 
     * @return true if this is an error state
     */
    public boolean isError() {
        return this instanceof Error;
    }
    
    /**
     * Get the data from this state if available
     * 
     * @return The data, or null if not available
     */
    public abstract T getData();
    
    /**
     * Get the error from this state if available
     * 
     * @return The error, or null if not available
     */
    public abstract Throwable getError();
}
