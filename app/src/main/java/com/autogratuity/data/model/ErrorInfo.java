package com.autogratuity.data.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard model class for representing error information throughout the application.
 * This class provides a unified approach to error representation, with support for
 * error severity levels, recovery actions, and standardized error codes.
 */
public class ErrorInfo {
    // Error severity levels
    public static final String SEVERITY_LOW = "low";
    public static final String SEVERITY_MEDIUM = "medium";
    public static final String SEVERITY_HIGH = "high";
    public static final String SEVERITY_CRITICAL = "critical";
    
    // Standard error codes
    public static final String CODE_NETWORK = "network_error";
    public static final String CODE_AUTHENTICATION = "auth_error";
    public static final String CODE_PERMISSION = "permission_error";
    public static final String CODE_VALIDATION = "validation_error";
    public static final String CODE_SERVER = "server_error";
    public static final String CODE_CONFLICT = "conflict_error";
    public static final String CODE_NOT_FOUND = "not_found_error";
    public static final String CODE_TIMEOUT = "timeout_error";
    public static final String CODE_CANCELLED = "cancelled_error";
    public static final String CODE_UNKNOWN = "unknown_error";
    
    // Standard recovery actions
    public static final String RECOVERY_RETRY = "retry";
    public static final String RECOVERY_CONTACT_SUPPORT = "contact_support";
    public static final String RECOVERY_CHECK_CONNECTION = "check_connection";
    public static final String RECOVERY_LOGIN_AGAIN = "login_again";
    public static final String RECOVERY_UPDATE_APP = "update_app";
    public static final String RECOVERY_WAIT = "wait";
    public static final String RECOVERY_NONE = "none";
    
    private String code;
    private String message;
    private String timestamp;
    private String severity;
    private String recoveryAction;
    private Map<String, Object> details;
    
    /**
     * Default constructor for Firestore
     */
    public ErrorInfo() {
        this.details = new HashMap<>();
        this.severity = SEVERITY_MEDIUM;
        this.recoveryAction = RECOVERY_RETRY;
    }
    
    /**
     * Constructor with basic error details
     * 
     * @param code Error code
     * @param message Error message
     * @param timestamp Error timestamp
     */
    public ErrorInfo(String code, String message, Date timestamp) {
        this();
        this.code = code;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp.toString() : null;
    }
    
    /**
     * Constructor with full error details
     * 
     * @param code Error code
     * @param message Error message
     * @param timestamp Error timestamp
     * @param severity Error severity
     * @param recoveryAction Recommended recovery action
     */
    public ErrorInfo(String code, String message, Date timestamp, String severity, String recoveryAction) {
        this(code, message, timestamp);
        this.severity = severity;
        this.recoveryAction = recoveryAction;
    }
    
    /**
     * Constructor with full error details and additional information
     * 
     * @param code Error code
     * @param message Error message
     * @param timestamp Error timestamp
     * @param severity Error severity
     * @param recoveryAction Recommended recovery action
     * @param details Additional error details
     */
    public ErrorInfo(String code, String message, Date timestamp, String severity, String recoveryAction, Map<String, Object> details) {
        this(code, message, timestamp, severity, recoveryAction);
        this.details = details != null ? details : new HashMap<>();
    }
    
    /**
     * Get the error code
     * @return Error code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Set the error code
     * @param code Error code
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * Get the error message
     * @return Error message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Set the error message
     * @param message Error message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Get the error timestamp
     * @return Error timestamp as string
     */
    public String getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set the error timestamp
     * @param timestamp Error timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get the error severity
     * @return Error severity
     */
    public String getSeverity() {
        return severity;
    }
    
    /**
     * Set the error severity
     * @param severity Error severity
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    /**
     * Get the recommended recovery action
     * @return Recovery action
     */
    public String getRecoveryAction() {
        return recoveryAction;
    }
    
    /**
     * Set the recommended recovery action
     * @param recoveryAction Recovery action
     */
    public void setRecoveryAction(String recoveryAction) {
        this.recoveryAction = recoveryAction;
    }
    
    /**
     * Get additional error details
     * @return Map of error details
     */
    public Map<String, Object> getDetails() {
        return details;
    }
    
    /**
     * Set additional error details
     * @param details Map of error details
     */
    public void setDetails(Map<String, Object> details) {
        this.details = details != null ? details : new HashMap<>();
    }
    
    /**
     * Add a detail to the error information
     * @param key Detail key
     * @param value Detail value
     */
    public void addDetail(String key, Object value) {
        if (details == null) {
            details = new HashMap<>();
        }
        details.put(key, value);
    }
    
    /**
     * Check if this is a network-related error
     * @return true if network error
     */
    public boolean isNetworkError() {
        return CODE_NETWORK.equals(code) || 
               (details != null && Boolean.TRUE.equals(details.get("isNetworkError")));
    }
    
    /**
     * Check if this is an authentication error
     * @return true if authentication error
     */
    public boolean isAuthenticationError() {
        return CODE_AUTHENTICATION.equals(code);
    }
    
    /**
     * Check if this error requires user action
     * @return true if user action required
     */
    public boolean requiresUserAction() {
        return RECOVERY_LOGIN_AGAIN.equals(recoveryAction) || 
               RECOVERY_CHECK_CONNECTION.equals(recoveryAction) ||
               RECOVERY_UPDATE_APP.equals(recoveryAction) ||
               RECOVERY_CONTACT_SUPPORT.equals(recoveryAction);
    }
    
