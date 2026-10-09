package com.example.eventhiveai.coordinator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

        // Keep existing event cards and completion actions.
        eventAdapter = new EventAdapter(
                eventList,
                "COORDINATOR",
                false,
                this::loadMyEvents
        );
        recyclerMyEvents.setAdapter(eventAdapter);

        btnBack.setOnClickListener(v -> finish());

        btnNewEvent.setOnClickListener(v ->
                startActivity(new Intent(
                        MyEventsActivity.this,
                        CreateEventActivity.class
                ))
        );

        btnEmptyCreate.setOnClickListener(v ->
                startActivity(new Intent(
                        MyEventsActivity.this,
                        CreateEventActivity.class
                ))
        );

        // Open the winner-upload flow from a selected completed event.
        recyclerMyEvents.addOnChildAttachStateChangeListener(
                new RecyclerView.OnChildAttachStateChangeListener() {
                    @Override
                    public void onChildViewAttachedToWindow(View view) {
                        view.setOnLongClickListener(v -> {
                            int position = recyclerMyEvents.getChildAdapterPosition(v);

                            if (position == RecyclerView.NO_POSITION) {
                                return true;
                            }

                            EventModel event = eventList.get(position);

                            if (!"completed".equalsIgnoreCase(event.getStatus())) {
                                Toast.makeText(
                                        MyEventsActivity.this,
                                        "Only completed events can have winners published.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return true;
                            }

                            new AlertDialog.Builder(MyEventsActivity.this)
                                    .setTitle(event.getEventName())
                                    .setMessage("Upload winners for this completed event?")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("Upload Winners", (dialog, which) -> {
                                        Intent intent = new Intent(
                                                MyEventsActivity.this,
                                                UploadWinnersActivity.class
                                        );
                                        intent.putExtra("EVENT_ID", event.getEventId());
                                        intent.putExtra("EVENT_NAME", event.getEventName());
                                        startActivity(intent);
                                    })
                                    .show();

                            return true;
                        });
                    }

                    @Override
                    public void onChildViewDetachedFromWindow(View view) {
                        view.setOnLongClickListener(null);
                    }
                }
        );

        loadMyEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyEvents();
    }

    private void loadMyEvents() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        db.collection("events")
                .whereEqualTo("coordinatorId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    eventList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String coordinatorId = doc.getString("coordinatorId");

                        if (user.getUid().equals(coordinatorId)) {
                            EventModel event = doc.toObject(EventModel.class);

                            if (event == null) {
                                event = new EventModel();
                            }

                            event.setEventId(doc.getId());

                            if (doc.contains("eventName")) {
                                event.setEventName(doc.getString("eventName"));
                            }
                            if (doc.contains("clubName")) {
                                event.setClubName(doc.getString("clubName"));
                            }
                            if (doc.contains("date")) {
                                event.setDate(doc.getString("date"));
                            }
                            if (doc.contains("venue")) {
                                event.setVenue(doc.getString("venue"));
                            }
                            if (doc.contains("status")) {
                                event.setStatus(doc.getString("status"));
                            }
                            if (doc.contains("category")) {
                                event.setCategory(doc.getString("category"));
                            }

                            eventList.add(event);
                        }
                    }

                    Collections.sort(
                            eventList,
                            Comparator.comparing(EventModel::getDate)
                    );

                    eventAdapter.notifyDataSetChanged();

                    emptyView.setVisibility(
                            eventList.isEmpty() ? View.VISIBLE : View.GONE
                    );
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(
                            MyEventsActivity.this,
                            "Failed to load events: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}

