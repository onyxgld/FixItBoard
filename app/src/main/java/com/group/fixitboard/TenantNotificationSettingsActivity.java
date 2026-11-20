package com.group.fixitboard;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class TenantNotificationSettingsActivity extends AppCompatActivity {

    private static final String PREF_NAME = "FixItBoardTenantNotifPrefs";
    private final String[] keys = {
            "requestUpdates",
            "requestApproved",
            "inProgress",
            "requestCompleted",
            "announcements"
    };
    private final String[] titles = {
            "Request Updates",
            "Request Approved",
            "In Progress",
            "Request Completed",
            "Announcements"
    };
    private final String[] subtitles = {
            "Get updates about your maintenance requests",
            "Know when your request is approved",
            "Get notified when repairs begin",
            "Know when maintenance is completed",
            "Receive building announcement"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);

        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int bgSlate = ContextCompat.getColor(this, R.color.slate_bg);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);
        int textGray = ContextCompat.getColor(this, R.color.text_gray);
        int dividerColor = ContextCompat.getColor(this, R.color.slate_light);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(bgSlate);
        scrollView.setFillViewport(true);
        scrollView.setFitsSystemWindows(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * d);
        root.setPadding(pad, pad, pad, pad);
        scrollView.addView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerLp.bottomMargin = (int) (14 * d);
        header.setLayoutParams(headerLp);

        ImageButton btnBack = new ImageButton(this);
        btnBack.setImageResource(android.R.drawable.ic_menu_revert);
        btnBack.setColorFilter(navy);
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setPadding(0, (int) (6 * d), (int) (14 * d), (int) (6 * d));
        btnBack.setOnClickListener(v -> finish());
        header.addView(btnBack);

        TextView tvHeader = new TextView(this);
        tvHeader.setText("Notifications");
        tvHeader.setTextColor(navy);
        tvHeader.setTextSize(20f);
        tvHeader.setTypeface(null, Typeface.BOLD);
        header.addView(tvHeader);
        root.addView(header);

        // Subtitle Description
        TextView tvDesc = new TextView(this);
        tvDesc.setText("Choose which notifications you want to receive.");
        tvDesc.setTextColor(textPrimary);
        tvDesc.setTextSize(14f);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        descLp.bottomMargin = (int) (14 * d);
        tvDesc.setLayoutParams(descLp);
        root.addView(tvDesc);

        addDivider(root, d, dividerColor);

        for (int i = 0; i < keys.length; i++) {
            final int idx = i;
            final boolean[] state = {prefs.getBoolean(keys[i], true)};

            RelativeLayout row = new RelativeLayout(this);
            row.setPadding(0, (int) (14 * d), 0, (int) (14 * d));

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            RelativeLayout.LayoutParams colLp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            colLp.rightMargin = (int) (72 * d);
            textCol.setLayoutParams(colLp);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(titles[i]);
            tvTitle.setTextColor(navy);
            tvTitle.setTextSize(14.5f);
            tvTitle.setTypeface(null, Typeface.BOLD);
            textCol.addView(tvTitle);

            TextView tvSub = new TextView(this);
            tvSub.setText(subtitles[i]);
            tvSub.setTextColor(textGray);
            tvSub.setTextSize(13f);
            LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            subLp.topMargin = (int) (3 * d);
            tvSub.setLayoutParams(subLp);
            textCol.addView(tvSub);

            row.addView(textCol);

            TextView badge = new TextView(this);
            badge.setTextSize(12f);
            badge.setTypeface(null, Typeface.BOLD);
            badge.setPadding((int) (14 * d), (int) (4 * d), (int) (14 * d), (int) (4 * d));
            RelativeLayout.LayoutParams badgeLp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            badgeLp.addRule(RelativeLayout.ALIGN_PARENT_END);
            badgeLp.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(badge, badgeLp);

            stylePillBadge(badge, state[0], d);

            row.setOnClickListener(v -> {
                state[0] = !state[0];
                prefs.edit().putBoolean(keys[idx], state[0]).apply();
                stylePillBadge(badge, state[0], d);
                syncPreferencesToFirestore(prefs);
                Toast.makeText(this, titles[idx] + " turned " + (state[0] ? "On" : "Off"), Toast.LENGTH_SHORT).show();
            });

            root.addView(row);
            addDivider(root, d, dividerColor);
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int basePad = (int) (20 * getResources().getDisplayMetrics().density);
            v.setPadding(basePad + insets.left, basePad + insets.top, basePad + insets.right, basePad + insets.bottom);
            return windowInsets;
        });

        setContentView(scrollView);
    }

    private void addDivider(LinearLayout parent, float d, int color) {
        View div = new View(this);
        div.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * d)));
        div.setBackgroundColor(color);
        parent.addView(div);
    }

    private void stylePillBadge(TextView badge, boolean isOn, float d) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(8 * d);
        if (isOn) {
            badge.setText("On");
            bg.setColor(ContextCompat.getColor(this, R.color.status_completed_bg));
            badge.setTextColor(ContextCompat.getColor(this, R.color.status_completed_text));
        } else {
            badge.setText("Off");
            bg.setColor(ContextCompat.getColor(this, R.color.status_pending_bg));
            badge.setTextColor(ContextCompat.getColor(this, R.color.status_pending_text));
        }
        badge.setBackground(bg);
    }

    private void syncPreferencesToFirestore(SharedPreferences prefs) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        Map<String, Object> map = new HashMap<>();
        for (String key : keys) {
            map.put(key, prefs.getBoolean(key, true));
        }
        Map<String, Object> update = new HashMap<>();
        update.put("tenantNotifPreferences", map);
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .set(update, SetOptions.merge());
    }
}