package com.autogratuity.data.local;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Type converters for Room database to handle complex types
 */
public class Converters {
    
    /**
     * Convert Date to Long timestamp for storage
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    
    /**
     * Convert Long timestamp to Date for retrieval
     */
    @TypeConverter
    public static Date timestampToDate(Long value) {
        return value == null ? null : new Date(value);
    }
}
