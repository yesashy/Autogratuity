package com.autogratuity.data.model;

import com.google.firebase                                                                                                                                                                      .Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.List;

/**
 * Model class representing an address in the Autogratuity app.
 * Maps to documents in the addresses collection in Firestore.
 */
public class Address {
    
    @DocumentId
    private String addressId;
    
    private String userId;
    private String fullAddress;
    private String normalizedAddress;
    private boolean isDefault;
    private String notes;
    private List<String> tags;
    
    // Nested objects
    private Components components;
    private Location location;
    private SearchFields searchFields;
    private DeliveryStats deliveryStats;
    private Flags flags;
    private Metadata metadata;
    
    // Default constructor required for Firestore
    public Address() {
    }
    
    /**
     * Nested class for address components
     */
    public static class Components {
        private String streetNumber;
        private String streetName;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        
        public Components() {
        }
        
        // Getters and setters
        public String getStreetNumber() {
            return streetNumber;
        }
        
        public void setStreetNumber(String streetNumber) {
            this.streetNumber = streetNumber;
        }
        
        public String getStreetName() {
            return streetName;
        }
        
        public void setStreetName(String streetName) {
            this.streetName = streetName;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getState() {
            return state;
        }
        
        public void setState(String state) {
            this.state = state;
        }
        
        public String getPostalCode() {
            return postalCode;
        }
        
        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
    }
    
    /**
     * Nested class for geographic location
     */
    public static class Location {
        private double latitude;
        private double longitude;
        private String geohash;
        
        public Location() {
        }
        
        // Getters and setters
        public double getLatitude() {
            return latitude;
        }
        
        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
        
        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
        
        public String getGeohash() {
            return geohash;
        }
        
        public void setGeohash(String geohash) {
            this.geohash = geohash;
        }
    }
    
    /**
     * Nested class for search field information
     */
    public static class SearchFields {
        private List<String> searchTerms;
        private String normalizedKey;
        
        public SearchFields() {
        }
        
        // Getters and setters
        public List<String> getSearchTerms() {
            return searchTerms;
        }
        
        public void setSearchTerms(List<String> searchTerms) {
            this.searchTerms = searchTerms;
        }
        
        public String getNormalizedKey() {
            return normalizedKey;
        }
        
        public void setNormalizedKey(String normalizedKey) {
            this.normalizedKey = normalizedKey;
        }
    }
    
    /**
     * Nested class for delivery statistics
     */
    public static class DeliveryStats {
        private int deliveryCount;
        private int tipCount;
        private double totalTips;
        private double averageTip;
        private double highestTip;
        private Timestamp lastDeliveryDate;
        
        public DeliveryStats() {
        }
        
        // Getters and setters
        public int getDeliveryCount() {
            return deliveryCount;
        }
        
        public void setDeliveryCount(int deliveryCount) {
            this.deliveryCount = deliveryCount;
        }
        
        public int getTipCount() {
            return tipCount;
        }
        
        public void setTipCount(int tipCount) {
            this.tipCount = tipCount;
        }
        
        public double getTotalTips() {
            return totalTips;
        }
        
        public void setTotalTips(double totalTips) {
            this.totalTips = totalTips;
        }
        
        public double getAverageTip() {
            return averageTip;
        }
        
        public void setAverageTip(double averageTip) {
            this.averageTip = averageTip;
        }
        
        public double getHighestTip() {
            return highestTip;
        }
        
        public void setHighestTip(double highestTip) {
            this.highestTip = highestTip;
        }
        
        public Date getLastDeliveryDate() {
            return lastDeliveryDate != null ? lastDeliveryDate.toDate() : null;
        }
        
        public void setLastDeliveryDate(Date lastDeliveryDate) {
            this.lastDeliveryDate = lastDeliveryDate != null ? new Timestamp(lastDeliveryDate) : null;
        }
    }
    
    /**
     * Nested class for address flags
     */
    public static class Flags {
        private boolean doNotDeliver;
        private boolean favorite;
        private boolean verified;
        private boolean hasAccessIssues;
        private boolean isApartment;
        
