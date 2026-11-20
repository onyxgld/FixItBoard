package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AdminSettingsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private boolean initialDarkState;
    private final boolean[] notifStates = {true, true, true, true, true};
    private final String[] notifKeys = {
            "newMaintenanceRequests",
            "requestUpdates",
            "urgentRequestAlerts",
            "completedRepairs",
            "newUserRegistration"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);

        initialDarkState = ThemeHelper.isDarkMode(this);
        db = FirebaseFirestore.getInstance();
        loadAdminNotificationPreferences();

        ImageButton btnBack = findViewById(R.id.btnBack);
        LinearLayout rowAdminProfile = findViewById(R.id.rowAdminProfile);
        LinearLayout rowAdminSecurity = findViewById(R.id.rowAdminSecurity);
        LinearLayout rowUserManagement = findViewById(R.id.rowUserManagement);
        LinearLayout rowNotificationSettings = findViewById(R.id.rowNotificationSettings);
        LinearLayout rowPropertyManagement = findViewById(R.id.rowPropertyManagement);
        LinearLayout rowAdminHelpSupport = findViewById(R.id.rowAdminHelpSupport);
        LinearLayout rowAdminPrivacyTerms = findViewById(R.id.rowAdminPrivacyTerms);
        Button btnAdminLogout = findViewById(R.id.btnAdminLogout);

        injectAppPreferencesRow(rowPropertyManagement);

        btnBack.setOnClickListener(v -> finish());
        rowAdminProfile.setOnClickListener(v -> finish());

        rowAdminSecurity.setOnClickListener(v ->
                startActivity(new Intent(this, AccountSecurityActivity.class)));

        rowUserManagement.setOnClickListener(v ->
                startActivity(new Intent(this, AdminUserManagementActivity.class)));

        rowNotificationSettings.setOnClickListener(v -> showNotificationPreferencesDialog());

        rowPropertyManagement.setOnClickListener(v ->
                startActivity(new Intent(this, AdminPropertyManagementActivity.class)));

        rowAdminHelpSupport.setOnClickListener(v -> {
            Intent intent = new Intent(this, HelpSupportActivity.class);
            intent.putExtra("IS_ADMIN", true);
            startActivity(intent);
        });

        rowAdminPrivacyTerms.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            intent.putExtra("IS_ADMIN", true);
            startActivity(intent);
        });

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

    @Override
    protected void onResume() {
        super.onResume();
        if (initialDarkState != ThemeHelper.isDarkMode(this)) {
            recreate();
        }
    }

    private void injectAppPreferencesRow(LinearLayout anchorRow) {
        if (anchorRow == null || !(anchorRow.getParent() instanceof ViewGroup)) return;
        ViewGroup parent = (ViewGroup) anchorRow.getParent();
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);
        int textGray = ContextCompat.getColor(this, R.color.text_gray);

        View divider = new View(this);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * d));
        divider.setLayoutParams(divLp);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.slate_light));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        int padV = (int) (14 * d);
        int padH = (int) (16 * d);
        row.setPadding(padH, padV, padH, padV);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvTitle = new TextView(this);
        tvTitle.setText("App Preferences");
        tvTitle.setTextColor(textPrimary);
        tvTitle.setTextSize(14f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        textCol.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText(ThemeHelper.isDarkMode(this) ? "Dark Mode active" : "Light Mode active");
        tvSub.setTextColor(textGray);
        tvSub.setTextSize(12f);
        textCol.addView(tvSub);

        TextView tvChevron = new TextView(this);
        tvChevron.setText("›");
        tvChevron.setTextColor(navy);
        tvChevron.setTextSize(20f);

        row.addView(textCol);
        row.addView(tvChevron);
        row.setOnClickListener(v ->
                startActivity(new Intent(this, AppPreferencesActivity.class)));

        int insertIndex = parent.indexOfChild(anchorRow) + 1;
        parent.addView(divider, insertIndex);
        parent.addView(row, insertIndex + 1);
    }

    @SuppressWarnings("unchecked")
    private void loadAdminNotificationPreferences() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;
            Object prefsObj = doc.get("notifPreferences");
            if (prefsObj instanceof Map) {
                Map<String, Object> prefs = (Map<String, Object>) prefsObj;
                for (int i = 0; i < notifKeys.length; i++) {
                    Object val = prefs.get(notifKeys[i]);
                    if (val instanceof Boolean) {
                        notifStates[i] = (Boolean) val;
                    }
                }
            }
        });
    }

    private void showNotificationPreferencesDialog() {
        String[] items = {
                "New Maintenance Requests",
                "Request Updates",
                "Urgent Request Alerts",
                "Completed Repairs",
                "New User Registration"
        };

        new AlertDialog.Builder(this)
                .setTitle("Notification Settings")
                .setMultiChoiceItems(items, notifStates, (dialog, which, isChecked) -> notifStates[which] = isChecked)
                .setPositiveButton("Save Preferences", (d, w) -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        Map<String, Object> prefs = new HashMap<>();
                        for (int i = 0; i < notifKeys.length; i++) {
                            prefs.put(notifKeys[i], notifStates[i]);
                        }
                        Map<String, Object> update = new HashMap<>();
                        update.put("notifPreferences", prefs);

                        db.collection("users").document(user.getUid())
                                .set(update, SetOptions.merge())
                                .addOnSuccessListener(u ->
                                        Toast.makeText(this, "Notification preferences synced to cloud!", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Admin notification preferences saved!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}