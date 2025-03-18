package com.autogratuity.data.model;

/**
 * Model class representing geographic coordinates in the Autogratuity app.
 */
public class Coordinates {
    private double latitude;
    private double longitude;
    
    // Default constructor required for Firestore
    public Coordinates() {
    }
    
    /**
     * Constructor with latitude and longitude
     * 
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     */
    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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
}
