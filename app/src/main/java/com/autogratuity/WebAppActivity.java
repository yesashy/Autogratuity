package com.autogratuity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.security.AuthenticationManager;
import com.autogratuity.ui.common.RepositoryViewModelFactory;
import com.autogratuity.ui.webapp.WebAppViewModel;

public class WebAppActivity extends AppCompatActivity {
    private static final String TAG = "WebAppActivity";
    private WebView webView;
    private WebAppViewModel viewModel;
    private AuthenticationManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webapp);

        // Initialize ViewModel
        RepositoryViewModelFactory factory = RepositoryViewModelFactory.fromRepositoryProvider();
        viewModel = new ViewModelProvider(this, factory).get(WebAppViewModel.class);
        
        // Initialize AuthenticationManager
        authManager = AuthenticationManager.getInstance(this);

        // Set up WebView
        webView = findViewById(R.id.webAppView);
        if (webView != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);

            // Add JavaScript interface
            webView.addJavascriptInterface(new WebAppInterface(), "Android");

            // Set up observers for LiveData from ViewModel
            viewModel.isAuthenticated().observe(this, isAuthenticated -> {
                // Update UI based on authentication status if needed
            });
            
            viewModel.getWebAppContent().observe(this, content -> {
                if (content != null && !content.isEmpty()) {
                    webView.loadData(content, "text/html", "UTF-8");
                }
            });
            
            viewModel.getError().observe(this, error -> {
                if (error != null) {
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
            // Load web app content from ViewModel
            viewModel.loadUserInfo();
            viewModel.loadWebAppContent();
        } else {
            Toast.makeText(this, "Error: WebView not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // JavaScript interface for communication between WebView and Android
    public class WebAppInterface {
        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(WebAppActivity.this, message, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public String getUserId() {
            if (authManager.isAuthenticated()) {
                return authManager.getCurrentUserId();
            }
            return "Not logged in";
        }
        
        @JavascriptInterface
        public void savePreference(String key, String value) {
            viewModel.savePreference(key, value);
        }
    }
}