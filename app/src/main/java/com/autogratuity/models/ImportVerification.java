package com.autogratuity.models;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for import verification status
 * Tracks metadata about data verification
 */
public class ImportVerification {
    private boolean verifiedByPro;
    private Timestamp verificationTimestamp;
    private String verificationSource;
    private String verificationNotes;

    /**
     * Default constructor
     */
    public ImportVerification() {
        this.verifiedByPro = false;
        this.verificationTimestamp = Timestamp.now();
        this.verificationSource = "manual";
        this.verificationNotes = "";
    }

    /**
     * Convert to Firestore document
     *
     * @return Map with verification data fields
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("verifiedByPro", verifiedByPro);
        map.put("verificationTimestamp", verificationTimestamp);
        map.put("verificationSource", verificationSource);
        
        if (verificationNotes != null && !verificationNotes.isEmpty()) {
            map.put("verificationNotes", verificationNotes);
        }
        
        return map;
    }

    // Getters and setters
    public boolean isVerifiedByPro() {
        return verifiedByPro;
    }

    public void setVerifiedByPro(boolean verifiedByPro) {
        this.verifiedByPro = verifiedByPro;
    }

    public Timestamp getVerificationTimestamp() {
        return verificationTimestamp;
    }

    public void setVerificationTimestamp(Timestamp verificationTimestamp) {
        this.verificationTimestamp = verificationTimestamp;
    }

    public String getVerificationSource() {
        return verificationSource;
    }

    public void setVerificationSource(String verificationSource) {
        this.verificationSource = verificationSource;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }
}