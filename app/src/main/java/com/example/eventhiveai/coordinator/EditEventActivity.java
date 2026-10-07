package com.example.eventhiveai.coordinator;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.example.eventhiveai.admin.EventModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etEditEventName;
    private EditText etEditDescription;
    private EditText etEditDate;
    private EditText etEditStartTime;
    private EditText etEditEndTime;
    private EditText etEditVenue;
    private EditText etEditMax;
    private Button btnSaveEvent;

    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("EVENT_ID");

        btnBack = findViewById(R.id.btnBack);
        etEditEventName = findViewById(R.id.etEditEventName);
        etEditDescription = findViewById(R.id.etEditDescription);
        etEditDate = findViewById(R.id.etEditDate);
        etEditStartTime = findViewById(R.id.etEditStartTime);
        etEditEndTime = findViewById(R.id.etEditEndTime);
        etEditVenue = findViewById(R.id.etEditVenue);
        etEditMax = findViewById(R.id.etEditMax);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);

        btnBack.setOnClickListener(v -> finish());
        btnSaveEvent.setOnClickListener(v -> saveEventChanges());

        loadEventData();
    }

    private void loadEventData() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event not specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etEditEventName.setText(doc.getString("eventName"));
                        etEditDescription.setText(doc.getString("description"));
                        etEditDate.setText(doc.getString("date"));
                        etEditStartTime.setText(doc.getString("startTime"));
                        etEditEndTime.setText(doc.getString("endTime"));
                        etEditVenue.setText(doc.getString("venue"));

                        Long max = doc.getLong("maxParticipants");
                        etEditMax.setText(String.valueOf(max != null ? max : 100));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditEventActivity.this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveEventChanges() {
        String name = etEditEventName.getText().toString().trim();
        String desc = etEditDescription.getText().toString().trim();
        String date = etEditDate.getText().toString().trim();
        String start = etEditStartTime.getText().toString().trim();
        String end = etEditEndTime.getText().toString().trim();
        String venue = etEditVenue.getText().toString().trim();
        String maxStr = etEditMax.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(date) || TextUtils.isEmpty(venue)) {
            Toast.makeText(this, "Please fill in all required fields (Name, Date, Venue)", Toast.LENGTH_SHORT).show();
            return;
        }

        int max = 100;
        if (!TextUtils.isEmpty(maxStr)) {
            try {
                max = Integer.parseInt(maxStr);
            } catch (NumberFormatException ignored) {}
        }

        btnSaveEvent.setEnabled(false);
        btnSaveEvent.setText("Updating Event...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("eventName", name);
        updates.put("description", desc);
        updates.put("date", date);
        updates.put("startTime", start);
        updates.put("endTime", end);
        updates.put("venue", venue);
        updates.put("maxParticipants", max);
        updates.put("coordinatorId", com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Crucial requirement: Reset status back to pending so Admin can approve again!
        updates.put("status", "pending");
        updates.put("rejectionReason", "");

        db.collection("events").document(eventId).update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EditEventActivity.this, "Event updated! Status reset to pending for Admin approval.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveEvent.setEnabled(true);
                    btnSaveEvent.setText("Save & Submit For Re-Approval");
                    Toast.makeText(EditEventActivity.this, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
