package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private static class AdminRequestItem {
        String docId, id, title, problemType, location, description, date, status, tenantUnit, notes;
        long sortTime;

        AdminRequestItem(String docId, String id, String title, String problemType, String location,
                         String description, String date, String status, String tenantUnit, String notes, long sortTime) {
            this.docId = docId;
            this.id = id;
            this.title = title;
            this.problemType = problemType;
            this.location = location;
            this.description = description;
            this.date = date;
            this.status = status;
            this.tenantUnit = tenantUnit;
            this.notes = notes;
            this.sortTime = sortTime;
        }
    }

    private LinearLayout containerAdminRequests;
    private CardView announcementBannerCard;
    private TextView tabAll, tabPending, tabInProgress, tabDone;
    private TextView tvCountPending, tvCountInProgress, tvCountCompleted;
    private TextView tvAdminAnnouncementPreview;
    private String latestAnnouncementText = "Please review the house rules and submit maintenance concerns through FixIt Board.";
    private final List<AdminRequestItem> adminRequests = new ArrayList<>();
    private String currentFilter = "All";

    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener;
    private ListenerRegistration announcementListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

        ImageButton btnLogout = findViewById(R.id.btnAdminLogout);
        containerAdminRequests = findViewById(R.id.containerAdminRequests);
        tabAll = findViewById(R.id.tabAdminAll);
        tabPending = findViewById(R.id.tabAdminPending);
        tabInProgress = findViewById(R.id.tabAdminInProgress);
        tabDone = findViewById(R.id.tabAdminDone);
        tvCountPending = findViewById(R.id.tvCountPending);
        tvCountInProgress = findViewById(R.id.tvCountInProgress);
        tvCountCompleted = findViewById(R.id.tvCountCompleted);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showAdminLogoutDialog());
        }

        announcementBannerCard = buildAdminAnnouncementBanner();
        if (containerAdminRequests != null && announcementBannerCard != null) {
            containerAdminRequests.addView(announcementBannerCard, 0);
        }

        if (tabAll != null) tabAll.setOnClickListener(v -> applyFilter("All"));
        if (tabPending != null) tabPending.setOnClickListener(v -> applyFilter("Pending"));
        if (tabInProgress != null) tabInProgress.setOnClickListener(v -> applyFilter("In Progress"));
        if (tabDone != null) tabDone.setOnClickListener(v -> applyFilter("Completed"));

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navRequests = findViewById(R.id.navRequests);
        LinearLayout navHistory = findViewById(R.id.navHistory);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome != null) navHome.setOnClickListener(v -> applyFilter("All"));
        if (navRequests != null) navRequests.setOnClickListener(v -> applyFilter("Pending"));
        if (navHistory != null) navHistory.setOnClickListener(v -> applyFilter("Completed"));
        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(AdminDashboardActivity.this, AdminProfileActivity.class)));
        }

        listenToAnnouncement();
        listenToRequests();
    }

    private CardView buildAdminAnnouncementBanner() {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);

        CardView bannerCard = new CardView(this);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = (int) (14 * d);
        bannerCard.setLayoutParams(cardLp);
        bannerCard.setCardBackgroundColor(ThemeHelper.isDarkMode(this)
                ? Color.parseColor("#182842")
                : Color.parseColor("#EEF4FF"));
        bannerCard.setRadius(12 * d);
        bannerCard.setCardElevation(1 * d);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (14 * d);
        inner.setPadding(pad, pad, pad, pad);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Property Announcement (Tap to edit)  ›");
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(13f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        inner.addView(tvTitle);

        tvAdminAnnouncementPreview = new TextView(this);
        tvAdminAnnouncementPreview.setText(latestAnnouncementText);
        tvAdminAnnouncementPreview.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvAdminAnnouncementPreview.setTextSize(12f);
        tvAdminAnnouncementPreview.setMaxLines(2);
        LinearLayout.LayoutParams snipLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        snipLp.topMargin = (int) (4 * d);
        tvAdminAnnouncementPreview.setLayoutParams(snipLp);
        inner.addView(tvAdminAnnouncementPreview);

        bannerCard.addView(inner);
        bannerCard.setOnClickListener(v -> PropertyAnnouncementHelper.showAnnouncementDialog(this, true));

        return bannerCard;
    }

    private void listenToAnnouncement() {
        announcementListener = db.collection("announcements").document("latest")
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null || !doc.exists()) return;
                    String msg = doc.getString("message");
                    if (msg != null && !msg.trim().isEmpty()) {
                        latestAnnouncementText = msg.trim();
                        if (tvAdminAnnouncementPreview != null) {
                            tvAdminAnnouncementPreview.setText(latestAnnouncementText);
                        }
                    }
                });
    }

    private void listenToRequests() {
        firestoreListener = db.collection("requests")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    adminRequests.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String docId = doc.getId();
                            String id = doc.getString("id") != null ? doc.getString("id") : "#REQ-00000";
                            String title = doc.getString("title") != null ? doc.getString("title") : "Maintenance Request";
                            String problemType = doc.getString("problemType") != null ? doc.getString("problemType") : "General";
                            String location = doc.getString("location") != null ? doc.getString("location") : "";
                            String description = doc.getString("description") != null ? doc.getString("description") : "";
                            String date = doc.getString("date") != null ? doc.getString("date") : "";
                            String status = doc.getString("status") != null ? doc.getString("status") : "Pending";
                            String tenantUnit = doc.getString("tenantUnit") != null ? doc.getString("tenantUnit") : "Tenant";
                            String notes = doc.getString("notes") != null ? doc.getString("notes") : "";

                            long sortTime = 0L;
                            Object tsObj = doc.get("timestamp");
                            if (tsObj instanceof Timestamp) {
                                sortTime = ((Timestamp) tsObj).toDate().getTime();
                            }

                            adminRequests.add(new AdminRequestItem(
                                    docId, id, title, problemType, location, description, date, status, tenantUnit, notes, sortTime
                            ));
                        }
                    }

                    adminRequests.sort((a, b) -> {
                        if (b.sortTime != a.sortTime) {
                            return Long.compare(b.sortTime, a.sortTime);
                        }
                        return b.id.compareToIgnoreCase(a.id);
                    });

                    updateSummaryCounts();
                    applyFilter(currentFilter);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
        if (announcementListener != null) {
            announcementListener.remove();
        }
    }

    private void updateSummaryCounts() {
        int pending = 0, inProgress = 0, completed = 0;
        for (AdminRequestItem item : adminRequests) {
            if ("Pending".equalsIgnoreCase(item.status)) pending++;
            else if ("In Progress".equalsIgnoreCase(item.status)) inProgress++;
            else if ("Completed".equalsIgnoreCase(item.status)) completed++;
        }
        if (tvCountPending != null) tvCountPending.setText(String.valueOf(pending));
        if (tvCountInProgress != null) tvCountInProgress.setText(String.valueOf(inProgress));
        if (tvCountCompleted != null) tvCountCompleted.setText(String.valueOf(completed));
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        updateTabStyle(tabAll, filter.equals("All"));
        updateTabStyle(tabPending, filter.equals("Pending"));
        updateTabStyle(tabInProgress, filter.equals("In Progress"));
        updateTabStyle(tabDone, filter.equals("Completed"));

        if (containerAdminRequests == null) return;
        containerAdminRequests.removeAllViews();

        if (announcementBannerCard != null) {
            containerAdminRequests.addView(announcementBannerCard);
        }

        int shownCount = 0;
        for (AdminRequestItem item : adminRequests) {
            if (filter.equals("All") || item.status.equalsIgnoreCase(filter)) {
                containerAdminRequests.addView(createRequestCard(item));
                shownCount++;
            }
        }

        if (shownCount == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No maintenance requests found in this category.");
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvEmpty.setTextSize(14f);
            tvEmpty.setGravity(Gravity.CENTER);
            int pad = (int) (32 * getResources().getDisplayMetrics().density);
            tvEmpty.setPadding(pad, pad, pad, pad);
            containerAdminRequests.addView(tvEmpty);
        }
    }

    private void updateTabStyle(TextView tab, boolean active) {
        if (tab == null) return;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        if (active) {
            tab.setBackgroundColor(navy);
            tab.setTextColor(ThemeHelper.isDarkMode(this) ? Color.parseColor("#0C1728") : Color.WHITE);
            tab.setTypeface(null, Typeface.BOLD);
        } else {
            tab.setBackgroundResource(R.drawable.bg_input_outline);
            tab.setTextColor(navy);
            tab.setTypeface(null, Typeface.NORMAL);
        }
    }

    private CardView createRequestCard(AdminRequestItem item) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int gray = ContextCompat.getColor(this, R.color.text_gray);

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = (int) (12 * d);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
        card.setRadius(12 * d);
        card.setCardElevation(2 * d);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * d);
        inner.setPadding(pad, pad, pad, pad);

        RelativeLayout topRow = new RelativeLayout(this);

        TextView tvId = new TextView(this);
        tvId.setText(item.id + "  •  " + item.tenantUnit);
        tvId.setTextColor(navy);
        tvId.setTextSize(13f);
        tvId.setTypeface(null, Typeface.BOLD);
        topRow.addView(tvId);

        TextView tvBadge = new TextView(this);
        tvBadge.setText(item.status);
        tvBadge.setTextSize(11f);
        tvBadge.setTypeface(null, Typeface.BOLD);
        tvBadge.setPadding((int) (10 * d), (int) (3 * d), (int) (10 * d), (int) (3 * d));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(8 * d);
        if ("In Progress".equalsIgnoreCase(item.status)) {
            badgeBg.setColor(Color.parseColor("#FFF3E0"));
            tvBadge.setTextColor(Color.parseColor("#E65100"));
        } else if ("Pending".equalsIgnoreCase(item.status)) {
            badgeBg.setColor(Color.parseColor("#FFEBEE"));
            tvBadge.setTextColor(Color.parseColor("#C62828"));
        } else {
            badgeBg.setColor(Color.parseColor("#E8F5E9"));
            tvBadge.setTextColor(Color.parseColor("#2E7D32"));
        }
        tvBadge.setBackground(badgeBg);

        RelativeLayout.LayoutParams badgeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        badgeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        topRow.addView(tvBadge, badgeParams);

        inner.addView(topRow);

        RelativeLayout midRow = new RelativeLayout(this);
        LinearLayout.LayoutParams midParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        midParams.topMargin = (int) (8 * d);
        midParams.bottomMargin = (int) (8 * d);
        midRow.setLayoutParams(midParams);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(item.title);
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(14f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        midRow.addView(tvTitle);

        TextView tvArrow = new TextView(this);
        tvArrow.setText("›");
        tvArrow.setTextColor(navy);
        tvArrow.setTextSize(20f);
        tvArrow.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        arrowParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        midRow.addView(tvArrow, arrowParams);

        inner.addView(midRow);

        TextView tvDate = new TextView(this);
        tvDate.setText(item.date + " (" + item.problemType + ")");
        tvDate.setTextColor(gray);
        tvDate.setTextSize(11f);
        inner.addView(tvDate);

        card.addView(inner);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, RequestDetailsActivity.class);
            intent.putExtra("IS_ADMIN", true);
            intent.putExtra("DOC_ID", item.docId);
            intent.putExtra("REQ_ID", item.id);
            intent.putExtra("PROBLEM_TYPE", item.problemType);
            intent.putExtra("LOCATION", item.location);
            intent.putExtra("DESCRIPTION", item.description);
            intent.putExtra("STATUS", item.status);
            intent.putExtra("TENANT_UNIT", item.tenantUnit);
            intent.putExtra("DATE", item.date);
            intent.putExtra("NOTES", item.notes);
            startActivity(intent);
        });

        return card;
    }

    private void showAdminLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Admin Profile")
                .setMessage("Would you like to log out of Admin Dashboard?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
