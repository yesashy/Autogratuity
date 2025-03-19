package com.autogratuity.data.repository.sync;

import android.util.Log;

import com.autogratuity.data.model.SyncOperation;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Timestamp-based implementation of the ConflictDetector interface.
 * <p>
 * This implementation detects conflicts by comparing timestamps between
 * the local operation and the server data. It uses a timestamp-based approach
 * to determine if a conflict exists and provides detailed information about
 * the conflicting data.
 * <p>
 * The detector considers several factors such as:
 * - The creation and update timestamps of the entities
 * - The time when the operation was created
 * - Specific field-by-field comparison for critical fields
 */
public class TimestampConflictDetector implements ConflictDetector {
    private static final String TAG = "TimestampConflictDetector";
    
    // Fields to examine for timestamps
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_LAST_MODIFIED = "lastModified";
    private static final String FIELD_VERSION = "version";
    
    // Default tolerance in milliseconds (5 seconds)
    private static final long DEFAULT_TOLERANCE_MS = 5000;
    
    // Entity-specific resolution strategies
    private static final Map<String, String> ENTITY_RESOLUTION_STRATEGIES = new HashMap<>();
    
    static {
        // Define default resolution strategies by entity type
        ENTITY_RESOLUTION_STRATEGIES.put("address", SyncOperation.CONFLICT_RESOLUTION_CLIENT_WINS);
        ENTITY_RESOLUTION_STRATEGIES.put("delivery", SyncOperation.CONFLICT_RESOLUTION_CLIENT_WINS);
        ENTITY_RESOLUTION_STRATEGIES.put("userProfile", SyncOperation.CONFLICT_RESOLUTION_SERVER_WINS);
        // Default to client wins for unspecified entity types
        ENTITY_RESOLUTION_STRATEGIES.put("default", SyncOperation.CONFLICT_RESOLUTION_CLIENT_WINS);
    }
    
    // The tolerance in milliseconds for timestamp comparison
    private final long toleranceMs;
    
    // A list of critical fields that will always be checked for conflicts
    private final List<String> criticalFields;
    
    /**
     * Default constructor with standard tolerance
     */
    public TimestampConflictDetector() {
        this(DEFAULT_TOLERANCE_MS, new ArrayList<>());
    }
    
    /**
     * Constructor with custom tolerance
     *
     * @param toleranceMs Tolerance in milliseconds
     */
    public TimestampConflictDetector(long toleranceMs) {
        this(toleranceMs, new ArrayList<>());
    }
    
    /**
     * Constructor with custom tolerance and critical fields
     *
     * @param toleranceMs Tolerance in milliseconds
     * @param criticalFields List of field names to always check for conflicts
     */
    public TimestampConflictDetector(long toleranceMs, List<String> criticalFields) {
        this.toleranceMs = toleranceMs;
        this.criticalFields = criticalFields != null ? criticalFields : new ArrayList<>();
    }
    
    @Override
    public ConflictResult detectConflict(SyncOperation operation, Map<String, Object> serverData) {
        if (operation == null || serverData == null) {
            return new ConflictResult(false, ConflictType.NONE, "No data to compare", null);
        }
        
        // Skip conflict detection if the operation doesn't need it
        if (!needsConflictDetection(operation)) {
            return new ConflictResult(false, ConflictType.NONE, "Conflict detection not required", null);
        }
        
        // Compare timestamps
        Date operationTimestamp = operation.getCreatedAt();
        Date serverTimestamp = extractTimestamp(serverData);
        
        // If we can't extract timestamps, check if there's a version field
        if (operationTimestamp == null || serverTimestamp == null) {
            if (serverData.containsKey(FIELD_VERSION)) {
                return detectVersionConflict(operation, serverData);
            } else {
                // Fall back to field-by-field comparison for critical fields
                return detectFieldValueConflict(operation, serverData);
            }
        }
        
        // Compare timestamps with tolerance
        long timeDiff = operationTimestamp.getTime() - serverTimestamp.getTime();
        boolean isConflict = Math.abs(timeDiff) <= toleranceMs;
        
        if (isConflict) {
            // Create conflict details
            Map<String, Object> conflictDetails = new HashMap<>();
            conflictDetails.put("operationTimestamp", operationTimestamp);
            conflictDetails.put("serverTimestamp", serverTimestamp);
            conflictDetails.put("timeDifferenceMs", timeDiff);
            conflictDetails.put("toleranceMs", toleranceMs);
            conflictDetails.put("entityType", operation.getEntityType());
            conflictDetails.put("entityId", operation.getEntityId());
            
            // Add conflicting fields
            Map<String, Object[]> conflictingFields = getConflictingFields(operation, serverData);
            conflictDetails.put("conflictingFields", conflictingFields);
            
            String message = String.format(
                    "Timestamp conflict detected: operation=%s, server=%s, diff=%d ms",
                    operationTimestamp, serverTimestamp, timeDiff
            );
            
            return new ConflictResult(true, ConflictType.TIMESTAMP_CONFLICT, message, conflictDetails);
        }
        
        return new ConflictResult(false, ConflictType.NONE, "No conflict detected", null);
    }
    
