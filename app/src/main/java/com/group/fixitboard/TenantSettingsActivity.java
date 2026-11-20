package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
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

public class TenantSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
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

        rowAppPreferences.setOnClickListener(v -> showThemeSelectorDialog());

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

        // Light Mode Card
        LinearLayout lightCard = buildThemeOptionCard(
                "☀", "Light Mode", !isCurrentlyDark, d, navy, textPrimary, cardBgColor, true);
        lightCard.setOnClickListener(v -> {
            dialog.dismiss();
            if (isCurrentlyDark) {
                ThemeHelper.setDarkMode(this, false);
                recreate();
            }
        });

        // Dark Mode Card
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
}