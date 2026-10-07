package com.example.eventhiveai.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.models.ClubModel;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManageClubsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnAddClub;
    private RecyclerView recyclerClubs;
    private ProgressBar progressBar;
    private LinearLayout emptyView;

    private ArrayList<ClubModel> clubList;
    private ClubAdapter clubAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_clubs);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnAddClub = findViewById(R.id.btnAddClub);
        recyclerClubs = findViewById(R.id.recyclerClubs);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        recyclerClubs.setLayoutManager(new LinearLayoutManager(this));
        clubList = new ArrayList<>();
        clubAdapter = new ClubAdapter(clubList, "ADMIN");
        recyclerClubs.setAdapter(clubAdapter);

        btnBack.setOnClickListener(v -> finish());
        btnAddClub.setOnClickListener(v -> showAddClubDialog());

        loadClubs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClubs();
    }

    private void loadClubs() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        db.collection("clubs").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    clubList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ClubModel club = doc.toObject(ClubModel.class);
                        if (club == null) club = new ClubModel();
                        club.setClubId(doc.getId());
                        if (doc.contains("name")) club.setName(doc.getString("name"));
                        if (doc.contains("description")) club.setDescription(doc.getString("description"));
                        if (doc.contains("coordinatorId")) club.setCoordinatorId(doc.getString("coordinatorId"));
                        if (doc.contains("contactEmail")) club.setContactEmail(doc.getString("contactEmail"));
                        if (doc.contains("contactPhone")) club.setContactPhone(doc.getString("contactPhone"));
                        if (doc.contains("status")) club.setStatus(doc.getString("status"));

                        clubList.add(club);
                    }

                    clubAdapter.notifyDataSetChanged();

                    if (clubList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageClubsActivity.this, "Failed to load clubs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddClubDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New College Club");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etName = new EditText(this);
        etName.setHint("Club Name (e.g. Literary Club)");
        layout.addView(etName);

        final EditText etDesc = new EditText(this);
        etDesc.setHint("Description");
        layout.addView(etDesc);

        final EditText etEmail = new EditText(this);
        etEmail.setHint("Contact Email");
        layout.addView(etEmail);

        final EditText etPhone = new EditText(this);
        etPhone.setHint("Contact Phone");
        layout.addView(etPhone);

        builder.setView(layout);

        builder.setPositiveButton("Create Club", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(ManageClubsActivity.this, "Club name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            String clubId = "club_" + System.currentTimeMillis();

            Map<String, Object> clubData = new HashMap<>();
            clubData.put("clubId", clubId);
            clubData.put("name", name);
            clubData.put("description", desc);
            clubData.put("coordinatorId", "");
            clubData.put("contactEmail", email);
            clubData.put("contactPhone", phone);
            clubData.put("status", "active");
            clubData.put("createdAt", FieldValue.serverTimestamp());

            db.collection("clubs").document(clubId).set(clubData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(ManageClubsActivity.this, "Club created successfully!", Toast.LENGTH_SHORT).show();
                        loadClubs();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ManageClubsActivity.this, "Failed to create club: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
