package com.autogratuity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.data.model.SyncStatus;
import com.autogratuity.ui.address.AddressesFragment;
import com.autogratuity.ui.common.RepositoryViewModelFactory;
import com.autogratuity.ui.dashboard.DashboardFragment;
import com.autogratuity.ui.delivery.DeliveriesFragment;
import com.autogratuity.ui.main.MainViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Main activity for the Autogratuity application.
 * Serves as the primary UI container and navigation hub.
 * Updated to use MVVM pattern with MainViewModel.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // ViewModel reference
    private MainViewModel viewModel;

    // UI elements
    private ImageView syncIcon;
    private TextView syncText;
    private ImageButton refreshButton;
    private TextView remainingMappingsText;
    private MaterialButton addDeliveryButton;
    private FloatingActionButton quickSwitchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, 
                RepositoryViewModelFactory.fromRepositoryProvider())
                .get(MainViewModel.class);
        
        // Initialize UI elements
        initializeViews();
        
        // Set up observers for ViewModel LiveData
        setupObservers();
        
        // Set up listeners
        setupListeners();
        
        // Load default fragment if this is a fresh start
        if (savedInstanceState == null) {
            viewModel.setCurrentFragment("dashboard");
            loadFragmentByTag("dashboard");
        }
    }

    /**
     * Initialize UI element references
     */
    private void initializeViews() {
        // Sync status elements
        syncIcon = findViewById(R.id.sync_icon);
        syncText = findViewById(R.id.sync_text);
        refreshButton = findViewById(R.id.refresh_button);
        remainingMappingsText = findViewById(R.id.remaining_mappings_text);
        
        // Action buttons
        addDeliveryButton = findViewById(R.id.add_delivery_button);
        quickSwitchButton = findViewById(R.id.quick_switch_button);
    }
    
    /**
     * Set up observers for ViewModel LiveData
     */
    private void setupObservers() {
        // Observe sync status
        viewModel.getSyncStatus().observe(this, this::updateSyncStatusUI);
        
        // Observe formatted sync time
        viewModel.getFormattedSyncTime().observe(this, formattedTime -> {
            syncText.setText(formattedTime);
        });
        
        // Observe pending operations count
        viewModel.getPendingOperationsCount().observe(this, count -> {
            remainingMappingsText.setText(count + " remaining");
            remainingMappingsText.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        });
        
        // Observe current fragment
        viewModel.getCurrentFragment().observe(this, this::loadFragmentByTag);
        
        // Observe loading state
        viewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                syncIcon.setImageResource(R.drawable.ic_sync);
                syncText.setText("Syncing...");
            }
        });
        
        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Log.e(TAG, "Error from ViewModel", error);
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
     * Set up click listeners and other event handlers
     */
    private void setupListeners() {
        // Refresh button triggers sync
        refreshButton.setOnClickListener(v -> {
            viewModel.performSync();
        });
        
        // Add delivery button
        addDeliveryButton.setOnClickListener(v -> {
            // TODO: Replace with proper add delivery flow
            // Could navigate to an AddDeliveryFragment or start an AddDeliveryActivity
            Toast.makeText(this, "Add delivery functionality coming soon", Toast.LENGTH_SHORT).show();
        });
        
        // Quick switch button cycles through main fragments
        quickSwitchButton.setOnClickListener(v -> {
            viewModel.cycleMainFragments();
        });
    }
    
    /**
     * Updates the UI based on sync status
     */
    private void updateSyncStatusUI(SyncStatus status) {
        if (status == null) {
            syncIcon.setImageResource(R.drawable.ic_sync);
            return;
        }
        
        // Update sync icon based on status
        if (status.isInProgress()) {
            syncIcon.setImageResource(R.drawable.ic_sync);
        } else if (status.isError()) {
            syncIcon.setImageResource(R.drawable.ic_error);
        } else {
            syncIcon.setImageResource(R.drawable.ic_check);
        }
    }

    /**
     * Loads a fragment by its tag
     */
    private void loadFragmentByTag(String tag) {
        Fragment fragment;
        
        switch (tag) {
            case "dashboard":
                fragment = DashboardFragment.newInstance();
                break;
            case "deliveries":
                fragment = DeliveriesFragment.newInstance();
                break;
            case "addresses":
                fragment = AddressesFragment.newInstance();
                break;
            default:
                fragment = DashboardFragment.newInstance();
                tag = "dashboard";
                break;
        }
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.commit();
    }
    
    /**
     * Refreshes the current fragment by replacing it with a new instance
     */
    private void refreshCurrentFragment() {
        String currentFragmentTag = viewModel.getCurrentFragment().getValue();
        if (currentFragmentTag != null) {
            loadFragmentByTag(currentFragmentTag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        // Handle menu item selections
        if (id == R.id.action_upgrade_pro) {
            // Use ViewModel to check pro status before navigating
            viewModel.checkProStatus();
            viewModel.isProUser().observe(this, isPro -> {
                if (!isPro) {
                    startActivity(new Intent(this, ProSubscribeActivity.class));
                } else {
                    Toast.makeText(this, "You're already a Pro user!", Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        } else if (id == R.id.action_import_data) {
            // Handle data import (could be implemented in a separate method)
            Toast.makeText(this, "Import from Google Maps coming soon", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_sign_out) {
            // Handle sign out (should clear user data and navigate to login)
            handleSignOut();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Handles user sign out
     */
    private void handleSignOut() {
        // Perform sign out logic
        Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
        
        // Clear user data and navigate to login screen
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start observing data when the activity is visible
        viewModel.observeSyncStatus();
        viewModel.getPendingSyncOperations();
    }
}
