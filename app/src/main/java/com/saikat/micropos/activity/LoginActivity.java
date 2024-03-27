package com.saikat.micropos.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.saikat.micropos.R;

import java.util.Calendar;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private static final long FINGERPRINT_AUTH_TIMEOUT = 30 * 1000; // 30 secs in milliseconds

    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> loginUser());

        // Initialize executor
        executor = ContextCompat.getMainExecutor(this);

        // Initialize biometric prompt
        biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                loginUserWithEmailPassword();
            }
        });

        // Configure biometric prompt
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Log in using your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Always prompt for fingerprint authentication when the app is opened
        biometricPrompt.authenticate(promptInfo);
    }

    private boolean isFingerprintAuthRequired() {
        long lastLoginTimestamp = sharedPreferences.getLong("last_login_timestamp", 0);
        long currentTime = Calendar.getInstance().getTimeInMillis();
        return (currentTime - lastLoginTimestamp) >= FINGERPRINT_AUTH_TIMEOUT;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong("last_login_timestamp", Calendar.getInstance().getTimeInMillis());
                        editor.apply();
                        // Check if fingerprint authentication is required
                        if (isFingerprintAuthRequired()) {
                            biometricPrompt.authenticate(promptInfo);
                        } else {
                            loginUserWithEmailPassword();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUserWithEmailPassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Check if the user account is disabled
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Error occurred while reloading user data
                    Toast.makeText(LoginActivity.this, "Failed to check user account status.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
