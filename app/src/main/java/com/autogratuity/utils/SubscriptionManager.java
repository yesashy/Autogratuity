package com.autogratuity.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager for handling Pro subscription features
 */
public class SubscriptionManager implements PurchasesUpdatedListener {
    private static final String TAG = "SubscriptionManager";

    // Product IDs
    public static final String PRODUCT_ID_PRO_MONTHLY = "com.autogratuity.pro.monthly";
    public static final String PRODUCT_ID_PRO_YEARLY = "com.autogratuity.pro.yearly";
    public static final String PRODUCT_ID_PRO_LIFETIME = "com.autogratuity.pro.lifetime";

    // Subscription status constants
    public static final String STATUS_FREE = "free";
    public static final String STATUS_PRO = "pro";
    public static final String STATUS_TRIAL = "trial";

    // Shared preferences
    private static final String PREF_NAME = "autogratuity_subscription";
    private static final String PREF_KEY_SUBSCRIPTION_STATUS = "subscription_status";
    private static final String PREF_KEY_SUBSCRIPTION_EXPIRY = "subscription_expiry";
    private static final String PREF_KEY_TRIAL_USED = "trial_used";

    // Instance variables
    private static SubscriptionManager instance;
    private Context context;
    private BillingClient billingClient;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<ProductDetails> productDetailsList = new ArrayList<>();
    private SubscriptionStatusListener statusListener;

    private SubscriptionManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();

