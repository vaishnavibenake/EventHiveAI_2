package com.example.eventhiveai.coordinator;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.models.RegistrationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CoordinatorRegistrationsActivity extends AppCompatActivity {

    private static final String TAG = "CoordRegistrations";

    private ImageButton btnBack;
    private Spinner spinnerEventsFilter;
    private RecyclerView recyclerRegistrations;
    private ProgressBar progressBar;
    private LinearLayout emptyView;

    private TextView tvTotalRegCount;
    private TextView tvAttendedCount;
    private TextView tvPendingCount;

    private final List<String> eventFilterIds = new ArrayList<>();
    private final List<String> eventFilterNames = new ArrayList<>();

    private final ArrayList<RegistrationModel> allRegistrationsList = new ArrayList<>();
    private final ArrayList<RegistrationModel> displayedRegistrationsList = new ArrayList<>();

    private RegAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.eventhiveai.SettingsActivity.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_registrations);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        spinnerEventsFilter = findViewById(R.id.spinnerEventsFilter);
        recyclerRegistrations = findViewById(R.id.recyclerRegistrations);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        tvTotalRegCount = findViewById(R.id.tvTotalRegCount);
        tvAttendedCount = findViewById(R.id.tvAttendedCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);

        recyclerRegistrations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RegAdapter(displayedRegistrationsList, db, this::updateAttendanceMetrics);
        recyclerRegistrations.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        setupEventFilterSpinner();
        loadCoordinatorEvents();
        loadRegistrations();
    }

    private void setupEventFilterSpinner() {
        eventFilterIds.clear();
        eventFilterNames.clear();

        eventFilterIds.add("ALL");
        eventFilterNames.add("All Events (Show All)");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                eventFilterNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventsFilter.setAdapter(spinnerAdapter);

        spinnerEventsFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyEventFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadCoordinatorEvents() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        db.collection("events")
                .whereEqualTo("coordinatorId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isFinishing() || isDestroyed()) return;

                    eventFilterIds.clear();
                    eventFilterNames.clear();

                    eventFilterIds.add("ALL");
                    eventFilterNames.add("All Events (Show All)");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String eId = doc.getId();
                        String eName = doc.getString("eventName");
                        if (eName == null || eName.isEmpty()) eName = eId;

                        eventFilterIds.add(eId);
                        eventFilterNames.add(eName);
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            CoordinatorRegistrationsActivity.this,
                            android.R.layout.simple_spinner_item,
                            eventFilterNames
                    );
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerEventsFilter.setAdapter(spinnerAdapter);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading coordinator events for filter: " + e.getMessage(), e));
    }

    private void loadRegistrations() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        db.collection("registrations").whereEqualTo("coordinatorId", currentUser.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isFinishing() || isDestroyed()) return;

                    progressBar.setVisibility(View.GONE);
                    allRegistrationsList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        RegistrationModel reg = doc.toObject(RegistrationModel.class);
                        if (reg == null) reg = new RegistrationModel();
                        reg.setRegistrationId(doc.getId());

                        if (doc.contains("eventId")) reg.setEventId(doc.getString("eventId"));
                        if (doc.contains("eventName")) reg.setEventName(doc.getString("eventName"));
                        if (doc.contains("studentName")) reg.setStudentName(doc.getString("studentName"));
                        if (doc.contains("studentEmail")) reg.setStudentEmail(doc.getString("studentEmail"));
                        if (doc.contains("eventDate")) reg.setEventDate(doc.getString("eventDate"));
                        if (doc.contains("status")) reg.setStatus(doc.getString("status"));
                        if (doc.contains("participationType")) reg.setParticipationType(doc.getString("participationType"));
                        if (doc.contains("teamName")) reg.setTeamName(doc.getString("teamName"));
                        if (doc.contains("memberNames")) {
                            List<String> names = (List<String>) doc.get("memberNames");
                            reg.setMemberNames(names);
                        }
                        if (doc.contains("memberCount")) {
                            Long mc = doc.getLong("memberCount");
                            reg.setMemberCount(mc != null ? mc.intValue() : 1);
                        }

                        allRegistrationsList.add(reg);
                    }

                    applyEventFilter();
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading registrations: " + e.getMessage(), e);
                    Toast.makeText(this, "Error loading registrations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void applyEventFilter() {
        int selectedIdx = spinnerEventsFilter != null ? spinnerEventsFilter.getSelectedItemPosition() : 0;
        String targetEventId = (selectedIdx >= 0 && selectedIdx < eventFilterIds.size()) ? eventFilterIds.get(selectedIdx) : "ALL";

        displayedRegistrationsList.clear();

        if ("ALL".equals(targetEventId)) {
            displayedRegistrationsList.addAll(allRegistrationsList);
        } else {
            for (RegistrationModel reg : allRegistrationsList) {
                if (targetEventId.equals(reg.getEventId())) {
                    displayedRegistrationsList.add(reg);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        updateAttendanceMetrics();

        if (emptyView != null) {
            emptyView.setVisibility(displayedRegistrationsList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateAttendanceMetrics() {
        int total = displayedRegistrationsList.size();
        int attended = 0;
        for (RegistrationModel r : displayedRegistrationsList) {
            if ("attended".equalsIgnoreCase(r.getStatus())) attended++;
        }
        int pending = total - attended;

        if (tvTotalRegCount != null) tvTotalRegCount.setText(String.valueOf(total));
        if (tvAttendedCount != null) tvAttendedCount.setText(String.valueOf(attended));
        if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(pending));
    }

    private static class RegAdapter extends RecyclerView.Adapter<RegAdapter.ViewHolder> {
        private final ArrayList<RegistrationModel> items;
        private final FirebaseFirestore db;
        private final Runnable onMetricsChanged;

        RegAdapter(ArrayList<RegistrationModel> items, FirebaseFirestore db, Runnable onMetricsChanged) {
            this.items = items;
            this.db = db;
            this.onMetricsChanged = onMetricsChanged;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.registration_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RegistrationModel item = items.get(position);
            holder.tvRegEventName.setText(item.getEventName());

            // Show team info if applicable
            String pType = item.getParticipationType();
            if ("Team".equals(pType) || "Pair".equals(pType)) {
                String teamInfo = "👥 " + pType + ": " + item.getTeamName();
                if (item.getMemberNames() != null && !item.getMemberNames().isEmpty()) {
                    teamInfo += " (" + String.join(", ", item.getMemberNames()) + ")";
                } else {
                    teamInfo += " [" + item.getMemberCount() + " members]";
                }
                holder.tvRegStudentName.setText(teamInfo);
            } else {
                holder.tvRegStudentName.setText("👤 " + item.getStudentName());
            }

            holder.tvRegStudentEmail.setText("✉️ " + item.getStudentEmail());
            holder.tvRegDate.setText("📅 " + (item.getEventDate().isEmpty() ? "Scheduled" : item.getEventDate()));

            boolean isAttended = "attended".equalsIgnoreCase(item.getStatus());

            if (isAttended) {
                holder.tvRegStatus.setText("ATTENDED (PRESENT)");
                holder.tvRegStatus.setBackgroundColor(Color.parseColor("#11382A"));
                holder.tvRegStatus.setTextColor(Color.parseColor("#10B981"));
                holder.btnToggleAttendance.setText("Mark Absent");
                holder.btnToggleAttendance.setBackgroundColor(Color.parseColor("#334155"));
            } else {
                holder.tvRegStatus.setText("REGISTERED");
                holder.tvRegStatus.setBackgroundColor(Color.parseColor("#1E293B"));
                holder.tvRegStatus.setTextColor(Color.parseColor("#94A3B8"));
                holder.btnToggleAttendance.setText("Mark Present");
                holder.btnToggleAttendance.setBackgroundColor(Color.parseColor("#7C3AED"));
            }

            holder.layoutAttendanceAction.setVisibility(View.VISIBLE);

            holder.btnToggleAttendance.setOnClickListener(v -> {
                String newStatus = isAttended ? "registered" : "attended";

                holder.btnToggleAttendance.setEnabled(false);

                // Update registration status
                db.collection("registrations").document(item.getRegistrationId())
                        .update("status", newStatus)
                        .addOnSuccessListener(unused -> {
                            // Also update all participation records for this registration
                            String newAttendance = "attended".equals(newStatus) ? "present" : "pending";
                            db.collection("participations")
                                    .whereEqualTo("registrationId", item.getRegistrationId())
                                    .get()
                                    .addOnSuccessListener(partSnap -> {
                                        for (QueryDocumentSnapshot pDoc : partSnap) {
                                            pDoc.getReference().update("attendance", newAttendance);
                                        }
                                    });

                            holder.btnToggleAttendance.setEnabled(true);
                            item.setStatus(newStatus);
                            notifyItemChanged(position);
                            if (onMetricsChanged != null) onMetricsChanged.run();
                            String msg = "attended".equals(newStatus)
                                    ? "Marked Present: " + item.getStudentName()
                                    : "Marked Absent: " + item.getStudentName();
                            Toast.makeText(holder.itemView.getContext(), msg, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            holder.btnToggleAttendance.setEnabled(true);
                            Toast.makeText(holder.itemView.getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRegEventName, tvRegStudentName, tvRegStudentEmail, tvRegDate, tvRegStatus;
            LinearLayout layoutAttendanceAction;
            Button btnToggleAttendance;

            ViewHolder(@NonNull View v) {
                super(v);
                tvRegEventName = v.findViewById(R.id.tvRegEventName);
                tvRegStudentName = v.findViewById(R.id.tvRegStudentName);
                tvRegStudentEmail = v.findViewById(R.id.tvRegStudentEmail);
                tvRegDate = v.findViewById(R.id.tvRegDate);
                tvRegStatus = v.findViewById(R.id.tvRegStatus);
                layoutAttendanceAction = v.findViewById(R.id.layoutAttendanceAction);
                btnToggleAttendance = v.findViewById(R.id.btnToggleAttendance);
            }
        }
    }
}
