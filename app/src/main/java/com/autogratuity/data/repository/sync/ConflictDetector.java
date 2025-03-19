package com.autogratuity.data.repository.sync;

import com.autogratuity.data.model.SyncOperation;
import java.util.Map;

/**
 * Interface for detecting conflicts during data synchronization.
 * <p>
 * This interface defines the contract for components that detect conflicts
 * between local changes and remote data during synchronization operations.
 * It provides methods to analyze data versions and determine if there are
 * conflicts that need resolution before completing a synchronization.
 * <p>
 * Different implementations can use various strategies for conflict detection,
 * such as timestamps, version numbers, or field-by-field comparisons.
 */
public interface ConflictDetector {

    /**
     * Enum representing different types of conflicts
     */
    enum ConflictType {
        /**
         * No conflict detected
         */
        NONE,
        
        /**
         * Conflict detected based on timestamp comparison
         */
        TIMESTAMP_CONFLICT,
        
        /**
         * Conflict detected based on version number comparison
         */
        VERSION_CONFLICT,
        
        /**
         * Conflict detected based on specific field values
         */
        FIELD_VALUE_CONFLICT,
        
        /**
         * Conflict detected but type is unknown or multiple types
         */
        UNKNOWN_CONFLICT
    }

    /**
     * Class representing the result of a conflict detection operation
     */
    class ConflictResult {
        private final boolean isConflict;
        private final ConflictType conflictType;
        private final String message;
        private final Map<String, Object> conflictDetails;
        
        public ConflictResult(boolean isConflict, ConflictType conflictType, String message, Map<String, Object> conflictDetails) {
            this.isConflict = isConflict;
            this.conflictType = conflictType;
            this.message = message;
            this.conflictDetails = conflictDetails;
        }
        
        public boolean isConflict() {
            return isConflict;
        }
        
        public ConflictType getConflictType() {
            return conflictType;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Map<String, Object> getConflictDetails() {
            return conflictDetails;
        }
    }

    /**
     * Detect conflict between local operation and server data
     *
     * @param operation The sync operation with local changes
     * @param serverData Current server data for the entity
     * @return ConflictResult with conflict detection details
     */
    ConflictResult detectConflict(SyncOperation operation, Map<String, Object> serverData);
    
    /**
     * Get the recommended conflict resolution strategy for the given conflict type
     *
     * @param conflictType Type of conflict detected
     * @param entityType Type of entity (e.g., "delivery", "address", etc.)
     * @return Recommended conflict resolution strategy (e.g., SyncOperation.CONFLICT_RESOLUTION_SERVER_WINS)
     */
    String getRecommendedResolutionStrategy(ConflictType conflictType, String entityType);
    
    /**
     * Check if the operation needs conflict detection
     *
     * @param operation The sync operation to check
     * @return true if conflict detection should be performed, false otherwise
     */
    boolean needsConflictDetection(SyncOperation operation);
    
    /**
     * Extract the conflicting fields between local changes and server data
     *
     * @param operation The sync operation with local changes
     * @param serverData Current server data for the entity
     * @return Map of field names to a pair of [localValue, serverValue] that conflict
     */
    Map<String, Object[]> getConflictingFields(SyncOperation operation, Map<String, Object> serverData);
}
