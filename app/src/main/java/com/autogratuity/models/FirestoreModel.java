package com.autogratuity.models;

import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Map;

public class FirestoreModel {

    protected static String getString(DocumentSnapshot doc, String field) {
        return getString(doc, field, "");
    }

    protected static String getString(DocumentSnapshot doc, String field, String defaultValue) {
        if (field.contains(".")) {
            String[] parts = field.split("\\.", 2);
            Object nestedObj = doc.get(parts[0]);
            if (nestedObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) nestedObj;
                Object value = nestedMap.get(parts[1]);
                return value instanceof String ? (String) value : defaultValue;
            }
            return defaultValue;
        }
        return doc.getString(field) != null ? doc.getString(field) : defaultValue;
    }

    protected static double getDouble(DocumentSnapshot doc, String field) {
        return getDouble(doc, field, 0.0);
    }

    protected static double getDouble(DocumentSnapshot doc, String field, double defaultValue) {
        if (field.contains(".")) {
            String[] parts = field.split("\\.", 2);
            Object nestedObj = doc.get(parts[0]);
            if (nestedObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) nestedObj;
                Object value = nestedMap.get(parts[1]);
                if (value instanceof Double) return (Double) value;
                if (value instanceof Long) return ((Long) value).doubleValue();
                if (value instanceof Integer) return ((Integer) value).doubleValue();
                return defaultValue;
            }
            return defaultValue;
        }

        if (doc.getDouble(field) != null) return doc.getDouble(field);
        if (doc.getLong(field) != null) return doc.getLong(field).doubleValue();
        return defaultValue;
    }

    protected static boolean getBoolean(DocumentSnapshot doc, String field) {
        return getBoolean(doc, field, false);
    }

    protected static boolean getBoolean(DocumentSnapshot doc, String field, boolean defaultValue) {
        if (field.contains(".")) {
            String[] parts = field.split("\\.", 2);
            Object nestedObj = doc.get(parts[0]);
            if (nestedObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) nestedObj;
                Object value = nestedMap.get(parts[1]);
                return value instanceof Boolean ? (Boolean) value : defaultValue;
            }
            return defaultValue;
        }
        return doc.getBoolean(field) != null ? doc.getBoolean(field) : defaultValue;
    }

    protected static int getInt(DocumentSnapshot doc, String field) {
        return getInt(doc, field, 0);
    }

    protected static int getInt(DocumentSnapshot doc, String field, int defaultValue) {
        if (field.contains(".")) {
            String[] parts = field.split("\\.", 2);
            Object nestedObj = doc.get(parts[0]);
            if (nestedObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) nestedObj;
                Object value = nestedMap.get(parts[1]);
                if (value instanceof Integer) return (Integer) value;
                if (value instanceof Long) return ((Long) value).intValue();
                return defaultValue;
            }
            return defaultValue;
        }

        if (doc.getLong(field) != null) return doc.getLong(field).intValue();
        return defaultValue;
    }
}