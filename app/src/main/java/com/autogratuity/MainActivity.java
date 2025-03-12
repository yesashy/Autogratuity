package com.autogratuity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.autogratuity.adapters.ViewPagerAdapter;
import com.autogratuity.fragments.AddressesFragment;
import com.autogratuity.fragments.DeliveriesFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        fabAddDelivery = findViewById(R.id.fab_add_delivery);

        // Set up FAB click listener
        fabAddDelivery.setOnClickListener(v -> {
            // Will implement add delivery later
            Toast.makeText(MainActivity.this, "Add delivery", Toast.LENGTH_SHORT).show();
        });

        // Set up ViewPager and TabLayout
        setupViewPager();

        // Request notification access
        requestNotificationAccess();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Not signed in, launch the sign in activity
            startLoginActivity();
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.addFragment(DeliveriesFragment.newInstance(), "Deliveries");
        adapter.addFragment(AddressesFragment.newInstance(), "Addresses");
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(adapter.getPageTitle(position));
        }).attach();
    }

    private void startLoginActivity() {
        // Will implement login activity later using FirebaseUI Auth
    }

    private void requestNotificationAccess() {
        String enabledListeners = Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners");

        boolean hasAccess = enabledListeners != null &&
                enabledListeners.contains(getPackageName());

        if (!hasAccess) {
            new AlertDialog.Builder(this)
                    .setTitle("Notification Access Required")
                    .setMessage("Autogratuity needs access to your notifications to capture Shipt tips automatically.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    })
                    .setNegativeButton("Later", null)
                    .show();
        }
    }
}