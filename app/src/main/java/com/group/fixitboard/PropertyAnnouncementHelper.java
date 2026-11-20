package com.group.fixitboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PropertyAnnouncementHelper {

    public static void showAnnouncementDialog(Context context, boolean isAdmin) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        float d = context.getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(context, R.color.navy_primary);
        int gray = ContextCompat.getColor(context, R.color.text_gray);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * d);
        container.setPadding(pad, pad, pad, pad);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_input_outline);
        int cardPad = (int) (16 * d);
        card.setPadding(cardPad, cardPad, cardPad, cardPad);

        TextView tvOrg = new TextView(context);
        tvOrg.setText("Property Management");
        tvOrg.setTextColor(navy);
        tvOrg.setTextSize(15f);
        tvOrg.setTypeface(null, Typeface.BOLD);
        card.addView(tvOrg);

        TextView tvDate = new TextView(context);
        tvDate.setText("Nov 8, 2026 | 9:00 AM");
        tvDate.setTextColor(gray);
        tvDate.setTextSize(11f);
        LinearLayout.LayoutParams dateLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dateLp.topMargin = (int) (4 * d);
        dateLp.bottomMargin = (int) (10 * d);
        tvDate.setLayoutParams(dateLp);
        card.addView(tvDate);

        TextView tvBody = new TextView(context);
        tvBody.setText("Please review the house rules and submit maintenance concerns through FixIt Board. For urgent concerns, contact your administrator.");
        tvBody.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        tvBody.setTextSize(13f);
        tvBody.setLineSpacing(0f, 1.2f);
        card.addView(tvBody);

        container.addView(card);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Property announcement")
                .setView(container)
                .setNegativeButton("Close", null)
                .create();

        if (!isAdmin) {
            Button btnHouseRules = new Button(context);
            btnHouseRules.setText("Read house rules");
            btnHouseRules.setAllCaps(false);
            btnHouseRules.setTextColor(Color.WHITE);
            btnHouseRules.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d));
            btnLp.topMargin = (int) (14 * d);
            btnHouseRules.setLayoutParams(btnLp);
            btnHouseRules.setOnClickListener(v -> {
                dialog.dismiss();
                context.startActivity(new Intent(context, HouseRulesActivity.class));
            });
            container.addView(btnHouseRules);
        } else {
            Button btnEdit = new Button(context);
            btnEdit.setText("Edit Announcement");
            btnEdit.setAllCaps(false);
            btnEdit.setTextColor(Color.WHITE);
            btnEdit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d));
            btnLp.topMargin = (int) (14 * d);
            btnEdit.setLayoutParams(btnLp);
            btnEdit.setOnClickListener(v -> {
                dialog.dismiss();
                showEditAnnouncementDialog(context, db, tvBody.getText().toString());
            });
            container.addView(btnEdit);
        }

        db.collection("announcements").document("latest").get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String dateStr = doc.getString("updatedAt");
                String msgStr = doc.getString("message");
                if (dateStr != null && !dateStr.isEmpty()) tvDate.setText(dateStr);
                if (msgStr != null && !msgStr.isEmpty()) tvBody.setText(msgStr);
            }
        });

        dialog.show();
    }

    private static void showEditAnnouncementDialog(Context context, FirebaseFirestore db, String currentText) {
        EditText etMsg = new EditText(context);
        etMsg.setText(currentText);
        etMsg.setMinLines(3);

        new AlertDialog.Builder(context)
                .setTitle("Update Property Announcement")
                .setView(etMsg)
                .setPositiveButton("Publish", (d2, w2) -> {
                    String updatedText = etMsg.getText().toString().trim();
                    if (updatedText.isEmpty()) return;
                    String nowStr = new SimpleDateFormat("MMM d, yyyy | h:mm a", Locale.US).format(new Date());

                    Map<String, Object> data = new HashMap<>();
                    data.put("message", updatedText);
                    data.put("updatedAt", nowStr);

                    db.collection("announcements").document("latest").set(data)
                            .addOnSuccessListener(u ->
                                    Toast.makeText(context, "Property announcement published!", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}