    /**
     * Check if this error is critical (high or critical severity)
     * @return true if critical error
     */
    public boolean isCritical() {
        return SEVERITY_HIGH.equals(severity) || SEVERITY_CRITICAL.equals(severity);
    }
    
    /**
     * Check if this error should be retried automatically
     * @return true if should retry
     */
    public boolean shouldRetry() {
        return RECOVERY_RETRY.equals(recoveryAction) || 
               CODE_NETWORK.equals(code) || 
               CODE_TIMEOUT.equals(code);
    }
    
    /**
     * Get a user-friendly message for this error
     * @return User-friendly error message
     */
    public String getUserFriendlyMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }
        
        // Return standard messages based on code
        switch (code) {
            case CODE_NETWORK:
                return "Network connection error. Please check your internet connection.";
            case CODE_AUTHENTICATION:
                return "Authentication error. Please log in again.";
            case CODE_PERMISSION:
                return "You don't have permission to perform this action.";
            case CODE_VALIDATION:
                return "Invalid data. Please check your input.";
            case CODE_SERVER:
                return "Server error. Please try again later.";
            case CODE_CONFLICT:
                return "Data conflict error. The data has been changed by another user.";
            case CODE_NOT_FOUND:
                return "The requested resource was not found.";
            case CODE_TIMEOUT:
                return "Operation timed out. Please try again.";
            case CODE_CANCELLED:
                return "Operation was cancelled.";
            default:
                return "An unexpected error occurred.";
        }
    }
    
    /**
     * Get a user-friendly recovery action message
     * @return User-friendly recovery action message
     */
    public String getRecoveryActionMessage() {
        switch (recoveryAction) {
            case RECOVERY_RETRY:
                return "Please try again.";
            case RECOVERY_CONTACT_SUPPORT:
                return "Please contact support for assistance.";
            case RECOVERY_CHECK_CONNECTION:
                return "Please check your internet connection and try again.";
            case RECOVERY_LOGIN_AGAIN:
                return "Please log in again.";
            case RECOVERY_UPDATE_APP:
                return "Please update the app to the latest version.";
            case RECOVERY_WAIT:
                return "Please wait a moment and try again.";
            case RECOVERY_NONE:
            default:
                return "";
        }
    }
    
    //-----------------------------------------------------------------------------------
    // Static factory methods for common error scenarios
    //-----------------------------------------------------------------------------------
    
    /**
     * Create a network error
     * @param message Error message
     * @return ErrorInfo instance
     */
    public static ErrorInfo createNetworkError(String message) {
        return new ErrorInfo(
            CODE_NETWORK,
            message != null ? message : "Network connection error",
            new Date(),
            SEVERITY_MEDIUM,
            RECOVERY_CHECK_CONNECTION
        );
    }
    
    /**
     * Create an authentication error
     * @param message Error message
     * @return ErrorInfo instance
     */
    public static ErrorInfo createAuthError(String message) {
        return new ErrorInfo(
            CODE_AUTHENTICATION,
            message != null ? message : "Authentication error",
            new Date(),
            SEVERITY_HIGH,
            RECOVERY_LOGIN_AGAIN
        );
    }
    
    /**
     * Create a server error
     * @param message Error message
     * @return ErrorInfo instance
     */
    public static ErrorInfo createServerError(String message) {
        return new ErrorInfo(
            CODE_SERVER,
            message != null ? message : "Server error",
            new Date(),
            SEVERITY_HIGH,
            RECOVERY_RETRY
        );
    }
    
    /**
     * Create a validation error
     * @param message Error message
     * @return ErrorInfo instance
     */
    public static ErrorInfo createValidationError(String message) {
        return new ErrorInfo(
            CODE_VALIDATION,
            message != null ? message : "Validation error",
            new Date(),
            SEVERITY_MEDIUM,
            RECOVERY_NONE
        );
    }
    
    /**
     * Create a timeout error
     * @param message Error message
     * @return ErrorInfo instance
     */
    public static ErrorInfo createTimeoutError(String message) {
        return new ErrorInfo(
            CODE_TIMEOUT,
            message != null ? message : "Operation timed out",
            new Date(),
            SEVERITY_MEDIUM,
            RECOVERY_RETRY
        );
    }
    
    /**
     * Create an error from a Throwable
     * @param throwable The exception or error
     * @return ErrorInfo instance
     */
    public static ErrorInfo fromThrowable(Throwable throwable) {
        if (throwable == null) {
            return new ErrorInfo(CODE_UNKNOWN, "Unknown error", new Date());
        }
        
        String message = throwable.getMessage();
        if (message == null || message.isEmpty()) {
            message = throwable.getClass().getSimpleName();
        }
        
        // Determine error type from exception class
        if (throwable instanceof java.net.UnknownHostException || 
            throwable instanceof java.net.ConnectException ||
            throwable instanceof java.net.SocketException ||
            throwable instanceof java.net.SocketTimeoutException) {
            return createNetworkError(message);
        } else if (throwable instanceof java.io.InterruptedIOException) {
            return createTimeoutError(message);
        } else if (throwable instanceof SecurityException ||
                  throwable instanceof IllegalAccessException) {
            return new ErrorInfo(
                CODE_PERMISSION,
                message,
                new Date(),
                SEVERITY_HIGH,
                RECOVERY_CONTACT_SUPPORT
            );
        } else if (throwable instanceof IllegalArgumentException ||
                  throwable instanceof IllegalStateException) {
            return createValidationError(message);
        }
        
        // Default to unknown error
        return new ErrorInfo(
            CODE_UNKNOWN,
            message,
            new Date(),
            SEVERITY_MEDIUM,
            RECOVERY_RETRY
        );
    }
}
