package com.autogratuity.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.subscription.SubscriptionRepository;
import com.autogratuity.services.NotificationPersistenceService;
import com.autogratuity.services.ShiptCaptureBackgroundService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Receiver that starts our services when the device boots up
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, starting services");

            // Always start notification service (available to all users)
            startNotificationService(context);

            // Check subscription status through repository
            if (RepositoryProvider.isInitialized()) {
                SubscriptionRepository subscriptionRepository = RepositoryProvider.getSubscriptionRepository();
                
                // Use RxJava to check subscription status
                disposables.add(
                    subscriptionRepository.getCurrentSubscriptionStatus()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            // onSuccess
                            status -> {
                                if (status != null && status.isActive()) {
                                    startCaptureService(context);
                                } else {
                                    Log.d(TAG, "User does not have pro access, not starting capture service");
                                }
                                // Clean up resources
                                disposables.clear();
                            },
                            // onError
                            error -> {
                                Log.e(TAG, "Error checking subscription status", error);
                                // Default to not starting pro service on error
                                disposables.clear();
                            }
                        )
                );
            } else {
                Log.e(TAG, "RepositoryProvider not initialized, cannot check subscription status");
                // Initialize repositories if possible, otherwise fall back to non-pro behavior
                try {
                    RepositoryProvider.initialize(context);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to initialize repositories", e);
                }
            }
        }
    }
    
    private void startNotificationService(Context context) {
        Intent notificationService = new Intent(context, NotificationPersistenceService.class);
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(notificationService);
            } else {
                context.startService(notificationService);
            }
            Log.d(TAG, "Notification service started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start notification service", e);
        }
    }
    
    private void startCaptureService(Context context) {
        Intent captureService = new Intent(context, ShiptCaptureBackgroundService.class);
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(captureService);
            } else {
                context.startService(captureService);
            }
            Log.d(TAG, "Capture service started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start capture service", e);
        }
    }
}