package com.autogratuity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.ui.common.RepositoryViewModelFactory;
import com.autogratuity.ui.login.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        RepositoryViewModelFactory factory = RepositoryViewModelFactory.fromRepositoryProvider();
        viewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);

        // Initialize views
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);
        progressBar = findViewById(R.id.login_progress);

        // Set up observers
        setupObservers();

        // Set up login button
        loginButton.setOnClickListener(v -> {
            loginUser();
        });

        // Set up register link
        registerLink.setOnClickListener(v -> {
            // Show registration dialog
            showRegistrationDialog();
        });
    }

    private void setupObservers() {
        // Observe loading state
        viewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Observe toast messages
        viewModel.getToastMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe authentication state
        viewModel.isAuthenticated().observe(this, isAuthenticated -> {
            if (isAuthenticated) {
                startMainActivity();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly
        if (viewModel.checkAuthenticationState()) {
            // User is already signed in, go to main activity
            startMainActivity();
        }
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Login using ViewModel
        viewModel.login(email, password);
    }

    private void showRegistrationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Register New Account")
                .setView(R.layout.dialog_register)
                .setPositiveButton("Register", (dialog, which) -> {
                    // Get dialog views
                    androidx.appcompat.app.AlertDialog alertDialog = (androidx.appcompat.app.AlertDialog) dialog;
                    TextInputEditText regEmailInput = alertDialog.findViewById(R.id.reg_email_input);
                    TextInputEditText regPasswordInput = alertDialog.findViewById(R.id.reg_password_input);
                    TextInputEditText regConfirmPasswordInput = alertDialog.findViewById(R.id.reg_confirm_password_input);

                    // Get input values
                    String email = regEmailInput.getText().toString().trim();
                    String password = regPasswordInput.getText().toString().trim();
                    String confirmPassword = regConfirmPasswordInput.getText().toString().trim();

                    // Validate inputs
                    if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!password.equals(confirmPassword)) {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Register user using ViewModel
                    registerUser(email, password);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void registerUser(String email, String password) {
        // Register user using ViewModel
        viewModel.register(email, password);
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}