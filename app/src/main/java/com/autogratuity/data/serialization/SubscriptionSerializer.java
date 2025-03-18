package com.autogratuity.data.serialization;

import android.content.Context;

import com.autogratuity.data.local.JsonSerializer;
import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.security.EncryptionUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for serializing/deserializing SubscriptionStatus objects to/from Firestore maps.
 * This class includes encryption for sensitive subscription data.
 */
public class SubscriptionSerializer {
    
    private static EncryptionUtils encryptionUtils;
    
    /**
     * Initialize the serializer with encryption support
     * 
     * @param context Android context for encryption
     */
    public static void init(Context context) {
        encryptionUtils = EncryptionUtils.getInstance(context);
    }
    
    /**
     * Convert a SubscriptionStatus object to a Map for Firestore storage
     * 
     * @param status The subscription status to convert
     * @param context Optional context for encryption (can be null if initialized)
     * @return Map representation of the subscription status
     */
    public static Map<String, Object> toMap(SubscriptionStatus status, Context context) {
        if (status == null) {
            return null;
        }
        
        // Ensure encryption is available
        if (encryptionUtils == null && context != null) {
            encryptionUtils = EncryptionUtils.getInstance(context);
        }
        
        Map<String, Object> map = new HashMap<>();
        
        // Basic fields
        map.put("userId", status.getUserId());
        map.put("status", status.getStatus());
        map.put("level", status.getLevel());
        map.put("isActive", status.isActive());
        map.put("isLifetime", status.isLifetime());
        
        // Time-related fields
        map.put("expiryDate", status.getExpiryDate() != null ? new Timestamp(status.getExpiryDate()) : null);
        map.put("startDate", status.getStartDate() != null ? new Timestamp(status.getStartDate()) : null);
        
        // Sensitive fields - encrypt if possible
        if (encryptionUtils != null) {
            map.put("provider", encryptionUtils.encrypt(status.getProvider()));
            map.put("orderId", encryptionUtils.encrypt(status.getOrderId()));
        } else {
            map.put("provider", status.getProvider());
            map.put("orderId", status.getOrderId());
        }
        
        // Verification data
        Map<String, Object> verification = new HashMap<>();
        verification.put("lastVerified", status.getLastVerified() != null ? 
                new Timestamp(status.getLastVerified()) : null);
        verification.put("status", status.getVerificationStatus());
        verification.put("error", status.getVerificationError());
        map.put("verification", verification);
        
        return map;
    }
    
    /**
     * Convert a DocumentSnapshot to a SubscriptionStatus object
     * 
     * @param snapshot Firestore document snapshot
     * @param context Optional context for decryption (can be null if initialized)
     * @return SubscriptionStatus object
     */
    public static SubscriptionStatus fromDocumentSnapshot(DocumentSnapshot snapshot, Context context) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        
        // Ensure encryption is available
        if (encryptionUtils == null && context != null) {
            encryptionUtils = EncryptionUtils.getInstance(context);
        }
        
        String userId = snapshot.getString("userId");
        SubscriptionStatus status = new SubscriptionStatus(userId);
        
        // Set basic fields
        status.setStatus(snapshot.getString("status"));
        status.setLevel(snapshot.getString("level"));
        
        Boolean isActive = snapshot.getBoolean("isActive");
        if (isActive != null) {
            status.setActive(isActive);
        }
        
        Boolean isLifetime = snapshot.getBoolean("isLifetime");
        if (isLifetime != null) {
            status.setLifetime(isLifetime);
        }
        
        // Set time-related fields
        Timestamp expiryDate = snapshot.getTimestamp("expiryDate");
        if (expiryDate != null) {
            status.setExpiryDate(expiryDate.toDate());
        }
        
        Timestamp startDate = snapshot.getTimestamp("startDate");
        if (startDate != null) {
            status.setStartDate(startDate.toDate());
        }
        
        // Set sensitive fields - decrypt if possible
        String provider = snapshot.getString("provider");
        if (provider != null && encryptionUtils != null && encryptionUtils.isEncrypted(provider)) {
            status.setProvider(encryptionUtils.decrypt(provider));
        } else {
            status.setProvider(provider);
        }
        
        String orderId = snapshot.getString("orderId");
        if (orderId != null && encryptionUtils != null && encryptionUtils.isEncrypted(orderId)) {
            status.setOrderId(encryptionUtils.decrypt(orderId));
        } else {
            status.setOrderId(orderId);
        }
        
        // Set verification data
        Map<String, Object> verification = (Map<String, Object>) snapshot.get("verification");
        if (verification != null) {
            Timestamp lastVerified = (Timestamp) verification.get("lastVerified");
            if (lastVerified != null) {
                status.setLastVerified(lastVerified.toDate());
            }
            
            status.setVerificationStatus((String) verification.get("status"));
            status.setVerificationError((String) verification.get("error"));
        }
        
        return status;
    }
    
    /**
     * Convert a Map to a SubscriptionStatus object
     * 
     * @param map Map representation of a subscription status
     * @param context Optional context for decryption (can be null if initialized)
     * @return SubscriptionStatus object
     */
    public static SubscriptionStatus fromMap(Map<String, Object> map, Context context) {
        if (map == null) {
            return null;
        }
        
        // Create a JSON representation
        String json = JsonSerializer.toJson(map);
        
        // Use GSON to deserialize
        SubscriptionStatus status = JsonSerializer.fromJson(json, SubscriptionStatus.class);
        
        // Decrypt sensitive fields if needed
        if (status != null && encryptionUtils != null) {
            String provider = status.getProvider();
            if (provider != null && encryptionUtils.isEncrypted(provider)) {
                status.setProvider(encryptionUtils.decrypt(provider));
            }
            
            String orderId = status.getOrderId();
            if (orderId != null && encryptionUtils.isEncrypted(orderId)) {
                status.setOrderId(encryptionUtils.decrypt(orderId));
            }
        }
        
        return status;
    }
    
    /**
     * Helper method to convert Date to Timestamp
     */
    private static Timestamp dateToTimestamp(Date date) {
        return date != null ? new Timestamp(date) : null;
    }
}
