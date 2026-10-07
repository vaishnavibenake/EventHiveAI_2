package com.example.eventhiveai;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.admin.EventModel;
import com.example.eventhiveai.coordinator.EditEventActivity;
import com.example.eventhiveai.models.RegistrationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvDetailStatus;
    private TextView tvDetailTitle;
    private TextView tvDetailClub;
    private TextView tvDetailDate;
    private TextView tvDetailTime;
    private TextView tvDetailVenue;
    private TextView tvDetailCategory;
    private TextView tvDetailDepartment;
    private TextView tvDetailCapacity;
    private TextView tvDetailBudget;
    private TextView tvDetailDeadline;
    private TextView tvDetailDescription;

    private LinearLayout layoutRejectionBanner;
    private TextView tvRejectionReason;

    private LinearLayout layoutAdminActions;
    private Button btnApproveDetail;
    private Button btnRejectDetail;

    private Button btnStudentRegister;
    private Button btnCoordinatorEdit;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String eventId;
    private String userRole = "STUDENT";
    private EventModel currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        eventId = getIntent().getStringExtra("EVENT_ID");
        String roleExtra = getIntent().getStringExtra("USER_ROLE");
        if (roleExtra != null && !roleExtra.isEmpty()) {
            userRole = roleExtra.toUpperCase();
        }

        initializeViews();
        loadEventDetails();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailClub = findViewById(R.id.tvDetailClub);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailTime = findViewById(R.id.tvDetailTime);
        tvDetailVenue = findViewById(R.id.tvDetailVenue);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailDepartment = findViewById(R.id.tvDetailDepartment);
        tvDetailCapacity = findViewById(R.id.tvDetailCapacity);
        tvDetailBudget = findViewById(R.id.tvDetailBudget);
        tvDetailDeadline = findViewById(R.id.tvDetailDeadline);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);

        layoutRejectionBanner = findViewById(R.id.layoutRejectionBanner);
        tvRejectionReason = findViewById(R.id.tvRejectionReason);

        layoutAdminActions = findViewById(R.id.layoutAdminActions);
        btnApproveDetail = findViewById(R.id.btnApproveDetail);
        btnRejectDetail = findViewById(R.id.btnRejectDetail);

        btnStudentRegister = findViewById(R.id.btnStudentRegister);
        btnCoordinatorEdit = findViewById(R.id.btnCoordinatorEdit);

        btnBack.setOnClickListener(v -> finish());
        btnApproveDetail.setOnClickListener(v -> approveEvent());
        btnRejectDetail.setOnClickListener(v -> showRejectDialog());
        btnStudentRegister.setOnClickListener(v -> handleStudentRegistration());

        btnCoordinatorEdit.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, EditEventActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });
    }

    private void loadEventDetails() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID not specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(EventDetailsActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    currentEvent = doc.toObject(EventModel.class);
                    if (currentEvent == null) {
                        currentEvent = new EventModel();
                    }
                    currentEvent.setEventId(doc.getId());
                    if (doc.contains("eventName")) currentEvent.setEventName(doc.getString("eventName"));
                    if (doc.contains("clubName")) currentEvent.setClubName(doc.getString("clubName"));
                    if (doc.contains("description")) currentEvent.setDescription(doc.getString("description"));
                    if (doc.contains("date")) currentEvent.setDate(doc.getString("date"));
                    if (doc.contains("startTime")) currentEvent.setStartTime(doc.getString("startTime"));
                    if (doc.contains("endTime")) currentEvent.setEndTime(doc.getString("endTime"));
                    if (doc.contains("venue")) currentEvent.setVenue(doc.getString("venue"));
                    if (doc.contains("category")) currentEvent.setCategory(doc.getString("category"));
                    if (doc.contains("department")) currentEvent.setDepartment(doc.getString("department"));
                    if (doc.contains("budget")) {
                        Double b = doc.getDouble("budget");
                        currentEvent.setBudget(b != null ? b : 0);
                    }
                    if (doc.contains("maxParticipants")) {
                        Long mp = doc.getLong("maxParticipants");
                        currentEvent.setMaxParticipants(mp != null ? mp.intValue() : 0);
                    }
                    if (doc.contains("registrationDeadline")) currentEvent.setRegistrationDeadline(doc.getString("registrationDeadline"));
                    if (doc.contains("status")) currentEvent.setStatus(doc.getString("status"));
                    if (doc.contains("rejectionReason")) currentEvent.setRejectionReason(doc.getString("rejectionReason"));
                    if (doc.contains("coordinatorId")) currentEvent.setCoordinatorId(doc.getString("coordinatorId"));

                    bindEventData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventDetailsActivity.this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void bindEventData() {
        tvDetailTitle.setText(currentEvent.getEventName());
        tvDetailClub.setText("Organized by " + (currentEvent.getClubName().isEmpty() ? "College Club" : currentEvent.getClubName()));
        tvDetailDate.setText("📅 Date: " + (currentEvent.getDate().isEmpty() ? "TBA" : currentEvent.getDate()));

        String timeStr = currentEvent.getStartTime();
        if (!currentEvent.getEndTime().isEmpty()) {
            timeStr += " - " + currentEvent.getEndTime();
        }
        tvDetailTime.setText("⏰ Time: " + (timeStr.isEmpty() ? "TBA" : timeStr));
        tvDetailVenue.setText("📍 Venue: " + (currentEvent.getVenue().isEmpty() ? "Campus Ground" : currentEvent.getVenue()));

        tvDetailCategory.setText("🏷️ Category: " + (currentEvent.getCategory().isEmpty() ? "General" : currentEvent.getCategory()));
        tvDetailDepartment.setText("🏛️ Dept: " + (currentEvent.getDepartment().isEmpty() ? "All" : currentEvent.getDepartment()));

        tvDetailCapacity.setText("👥 Capacity: " + (currentEvent.getMaxParticipants() > 0 ? currentEvent.getMaxParticipants() : "Open"));
        tvDetailBudget.setText("💰 Budget: ₹" + (int) currentEvent.getBudget());
        tvDetailDeadline.setText("⏳ Reg. Deadline: " + (currentEvent.getRegistrationDeadline().isEmpty() ? "None" : currentEvent.getRegistrationDeadline()));

        tvDetailDescription.setText(currentEvent.getDescription().isEmpty() ? "No description provided." : currentEvent.getDescription());

        // Status badge styling
        String status = currentEvent.getStatus().toUpperCase();
        tvDetailStatus.setText(status);
        if ("APPROVED".equals(status)) {
            tvDetailStatus.setBackgroundColor(Color.parseColor("#11382A"));
            tvDetailStatus.setTextColor(Color.parseColor("#10B981"));
        } else if ("REJECTED".equals(status)) {
            tvDetailStatus.setBackgroundColor(Color.parseColor("#3D1616"));
            tvDetailStatus.setTextColor(Color.parseColor("#EF4444"));
        } else {
            tvDetailStatus.setBackgroundColor(Color.parseColor("#3D2F12"));
            tvDetailStatus.setTextColor(Color.parseColor("#F59E0B"));
        }

        // Show Rejection Reason if rejected
        if ("rejected".equalsIgnoreCase(currentEvent.getStatus()) && !currentEvent.getRejectionReason().isEmpty()) {
            layoutRejectionBanner.setVisibility(View.VISIBLE);
            tvRejectionReason.setText(currentEvent.getRejectionReason());
        } else {
            layoutRejectionBanner.setVisibility(View.GONE);
        }

        // Role-specific action buttons
        if ("ADMIN".equals(userRole)) {
            if ("pending".equalsIgnoreCase(currentEvent.getStatus())) {
                layoutAdminActions.setVisibility(View.VISIBLE);
            } else {
                layoutAdminActions.setVisibility(View.GONE);
            }
            btnStudentRegister.setVisibility(View.GONE);
            btnCoordinatorEdit.setVisibility(View.GONE);
        } else if ("COORDINATOR".equals(userRole)) {
            layoutAdminActions.setVisibility(View.GONE);
            btnStudentRegister.setVisibility(View.GONE);
            btnCoordinatorEdit.setVisibility(View.VISIBLE);
        } else {
            // Student
            layoutAdminActions.setVisibility(View.GONE);
            btnCoordinatorEdit.setVisibility(View.GONE);
            if ("approved".equalsIgnoreCase(currentEvent.getStatus())) {
                btnStudentRegister.setVisibility(View.VISIBLE);
                checkStudentRegistrationStatus();
            } else {
                btnStudentRegister.setVisibility(View.GONE);
            }
        }
    }

    private void approveEvent() {
        db.collection("events").document(eventId)
                .update("status", "approved", "rejectionReason", "")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EventDetailsActivity.this, "Event approved! Now live for students.", Toast.LENGTH_SHORT).show();
                    loadEventDetails();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventDetailsActivity.this, "Failed to approve: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showRejectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reject Event");
        builder.setMessage("Please provide a reason for rejecting this event proposal:");

        final EditText input = new EditText(this);
        input.setHint("Enter reason (e.g. Venue double-booked, budget unverified)...");
        input.setPadding(40, 30, 40, 30);
        builder.setView(input);

        builder.setPositiveButton("Reject Event", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (TextUtils.isEmpty(reason)) {
                Toast.makeText(EventDetailsActivity.this, "Rejection reason cannot be empty.", Toast.LENGTH_LONG).show();
                return;
            }

            db.collection("events").document(eventId)
                    .update("status", "rejected", "rejectionReason", reason)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(EventDetailsActivity.this, "Event rejected with reason saved.", Toast.LENGTH_SHORT).show();
                        loadEventDetails();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EventDetailsActivity.this, "Failed to reject: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void checkStudentRegistrationStatus() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("registrations")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("studentId", user.getUid())
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        btnStudentRegister.setText("Already Registered ✓");
                        btnStudentRegister.setEnabled(false);
                        btnStudentRegister.setBackgroundColor(Color.parseColor("#059669"));
                    }
                });
    }

    private void handleStudentRegistration() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in to register.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Check Deadline
        if (!isRegistrationOpen(currentEvent.getRegistrationDeadline())) {
            Toast.makeText(this, "Registration Closed: The deadline for this event has passed.", Toast.LENGTH_LONG).show();
            return;
        }

        btnStudentRegister.setEnabled(false);
        btnStudentRegister.setText("Checking availability...");

        // 2. Check Duplicate Registration
        db.collection("registrations")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("studentId", user.getUid())
                .get()
                .addOnSuccessListener(dupSnapshots -> {
                    if (!dupSnapshots.isEmpty()) {
                        btnStudentRegister.setText("Already Registered ✓");
                        btnStudentRegister.setEnabled(false);
                        Toast.makeText(EventDetailsActivity.this, "Already Registered: You are already registered for this event.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // 3. Check Capacity
                    if (currentEvent.getMaxParticipants() > 0) {
                        db.collection("registrations")
                                .whereEqualTo("eventId", eventId)
                                .whereEqualTo("status", "registered")
                                .get()
                                .addOnSuccessListener(countSnapshots -> {
                                    if (countSnapshots.size() >= currentEvent.getMaxParticipants()) {
                                        btnStudentRegister.setText("Registration Full");
                                        btnStudentRegister.setEnabled(false);
                                        Toast.makeText(EventDetailsActivity.this, "Registration Full: Event has reached maximum capacity.", Toast.LENGTH_LONG).show();
                                    } else {
                                        proceedWithRegistration(user);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    btnStudentRegister.setEnabled(true);
                                    btnStudentRegister.setText("Register For Event →");
                                    Toast.makeText(EventDetailsActivity.this, "Error verifying capacity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        proceedWithRegistration(user);
                    }
                })
                .addOnFailureListener(e -> {
                    btnStudentRegister.setEnabled(true);
                    btnStudentRegister.setText("Register For Event →");
                    Toast.makeText(EventDetailsActivity.this, "Error checking registration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void proceedWithRegistration(FirebaseUser user) {
        // Fetch student profile details from users collection
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    String studentName = userDoc.exists() && userDoc.getString("name") != null
                            ? userDoc.getString("name") : "Student";
                    String studentEmail = user.getEmail() != null ? user.getEmail() : "";

                    String registrationId = "reg_" + System.currentTimeMillis() + "_" + user.getUid().substring(0, 5);

                    Map<String, Object> regData = new HashMap<>();
                    regData.put("registrationId", registrationId);
                    regData.put("eventId", eventId);
                    regData.put("coordinatorId", currentEvent.getCoordinatorId());
                    regData.put("eventName", currentEvent.getEventName());
                    regData.put("eventDate", currentEvent.getDate());
                    regData.put("eventVenue", currentEvent.getVenue());
                    regData.put("studentId", user.getUid());
                    regData.put("studentName", studentName);
                    regData.put("studentEmail", studentEmail);
                    regData.put("status", "registered");
                    regData.put("registeredAt", FieldValue.serverTimestamp());

                    db.collection("registrations").document(registrationId)
                            .set(regData)
                            .addOnSuccessListener(unused -> {
                                btnStudentRegister.setText("Registered ✓");
                                btnStudentRegister.setEnabled(false);
                                btnStudentRegister.setBackgroundColor(Color.parseColor("#059669"));
                                Toast.makeText(EventDetailsActivity.this, "Successfully registered for " + currentEvent.getEventName() + "!", Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                btnStudentRegister.setEnabled(true);
                                btnStudentRegister.setText("Register For Event →");
                                Toast.makeText(EventDetailsActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnStudentRegister.setEnabled(true);
                    btnStudentRegister.setText("Register For Event →");
                    Toast.makeText(EventDetailsActivity.this, "Failed to retrieve student profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isRegistrationOpen(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) {
            return true;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date deadlineDate = sdf.parse(deadline);
            Date now = new Date();
            if (deadlineDate != null && now.after(deadlineDate)) {
                return false;
            }
        } catch (Exception e) {
            // If date format differs, allow registration
            return true;
        }
        return true;
    }
}
