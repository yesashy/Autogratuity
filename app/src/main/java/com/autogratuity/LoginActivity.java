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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);
        progressBar = findViewById(R.id.login_progress);

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

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
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

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        startMainActivity();
                    } else {
                        // If sign in fails, display a message to the user
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
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

                    // Register user
                    registerUser(email, password);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void registerUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Registration successful
                        Toast.makeText(LoginActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        // If registration fails, display a message to the user
                        Toast.makeText(LoginActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}