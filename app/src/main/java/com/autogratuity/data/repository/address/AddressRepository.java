package com.autogratuity.data.repository.address;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.repository.core.DataRepository;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Repository interface for managing address data.
 * Extends the core DataRepository for address-specific operations.
 */
public interface AddressRepository extends DataRepository {
    
    //-----------------------------------------------------------------------------------
    // Address Operations
    //-----------------------------------------------------------------------------------
    
    /**
     * Get all addresses for the current user.
     * 
     * @return Single that emits a list of addresses
     */
    @Override
    Single<List<Address>> getAddresses();
    
    /**
     * Get an address by its ID.
     * 
     * @param addressId The address ID
     * @return Single that emits the address
     */
    @Override
    Single<Address> getAddressById(String addressId);
    
    /**
     * Find an address by its normalized form.
     * 
     * @param normalizedAddress The normalized address string
     * @return Single that emits the address if found, or null if not
     */
    @Override
    Single<Address> findAddressByNormalizedAddress(String normalizedAddress);
    
    /**
     * Add a new address. Will check for duplicates using normalized address.
     * 
     * @param address The address to add
     * @return Single that emits the document reference
     */
    @Override
    Single<DocumentReference> addAddress(Address address);
    
    /**
     * Update an existing address.
     * 
     * @param address The updated address
     * @return Completable that completes when update is finished
     */
    @Override
    Completable updateAddress(Address address);
    
    /**
     * Delete an address.
     * 
     * @param addressId The address ID to delete
     * @return Completable that completes when deletion is finished
     */
    @Override
    Completable deleteAddress(String addressId);
    
    /**
     * Observe changes to all addresses in real-time.
     * 
     * @return Observable that emits updates to the address list
     */
    @Override
    Observable<List<Address>> observeAddresses();
    
    /**
     * Observe changes to a specific address in real-time.
     * 
     * @param addressId The address ID to observe
     * @return Observable that emits updates to the address
     */
    @Override
    Observable<Address> observeAddress(String addressId);
    
    //-----------------------------------------------------------------------------------
    // Convenience Methods
    //-----------------------------------------------------------------------------------
    
    /**
     * Get favorite addresses.
     * 
     * @return Single that emits a list of favorite addresses
     */
    Single<List<Address>> getFavoriteAddresses();
    
    /**
     * Get addresses sorted by most recent delivery.
     * 
     * @param limit Maximum number of addresses to return
     * @return Single that emits a list of addresses
     */
    Single<List<Address>> getRecentlyUsedAddresses(int limit);
    
    /**
     * Search addresses by query string.
     * 
     * @param query The search query
     * @return Single that emits a list of addresses matching the query
     */
    Single<List<Address>> searchAddresses(String query);
    
    /**
     * Get addresses within a certain radius of a location.
     * 
     * @param latitude Latitude of the center point
     * @param longitude Longitude of the center point
     * @param radiusKm Radius in kilometers
     * @return Single that emits a list of addresses within the radius
     */
    Single<List<Address>> getAddressesNearLocation(double latitude, double longitude, double radiusKm);
    
    /**
     * Mark an address as favorite.
     * 
     * @param addressId The address ID
     * @param isFavorite Whether to mark as favorite or not
     * @return Completable that completes when update is finished
     */
    Completable setAddressFavorite(String addressId, boolean isFavorite);
    
    /**
     * Mark an address as verified.
     * 
     * @param addressId The address ID
     * @param isVerified Whether to mark as verified or not
     * @return Completable that completes when update is finished
     */
    Completable setAddressVerified(String addressId, boolean isVerified);
    
    /**
     * Set access issues flag for an address.
     * 
     * @param addressId The address ID
     * @param hasAccessIssues Whether the address has access issues
     * @return Completable that completes when update is finished
     */
    Completable setAddressAccessIssues(String addressId, boolean hasAccessIssues);
    
    /**
     * Update address notes.
     * 
     * @param addressId The address ID
     * @param notes The notes to set
     * @return Completable that completes when update is finished
     */
    Completable updateAddressNotes(String addressId, String notes);
    
    /**
     * Update delivery statistics for an address.
     * 
     * @param addressId The address ID
     * @param deliveryStats The updated delivery statistics
     * @return Completable that completes when update is finished
     */
    Completable updateAddressDeliveryStats(String addressId, Address.DeliveryStats deliveryStats);
    
    /**
     * Normalize an address string for consistent lookup and comparison.
     * 
     * @param addressString The raw address string
     * @return The normalized address string
     */
    String normalizeAddress(String addressString);
    
    /**
     * Parse an address string into components.
     * 
     * @param addressString The address string to parse
     * @return The address components
     */
    Address.Components parseAddressComponents(String addressString);
    
    /**
     * Geocode an address to obtain coordinates.
     * 
     * @param address The address to geocode
     * @return Single that emits the updated address with location
     */
    Single<Address> geocodeAddress(Address address);
    
    /**
     * Set the default address.
     * 
     * @param addressId The address ID to set as default
     * @return Completable that completes when update is finished
     */
    Completable setDefaultAddress(String addressId);
    
    /**
     * Get the default address.
     * 
     * @return Single that emits the default address
     */
    Single<Address> getDefaultAddress();
    
    /**
     * Import addresses from CSV or other format.
     * 
     * @param addresses List of addresses to import
     * @return Single that emits the number of addresses successfully imported
     */
    Single<Integer> importAddresses(List<Map<String, Object>> addresses);
}
