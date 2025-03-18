package com.autogratuity.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Coordinates;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.Metadata;
import com.autogratuity.data.model.Reference;
import com.autogratuity.data.model.Status;
import com.autogratuity.data.model.Times;
import com.autogratuity.data.model.Amounts;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.delivery.DeliveryRepository;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Utility for importing KML and KMZ files
 * Updated to use domain repositories with RxJava
 */
public class KmlImportUtil {
    private static final String TAG = "KmlImportUtil";

    // Regex pattern to extract order ID (numeric sequence at the beginning of name)
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^(\\d+)");

    // Extract tip amount if already present in the label
    private static final Pattern TIP_PATTERN = Pattern.compile("\\$(\\d+\\.\\d+)");

    private final Context context;
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // Keep track of imports
    private int totalPlacemarks = 0;
    private AtomicInteger processedPlacemarks = new AtomicInteger(0);

    /**
     * Create a new KmlImportUtil
     *
     * @param context The application context
     * @param deliveryRepository The repository for delivery operations
     * @param addressRepository The repository for address operations
     */
    public KmlImportUtil(Context context, DeliveryRepository deliveryRepository, 
                       AddressRepository addressRepository) {
        this.context = context;
        this.deliveryRepository = deliveryRepository;
        this.addressRepository = addressRepository;
    }

    /**
     * Import data from a KML or KMZ file
     * 
     * @param uri The URI of the KML or KMZ file
     * @return true if import started successfully, false otherwise
     */
    public boolean importFromKmlKmz(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(context, "Couldn't open file", Toast.LENGTH_SHORT).show();
                return false;
            }

            String fileName = uri.getLastPathSegment();
            if (fileName != null && fileName.toLowerCase().endsWith(".kmz")) {
                // For KMZ files, we need to unzip them first
                ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                ZipEntry zipEntry;

                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (zipEntry.getName().toLowerCase().endsWith(".kml")) {
                        // Found the KML file inside the KMZ
                        parseKml(zipInputStream);
                        break;
                    }
                }

