package com.group.fixitboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminUserManagementActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListenerRegistration usersListener;
    private final List<DocumentSnapshot> allUserDocs = new ArrayList<>();

    private LinearLayout containerUsersList;
    private TextView tvUserCount, tabAll, tabTenants, tabAdmins;
    private EditText etSearch;

    private String currentRoleFilter = "ALL";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnAddUser = findViewById(R.id.btnAddUser);
        etSearch = findViewById(R.id.etSearchUsers);
        tabAll = findViewById(R.id.tabUsersAll);
        tabTenants = findViewById(R.id.tabUsersTenants);
        tabAdmins = findViewById(R.id.tabUsersAdmins);
        tvUserCount = findViewById(R.id.tvUserCount);
        containerUsersList = findViewById(R.id.containerUsersList);

        btnBack.setOnClickListener(v -> finish());
        btnAddUser.setOnClickListener(v -> showAddOrEditUserDialog(null));

        tabAll.setOnClickListener(v -> setFilter("ALL"));
        tabTenants.setOnClickListener(v -> setFilter("TENANT"));
        tabAdmins.setOnClickListener(v -> setFilter("ADMIN"));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                renderUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listenToUsers();
    }

    private void setFilter(String role) {
        currentRoleFilter = role;
        updateTabStyle(tabAll, "ALL".equals(role));
        updateTabStyle(tabTenants, "TENANT".equals(role));
        updateTabStyle(tabAdmins, "ADMIN".equals(role));
        renderUsers();
    }

    private void updateTabStyle(TextView tab, boolean active) {
        if (active) {
            tab.setBackgroundColor(ContextCompat.getColor(this, R.color.navy_primary));
            tab.setTextColor(Color.WHITE);
            tab.setTypeface(null, Typeface.BOLD);
        } else {
            tab.setBackgroundColor(ContextCompat.getColor(this, R.color.slate_bg));
            tab.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tab.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void listenToUsers() {
        usersListener = db.collection("users")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    allUserDocs.clear();
                    allUserDocs.addAll(snapshots.getDocuments());
                    renderUsers();
                });
    }

    private void renderUsers() {
        containerUsersList.removeAllViews();
        int count = 0;

        for (DocumentSnapshot doc : allUserDocs) {
            String name = doc.getString("name") != null ? doc.getString("name") : "Unnamed User";
            String email = doc.getString("email") != null ? doc.getString("email") : "No email";
            String role = doc.getString("role") != null ? doc.getString("role").toLowerCase() : "tenant";
            String unit = doc.getString("unit") != null ? doc.getString("unit") : "Unassigned";

            if ("TENANT".equals(currentRoleFilter) && "admin".equals(role)) continue;
            if ("ADMIN".equals(currentRoleFilter) && !"admin".equals(role)) continue;

            if (!searchQuery.isEmpty()) {
                boolean match = name.toLowerCase().contains(searchQuery)
                        || email.toLowerCase().contains(searchQuery)
                        || unit.toLowerCase().contains(searchQuery);
                if (!match) continue;
            }

            count++;
            containerUsersList.addView(buildUserCard(doc, name, email, role, unit));
        }

        tvUserCount.setText("Showing " + count + " user(s)");
    }

    private CardView buildUserCard(DocumentSnapshot doc, String name, String email, String role, String unit) {
        float d = getResources().getDisplayMetrics().density;

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = (int) (10 * d);
        card.setLayoutParams(cardParams);
        card.setRadius(12 * d);
        card.setCardElevation(2 * d);
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pad = (int) (14 * d);
        row.setPadding(pad, pad, pad, pad);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        info.setLayoutParams(infoParams);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(ContextCompat.getColor(this, R.color.navy_primary));
        tvName.setTextSize(15f);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvEmail = new TextView(this);
        tvEmail.setText(email);
        tvEmail.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvEmail.setTextSize(12.5f);

        TextView tvUnit = new TextView(this);
        tvUnit.setText("admin".equals(role) ? "Property Administrator" : "Unit: " + unit);
        tvUnit.setTextColor(ContextCompat.getColor(this, R.color.navy_primary));
        tvUnit.setTextSize(12f);
        tvUnit.setPadding(0, (int) (4 * d), 0, 0);

        info.addView(tvName);
        info.addView(tvEmail);
        info.addView(tvUnit);

        TextView badge = new TextView(this);
        badge.setText("admin".equals(role) ? "ADMIN" : "TENANT");
        badge.setTextSize(11f);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setPadding((int) (10 * d), (int) (4 * d), (int) (10 * d), (int) (4 * d));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(16 * d);
        if ("admin".equals(role)) {
            badgeBg.setColor(Color.parseColor("#DBEAFE"));
            badge.setTextColor(Color.parseColor("#1D4ED8"));
        } else {
            badgeBg.setColor(Color.parseColor("#DCFCE7"));
            badge.setTextColor(Color.parseColor("#15803D"));
        }
        badge.setBackground(badgeBg);

        row.addView(info);
        row.addView(badge);
        card.addView(row);

        card.setOnClickListener(v -> showUserOptionsDialog(doc, name, email, role, unit));
        return card;
    }

    private void showUserOptionsDialog(DocumentSnapshot doc, String name, String email, String role, String unit) {
        String contact = doc.getString("contact") != null ? doc.getString("contact") : "N/A";
        String building = doc.getString("building") != null ? doc.getString("building") : "Building A";

        new AlertDialog.Builder(this)
                .setTitle(name)
                .setMessage("Email: " + email + "\nRole: " + role.toUpperCase()
                        + "\nBuilding: " + building + "\nUnit: " + unit + "\nContact: " + contact)
                .setPositiveButton("Edit User", (d, w) -> showAddOrEditUserDialog(doc))
                .setNegativeButton("Delete User", (d, w) -> confirmDeleteUser(doc.getId(), name))
                .setNeutralButton("Close", null)
                .show();
    }

    private void confirmDeleteUser(String docId, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Remove User")
                .setMessage("Delete " + name + " from FixItBoard records?")
                .setPositiveButton("Delete", (d, w) ->
                        db.collection("users").document(docId).delete()
                                .addOnSuccessListener(u -> Toast.makeText(this, "User removed", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddOrEditUserDialog(DocumentSnapshot existingDoc) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText etName = new EditText(this);
        etName.setHint("Full Name (e.g., Juan Dela Cruz)");
        layout.addView(etName);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email Address");
        layout.addView(etEmail);

        EditText etUnit = new EditText(this);
        etUnit.setHint("Unit Number (e.g., Unit 3B)");
        layout.addView(etUnit);

        EditText etContact = new EditText(this);
        etContact.setHint("Contact Number (e.g., 0917-123-4567)");
        layout.addView(etContact);

        Spinner spRole = new Spinner(this);
        String[] roles = {"tenant", "admin"};
        spRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles));
        layout.addView(spRole);

        if (existingDoc != null) {
            etName.setText(existingDoc.getString("name"));
            etEmail.setText(existingDoc.getString("email"));
            etUnit.setText(existingDoc.getString("unit"));
            etContact.setText(existingDoc.getString("contact"));
            if ("admin".equalsIgnoreCase(existingDoc.getString("role"))) {
                spRole.setSelection(1);
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(existingDoc == null ? "Add New User" : "Edit User Details")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String unit = etUnit.getText().toString().trim();
                    String contact = etContact.getText().toString().trim();
                    String role = spRole.getSelectedItem().toString();

                    if (name.isEmpty() || email.isEmpty()) {
                        Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("email", email);
                    data.put("unit", unit.isEmpty() ? "Unit 1A" : unit);
                    data.put("contact", contact);
                    data.put("role", role);

                    if (existingDoc == null) {
                        db.collection("users").add(data)
                                .addOnSuccessListener(ref -> Toast.makeText(this, "User added!", Toast.LENGTH_SHORT).show());
                    } else {
                        db.collection("users").document(existingDoc.getId()).update(data)
                                .addOnSuccessListener(u -> Toast.makeText(this, "User updated!", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usersListener != null) usersListener.remove();
    }
}