package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TenantLoginActivity extends AppCompatActivity {

    private boolean isLoginMode = true;

    private TextView tvHeaderTitle, tvTabLogin, tvTabRegister, tvForgotPassword, tvSwitchPrompt, tvSwitchAction;
    private View indicatorLogin, indicatorRegister;
    private EditText etFullName, etEmail, etPassword;
    private Button btnSubmitAuth;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        LinearLayout tabLogin = findViewById(R.id.tabLogin);
        LinearLayout tabRegister = findViewById(R.id.tabRegister);
        tvTabLogin = findViewById(R.id.tvTabLogin);
        tvTabRegister = findViewById(R.id.tvTabRegister);
        indicatorLogin = findViewById(R.id.indicatorLogin);
        indicatorRegister = findViewById(R.id.indicatorRegister);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnSubmitAuth = findViewById(R.id.btnSubmitAuth);
        Button btnGoogleAuth = findViewById(R.id.btnGoogleAuth);
        tvSwitchPrompt = findViewById(R.id.tvSwitchPrompt);
        tvSwitchAction = findViewById(R.id.tvSwitchAction);

        btnBack.setOnClickListener(v -> finish());

        tabLogin.setOnClickListener(v -> switchMode(true));
        tabRegister.setOnClickListener(v -> switchMode(false));
        tvSwitchAction.setOnClickListener(v -> switchMode(!isLoginMode));

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                etEmail.setError("Enter your email first to reset password");
                etEmail.requestFocus();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Password reset link sent to " + email, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnSubmitAuth.setOnClickListener(v -> handleAuthSubmit());

        btnGoogleAuth.setOnClickListener(v ->
                Toast.makeText(this, "Please sign in with your registered Tenant Email & Password.", Toast.LENGTH_SHORT).show());
    }

    private void switchMode(boolean loginMode) {
        isLoginMode = loginMode;

        int navyColor = ContextCompat.getColor(this, R.color.navy_primary);
        int grayColor = ContextCompat.getColor(this, R.color.text_gray);
        int slateLightColor = ContextCompat.getColor(this, R.color.slate_light);

        if (isLoginMode) {
            tvHeaderTitle.setText("Tenant Log In");

            tvTabLogin.setTextColor(navyColor);
            tvTabLogin.setTypeface(null, Typeface.BOLD);
            indicatorLogin.setBackgroundColor(navyColor);

            tvTabRegister.setTextColor(grayColor);
            tvTabRegister.setTypeface(null, Typeface.NORMAL);
            indicatorRegister.setBackgroundColor(slateLightColor);

            etFullName.setVisibility(View.GONE);
            tvForgotPassword.setVisibility(View.VISIBLE);
            btnSubmitAuth.setText("Log In");
            tvSwitchPrompt.setText("Don't have an account? ");
            tvSwitchAction.setText("Register");
        } else {
            tvHeaderTitle.setText("Tenant Register");

            tvTabRegister.setTextColor(navyColor);
            tvTabRegister.setTypeface(null, Typeface.BOLD);
            indicatorRegister.setBackgroundColor(navyColor);

            tvTabLogin.setTextColor(grayColor);
            tvTabLogin.setTypeface(null, Typeface.NORMAL);
            indicatorLogin.setBackgroundColor(slateLightColor);

            etFullName.setVisibility(View.VISIBLE);
            tvForgotPassword.setVisibility(View.GONE);
            btnSubmitAuth.setText("Register");
            tvSwitchPrompt.setText("Already have an account? ");
            tvSwitchAction.setText("Log In");
        }
    }

    private void handleAuthSubmit() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();

        if (!isLoginMode && fullName.isEmpty()) {
            etFullName.setError("Please enter your full name and unit");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (email.equalsIgnoreCase("admin@fixitboard.com")) {
            etEmail.setError("Please use the Admin Login portal for admin accounts");
            etEmail.requestFocus();
            return;
        }

        btnSubmitAuth.setEnabled(false);

        if (isLoginMode) {
            btnSubmitAuth.setText("Logging in...");
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        String resolvedName = resolveTenantDisplayName(user, email);
                        goToTenantDashboard(resolvedName);
                    })
                    .addOnFailureListener(e -> {
                        btnSubmitAuth.setEnabled(true);
                        btnSubmitAuth.setText("Log In");
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            btnSubmitAuth.setText("Creating account...");
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();
                            user.updateProfile(profileUpdates);

                            Map<String, Object> userDoc = new HashMap<>();
                            userDoc.put("uid", user.getUid());
                            userDoc.put("name", fullName);
                            userDoc.put("email", email);
                            userDoc.put("role", "tenant");

                            db.collection("users").document(user.getUid()).set(userDoc);
                        }
                        Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                        goToTenantDashboard(fullName);
                    })
                    .addOnFailureListener(e -> {
                        btnSubmitAuth.setEnabled(true);
                        btnSubmitAuth.setText("Register");
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    public static String resolveTenantDisplayName(FirebaseUser user, String email) {
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            return user.getDisplayName().trim();
        }
        if (email != null && email.toLowerCase().startsWith("unit3b@")) {
            return "Unit 3B";
        }
        if (email != null && email.contains("@")) {
            String prefix = email.split("@")[0];
            return prefix.substring(0, 1).toUpperCase() + prefix.substring(1);
        }
        return "Tenant";
    }

    private void goToTenantDashboard(String tenantName) {
        Intent intent = new Intent(TenantLoginActivity.this, TenantDashboardActivity.class);
        intent.putExtra("TENANT_NAME", tenantName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}