    @Override
    public String getRecommendedResolutionStrategy(ConflictType conflictType, String entityType) {
        // Get entity-specific strategy or use default
        String strategy = ENTITY_RESOLUTION_STRATEGIES.getOrDefault(
                entityType, 
                ENTITY_RESOLUTION_STRATEGIES.get("default")
        );
        
        // Override based on conflict type if necessary
        if (conflictType == ConflictType.VERSION_CONFLICT) {
            // For version conflicts, server data is usually more reliable
            return SyncOperation.CONFLICT_RESOLUTION_SERVER_WINS;
        }
        
        return strategy;
    }
    
    @Override
    public boolean needsConflictDetection(SyncOperation operation) {
        if (operation == null) {
            return false;
        }
        
        // Skip conflict detection for create operations (they can't conflict)
        if ("create".equals(operation.getType())) {
            return false;
        }
        
        // Skip conflict detection for delete operations (handled differently)
        if ("delete".equals(operation.getType())) {
            return false;
        }
        
        // All update operations need conflict detection
        return "update".equals(operation.getType());
    }
    
    @Override
    public Map<String, Object[]> getConflictingFields(SyncOperation operation, Map<String, Object> serverData) {
        Map<String, Object[]> conflictingFields = new HashMap<>();
        
        if (operation == null || serverData == null) {
            return conflictingFields;
        }
        
        Map<String, Object> operationData = operation.getData();
        
        // Skip timestamp fields
        List<String> skipFields = new ArrayList<>();
        skipFields.add(FIELD_CREATED_AT);
        skipFields.add(FIELD_UPDATED_AT);
        skipFields.add(FIELD_TIMESTAMP);
        skipFields.add(FIELD_LAST_MODIFIED);
        skipFields.add(FIELD_VERSION);
        
        // Check all fields in operation data
        for (Map.Entry<String, Object> entry : operationData.entrySet()) {
            String field = entry.getKey();
            
            // Skip timestamp fields
            if (skipFields.contains(field)) {
                continue;
            }
            
            Object operationValue = entry.getValue();
            Object serverValue = serverData.get(field);
            
            // Check for conflicts
            if (serverValue != null && !Objects.equals(operationValue, serverValue)) {
                conflictingFields.put(field, new Object[]{operationValue, serverValue});
            }
        }
        
        // Also check critical fields explicitly
        for (String criticalField : criticalFields) {
            if (!conflictingFields.containsKey(criticalField) && 
                    serverData.containsKey(criticalField) && 
                    operationData.containsKey(criticalField)) {
                
                Object operationValue = operationData.get(criticalField);
                Object serverValue = serverData.get(criticalField);
                
                if (!Objects.equals(operationValue, serverValue)) {
                    conflictingFields.put(criticalField, new Object[]{operationValue, serverValue});
                }
            }
        }
        
        return conflictingFields;
    }
    
    /**
     * Extract a timestamp from the server data
     *
     * @param serverData The server data map
     * @return Extracted timestamp or null if not found
     */
    private Date extractTimestamp(Map<String, Object> serverData) {
        // Try standard timestamp fields
        Object timestampObj = serverData.get(FIELD_UPDATED_AT);
        if (timestampObj == null) {
            timestampObj = serverData.get(FIELD_CREATED_AT);
        }
        if (timestampObj == null) {
            timestampObj = serverData.get(FIELD_TIMESTAMP);
        }
        if (timestampObj == null) {
            timestampObj = serverData.get(FIELD_LAST_MODIFIED);
        }
        
        // Convert to Date
        if (timestampObj instanceof Timestamp) {
            return ((Timestamp) timestampObj).toDate();
        } else if (timestampObj instanceof Date) {
            return (Date) timestampObj;
        } else if (timestampObj instanceof Long) {
            return new Date((Long) timestampObj);
        }
        
        return null;
    }
    
