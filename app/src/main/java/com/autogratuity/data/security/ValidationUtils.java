package com.autogratuity.data.security;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Utility class for validating data before storage or processing.
 * This helps prevent invalid data from being stored and enhances security.
 */
public class ValidationUtils {
    
    // Email pattern
    private static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;
    
    // Phone pattern - matches standard US formats plus international
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$");
    
    // Payment card pattern - matches most common credit cards
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile(
            "^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|6(?:011|5[0-9]{2})[0-9]{12})$");
    
    /**
     * Validate an email address
     * 
     * @param email Email to validate
     * @return True if the email is valid
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate a phone number
     * 
     * @param phone Phone number to validate
     * @return True if the phone number is valid
     */
    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validate a payment card number using Luhn algorithm
     * 
     * @param cardNumber Card number to validate
     * @return True if the card number is valid
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return false;
        }
        
        // Remove spaces and dashes
        String normalizedNumber = cardNumber.replaceAll("[ -]", "");
        
        // Check against pattern
        if (!CARD_NUMBER_PATTERN.matcher(normalizedNumber).matches()) {
            return false;
        }
        
        // Validate using Luhn algorithm
        int sum = 0;
        boolean alternate = false;
        
        for (int i = normalizedNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(normalizedNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
    
    /**
     * Sanitize a string for database storage.
     * Helps prevent SQL injection and XSS attacks.
     * 
     * @param input String to sanitize
     * @return Sanitized string
     */
    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove potentially dangerous characters
        String sanitized = input.replaceAll("[\\<\\>\\&\\;\\`\\']", "");
        
        // Trim to reasonable length if too long
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000);
        }
        
        return sanitized;
    }
    
    /**
     * Validate a subscription payload before processing
     * 
     * @param payload Subscription payload to validate
     * @return True if the payload seems valid
     */
    public static boolean isValidSubscriptionPayload(String payload) {
        if (TextUtils.isEmpty(payload)) {
            return false;
        }
        
        // Check for minimum length (simple validation)
        if (payload.length() < 10) {
            return false;
        }
        
        // Check for expected JSON structure
        return payload.contains("receipt") && 
               (payload.contains("purchaseToken") || 
                payload.contains("transactionId"));
    }
}
