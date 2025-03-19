package com.autogratuity.data.repository.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Event bus for cross-repository communication.
 * Enables decoupling between repositories while maintaining integration capabilities.
 * 
 * This class follows the publisher-subscriber pattern to facilitate message passing
 * between repositories without creating direct dependencies.
 */
public class RepositoryEventBus {
    
    private static final String TAG = "RepositoryEventBus";
    
    /**
     * Event class for repository events
     */
    public static class RepositoryEvent {
        private final String type;
        private final String source;
        private final Map<String, Object> data;
        private final String targetRepository;
        
        /**
         * Constructor for repository event
         * 
         * @param type Event type identifier
         * @param source Source repository
         * @param data Event data payload
         * @param targetRepository Optional target repository or null for broadcast
         */
        public RepositoryEvent(
                @NonNull String type, 
                @NonNull String source, 
                @Nullable Map<String, Object> data,
                @Nullable String targetRepository) {
            this.type = type;
            this.source = source;
            this.data = data;
            this.targetRepository = targetRepository;
        }
        
        /**
         * Get the event type identifier
         * 
         * @return Event type
         */
        public String getType() {
            return type;
        }
        
        /**
         * Get the source repository
         * 
         * @return Source repository identifier
         */
        public String getSource() {
            return source;
        }
        
        /**
         * Get the event data payload
         * 
         * @return Event data or null if no data
         */
        public Map<String, Object> getData() {
            return data;
        }
        
        /**
         * Get the target repository
         * 
         * @return Target repository or null if broadcast
         */
        public String getTargetRepository() {
            return targetRepository;
        }
        
        @Override
        public String toString() {
            return "RepositoryEvent{" +
                    "type='" + type + '\'' +
                    ", source='" + source + '\'' +
                    ", data=" + (data != null ? data.size() + " items" : "null") +
                    ", target='" + (targetRepository != null ? targetRepository : "broadcast") + '\'' +
                    '}';
        }
    }
    
    // Singleton instance
    private static RepositoryEventBus instance;
    
    // Subject for publishing events
    private final Subject<RepositoryEvent> eventSubject = PublishSubject.<RepositoryEvent>create().toSerialized();
    
    // Map of registered repositories and their listeners
    private final Map<String, Set<EventListener>> repositoryListeners = new ConcurrentHashMap<>();
    
    /**
     * Interface for event listeners
     */
    public interface EventListener {
        void onEvent(RepositoryEvent event);
    }
    
    /**
     * Private constructor for singleton pattern
     */
    private RepositoryEventBus() {
        // Initialize event handling
        eventSubject.subscribe(
                event -> dispatchEvent(event),
                error -> Log.e(TAG, "Error in event subject", error)
        );
    }
    
    /**
     * Get the singleton instance
     * 
     * @return RepositoryEventBus instance
     */
    public static synchronized RepositoryEventBus getInstance() {
        if (instance == null) {
            instance = new RepositoryEventBus();
        }
        return instance;
    }
    
    /**
     * Register a repository listener
     * 
     * @param repositoryId Repository identifier
     * @param listener Event listener
     */
    public void register(String repositoryId, EventListener listener) {
        if (repositoryId == null || listener == null) {
            return;
        }
        
        Set<EventListener> listeners = repositoryListeners.get(repositoryId);
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<>();
            repositoryListeners.put(repositoryId, listeners);
        }
        listeners.add(listener);
        
        Log.d(TAG, "Registered listener for repository: " + repositoryId);
    }
    
    /**
     * Unregister a repository listener
     * 
     * @param repositoryId Repository identifier
     * @param listener Event listener
     */
    public void unregister(String repositoryId, EventListener listener) {
        if (repositoryId == null || listener == null) {
            return;
        }
        
        Set<EventListener> listeners = repositoryListeners.get(repositoryId);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                repositoryListeners.remove(repositoryId);
            }
        }
        
        Log.d(TAG, "Unregistered listener for repository: " + repositoryId);
    }
    
    /**
     * Post an event to the bus
     * 
     * @param event Repository event
     */
    public void post(RepositoryEvent event) {
        if (event == null) {
            return;
        }
        
        Log.d(TAG, "Posted event: " + event);
        eventSubject.onNext(event);
    }
    
    /**
     * Post a new event to the bus
     * 
     * @param type Event type
     * @param source Source repository
     * @param data Event data
     * @param targetRepository Target repository (null for broadcast)
     */
    public void post(String type, String source, Map<String, Object> data, String targetRepository) {
        post(new RepositoryEvent(type, source, data, targetRepository));
    }
    
    /**
     * Get the event stream for a specific repository
     * 
     * @param repositoryId Repository identifier
     * @return Observable of repository events
     */
    public Observable<RepositoryEvent> getEventsForRepository(String repositoryId) {
        if (repositoryId == null) {
            return Observable.empty();
        }
        
        return eventSubject.filter(event -> 
                event.getTargetRepository() == null || // Broadcast events
                event.getTargetRepository().equals(repositoryId)); // Targeted events
    }
    
    /**
     * Get the event stream for a specific event type
     * 
     * @param eventType Event type
     * @return Observable of repository events
     */
    public Observable<RepositoryEvent> getEventsOfType(String eventType) {
        if (eventType == null) {
            return Observable.empty();
        }
        
        return eventSubject.filter(event -> event.getType().equals(eventType));
    }
    
    /**
     * Dispatch an event to appropriate listeners
     * 
     * @param event Repository event
     */
    private void dispatchEvent(RepositoryEvent event) {
        // Skip if event is null
        if (event == null) {
            return;
        }
        
        try {
            // Handle broadcast events (no specific target)
            if (event.getTargetRepository() == null) {
                // Notify all registered listeners except the source
                for (Map.Entry<String, Set<EventListener>> entry : repositoryListeners.entrySet()) {
                    String repositoryId = entry.getKey();
                    if (!repositoryId.equals(event.getSource())) {
                        Set<EventListener> listeners = entry.getValue();
                        if (listeners != null) {
                            for (EventListener listener : listeners) {
                                notifyListener(listener, event);
                            }
                        }
                    }
                }
            } else {
                // Handle targeted events
                Set<EventListener> listeners = repositoryListeners.get(event.getTargetRepository());
                if (listeners != null) {
                    for (EventListener listener : listeners) {
                        notifyListener(listener, event);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error dispatching event", e);
        }
    }
    
    /**
     * Notify a listener of an event
     * 
     * @param listener Event listener
     * @param event Repository event
     */
    private void notifyListener(EventListener listener, RepositoryEvent event) {
        try {
            listener.onEvent(event);
        } catch (Exception e) {
            Log.e(TAG, "Error notifying listener", e);
        }
    }
    
    // Event type constants
    public static final class EventType {
        // Sync events
        public static final String SYNC_OPERATION_ENQUEUED = "sync_operation_enqueued";
        public static final String SYNC_OPERATION_COMPLETED = "sync_operation_completed";
        public static final String SYNC_OPERATION_FAILED = "sync_operation_failed";
        public static final String SYNC_STATUS_CHANGED = "sync_status_changed";
        
        // Config events
        public static final String CONFIG_UPDATED = "config_updated";
        public static final String DEVICE_REGISTERED = "device_registered";
        public static final String COUNTER_INCREMENTED = "counter_incremented";
        
        // Repository identifiers
        public static final String SYNC_REPOSITORY = "sync_repository";
        public static final String CONFIG_REPOSITORY = "config_repository";
    }
}
