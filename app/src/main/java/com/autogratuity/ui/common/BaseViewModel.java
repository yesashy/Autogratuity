package com.autogratuity.ui.common;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.autogratuity.data.model.ErrorInfo;
import com.autogratuity.data.util.RxSchedulers;
import com.autogratuity.ui.common.state.ViewState;
import com.autogratuity.ui.common.LiveDataTransformer;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import com.autogratuity.ui.common.DisposableLifecycleManager;

/**
 * Enhanced BaseViewModel class with standardized patterns for error handling,
 * state management, and lifecycle integration.
 */
public abstract class BaseViewModel extends ViewModel {
    
    protected final CompositeDisposable disposables = new CompositeDisposable();
    protected final DisposableLifecycleManager disposableManager = new DisposableLifecycleManager();
    protected final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    protected final MutableLiveData<Throwable> errorLiveData = new MutableLiveData<>();
    protected final MutableLiveData<ErrorInfo> errorInfoLiveData = new MutableLiveData<>();
    protected final MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();
    
    //-----------------------------------------------------------------------------------
    // Legacy LiveData accessors (for backwards compatibility)
    //-----------------------------------------------------------------------------------
    
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
     * Get standardized error information as LiveData
     * 
     * @return LiveData for ErrorInfo
     */
    public LiveData<ErrorInfo> getErrorInfo() {
        return errorInfoLiveData;
    }
    
    /**
     * Get toast message as LiveData
     * 
     * @return LiveData for toast message
     */
    public LiveData<String> getToastMessage() {
        return toastMessageLiveData;
    }
    
    //-----------------------------------------------------------------------------------
    // Enhanced state management with ViewState
    //-----------------------------------------------------------------------------------
    
    /**
     * Create a LiveData that transforms results into ViewState objects
     * 
     * @param source The source LiveData to transform
     * @param <T> The type of data
     * @return LiveData of ViewState containing the data or error
     */
    protected <T> LiveData<ViewState<T>> createViewStateLiveData(LiveData<T> source) {
        return Transformations.map(source, data -> {
            if (data == null && errorLiveData.getValue() != null) {
                return ViewState.error(errorLiveData.getValue());
            } else if (data == null && errorInfoLiveData.getValue() != null) {
                return ViewState.error(new Exception(errorInfoLiveData.getValue().getMessage()));
            } else if (loadingLiveData.getValue() != null && loadingLiveData.getValue()) {
                return ViewState.loading();
            } else {
                return ViewState.success(data);
            }
        });
    }
    
    /**
     * Create a new ViewState LiveData
     * 
     * @param <T> Type of data in the ViewState
     * @return MutableLiveData of ViewState
     */
    protected <T> MutableLiveData<ViewState<T>> createViewStateLiveData() {
        return new MutableLiveData<>(ViewState.loading());
    }
    
    /**
     * Set ViewState to loading
     * 
     * @param liveData LiveData to update
     * @param <T> Type of data in the ViewState
     */
    protected <T> void setLoading(MutableLiveData<ViewState<T>> liveData) {
        liveData.setValue(ViewState.loading());
        setLoading(true);
    }
    
    /**
     * Set ViewState to success with data
     * 
     * @param liveData LiveData to update
     * @param data Data to include in the success state
     * @param <T> Type of data
     */
    protected <T> void setSuccess(MutableLiveData<ViewState<T>> liveData, T data) {
        liveData.setValue(ViewState.success(data));
        setLoading(false);
        clearError();
    }
    
    /**
     * Set ViewState to error with exception
     * 
     * @param liveData LiveData to update
     * @param error Error to include in the error state
     * @param <T> Type of data
     */
    protected <T> void setError(MutableLiveData<ViewState<T>> liveData, Throwable error) {
        liveData.setValue(ViewState.error(error));
        setError(error);
        setLoading(false);
    }
    
    /**
     * Set ViewState to error with ErrorInfo
     * 
     * @param liveData LiveData to update
     * @param errorInfo ErrorInfo to include in the error state
     * @param <T> Type of data
     */
    protected <T> void setError(MutableLiveData<ViewState<T>> liveData, ErrorInfo errorInfo) {
        liveData.setValue(ViewState.error(new Exception(errorInfo.getMessage())));
        setError(errorInfo);
        setLoading(false);
    }
    
