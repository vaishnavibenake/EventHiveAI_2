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
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);

        db.collection("clubs").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
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

                    if (clubList.isEmpty() && emptyView != null) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else if (emptyView != null) {
                        emptyView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageClubsActivity.this, "Failed to load clubs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddClubDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_club, null);
        EditText etName = dialogView.findViewById(R.id.etClubName);
        EditText etDept = dialogView.findViewById(R.id.etDepartment);
        EditText etDesc = dialogView.findViewById(R.id.etDescription);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Create Club", null) // Handled below to prevent premature dismiss
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dInterface -> {
            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String name = etName != null ? etName.getText().toString().trim() : "";
                String dept = etDept != null ? etDept.getText().toString().trim() : "";
                String desc = etDesc != null ? etDesc.getText().toString().trim() : "";
                String email = etEmail != null ? etEmail.getText().toString().trim() : "";
                String phone = etPhone != null ? etPhone.getText().toString().trim() : "";

                if (TextUtils.isEmpty(name)) {
                    if (etName != null) etName.setError("Club name is required");
                    return;
                }

                // Check duplicate locally first
                for (ClubModel c : clubList) {
                    if (c.getName() != null && c.getName().equalsIgnoreCase(name)) {
                        Toast.makeText(ManageClubsActivity.this, "A club named \"" + name + "\" already exists.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                dialog.dismiss();
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

                String clubId = "club_" + System.currentTimeMillis();

                Map<String, Object> clubData = new HashMap<>();
                clubData.put("clubId", clubId);
                clubData.put("name", name);
                clubData.put("department", dept);
                clubData.put("description", desc);
                clubData.put("coordinatorId", "");
                clubData.put("contactEmail", email);
                clubData.put("contactPhone", phone);
                clubData.put("status", "active");
                clubData.put("createdAt", FieldValue.serverTimestamp());

                db.collection("clubs").document(clubId).set(clubData)
                        .addOnSuccessListener(unused -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(ManageClubsActivity.this, "Club \"" + name + "\" created successfully!", Toast.LENGTH_SHORT).show();
                            loadClubs();
                        })
                        .addOnFailureListener(e -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(ManageClubsActivity.this, "Failed to create club: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            });
        });

        dialog.show();
    }
}
