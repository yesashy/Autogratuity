# Autogratuity Build Error Resolution Checklist

## Overview

This document provides a structured approach to resolving all build errors in the Autogratuity project. The errors have been systematically analyzed and categorized, with specific solutions provided for each issue.

### Project Context

Autogratuity has recently completed a significant architectural overhaul, transitioning from a fragmented repository structure to a comprehensive domain-based repository pattern. According to the CONSOLIDATED_CHECKLIST_V2.md, the project is approximately 93.5% complete, with most implementation tasks marked as finished as of March 17-18, 2025.

The project has also shifted strategy from cross-platform UI technologies (React Native, Flutter) to native Android UI with Material Design Components due to integration challenges with the domain-based repository architecture. The current build errors represent the final technical hurdles in completing this architectural transition.

## Error Categories

The build errors fall into the following categories:

1. **Missing Methods** - Methods referenced but not implemented in classes
2. **Type Conversion Issues** - Incompatible types between objects
3. **Lambda Expression Issues** - Variables in lambdas must be final or effectively final
4. **Constructor/Method Parameter Mismatches** - Incorrect parameter counts or types
5. **Missing Imports** - Required classes not imported
6. **Static Context Issues** - Non-static variables referenced from static contexts
7. **Resource Reference Issues** - Missing layout files, drawables, and IDs

## Repository Layer Fixes

### 1. SyncRepositoryImpl.java

This file contains the most critical issues, primarily centered around missing the `getUserId()` method and issues with error handling. According to MASTER_NOTES.md, the SyncRepository implementation should be complete, but there appear to be implementation details that need to be addressed.

#### Implementation Steps:

1. **Add getUserId() method**:
   ```java
   /**
    * Get the current user ID.
    * @return The user ID string
    */
   protected String getUserId() {
       return userId != null ? userId : getCurrentUserId();
   }
   ```

2. **Fix the type conversion in retryOperation() method** (line 846-892):
   ```java
   return Single.create(emitter -> {
       DocumentReference docRef = db.collection(COLLECTION_SYNC_OPERATIONS).document(operationId);
       docRef.get()
               .addOnSuccessListener(documentSnapshot -> {
                   if (documentSnapshot.exists()) {
                       SyncOperation operation = documentSnapshot.toObject(SyncOperation.class);
                       if (operation != null && getUserId().equals(operation.getUserId())) {
                           // Cast to SyncOperation to fix the error
                           SyncOperation syncOperation = operation;
                           syncOperation.setStatus(SyncOperation.STATUS_PENDING);
                           syncOperation.setUpdatedAt(new Date());
                           
                           // Update operation in Firestore
                           // Other code...
                           
                           // Process the operation if online
                           if (isNetworkAvailable()) {
                               processSyncOperation(syncOperation) // Now using proper cast
                                       .subscribe(
                                               () -> emitter.onComplete(),
                                               emitter::onError
                                       );
                           } else {
                               emitter.onComplete();
                           }
                       } else {
                           emitter.onError(new SecurityException("Operation does not belong to current user"));
                       }
                   } else {
                       emitter.onError(new Exception("Operation not found: " + operationId));
                   }
               })
               .addOnFailureListener(e -> {
                   Log.e(TAG, "Error getting operation for retry", e);
                   emitter.onError(e);
               });
   });
   ```

3. **Fix RepositoryProvider reference in SyncWorker class** (line 1206):
   ```java
   // Add import for RepositoryProvider
   import com.autogratuity.data.repository.core.RepositoryProvider;

   // Then in SyncWorker class:
   public Result doWork() {
       // Get repository instance
       SyncRepository syncRepo = RepositoryProvider.getInstance(getApplicationContext()).getSyncRepository();
       
       try {
           // Perform sync...
       }
   }
   ```

4. **Create Error handler for SyncOperation** (lines 579-581):
   ```java
   // Create an ErrorInfo class to handle error details
   public static class ErrorInfo {
       private String code;
       private String message;
       private String timestamp;
       
       public ErrorInfo() {}
       
       public ErrorInfo(String code, String message, Date timestamp) {
           this.code = code;
           this.message = message;
           this.timestamp = timestamp != null ? timestamp.toString() : null;
       }
       
       public String getCode() { return code; }
       public String getMessage() { return message; }
       public String getTimestamp() { return timestamp; }
   }
   
   // Then modify SyncOperation.getError() to return ErrorInfo instead of String
   public ErrorInfo getError() {
       if (error == null) return null;
       return new ErrorInfo("error", error, updatedAt != null ? updatedAt.toDate() : null);
   }
   ```

