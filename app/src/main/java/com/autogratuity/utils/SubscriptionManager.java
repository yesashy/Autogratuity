package com.autogratuity.utils;

import android.app.Activity;
import android.content.Context;
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
import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.repository.subscription.SubscriptionRepository;
import com.autogratuity.data.repository.preference.PreferenceRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Manager for handling Pro subscription features
 * Updated to use domain repositories with RxJava
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

    // Preference keys
    private static final String KEY_TRIAL_USED = "trial_used";

    // Instance variables
    private static SubscriptionManager instance;
    private Context context;
    private BillingClient billingClient;
    private SubscriptionRepository subscriptionRepository;
    private PreferenceRepository preferenceRepository;
    private List<ProductDetails> productDetailsList = new ArrayList<>();
    private SubscriptionStatusListener statusListener;
    private PurchasesUpdatedListener purchasesUpdatedListener;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private SubscriptionManager(Context context, SubscriptionRepository subscriptionRepository, 
                               PreferenceRepository preferenceRepository) {
        this.context = context.getApplicationContext();
        this.subscriptionRepository = subscriptionRepository;
        this.preferenceRepository = preferenceRepository;

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
    public static synchronized SubscriptionManager getInstance(Context context, 
                                SubscriptionRepository subscriptionRepository,
                                PreferenceRepository preferenceRepository) {
        if (instance == null) {
            instance = new SubscriptionManager(context, subscriptionRepository, preferenceRepository);
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
     * Set a listener for purchases updated
     */
    public void setPurchasesUpdatedListener(PurchasesUpdatedListener listener) {
        this.purchasesUpdatedListener = listener;
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
    public void queryPurchases() {
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
    public void acknowledgePurchase(Purchase purchase) {
        if (!purchase.isAcknowledged()) {
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
            // Create subscription status object
            SubscriptionStatus status = new SubscriptionStatus();
            status.setLevel(STATUS_PRO);
            status.setActive(true);
            status.setStartDate(new Date(purchase.getPurchaseTime()));
            
            // Set expiry for subscriptions, null for lifetime
            if (purchase.getProducts().contains(PRODUCT_ID_PRO_LIFETIME)) {
                status.setExpiryDate(null);
            } else {
                // Add 1 day buffer for expiry
                status.setExpiryDate(new Date(purchase.getPurchaseTime() + 86400000));
            }
            
            status.setProvider("google_play");
            status.setOrderId(purchase.getOrderId());
            status.setPurchaseToken(purchase.getPurchaseToken());
            status.setLastUpdated(new Date());
            
            // Update repository
            disposables.add(
                subscriptionRepository.updateSubscriptionStatus(status)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        () -> {
                            Log.d(TAG, "Subscription updated in repository");
                            
                            // Also log the event
                            logSubscriptionEvent("purchase", purchase.getOrderId());
                            
                            // Notify listener
                            if (statusListener != null) {
                                statusListener.onSubscriptionStatusChanged(STATUS_PRO);
                            }
                        },
                        error -> {
                            Log.e(TAG, "Error updating subscription in repository", error);
                        }
                    )
            );
        }
    }

    /**
     * Log a subscription event to repository
     */
    private void logSubscriptionEvent(String eventType, String transactionId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("timestamp", System.currentTimeMillis());

        if (transactionId != null) {
            event.put("transactionId", transactionId);
        }

        disposables.add(
            subscriptionRepository.addSubscriptionRecord(event)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    reference -> {
                        Log.d(TAG, "Subscription event logged");
                    },
                    error -> {
                        Log.e(TAG, "Error logging subscription event", error);
                    }
                )
        );
    }

    /**
     * Check if user has Pro features
     */
    public boolean isProUser() {
        try {
            return subscriptionRepository.isProUser().blockingGet();
        } catch (Exception e) {
            Log.e(TAG, "Error checking pro status", e);
            return false;
        }
    }

    /**
     * Check if a free trial is available
     */
    public boolean isTrialAvailable() {
        try {
            return preferenceRepository.getPreferenceSetting(KEY_TRIAL_USED, false)
                    .map(used -> !used)
                    .blockingGet();
        } catch (Exception e) {
            Log.e(TAG, "Error checking trial availability", e);
            return false;
        }
    }

    /**
     * Start a free trial
     */
    public void startFreeTrial() {
        if (isTrialAvailable()) {
            // Set trial period (7 days)
            long expiryTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000);
            Date expiryDate = new Date(expiryTime);
            
            // Create subscription status
            SubscriptionStatus status = new SubscriptionStatus();
            status.setLevel(STATUS_TRIAL);
            status.setActive(true);
            status.setStartDate(new Date());
            status.setExpiryDate(expiryDate);
            status.setProvider("trial");
            status.setLastUpdated(new Date());
            
            // Update repositories
            disposables.add(
                subscriptionRepository.updateSubscriptionStatus(status)
                    .andThen(preferenceRepository.setPreferenceSetting(KEY_TRIAL_USED, true))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        () -> {
                            Log.d(TAG, "Trial started successfully");
                            
                            // Log the trial start event
                            logSubscriptionEvent("trial_start", null);
                            
                            // Notify listener
                            if (statusListener != null) {
                                statusListener.onSubscriptionStatusChanged(STATUS_TRIAL);
                            }
                        },
                        error -> {
                            Log.e(TAG, "Error starting trial", error);
                        }
                    )
            );
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
            
            // Notify custom listener if set
            if (purchasesUpdatedListener != null) {
                purchasesUpdatedListener.onPurchasesUpdated(billingResult, purchases);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled the purchase");
        } else {
            Log.e(TAG, "Purchase update error: " + billingResult.getDebugMessage());
        }
    }
    
    /**
     * Clean up resources when manager is no longer needed
     */
    public void dispose() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
        
        if (billingClient != null) {
            billingClient.endConnection();
        }
        
        instance = null;
    }

    /**
     * Interface for subscription status changes
     */
    public interface SubscriptionStatusListener {
        void onSubscriptionStatusChanged(String status);
    }
}