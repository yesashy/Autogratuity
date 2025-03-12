package com.autogratuity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView statusText;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI components
        statusText = findViewById(R.id.statusText);
        Button testAuthButton = findViewById(R.id.testAuthButton);
        Button testDbButton = findViewById(R.id.testDbButton);
        Button testWebViewButton = findViewById(R.id.testWebViewButton);
        Button launchAppButton = findViewById(R.id.launchAppButton);
        webView = findViewById(R.id.webView);

        // Set up WebView
        if (webView != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
        }

        // Test Auth Button
        if (testAuthButton != null) {
            testAuthButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    testAuthentication();
                }
            });
        }

        // Test Database Button
        if (testDbButton != null) {
            testDbButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    testDatabase();
                }
            });
        }

        // Test WebView Button
        if (testWebViewButton != null) {
            testWebViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    testWebView();
                }
            });
        }

        // Launch App Button
        if (launchAppButton != null) {
            launchAppButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(MainActivity.this, WebAppActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error launching WebAppActivity", e);
                        if (statusText != null) {
                            statusText.setText("Error launching app: " + e.getMessage());
                        }
                    }
                }
            });
        }

        // Request notification access
        requestNotificationAccess();
    }

    private void testAuthentication() {
        if (statusText != null) {
            statusText.setText("Testing Authentication...");
        }

        // Try to sign in anonymously for testing
        if (mAuth != null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful() && statusText != null) {
                                // Sign in success
                                Log.d(TAG, "Anonymous auth succeeded");
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    statusText.setText("Auth Test: SUCCESS\nUser ID: " + user.getUid());
                                } else {
                                    statusText.setText("Auth Test: SUCCESS\nUser ID: null");
                                }
                            } else if (statusText != null) {
                                // If sign in fails
                                Log.w(TAG, "Anonymous auth failed", task.getException());
                                String errorMsg = (task.getException() != null) ?
                                        task.getException().getMessage() : "Unknown error";
                                statusText.setText("Auth Test: FAILED\n" + errorMsg);
                            }
                        }
                    });
        }
    }

    private void testDatabase() {
        if (statusText != null) {
            statusText.setText("Testing Firestore...");
        }

        // Test reading from Firestore
        if (db != null) {
            db.collection("deliveries")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (statusText == null) return;

                            if (task.isSuccessful()) {
                                StringBuilder result = new StringBuilder("Firestore Test: SUCCESS\n");
                                result.append("Found ").append(task.getResult().size()).append(" documents:\n");

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String docId = document.getId() != null ? document.getId() : "null";
                                    Log.d(TAG, docId + " => " + document.getData());
                                    result.append("- ").append(docId).append("\n");
                                }

                                statusText.setText(result.toString());
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                                String errorMsg = (task.getException() != null) ?
                                        task.getException().getMessage() : "Unknown error";
                                statusText.setText("Firestore Test: FAILED\n" + errorMsg);
                            }
                        }
                    });
        }
    }

    private void testWebView() {
        if (statusText != null) {
            statusText.setText("Testing WebView...");
        }

        if (webView != null) {
            webView.setVisibility(View.VISIBLE);

            // Load a test HTML that verifies JavaScript is working
            String testHtml = "<html><body>"
                    + "<h2>WebView Test</h2>"
                    + "<p>If you can see this, WebView is working!</p>"
                    + "<p id='result'>JavaScript status: checking...</p>"
                    + "<script>"
                    + "document.getElementById('result').innerHTML = 'JavaScript status: ENABLED';"
                    + "</script>"
                    + "</body></html>";

            webView.loadData(testHtml, "text/html", "UTF-8");

            // Show success message
            Toast.makeText(this, "WebView loaded successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestNotificationAccess() {
        // Check if we already have permission
        if (statusText == null) return;

        String enabledNotificationListeners = android.provider.Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners");

        String packageName = getPackageName();
        boolean hasPermission = enabledNotificationListeners != null && packageName != null &&
                enabledNotificationListeners.contains(packageName);

        if (!hasPermission) {
            statusText.setText("Notification access required to capture tips automatically");

            // Show dialog to request permission
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Notification Access Required")
                    .setMessage("Autogratuity needs access to your notifications to automatically capture Shipt tips. Please enable notification access and select Autogratuity.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        // Open notification listener settings
                        try {
                            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        } catch (Exception e) {
                            Log.e(TAG, "Error opening notification settings", e);
                            Toast.makeText(MainActivity.this, "Error opening settings", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Later", null)
                    .show();
        } else {
            statusText.setText("Notification access granted. Autogratuity will capture Shipt tips automatically.");
        }
    }
}