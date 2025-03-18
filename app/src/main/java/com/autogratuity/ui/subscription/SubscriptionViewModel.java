package com.autogratuity.ui.subscription;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.Purchase;
import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.repository.subscription.SubscriptionRepository;
import com.autogratuity.ui.common.BaseViewModel;

import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for subscription management, implementing the repository pattern.
 * Handles business logic related to pro subscriptions and payment processing.
 */
public class SubscriptionViewModel extends BaseViewModel {
    private static final String TAG = "SubscriptionViewModel";
    
    private final SubscriptionRepository subscriptionRepository;
    private final MutableLiveData<SubscriptionStatus> subscriptionStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTrialAvailableLiveData = new MutableLiveData<>(false);
    
    /**
     * Constructor with repository injection
     * 
     * @param subscriptionRepository Repository for subscription operations
     */
    public SubscriptionViewModel(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }
    
    /**
     * Get subscription status as LiveData
     * 
     * @return LiveData containing subscription status
     */
    public LiveData<SubscriptionStatus> getSubscriptionStatus() {
        return subscriptionStatusLiveData;
    }
    
    /**
     * Get trial availability as LiveData
     * 
     * @return LiveData containing trial availability status
     */
    public LiveData<Boolean> isTrialAvailable() {
        return isTrialAvailableLiveData;
    }
    
    /**
     * Load subscription status
     */
    public void loadSubscriptionStatus() {
        setLoading(true);
        clearError();
        
        disposables.add(
            subscriptionRepository.getSubscriptionStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    status -> {
                        subscriptionStatusLiveData.setValue(status);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading subscription status", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Observe subscription status changes
     */
    public void observeSubscriptionStatus() {
        disposables.add(
            subscriptionRepository.observeSubscriptionStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    status -> {
                        subscriptionStatusLiveData.setValue(status);
                    },
                    error -> {
                        Log.e(TAG, "Error observing subscription status", error);
                        setError(error);
                    }
                )
        );
    }
    
    /**
     * Check if trial is available
     */
    public void checkTrialAvailability() {
        setLoading(true);
        clearError();
        
        disposables.add(
            subscriptionRepository.isTrialAvailable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    isAvailable -> {
                        isTrialAvailableLiveData.setValue(isAvailable);
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error checking trial availability", error);
                        setError(error);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Start a free trial
     */
    public void startFreeTrial() {
        setLoading(true);
        clearError();
        
        // Create a trial subscription
        SubscriptionStatus trialStatus = new SubscriptionStatus();
        trialStatus.setStatus("trial");
        trialStatus.setActive(true);
        
        // Set start date to now
        Date startDate = new Date();
        trialStatus.setStartDate(startDate);
        
        // Set expiry date to 7 days from now
        long expiryTimeMillis = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 days in milliseconds
        Date expiryDate = new Date(expiryTimeMillis);
        trialStatus.setExpiryDate(expiryDate);
        
        // Update subscription status in repository
        disposables.add(
            subscriptionRepository.updateSubscriptionStatus(trialStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        setLoading(false);
                        showToast("Your 7-day free trial has started!");
                        loadSubscriptionStatus(); // Refresh subscription status
                    },
                    error -> {
                        Log.e(TAG, "Error starting free trial", error);
                        setError(error);
                        setLoading(false);
                        showToast("Error starting free trial: " + error.getMessage());
                    }
                )
        );
    }
    
    /**
     * Process a purchase
     * 
     * @param purchase Purchase to process
     * @param productId Product ID of the purchase
     */
    public void processPurchase(Purchase purchase, String productId) {
        setLoading(true);
        clearError();
        
        // Create subscription status from purchase
        SubscriptionStatus status = new SubscriptionStatus();
        status.setStatus("pro");
        status.setActive(true);
        status.setOrderId(purchase.getOrderId());
        status.setProvider("google_play");
        
        // Get current date for start date
        Date startDate = new Date(purchase.getPurchaseTime());
        status.setStartDate(startDate);
        
        // Set lifetime flag for lifetime purchase
        boolean isLifetime = "pro_lifetime".equals(productId);
        status.setLifetime(isLifetime);
        
        // Set expiry date for non-lifetime purchases
        if (!isLifetime) {
            // For a real implementation, this would calculate based on subscription duration
            // For now, just add appropriate time to the purchase date
            long durationMillis = 0;
            if ("pro_monthly".equals(productId)) {
                durationMillis = 30L * 24 * 60 * 60 * 1000; // 30 days
            } else if ("pro_yearly".equals(productId)) {
                durationMillis = 365L * 24 * 60 * 60 * 1000; // 365 days
            }
            
            long expiryTimeMillis = purchase.getPurchaseTime() + durationMillis;
            Date expiryDate = new Date(expiryTimeMillis);
            status.setExpiryDate(expiryDate);
        }
        
        // Update subscription status in repository
        disposables.add(
            subscriptionRepository.updateSubscriptionStatus(status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        setLoading(false);
                        showToast("Thank you for your purchase!");
                        loadSubscriptionStatus(); // Refresh subscription status
                    },
                    error -> {
                        Log.e(TAG, "Error updating subscription", error);
                        setError(error);
                        setLoading(false);
                        showToast("Error updating subscription: " + error.getMessage());
                    }
                )
        );
    }
}
