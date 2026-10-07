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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdminCalendarActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private CalendarView calendarView;
    private TextView tvSelectedDateTitle;
    private RecyclerView recyclerCalendarEvents;
    private ProgressBar progressBar;
    private TextView tvEmptyCalendar;

    private ArrayList<EventModel> dateEvents;
    private EventAdapter eventAdapter;
    private FirebaseFirestore db;

    private String selectedDateStr = "";
    private String userRole = "ADMIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_calendar);

        db = FirebaseFirestore.getInstance();

        String roleExtra = getIntent().getStringExtra("USER_ROLE");
        if (roleExtra != null) userRole = roleExtra;

        btnBack = findViewById(R.id.btnBack);
        calendarView = findViewById(R.id.calendarView);
        tvSelectedDateTitle = findViewById(R.id.tvSelectedDateTitle);
        recyclerCalendarEvents = findViewById(R.id.recyclerCalendarEvents);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyCalendar = findViewById(R.id.tvEmptyCalendar);

        recyclerCalendarEvents.setLayoutManager(new LinearLayoutManager(this));
        dateEvents = new ArrayList<>();
        eventAdapter = new EventAdapter(dateEvents, userRole, false, null);
        recyclerCalendarEvents.setAdapter(eventAdapter);

        btnBack.setOnClickListener(v -> finish());

        // Default to today
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDateStr = sdf.format(today.getTime());
        tvSelectedDateTitle.setText("Events on: " + selectedDateStr);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Month is 0-indexed
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDateStr = sdf.format(cal.getTime());
            tvSelectedDateTitle.setText("Events on: " + selectedDateStr);
            loadEventsForDate(selectedDateStr);
        });

        loadEventsForDate(selectedDateStr);
    }

    private void loadEventsForDate(String dateStr) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyCalendar.setVisibility(View.GONE);

        db.collection("events")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    dateEvents.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String eventDate = doc.getString("date");

                        // Check if event date matches
                        if (eventDate != null && isMatchingDate(eventDate, dateStr)) {
                            EventModel event = doc.toObject(EventModel.class);
                            if (event == null) event = new EventModel();
                            event.setEventId(doc.getId());
                            if (doc.contains("eventName")) event.setEventName(doc.getString("eventName"));
                            if (doc.contains("clubName")) event.setClubName(doc.getString("clubName"));
                            if (doc.contains("date")) event.setDate(doc.getString("date"));
                            if (doc.contains("venue")) event.setVenue(doc.getString("venue"));
                            if (doc.contains("status")) event.setStatus(doc.getString("status"));

                            dateEvents.add(event);
                        }
                    }

                    eventAdapter.notifyDataSetChanged();
                    if (dateEvents.isEmpty()) {
                        tvEmptyCalendar.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminCalendarActivity.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isMatchingDate(String eventDate, String targetDate) {
        if (eventDate.equalsIgnoreCase(targetDate)) return true;
        // Also support DD-MM-YYYY vs YYYY-MM-DD
        String cleanEvent = eventDate.replace("/", "-").trim();
        String cleanTarget = targetDate.replace("/", "-").trim();
        return cleanEvent.equals(cleanTarget);
    }
}
