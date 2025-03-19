package com.autogratuity.data.repository.address;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.UserProfile;
import com.autogratuity.data.repository.core.FirestoreRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Implementation of AddressRepository interface.
 * Responsible for managing address data.
 */
public class AddressRepositoryImpl extends FirestoreRepository implements AddressRepository {
    
    private static final String TAG = "AddressRepository";
    
    // Collection names
    private static final String COLLECTION_ADDRESSES = "addresses";
    private static final String COLLECTION_USER_PROFILES = "user_profiles";
    
    // Cache keys
    private static final String CACHE_ADDRESSES = "addresses";
    private static final String CACHE_ADDRESS = "address";
    
    // Subject for real-time updates
    private final BehaviorSubject<List<Address>> addressesSubject = BehaviorSubject.create();
    private final Map<String, BehaviorSubject<Address>> addressSubjects = new HashMap<>();
    
    /**
     * Constructor for AddressRepositoryImpl
     * 
     * @param context Android context for SharedPreferences and connectivity
     */
    public AddressRepositoryImpl(Context context) {
        super(context);
        setupAddressesListener();
    }
    
    /**
     * Sets up real-time listener for addresses
     */
    private void setupAddressesListener() {
        // Set up listener for all addresses
        String listenerKey = "addresses_" + userId + "_listener";
        ListenerRegistration listener = db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening to addresses", e);
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        List<Address> addresses = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Address address = doc.toObject(Address.class);
                            if (address != null) {
                                addresses.add(address);
                                
                                // Also emit to address-specific subject if exists
                                String addressId = address.getAddressId();
                                if (addressId != null && addressSubjects.containsKey(addressId)) {
                                    addressSubjects.get(addressId).onNext(address);
                                }
                            }
                        }
                        