                zipInputStream.close();
            } else {
                // For KML files, parse directly
                parseKml(inputStream);
                inputStream.close();
            }

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error reading file", e);
            Toast.makeText(context, "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Parse KML data from an input stream
     * 
     * @param inputStream The input stream containing KML data
     */
    private void parseKml(InputStream inputStream) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

            // Count total placemarks for progress tracking
            countPlacemarks(parser);

            // Reset stream and parser for second pass
            try {
                inputStream.reset();
            } catch (IOException e) {
                Log.e(TAG, "Cannot reset stream, using counting only", e);
                // If we can't reset, just proceed with counting
            }

            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            String currentTag = "";

            String name = null;
            String description = null;
            String coordinates = null;
            String styleUrl = null;

            // Start parsing
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if ("Placemark".equals(currentTag)) {
                            // Reset values for new placemark
                            name = null;
                            description = null;
                            coordinates = null;
                            styleUrl = null;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        String text = parser.getText().trim();
                        if (!text.isEmpty()) {
                            if ("name".equals(currentTag)) {
                                name = text;
                            } else if ("description".equals(currentTag)) {
                                description = text;
                            } else if ("coordinates".equals(currentTag)) {
                                coordinates = text;
                            } else if ("styleUrl".equals(currentTag)) {
                                styleUrl = text;
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("Placemark".equals(parser.getName()) && name != null) {
                            // Process completed placemark
                            processPlacemark(name, description, coordinates, styleUrl);
                        }
                        currentTag = "";
                        break;
                }
                eventType = parser.next();
            }

            Toast.makeText(context, "Started import of " + totalPlacemarks + " locations", 
                    Toast.LENGTH_SHORT).show();

        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error parsing KML", e);
            Toast.makeText(context, "Error parsing KML: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Count placemarks in KML data
     * 
     * @param parser The XML parser
     * @throws XmlPullParserException If parsing fails
     * @throws IOException If reading fails
     */
    private void countPlacemarks(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        totalPlacemarks = 0;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && "Placemark".equals(parser.getName())) {
                totalPlacemarks++;
            }
            eventType = parser.next();
        }

        processedPlacemarks.set(0);
        Log.d(TAG, "Found " + totalPlacemarks + " placemarks in KML");
    }

    /**
     * Process a placemark from KML data
     * 
     * @param name The placemark name
     * @param description The placemark description
     * @param coordinates The placemark coordinates
     * @param styleUrl The placemark style URL
     */
    private void processPlacemark(String name, String description, String coordinates, String styleUrl) {
        Log.d(TAG, "Processing placemark: " + name);

        // Extract order ID from name
        String orderId = extractOrderId(name);
        if (orderId == null || orderId.isEmpty()) {
            Log.w(TAG, "Skipping placemark without order ID: " + name);
            processedPlacemarks.incrementAndGet();
            return;
        }

        // Extract tip amount if present in the name
        double tipAmount = extractTipAmount(name);

        // Use address from name as that's where it is in Google Maps pins
        String addressText = name;
        if (orderId.equals(name)) {
            // If name is just the order ID, try to use coordinates or description
            addressText = "Location at " + 
                    (coordinates != null ? formatCoordinates(coordinates) : "Unknown");
        }

        // Extract coordinates for location
        Coordinates location = null;
        if (coordinates != null) {
            String[] parts = coordinates.split(",");
            if (parts.length >= 2) {
                try {
                    double longitude = Double.parseDouble(parts[0]);
                    double latitude = Double.parseDouble(parts[1]);
                    location = new Coordinates(latitude, longitude);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid coordinates format: " + coordinates);
                }
            }
        }

        // Create address
        Address address = new Address();
        address.setFullAddress(addressText);
        address.setNormalizedAddress(addressRepository.normalizeAddress(addressText));
        
        if (location != null) {
            address.setLocation(location);
        }
        
        // Create delivery
        Delivery delivery = new Delivery();
        
        // Set address
        delivery.setAddress(address);
        
        // Set reference (will be updated with address ID after saving)
        Reference reference = new Reference();
        reference.setAddressText(addressText);
        delivery.setReference(reference);
        
        // Set metadata
        Metadata metadata = new Metadata();
        metadata.setOrderId(orderId);
        metadata.setCreatedAt(new Date());
        metadata.setSource("kml_import");
        delivery.setMetadata(metadata);
        
        // Set times
        Times times = new Times();
        times.setCreatedAt(new Date());
        times.setOrderedAt(new Date());
        delivery.setTimes(times);
        
        // Set status
        Status status = new Status();
        status.setCompleted(true);
        status.setTipped(tipAmount > 0);
        delivery.setStatus(status);
        
        // Set amounts if tip found
        if (tipAmount > 0) {
            Amounts amounts = new Amounts();
            amounts.setTipAmount(tipAmount);
            delivery.setAmounts(amounts);
            
            // Set tipped time
            times.setTippedAt(new Date());
        }
        
        // Set notes with description if available
        if (description != null && !description.isEmpty()) {
            delivery.setNotes(description);
        }
        
        // Save delivery to repository
        disposables.add(
            deliveryRepository.addDelivery(delivery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    documentReference -> {
                        Log.d(TAG, "Added delivery: " + orderId);
                        
                        // Update counter and check if import is complete
                        int processed = processedPlacemarks.incrementAndGet();
                        if (processed == totalPlacemarks) {
                            // All placemarks processed
                            Toast.makeText(context, "Import complete: " + processed + 
                                    " locations imported", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error adding delivery", error);
                        processedPlacemarks.incrementAndGet();
                    }
                )
        );
    }

    /**
     * Format coordinates for display
     * 
     * @param coordinates The raw coordinates string
     * @return Formatted coordinates string
     */
    private String formatCoordinates(String coordinates) {
        // Turn "-93.760542,41.684084,0" into a more readable format
        String[] parts = coordinates.split(",");
        if (parts.length >= 2) {
            try {
                double longitude = Double.parseDouble(parts[0]);
                double latitude = Double.parseDouble(parts[1]);
                return String.format("%.6f, %.6f", latitude, longitude);
            } catch (NumberFormatException e) {
                return coordinates;
            }
        }
        return coordinates;
    }

    /**
     * Extract order ID from placemark name
     * 
     * @param placemark The placemark name
     * @return Order ID if found, null otherwise
     */
    private String extractOrderId(String placemark) {
        if (placemark == null) return null;

        // Extract numeric ID from the beginning of the name
        Matcher matcher = ORDER_ID_PATTERN.matcher(placemark);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Extract tip amount from placemark name
     * 
     * @param name The placemark name
     * @return Tip amount if found, 0.0 otherwise
     */
    private double extractTipAmount(String name) {
        if (name == null) return 0.0;

        // Try to extract tip amount using pattern ($XX.XX)
        Matcher matcher = TIP_PATTERN.matcher(name);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        return 0.0;
    }
    
    /**
     * Clean up resources when no longer needed
     */
    public void dispose() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }
}