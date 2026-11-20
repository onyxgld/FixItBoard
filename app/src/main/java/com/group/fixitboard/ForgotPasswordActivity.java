package com.group.fixitboard;

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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String prefillEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        String passed = getIntent().getStringExtra("PREFILL_EMAIL");
        if (passed != null) prefillEmail = passed;

        showRecoveryFormView();
    }

    private void showRecoveryFormView() {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int bgSlate = ContextCompat.getColor(this, R.color.slate_bg);
        int cardBg = ContextCompat.getColor(this, R.color.white);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);
        int textGray = ContextCompat.getColor(this, R.color.text_gray);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(bgSlate);
        scrollView.setFillViewport(true);
        scrollView.setFitsSystemWindows(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (22 * d);
        root.setPadding(pad, pad, pad, pad);
        scrollView.addView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerLp.bottomMargin = (int) (24 * d);
        header.setLayoutParams(headerLp);

        ImageButton btnBack = new ImageButton(this);
        btnBack.setImageResource(android.R.drawable.ic_menu_revert);
        btnBack.setColorFilter(navy);
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setPadding(0, (int) (6 * d), (int) (14 * d), (int) (6 * d));
        btnBack.setOnClickListener(v -> finish());
        header.addView(btnBack);

        TextView tvBrand = new TextView(this);
        tvBrand.setText("FIXIT BOARD");
        tvBrand.setTextColor(navy);
        tvBrand.setTextSize(18f);
        tvBrand.setTypeface(null, Typeface.BOLD);
        header.addView(tvBrand);
        root.addView(header);

        // Card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int cardPad = (int) (24 * d);
        card.setPadding(cardPad, cardPad, cardPad, cardPad);
        GradientDrawable cardDrawable = new GradientDrawable();
        cardDrawable.setCornerRadius(16 * d);
        cardDrawable.setColor(cardBg);
        cardDrawable.setStroke((int) (1 * d), ContextCompat.getColor(this, R.color.slate_light));
        card.setBackground(cardDrawable);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Forgot password?");
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(22f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        card.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText("We will send a secure password reset link to your email.");
        tvSub.setTextColor(textGray);
        tvSub.setTextSize(13.5f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = (int) (6 * d);
        subLp.bottomMargin = (int) (22 * d);
        tvSub.setLayoutParams(subLp);
        card.addView(tvSub);

        TextView tvEmailLabel = new TextView(this);
        tvEmailLabel.setText("Account email");
        tvEmailLabel.setTextColor(textPrimary);
        tvEmailLabel.setTextSize(13f);
        tvEmailLabel.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lblLp.bottomMargin = (int) (6 * d);
        tvEmailLabel.setLayoutParams(lblLp);
        card.addView(tvEmailLabel);

        EditText etResetEmail = new EditText(this);
        etResetEmail.setHint("Email");
        etResetEmail.setHintTextColor(textGray);
        etResetEmail.setTextColor(textPrimary);
        etResetEmail.setTextSize(14f);
        etResetEmail.setText(prefillEmail);
        etResetEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etResetEmail.setBackgroundResource(R.drawable.bg_input_outline);
        etResetEmail.setPadding((int) (14 * d), 0, (int) (14 * d), 0);
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        inputLp.bottomMargin = (int) (24 * d);
        etResetEmail.setLayoutParams(inputLp);
        card.addView(etResetEmail);

        Button btnSendLink = new Button(this);
        btnSendLink.setText("Send reset link");
        btnSendLink.setAllCaps(false);
        btnSendLink.setTextColor(ThemeHelper.isDarkMode(this) ? Color.parseColor("#0C1728") : Color.WHITE);
        btnSendLink.setTextSize(14.5f);
        btnSendLink.setTypeface(null, Typeface.BOLD);
        btnSendLink.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        LinearLayout.LayoutParams sendLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        sendLp.bottomMargin = (int) (12 * d);
        btnSendLink.setLayoutParams(sendLp);
        btnSendLink.setOnClickListener(v -> {
            String emailInput = etResetEmail.getText().toString().trim();
            if (emailInput.isEmpty()) {
                etResetEmail.setError("Please enter your account email");
                etResetEmail.requestFocus();
                return;
            }
            btnSendLink.setEnabled(false);
            btnSendLink.setText("Sending...");
            mAuth.sendPasswordResetEmail(emailInput)
                    .addOnSuccessListener(unused -> {
                        prefillEmail = emailInput;
                        showEmailSentView(emailInput);
                    })
                    .addOnFailureListener(e -> {
                        btnSendLink.setEnabled(true);
                        btnSendLink.setText("Send reset link");
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
        card.addView(btnSendLink);

        Button btnBackLogin = new Button(this);
        btnBackLogin.setText("Back to log in");
        btnBackLogin.setAllCaps(false);
        btnBackLogin.setTextColor(navy);
        btnBackLogin.setTextSize(14f);
        btnBackLogin.setTypeface(null, Typeface.BOLD);
        btnBackLogin.setBackgroundResource(R.drawable.bg_input_outline);
        btnBackLogin.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d)));
        btnBackLogin.setOnClickListener(v -> finish());
        card.addView(btnBackLogin);

        root.addView(card);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int basePad = (int) (22 * getResources().getDisplayMetrics().density);
            v.setPadding(basePad + insets.left, basePad + insets.top, basePad + insets.right, basePad + insets.bottom);
            return windowInsets;
        });

        setContentView(scrollView);
    }

    private void showEmailSentView(String sentEmail) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int bgSlate = ContextCompat.getColor(this, R.color.slate_bg);
        int cardBg = ContextCompat.getColor(this, R.color.white);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);
        int textGray = ContextCompat.getColor(this, R.color.text_gray);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(bgSlate);
        scrollView.setFillViewport(true);
        scrollView.setFitsSystemWindows(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (22 * d);
        root.setPadding(pad, pad, pad, pad);
        scrollView.addView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerLp.bottomMargin = (int) (24 * d);
        header.setLayoutParams(headerLp);

        ImageButton btnBack = new ImageButton(this);
        btnBack.setImageResource(android.R.drawable.ic_menu_revert);
        btnBack.setColorFilter(navy);
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setPadding(0, (int) (6 * d), (int) (14 * d), (int) (6 * d));
        btnBack.setOnClickListener(v -> finish());
        header.addView(btnBack);

        TextView tvBrand = new TextView(this);
        tvBrand.setText("FIXIT BOARD");
        tvBrand.setTextColor(navy);
        tvBrand.setTextSize(18f);
        tvBrand.setTypeface(null, Typeface.BOLD);
        header.addView(tvBrand);
        root.addView(header);

        // Card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int cardPad = (int) (24 * d);
        card.setPadding(cardPad, cardPad, cardPad, cardPad);
        GradientDrawable cardDrawable = new GradientDrawable();
        cardDrawable.setCornerRadius(16 * d);
        cardDrawable.setColor(cardBg);
        cardDrawable.setStroke((int) (1 * d), ContextCompat.getColor(this, R.color.slate_light));
        card.setBackground(cardDrawable);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Check your email");
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(22f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        card.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText("Your password reset link has been sent.");
        tvSub.setTextColor(textGray);
        tvSub.setTextSize(13.5f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = (int) (4 * d);
        subLp.bottomMargin = (int) (18 * d);
        tvSub.setLayoutParams(subLp);
        card.addView(tvSub);

        TextView tvEmailHdr = new TextView(this);
        tvEmailHdr.setText("Email");
        tvEmailHdr.setTextColor(navy);
        tvEmailHdr.setTextSize(14f);
        tvEmailHdr.setTypeface(null, Typeface.BOLD);
        card.addView(tvEmailHdr);

        TextView tvSentAddr = new TextView(this);
        tvSentAddr.setText(sentEmail + "\n\nOpen the reset link sent to your inbox (or spam folder) to set a new password.");
        tvSentAddr.setTextColor(textPrimary);
        tvSentAddr.setTextSize(13.5f);
        LinearLayout.LayoutParams addrLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        addrLp.topMargin = (int) (6 * d);
        addrLp.bottomMargin = (int) (24 * d);
        tvSentAddr.setLayoutParams(addrLp);
        card.addView(tvSentAddr);

        Button btnResend = new Button(this);
        btnResend.setText("Resend email");
        btnResend.setAllCaps(false);
        btnResend.setTextColor(ThemeHelper.isDarkMode(this) ? Color.parseColor("#0C1728") : Color.WHITE);
        btnResend.setTextSize(14.5f);
        btnResend.setTypeface(null, Typeface.BOLD);
        btnResend.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        LinearLayout.LayoutParams resLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        resLp.bottomMargin = (int) (10 * d);
        btnResend.setLayoutParams(resLp);
        btnResend.setOnClickListener(v -> {
            mAuth.sendPasswordResetEmail(sentEmail)
                    .addOnSuccessListener(u -> Toast.makeText(this, "Reset email resent to " + sentEmail, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        card.addView(btnResend);

        Button btnChangeEmail = new Button(this);
        btnChangeEmail.setText("Change email");
        btnChangeEmail.setAllCaps(false);
        btnChangeEmail.setTextColor(navy);
        btnChangeEmail.setTextSize(14f);
        btnChangeEmail.setTypeface(null, Typeface.BOLD);
        btnChangeEmail.setBackgroundResource(R.drawable.bg_input_outline);
        LinearLayout.LayoutParams chgLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d));
        chgLp.bottomMargin = (int) (10 * d);
        btnChangeEmail.setLayoutParams(chgLp);
        btnChangeEmail.setOnClickListener(v -> showRecoveryFormView());
        card.addView(btnChangeEmail);

        Button btnBackLogin = new Button(this);
        btnBackLogin.setText("Back to log in");
        btnBackLogin.setAllCaps(false);
        btnBackLogin.setTextColor(navy);
        btnBackLogin.setTextSize(14f);
        btnBackLogin.setTypeface(null, Typeface.BOLD);
        btnBackLogin.setBackgroundResource(R.drawable.bg_input_outline);
        btnBackLogin.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d)));
        btnBackLogin.setOnClickListener(v -> finish());
        card.addView(btnBackLogin);

        root.addView(card);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int basePad = (int) (22 * getResources().getDisplayMetrics().density);
            v.setPadding(basePad + insets.left, basePad + insets.top, basePad + insets.right, basePad + insets.bottom);
            return windowInsets;
        });

        setContentView(scrollView);
    }
}