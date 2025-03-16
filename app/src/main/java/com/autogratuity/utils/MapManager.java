package com.autogratuity.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.autogratuity.models.Address;
import com.autogratuity.models.Coordinates;
import com.autogratuity.models.DeliveryData;
import com.autogratuity.models.TipData;
import com.autogratuity.repositories.IFirestoreRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class for handling map operations and marker display
 * Ensures markers are properly displayed on the map after imports
 */
public class MapManager {
    private static final String TAG = "MapManager";
    
    // Google Maps related fields
    private final GoogleMap googleMap;
    private final Context context;
    
    // Repository for data access
    private final IFirestoreRepository repository;
    
    // Marker tracking
    private final Map<String, Marker> markerMap = new HashMap<>();
    private boolean markersVisible = true;
    
    // Constants for map display
    private static final float DEFAULT_ZOOM = 14.0f;
    private static final int DEFAULT_MARKER_COLOR = 0xFF0000FF; // Blue
    private static final int TIPPED_MARKER_COLOR = 0xFF00AA00; // Green
    private static final int LOW_TIP_MARKER_COLOR = 0xFFFF0000; // Red
    private static final int DO_NOT_DELIVER_COLOR = 0xFF000000; // Black
    
    /**
     * Create a new MapManager
     *
     * @param context The application context
     * @param googleMap The GoogleMap object
     * @param repository The repository for data access
     */
    public MapManager(Context context, GoogleMap googleMap, IFirestoreRepository repository) {
        this.context = context;
        this.googleMap = googleMap;
        this.repository = repository;
        
        if (googleMap != null) {
            setupMap();
        }
    }
    
    /**
     * Setup initial map configuration
     */
    private void setupMap() {
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted: " + e.getMessage());
        }
        
