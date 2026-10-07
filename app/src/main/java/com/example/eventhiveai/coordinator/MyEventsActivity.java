package com.example.eventhiveai.coordinator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.admin.EventAdapter;
import com.example.eventhiveai.admin.EventModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MyEventsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnNewEvent;
    private RecyclerView recyclerMyEvents;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private Button btnEmptyCreate;

    private ArrayList<EventModel> eventList;
    private EventAdapter eventAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnNewEvent = findViewById(R.id.btnNewEvent);
        recyclerMyEvents = findViewById(R.id.recyclerMyEvents);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        btnEmptyCreate = findViewById(R.id.btnEmptyCreate);

        recyclerMyEvents.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        // In My Events, clicking opens EventDetailsActivity with Edit option
        eventAdapter = new EventAdapter(eventList, "COORDINATOR", false, this::loadMyEvents);
        recyclerMyEvents.setAdapter(eventAdapter);

        btnBack.setOnClickListener(v -> finish());
        btnNewEvent.setOnClickListener(v -> startActivity(new Intent(MyEventsActivity.this, CreateEventActivity.class)));
        btnEmptyCreate.setOnClickListener(v -> startActivity(new Intent(MyEventsActivity.this, CreateEventActivity.class)));

        loadMyEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyEvents();
    }

    private void loadMyEvents() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        db.collection("events").whereEqualTo("coordinatorId", user.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    eventList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String coord = doc.getString("coordinatorId");
                        String club = doc.getString("clubId");

                        boolean matches = user.getUid().equals(coord);
                        if (matches) {
                            EventModel event = doc.toObject(EventModel.class);
                            if (event == null) event = new EventModel();
                            event.setEventId(doc.getId());
                            if (doc.contains("eventName")) event.setEventName(doc.getString("eventName"));
                            if (doc.contains("clubName")) event.setClubName(doc.getString("clubName"));
                            if (doc.contains("date")) event.setDate(doc.getString("date"));
                            if (doc.contains("venue")) event.setVenue(doc.getString("venue"));
                            if (doc.contains("status")) event.setStatus(doc.getString("status"));
                            if (doc.contains("category")) event.setCategory(doc.getString("category"));

                            eventList.add(event);
                        }
                    }

                    Collections.sort(eventList, Comparator.comparing(EventModel::getDate));
                    eventAdapter.notifyDataSetChanged();

                    if (eventList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyEventsActivity.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
