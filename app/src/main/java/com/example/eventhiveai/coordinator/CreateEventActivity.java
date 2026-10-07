package com.example.eventhiveai.coordinator;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etEventName;
    private EditText etClubName;
    private EditText etDescription;
    private EditText etDate;
    private EditText etStartTime;
    private EditText etEndTime;
    private EditText etVenue;
    private Spinner spinnerCategory;
    private Spinner spinnerDepartment;
    private EditText etBudget;
    private EditText etMaxParticipants;
    private EditText etDeadline;
    private Button btnSubmitEvent;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String coordinatorClubId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        etEventName = findViewById(R.id.etEventName);
        etClubName = findViewById(R.id.etClubName);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etVenue = findViewById(R.id.etVenue);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        etBudget = findViewById(R.id.etBudget);
        etMaxParticipants = findViewById(R.id.etMaxParticipants);
        etDeadline = findViewById(R.id.etDeadline);
        btnSubmitEvent = findViewById(R.id.btnSubmitEvent);

        setupSpinners();

        btnBack.setOnClickListener(v -> finish());
        btnSubmitEvent.setOnClickListener(v -> submitEvent());

        // Pre-fill club name if coordinator is assigned to a club
        loadCoordinatorClubInfo();
    }

    private void setupSpinners() {
        String[] categories = {"Technical", "Non-Technical", "Cultural", "Sports", "Workshop", "Hackathon", "Seminar"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        String[] departments = {"All Departments", "CSE", "CSE (AI & ML)", "Information Technology", "E&TC", "Electrical", "Mechanical", "Civil"};
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, departments);
        spinnerDepartment.setAdapter(deptAdapter);
    }

    private void loadCoordinatorClubInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String clubId = doc.getString("clubId");
                        if (clubId != null && !clubId.isEmpty()) {
                            coordinatorClubId = clubId;
                            db.collection("clubs").document(clubId).get()
                                    .addOnSuccessListener(clubDoc -> {
                                        if (clubDoc.exists() && clubDoc.getString("name") != null) {
                                            etClubName.setText(clubDoc.getString("name"));
                                        }
                                    });
                        }
                    }
                });
    }

    private void submitEvent() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to create events.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etEventName.getText().toString().trim();
        String club = etClubName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String venue = etVenue.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String department = spinnerDepartment.getSelectedItem().toString();
        String budgetStr = etBudget.getText().toString().trim();
        String maxStr = etMaxParticipants.getText().toString().trim();
        String deadline = etDeadline.getText().toString().trim();

        // VALIDATION
        if (TextUtils.isEmpty(name)) {
            etEventName.setError("Event Name is required");
            etEventName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(club)) {
            etClubName.setError("Club Name is required");
            etClubName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(coordinatorClubId)) {
            Toast.makeText(this, "Your account is not assigned to a club. Ask an administrator to assign one.", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(desc)) {
            etDescription.setError("Event Description is required");
            etDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(date)) {
            etDate.setError("Event Date is required (e.g. 2026-10-25)");
            etDate.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(startTime)) {
            etStartTime.setError("Start Time is required");
            etStartTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(venue)) {
            etVenue.setError("Venue is required");
            etVenue.requestFocus();
            return;
        }

        int maxParticipants = 100;
        if (!TextUtils.isEmpty(maxStr)) {
            try {
                maxParticipants = Integer.parseInt(maxStr);
            } catch (NumberFormatException ignored) {}
        }

        double budget = 0;
        if (!TextUtils.isEmpty(budgetStr)) {
            try {
                budget = Double.parseDouble(budgetStr);
            } catch (NumberFormatException ignored) {}
        }

        btnSubmitEvent.setEnabled(false);
        btnSubmitEvent.setText("Submitting Proposal...");

        String eventId = "event_" + System.currentTimeMillis();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventId", eventId);
        eventData.put("eventName", name);
        eventData.put("clubName", club);
        eventData.put("clubId", coordinatorClubId);
        eventData.put("description", desc);
        eventData.put("date", date);
        eventData.put("startTime", startTime);
        eventData.put("endTime", endTime);
        eventData.put("venue", venue);
        eventData.put("category", category);
        eventData.put("department", department);
        eventData.put("budget", budget);
        eventData.put("maxParticipants", maxParticipants);
        eventData.put("registrationDeadline", deadline);
        eventData.put("coordinatorId", currentUser.getUid());

        // CRITICAL: Must start as "pending"
        eventData.put("status", "pending");
        eventData.put("rejectionReason", "");
        eventData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("events").document(eventId).set(eventData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(CreateEventActivity.this,
                            "Event proposal submitted successfully! Pending faculty admin approval.",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitEvent.setEnabled(true);
                    btnSubmitEvent.setText("Submit Event Proposal (Pending Review) →");
                    Toast.makeText(CreateEventActivity.this, "Failed to submit event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
