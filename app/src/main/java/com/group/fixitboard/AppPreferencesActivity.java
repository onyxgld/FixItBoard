package com.group.fixitboard;

import android.content.SharedPreferences;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AppPreferencesActivity extends AppCompatActivity {

    private static final String PREF_NAME = "FixItBoardAppPrefs";
    private boolean isSoundOn = true;
    private boolean isVibrationOn = true;

    private interface OnToggleChangedListener {
        void onChanged(boolean newState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);

        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int bgSlate = ContextCompat.getColor(this, R.color.slate_bg);
        int cardBg = ContextCompat.getColor(this, R.color.white);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);
        int textGray = ContextCompat.getColor(this, R.color.text_gray);
        int dividerColor = ContextCompat.getColor(this, R.color.slate_light);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        isSoundOn = prefs.getBoolean("sound_effects", true);
        isVibrationOn = prefs.getBoolean("vibration", true);

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
        headerLp.bottomMargin = (int) (20 * d);
        header.setLayoutParams(headerLp);

        ImageButton btnBack = new ImageButton(this);
        btnBack.setImageResource(android.R.drawable.ic_menu_revert);
        btnBack.setColorFilter(navy);
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setPadding(0, (int) (6 * d), (int) (14 * d), (int) (6 * d));
        btnBack.setOnClickListener(v -> finish());
        header.addView(btnBack);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("App Preferences");
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(20f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        header.addView(tvTitle);
        root.addView(header);

        // Theme Cards Row (Light Mode / Dark Mode)
        boolean isDark = ThemeHelper.isDarkMode(this);
        LinearLayout themeRow = new LinearLayout(this);
        themeRow.setOrientation(LinearLayout.HORIZONTAL);
        themeRow.setWeightSum(2f);

        LinearLayout lightCard = buildThemeCard("☀", "Light Mode", !isDark ? "Default / selected" : "",
                !isDark, d, navy, textPrimary, textGray, cardBg, true);
        lightCard.setOnClickListener(v -> {
            if (ThemeHelper.isDarkMode(this)) {
                ThemeHelper.setDarkMode(this, false);
                recreate();
            }
        });

        LinearLayout darkCard = buildThemeCard("☾", "Dark Mode", isDark ? "Selected" : "",
                isDark, d, navy, textPrimary, textGray, cardBg, false);
        darkCard.setOnClickListener(v -> {
            if (!ThemeHelper.isDarkMode(this)) {
                ThemeHelper.setDarkMode(this, true);
                recreate();
            }
        });

        themeRow.addView(lightCard);
        themeRow.addView(darkCard);
        root.addView(themeRow);

        // Divider
        View div = new View(this);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * d));
        divLp.topMargin = (int) (20 * d);
        divLp.bottomMargin = (int) (16 * d);
        div.setLayoutParams(divLp);
        div.setBackgroundColor(dividerColor);
        root.addView(div);

        // Language Section (Matches Figma modal trigger)
        TextView tvLangLabel = new TextView(this);
        tvLangLabel.setText("Language");
        tvLangLabel.setTextColor(navy);
        tvLangLabel.setTextSize(15f);
        tvLangLabel.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams langLblLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        langLblLp.bottomMargin = (int) (8 * d);
        tvLangLabel.setLayoutParams(langLblLp);
        root.addView(tvLangLabel);

        RelativeLayout langBox = new RelativeLayout(this);
        GradientDrawable langBg = new GradientDrawable();
        langBg.setCornerRadius(10 * d);
        langBg.setColor(cardBg);
        langBg.setStroke((int) (1 * d), Color.parseColor("#CBD5E1"));
        langBox.setBackground(langBg);
        langBox.setPadding((int) (14 * d), 0, (int) (14 * d), 0);
        LinearLayout.LayoutParams langBoxLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d));
        langBoxLp.bottomMargin = (int) (20 * d);
        langBox.setLayoutParams(langBoxLp);

        TextView tvSelectedLang = new TextView(this);
        tvSelectedLang.setText("English");
        tvSelectedLang.setTextColor(textPrimary);
        tvSelectedLang.setTextSize(14f);
        RelativeLayout.LayoutParams selLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        selLp.addRule(RelativeLayout.CENTER_VERTICAL);
        langBox.addView(tvSelectedLang, selLp);

        TextView tvChevron = new TextView(this);
        tvChevron.setText("▾");
        tvChevron.setTextColor(navy);
        tvChevron.setTextSize(16f);
        RelativeLayout.LayoutParams chevLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        chevLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        chevLp.addRule(RelativeLayout.CENTER_VERTICAL);
        langBox.addView(tvChevron, chevLp);

        langBox.setOnClickListener(v -> showLanguageInfoModal());
        root.addView(langBox);

        // Other Preferences
        TextView tvOther = new TextView(this);
        tvOther.setText("Other Preferences");
        tvOther.setTextColor(navy);
        tvOther.setTextSize(15f);
        tvOther.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams otherLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        otherLp.bottomMargin = (int) (10 * d);
        tvOther.setLayoutParams(otherLp);
        root.addView(tvOther);

        addToggleRow(root, "Sound Effects", isSoundOn, d, textPrimary, newState -> {
            isSoundOn = newState;
            prefs.edit().putBoolean("sound_effects", isSoundOn).apply();
            Toast.makeText(this, "Sound Effects " + (isSoundOn ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        addToggleRow(root, "Vibration", isVibrationOn, d, textPrimary, newState -> {
            isVibrationOn = newState;
            prefs.edit().putBoolean("vibration", isVibrationOn).apply();
            Toast.makeText(this, "Vibration " + (isVibrationOn ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Footer Note
        TextView tvNote = new TextView(this);
        tvNote.setText("Theme applies throughout the app. Sound and vibration choices are saved as preferences.");
        tvNote.setTextColor(textGray);
        tvNote.setTextSize(11.5f);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noteLp.topMargin = (int) (18 * d);
        tvNote.setLayoutParams(noteLp);
        root.addView(tvNote);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int basePad = (int) (20 * getResources().getDisplayMetrics().density);
            v.setPadding(basePad + insets.left, basePad + insets.top, basePad + insets.right, basePad + insets.bottom);
            return windowInsets;
        });

        setContentView(scrollView);
    }

    private void showLanguageInfoModal() {
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
        tvTitle.setText("Language");
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(20f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        modalBox.addView(tvTitle);

        TextView tvBody = new TextView(this);
        tvBody.setText("English is the currently supported language for this version. Additional languages are not yet implemented.");
        tvBody.setTextColor(textPrimary);
        tvBody.setTextSize(14f);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bodyLp.topMargin = (int) (14 * d);
        bodyLp.bottomMargin = (int) (28 * d);
        tvBody.setLayoutParams(bodyLp);
        modalBox.addView(tvBody);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(modalBox)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnDone = new Button(this);
        btnDone.setText("Done");
        btnDone.setAllCaps(false);
        btnDone.setTextSize(14.5f);
        btnDone.setTypeface(null, Typeface.BOLD);
        btnDone.setTextColor(ThemeHelper.isDarkMode(this) ? Color.parseColor("#0C1728") : Color.WHITE);
        btnDone.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        btnDone.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d)));
        btnDone.setOnClickListener(v -> dialog.dismiss());
        modalBox.addView(btnDone);

        dialog.show();
    }

    private LinearLayout buildThemeCard(String icon, String label, String sublabel, boolean selected, float d,
                                        int navy, int textPrimary, int textGray, int cardBgColor, boolean isLeft) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        int cardPad = (int) (18 * d);
        card.setPadding(cardPad, cardPad, cardPad, cardPad);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        if (isLeft) {
            lp.rightMargin = (int) (8 * d);
        } else {
            lp.leftMargin = (int) (8 * d);
        }
        card.setLayoutParams(lp);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(14 * d);
        bg.setColor(cardBgColor);
        bg.setStroke(selected ? (int) (2.5f * d) : (int) (1 * d),
                selected ? navy : Color.parseColor("#CBD5E1"));
        card.setBackground(bg);

        TextView tvIcon = new TextView(this);
        tvIcon.setText(icon);
        tvIcon.setTextSize(28f);
        tvIcon.setTextColor(selected ? navy : textPrimary);
        tvIcon.setGravity(Gravity.CENTER);
        card.addView(tvIcon);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(14f);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setTextColor(selected ? navy : textPrimary);
        tvLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lblLp.topMargin = (int) (8 * d);
        tvLabel.setLayoutParams(lblLp);
        card.addView(tvLabel);

        TextView tvSub = new TextView(this);
        tvSub.setText(sublabel);
        tvSub.setTextSize(11f);
        tvSub.setTextColor(textGray);
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setVisibility(sublabel.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = (int) (3 * d);
        tvSub.setLayoutParams(subLp);
        card.addView(tvSub);

        return card;
    }

    private void addToggleRow(LinearLayout parent, String title, boolean initialState, float d,
                              int textPrimary, OnToggleChangedListener listener) {
        RelativeLayout row = new RelativeLayout(this);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = (int) (6 * d);
        rowLp.bottomMargin = (int) (6 * d);
        row.setLayoutParams(rowLp);
        row.setPadding(0, (int) (6 * d), 0, (int) (6 * d));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(textPrimary);
        tvTitle.setTextSize(14f);
        RelativeLayout.LayoutParams titleLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleLp.addRule(RelativeLayout.CENTER_VERTICAL);
        row.addView(tvTitle, titleLp);

        TextView badge = new TextView(this);
        badge.setTextSize(12f);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setPadding((int) (14 * d), (int) (4 * d), (int) (14 * d), (int) (4 * d));
        RelativeLayout.LayoutParams badgeLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        badgeLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        badgeLp.addRule(RelativeLayout.CENTER_VERTICAL);
        row.addView(badge, badgeLp);

        final boolean[] state = {initialState};
        stylePillBadge(badge, state[0], d);

        row.setOnClickListener(v -> {
            state[0] = !state[0];
            stylePillBadge(badge, state[0], d);
            listener.onChanged(state[0]);
        });

        parent.addView(row);
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
}