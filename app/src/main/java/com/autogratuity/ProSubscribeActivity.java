package com.autogratuity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.autogratuity.data.model.SubscriptionStatus;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.ui.common.RepositoryViewModelFactory;
import com.autogratuity.ui.subscription.SubscriptionViewModel;
import com.autogratuity.utils.SubscriptionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for user to subscribe to Pro features.
 * Updated to use the MVVM pattern with SubscriptionViewModel.
 */
public class ProSubscribeActivity extends AppCompatActivity implements com.android.billingclient.api.PurchasesUpdatedListener {
    private static final String TAG = "ProSubscribeActivity";
    
    // ViewModel reference
    private SubscriptionViewModel viewModel;
    
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

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, 
                RepositoryViewModelFactory.fromRepositoryProvider())
                .get(SubscriptionViewModel.class);
        
        // Initialize subscription manager
        subscriptionManager = SubscriptionManager.getInstance(this,
                RepositoryProvider.getSubscriptionRepository(),
                RepositoryProvider.getPreferenceRepository());
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
        
        // Set up observers
        setupObservers();
        
        // Load current subscription status
        viewModel.loadSubscriptionStatus();
        viewModel.checkTrialAvailability();
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
            if (viewModel.isTrialAvailable().getValue() != null && 
                viewModel.isTrialAvailable().getValue()) {
                viewModel.startFreeTrial();
            } else {
                Toast.makeText(this, "You've already used your free trial", Toast.LENGTH_SHORT).show();
                trialButton.setEnabled(false);
            }
        });
    }
    
    /**
     * Set up observers for ViewModel LiveData
     */
    private void setupObservers() {
        // Observe subscription status
        viewModel.getSubscriptionStatus().observe(this, this::updateUI);
        
        // Observe trial availability
        viewModel.isTrialAvailable().observe(this, isAvailable -> {
            trialButton.setEnabled(isAvailable);
            trialButton.setAlpha(isAvailable ? 1.0f : 0.5f);
            
            if (!isAvailable) {
                trialButton.setText("Free Trial (Already Used)");
            }
        });
        
        // Observe loading state
        viewModel.isLoading().observe(this, this::showLoading);
        
        // Observe error state
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                showError(error.getMessage());
            } else {
                showContent();
            }
        });
        
        // Observe toast messages
        viewModel.getToastMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Update UI based on subscription status
     */
    private void updateUI(SubscriptionStatus status) {
        showContent();
        
        if (status == null) {
            return;
        }
        
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
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        if (purchases != null && !purchases.isEmpty()) {
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // Get product ID
                    String productId = purchase.getProducts().isEmpty() ? "" : purchase.getProducts().get(0);
                    
                    // Process the purchase with ViewModel
                    viewModel.processPurchase(purchase, productId);
                    
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
        viewModel.observeSubscriptionStatus();
        
        // Make sure subscription manager is ready
        subscriptionManager.queryPurchases();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
