# Autogratuity Architectural Roadmap

## Core Problem Statement

Autogratuity suffers from fundamental architectural flaws that prevent proper synchronization between Firestore and local app state. The most critical issues are:

1. **Disconnected Data Systems**: Cloud data (Firestore) and local data (SharedPreferences) operate independently without proper synchronization
2. **One-Way Data Flow**: The app primarily writes to Firestore but rarely reads changes back
3. **No Real-Time Listeners**: Firestore's key feature of real-time updates is not utilized
4. **Over-Reliance on Local Storage**: Critical features depend on SharedPreferences instead of authoritative cloud data
5. **Missing Sync on App Start**: No comprehensive data refresh when app launches

## Technical Roadmap

### Phase 1: Core Architecture Refactoring (Estimated: 3 weeks)

#### 1.1 Create Unified Data Layer (Week 1)

```java
// Create a unifying interface that all data operations must go through
public interface DataRepository {
    // User data operations
    Single<UserProfile> getUserProfile();
    Completable updateUserProfile(UserProfile profile);
    Observable<UserProfile> observeUserProfile(); // Real-time updates
    
    // Subscription operations
    Single<SubscriptionStatus> getSubscriptionStatus();
    Completable updateSubscriptionStatus(SubscriptionStatus status);
    Observable<SubscriptionStatus> observeSubscriptionStatus(); // Real-time updates
    
    // Additional methods for other data types...
}

// Implementation that coordinates between Firestore and local storage
public class FirestoreRepository implements DataRepository {
    private final FirebaseFirestore db;
    private final SharedPreferences prefs;
    private final String userId;
    
    // Cache strategy - Firestore is always authoritative but cache for performance
    @Override
    public Observable<UserProfile> observeUserProfile() {
        return Observable.create(emitter -> {
            // Listen for real-time updates from Firestore
            ListenerRegistration registration = db.collection("users")
                    .document(userId)
                    .addSnapshotListener((snapshot, error) -> {
                        if (error != null) {
                            emitter.onError(error);
                            return;
                        }
                        
                        if (snapshot != null && snapshot.exists()) {
                            UserProfile profile = snapshot.toObject(UserProfile.class);
                            
                            // Update local cache whenever Firestore changes
                            updateLocalCache(profile);
                            
                            // Emit the updated profile
                            emitter.onNext(profile);
                        }
                    });
            
            // Clean up listener when observer disposes
            emitter.setCancellable(registration::remove);
        });
    }
    
    // More implementation methods...
}
```

#### 1.2 Migrate Subscription System (Week 2)

```java
// Replace the current SubscriptionManager with new integrated approach
public class SubscriptionManager {
    private final DataRepository repository;
    private final BillingClient billingClient;
    
    // Constructor injects the unified data repository
    public SubscriptionManager(DataRepository repository, Context context) {
        this.repository = repository;
        // Billing client setup...
    }
    
    // This method now checks Firestore first, then local cache as backup
    public Single<Boolean> isProUser() {
        return repository.getSubscriptionStatus()
            .map(status -> {
                // First check cloud status (authoritative)
                if (status.isPro()) {
                    return true;
                }
                
                // As fallback, check local cache (for offline support)
                return checkLocalCacheForProStatus();
            })
            .onErrorResumeNext(error -> {
                // On network error, fall back to local cache
                return Single.just(checkLocalCacheForProStatus());
            });
    }
    
    // When purchases happen, update both Firestore and local cache atomically
    public void processPurchase(Purchase purchase) {
        // Create subscription object
        SubscriptionStatus status = new SubscriptionStatus();
        status.setLevel("pro");
        status.setExpiryDate(calculateExpiryDate(purchase));
        // etc...
        
        // Update repository - this will update both Firestore and local
        repository.updateSubscriptionStatus(status)
            .subscribe(
                () -> Log.d(TAG, "Subscription updated successfully"),
                error -> Log.e(TAG, "Error updating subscription", error)
            );
    }
}
```

