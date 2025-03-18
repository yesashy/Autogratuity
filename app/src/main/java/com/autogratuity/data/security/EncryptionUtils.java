package com.autogratuity.data.security;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Utility class for encrypting and decrypting sensitive data.
 * Uses the Android KeyStore to securely store encryption keys.
 */
public class EncryptionUtils {
    
    private static final String TAG = "EncryptionUtils";
    
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String MASTER_KEY_ALIAS = "autogratuity_master_key";
    private static final String TRANSFORMATION = KeyProperties.KEY_ALGORITHM_AES + "/" +
            KeyProperties.BLOCK_MODE_GCM + "/" +
            KeyProperties.ENCRYPTION_PADDING_NONE;
    
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    private static EncryptionUtils instance;
    private final KeyStore keyStore;
    
    /**
     * Get singleton instance of EncryptionUtils
     * 
     * @param context Android context
     * @return EncryptionUtils instance
     */
    public static synchronized EncryptionUtils getInstance(Context context) {
        if (instance == null) {
            instance = new EncryptionUtils(context);
        }
        return instance;
    }
    
    /**
     * Private constructor
     * 
     * @param context Android context
     */
    private EncryptionUtils(Context context) {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            
            // Create master key if it doesn't exist
            if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                createMasterKey();
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            Log.e(TAG, "Error initializing encryption utils", e);
            throw new RuntimeException("Could not initialize encryption utils", e);
        }
    }
    
    /**
     * Create a new master key in the Android KeyStore
     */
    private void createMasterKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            
            KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
                    MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false)
                    .build();
            
            keyGenerator.init(keySpec);
            keyGenerator.generateKey();
            
            Log.d(TAG, "Master key created successfully");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            Log.e(TAG, "Error creating master key", e);
            throw new RuntimeException("Could not create master key", e);
        }
    }
    
    /**
     * Get the master key from the Android KeyStore
     * 
     * @return SecretKey for encryption/decryption
     */
    private SecretKey getMasterKey() {
        try {
            return (SecretKey) keyStore.getKey(MASTER_KEY_ALIAS, null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            Log.e(TAG, "Error getting master key", e);
            throw new RuntimeException("Could not get master key", e);
        }
    }
    
    /**
     * Encrypt a string
     * 
     * @param plaintext String to encrypt
     * @return Base64-encoded encrypted string
     */
    public String encrypt(String plaintext) {
        try {
            if (plaintext == null) {
                return null;
            }
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getMasterKey());
            
            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            Log.e(TAG, "Error encrypting data", e);
            throw new RuntimeException("Could not encrypt data", e);
        }
    }
    
    /**
     * Decrypt a Base64-encoded encrypted string
     * 
     * @param encrypted Base64-encoded encrypted string
     * @return Decrypted string
     */
    public String decrypt(String encrypted) {
        try {
            if (encrypted == null) {
                return null;
            }
            
            byte[] combined = Base64.decode(encrypted, Base64.DEFAULT);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[combined.length - GCM_IV_LENGTH];
            
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), spec);
            
            byte[] decrypted = cipher.doFinal(encryptedData);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            Log.e(TAG, "Error decrypting data", e);
            throw new RuntimeException("Could not decrypt data", e);
        }
    }
    
    /**
     * Check if the given string is encrypted (Base64-encoded)
     * 
     * @param text String to check
     * @return True if the string appears to be encrypted
     */
    public boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        try {
            // Try to decode as Base64
            byte[] decoded = Base64.decode(text, Base64.DEFAULT);
            
            // Check if it's long enough to contain IV + some data
            return decoded.length > GCM_IV_LENGTH;
        } catch (IllegalArgumentException e) {
            // Not a valid Base64 string
            return false;
        }
    }
}
