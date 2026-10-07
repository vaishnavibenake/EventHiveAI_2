package com.example.eventhiveai.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.models.ClubModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ClubDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvClubDetailName;
    private TextView tvClubDetailStatus;
    private TextView tvClubDetailDesc;
    private TextView tvClubDetailCoordinator;
    private TextView tvClubDetailEmail;
    private TextView tvClubDetailPhone;

    private RecyclerView recyclerClubEvents;
    private ProgressBar progressBarEvents;
    private TextView tvNoEvents;

    private ArrayList<EventModel> clubEvents;
    private EventAdapter eventAdapter;
    private FirebaseFirestore db;

    private String clubId;
    private String clubName = "";
    private String userRole = "ADMIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_details);

        db = FirebaseFirestore.getInstance();

        clubId = getIntent().getStringExtra("CLUB_ID");
        String nameExtra = getIntent().getStringExtra("CLUB_NAME");
        if (nameExtra != null) clubName = nameExtra;

        String roleExtra = getIntent().getStringExtra("USER_ROLE");
        if (roleExtra != null) userRole = roleExtra;

        btnBack = findViewById(R.id.btnBack);
        tvClubDetailName = findViewById(R.id.tvClubDetailName);
        tvClubDetailStatus = findViewById(R.id.tvClubDetailStatus);
        tvClubDetailDesc = findViewById(R.id.tvClubDetailDesc);
        tvClubDetailCoordinator = findViewById(R.id.tvClubDetailCoordinator);
        tvClubDetailEmail = findViewById(R.id.tvClubDetailEmail);
        tvClubDetailPhone = findViewById(R.id.tvClubDetailPhone);

        recyclerClubEvents = findViewById(R.id.recyclerClubEvents);
        progressBarEvents = findViewById(R.id.progressBarEvents);
        tvNoEvents = findViewById(R.id.tvNoEvents);

        recyclerClubEvents.setLayoutManager(new LinearLayoutManager(this));
        clubEvents = new ArrayList<>();
        eventAdapter = new EventAdapter(clubEvents, userRole, false, null);
        recyclerClubEvents.setAdapter(eventAdapter);

        btnBack.setOnClickListener(v -> finish());

        loadClubDetails();
    }

    private void loadClubDetails() {
        if (clubId == null || clubId.isEmpty()) {
            Toast.makeText(this, "Club not specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("clubs").document(clubId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        ClubModel club = doc.toObject(ClubModel.class);
                        if (club == null) club = new ClubModel();
                        club.setClubId(doc.getId());

                        tvClubDetailName.setText(club.getName());
                        clubName = club.getName();
                        tvClubDetailDesc.setText(club.getDescription().isEmpty() ? "No description available." : club.getDescription());
                        tvClubDetailEmail.setText("✉️ Contact: " + (club.getContactEmail().isEmpty() ? "N/A" : club.getContactEmail()));
                        tvClubDetailPhone.setText("📞 Phone: " + (club.getContactPhone().isEmpty() ? "N/A" : club.getContactPhone()));

                        String status = club.getStatus() != null ? club.getStatus().toUpperCase() : "ACTIVE";
                        tvClubDetailStatus.setText(status);
                        if ("ACTIVE".equals(status)) {
                            tvClubDetailStatus.setBackgroundColor(Color.parseColor("#11382A"));
                            tvClubDetailStatus.setTextColor(Color.parseColor("#10B981"));
                        } else {
                            tvClubDetailStatus.setBackgroundColor(Color.parseColor("#3D1616"));
                            tvClubDetailStatus.setTextColor(Color.parseColor("#EF4444"));
                        }

                        // Load coordinator info if coordinatorId is set
                        final String coordId = club.getCoordinatorId();
                        if (coordId != null && !coordId.isEmpty()) {
                            db.collection("users").document(coordId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists() && userDoc.getString("name") != null) {
                                            tvClubDetailCoordinator.setText("👤 Coordinator: " + userDoc.getString("name")
                                                    + " (" + userDoc.getString("email") + ")");
                                        } else {
                                            tvClubDetailCoordinator.setText("👤 Coordinator ID: " + coordId);
                                        }
                                    });
                        } else {
                            tvClubDetailCoordinator.setText("👤 Coordinator: Faculty In-charge Assigned");
                        }

                        loadClubEvents();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ClubDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadClubEvents() {
        progressBarEvents.setVisibility(View.VISIBLE);
        tvNoEvents.setVisibility(View.GONE);

        // Fetch events where clubId matches or clubName matches
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBarEvents.setVisibility(View.GONE);
                    clubEvents.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String docClubId = doc.getString("clubId");
                        String docClubName = doc.getString("clubName");

                        boolean matches = (docClubId != null && docClubId.equals(clubId)) ||
                                (!clubName.isEmpty() && docClubName != null && docClubName.equalsIgnoreCase(clubName));

                        if (matches) {
                            EventModel event = doc.toObject(EventModel.class);
                            if (event == null) event = new EventModel();
                            event.setEventId(doc.getId());
                            if (doc.contains("eventName")) event.setEventName(doc.getString("eventName"));
                            if (doc.contains("clubName")) event.setClubName(doc.getString("clubName"));
                            if (doc.contains("date")) event.setDate(doc.getString("date"));
                            if (doc.contains("venue")) event.setVenue(doc.getString("venue"));
                            if (doc.contains("status")) event.setStatus(doc.getString("status"));

                            clubEvents.add(event);
                        }
                    }

                    eventAdapter.notifyDataSetChanged();
                    if (clubEvents.isEmpty()) {
                        tvNoEvents.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBarEvents.setVisibility(View.GONE);
                });
    }
}
