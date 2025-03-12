package com.autogratuity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class WebAppActivity extends AppCompatActivity {
    private static final String TAG = "WebAppActivity";
    private WebView webView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webapp);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up WebView
        webView = findViewById(R.id.webAppView);
        if (webView != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);

            // Add JavaScript interface
            webView.addJavascriptInterface(new WebAppInterface(), "Android");

            // Load the web app
            // For testing, we'll load a simple HTML page
            String testHtml = "<!DOCTYPE html><html><head>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "<title>Autogratuity</title>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }"
                    + "h1 { color: #6200EE; }"
                    + ".card { background: #f9f9f9; border-radius: 8px; padding: 15px; margin-bottom: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }"
                    + "button { background: #6200EE; color: white; border: none; padding: 10px 15px; border-radius: 4px; margin-top: 10px; }"
                    + "</style>"
                    + "</head><body>"
                    + "<h1>Autogratuity</h1>"
                    + "<div class='card'>"
                    + "<h2>Welcome to Autogratuity</h2>"
                    + "<p>Your Shipt tip tracking app is running!</p>"
                    + "<p>This is a placeholder for the full web app interface.</p>"
                    + "<button onclick='Android.showToast(\"Button clicked!\")'>Test Android Bridge</button>"
                    + "</div>"
                    + "<div class='card'>"
                    + "<h3>Notification Status</h3>"
                    + "<p>Listening for Shipt notifications...</p>"
                    + "</div>"
                    + "</body></html>";

            webView.loadData(testHtml, "text/html", "UTF-8");
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
            if (mAuth.getCurrentUser() != null) {
                return mAuth.getCurrentUser().getUid();
            }
            return "Not logged in";
        }
    }
}