#### 1.3 Replace Direct SharedPreferences Access (Week 3)

1. Scan codebase for all direct SharedPreferences calls
2. Replace with repository pattern calls
3. Add migration code to preserve existing user data

```java
// Before
boolean isPro = prefs.getBoolean("is_pro", false);

// After
repository.getSubscriptionStatus()
    .map(SubscriptionStatus::isPro)
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        isPro -> updateUiForProStatus(isPro),
        error -> handleError(error)
    );
```

### Phase 2: User Interface Updates (Estimated: 2 weeks)

#### 2.1 Add Loading States (Week 1)

```java
// Add loading state to all screens that depend on data
public class DeliveriesFragment extends Fragment {
    private DataRepository repository;
    private ProgressBar loadingIndicator;
    private RecyclerView deliveriesRecyclerView;
    private TextView errorView;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Setup views...
        
        // Show loading state
        loadingIndicator.setVisibility(View.VISIBLE);
        deliveriesRecyclerView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        
        // Load data with proper loading states
        repository.getDeliveries()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                deliveries -> {
                    loadingIndicator.setVisibility(View.GONE);
                    deliveriesRecyclerView.setVisibility(View.VISIBLE);
                    adapter.setDeliveries(deliveries);
                },
                error -> {
                    loadingIndicator.setVisibility(View.GONE);
                    errorView.setVisibility(View.VISIBLE);
                    errorView.setText("Error loading deliveries: " + error.getMessage());
                }
            );
    }
}
```

#### 2.2 Update Subscription UI (Week 2)

1. Refactor all subscription-dependent UI to use the new data layer
2. Add real-time updates for subscription status changes
3. Implement offline indicators

```java
// Set up real-time subscription monitoring
private void setupSubscriptionMonitoring() {
    // Subscribe to real-time updates of subscription status
    CompositeDisposable disposables = new CompositeDisposable();
    
    disposables.add(repository.observeSubscriptionStatus()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            status -> updateSubscriptionUI(status),
            error -> Log.e(TAG, "Error monitoring subscription status", error)
        ));
        
    // Clean up in onDestroy
}

private void updateSubscriptionUI(SubscriptionStatus status) {
    // Update UI elements based on subscription status
    boolean isPro = status.isPro();
    proFeatureButton.setEnabled(isPro);
    
    if (isPro) {
        subscriptionBanner.setVisibility(View.GONE);
        proUserBadge.setVisibility(View.VISIBLE);
    } else {
        subscriptionBanner.setVisibility(View.VISIBLE);
        proUserBadge.setVisibility(View.GONE);
    }
}
```

### Phase 3: Data Synchronization & Conflict Resolution (Estimated: 2 weeks)

#### 3.1 Implement Offline Support (Week 1)

```java
// Enable offline capabilities in FirestoreRepository
public FirestoreRepository(FirebaseFirestore db, SharedPreferences prefs) {
    this.db = db;
    this.prefs = prefs;
    
    // Enable disk persistence for offline support
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
        .build();
    db.setFirestoreSettings(settings);
    
    // Set up network monitoring
    connectivityMonitor.observe()
        .subscribe(isConnected -> {
            if (isConnected) {
                // Trigger sync when coming back online
                syncOfflineChanges();
            }
        });
}
```

#### 3.2 Add Conflict Resolution (Week 2)

