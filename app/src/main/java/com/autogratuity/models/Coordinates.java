package com.autogratuity.models;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for geographic coordinates
 */
public class Coordinates {
    private double latitude;
    private double longitude;

    /**
     * Default constructor
     */
    public Coordinates() {
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    /**
     * Create Coordinates with specific values
     *
     * @param latitude Latitude
     * @param longitude Longitude
     */
    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Create Coordinates from a GeoPoint
     *
     * @param geoPoint GeoPoint to convert
     * @return Coordinates
     */
    public static Coordinates fromGeoPoint(GeoPoint geoPoint) {
        if (geoPoint == null) {
            return new Coordinates();
        }
        return new Coordinates(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    /**
     * Convert to a GeoPoint
     *
     * @return GeoPoint representation
     */
    public GeoPoint toGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }

    /**
     * Convert to Firestore data
     *
     * @return Map of data
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        return map;
    }

    /**
     * Convert to string format
     *
     * @return Comma-separated string
     */
    public String toString() {
        return latitude + "," + longitude;
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

    /**
     * Calculate distance to another coordinate in kilometers
     *
     * @param other Other coordinates
     * @return Distance in kilometers
     */
    public double distanceTo(Coordinates other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }
        
        // Haversine formula
        double earthRadius = 6371; // kilometers
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}