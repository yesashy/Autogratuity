package com.autogratuity.ui.common;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Base ViewModel class with common functionality for all ViewModels
 */
public abstract class BaseViewModel extends ViewModel {
    
    protected final CompositeDisposable disposables = new CompositeDisposable();
    protected final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    protected final MutableLiveData<Throwable> errorLiveData = new MutableLiveData<>();
    protected final MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();
    
    /**
     * Get loading state as LiveData
     * 
     * @return LiveData for loading state
     */
    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }
    
    /**
     * Get error state as LiveData
     * 
     * @return LiveData for error
     */
    public LiveData<Throwable> getError() {
        return errorLiveData;
    }
    
    /**
     * Get toast message as LiveData
     * 
     * @return LiveData for toast message
     */
    public LiveData<String> getToastMessage() {
        return toastMessageLiveData;
    }
    
    /**
     * Set loading state
     * 
     * @param isLoading true if loading, false otherwise
     */
    protected void setLoading(boolean isLoading) {
        loadingLiveData.setValue(isLoading);
    }
    
    /**
     * Set error state
     * 
     * @param error Throwable error object
     */
    protected void setError(Throwable error) {
        errorLiveData.setValue(error);
    }
    
    /**
     * Clear error state
     */
    protected void clearError() {
        errorLiveData.setValue(null);
    }
    
    /**
     * Show toast message
     * 
     * @param message Message to show
     */
    protected void showToast(String message) {
        toastMessageLiveData.setValue(message);
    }
    
    /**
     * Clean up resources when ViewModel is no longer used
     */
    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
