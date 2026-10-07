package com.example.eventhiveai.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MyRegistrationsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerMyRegistrations;
    private ProgressBar progressBar;
    private LinearLayout emptyView;

    private ArrayList<RegistrationModel> registrationsList;
    private StudentRegAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_registrations);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        recyclerMyRegistrations = findViewById(R.id.recyclerMyRegistrations);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        recyclerMyRegistrations.setLayoutManager(new LinearLayoutManager(this));
        registrationsList = new ArrayList<>();
        adapter = new StudentRegAdapter(registrationsList);
        recyclerMyRegistrations.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadMyRegistrations();
    }

    private void loadMyRegistrations() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        db.collection("registrations")
                .whereEqualTo("studentId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    registrationsList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        RegistrationModel reg = doc.toObject(RegistrationModel.class);
                        if (reg == null) reg = new RegistrationModel();
                        reg.setRegistrationId(doc.getId());
                        if (doc.contains("eventName")) reg.setEventName(doc.getString("eventName"));
                        if (doc.contains("eventDate")) reg.setEventDate(doc.getString("eventDate"));
                        if (doc.contains("eventVenue")) reg.setEventVenue(doc.getString("eventVenue"));
                        if (doc.contains("status")) reg.setStatus(doc.getString("status"));

                        registrationsList.add(reg);
                    }

                    adapter.notifyDataSetChanged();
                    if (registrationsList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyRegistrationsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private static class StudentRegAdapter extends RecyclerView.Adapter<StudentRegAdapter.ViewHolder> {
        private final ArrayList<RegistrationModel> list;

        StudentRegAdapter(ArrayList<RegistrationModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.registration_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RegistrationModel item = list.get(position);
            holder.tvRegEventName.setText(item.getEventName());
            holder.tvRegStudentName.setText("📍 Venue: " + (item.getEventVenue().isEmpty() ? "Campus" : item.getEventVenue()));
            holder.tvRegDate.setText("📅 " + (item.getEventDate().isEmpty() ? "TBA" : item.getEventDate()));
            holder.tvRegStudentEmail.setText("Pass ID: " + item.getRegistrationId());

            if ("attended".equalsIgnoreCase(item.getStatus())) {
                holder.tvRegStatus.setText("ATTENDED ✓ (VERIFIED)");
                holder.tvRegStatus.setBackgroundColor(android.graphics.Color.parseColor("#11382A"));
                holder.tvRegStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
            } else {
                holder.tvRegStatus.setText("CONFIRMED PASS");
                holder.tvRegStatus.setBackgroundColor(android.graphics.Color.parseColor("#1E293B"));
                holder.tvRegStatus.setTextColor(android.graphics.Color.parseColor("#8B5CF6"));
            }

            if (holder.layoutAttendanceAction != null) {
                holder.layoutAttendanceAction.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRegEventName, tvRegStudentName, tvRegStudentEmail, tvRegDate, tvRegStatus;
            View layoutAttendanceAction;

            ViewHolder(@NonNull View v) {
                super(v);
                tvRegEventName = v.findViewById(R.id.tvRegEventName);
                tvRegStudentName = v.findViewById(R.id.tvRegStudentName);
                tvRegStudentEmail = v.findViewById(R.id.tvRegStudentEmail);
                tvRegDate = v.findViewById(R.id.tvRegDate);
                tvRegStatus = v.findViewById(R.id.tvRegStatus);
                layoutAttendanceAction = v.findViewById(R.id.layoutAttendanceAction);
            }
        }
    }
}
