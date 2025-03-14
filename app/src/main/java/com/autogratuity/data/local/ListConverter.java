package com.autogratuity.data.local;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Type converter for Room database to handle List<String> objects
 */
public class ListConverter {
    
    private static final String SEPARATOR = ",";
    
    /**
     * Convert List<String> to a single String for storage
     */
    @TypeConverter
    public static String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        
        StringBuilder builder = new StringBuilder();
        for (String item : list) {
            if (builder.length() > 0) {
                builder.append(SEPARATOR);
            }
            // Escape commas in the items
            builder.append(item.replace(SEPARATOR, "\\,"));
        }
        return builder.toString();
    }
    
    /**
     * Convert a single String to List<String> for retrieval
     */
    @TypeConverter
    public static List<String> stringToList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> result = new ArrayList<>();
        StringBuilder currentItem = new StringBuilder();
        boolean escaped = false;
        
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            
            if (escaped) {
                currentItem.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == ',') {
                result.add(currentItem.toString());
                currentItem = new StringBuilder();
            } else {
                currentItem.append(c);
            }
        }
        
        // Add the last item
        if (currentItem.length() > 0) {
            result.add(currentItem.toString());
        }
        
        return result;
    }
}
