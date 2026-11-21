package com.group.fixitboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminEmail, etAdminPassword;
    private Button btnAdminLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnBack.setOnClickListener(v -> finish());

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra("PREFILL_EMAIL", etAdminEmail.getText().toString().trim());
            startActivity(intent);
        });

        btnAdminLogin.setOnClickListener(v -> handleAdminLogin());
    }

    private void handleAdminLogin() {
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etAdminEmail.setError("Please enter admin email");
            etAdminEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etAdminPassword.setError("Please enter admin password");
            etAdminPassword.requestFocus();
            return;
        }

        btnAdminLogin.setEnabled(false);
        btnAdminLogin.setText("Signing in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        resetLoginButton();
                        return;
                    }

                    boolean isDefaultAdminEmail = email.toLowerCase().startsWith("admin");

                    db.collection("users").document(user.getUid()).get()
                            .addOnSuccessListener(doc -> {
                                String role = doc.exists() ? doc.getString("role") : null;
                                if (isDefaultAdminEmail || "admin".equalsIgnoreCase(role)) {
                                    ensureAdminProfileDoc(user, email);
                                    goToAdminDashboard();
                                } else {
                                    mAuth.signOut();
                                    resetLoginButton();
                                    etAdminEmail.setError("Unauthorized: Only admin accounts can access this portal");
                                    etAdminEmail.requestFocus();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (isDefaultAdminEmail) {
                                    goToAdminDashboard();
                                } else {
                                    mAuth.signOut();
                                    resetLoginButton();
                                    Toast.makeText(this, "Failed to verify admin role: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    resetLoginButton();
                    Toast.makeText(this, "Admin login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void ensureAdminProfileDoc(FirebaseUser user, String email) {
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("uid", user.getUid());
        adminData.put("email", email);
        adminData.put("role", "admin");
        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            adminData.put("name", user.getDisplayName().trim());
        } else {
            adminData.put("name", "Property Admin");
        }
        db.collection("users").document(user.getUid()).set(adminData, SetOptions.merge());
    }

    private void resetLoginButton() {
        btnAdminLogin.setEnabled(true);
        btnAdminLogin.setText("Log In");
    }

    private void goToAdminDashboard() {
        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}