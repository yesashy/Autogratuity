package com.autogratuity.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Manager class for handling map operations and marker display
 * Refactored to use domain repositories with RxJava.
 */
public class MapManager {
    private static final String TAG = "MapManager";
    
    // Google Maps related fields
    private final GoogleMap googleMap;
    private final Context context;
    
    // Repositories for data access
    private final AddressRepository addressRepository;
    private final DeliveryRepository deliveryRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    // Marker tracking
    private final Map<String, Marker> markerMap = new HashMap<>();
    private boolean markersVisible = true;
    private boolean isLoading = false;
    
    // Constants for map display
    private static final float DEFAULT_ZOOM = 14.0f;
    private static final int DEFAULT_MARKER_COLOR = 0xFF0000FF; // Blue
    private static final int TIPPED_MARKER_COLOR = 0xFF00AA00; // Green
    private static final int LOW_TIP_MARKER_COLOR = 0xFFFF0000; // Red
    private static final int DO_NOT_DELIVER_COLOR = 0xFF000000; // Black
    
    // Currency formatter
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    
    /**
     * Create a new MapManager
     *
     * @param context The application context
     * @param googleMap The GoogleMap object
     * @param addressRepository The address repository for data access
     * @param deliveryRepository The delivery repository for data access
     */
    public MapManager(Context context, GoogleMap googleMap, 
                    AddressRepository addressRepository, 
                    DeliveryRepository deliveryRepository) {
        this.context = context;
        this.googleMap = googleMap;
        this.addressRepository = addressRepository;
        this.deliveryRepository = deliveryRepository;
        
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
        
        // Safety check for valid coordinates
        if (Double.isNaN(latitude) || Double.isNaN(longitude) ||
            Double.isInfinite(latitude) || Double.isInfinite(longitude)) {
            Log.w(TAG, "Invalid coordinates for marker: " + id);
            return null;
        }
        
        // Remove existing marker with same ID if present
        if (markerMap.containsKey(id)) {
            markerMap.get(id).remove();
        }
        
        // Create new marker
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(title != null ? title : "Unknown")
                .snippet(snippet != null ? snippet : "")
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
        // Prevent multiple simultaneous loading operations
        if (isLoading) {
            Log.w(TAG, "Already loading data, ignoring request");
            return;
        }
        
        isLoading = true;
        clearMarkers();
        
        // Show loading toast
        Toast.makeText(context, "Loading addresses...", Toast.LENGTH_SHORT).show();
        
        // Load addresses using AddressRepository with RxJava
        disposables.add(
            addressRepository.getAddresses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> isLoading = false)
                .subscribe(
                    addresses -> {
                        List<LatLng> points = new ArrayList<>();
                        
                        if (addresses.isEmpty()) {
                            Toast.makeText(context, "No addresses found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Process each address (limit to specified count)
                        int count = Math.min(addresses.size(), limit);
                        for (int i = 0; i < count; i++) {
                            Address address = addresses.get(i);
                            
                            // Skip addresses without location
                            if (address.getLocation() == null) continue;
                            
                            double latitude = address.getLocation().getLatitude();
                            double longitude = address.getLocation().getLongitude();
                            
                            // Skip invalid coordinates
                            if (latitude == 0 && longitude == 0) continue;
                            
                            points.add(new LatLng(latitude, longitude));
                            
                            // Determine marker color based on address properties
                            int markerColor = DEFAULT_MARKER_COLOR;
                            
                            // Check for do not deliver flag
                            if (address.getFlags() != null && address.getFlags().isDoNotDeliver()) {
                                markerColor = DO_NOT_DELIVER_COLOR;
                            } 
                            // Check for tip data
                            else if (address.getDeliveryStats() != null) {
                                double avgTip = address.getDeliveryStats().getAverageTip();
                                if (avgTip > 0) {
                                    markerColor = avgTip > 5.0 ? TIPPED_MARKER_COLOR : LOW_TIP_MARKER_COLOR;
                                }
                            }
                            
                            // Get title and snippet
                            String title = address.getFullAddress();
                            String snippet = "";
                            
                            // Add delivery stats if available
                            if (address.getDeliveryStats() != null) {
                                Address.DeliveryStats stats = address.getDeliveryStats();
                                snippet = String.format("Avg Tip: %s (%d deliveries)",
                                        currencyFormat.format(stats.getAverageTip()), 
                                        stats.getDeliveryCount());
                            }
                            
                            addMarker(address.getAddressId(), latitude, longitude, title, snippet, markerColor);
                        }
                        
                        // Move camera to show all markers
                        if (!points.isEmpty()) {
                            zoomToFitMarkers(points);
                            Toast.makeText(context, count + " addresses loaded", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error loading addresses: " + error.getMessage(), error);
                        Toast.makeText(context, "Error loading addresses", Toast.LENGTH_SHORT).show();
                    }
                )
        );
    }
    
    /**
     * Load and display markers for recent deliveries
     */
    public void loadRecentImportMarkers() {
        // Prevent multiple simultaneous loading operations
        if (isLoading) {
            Log.w(TAG, "Already loading data, ignoring request");
            return;
        }
        
        isLoading = true;
        clearMarkers();
        
        // Show loading toast
        Toast.makeText(context, "Loading recent deliveries...", Toast.LENGTH_SHORT).show();
        
        // Load recent deliveries using DeliveryRepository with RxJava
        disposables.add(
            deliveryRepository.getRecentDeliveries(50)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> isLoading = false)
                .subscribe(
                    deliveries -> {
                        List<LatLng> points = new ArrayList<>();
                        int validMarkers = 0;
                        
                        if (deliveries.isEmpty()) {
                            Toast.makeText(context, "No recent deliveries found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        for (int i = 0; i < deliveries.size(); i++) {
                            try {
                                Delivery delivery = deliveries.get(i);
                                
                                // Skip deliveries without address
                                if (delivery.getAddress() == null) continue;
                                
                                // Get coordinates from address
                                double latitude = 0;
                                double longitude = 0;
                                
                                // Try to get coordinates
                                try {
                                    latitude = delivery.getAddress().getLatitude();
                                    longitude = delivery.getAddress().getLongitude();
                                } catch (Exception e) {
                                    // If we can't get coordinates, skip this delivery
                                    Log.w(TAG, "Error getting delivery coordinates: " + e.getMessage());
                                    continue;
                                }
                                
                                // Skip invalid coordinates
                                if (latitude == 0 && longitude == 0) continue;
                                
                                points.add(new LatLng(latitude, longitude));
                                validMarkers++;
                                
                                // Set marker properties
                                String title = delivery.getAddress().getFullAddress();
                                if (title == null || title.isEmpty()) {
                                    title = "Unnamed Location";
                                }
                                
                                String snippet = "";
                                
                                // Get creation/delivery date if available
                                if (delivery.getTimes() != null && delivery.getTimes().getCompletedAt() != null) {
                                    snippet = "Delivered: " + delivery.getTimes().getCompletedAt().toString();
                                } else if (delivery.getMetadata() != null && delivery.getMetadata().getCreatedAt() != null) {
                                    snippet = "Created: " + delivery.getMetadata().getCreatedAt().toString();
                                }
                                
                                // Add tip amount if available
                                if (delivery.getAmounts() != null && delivery.getAmounts().getTipAmount() > 0) {
                                    if (!snippet.isEmpty()) {
                                        snippet += " | ";
                                    }
                                    snippet += "Tip: " + currencyFormat.format(delivery.getAmounts().getTipAmount());
                                }
                                
                                // Determine marker color based on tip
                                int markerColor = DEFAULT_MARKER_COLOR;
                                if (delivery.getStatus() != null && delivery.getStatus().isTipped() &&
                                        delivery.getAmounts() != null && delivery.getAmounts().getTipAmount() > 0) {
                                    markerColor = TIPPED_MARKER_COLOR;
                                }
                                
                                addMarker("delivery_" + delivery.getDeliveryId(), 
                                        latitude, longitude, title, snippet, markerColor);
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing delivery: " + e.getMessage(), e);
                            }
                        }
                        
                        // Move camera to show all markers
                        if (!points.isEmpty()) {
                            zoomToFitMarkers(points);
                            Toast.makeText(context, validMarkers + " deliveries loaded", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "No valid delivery locations found", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error loading recent deliveries: " + error.getMessage(), error);
                        Toast.makeText(context, "Error loading deliveries", Toast.LENGTH_SHORT).show();
                    }
                )
        );
    }
    
    /**
     * Display markers for a list of Delivery objects
     * Used after bulk imports
     *
     * @param deliveries List of Delivery objects to display
     */
    public void displayDeliveryMarkers(List<Delivery> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            Toast.makeText(context, "No deliveries to display", Toast.LENGTH_SHORT).show();
            return;
        }
        
        clearMarkers();
        List<LatLng> points = new ArrayList<>();
        int validMarkers = 0;
        
        for (int i = 0; i < deliveries.size(); i++) {
            try {
                Delivery delivery = deliveries.get(i);
                
                // Skip deliveries without address
                if (delivery.getAddress() == null) continue;
                
                // Get coordinates from address
                double latitude = 0;
                double longitude = 0;
                
                // Try to get coordinates
                try {
                    latitude = delivery.getAddress().getLatitude();
                    longitude = delivery.getAddress().getLongitude();
                } catch (Exception e) {
                    // If we can't get coordinates, skip this delivery
                    continue;
                }
                
                // Skip invalid coordinates
                if (latitude == 0 && longitude == 0) continue;
                
                points.add(new LatLng(latitude, longitude));
                validMarkers++;
                
                // Determine marker properties
                String title = delivery.getAddress().getFullAddress();
                if (title == null || title.isEmpty()) {
                    title = "Unnamed Location";
                }
                
                String snippet = "";
                if (delivery.getAmounts() != null && delivery.getAmounts().getTipAmount() > 0) {
                    snippet = "Tip: " + currencyFormat.format(delivery.getAmounts().getTipAmount());
                } else {
                    snippet = "No tip data";
                }
                
                // Determine marker color
                int markerColor = DEFAULT_MARKER_COLOR;
                if (delivery.getStatus() != null && delivery.getStatus().isTipped() &&
                        delivery.getAmounts() != null && delivery.getAmounts().getTipAmount() > 0) {
                    markerColor = TIPPED_MARKER_COLOR;
                }
                
                addMarker("import_" + i, latitude, longitude, title, snippet, markerColor);
            } catch (Exception e) {
                Log.e(TAG, "Error processing delivery for display: " + e.getMessage(), e);
            }
        }
        
        // Move camera to show all markers
        if (!points.isEmpty()) {
            zoomToFitMarkers(points);
            Toast.makeText(context, validMarkers + " deliveries displayed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "No valid delivery locations to display", Toast.LENGTH_SHORT).show();
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
    
    /**
     * Clean up resources when manager is no longer needed
     */
    public void dispose() {
        // Clear all RxJava subscriptions
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
        
        // Clear markers
        clearMarkers();
    }
}