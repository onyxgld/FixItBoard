package com.group.fixitboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HelpSupportActivity extends AppCompatActivity {

    private LinearLayout container;
    private String currentSubPage = "MENU";
    private boolean isAdmin = false;
    private FirebaseFirestore db;

    private String selectedProblemCategory = "";
    private String attachedScreenshotBase64 = "";
    private ImageView ivScreenshotPreview;
    private TextView tvUploadTitle, tvUploadSubtitle;

    private final ActivityResultLauncher<String> pickScreenshotLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), (Uri uri) -> {
                if (uri != null) {
                    String encoded = encodeUriToBase64(uri);
                    if (encoded != null) {
                        attachedScreenshotBase64 = encoded;
                        if (ivScreenshotPreview != null) {
                            ivScreenshotPreview.setVisibility(View.VISIBLE);
                            ivScreenshotPreview.setImageURI(uri);
                        }
                        if (tvUploadTitle != null) tvUploadTitle.setText("Screenshot Attached ✓");
                        if (tvUploadSubtitle != null) tvUploadSubtitle.setText("Tap to change screenshot");
                    } else {
                        Toast.makeText(this, "Could not load selected screenshot.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
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
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        Button btnEnquiry = new Button(this);
        btnEnquiry.setText("Write an enquiry");
        btnEnquiry.setAllCaps(false);
        btnEnquiry.setTextColor(ThemeHelper.isDarkMode(this) ? Color.parseColor("#0C1728") : Color.WHITE);
        btnEnquiry.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        params.topMargin = (int) (12 * d);
        btnEnquiry.setLayoutParams(params);
        btnEnquiry.setOnClickListener(v -> showEnquiryDialog("Enquiry"));
        container.addView(btnEnquiry);
    }

    private void showReportProblem() {
        currentSubPage = "REPORT";
        selectedProblemCategory = "";
        attachedScreenshotBase64 = "";
        container.removeAllViews();

        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int cardBg = ContextCompat.getColor(this, R.color.white);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);
        int textGray = ContextCompat.getColor(this, R.color.text_gray);

        container.addView(buildHeaderCard(
                android.R.drawable.ic_dialog_alert,
                "Problem Report",
                null
        ));

        TextView tvFormTitle = new TextView(this);
        tvFormTitle.setText("App problem report");
        tvFormTitle.setTextColor(navy);
        tvFormTitle.setTextSize(16f);
        tvFormTitle.setTypeface(null, Typeface.BOLD);
        container.addView(tvFormTitle);

        TextView tvFormSub = new TextView(this);
        tvFormSub.setText("Please provide the details of the issue you encountered.");
        tvFormSub.setTextColor(textGray);
        tvFormSub.setTextSize(13f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = (int) (4 * d);
        subLp.bottomMargin = (int) (14 * d);
        tvFormSub.setLayoutParams(subLp);
        container.addView(tvFormSub);

        container.addView(buildDivider());

        // 1. Problem Category Selector (triggers Figma Problem Category Overlay)
        addFieldLabel("Problem Category", navy, d);

        RelativeLayout catBox = new RelativeLayout(this);
        GradientDrawable catBg = new GradientDrawable();
        catBg.setCornerRadius(10 * d);
        catBg.setColor(cardBg);
        catBg.setStroke((int) (1 * d), Color.parseColor("#CBD5E1"));
        catBox.setBackground(catBg);
        catBox.setPadding((int) (14 * d), 0, (int) (14 * d), 0);
        LinearLayout.LayoutParams catLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        catLp.bottomMargin = (int) (14 * d);
        catBox.setLayoutParams(catLp);

        TextView tvSelectedCat = new TextView(this);
        tvSelectedCat.setText("Select a category");
        tvSelectedCat.setTextColor(textGray);
        tvSelectedCat.setTextSize(14f);
        RelativeLayout.LayoutParams selLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        selLp.addRule(RelativeLayout.CENTER_VERTICAL);
        catBox.addView(tvSelectedCat, selLp);

        TextView tvArrow = new TextView(this);
        tvArrow.setText("▾");
        tvArrow.setTextColor(navy);
        tvArrow.setTextSize(16f);
        RelativeLayout.LayoutParams arrLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        arrLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        arrLp.addRule(RelativeLayout.CENTER_VERTICAL);
        catBox.addView(tvArrow, arrLp);

        catBox.setOnClickListener(v -> showProblemCategoryOverlay(category -> {
            selectedProblemCategory = category;
            tvSelectedCat.setText(category);
            tvSelectedCat.setTextColor(textPrimary);
        }));
        container.addView(catBox);

        // 2. Issue Title
        addFieldLabel("Issue Title", navy, d);

        EditText etIssueTitle = new EditText(this);
        etIssueTitle.setHint("Enter a short issue title");
        etIssueTitle.setHintTextColor(textGray);
        etIssueTitle.setTextColor(textPrimary);
        etIssueTitle.setTextSize(14f);
        GradientDrawable titleBg = new GradientDrawable();
        titleBg.setCornerRadius(10 * d);
        titleBg.setColor(cardBg);
        titleBg.setStroke((int) (1 * d), Color.parseColor("#CBD5E1"));
        etIssueTitle.setBackground(titleBg);
        etIssueTitle.setPadding((int) (14 * d), 0, (int) (14 * d), 0);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d));
        titleLp.bottomMargin = (int) (14 * d);
        etIssueTitle.setLayoutParams(titleLp);
        container.addView(etIssueTitle);

        // 3. Description
        addFieldLabel("Description", navy, d);

        EditText etIssueDesc = new EditText(this);
        etIssueDesc.setHint("Describe what happened and how to reproduce it...");
        etIssueDesc.setHintTextColor(textGray);
        etIssueDesc.setTextColor(textPrimary);
        etIssueDesc.setTextSize(14f);
        etIssueDesc.setGravity(Gravity.TOP | Gravity.START);
        etIssueDesc.setMinLines(3);
        GradientDrawable descBg = new GradientDrawable();
        descBg.setCornerRadius(10 * d);
        descBg.setColor(cardBg);
        descBg.setStroke((int) (1 * d), Color.parseColor("#CBD5E1"));
        etIssueDesc.setBackground(descBg);
        int descPad = (int) (14 * d);
        etIssueDesc.setPadding(descPad, descPad, descPad, descPad);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        descLp.bottomMargin = (int) (14 * d);
        etIssueDesc.setLayoutParams(descLp);
        container.addView(etIssueDesc);

        // 4. Attachment (Optional) — Upload Screenshot
        addFieldLabel("Attachment (Optional)", navy, d);

        LinearLayout uploadBox = new LinearLayout(this);
        uploadBox.setOrientation(LinearLayout.VERTICAL);
        uploadBox.setGravity(Gravity.CENTER);
        GradientDrawable upBg = new GradientDrawable();
        upBg.setCornerRadius(12 * d);
        upBg.setColor(cardBg);
        upBg.setStroke((int) (1 * d), Color.parseColor("#CBD5E1"));
        uploadBox.setBackground(upBg);
        int upPad = (int) (18 * d);
        uploadBox.setPadding(upPad, upPad, upPad, upPad);
        LinearLayout.LayoutParams upLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        upLp.bottomMargin = (int) (20 * d);
        uploadBox.setLayoutParams(upLp);

        ivScreenshotPreview = new ImageView(this);
        ivScreenshotPreview.setVisibility(View.GONE);
        ivScreenshotPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams prevLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (140 * d));
        prevLp.bottomMargin = (int) (10 * d);
        ivScreenshotPreview.setLayoutParams(prevLp);
        uploadBox.addView(ivScreenshotPreview);

        tvUploadTitle = new TextView(this);
        tvUploadTitle.setText("Upload Screenshot");
        tvUploadTitle.setTextColor(navy);
        tvUploadTitle.setTextSize(14f);
        tvUploadTitle.setTypeface(null, Typeface.BOLD);
        tvUploadTitle.setGravity(Gravity.CENTER);
        uploadBox.addView(tvUploadTitle);

        tvUploadSubtitle = new TextView(this);
        tvUploadSubtitle.setText("Attach an image showing the issue");
        tvUploadSubtitle.setTextColor(textGray);
        tvUploadSubtitle.setTextSize(12.5f);
        tvUploadSubtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams upSubLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        upSubLp.topMargin = (int) (4 * d);
        tvUploadSubtitle.setLayoutParams(upSubLp);
        uploadBox.addView(tvUploadSubtitle);

        uploadBox.setOnClickListener(v -> pickScreenshotLauncher.launch("image/*"));
        container.addView(uploadBox);

        // 5. Submit Report Button
        Button btnReport = new Button(this);
        btnReport.setText("Submit Report");
        btnReport.setAllCaps(false);
        btnReport.setTextSize(14.5f);
        btnReport.setTypeface(null, Typeface.BOLD);
        btnReport.setTextColor(ThemeHelper.isDarkMode(this) ? Color.parseColor("#0C1728") : Color.WHITE);
        btnReport.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        btnReport.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (48 * d)));

        btnReport.setOnClickListener(v -> {
            String title = etIssueTitle.getText().toString().trim();
            String desc = etIssueDesc.getText().toString().trim();

            if (selectedProblemCategory.isEmpty()) {
                Toast.makeText(this, "Please select a problem category.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (title.isEmpty()) {
                etIssueTitle.setError("Please enter an issue title");
                etIssueTitle.requestFocus();
                return;
            }
            if (desc.isEmpty()) {
                etIssueDesc.setError("Please enter a description");
                etIssueDesc.requestFocus();
                return;
            }

            btnReport.setEnabled(false);
            btnReport.setText("Submitting...");

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Map<String, Object> ticket = new HashMap<>();
            ticket.put("senderUid", user != null ? user.getUid() : "guest");
            ticket.put("senderEmail", user != null && user.getEmail() != null ? user.getEmail() : "unknown");
            ticket.put("role", isAdmin ? "admin" : "tenant");
            ticket.put("category", selectedProblemCategory);
            ticket.put("title", title);
            ticket.put("message", desc);
            ticket.put("screenshotBase64", attachedScreenshotBase64);
            ticket.put("status", "Open");
            ticket.put("timestamp", FieldValue.serverTimestamp());

            db.collection("support_tickets").add(ticket)
                    .addOnSuccessListener(ref -> {
                        String code = ref.getId().substring(0, 6).toUpperCase();
                        showReportSubmittedConfirmation(code);
                    })
                    .addOnFailureListener(e -> {
                        btnReport.setEnabled(true);
                        btnReport.setText("Submit Report");
                        Toast.makeText(this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        container.addView(btnReport);
    }

    private interface OnCategorySelectedListener {
        void onSelected(String category);
    }

    private void showProblemCategoryOverlay(OnCategorySelectedListener listener) {
        float d = getResources().getDisplayMetrics().density;
        int navy = ContextCompat.getColor(this, R.color.navy_primary);
        int cardBg = ContextCompat.getColor(this, R.color.white);
        int pillBg = ThemeHelper.isDarkMode(this)
                ? Color.parseColor("#1E314E")
                : Color.parseColor("#F1F5F9");

        String[] categories = {
                "App error",
                "Login/Account Issue",
                "Button or Feature not working",
                "Incorrect Information",
                "Display/Layout Issue"
        };

        LinearLayout modalBox = new LinearLayout(this);
        modalBox.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (22 * d);
        modalBox.setPadding(pad, pad, pad, pad);

        GradientDrawable boxBg = new GradientDrawable();
        boxBg.setCornerRadius(18 * d);
        boxBg.setColor(cardBg);
        modalBox.setBackground(boxBg);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Problem category");
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(19f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLp.bottomMargin = (int) (16 * d);
        tvTitle.setLayoutParams(titleLp);
        modalBox.addView(tvTitle);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(modalBox)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        for (String cat : categories) {
            TextView tvPill = new TextView(this);
            tvPill.setText(cat);
            tvPill.setTextColor(navy);
            tvPill.setTextSize(14f);
            tvPill.setTypeface(null, Typeface.BOLD);
            int pPadV = (int) (14 * d);
            int pPadH = (int) (16 * d);
            tvPill.setPadding(pPadH, pPadV, pPadH, pPadV);

            GradientDrawable pDrawable = new GradientDrawable();
            pDrawable.setCornerRadius(10 * d);
            pDrawable.setColor(pillBg);
            tvPill.setBackground(pDrawable);

            LinearLayout.LayoutParams pillLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            pillLp.bottomMargin = (int) (10 * d);
            tvPill.setLayoutParams(pillLp);

            tvPill.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onSelected(cat);
            });
            modalBox.addView(tvPill);
        }

        Button btnCancel = new Button(this);
        btnCancel.setText("Cancel");
        btnCancel.setAllCaps(false);
        btnCancel.setTextSize(14.5f);
        btnCancel.setTypeface(null, Typeface.BOLD);
        btnCancel.setTextColor(ThemeHelper.isDarkMode(this) ? Color.parseColor("#0C1728") : Color.WHITE);
        btnCancel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (46 * d));
        cancelLp.topMargin = (int) (8 * d);
        btnCancel.setLayoutParams(cancelLp);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        modalBox.addView(btnCancel);

        dialog.show();
    }

    private void showReportSubmittedConfirmation(String ticketCode) {
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
        tvTitle.setText("Report submitted");
        tvTitle.setTextColor(navy);
        tvTitle.setTextSize(20f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        modalBox.addView(tvTitle);

        TextView tvBody = new TextView(this);
        tvBody.setText("Your problem report (Ticket #" + ticketCode + ") has been sent to support.");
        tvBody.setTextColor(textPrimary);
        tvBody.setTextSize(14f);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bodyLp.topMargin = (int) (12 * d);
        bodyLp.bottomMargin = (int) (24 * d);
        tvBody.setLayoutParams(bodyLp);
        modalBox.addView(tvBody);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(modalBox)
                .setCancelable(false)
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
        btnDone.setOnClickListener(v -> {
            dialog.dismiss();
            showMenu();
        });
        modalBox.addView(btnDone);

        dialog.show();
    }

    private void addFieldLabel(String text, int color, float d) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(13.5f);
        tv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = (int) (6 * d);
        tv.setLayoutParams(lp);
        container.addView(tv);
    }

    private String encodeUriToBase64(Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream isBounds = getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(isBounds, null, bounds);
            }

            int maxDim = 600;
            int sampleSize = 1;
            while ((bounds.outWidth / sampleSize) > maxDim * 2 || (bounds.outHeight / sampleSize) > maxDim * 2) {
                sampleSize *= 2;
            }

            BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
            decodeOpts.inSampleSize = sampleSize;
            Bitmap original;
            try (InputStream is = getContentResolver().openInputStream(uri)) {
                original = BitmapFactory.decodeStream(is, null, decodeOpts);
            }
            if (original == null) return null;

            int w = original.getWidth();
            int h = original.getHeight();
            float scale = Math.min((float) maxDim / w, (float) maxDim / h);
            Bitmap scaled = scale < 1f
                    ? Bitmap.createScaledBitmap(original, Math.round(w * scale), Math.round(h * scale), true)
                    : original;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
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
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
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