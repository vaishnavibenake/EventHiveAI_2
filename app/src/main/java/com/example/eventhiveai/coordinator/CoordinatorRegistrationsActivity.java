package com.example.eventhiveai.coordinator;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.models.RegistrationModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class CoordinatorRegistrationsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerRegistrations;
    private ProgressBar progressBar;
    private LinearLayout emptyView;

    private TextView tvTotalRegCount;
    private TextView tvAttendedCount;
    private TextView tvPendingCount;

    private ArrayList<RegistrationModel> list;
    private RegAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_registrations);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        recyclerRegistrations = findViewById(R.id.recyclerRegistrations);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        tvTotalRegCount = findViewById(R.id.tvTotalRegCount);
        tvAttendedCount = findViewById(R.id.tvAttendedCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);

        recyclerRegistrations.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new RegAdapter(list, db, this::updateAttendanceMetrics);
        recyclerRegistrations.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadRegistrations();
    }

    private void loadRegistrations() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return;
        }
        db.collection("registrations").whereEqualTo("coordinatorId", currentUser.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    list.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        RegistrationModel reg = doc.toObject(RegistrationModel.class);
                        if (reg == null) reg = new RegistrationModel();
                        reg.setRegistrationId(doc.getId());
                        if (doc.contains("eventName")) reg.setEventName(doc.getString("eventName"));
                        if (doc.contains("studentName")) reg.setStudentName(doc.getString("studentName"));
                        if (doc.contains("studentEmail")) reg.setStudentEmail(doc.getString("studentEmail"));
                        if (doc.contains("eventDate")) reg.setEventDate(doc.getString("eventDate"));
                        if (doc.contains("status")) reg.setStatus(doc.getString("status"));

                        list.add(reg);
                    }

                    adapter.notifyDataSetChanged();
                    updateAttendanceMetrics();

                    if (list.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CoordinatorRegistrationsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAttendanceMetrics() {
        int total = list.size();
        int attended = 0;
        for (RegistrationModel r : list) {
            if ("attended".equalsIgnoreCase(r.getStatus())) {
                attended++;
            }
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

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.registration_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RegistrationModel item = items.get(position);
            holder.tvRegEventName.setText(item.getEventName());
            holder.tvRegStudentName.setText("👤 " + item.getStudentName());
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

                db.collection("registrations").document(item.getRegistrationId())
                        .update("status", newStatus)
                        .addOnSuccessListener(unused -> {
                            holder.btnToggleAttendance.setEnabled(true);
                            item.setStatus(newStatus);
                            notifyItemChanged(position);
                            if (onMetricsChanged != null) {
                                onMetricsChanged.run();
                            }
                            String msg = "attended".equals(newStatus)
                                    ? "Marked Present: " + item.getStudentName()
                                    : "Marked Absent / Pending: " + item.getStudentName();
                            Toast.makeText(holder.itemView.getContext(), msg, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            holder.btnToggleAttendance.setEnabled(true);
                            Toast.makeText(holder.itemView.getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

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
