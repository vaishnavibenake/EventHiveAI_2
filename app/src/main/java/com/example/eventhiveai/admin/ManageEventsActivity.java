package com.example.eventhiveai.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ManageEventsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button tabPending;
    private Button tabApproved;
    private Button tabAll;

    private RecyclerView recyclerEvents;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private TextView tvEmptyMessage;

    private ArrayList<EventModel> eventList;
    private EventAdapter eventAdapter;
    private FirebaseFirestore db;

    private String currentFilter = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        tabPending = findViewById(R.id.tabPending);
        tabApproved = findViewById(R.id.tabApproved);
        tabAll = findViewById(R.id.tabAll);

        recyclerEvents = findViewById(R.id.recyclerEvents);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, "ADMIN", true, () -> loadEvents(currentFilter));
        recyclerEvents.setAdapter(eventAdapter);

        btnBack.setOnClickListener(v -> finish());

        tabPending.setOnClickListener(v -> {
            currentFilter = "pending";
            updateTabStyles();
            loadEvents(currentFilter);
        });

        tabApproved.setOnClickListener(v -> {
            currentFilter = "approved";
            updateTabStyles();
            loadEvents(currentFilter);
        });

        tabAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateTabStyles();
            loadEvents(currentFilter);
        });

        loadEvents(currentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents(currentFilter);
    }

    private void updateTabStyles() {
        int selectedBg = Color.parseColor("#7C3AED");
        int unselectedBg = Color.parseColor("#171A27");
        int selectedText = Color.parseColor("#FFFFFF");
        int unselectedText = Color.parseColor("#94A3B8");

        tabPending.setBackgroundColor(currentFilter.equals("pending") ? selectedBg : unselectedBg);
        tabPending.setTextColor(currentFilter.equals("pending") ? selectedText : unselectedText);

        tabApproved.setBackgroundColor(currentFilter.equals("approved") ? selectedBg : unselectedBg);
        tabApproved.setTextColor(currentFilter.equals("approved") ? selectedText : unselectedText);

        tabAll.setBackgroundColor(currentFilter.equals("all") ? selectedBg : unselectedBg);
        tabAll.setTextColor(currentFilter.equals("all") ? selectedText : unselectedText);
    }

    private void loadEvents(String filter) {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        Query query = db.collection("events");
        if (!"all".equals(filter)) {
            query = query.whereEqualTo("status", filter);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    eventList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        EventModel event = doc.toObject(EventModel.class);
                        if (event == null) {
                            event = new EventModel();
                        }
                        event.setEventId(doc.getId());
                        if (doc.contains("eventName")) event.setEventName(doc.getString("eventName"));
                        if (doc.contains("clubName")) event.setClubName(doc.getString("clubName"));
                        if (doc.contains("date")) event.setDate(doc.getString("date"));
                        if (doc.contains("venue")) event.setVenue(doc.getString("venue"));
                        if (doc.contains("status")) event.setStatus(doc.getString("status"));
                        if (doc.contains("category")) event.setCategory(doc.getString("category"));

                        eventList.add(event);
                    }

                    eventAdapter.notifyDataSetChanged();

                    if (eventList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        if ("pending".equals(filter)) {
                            tvEmptyMessage.setText("No pending event proposals to review.");
                        } else if ("approved".equals(filter)) {
                            tvEmptyMessage.setText("No approved events found.");
                        } else {
                            tvEmptyMessage.setText("No events recorded in the system.");
                        }
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageEventsActivity.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}