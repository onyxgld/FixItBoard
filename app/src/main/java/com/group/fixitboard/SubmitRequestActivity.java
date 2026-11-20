package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class SubmitRequestActivity extends AppCompatActivity {

    private EditText etProblemType, etLocation, etDescription;
    private TextView tvCharCount;
    private Button btnSubmitRequest;
    private ImageButton activePhotoSlot;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), (Uri uri) -> {
                if (uri != null && activePhotoSlot != null) {
                    activePhotoSlot.setImageTintList(null);
                    activePhotoSlot.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    activePhotoSlot.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_request);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        etProblemType = findViewById(R.id.etProblemType);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        tvCharCount = findViewById(R.id.tvCharCount);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        btnBack.setOnClickListener(v -> finish());

        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCount.setText(s.length() + "/300");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        int[] photoSlotIds = {
                R.id.btnPhoto1, R.id.btnPhoto2, R.id.btnPhoto3,
                R.id.btnPhoto4, R.id.btnPhoto5, R.id.btnPhoto6
        };
        for (int id : photoSlotIds) {
            ImageButton slot = findViewById(id);
            slot.setOnClickListener(v -> {
                activePhotoSlot = slot;
                pickImageLauncher.launch("image/*");
            });
        }

        btnSubmitRequest.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        String problemType = etProblemType.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (problemType.isEmpty()) {
            etProblemType.setError("Please enter problem type");
            etProblemType.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etLocation.setError("Please enter location");
            etLocation.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Please enter a short description");
            etDescription.requestFocus();
            return;
        }

        btnSubmitRequest.setEnabled(false);
        btnSubmitRequest.setText("Submitting...");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String tenantUid = currentUser != null ? currentUser.getUid() : "guest";
        String tenantEmail = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "";
        String tenantUnit = TenantLoginActivity.resolveTenantDisplayName(currentUser, tenantEmail);

        String reqId = String.format(Locale.US, "#REQ-%05d", 100 + new Random().nextInt(900));
        String dateStr = new SimpleDateFormat("MMM d, yyyy", Locale.US).format(new Date()) + " | " + tenantUnit;
        String title = problemType + " in " + location;

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("id", reqId);
        requestData.put("title", title);
        requestData.put("problemType", problemType);
        requestData.put("location", location);
        requestData.put("description", description);
        requestData.put("date", dateStr);
        requestData.put("status", "Pending");
        requestData.put("tenantUnit", tenantUnit);
        requestData.put("tenantUid", tenantUid);
        requestData.put("notes", "");
        requestData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("requests")
                .add(requestData)
                .addOnSuccessListener(documentReference -> showSuccessDialog())
                .addOnFailureListener(e -> {
                    btnSubmitRequest.setEnabled(true);
                    btnSubmitRequest.setText("Submit Request");
                    Toast.makeText(this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showSuccessDialog() {
        int navyColor = ContextCompat.getColor(this, R.color.navy_primary);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        container.setBackgroundResource(R.drawable.bg_input_outline);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, pad);

        TextView tvClose = new TextView(this);
        tvClose.setText("✕");
        tvClose.setTextSize(18f);
        tvClose.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvClose.setGravity(Gravity.END);
        tvClose.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(tvClose);

        ImageView ivCheck = new ImageView(this);
        ivCheck.setImageResource(android.R.drawable.checkbox_on_background);
        ivCheck.setColorFilter(Color.parseColor("#2E7D32"));
        int iconSize = (int) (54 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconParams.bottomMargin = (int) (14 * getResources().getDisplayMetrics().density);
        ivCheck.setLayoutParams(iconParams);
        container.addView(ivCheck);

        TextView tvMessage = new TextView(this);
        tvMessage.setText("Your request submitted\nsuccessfully");
        tvMessage.setTextColor(navyColor);
        tvMessage.setTextSize(16f);
        tvMessage.setTypeface(null, Typeface.BOLD);
        tvMessage.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        msgParams.bottomMargin = (int) (18 * getResources().getDisplayMetrics().density);
        tvMessage.setLayoutParams(msgParams);
        container.addView(tvMessage);

        Button btnViewRequest = new Button(this);
        btnViewRequest.setText("View request");
        btnViewRequest.setAllCaps(false);
        btnViewRequest.setTextColor(Color.WHITE);
        btnViewRequest.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navyColor));
        btnViewRequest.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (46 * getResources().getDisplayMetrics().density)));
        container.addView(btnViewRequest);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(container)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        tvClose.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        btnViewRequest.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(SubmitRequestActivity.this, MyRequestsActivity.class));
            finish();
        });

        dialog.show();
    }
}