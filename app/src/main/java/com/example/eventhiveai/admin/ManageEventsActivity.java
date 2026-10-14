package com.example.eventhiveai.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageEventsActivity extends AppCompatActivity implements EventAdapter.OnEventActionListener {

    private ImageButton btnBack;
    private TabLayout tabLayout;
    private RecyclerView rvEvents;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private FirebaseFirestore db;
    private EventAdapter adapter;
    private List<EventModel> eventList = new ArrayList<>();
    private String currentFilter = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        tabLayout = findViewById(R.id.tabLayout);
        rvEvents = findViewById(R.id.rvEvents);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this, eventList, this);
        rvEvents.setAdapter(adapter);

        setupTabs();
        loadEvents(currentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents(currentFilter);
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Pending Approvals"));
        tabLayout.addTab(tabLayout.newTab().setText("Approved Events"));
        tabLayout.addTab(tabLayout.newTab().setText("All Events"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    currentFilter = "pending";
                } else if (position == 1) {
                    currentFilter = "approved";
                } else {
                    currentFilter = "all";
                }
                loadEvents(currentFilter);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadEvents(String filter) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

        db.collection("events").get().addOnCompleteListener(task -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                eventList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    EventModel event = doc.toObject(EventModel.class);
                    if (event == null) event = new EventModel();

                    // Ensure document ID is always assigned
                    event.setEventId(doc.getId());

                    if (doc.contains("eventName")) event.setEventName(doc.getString("eventName"));
                    if (doc.contains("clubName")) event.setClubName(doc.getString("clubName"));
                    if (doc.contains("date")) event.setDate(doc.getString("date"));
                    if (doc.contains("venue")) event.setVenue(doc.getString("venue"));

                    String status = doc.getString("status");
                    if (status != null && !status.trim().isEmpty()) {
                        event.setStatus(status.trim().toLowerCase());
                    } else {
                        event.setStatus("pending");
                    }

                    if ("all".equalsIgnoreCase(filter) || event.getStatus().equalsIgnoreCase(filter)) {
                        eventList.add(event);
                    }
                }

                adapter.notifyDataSetChanged();

                if (eventList.isEmpty() && tvEmptyState != null) {
                    if ("pending".equalsIgnoreCase(filter)) {
                        tvEmptyState.setText("No pending event proposals to review.");
                    } else if ("approved".equalsIgnoreCase(filter)) {
                        tvEmptyState.setText("No approved events yet.");
                    } else {
                        tvEmptyState.setText("No events found in the database.");
                    }
                    tvEmptyState.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(this, "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApprove(EventModel event) {
        String eventId = event.getEventId();
        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(this, "Error: Invalid Event ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "approved");
        updates.put("rejectionReason", "");

        db.collection("events").document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Event approved! Now visible in Approved and Student portal.", Toast.LENGTH_SHORT).show();
                    loadEvents(currentFilter);
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error approving event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onReject(EventModel event) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rejection_reason, null);
        EditText etReason = dialogView.findViewById(R.id.etReason);
        android.widget.RadioGroup rgPredefined = dialogView.findViewById(R.id.rgPredefinedReasons);
        android.widget.RadioButton rbVenue = dialogView.findViewById(R.id.rbVenueConflict);
        android.widget.RadioButton rbBudget = dialogView.findViewById(R.id.rbBudgetIssue);
        android.widget.RadioButton rbIncomplete = dialogView.findViewById(R.id.rbIncompleteInfo);
        android.widget.RadioButton rbDuplicate = dialogView.findViewById(R.id.rbDuplicateEvent);

        if (rgPredefined != null) {
            rgPredefined.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbVenueConflict && rbVenue != null) {
                    etReason.setText(rbVenue.getText().toString());
                } else if (checkedId == R.id.rbBudgetIssue && rbBudget != null) {
                    etReason.setText(rbBudget.getText().toString());
                } else if (checkedId == R.id.rbIncompleteInfo && rbIncomplete != null) {
                    etReason.setText(rbIncomplete.getText().toString());
                } else if (checkedId == R.id.rbDuplicateEvent && rbDuplicate != null) {
                    etReason.setText(rbDuplicate.getText().toString());
                } else {
                    etReason.setText("");
                    etReason.requestFocus();
                }
            });
        }

        new AlertDialog.Builder(this)
                .setTitle("Reject Event Proposal")
                .setMessage("Please specify the rejection reason:")
                .setView(dialogView)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String reason = etReason != null ? etReason.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(reason)) {
                        Toast.makeText(this, "A rejection reason is mandatory!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    performRejection(event.getEventId(), reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performRejection(String eventId, String reason) {
        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(this, "Error: Invalid Event ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "rejected");
        updates.put("rejectionReason", reason);

        db.collection("events").document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Event proposal rejected. Reason recorded.", Toast.LENGTH_SHORT).show();
                    loadEvents(currentFilter);
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to reject: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}