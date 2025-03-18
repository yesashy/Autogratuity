package com.autogratuity.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.data.security.AuthenticationManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Job service that marks orders without tips as "Do Not Deliver" after 14 days
 */
public class DoNotDeliverService extends JobService {
    private static final String TAG = "DoNotDeliverService";

    // Number of days after which to mark as "Do Not Deliver"
    private static final int DAYS_THRESHOLD = 14;

    private DeliveryRepository deliveryRepository;
    private AddressRepository addressRepository;
    private AuthenticationManager authManager;
    private CompositeDisposable disposables = new CompositeDisposable();
    private boolean isRunning = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        isRunning = true;
        Log.d(TAG, "Starting Do Not Deliver service");

        // Initialize repositories
        if (RepositoryProvider.isInitialized()) {
            deliveryRepository = RepositoryProvider.getDeliveryRepository();
            addressRepository = RepositoryProvider.getAddressRepository();
            authManager = AuthenticationManager.getInstance();
            
            // Run the update using RxJava
            updateOldOrders(params);
            
            // Return true to indicate we're doing work on a different thread
            return true;
        } else {
            Log.e(TAG, "RepositoryProvider not initialized, cannot proceed");
            jobFinished(params, true); // Request retry
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        isRunning = false;
        Log.d(TAG, "Do Not Deliver service stopped");
        
        // Clean up any ongoing operations
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }

        // Return true to reschedule the job if it's stopped prematurely
        return true;
    }

    private void updateOldOrders(JobParameters params) {
        // Only proceed if user is authenticated
        if (!authManager.isAuthenticated()) {
            Log.d(TAG, "No user logged in, skipping update");
            jobFinished(params, false);
            return;
        }

        // Calculate the threshold date (14 days ago)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -DAYS_THRESHOLD);
        Date thresholdDate = calendar.getTime();

        // Add the operation to our disposables for proper lifecycle management
        disposables.add(
            deliveryRepository.getUntippedDeliveries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    // onSuccess
                    deliveries -> {
                        if (deliveries.isEmpty()) {
                            Log.d(TAG, "No untipped deliveries to update");
                            jobFinished(params, false);
                            return;
                        }
                        
                        // Filter deliveries older than threshold
                        List<Delivery> oldDeliveries = filterOldDeliveries(deliveries, thresholdDate);
                        
                        if (oldDeliveries.isEmpty()) {
                            Log.d(TAG, "No old untipped deliveries to update");
                            jobFinished(params, false);
                            return;
                        }
                        
                        // Process the old deliveries
                        processOldDeliveries(oldDeliveries, params);
                    },
                    // onError
                    error -> {
                        Log.e(TAG, "Error querying untipped deliveries", error);
                        jobFinished(params, true); // Retry on failure
                    }
                )
        );
    }
    
    private List<Delivery> filterOldDeliveries(List<Delivery> deliveries, Date thresholdDate) {
        return io.reactivex.Observable.fromIterable(deliveries)
            .filter(delivery -> {
                if (delivery.getTimes() == null || delivery.getTimes().getCompletedAt() == null) {
                    return false;
                }
                return delivery.getTimes().getCompletedAt().before(thresholdDate);
            })
            .toList()
            .blockingGet();
    }

    private void processOldDeliveries(List<Delivery> oldDeliveries, JobParameters params) {
        AtomicInteger pendingUpdates = new AtomicInteger(oldDeliveries.size());
        
        for (Delivery delivery : oldDeliveries) {
            updateDeliveryAndAddress(delivery, pendingUpdates, params);
        }
    }
    
    private void updateDeliveryAndAddress(Delivery delivery, AtomicInteger pendingUpdates, JobParameters params) {
        // Set Do Not Deliver flag
        if (delivery.getStatus() == null) {
            delivery.setStatus(new Delivery.Status());
        }
        delivery.getStatus().setDoNotDeliver(true);
        
        // Update the delivery
        disposables.add(
            deliveryRepository.updateDelivery(delivery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    // onComplete
                    () -> {
                        Log.d(TAG, "Delivery " + delivery.getDeliveryId() + " marked as Do Not Deliver");
                        
                        // Also update the associated address if exists
                        if (delivery.getAddress() != null && delivery.getAddress().getAddressId() != null) {
                            updateAddress(delivery.getAddress().getAddressId(), pendingUpdates, params);
                        } else if (delivery.getReference() != null && delivery.getReference().getAddressId() != null) {
                            updateAddress(delivery.getReference().getAddressId(), pendingUpdates, params);
                        } else {
                            // No address to update
                            if (pendingUpdates.decrementAndGet() == 0) {
                                jobFinished(params, false);
                            }
                        }
                    },
                    // onError
                    error -> {
                        Log.e(TAG, "Error updating delivery", error);
                        if (pendingUpdates.decrementAndGet() == 0) {
                            jobFinished(params, true); // Retry on failure
                        }
                    }
                )
        );
    }
    
    private void updateAddress(String addressId, AtomicInteger pendingUpdates, JobParameters params) {
        disposables.add(
            addressRepository.getAddressById(addressId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    // onSuccess
                    address -> {
                        // Update the flag
                        if (address.getFlags() == null) {
                            address.setFlags(new Address.Flags());
                        }
                        address.getFlags().setDoNotDeliver(true);
                        
                        // Save the updated address
                        disposables.add(
                            addressRepository.updateAddress(address)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    // onComplete
                                    () -> {
                                        Log.d(TAG, "Address marked as Do Not Deliver: " + addressId);
                                        if (pendingUpdates.decrementAndGet() == 0) {
                                            jobFinished(params, false);
                                        }
                                    },
                                    // onError
                                    error -> {
                                        Log.e(TAG, "Error updating address", error);
                                        if (pendingUpdates.decrementAndGet() == 0) {
                                            jobFinished(params, true); // Retry on failure
                                        }
                                    }
                                )
                        );
                    },
                    // onError
                    error -> {
                        Log.e(TAG, "Error querying address", error);
                        if (pendingUpdates.decrementAndGet() == 0) {
                            jobFinished(params, true); // Retry on failure
                        }
                    }
                )
        );
    }
}