5. **Add markAsFailed method to SyncOperation class**:
   ```java
   /**
    * Mark operation as failed with error details
    * @param errorCode Error code
    * @param errorMessage Error message
    */
   public void markAsFailed(String errorCode, String errorMessage) {
       this.failed = true;
       this.completed = false;
       this.error = errorMessage;
       this.attempts++;
       
       // Calculate next retry time with exponential backoff
       long delayMillis = Math.min(1000 * (long) Math.pow(2, this.attempts), 3600000); // Max 1 hour
       Date nextAttempt = new Date(System.currentTimeMillis() + delayMillis);
       this.nextAttemptTime = new Timestamp(nextAttempt);
   }
   ```

### 2. ConfigRepositoryImpl.java

1. **Fix lambda expression issue** (line 312):
   ```java
   // Create a final copy of deviceInfo
   final Map<String, Object> finalDeviceInfo = deviceInfo;
   docRef.set(finalDeviceInfo)
       .addOnSuccessListener(aVoid -> {
           // Rest of code
       })
   ```

2. **Add missing methods to ConfigRepository interface**:
   ```java
   /**
    * Get a configuration value as a string.
    * @param key The configuration key
    * @param defaultValue Default value if not found
    * @return The configuration value
    */
   String getConfigValue(String key, String defaultValue);
   
   /**
    * Get a configuration value as a boolean.
    * @param key The configuration key
    * @param defaultValue Default value if not found
    * @return The configuration value
    */
   boolean getConfigBoolean(String key, boolean defaultValue);
   
   /**
    * Increment a counter value.
    * @param counterKey The counter key to increment
    * @return Completable that completes when counter is incremented
    */
   Completable incrementCounter(String counterKey);
   
   /**
    * Return a no-op completable that just completes immediately.
    * @return Completable that completes immediately
    */
   Completable noOpCompletable();
   ```

3. **Implement these methods in ConfigRepositoryImpl**:
   ```java
   @Override
   public String getConfigValue(String key, String defaultValue) {
       return getAppConfig()
               .map(config -> {
                   if (config.getCustomData() != null && config.getCustomData().containsKey(key)) {
                       Object value = config.getCustomData().get(key);
                       return value != null ? value.toString() : defaultValue;
                   }
                   return defaultValue;
               })
               .blockingGet();
   }
   
   @Override
   public boolean getConfigBoolean(String key, boolean defaultValue) {
       return getAppConfig()
               .map(config -> {
                   if (config.getCustomData() != null && config.getCustomData().containsKey(key)) {
                       Object value = config.getCustomData().get(key);
                       if (value instanceof Boolean) {
                           return (Boolean) value;
                       } else if (value != null) {
                           return Boolean.parseBoolean(value.toString());
                       }
                   }
                   return defaultValue;
               })
               .blockingGet();
   }
   
   @Override
   public Completable incrementCounter(String counterKey) {
       return Completable.defer(() -> {
           DocumentReference docRef = db.collection(COLLECTION_SYSTEM_CONFIG)
                   .document("counters");
           
           return Completable.create(emitter -> {
               docRef.update(counterKey, FieldValue.increment(1))
                       .addOnSuccessListener(aVoid -> emitter.onComplete())
                       .addOnFailureListener(e -> {
                           if (e instanceof FirebaseFirestoreException &&
                                   ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.NOT_FOUND) {
                               // Create if not exists
                               Map<String, Object> initialData = new HashMap<>();
                               initialData.put(counterKey, 1);
                               docRef.set(initialData)
                                       .addOnSuccessListener(aVoid -> emitter.onComplete())
                                       .addOnFailureListener(emitter::onError);
                           } else {
                               emitter.onError(e);
                           }
                       });
           });
       });
   }
   
   @Override
   public Completable noOpCompletable() {
       return Completable.complete();
   }
   ```

### 3. UserProfileSerializer.java

1. **Fix missing Log imports**:
   ```java
   import android.util.Log;
   ```

2. **Fix missing List imports**:
   ```java
   import java.util.List;
   import java.util.ArrayList;
   ```

