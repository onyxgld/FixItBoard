package com.group.fixitboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);

        ImageButton btnBack = findViewById(R.id.btnBack);
        LinearLayout rowAdminProfile = findViewById(R.id.rowAdminProfile);
        LinearLayout rowAdminSecurity = findViewById(R.id.rowAdminSecurity);
        LinearLayout rowUserManagement = findViewById(R.id.rowUserManagement);
        LinearLayout rowNotificationSettings = findViewById(R.id.rowNotificationSettings);
        LinearLayout rowPropertyManagement = findViewById(R.id.rowPropertyManagement);
        LinearLayout rowAdminHelpSupport = findViewById(R.id.rowAdminHelpSupport);
        LinearLayout rowAdminPrivacyTerms = findViewById(R.id.rowAdminPrivacyTerms);
        Button btnAdminLogout = findViewById(R.id.btnAdminLogout);

        btnBack.setOnClickListener(v -> finish());
        rowAdminProfile.setOnClickListener(v -> finish());

        rowAdminSecurity.setOnClickListener(v -> showSecurityDialog());

        rowUserManagement.setOnClickListener(v ->
                startActivity(new Intent(this, AdminUserManagementActivity.class)));

        rowNotificationSettings.setOnClickListener(v -> showNotificationPreferencesDialog());

        rowPropertyManagement.setOnClickListener(v ->
                startActivity(new Intent(this, AdminPropertyManagementActivity.class)));

        rowAdminHelpSupport.setOnClickListener(v ->
                startActivity(new Intent(this, HelpSupportActivity.class)));

        rowAdminPrivacyTerms.setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class)));

        btnAdminLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log out")
                    .setMessage("Are you sure you want to log out of Admin Portal?")
                    .setPositiveButton("Log out", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(AdminSettingsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showSecurityDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = (user != null && user.getEmail() != null) ? user.getEmail() : "admin@fixitboard.com";

        new AlertDialog.Builder(this)
                .setTitle("Account & Security")
                .setMessage("Send a password reset email to " + email + "?\n\nTwo-Factor Authentication: Enabled")
                .setPositiveButton("Send Reset Link", (d, w) ->
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnSuccessListener(u -> Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Close", null)
                .show();
    }

    private void showNotificationPreferencesDialog() {
        String[] items = {
                "New Maintenance Requests",
                "Request Updates",
                "Urgent Request Alerts",
                "Completed Repairs",
                "New User Registration"
        };
        boolean[] checked = {true, true, true, true, true};

        new AlertDialog.Builder(this)
                .setTitle("Notification Settings")
                .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("Save Preferences", (d, w) ->
                        Toast.makeText(this, "Admin notification preferences saved!", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Cancel", null)
                .show();
    }
}