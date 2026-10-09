package com.example.eventhiveai.coordinator;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ClubProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private EditText etClubProfileName;
    private EditText etClubProfileDepartment;
    private EditText etClubProfileGoal;
    private EditText etClubProfileDesc;
    private EditText etClubProfileFocusAreas;
    private EditText etClubProfileEmail;
    private EditText etClubProfilePhone;

    private Button btnSaveClubProfile;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String currentClubId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_profile);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views
        btnBack = findViewById(R.id.btnBack);

        etClubProfileName = findViewById(R.id.etClubProfileName);
        etClubProfileDepartment = findViewById(R.id.etClubProfileDepartment);
        etClubProfileGoal = findViewById(R.id.etClubProfileGoal);
        etClubProfileDesc = findViewById(R.id.etClubProfileDesc);
        etClubProfileFocusAreas = findViewById(R.id.etClubProfileFocusAreas);
        etClubProfileEmail = findViewById(R.id.etClubProfileEmail);
        etClubProfilePhone = findViewById(R.id.etClubProfilePhone);

        btnSaveClubProfile = findViewById(R.id.btnSaveClubProfile);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Save button
        btnSaveClubProfile.setOnClickListener(v -> saveClubProfile());

        // Load current coordinator's club
        loadClubProfile();
    }


    // =========================================================
    // LOAD CLUB PROFILE
    // =========================================================

    private void loadClubProfile() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String uid = user.getUid();

        // First get the coordinator's user document
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {

                    if (!userDoc.exists()) {
                        Toast.makeText(
                                ClubProfileActivity.this,
                                "User profile not found.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    String clubId = userDoc.getString("clubId");

                    if (clubId == null || clubId.trim().isEmpty()) {

                        Toast.makeText(
                                ClubProfileActivity.this,
                                "No club assigned to your account.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    // Store the club ID
                    currentClubId = clubId;

                    // Now load the club
                    loadClubFromFirestore(clubId);

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ClubProfileActivity.this,
                            "Failed to load user profile.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // =========================================================
    // LOAD CLUB FROM FIRESTORE
    // =========================================================

    private void loadClubFromFirestore(String clubId) {

        db.collection("clubs")
                .document(clubId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {

                        Toast.makeText(
                                ClubProfileActivity.this,
                                "Club profile not found.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    // Club name
                    String name = doc.getString("name");

                    if (name != null) {
                        etClubProfileName.setText(name);
                    }

                    // Department
                    String department = doc.getString("department");

                    if (department != null) {
                        etClubProfileDepartment.setText(department);
                    }

                    // Goal
                    String goal = doc.getString("goal");

                    if (goal != null) {
                        etClubProfileGoal.setText(goal);
                    }

                    // Description
                    String description = doc.getString("description");

                    if (description != null) {
                        etClubProfileDesc.setText(description);
                    }

                    // Focus areas
                    String focusAreas = doc.getString("focusAreas");

                    if (focusAreas != null) {
                        etClubProfileFocusAreas.setText(focusAreas);
                    }

                    // Email
                    String email = doc.getString("contactEmail");

                    if (email != null) {
                        etClubProfileEmail.setText(email);
                    }

                    // Phone
                    String phone = doc.getString("contactPhone");

                    if (phone != null) {
                        etClubProfilePhone.setText(phone);
                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ClubProfileActivity.this,
                            "Failed to load club: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // =========================================================
    // SAVE CLUB PROFILE
    // =========================================================

    private void saveClubProfile() {

        // Make sure a club was loaded
        if (currentClubId == null || currentClubId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "No club assigned.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Get values
        String name = etClubProfileName.getText().toString().trim();
        String department = etClubProfileDepartment.getText().toString().trim();
        String goal = etClubProfileGoal.getText().toString().trim();
        String description = etClubProfileDesc.getText().toString().trim();
        String focusAreas = etClubProfileFocusAreas.getText().toString().trim();
        String email = etClubProfileEmail.getText().toString().trim();
        String phone = etClubProfilePhone.getText().toString().trim();


        // Basic validation
        if (TextUtils.isEmpty(name)) {

            etClubProfileName.setError("Club name is required");
            etClubProfileName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(department)) {

            etClubProfileDepartment.setError("Department is required");
            etClubProfileDepartment.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(goal)) {

            etClubProfileGoal.setError("Club goal is required");
            etClubProfileGoal.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {

            etClubProfileDesc.setError("Description is required");
            etClubProfileDesc.requestFocus();
            return;
        }


        // Prepare Firestore data
        Map<String, Object> updates = new HashMap<>();

        updates.put("name", name);
        updates.put("department", department);
        updates.put("goal", goal);
        updates.put("description", description);
        updates.put("focusAreas", focusAreas);
        updates.put("contactEmail", email);
        updates.put("contactPhone", phone);


        // Save only to the coordinator's assigned club
        db.collection("clubs")
                .document(currentClubId)
                .update(updates)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            ClubProfileActivity.this,
                            "Club profile updated successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ClubProfileActivity.this,
                            "Failed to update club: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}