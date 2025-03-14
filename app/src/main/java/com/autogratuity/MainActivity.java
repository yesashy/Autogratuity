package com.autogratuity;

import com.autogratuity.repositories.FirestoreRepository;
import com.autogratuity.repositories.IFirestoreRepository;
import android.os.Bundle;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;


import com.autogratuity.fragments.AddressesFragment;
import com.autogratuity.fragments.BulkUploadFragment;
import com.autogratuity.fragments.DashboardFragment;
import com.autogratuity.fragments.DeliveriesFragment;
import com.autogratuity.fragments.MapFragment;
import com.autogratuity.models.Delivery;
import com.autogratuity.services.DoNotDeliverService;
import com.autogratuity.services.NotificationPersistenceService;
import com.autogratuity.services.RobustShiptAccessibilityService;
import com.autogratuity.services.ShiptCaptureBackgroundService;
import com.autogratuity.utils.KmlImportUtil;
import com.autogratuity.utils.SubscriptionManager;
import com.autogratuity.utils.UsageTracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_KML_KMZ_FILE = 123;
    private static final int DO_NOT_DELIVER_JOB_ID = 1001;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FloatingActionButton quickSwitchButton;
    private ImageView syncIcon;
    private TextView syncText;
    private ImageButton refreshButton;
    private Button addDeliveryButton;
    private TextView navHeaderEmail;
    private TextView remainingMappingsText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private IFirestoreRepository repository; // Changed to interface

    // Subscription management
    private SubscriptionManager subscriptionManager;
    private UsageTracker usageTracker;
    private MenuItem proUpgradeMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        repository = FirestoreRepository.getInstance(); // Using the interface

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize subscription manager
        subscriptionManager = SubscriptionManager.getInstance(this);
        subscriptionManager.setStatusListener(new SubscriptionManager.SubscriptionStatusListener() {
            @Override
            public void onSubscriptionStatusChanged(String status) {
                // Update UI elements based on subscription status
                updateProFeaturesUI();
            }
        });

        // Initialize usage tracker
        usageTracker = UsageTracker.getInstance(this);
        usageTracker.setListener(new UsageTracker.UsageUpdateListener() {
            @Override
            public void onUsageUpdated(int mappingCount, int remainingMappings, boolean isPro) {
                updateRemainingMappingsUI(remainingMappings, isPro);
            }
        });

        // Load usage data
        usageTracker.loadUsageData(new UsageTracker.UsageDataCallback() {
            @Override
            public void onDataLoaded(int mappingCount, boolean isPro) {
                // Data loaded, UI will be updated via the listener
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading usage data", e);
            }
        });

        // Schedule the Do Not Deliver job
        scheduleDoNotDeliverJob();

        // Initialize views
        setupToolbar();
        setupNavigationDrawer();
        setupViewComponents();

        // Load default fragment on start
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, DashboardFragment.newInstance())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }

        // Set up Quick Switch button
        quickSwitchButton.setOnClickListener(v -> {
            // Add animation
            Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_360);
            quickSwitchButton.startAnimation(rotateAnim);

            // Try to launch Shipt app
            launchShiptApp();
        });

        // Set up Refresh button
        refreshButton.setOnClickListener(v -> {
            refreshData();
        });

        // Set up Add Delivery button
        addDeliveryButton.setOnClickListener(v -> {
            showAddDeliveryDialog();
        });

        // Set user email in nav header if logged in
        if (currentUser != null && currentUser.getEmail() != null) {
            navHeaderEmail.setText(currentUser.getEmail());
        }

        // Initialize all services based on user's subscription
        initializeServices();

        // Request battery optimization exemption for reliability
        requestBatteryOptimizationExemption();
    }
    
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Setup drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation item selection
        navigationView.setNavigationItemSelectedListener(this);

        // Set the header email text view
        View headerView = navigationView.getHeaderView(0);
        navHeaderEmail = headerView.findViewById(R.id.nav_header_email);
    }

    private void setupViewComponents() {
        // These views are inside the included layout
        View mainContent = findViewById(R.id.app_main_content);
        quickSwitchButton = mainContent.findViewById(R.id.quick_switch_button);
        syncIcon = mainContent.findViewById(R.id.sync_icon);
        syncText = mainContent.findViewById(R.id.sync_text);
        refreshButton = mainContent.findViewById(R.id.refresh_button);
        addDeliveryButton = mainContent.findViewById(R.id.add_delivery_button);

        // Find remaining mappings text if in the layout
        remainingMappingsText = mainContent.findViewById(R.id.remaining_mappings_text);
        if (remainingMappingsText != null) {
            // Will be updated when usage data loads
            remainingMappingsText.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Fragment fragment = null;
        String title = "";

        if (itemId == R.id.nav_dashboard) {
            fragment = DashboardFragment.newInstance();
            title = "Dashboard";
        } else if (itemId == R.id.nav_deliveries) {
            fragment = DeliveriesFragment.newInstance();
            title = "Deliveries";
        } else if (itemId == R.id.nav_addresses) {
            fragment = AddressesFragment.newInstance();
            title = "Addresses";
        } else if (itemId == R.id.nav_map) {
            fragment = MapFragment.newInstance();
            title = "Map";
        } else if (itemId == R.id.nav_bulk_upload) {
            fragment = BulkUploadFragment.newInstance();
            title = "Bulk Upload";
        } else if (itemId == R.id.nav_sign_out) {
            signOut();
            return true;
        } else if (itemId == R.id.nav_upgrade) {
            showSubscriptionOptions();
            return true;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    
    private void launchShiptApp() {
        // Try multiple potential package names for Shipt
        String[] packageNames = {"com.shipt.shopper", "com.shipt.user", "com.shipt"};
        boolean appFound = false;

        for (String packageName : packageNames) {
            try {
                // Check if the package is installed
                getPackageManager().getPackageInfo(packageName, 0);

                // Launch the app
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    appFound = true;
                    break;
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Package not found, try next one
            }
        }

        if (!appFound) {
            Toast.makeText(this, "Shipt app not installed", Toast.LENGTH_SHORT).show();

            // Optionally, open Play Store to install Shipt
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.shipt.shopper")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.shipt.shopper")));
            }
        }
    }

    private void refreshData() {
        // Show refresh indicator
        syncIcon.setImageResource(R.drawable.ic_refresh);
        syncText.setText("Refreshing...");

        // Perform actual data refresh here
        syncText.postDelayed(() -> {
            syncIcon.setImageResource(R.drawable.ic_check);
            syncText.setText("Synced Just now");

            // Refresh the current fragment if it's the dashboard
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof DashboardFragment) {
                ((DashboardFragment) currentFragment).refreshData();
            }
        }, 1500);
    }

    private void showAddDeliveryDialog() {
        // Check if user can add more mappings
        if (!usageTracker.canAddMapping() && !subscriptionManager.isProUser()) {
            // User has reached the free tier limit
            showFreeTierLimitReachedDialog();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_delivery);
        dialog.getWindow().setBackgroundDrawableResource(R.color.gray_900);

        // Get dialog views
        TextInputEditText orderIdInput = dialog.findViewById(R.id.order_id_input);
        TextInputEditText addressInput = dialog.findViewById(R.id.address_input);
        TextInputEditText tipAmountInput = dialog.findViewById(R.id.tip_amount_input);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button saveButton = dialog.findViewById(R.id.save_button);

        // Set up cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Set up save button
        saveButton.setOnClickListener(v -> {
            // Get input values
            String orderId = orderIdInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String tipAmountStr = tipAmountInput.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(orderId)) {
                orderIdInput.setError("Order ID is required");
                return;
            }

            if (TextUtils.isEmpty(address)) {
                addressInput.setError("Address is required");
                return;
            }

            // Create delivery
            double tipAmount = 0.0;
            if (!TextUtils.isEmpty(tipAmountStr)) {
                try {
                    tipAmount = Double.parseDouble(tipAmountStr);
                } catch (NumberFormatException e) {
                    tipAmountInput.setError("Invalid tip amount");
                    return;
                }
            }

            // Save to Firestore
            saveDelivery(orderId, address, tipAmount);

            dialog.dismiss();
        });

        dialog.show();
    }
    
    private void showFreeTierLimitReachedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Free Tier Limit Reached")
                .setMessage("You've reached the " + UsageTracker.FREE_TIER_MAPPING_LIMIT +
                        " delivery mapping limit for the free tier. Upgrade to Pro for unlimited " +
                        "mappings and automatic order capture!")
                .setPositiveButton("Upgrade to Pro", (dialog, which) -> {
                    showSubscriptionOptions();
                })
                .setNeutralButton("Create New Account", (dialog, which) -> {
                    // Sign out and redirect to login screen
                    signOut();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void saveDelivery(String orderId, String address, double tipAmount) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // Create delivery using new Delivery model
        Delivery delivery = new Delivery(orderId, address, Timestamp.now());
        delivery.setUserId(currentUser.getUid());
        delivery.setImportDate(Timestamp.now());

        if (tipAmount > 0) {
            delivery.setTipAmount(tipAmount);
            delivery.setTipDate(Timestamp.now());
        }

        // Save using repository through the interface
        repository.addDelivery(delivery)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MainActivity.this, "Delivery added successfully", Toast.LENGTH_SHORT).show();

                    // Record this mapping in usage tracker
                    usageTracker.recordMapping();

                    // Navigate to Deliveries fragment to show the new entry
                    Fragment deliveriesFragment = DeliveriesFragment.newInstance();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, deliveriesFragment)
                            .commit();
                    navigationView.setCheckedItem(R.id.nav_deliveries);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error adding delivery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateRemainingMappingsUI(int remainingMappings, boolean isPro) {
        if (remainingMappingsText == null) return;

        if (isPro) {
            remainingMappingsText.setText(R.string.pro_user);
            remainingMappingsText.setTextColor(getResources().getColor(R.color.green_700));
        } else {
            String message = String.format(getString(R.string.remaining_mappings), remainingMappings);
            remainingMappingsText.setText(message);

            // Change text color based on how many are left
            int colorRes;
            if (remainingMappings > 20) {
                colorRes = R.color.white;
            } else if (remainingMappings > 5) {
                colorRes = R.color.yellow_500;
            } else {
                colorRes = R.color.red_500;
            }

            remainingMappingsText.setTextColor(getResources().getColor(colorRes));
        }
    }
    
    private void initializeServices() {
        // Always start the notification listener service
        startNotificationListenerService();

        // Only start Pro features if user has access
        if (subscriptionManager.isProUser()) {
            startProFeatures();
        } else {
            // If not a Pro user, show a prompt about Pro features during first launch
            showProFeaturePromptIfNeeded();
        }
    }
    
    private void startNotificationListenerService() {
        // Request notification access if needed
        requestNotificationAccessIfNeeded();

        // Start the persistence service
        Intent persistenceIntent = new Intent(this, NotificationPersistenceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(persistenceIntent);
        } else {
            startService(persistenceIntent);
        }
    }
    
    private void requestNotificationAccessIfNeeded() {
        // Check if notification access is granted
        String enabledListeners = Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners");

        if (enabledListeners == null || !enabledListeners.contains(getPackageName())) {
            // Show dialog to request notification access
            new AlertDialog.Builder(this)
                    .setTitle("Notification Access Required")
                    .setMessage("Autogratuity needs access to your notifications to automatically capture Shipt tips.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    })
                    .setNegativeButton("Later", null)
                    .show();
        }
    }
    
    private void requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                // Show dialog asking user to disable battery optimization
                new AlertDialog.Builder(this)
                        .setTitle("Battery Optimization")
                        .setMessage("Autogratuity needs to be exempted from battery optimization to reliably capture Shipt notifications in the background. Would you like to disable battery optimization for this app?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            try {
                                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + packageName));
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.e(TAG, "Could not launch battery optimization settings", e);
                                Toast.makeText(this, "Please manually disable battery optimization for Autogratuity in your device settings", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            Toast.makeText(this, "Autogratuity might not reliably capture tip notifications in the background", Toast.LENGTH_LONG).show();
                        })
                        .show();
            }
        }
    }
    
    private void startProFeatures() {
        // Request accessibility permission if needed
        requestShiptAccessibilityServiceIfNeeded();

        // Start the background service for Shipt captures
        Intent captureIntent = new Intent(this, ShiptCaptureBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(captureIntent);
        } else {
            startService(captureIntent);
        }
    }
    
    private void requestShiptAccessibilityServiceIfNeeded() {
        boolean isEnabled = isAccessibilityServiceEnabled(RobustShiptAccessibilityService.class);

        if (!isEnabled) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.accessibility_permission_title)
                    .setMessage(R.string.accessibility_permission_message)
                    .setPositiveButton(R.string.accessibility_permission_button, (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }
    
    private boolean isAccessibilityServiceEnabled(Class<?> serviceClass) {
        String expectedServiceName = serviceClass.getName();
        String enabledServicesSetting = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabledServicesSetting);

        while (splitter.hasNext()) {
            String enabledService = splitter.next();

            if (enabledService.contains(expectedServiceName)) {
                return true;
            }
        }

        return false;
    }
    
    private void showProFeaturePromptIfNeeded() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean hasShownPromo = prefs.getBoolean("has_shown_pro_promo", false);

        if (!hasShownPromo) {
            // Show the dialog
            new AlertDialog.Builder(this)
                    .setTitle("Autogratuity Pro Features")
                    .setMessage("Upgrade to Pro for automatic order tracking! " +
                            "Pro users can automatically track Shipt deliveries without manual entry, " +
                            "building a comprehensive tip map with minimal effort.")
                    .setPositiveButton("Try Free for 7 Days", (dialog, which) -> {
                        // Start the trial
                        if (subscriptionManager.isTrialAvailable()) {
                            subscriptionManager.startFreeTrial();
                            startProFeatures();
                            Toast.makeText(this, "Your 7-day free trial has started!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "You've already used your free trial", Toast.LENGTH_SHORT).show();
                            showSubscriptionOptions();
                        }
                    })
                    .setNeutralButton("Learn More", (dialog, which) -> {
                        showProFeatureDetails();
                    })
                    .setNegativeButton("Not Now", null)
                    .show();

            // Mark as shown
            prefs.edit().putBoolean("has_shown_pro_promo", true).apply();
        }
    }
    
    private void showProFeatureDetails() {
        new AlertDialog.Builder(this)
                .setTitle("Pro Features")
                .setMessage("Autogratuity Pro includes:\n\n" +
                        "• Automatic order capture from Shipt\n" +
                        "• Zero manual entry required\n" +
                        "• Build your tip map passively while you work\n" +
                        "• Increase earnings by making data-driven decisions\n\n" +
                        "Available as a monthly subscription, yearly subscription (save 25%), " +
                        "or lifetime purchase.")
                .setPositiveButton("Subscribe Now", (dialog, which) -> {
                    showSubscriptionOptions();
                })
                .setNegativeButton("Maybe Later", null)
                .show();
    }
    
    private void showSubscriptionOptions() {
        final String[] options = {
                "Monthly: $3.99/month",
                "Yearly: $29.99/year (save 37%)",
                "Lifetime: $79.99 (never pay again)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Choose a Subscription")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Monthly
                            subscriptionManager.launchPurchaseFlow(this,
                                    SubscriptionManager.PRODUCT_ID_PRO_MONTHLY);
                            break;
                        case 1: // Yearly
                            subscriptionManager.launchPurchaseFlow(this,
                                    SubscriptionManager.PRODUCT_ID_PRO_YEARLY);
                            break;
                        case 2: // Lifetime
                            subscriptionManager.launchPurchaseFlow(this,
                                    SubscriptionManager.PRODUCT_ID_PRO_LIFETIME);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void updateProFeaturesUI() {
        boolean isPro = subscriptionManager.isProUser();

        // Update menu items if available
        if (proUpgradeMenuItem != null) {
            proUpgradeMenuItem.setVisible(!isPro);
        }

        // Update nav drawer items
        MenuItem navUpgradeItem = navigationView.getMenu().findItem(R.id.nav_upgrade);
        if (navUpgradeItem != null) {
            navUpgradeItem.setVisible(!isPro);
        }

        // If user just upgraded to Pro, start the Pro features
        if (isPro) {
            startProFeatures();
        }
    }
    
    private void scheduleDoNotDeliverJob() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        // Check if the job is already scheduled
        boolean jobScheduled = false;
        if (jobScheduler != null) {
            for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
                if (jobInfo.getId() == DO_NOT_DELIVER_JOB_ID) {
                    jobScheduled = true;
                    break;
                }
            }
        }

        // Only schedule if not already scheduled
        if (!jobScheduled && jobScheduler != null) {
            ComponentName componentName = new ComponentName(this, DoNotDeliverService.class);
            JobInfo jobInfo = new JobInfo.Builder(DO_NOT_DELIVER_JOB_ID, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Need internet for Firestore
                    .setPeriodic(24 * 60 * 60 * 1000) // Run once per day
                    .setPersisted(true) // Survive reboots
                    .build();

            int resultCode = jobScheduler.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "Do Not Deliver job scheduled successfully");
            } else {
                Log.e(TAG, "Failed to schedule Do Not Deliver job");
            }
        }
    }
    
    public void signOut() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
