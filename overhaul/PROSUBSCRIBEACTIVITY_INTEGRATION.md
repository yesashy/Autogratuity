# ProSubscribeActivity Integration Plan

## Overview

This document outlines the implementation plan for integrating `ProSubscribeActivity` with the new repository architecture. The subscription system is a critical component of Autogratuity, and this integration will ensure proper synchronization between Firestore and local subscription state, providing a more reliable user experience.

## Current Issues

The existing `ProSubscribeActivity` has several architectural problems:

1. Direct access to Firestore without proper caching
2. Manual SharedPreferences management for offline subscription status
3. No real-time subscription updates
4. Inconsistent error handling
5. Lack of proper loading states

## Implementation Goals

1. Replace direct Firestore calls with repository methods
2. Implement proper loading, success, and error states
3. Use real-time subscription status updates via observables
4. Ensure proper lifecycle management with RxJava disposables
5. Maintain backward compatibility with existing billing logic

## Implementation Steps

### 1. Update Dependencies and Imports

```java
// Add these imports
import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.repository.DataRepository;
import com.autogratuity.data.repository.RepositoryProvider;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
```

### 2. Add Repository and RxJava Components

```java
// Class-level variables
private DataRepository repository;
private CompositeDisposable disposables = new CompositeDisposable();
private SubscriptionStatus currentSubscriptionStatus;
```

### 3. Initialize Repository in onCreate

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pro_subscribe);
    
    // Initialize repository
    repository = RepositoryProvider.getRepository();
    
    // Initialize UI components
    initializeViews();
    
    // Load current subscription status
    loadSubscriptionStatus();
}
```

### 4. Implement Loading and UI State Management

```java
// Add class variables for UI components
private View contentContainer;
private ProgressBar loadingIndicator;
private TextView errorView;

private void initializeViews() {
    // Find views
    contentContainer = findViewById(R.id.content_container);
    loadingIndicator = findViewById(R.id.loading_indicator);
    errorView = findViewById(R.id.error_view);
    
    // ... existing view initialization
}

private void showLoading(boolean isLoading) {
    loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    
    if (isLoading) {
        contentContainer.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }
}

private void showContent() {
    loadingIndicator.setVisibility(View.GONE);
    contentContainer.setVisibility(View.VISIBLE);
    errorView.setVisibility(View.GONE);
}

private void showError(String message) {
    loadingIndicator.setVisibility(View.GONE);
    contentContainer.setVisibility(View.GONE);
    
    errorView.setText(message);
    errorView.setVisibility(View.VISIBLE);
}
```

### 5. Load Subscription Status

```java
private void loadSubscriptionStatus() {
    showLoading(true);
    
    disposables.add(
        repository.getSubscriptionStatus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::onSubscriptionStatusLoaded,
                this::onError
            )
    );
}

private void onSubscriptionStatusLoaded(SubscriptionStatus status) {
    showContent();
    currentSubscriptionStatus = status;
    updateUI(status);
}

private void onError(Throwable error) {
    Log.e(TAG, "Error loading subscription status", error);
    showError("Error loading subscription details: " + error.getMessage());
}

private void updateUI(SubscriptionStatus status) {
    // Update UI based on subscription status
    boolean isPro = status.isPro();
    
    // Update subscription type text
    subscriptionTypeTextView.setText(isPro ? "PRO" : "FREE");
    
    // Update subscription details
    if (isPro) {
        if (status.isLifetime()) {
            subscriptionDetailsTextView.setText("Lifetime Subscription");
        } else if (status.getExpiryDate() != null) {
            String expiryDate = dateFormat.format(status.getExpiryDate());
            subscriptionDetailsTextView.setText("Expires: " + expiryDate);
        } else {
            subscriptionDetailsTextView.setText("Active Subscription");
        }
        
        // Update UI for PRO users
        subscribeButton.setText("Manage Subscription");
        benefitsContainer.setVisibility(View.GONE);
        proFeaturesContainer.setVisibility(View.VISIBLE);
    } else {
        // Update UI for free users
        subscribeButton.setText("Upgrade to PRO");
        benefitsContainer.setVisibility(View.VISIBLE);
        proFeaturesContainer.setVisibility(View.GONE);
    }
}
```

### 6. Observe Real-Time Subscription Changes

```java
private void observeSubscriptionStatus() {
    disposables.add(
        repository.observeSubscriptionStatus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::onSubscriptionStatusLoaded,
                this::onError
            )
    );
}

