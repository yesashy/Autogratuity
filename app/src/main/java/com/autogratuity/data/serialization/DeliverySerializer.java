package com.autogratuity.data.serialization;

import com.autogratuity.data.local.JsonSerializer;
import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for serializing/deserializing Delivery objects to/from Firestore maps.
 * This class improves the conversion process between Delivery objects and Firestore.
 */
public class DeliverySerializer {
    
    /**
     * Convert a Delivery object to a Map for Firestore storage
     * 
     * @param delivery The delivery to convert
     * @return Map representation of the delivery
     */
    public static Map<String, Object> toMap(Delivery delivery) {
        if (delivery == null) {
            return null;
        }
        
        Map<String, Object> map = new HashMap<>();
        
        // Add basic fields
        map.put("deliveryId", delivery.getDeliveryId());
        map.put("userId", delivery.getUserId());
        map.put("orderId", delivery.getOrderId());
        map.put("notes", delivery.getNotes());
        map.put("tags", delivery.getTags());
        
        // Add nested objects
        if (delivery.getReference() != null) {
            map.put("reference", referenceToMap(delivery.getReference()));
        }
        
        if (delivery.getAddress() != null) {
            map.put("address", addressToMap(delivery.getAddress()));
        }
        
        if (delivery.getAmounts() != null) {
            map.put("amounts", amountsToMap(delivery.getAmounts()));
        }
        
        if (delivery.getTimes() != null) {
            map.put("times", timesToMap(delivery.getTimes()));
        }
        
        if (delivery.getStatus() != null) {
            map.put("status", statusToMap(delivery.getStatus()));
        }
        
        if (delivery.getPlatform() != null) {
            map.put("platform", platformToMap(delivery.getPlatform()));
        }
        
        if (delivery.getCustomer() != null) {
            map.put("customer", customerToMap(delivery.getCustomer()));
        }
        
        if (delivery.getItems() != null) {
            map.put("items", itemsToMap(delivery.getItems()));
        }
        
        if (delivery.getDisputeInfo() != null) {
            map.put("disputeInfo", disputeInfoToMap(delivery.getDisputeInfo()));
        }
        
        if (delivery.getMetadata() != null) {
            map.put("metadata", metadataToMap(delivery.getMetadata()));
        }
        
        return map;
    }
    
    /**
     * Convert a DocumentSnapshot to a Delivery object
     * 
     * @param snapshot Firestore document snapshot
     * @return Delivery object
     */
    public static Delivery fromDocumentSnapshot(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        
        Delivery delivery = snapshot.toObject(Delivery.class);
        if (delivery != null) {
            // Ensure ID is set
            if (delivery.getDeliveryId() == null) {
                delivery.setDeliveryId(snapshot.getId());
            }
        }
        
        return delivery;
    }
    
    /**
     * Convert a Map to a Delivery object
     * 
     * @param map Map representation of a delivery
     * @return Delivery object
     */
    public static Delivery fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        // Fast path: Use Gson serialization
        return JsonSerializer.fromJson(JsonSerializer.toJson(map), Delivery.class);
    }
    
    //-----------------------------------------------------------------------------------
    // Helper methods for converting nested objects to maps
    //-----------------------------------------------------------------------------------
    
    private static Map<String, Object> referenceToMap(Delivery.Reference reference) {
        Map<String, Object> map = new HashMap<>();
        map.put("addressId", reference.getAddressId());
        map.put("platformOrderId", reference.getPlatformOrderId());
        return map;
    }
    
    private static Map<String, Object> addressToMap(Address.SimpleAddress address) {
        Map<String, Object> map = new HashMap<>();
        map.put("addressId", address.getAddressId());
        map.put("fullAddress", address.getFullAddress());
        map.put("latitude", address.getLatitude());
        map.put("longitude", address.getLongitude());
        return map;
    }
    
    private static Map<String, Object> amountsToMap(Delivery.Amounts amounts) {
        Map<String, Object> map = new HashMap<>();
        map.put("baseAmount", amounts.getBaseAmount());
        map.put("estimatedPay", amounts.getEstimatedPay());
        map.put("finalPay", amounts.getFinalPay());
        map.put("tipAmount", amounts.getTipAmount());
        map.put("tipPercentage", amounts.getTipPercentage());
        map.put("distanceMiles", amounts.getDistanceMiles());
        map.put("currency", amounts.getCurrency());
        return map;
    }
    
    private static Map<String, Object> timesToMap(Delivery.Times times) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderedAt", dateToTimestamp(times.getOrderedAt()));
        map.put("acceptedAt", dateToTimestamp(times.getAcceptedAt()));
        map.put("pickedUpAt", dateToTimestamp(times.getPickedUpAt()));
        map.put("completedAt", dateToTimestamp(times.getCompletedAt()));
        map.put("tippedAt", dateToTimestamp(times.getTippedAt()));
        map.put("estimatedDuration", times.getEstimatedDuration());
        return map;
    }
    
    private static Map<String, Object> statusToMap(Delivery.Status status) {
        Map<String, Object> map = new HashMap<>();
        map.put("state", status.getState());
        map.put("isTipped", status.isTipped());
        map.put("isCompleted", status.isCompleted());
        map.put("isVerified", status.isVerified());
        map.put("cancellationReason", status.getCancellationReason());
        map.put("verificationSource", status.getVerificationSource());
        map.put("verificationTimestamp", dateToTimestamp(status.getVerificationTimestamp()));
        return map;
    }
    
    private static Map<String, Object> platformToMap(Delivery.Platform platform) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", platform.getName());
        map.put("displayName", platform.getDisplayName());
        map.put("iconUrl", platform.getIconUrl());
        return map;
    }
    
    private static Map<String, Object> customerToMap(Delivery.Customer customer) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", customer.getName());
        map.put("contactInfo", customer.getContactInfo());
        map.put("rating", customer.getRating());
        return map;
    }
    
    private static Map<String, Object> itemsToMap(Delivery.Items items) {
        Map<String, Object> map = new HashMap<>();
        map.put("count", items.getCount());
        map.put("description", items.getDescription());
        map.put("itemsList", items.getItemsList());
        return map;
    }
    
    private static Map<String, Object> disputeInfoToMap(Delivery.DisputeInfo disputeInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("hasDispute", disputeInfo.isHasDispute());
        map.put("disputeReason", disputeInfo.getDisputeReason());
        map.put("disputeStatus", disputeInfo.getDisputeStatus());
        map.put("resolution", disputeInfo.getResolution());
        map.put("disputeDate", dateToTimestamp(disputeInfo.getDisputeDate()));
        return map;
    }
    
    private static Map<String, Object> metadataToMap(Delivery.Metadata metadata) {
        Map<String, Object> map = new HashMap<>();
        map.put("createdAt", dateToTimestamp(metadata.getCreatedAt()));
        map.put("updatedAt", dateToTimestamp(metadata.getUpdatedAt()));
        map.put("source", metadata.getSource());
        map.put("importId", metadata.getImportId());
        map.put("captureId", metadata.getCaptureId());
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
