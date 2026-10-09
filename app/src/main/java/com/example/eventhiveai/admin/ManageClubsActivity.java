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
                        if (doc.contains("department")) club.setDepartment(doc.getString("department"));
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

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        scrollView.addView(layout);

        final EditText etName = new EditText(this);
        etName.setHint("Club Name (e.g. Robotics & IoT Club) *");
        layout.addView(etName);

        final EditText etClubId = new EditText(this);
        etClubId.setHint("Unique Club ID (e.g. robotics_club) *");
        layout.addView(etClubId);

        final EditText etDepartment = new EditText(this);
        etDepartment.setHint("Department (e.g. CSE / E&TC / All)");
        layout.addView(etDepartment);

        final EditText etDesc = new EditText(this);
        etDesc.setHint("Description");
        layout.addView(etDesc);

        final EditText etEmail = new EditText(this);
        etEmail.setHint("Contact Email");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(etEmail);

        final EditText etPhone = new EditText(this);
        etPhone.setHint("Contact Phone");
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        // Auto-slugify club name into clubId as user types
        etName.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && etClubId.getTag() == null) {
                    String slug = s.toString().toLowerCase().trim()
                            .replaceAll("[^a-z0-9]", "_")
                            .replaceAll("_+", "_");
                    if (slug.endsWith("_")) {
                        slug = slug.substring(0, slug.length() - 1);
                    }
                    etClubId.setText(slug);
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        etClubId.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etClubId.setTag("USER_EDITED");
            }
        });

        builder.setView(scrollView);

        builder.setPositiveButton("Create Club", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String rawClubId = etClubId.getText().toString().trim();
            String dept = etDepartment.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Club name is required");
                etName.requestFocus();
                return;
            }

            String clubId = rawClubId.toLowerCase().replaceAll("[^a-z0-9_]", "_");
            if (TextUtils.isEmpty(clubId)) {
                etClubId.setError("Valid Club ID is required");
                etClubId.requestFocus();
                return;
            }

            // Check for duplicate Club ID in Firestore
            db.collection("clubs").document(clubId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Toast.makeText(ManageClubsActivity.this,
                                    "A club with ID '" + clubId + "' already exists! Please use a unique Club ID.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Create club document
                            Map<String, Object> clubData = new HashMap<>();
                            clubData.put("clubId", clubId);
                            clubData.put("name", name);
                            clubData.put("department", dept.isEmpty() ? "All Departments" : dept);
                            clubData.put("description", desc);
                            clubData.put("coordinatorId", "");
                            clubData.put("contactEmail", email);
                            clubData.put("contactPhone", phone);
                            clubData.put("status", "active");
                            clubData.put("createdAt", FieldValue.serverTimestamp());

                            db.collection("clubs").document(clubId).set(clubData)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(ManageClubsActivity.this,
                                                "✓ Club '" + name + "' created successfully!",
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        loadClubs();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ManageClubsActivity.this,
                                                "Failed to create club: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ManageClubsActivity.this,
                                "Error verifying Club ID: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
