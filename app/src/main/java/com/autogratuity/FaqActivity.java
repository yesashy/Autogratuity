package com.autogratuity;

import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.ui.common.RepositoryViewModelFactory;
import com.autogratuity.ui.faq.FaqViewModel;

/**
 * Activity to display the Knowledge Base / FAQ
 */
public class FaqActivity extends AppCompatActivity {
    
    private WebView webView;
    private ProgressBar progressBar;
    private FaqViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        
        // Initialize ViewModel
        RepositoryViewModelFactory factory = RepositoryViewModelFactory.fromRepositoryProvider();
        viewModel = new ViewModelProvider(this, factory).get(FaqViewModel.class);
        
        // Initialize views
        webView = findViewById(R.id.faq_webview);
        progressBar = findViewById(R.id.faq_progress_bar);
        
        // Configure WebView
        webView.getSettings().setJavaScriptEnabled(true); // Needed for collapsible sections
        
        // Set up observers
        setupObservers();
        
        // Load FAQ content and title
        viewModel.loadFaqTitle();
        viewModel.loadFaqContent();
        
        // Track this view
        viewModel.trackFaqView();
    }
    
    private void setupObservers() {
        // Observe loading state
        viewModel.isLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
        
        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error loading FAQ content: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe FAQ title
        viewModel.getFaqTitle().observe(this, title -> {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null && title != null) {
                actionBar.setTitle(title);
            }
        });
        
        // Observe FAQ content
        viewModel.getFaqContent().observe(this, content -> {
            if (content != null && !content.isEmpty()) {
                // Load custom content from repository
                loadHtmlContent(content);
            } else {
                // Load default content from assets
                webView.loadUrl("file:///android_asset/faq.html");
            }
        });
    }
    
    /**
     * Load HTML content into WebView
     * 
     * @param htmlContent HTML content to load
     */
    private void loadHtmlContent(String htmlContent) {
        String encodedHtml = Base64.encodeToString(htmlContent.getBytes(), Base64.NO_PADDING);
        webView.loadData(encodedHtml, "text/html", "base64");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Set up action bar with back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            
            // Use title from ViewModel if available
            String title = viewModel.getFaqTitle().getValue();
            if (title != null) {
                actionBar.setTitle(title);
            } else {
                actionBar.setTitle("Knowledge Base");
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle back button click
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
