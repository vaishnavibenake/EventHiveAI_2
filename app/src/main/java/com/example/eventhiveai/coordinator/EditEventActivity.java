package com.example.eventhiveai.coordinator;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
        com.example.eventhiveai.SettingsActivity.applySavedTheme(this);
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

        setupDatePicker(etEditDate);
        setupTimePicker(etEditStartTime);
        setupTimePicker(etEditEndTime);

        btnBack.setOnClickListener(v -> finish());
        btnSaveEvent.setOnClickListener(v -> saveEventChanges());

        loadEventData();
    }

    private void setupDatePicker(EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            String currentText = editText.getText().toString().trim();
            if (!TextUtils.isEmpty(currentText)) {
                try {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date d = displayFormat.parse(currentText);
                    if (d != null) calendar.setTime(d);
                } catch (ParseException ignored) {
                    try {
                        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date d = dbFormat.parse(currentText);
                        if (d != null) calendar.setTime(d);
                    } catch (ParseException ignored2) {}
                }
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    EditEventActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(selectedYear, selectedMonth, selectedDay);
                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        editText.setText(displayFormat.format(selectedCal.getTime()));
                        editText.setError(null);
                    },
                    year, month, day
            );
            dialog.show();
        });
    }

    private void setupTimePicker(EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            String currentText = editText.getText().toString().trim();
            if (!TextUtils.isEmpty(currentText)) {
                try {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    Date d = timeFormat.parse(currentText);
                    if (d != null) {
                        Calendar tempCal = Calendar.getInstance();
                        tempCal.setTime(d);
                        calendar.set(Calendar.HOUR_OF_DAY, tempCal.get(Calendar.HOUR_OF_DAY));
                        calendar.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));
                    }
                } catch (ParseException ignored) {}
            }

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog dialog = new TimePickerDialog(
                    EditEventActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(Calendar.HOUR_OF_DAY, selectedHour);
                        selectedCal.set(Calendar.MINUTE, selectedMinute);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        editText.setText(timeFormat.format(selectedCal.getTime()));
                        editText.setError(null);
                    },
                    hour, minute, false
            );
            dialog.show();
        });
    }

    private String convertDbToDisplayDate(String dbDate) {
        if (TextUtils.isEmpty(dbDate)) return "";
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date d = dbFormat.parse(dbDate);
            return d != null ? displayFormat.format(d) : dbDate;
        } catch (ParseException e) {
            return dbDate;
        }
    }

    private String convertDisplayToDbDate(String displayDate) {
        if (TextUtils.isEmpty(displayDate)) return "";
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = displayFormat.parse(displayDate);
            return d != null ? dbFormat.format(d) : displayDate;
        } catch (ParseException e) {
            return displayDate;
        }
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

                        String rawDate = doc.getString("date");
                        etEditDate.setText(convertDbToDisplayDate(rawDate));

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
        String dateDisplay = etEditDate.getText().toString().trim();
        String start = etEditStartTime.getText().toString().trim();
        String end = etEditEndTime.getText().toString().trim();
        String venue = etEditVenue.getText().toString().trim();
        String maxStr = etEditMax.getText().toString().trim();

        // ── Required fields ──────────────────────────────────────────────────
        if (TextUtils.isEmpty(name)) {
            etEditEventName.setError("Event Name is required");
            etEditEventName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(dateDisplay)) {
            etEditDate.setError("Event Date is required");
            etEditDate.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(venue)) {
            etEditVenue.setError("Venue is required");
            etEditVenue.requestFocus();
            return;
        }

        // ── End time must be after start time ────────────────────────────────
        if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) {
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date startParsed = timeFormat.parse(start);
                Date endParsed   = timeFormat.parse(end);
                if (startParsed != null && endParsed != null && !endParsed.after(startParsed)) {
                    etEditEndTime.setError("End time must be after start time");
                    etEditEndTime.requestFocus();
                    return;
                }
            } catch (ParseException ignored) {
                // times were set by the picker — allow through
            }
        }

        // ── Max participants range check ──────────────────────────────────────
        int max = 100;
        if (!TextUtils.isEmpty(maxStr)) {
            try {
                max = Integer.parseInt(maxStr);
                if (max < 1) {
                    etEditMax.setError("Must be at least 1");
                    etEditMax.requestFocus();
                    return;
                }
                if (max > 10000) {
                    etEditMax.setError("Value seems too large (max 10 000)");
                    etEditMax.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etEditMax.setError("Enter a valid number");
                etEditMax.requestFocus();
                return;
            }
        }

        btnSaveEvent.setEnabled(false);
        btnSaveEvent.setText("Updating Event...");

        String dateDb = convertDisplayToDbDate(dateDisplay);

        Map<String, Object> updates = new HashMap<>();
        updates.put("eventName", name);
        updates.put("description", desc);
        updates.put("date", dateDb);
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
