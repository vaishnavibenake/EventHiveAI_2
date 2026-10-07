package com.example.eventhiveai.admin;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminReportsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvReportTotalEvents;
    private TextView tvReportApprovedEvents;
    private TextView tvReportRejectedEvents;
    private TextView tvClubPerformanceSummary;
    private TextView tvTotalRegistrations;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        tvReportTotalEvents = findViewById(R.id.tvReportTotalEvents);
        tvReportApprovedEvents = findViewById(R.id.tvReportApprovedEvents);
        tvReportRejectedEvents = findViewById(R.id.tvReportRejectedEvents);
        tvClubPerformanceSummary = findViewById(R.id.tvClubPerformanceSummary);
        tvTotalRegistrations = findViewById(R.id.tvTotalRegistrations);

        btnBack.setOnClickListener(v -> finish());

        loadReportsData();
    }

    private void loadReportsData() {
        // 1. Fetch Events Data
        db.collection("events").get()
                .addOnSuccessListener(snapshot -> {
                    int total = snapshot.size();
                    int approved = 0;
                    int rejected = 0;
                    Map<String, Integer> clubEventCounts = new HashMap<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = doc.getString("status");
                        if (status != null) {
                            if ("approved".equalsIgnoreCase(status)) approved++;
                            if ("rejected".equalsIgnoreCase(status)) rejected++;
                        }

                        String club = doc.getString("clubName");
                        if (club == null || club.isEmpty()) club = "General Club";
                        clubEventCounts.put(club, clubEventCounts.getOrDefault(club, 0) + 1);
                    }

                    tvReportTotalEvents.setText(String.valueOf(total));
                    tvReportApprovedEvents.setText(String.valueOf(approved));
                    tvReportRejectedEvents.setText(String.valueOf(rejected));

                    // Build Club Performance Summary
                    StringBuilder sb = new StringBuilder();
                    if (clubEventCounts.isEmpty()) {
                        sb.append("No club activity recorded yet.");
                    } else {
                        for (Map.Entry<String, Integer> entry : clubEventCounts.entrySet()) {
                            sb.append("• ").append(entry.getKey()).append(": ")
                                    .append(entry.getValue()).append(" event(s) proposed\n");
                        }
                    }
                    tvClubPerformanceSummary.setText(sb.toString().trim());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminReportsActivity.this, "Error fetching events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // 2. Fetch Registrations & Verified Attendance
        db.collection("registrations").get()
                .addOnSuccessListener(snapshot -> {
                    int total = snapshot.size();
                    int attended = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = doc.getString("status");
                        if ("attended".equalsIgnoreCase(status)) {
                            attended++;
                        }
                    }
                    int rate = total > 0 ? (attended * 100 / total) : 0;
                    tvTotalRegistrations.setText(total + " (" + attended + " Attended • " + rate + "%)");
                })
                .addOnFailureListener(e -> {
                    tvTotalRegistrations.setText("0");
                });
    }
}
