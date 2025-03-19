package com.autogratuity.ui.common;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * A lifecycle-aware manager for RxJava disposables.
 * <p>
 * This class provides tools to manage RxJava subscriptions tied to Android lifecycle events,
 * helping prevent memory leaks and unnecessary background processing when components
 * are no longer active or visible.
 * <p>
 * Key features:
 * - Automatic disposal on lifecycle events (ON_DESTROY, ON_STOP, etc.)
 * - Support for disposable grouping with tags
 * - Lifecycle-aware subscription methods
 * - Tracking status of disposables
 */
public class DisposableLifecycleManager implements DefaultLifecycleObserver {
    private static final String TAG = "DisposableManager";

    /**
     * Primary CompositeDisposable that will be cleared on destruction
     */
    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * Maps to hold tagged disposable groups
     */
    private final Map<String, CompositeDisposable> taggedDisposables = new HashMap<>();

    /**
     * Set of active tags for faster lookup
     */
    private final Set<String> activeTags = new HashSet<>();

    /**
     * Track if this manager has been destroyed
     */
    private boolean isDestroyed = false;

    /**
     * Constructor
     */
    public DisposableLifecycleManager() {
        // Default constructor
    }

    /**
     * Constructor with lifecycle binding
     *
     * @param lifecycleOwner LifecycleOwner to bind to (typically a Fragment or Activity)
     */
    public DisposableLifecycleManager(@NonNull LifecycleOwner lifecycleOwner) {
        bind(lifecycleOwner);
    }

    /**
     * Constructor with lifecycle binding and specific event
     *
     * @param lifecycleOwner LifecycleOwner to bind to
     * @param disposeEvent   Lifecycle event at which to dispose subscriptions
     */
    public DisposableLifecycleManager(@NonNull LifecycleOwner lifecycleOwner, Lifecycle.Event disposeEvent) {
        bind(lifecycleOwner, disposeEvent);
    }

    /**
     * Bind this manager to a lifecycle owner, disposing on destroy by default
     *
     * @param lifecycleOwner LifecycleOwner to bind to
     */
    public void bind(@NonNull LifecycleOwner lifecycleOwner) {
        bind(lifecycleOwner, Lifecycle.Event.ON_DESTROY);
    }

