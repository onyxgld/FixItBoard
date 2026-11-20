package com.group.fixitboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class HelpSupportActivity extends AppCompatActivity {

    private LinearLayout container;
    private String currentSubPage = "MENU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        container = findViewById(R.id.containerHelpSupport);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> handleBackNavigation());
        showMenu();
    }

    private void handleBackNavigation() {
        if (!"MENU".equals(currentSubPage)) {
            showMenu();
        } else {
            finish();
        }
    }

    private void showMenu() {
        currentSubPage = "MENU";
        container.removeAllViews();

        container.addView(buildHeaderCard(
                android.R.drawable.ic_menu_call,
                "How can we help you?",
                "Find an answer or report an issue"
        ));

        container.addView(buildMenuRow("Frequently Asked Questions", v -> showFaq()));
        container.addView(buildDivider());
        container.addView(buildMenuRow("Contact Administrator", v -> showContactAdmin()));
        container.addView(buildDivider());
        container.addView(buildMenuRow("Report a Problem", v -> showReportProblem()));
        container.addView(buildDivider());
    }

    private void showFaq() {
        currentSubPage = "FAQ";
        container.removeAllViews();

        container.addView(buildHeaderCard(
                android.R.drawable.ic_menu_help,
                "Frequently Asked Questions",
                null
        ));

        addSectionBlock("How do I report a maintenance issue?",
                "Go to New Request on your dashboard, select the problem type, enter the location and description, and submit your request.");
        addSectionBlock("How can I check my request?",
                "Open Requests from the bottom navigation bar to view the real-time status and admin updates of your reported issue.");
        addSectionBlock("How long will my request take?",
                "Response times depend on the type and urgency of the issue. You will receive updates through the Notification tab.");
        addSectionBlock("What if my issue is urgent?",
                "For emergencies that may cause immediate danger or serious property damage, contact your administrator immediately.");
    }

    private void showContactAdmin() {
        currentSubPage = "CONTACT";
        container.removeAllViews();

        container.addView(buildHeaderCard(
                android.R.drawable.ic_menu_myplaces,
                "Contact Administrator",
                null
        ));

        addSectionBlock("Contact Admin",
                "Need help with your rental unit or have a concern? Contact your property administrator.");

        addSectionBlock("Administrator Details",
                "• Name: Property Admin\n• Email: admin@fixitboard.com\n• Phone: +63 912 345 6789\n• Office Hours: Monday – Friday, 8:00 AM – 5:00 PM");

        float d = getResources().getDisplayMetrics().density;
        Button btnEnquiry = new Button(this);
        btnEnquiry.setText("Write an enquiry");
        btnEnquiry.setAllCaps(false);
        btnEnquiry.setTextColor(Color.WHITE);
        btnEnquiry.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.navy_primary)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        params.topMargin = (int) (12 * d);
        btnEnquiry.setLayoutParams(params);
        btnEnquiry.setOnClickListener(v -> showEnquiryDialog());
        container.addView(btnEnquiry);
    }

    private void showReportProblem() {
        currentSubPage = "REPORT";
        container.removeAllViews();

        container.addView(buildHeaderCard(
                android.R.drawable.ic_dialog_alert,
                "Problem report",
                null
        ));

        addSectionBlock("App problem report",
                "Having trouble with the Fixit Board app? Let us know what went wrong.");

        addSectionBlock("Problem type",
                "• App error\n• Login / Account Issue\n• Button or Feature not working\n• Incorrect Information\n• Display / Layout Issue");

        float d = getResources().getDisplayMetrics().density;
        Button btnReport = new Button(this);
        btnReport.setText("Submit App Problem");
        btnReport.setAllCaps(false);
        btnReport.setTextColor(Color.WHITE);
        btnReport.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.navy_primary)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        params.topMargin = (int) (12 * d);
        btnReport.setLayoutParams(params);
        btnReport.setOnClickListener(v -> showEnquiryDialog());
        container.addView(btnReport);
    }

    private void showEnquiryDialog() {
        EditText etMsg = new EditText(this);
        etMsg.setHint("Type your message here...");
        etMsg.setMinLines(3);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        etMsg.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle("Send Message to Admin")
                .setView(etMsg)
                .setPositiveButton("Send", (d, w) ->
                        Toast.makeText(this, "Your message has been sent to the administrator.", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private CardView buildHeaderCard(int iconRes, String title, String subtitle) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = (int) (20 * d);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(12 * d);
        card.setCardElevation(2 * d);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setGravity(Gravity.CENTER);
        int pad = (int) (20 * d);
        inner.setPadding(pad, pad, pad, pad);

        ImageView iv = new ImageView(this);
        iv.setImageResource(iconRes);
        iv.setColorFilter(navy);
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams((int) (42 * d), (int) (42 * d));
        ivParams.bottomMargin = (int) (10 * d);
        iv.setLayoutParams(ivParams);
        inner.addView(iv);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        inner.addView(tvTitle);

        if (subtitle != null) {
            TextView tvSub = new TextView(this);
            tvSub.setText(subtitle);
            tvSub.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvSub.setTextSize(12f);
            tvSub.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            subParams.topMargin = (int) (4 * d);
            tvSub.setLayoutParams(subParams);
            inner.addView(tvSub);
        }

        card.addView(inner);
        return card;
    }

    private LinearLayout buildMenuRow(String label, View.OnClickListener listener) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (52 * d)));
        row.setOnClickListener(listener);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(navy);
        tvLabel.setTextSize(15f);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvLabel);

        TextView tvArrow = new TextView(this);
        tvArrow.setText("›");
        tvArrow.setTextColor(navy);
        tvArrow.setTextSize(20f);
        row.addView(tvArrow);

        return row;
    }

    private void addSectionBlock(String heading, String body) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int gray = ContextCompat.getColor(this, R.color.text_gray);

        TextView tvH = new TextView(this);
        tvH.setText(heading);
        tvH.setTextColor(navy);
        tvH.setTextSize(14f);
        tvH.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams hParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hParams.bottomMargin = (int) (4 * d);
        tvH.setLayoutParams(hParams);
        container.addView(tvH);

        TextView tvB = new TextView(this);
        tvB.setText(body);
        tvB.setTextColor(gray);
        tvB.setTextSize(13f);
        LinearLayout.LayoutParams bParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bParams.bottomMargin = (int) (14 * d);
        tvB.setLayoutParams(bParams);
        container.addView(tvB);

        container.addView(buildDivider());
    }

    private View buildDivider() {
        float d = getResources().getDisplayMetrics().density;
        View div = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * d));
        params.bottomMargin = (int) (14 * d);
        div.setLayoutParams(params);
        div.setBackgroundColor(ContextCompat.getColor(this, R.color.slate_light));
        return div;
    }
}