        // Setup marker click listeners
        googleMap.setOnMarkerClickListener(marker -> {
            showMarkerInfo(marker);
            return false; // False to allow default behavior (info window)
        });
    }
    
    /**
     * Add a marker to the map
     *
     * @param id Unique identifier for the marker
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param title Title for the marker
     * @param snippet Additional info for the marker
     * @param color Color for the marker
     * @return The created marker
     */
    public Marker addMarker(String id, double latitude, double longitude, String title, String snippet, @ColorInt int color) {
        if (googleMap == null || !markersVisible) {
            return null;
        }
        
        // Remove existing marker with same ID if present
        if (markerMap.containsKey(id)) {
            markerMap.get(id).remove();
        }
        
        // Create new marker
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(title)
                .snippet(snippet)
                .icon(getMarkerIcon(color));
        
        Marker marker = googleMap.addMarker(options);
        if (marker != null) {
            marker.setTag(id);
            markerMap.put(id, marker);
        }
        
        return marker;
    }
    
    /**
     * Create a colored marker icon
     *
     * @param color Color for the marker
     * @return BitmapDescriptor for the colored marker
     */
    private BitmapDescriptor getMarkerIcon(@ColorInt int color) {
        return getVectorBitmapDescriptor(context, android.R.drawable.ic_menu_mylocation, color);
    }
    
    /**
     * Create a BitmapDescriptor from a vector drawable
     *
     * @param context Context for resource access
     * @param vectorResId Resource ID of the vector drawable
     * @param color Color to tint the drawable
     * @return BitmapDescriptor created from the drawable
     */
    private BitmapDescriptor getVectorBitmapDescriptor(Context context, @DrawableRes int vectorResId, @ColorInt int color) {
        Drawable drawable = ContextCompat.getDrawable(context, vectorResId);
        if (drawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }
        
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    
    /**
     * Show information for a marker
     *
     * @param marker The marker to show info for
     */
    private void showMarkerInfo(Marker marker) {
        String id = (String) marker.getTag();
        if (id == null) {
            return;
        }
        
        // Here you could load additional data about the marker
        // and show it in a custom view or dialog
        marker.showInfoWindow();
    }
    
    /**
     * Clear all markers from the map
     */
    public void clearMarkers() {
        for (Marker marker : markerMap.values()) {
            marker.remove();
        }
        markerMap.clear();
    }
    
    /**
     * Set visibility of all markers
     *
     * @param visible Whether markers should be visible
     */
    public void setMarkersVisible(boolean visible) {
        markersVisible = visible;
        for (Marker marker : markerMap.values()) {
            marker.setVisible(visible);
        }
    }
    
    /**
     * Load and display markers for addresses in the database
     *
     * @param limit Maximum number of addresses to load
     */
    public void loadAddressMarkers(int limit) {
        clearMarkers();
        
        // Load addresses from repository
        repository.getAddressesWithMultipleDeliveries(1)
                .addOnSuccessListener(querySnapshot -> {
                    List<LatLng> points = new ArrayList<>();
                    
                    // Process each address
                    for (int i = 0; i < querySnapshot.size() && i < limit; i++) {
                        Address address = Address.fromDocument(querySnapshot.getDocuments().get(i));
                        if (address == null) continue;
                        
                        GeoPoint geoPoint = address.getGeoPoint();
                        if (geoPoint == null) continue;
                        
                        double latitude = geoPoint.getLatitude();
                        double longitude = geoPoint.getLongitude();
                        
                        // Skip invalid coordinates
                        if (latitude == 0 && longitude == 0) continue;
                        
                        points.add(new LatLng(latitude, longitude));
                        
                        // Determine marker color based on address properties
                        int markerColor = DEFAULT_MARKER_COLOR;
                        if (address.isDoNotDeliver()) {
                            markerColor = DO_NOT_DELIVER_COLOR;
                        } else if (address.getAverageTip() > 0) {
                            markerColor = address.getAverageTip() > 5.0 ? 
                                    TIPPED_MARKER_COLOR : LOW_TIP_MARKER_COLOR;
                        }
                        
                        String title = address.getFullAddress();
                        String snippet = String.format("Avg Tip: $%.2f (%d deliveries)",
                                address.getAverageTip(), address.getDeliveryCount());
                        
                        addMarker(address.getId(), latitude, longitude, title, snippet, markerColor);
                    }
                    
                    // Move camera to show all markers
                    if (!points.isEmpty()) {
                        zoomToFitMarkers(points);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading addresses: " + e.getMessage()));
    }
    
    /**
     * Load and display markers for recent imports
     */
    public void loadRecentImportMarkers() {
        clearMarkers();
        
        // Load recent deliveries from repository
        repository.getRecentDeliveries(50)
                .addOnSuccessListener(querySnapshot -> {
                    List<LatLng> points = new ArrayList<>();
                    
                    for (int i = 0; i < querySnapshot.size(); i++) {
                        try {
                            // Convert to Delivery or DeliveryData based on your model
                            // This is just an example
                            Map<String, Object> data = querySnapshot.getDocuments().get(i).getData();
                            if (data == null) continue;
                            
                            // Extract location data
                            Map<String, Object> addressData = (Map<String, Object>) data.get("address");
                            if (addressData == null) continue;
                            
                            Object geoPointObj = addressData.get("geoPoint");
                            double latitude = 0;
                            double longitude = 0;
                            
                            if (geoPointObj instanceof Map) {
                                Map<String, Object> geoPointMap = (Map<String, Object>) geoPointObj;
                                if (geoPointMap.containsKey("latitude") && geoPointMap.containsKey("longitude")) {
                                    latitude = ((Number) geoPointMap.get("latitude")).doubleValue();
                                    longitude = ((Number) geoPointMap.get("longitude")).doubleValue();
                                }
                            } else if (geoPointObj instanceof GeoPoint) {
                                GeoPoint geoPoint = (GeoPoint) geoPointObj;
                                latitude = geoPoint.getLatitude();
                                longitude = geoPoint.getLongitude();
                            } else if (addressData.containsKey("coordinates")) {
                                String coordsStr = (String) addressData.get("coordinates");
                                if (coordsStr != null && coordsStr.contains(",")) {
                                    String[] parts = coordsStr.split(",");
                                    latitude = Double.parseDouble(parts[0]);
                                    longitude = Double.parseDouble(parts[1]);
                                }
                            }
                            
                            // Skip invalid coordinates
                            if (latitude == 0 && longitude == 0) continue;
                            
                            points.add(new LatLng(latitude, longitude));
                            
                            // Determine marker properties
                            String title = addressData.containsKey("fullAddress") ? 
                                    (String) addressData.get("fullAddress") : "Unnamed Location";
                            
                            String snippet = data.containsKey("importDate") ? 
                                    "Imported: " + data.get("importDate").toString() : "";
                            
                            addMarker("recent_" + i, latitude, longitude, title, snippet, DEFAULT_MARKER_COLOR);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing delivery: " + e.getMessage());
                        }
                    }
                    
                    // Move camera to show all markers
                    if (!points.isEmpty()) {
                        zoomToFitMarkers(points);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading recent deliveries: " + e.getMessage()));
    }
    
    /**
     * Display markers for a list of DeliveryData objects
     * Used after bulk imports
     *
     * @param deliveries List of DeliveryData objects to display
     */
    public void displayDeliveryMarkers(List<DeliveryData> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            return;
        }
        
        clearMarkers();
        List<LatLng> points = new ArrayList<>();
        
        for (int i = 0; i < deliveries.size(); i++) {
            DeliveryData delivery = deliveries.get(i);
            Address address = delivery.getAddress();
            if (address == null) continue;
            
            Coordinates coordinates = address.getCoordinates();
            if (coordinates == null) continue;
            
            double latitude = coordinates.getLatitude();
            double longitude = coordinates.getLongitude();
            
            // Skip invalid coordinates
            if (latitude == 0 && longitude == 0) continue;
            
            points.add(new LatLng(latitude, longitude));
            
            // Determine marker properties
            String title = address.getFullAddress();
            TipData tipData = delivery.getTipData();
            
            String snippet = tipData != null && tipData.getAmount() > 0 ? 
                    String.format("Tip: $%.2f", tipData.getAmount()) : "No tip data";
            
            int markerColor = tipData != null && tipData.getAmount() > 0 ? 
                    TIPPED_MARKER_COLOR : DEFAULT_MARKER_COLOR;
            
            addMarker("import_" + i, latitude, longitude, title, snippet, markerColor);
        }
        
        // Move camera to show all markers
        if (!points.isEmpty()) {
            zoomToFitMarkers(points);
        }
    }
    
    /**
     * Zoom map to fit all markers
     *
     * @param points List of points to include in the view
     */
    private void zoomToFitMarkers(List<LatLng> points) {
        if (points.isEmpty()) return;
        
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        
        final LatLngBounds bounds = builder.build();
        
        // Zoom with padding
        int padding = 100; // Padding in pixels
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }
    
    /**
     * Move camera to a specific location
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param zoom Zoom level
     */
    public void moveCamera(double latitude, double longitude, float zoom) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude, longitude), zoom));
    }
}