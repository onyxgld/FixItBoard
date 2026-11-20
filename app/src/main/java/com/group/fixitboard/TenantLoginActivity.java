package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TenantLoginActivity extends AppCompatActivity {

    private boolean isLoginMode = true;

    private TextView tvHeaderTitle, tvTabLogin, tvTabRegister, tvForgotPassword, tvSwitchPrompt, tvSwitchAction;
    private View indicatorLogin, indicatorRegister;
    private EditText etFullName, etPropertyCode, etEmail, etPassword;
    private Button btnSubmitAuth;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
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

        injectPropertyCodeField();

        btnBack.setOnClickListener(v -> finish());

        tabLogin.setOnClickListener(v -> switchMode(true));
        tabRegister.setOnClickListener(v -> switchMode(false));
        tvSwitchAction.setOnClickListener(v -> switchMode(!isLoginMode));

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra("PREFILL_EMAIL", etEmail.getText().toString().trim());
            startActivity(intent);
        });

        btnSubmitAuth.setOnClickListener(v -> handleAuthSubmit());

        btnGoogleAuth.setOnClickListener(v ->
                Toast.makeText(this, "Please sign in with your registered Tenant Email & Password.", Toast.LENGTH_SHORT).show());
    }

    private void injectPropertyCodeField() {
        if (etEmail == null || !(etEmail.getParent() instanceof ViewGroup)) return;
        ViewGroup parent = (ViewGroup) etEmail.getParent();
        float d = getResources().getDisplayMetrics().density;

        etPropertyCode = new EditText(this);
        etPropertyCode.setHint("Property / invitation code (e.g., Unit 2A)");
        etPropertyCode.setHintTextColor(ContextCompat.getColor(this, R.color.text_gray));
        etPropertyCode.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        etPropertyCode.setTextSize(14f);
        etPropertyCode.setInputType(InputType.TYPE_CLASS_TEXT);
        etPropertyCode.setBackgroundResource(R.drawable.bg_input_outline);
        int padH = (int) (14 * d);
        etPropertyCode.setPadding(padH, 0, padH, 0);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        lp.bottomMargin = (int) (12 * d);
        etPropertyCode.setLayoutParams(lp);
        etPropertyCode.setVisibility(View.GONE);

        int emailIdx = parent.indexOfChild(etEmail);
        parent.addView(etPropertyCode, emailIdx + 1);
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
            if (etPropertyCode != null) etPropertyCode.setVisibility(View.GONE);
            tvForgotPassword.setVisibility(View.VISIBLE);
            btnSubmitAuth.setText("Log In");
            tvSwitchPrompt.setText("Don't have an account? ");
            tvSwitchAction.setText("Register");
        } else {
            tvHeaderTitle.setText("Create an account");

            tvTabRegister.setTextColor(navyColor);
            tvTabRegister.setTypeface(null, Typeface.BOLD);
            indicatorRegister.setBackgroundColor(navyColor);

            tvTabLogin.setTextColor(grayColor);
            tvTabLogin.setTypeface(null, Typeface.NORMAL);
            indicatorLogin.setBackgroundColor(slateLightColor);

            etFullName.setVisibility(View.VISIBLE);
            if (etPropertyCode != null) etPropertyCode.setVisibility(View.VISIBLE);
            tvForgotPassword.setVisibility(View.GONE);
            btnSubmitAuth.setText("Create account");
            tvSwitchPrompt.setText("Already have an account? ");
            tvSwitchAction.setText("Log in instead");
        }
    }

    private void handleAuthSubmit() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String propertyCode = etPropertyCode != null ? etPropertyCode.getText().toString().trim() : "";

        if (!isLoginMode && fullName.isEmpty()) {
            etFullName.setError("Please enter your full name");
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

        if (email.toLowerCase().startsWith("admin")) {
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
                        String assignedUnit = propertyCode.isEmpty() ? "Unassigned" : propertyCode;
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
                            userDoc.put("unit", assignedUnit);
                            userDoc.put("propertyCode", propertyCode);
                            userDoc.put("contact", "");

                            db.collection("users").document(user.getUid()).set(userDoc);
                        }
                        showRegistrationSuccessView(fullName, email, assignedUnit);
                    })
                    .addOnFailureListener(e -> {
                        btnSubmitAuth.setEnabled(true);
                        btnSubmitAuth.setText("Create account");
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void showRegistrationSuccessView(String fullName, String email, String unit) {
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

        // Top Bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams topLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topLp.bottomMargin = (int) (24 * d);
        topBar.setLayoutParams(topLp);

        TextView tvBrand = new TextView(this);
        tvBrand.setText("FIXIT BOARD");
        tvBrand.setTextColor(navy);
        tvBrand.setTextSize(18f);
        tvBrand.setTypeface(null, Typeface.BOLD);
        topBar.addView(tvBrand);
        root.addView(topBar);

        // Card Container
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
        tvTitle.setText("Account created");
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(22f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        card.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText("Your tenant account is ready.");
        tvSub.setTextColor(textGray);
        tvSub.setTextSize(13.5f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = (int) (4 * d);
        subLp.bottomMargin = (int) (20 * d);
        tvSub.setLayoutParams(subLp);
        card.addView(tvSub);

        TextView tvComplete = new TextView(this);
        tvComplete.setText("Registration complete");
        tvComplete.setTextColor(navy);
        tvComplete.setTextSize(15f);
        tvComplete.setTypeface(null, Typeface.BOLD);
        card.addView(tvComplete);

        TextView tvDetails = new TextView(this);
        tvDetails.setText("Welcome, " + fullName + " (" + email + ").\nAssigned Unit: " + unit + ".");
        tvDetails.setTextColor(textPrimary);
        tvDetails.setTextSize(13.5f);
        LinearLayout.LayoutParams detLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        detLp.topMargin = (int) (8 * d);
        detLp.bottomMargin = (int) (28 * d);
        tvDetails.setLayoutParams(detLp);
        card.addView(tvDetails);

        Button btnContinue = new Button(this);
        btnContinue.setText("Continue to dashboard");
        btnContinue.setAllCaps(false);
        btnContinue.setTextColor(ThemeHelper.isDarkMode(this) ? Color.parseColor("#0C1728") : Color.WHITE);
        btnContinue.setTextSize(14.5f);
        btnContinue.setTypeface(null, Typeface.BOLD);
        btnContinue.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        LinearLayout.LayoutParams contLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        contLp.bottomMargin = (int) (12 * d);
        btnContinue.setLayoutParams(contLp);
        btnContinue.setOnClickListener(v -> goToTenantDashboard(fullName));
        card.addView(btnContinue);

        Button btnBackLogin = new Button(this);
        btnBackLogin.setText("Back to log in");
        btnBackLogin.setAllCaps(false);
        btnBackLogin.setTextColor(navy);
        btnBackLogin.setTextSize(14f);
        btnBackLogin.setTypeface(null, Typeface.BOLD);
        btnBackLogin.setBackgroundResource(R.drawable.bg_input_outline);
        btnBackLogin.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d)));
        btnBackLogin.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            recreate();
        });
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