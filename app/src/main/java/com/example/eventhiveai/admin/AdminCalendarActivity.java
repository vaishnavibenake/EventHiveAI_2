package com.example.eventhiveai.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminCalendarActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private CalendarView calendarView;
    private RecyclerView rvCalendarEvents;
    private TextView tvSelectedDate, tvEmptyState;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private EventAdapter eventAdapter;
    private List<EventModel> allActiveEvents = new ArrayList<>();
    private List<EventModel> selectedDateEvents = new ArrayList<>();
    private String selectedDateStandard = "";

    private final SimpleDateFormat stdSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_calendar);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        calendarView = findViewById(R.id.calendarView);
        rvCalendarEvents = findViewById(R.id.rvCalendarEvents);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvCalendarEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(this, selectedDateEvents, null);
        rvCalendarEvents.setAdapter(eventAdapter);

        Calendar calendar = Calendar.getInstance();
        selectedDateStandard = stdSdf.format(calendar.getTime());
        if (tvSelectedDate != null) {
            tvSelectedDate.setText("Events for: " + selectedDateStandard);
        }

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDateStandard = stdSdf.format(cal.getTime());

            if (tvSelectedDate != null) {
                tvSelectedDate.setText("Events for: " + selectedDateStandard);
            }
            filterEventsForDate(selectedDateStandard);
        });

        loadAllApprovedAndActiveEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllApprovedAndActiveEvents();
    }

    private void loadAllApprovedAndActiveEvents() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        allActiveEvents.clear();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            EventModel event = doc.toObject(EventModel.class);
                            if (event == null) event = new EventModel();
                            event.setEventId(doc.getId());

                            String status = doc.getString("status");
                            if (status != null) {
                                status = status.trim().toLowerCase();
                                event.setStatus(status);
                                // Include approved, active, and completed events on the calendar
                                if ("approved".equals(status) || "active".equals(status) || "completed".equals(status)) {
                                    allActiveEvents.add(event);
                                }
                            }
                        }
                        filterEventsForDate(selectedDateStandard);
                    } else {
                        Toast.makeText(this, "Failed to load calendar events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterEventsForDate(String targetDateStd) {
        selectedDateEvents.clear();

        for (EventModel event : allActiveEvents) {
            String rawDate = event.getDate();
            if (rawDate == null || rawDate.trim().isEmpty()) continue;

            String normalized = normalizeDate(rawDate.trim());
            if (targetDateStd.equals(normalized)) {
                selectedDateEvents.add(event);
            }
        }

        eventAdapter.notifyDataSetChanged();

        if (selectedDateEvents.isEmpty() && tvEmptyState != null) {
            tvEmptyState.setText("No approved or active events scheduled on " + targetDateStd);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    /**
     * Normalizes various date formats (e.g. yyyy-MM-dd, yyyy-M-d, dd-MM-yyyy) into standard yyyy-MM-dd
     */
    private String normalizeDate(String input) {
        String[] possibleFormats = {"yyyy-MM-dd", "yyyy-M-d", "dd-MM-yyyy", "d-M-yyyy", "yyyy/MM/dd", "dd/MM/yyyy"};
        for (String fmt : possibleFormats) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(fmt, Locale.getDefault());
                parser.setLenient(false);
                Date parsed = parser.parse(input);
                if (parsed != null) {
                    return stdSdf.format(parsed);
                }
            } catch (ParseException ignored) {}
        }
        return input;
    }
}