3. **Fix Date to Timestamp conversion** (line 300):
   ```java
   // Replace
   profile.setCreatedAt(new Date());
   
   // With
   profile.setCreatedAt(new Timestamp(new Date()));
   ```

## Model Class Enhancements

### 1. SyncStatus.java

Add missing status check methods, aligning with UI-06 (MainViewModel) implementation which relies on these methods for status monitoring:

```java
/**
 * Check if sync is currently in progress
 * @return true if sync is in progress
 */
public boolean isInProgress() {
    return STATUS_SYNCING.equals(status);
}

/**
 * Check if sync has an error
 * @return true if sync has an error
 */
public boolean isError() {
    return STATUS_ERROR.equals(status) || (lastError != null && !lastError.isEmpty());
}
```

Note: These methods are used in MainActivity and MainViewModel for UI status updates and should be consistent with the sync status tracking implemented in FirestoreRepository.

### 2. Address.java

Add missing methods:

```java
/**
 * Get the unique identifier for this address
 * @return String ID
 */
public String getId() {
    return addressId;
}

/**
 * Check if address is marked as favorite
 * @return true if favorite
 */
public boolean isFavorite() {
    return favorite;
}
```

### 3. Reference.java (in Delivery class)

Add missing methods:

```java
/**
 * Get the external order ID
 * @return String order ID
 */
public String getOrderId() {
    return orderId;
}

/**
 * Get the formatted address text
 * @return String address text
 */
public String getAddressText() {
    return addressText;
}
```

### 4. Metadata.java

Add missing method:

```java
/**
 * Get custom data map
 * @return Map of custom data
 */
public Map<String, Object> getCustomData() {
    if (customData == null) {
        customData = new HashMap<>();
    }
    return customData;
}
```

### 5. Status.java (in Delivery class)

Add missing method:

```java
/**
 * Set whether this delivery should not be delivered
 * @param doNotDeliver true if should not deliver
 */
public void setDoNotDeliver(boolean doNotDeliver) {
    this.doNotDeliver = doNotDeliver;
}
```

### 6. SubscriptionStatus.java

Add missing methods:

```java
/**
 * Set the purchase token
 * @param purchaseToken The token from the purchase
 */
public void setPurchaseToken(String purchaseToken) {
    this.purchaseToken = purchaseToken;
}

/**
 * Set the last updated timestamp
 * @param lastUpdated Date when status was last updated
 */
public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated != null ? new Timestamp(lastUpdated) : null;
}
```

## Repository Interface Enhancements

### 1. PreferenceRepository.java

Add missing methods:

```java
/**
 * Set a long preference value
 * @param key Preference key
 * @param value Long value
 * @return Completable that completes when preference is saved
 */
Completable setLongPreference(String key, long value);

/**
 * Set a string preference value
 * @param key Preference key
 * @param value String value
 * @return Completable that completes when preference is saved
 */
Completable setStringPreference(String key, String value);
```

### 2. SubscriptionRepository.java

Add missing methods:

```java
/**
 * Check if trial is available for the current user
 * @return Single emitting boolean indicating trial availability
 */
Single<Boolean> isTrialAvailable();

/**
 * Get the current subscription status
 * @return Single emitting the current subscription status
 */
Single<SubscriptionStatus> getCurrentSubscriptionStatus();
```

### 3. AddressRepository.java

Add missing method:

```java
/**
 * Get addresses with the best tipping history
 * @param limit Maximum number of addresses to return
 * @return Single emitting a list of addresses
 */
Single<List<Address>> getBestTippingAddresses(int limit);
```

## Service/Activity Parameter Fixes

### 1. ProSubscribeActivity.java

Fix SubscriptionManager getInstance call:

```java
// Get required repositories
SubscriptionRepository subscriptionRepo = RepositoryProvider.getInstance(this).getSubscriptionRepository();
PreferenceRepository preferenceRepo = RepositoryProvider.getInstance(this).getPreferenceRepository();

// Initialize with correct parameters
subscriptionManager = SubscriptionManager.getInstance(this, subscriptionRepo, preferenceRepo);
```

### 2. CaptureProcessReceiver.java & ShiptCaptureBackgroundService.java

Fix ShiptCaptureProcessor instantiation:

