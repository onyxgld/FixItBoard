package com.group.fixitboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class TenantDashboardActivity extends AppCompatActivity {

    private TextView tvCountPending, tvCountInProgress, tvCountCompleted;
    private FirebaseFirestore db;
    private ListenerRegistration summaryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_dashboard);

        db = FirebaseFirestore.getInstance();

        TextView tvWelcomeTenant = findViewById(R.id.tvWelcomeTenant);
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

        listenToRequestSummary();
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
    }

    private void showProfileLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tenant Profile")
                .setMessage("Would you like to log out of FixItBoard?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(TenantDashboardActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}