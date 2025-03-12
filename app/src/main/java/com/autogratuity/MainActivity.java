package com.autogratuity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.autogratuity.adapters.ViewPagerAdapter;
import com.autogratuity.fragments.AddressesFragment;
import com.autogratuity.fragments.DashboardFragment;
import com.autogratuity.fragments.DeliveriesFragment;
import com.autogratuity.fragments.MapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

// Import Firebase classes
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton quickSwitchButton;
    private ImageView syncIcon;
    private TextView syncText;
    private ImageButton refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Initialize views
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        quickSwitchButton = findViewById(R.id.quick_switch_button);
        syncIcon = findViewById(R.id.sync_icon);
        syncText = findViewById(R.id.sync_text);
        refreshButton = findViewById(R.id.refresh_button);

        // Set up ViewPager with Fragments
        setupViewPager();

        // Set up Quick Switch button
        quickSwitchButton.setOnClickListener(v -> {
            launchShiptApp();
        });

        // Set up Refresh button
        refreshButton.setOnClickListener(v -> {
            refreshData();
        });

        // Request notification access if needed
        requestNotificationAccessIfNeeded();
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.addFragment(DashboardFragment.newInstance(), "Dashboard");
        adapter.addFragment(DeliveriesFragment.newInstance(), "Deliveries");
        adapter.addFragment(AddressesFragment.newInstance(), "Addresses");
        adapter.addFragment(MapFragment.newInstance(), "Map");

        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(adapter.getPageTitle(position));
            // Set icons for tabs
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.ic_dashboard);
                    break;
                case 1:
                    tab.setIcon(R.drawable.ic_package);
                    break;
                case 2:
                    tab.setIcon(R.drawable.ic_home);
                    break;
                case 3:
                    tab.setIcon(R.drawable.ic_map);
                    break;
            }
        }).attach();
    }

    private void launchShiptApp() {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.shipt.shopper");
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, "Shipt app not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshData() {
        // Simulate refresh
        syncIcon.setImageResource(R.drawable.ic_refresh);
        syncText.setText("Refreshing...");

        // Perform actual data refresh here
        // For now, just simulate a delay
        syncText.postDelayed(() -> {
            syncIcon.setImageResource(R.drawable.ic_check);
            syncText.setText("Synced Just now");
        }, 1500);
    }

    private void requestNotificationAccessIfNeeded() {
        // Check if notification access is granted
        String enabledListeners = android.provider.Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners");

        if (enabledListeners == null || !enabledListeners.contains(getPackageName())) {
            // Show dialog to request notification access
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Notification Access Required")
                    .setMessage("Autogratuity needs access to your notifications to automatically capture Shipt tips.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    })
                    .setNegativeButton("Later", null)
                    .show();
        }
    }
}