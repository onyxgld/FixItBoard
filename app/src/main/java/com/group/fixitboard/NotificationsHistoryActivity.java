package com.group.fixitboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_history);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvMarkAllRead = findViewById(R.id.tvMarkAllRead);
        containerNotifications = findViewById(R.id.containerNotifications);

        btnBack.setOnClickListener(v -> finish());

        // Transform top-right action into Figma three-dots menu (⋮)
        tvMarkAllRead.setText("⋮");
        tvMarkAllRead.setTextSize(22f);
        int padDots = (int) (10 * getResources().getDisplayMetrics().density);
        tvMarkAllRead.setPadding(padDots, padDots / 2, padDots / 2, padDots / 2);
        tvMarkAllRead.setOnClickListener(v -> showMarkAllReadModal());

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

    private void showMarkAllReadModal() {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int cardBg = ContextCompat.getColor(this, R.color.white);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);

        LinearLayout modalBox = new LinearLayout(this);
        modalBox.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (24 * d);
        modalBox.setPadding(pad, pad, pad, pad);

        GradientDrawable boxBg = new GradientDrawable();
        boxBg.setCornerRadius(18 * d);
        boxBg.setColor(cardBg);
        modalBox.setBackground(boxBg);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Notifications");
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(20f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        modalBox.addView(tvTitle);

        TextView tvBody = new TextView(this);
        tvBody.setText("Mark all visible notifications as read?");
        tvBody.setTextColor(textPrimary);
        tvBody.setTextSize(14f);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bodyLp.topMargin = (int) (14 * d);
        bodyLp.bottomMargin = (int) (26 * d);
        tvBody.setLayoutParams(bodyLp);
        modalBox.addView(tvBody);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(modalBox)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvCancel = new TextView(this);
        tvCancel.setText("Cancel");
        tvCancel.setTextColor(navy);
        tvCancel.setTextSize(14f);
        tvCancel.setTypeface(null, Typeface.BOLD);
        tvCancel.setGravity(Gravity.CENTER);
        tvCancel.setPadding(0, (int) (10 * d), 0, (int) (12 * d));
        tvCancel.setOnClickListener(v -> dialog.dismiss());
        modalBox.addView(tvCancel);

        Button btnConfirm = new Button(this);
        btnConfirm.setText("Mark all as read");
        btnConfirm.setAllCaps(false);
        btnConfirm.setTextSize(14.5f);
        btnConfirm.setTypeface(null, Typeface.BOLD);
        btnConfirm.setTextColor(ThemeHelper.isDarkMode(this) ? Color.parseColor("#0C1728") : Color.WHITE);
        btnConfirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d));
        btnConfirm.setLayoutParams(btnLp);
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            for (NotificationItem item : notifications) {
                if (item.unread && item.docId != null) {
                    db.collection("notifications").document(item.docId).update("unread", false);
                }
                item.unread = false;
            }
            renderNotifications();
            Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
        });
        modalBox.addView(btnConfirm);

        dialog.show();
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
        float d = getResources().getDisplayMetrics().density;

        if (notifications.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No notifications yet.\nUpdates from Admin will appear here.");
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvEmpty.setTextSize(14f);
            tvEmpty.setGravity(Gravity.CENTER);
            int pad = (int) (32 * d);
            tvEmpty.setPadding(pad, pad, pad, pad);
            containerNotifications.addView(tvEmpty);
            return;
        }

        int unreadCount = 0;
        for (NotificationItem item : notifications) {
            if (item.unread) unreadCount++;
        }

        TextView tvUnreadCount = new TextView(this);
        tvUnreadCount.setText(unreadCount + " unread notification" + (unreadCount == 1 ? "" : "s"));
        tvUnreadCount.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvUnreadCount.setTextSize(12.5f);
        LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        countLp.bottomMargin = (int) (12 * d);
        tvUnreadCount.setLayoutParams(countLp);
        containerNotifications.addView(tvUnreadCount);

        for (NotificationItem item : notifications) {
            containerNotifications.addView(createNotificationCard(item));
        }
    }

    private CardView createNotificationCard(NotificationItem item) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int gray = ContextCompat.getColor(this, R.color.text_gray);
        int cardReadBg = ContextCompat.getColor(this, R.color.white);
        int cardUnreadBg = ThemeHelper.isDarkMode(this)
                ? Color.parseColor("#1E314E")
                : Color.parseColor("#F0F5FF");

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = (int) (12 * d);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(item.unread ? cardUnreadBg : cardReadBg);
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

        RelativeLayout bottomRow = new RelativeLayout(this);

        TextView tvTime = new TextView(this);
        tvTime.setText(item.time);
        tvTime.setTextColor(gray);
        tvTime.setTextSize(11f);
        bottomRow.addView(tvTime);

        TextView tvChevron = new TextView(this);
        tvChevron.setText("›");
        tvChevron.setTextColor(navy);
        tvChevron.setTextSize(18f);
        RelativeLayout.LayoutParams chevLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        chevLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        bottomRow.addView(tvChevron, chevLp);

        inner.addView(bottomRow);
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