```java
// Get required repositories
DeliveryRepository deliveryRepo = RepositoryProvider.getInstance(context).getDeliveryRepository();
AddressRepository addressRepo = RepositoryProvider.getInstance(context).getAddressRepository();

// Initialize with correct parameters
ShiptCaptureProcessor processor = new ShiptCaptureProcessor(context, deliveryRepo, addressRepo);
```

### 3. DoNotDeliverService.java

Fix AuthenticationManager getInstance call:

```java
// Initialize with context
authManager = AuthenticationManager.getInstance(this);
```

## Lambda and Type Issues

### 1. DeliveriesAdapter.java

Fix dateFormat static issues:

```java
// Make dateFormat static
private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);

// OR change the references to use a local instance
SimpleDateFormat localDateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
dateText.setText(localDateFormat.format(deliveryDate));
```

## Resource Issues

These issues involve refactoring existing XML resources to align with the new architecture:

### 1. Refactor Layout Files

1. **dialog_delivery_detail.xml** - Refactor existing dialog layout or create new version
   - Check `res/layout/` directory for existing version that may require updating
   - Ensure compatibility with new DeliveryDialogViewModel mentioned in the completed task UI-08

2. **item_recent_activity.xml** - Refactor existing item layout for recent activities
   - This should align with the DashboardFragment implementation (UI-09)
   - Should support the enhanced StatCard components that have been implemented

### 2. Update Drawable Resources

1. **ic_sync.xml** - Locate existing icon or create new vector drawable
   - Check both `res/drawable/` and `res/drawable-v24/` directories
   - Material Design icon set may contain appropriate replacements

2. **ic_error.xml** - Locate existing icon or create new vector drawable
   - Check both `res/drawable/` and `res/drawable-v24/` directories
   - Material Design icon set may contain appropriate replacements

### 3. Update ID References

Refactor layout files to include these missing IDs:
1. update_button - Required in dialog_delivery_detail.xml
2. delete_button - Required in dialog_delivery_detail.xml 
3. close_button - Required in dialog_delivery_detail.xml
4. button_favorite - Required in address item layout
5. favorite_indicator - Required in address item layout
6. try_new_ui_card - Required in DashboardFragment layout
7. amount_text - Required in item_recent_activity.xml
8. stats_text - Required in statistical view components

## Verification Steps

After implementing the fixes, follow this comprehensive verification process:

1. [ ] Run a clean build: `./gradlew clean build`
2. [ ] Check the build output for any new errors that may appear
3. [ ] Verify repository instantiation and dependency injection:
   - [ ] Confirm RepositoryProvider initialization in AutogratuityApp
   - [ ] Verify domain repositories can be instantiated without errors
   - [ ] Test initialization with and without user authentication
4. [ ] Verify ViewModels integration:
   - [ ] Test ViewModel instantiation with appropriate repositories
   - [ ] Verify proper LiveData implementation and observation
5. [ ] Run the RepositoryMigrationTest utility mentioned in MASTER_NOTES.md
6. [ ] Test application in debug mode on a device or emulator
7. [ ] Verify all key screens function properly
8. [ ] Document any remaining issues for the DT-01 documentation task

## Notes on Implementation

### Architecture Alignment

- All implementations should follow the domain-based repository pattern established in MASTER_NOTES.md
- Refer to the completed items in CONSOLIDATED_CHECKLIST_V2.md for implementation guidance and patterns
- Maintain the clear separation between repositories, ViewModels, and UI components

### Code Considerations

- Some fixes require adding new classes or methods to interfaces
- Ensure consistent error handling across repositories using RxJava error propagation patterns
- Follow Material Design guidelines for all UI components
- Properly manage RxJava disposables to prevent memory leaks

### Resource Handling

- When refactoring XML resources, maintain compatibility with existing code where possible
- Use Material Design Components instead of regular Android widgets
- Follow the theming approach described in MASTER_NOTES.md
- Check the example Material Design Components pattern in MASTER_NOTES.md for guidance

### Testing Approach

- After fixing compilation issues, verify integration with unit tests
- Test UI components for proper lifecycle management
- Verify offline functionality works correctly
- Test synchronization operations with the SyncRepository

### Documentation

- Document all significant changes made to resolve the build errors
- Update existing technical documentation to reflect the final implementation
- These changes will contribute to the pending DT-01 documentation task