    /**
     * Bind this manager to a lifecycle owner with a specific lifecycle event
     *
     * @param lifecycleOwner LifecycleOwner to bind to
     * @param disposeEvent   Lifecycle event at which to dispose subscriptions
     */
    public void bind(@NonNull LifecycleOwner lifecycleOwner, Lifecycle.Event disposeEvent) {
        lifecycleOwner.getLifecycle().addObserver(new androidx.lifecycle.LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == disposeEvent) {
                    clear();
                    lifecycleOwner.getLifecycle().removeObserver(this);
                }
            }
        });
    }

    /**
     * Add a disposable to be managed and automatically disposed
     *
     * @param disposable Disposable to add
     * @return The added disposable for chaining
     */
    public Disposable add(@NonNull Disposable disposable) {
        if (isDestroyed) {
            Log.w(TAG, "Adding disposable to a destroyed manager. Disposing immediately.");
            disposable.dispose();
            return disposable;
        }
        disposables.add(disposable);
        return disposable;
    }

    /**
     * Add a disposable with a specific tag for grouped management
     *
     * @param tag        Tag identifier for the disposable group
     * @param disposable Disposable to add
     * @return The added disposable for chaining
     */
    public Disposable add(@NonNull String tag, @NonNull Disposable disposable) {
        if (isDestroyed) {
            Log.w(TAG, "Adding tagged disposable to a destroyed manager. Disposing immediately.");
            disposable.dispose();
            return disposable;
        }

        CompositeDisposable tagGroup = taggedDisposables.get(tag);
        if (tagGroup == null) {
            tagGroup = new CompositeDisposable();
            taggedDisposables.put(tag, tagGroup);
            activeTags.add(tag);
        }
        tagGroup.add(disposable);
        return disposable;
    }

    /**
     * Remove and dispose all disposables with the given tag
     *
     * @param tag Tag identifier to clear
     */
    public void clear(@NonNull String tag) {
        CompositeDisposable tagGroup = taggedDisposables.get(tag);
        if (tagGroup != null) {
            tagGroup.clear();
            taggedDisposables.remove(tag);
            activeTags.remove(tag);
        }
    }

    /**
     * Check if a specific tag group has active disposables
     *
     * @param tag Tag identifier to check
     * @return true if the tag group exists and is not empty
     */
    public boolean hasActiveDisposables(@NonNull String tag) {
        return activeTags.contains(tag);
    }

    /**
     * Clear all disposables and marked as destroyed
     */
    public void clear() {
        disposables.clear();
        for (CompositeDisposable tagGroup : taggedDisposables.values()) {
            tagGroup.clear();
        }
        taggedDisposables.clear();
        activeTags.clear();
        isDestroyed = true;
    }

    /**
     * Add an Observable subscription with simplified syntax
     *
     * @param observable Observable to subscribe to
     * @param onNext     Handler for emitted items
     * @param onError    Handler for errors
     * @param <T>        Type of items emitted by the observable
     * @return The created disposable
     */
    public <T> Disposable add(
            @NonNull Observable<T> observable,
            @NonNull io.reactivex.functions.Consumer<T> onNext,
            @NonNull io.reactivex.functions.Consumer<Throwable> onError) {
        return add(observable.subscribe(onNext, onError));
    }

    /**
     * Add a tagged Observable subscription with simplified syntax
     *
     * @param tag        Tag identifier for the disposable group
     * @param observable Observable to subscribe to
     * @param onNext     Handler for emitted items
     * @param onError    Handler for errors
     * @param <T>        Type of items emitted by the observable
     * @return The created disposable
     */
    public <T> Disposable add(
            @NonNull String tag,
            @NonNull Observable<T> observable,
            @NonNull io.reactivex.functions.Consumer<T> onNext,
            @NonNull io.reactivex.functions.Consumer<Throwable> onError) {
        return add(tag, observable.subscribe(onNext, onError));
    }

    /**
     * Add a Single subscription with simplified syntax
     *
     * @param single    Single to subscribe to
     * @param onSuccess Handler for success result
     * @param onError   Handler for errors
     * @param <T>       Type of result emitted by the single
     * @return The created disposable
     */
    public <T> Disposable add(
            @NonNull Single<T> single,
            @NonNull io.reactivex.functions.Consumer<T> onSuccess,
            @NonNull io.reactivex.functions.Consumer<Throwable> onError) {
        return add(single.subscribe(onSuccess, onError));
    }

    /**
     * Add a tagged Single subscription with simplified syntax
     *
     * @param tag       Tag identifier for the disposable group
     * @param single    Single to subscribe to
     * @param onSuccess Handler for success result
     * @param onError   Handler for errors
     * @param <T>       Type of result emitted by the single
     * @return The created disposable
     */
    public <T> Disposable add(
            @NonNull String tag,
            @NonNull Single<T> single,
            @NonNull io.reactivex.functions.Consumer<T> onSuccess,
            @NonNull io.reactivex.functions.Consumer<Throwable> onError) {
        return add(tag, single.subscribe(onSuccess, onError));
    }

    /**
     * Add a Completable subscription with simplified syntax
     *
     * @param completable Completable to subscribe to
     * @param onComplete  Handler for completion
     * @param onError     Handler for errors
     * @return The created disposable
     */
    public Disposable add(
            @NonNull Completable completable,
            @NonNull io.reactivex.functions.Action onComplete,
            @NonNull io.reactivex.functions.Consumer<Throwable> onError) {
        return add(completable.subscribe(onComplete, onError));
    }

    /**
     * Add a tagged Completable subscription with simplified syntax
     *
     * @param tag         Tag identifier for the disposable group
     * @param completable Completable to subscribe to
     * @param onComplete  Handler for completion
     * @param onError     Handler for errors
     * @return The created disposable
     */
    public Disposable add(
            @NonNull String tag,
            @NonNull Completable completable,
            @NonNull io.reactivex.functions.Action onComplete,
            @NonNull io.reactivex.functions.Consumer<Throwable> onError) {
        return add(tag, completable.subscribe(onComplete, onError));
    }

    /**
     * Remove a specific disposable from management
     *
     * @param disposable Disposable to remove
     * @return true if the disposable was removed
     */
    public boolean remove(@NonNull Disposable disposable) {
        return disposables.remove(disposable);
    }

    /**
     * Check if this manager has been destroyed
     *
     * @return true if the manager has been destroyed
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    /**
     * Get the total count of managed disposables
     *
     * @return Count of all disposables being managed
     */
    public int size() {
        int count = disposables.size();
        for (CompositeDisposable tagGroup : taggedDisposables.values()) {
            count += tagGroup.size();
        }
        return count;
    }

    // DefaultLifecycleObserver implementation

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        clear();
    }
}