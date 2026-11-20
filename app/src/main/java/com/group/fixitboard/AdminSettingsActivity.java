package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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

        rowAdminSecurity.setOnClickListener(v -> showSecurityDialog());

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
        row.setOnClickListener(v -> showThemeSelectorDialog());

        int insertIndex = parent.indexOfChild(anchorRow) + 1;
        parent.addView(divider, insertIndex);
        parent.addView(row, insertIndex + 1);
    }

    private void showThemeSelectorDialog() {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);
        int textGray = ContextCompat.getColor(this, R.color.text_gray);
        int cardBgColor = ContextCompat.getColor(this, R.color.white);
        boolean isCurrentlyDark = ThemeHelper.isDarkMode(this);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * d);
        container.setPadding(pad, (int) (12 * d), pad, pad);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("Choose your preferred display theme");
        tvSubtitle.setTextColor(textGray);
        tvSubtitle.setTextSize(13f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subLp.bottomMargin = (int) (14 * d);
        tvSubtitle.setLayoutParams(subLp);
        container.addView(tvSubtitle);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setWeightSum(2f);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("App Preferences")
                .setView(container)
                .setNegativeButton("Close", null)
                .create();

        LinearLayout lightCard = buildThemeOptionCard(
                "☀", "Light Mode", !isCurrentlyDark, d, navy, textPrimary, cardBgColor, true);
        lightCard.setOnClickListener(v -> {
            dialog.dismiss();
            if (isCurrentlyDark) {
                ThemeHelper.setDarkMode(this, false);
                recreate();
            }
        });

        LinearLayout darkCard = buildThemeOptionCard(
                "☾", "Dark Mode", isCurrentlyDark, d, navy, textPrimary, cardBgColor, false);
        darkCard.setOnClickListener(v -> {
            dialog.dismiss();
            if (!isCurrentlyDark) {
                ThemeHelper.setDarkMode(this, true);
                recreate();
            }
        });

        row.addView(lightCard);
        row.addView(darkCard);
        container.addView(row);

        dialog.show();
    }

    private LinearLayout buildThemeOptionCard(String icon, String label, boolean selected, float d,
                                              int navy, int textPrimary, int cardBgColor, boolean isLeft) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        int cardPad = (int) (18 * d);
        card.setPadding(cardPad, cardPad, cardPad, cardPad);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        if (isLeft) {
            lp.rightMargin = (int) (8 * d);
        } else {
            lp.leftMargin = (int) (8 * d);
        }
        card.setLayoutParams(lp);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(14 * d);
        bg.setColor(cardBgColor);
        bg.setStroke(selected ? (int) (2.5f * d) : (int) (1 * d),
                selected ? navy : Color.parseColor("#64748B"));
        card.setBackground(bg);

        TextView tvIcon = new TextView(this);
        tvIcon.setText(icon);
        tvIcon.setTextSize(26f);
        tvIcon.setTextColor(selected ? navy : textPrimary);
        tvIcon.setGravity(Gravity.CENTER);
        card.addView(tvIcon);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(13f);
        tvLabel.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        tvLabel.setTextColor(selected ? navy : textPrimary);
        tvLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lblLp.topMargin = (int) (8 * d);
        tvLabel.setLayoutParams(lblLp);
        card.addView(tvLabel);

        return card;
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