    /**
     * Detect conflict based on version numbers
     *
     * @param operation The sync operation
     * @param serverData The server data
     * @return ConflictResult with version conflict details
     */
    private ConflictResult detectVersionConflict(SyncOperation operation, Map<String, Object> serverData) {
        Map<String, Object> operationData = operation.getData();
        
        // Check if operation has version info
        if (!operationData.containsKey(FIELD_VERSION)) {
            return new ConflictResult(false, ConflictType.NONE, "No version information available", null);
        }
        
        // Get version values
        Object operationVersion = operationData.get(FIELD_VERSION);
        Object serverVersion = serverData.get(FIELD_VERSION);
        
        // Compare versions if both are available
        if (operationVersion != null && serverVersion != null) {
            boolean isConflict = false;
            String message = "No version conflict detected";
            
            // Compare based on version type
            if (operationVersion instanceof Integer && serverVersion instanceof Integer) {
                isConflict = (Integer) operationVersion < (Integer) serverVersion;
                message = isConflict ? "Server version is newer than operation version" : message;
            } else if (operationVersion instanceof Long && serverVersion instanceof Long) {
                isConflict = (Long) operationVersion < (Long) serverVersion;
                message = isConflict ? "Server version is newer than operation version" : message;
            } else if (operationVersion instanceof String && serverVersion instanceof String) {
                // Try to compare as version strings (assuming format like "1.0.0")
                try {
                    String[] opParts = ((String) operationVersion).split("\\.");
                    String[] serverParts = ((String) serverVersion).split("\\.");
                    
                    for (int i = 0; i < Math.min(opParts.length, serverParts.length); i++) {
                        int opVal = Integer.parseInt(opParts[i]);
                        int serverVal = Integer.parseInt(serverParts[i]);
                        
                        if (opVal < serverVal) {
                            isConflict = true;
                            message = "Server version is newer than operation version";
                            break;
                        } else if (opVal > serverVal) {
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    // If version strings can't be parsed, compare lexicographically
                    isConflict = ((String) operationVersion).compareTo((String) serverVersion) < 0;
                    message = isConflict ? "Server version lexicographically greater than operation version" : message;
                }
            } else {
                // Different types - just check if they're different
                isConflict = !operationVersion.equals(serverVersion);
                message = isConflict ? "Version values are different types" : message;
            }
            
            if (isConflict) {
                // Create conflict details
                Map<String, Object> conflictDetails = new HashMap<>();
                conflictDetails.put("operationVersion", operationVersion);
                conflictDetails.put("serverVersion", serverVersion);
                conflictDetails.put("entityType", operation.getEntityType());
                conflictDetails.put("entityId", operation.getEntityId());
                
                // Add conflicting fields
                Map<String, Object[]> conflictingFields = getConflictingFields(operation, serverData);
                conflictDetails.put("conflictingFields", conflictingFields);
                
                return new ConflictResult(true, ConflictType.VERSION_CONFLICT, message, conflictDetails);
            }
        }
        
        return new ConflictResult(false, ConflictType.NONE, "No version conflict detected", null);
    }
    
    /**
     * Detect conflict based on field values
     *
     * @param operation The sync operation
     * @param serverData The server data
     * @return ConflictResult with field value conflict details
     */
    private ConflictResult detectFieldValueConflict(SyncOperation operation, Map<String, Object> serverData) {
        // Get conflicting fields
        Map<String, Object[]> conflictingFields = getConflictingFields(operation, serverData);
        
        // Check if there are any conflicts
        if (conflictingFields.isEmpty()) {
            return new ConflictResult(false, ConflictType.NONE, "No field value conflicts detected", null);
        }
        
        // Create conflict details
        Map<String, Object> conflictDetails = new HashMap<>();
        conflictDetails.put("entityType", operation.getEntityType());
        conflictDetails.put("entityId", operation.getEntityId());
        conflictDetails.put("conflictingFields", conflictingFields);
        
        String message = String.format(
                "Field value conflict detected: %d conflicting fields for entity %s/%s",
                conflictingFields.size(),
                operation.getEntityType(),
                operation.getEntityId()
        );
        
        return new ConflictResult(true, ConflictType.FIELD_VALUE_CONFLICT, message, conflictDetails);
    }
}
