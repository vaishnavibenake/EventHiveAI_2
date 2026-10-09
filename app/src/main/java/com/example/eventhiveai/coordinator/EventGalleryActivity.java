package com.example.eventhiveai.coordinator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.admin.EventModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class EventGalleryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerEvents;
    private ProgressBar progressBarEvents;
    private TextView tvEmptyEvents;

    private ArrayList<EventModel> eventList;
    private EventAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_gallery);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        recyclerEvents = findViewById(R.id.recyclerEvents);
        progressBarEvents = findViewById(R.id.progressBarEvents);
        tvEmptyEvents = findViewById(R.id.tvEmptyEvents);

        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));

        eventList = new ArrayList<>();
        adapter = new EventAdapter(eventList);
        recyclerEvents.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadCompletedEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCompletedEvents();
    }

    private void loadCompletedEvents() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        progressBarEvents.setVisibility(View.VISIBLE);
        tvEmptyEvents.setVisibility(View.GONE);

        db.collection("events")
                .whereEqualTo("coordinatorId", user.getUid())
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    progressBarEvents.setVisibility(View.GONE);

                    eventList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        EventModel event = doc.toObject(EventModel.class);

                        if (event == null) {
                            event = new EventModel();
                        }

                        event.setEventId(doc.getId());

                        eventList.add(event);
                    }

                    adapter.notifyDataSetChanged();

                    if (eventList.isEmpty()) {
                        tvEmptyEvents.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyEvents.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {

                    progressBarEvents.setVisibility(View.GONE);

                    Toast.makeText(
                            EventGalleryActivity.this,
                            "Failed to load completed events: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private static class EventAdapter
            extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

        private final ArrayList<EventModel> items;

        EventAdapter(ArrayList<EventModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(
                            R.layout.event_gallery_item,
                            parent,
                            false
                    );

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull ViewHolder holder,
                int position) {

            EventModel event = items.get(position);

            holder.tvEventName.setText(event.getEventName());

            holder.tvClubName.setText(
                    event.getClubName().isEmpty()
                            ? "Club"
                            : event.getClubName()
            );

            holder.tvEventDate.setText(
                    "📅 " + event.getDate()
            );

            holder.tvEventVenue.setText(
                    "📍 " + event.getVenue()
            );
            holder.itemView.setOnClickListener(v -> {

                android.content.Intent intent =
                        new android.content.Intent(
                                v.getContext(),
                                EventGalleryDetailsActivity.class
                        );

                intent.putExtra(
                        "EVENT_ID",
                        event.getEventId()
                );

                intent.putExtra(
                        "EVENT_NAME",
                        event.getEventName()
                );

                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder
                extends RecyclerView.ViewHolder {

            TextView tvEventName;
            TextView tvClubName;
            TextView tvEventDate;
            TextView tvEventVenue;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                tvEventName =
                        itemView.findViewById(R.id.tvGalleryEventName);

                tvClubName =
                        itemView.findViewById(R.id.tvGalleryClubName);

                tvEventDate =
                        itemView.findViewById(R.id.tvGalleryEventDate);

                tvEventVenue =
                        itemView.findViewById(R.id.tvGalleryEventVenue);
            }
        }
    }
}