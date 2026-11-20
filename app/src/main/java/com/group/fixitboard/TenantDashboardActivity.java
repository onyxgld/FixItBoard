package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class TenantDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeTenant, tvCountPending, tvCountInProgress, tvCountCompleted;
    private TextView tvAnnouncementSnippet;
    private FirebaseFirestore db;
    private ListenerRegistration summaryListener;
    private ListenerRegistration profileListener;
    private ListenerRegistration announcementListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_dashboard);

        db = FirebaseFirestore.getInstance();

        tvWelcomeTenant = findViewById(R.id.tvWelcomeTenant);
        tvCountPending = findViewById(R.id.tvCountPending);
        tvCountInProgress = findViewById(R.id.tvCountInProgress);
        tvCountCompleted = findViewById(R.id.tvCountCompleted);

        tvCountPending.setText("0");
        tvCountInProgress.setText("0");
        tvCountCompleted.setText("0");

        String tenantName = getIntent().getStringExtra("TENANT_NAME");
        if (tenantName == null || tenantName.trim().isEmpty()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                tenantName = TenantLoginActivity.resolveTenantDisplayName(user, user.getEmail());
            }
        }
        if (tenantName != null && !tenantName.trim().isEmpty()) {
            tvWelcomeTenant.setText("Hello " + tenantName + "!");
        }

        injectAnnouncementBanner();

        CardView cardNewRequest = findViewById(R.id.cardNewRequest);
        CardView cardHouseRules = findViewById(R.id.cardHouseRules);

        cardNewRequest.setOnClickListener(v ->
                startActivity(new Intent(TenantDashboardActivity.this, SubmitRequestActivity.class)));

        cardHouseRules.setOnClickListener(v ->
                startActivity(new Intent(TenantDashboardActivity.this, HouseRulesActivity.class)));

        CardView cardRequestSummary = findViewById(R.id.cardRequestSummary);
        cardRequestSummary.setOnClickListener(v ->
                startActivity(new Intent(TenantDashboardActivity.this, MyRequestsActivity.class)));

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navRequests = findViewById(R.id.navRequests);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v ->
                Toast.makeText(this, "You are on Home", Toast.LENGTH_SHORT).show());

        navRequests.setOnClickListener(v ->
                startActivity(new Intent(TenantDashboardActivity.this, MyRequestsActivity.class)));

        navNotifications.setOnClickListener(v ->
                startActivity(new Intent(TenantDashboardActivity.this, NotificationsHistoryActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(TenantDashboardActivity.this, TenantProfileActivity.class)));

        listenToProfileGreeting();
        listenToRequestSummary();
        listenToAnnouncement();
    }

    private void injectAnnouncementBanner() {
        if (tvWelcomeTenant == null || !(tvWelcomeTenant.getParent() instanceof ViewGroup)) return;
        ViewGroup parent = (ViewGroup) tvWelcomeTenant.getParent();
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);

        CardView bannerCard = new CardView(this);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.topMargin = (int) (10 * d);
        cardLp.bottomMargin = (int) (14 * d);
        bannerCard.setLayoutParams(cardLp);
        bannerCard.setCardBackgroundColor(Color.parseColor("#EEF4FF"));
        bannerCard.setRadius(12 * d);
        bannerCard.setCardElevation(1 * d);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (14 * d);
        inner.setPadding(pad, pad, pad, pad);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Property Announcement  ›");
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(13f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        inner.addView(tvTitle);

        tvAnnouncementSnippet = new TextView(this);
        tvAnnouncementSnippet.setText("Please review the house rules and submit maintenance concerns through FixIt Board.");
        tvAnnouncementSnippet.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvAnnouncementSnippet.setTextSize(12f);
        tvAnnouncementSnippet.setMaxLines(2);
        LinearLayout.LayoutParams snipLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        snipLp.topMargin = (int) (4 * d);
        tvAnnouncementSnippet.setLayoutParams(snipLp);
        inner.addView(tvAnnouncementSnippet);

        bannerCard.addView(inner);
        bannerCard.setOnClickListener(v -> PropertyAnnouncementHelper.showAnnouncementDialog(this, false));

        int insertIndex = parent.indexOfChild(tvWelcomeTenant) + 1;
        parent.addView(bannerCard, insertIndex);
    }

    private void listenToAnnouncement() {
        announcementListener = db.collection("announcements").document("latest")
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null || !doc.exists() || tvAnnouncementSnippet == null) return;
                    String msg = doc.getString("message");
                    if (msg != null && !msg.trim().isEmpty()) {
                        tvAnnouncementSnippet.setText(msg.trim());
                    }
                });
    }

    private void listenToProfileGreeting() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        profileListener = db.collection("users").document(currentUser.getUid())
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null || !doc.exists()) return;

                    String name = doc.getString("name");
                    String unit = doc.getString("unit");

                    if (name != null && !name.trim().isEmpty()) {
                        String display = name.trim();
                        if (unit != null && !unit.trim().isEmpty()
                                && !"Unassigned".equalsIgnoreCase(unit.trim())
                                && !display.toLowerCase().contains(unit.trim().toLowerCase())) {
                            display += " - " + unit.trim();
                        }
                        tvWelcomeTenant.setText("Hello " + display + "!");
                    }
                });
    }

    private void listenToRequestSummary() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : "guest";

        summaryListener = db.collection("requests")
                .whereEqualTo("tenantUid", uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    int pending = 0, inProgress = 0, completed = 0;
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String status = doc.getString("status");
                        if ("Pending".equalsIgnoreCase(status)) {
                            pending++;
                        } else if ("In Progress".equalsIgnoreCase(status)) {
                            inProgress++;
                        } else if ("Completed".equalsIgnoreCase(status)) {
                            completed++;
                        }
                    }

                    tvCountPending.setText(String.valueOf(pending));
                    tvCountInProgress.setText(String.valueOf(inProgress));
                    tvCountCompleted.setText(String.valueOf(completed));
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (summaryListener != null) {
            summaryListener.remove();
        }
        if (profileListener != null) {
            profileListener.remove();
        }
        if (announcementListener != null) {
            announcementListener.remove();
        }
    }
}