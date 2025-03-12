package com.autogratuity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.autogratuity.utils.SubscriptionManager;

/**
 * Activity for user to subscribe to Pro features
 */
public class ProSubscribeActivity extends AppCompatActivity {
    private SubscriptionManager subscriptionManager;

    private Button monthlyButton;
    private Button yearlyButton;
    private Button lifetimeButton;
    private Button trialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_subscribe);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Upgrade to Pro");
        }

        // Initialize subscription manager
        subscriptionManager = SubscriptionManager.getInstance(this);

        // Initialize views
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
                subscriptionManager.startFreeTrial();
                Toast.makeText(this, "Your 7-day free trial has started!", Toast.LENGTH_LONG).show();
                finish(); // Return to main activity
            } else {
                Toast.makeText(this, "You've already used your free trial", Toast.LENGTH_SHORT).show();
                trialButton.setEnabled(false);
            }
        });

        // Update trial button visibility
        updateTrialButton();
    }

    /**
     * Update the trial button visibility based on availability
     */
    private void updateTrialButton() {
        boolean trialAvailable = subscriptionManager.isTrialAvailable();
        trialButton.setEnabled(trialAvailable);
        trialButton.setAlpha(trialAvailable ? 1.0f : 0.5f);

        if (!trialAvailable) {
            trialButton.setText("Free Trial (Already Used)");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}