                        // Cache and emit
                        putInCache(CACHE_ADDRESSES + "_" + userId, addresses);
                        addressesSubject.onNext(addresses);
                    }
                });
        
        // Store listener for cleanup
        activeListeners.put(listenerKey, listener);
    }
    
    @Override
    public Single<List<Address>> getAddresses() {
        return Single.create(emitter -> {
            // First try memory cache
            List<Address> cached = getFromCache(CACHE_ADDRESSES + "_" + userId);
            if (cached != null) {
                emitter.onSuccess(cached);
                return;
            }
            
            // Then try Firestore
            db.collection(COLLECTION_ADDRESSES)
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Address> addresses = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Address address = doc.toObject(Address.class);
                            if (address != null) {
                                addresses.add(address);
                            }
                        }
                        
                        // Cache and emit
                        putInCache(CACHE_ADDRESSES + "_" + userId, addresses);
                        emitter.onSuccess(addresses);
                        
                        // Also emit to subject
                        addressesSubject.onNext(addresses);
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // If offline, return empty list for now
                            // A more complete implementation would store in local database
                            emitter.onSuccess(new ArrayList<>());
                        } else {
                            Log.e(TAG, "Error getting addresses", e);
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    @Override
    public Single<Address> getAddressById(String addressId) {
        if (addressId == null || addressId.isEmpty()) {
            return Single.error(new IllegalArgumentException("Address ID cannot be empty"));
        }
        
        return Single.create(emitter -> {
            // First try memory cache
            Address cached = getFromCache(CACHE_ADDRESS + "_" + addressId);
            if (cached != null) {
                emitter.onSuccess(cached);
                return;
            }
            
            // Then try Firestore
            DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document(addressId);
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Address address = documentSnapshot.toObject(Address.class);
                            if (address != null) {
                                // Ensure the address belongs to the current user
                                if (!userId.equals(address.getUserId())) {
                                    emitter.onError(new SecurityException("Address belongs to another user"));
                                    return;
                                }
                                
                                // Cache and emit
                                putInCache(CACHE_ADDRESS + "_" + addressId, address);
                                emitter.onSuccess(address);
                                
                                // Also emit to address-specific subject if exists
                                if (addressSubjects.containsKey(addressId)) {
                                    addressSubjects.get(addressId).onNext(address);
                                }
                            } else {
                                emitter.onError(new Exception("Failed to parse address"));
                            }
                        } else {
                            emitter.onError(new Exception("Address not found: " + addressId));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting address by ID", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<Address> findAddressByNormalizedAddress(String normalizedAddress) {
        if (normalizedAddress == null || normalizedAddress.isEmpty()) {
            return Single.error(new IllegalArgumentException("Normalized address cannot be empty"));
        }
        
        return Single.create(emitter -> {
            // Query Firestore for the address
            db.collection(COLLECTION_ADDRESSES)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("normalizedAddress", normalizedAddress)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            Address address = querySnapshot.getDocuments().get(0).toObject(Address.class);
                            if (address != null) {
                                // Cache and emit
                                putInCache(CACHE_ADDRESS + "_" + address.getAddressId(), address);
                                emitter.onSuccess(address);
                            } else {
                                emitter.onError(new Exception("Failed to parse address"));
                            }
                        } else {
                            // Address not found, emit null (not an error)
                            emitter.onSuccess(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error finding address by normalized address", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<DocumentReference> addAddress(Address address) {
        if (address == null) {
            return Single.error(new IllegalArgumentException("Address cannot be null"));
        }
        
        if (address.getNormalizedAddress() == null || address.getNormalizedAddress().isEmpty()) {
            // If normalized address is not provided, generate it
            String normalizedAddress = normalizeAddress(address.getFullAddress());
            if (normalizedAddress.isEmpty()) {
                return Single.error(new IllegalArgumentException("Address must have a valid full address"));
            }
            address.setNormalizedAddress(normalizedAddress);
        }
        
        // Check if address already exists to prevent duplicates
        return findAddressByNormalizedAddress(address.getNormalizedAddress())
                .flatMap(existingAddress -> {
                    if (existingAddress != null) {
                        // Address already exists, return existing reference
                        return Single.just(
                                db.collection(COLLECTION_ADDRESSES).document(existingAddress.getAddressId()));
                    } else {
                        // Create new address
                        return createNewAddress(address);
                    }
                });
    }
    
    /**
     * Helper method to create a new address
     * 
     * @param address The address to create
     * @return Single that emits the document reference
     */
    private Single<DocumentReference> createNewAddress(Address address) {
        // Generate document ID if needed
        String addressId = address.getAddressId();
        if (addressId == null || addressId.isEmpty()) {
            addressId = UUID.randomUUID().toString();
            address.setAddressId(addressId);
        }
        
        // Set user ID
        address.setUserId(userId);
        
        // Initialize metadata if needed
        if (address.getMetadata() == null) {
            Address.Metadata metadata = new Address.Metadata();
            metadata.setCreatedAt(new Date());
            metadata.setUpdatedAt(new Date());
            metadata.setVersion(1);
            metadata.setSource("app");
            address.setMetadata(metadata);
        } else {
            address.getMetadata().setUpdatedAt(new Date());
        }
        
        // Initialize delivery stats if needed
        if (address.getDeliveryStats() == null) {
            Address.DeliveryStats stats = new Address.DeliveryStats();
            stats.setDeliveryCount(0);
            stats.setTipCount(0);
            stats.setTotalTips(0);
            stats.setAverageTip(0);
            address.setDeliveryStats(stats);
        }
        
        // Initialize flags if needed
        if (address.getFlags() == null) {
            Address.Flags flags = new Address.Flags();
            address.setFlags(flags);
        }
        
        // If location is missing, try geocoding
        if (address.getLocation() == null || 
                (address.getLocation().getLatitude() == 0 && address.getLocation().getLongitude() == 0)) {
            // For real implementation, we would call geocoding service here
            // For now, just create empty location
            Address.Location location = new Address.Location();
            address.setLocation(location);
        }
        
        DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document(addressId);
        final String finalAddressId = addressId;
        
        return Single.create(emitter -> {
            docRef.set(address)
                    .addOnSuccessListener(aVoid -> {
                        // Cache the address
                        putInCache(CACHE_ADDRESS + "_" + finalAddressId, address);
                        
                        // Invalidate addresses cache
                        invalidateCache(CACHE_ADDRESSES + "_" + userId);
                        
                        // Update user profile with address count
                        updateAddressCountInUserProfile(1)
                                .subscribe(() -> {}, throwable -> {
                                    Log.e(TAG, "Error updating address count", throwable);
                                });
                        
                        emitter.onSuccess(docRef);
                    })
                    .addOnFailureListener(e -> {
                        if (!isNetworkAvailable()) {
                            // Create sync operation
                            Map<String, Object> data = new HashMap<>(); // Convert address to map
                            enqueueOperation("create", "address", finalAddressId, data)
                                    .subscribe(
                                            () -> emitter.onSuccess(docRef),
                                            emitter::onError
                                    );
                        } else {
                            Log.e(TAG, "Error adding address", e);
                            emitter.onError(e);
                        }
                    });
        });
    }
    
    /**
     * Helper method to update address count in user profile
     * 
     * @param delta Change in address count (1 for add, -1 for delete)
     * @return Completable that completes when update is finished
     */
    private Completable updateAddressCountInUserProfile(int delta) {
        return getUserProfile()
                .flatMapCompletable(profile -> {
                    if (profile.getUsage() == null) {
                        profile.setUsage(new UserProfile.Usage());
                    }
                    
                    int currentCount = profile.getUsage().getAddressCount();
                    profile.getUsage().setAddressCount(Math.max(0, currentCount + delta));
                    profile.getUsage().setLastUsageUpdate(new Date());
                    
                    return updateUserProfile(profile);
                });
    }
    
    @Override
    public Completable updateAddress(Address address) {
        if (address == null) {
            return Completable.error(new IllegalArgumentException("Address cannot be null"));
        }
        
        if (address.getAddressId() == null || address.getAddressId().isEmpty()) {
            return Completable.error(new IllegalArgumentException("Address ID is required"));
        }
        
        // Ensure user ID is set correctly
        address.setUserId(userId);
        
        // Update metadata
        if (address.getMetadata() == null) {
            Address.Metadata metadata = new Address.Metadata();
            metadata.setUpdatedAt(new Date());
            metadata.setVersion(1);
            address.setMetadata(metadata);
        } else {
            address.getMetadata().setUpdatedAt(new Date());
            address.getMetadata().setVersion(address.getMetadata().getVersion() + 1);
        }
        
        DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document(address.getAddressId());
        
        return Completable.create(emitter -> {
            // First check if address exists and belongs to user
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Address existingAddress = documentSnapshot.toObject(Address.class);
                            if (existingAddress != null && userId.equals(existingAddress.getUserId())) {
                                // Update the address
                                docRef.set(address)
                                        .addOnSuccessListener(aVoid -> {
                                            // Update cache
                                            putInCache(CACHE_ADDRESS + "_" + address.getAddressId(), address);
                                            
                                            // Invalidate addresses cache
                                            invalidateCache(CACHE_ADDRESSES + "_" + userId);
                                            
                                            emitter.onComplete();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (!isNetworkAvailable()) {
                                                // Create sync operation
                                                Map<String, Object> data = new HashMap<>(); // Convert address to map
                                                enqueueOperation("update", "address", address.getAddressId(), data)
                                                        .subscribe(
                                                                emitter::onComplete,
                                                                emitter::onError
                                                        );
                                            } else {
                                                Log.e(TAG, "Error updating address", e);
                                                emitter.onError(e);
                                            }
                                        });
                            } else {
                                emitter.onError(new SecurityException("Address does not belong to current user"));
                            }
                        } else {
                            emitter.onError(new Exception("Address not found: " + address.getAddressId()));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking address before update", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Completable deleteAddress(String addressId) {
        if (addressId == null || addressId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Address ID cannot be empty"));
        }
        
        DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document(addressId);
        
        return Completable.create(emitter -> {
            // First check if address exists and belongs to user
            docRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Address address = documentSnapshot.toObject(Address.class);
                            if (address != null && userId.equals(address.getUserId())) {
                                // Delete the address
                                docRef.delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Remove from cache
                                            invalidateCache(CACHE_ADDRESS + "_" + addressId);
                                            invalidateCache(CACHE_ADDRESSES + "_" + userId);
                                            
                                            // Update user profile with address count
                                            updateAddressCountInUserProfile(-1)
                                                    .subscribe(() -> {}, throwable -> {
                                                        Log.e(TAG, "Error updating address count", throwable);
                                                    });
                                            
                                            // If this was the default address, unset it
                                            if (address.isDefault()) {
                                                updateUserProfileFields(
                                                        Collections.singletonMap("defaultAddressId", null))
                                                        .subscribe(() -> {}, throwable -> {
                                                            Log.e(TAG, "Error updating default address", throwable);
                                                        });
                                            }
                                            
                                            emitter.onComplete();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (!isNetworkAvailable()) {
                                                // Create sync operation
                                                enqueueOperation("delete", "address", addressId, null)
                                                        .subscribe(
                                                                emitter::onComplete,
                                                                emitter::onError
                                                        );
                                            } else {
                                                Log.e(TAG, "Error deleting address", e);
                                                emitter.onError(e);
                                            }
                                        });
                            } else {
                                emitter.onError(new SecurityException("Address does not belong to current user"));
                            }
                        } else {
                            // Address already deleted
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking address before delete", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Observable<List<Address>> observeAddresses() {
        // If we don't have an initial value, trigger a fetch
        if (!addressesSubject.hasValue()) {
            getAddresses().subscribe(
                    addresses -> {}, // Will be emitted through the subject
                    error -> Log.e(TAG, "Error fetching initial addresses for observation", error)
            );
        }
        
        return addressesSubject;
    }
    
    @Override
    public Observable<Address> observeAddress(String addressId) {
        if (addressId == null || addressId.isEmpty()) {
            return Observable.error(new IllegalArgumentException("Address ID cannot be empty"));
        }
        
        // Create subject if it doesn't exist
        if (!addressSubjects.containsKey(addressId)) {
            addressSubjects.put(addressId, BehaviorSubject.create());
            
            // Set up listener for this address
            String listenerKey = "address_" + addressId + "_listener";
            DocumentReference docRef = db.collection(COLLECTION_ADDRESSES).document(addressId);
            ListenerRegistration listener = docRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    Log.e(TAG, "Error listening to address " + addressId, e);
                    return;
                }
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Address address = documentSnapshot.toObject(Address.class);
                    if (address != null && userId.equals(address.getUserId())) {
                        // Cache and emit
                        putInCache(CACHE_ADDRESS + "_" + addressId, address);
                        addressSubjects.get(addressId).onNext(address);
                    }
                }
            });
            
            // Store listener for cleanup
            activeListeners.put(listenerKey, listener);
            
            // Trigger initial fetch
            getAddressById(addressId).subscribe(
                    address -> {}, // Will be emitted through the subject
                    error -> Log.e(TAG, "Error fetching initial address for observation", error)
            );
        }
        
        return addressSubjects.get(addressId);
    }
    
    @Override
    public Single<List<Address>> getFavoriteAddresses() {
        return getAddresses()
                .map(addresses -> {
                    List<Address> favorites = new ArrayList<>();
                    for (Address address : addresses) {
                        if (address.getFlags() != null && address.getFlags().isFavorite()) {
                            favorites.add(address);
                        }
                    }
                    return favorites;
                });
    }
    
    @Override
    public Single<List<Address>> getBestTippingAddresses(int limit) {
        return Single.create(emitter -> {
            // Query Firestore for addresses with the highest average tip or tip count
            db.collection(COLLECTION_ADDRESSES)
                    .whereEqualTo("userId", userId)
                    .whereGreaterThan("deliveryStats.tipCount", 0) // Only include addresses with tips
                    .orderBy("deliveryStats.tipCount", Query.Direction.DESCENDING) // Sort by tip count
                    .orderBy("deliveryStats.averageTip", Query.Direction.DESCENDING) // Then by average tip
                    .limit(limit)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Address> addresses = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Address address = doc.toObject(Address.class);
                            if (address != null) {
                                addresses.add(address);
                            }
                        }
                        
                        emitter.onSuccess(addresses);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting best tipping addresses", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<List<Address>> getRecentlyUsedAddresses(int limit) {
        return Single.create(emitter -> {
            // Query Firestore by last delivery date
            db.collection(COLLECTION_ADDRESSES)
                    .whereEqualTo("userId", userId)
                    .orderBy("deliveryStats.lastDeliveryDate", Query.Direction.DESCENDING)
                    .limit(limit)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Address> addresses = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Address address = doc.toObject(Address.class);
                            if (address != null) {
                                addresses.add(address);
                            }
                        }
                        
                        emitter.onSuccess(addresses);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting recently used addresses", e);
                        emitter.onError(e);
                    });
        });
    }
    
    @Override
    public Single<List<Address>> searchAddresses(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAddresses();
        }
        
        final String normalizedQuery = query.toLowerCase().trim();
        
        return getAddresses()
                .map(addresses -> {
                    List<Address> results = new ArrayList<>();
                    
                    for (Address address : addresses) {
                        // Check if address matches query
                        boolean matches = false;
                        
                        // Check full address
                        if (address.getFullAddress() != null && 
                                address.getFullAddress().toLowerCase().contains(normalizedQuery)) {
                            matches = true;
                        }
                        
                        // Check components
                        if (!matches && address.getComponents() != null) {
                            Address.Components components = address.getComponents();
                            if ((components.getStreetName() != null && 
                                 components.getStreetName().toLowerCase().contains(normalizedQuery)) ||
                                (components.getCity() != null && 
                                 components.getCity().toLowerCase().contains(normalizedQuery)) ||
                                (components.getPostalCode() != null && 
                                 components.getPostalCode().toLowerCase().contains(normalizedQuery))) {
                                matches = true;
                            }
                        }
                        
                        // Check tags
                        if (!matches && address.getTags() != null) {
                            for (String tag : address.getTags()) {
                                if (tag.toLowerCase().contains(normalizedQuery)) {
                                    matches = true;
                                    break;
                                }
                            }
                        }
                        
                        // Check notes
                        if (!matches && address.getNotes() != null && 
                                address.getNotes().toLowerCase().contains(normalizedQuery)) {
                            matches = true;
                        }
                        
                        if (matches) {
                            results.add(address);
                        }
                    }
                    
                    return results;
                });
    }
    
    @Override
    public Single<List<Address>> getAddressesNearLocation(double latitude, double longitude, double radiusKm) {
        // For a real implementation, we would use geoqueries with GeoFirestore
        // For simplicity, we'll just filter in memory
        
        final double earthRadius = 6371; // km
        
        return getAddresses()
                .map(addresses -> {
                    List<Address> nearbyAddresses = new ArrayList<>();
                    
                    for (Address address : addresses) {
                        if (address.getLocation() != null) {
                            double addressLat = address.getLocation().getLatitude();
                            double addressLng = address.getLocation().getLongitude();
                            
                            // Skip if no valid coordinates
                            if (addressLat == 0 && addressLng == 0) {
                                continue;
                            }
                            
                            // Calculate distance using Haversine formula
                            double dLat = Math.toRadians(addressLat - latitude);
                            double dLng = Math.toRadians(addressLng - longitude);
                            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                                       Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(addressLat)) *
                                       Math.sin(dLng / 2) * Math.sin(dLng / 2);
                            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                            double distance = earthRadius * c;
                            
                            if (distance <= radiusKm) {
                                nearbyAddresses.add(address);
                            }
                        }
                    }
                    
                    return nearbyAddresses;
                });
    }
    
    @Override
    public Completable setAddressFavorite(String addressId, boolean isFavorite) {
        if (addressId == null || addressId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Address ID cannot be empty"));
        }
        
        return getAddressById(addressId)
                .flatMapCompletable(address -> {
                    if (address.getFlags() == null) {
                        address.setFlags(new Address.Flags());
                    }
                    
                    address.getFlags().setFavorite(isFavorite);
                    return updateAddress(address);
                });
    }
    
    @Override
    public Completable setAddressVerified(String addressId, boolean isVerified) {
        if (addressId == null || addressId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Address ID cannot be empty"));
        }
        
        return getAddressById(addressId)
                .flatMapCompletable(address -> {
                    if (address.getFlags() == null) {
                        address.setFlags(new Address.Flags());
                    }
                    
                    address.getFlags().setVerified(isVerified);
                    return updateAddress(address);
                });
    }
    
    @Override
    public Completable setAddressAccessIssues(String addressId, boolean hasAccessIssues) {
        if (addressId == null || addressId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Address ID cannot be empty"));
        }
        
        return getAddressById(addressId)
                .flatMapCompletable(address -> {
                    if (address.getFlags() == null) {
                        address.setFlags(new Address.Flags());
                    }
                    
                    address.getFlags().setHasAccessIssues(hasAccessIssues);
                    return updateAddress(address);
                });
    }
    
    @Override
    public Completable updateAddressNotes(String addressId, String notes) {
        if (addressId == null || addressId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Address ID cannot be empty"));
        }
        
        return getAddressById(addressId)
                .flatMapCompletable(address -> {
                    address.setNotes(notes);
                    return updateAddress(address);
                });
    }
    
    @Override
    public Completable updateAddressDeliveryStats(String addressId, Address.DeliveryStats deliveryStats) {
        if (addressId == null || addressId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Address ID cannot be empty"));
        }
        
        if (deliveryStats == null) {
            return Completable.error(new IllegalArgumentException("Delivery stats cannot be null"));
        }
        
        return getAddressById(addressId)
                .flatMapCompletable(address -> {
                    address.setDeliveryStats(deliveryStats);
                    return updateAddress(address);
                });
    }
    
    @Override
    public String normalizeAddress(String addressString) {
        if (addressString == null || addressString.trim().isEmpty()) {
            return "";
        }
        
        // Basic normalization
        String normalized = addressString.trim()
                .toLowerCase(Locale.US)
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-z0-9 ]", ""); // Remove all non-alphanumeric chars except spaces
        
        // A more sophisticated implementation would:
        // 1. Parse into components (street, city, state, zip)
        // 2. Standardize abbreviations (St. -> Street, Ave -> Avenue)
        // 3. Remove unnecessary words (The, Suite, Apt, etc.)
        // 4. Handle unit/apartment numbers consistently
        
        return normalized;
    }
    
    @Override
    public Address.Components parseAddressComponents(String addressString) {
        if (addressString == null || addressString.trim().isEmpty()) {
            return null;
        }
        
        // Simple implementation - in a real app, this would use a proper address parser
        // or geo/reverse-geocoding service
        
        Address.Components components = new Address.Components();
        
        // Very basic parsing - just for demonstration purposes
        // Would be replaced with a proper address parser in production
        
        // Example format: "123 Main St, Anytown, CA 12345, USA"
        String[] parts = addressString.split(",");
        
        if (parts.length >= 1) {
            // Extract street info from first part
            String streetPart = parts[0].trim();
            
            // Try to extract street number and name
            String[] streetElements = streetPart.split(" ", 2);
            if (streetElements.length == 2 && Pattern.matches("\\d+", streetElements[0])) {
                components.setStreetNumber(streetElements[0]);
                components.setStreetName(streetElements[1]);
            } else {
                components.setStreetName(streetPart);
            }
        }
        
        if (parts.length >= 2) {
            // Second part is usually city
            components.setCity(parts[1].trim());
        }
        
        if (parts.length >= 3) {
            // Third part often has state and zip
            String statePart = parts[2].trim();
            String[] stateZip = statePart.split(" ");
            
            if (stateZip.length >= 1) {
                components.setState(stateZip[0]);
            }
            
            if (stateZip.length >= 2) {
                components.setPostalCode(stateZip[1]);
            }
        }
        
        if (parts.length >= 4) {
            // Fourth part would be country
            components.setCountry(parts[3].trim());
        } else {
            // Default country
            components.setCountry("USA");
        }
        
        return components;
    }
    
    @Override
    public Single<Address> geocodeAddress(Address address) {
        if (address == null) {
            return Single.error(new IllegalArgumentException("Address cannot be null"));
        }
        
        if (address.getFullAddress() == null || address.getFullAddress().trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("Full address cannot be empty"));
        }
        
        // In a real implementation, this would call a geocoding service like Google Maps
        // For demonstration, we'll just create a fake location
        
        return Single.create(emitter -> {
            // Fake geocoding with random-ish coordinates
            Address.Location location = new Address.Location();
            
            // Create a deterministic but "random" latitude/longitude based on address string
            String addressString = address.getFullAddress();
            int hashCode = addressString.hashCode();
            
            // Generate coordinates in continental USA
            double baseLat = 40.0; // Around the middle of USA
            double baseLng = -100.0;
            
            // Add some "randomness" based on hash code
            double latOffset = (hashCode % 1000) / 1000.0 * 10.0; // +/- 5 degrees
            double lngOffset = (hashCode % 500) / 500.0 * 20.0; // +/- 10 degrees
            
            location.setLatitude(baseLat + latOffset - 5.0);
            location.setLongitude(baseLng + lngOffset - 10.0);
            
            // Fake geohash
            location.setGeohash("geohash_" + Math.abs(hashCode) % 1000000);
            
            // Update address with location
            address.setLocation(location);
            
            emitter.onSuccess(address);
        });
    }
    
    @Override
    public Completable setDefaultAddress(String addressId) {
        if (addressId == null || addressId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("Address ID cannot be empty"));
        }
        
        return getAddressById(addressId)
                .flatMap(address -> {
                    // Verify address exists and belongs to user
                    if (!userId.equals(address.getUserId())) {
                        return Single.error(new SecurityException("Address belongs to another user"));
                    }
                    
                    // First, update user profile with default address ID
                    return getUserProfile()
                            .flatMap(profile -> {
                                profile.setDefaultAddressId(addressId);
                                return updateUserProfile(profile)
                                        .andThen(Single.just(address));
                            });
                })
                .flatMapCompletable(address -> {
                    // Then update all addresses to set default flag
                    return getAddresses()
                            .flatMapCompletable(addresses -> {
                                List<Completable> updates = new ArrayList<>();
                                
                                for (Address addr : addresses) {
                                    boolean shouldBeDefault = addr.getAddressId().equals(addressId);
                                    
                                    // Only update if the default flag needs to change
                                    if (addr.isDefault() != shouldBeDefault) {
                                        addr.setDefault(shouldBeDefault);
                                        updates.add(updateAddress(addr));
                                    }
                                }
                                
                                return Completable.concat(updates);
                            });
                });
    }
    
    @Override
    public Single<Address> getDefaultAddress() {
        return getUserProfile()
                .flatMap(profile -> {
                    String defaultAddressId = profile.getDefaultAddressId();
                    if (defaultAddressId != null && !defaultAddressId.isEmpty()) {
                        return getAddressById(defaultAddressId);
                    } else {
                        // No default address set, try to find an address marked as default
                        return getAddresses()
                                .map(addresses -> {
                                    for (Address address : addresses) {
                                        if (address.isDefault()) {
                                            return address;
                                        }
                                    }
                                    
                                    // No default address found
                                    if (!addresses.isEmpty()) {
                                        // Return the first address
                                        return addresses.get(0);
                                    }
                                    
                                    return null;
                                });
                    }
                });
    }
    
    @Override
    public Single<Integer> importAddresses(List<Map<String, Object>> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return Single.just(0);
        }
        
        return Single.create(emitter -> {
            int successCount = 0;
            List<Throwable> errors = new ArrayList<>();
            
            for (Map<String, Object> addressData : addresses) {
                try {
                    // Convert map to Address object
                    Address address = new Address();
                    
                    // Set basic fields
                    if (addressData.containsKey("fullAddress")) {
                        address.setFullAddress((String) addressData.get("fullAddress"));
                    }
                    
                    if (addressData.containsKey("notes")) {
                        address.setNotes((String) addressData.get("notes"));
                    }
                    
                    if (addressData.containsKey("tags") && addressData.get("tags") instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> tags = (List<String>) addressData.get("tags");
                        address.setTags(tags);
                    }
                    
                    // Normalize address
                    if (address.getFullAddress() != null) {
                        address.setNormalizedAddress(normalizeAddress(address.getFullAddress()));
                        
                        // Parse components
                        address.setComponents(parseAddressComponents(address.getFullAddress()));
                    }
                    
                    // Add address
                    DocumentReference docRef = addAddress(address).blockingGet();
                    if (docRef != null) {
                        successCount++;
                    }
                } catch (Exception e) {
                    errors.add(e);
                    Log.e(TAG, "Error importing address", e);
                }
            }
            
            if (successCount > 0) {
                emitter.onSuccess(successCount);
            } else if (!errors.isEmpty()) {
                emitter.onError(new Exception("Failed to import addresses: " + errors.get(0).getMessage()));
            } else {
                emitter.onSuccess(0);
            }
        });
    }
    
    /**
     * Clean up resources when the repository is no longer needed
     */
    public void cleanup() {
        // Remove all listeners
        for (ListenerRegistration listener : activeListeners.values()) {
            listener.remove();
        }
        activeListeners.clear();
        
        // Clear subjects
        addressesSubject.onComplete();
        for (BehaviorSubject<Address> subject : addressSubjects.values()) {
            subject.onComplete();
        }
        addressSubjects.clear();
    }
}
