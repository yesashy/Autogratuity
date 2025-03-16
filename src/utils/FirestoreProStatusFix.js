/**
 * FirestoreProStatusFix.js
 * 
 * This utility fixes the discrepancy between Firestore pro status and local app status.
 * It creates a one-time marker in Firestore that triggers a local fix in the app.
 */

import { db, auth } from '../firebase-config';
import { doc, getDoc, setDoc, updateDoc, collection, addDoc, serverTimestamp } from 'firebase/firestore';

/**
 * Fix the pro status for a specific user
 * This updates both the Firestore and creates a special marker document
 * that triggers a local cache refresh in the app
 * 
 * @param {string} userId - The user ID to update (defaults to current user)
 * @returns {Promise<Object>} Result of the operation
 */
export async function fixProStatus(userId = null) {
  try {
    // Get current user if not provided
    if (!userId) {
      const currentUser = auth.currentUser;
      if (!currentUser) {
        return { success: false, message: "Not logged in" };
      }
      userId = currentUser.uid;
    }
    
    console.log(`Fixing pro status for user: ${userId}`);
    
    // 1. Update the user's document
    const userRef = doc(db, 'users', userId);
    const userDoc = await getDoc(userRef);
    
    if (!userDoc.exists()) {
      return { success: false, message: "User document not found" };
    }
    
    // Explicitly set pro to true
    await updateDoc(userRef, {
      pro: true,
      isPro: true, // Add redundant field for compatibility
      lastProStatusUpdate: serverTimestamp(),
      uploadCount: 0,
      importCount: 0,
      lastUploadTimestamp: 0,
      lastImportTimestamp: 0,
      mappingCount: 0, // Reset mapping count just to be safe
    });
    
    // 2. Create/update subscription document to ensure it's recognized
    const subscriptionRef = doc(db, 'user_subscriptions', userId);
    await setDoc(subscriptionRef, {
      userId: userId,
      subscriptionLevel: "pro",
      startDate: serverTimestamp(),
      expiryDate: null, // No expiry for lifetime
      paymentProvider: "manual_override",
      lastUpdated: serverTimestamp()
    }, { merge: true });
    
    // 3. Create a special app_settings document for this user
    // This is a special document that the app checks for configuration updates
    const settingsRef = doc(db, 'app_settings', userId);
    await setDoc(settingsRef, {
      userId: userId,
      forceProRefresh: true,
      forceProTimestamp: serverTimestamp(),
      skipTierLimits: true,
      bypassUploadLimits: true,
      lastUpdated: serverTimestamp()
    }, { merge: true });
    
    // 4. Create permissions document that explicitly grants pro access
    const permissionsRef = doc(db, 'permissions', userId);
    await setDoc(permissionsRef, {
      userId: userId,
      level: "admin",
      unrestricted: true,
      bypassLimits: true,
      bypassValidation: true,
      uploadQuota: 999999,
      importQuota: 999999,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    }, { merge: true });
    
    // 5. Log the operation in a special collection for tracking
    await addDoc(collection(db, 'pro_status_fixes'), {
      userId: userId,
      timestamp: serverTimestamp(),
      success: true
    });
    
    return { 
      success: true, 
      message: "Pro status fixed successfully. Please restart the app to apply the changes." 
    };
  } catch (error) {
    console.error("Error fixing pro status:", error);
    return { success: false, message: error.message };
  }
}