        public Flags() {
        }
        
        // Getters and setters
        public boolean isDoNotDeliver() {
            return doNotDeliver;
        }
        
        public void setDoNotDeliver(boolean doNotDeliver) {
            this.doNotDeliver = doNotDeliver;
        }
        
        public boolean isFavorite() {
            return favorite;
        }
        
        public void setFavorite(boolean favorite) {
            this.favorite = favorite;
        }
        
        public boolean isVerified() {
            return verified;
        }
        
        public void setVerified(boolean verified) {
            this.verified = verified;
        }
        
        public boolean isHasAccessIssues() {
            return hasAccessIssues;
        }
        
        public void setHasAccessIssues(boolean hasAccessIssues) {
            this.hasAccessIssues = hasAccessIssues;
        }
        
        public boolean isApartment() {
            return isApartment;
        }
        
        public void setApartment(boolean apartment) {
            isApartment = apartment;
        }
    }
    
    /**
     * Nested class for metadata
     */
    public static class Metadata {
        private Timestamp createdAt;
        private Timestamp updatedAt;
        private String source;
        private String importId;
        private long version;
        
        public Metadata() {
        }
        
        // Getters and setters
        public Date getCreatedAt() {
            return createdAt != null ? createdAt.toDate() : null;
        }
        
        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt != null ? new Timestamp(createdAt) : null;
        }
        
        public Date getUpdatedAt() {
            return updatedAt != null ? updatedAt.toDate() : null;
        }
        
        public void setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt != null ? new Timestamp(updatedAt) : null;
        }
        
        public String getSource() {
            return source;
        }
        
        public void setSource(String source) {
            this.source = source;
        }
        
        public String getImportId() {
            return importId;
        }
        
        public void setImportId(String importId) {
            this.importId = importId;
        }
        
        public long getVersion() {
            return version;
        }
        
        public void setVersion(long version) {
            this.version = version;
        }
    }
    
    // Getters and setters
    
    public String getAddressId() {
        return addressId;
    }
    
    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getFullAddress() {
        return fullAddress;
    }
    
    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }
    
    public String getNormalizedAddress() {
        return normalizedAddress;
    }
    
    public void setNormalizedAddress(String normalizedAddress) {
        this.normalizedAddress = normalizedAddress;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public Components getComponents() {
        return components;
    }
    
    public void setComponents(Components components) {
        this.components = components;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public SearchFields getSearchFields() {
        return searchFields;
    }
    
    public void setSearchFields(SearchFields searchFields) {
        this.searchFields = searchFields;
    }
    
    public DeliveryStats getDeliveryStats() {
        return deliveryStats;
    }
    
    public void setDeliveryStats(DeliveryStats deliveryStats) {
        this.deliveryStats = deliveryStats;
    }
    
    public Flags getFlags() {
        return flags;
    }
    
    public void setFlags(Flags flags) {
        this.flags = flags;
    }
    
    public Metadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Creates a simple address for search and display purposes only
     * 
     * @return A simplified address with minimal fields
     */
    @Exclude
    public SimpleAddress toSimpleAddress() {
        SimpleAddress simple = new SimpleAddress();
        simple.setAddressId(addressId);
        simple.setFullAddress(fullAddress);
        
        if (location != null) {
            simple.setLatitude(location.getLatitude());
            simple.setLongitude(location.getLongitude());
        }
        
        return simple;
    }
    
    /**
     * Simple class for address reference in other objects
     */
    public static class SimpleAddress {
        private String addressId;
        private String fullAddress;
        private double latitude;
        private double longitude;
        
        public SimpleAddress() {
        }
        
        // Getters and setters
        public String getAddressId() {
            return addressId;
        }
        
        public void setAddressId(String addressId) {
            this.addressId = addressId;
        }
        
        public String getFullAddress() {
            return fullAddress;
        }
        
        public void setFullAddress(String fullAddress) {
            this.fullAddress = fullAddress;
        }
        
        public double getLatitude() {
            return latitude;
        }
        
        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
        
        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
