package com.example.eventhiveai.coordinator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Coordinator Reports & Analytics screen.
 * Loads real Firestore data for the coordinator's own events and registrations.
 * Provides: summary metrics, event-wise table, PDF download, CSV export.
 */
public class CoordinatorAnalyticsActivity extends AppCompatActivity {

    private static final String TAG = "CoordAnalytics";

    private ImageButton btnBack;
    private Spinner spinnerStatusFilter;
    private Button btnRefresh;
    private Button btnExportPdf;
    private Button btnExportCsv;

    // Summary cards
    private TextView tvTotalEvents;
    private TextView tvApprovedEvents;
    private TextView tvPendingEvents;
    private TextView tvCompletedEvents;
    private TextView tvTotalRegistrations;
    private TextView tvAttendanceRate;

    // Event table container
    private LinearLayout tableContainer;
    private ProgressBar progressBar;
    private TextView tvNoData;
    private TextView tvClubLabel;
    private PieChart pieChartStatus;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String coordinatorUid;
    private String coordinatorClubId = "";
    private String coordinatorClubName = "";

    // All events for this coordinator
    private final List<EventRow> allEvents = new ArrayList<>();
    // Map eventId -> registration count
    private final Map<String, Integer> regCountMap = new LinkedHashMap<>();
    // Map eventId -> attended count
    private final Map<String, Integer> attendedCountMap = new LinkedHashMap<>();

    private String currentStatusFilter = "ALL";

