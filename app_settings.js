// Firebase Admin Script for fixing pro status issue
// This script creates special documents that force the app to refresh its cache

const admin = require('firebase-admin');
const serviceAccount = require('./path/to/your-service-account-key.json');

// Initialize the admin SDK - you need to provide your service account key
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const userId = "KRqWcCcR7bOnwdfUe2tKANlwsX03";

async function fixProStatus() {
  try {
    console.log(`Starting pro status fix for user: ${userId}`);
    
    // Create a batch for atomic updates
    const batch = db.batch();
    
    // 1. Update user document
    const userRef = db.collection('users').doc(userId);
    batch.update(userRef, {
      pro: true,
      isPro: true,
      lastProStatusUpdate: admin.firestore.FieldValue.serverTimestamp(),
      uploadCount: 0,
      importCount: 0,
      lastUploadTimestamp: 0,
      lastImportTimestamp: 0,
      mappingCount: 0
    });
    
    // 2. Create/update subscription
    const subscriptionRef = db.collection('user_subscriptions').doc(userId);
    batch.set(subscriptionRef, {
      userId: userId,
      subscriptionLevel: "pro",
      startDate: admin.firestore.FieldValue.serverTimestamp(),
      expiryDate: null,
      paymentProvider: "manual_override",
      lastUpdated: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });
    
    // 3. Create app_settings document (critical for cache refresh)
    const settingsRef = db.collection('app_settings').doc(userId);
    batch.set(settingsRef, {
      userId: userId,
      forceProRefresh: true,
      forceProTimestamp: admin.firestore.FieldValue.serverTimestamp(),
      skipTierLimits: true,
      bypassUploadLimits: true,
      lastUpdated: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });
    
    // 4. Create/update permissions document
    const permissionsRef = db.collection('permissions').doc(userId);
    batch.set(permissionsRef, {
      userId: userId,
      level: "admin",
      unrestricted: true,
      bypassLimits: true,
      bypassValidation: true,
      uploadQuota: 999999,
      importQuota: 999999,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });
    
    // Execute all updates atomically
    await batch.commit();
    
    // Log the operation separately
    await db.collection('pro_status_fixes').add({
      userId: userId,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      success: true
    });
    
    console.log("Pro status fix completed successfully!");
    console.log("Restart your app to apply changes.");
    
  } catch (error) {
    console.error("Error fixing pro status:", error);
  } finally {
    process.exit(0);
  }
}

// Execute the fix
fixProStatus();
