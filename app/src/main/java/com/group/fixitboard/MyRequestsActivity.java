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

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private static class RequestItem {
        String docId, id, title, problemType, location, description, date, status, tenantUnit, notes;

        RequestItem(String docId, String id, String title, String problemType, String location, String description, String date, String status, String tenantUnit, String notes) {
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
        }
    }

    private LinearLayout containerRequests;
    private TextView tabAll, tabPending, tabInProgress, tabDone;
    private final List<RequestItem> allRequests = new ArrayList<>();
    private String currentFilter = "All";

    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        containerRequests = findViewById(R.id.containerRequests);
        tabAll = findViewById(R.id.tabFilterAll);
        tabPending = findViewById(R.id.tabFilterPending);
        tabInProgress = findViewById(R.id.tabFilterInProgress);
        tabDone = findViewById(R.id.tabFilterDone);

        btnBack.setOnClickListener(v -> finish());

        tabAll.setOnClickListener(v -> applyFilter("All"));
        tabPending.setOnClickListener(v -> applyFilter("Pending"));
        tabInProgress.setOnClickListener(v -> applyFilter("In Progress"));
        tabDone.setOnClickListener(v -> applyFilter("Completed"));

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navRequests = findViewById(R.id.navRequests);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, TenantDashboardActivity.class));
            finish();
        });

        navRequests.setOnClickListener(v ->
                Toast.makeText(this, "You are on Requests", Toast.LENGTH_SHORT).show());

        navNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsHistoryActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, TenantProfileActivity.class));
            finish();
        });

        listenToMyRequests();
    }

    private void listenToMyRequests() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : "guest";

        firestoreListener = db.collection("requests")
                .whereEqualTo("tenantUid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        loadWithoutOrderBy(uid);
                        return;
                    }

                    parseAndDisplay(snapshots != null ? snapshots.getDocuments() : null);
                });
    }

    private void loadWithoutOrderBy(String uid) {
        db.collection("requests")
                .whereEqualTo("tenantUid", uid)
                .get()
                .addOnSuccessListener(snapshots -> parseAndDisplay(snapshots.getDocuments()))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading requests: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void parseAndDisplay(List<DocumentSnapshot> docs) {
        allRequests.clear();
        if (docs != null) {
            for (DocumentSnapshot doc : docs) {
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

                allRequests.add(new RequestItem(
                        docId, id, title, problemType, location, description, date, status, tenantUnit, notes
                ));
            }
        }
        applyFilter(currentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        updateTabStyle(tabAll, filter.equals("All"));
        updateTabStyle(tabPending, filter.equals("Pending"));
        updateTabStyle(tabInProgress, filter.equals("In Progress"));
        updateTabStyle(tabDone, filter.equals("Completed"));

        containerRequests.removeAllViews();

        int shown = 0;
        for (RequestItem item : allRequests) {
            if (filter.equals("All") || item.status.equals(filter)) {
                containerRequests.addView(createRequestCard(item));
                shown++;
            }
        }

        if (shown == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No maintenance requests yet.\nTap 'Submit Request' on Home to report an issue.");
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvEmpty.setTextSize(14f);
            tvEmpty.setGravity(Gravity.CENTER);
            int pad = (int) (32 * getResources().getDisplayMetrics().density);
            tvEmpty.setPadding(pad, pad, pad, pad);
            containerRequests.addView(tvEmpty);
        }
    }

    private void updateTabStyle(TextView tab, boolean active) {
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        if (active) {
            tab.setBackgroundColor(navy);
            tab.setTextColor(Color.WHITE);
            tab.setTypeface(null, Typeface.BOLD);
        } else {
            tab.setBackgroundResource(R.drawable.bg_input_outline);
            tab.setTextColor(navy);
            tab.setTypeface(null, Typeface.NORMAL);
        }
    }

    private CardView createRequestCard(RequestItem item) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int gray = ContextCompat.getColor(this, R.color.text_gray);

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = (int) (12 * d);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(12 * d);
        card.setCardElevation(2 * d);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * d);
        inner.setPadding(pad, pad, pad, pad);

        RelativeLayout topRow = new RelativeLayout(this);

        TextView tvId = new TextView(this);
        tvId.setText(item.id);
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
        if ("In Progress".equals(item.status)) {
            badgeBg.setColor(Color.parseColor("#FFF3E0"));
            tvBadge.setTextColor(Color.parseColor("#E65100"));
        } else if ("Pending".equals(item.status)) {
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
        tvDate.setText(item.date);
        tvDate.setTextColor(gray);
        tvDate.setTextSize(11f);
        inner.addView(tvDate);

        card.addView(inner);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(MyRequestsActivity.this, RequestDetailsActivity.class);
            intent.putExtra("IS_ADMIN", false);
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
}