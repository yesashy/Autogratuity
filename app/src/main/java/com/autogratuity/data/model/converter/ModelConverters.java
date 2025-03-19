package com.autogratuity.data.model.converter;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Coordinates;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.Metadata;
import com.autogratuity.data.model.Reference;
import com.autogratuity.data.model.Status;
import com.autogratuity.data.model.Times;
import com.autogratuity.data.model.Amounts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class that provides conversion methods between different model types.
 * Used to maintain compatibility between different parts of the application.
 */
public class ModelConverters {

    // Address <--> SimpleAddress conversions
    
    /**
     * Convert an Address to a SimpleAddress
     * @param address The full address to convert
     * @return A simplified address with minimal fields
     */
    public static Address.SimpleAddress toSimpleAddress(Address address) {
        if (address == null) return null;
        
        // Use the existing method in Address
        return address.toSimpleAddress();
    }
    
    /**
     * Convert a SimpleAddress to an Address
     * @param simpleAddress The simple address to convert
     * @return A full address with available fields populated
     */
    public static Address fromSimpleAddress(Address.SimpleAddress simpleAddress) {
        if (simpleAddress == null) return null;
        
        Address address = new Address();
        address.setAddressId(simpleAddress.getAddressId());
        address.setFullAddress(simpleAddress.getFullAddress());
        
        // Create Location if latitude/longitude are present
        if (simpleAddress.getLatitude() != 0 || simpleAddress.getLongitude() != 0) {
            Address.Location location = new Address.Location();
            location.setLatitude(simpleAddress.getLatitude());
            location.setLongitude(simpleAddress.getLongitude());
            address.setLocation(location);
        }
        
        return address;
    }
    
    // Coordinates <--> Location conversions
    
    /**
     * Convert Coordinates to Address.Location
     * @param coordinates The coordinates to convert
     * @return A Location object with the same coordinates
     */
    public static Address.Location toLocation(Coordinates coordinates) {
        if (coordinates == null) return null;
        
        Address.Location location = new Address.Location();
        location.setLatitude(coordinates.getLatitude());
        location.setLongitude(coordinates.getLongitude());
        
        return location;
    }
    
    /**
     * Convert Address.Location to Coordinates
     * @param location The location to convert
     * @return A Coordinates object with the same latitude/longitude
     */
    public static Coordinates toCoordinates(Address.Location location) {
        if (location == null) return null;
        
        return new Coordinates(location.getLatitude(), location.getLongitude());
    }
    
    // Reference <--> Delivery.Reference conversions
    
    /**
     * Convert a standalone Reference to a Delivery.Reference
     * @param reference The standalone reference
     * @return A Delivery.Reference with the appropriate fields
     */
    public static Delivery.Reference toDeliveryReference(Reference reference) {
        if (reference == null) return null;
        
        Delivery.Reference deliveryReference = new Delivery.Reference();
        deliveryReference.setAddressId(reference.getAddressId());
        deliveryReference.setPlatformOrderId(reference.getPlatformOrderId());
        
        return deliveryReference;
    }
    
    /**
     * Convert a Delivery.Reference to a standalone Reference
     * @param deliveryReference The delivery reference
     * @return A standalone Reference with appropriate fields
     */
    public static Reference toReference(Delivery.Reference deliveryReference) {
        if (deliveryReference == null) return null;
        
        Reference reference = new Reference();
        reference.setAddressId(deliveryReference.getAddressId());
        reference.setPlatformOrderId(deliveryReference.getPlatformOrderId());
        
        return reference;
    }
    
    // Metadata conversion methods
    
    /**
     * Convert a standalone Metadata to a Delivery.Metadata
     * @param metadata The standalone metadata
     * @return A Delivery.Metadata with the appropriate fields
     */
    public static Delivery.Metadata toDeliveryMetadata(Metadata metadata) {
        if (metadata == null) return null;
        
        Delivery.Metadata deliveryMetadata = new Delivery.Metadata();
        deliveryMetadata.setCreatedAt(metadata.getCreatedAt());
        deliveryMetadata.setUpdatedAt(metadata.getUpdatedAt());
        deliveryMetadata.setSource(metadata.getSource());
        deliveryMetadata.setImportId(metadata.getImportId());
        deliveryMetadata.setVersion(metadata.getVersion());
        
        return deliveryMetadata;
    }
    
    /**
     * Convert a Delivery.Metadata to a standalone Metadata
     * @param deliveryMetadata The delivery metadata
     * @return A standalone Metadata with appropriate fields
     */
    public static Metadata toMetadata(Delivery.Metadata deliveryMetadata) {
        if (deliveryMetadata == null) return null;
        
        Metadata metadata = new Metadata();
        metadata.setCreatedAt(deliveryMetadata.getCreatedAt());
        metadata.setUpdatedAt(deliveryMetadata.getUpdatedAt());
        metadata.setSource(deliveryMetadata.getSource());
        metadata.setImportId(deliveryMetadata.getImportId());
        metadata.setVersion(deliveryMetadata.getVersion());
        
        return metadata;
    }
    
    // Status conversion methods
    
    /**
     * Convert a standalone Status to a Delivery.Status
     * @param status The standalone status
     * @return A Delivery.Status with the appropriate fields
     */
    public static Delivery.Status toDeliveryStatus(Status status) {
        if (status == null) return null;
        
        Delivery.Status deliveryStatus = new Delivery.Status();
        deliveryStatus.setState(status.getState());
        deliveryStatus.setCompleted(status.isCompleted());
        deliveryStatus.setTipped(status.isTipped());
        deliveryStatus.setVerified(status.isVerified());
        deliveryStatus.setCancellationReason(status.getCancellationReason());
        deliveryStatus.setVerificationSource(status.getVerificationSource());
        deliveryStatus.setVerificationTimestamp(status.getVerificationTimestamp());
        
        return deliveryStatus;
    }
    
