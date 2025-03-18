package com.autogratuity.data.serialization;

import com.autogratuity.data.local.JsonSerializer;
import com.autogratuity.data.model.Address;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for serializing/deserializing Address objects to/from Firestore maps.
 * This class improves the conversion process between Address objects and Firestore.
 */
public class AddressSerializer {
    
    /**
     * Convert an Address object to a Map for Firestore storage
     * 
     * @param address The address to convert
     * @return Map representation of the address
     */
    public static Map<String, Object> toMap(Address address) {
        if (address == null) {
            return null;
        }
        
        Map<String, Object> map = new HashMap<>();
        
        // Add basic fields
        map.put("addressId", address.getAddressId());
        map.put("userId", address.getUserId());
        map.put("fullAddress", address.getFullAddress());
        map.put("normalizedAddress", address.getNormalizedAddress());
        map.put("isDefault", address.isDefault());
        map.put("notes", address.getNotes());
        map.put("tags", address.getTags());
        
        // Add nested objects
        if (address.getComponents() != null) {
            map.put("components", componentsToMap(address.getComponents()));
        }
        
        if (address.getLocation() != null) {
            map.put("location", locationToMap(address.getLocation()));
        }
        
        if (address.getSearchFields() != null) {
            map.put("searchFields", searchFieldsToMap(address.getSearchFields()));
        }
        
        if (address.getDeliveryStats() != null) {
            map.put("deliveryStats", deliveryStatsToMap(address.getDeliveryStats()));
        }
        
        if (address.getFlags() != null) {
            map.put("flags", flagsToMap(address.getFlags()));
        }
        
        if (address.getMetadata() != null) {
            map.put("metadata", metadataToMap(address.getMetadata()));
        }
        
        return map;
    }
    
    /**
     * Convert a DocumentSnapshot to an Address object
     * 
     * @param snapshot Firestore document snapshot
     * @return Address object
     */
    public static Address fromDocumentSnapshot(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        
        Address address = snapshot.toObject(Address.class);
        if (address != null) {
            // Ensure ID is set
            if (address.getAddressId() == null) {
                address.setAddressId(snapshot.getId());
            }
        }
        
        return address;
    }
    
    /**
     * Convert a Map to an Address object
     * 
     * @param map Map representation of an address
     * @return Address object
     */
    public static Address fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        // Fast path: Use Gson serialization
        return JsonSerializer.fromJson(JsonSerializer.toJson(map), Address.class);
    }
    
    //-----------------------------------------------------------------------------------
    // Helper methods for converting nested objects to maps
    //-----------------------------------------------------------------------------------
    
    private static Map<String, Object> componentsToMap(Address.Components components) {
        Map<String, Object> map = new HashMap<>();
        map.put("streetNumber", components.getStreetNumber());
        map.put("streetName", components.getStreetName());
        map.put("city", components.getCity());
        map.put("state", components.getState());
        map.put("postalCode", components.getPostalCode());
        map.put("country", components.getCountry());
        return map;
    }
    
    private static Map<String, Object> locationToMap(Address.Location location) {
        Map<String, Object> map = new HashMap<>();
        map.put("latitude", location.getLatitude());
        map.put("longitude", location.getLongitude());
        map.put("geohash", location.getGeohash());
        return map;
    }
    
    private static Map<String, Object> searchFieldsToMap(Address.SearchFields searchFields) {
        Map<String, Object> map = new HashMap<>();
        map.put("searchTerms", searchFields.getSearchTerms());
        map.put("normalizedKey", searchFields.getNormalizedKey());
        return map;
    }
    
    private static Map<String, Object> deliveryStatsToMap(Address.DeliveryStats stats) {
        Map<String, Object> map = new HashMap<>();
        map.put("deliveryCount", stats.getDeliveryCount());
        map.put("tipCount", stats.getTipCount());
        map.put("totalTips", stats.getTotalTips());
        map.put("averageTip", stats.getAverageTip());
        map.put("highestTip", stats.getHighestTip());
        map.put("lastDeliveryDate", dateToTimestamp(stats.getLastDeliveryDate()));
        return map;
    }
    
    private static Map<String, Object> flagsToMap(Address.Flags flags) {
        Map<String, Object> map = new HashMap<>();
        map.put("doNotDeliver", flags.isDoNotDeliver());
        map.put("favorite", flags.isFavorite());
        map.put("verified", flags.isVerified());
        map.put("hasAccessIssues", flags.isHasAccessIssues());
        map.put("isApartment", flags.isApartment());
        return map;
    }
    
    private static Map<String, Object> metadataToMap(Address.Metadata metadata) {
        Map<String, Object> map = new HashMap<>();
        map.put("createdAt", dateToTimestamp(metadata.getCreatedAt()));
        map.put("updatedAt", dateToTimestamp(metadata.getUpdatedAt()));
        map.put("source", metadata.getSource());
        map.put("importId", metadata.getImportId());
        map.put("version", metadata.getVersion());
        return map;
    }
    
    /**
     * Helper method to convert Date to Timestamp
     */
    private static Timestamp dateToTimestamp(Date date) {
        return date != null ? new Timestamp(date) : null;
    }
}
