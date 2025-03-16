# Obsolete Files

The following files are now obsolete due to the new repository implementation:

## Old Repository Structure
These files have been completely replaced by our new repository pattern implementation:

1. `app/src/main/java/com/autogratuity/repositories/IFirestoreRepository.java`
   - Replaced by our new `DataRepository` interface which provides a more comprehensive API
   - The new interface uses RxJava instead of Google Tasks for better async handling

2. `app/src/main/java/com/autogratuity/repositories/FirestoreRepository.java`
   - The original implementation that used direct Firestore calls
   - Replaced by our new `FirestoreRepository` which implements the `DataRepository` interface

3. `app/src/main/java/com/autogratuity/repositories/CachedFirestoreRepository.java`
   - Implemented simple caching but had limited offline support
   - Our new repository has built-in multi-level caching (memory, SharedPreferences, Firestore)
   - The cache implementation in `FirestoreRepository` is more robust with proper invalidation

## Model Classes
The old model classes are obsolete and should be replaced by our new model classes:

1. `app/src/main/java/com/autogratuity/models/Address.java`
   - Replaced by `com.autogratuity.data.model.Address`
   - The new model aligns with the updated Firestore structure

2. `app/src/main/java/com/autogratuity/models/Delivery.java`
   - Replaced by `com.autogratuity.data.model.Delivery`
   - The new model includes more comprehensive metadata and status tracking

3. `app/src/main/java/com/autogratuity/models/DeliveryData.java`
   - This class is no longer needed as our new model structure is more comprehensive

## Shared Preferences Utilities

Any direct SharedPreferences utilities have been replaced by:

1. `com.autogratuity.data.local.PreferenceManager`
   - Provides type-safe access to SharedPreferences with JSON serialization
   - Uses GSON for proper serialization/deserialization of complex objects

2. `com.autogratuity.data.local.JsonSerializer`
   - Handles serialization of model objects to/from JSON
   - Includes special handling for Firebase Timestamp

## Current Implementation Structure

Our new implementation maintains a modular structure with separate files for different functionality:

1. `FirestoreRepository.java` - Core implementation with main functionality
2. `FirestoreRepository_ConfigMethods.java` - Configuration methods
3. `FirestoreRepository_DeliveryMethods.java` - Delivery methods
4. `FirestoreRepository_SyncMethods.java` - Sync methods
5. `FirestoreRepository_PreferenceMethods.java` - Preference helper methods
6. `FirestoreRepository_Constructor.java` - Constructor and initialization

This separation of concerns allows for focused development and maintenance while preserving the cohesive repository pattern approach.

## Migration Approach

1. Fully implement the new repository pattern with all required functionality
2. Update UI components to use the new repository pattern
3. Remove references to the old repository classes
4. Once all components are migrated, delete the obsolete files
