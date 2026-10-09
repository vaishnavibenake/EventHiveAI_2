package com.example.eventhiveai.student;

import android.graphics.Color;
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
import com.example.eventhiveai.models.ParticipationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MyParticipationActivity extends AppCompatActivity {

    private RecyclerView recyclerParticipation;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private ArrayList<ParticipationModel> list;
    private ParticipationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_participation);

        ImageButton btnBack = findViewById(R.id.btnBack);
        recyclerParticipation = findViewById(R.id.recyclerParticipation);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        recyclerParticipation.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new ParticipationAdapter(list);
        recyclerParticipation.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        loadParticipation();
    }

    private void loadParticipation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        FirebaseFirestore.getInstance().collection("participations")
                .whereEqualTo("studentId", user.getUid())
                .get()
                .addOnSuccessListener(snap -> {
                    progressBar.setVisibility(View.GONE);
                    list.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        ParticipationModel p = doc.toObject(ParticipationModel.class);
                        if (p == null) p = new ParticipationModel();
                        p.setParticipationId(doc.getId());
                        list.add(p);
                    }
                    adapter.notifyDataSetChanged();
                    emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private static class ParticipationAdapter extends RecyclerView.Adapter<ParticipationAdapter.VH> {
        private final ArrayList<ParticipationModel> items;
        ParticipationAdapter(ArrayList<ParticipationModel> items) { this.items = items; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.participation_item, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            ParticipationModel p = items.get(pos);
            h.tvPartEventName.setText(p.getEventName());
            h.tvPartType.setText("👤 " + p.getParticipationType());
            h.tvPartDateVenue.setText("📅 " + p.getEventDate() + " • 📍 " + p.getEventVenue());

            if (!p.getTeamName().isEmpty()) {
                h.tvPartTeam.setVisibility(View.VISIBLE);
                h.tvPartTeam.setText("🏷️ Team: " + p.getTeamName());
            } else {
                h.tvPartTeam.setVisibility(View.GONE);
            }

            // Attendance
            String att = p.getAttendance();
            if ("present".equalsIgnoreCase(att)) {
                h.tvPartAttendance.setText("✅ Present");
                h.tvPartAttendance.setTextColor(Color.parseColor("#10B981"));
            } else if ("absent".equalsIgnoreCase(att)) {
                h.tvPartAttendance.setText("❌ Absent");
                h.tvPartAttendance.setTextColor(Color.parseColor("#EF4444"));
            } else {
                h.tvPartAttendance.setText("📋 Attendance Pending");
                h.tvPartAttendance.setTextColor(Color.parseColor("#94A3B8"));
            }

            // Result
            String result = p.getResult();
            if (!"none".equals(result) && !result.isEmpty()) {
                h.tvPartResult.setVisibility(View.VISIBLE);
                h.tvPartResult.setText("🏆 " + result);
            } else {
                h.tvPartResult.setVisibility(View.GONE);
            }

            // Status badge
            if ("cancelled".equalsIgnoreCase(p.getStatus())) {
                h.tvPartStatus.setText("CANCELLED");
                h.tvPartStatus.setBackgroundColor(Color.parseColor("#3D1616"));
                h.tvPartStatus.setTextColor(Color.parseColor("#EF4444"));
            } else {
                h.tvPartStatus.setText("REGISTERED");
                h.tvPartStatus.setBackgroundColor(Color.parseColor("#11382A"));
                h.tvPartStatus.setTextColor(Color.parseColor("#10B981"));
            }
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvPartEventName, tvPartType, tvPartTeam, tvPartDateVenue,
                    tvPartAttendance, tvPartResult, tvPartStatus;
            VH(@NonNull View v) {
                super(v);
                tvPartEventName = v.findViewById(R.id.tvPartEventName);
                tvPartType = v.findViewById(R.id.tvPartType);
                tvPartTeam = v.findViewById(R.id.tvPartTeam);
                tvPartDateVenue = v.findViewById(R.id.tvPartDateVenue);
                tvPartAttendance = v.findViewById(R.id.tvPartAttendance);
                tvPartResult = v.findViewById(R.id.tvPartResult);
                tvPartStatus = v.findViewById(R.id.tvPartStatus);
            }
        }
    }
}
