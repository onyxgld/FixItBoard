package com.group.fixitboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private LinearLayout container;
    private String currentSubPage = "MENU";
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false)
                || (user != null && user.getEmail() != null && user.getEmail().toLowerCase().startsWith("admin"));

        container = findViewById(R.id.containerPrivacyPolicy);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            if (!"MENU".equals(currentSubPage)) {
                showMenu();
            } else {
                finish();
            }
        });

        showMenu();
    }

    private void showMenu() {
        currentSubPage = "MENU";
        container.removeAllViews();

        container.addView(buildHeaderCard(
                android.R.drawable.ic_secure,
                isAdmin ? "Privacy Policy & Admin Terms" : "Your Privacy Matters",
                isAdmin
                        ? "Data protection guidelines, terms of service, and administrator responsibilities."
                        : "Learn how Fixit Board collects, uses, and protects your personal information."
        ));

        container.addView(buildMenuRow("Information We Collect", v -> showInfoCollect()));
        container.addView(buildDivider());
        container.addView(buildMenuRow("How We Use Your Information", v -> showHowWeUse()));
        container.addView(buildDivider());
        container.addView(buildMenuRow("Data Protection", v -> showDataProtection()));
        container.addView(buildDivider());
        container.addView(buildMenuRow("Information Sharing", v -> showInfoSharing()));
        container.addView(buildDivider());
        container.addView(buildMenuRow("Your Privacy Rights", v -> showPrivacyRights()));
        container.addView(buildDivider());

        if (isAdmin) {
            container.addView(buildMenuRow("Terms of Service & Admin Responsibilities", v -> showAdminTerms()));
            container.addView(buildDivider());
        }
    }

    private void showInfoCollect() {
        currentSubPage = "COLLECT";
        container.removeAllViews();
        container.addView(buildHeaderCard(android.R.drawable.ic_secure, "Information We Collect", null));

        addSection("Personal Information", "• Name\n• Contact Information\n• Account Information");
        addSection("Property Information", "• Unit or room information\n• Maintenance request details\n• Photos or files attached to reports");
        addSection("Technical Information", "• Account activity\n• App usage information\n• Device information");
    }

    private void showHowWeUse() {
        currentSubPage = "USE";
        container.removeAllViews();
        container.addView(buildHeaderCard(android.R.drawable.ic_menu_preferences, "How we use your information", null));

        addSection("We use your information to:",
                "• Process and manage maintenance requests\n• Communicate with you about your requests\n• Provide customer support\n• Improve Fixit Board services\n• Comply with applicable requirements");
    }

    private void showDataProtection() {
        currentSubPage = "PROTECTION";
        container.removeAllViews();
        container.addView(buildHeaderCard(android.R.drawable.ic_lock_idle_lock, "Data Protection", null));

        addSection("Data protection",
                "We take reasonable measures to protect your information from unauthorized access, loss, misuse, or disclosure.");
        addSection("Your information is protected through:",
                "• Secure Firebase authentication\n• Appropriate data security measures\n• Limited access to personal information\n• Regular security monitoring");
    }

    private void showInfoSharing() {
        currentSubPage = "SHARING";
        container.removeAllViews();
        container.addView(buildHeaderCard(android.R.drawable.ic_menu_share, "Information Sharing", null));

        addSection("Information Sharing",
                "Fixit Board does not share your personal information unnecessarily.");
        addSection("Information may be shared with:",
                "• Property administration – to handle your request\n• Service providers – when needed to complete maintenance work\n• Legal authorities – when required by law");
    }

    private void showPrivacyRights() {
        currentSubPage = "RIGHTS";
        container.removeAllViews();
        container.addView(buildHeaderCard(android.R.drawable.ic_menu_info_details, "Your Privacy Rights", null));

        addSection("Depending on applicable privacy laws, you may have the right to:",
                "• Access your personal information\n• Correct inaccurate information\n• Request deletion of your information\n• Ask how your information is being used\n• Withdraw certain permissions or consent");
        addSection("Need help with your privacy?",
                "Contact the Fixit Board administrator or privacy contact.");
    }

    private void showAdminTerms() {
        currentSubPage = "ADMIN_TERMS";
        container.removeAllViews();
        container.addView(buildHeaderCard(android.R.drawable.ic_menu_agenda, "Terms of Service & Admin Responsibilities", null));

        addSection("Authorized Administrative Access",
                "Administrator accounts are granted elevated privileges solely for managing property units, tenant maintenance tickets, and official building communications.");
        addSection("Confidentiality & Data Handling",
                "• Do not export, share, or disclose tenant contact numbers, unit assignments, or maintenance records outside authorized property operations.\n• Ensure status updates and admin notes remain professional and accurate.");
        addSection("Account Security",
                "Administrators must safeguard login credentials, log out on shared devices, and promptly report any suspected unauthorized access.");
    }

    private CardView buildHeaderCard(int iconRes, String title, String subtitle) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = (int) (20 * d);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(12 * d);
        card.setCardElevation(2 * d);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setGravity(Gravity.CENTER);
        int pad = (int) (20 * d);
        inner.setPadding(pad, pad, pad, pad);

        ImageView iv = new ImageView(this);
        iv.setImageResource(iconRes);
        iv.setColorFilter(navy);
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams((int) (42 * d), (int) (42 * d));
        ivParams.bottomMargin = (int) (10 * d);
        iv.setLayoutParams(ivParams);
        inner.addView(iv);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        inner.addView(tvTitle);

        if (subtitle != null) {
            TextView tvSub = new TextView(this);
            tvSub.setText(subtitle);
            tvSub.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvSub.setTextSize(12f);
            tvSub.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            subParams.topMargin = (int) (4 * d);
            tvSub.setLayoutParams(subParams);
            inner.addView(tvSub);
        }

        card.addView(inner);
        return card;
    }

    private LinearLayout buildMenuRow(String label, View.OnClickListener listener) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (52 * d)));
        row.setOnClickListener(listener);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(navy);
        tvLabel.setTextSize(15f);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvLabel);

        TextView tvArrow = new TextView(this);
        tvArrow.setText("›");
        tvArrow.setTextColor(navy);
        tvArrow.setTextSize(20f);
        row.addView(tvArrow);

        return row;
    }

    private void addSection(String heading, String body) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int gray = ContextCompat.getColor(this, R.color.text_gray);

        TextView tvH = new TextView(this);
        tvH.setText(heading);
        tvH.setTextColor(navy);
        tvH.setTextSize(14f);
        tvH.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams hParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hParams.bottomMargin = (int) (4 * d);
        tvH.setLayoutParams(hParams);
        container.addView(tvH);

        TextView tvB = new TextView(this);
        tvB.setText(body);
        tvB.setTextColor(gray);
        tvB.setTextSize(13f);
        LinearLayout.LayoutParams bParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bParams.bottomMargin = (int) (14 * d);
        tvB.setLayoutParams(bParams);
        container.addView(tvB);

        container.addView(buildDivider());
    }

    private View buildDivider() {
        float d = getResources().getDisplayMetrics().density;
        View div = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * d));
        params.bottomMargin = (int) (14 * d);
        div.setLayoutParams(params);
        div.setBackgroundColor(ContextCompat.getColor(this, R.color.slate_light));
        return div;
    }
}