@Override
protected void onResume() {
    super.onResume();
    // Start observing subscription status when activity is visible
    observeSubscriptionStatus();
}
```

### 7. Update Purchase Processing Logic

```java
private void processPurchase(Purchase purchase) {
    showLoading(true);
    
    // Create subscription status object
    SubscriptionStatus status = new SubscriptionStatus();
    status.setUserId(repository.getUserId());
    status.setStatus("pro");
    status.setActive(true);
    
    // Set subscription details based on purchase
    if (LIFETIME_SKU.equals(purchase.getSku())) {
        status.setLifetime(true);
        status.setExpiryDate(null);
    } else {
        status.setLifetime(false);
        
        // Calculate expiry date based on subscription type
        Date expiryDate = calculateExpiryDate(purchase);
        status.setExpiryDate(expiryDate);
    }
    
    // Set purchase details
    status.setOrderId(purchase.getOrderId());
    status.setProvider("google_play");
    status.setStartDate(new Date());
    
    // Update subscription in repository
    disposables.add(
        repository.updateSubscriptionStatus(status)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                    showContent();
                    Toast.makeText(this, "Subscription updated successfully", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e(TAG, "Error updating subscription", error);
                    showError("Error updating subscription: " + error.getMessage());
                }
            )
    );
}
```

### 8. Implement Proper Lifecycle Management

```java
@Override
protected void onPause() {
    super.onPause();
    // Clear all subscription observers to prevent memory leaks
    disposables.clear();
}

@Override
protected void onDestroy() {
    // Ensure all disposables are cleared
    if (disposables != null && !disposables.isDisposed()) {
        disposables.dispose();
    }
    super.onDestroy();
}
```

### 9. Update Layout XML

Update the activity_pro_subscribe.xml layout to add loading and error views:

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Content Container -->
    <ScrollView
        android:id="@+id/content_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        <!-- Existing layout content -->
        
    </ScrollView>

    <!-- Loading Indicator -->
    <ProgressBar
        android:id="@+id/loading_indicator"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:visibility="gone" />

    <!-- Error View -->
    <TextView
        android:id="@+id/error_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:layout_marginStart="32dp"
        android:layout_marginEnd="32dp"
        android:gravity="center"
        android:textColor="@android:color/holo_red_dark"
        android:textSize="16sp"
        android:visibility="gone"
        tools:text="Error message" />

</RelativeLayout>
```

## Testing Plan

1. **Basic Functionality Testing**
   - Verify loading, error, and content states display correctly
   - Confirm subscription status is properly loaded and displayed
   - Ensure real-time updates are received when subscription changes

2. **Edge Case Testing**
   - Test behavior when offline
   - Verify subscription recognition after app restart
   - Test handling of subscription verification failures

3. **Purchase Flow Testing**
   - Verify new subscriptions are properly recorded in Firestore
   - Confirm subscription updates are reflected in UI immediately
   - Test subscription expiration handling

4. **Integration Testing**
   - Verify subscription status propagates to other app components
   - Test behavior when switching between subscription plans
   - Confirm backward compatibility with existing billing features

## Completion Criteria

The integration will be considered complete when:

1. All direct Firestore calls are replaced with repository methods
2. Loading, error, and content states are properly implemented
3. Real-time subscription updates are working correctly
4. The purchase flow successfully updates both Firestore and local state
5. The UI correctly reflects the current subscription status
6. All tests pass successfully

## Timeline

This integration should take approximately 1-2 days to complete, with an additional 1 day for testing and refinement. The target completion date is March 21, 2025, in line with the overall project timeline.
