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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadWinnersActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Spinner spinnerEvent;
    private TextView tvParticipationTypeInfo;

    private EditText etFirstName, etFirstDept, etFirstTeamName;
    private EditText etSecondName, etSecondDept, etSecondTeamName;
    private EditText etThirdName, etThirdDept, etThirdTeamName;
    private LinearLayout layoutFirstTeam, layoutSecondTeam, layoutThirdTeam;

    private Button btnPublishWinners;
    private FirebaseFirestore db;

    private String preselectedEventId;
    private String preselectedEventName;
    private String existingWinnerId; // for edit mode

    private final List<String> eventIds = new ArrayList<>();
    private final List<String> eventNames = new ArrayList<>();
    private final List<String> eventParticipationTypes = new ArrayList<>();

    private String currentParticipationType = "Individual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_winners);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnPublishWinners = findViewById(R.id.btnPublishWinners);

        // Event selector — use the existing etWinnerEvent as fallback label
        spinnerEvent = findViewById(R.id.spinnerCompletedEvents);
        tvParticipationTypeInfo = findViewById(R.id.tvParticipationTypeInfo);

        etFirstName = findViewById(R.id.etFirstPlace);
        etFirstDept = findViewById(R.id.etFirstDept);
        etFirstTeamName = findViewById(R.id.etFirstTeamName);
        layoutFirstTeam = findViewById(R.id.layoutFirstTeam);

        etSecondName = findViewById(R.id.etSecondPlace);
        etSecondDept = findViewById(R.id.etSecondDept);
        etSecondTeamName = findViewById(R.id.etSecondTeamName);
        layoutSecondTeam = findViewById(R.id.layoutSecondTeam);

        etThirdName = findViewById(R.id.etThirdPlace);
        etThirdDept = findViewById(R.id.etThirdDept);
        etThirdTeamName = findViewById(R.id.etThirdTeamName);
        layoutThirdTeam = findViewById(R.id.layoutThirdTeam);

        preselectedEventId = getIntent().getStringExtra("EVENT_ID");
        preselectedEventName = getIntent().getStringExtra("EVENT_NAME");

        btnBack.setOnClickListener(v -> finish());
        btnPublishWinners.setOnClickListener(v -> publishWinners());

        if (spinnerEvent != null) {
            spinnerEvent.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                    if (pos < eventParticipationTypes.size()) {
                        currentParticipationType = eventParticipationTypes.get(pos);
                        updateTeamFieldVisibility();
                        checkExistingWinners(eventIds.get(pos));
                    }
                }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }

        loadCompletedEvents();
    }

    private void loadCompletedEvents() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("events")
                .whereEqualTo("coordinatorId", uid)
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(snap -> {
                    eventIds.clear();
                    eventNames.clear();
                    eventParticipationTypes.clear();

                    for (QueryDocumentSnapshot doc : snap) {
                        eventIds.add(doc.getId());
                        String name = doc.getString("eventName");
                        eventNames.add(name != null ? name : doc.getId());
                        String pt = doc.getString("participationType");
                        eventParticipationTypes.add(pt != null ? pt : "Individual");
                    }

                    if (preselectedEventId != null && !preselectedEventId.isEmpty()) {
                        if (!eventIds.contains(preselectedEventId)) {
                            eventIds.add(0, preselectedEventId);
                            eventNames.add(0, preselectedEventName != null ? preselectedEventName : preselectedEventId);
                            eventParticipationTypes.add(0, "Individual");
                        }
                    }

                    if (spinnerEvent != null && !eventNames.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, eventNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerEvent.setAdapter(adapter);

                        if (preselectedEventId != null) {
                            int idx = eventIds.indexOf(preselectedEventId);
                            if (idx >= 0) spinnerEvent.setSelection(idx);
                        }
                    }

                    if (!eventParticipationTypes.isEmpty()) {
                        currentParticipationType = eventParticipationTypes.get(0);
                        updateTeamFieldVisibility();
                    }
                });
    }

    private void updateTeamFieldVisibility() {
        boolean isTeam = "Team".equals(currentParticipationType) || "Pair".equals(currentParticipationType);

        if (layoutFirstTeam != null) layoutFirstTeam.setVisibility(isTeam ? View.VISIBLE : View.GONE);
        if (layoutSecondTeam != null) layoutSecondTeam.setVisibility(isTeam ? View.VISIBLE : View.GONE);
        if (layoutThirdTeam != null) layoutThirdTeam.setVisibility(isTeam ? View.VISIBLE : View.GONE);

        if (tvParticipationTypeInfo != null) {
            tvParticipationTypeInfo.setText("Type: " + currentParticipationType);
            tvParticipationTypeInfo.setVisibility(View.VISIBLE);
        }
    }

    private void checkExistingWinners(String eventId) {
        db.collection("winners")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        DocumentSnapshot doc = snap.getDocuments().get(0);
                        existingWinnerId = doc.getId();

                        // Populate fields for editing
                        String f = doc.getString("firstPlaceName");
                        String s = doc.getString("secondPlaceName");
                        String t = doc.getString("thirdPlaceName");
                        if (f != null) etFirstName.setText(f);
                        if (s != null) etSecondName.setText(s);
                        if (t != null) etThirdName.setText(t);

                        if (etFirstDept != null) {
                            String fd = doc.getString("firstPlaceDepartment");
                            if (fd != null) etFirstDept.setText(fd);
                        }
                        if (etSecondDept != null) {
                            String sd = doc.getString("secondPlaceDepartment");
                            if (sd != null) etSecondDept.setText(sd);
                        }
                        if (etThirdDept != null) {
                            String td = doc.getString("thirdPlaceDepartment");
                            if (td != null) etThirdDept.setText(td);
                        }
                        if (etFirstTeamName != null) {
                            String ft = doc.getString("firstPlaceTeamName");
                            if (ft != null) etFirstTeamName.setText(ft);
                        }
                        if (etSecondTeamName != null) {
                            String st = doc.getString("secondPlaceTeamName");
                            if (st != null) etSecondTeamName.setText(st);
                        }
                        if (etThirdTeamName != null) {
                            String tt = doc.getString("thirdPlaceTeamName");
                            if (tt != null) etThirdTeamName.setText(tt);
                        }

                        btnPublishWinners.setText("Update Winners");
                        Toast.makeText(this, "Winners already published. You can edit them.", Toast.LENGTH_SHORT).show();
                    } else {
                        existingWinnerId = null;
                        etFirstName.setText("");
                        etSecondName.setText("");
                        etThirdName.setText("");
                        if (etFirstDept != null) etFirstDept.setText("");
                        if (etSecondDept != null) etSecondDept.setText("");
                        if (etThirdDept != null) etThirdDept.setText("");
                        if (etFirstTeamName != null) etFirstTeamName.setText("");
                        if (etSecondTeamName != null) etSecondTeamName.setText("");
                        if (etThirdTeamName != null) etThirdTeamName.setText("");
                        btnPublishWinners.setText("Publish Winners");
                    }
                });
    }

    private void publishWinners() {
        int selectedIdx = spinnerEvent != null ? spinnerEvent.getSelectedItemPosition() : 0;
        if (selectedIdx < 0 || selectedIdx >= eventIds.size()) {
            Toast.makeText(this, "Please select an event.", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventId = eventIds.get(selectedIdx);
        String eventName = eventNames.get(selectedIdx);
        String first = etFirstName.getText().toString().trim();

        if (TextUtils.isEmpty(first)) {
            etFirstName.setError("1st place winner is required");
            etFirstName.requestFocus();
            return;
        }

        String second = etSecondName.getText().toString().trim();
        String third = etThirdName.getText().toString().trim();
        String firstDept = etFirstDept != null ? etFirstDept.getText().toString().trim() : "";
        String secondDept = etSecondDept != null ? etSecondDept.getText().toString().trim() : "";
        String thirdDept = etThirdDept != null ? etThirdDept.getText().toString().trim() : "";
        String firstTeam = etFirstTeamName != null ? etFirstTeamName.getText().toString().trim() : "";
        String secondTeam = etSecondTeamName != null ? etSecondTeamName.getText().toString().trim() : "";
        String thirdTeam = etThirdTeamName != null ? etThirdTeamName.getText().toString().trim() : "";

        btnPublishWinners.setEnabled(false);

        String winnerId = existingWinnerId != null ? existingWinnerId : "win_" + System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("winnerId", winnerId);
        data.put("eventId", eventId);
        data.put("eventName", eventName);
        data.put("participationType", currentParticipationType);
        data.put("coordinatorId", FirebaseAuth.getInstance().getUid());

        // Structured fields
        data.put("firstPlaceName", first);
        data.put("firstPlaceDepartment", firstDept);
        data.put("firstPlaceTeamName", firstTeam);
        data.put("secondPlaceName", second);
        data.put("secondPlaceDepartment", secondDept);
        data.put("secondPlaceTeamName", secondTeam);
        data.put("thirdPlaceName", third);
        data.put("thirdPlaceDepartment", thirdDept);
        data.put("thirdPlaceTeamName", thirdTeam);

        // Legacy flat fields for backward compat
        String firstDisplay = "🥇 " + first + (firstDept.isEmpty() ? "" : " (" + firstDept + ")")
                + (firstTeam.isEmpty() ? "" : " — " + firstTeam);
        String secondDisplay = second.isEmpty() ? "" : "🥈 " + second + (secondDept.isEmpty() ? "" : " (" + secondDept + ")")
                + (secondTeam.isEmpty() ? "" : " — " + secondTeam);
        String thirdDisplay = third.isEmpty() ? "" : "🥉 " + third + (thirdDept.isEmpty() ? "" : " (" + thirdDept + ")")
                + (thirdTeam.isEmpty() ? "" : " — " + thirdTeam);
        data.put("firstPlace", firstDisplay);
        data.put("secondPlace", secondDisplay);
        data.put("thirdPlace", thirdDisplay);

        data.put("publishedAt", FieldValue.serverTimestamp());

        boolean isUpdate = existingWinnerId != null;

        db.collection("winners").document(winnerId).set(data)
                .addOnSuccessListener(unused -> {
                    // Also store under event subcollection
                    db.collection("events").document(eventId)
                            .collection("winners").document(winnerId).set(data);

                    // Post notification
                    String notifId = "notif_" + System.currentTimeMillis();
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("notificationId", notifId);
                    notif.put("title", (isUpdate ? "🔄 Winners Updated: " : "🏆 Winners Announced: ") + eventName);
                    notif.put("message", "1st: " + first +
                            (second.isEmpty() ? "" : "\n2nd: " + second) +
                            (third.isEmpty() ? "" : "\n3rd: " + third));
                    notif.put("recipientRole", "ALL");
                    notif.put("coordinatorId", FirebaseAuth.getInstance().getUid());
                    notif.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("notifications").document(notifId).set(notif);

                    Toast.makeText(this,
                            isUpdate ? "Winners updated successfully!" : "Winners published and students notified!",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnPublishWinners.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
