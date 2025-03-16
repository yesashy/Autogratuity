# Repository Structure - Current Implementation Overview

## Overview

This document provides an overview of the current `FirestoreRepository` implementation, which uses a modular file structure to separate concerns by functionality. This approach provides clear responsibility boundaries while enabling focused development.

## Current Structure

The repository implementation is organized across multiple focused files:

1. `FirestoreRepository.java`
   - Contains core functionality including user profile and address methods
   - Serves as the main implementation file

2. `FirestoreRepository_Constructor.java`
   - Contains constructor and initialization code
   - Sets up Firebase, caching, and network monitoring

3. `FirestoreRepository_ConfigMethods.java`
   - Contains app configuration and device management methods
   - Handles system-wide settings and device registration

4. `FirestoreRepository_DeliveryMethods.java`
   - Contains delivery-related methods
   - Manages creation, retrieval, and updates of delivery data

5. `FirestoreRepository_PreferenceMethods.java`
   - Contains helper methods for SharedPreferences
   - Provides utilities for data serialization and persistence

6. `FirestoreRepository_SyncMethods.java`
   - Contains sync operation methods
   - Manages offline operation queueing and conflict resolution

## Structure Benefits

### Development Benefits

1. **Focused Development**
   - Developers can work on specific functionality independently
   - Changes to one domain area don't affect others
   - Easier to understand specific functionality in isolation

2. **Clear Responsibility Boundaries**
   - Each file has a specific domain focus
   - Methods are grouped by related functionality
   - Simplified reasoning about related operations

3. **Improved Maintainability**
   - Smaller files are easier to navigate
   - Domain-specific changes are localized
   - Code organization follows functional boundaries

### Technical Benefits

1. **Simplified Testing**
   - Testing can focus on specific domains
   - Test classes can mirror the implementation structure
   - Smaller surface area for each test class

2. **Controlled Dependencies**
   - Dependencies can be managed at the file level
   - Clearer visibility into cross-domain dependencies
   - Reduced risk of circular dependencies

3. **Version Control Advantages**
   - Domain-specific changes create focused commits
   - Reduced merge conflicts when multiple developers work concurrently
   - Clearer history of functional changes

## Implementation Details

### Data Flow

The repository acts as a mediator between different data sources:

1. **Cloud Data (Firestore)**
   - Primary authoritative source
   - Real-time listening for critical collections
   - Batch operations for efficiency

2. **Local Persistence (SharedPreferences)**
   - Secondary persistence for offline access
   - Fallback when network is unavailable
   - Temporary storage for pending operations

3. **Memory Cache**
   - Primary retrieval source for performance
   - Time-based invalidation (TTL)
   - Targeted invalidation on updates

### Key Components

1. **Network Monitoring**
   - Real-time network state tracking
   - Automatic sync when connection is restored
   - Observable network state for UI components

2. **Three-Tier Caching**
   - Memory cache for fast access
   - SharedPreferences for persistent local storage
   - Firestore for cloud storage and synchronization

3. **Conflict Resolution**
   - Version-based tracking for detecting conflicts
   - Configurable resolution strategies (server wins, client wins, merge)
   - Field-level merging for complex objects

## Interface Structure

The repository implements the `DataRepository` interface, which includes:

1. **Profile Operations**
   - Get/update user profile
   - Observe real-time profile changes
   - Update specific profile fields

2. **Subscription Operations**
   - Get/update subscription status
   - Verify subscription with payment provider
   - Add subscription records

3. **Address Operations**
   - Get/add/update/delete addresses
   - Find addresses by ID or normalized address
   - Observe address changes

4. **Delivery Operations**
   - Manage delivery records
   - Track delivery statistics
   - Filter deliveries by criteria

5. **Sync Operations**
   - Queue operations for offline use
   - Process pending operations
   - Monitor sync status

6. **Config Operations**
   - Get application configuration
   - Register/update devices
   - Observe config changes

## Usage Patterns

### Repository Access

All access to the repository should go through the `RepositoryProvider`:

```java
DataRepository repository = RepositoryProvider.getRepository();
```

### Asynchronous Operations

All repository methods return RxJava types:

1. `Single<T>` for one-time retrieval operations
2. `Completable` for operations with no return value
3. `Observable<T>` for real-time data streams

Example usage:

```java
repository.getAddresses()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        addresses -> updateUI(addresses),
        error -> handleError(error)
    );
```

### Real-Time Updates

For data that needs real-time updates, use observe methods:

```java
CompositeDisposable disposables = new CompositeDisposable();

disposables.add(
    repository.observeAddresses()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            addresses -> updateUI(addresses),
            error -> handleError(error)
        )
);

// In onDestroy/onDestroyView
disposables.clear();
```

## Future Considerations

While the current structure is serving the project well, future expansion might include:

1. **Enhanced Documentation**
   - More comprehensive method documentation
   - Examples of common usage patterns
   - Architecture diagrams

2. **Testing Infrastructure**
   - Mock implementations for testing
   - Test utilities for common scenarios
   - Integration tests for cross-domain operations

3. **Performance Optimization**
   - More sophisticated cache invalidation
   - Query optimization for large datasets
   - Batched operations for bulk changes

The current modular file structure provides a solid foundation that allows for independent development while maintaining a cohesive architectural approach.
