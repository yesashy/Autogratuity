package com.autogratuity.ui.common;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.model.ErrorInfo;
import com.autogratuity.data.util.RxSchedulers;
import com.autogratuity.ui.common.state.ViewState;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import com.autogratuity.ui.common.DisposableLifecycleManager;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.schedulers.Schedulers;

/**
 * Utility class for transforming RxJava streams to LiveData with state tracking.
 * <p>
 * This class provides standardized approaches for transforming Singles, Observables,
 * and Completables to LiveData, with consistent handling of loading states, success
 * states, and error states.
 * <p>
 * All transformations support:
 * - Loading state tracking
 * - Success state with data
 * - Error state with standardized error representation
 * - Thread management (UI operations on main thread, background work on IO thread)
 * <p>
 * Using these utilities ensures consistent state handling across ViewModels.
 */
public class LiveDataTransformer {

    /**
     * Transform a Single to LiveData with ViewState for state tracking.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when the Single is subscribed to
     * - Success state with data when the Single emits a value
     * - Error state with exception when the Single emits an error
     *
     * @param single The Single to transform
     * @param <T>    The type of data emitted by the Single
     * @return LiveData of ViewState with state tracking
     */
    public static <T> LiveData<ViewState<T>> fromSingle(Single<T> single) {
        MutableLiveData<ViewState<T>> liveData = new MutableLiveData<>(ViewState.loading());

        single
                .compose(RxSchedulers.applySingleSchedulers())
                .subscribe(
                        result -> liveData.postValue(ViewState.success(result)),
                        error -> liveData.postValue(ViewState.error(error))
                );

        return liveData;
    }

    /**
     * Transform a Single to LiveData with ViewState for state tracking,
     * with additional control for loading state.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when showLoading is true and the Single is subscribed to
     * - Success state with data when the Single emits a value
     * - Error state with exception when the Single emits an error
     *
     * @param single      The Single to transform
     * @param showLoading Whether to show loading state initially
     * @param <T>         The type of data emitted by the Single
     * @return LiveData of ViewState with state tracking
     */
    public static <T> LiveData<ViewState<T>> fromSingle(Single<T> single, boolean showLoading) {
        MutableLiveData<ViewState<T>> liveData = new MutableLiveData<>(
                showLoading ? ViewState.loading() : null);

        single
                .compose(RxSchedulers.applySingleSchedulers())
                .subscribe(
                        result -> liveData.postValue(ViewState.success(result)),
                        error -> liveData.postValue(ViewState.error(error))
                );

        return liveData;
    }

    /**
     * Transform a Single to LiveData with ViewState for state tracking,
     * with additional control for loading state and a CompositeDisposable for lifecycle management.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when showLoading is true and the Single is subscribed to
     * - Success state with data when the Single emits a value
     * - Error state with exception when the Single emits an error
     * <p>
     * The subscription is automatically added to the provided CompositeDisposable.
     *
     * @param single       The Single to transform
     * @param showLoading  Whether to show loading state initially
     * @param disposables  CompositeDisposable to manage the subscription lifecycle
     * @param <T>          The type of data emitted by the Single
     * @return LiveData of ViewState with state tracking
     */
    public static <T> LiveData<ViewState<T>> fromSingle(
            Single<T> single,
            boolean showLoading,
            CompositeDisposable disposables) {

        MutableLiveData<ViewState<T>> liveData = new MutableLiveData<>(
                showLoading ? ViewState.loading() : null);

        disposables.add(single
                .compose(RxSchedulers.applySingleSchedulers())
                .subscribe(
                        result -> liveData.postValue(ViewState.success(result)),
                        error -> liveData.postValue(ViewState.error(error))
                ));

        return liveData;
    }
    
