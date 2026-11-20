package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsHistoryActivity extends AppCompatActivity {

    private static class NotificationItem {
        String docId, reqId, title, message, time, status;
        boolean unread;

        NotificationItem(String docId, String reqId, String title, String message, String time, String status, boolean unread) {
            this.docId = docId;
            this.reqId = reqId;
            this.title = title;
            this.message = message;
            this.time = time;
            this.status = status;
            this.unread = unread;
        }
    }

    private LinearLayout containerNotifications;
    private final List<NotificationItem> notifications = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_history);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvMarkAllRead = findViewById(R.id.tvMarkAllRead);
        containerNotifications = findViewById(R.id.containerNotifications);

        btnBack.setOnClickListener(v -> finish());

        tvMarkAllRead.setOnClickListener(v -> {
            for (NotificationItem item : notifications) {
                if (item.unread && item.docId != null) {
                    db.collection("notifications").document(item.docId).update("unread", false);
                }
                item.unread = false;
            }
            renderNotifications();
            Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
        });

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

        navNotifications.setOnClickListener(v ->
                Toast.makeText(this, "You are on Notifications", Toast.LENGTH_SHORT).show());

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, TenantProfileActivity.class));
            finish();
        });

        listenToNotifications();
    }

    private void listenToNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : "guest";

        firestoreListener = db.collection("notifications")
                .whereEqualTo("tenantUid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        db.collection("notifications")
                                .whereEqualTo("tenantUid", uid)
                                .get()
                                .addOnSuccessListener(s -> parseNotifications(s.getDocuments()));
                        return;
                    }
                    parseNotifications(snapshots != null ? snapshots.getDocuments() : null);
                });
    }

    private void parseNotifications(List<DocumentSnapshot> docs) {
        notifications.clear();
        if (docs != null) {
            for (DocumentSnapshot doc : docs) {
                String docId = doc.getId();
                String reqId = doc.getString("reqId") != null ? doc.getString("reqId") : "#REQ";
                String title = doc.getString("title") != null ? doc.getString("title") : "Notification";
                String message = doc.getString("message") != null ? doc.getString("message") : "";
                String time = doc.getString("time") != null ? doc.getString("time") : "";
                String status = doc.getString("status") != null ? doc.getString("status") : "Pending";
                Boolean unreadObj = doc.getBoolean("unread");
                boolean unread = unreadObj != null && unreadObj;

                notifications.add(new NotificationItem(docId, reqId, title, message, time, status, unread));
            }
        }
        renderNotifications();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

    private void renderNotifications() {
        containerNotifications.removeAllViews();

        if (notifications.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No notifications yet.\nUpdates from Admin will appear here.");
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvEmpty.setTextSize(14f);
            tvEmpty.setGravity(Gravity.CENTER);
            int pad = (int) (32 * getResources().getDisplayMetrics().density);
            tvEmpty.setPadding(pad, pad, pad, pad);
            containerNotifications.addView(tvEmpty);
            return;
        }

        for (NotificationItem item : notifications) {
            containerNotifications.addView(createNotificationCard(item));
        }
    }

    private CardView createNotificationCard(NotificationItem item) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int gray = ContextCompat.getColor(this, R.color.text_gray);

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = (int) (12 * d);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(item.unread ? Color.parseColor("#F0F5FF") : Color.WHITE);
        card.setRadius(12 * d);
        card.setCardElevation(2 * d);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * d);
        inner.setPadding(pad, pad, pad, pad);

        RelativeLayout topRow = new RelativeLayout(this);

        LinearLayout titleWrap = new LinearLayout(this);
        titleWrap.setOrientation(LinearLayout.HORIZONTAL);
        titleWrap.setGravity(Gravity.CENTER_VERTICAL);

        if (item.unread) {
            View dot = new View(this);
            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(navy);
            dot.setBackground(dotBg);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams((int) (8 * d), (int) (8 * d));
            dotParams.rightMargin = (int) (8 * d);
            titleWrap.addView(dot, dotParams);
        }

        TextView tvTitle = new TextView(this);
        tvTitle.setText(item.title);
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(14f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        titleWrap.addView(tvTitle);

        topRow.addView(titleWrap);

        TextView tvReqId = new TextView(this);
        tvReqId.setText(item.reqId);
        tvReqId.setTextColor(gray);
        tvReqId.setTextSize(11f);
        tvReqId.setTypeface(null, Typeface.BOLD);
        RelativeLayout.LayoutParams idParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        idParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        topRow.addView(tvReqId, idParams);

        inner.addView(topRow);

        TextView tvMsg = new TextView(this);
        tvMsg.setText(item.message);
        tvMsg.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvMsg.setTextSize(13f);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        msgParams.topMargin = (int) (6 * d);
        msgParams.bottomMargin = (int) (8 * d);
        tvMsg.setLayoutParams(msgParams);
        inner.addView(tvMsg);

        TextView tvTime = new TextView(this);
        tvTime.setText(item.time);
        tvTime.setTextColor(gray);
        tvTime.setTextSize(11f);
        inner.addView(tvTime);

        card.addView(inner);

        card.setOnClickListener(v -> {
            if (item.unread && item.docId != null) {
                db.collection("notifications").document(item.docId).update("unread", false);
            }
            item.unread = false;
            renderNotifications();

            Intent intent = new Intent(NotificationsHistoryActivity.this, RequestDetailsActivity.class);
            intent.putExtra("IS_ADMIN", false);
            intent.putExtra("REQ_ID", item.reqId);
            intent.putExtra("STATUS", item.status);
            intent.putExtra("DATE", item.time);
            intent.putExtra("DESCRIPTION", item.message);
            startActivity(intent);
        });

        return card;
    }
}