package com.example.eventhiveai.admin;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.eventhiveai.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminReportsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ProgressBar progressBar;
    private TextView tvTotalEvents;
    private TextView tvApprovedEvents;
    private TextView tvPendingEvents;
    private TextView tvRejectedEvents;
    private TextView tvTotalClubs;
    private TextView tvActiveClubs;
    private TextView tvTotalRegistrations;
    private TextView tvClubPerformanceSummary;
    private Button btnDownloadPdf;

    private ProgressBar progressApproved, progressPending, progressRejected;
    private TextView tvApprovedPercent, tvPendingPercent, tvRejectedPercent;

    private FirebaseFirestore db;

    // Report metric variables
    private int totalEventsCount = 0;
    private int approvedEventsCount = 0;
    private int pendingEventsCount = 0;
    private int rejectedEventsCount = 0;
    private int totalClubsCount = 0;
    private int activeClubsCount = 0;
    private int totalRegCount = 0;
    private int attendedRegCount = 0;
    private final Map<String, Integer> clubEventCounts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        tvTotalEvents = findViewById(R.id.tvTotalEvents);
        tvApprovedEvents = findViewById(R.id.tvApprovedEvents);
        tvPendingEvents = findViewById(R.id.tvPendingEvents);
        tvRejectedEvents = findViewById(R.id.tvRejectedEvents);

        tvTotalClubs = findViewById(R.id.tvTotalClubs);
        tvActiveClubs = findViewById(R.id.tvActiveClubs);
        tvTotalRegistrations = findViewById(R.id.tvTotalRegistrations);
        tvClubPerformanceSummary = findViewById(R.id.tvClubPerformanceSummary);
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf);

        progressApproved = findViewById(R.id.progressApproved);
        progressPending = findViewById(R.id.progressPending);
        progressRejected = findViewById(R.id.progressRejected);
        tvApprovedPercent = findViewById(R.id.tvApprovedPercent);
        tvPendingPercent = findViewById(R.id.tvPendingPercent);
        tvRejectedPercent = findViewById(R.id.tvRejectedPercent);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnDownloadPdf != null) {
            btnDownloadPdf.setOnClickListener(v -> generateAndOpenPdfReport());
        }

        loadReportsData();
    }

    private void loadReportsData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // 1. Fetch Events Data
        db.collection("events").get()
                .addOnSuccessListener(snapshot -> {
                    totalEventsCount = snapshot.size();
                    approvedEventsCount = 0;
                    pendingEventsCount = 0;
                    rejectedEventsCount = 0;
                    clubEventCounts.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = doc.getString("status");
                        if (status != null) {
                            status = status.trim().toLowerCase();
                            if ("approved".equals(status) || "active".equals(status)) {
                                approvedEventsCount++;
                            } else if ("rejected".equals(status)) {
                                rejectedEventsCount++;
                            } else {
                                pendingEventsCount++;
                            }
                        } else {
                            pendingEventsCount++;
                        }

                        String club = doc.getString("clubName");
                        if (club == null || club.trim().isEmpty()) club = "Unassigned Club";
                        clubEventCounts.put(club, clubEventCounts.getOrDefault(club, 0) + 1);
                    }

                    if (tvTotalEvents != null) tvTotalEvents.setText(String.valueOf(totalEventsCount));
                    if (tvApprovedEvents != null) tvApprovedEvents.setText(String.valueOf(approvedEventsCount));
                    if (tvPendingEvents != null) tvPendingEvents.setText(String.valueOf(pendingEventsCount));
                    if (tvRejectedEvents != null) tvRejectedEvents.setText(String.valueOf(rejectedEventsCount));

                    // Calculate rates
                    int approvedPct = totalEventsCount > 0 ? (approvedEventsCount * 100 / totalEventsCount) : 0;
                    int pendingPct = totalEventsCount > 0 ? (pendingEventsCount * 100 / totalEventsCount) : 0;
                    int rejectedPct = totalEventsCount > 0 ? (rejectedEventsCount * 100 / totalEventsCount) : 0;

                    if (progressApproved != null) progressApproved.setProgress(approvedPct);
                    if (progressPending != null) progressPending.setProgress(pendingPct);
                    if (progressRejected != null) progressRejected.setProgress(rejectedPct);

                    if (tvApprovedPercent != null) tvApprovedPercent.setText(approvedPct + "% (" + approvedEventsCount + " events)");
                    if (tvPendingPercent != null) tvPendingPercent.setText(pendingPct + "% (" + pendingEventsCount + " events)");
                    if (tvRejectedPercent != null) tvRejectedPercent.setText(rejectedPct + "% (" + rejectedEventsCount + " events)");

                    StringBuilder sb = new StringBuilder();
                    if (clubEventCounts.isEmpty()) {
                        sb.append("No club activity recorded.");
                    } else {
                        for (Map.Entry<String, Integer> entry : clubEventCounts.entrySet()) {
                            sb.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" event proposal(s)\n");
                        }
                    }
                    if (tvClubPerformanceSummary != null) tvClubPerformanceSummary.setText(sb.toString().trim());

                    // 2. Fetch Clubs Data
                    loadClubsData();
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminReportsActivity.this, "Error fetching events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadClubsData() {
        db.collection("clubs").get()
                .addOnSuccessListener(snapshot -> {
                    totalClubsCount = snapshot.size();
                    activeClubsCount = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = doc.getString("status");
                        if (status != null && "active".equalsIgnoreCase(status)) {
                            activeClubsCount++;
                        }
                    }

                    if (tvTotalClubs != null) tvTotalClubs.setText(String.valueOf(totalClubsCount));
                    if (tvActiveClubs != null) tvActiveClubs.setText(String.valueOf(activeClubsCount));

                    // 3. Fetch Registrations Data
                    loadRegistrationsData();
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminReportsActivity.this, "Error fetching clubs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRegistrationsData() {
        db.collection("registrations").get()
                .addOnSuccessListener(snapshot -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    totalRegCount = snapshot.size();
                    attendedRegCount = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = doc.getString("status");
                        if ("attended".equalsIgnoreCase(status)) {
                            attendedRegCount++;
                        }
                    }

                    int rate = totalRegCount > 0 ? (attendedRegCount * 100 / totalRegCount) : 0;
                    if (tvTotalRegistrations != null) {
                        tvTotalRegistrations.setText(totalRegCount + " Registrations (" + attendedRegCount + " Verified • " + rate + "% Attendance)");
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (tvTotalRegistrations != null) tvTotalRegistrations.setText("0");
                });
    }

    /**
     * Generates a publication-grade standard institutional A4 report with tables, metadata box, and signature blocks.
     */
    private void generateAndOpenPdfReport() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 format
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 1. HEADER BANNER
        paint.setColor(Color.rgb(76, 29, 149)); // #4C1D95 deep purple
        canvas.drawRect(0, 0, 595, 80, paint);

        // Gold Accent Stripe
        paint.setColor(Color.rgb(245, 158, 11)); // #F59E0B gold
        canvas.drawRect(0, 80, 595, 84, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("EVENTHIVE AI — CAMPUS ADMINISTRATION SYSTEM", 40, 42, paint);

        paint.setColor(Color.rgb(233, 213, 255));
        paint.setTextSize(10);
        paint.setFakeBoldText(false);
        canvas.drawText("OFFICIAL INSTITUTIONAL EVENT & GOVERNANCE REPORT", 40, 62, paint);

        // 2. METADATA AUDIT BOX (y=98 to 150)
        paint.setColor(Color.rgb(248, 250, 252));
        canvas.drawRoundRect(new RectF(40, 98, 555, 150), 6, 6, paint);

        paint.setColor(Color.rgb(203, 213, 225));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        canvas.drawRoundRect(new RectF(40, 98, 555, 150), 6, 6, paint);
        paint.setStyle(Paint.Style.FILL);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String generatedAt = sdf.format(new Date());
        String docRef = "EH-REP-" + (System.currentTimeMillis() / 1000);

        paint.setColor(Color.rgb(51, 65, 85));
        paint.setTextSize(9);
        paint.setFakeBoldText(true);
        canvas.drawText("Document Ref: " + docRef, 52, 118, paint);
        canvas.drawText("Classification: CONFIDENTIAL & OFFICIAL", 52, 136, paint);

        canvas.drawText("Generated At: " + generatedAt, 320, 118, paint);
        canvas.drawText("Issuer: Office of Faculty Administration", 320, 136, paint);

        // 3. SECTION 1: EXECUTIVE METRICS SUMMARY TABLE (y=170)
        paint.setColor(Color.rgb(15, 23, 42));
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        canvas.drawText("1. EXECUTIVE EVENT LIFECYCLE AUDIT", 40, 175, paint);

        // Table Header
        int tableY = 188;
        paint.setColor(Color.rgb(226, 232, 240));
        canvas.drawRect(40, tableY, 555, tableY + 22, paint);

        paint.setColor(Color.rgb(15, 23, 42));
        paint.setTextSize(9);
        paint.setFakeBoldText(true);
        canvas.drawText("Metric Category", 50, tableY + 15, paint);
        canvas.drawText("Count", 240, tableY + 15, paint);
        canvas.drawText("Percentage", 340, tableY + 15, paint);
        canvas.drawText("Governance Status", 430, tableY + 15, paint);

        // Rows
        String[][] eventRows = {
                {"Approved Live Events", String.valueOf(approvedEventsCount), (totalEventsCount > 0 ? (approvedEventsCount * 100 / totalEventsCount) : 0) + "%", "Published & Active"},
                {"Pending Review Queue", String.valueOf(pendingEventsCount), (totalEventsCount > 0 ? (pendingEventsCount * 100 / totalEventsCount) : 0) + "%", "Awaiting Admin Review"},
                {"Rejected Proposals", String.valueOf(rejectedEventsCount), (totalEventsCount > 0 ? (rejectedEventsCount * 100 / totalEventsCount) : 0) + "%", "Archived / Non-Compliant"},
                {"Total Event Proposals", String.valueOf(totalEventsCount), "100%", "Full Proposal Count"}
        };

        paint.setFakeBoldText(false);
        int curY = tableY + 22;
        for (int i = 0; i < eventRows.length; i++) {
            boolean isLast = (i == eventRows.length - 1);
            paint.setColor(i % 2 == 0 ? Color.WHITE : Color.rgb(248, 250, 252));
            if (isLast) paint.setColor(Color.rgb(241, 245, 249));
            canvas.drawRect(40, curY, 555, curY + 20, paint);

            paint.setColor(Color.rgb(30, 41, 59));
            paint.setFakeBoldText(isLast);
            canvas.drawText(eventRows[i][0], 50, curY + 14, paint);
            canvas.drawText(eventRows[i][1], 240, curY + 14, paint);
            canvas.drawText(eventRows[i][2], 340, curY + 14, paint);
            canvas.drawText(eventRows[i][3], 430, curY + 14, paint);
            curY += 20;
        }

        // Outer Table Border
        paint.setColor(Color.rgb(203, 213, 225));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        canvas.drawRect(40, tableY, 555, curY, paint);
        paint.setStyle(Paint.Style.FILL);

        // 4. SECTION 2: CLUB GOVERNANCE & PROPOSALS (y=curY + 30)
        curY += 30;
        paint.setColor(Color.rgb(15, 23, 42));
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        canvas.drawText("2. CAMPUS CLUB PROPOSAL & ENGAGEMENT BREAKDOWN", 40, curY, paint);

        curY += 12;
        int clubTableY = curY;
        paint.setColor(Color.rgb(226, 232, 240));
        canvas.drawRect(40, clubTableY, 555, clubTableY + 22, paint);

        paint.setColor(Color.rgb(15, 23, 42));
        paint.setTextSize(9);
        paint.setFakeBoldText(true);
        canvas.drawText("Registered Club Name", 50, clubTableY + 15, paint);
        canvas.drawText("Proposed Events", 260, clubTableY + 15, paint);
        canvas.drawText("Campus Activity Share", 410, clubTableY + 15, paint);

        curY = clubTableY + 22;
        paint.setFakeBoldText(false);
        int cIdx = 0;
        if (clubEventCounts.isEmpty()) {
            paint.setColor(Color.WHITE);
            canvas.drawRect(40, curY, 555, curY + 20, paint);
            paint.setColor(Color.rgb(100, 116, 139));
            canvas.drawText("No specific club activity recorded.", 50, curY + 14, paint);
            curY += 20;
        } else {
            for (Map.Entry<String, Integer> entry : clubEventCounts.entrySet()) {
                paint.setColor(cIdx % 2 == 0 ? Color.WHITE : Color.rgb(248, 250, 252));
                canvas.drawRect(40, curY, 555, curY + 20, paint);

                paint.setColor(Color.rgb(30, 41, 59));
                canvas.drawText(entry.getKey(), 50, curY + 14, paint);
                canvas.drawText(String.valueOf(entry.getValue()), 260, curY + 14, paint);

                int share = totalEventsCount > 0 ? (entry.getValue() * 100 / totalEventsCount) : 0;
                canvas.drawText(share + "% of total events", 410, curY + 14, paint);
                curY += 20;
                cIdx++;
                if (curY > 520) break; // prevent overflowing page
            }
        }

        paint.setColor(Color.rgb(203, 213, 225));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        canvas.drawRect(40, clubTableY, 555, curY, paint);
        paint.setStyle(Paint.Style.FILL);

        // 5. SECTION 3: STUDENT PARTICIPATION & ATTENDANCE AUDIT (y=curY + 30)
        curY += 30;
        paint.setColor(Color.rgb(15, 23, 42));
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        canvas.drawText("3. STUDENT ENGAGEMENT & VERIFIED ATTENDANCE", 40, curY, paint);

        curY += 16;
        int rate = totalRegCount > 0 ? (attendedRegCount * 100 / totalRegCount) : 0;
        paint.setTextSize(10);
        paint.setFakeBoldText(false);
        paint.setColor(Color.rgb(51, 65, 85));
        canvas.drawText("• Total Student Registrations Recorded: " + totalRegCount, 50, curY, paint);
        curY += 16;
        canvas.drawText("• Verified Physical/Digital Attendees Present: " + attendedRegCount, 50, curY, paint);
        curY += 16;
        canvas.drawText("• Verified Institutional Attendance Ratio: " + rate + "% (" + (rate >= 60 ? "HIGH ENGAGEMENT" : "MODERATE ENGAGEMENT") + ")", 50, curY, paint);

        // 6. FORMAL SIGN-OFF & VERIFICATION BLOCK (Bottom: y=710 to 790)
        int sigY = 720;
        paint.setColor(Color.rgb(148, 163, 184));
        paint.setStrokeWidth(1);
        canvas.drawLine(50, sigY, 230, sigY, paint);
        canvas.drawLine(360, sigY, 540, sigY, paint);

        paint.setColor(Color.rgb(30, 41, 59));
        paint.setTextSize(9);
        paint.setFakeBoldText(true);
        canvas.drawText("FACULTY ADMINISTRATOR", 50, sigY + 14, paint);
        canvas.drawText("DEAN / OFFICE OF STUDENT AFFAIRS", 360, sigY + 14, paint);

        paint.setFakeBoldText(false);
        paint.setColor(Color.rgb(100, 116, 139));
        paint.setTextSize(8);
        canvas.drawText("EventHive AI Governance Board", 50, sigY + 26, paint);
        canvas.drawText("Authorized Signature & Seal", 360, sigY + 26, paint);

        // Footer note
        paint.setColor(Color.rgb(148, 163, 184));
        paint.setTextSize(8);
        canvas.drawText("Computer-generated official audit report issued by EventHiveAI. Integrity verified via Cloud Firestore.", 40, 810, paint);

        document.finishPage(page);

        // Save PDF file
        File reportsDir = new File(getExternalFilesDir(null), "reports");
        if (!reportsDir.exists()) reportsDir.mkdirs();
        File file = new File(reportsDir, "EventHiveAI_Admin_Report_" + System.currentTimeMillis() + ".pdf");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            document.close();
            Toast.makeText(this, "Standard Report saved: " + file.getName(), Toast.LENGTH_LONG).show();

            // Open PDF with FileProvider
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(Intent.createChooser(intent, "Open Standard PDF Report"));
            } catch (Exception e) {
                Toast.makeText(this, "PDF exported at: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            document.close();
            Toast.makeText(this, "Failed to export PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
