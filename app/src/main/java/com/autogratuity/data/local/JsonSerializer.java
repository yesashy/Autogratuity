package com.autogratuity.data.local;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
// Removed import with alias - will use fully qualified name instead

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Utility class for serializing and deserializing objects to/from JSON.
 * Handles Firebase Timestamp conversion and other special types.
 */
public class JsonSerializer {
    private static final String TAG = "JsonSerializer";
    
    private static Gson gson;
    
    /**
     * Get or create the Gson instance with custom type adapters.
     * 
     * @return Configured Gson instance
     */
    private static synchronized Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapter(Timestamp.class, new TimestampAdapter())
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                    .create();
        }
        return gson;
    }
    
    /**
     * Serialize an object to JSON.
     * 
     * @param object Object to serialize
     * @return JSON string, or null if serialization fails
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return getGson().toJson(object);
        } catch (Exception e) {
            Log.e(TAG, "Error serializing object to JSON", e);
            return null;
        }
    }
    
    /**
     * Deserialize a JSON string to an object.
     * 
     * @param json JSON string
     * @param classOfT Class of the object to deserialize
     * @param <T> Type of the object
     * @return Deserialized object, or null if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        try {
            return getGson().fromJson(json, classOfT);
        } catch (Exception e) {
            Log.e(TAG, "Error deserializing JSON to object of type " + classOfT.getName(), e);
            return null;
        }
    }
    
    /**
     * Custom adapter for Firebase Timestamp to handle serialization and deserialization.
     */
    private static class TimestampAdapter implements com.google.gson.JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
        @Override
        public JsonElement serialize(Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("seconds", new JsonPrimitive(src.getSeconds()));
            obj.add("nanoseconds", new JsonPrimitive(src.getNanoseconds()));
            return obj;
        }
        
        @Override
        public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            long seconds = obj.get("seconds").getAsLong();
            int nanoseconds = obj.get("nanoseconds").getAsInt();
            return new Timestamp(seconds, nanoseconds);
        }
    }
    
    /**
     * Save a date in a format compatible with Firestore Timestamp.
     * 
     * @param date Date to convert
     * @return JSON representation of the date
     */
    public static String dateToJson(Date date) {
        if (date == null) {
            return null;
        }
        
        Timestamp timestamp = new Timestamp(date);
        return getGson().toJson(timestamp);
    }
    
    /**
     * Parse a date from a JSON string in Firestore Timestamp format.
     * 
     * @param json JSON string
     * @return Parsed date, or null if parsing fails
     */
    public static Date dateFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        try {
            Timestamp timestamp = getGson().fromJson(json, Timestamp.class);
            return timestamp.toDate();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date from JSON", e);
            return null;
        }
    }
}
