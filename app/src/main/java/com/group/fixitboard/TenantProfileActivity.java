package com.group.fixitboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TenantProfileActivity extends AppCompatActivity {

    private TextView tvInitials, tvFullName, tvBuilding, tvUnit, tvEmail, tvContact;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        tvInitials = findViewById(R.id.tvProfileInitials);
        tvFullName = findViewById(R.id.tvProfileFullName);
        tvBuilding = findViewById(R.id.tvProfileBuilding);
        tvUnit = findViewById(R.id.tvProfileUnit);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvContact = findViewById(R.id.tvProfileContact);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);

        btnBack.setOnClickListener(v -> finish());
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(TenantProfileActivity.this, TenantSettingsActivity.class)));

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Bottom Navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navRequests = findViewById(R.id.navRequests);
        LinearLayout navNotifications = findViewById(R.id.navNotifications);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, TenantDashboardActivity.class));
            finish();
        });

        navRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, MyRequestsActivity.class));
            finish();
        });

        navNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsHistoryActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v ->
                Toast.makeText(this, "You are on Profile", Toast.LENGTH_SHORT).show());

        loadProfileData();
    }

    private void loadProfileData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String email = user.getEmail() != null ? user.getEmail() : "unit3b@fixitboard.com";
        String defaultUnit = TenantLoginActivity.resolveTenantDisplayName(user, email);

        tvInitials.setText(defaultUnit);
        tvFullName.setText(defaultUnit);
        tvUnit.setText(defaultUnit);
        tvEmail.setText(email);

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String building = doc.getString("building");
                        String unit = doc.getString("unit");
                        String contact = doc.getString("contact");

                        if (name != null && !name.isEmpty()) {
                            tvFullName.setText(name);
                            tvInitials.setText(name);
                        }
                        if (building != null && !building.isEmpty()) tvBuilding.setText(building);
                        if (unit != null && !unit.isEmpty()) tvUnit.setText(unit);
                        if (contact != null && !contact.isEmpty()) tvContact.setText(contact);
                    }
                });
    }

    private void showEditProfileDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText etName = new EditText(this);
        etName.setHint("Full Name");
        etName.setText(tvFullName.getText().toString());
        layout.addView(etName);

        EditText etBuilding = new EditText(this);
        etBuilding.setHint("Building (e.g. Building A)");
        etBuilding.setText(tvBuilding.getText().toString());
        layout.addView(etBuilding);

        EditText etUnit = new EditText(this);
        etUnit.setHint("Room / Unit Number (e.g. Unit 3B)");
        etUnit.setText(tvUnit.getText().toString());
        layout.addView(etUnit);

        EditText etContact = new EditText(this);
        etContact.setHint("Contact Number");
        etContact.setText(tvContact.getText().toString());
        layout.addView(etContact);

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newBuilding = etBuilding.getText().toString().trim();
                    String newUnit = etUnit.getText().toString().trim();
                    String newContact = etContact.getText().toString().trim();

                    Map<String, Object> profileMap = new HashMap<>();
                    profileMap.put("uid", user.getUid());
                    profileMap.put("email", user.getEmail());
                    profileMap.put("name", newName);
                    profileMap.put("building", newBuilding);
                    profileMap.put("unit", newUnit);
                    profileMap.put("contact", newContact);
                    profileMap.put("role", "tenant");

                    db.collection("users").document(user.getUid()).set(profileMap)
                            .addOnSuccessListener(unused -> {
                                tvInitials.setText(newName);
                                tvFullName.setText(newName);
                                tvBuilding.setText(newBuilding);
                                tvUnit.setText(newUnit);
                                tvContact.setText(newContact);
                                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}