    //-----------------------------------------------------------------------------------
    // Enhanced error handling with ErrorInfo
    //-----------------------------------------------------------------------------------
    
    /**
     * Set loading state
     * 
     * @param isLoading true if loading, false otherwise
     */
    protected void setLoading(boolean isLoading) {
        loadingLiveData.setValue(isLoading);
    }
    
    /**
     * Set error state with a Throwable
     * Also creates and sets a corresponding ErrorInfo
     * 
     * @param error Throwable error object
     */
    protected void setError(Throwable error) {
        errorLiveData.setValue(error);
        
        // Convert Throwable to ErrorInfo for standardized error handling
        if (error != null) {
            errorInfoLiveData.setValue(ErrorInfo.fromThrowable(error));
        } else {
            errorInfoLiveData.setValue(null);
        }
    }
    
    /**
     * Set error state with ErrorInfo
     * 
     * @param errorInfo Standardized error information
     */
    protected void setError(ErrorInfo errorInfo) {
        errorInfoLiveData.setValue(errorInfo);
        
        // For backwards compatibility, also set Throwable
        if (errorInfo != null) {
            errorLiveData.setValue(new Exception(errorInfo.getMessage()));
        } else {
            errorLiveData.setValue(null);
        }
    }
    
    /**
     * Clear error state
     */
    protected void clearError() {
        errorLiveData.setValue(null);
        errorInfoLiveData.setValue(null);
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
     * Create a network error and set as current error
     * 
     * @param message Error message
     */
    protected void setNetworkError(String message) {
        setError(ErrorInfo.createNetworkError(message));
    }
    
    /**
     * Create a server error and set as current error
     * 
     * @param message Error message
     */
    protected void setServerError(String message) {
        setError(ErrorInfo.createServerError(message));
    }
    
    /**
     * Create a validation error and set as current error
     * 
     * @param message Error message
     */
    protected void setValidationError(String message) {
        setError(ErrorInfo.createValidationError(message));
    }
    
    //-----------------------------------------------------------------------------------
    // Enhanced disposable management with error handling
    //-----------------------------------------------------------------------------------
    
    /**
     * Add a disposable to be managed by this ViewModel
     * 
     * @param disposable Disposable to add
     * @return The added disposable for chaining
     */
    protected Disposable addDisposable(Disposable disposable) {
        disposables.add(disposable);
        return disposableManager.add(disposable);
    }
    
    /**
     * Add a disposable with a specific tag for grouped management
     * 
     * @param tag Tag identifier for the disposable group
     * @param disposable Disposable to add
     * @return The added disposable for chaining
     */
    protected Disposable addDisposable(String tag, Disposable disposable) {
        disposables.add(disposable); // For backwards compatibility
        return disposableManager.add(tag, disposable);
    }
    
    /**
     * Remove and dispose all disposables with the given tag
     * 
     * @param tag Tag identifier to clear
     */
    protected void clearDisposables(String tag) {
        disposableManager.clear(tag);
    }
    
    /**
     * Check if a specific tag group has active disposables
     * 
     * @param tag Tag identifier to check
     * @return true if the tag group exists and is not empty
     */
    protected boolean hasActiveDisposables(String tag) {
        return disposableManager.hasActiveDisposables(tag);
    }
    
    //-----------------------------------------------------------------------------------
    // RxJava to LiveData transformation methods using LiveDataTransformer
    //-----------------------------------------------------------------------------------
    
    /**
     * Transform a Single to LiveData with ViewState for state tracking.
     * Uses the standardized LiveDataTransformer utility.
     * 
     * @param single The Single to transform
     * @param <T> The type of data emitted by the Single
     * @return LiveData of ViewState with state tracking
     */
    protected <T> LiveData<ViewState<T>> toLiveData(Single<T> single) {
        return LiveDataTransformer.fromSingle(single, true, disposableManager);
    }
    
    /**
     * Transform a Single to LiveData with ViewState for state tracking,
     * with control over initial loading state.
     * 
     * @param single The Single to transform
     * @param showLoading Whether to show loading state initially
     * @param <T> The type of data emitted by the Single
     * @return LiveData of ViewState with state tracking
     */
    protected <T> LiveData<ViewState<T>> toLiveData(Single<T> single, boolean showLoading) {
        return LiveDataTransformer.fromSingle(single, showLoading, disposableManager);
    }
    
    /**
     * Transform a Single to LiveData with ViewState for state tracking,
     * with custom success and error handlers.
     * 
     * @param single The Single to transform
     * @param showLoading Whether to show loading state initially
     * @param onSuccess Custom success handler
     * @param onError Custom error handler
     * @param <T> The type of data emitted by the Single
     * @return LiveData of ViewState with state tracking
     */
    protected <T> LiveData<ViewState<T>> toLiveData(
            Single<T> single, 
            boolean showLoading,
            io.reactivex.functions.Consumer<T> onSuccess,
            io.reactivex.functions.Consumer<Throwable> onError) {
        return LiveDataTransformer.fromSingle(single, showLoading, disposables, onSuccess, onError);
    }
    
    /**
     * Transform an Observable to LiveData with ViewState for state tracking.
     * 
     * @param observable The Observable to transform
     * @param <T> The type of data emitted by the Observable
     * @return LiveData of ViewState with state tracking
     */
    protected <T> LiveData<ViewState<T>> toLiveData(Observable<T> observable) {
        return LiveDataTransformer.fromObservable(observable, true, disposableManager);
    }
    
    /**
     * Transform an Observable to LiveData with ViewState for state tracking,
     * with control over initial loading state.
     * 
     * @param observable The Observable to transform
     * @param showLoading Whether to show loading state initially
     * @param <T> The type of data emitted by the Observable
     * @return LiveData of ViewState with state tracking
     */
    protected <T> LiveData<ViewState<T>> toLiveData(Observable<T> observable, boolean showLoading) {
        return LiveDataTransformer.fromObservable(observable, showLoading, disposableManager);
    }
    
    /**
     * Transform an Observable directly to LiveData without state tracking.
     * 
     * @param observable The Observable to transform
     * @param <T> The type of data emitted by the Observable
     * @return LiveData receiving values from the Observable
     */
    protected <T> LiveData<T> toDirectLiveData(Observable<T> observable) {
        return LiveDataTransformer.fromObservableDirect(observable);
    }
    
    /**
     * Transform a Completable to LiveData with Boolean for state tracking.
     * 
     * @param completable The Completable to transform
     * @return LiveData of Boolean indicating completion status
     */
    protected LiveData<Boolean> toLiveData(Completable completable) {
        return LiveDataTransformer.fromCompletable(completable, this::setError);
    }
    
    /**
     * Transform a Completable to LiveData with ViewState for state tracking.
     * 
     * @param completable The Completable to transform
     * @return LiveData of ViewState with Boolean for completion status
     */
    protected LiveData<ViewState<Boolean>> toStateLiveData(Completable completable) {
        return LiveDataTransformer.fromCompletableAsViewState(completable, disposableManager);
    }
    
    /**
     * Transform a Completable to LiveData with ViewState for state tracking,
     * with custom completion and error handlers.
     * 
     * @param completable The Completable to transform
     * @param onComplete Custom completion handler
     * @param onError Custom error handler
     * @return LiveData of ViewState with Boolean for completion status
     */
    protected LiveData<ViewState<Boolean>> toStateLiveData(
            Completable completable,
            io.reactivex.functions.Action onComplete,
            io.reactivex.functions.Consumer<Throwable> onError) {
        return LiveDataTransformer.fromCompletableAsViewState(completable, disposables, onComplete, onError);
    }
    
    /**
     * Create a LiveData with pagination support.
     * 
     * @param dataSource Function that produces a Single with the data for a given page
     * @param pageSize Number of items per page
     * @param <T> The type of data emitted
     * @return LiveData of ViewState with state tracking
     */
    protected <T> LiveData<ViewState<T>> createPaginatedLiveData(
            io.reactivex.functions.Function<Integer, Single<T>> dataSource,
            int pageSize) {
        return LiveDataTransformer.createPaginatedLiveData(dataSource, pageSize);
    }
    
    //-----------------------------------------------------------------------------------
    // Legacy execution methods (for backwards compatibility)
    //-----------------------------------------------------------------------------------
    
    /**
     * Add a disposable with automatic error handling
     * 
     * @param observable Observable to subscribe to
     * @param onNext Consumer for successfully emitted items
     * @param <T> Type of items emitted by the observable
     * 
     * @deprecated Use toLiveData(observable) instead for better state management
     */
    @Deprecated
    protected <T> void execute(Observable<T> observable, io.reactivex.functions.Consumer<T> onNext) {
        addDisposable(observable
                .compose(RxSchedulers.applyObservableSchedulers())
                .doOnSubscribe(__ -> setLoading(true))
                .doFinally(() -> setLoading(false))
                .subscribe(onNext, this::setError));
    }
    
    /**
     * Add a disposable with automatic error handling
     * 
     * @param single Single to subscribe to
     * @param onSuccess Consumer for success result
     * @param <T> Type of result emitted by the single
     * 
     * @deprecated Use toLiveData(single) instead for better state management
     */
    @Deprecated
    protected <T> void execute(Single<T> single, io.reactivex.functions.Consumer<T> onSuccess) {
        addDisposable(single
                .compose(RxSchedulers.applySingleSchedulers())
                .doOnSubscribe(__ -> setLoading(true))
                .doFinally(() -> setLoading(false))
                .subscribe(onSuccess, this::setError));
    }
    
    /**
     * Add a disposable with automatic error handling
     * 
     * @param completable Completable to subscribe to
     * @param onComplete Action to run on completion
     * 
     * @deprecated Use toLiveData(completable) instead for better state management
     */
    @Deprecated
    protected void execute(Completable completable, io.reactivex.functions.Action onComplete) {
        addDisposable(completable
                .compose(RxSchedulers.applyCompletableSchedulers())
                .doOnSubscribe(__ -> setLoading(true))
                .doFinally(() -> setLoading(false))
                .subscribe(onComplete, this::setError));
    }
    
    /**
     * Add a disposable with automatic error handling and ViewState management
     * 
     * @param observable Observable to subscribe to
     * @param stateLiveData LiveData of ViewState to update
     * @param <T> Type of items emitted by the observable
     * 
     * @deprecated Use toLiveData(observable) instead for better state management
     */
    @Deprecated
    protected <T> void executeWithState(Observable<T> observable, MutableLiveData<ViewState<T>> stateLiveData) {
        setLoading(stateLiveData);
        addDisposable(observable
                .compose(RxSchedulers.applyObservableSchedulers())
                .subscribe(
                        data -> setSuccess(stateLiveData, data),
                        error -> setError(stateLiveData, error)
                ));
    }
    
    /**
     * Add a disposable with automatic error handling and ViewState management
     * 
     * @param single Single to subscribe to
     * @param stateLiveData LiveData of ViewState to update
     * @param <T> Type of result emitted by the single
     * 
     * @deprecated Use toLiveData(single) instead for better state management
     */
    @Deprecated
    protected <T> void executeWithState(Single<T> single, MutableLiveData<ViewState<T>> stateLiveData) {
        setLoading(stateLiveData);
        addDisposable(single
                .compose(RxSchedulers.applySingleSchedulers())
                .subscribe(
                        data -> setSuccess(stateLiveData, data),
                        error -> setError(stateLiveData, error)
                ));
    }
    
    //-----------------------------------------------------------------------------------
    // Lifecycle integration utilities
    //-----------------------------------------------------------------------------------
    
    /**
     * Observe a LiveData with lifecycle awareness
     * 
     * @param liveData LiveData to observe
     * @param owner LifecycleOwner (usually a Fragment or Activity)
     * @param observer Observer to receive events
     * @param <T> Type of data
     */
    protected <T> void observe(LiveData<T> liveData, LifecycleOwner owner, Observer<T> observer) {
        liveData.observe(owner, observer);
    }
    
    /**
     * Execute a task once and then dispose when lifecycle owner is destroyed
     * 
     * @param task Task to execute, returning a Disposable
     * @param owner LifecycleOwner (usually a Fragment or Activity)
     */
    protected void executeOnce(io.reactivex.functions.Function<Void, Disposable> task, LifecycleOwner owner) {
        try {
            Disposable disposable = task.apply(null);
            owner.getLifecycle().addObserver(new androidx.lifecycle.LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull androidx.lifecycle.Lifecycle.Event event) {
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
                        if (!disposable.isDisposed()) {
                            disposable.dispose();
                        }
                        owner.getLifecycle().removeObserver(this);
                    }
                }
            });
        } catch (Exception e) {
            setError(e);
        }
    }
    
    /**
     * Clean up resources when ViewModel is no longer used
     */
    @Override
    protected void onCleared() {
        disposables.clear();
        disposableManager.clear();
        super.onCleared();
    }
}