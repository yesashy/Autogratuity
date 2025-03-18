package com.autogratuity.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.model.SyncStatus;
import com.autogratuity.data.repository.subscription.SubscriptionRepository;
import com.autogratuity.data.repository.sync.SyncRepository;
import com.autogratuity.ui.common.BaseViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for the main activity, implementing the repository pattern.
 * Handles data operations and business logic related to the main activity.
 */
public class MainViewModel extends BaseViewModel {
    private static final String TAG = "MainViewModel";
    
    private final SyncRepository syncRepository;
    private final SubscriptionRepository subscriptionRepository;
    
    // LiveData fields for UI state
    private final MutableLiveData<SyncStatus> syncStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> pendingOperationsLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<String> formattedSyncTimeLiveData = new MutableLiveData<>("Not synced");
    private final MutableLiveData<Boolean> isProUserLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentFragmentLiveData = new MutableLiveData<>("dashboard");
    
    /**
     * Constructor with repository injection
     * 
     * @param syncRepository Repository for sync operations
     * @param subscriptionRepository Repository for subscription operations
     */
    public MainViewModel(SyncRepository syncRepository, SubscriptionRepository subscriptionRepository) {
        this.syncRepository = syncRepository;
        this.subscriptionRepository = subscriptionRepository;
    }
    
    /**
     * Get sync status as LiveData
     * 
     * @return LiveData containing sync status
     */
    public LiveData<SyncStatus> getSyncStatus() {
        return syncStatusLiveData;
    }
    
    /**
     * Get formatted sync time as LiveData
     * 
     * @return LiveData containing formatted sync time string
     */
    public LiveData<String> getFormattedSyncTime() {
        return formattedSyncTimeLiveData;
    }
    
    /**
     * Get pending operations count as LiveData
     * 
     * @return LiveData containing pending operations count
     */
    public LiveData<Integer> getPendingOperationsCount() {
        return pendingOperationsLiveData;
    }
    
    /**
     * Get pro user status as LiveData
     * 
     * @return LiveData containing boolean pro user status
     */
    public LiveData<Boolean> isProUser() {
        return isProUserLiveData;
    }
    
    /**
     * Get current fragment tag as LiveData
     * 
     * @return LiveData containing current fragment tag
     */
    public LiveData<String> getCurrentFragment() {
        return currentFragmentLiveData;
    }
    
    /**
     * Set current fragment tag
     * 
     * @param fragmentTag Fragment tag to set as current
     */
    public void setCurrentFragment(String fragmentTag) {
        currentFragmentLiveData.setValue(fragmentTag);
    }
    
    /**
     * Observe sync status changes
     * Updates the LiveData whenever sync status changes
     */
    public void observeSyncStatus() {
        disposables.add(
            syncRepository.observeSyncStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    status -> {
                        syncStatusLiveData.setValue(status);
                        updateFormattedSyncTime(status);
                    },
                    error -> {
                        Log.e(TAG, "Error observing sync status", error);
                        setError(error);
                    }
                )
        );
    }
    
    /**
     * Get pending sync operations
     * Updates the LiveData with the count of pending operations
     */
    public void getPendingSyncOperations() {
        disposables.add(
            syncRepository.getPendingSyncOperations()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    operations -> {
                        pendingOperationsLiveData.setValue(operations.size());
                    },
                    error -> {
                        Log.e(TAG, "Error getting pending operations", error);
                        setError(error);
                    }
                )
        );
    }
    
    /**
     * Check if user has pro subscription
     * Updates the LiveData with the pro user status
     */
    public void checkProStatus() {
        disposables.add(
            subscriptionRepository.isProUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    isPro -> {
                        isProUserLiveData.setValue(isPro);
                    },
                    error -> {
                        Log.e(TAG, "Error checking pro status", error);
                        setError(error);
                    }
                )
        );
    }
    
    /**
     * Perform data synchronization
     */
    public void performSync() {
        setLoading(true);
        clearError();
        
        disposables.add(
            syncRepository.syncData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        Log.d(TAG, "Sync completed successfully");
                        setLoading(false);
                        showToast("Sync completed successfully");
                        getPendingSyncOperations(); // Refresh pending operations count
                    },
                    error -> {
                        Log.e(TAG, "Error during sync", error);
                        setError(error);
                        setLoading(false);
                        showToast("Sync failed: " + error.getMessage());
                    }
                )
        );
    }
    
    /**
     * Cycle to the next main fragment
     */
    public void cycleMainFragments() {
        String currentFragment = currentFragmentLiveData.getValue();
        String nextFragment;
        
        switch (currentFragment) {
            case "dashboard":
                nextFragment = "deliveries";
                break;
            case "deliveries":
                nextFragment = "addresses";
                break;
            case "addresses":
            default:
                nextFragment = "dashboard";
                break;
        }
        
        currentFragmentLiveData.setValue(nextFragment);
    }
    
    /**
     * Helper method to format sync time for display
     */
    private void updateFormattedSyncTime(SyncStatus status) {
        if (status == null) {
            formattedSyncTimeLiveData.setValue("Not synced");
            return;
        }
        
        if (status.isInProgress()) {
            formattedSyncTimeLiveData.setValue("Syncing...");
            return;
        }
        
        if (status.isError()) {
            formattedSyncTimeLiveData.setValue("Sync failed");
            return;
        }
        
        // Format the last sync time
        Date lastSyncTime = status.getLastSyncTime();
        if (lastSyncTime != null) {
            long diffMillis = System.currentTimeMillis() - lastSyncTime.getTime();
            
            if (diffMillis < TimeUnit.MINUTES.toMillis(1)) {
                formattedSyncTimeLiveData.setValue("Synced Just now");
            } else if (diffMillis < TimeUnit.HOURS.toMillis(1)) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
                formattedSyncTimeLiveData.setValue("Synced " + minutes + "m ago");
            } else if (diffMillis < TimeUnit.DAYS.toMillis(1)) {
                long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
                formattedSyncTimeLiveData.setValue("Synced " + hours + "h ago");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
                formattedSyncTimeLiveData.setValue("Synced " + sdf.format(lastSyncTime));
            }
        } else {
            formattedSyncTimeLiveData.setValue("Never synced");
        }
    }
}
