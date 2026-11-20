package com.group.fixitboard;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        String email = user.getEmail() != null ? user.getEmail() : "tenant@fixitboard.com";
        String defaultName = TenantLoginActivity.resolveTenantDisplayName(user, email);

        tvInitials.setText(defaultName);
        tvFullName.setText(defaultName);
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

        db.collection("units").get().addOnSuccessListener(snapshots -> {
            List<String> unitLabels = new ArrayList<>();
            List<DocumentSnapshot> selectableDocs = new ArrayList<>();

            String currentUnit = tvUnit.getText().toString().trim();
            int preselectedIndex = 0;

            unitLabels.add("Unassigned (No Room Selected)");
            selectableDocs.add(null);

            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                String bldg = doc.getString("building") != null ? doc.getString("building") : "Building A";
                String unitNum = doc.getString("unitNumber") != null ? doc.getString("unitNumber") : "";
                String status = doc.getString("status") != null ? doc.getString("status") : "Vacant";

                boolean isCurrentAssigned = !unitNum.isEmpty() && unitNum.equalsIgnoreCase(currentUnit);
                if ("Vacant".equalsIgnoreCase(status) || isCurrentAssigned) {
                    unitLabels.add(unitNum + " • " + bldg + " (" + status + ")");
                    selectableDocs.add(doc);
                    if (isCurrentAssigned) {
                        preselectedIndex = unitLabels.size() - 1;
                    }
                }
            }

            if (selectableDocs.size() == 1) {
                Toast.makeText(this, "No vacant rooms added by Admin yet. You can still update your name & contact.", Toast.LENGTH_LONG).show();
            }

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            int pad = (int) (20 * getResources().getDisplayMetrics().density);
            layout.setPadding(pad, pad, pad, pad);

            EditText etName = new EditText(this);
            etName.setHint("Full Name");
            etName.setText(tvFullName.getText().toString());
            layout.addView(etName);

            Spinner spUnit = new Spinner(this);
            spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unitLabels));
            spUnit.setSelection(preselectedIndex);
            layout.addView(spUnit);

            EditText etContact = new EditText(this);
            etContact.setHint("Contact Number");
            etContact.setText(tvContact.getText().toString());
            layout.addView(etContact);

            new AlertDialog.Builder(this)
                    .setTitle("Edit Profile")
                    .setView(layout)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newName = etName.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(this, "Full Name is required.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String newContact = etContact.getText().toString().trim();
                        int selectedPos = spUnit.getSelectedItemPosition();
                        DocumentSnapshot chosenUnitDoc = selectableDocs.get(selectedPos);

                        String newBuilding = chosenUnitDoc != null && chosenUnitDoc.getString("building") != null
                                ? chosenUnitDoc.getString("building") : "Unassigned";
                        String newUnit = chosenUnitDoc != null && chosenUnitDoc.getString("unitNumber") != null
                                ? chosenUnitDoc.getString("unitNumber") : "Unassigned";

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
                                    // Free previous unit if tenant switched rooms
                                    if (!currentUnit.isEmpty()
                                            && !"Unassigned".equalsIgnoreCase(currentUnit)
                                            && !currentUnit.equalsIgnoreCase(newUnit)) {
                                        for (DocumentSnapshot oldDoc : snapshots.getDocuments()) {
                                            if (currentUnit.equalsIgnoreCase(oldDoc.getString("unitNumber"))) {
                                                Map<String, Object> vacateMap = new HashMap<>();
                                                vacateMap.put("status", "Vacant");
                                                vacateMap.put("tenantName", "None");
                                                db.collection("units").document(oldDoc.getId()).update(vacateMap);
                                            }
                                        }
                                    }

                                    // Mark newly selected unit as Occupied in Admin Property Management
                                    if (chosenUnitDoc != null) {
                                        Map<String, Object> occupyMap = new HashMap<>();
                                        occupyMap.put("status", "Occupied");
                                        occupyMap.put("tenantName", newName);
                                        db.collection("units").document(chosenUnitDoc.getId()).update(occupyMap);
                                    }

                                    String combinedDisplay = "Unassigned".equalsIgnoreCase(newUnit)
                                            ? newName : (newName + " - " + newUnit);
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(combinedDisplay)
                                            .build();
                                    user.updateProfile(profileUpdates);

                                    tvInitials.setText(newName);
                                    tvFullName.setText(newName);
                                    tvBuilding.setText(newBuilding);
                                    tvUnit.setText(newUnit);
                                    tvContact.setText(newContact);
                                    Toast.makeText(this, "Profile & room assignment synced!", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}