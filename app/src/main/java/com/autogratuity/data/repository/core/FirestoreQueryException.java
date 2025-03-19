package com.autogratuity.data.repository.core;

/**
 * Exception class for Firestore query errors.
 * <p>
 * This class provides standardized error reporting for query building and execution errors
 * with specific error types and detailed error messages to aid debugging.
 */
public class FirestoreQueryException extends RuntimeException {

    /**
     * Enum defining the different types of query errors
     */
    public enum QueryErrorType {
        /**
         * Invalid field name used in query
         */
        INVALID_FIELD,
        
        /**
         * Invalid comparison operator used
         */
        INVALID_OPERATOR,
        
        /**
         * Invalid value type for the specified field
         */
        INVALID_VALUE_TYPE,
        
        /**
         * Query contains more clauses than Firestore supports
         */
        TOO_MANY_CLAUSES,
        
        /**
         * Query limit exceeds maximum allowed value
         */
        LIMIT_EXCEEDED,
        
        /**
         * Multiple order by clauses for the same field
         */
        DUPLICATE_ORDER_BY,
        
        /**
         * Invalid order direction
         */
        INVALID_ORDER_DIRECTION,
        
        /**
         * General query validation error
         */
        VALIDATION_ERROR,
        
        /**
         * Query contains unsupported operation
         */
        UNSUPPORTED_OPERATION,
        
        /**
         * Query execution error
         */
        EXECUTION_ERROR
    }
    
    // The specific type of query error
    private final QueryErrorType errorType;
    
    // The field that caused the error (if applicable)
    private final String fieldName;
    
    /**
     * Constructor with error message
     *
     * @param message Detailed error message
     */
    public FirestoreQueryException(String message) {
        this(QueryErrorType.VALIDATION_ERROR, message, null);
    }
    
    /**
     * Constructor with error type and message
     *
     * @param errorType Specific type of query error
     * @param message Detailed error message
     */
    public FirestoreQueryException(QueryErrorType errorType, String message) {
        this(errorType, message, null);
    }
    
    /**
     * Constructor with error type, message, and field name
     *
     * @param errorType Specific type of query error
     * @param message Detailed error message
     * @param fieldName Name of the field that caused the error (if applicable)
     */
    public FirestoreQueryException(QueryErrorType errorType, String message, String fieldName) {
        super(formatMessage(errorType, message, fieldName));
        this.errorType = errorType;
        this.fieldName = fieldName;
    }
    
    /**
     * Constructor with error type, message, and cause
     *
     * @param errorType Specific type of query error
     * @param message Detailed error message
     * @param cause The original exception that caused this error
     */
    public FirestoreQueryException(QueryErrorType errorType, String message, Throwable cause) {
        super(formatMessage(errorType, message, null), cause);
        this.errorType = errorType;
        this.fieldName = null;
    }
    
    /**
     * Get the specific type of query error
     *
     * @return QueryErrorType indicating the category of error
     */
    public QueryErrorType getErrorType() {
        return errorType;
    }
    
    /**
     * Get the name of the field that caused the error (if applicable)
     *
     * @return String field name or null if not applicable
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Format a detailed error message with type and field information
     *
     * @param errorType The type of query error
     * @param message Base error message
     * @param fieldName Name of the field that caused the error (if applicable)
     * @return Formatted error message
     */
    private static String formatMessage(QueryErrorType errorType, String message, String fieldName) {
        StringBuilder formattedMessage = new StringBuilder();
        formattedMessage.append("Firestore query error [").append(errorType).append("]: ");
        formattedMessage.append(message);
        
        if (fieldName != null && !fieldName.isEmpty()) {
            formattedMessage.append(" (Field: ").append(fieldName).append(")");
        }
        
        return formattedMessage.toString();
    }
}
