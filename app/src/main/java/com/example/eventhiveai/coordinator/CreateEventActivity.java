
package com.example.eventhiveai.coordinator;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    private EditText etEventName, etClubName, etDescription;
    private EditText etDate, etStartTime, etEndTime, etVenue;
    private Spinner spinnerCategory, spinnerDepartment;
    private EditText etBudget, etMaxParticipants, etDeadline;

    private Spinner spinnerParticipationType;
    private LinearLayout teamSizeContainer;
    private EditText etTeamSizeMin, etTeamSizeMax;

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

        spinnerParticipationType = findViewById(R.id.spinnerParticipationType);
        teamSizeContainer = findViewById(R.id.teamSizeContainer);
        etTeamSizeMin = findViewById(R.id.etTeamSizeMin);
        etTeamSizeMax = findViewById(R.id.etTeamSizeMax);

        btnSubmitEvent = findViewById(R.id.btnSubmitEvent);

        setupSpinners();

        spinnerParticipationType.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        String selected =
                                spinnerParticipationType.getSelectedItem().toString();

                        if ("Team".equals(selected)) {
                            teamSizeContainer.setVisibility(View.VISIBLE);
                        } else {
                            teamSizeContainer.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                });

        btnBack.setOnClickListener(v -> finish());
        btnSubmitEvent.setOnClickListener(v -> submitEvent());

        loadCoordinatorClubInfo();
    }

    private void setupSpinners() {
        String[] categories = {
                "Technical", "Non-Technical", "Cultural",
                "Sports", "Workshop", "Hackathon", "Seminar"
        };

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        catAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerCategory.setAdapter(catAdapter);

        String[] departments = {
                "All Departments", "CSE", "CSE (AI & ML)",
                "Information Technology", "E&TC", "Electrical",
                "Mechanical", "Civil"
        };

        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                departments
        );
        deptAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerDepartment.setAdapter(deptAdapter);

        String[] participationTypes = {
                "Individual", "Pair", "Team"
        };

        ArrayAdapter<String> participationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                participationTypes
        );
        participationAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerParticipationType.setAdapter(participationAdapter);
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
                                        if (clubDoc.exists()
                                                && clubDoc.getString("name") != null) {
                                            etClubName.setText(
                                                    clubDoc.getString("name")
                                            );
                                        }
                                    });
                        }
                    }
                });
    }

    private void submitEvent() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this,
                    "Please sign in to create events.",
                    Toast.LENGTH_SHORT).show();
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

        String participationType =
                spinnerParticipationType.getSelectedItem().toString();

        int minTeamSize = 1;
        int maxTeamSize = 1;

        if ("Team".equals(participationType)) {
            String minStr = etTeamSizeMin.getText().toString().trim();
            String maxTeamStr = etTeamSizeMax.getText().toString().trim();

            if (TextUtils.isEmpty(minStr)) {
                etTeamSizeMin.setError("Enter minimum team size");
                etTeamSizeMin.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(maxTeamStr)) {
                etTeamSizeMax.setError("Enter maximum team size");
                etTeamSizeMax.requestFocus();
                return;
            }

            try {
                minTeamSize = Integer.parseInt(minStr);
                maxTeamSize = Integer.parseInt(maxTeamStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this,
                        "Enter valid team sizes.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (minTeamSize < 2 || maxTeamSize < minTeamSize) {
                Toast.makeText(this,
                        "Team size must be at least 2, and maximum must be greater than or equal to minimum.",
                        Toast.LENGTH_LONG).show();
                return;
            }
        } else if ("Pair".equals(participationType)) {
            minTeamSize = 2;
            maxTeamSize = 2;
        }

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
            Toast.makeText(this,
                    "Your account is not assigned to a club. Ask an administrator to assign one.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(desc)) {
            etDescription.setError("Event Description is required");
            etDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(date)) {
            etDate.setError("Event Date is required (YYYY-MM-DD)");
            etDate.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(startTime)) {
            etStartTime.setError("Start Time is required");
            etStartTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(endTime)) {
            etEndTime.setError("End Time is required");
            etEndTime.requestFocus();
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

                if (maxParticipants < 1) {
                    etMaxParticipants.setError(
                            "Maximum participants must be at least 1"
                    );
                    etMaxParticipants.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etMaxParticipants.setError("Enter a valid number");
                etMaxParticipants.requestFocus();
                return;
            }
        }

        double budget = 0;

        if (!TextUtils.isEmpty(budgetStr)) {
            try {
                budget = Double.parseDouble(budgetStr);

                if (budget < 0) {
                    etBudget.setError("Budget cannot be negative");
                    etBudget.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etBudget.setError("Enter a valid budget");
                etBudget.requestFocus();
                return;
            }
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

        // Participation settings
        eventData.put("participationType", participationType);
        eventData.put("minTeamSize", minTeamSize);
        eventData.put("maxTeamSize", maxTeamSize);

        // Event proposals must begin as pending
        eventData.put("status", "pending");
        eventData.put("rejectionReason", "");
        eventData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("events").document(eventId).set(eventData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            CreateEventActivity.this,
                            "Event proposal submitted successfully! Pending faculty admin approval.",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitEvent.setEnabled(true);
                    btnSubmitEvent.setText(
                            "Submit Event Proposal (Pending Review) →"
                    );

                    Toast.makeText(
                            CreateEventActivity.this,
                            "Failed to submit event: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}