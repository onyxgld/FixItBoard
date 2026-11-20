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

public class AdminProfileActivity extends AppCompatActivity {

    private TextView tvInitials, tvFullName, tvRole, tvEmail, tvContact;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        tvInitials = findViewById(R.id.tvAdminInitials);
        tvFullName = findViewById(R.id.tvAdminFullName);
        tvRole = findViewById(R.id.tvAdminRole);
        tvEmail = findViewById(R.id.tvAdminEmail);
        tvContact = findViewById(R.id.tvAdminContact);
        Button btnEdit = findViewById(R.id.btnEditAdminProfile);

        btnBack.setOnClickListener(v -> finish());
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(AdminProfileActivity.this, AdminSettingsActivity.class)));

        btnEdit.setOnClickListener(v -> showEditAdminDialog());

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navRequests = findViewById(R.id.navRequests);
        LinearLayout navHistory = findViewById(R.id.navHistory);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
        });

        navRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
        });

        navHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v ->
                Toast.makeText(this, "You are on Admin Profile", Toast.LENGTH_SHORT).show());

        loadAdminProfile();
    }

    private void loadAdminProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String email = user.getEmail() != null ? user.getEmail() : "admin@fixitboard.com";
        tvEmail.setText(email);
        tvRole.setText("Administrator");

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String contact = doc.getString("contact");
                        if (name != null && !name.isEmpty()) {
                            tvFullName.setText(name);
                            tvInitials.setText(name.substring(0, Math.min(2, name.length())).toUpperCase());
                        }
                        if (contact != null && !contact.isEmpty()) {
                            tvContact.setText(contact);
                        }
                    }
                });
    }

    private void showEditAdminDialog() {
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

        EditText etContact = new EditText(this);
        etContact.setHint("Contact Number");
        etContact.setText(tvContact.getText().toString());
        layout.addView(etContact);

        new AlertDialog.Builder(this)
                .setTitle("Edit Admin Profile")
                .setView(layout)
                .setPositiveButton("Save Changes", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newContact = etContact.getText().toString().trim();

                    Map<String, Object> map = new HashMap<>();
                    map.put("uid", user.getUid());
                    map.put("email", user.getEmail());
                    map.put("name", newName);
                    map.put("contact", newContact);
                    map.put("role", "admin");

                    db.collection("users").document(user.getUid()).set(map)
                            .addOnSuccessListener(unused -> {
                                if (!newName.isEmpty()) {
                                    tvInitials.setText(newName.substring(0, Math.min(2, newName.length())).toUpperCase());
                                    tvFullName.setText(newName);
                                }
                                tvContact.setText(newContact);
                                Toast.makeText(this, "Admin profile updated!", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}