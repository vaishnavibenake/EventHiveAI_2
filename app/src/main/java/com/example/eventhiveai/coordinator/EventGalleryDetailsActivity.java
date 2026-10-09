package com.example.eventhiveai.coordinator;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class EventGalleryDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvEventName;
    private LinearLayout winnersContainer;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_gallery_details);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        tvEventName = findViewById(R.id.tvEventName);
        winnersContainer = findViewById(R.id.winnersContainer);

        eventId = getIntent().getStringExtra("EVENT_ID");
        String eventName = getIntent().getStringExtra("EVENT_NAME");
        if (eventName == null || eventName.isEmpty()) eventName = "Event Gallery";
        tvEventName.setText(eventName);

        btnBack.setOnClickListener(v -> finish());
        loadWinners();
    }

    private void loadWinners() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event information is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try event subcollection first, then top-level winners
        db.collection("events").document(eventId).collection("winners").get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        displayWinners(snap);
                    } else {
                        // Fallback: query top-level winners collection
                        db.collection("winners").whereEqualTo("eventId", eventId).get()
                                .addOnSuccessListener(this::displayWinners)
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load winners: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void displayWinners(com.google.firebase.firestore.QuerySnapshot querySnap) {
        winnersContainer.removeAllViews();

        if (querySnap.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No winners announced yet.");
            emptyText.setTextColor(Color.parseColor("#94A3B8"));
            emptyText.setTextSize(14);
            emptyText.setPadding(20, 40, 20, 20);
            winnersContainer.addView(emptyText);
            return;
        }

        for (QueryDocumentSnapshot doc : querySnap) {
            String pType = doc.getString("participationType");
            boolean isTeam = "Team".equals(pType) || "Pair".equals(pType);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.parseColor("#171A27"));
            card.setPadding(40, 32, 40, 32);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 16, 0, 0);
            card.setLayoutParams(lp);

            // Participation type badge
            if (pType != null) {
                TextView typeBadge = new TextView(this);
                typeBadge.setText("🏷️ " + pType + " Event");
                typeBadge.setTextColor(Color.parseColor("#A78BFA"));
                typeBadge.setTextSize(12);
                typeBadge.setPadding(0, 0, 0, 16);
                card.addView(typeBadge);
            }

            // 1st Place
            addWinnerRow(card, "🥇 1st Place", doc.getString("firstPlace"),
                    doc.getString("firstPlaceName"), doc.getString("firstPlaceDepartment"),
                    doc.getString("firstPlaceTeamName"), "#F59E0B");

            // 2nd Place
            addWinnerRow(card, "🥈 2nd Place", doc.getString("secondPlace"),
                    doc.getString("secondPlaceName"), doc.getString("secondPlaceDepartment"),
                    doc.getString("secondPlaceTeamName"), "#94A3B8");

            // 3rd Place
            addWinnerRow(card, "🥉 3rd Place", doc.getString("thirdPlace"),
                    doc.getString("thirdPlaceName"), doc.getString("thirdPlaceDepartment"),
                    doc.getString("thirdPlaceTeamName"), "#CD7F32");

            winnersContainer.addView(card);
        }
    }

    private void addWinnerRow(LinearLayout container, String label, String legacyText,
                              String name, String dept, String teamName, String color) {
        // Use structured data if available, otherwise legacy flat string
        String display;
        if (name != null && !name.isEmpty()) {
            StringBuilder sb = new StringBuilder(name);
            if (dept != null && !dept.isEmpty()) sb.append(" (").append(dept).append(")");
            if (teamName != null && !teamName.isEmpty()) sb.append("\n   Team: ").append(teamName);
            display = sb.toString();
        } else if (legacyText != null && !legacyText.isEmpty()) {
            display = legacyText;
        } else {
            return; // No winner for this position
        }

        TextView labelTv = new TextView(this);
        labelTv.setText(label);
        labelTv.setTextColor(Color.parseColor(color));
        labelTv.setTextSize(13);
        labelTv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        labelTv.setPadding(0, 12, 0, 2);
        container.addView(labelTv);

        TextView valueTv = new TextView(this);
        valueTv.setText(display);
        valueTv.setTextColor(Color.WHITE);
        valueTv.setTextSize(15);
        valueTv.setPadding(0, 0, 0, 4);
        container.addView(valueTv);
    }
}