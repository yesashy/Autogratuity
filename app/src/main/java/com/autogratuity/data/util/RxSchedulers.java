package com.autogratuity.data.util;

import io.reactivex.CompletableTransformer;
import io.reactivex.MaybeTransformer;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Utility class providing standard scheduler patterns for RxJava operations.
 * This ensures consistent threading model across the application.
 */
public class RxSchedulers {
    
    /**
     * Returns the IO scheduler for database and network operations.
     * 
     * @return IO Scheduler instance
     */
    public static Scheduler io() {
        return Schedulers.io();
    }
    
    /**
     * Returns the computation scheduler for CPU-intensive work.
     * 
     * @return Computation Scheduler instance
     */
    public static Scheduler computation() {
        return Schedulers.computation();
    }
    
    /**
     * Returns the main thread scheduler for UI operations.
     * 
     * @return UI Scheduler instance
     */
    public static Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }
    
    /**
     * Transformer for Observable to switch execution to IO thread and observe on UI thread.
     * Ideal for data operations that update the UI.
     * 
     * @param <T> The type emitted by the Observable
     * @return ObservableTransformer that applies the standard threading pattern
     */
    public static <T> ObservableTransformer<T, T> applyObservableSchedulers() {
        return observable -> observable
                .subscribeOn(io())
                .observeOn(ui());
    }
    
    /**
     * Transformer for Single to switch execution to IO thread and observe on UI thread.
     * Ideal for single data operations that update the UI.
     * 
     * @param <T> The type emitted by the Single
     * @return SingleTransformer that applies the standard threading pattern
     */
    public static <T> SingleTransformer<T, T> applySingleSchedulers() {
        return single -> single
                .subscribeOn(io())
                .observeOn(ui());
    }
    
    /**
     * Transformer for Completable to switch execution to IO thread and observe on UI thread.
     * Ideal for operations that don't return data but update the UI on completion.
     * 
     * @return CompletableTransformer that applies the standard threading pattern
     */
    public static CompletableTransformer applyCompletableSchedulers() {
        return completable -> completable
                .subscribeOn(io())
                .observeOn(ui());
    }
    
    /**
     * Transformer for Maybe to switch execution to IO thread and observe on UI thread.
     * Ideal for operations that may return data and update the UI.
     * 
     * @param <T> The type that may be emitted by the Maybe
     * @return MaybeTransformer that applies the standard threading pattern
     */
    public static <T> MaybeTransformer<T, T> applyMaybeSchedulers() {
        return maybe -> maybe
                .subscribeOn(io())
                .observeOn(ui());
    }
    
    /**
     * Transformer for Observable for background operations that don't update the UI.
     * Both subscribe and observe happen on IO thread.
     * 
     * @param <T> The type emitted by the Observable
     * @return ObservableTransformer that applies the background threading pattern
     */
    public static <T> ObservableTransformer<T, T> applyBackgroundSchedulers() {
        return observable -> observable
                .subscribeOn(io())
                .observeOn(io());
    }
    
    /**
     * Transformer for computation-intensive Observable operations.
     * Subscribe on computation thread, observe on UI thread.
     * 
     * @param <T> The type emitted by the Observable
     * @return ObservableTransformer that applies the computation threading pattern
     */
    public static <T> ObservableTransformer<T, T> applyComputationSchedulers() {
        return observable -> observable
                .subscribeOn(computation())
                .observeOn(ui());
    }
}
