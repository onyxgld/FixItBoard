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

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminEmail, etAdminPassword;
    private Button btnAdminLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        Button btnAdminGoogleLogin = findViewById(R.id.btnAdminGoogleLogin);

        btnBack.setOnClickListener(v -> finish());

        tvForgotPassword.setOnClickListener(v -> {
            String email = etAdminEmail.getText().toString().trim();
            if (email.isEmpty()) {
                etAdminEmail.setError("Enter admin email first");
                etAdminEmail.requestFocus();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Admin password reset link sent to " + email, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnAdminLogin.setOnClickListener(v -> handleAdminLogin());

        btnAdminGoogleLogin.setOnClickListener(v ->
                Toast.makeText(this, "Please sign in with your official Admin Email & Password.", Toast.LENGTH_SHORT).show());
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

        if (!email.toLowerCase().startsWith("admin")) {
            etAdminEmail.setError("Unauthorized: Only admin accounts can access this portal");
            etAdminEmail.requestFocus();
            return;
        }

        btnAdminLogin.setEnabled(false);
        btnAdminLogin.setText("Signing in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> goToAdminDashboard())
                .addOnFailureListener(e -> {
                    btnAdminLogin.setEnabled(true);
                    btnAdminLogin.setText("Log In as Admin");
                    Toast.makeText(this, "Admin login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void goToAdminDashboard() {
        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}