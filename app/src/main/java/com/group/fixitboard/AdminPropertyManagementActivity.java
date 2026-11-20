package com.group.fixitboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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

public class AdminPropertyManagementActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListenerRegistration unitsListener;
    private final List<DocumentSnapshot> allUnitDocs = new ArrayList<>();

    private LinearLayout containerUnitsList;
    private TextView tvOccupied, tvVacant, tvMaintenance;
    private TextView tabAll, tabA, tabB, tabC;
    private String selectedBuilding = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_property_management);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnAddUnit = findViewById(R.id.btnAddUnit);
        tvOccupied = findViewById(R.id.tvSummaryOccupied);
        tvVacant = findViewById(R.id.tvSummaryVacant);
        tvMaintenance = findViewById(R.id.tvSummaryMaintenance);
        tabAll = findViewById(R.id.tabBldgAll);
        tabA = findViewById(R.id.tabBldgA);
        tabB = findViewById(R.id.tabBldgB);
        tabC = findViewById(R.id.tabBldgC);
        containerUnitsList = findViewById(R.id.containerUnitsList);

        btnBack.setOnClickListener(v -> finish());
        btnAddUnit.setOnClickListener(v -> showAddOrEditUnitDialog(null));

        tabAll.setOnClickListener(v -> setBuildingFilter("ALL"));
        tabA.setOnClickListener(v -> setBuildingFilter("Building A"));
        tabB.setOnClickListener(v -> setBuildingFilter("Building B"));
        tabC.setOnClickListener(v -> setBuildingFilter("Building C"));

        listenToUnits();
    }

    private void setBuildingFilter(String bldg) {
        selectedBuilding = bldg;
        updateTabStyle(tabAll, "ALL".equals(bldg));
        updateTabStyle(tabA, "Building A".equals(bldg));
        updateTabStyle(tabB, "Building B".equals(bldg));
        updateTabStyle(tabC, "Building C".equals(bldg));
        renderUnits();
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

    private void listenToUnits() {
        unitsListener = db.collection("units")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    allUnitDocs.clear();
                    allUnitDocs.addAll(snapshots.getDocuments());
                    renderUnits();
                });
    }

    private void renderUnits() {
        containerUnitsList.removeAllViews();
        int occupied = 0, vacant = 0, maintenance = 0;

        for (DocumentSnapshot doc : allUnitDocs) {
            String building = doc.getString("building") != null ? doc.getString("building") : "Building A";
            String unitNumber = doc.getString("unitNumber") != null ? doc.getString("unitNumber") : "Unit 101";
            String status = doc.getString("status") != null ? doc.getString("status") : "Vacant";
            String tenantName = doc.getString("tenantName") != null ? doc.getString("tenantName") : "None";

            if ("Occupied".equalsIgnoreCase(status)) occupied++;
            else if ("Maintenance".equalsIgnoreCase(status)) maintenance++;
            else vacant++;

            if (!"ALL".equals(selectedBuilding) && !selectedBuilding.equalsIgnoreCase(building)) {
                continue;
            }

            containerUnitsList.addView(buildUnitCard(doc, building, unitNumber, status, tenantName));
        }

        if (containerUnitsList.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText("No units found. Tap + to add a property unit.");
            empty.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 80, 0, 0);
            containerUnitsList.addView(empty);
        }

        tvOccupied.setText(occupied + " Occupied");
        tvVacant.setText(vacant + " Vacant");
        tvMaintenance.setText(maintenance + " Maintenance");
    }

    private CardView buildUnitCard(DocumentSnapshot doc, String building, String unitNumber, String status, String tenantName) {
        float d = getResources().getDisplayMetrics().density;

        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = (int) (10 * d);
        card.setLayoutParams(params);
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
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(unitNumber + " • " + building);
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.navy_primary));
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(null, Typeface.BOLD);

        TextView tvTenant = new TextView(this);
        tvTenant.setText("Tenant: " + tenantName);
        tvTenant.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvTenant.setTextSize(13f);
        tvTenant.setPadding(0, (int) (4 * d), 0, 0);

        info.addView(tvTitle);
        info.addView(tvTenant);

        TextView badge = new TextView(this);
        badge.setText(status.toUpperCase());
        badge.setTextSize(11f);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setPadding((int) (10 * d), (int) (4 * d), (int) (10 * d), (int) (4 * d));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(16 * d);
        if ("Occupied".equalsIgnoreCase(status)) {
            badgeBg.setColor(Color.parseColor("#DCFCE7"));
            badge.setTextColor(Color.parseColor("#15803D"));
        } else if ("Maintenance".equalsIgnoreCase(status)) {
            badgeBg.setColor(Color.parseColor("#FEF3C7"));
            badge.setTextColor(Color.parseColor("#B45309"));
        } else {
            badgeBg.setColor(Color.parseColor("#E0E7FF"));
            badge.setTextColor(Color.parseColor("#3730A3"));
        }
        badge.setBackground(badgeBg);

        row.addView(info);
        row.addView(badge);
        card.addView(row);

        card.setOnClickListener(v -> showAddOrEditUnitDialog(doc));
        return card;
    }

    private void showAddOrEditUnitDialog(DocumentSnapshot existingDoc) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText etBuilding = new EditText(this);
        etBuilding.setHint("Building / Floor (e.g., Building A)");
        layout.addView(etBuilding);

        EditText etUnitNumber = new EditText(this);
        etUnitNumber.setHint("Unit Number (e.g., Unit 3B)");
        layout.addView(etUnitNumber);

        Spinner spStatus = new Spinner(this);
        String[] statuses = {"Occupied", "Vacant", "Maintenance"};
        spStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses));
        layout.addView(spStatus);

        EditText etTenant = new EditText(this);
        etTenant.setHint("Assigned Tenant Name (or None)");
        layout.addView(etTenant);

        if (existingDoc != null) {
            etBuilding.setText(existingDoc.getString("building"));
            etUnitNumber.setText(existingDoc.getString("unitNumber"));
            etTenant.setText(existingDoc.getString("tenantName"));

            String st = existingDoc.getString("status");
            if ("Vacant".equalsIgnoreCase(st)) spStatus.setSelection(1);
            else if ("Maintenance".equalsIgnoreCase(st)) spStatus.setSelection(2);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(existingDoc == null ? "Add Property Unit" : "Manage Unit Details")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    String building = etBuilding.getText().toString().trim();
                    String unitNum = etUnitNumber.getText().toString().trim();
                    if (unitNum.isEmpty()) {
                        Toast.makeText(this, "Unit Number is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("building", building.isEmpty() ? "Building A" : building);
                    data.put("unitNumber", unitNum);
                    data.put("status", spStatus.getSelectedItem().toString());
                    String tenant = etTenant.getText().toString().trim();
                    data.put("tenantName", tenant.isEmpty() ? "None" : tenant);

                    if (existingDoc == null) {
                        db.collection("units").add(data)
                                .addOnSuccessListener(r -> Toast.makeText(this, "Unit added!", Toast.LENGTH_SHORT).show());
                    } else {
                        db.collection("units").document(existingDoc.getId()).update(data)
                                .addOnSuccessListener(u -> Toast.makeText(this, "Unit updated!", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null);

        if (existingDoc != null) {
            builder.setNeutralButton("Delete Unit", (d, w) ->
                    db.collection("units").document(existingDoc.getId()).delete()
                            .addOnSuccessListener(u -> Toast.makeText(this, "Unit deleted", Toast.LENGTH_SHORT).show()));
        }

        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unitsListener != null) unitsListener.remove();
    }
}