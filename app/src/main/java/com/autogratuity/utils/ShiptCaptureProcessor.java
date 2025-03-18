package com.autogratuity.utils;

import android.content.Context;
import android.util.Log;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.Reference;
import com.autogratuity.data.model.Status;
import com.autogratuity.data.model.Times;
import com.autogratuity.data.model.Metadata;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.delivery.DeliveryRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Processes captured Shipt data and integrates it with main app database
 * Updated to use domain repositories with RxJava
 */
public class ShiptCaptureProcessor {
    private static final String TAG = "ShiptCaptureProcessor";

    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final Context context;
    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * Create a new ShiptCaptureProcessor
     *
     * @param context The application context
     * @param deliveryRepository The repository for delivery operations
     * @param addressRepository The repository for address operations
     */
    public ShiptCaptureProcessor(Context context, 
                               DeliveryRepository deliveryRepository,
                               AddressRepository addressRepository) {
        this.context = context;
        this.deliveryRepository = deliveryRepository;
        this.addressRepository = addressRepository;
    }

    /**
     * Process all unprocessed captures
     *
     * @param callback Callback for processing results
     */
    public void processCaptures(ProcessCallback callback) {
        // Load unprocessed captures using a query to find captures with processed=false
        disposables.add(
            deliveryRepository.getAllDeliveries()
                .map(allDeliveries -> {
                    List<Delivery> unprocessed = new ArrayList<>();
                    for (Delivery delivery : allDeliveries) {
                        if (delivery.getMetadata() != null &&
                            "shipt_capture".equals(delivery.getMetadata().getSource()) &&
                            (delivery.getStatus() == null || !delivery.getStatus().isProcessed())) {
                            unprocessed.add(delivery);
                        }
                    }
                    return unprocessed;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    unprocessedCaptures -> {
                        if (unprocessedCaptures.isEmpty()) {
                            if (callback != null) callback.onComplete(0);
                            return;
                        }

                        final AtomicInteger processedCount = new AtomicInteger(0);
                        final AtomicInteger errorCount = new AtomicInteger(0);
                        final int totalCount = unprocessedCaptures.size();

                        for (Delivery capture : unprocessedCaptures) {
                            processSingleCapture(capture, new ProcessCallback() {
                                @Override
                                public void onComplete(int count) {
                                    processedCount.incrementAndGet();
                                    checkCompletion();
                                }

                                @Override
                                public void onError(Exception e) {
                                    errorCount.incrementAndGet();
                                    Log.e(TAG, "Error processing capture: " + 
                                            capture.getDeliveryId(), e);
                                    checkCompletion();
                                }

                                private void checkCompletion() {
                                    if (processedCount.get() + errorCount.get() >= totalCount) {
                                        if (callback != null) {
                                            callback.onComplete(processedCount.get());
                                        }
                                    }
                                }
                            });
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error getting captures", error);
                        if (callback != null) callback.onError(error);
                    }
                )
        );
    }

    /**
     * Process a single capture
     *
     * @param capture The capture to process
     * @param callback Callback for processing result
     */
    private void processSingleCapture(Delivery capture, ProcessCallback callback) {
        // Skip invalid captures
        if (capture.getMetadata() == null || capture.getMetadata().getOrderId() == null) {
            markAsProcessed(capture, false);
            if (callback != null) callback.onError(
                    new Exception("Missing order ID"));
            return;
        }

        String orderId = capture.getMetadata().getOrderId();
        String location = "";

        // Determine the location string to use
        if (capture.getAddress() != null && capture.getAddress().getFullAddress() != null) {
            location = capture.getAddress().getFullAddress();
        } else if (capture.getNotes() != null && !capture.getNotes().isEmpty()) {
            location = capture.getNotes();
        } else {
            // Skip capture if no location info
            markAsProcessed(capture, false);
            if (callback != null) callback.onError(
                    new Exception("Missing location information"));
            return;
        }

        // Create or update the delivery
        addOrUpdateDelivery(orderId, location, capture, callback);
    }

    /**
     * Add or update a delivery in the main database
     *
     * @param orderId The order ID
     * @param location The location
     * @param sourceCapture The source capture
     * @param callback Callback for processing result
     */
    private void addOrUpdateDelivery(String orderId, String location, 
                                   Delivery sourceCapture, ProcessCallback callback) {
        // Check if delivery already exists by finding one with matching order ID
        disposables.add(
            deliveryRepository.getAllDeliveries()
                .map(allDeliveries -> {
                    for (Delivery delivery : allDeliveries) {
                        if (delivery.getMetadata() != null && 
                            orderId.equals(delivery.getMetadata().getOrderId())) {
                            return delivery;
                        }
                    }
                    return null; // No matching delivery found
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    existingDelivery -> {
                        if (existingDelivery != null) {
                            // Update existing delivery
                            updateExistingDelivery(existingDelivery, location, sourceCapture, callback);
                        } else {
                            // Create new delivery
                            createNewDelivery(orderId, location, sourceCapture, callback);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error checking for existing delivery", error);
                        if (callback != null) callback.onError(error);
                    }
                )
        );
    }

    /**
     * Update an existing delivery
     *
     * @param existingDelivery The existing delivery to update
     * @param location The location string
     * @param sourceCapture The source capture
     * @param callback Callback for processing result
     */
    private void updateExistingDelivery(Delivery existingDelivery, String location, 
                                      Delivery sourceCapture, ProcessCallback callback) {
        // Update only if new location is more specific
        boolean shouldUpdateAddress = false;
        
        if (existingDelivery.getAddress() == null || 
            existingDelivery.getAddress().getFullAddress() == null ||
            (location.length() > existingDelivery.getAddress().getFullAddress().length())) {
            shouldUpdateAddress = true;
        }
        
        if (shouldUpdateAddress) {
            // Update the address with new location
            if (existingDelivery.getAddress() == null) {
                // Create new address if none exists
                Address address = new Address();
                address.setFullAddress(location);
                address.setNormalizedAddress(addressRepository.normalizeAddress(location));
                existingDelivery.setAddress(address);
            } else {
                // Update existing address
                existingDelivery.getAddress().setFullAddress(location);
                existingDelivery.getAddress().setNormalizedAddress(
                        addressRepository.normalizeAddress(location));
            }
        }
        
        // Update metadata
        if (existingDelivery.getMetadata() == null) {
            existingDelivery.setMetadata(new Metadata());
        }
        existingDelivery.getMetadata().setUpdatedAt(new Date());
        
        // Update the delivery
        disposables.add(
            deliveryRepository.updateDelivery(existingDelivery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        // Mark source capture as processed
                        markAsProcessed(sourceCapture, true);
                        if (callback != null) callback.onComplete(1);
                    },
                    error -> {
                        Log.e(TAG, "Error updating delivery", error);
                        if (callback != null) callback.onError(error);
                    }
                )
        );
    }

    /**
     * Create a new delivery
     *
     * @param orderId The order ID
     * @param location The location string
     * @param sourceCapture The source capture
     * @param callback Callback for processing result
     */
    private void createNewDelivery(String orderId, String location, 
                                 Delivery sourceCapture, ProcessCallback callback) {
        // Create address
        Address address = new Address();
        address.setFullAddress(location);
        address.setNormalizedAddress(addressRepository.normalizeAddress(location));
        
        // Create new delivery
        Delivery delivery = new Delivery();
        
        // Set metadata
        Metadata metadata = new Metadata();
        metadata.setOrderId(orderId);
        metadata.setCreatedAt(new Date());
        metadata.setSource("auto_capture");
        delivery.setMetadata(metadata);
        
        // Set address
        delivery.setAddress(address);
        
        // Set reference (will be updated with address ID after saving)
        Reference reference = new Reference();
        reference.setAddressText(location);
        delivery.setReference(reference);
        
        // Set times
        Times times = new Times();
        times.setCreatedAt(new Date());
        times.setOrderedAt(new Date());
        delivery.setTimes(times);
        
        // Set status
        Status status = new Status();
        status.setCompleted(false);
        status.setTipped(false);
        delivery.setStatus(status);
        
        // Save the delivery
        disposables.add(
            deliveryRepository.addDelivery(delivery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    documentReference -> {
                        // Update address
                        updateAddress(address, orderId);
                        
                        // Mark source capture as processed
                        markAsProcessed(sourceCapture, true);
                        
                        if (callback != null) callback.onComplete(1);
                    },
                    error -> {
                        Log.e(TAG, "Error adding delivery", error);
                        if (callback != null) callback.onError(error);
                    }
                )
        );
    }

    /**
     * Mark a capture as processed
     *
     * @param capture The capture to mark as processed
     * @param success Whether processing was successful
     */
    private void markAsProcessed(Delivery capture, boolean success) {
        // Update status
        if (capture.getStatus() == null) {
            capture.setStatus(new Status());
        }
        capture.getStatus().setProcessed(true);
        capture.getStatus().setProcessSuccess(success);
        
        // Update metadata
        if (capture.getMetadata() == null) {
            capture.setMetadata(new Metadata());
        }
        capture.getMetadata().setUpdatedAt(new Date());
        
        // Update the capture
        disposables.add(
            deliveryRepository.updateDelivery(capture)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> Log.d(TAG, "Capture marked as processed: " + 
                            capture.getDeliveryId()),
                    error -> Log.e(TAG, "Error marking capture as processed", error)
                )
        );
    }

    /**
     * Update or create an address entry
     *
     * @param address The address to update or create
     * @param orderId The order ID associated with this address
     */
    private void updateAddress(Address address, String orderId) {
        String normalizedAddress = address.getNormalizedAddress();
        
        // Check if address exists in repository
        disposables.add(
            addressRepository.findAddressByNormalizedAddress(normalizedAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    existingAddress -> {
                        if (existingAddress != null) {
                            // Address exists, update it
                            updateExistingAddress(existingAddress, orderId);
                        } else {
                            // Address doesn't exist, create it
                            createNewAddress(address, orderId);
                        }
                    },
                    error -> {
                        // Address not found, create new one
                        createNewAddress(address, orderId);
                    }
                )
        );
    }

    /**
     * Update an existing address
     *
     * @param address The address to update
     * @param orderId The order ID to add
     */
    private void updateExistingAddress(Address address, String orderId) {
        // Update order IDs list
        List<String> orderIds = address.getOrderIds();
        if (orderIds == null) {
            orderIds = new ArrayList<>();
        }
        
        if (!orderIds.contains(orderId)) {
            orderIds.add(orderId);
            address.setOrderIds(orderIds);
        }
        
        // Update delivery count
        if (address.getDeliveryStats() == null) {
            address.setDeliveryStats(new Address.DeliveryStats());
        }
        
        Address.DeliveryStats stats = address.getDeliveryStats();
        stats.setDeliveryCount(stats.getDeliveryCount() + 1);
        stats.setLastDeliveryDate(new Date());
        
        // Update the address
        disposables.add(
            addressRepository.updateAddress(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> Log.d(TAG, "Address updated: " + address.getAddressId()),
                    error -> Log.e(TAG, "Error updating address", error)
                )
        );
    }

    /**
     * Create a new address
     *
     * @param address The address to create
     * @param orderId The order ID to add
     */
    private void createNewAddress(Address address, String orderId) {
        // Set up order IDs list
        List<String> orderIds = new ArrayList<>();
        orderIds.add(orderId);
        address.setOrderIds(orderIds);
        
        // Set up delivery stats
        Address.DeliveryStats stats = new Address.DeliveryStats();
        stats.setDeliveryCount(1);
        stats.setTipCount(0);
        stats.setTotalTips(0.0);
        stats.setAverageTip(0.0);
        stats.setLastDeliveryDate(new Date());
        address.setDeliveryStats(stats);
        
        // Set up search terms for improved searching
        List<String> searchTerms = new ArrayList<>();
        String[] addressParts = address.getNormalizedAddress().split("\\s+");
        for (String part : addressParts) {
            if (!part.isEmpty()) {
                searchTerms.add(part);
            }
        }
        address.setSearchTerms(searchTerms);
        
        // Set flags
        Address.Flags flags = new Address.Flags();
        flags.setDoNotDeliver(false);
        address.setFlags(flags);
        
        // Set metadata
        address.setMetadata(new Metadata());
        address.getMetadata().setCreatedAt(new Date());
        
        // Save the address
        disposables.add(
            addressRepository.addAddress(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    documentReference -> Log.d(TAG, "Address added: " + 
                            documentReference.getId()),
                    error -> Log.e(TAG, "Error adding address", error)
                )
        );
    }

    /**
     * Clean up resources when processor is no longer needed
     */
    public void dispose() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    /**
     * Callback interface for processing
     */
    public interface ProcessCallback {
        void onComplete(int count);
        void onError(Exception e);
    }
}