```java
// Add version tracking to detect conflicts
public class UserProfile {
    private String userId;
    private String email;
    private boolean isPro;
    private long version; // For conflict detection
    
    // Fields, getters, setters...
}

// Implement conflict resolution in repository
private Completable updateWithConflictResolution(DocumentReference docRef, 
                                                UserProfile newData) {
    return Completable.create(emitter -> {
        db.runTransaction(transaction -> {
            // Get the current data
            DocumentSnapshot snapshot = transaction.get(docRef);
            UserProfile currentData = snapshot.toObject(UserProfile.class);
            
            // Check for conflicts
            if (currentData.getVersion() > newData.getVersion()) {
                // Conflict detected - server data is newer
                // Implement merge strategy based on field priority
                UserProfile merged = mergeProfiles(currentData, newData);
                
                // Update with merged data
                transaction.set(docRef, merged);
                return merged;
            } else {
                // No conflict or local data is newer
                // Increment version
                newData.setVersion(currentData.getVersion() + 1);
                transaction.set(docRef, newData);
                return newData;
            }
        }).addOnSuccessListener(result -> {
            // Also update local cache with the final result
            updateLocalCache(result);
            emitter.onComplete();
        }).addOnFailureListener(emitter::onError);
    });
}

// Field-specific merge strategy
private UserProfile mergeProfiles(UserProfile server, UserProfile local) {
    UserProfile merged = new UserProfile();
    
    // Copy all server data first
    merged.setUserId(server.getUserId());
    merged.setEmail(server.getEmail());
    merged.setVersion(server.getVersion());
    
    // For some fields, server always wins
    merged.setIsPro(server.isPro()); // Subscription status from server is authoritative
    
    // For other fields, take the most recent changes
    // This requires keeping track of field-level modification times
    
    return merged;
}
```

### Phase 4: Testing & Performance Optimization (Estimated: 2 weeks)

#### 4.1 Comprehensive Testing Suite (Week 1)

1. Unit tests for repository pattern and data synchronization
2. Instrumentation tests for offline/online transitions
3. UI tests for subscription status changes

```java
// Sample unit test for repository
@Test
public void subscriptionStatus_whenFirestoreHasProTrue_returnsPro() {
    // Arrange
    FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
    DocumentReference mockDocRef = mock(DocumentReference.class);
    DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
    
    when(mockDb.collection("users")).thenReturn(mockCollection);
    when(mockCollection.document(anyString())).thenReturn(mockDocRef);
    when(mockDocRef.get()).thenReturn(Tasks.forResult(mockSnapshot));
    
    // Mock a pro user in Firestore
    when(mockSnapshot.exists()).thenReturn(true);
    when(mockSnapshot.getBoolean("pro")).thenReturn(true);
    
    // Mock local preferences as non-pro
    SharedPreferences mockPrefs = mock(SharedPreferences.class);
    when(mockPrefs.getString(eq("subscription_status"), anyString())).thenReturn("free");
    
    // Create repository with mocks
    DataRepository repository = new FirestoreRepository(mockDb, mockPrefs);
    
    // Act
    TestObserver<Boolean> testObserver = repository.getSubscriptionStatus()
        .map(SubscriptionStatus::isPro)
        .test();
    
    // Assert
    testObserver.assertValue(true); // Should return true from Firestore, not false from local
}
```

#### 4.2 Performance Optimization (Week 2)

1. Implement data prefetching on app start
2. Add query optimization for large datasets
3. Memory usage optimization

```java
// App startup optimization
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Start prefetching critical data
        DataRepository repository = ServiceLocator.getRepository();
        repository.prefetchCriticalData()
            .subscribeOn(Schedulers.io())
            .subscribe(
                () -> Log.d(TAG, "Critical data prefetched successfully"),
                error -> Log.e(TAG, "Error prefetching data", error)
            );
    }
}

// Optimized query for large datasets
public Flowable<List<Delivery>> getDeliveries() {
    return Flowable.create(emitter -> {
        // Use pagination for large datasets
        Query query = db.collection("deliveries")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20);
            
        // Listen for real-time updates with pagination
        ListenerRegistration registration = query.addSnapshotListener(
            (snapshots, error) -> {
                if (error != null) {
                    emitter.onError(error);
                    return;
                }
                
                List<Delivery> deliveries = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    deliveries.add(doc.toObject(Delivery.class));
                }
                
                emitter.onNext(deliveries);
            });
            
        // Provide a way to load more
        emitter.setCancellable(registration::remove);
    }, BackpressureStrategy.LATEST);
}
```

