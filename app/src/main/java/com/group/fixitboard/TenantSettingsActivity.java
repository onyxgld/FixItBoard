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

public class TenantSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_settings);

        ImageButton btnBack = findViewById(R.id.btnBack);
        LinearLayout rowMyProfile = findViewById(R.id.rowMyProfile);
        LinearLayout rowAccountSecurity = findViewById(R.id.rowAccountSecurity);
        LinearLayout rowNotifications = findViewById(R.id.rowNotifications);
        LinearLayout rowAppPreferences = findViewById(R.id.rowAppPreferences);
        LinearLayout rowHelpSupport = findViewById(R.id.rowHelpSupport);
        LinearLayout rowPrivacyPolicy = findViewById(R.id.rowPrivacyPolicy);
        LinearLayout rowAbout = findViewById(R.id.rowAbout);
        Button btnSettingsLogout = findViewById(R.id.btnSettingsLogout);

        btnBack.setOnClickListener(v -> finish());

        rowMyProfile.setOnClickListener(v -> finish());

        rowAccountSecurity.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Account & Security")
                        .setMessage("Send a password reset link to " + user.getEmail() + "?")
                        .setPositiveButton("Send Reset Email", (d, w) ->
                                FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                                        .addOnSuccessListener(u -> Toast.makeText(this, "Reset email sent!", Toast.LENGTH_SHORT).show()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        rowNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsHistoryActivity.class)));

        rowAppPreferences.setOnClickListener(v ->
                Toast.makeText(this, "Push notifications & real-time sync are enabled", Toast.LENGTH_SHORT).show());

        rowHelpSupport.setOnClickListener(v ->
                startActivity(new Intent(this, HelpSupportActivity.class)));

        rowPrivacyPolicy.setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class)));

        rowAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        btnSettingsLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log out")
                    .setMessage("Are you sure you want to log out of FixItBoard?")
                    .setPositiveButton("Log out", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(TenantSettingsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}