    /**
     * Convert a Delivery.Status to a standalone Status
     * @param deliveryStatus The delivery status
     * @return A standalone Status with appropriate fields
     */
    public static Status toStatus(Delivery.Status deliveryStatus) {
        if (deliveryStatus == null) return null;
        
        Status status = new Status();
        status.setState(deliveryStatus.getState());
        status.setCompleted(deliveryStatus.isCompleted());
        status.setTipped(deliveryStatus.isTipped());
        status.setVerified(deliveryStatus.isVerified());
        status.setCancellationReason(deliveryStatus.getCancellationReason());
        status.setVerificationSource(deliveryStatus.getVerificationSource());
        status.setVerificationTimestamp(deliveryStatus.getVerificationTimestamp());
        
        return status;
    }
    
    // Times conversion methods
    
    /**
     * Convert a standalone Times to a Delivery.Times
     * @param times The standalone times
     * @return A Delivery.Times with the appropriate fields
     */
    public static Delivery.Times toDeliveryTimes(Times times) {
        if (times == null) return null;
        
        Delivery.Times deliveryTimes = new Delivery.Times();
        deliveryTimes.setOrderedAt(times.getOrderedAt());
        deliveryTimes.setAcceptedAt(times.getAcceptedAt());
        deliveryTimes.setPickedUpAt(times.getPickedUpAt());
        deliveryTimes.setCompletedAt(times.getCompletedAt());
        deliveryTimes.setTippedAt(times.getTippedAt());
        deliveryTimes.setEstimatedDuration(times.getEstimatedDuration());
        
        return deliveryTimes;
    }
    
    /**
     * Convert a Delivery.Times to a standalone Times
     * @param deliveryTimes The delivery times
     * @return A standalone Times with appropriate fields
     */
    public static Times toTimes(Delivery.Times deliveryTimes) {
        if (deliveryTimes == null) return null;
        
        Times times = new Times();
        times.setOrderedAt(deliveryTimes.getOrderedAt());
        times.setAcceptedAt(deliveryTimes.getAcceptedAt());
        times.setPickedUpAt(deliveryTimes.getPickedUpAt());
        times.setCompletedAt(deliveryTimes.getCompletedAt());
        times.setTippedAt(deliveryTimes.getTippedAt());
        times.setEstimatedDuration(deliveryTimes.getEstimatedDuration());
        
        return times;
    }
    
    // Amounts conversion methods
    
    /**
     * Convert a standalone Amounts to a Delivery.Amounts
     * @param amounts The standalone amounts
     * @return A Delivery.Amounts with the appropriate fields
     */
    public static Delivery.Amounts toDeliveryAmounts(Amounts amounts) {
        if (amounts == null) return null;
        
        Delivery.Amounts deliveryAmounts = new Delivery.Amounts();
        deliveryAmounts.setBaseAmount(amounts.getBaseAmount());
        deliveryAmounts.setEstimatedPay(amounts.getEstimatedPay());
        deliveryAmounts.setFinalPay(amounts.getFinalPay());
        deliveryAmounts.setTipAmount(amounts.getTipAmount());
        deliveryAmounts.setTipPercentage(amounts.getTipPercentage());
        deliveryAmounts.setDistanceMiles(amounts.getDistanceMiles());
        deliveryAmounts.setCurrency(amounts.getCurrency());
        
        return deliveryAmounts;
    }
    
    /**
     * Convert a Delivery.Amounts to a standalone Amounts
     * @param deliveryAmounts The delivery amounts
     * @return A standalone Amounts with appropriate fields
     */
    public static Amounts toAmounts(Delivery.Amounts deliveryAmounts) {
        if (deliveryAmounts == null) return null;
        
        Amounts amounts = new Amounts();
        amounts.setBaseAmount(deliveryAmounts.getBaseAmount());
        amounts.setEstimatedPay(deliveryAmounts.getEstimatedPay());
        amounts.setFinalPay(deliveryAmounts.getFinalPay());
        amounts.setTipAmount(deliveryAmounts.getTipAmount());
        amounts.setTipPercentage(deliveryAmounts.getTipPercentage());
        amounts.setDistanceMiles(deliveryAmounts.getDistanceMiles());
        amounts.setCurrency(deliveryAmounts.getCurrency());
        
        return amounts;
    }
    
    /**
     * Helper method to get Location from an Address for interfaces that expect it
     * @param address The address to get location from
     * @return The location if available, null otherwise
     */
    public static Address.Location getLocation(Address address) {
        if (address == null) return null;
        return address.getLocation();
    }
    
    /**
     * Helper method to get Location from a SimpleAddress
     * Creates a Location object from the available coordinates
     * @param simpleAddress The simple address to get location from
     * @return A new Location object with the coordinates
     */
    public static Address.Location getLocation(Address.SimpleAddress simpleAddress) {
        if (simpleAddress == null) return null;
        
        Address.Location location = new Address.Location();
        location.setLatitude(simpleAddress.getLatitude());
        location.setLongitude(simpleAddress.getLongitude());
        
        return location;
    }
    
    /**
     * Helper method to get custom data from an Address
     * @param address The address to get custom data from
     * @return The custom data Map or an empty Map if not available
     */
    public static Map<String, Object> getCustomData(Address address) {
        if (address == null || address.getMetadata() == null) {
            return new HashMap<>();
        }
        
        return address.getMetadata().getCustomData();
    }
}
