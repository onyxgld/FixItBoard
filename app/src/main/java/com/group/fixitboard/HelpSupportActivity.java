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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HelpSupportActivity extends AppCompatActivity {

    private LinearLayout container;
    private String currentSubPage = "MENU";
    private boolean isAdmin = false;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false)
                || (user != null && user.getEmail() != null && user.getEmail().toLowerCase().startsWith("admin"));

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
                isAdmin ? "Admin Help & Support" : "How can we help you?",
                isAdmin ? "Manage portal guides or contact technical support" : "Find an answer or report an issue"
        ));

        container.addView(buildMenuRow("Frequently Asked Questions", v -> showFaq()));
        container.addView(buildDivider());
        container.addView(buildMenuRow(
                isAdmin ? "Contact Technical Support" : "Contact Administrator",
                v -> showContactSupport()
        ));
        container.addView(buildDivider());
        container.addView(buildMenuRow("Report a Problem", v -> showReportProblem()));
        container.addView(buildDivider());
    }

    private void showFaq() {
        currentSubPage = "FAQ";
        container.removeAllViews();

        container.addView(buildHeaderCard(
                android.R.drawable.ic_menu_help,
                isAdmin ? "Admin Portal FAQ" : "Frequently Asked Questions",
                null
        ));

        if (isAdmin) {
            addSectionBlock("How do I update a maintenance request status?",
                    "Open any request card from the Admin Dashboard, select Pending, In Progress, or Completed, add optional admin notes, and tap Save Changes.");
            addSectionBlock("How do I assign tenants to units?",
                    "Go to Admin Profile → Settings → User Management to assign units/roles, or open Property Management to update unit occupancy.");
            addSectionBlock("How do tenants receive updates?",
                    "Whenever you save a status update on a request, FixItBoard automatically sends a real-time notification to the tenant's Notification tab.");
            addSectionBlock("How do I manage building units?",
                    "Open Admin Settings → Property Management and tap the + icon to add new units or tap an existing unit card to update its status.");
        } else {
            addSectionBlock("How do I report a maintenance issue?",
                    "Go to New Request on your dashboard, select the problem type, enter the location and description, and submit your request.");
            addSectionBlock("How can I check my request?",
                    "Open Requests from the bottom navigation bar to view the real-time status and admin updates of your reported issue.");
            addSectionBlock("How long will my request take?",
                    "Response times depend on the type and urgency of the issue. You will receive updates through the Notification tab.");
            addSectionBlock("What if my issue is urgent?",
                    "For emergencies that may cause immediate danger or serious property damage, contact your administrator immediately.");
        }
    }

    private void showContactSupport() {
        currentSubPage = "CONTACT";
        container.removeAllViews();

        container.addView(buildHeaderCard(
                android.R.drawable.ic_menu_myplaces,
                isAdmin ? "Contact Technical Support" : "Contact Administrator",
                null
        ));

        if (isAdmin) {
            addSectionBlock("System & Developer Support",
                    "Need technical assistance with Firebase sync, user permissions, or portal configuration? Reach out to the FixItBoard support team.");
            addSectionBlock("Support Details",
                    "• Team: FixItBoard Technical Support\n• Email: support@fixitboard.com\n• Hotline: +63 917 800 1234\n• Support Hours: Monday – Saturday, 8:00 AM – 6:00 PM");
        } else {
            addSectionBlock("Contact Admin",
                    "Need help with your rental unit or have a concern? Contact your property administrator.");
            addSectionBlock("Administrator Details",
                    "• Name: Property Admin\n• Email: admin@fixitboard.com\n• Phone: +63 912 345 6789\n• Office Hours: Monday – Friday, 8:00 AM – 5:00 PM");
        }

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
        btnEnquiry.setOnClickListener(v -> showEnquiryDialog("Enquiry"));
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
        btnReport.setOnClickListener(v -> showEnquiryDialog("App Problem Report"));
        container.addView(btnReport);
    }

    private void showEnquiryDialog(String category) {
        EditText etMsg = new EditText(this);
        etMsg.setHint("Type your message here...");
        etMsg.setMinLines(3);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        etMsg.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle(isAdmin ? "Send Support Ticket" : "Send Message to Admin")
                .setView(etMsg)
                .setPositiveButton("Send", (d, w) -> {
                    String msg = etMsg.getText().toString().trim();
                    if (msg.isEmpty()) {
                        Toast.makeText(this, "Please enter a message first.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Map<String, Object> ticket = new HashMap<>();
                    ticket.put("senderUid", user != null ? user.getUid() : "guest");
                    ticket.put("senderEmail", user != null && user.getEmail() != null ? user.getEmail() : "unknown");
                    ticket.put("role", isAdmin ? "admin" : "tenant");
                    ticket.put("category", category);
                    ticket.put("message", msg);
                    ticket.put("status", "Open");
                    ticket.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("support_tickets").add(ticket)
                            .addOnSuccessListener(ref ->
                                    Toast.makeText(this, "Submitted! Ticket ID: " + ref.getId().substring(0, 6).toUpperCase(), Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
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