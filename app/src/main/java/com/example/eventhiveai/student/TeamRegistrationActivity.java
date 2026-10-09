package com.example.eventhiveai.student;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamRegistrationActivity extends AppCompatActivity {

    private TextView tvScreenTitle, tvEventName, tvEventInfo, tvTeamSizeInfo;
    private TextView tvTeamNameLabel, tvLeaderInfo, tvMemberCount;
    private EditText etTeamName, etTeammateEmail;
    private Button btnAddTeammate, btnRegisterTeam;
    private LinearLayout layoutTeamMembers;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String eventId, eventName, eventDate, eventVenue, coordinatorId;
    private String participationType;
    private int minTeamSize, maxTeamSize;

    // Leader data
    private String leaderId, leaderName, leaderEmail, leaderDepartment;

    // Teammates
    private final List<String> memberIds = new ArrayList<>();
    private final List<String> memberNames = new ArrayList<>();
    private final List<String> memberEmails = new ArrayList<>();
    private final List<String> memberDepartments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_registration);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get event data from intent
        eventId = getIntent().getStringExtra("EVENT_ID");
        eventName = getIntent().getStringExtra("EVENT_NAME");
        eventDate = getIntent().getStringExtra("EVENT_DATE");
        eventVenue = getIntent().getStringExtra("EVENT_VENUE");
        coordinatorId = getIntent().getStringExtra("COORDINATOR_ID");
        participationType = getIntent().getStringExtra("PARTICIPATION_TYPE");
        minTeamSize = getIntent().getIntExtra("MIN_TEAM_SIZE", 2);
        maxTeamSize = getIntent().getIntExtra("MAX_TEAM_SIZE", 4);

        if ("Pair".equals(participationType)) {
            minTeamSize = 2;
            maxTeamSize = 2;
        }

        initViews();
        loadLeaderData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        tvEventName = findViewById(R.id.tvEventName);
        tvEventInfo = findViewById(R.id.tvEventInfo);
        tvTeamSizeInfo = findViewById(R.id.tvTeamSizeInfo);
        tvTeamNameLabel = findViewById(R.id.tvTeamNameLabel);
        tvLeaderInfo = findViewById(R.id.tvLeaderInfo);
        tvMemberCount = findViewById(R.id.tvMemberCount);
        etTeamName = findViewById(R.id.etTeamName);
        etTeammateEmail = findViewById(R.id.etTeammateEmail);
        btnAddTeammate = findViewById(R.id.btnAddTeammate);
        btnRegisterTeam = findViewById(R.id.btnRegisterTeam);
        layoutTeamMembers = findViewById(R.id.layoutTeamMembers);

        if ("Pair".equals(participationType)) {
            tvScreenTitle.setText("Pair Registration");
            tvTeamNameLabel.setVisibility(View.GONE);
            etTeamName.setVisibility(View.GONE);
            btnRegisterTeam.setText("Register Pair →");
        }

        tvEventName.setText(eventName != null ? eventName : "Event");
        tvEventInfo.setText("📅 " + (eventDate != null ? eventDate : "TBA") +
                " • 📍 " + (eventVenue != null ? eventVenue : "Campus"));
        tvTeamSizeInfo.setText("👥 Team size: " + minTeamSize + "-" + maxTeamSize + " members (including you)");

        btnBack.setOnClickListener(v -> finish());
        btnAddTeammate.setOnClickListener(v -> verifyAndAddTeammate());
        btnRegisterTeam.setOnClickListener(v -> submitRegistration());

        updateMemberCount();
    }

    private void loadLeaderData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        leaderId = user.getUid();
        leaderEmail = user.getEmail() != null ? user.getEmail() : "";

        db.collection("users").document(leaderId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        leaderName = doc.getString("name") != null ? doc.getString("name") : "You";
                        leaderDepartment = doc.getString("department") != null ? doc.getString("department") : "";
                        tvLeaderInfo.setText("👤 " + leaderName + " • " + leaderEmail +
                                (leaderDepartment.isEmpty() ? "" : " • " + leaderDepartment));
                    } else {
                        leaderName = "You";
                        tvLeaderInfo.setText("👤 " + leaderEmail);
                    }
                });
    }

    private void verifyAndAddTeammate() {
        String email = etTeammateEmail.getText().toString().trim().toLowerCase();

        if (TextUtils.isEmpty(email)) {
            etTeammateEmail.setError("Enter teammate's email");
            return;
        }

        // Check: not self
        if (email.equalsIgnoreCase(leaderEmail)) {
            Toast.makeText(this, "You're already the team leader!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check: not already added
        if (memberEmails.contains(email)) {
            Toast.makeText(this, "This teammate is already added.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check: team not full
        int totalMembers = 1 + memberIds.size(); // leader + existing teammates
        if (totalMembers >= maxTeamSize) {
            Toast.makeText(this, "Team is full! Maximum " + maxTeamSize + " members allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAddTeammate.setEnabled(false);
        btnAddTeammate.setText("Verifying...");

        // Verify the user exists in Firestore
        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("role", "STUDENT")
                .get()
                .addOnSuccessListener(snapshots -> {
                    btnAddTeammate.setEnabled(true);
                    btnAddTeammate.setText("Verify & Add");

                    if (snapshots.isEmpty()) {
                        Toast.makeText(this, "No student found with this email. They must sign up first.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    QueryDocumentSnapshot userDoc = null;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        userDoc = doc;
                        break;
                    }

                    String mateId = userDoc.getId();
                    String mateName = userDoc.getString("name") != null ? userDoc.getString("name") : email;
                    String mateDept = userDoc.getString("department") != null ? userDoc.getString("department") : "";

                    // Check: not already registered for this event
                    checkDuplicateRegistration(mateId, mateName, email, mateDept);
                })
                .addOnFailureListener(e -> {
                    btnAddTeammate.setEnabled(true);
                    btnAddTeammate.setText("Verify & Add");
                    android.util.Log.e("TeamRegistrationActivity", "Teammate lookup failed: " + e.getMessage(), e);
                    Toast.makeText(this, "Error verifying: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkDuplicateRegistration(String mateId, String mateName, String email, String dept) {
        // Check if this student is already registered for this event (in any team or individually)
        db.collection("participations")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("studentId", mateId)
                .get()
                .addOnSuccessListener(existing -> {
                    if (!existing.isEmpty()) {
                        Toast.makeText(this, mateName + " is already registered for this event!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // All clear — add teammate
                    memberIds.add(mateId);
                    memberNames.add(mateName);
                    memberEmails.add(email);
                    memberDepartments.add(dept);

                    addTeammateCard(mateName, email, dept, memberIds.size() - 1);
                    etTeammateEmail.setText("");
                    updateMemberCount();

                    Toast.makeText(this, "✓ " + mateName + " verified and added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking duplicate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addTeammateCard(String name, String email, String dept, int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundColor(Color.parseColor("#171A27"));
        card.setPadding(36, 28, 36, 28);
        card.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 16, 0, 0);
        card.setLayoutParams(lp);

        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tv.setText("👤 " + name + "\n✉️ " + email + (dept.isEmpty() ? "" : " • " + dept));
        tv.setTextColor(Color.parseColor("#CBD5E1"));
        tv.setTextSize(12f);

        Button btnRemove = new Button(this);
        btnRemove.setText("✕");
        btnRemove.setTextColor(Color.parseColor("#EF4444"));
        btnRemove.setBackgroundColor(Color.TRANSPARENT);
        btnRemove.setTextSize(16f);
        btnRemove.setOnClickListener(v -> {
            int idx = layoutTeamMembers.indexOfChild(card);
            if (idx >= 0 && idx < memberIds.size()) {
                memberIds.remove(idx);
                memberNames.remove(idx);
                memberEmails.remove(idx);
                memberDepartments.remove(idx);
                layoutTeamMembers.removeView(card);
                updateMemberCount();
            }
        });

        card.addView(tv);
        card.addView(btnRemove);
        layoutTeamMembers.addView(card);
    }

    private void updateMemberCount() {
        int total = 1 + memberIds.size();
        tvMemberCount.setText("Members: " + total + "/" + maxTeamSize + " (including you)");

        if (total >= minTeamSize && total <= maxTeamSize) {
            tvMemberCount.setTextColor(Color.parseColor("#10B981"));
        } else {
            tvMemberCount.setTextColor(Color.parseColor("#F59E0B"));
        }
    }

    private void submitRegistration() {
        int totalMembers = 1 + memberIds.size();

        // Validate team name
        String teamName = etTeamName.getText().toString().trim();
        if ("Team".equals(participationType) && TextUtils.isEmpty(teamName)) {
            etTeamName.setError("Team name is required");
            etTeamName.requestFocus();
            return;
        }

        if ("Pair".equals(participationType)) {
            if (TextUtils.isEmpty(teamName)) {
                teamName = leaderName + " & " + (memberNames.isEmpty() ? "Partner" : memberNames.get(0));
            }
        }

        // Validate team size
        if (totalMembers < minTeamSize) {
            Toast.makeText(this, "You need at least " + minTeamSize + " members. Add " +
                    (minTeamSize - totalMembers) + " more.", Toast.LENGTH_LONG).show();
            return;
        }

        if (totalMembers > maxTeamSize) {
            Toast.makeText(this, "Maximum " + maxTeamSize + " members allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegisterTeam.setEnabled(false);
        btnRegisterTeam.setText("Checking event status...");

        // Build full member lists (leader + teammates)
        List<String> allMemberIds = new ArrayList<>();
        List<String> allMemberNames = new ArrayList<>();
        List<String> allMemberEmails = new ArrayList<>();
        List<String> allMemberDepts = new ArrayList<>();

        allMemberIds.add(leaderId);
        allMemberNames.add(leaderName);
        allMemberEmails.add(leaderEmail);
        allMemberDepts.add(leaderDepartment);

        allMemberIds.addAll(memberIds);
        allMemberNames.addAll(memberNames);
        allMemberEmails.addAll(memberEmails);
        allMemberDepts.addAll(memberDepartments);

        String finalTeamName = teamName;

        // 1. Verify Event is approved and deadline open in Firestore
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        resetSubmitButton();
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String status = eventDoc.getString("status");
                    if (!"approved".equalsIgnoreCase(status)) {
                        resetSubmitButton();
                        Toast.makeText(this, "Registration failed: This event is no longer active for registration.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String deadline = eventDoc.getString("registrationDeadline");
                    if (!isRegistrationOpen(deadline)) {
                        resetSubmitButton();
                        Toast.makeText(this, "Registration Closed: The deadline for this event has passed.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // 2. Verify no member is already registered for this event
                    verifyAllMembersNotRegistered(finalTeamName, allMemberIds, allMemberNames, allMemberEmails, allMemberDepts);
                })
                .addOnFailureListener(e -> {
                    resetSubmitButton();
                    Toast.makeText(this, "Failed to verify event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void verifyAllMembersNotRegistered(String teamName, List<String> allIds, List<String> allNames,
                                               List<String> allEmails, List<String> allDepts) {
        db.collection("participations")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String studentId = doc.getString("studentId");
                        if (studentId != null && allIds.contains(studentId)) {
                            int idx = allIds.indexOf(studentId);
                            String existingName = idx >= 0 ? allNames.get(idx) : "A team member";
                            resetSubmitButton();
                            Toast.makeText(this, existingName + " is already registered for this event!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    // All clear — perform registration
                    performRegistration(teamName, allIds, allNames, allEmails, allDepts);
                })
                .addOnFailureListener(e -> {
                    resetSubmitButton();
                    Toast.makeText(this, "Error checking duplicate registration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetSubmitButton() {
        btnRegisterTeam.setEnabled(true);
        if ("Pair".equals(participationType)) {
            btnRegisterTeam.setText("Register Pair →");
        } else {
            btnRegisterTeam.setText("Register Team →");
        }
    }

    private boolean isRegistrationOpen(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) return true;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date deadlineDate = sdf.parse(deadline);
            java.util.Date now = new java.util.Date();
            return deadlineDate == null || !now.after(deadlineDate);
        } catch (Exception e) {
            return true;
        }
    }

    private void performRegistration(String teamName, List<String> allIds, List<String> allNames,
                                     List<String> allEmails, List<String> allDepts) {
        btnRegisterTeam.setText("Registering Team...");
        String registrationId = "reg_" + System.currentTimeMillis() + "_" + leaderId.substring(0, 5);
        String teamId = "team_" + System.currentTimeMillis() + "_" + leaderId.substring(0, 5);

        WriteBatch batch = db.batch();

        // 1. Create team document
        Map<String, Object> teamData = new HashMap<>();
        teamData.put("teamId", teamId);
        teamData.put("teamName", teamName);
        teamData.put("eventId", eventId);
        teamData.put("eventName", eventName);
        teamData.put("leaderId", leaderId);
        teamData.put("leaderName", leaderName);
        teamData.put("memberIds", allIds);
        teamData.put("memberNames", allNames);
        teamData.put("memberEmails", allEmails);
        teamData.put("memberCount", allIds.size());
        teamData.put("status", "active");
        teamData.put("createdAt", FieldValue.serverTimestamp());
        batch.set(db.collection("teams").document(teamId), teamData);

        // 2. Create registration document
        Map<String, Object> regData = new HashMap<>();
        regData.put("registrationId", registrationId);
        regData.put("eventId", eventId);
        regData.put("eventName", eventName);
        regData.put("eventDate", eventDate);
        regData.put("eventVenue", eventVenue);
        regData.put("coordinatorId", coordinatorId != null ? coordinatorId : "");
        regData.put("studentId", leaderId);
        regData.put("studentName", leaderName);
        regData.put("studentEmail", leaderEmail);
        regData.put("participationType", participationType);
        regData.put("teamId", teamId);
        regData.put("teamName", teamName);
        regData.put("memberIds", allIds);
        regData.put("memberNames", allNames);
        regData.put("memberCount", allIds.size());
        regData.put("status", "registered");
        regData.put("registeredAt", FieldValue.serverTimestamp());
        batch.set(db.collection("registrations").document(registrationId), regData);

        // 3. Create participation record for EACH member
        for (int i = 0; i < allIds.size(); i++) {
            String partId = "part_" + System.currentTimeMillis() + "_" + allIds.get(i).substring(0, 5) + "_" + i;
            Map<String, Object> partData = new HashMap<>();
            partData.put("participationId", partId);
            partData.put("studentId", allIds.get(i));
            partData.put("studentName", allNames.get(i));
            partData.put("studentEmail", allEmails.get(i));
            partData.put("department", allDepts.get(i));
            partData.put("eventId", eventId);
            partData.put("eventName", eventName);
            partData.put("eventDate", eventDate);
            partData.put("eventVenue", eventVenue);
            partData.put("registrationId", registrationId);
            partData.put("teamId", teamId);
            partData.put("teamName", teamName);
            partData.put("participationType", participationType);
            partData.put("attendance", "pending");
            partData.put("result", "none");
            partData.put("status", "registered");
            partData.put("createdAt", FieldValue.serverTimestamp());
            batch.set(db.collection("participations").document(partId), partData);
        }

        // 4. Commit all at once
        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "✓ " + participationType + " registered successfully!",
                            Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    resetSubmitButton();
                    android.util.Log.e("TeamRegistrationActivity", "Team registration WriteBatch failed: " + e.getMessage(), e);
                    Toast.makeText(this, "Registration failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