### Phase 5: Deployment & Migration (Estimated: 1 week)

#### 5.1 Data Migration Strategy

```java
// One-time migration to handle existing users
private void migrateExistingUsers() {
    // Get all users
    db.collection("users").get()
        .addOnSuccessListener(snapshots -> {
            for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                String userId = snapshot.getId();
                
                // Check if user has a subscription record
                db.collection("user_subscriptions")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(subscriptionDoc -> {
                        if (!subscriptionDoc.exists()) {
                            // Create subscription record based on user's pro status
                            boolean isPro = snapshot.getBoolean("pro") == Boolean.TRUE;
                            
                            SubscriptionStatus status = new SubscriptionStatus();
                            status.setUserId(userId);
                            status.setLevel(isPro ? "pro" : "free");
                            status.setExpiryDate(null); // No expiration for imported data
                            
                            // Save the new subscription record
                            db.collection("user_subscriptions")
                                .document(userId)
                                .set(status);
                        }
                    });
            }
        });
}
```

#### 5.2 Version Transition

1. Staged rollout with Firebase Remote Config
2. A/B testing to confirm improvements
3. Monitoring for synchronization issues

```java
// Use Remote Config to control feature rollout
public class SyncConfigManager {
    private final FirebaseRemoteConfig remoteConfig;
    
    public SyncConfigManager() {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        
        // Set defaults
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("use_new_sync_system", false);
        defaults.put("sync_interval_seconds", 60);
        remoteConfig.setDefaultsAsync(defaults);
        
        // Fetch config
        remoteConfig.fetch(3600) // 1 hour cache
            .addOnSuccessListener(aVoid -> remoteConfig.activate());
    }
    
    public boolean shouldUseNewSyncSystem() {
        return remoteConfig.getBoolean("use_new_sync_system");
    }
    
    public int getSyncIntervalSeconds() {
        return (int) remoteConfig.getLong("sync_interval_seconds");
    }
}

// Use this in repository initialization
public DataRepository createRepository(Context context) {
    SyncConfigManager configManager = new SyncConfigManager();
    
    if (configManager.shouldUseNewSyncSystem()) {
        return new FirestoreRepository(FirebaseFirestore.getInstance(), 
                                   context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                                   configManager);
    } else {
        // Fall back to old implementation for some users during rollout
        return new LegacyRepository(context);
    }
}
```

## Critical Success Factors

1. **Single Source of Truth**: All data operations must go through the unified DataRepository
2. **Real-Time First**: Use Firestore real-time listeners for all critical data
3. **Cache Consistency**: Local cache should always reflect server state
4. **Offline Support**: App must function when offline and sync when connection is restored
5. **Conflict Resolution**: Clear strategy for resolving conflicts between local and server data

## Implementation Timeline

**Week 1-3**: Core Architecture Refactoring
- Unified Data Layer
- Subscription System Migration
- SharedPreferences Replacement

**Week 4-5**: User Interface Updates
- Loading States
- Subscription UI Updates

**Week 6-7**: Data Synchronization & Conflict Resolution
- Offline Support
- Conflict Resolution

**Week 8-9**: Testing & Performance Optimization
- Testing Suite
- Performance Optimization

**Week 10**: Deployment & Migration
- Data Migration
- Version Transition

## Monitoring & Success Metrics

1. **Sync Failures**: Track and alert on synchronization failures
2. **Offline Usage**: Monitor how often users operate in offline mode
3. **Conflict Rate**: Percentage of updates that result in conflicts
4. **Support Tickets**: Reduction in subscription-related support tickets
5. **User Satisfaction**: Improved ratings and feedback around data consistency

This comprehensive architectural overhaul will resolve the fundamental flaws in how Autogratuity interacts with Firestore, ensuring proper synchronization between cloud and local data while maintaining good performance and offline capabilities.