    /**
     * Transform a Single to LiveData with ViewState for state tracking,
     * with additional control for loading state and a DisposableLifecycleManager for lifecycle management.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when showLoading is true and the Single is subscribed to
     * - Success state with data when the Single emits a value
     * - Error state with exception when the Single emits an error
     * <p>
     * The subscription is automatically added to the provided DisposableLifecycleManager.
     *
     * @param single       The Single to transform
     * @param showLoading  Whether to show loading state initially
     * @param manager      DisposableLifecycleManager to manage the subscription lifecycle
     * @param <T>          The type of data emitted by the Single
     * @return LiveData of ViewState with state tracking
     */
    public static <T> LiveData<ViewState<T>> fromSingle(
            Single<T> single,
            boolean showLoading,
            DisposableLifecycleManager manager) {

        MutableLiveData<ViewState<T>> liveData = new MutableLiveData<>(
                showLoading ? ViewState.loading() : null);

        manager.add(single
                .compose(RxSchedulers.applySingleSchedulers())
                .subscribe(
                        result -> liveData.postValue(ViewState.success(result)),
                        error -> liveData.postValue(ViewState.error(error))
                ));

        return liveData;
    }

    /**
     * Transform a Single to LiveData with ViewState for state tracking,
     * with additional control for loading state, a CompositeDisposable for lifecycle management,
     * and custom success and error handlers.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when showLoading is true and the Single is subscribed to
     * - Success state with data when the Single emits a value and the success handler is called
     * - Error state with exception when the Single emits an error and the error handler is called
     * <p>
     * The subscription is automatically added to the provided CompositeDisposable.
     *
     * @param single       The Single to transform
     * @param showLoading  Whether to show loading state initially
     * @param disposables  CompositeDisposable to manage the subscription lifecycle
     * @param onSuccess    Custom success handler
     * @param onError      Custom error handler
     * @param <T>          The type of data emitted by the Single
     * @return LiveData of ViewState with state tracking
     */
    public static <T> LiveData<ViewState<T>> fromSingle(
            Single<T> single,
            boolean showLoading,
            CompositeDisposable disposables,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {

        MutableLiveData<ViewState<T>> liveData = new MutableLiveData<>(
                showLoading ? ViewState.loading() : null);

        disposables.add(single
                .compose(RxSchedulers.applySingleSchedulers())
                .subscribe(
                        result -> {
                            liveData.postValue(ViewState.success(result));
                            if (onSuccess != null) {
                                try {
                                    onSuccess.accept(result);
                                } catch (Exception e) {
                                    // Ignore exception in success handler
                                }
                            }
                        },
                        error -> {
                            liveData.postValue(ViewState.error(error));
                            if (onError != null) {
                                try {
                                    onError.accept(error);
                                } catch (Exception e) {
                                    // Ignore exception in error handler
                                }
                            }
                        }
                ));

        return liveData;
    }

    /**
     * Transform an Observable to LiveData with ViewState for state tracking.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when the Observable is subscribed to
     * - Success state with latest data when the Observable emits a value
     * - Error state with exception when the Observable emits an error
     * <p>
     * Note that only the latest value from the Observable will be reflected in the LiveData.
     *
     * @param observable The Observable to transform
     * @param <T>        The type of data emitted by the Observable
     * @return LiveData of ViewState with state tracking
     */
    public static <T> LiveData<ViewState<T>> fromObservable(Observable<T> observable) {
        MutableLiveData<ViewState<T>> liveData = new MutableLiveData<>(ViewState.loading());

        observable
                .compose(RxSchedulers.applyObservableSchedulers())
                .subscribe(
                        result -> liveData.postValue(ViewState.success(result)),
                        error -> liveData.postValue(ViewState.error(error))
                );

        return liveData;
    }

    /**
     * Transform an Observable to LiveData with ViewState for state tracking,
     * with additional control for loading state and a CompositeDisposable for lifecycle management.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when showLoading is true and the Observable is subscribed to
     * - Success state with latest data when the Observable emits a value
     * - Error state with exception when the Observable emits an error
     * <p>
     * The subscription is automatically added to the provided CompositeDisposable.
     *
     * @param observable   The Observable to transform
     * @param showLoading  Whether to show loading state initially
     * @param disposables  CompositeDisposable to manage the subscription lifecycle
     * @param <T>          The type of data emitted by the Observable
     * @return LiveData of ViewState with state tracking
     */
    public static <T> LiveData<ViewState<T>> fromObservable(
            Observable<T> observable,
            boolean showLoading,
            CompositeDisposable disposables) {

        MutableLiveData<ViewState<T>> liveData = new MutableLiveData<>(
                showLoading ? ViewState.loading() : null);

        disposables.add(observable
                .compose(RxSchedulers.applyObservableSchedulers())
                .subscribe(
                        result -> liveData.postValue(ViewState.success(result)),
                        error -> liveData.postValue(ViewState.error(error))
                ));

        return liveData;
    }
    
    /**
     * Transform an Observable to LiveData with ViewState for state tracking,
     * with additional control for loading state and a DisposableLifecycleManager for lifecycle management.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when showLoading is true and the Observable is subscribed to
     * - Success state with latest data when the Observable emits a value
     * - Error state with exception when the Observable emits an error
     * <p>
     * The subscription is automatically added to the provided DisposableLifecycleManager.
     *
     * @param observable   The Observable to transform
     * @param showLoading  Whether to show loading state initially
     * @param manager      DisposableLifecycleManager to manage the subscription lifecycle
     * @param <T>          The type of data emitted by the Observable
     * @return LiveData of ViewState with state tracking
     */
    public static <T> LiveData<ViewState<T>> fromObservable(
            Observable<T> observable,
            boolean showLoading,
            DisposableLifecycleManager manager) {

        MutableLiveData<ViewState<T>> liveData = new MutableLiveData<>(
                showLoading ? ViewState.loading() : null);

        manager.add(observable
                .compose(RxSchedulers.applyObservableSchedulers())
                .subscribe(
                        result -> liveData.postValue(ViewState.success(result)),
                        error -> liveData.postValue(ViewState.error(error))
                ));

        return liveData;
    }

    /**
     * Transform an Observable directly to LiveData.
     * <p>
     * This transformation does not include state tracking, but directly
     * maps the Observable's emissions to LiveData values.
     * <p>
     * This is useful for cases where you want direct mapping without state tracking,
     * such as for UI events or other non-data emissions.
     *
     * @param observable   The Observable to transform
     * @param <T>          The type of data emitted by the Observable
     * @return LiveData receiving values from the Observable
     */
    public static <T> LiveData<T> fromObservableDirect(Observable<T> observable) {
        return LiveDataReactiveStreams.fromPublisher(
                observable.toFlowable(BackpressureStrategy.LATEST)
                        .observeOn(AndroidSchedulers.mainThread())
        );
    }

    /**
     * Transform a Completable to LiveData with Boolean for state tracking.
     * <p>
     * The returned LiveData will emit:
     * - false when the Completable is subscribed to (indicating in-progress)
     * - true when the Completable completes (indicating success)
     * <p>
     * If the Completable errors, the LiveData will not emit a new value,
     * but the error will be passed to the provided error handler.
     *
     * @param completable  The Completable to transform
     * @param onError      Error handler to receive any errors
     * @return LiveData of Boolean indicating completion status
     */
    public static LiveData<Boolean> fromCompletable(
            Completable completable,
            Consumer<Throwable> onError) {

        MutableLiveData<Boolean> liveData = new MutableLiveData<>(false);

        completable
                .compose(RxSchedulers.applyCompletableSchedulers())
                .subscribe(
                        () -> liveData.postValue(true),
                        error -> {
                            if (onError != null) {
                                try {
                                    onError.accept(error);
                                } catch (Exception e) {
                                    // Ignore exception in error handler
                                }
                            }
                        }
                );

        return liveData;
    }

    /**
     * Transform a Completable to LiveData with ViewState for state tracking.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when the Completable is subscribed to
     * - Success state with a Boolean true value when the Completable completes
     * - Error state with exception when the Completable emits an error
     *
     * @param completable  The Completable to transform
     * @return LiveData of ViewState with Boolean for completion status
     */
    public static LiveData<ViewState<Boolean>> fromCompletableAsViewState(Completable completable) {
        MutableLiveData<ViewState<Boolean>> liveData = new MutableLiveData<>(ViewState.loading());

        completable
                .compose(RxSchedulers.applyCompletableSchedulers())
                .subscribe(
                        () -> liveData.postValue(ViewState.success(true)),
                        error -> liveData.postValue(ViewState.error(error))
                );

        return liveData;
    }

    /**
     * Transform a Completable to LiveData with ViewState for state tracking,
     * with a CompositeDisposable for lifecycle management.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when the Completable is subscribed to
     * - Success state with a Boolean true value when the Completable completes
     * - Error state with exception when the Completable emits an error
     * <p>
     * The subscription is automatically added to the provided CompositeDisposable.
     *
     * @param completable  The Completable to transform
     * @param disposables  CompositeDisposable to manage the subscription lifecycle
     * @return LiveData of ViewState with Boolean for completion status
     */
    public static LiveData<ViewState<Boolean>> fromCompletableAsViewState(
            Completable completable,
            CompositeDisposable disposables) {

        MutableLiveData<ViewState<Boolean>> liveData = new MutableLiveData<>(ViewState.loading());

        disposables.add(completable
                .compose(RxSchedulers.applyCompletableSchedulers())
                .subscribe(
                        () -> liveData.postValue(ViewState.success(true)),
                        error -> liveData.postValue(ViewState.error(error))
                ));

        return liveData;
    }
    
    /**
     * Transform a Completable to LiveData with ViewState for state tracking,
     * with a DisposableLifecycleManager for lifecycle management.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when the Completable is subscribed to
     * - Success state with a Boolean true value when the Completable completes
     * - Error state with exception when the Completable emits an error
     * <p>
     * The subscription is automatically added to the provided DisposableLifecycleManager.
     *
     * @param completable  The Completable to transform
     * @param manager      DisposableLifecycleManager to manage the subscription lifecycle
     * @return LiveData of ViewState with Boolean for completion status
     */
    public static LiveData<ViewState<Boolean>> fromCompletableAsViewState(
            Completable completable,
            DisposableLifecycleManager manager) {

        MutableLiveData<ViewState<Boolean>> liveData = new MutableLiveData<>(ViewState.loading());

        manager.add(completable
                .compose(RxSchedulers.applyCompletableSchedulers())
                .subscribe(
                        () -> liveData.postValue(ViewState.success(true)),
                        error -> liveData.postValue(ViewState.error(error))
                ));

        return liveData;
    }

    /**
     * Transform a Completable to LiveData with ViewState for state tracking,
     * with a CompositeDisposable for lifecycle management and custom completion and error handlers.
     * <p>
     * The returned LiveData will emit:
     * - Loading state when the Completable is subscribed to
     * - Success state with a Boolean true value when the Completable completes and the completion handler is called
     * - Error state with exception when the Completable emits an error and the error handler is called
     * <p>
     * The subscription is automatically added to the provided CompositeDisposable.
     *
     * @param completable  The Completable to transform
     * @param disposables  CompositeDisposable to manage the subscription lifecycle
     * @param onComplete   Custom completion handler
     * @param onError      Custom error handler
     * @return LiveData of ViewState with Boolean for completion status
     */
    public static LiveData<ViewState<Boolean>> fromCompletableAsViewState(
            Completable completable,
            CompositeDisposable disposables,
            Action onComplete,
            Consumer<Throwable> onError) {

        MutableLiveData<ViewState<Boolean>> liveData = new MutableLiveData<>(ViewState.loading());

        disposables.add(completable
                .compose(RxSchedulers.applyCompletableSchedulers())
                .subscribe(
                        () -> {
                            liveData.postValue(ViewState.success(true));
                            if (onComplete != null) {
                                try {
                                    onComplete.run();
                                } catch (Exception e) {
                                    // Ignore exception in completion handler
                                }
                            }
                        },
                        error -> {
                            liveData.postValue(ViewState.error(error));
                            if (onError != null) {
                                try {
                                    onError.accept(error);
                                } catch (Exception e) {
                                    // Ignore exception in error handler
                                }
                            }
                        }
                ));

        return liveData;
    }

    /**
     * Transform a stream of data into a LiveData with pagination support.
     * <p>
     * This transformation maintains a loading state separate from the data,
     * allowing UI to show a loading indicator while preserving existing data.
     * <p>
     * The data stream is expected to be cumulative, meaning each emission contains
     * all previously loaded items plus new ones.
     *
     * @param dataSource   Function that produces a Single with the data for a given page
     * @param pageSize     Number of items per page
     * @param <T>          The type of data emitted
     * @return LiveData of ViewState with state tracking
     */
    public static <T> LiveData<ViewState<T>> createPaginatedLiveData(
            io.reactivex.functions.Function<Integer, Single<T>> dataSource,
            int pageSize) {

        MediatorLiveData<ViewState<T>> result = new MediatorLiveData<>();
        MutableLiveData<Boolean> loadingMore = new MutableLiveData<>(false);
        BehaviorProcessor<Integer> pageProcessor = BehaviorProcessor.createDefault(1);

        // Add loading state source
        result.addSource(loadingMore, isLoading -> {
            ViewState<T> currentState = result.getValue();
            if (currentState == null || !currentState.isSuccess()) {
                result.setValue(ViewState.loading());
            }
            // If we already have data, don't change the state to loading
            // to avoid flickering - the UI can use the loadingMore value
            // to show a loading indicator
        });

        // Subscribe to page changes
        pageProcessor
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(page -> loadingMore.setValue(true))
                .flatMapSingle(dataSource)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            loadingMore.setValue(false);
                            result.setValue(ViewState.success(data));
                        },
                        error -> {
                            loadingMore.setValue(false);
                            result.setValue(ViewState.error(error));
                        }
                );