        // Initialize billing client
        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        // Connect to Google Play
        connectToGooglePlay();
    }

    /**
     * Get the singleton instance
     */
    public static synchronized SubscriptionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SubscriptionManager(context);
        }
        return instance;
    }

    /**
     * Set a listener for subscription status changes
     */
    public void setStatusListener(SubscriptionStatusListener listener) {
        this.statusListener = listener;
    }

    /**
     * Connect to Google Play
     */
    private void connectToGooglePlay() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Google Play Billing setup finished");
                    // Load product details
                    queryProductDetails();
                    // Load existing purchases
                    queryPurchases();
                } else {
                    Log.e(TAG, "Google Play Billing setup failed: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "Google Play Billing service disconnected");
                // Try to reconnect
                connectToGooglePlay();
            }
        });
    }

    /**
     * Query available product details
     */
    private void queryProductDetails() {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        // Add subscription products
        productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_PRO_MONTHLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
        );

        productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_PRO_YEARLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
        );

        productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_PRO_LIFETIME)
                        .setProductType(BillingClient.ProductType.INAPP) // One-time purchase
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    productDetailsList = list;
                    Log.d(TAG, "Product details loaded: " + list.size());
                } else {
                    Log.e(TAG, "Failed to load product details: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    /**
     * Query existing purchases
     */
    private void queryPurchases() {
        // Check subscriptions
        QueryPurchasesParams subscriptionParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build();

        billingClient.queryPurchasesAsync(subscriptionParams, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    processPurchases(list);
                }
            }
        });

        // Check one-time purchases
        QueryPurchasesParams oneTimeParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        billingClient.queryPurchasesAsync(oneTimeParams, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    processPurchases(list);
                }
            }
        });
    }

    /**
     * Process purchases
     */
    private void processPurchases(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                // Acknowledge purchase if needed
                if (!purchase.isAcknowledged()) {
                    acknowledgePurchase(purchase);
                }

                // Update subscription status
                updateSubscriptionStatus(purchase);
            }
        }
    }

    /**
     * Acknowledge a purchase
     */
    private void acknowledgePurchase(Purchase purchase) {
        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged: " + purchase.getOrderId());
                } else {
                    Log.e(TAG, "Failed to acknowledge purchase: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    /**
     * Update subscription status based on purchase
     */
    private void updateSubscriptionStatus(Purchase purchase) {
        // Check if this is a pro subscription or one-time purchase
        boolean isPro = false;

        for (String productId : purchase.getProducts()) {
            if (productId.equals(PRODUCT_ID_PRO_MONTHLY) ||
                    productId.equals(PRODUCT_ID_PRO_YEARLY) ||
                    productId.equals(PRODUCT_ID_PRO_LIFETIME)) {
                isPro = true;
                break;
            }
        }

        if (isPro) {
            // Save status locally
            saveSubscriptionStatus(STATUS_PRO, purchase.getPurchaseTime() + 86400000); // Add 1 day buffer

            // Update Firestore if logged in
            updateFirestoreSubscription(STATUS_PRO, purchase);

            // Notify listener
            if (statusListener != null) {
                statusListener.onSubscriptionStatusChanged(STATUS_PRO);
            }
        }
    }

    /**
     * Save subscription status to SharedPreferences
     */
    private void saveSubscriptionStatus(String status, long expiryTime) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_KEY_SUBSCRIPTION_STATUS, status);
        editor.putLong(PREF_KEY_SUBSCRIPTION_EXPIRY, expiryTime);
        editor.apply();
    }

    /**
     * Update subscription in Firestore
     */
    private void updateFirestoreSubscription(String status, Purchase purchase) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Create subscription record
        Map<String, Object> subscription = new HashMap<>();
        subscription.put("userId", user.getUid());
        subscription.put("subscriptionLevel", status);
        subscription.put("startDate", purchase.getPurchaseTime());

        if (purchase.getProducts().contains(PRODUCT_ID_PRO_LIFETIME)) {
            subscription.put("expiryDate", null); // No expiry for lifetime
        } else {
            // For subscriptions, use Google's expiry info
            subscription.put("expiryDate", purchase.getPurchaseTime() + 86400000); // Temporary +1 day
        }

        subscription.put("paymentProvider", "google_play");
        subscription.put("orderId", purchase.getOrderId());
        subscription.put("purchaseToken", purchase.getPurchaseToken());
        subscription.put("lastUpdated", System.currentTimeMillis());

        // Store in Firestore
        db.collection("user_subscriptions")
                .document(user.getUid())
                .set(subscription)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Subscription updated in Firestore");

                    // Also log the event
                    logSubscriptionEvent("purchase", purchase.getOrderId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating subscription in Firestore", e);
                });
    }

    /**
     * Log a subscription event to Firestore
     */
    private void logSubscriptionEvent(String eventType, String transactionId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> event = new HashMap<>();
        event.put("userId", user.getUid());
        event.put("eventType", eventType);
        event.put("timestamp", System.currentTimeMillis());

        if (transactionId != null) {
            event.put("transactionId", transactionId);
        }

        db.collection("subscription_events")
                .add(event)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error logging subscription event", e);
                });
    }

    /**
     * Check if user has Pro features
     */
    public boolean isProUser() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String status = prefs.getString(PREF_KEY_SUBSCRIPTION_STATUS, STATUS_FREE);
        long expiryTime = prefs.getLong(PREF_KEY_SUBSCRIPTION_EXPIRY, 0);

        // If status is PRO and not expired, or if it's a lifetime purchase (expiryTime = 0)
        if (STATUS_PRO.equals(status) && (expiryTime == 0 || System.currentTimeMillis() < expiryTime)) {
            return true;
        }

        // If status is TRIAL and not expired
        if (STATUS_TRIAL.equals(status) && System.currentTimeMillis() < expiryTime) {
            return true;
        }

        return false;
    }

    /**
     * Check if a free trial is available
     */
    public boolean isTrialAvailable() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return !prefs.getBoolean(PREF_KEY_TRIAL_USED, false);
    }

    /**
     * Start a free trial
     */
    public void startFreeTrial() {
        if (isTrialAvailable()) {
            // Set trial period (7 days)
            long expiryTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000);

            // Update local storage
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_KEY_SUBSCRIPTION_STATUS, STATUS_TRIAL);
            editor.putLong(PREF_KEY_SUBSCRIPTION_EXPIRY, expiryTime);
            editor.putBoolean(PREF_KEY_TRIAL_USED, true);
            editor.apply();

            // Update Firestore
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Map<String, Object> trial = new HashMap<>();
                trial.put("userId", user.getUid());
                trial.put("subscriptionLevel", STATUS_TRIAL);
                trial.put("startDate", System.currentTimeMillis());
                trial.put("expiryDate", expiryTime);
                trial.put("lastUpdated", System.currentTimeMillis());

                db.collection("user_subscriptions")
                        .document(user.getUid())
                        .set(trial);

                // Log the trial start event
                logSubscriptionEvent("trial_start", null);
            }

            // Notify listener
            if (statusListener != null) {
                statusListener.onSubscriptionStatusChanged(STATUS_TRIAL);
            }
        }
    }

    /**
     * Launch the purchase flow
     */
    public void launchPurchaseFlow(Activity activity, String productId) {
        // Find the product details
        ProductDetails productDetails = null;
        for (ProductDetails details : productDetailsList) {
            if (details.getProductId().equals(productId)) {
                productDetails = details;
                break;
            }
        }

        if (productDetails == null) {
            Log.e(TAG, "Product details not found for: " + productId);
            return;
        }

        // Create purchase params
        BillingFlowParams.Builder paramsBuilder = BillingFlowParams.newBuilder();

        // Handle different product types
        if (productId.equals(PRODUCT_ID_PRO_LIFETIME)) {
            // One-time purchase
            paramsBuilder.setProductDetailsParamsList(
                    Collections.singletonList(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .build()
                    )
            );
        } else {
            // Subscription purchase
            if (productDetails.getSubscriptionOfferDetails() != null &&
                    !productDetails.getSubscriptionOfferDetails().isEmpty()) {

                paramsBuilder.setProductDetailsParamsList(
                        Collections.singletonList(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                                        .build()
                        )
                );
            }
        }

        // Launch the flow
        BillingResult billingResult = billingClient.launchBillingFlow(activity, paramsBuilder.build());

        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Error launching billing flow: " + billingResult.getDebugMessage());
        }
    }

    /**
     * Called when purchases are updated
     */
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            // Process new purchases
            processPurchases(purchases);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled the purchase");
        } else {
            Log.e(TAG, "Purchase update error: " + billingResult.getDebugMessage());
        }
    }

    /**
     * Interface for subscription status changes
     */
    public interface SubscriptionStatusListener {
        void onSubscriptionStatusChanged(String status);
    }
}