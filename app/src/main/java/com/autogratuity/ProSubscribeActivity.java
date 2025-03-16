package com.autogratuity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.billingclient.api.Purchase;
import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.repository.DataRepository;
import com.autogratuity.data.repository.RepositoryProvider;
import com.autogratuity.utils.SubscriptionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity for user to subscribe to Pro features.
 * Updated to use the repository pattern for subscription management.
 */
public class ProSubscribeActivity extends AppCompatActivity implements SubscriptionManager.PurchasesUpdatedListener {
    private static final String TAG = "ProSubscribeActivity";
    
    // Repository and RxJava components
    private DataRepository repository;
    private CompositeDisposable disposables = new CompositeDisposable();
    
    // Subscription manager
    private SubscriptionManager subscriptionManager;
    
    // UI components
    private ConstraintLayout contentContainer;
    private ProgressBar loadingIndicator;
    private TextView errorView;
    private TextView subscriptionStatusView;
    private View subscriptionInfoContainer;
    
    // Subscription buttons
    private Button monthlyButton;
    private Button yearlyButton;
    private Button lifetimeButton;
    private Button trialButton;
    
    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_subscribe);

        // Get repository instance
        repository = RepositoryProvider.getRepository();
        
        // Initialize subscription manager
        subscriptionManager = SubscriptionManager.getInstance(this);
        subscriptionManager.setListener(this);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Upgrade to Pro");
        }

        // Initialize UI components
        initializeViews();
        
        // Load current subscription status
        loadSubscriptionStatus();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeViews() {
        // Find main containers
        contentContainer = findViewById(R.id.content_container);
        
        // Add loading indicator if not present in layout
        loadingIndicator = findViewById(R.id.loading_indicator);
        if (loadingIndicator == null) {
            loadingIndicator = new ProgressBar(this);
            loadingIndicator.setId(View.generateViewId());
            
            ConstraintLayout rootLayout = findViewById(android.R.id.content);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            
            loadingIndicator.setLayoutParams(params);
            loadingIndicator.setVisibility(View.GONE);
            rootLayout.addView(loadingIndicator);
        }
        
        // Add error view if not present in layout
        errorView = findViewById(R.id.error_view);
        if (errorView == null) {
            errorView = new TextView(this);
            errorView.setId(View.generateViewId());
            
            ConstraintLayout rootLayout = findViewById(android.R.id.content);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMargins(32, 0, 32, 0);
            
            errorView.setLayoutParams(params);
            errorView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            errorView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            errorView.setVisibility(View.GONE);
            rootLayout.addView(errorView);
        }
        
        // Find subscription status views (add to layout if needed)
        subscriptionStatusView = findViewById(R.id.subscription_status);
        subscriptionInfoContainer = findViewById(R.id.subscription_info_container);
        
        // Initialize buttons
        monthlyButton = findViewById(R.id.monthly_button);
        yearlyButton = findViewById(R.id.yearly_button);
        lifetimeButton = findViewById(R.id.lifetime_button);
        trialButton = findViewById(R.id.trial_button);

        // Set up button click listeners
        monthlyButton.setOnClickListener(v -> {
            subscriptionManager.launchPurchaseFlow(this,
                    SubscriptionManager.PRODUCT_ID_PRO_MONTHLY);
        });

        yearlyButton.setOnClickListener(v -> {
            subscriptionManager.launchPurchaseFlow(this,
                    SubscriptionManager.PRODUCT_ID_PRO_YEARLY);
        });

        lifetimeButton.setOnClickListener(v -> {
            subscriptionManager.launchPurchaseFlow(this,
                    SubscriptionManager.PRODUCT_ID_PRO_LIFETIME);
        });

        trialButton.setOnClickListener(v -> {
            if (subscriptionManager.isTrialAvailable()) {
                startFreeTrial();
            } else {
                Toast.makeText(this, "You've already used your free trial", Toast.LENGTH_SHORT).show();
                trialButton.setEnabled(false);
            }
        });
    }
    
    /**
     * Start a free trial using the repository
     */
    private void startFreeTrial() {
        showLoading(true);
        
        // Create a trial subscription
        SubscriptionStatus trialStatus = new SubscriptionStatus();
        trialStatus.setUserId(repository.getUserId());
        trialStatus.setStatus("trial");
        trialStatus.setActive(true);
        
        // Set start date to now
        Date startDate = new Date();
        trialStatus.setStartDate(startDate);
        
        // Set expiry date to 7 days from now
        long expiryTimeMillis = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 days in milliseconds
        Date expiryDate = new Date(expiryTimeMillis);
        trialStatus.setExpiryDate(expiryDate);
        
        // Update subscription status in repository
        disposables.add(
            repository.updateSubscriptionStatus(trialStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        showContent();
                        Toast.makeText(this, "Your 7-day free trial has started!", Toast.LENGTH_LONG).show();
                        finish(); // Return to main activity
                    },
                    error -> {
                        Log.e(TAG, "Error starting free trial", error);
                        showError("Error starting free trial: " + error.getMessage());
                    }
                )
        );
    }
    
    /**
     * Load subscription status using the repository
     */
    private void loadSubscriptionStatus() {
        showLoading(true);
        
        disposables.add(
            repository.getSubscriptionStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::updateUI,
                    this::handleError
                )
        );
    }
    
    /**
     * Set up real-time subscription updates
     */
    private void observeSubscriptionStatus() {
        disposables.add(
            repository.observeSubscriptionStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::updateUI,
                    this::handleError
                )
        );
    }
    
    /**
     * Update UI based on subscription status
     */
    private void updateUI(SubscriptionStatus status) {
        showContent();
        
        // Update trial button visibility based on status
        boolean isTrial = "trial".equals(status.getStatus());
        boolean isPro = status.isPro();
        
        // Update trial button state
        if (isPro || isTrial) {
            trialButton.setEnabled(false);
            trialButton.setAlpha(0.5f);
            
            // If user already has a paid plan, update button text
            if (isPro) {
                trialButton.setText("Pro plan active (trial not needed)");
            } else {
                trialButton.setText("Trial active");
            }
        } else {
            // Check if trial is available using legacy method
            // This can be refactored later to use the repository pattern completely
            boolean trialAvailable = subscriptionManager.isTrialAvailable();
            trialButton.setEnabled(trialAvailable);
            trialButton.setAlpha(trialAvailable ? 1.0f : 0.5f);
            
            if (!trialAvailable) {
                trialButton.setText("Free Trial (Already Used)");
            }
        }
        
        // Add subscription info display if it exists in the layout
        if (subscriptionStatusView != null) {
            if (isPro) {
                subscriptionStatusView.setText("PRO");
                
                // Display subscription details if container exists
                if (subscriptionInfoContainer != null) {
                    subscriptionInfoContainer.setVisibility(View.VISIBLE);
                    
                    // Display different text based on lifetime status
                    TextView subscriptionDetailsView = findViewById(R.id.subscription_details);
                    if (subscriptionDetailsView != null) {
                        if (status.isLifetime()) {
                            subscriptionDetailsView.setText("Lifetime access");
                        } else if (status.getExpiryDate() != null) {
                            String expiryDate = dateFormat.format(status.getExpiryDate());
                            subscriptionDetailsView.setText("Expires: " + expiryDate);
                        } else {
                            subscriptionDetailsView.setText("Active subscription");
                        }
                    }
                }
            } else if (isTrial) {
                subscriptionStatusView.setText("TRIAL");
                
                // Display trial details if container exists
                if (subscriptionInfoContainer != null) {
                    subscriptionInfoContainer.setVisibility(View.VISIBLE);
                    
                    TextView subscriptionDetailsView = findViewById(R.id.subscription_details);
                    if (subscriptionDetailsView != null && status.getExpiryDate() != null) {
                        String expiryDate = dateFormat.format(status.getExpiryDate());
                        subscriptionDetailsView.setText("Trial expires: " + expiryDate);
                    }
                }
            } else {
                subscriptionStatusView.setText("FREE");
                
                // Hide details container for free users
                if (subscriptionInfoContainer != null) {
                    subscriptionInfoContainer.setVisibility(View.GONE);
                }
            }
        }
    }
    
    /**
     * Handle error loading subscription status
     */
    private void handleError(Throwable error) {
        Log.e(TAG, "Error loading subscription status", error);
        showError("Error loading subscription status: " + error.getMessage());
    }
    
    /**
     * Process a purchase with the repository
     */
    private void processPurchaseWithRepository(Purchase purchase) {
        showLoading(true);
        
        // Extract product ID
        String productId = purchase.getProducts().isEmpty() ? "" : purchase.getProducts().get(0);
        
        // Create subscription status from purchase
        SubscriptionStatus status = new SubscriptionStatus();
        status.setUserId(repository.getUserId());
        status.setStatus("pro");
        status.setActive(true);
        status.setOrderId(purchase.getOrderId());
        status.setProvider("google_play");
        
        // Get current date for start date
        Date startDate = new Date(purchase.getPurchaseTime());
        status.setStartDate(startDate);
        
        // Set lifetime flag for lifetime purchase
        boolean isLifetime = SubscriptionManager.PRODUCT_ID_PRO_LIFETIME.equals(productId);
        status.setLifetime(isLifetime);
        
        // Set expiry date for non-lifetime purchases
        if (!isLifetime) {
            // For a real implementation, this would calculate based on subscription duration
            // For now, just add a year (or appropriate time) to the purchase date
            long durationMillis = 0;
            if (SubscriptionManager.PRODUCT_ID_PRO_MONTHLY.equals(productId)) {
                durationMillis = 30L * 24 * 60 * 60 * 1000; // 30 days
            } else if (SubscriptionManager.PRODUCT_ID_PRO_YEARLY.equals(productId)) {
                durationMillis = 365L * 24 * 60 * 60 * 1000; // 365 days
            }
            
            long expiryTimeMillis = purchase.getPurchaseTime() + durationMillis;
            Date expiryDate = new Date(expiryTimeMillis);
            status.setExpiryDate(expiryDate);
        }
        
        // Update subscription status in repository
        disposables.add(
            repository.updateSubscriptionStatus(status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        showContent();
                        Toast.makeText(this, "Thank you for your purchase!", Toast.LENGTH_LONG).show();
                    },
                    error -> {
                        Log.e(TAG, "Error updating subscription", error);
                        showError("Error updating subscription: " + error.getMessage());
                    }
                )
        );
    }
    
    /**
     * Show loading state
     */
    private void showLoading(boolean isLoading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        
        if (isLoading) {
            if (contentContainer != null) {
                contentContainer.setVisibility(View.GONE);
            }
            
            if (errorView != null) {
                errorView.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Show content state
     */
    private void showContent() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
        }
        
        if (contentContainer != null) {
            contentContainer.setVisibility(View.VISIBLE);
        }
        
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show error state
     */
    private void showError(String message) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
        }
        
        if (contentContainer != null) {
            contentContainer.setVisibility(View.GONE);
        }
        
        if (errorView != null) {
            errorView.setText(message);
            errorView.setVisibility(View.VISIBLE);
        } else {
            // If error view doesn't exist, show toast
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Handle purchase updates from the subscription manager
     */
    @Override
    public void onPurchasesUpdated(List<Purchase> purchases) {
        if (purchases != null && !purchases.isEmpty()) {
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // Process purchase with repository
                    processPurchaseWithRepository(purchase);
                    
                    // Also let the subscription manager handle it for backward compatibility
                    subscriptionManager.acknowledgePurchase(purchase);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start observing subscription status when activity is visible
        observeSubscriptionStatus();
        
        // Make sure subscription manager is ready
        subscriptionManager.queryPurchases();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Clear all subscriptions to prevent memory leaks
        disposables.clear();
    }
    
    @Override
    protected void onDestroy() {
        // Ensure all disposables are cleared
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