        return result;
    }

    /**
     * Convert an error to a standardized ErrorInfo object.
     * <p>
     * This utility method creates appropriate ErrorInfo objects based on the
     * type of exception, with user-friendly messages and appropriate recovery actions.
     *
     * @param error The exception to convert
     * @return Standardized ErrorInfo
     */
    public static ErrorInfo convertErrorToErrorInfo(Throwable error) {
        if (error == null) {
            return ErrorInfo.createUnknownError("Unknown error occurred");
        }

        // Network errors
        if (error instanceof java.net.UnknownHostException) {
            return ErrorInfo.createNetworkError("Network unavailable. Please check your connection.");
        }
        if (error instanceof java.net.SocketTimeoutException || 
            error instanceof java.io.InterruptedIOException) {
            return ErrorInfo.createNetworkError("Connection timed out. Please try again.");
        }

        // Specific error types based on message patterns
        String message = error.getMessage();
        if (message != null) {
            if (message.contains("PERMISSION_DENIED")) {
                return ErrorInfo.createPermissionError("You don't have permission to perform this action.");
            }
            if (message.contains("UNAUTHENTICATED")) {
                return ErrorInfo.createAuthenticationError("Authentication required. Please sign in again.");
            }
            if (message.contains("RESOURCE_EXHAUSTED")) {
                return ErrorInfo.createThrottledError("Too many requests. Please try again later.");
            }
            if (message.contains("NOT_FOUND")) {
                return ErrorInfo.createNotFoundError("Requested resource not found.");
            }
        }

        // Check if the error already has an ErrorInfo
        if (error instanceof Exception && error.getCause() instanceof Exception) {
            Throwable cause = error.getCause();
            if (cause.getMessage() != null && cause.getMessage().contains("ErrorInfo:")) {
                // Extract ErrorInfo from cause message
                return ErrorInfo.fromMessage(cause.getMessage());
            }
        }

        // Generic error with the original message
        String errorMessage = message != null ? message : "An unexpected error occurred";
        return ErrorInfo.createUnknownError(errorMessage);
    }
}
