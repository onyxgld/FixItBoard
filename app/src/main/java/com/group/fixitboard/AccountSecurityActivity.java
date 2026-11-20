package com.group.fixitboard;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AccountSecurityActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private TextView tv2faBadge;
    private Button btnUpdatePassword;
    private boolean is2faEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);

        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int bgSlate = ContextCompat.getColor(this, R.color.slate_bg);
        int cardBg = ContextCompat.getColor(this, R.color.white);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);
        int textGray = ContextCompat.getColor(this, R.color.text_gray);

        SharedPreferences prefs = getSharedPreferences("FixItBoardSecurityPrefs", MODE_PRIVATE);
        is2faEnabled = prefs.getBoolean("two_factor_enabled", true);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(bgSlate);
        scrollView.setFillViewport(true);
        scrollView.setFitsSystemWindows(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * d);
        root.setPadding(pad, pad, pad, pad);
        scrollView.addView(root);

        // Header Row (Back Button + Title)
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerLp.bottomMargin = (int) (22 * d);
        header.setLayoutParams(headerLp);

        ImageButton btnBack = new ImageButton(this);
        btnBack.setImageResource(android.R.drawable.ic_menu_revert);
        btnBack.setColorFilter(navy);
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setPadding(0, (int) (6 * d), (int) (14 * d), (int) (6 * d));
        btnBack.setOnClickListener(v -> finish());
        header.addView(btnBack);

        TextView tvHeader = new TextView(this);
        tvHeader.setText("Account & Security");
        tvHeader.setTextColor(navy);
        tvHeader.setTextSize(20f);
        tvHeader.setTypeface(null, Typeface.BOLD);
        header.addView(tvHeader);

        root.addView(header);

        // Section: Change Password
        TextView tvSection = new TextView(this);
        tvSection.setText("Change Password");
        tvSection.setTextColor(navy);
        tvSection.setTextSize(15f);
        tvSection.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams secLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        secLp.bottomMargin = (int) (14 * d);
        tvSection.setLayoutParams(secLp);
        root.addView(tvSection);

        etCurrentPassword = createPasswordField(root, "Current Password", "Enter current password", d, textPrimary, textGray, cardBg);
        etNewPassword = createPasswordField(root, "New Password", "Enter new password (min 6 chars)", d, textPrimary, textGray, cardBg);
        etConfirmPassword = createPasswordField(root, "Confirm New Password", "Re-enter new password", d, textPrimary, textGray, cardBg);

        // Two-Factor Authentication Row
        RelativeLayout tfaRow = new RelativeLayout(this);
        LinearLayout.LayoutParams tfaLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tfaLp.topMargin = (int) (10 * d);
        tfaLp.bottomMargin = (int) (22 * d);
        tfaRow.setLayoutParams(tfaLp);

        LinearLayout tfaTextCol = new LinearLayout(this);
        tfaTextCol.setOrientation(LinearLayout.VERTICAL);

        TextView tv2faTitle = new TextView(this);
        tv2faTitle.setText("Two-Factor Authentication");
        tv2faTitle.setTextColor(navy);
        tv2faTitle.setTextSize(14f);
        tv2faTitle.setTypeface(null, Typeface.BOLD);
        tfaTextCol.addView(tv2faTitle);

        TextView tv2faDesc = new TextView(this);
        tv2faDesc.setText("Adds an extra layer of protection");
        tv2faDesc.setTextColor(textGray);
        tv2faDesc.setTextSize(12f);
        tfaTextCol.addView(tv2faDesc);

        tfaRow.addView(tfaTextCol);

        tv2faBadge = new TextView(this);
        tv2faBadge.setTextSize(12f);
        tv2faBadge.setTypeface(null, Typeface.BOLD);
        tv2faBadge.setPadding((int) (12 * d), (int) (4 * d), (int) (12 * d), (int) (4 * d));
        RelativeLayout.LayoutParams badgeLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        badgeLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        badgeLp.addRule(RelativeLayout.CENTER_VERTICAL);
        tfaRow.addView(tv2faBadge, badgeLp);

        update2faBadgeStyle(d);
        tfaRow.setOnClickListener(v -> {
            is2faEnabled = !is2faEnabled;
            prefs.edit().putBoolean("two_factor_enabled", is2faEnabled).apply();
            update2faBadgeStyle(d);
            sync2faToFirestore(is2faEnabled);
            Toast.makeText(this, "Two-Factor Authentication " + (is2faEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        root.addView(tfaRow);

        // Update Password Button
        btnUpdatePassword = new Button(this);
        btnUpdatePassword.setText("Update Password");
        btnUpdatePassword.setAllCaps(false);
        btnUpdatePassword.setTextColor(ThemeHelper.isDarkMode(this) ? Color.parseColor("#0C1728") : Color.WHITE);
        btnUpdatePassword.setTextSize(15f);
        btnUpdatePassword.setTypeface(null, Typeface.BOLD);
        btnUpdatePassword.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (50 * d));
        btnUpdatePassword.setLayoutParams(btnLp);
        btnUpdatePassword.setOnClickListener(v -> handleUpdatePassword());
        root.addView(btnUpdatePassword);

        // Helper / Status Note
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String emailLabel = (user != null && user.getEmail() != null) ? user.getEmail() : "your account";
        TextView tvNote = new TextView(this);
        tvNote.setText("Signed in as " + emailLabel + ". Enter your current password to verify before saving changes.");
        tvNote.setTextColor(textGray);
        tvNote.setTextSize(11.5f);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noteLp.topMargin = (int) (14 * d);
        tvNote.setLayoutParams(noteLp);
        root.addView(tvNote);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int basePad = (int) (20 * getResources().getDisplayMetrics().density);
            v.setPadding(basePad + insets.left, basePad + insets.top, basePad + insets.right, basePad + insets.bottom);
            return windowInsets;
        });

        setContentView(scrollView);
    }

    private EditText createPasswordField(LinearLayout parent, String label, String hint, float d,
                                         int textPrimary, int textGray, int cardBg) {
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(textPrimary);
        tvLabel.setTextSize(13f);
        tvLabel.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lblLp.bottomMargin = (int) (6 * d);
        tvLabel.setLayoutParams(lblLp);
        parent.addView(tvLabel);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(10 * d);
        bg.setColor(cardBg);
        bg.setStroke((int) (1 * d), Color.parseColor("#CBD5E1"));
        box.setBackground(bg);
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        boxLp.bottomMargin = (int) (14 * d);
        box.setLayoutParams(boxLp);

        EditText et = new EditText(this);
        et.setHint(hint);
        et.setHintTextColor(textGray);
        et.setTextColor(textPrimary);
        et.setTextSize(14f);
        et.setBackgroundColor(Color.TRANSPARENT);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        et.setPadding((int) (14 * d), 0, (int) (8 * d), 0);
        et.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        box.addView(et);

        TextView tvToggle = new TextView(this);
        tvToggle.setText("👁");
        tvToggle.setTextSize(16f);
        tvToggle.setTextColor(textGray);
        tvToggle.setPadding((int) (10 * d), 0, (int) (14 * d), 0);
        final boolean[] visible = {false};
        tvToggle.setOnClickListener(v -> {
            visible[0] = !visible[0];
            if (visible[0]) {
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                tvToggle.setAlpha(1.0f);
            } else {
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                tvToggle.setAlpha(0.55f);
            }
            et.setSelection(et.getText().length());
        });
        tvToggle.setAlpha(0.55f);
        box.addView(tvToggle);

        parent.addView(box);
        return et;
    }

    private void update2faBadgeStyle(float d) {
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(8 * d);
        if (is2faEnabled) {
            tv2faBadge.setText("On");
            badgeBg.setColor(ContextCompat.getColor(this, R.color.status_completed_bg));
            tv2faBadge.setTextColor(ContextCompat.getColor(this, R.color.status_completed_text));
        } else {
            tv2faBadge.setText("Off");
            badgeBg.setColor(ContextCompat.getColor(this, R.color.status_pending_bg));
            tv2faBadge.setTextColor(ContextCompat.getColor(this, R.color.status_pending_text));
        }
        tv2faBadge.setBackground(badgeBg);
    }

    private void sync2faToFirestore(boolean enabled) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        Map<String, Object> update = new HashMap<>();
        update.put("twoFactorEnabled", enabled);
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .set(update, SetOptions.merge());
    }

    private void handleUpdatePassword() {
        String currentPass = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill in all password fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "New password and confirmation do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "No active signed-in account found.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdatePassword.setEnabled(false);
        btnUpdatePassword.setText("Updating...");

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
        user.reauthenticate(credential).addOnSuccessListener(unused -> {
            user.updatePassword(newPass).addOnSuccessListener(u -> {
                btnUpdatePassword.setEnabled(true);
                btnUpdatePassword.setText("Update Password");
                etCurrentPassword.setText("");
                etNewPassword.setText("");
                etConfirmPassword.setText("");
                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                finish();
            }).addOnFailureListener(e -> {
                btnUpdatePassword.setEnabled(true);
                btnUpdatePassword.setText("Update Password");
                Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {
            btnUpdatePassword.setEnabled(true);
            btnUpdatePassword.setText("Update Password");
            Toast.makeText(this, "Current password is incorrect.", Toast.LENGTH_LONG).show();
        });
    }
}