    // SAF launchers
    private ActivityResultLauncher<String> pdfSaveLauncher;
    private ActivityResultLauncher<String> csvSaveLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.eventhiveai.SettingsActivity.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_analytics);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { finish(); return; }
        coordinatorUid = user.getUid();

        btnBack = findViewById(R.id.btnBack);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnExportCsv = findViewById(R.id.btnExportCsv);

        tvTotalEvents = findViewById(R.id.tvAnalyticsTotalEvents);
        tvApprovedEvents = findViewById(R.id.tvAnalyticsApproved);
        tvPendingEvents = findViewById(R.id.tvAnalyticsPending);
        tvCompletedEvents = findViewById(R.id.tvAnalyticsCompleted);
        tvTotalRegistrations = findViewById(R.id.tvAnalyticsRegistrations);
        tvAttendanceRate = findViewById(R.id.tvAnalyticsApprovalRate);

        tableContainer = findViewById(R.id.tableContainer);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);
        tvClubLabel = findViewById(R.id.tvClubLabel);
        pieChartStatus = findViewById(R.id.pieChartStatus);

        btnBack.setOnClickListener(v -> finish());

        setupStatusFilter();
        setupSafLaunchers();

        btnRefresh.setOnClickListener(v -> loadAll());
        btnExportPdf.setOnClickListener(v -> {
            String filename = buildFilename("pdf");
            pdfSaveLauncher.launch(filename);
        });
        btnExportCsv.setOnClickListener(v -> {
            String filename = buildFilename("csv");
            csvSaveLauncher.launch(filename);
        });

        loadCoordinatorClubInfo();
    }

    // ───────────────────────────── Setup ─────────────────────────────

    private void setupStatusFilter() {
        String[] statuses = {"ALL", "pending", "approved", "completed", "rejected"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(adapter);
        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                currentStatusFilter = statuses[pos];
                renderTable();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupSafLaunchers() {
        pdfSaveLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"),
                uri -> { if (uri != null) writePdf(uri); }
        );
        csvSaveLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/csv"),
                uri -> { if (uri != null) writeCsv(uri); }
        );
    }

    private String buildFilename(String ext) {
        String clubPart = coordinatorClubName.isEmpty() ? "Club" : coordinatorClubName.replaceAll("\\s+", "_");
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return clubPart + "_Event_Report_" + date + "." + ext;
    }

    // ───────────────────────────── Data Loading ─────────────────────────────

    private void loadCoordinatorClubInfo() {
        db.collection("users").document(coordinatorUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String cId = doc.getString("clubId");
                        if (cId != null && !cId.isEmpty()) {
                            coordinatorClubId = cId;
                            db.collection("clubs").document(cId).get()
                                    .addOnSuccessListener(cDoc -> {
                                        if (cDoc.exists()) {
                                            String n = cDoc.getString("name");
                                            if (n == null || n.isEmpty()) n = cDoc.getString("clubName");
                                            if (n != null) coordinatorClubName = n;
                                        }
                                        if (tvClubLabel != null)
                                            tvClubLabel.setText("Club: " + (coordinatorClubName.isEmpty() ? coordinatorClubId : coordinatorClubName));
                                        loadAll();
                                    })
                                    .addOnFailureListener(e -> loadAll());
                        } else {
                            if (tvClubLabel != null) tvClubLabel.setText("Club: Not assigned");
                            loadAll();
                        }
                    } else {
                        loadAll();
                    }
                })
                .addOnFailureListener(e -> loadAll());
    }

    private void loadAll() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvNoData != null) tvNoData.setVisibility(View.GONE);
        allEvents.clear();
        regCountMap.clear();
        attendedCountMap.clear();

        db.collection("events")
                .whereEqualTo("coordinatorId", coordinatorUid)
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        EventRow row = new EventRow();
                        row.eventId = doc.getId();
                        row.eventName = getString(doc, "eventName", "(Unnamed)");
                        row.date = getString(doc, "date", "");
                        row.status = getString(doc, "status", "pending");
                        row.venue = getString(doc, "venue", "");
                        row.category = getString(doc, "category", "");
                        Long mp = doc.getLong("maxParticipants");
                        row.maxParticipants = mp != null ? mp.intValue() : 0;
                        allEvents.add(row);
                    }
                    loadRegistrations();
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "loadAll events error", e);
                });
    }

    private void loadRegistrations() {
        db.collection("registrations")
                .whereEqualTo("coordinatorId", coordinatorUid)
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String eid = doc.getString("eventId");
                        if (eid == null || eid.isEmpty()) continue;
                        regCountMap.put(eid, regCountMap.getOrDefault(eid, 0) + 1);
                        String status = doc.getString("status");
                        if ("attended".equalsIgnoreCase(status)) {
                            attendedCountMap.put(eid, attendedCountMap.getOrDefault(eid, 0) + 1);
                        }
                    }
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    renderMetrics();
                    renderTable();
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    renderMetrics(); // show whatever we have
                    renderTable();
                    Log.w(TAG, "loadRegistrations error (non-fatal): " + e.getMessage());
                });
    }

    // ───────────────────────────── Rendering ─────────────────────────────

    private void renderMetrics() {
        int total = allEvents.size();
        int approved = 0, pending = 0, completed = 0;
        int totalReg = 0, totalAttended = 0;

        for (EventRow r : allEvents) {
            String s = r.status.trim().toLowerCase();
            if (s.contains("approve")) approved++;
            else if ("pending".equals(s)) pending++;
            else if ("completed".equals(s)) completed++;

            totalReg += regCountMap.getOrDefault(r.eventId, 0);
            totalAttended += attendedCountMap.getOrDefault(r.eventId, 0);
        }

        setText(tvTotalEvents, String.valueOf(total));
        setText(tvApprovedEvents, String.valueOf(approved));
        setText(tvPendingEvents, String.valueOf(pending));
        setText(tvCompletedEvents, String.valueOf(completed));
        setText(tvTotalRegistrations, String.valueOf(totalReg));

        if (totalAttended > 0 && totalReg > 0) {
            int rate = (totalAttended * 100) / totalReg;
            setText(tvAttendanceRate, "Attendance Rate: " + rate + "% (" + totalAttended + "/" + totalReg + ")");
        } else {
            setText(tvAttendanceRate, "Attendance: N/A (no attendance records)");
        }

        renderPieChart(approved, pending, completed,
                count("rejected"), total - approved - pending - completed - count("rejected"));
    }

    private void renderTable() {
        if (tableContainer == null) return;
        tableContainer.removeAllViews();

        List<EventRow> filtered = new ArrayList<>();
        for (EventRow r : allEvents) {
            if ("ALL".equals(currentStatusFilter) || currentStatusFilter.equalsIgnoreCase(r.status)) {
                filtered.add(r);
            }
        }

        if (filtered.isEmpty()) {
            if (tvNoData != null) tvNoData.setVisibility(View.VISIBLE);
            return;
        }
        if (tvNoData != null) tvNoData.setVisibility(View.GONE);

        // Header row
        addTableRow(tableContainer, "Event Name", "Date", "Status", "Registrations", "Max", true);

        for (EventRow r : filtered) {
            int regCount = regCountMap.getOrDefault(r.eventId, 0);
            String maxStr = r.maxParticipants > 0 ? String.valueOf(r.maxParticipants) : "—";
            addTableRow(tableContainer, r.eventName, r.date.isEmpty() ? "—" : r.date,
                    capitalise(r.status), String.valueOf(regCount), maxStr, false);
        }
    }

    private void renderPieChart(int approved, int pending, int completed, int rejected, int other) {
        if (pieChartStatus == null) return;

        java.util.List<PieEntry> entries = new java.util.ArrayList<>();
        if (approved  > 0) entries.add(new PieEntry(approved,  "Approved"));
        if (pending   > 0) entries.add(new PieEntry(pending,   "Pending"));
        if (completed > 0) entries.add(new PieEntry(completed, "Completed"));
        if (rejected  > 0) entries.add(new PieEntry(rejected,  "Rejected"));
        if (other     > 0) entries.add(new PieEntry(other,     "Other"));

        if (entries.isEmpty()) {
            pieChartStatus.setVisibility(android.view.View.GONE);
            return;
        }
        pieChartStatus.setVisibility(android.view.View.VISIBLE);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                android.graphics.Color.parseColor("#10B981"),   // approved  — green
                android.graphics.Color.parseColor("#F59E0B"),   // pending   — amber
                android.graphics.Color.parseColor("#6366F1"),   // completed — indigo
                android.graphics.Color.parseColor("#EF4444"),   // rejected  — red
                android.graphics.Color.parseColor("#94A3B8")    // other     — slate
        );
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartStatus));

        pieChartStatus.setData(data);
        pieChartStatus.setUsePercentValues(true);
        pieChartStatus.getDescription().setEnabled(false);
        pieChartStatus.setHoleColor(android.graphics.Color.parseColor("#171A27"));
        pieChartStatus.setHoleRadius(45f);
        pieChartStatus.setTransparentCircleRadius(50f);
        pieChartStatus.setTransparentCircleColor(android.graphics.Color.parseColor("#0A0D18"));
        pieChartStatus.setEntryLabelColor(android.graphics.Color.WHITE);
        pieChartStatus.setEntryLabelTextSize(10f);

        com.github.mikephil.charting.components.Legend legend = pieChartStatus.getLegend();
        legend.setTextColor(android.graphics.Color.parseColor("#CBD5E1"));
        legend.setTextSize(10f);
        legend.setForm(com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE);

        pieChartStatus.animateY(600, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        pieChartStatus.invalidate();
    }

    private void addTableRow(LinearLayout parent, String c1, String c2, String c3, String c4, String c5, boolean isHeader) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (isHeader) {
            row.setBackgroundColor(android.graphics.Color.parseColor("#1E2235"));
        }
        row.setPadding(dp(8), dp(8), dp(8), dp(8));

        int[] weights = {4, 3, 2, 2, 2};
        String[] cols = {c1, c2, c3, c4, c5};
        for (int i = 0; i < cols.length; i++) {
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[i]);
            tv.setLayoutParams(lp);
            tv.setText(cols[i]);
            tv.setTextSize(isHeader ? 12 : 12);
            tv.setTextColor(isHeader
                    ? android.graphics.Color.parseColor("#A78BFA")
                    : android.graphics.Color.parseColor("#CBD5E1"));
            if (isHeader) tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setPadding(dp(4), dp(2), dp(4), dp(2));
            row.addView(tv);
        }

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(android.graphics.Color.parseColor("#1E2235"));

        parent.addView(row);
        parent.addView(divider);
    }

    // ───────────────────────────── PDF Export ─────────────────────────────

    private void writePdf(Uri uri) {
        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w")) {
            if (pfd == null) { Toast.makeText(this, "Cannot open file for writing.", Toast.LENGTH_SHORT).show(); return; }

            android.graphics.pdf.PdfDocument pdf = new android.graphics.pdf.PdfDocument();
            android.graphics.Paint paint = new android.graphics.Paint();
            android.graphics.Paint titlePaint = new android.graphics.Paint();
            android.graphics.Paint headerPaint = new android.graphics.Paint();

            titlePaint.setTextSize(18);
            titlePaint.setFakeBoldText(true);
            titlePaint.setColor(android.graphics.Color.parseColor("#7C3AED"));

            headerPaint.setTextSize(12);
            headerPaint.setFakeBoldText(true);
            headerPaint.setColor(android.graphics.Color.parseColor("#1E293B"));

            paint.setTextSize(11);
            paint.setColor(android.graphics.Color.parseColor("#334155"));

            int pageWidth = 595;  // A4 ~72dpi
            int pageHeight = 842;
            int margin = 40;
            int y = margin;
            int pageNum = 1;

            android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                    new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
            android.graphics.pdf.PdfDocument.Page page = pdf.startPage(pageInfo);
            android.graphics.Canvas canvas = page.getCanvas();

            // Title
            canvas.drawText("EventHive — Club Analytics Report", margin, y + 20, titlePaint);
            y += 36;

            paint.setTextSize(12);
            canvas.drawText("Club: " + (coordinatorClubName.isEmpty() ? coordinatorClubId : coordinatorClubName), margin, y, paint);
            y += 18;
            canvas.drawText("Generated: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date()), margin, y, paint);
            y += 18;
            canvas.drawText("Status Filter: " + currentStatusFilter, margin, y, paint);
            y += 28;

            // Divider
            android.graphics.Paint linePaint = new android.graphics.Paint();
            linePaint.setColor(android.graphics.Color.parseColor("#E2E8F0"));
            linePaint.setStrokeWidth(1);
            canvas.drawLine(margin, y, pageWidth - margin, y, linePaint);
            y += 16;

            // Summary
            canvas.drawText("Summary Metrics", margin, y, headerPaint);
            y += 18;

            paint.setTextSize(11);
            String[] metrics = {
                "Total Events: " + allEvents.size(),
                "Approved: " + count("approved"),
                "Pending: " + count("pending"),
                "Completed: " + count("completed"),
                "Total Registrations: " + totalRegistrations(),
            };
            for (String m : metrics) {
                canvas.drawText(m, margin + 10, y, paint);
                y += 16;
            }
            y += 12;

            // Table header
            canvas.drawLine(margin, y, pageWidth - margin, y, linePaint);
            y += 8;
            String[] headers = {"Event Name", "Date", "Status", "Registrations", "Capacity"};
            int[] colX = {margin, margin + 190, margin + 290, margin + 370, margin + 440};
            for (int i = 0; i < headers.length; i++) {
                canvas.drawText(headers[i], colX[i], y, headerPaint);
            }
            y += 14;
            canvas.drawLine(margin, y, pageWidth - margin, y, linePaint);
            y += 8;

            paint.setTextSize(10);
            List<EventRow> filtered = getFilteredRows();
            for (EventRow r : filtered) {
                if (y > pageHeight - margin - 20) {
                    // New page
                    pdf.finishPage(page);
                    pageNum++;
                    pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                    page = pdf.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = margin;
                    // Re-draw header
                    for (int i = 0; i < headers.length; i++) canvas.drawText(headers[i], colX[i], y, headerPaint);
                    y += 14;
                    canvas.drawLine(margin, y, pageWidth - margin, y, linePaint);
                    y += 8;
                }
                int reg = regCountMap.getOrDefault(r.eventId, 0);
                String max = r.maxParticipants > 0 ? String.valueOf(r.maxParticipants) : "—";
                String[] cells = {truncate(r.eventName, 32), r.date, capitalise(r.status), String.valueOf(reg), max};
                for (int i = 0; i < cells.length; i++) canvas.drawText(cells[i], colX[i], y, paint);
                y += 14;
            }

            // Footer
            paint.setTextSize(10);
            paint.setColor(android.graphics.Color.GRAY);
            canvas.drawText("EventHive — Page " + pageNum, margin, pageHeight - 20, paint);

            pdf.finishPage(page);

            FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
            pdf.writeTo(fos);
            fos.flush();
            pdf.close();

            Toast.makeText(this, "PDF saved successfully!", Toast.LENGTH_LONG).show();
            offerOpenFile(uri, "application/pdf");

        } catch (IOException e) {
            Toast.makeText(this, "PDF write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "PDF write error", e);
        }
    }

    // ───────────────────────────── CSV Export ─────────────────────────────

    private void writeCsv(Uri uri) {
        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
             PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                     new FileOutputStream(pfd.getFileDescriptor()), StandardCharsets.UTF_8))) {

            // BOM for Excel UTF-8 compatibility
            pw.print('\uFEFF');

            // Header
            pw.println(csvRow("Event Name", "Date", "Category", "Status", "Registrations", "Max Participants", "Attended", "Attendance Rate"));

            List<EventRow> filtered = getFilteredRows();
            for (EventRow r : filtered) {
                int reg = regCountMap.getOrDefault(r.eventId, 0);
                int attended = attendedCountMap.getOrDefault(r.eventId, 0);
                String rate = (reg > 0 && attended > 0) ? (attended * 100 / reg) + "%" : "N/A";
                String max = r.maxParticipants > 0 ? String.valueOf(r.maxParticipants) : "";
                pw.println(csvRow(r.eventName, r.date, r.category, r.status,
                        String.valueOf(reg), max, String.valueOf(attended), rate));
            }

            pw.flush();
            Toast.makeText(this, "CSV saved successfully!", Toast.LENGTH_LONG).show();
            offerOpenFile(uri, "text/csv");

        } catch (IOException e) {
            Toast.makeText(this, "CSV write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "CSV write error", e);
        }
    }

    /** Properly escape a CSV cell (RFC 4180). Prefixes formula injection chars with a tab. */
    private String escapeCsvCell(String value) {
        if (value == null) return "";
        // Prevent formula injection
        if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            value = "\t" + value;
        }
        // If the value contains a comma, newline, or double-quote, wrap in quotes and escape inner quotes
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            value = "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String csvRow(String... cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(escapeCsvCell(cells[i]));
        }
        return sb.toString();
    }

    // ───────────────────────────── Helpers ─────────────────────────────

    private void offerOpenFile(Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try { startActivity(Intent.createChooser(intent, "Open Report")); }
        catch (Exception ignored) {}
    }

    private List<EventRow> getFilteredRows() {
        List<EventRow> out = new ArrayList<>();
        for (EventRow r : allEvents) {
            if ("ALL".equals(currentStatusFilter) || currentStatusFilter.equalsIgnoreCase(r.status)) out.add(r);
        }
        return out;
    }

    private int count(String status) {
        int c = 0;
        for (EventRow r : allEvents) if (status.equalsIgnoreCase(r.status)) c++;
        return c;
    }

    private int totalRegistrations() {
        int t = 0;
        for (int v : regCountMap.values()) t += v;
        return t;
    }

    private void setText(TextView tv, String text) {
        if (tv != null) tv.setText(text);
    }

    private String getString(QueryDocumentSnapshot doc, String field, String def) {
        String v = doc.getString(field);
        return (v != null && !v.isEmpty()) ? v : def;
    }

    private String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    // ───────────────────────────── Data Model ─────────────────────────────

    private static class EventRow {
        String eventId = "";
        String eventName = "";
        String date = "";
        String status = "";
        String venue = "";
        String category = "";
        int maxParticipants = 0;
    }
}
