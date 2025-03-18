package com.autogratuity.data.model;

import java.util.Date;

/**
 * Model class for representing error information in sync operations.
 */
public class ErrorInfo {
    private String code;
    private String message;
    private String timestamp;
    
    /**
     * Default constructor for Firestore
     */
    public ErrorInfo() {
    }
    
    /**
     * Constructor with error details
     * 
     * @param code Error code
     * @param message Error message
     * @param timestamp Error timestamp
     */
    public ErrorInfo(String code, String message, Date timestamp) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp.toString() : null;
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
}
