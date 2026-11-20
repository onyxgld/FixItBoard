package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RequestDetailsActivity extends AppCompatActivity {

    private TextView tvStatusBadge;
    private Spinner spinnerStatus;
    private EditText etAdminNotes;
    private Button btnSave;
    private boolean isAdmin = false;
    private String docId;
    private String reqId;
    private String problemType;
    private String location;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvReqId = findViewById(R.id.tvDetailReqId);
        tvStatusBadge = findViewById(R.id.tvDetailStatusBadge);
        TextView tvProblemType = findViewById(R.id.tvDetailProblemType);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvTenantUnit = findViewById(R.id.tvDetailTenantUnit);
        TextView tvSubmittedOn = findViewById(R.id.tvDetailSubmittedOn);
        LinearLayout layoutAdminControls = findViewById(R.id.layoutAdminControls);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        etAdminNotes = findViewById(R.id.etAdminNotes);
        btnSave = findViewById(R.id.btnSaveDetails);
        TextView tvNavThirdLabel = findViewById(R.id.tvNavThirdLabel);

        btnBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        isAdmin = intent.getBooleanExtra("IS_ADMIN", false);
        docId = intent.getStringExtra("DOC_ID");
        reqId = intent.getStringExtra("REQ_ID");
        problemType = intent.getStringExtra("PROBLEM_TYPE");
        location = intent.getStringExtra("LOCATION");
        String description = intent.getStringExtra("DESCRIPTION");
        String status = intent.getStringExtra("STATUS");
        String tenantUnit = intent.getStringExtra("TENANT_UNIT");
        String date = intent.getStringExtra("DATE");
        String notes = intent.getStringExtra("NOTES");

        if (reqId != null) tvReqId.setText(reqId);
        if (problemType != null) tvProblemType.setText(problemType);
        if (location != null) tvLocation.setText(location);
        if (description != null) tvDescription.setText(description);
        if (tenantUnit != null) tvTenantUnit.setText(tenantUnit);
        if (date != null) tvSubmittedOn.setText(date);
        if (notes != null) etAdminNotes.setText(notes);

        String currentStatus = status != null ? status : "Pending";
        updateStatusBadge(currentStatus);

        String[] statuses = {"Pending", "In Progress", "Completed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(adapter);

        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(currentStatus)) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        if (isAdmin) {
            layoutAdminControls.setVisibility(View.VISIBLE);
            tvNavThirdLabel.setText("History");
            btnSave.setText("Save Changes");
        } else {
            layoutAdminControls.setVisibility(View.GONE);
            tvNavThirdLabel.setText("Notification");
            btnSave.setText("Done");
        }

        btnSave.setOnClickListener(v -> {
            if (!isAdmin) {
                finish();
                return;
            }

            String selectedStatus = spinnerStatus.getSelectedItem().toString();
            String updatedNotes = etAdminNotes.getText().toString().trim();

            if (docId == null || docId.isEmpty()) {
                Toast.makeText(this, "Request updated to " + selectedStatus, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            btnSave.setEnabled(false);
            btnSave.setText("Saving...");

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", selectedStatus);
            updates.put("notes", updatedNotes);

            db.collection("requests").document(docId)
                    .update(updates)
                    .addOnSuccessListener(unused -> {
                        createTenantNotification(selectedStatus, updatedNotes);
                        updateStatusBadge(selectedStatus);
                        Toast.makeText(this, "Request updated to " + selectedStatus, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save Changes");
                        Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navRequests = findViewById(R.id.navRequests);
        LinearLayout navThird = findViewById(R.id.navThird);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            if (isAdmin) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, TenantDashboardActivity.class));
            }
            finish();
        });

        navRequests.setOnClickListener(v -> {
            if (!isAdmin) {
                startActivity(new Intent(this, MyRequestsActivity.class));
            }
            finish();
        });

        navThird.setOnClickListener(v -> {
            if (!isAdmin) {
                startActivity(new Intent(this, NotificationsHistoryActivity.class));
                finish();
            } else {
                finish();
            }
        });

        navProfile.setOnClickListener(v -> {
            if (isAdmin) {
                showProfileLogoutDialog();
            } else {
                startActivity(new Intent(this, TenantProfileActivity.class));
                finish();
            }
        });
    }

    private void createTenantNotification(String newStatus, String adminNote) {
        db.collection("requests").document(docId).get().addOnSuccessListener(doc -> {
            String tenantUid = doc.getString("tenantUid");
            String titleStr = doc.getString("title") != null ? doc.getString("title") : (problemType + " in " + location);
            String ticketId = reqId != null ? reqId : "#REQ";

            String timeStr = new SimpleDateFormat("MMM d, yyyy | h:mm a", Locale.US).format(new Date());
            String message = "Your request " + ticketId + " (" + titleStr + ") is now " + newStatus + ".";
            if (!adminNote.isEmpty()) {
                message += " Admin note: " + adminNote;
            }

            Map<String, Object> notif = new HashMap<>();
            notif.put("tenantUid", tenantUid != null ? tenantUid : "guest");
            notif.put("reqId", ticketId);
            notif.put("title", "Request Status Updated");
            notif.put("message", message);
            notif.put("time", timeStr);
            notif.put("status", newStatus);
            notif.put("unread", true);
            notif.put("timestamp", FieldValue.serverTimestamp());

            db.collection("notifications").add(notif);
        });
    }

    private void updateStatusBadge(String status) {
        float d = getResources().getDisplayMetrics().density;
        tvStatusBadge.setText(status);

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(8 * d);

        if ("In Progress".equalsIgnoreCase(status)) {
            badgeBg.setColor(Color.parseColor("#FFF3E0"));
            tvStatusBadge.setTextColor(Color.parseColor("#E65100"));
        } else if ("Pending".equalsIgnoreCase(status)) {
            badgeBg.setColor(Color.parseColor("#FFEBEE"));
            tvStatusBadge.setTextColor(Color.parseColor("#C62828"));
        } else {
            badgeBg.setColor(Color.parseColor("#E8F5E9"));
            tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
        }
        tvStatusBadge.setBackground(badgeBg);
    }

    private void showProfileLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Admin Profile")
                .setMessage("Would you like to log out of FixItBoard?")
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