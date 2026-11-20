package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RequestDetailsActivity extends AppCompatActivity {

    private TextView tvReqId, tvStatusBadge, tvProblemType, tvLocation, tvDescription, tvTenantUnit, tvSubmittedOn;
    private TextView tvLatestUpdateLabel, tvLatestUpdateValue;
    private Spinner spinnerStatus;
    private EditText etAdminNotes;
    private Button btnSave, btnRateRepair, btnViewAttachments;
    private ImageView ivPhoto1, ivPhoto2, ivPhoto3;

    private boolean isAdmin = false;
    private String docId;
    private String reqId;
    private String problemType;
    private String location;
    private int existingRating = 0;
    private String existingFeedback = "";
    private List<String> attachedPhotoData;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        tvReqId = findViewById(R.id.tvDetailReqId);
        tvStatusBadge = findViewById(R.id.tvDetailStatusBadge);
        tvProblemType = findViewById(R.id.tvDetailProblemType);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvTenantUnit = findViewById(R.id.tvDetailTenantUnit);
        tvSubmittedOn = findViewById(R.id.tvDetailSubmittedOn);
        tvLatestUpdateLabel = findViewById(R.id.tvLatestUpdateLabel);
        tvLatestUpdateValue = findViewById(R.id.tvLatestUpdateValue);
        ivPhoto1 = findViewById(R.id.ivDetailPhoto1);
        ivPhoto2 = findViewById(R.id.ivDetailPhoto2);
        ivPhoto3 = findViewById(R.id.ivDetailPhoto3);

        LinearLayout layoutAdminControls = findViewById(R.id.layoutAdminControls);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        etAdminNotes = findViewById(R.id.etAdminNotes);
        btnViewAttachments = findViewById(R.id.btnViewAttachments);
        btnRateRepair = findViewById(R.id.btnRateRepair);
        btnSave = findViewById(R.id.btnSaveDetails);
        TextView tvNavThirdLabel = findViewById(R.id.tvNavThirdLabel);

        btnBack.setOnClickListener(v -> finish());

        injectNotesPickerTrigger();

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
        if (notes != null && !notes.trim().isEmpty()) {
            etAdminNotes.setText(notes);
            tvLatestUpdateLabel.setVisibility(View.VISIBLE);
            tvLatestUpdateValue.setVisibility(View.VISIBLE);
            tvLatestUpdateValue.setText(notes);
        }

        String currentStatus = status != null ? status : "Pending";
        updateStatusBadge(currentStatus);

        String[] statuses = {"Pending", "In Progress", "Completed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(adapter);
        selectSpinnerStatus(statuses, currentStatus);

        if (isAdmin) {
            layoutAdminControls.setVisibility(View.VISIBLE);
            tvNavThirdLabel.setText("History");
            btnSave.setText("Save Changes");
        } else {
            layoutAdminControls.setVisibility(View.GONE);
            tvNavThirdLabel.setText("Notification");
            btnSave.setText("Done");
            if ("Completed".equalsIgnoreCase(currentStatus)) {
                btnRateRepair.setVisibility(View.VISIBLE);
            }
        }

        btnViewAttachments.setOnClickListener(v -> showAttachmentsPreviewDialog());
        btnRateRepair.setOnClickListener(v -> showRateRepairDialog());

        fetchLiveRequestRecord(statuses);

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
            startActivity(new Intent(this, isAdmin ? AdminDashboardActivity.class : TenantDashboardActivity.class));
            finish();
        });

        navRequests.setOnClickListener(v -> {
            if (!isAdmin) startActivity(new Intent(this, MyRequestsActivity.class));
            finish();
        });

        navThird.setOnClickListener(v -> {
            if (!isAdmin) {
                startActivity(new Intent(this, NotificationsHistoryActivity.class));
            }
            finish();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, isAdmin ? AdminProfileActivity.class : TenantProfileActivity.class));
            finish();
        });
    }

    private void injectNotesPickerTrigger() {
        ViewGroup parent = (ViewGroup) etAdminNotes.getParent();
        if (parent == null) return;

        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int idx = parent.indexOfChild(etAdminNotes);

        TextView tvQuickPick = new TextView(this);
        tvQuickPick.setText("Quick Pick Preset Note ▾");
        tvQuickPick.setTextColor(navy);
        tvQuickPick.setTextSize(12f);
        tvQuickPick.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = (int) (6 * d);
        tvQuickPick.setLayoutParams(lp);
        tvQuickPick.setOnClickListener(v -> showNotesPickerDialog());

        parent.addView(tvQuickPick, idx);
    }

    private void showNotesPickerDialog() {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);

        String[] presets = {
                "Technician already assigned.",
                "Repair completed; issue resolved.",
                "Awaiting review."
        };

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * d);
        container.setPadding(pad, pad, pad, pad);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Update notes")
                .setView(container)
                .setNegativeButton("Cancel", null)
                .create();

        for (String preset : presets) {
            Button btnPreset = new Button(this);
            btnPreset.setText(preset);
            btnPreset.setAllCaps(false);
            btnPreset.setTextColor(navy);
            btnPreset.setTextSize(13f);
            btnPreset.setBackgroundResource(R.drawable.bg_input_outline);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d));
            btnParams.bottomMargin = (int) (10 * d);
            btnPreset.setLayoutParams(btnParams);
            btnPreset.setOnClickListener(v -> {
                etAdminNotes.setText(preset);
                etAdminNotes.setSelection(preset.length());
                if (preset.toLowerCase().contains("completed")) {
                    spinnerStatus.setSelection(2);
                } else if (preset.toLowerCase().contains("assigned")) {
                    spinnerStatus.setSelection(1);
                }
                dialog.dismiss();
            });
            container.addView(btnPreset);
        }

        dialog.show();
    }

    private void selectSpinnerStatus(String[] statuses, String targetStatus) {
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(targetStatus)) {
                spinnerStatus.setSelection(i);
                break;
            }
        }
    }

    private void fetchLiveRequestRecord(String[] statuses) {
        if (docId != null && !docId.isEmpty()) {
            db.collection("requests").document(docId).get()
                    .addOnSuccessListener(doc -> applyFirestoreDoc(doc, statuses));
        } else if (reqId != null && !reqId.isEmpty()) {
            db.collection("requests").whereEqualTo("id", reqId).limit(1).get()
                    .addOnSuccessListener(snapshots -> {
                        if (!snapshots.isEmpty()) {
                            applyFirestoreDoc(snapshots.getDocuments().get(0), statuses);
                        }
                    });
        }
    }

    @SuppressWarnings("unchecked")
    private void applyFirestoreDoc(DocumentSnapshot doc, String[] statuses) {
        if (doc == null || !doc.exists()) return;
        docId = doc.getId();

        String dbReqId = doc.getString("id");
        String dbProblem = doc.getString("problemType");
        String dbLoc = doc.getString("location");
        String dbDesc = doc.getString("description");
        String dbStatus = doc.getString("status");
        String dbTenant = doc.getString("tenantUnit");
        String dbDate = doc.getString("date");
        String dbNotes = doc.getString("notes");

        if (dbReqId != null) {
            reqId = dbReqId;
            tvReqId.setText(dbReqId);
        }
        if (dbProblem != null) {
            problemType = dbProblem;
            tvProblemType.setText(dbProblem);
        }
        if (dbLoc != null) {
            location = dbLoc;
            tvLocation.setText(dbLoc);
        }
        if (dbDesc != null) tvDescription.setText(dbDesc);
        if (dbTenant != null) tvTenantUnit.setText(dbTenant);
        if (dbDate != null) tvSubmittedOn.setText(dbDate);

        if (dbNotes != null && !dbNotes.trim().isEmpty()) {
            etAdminNotes.setText(dbNotes);
            tvLatestUpdateLabel.setVisibility(View.VISIBLE);
            tvLatestUpdateValue.setVisibility(View.VISIBLE);
            tvLatestUpdateValue.setText(dbNotes);
        }

        if (dbStatus != null) {
            updateStatusBadge(dbStatus);
            selectSpinnerStatus(statuses, dbStatus);
            if (!isAdmin && "Completed".equalsIgnoreCase(dbStatus)) {
                btnRateRepair.setVisibility(View.VISIBLE);
            }
        }

        Object photosObj = doc.get("photoUris");
        if (photosObj instanceof List) {
            attachedPhotoData = (List<String>) photosObj;
            if (!attachedPhotoData.isEmpty()) {
                ImageView[] slots = {ivPhoto1, ivPhoto2, ivPhoto3};
                for (int i = 0; i < attachedPhotoData.size() && i < slots.length; i++) {
                    bindPhotoIntoImageView(slots[i], attachedPhotoData.get(i));
                    slots[i].setOnClickListener(v -> showAttachmentsPreviewDialog());
                }
                btnViewAttachments.setText("View Attached Photos (" + attachedPhotoData.size() + ")");
                btnViewAttachments.setVisibility(View.VISIBLE);
            }
        }

        Long ratingVal = doc.getLong("rating");
        existingRating = ratingVal != null ? ratingVal.intValue() : 0;
        existingFeedback = doc.getString("ratingFeedback") != null ? doc.getString("ratingFeedback") : "";
        if (existingRating > 0 && btnRateRepair.getVisibility() == View.VISIBLE) {
            btnRateRepair.setText("Rated " + existingRating + "/5 ★ (Update Rating)");
        }
    }

    private void bindPhotoIntoImageView(ImageView iv, String data) {
        if (iv == null || data == null || data.isEmpty()) return;
        iv.setImageTintList(null);
        iv.setColorFilter(null);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setPadding(0, 0, 0, 0);

        if (data.startsWith("content://") || data.startsWith("file://")) {
            iv.setImageURI(Uri.parse(data));
        } else {
            try {
                byte[] bytes = Base64.decode(data, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bmp != null) iv.setImageBitmap(bmp);
            } catch (Exception ignored) {
            }
        }
    }

    private void showAttachmentsPreviewDialog() {
        if (attachedPhotoData == null || attachedPhotoData.isEmpty()) {
            Toast.makeText(this, "No attachments found.", Toast.LENGTH_SHORT).show();
            return;
        }

        float d = getResources().getDisplayMetrics().density;
        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * d);
        container.setPadding(pad, pad, pad, pad);

        for (String data : attachedPhotoData) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int) (240 * d));
            params.bottomMargin = (int) (12 * d);
            iv.setLayoutParams(params);
            bindPhotoIntoImageView(iv, data);
            container.addView(iv);
        }

        scrollView.addView(container);

        new AlertDialog.Builder(this)
                .setTitle("Attachment Preview")
                .setView(scrollView)
                .setPositiveButton("Close Preview", null)
                .show();
    }

    private void showRateRepairDialog() {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * d);
        layout.setPadding(pad, pad, pad, pad);

        TextView tvPrompt = new TextView(this);
        tvPrompt.setText("How was the repair for " + (reqId != null ? reqId : "this request") + "?");
        tvPrompt.setTextColor(navy);
        tvPrompt.setTextSize(14f);
        tvPrompt.setTypeface(null, Typeface.BOLD);
        layout.addView(tvPrompt);

        LinearLayout starRow = new LinearLayout(this);
        starRow.setOrientation(LinearLayout.HORIZONTAL);
        starRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = (int) (14 * d);
        rowParams.bottomMargin = (int) (14 * d);
        starRow.setLayoutParams(rowParams);

        final int[] selectedStars = {existingRating > 0 ? existingRating : 5};
        TextView[] starViews = new TextView[5];

        for (int i = 0; i < 5; i++) {
            final int starIndex = i + 1;
            TextView tvStar = new TextView(this);
            tvStar.setText(starIndex <= selectedStars[0] ? "★" : "☆");
            tvStar.setTextSize(34f);
            tvStar.setTextColor(starIndex <= selectedStars[0] ? Color.parseColor("#F59E0B") : Color.GRAY);
            tvStar.setPadding((int) (8 * d), 0, (int) (8 * d), 0);
            tvStar.setOnClickListener(v -> {
                selectedStars[0] = starIndex;
                for (int j = 0; j < 5; j++) {
                    boolean active = (j + 1) <= selectedStars[0];
                    starViews[j].setText(active ? "★" : "☆");
                    starViews[j].setTextColor(active ? Color.parseColor("#F59E0B") : Color.GRAY);
                }
            });
            starViews[i] = tvStar;
            starRow.addView(tvStar);
        }
        layout.addView(starRow);

        EditText etFeedback = new EditText(this);
        etFeedback.setHint("Optional feedback about the repair...");
        etFeedback.setText(existingFeedback);
        etFeedback.setMinLines(2);
        layout.addView(etFeedback);

        new AlertDialog.Builder(this)
                .setTitle("Rate Repair")
                .setView(layout)
                .setPositiveButton("Submit Rating", (dialog, which) -> {
                    if (docId == null || docId.isEmpty()) return;
                    String feedback = etFeedback.getText().toString().trim();

                    Map<String, Object> ratingData = new HashMap<>();
                    ratingData.put("rating", selectedStars[0]);
                    ratingData.put("ratingFeedback", feedback);

                    db.collection("requests").document(docId).update(ratingData)
                            .addOnSuccessListener(u -> {
                                existingRating = selectedStars[0];
                                existingFeedback = feedback;
                                btnRateRepair.setText("Rated " + existingRating + "/5 ★ (Update Rating)");
                                Toast.makeText(this, "Thank you for your " + selectedStars[0] + "-star feedback!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to save rating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
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
}