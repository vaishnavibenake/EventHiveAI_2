package com.example.eventhiveai.coordinator;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CoordinatorAnalyticsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvAnalyticsEvents;
    private TextView tvAnalyticsRegistrations;
    private TextView tvAnalyticsApprovalRate;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_analytics);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        tvAnalyticsEvents = findViewById(R.id.tvAnalyticsEvents);
        tvAnalyticsRegistrations = findViewById(R.id.tvAnalyticsRegistrations);
        tvAnalyticsApprovalRate = findViewById(R.id.tvAnalyticsApprovalRate);

        btnBack.setOnClickListener(v -> finish());

        loadAnalytics();
    }

    private void loadAnalytics() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("events").whereEqualTo("coordinatorId", user.getUid()).get()
                .addOnSuccessListener(snapshots -> {
                    int total = 0;
                    int approved = 0;
                    for (var doc : snapshots.getDocuments()) {
                        String coord = doc.getString("coordinatorId");
                        if (user.getUid().equals(coord)) {
                            total++;
                            if ("approved".equalsIgnoreCase(doc.getString("status"))) {
                                approved++;
                            }
                        }
                    }

                    tvAnalyticsEvents.setText("Total Events Organized: " + total);
                    int rate = total > 0 ? (approved * 100 / total) : 100;
                    tvAnalyticsApprovalRate.setText("Proposal Approval Rate: " + rate + "% (" + approved + " approved)");
                });

        db.collection("registrations").whereEqualTo("coordinatorId", user.getUid()).get().addOnSuccessListener(snapshots -> {
            int total = snapshots.size();
            int attended = 0;
            for (var doc : snapshots.getDocuments()) {
                if ("attended".equalsIgnoreCase(doc.getString("status"))) {
                    attended++;
                }
            }
            int attRate = total > 0 ? (attended * 100 / total) : 0;
            tvAnalyticsRegistrations.setText("Student Registrations: " + total + " (" + attended + " Attended • " + attRate + "%)");
        });
    }
}
