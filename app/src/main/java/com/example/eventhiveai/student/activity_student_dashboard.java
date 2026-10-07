package com.example.eventhiveai.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.EventDetailsActivity;
import com.example.eventhiveai.NotificationsActivity;
import com.example.eventhiveai.R;
import com.example.eventhiveai.SettingsActivity;
import com.example.eventhiveai.admin.AdminCalendarActivity;
import com.example.eventhiveai.admin.EventModel;
import com.example.eventhiveai.admin.ManageClubsActivity;
import com.example.eventhiveai.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class activity_student_dashboard extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private TextView studentNameTextView;
    private View notificationButton;
    private EditText searchEditText;

    private LinearLayout recommendedEventCard;
    private TextView recommendedEventTitle;
    private TextView recommendedEventInfo;

    private LinearLayout eventCard;
    private TextView eventTitle;
    private TextView eventDetails;

    private View eventsNavButton;
    private View notificationsNavButton;
    private View profileNavButton;

    private UserModel currentStudent;
    private final List<EventModel> approvedEvents = new ArrayList<>();
    private EventModel topRecommendedEvent;
    private EventModel topUpcomingEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        studentNameTextView = findViewById(R.id.studentNameTextView);
        notificationButton = findViewById(R.id.notificationButton);
        searchEditText = findViewById(R.id.searchEditText);

        recommendedEventCard = findViewById(R.id.recommendedEventCard);
        recommendedEventTitle = findViewById(R.id.recommendedEventTitle);
        recommendedEventInfo = findViewById(R.id.recommendedEventInfo);

        eventCard = findViewById(R.id.eventCard);
        eventTitle = findViewById(R.id.eventTitle);
        eventDetails = findViewById(R.id.eventDetails);

        eventsNavButton = findViewById(R.id.eventsNavButton);
        notificationsNavButton = findViewById(R.id.notificationsNavButton);
        profileNavButton = findViewById(R.id.profileNavButton);

        setupClickListeners();
        loadStudentData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApprovedEvents();
    }

    private void setupClickListeners() {
        if (notificationButton != null) {
            notificationButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, NotificationsActivity.class);
                intent.putExtra("USER_ROLE", "STUDENT");
                startActivity(intent);
            });
        }

        if (notificationsNavButton != null) {
            notificationsNavButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, NotificationsActivity.class);
                intent.putExtra("USER_ROLE", "STUDENT");
                startActivity(intent);
            });
        }

        if (eventsNavButton != null) {
            eventsNavButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminCalendarActivity.class);
                intent.putExtra("USER_ROLE", "STUDENT");
                startActivity(intent);
            });
        }

        if (profileNavButton != null) {
            profileNavButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("USER_ROLE", "STUDENT");
                startActivity(intent);
            });
        }

        if (recommendedEventCard != null) {
            recommendedEventCard.setOnClickListener(v -> {
                if (topRecommendedEvent != null) {
                    Intent intent = new Intent(this, EventDetailsActivity.class);
                    intent.putExtra("EVENT_ID", topRecommendedEvent.getEventId());
                    intent.putExtra("USER_ROLE", "STUDENT");
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No recommended event selected.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (eventCard != null) {
            eventCard.setOnClickListener(v -> {
                if (topUpcomingEvent != null) {
                    Intent intent = new Intent(this, EventDetailsActivity.class);
                    intent.putExtra("EVENT_ID", topUpcomingEvent.getEventId());
                    intent.putExtra("USER_ROLE", "STUDENT");
                    startActivity(intent);
                } else {
                    // Open My Registrations or Calendar
                    startActivity(new Intent(this, MyRegistrationsActivity.class));
                }
            });
        }

        View winnersGalleryCard = findViewById(R.id.winnersGalleryCard);
        if (winnersGalleryCard != null) {
            winnersGalleryCard.setOnClickListener(v -> {
                startActivity(new Intent(this, com.example.eventhiveai.coordinator.EventGalleryActivity.class));
            });
        }

        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEvents(s != null ? s.toString().trim() : "");
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void filterEvents(String query) {
        if (query.isEmpty()) {
            updateRecommendedAndUpcoming();
            return;
        }

        List<EventModel> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (EventModel e : approvedEvents) {
            if (e.getEventName().toLowerCase().contains(lowerQuery) ||
                e.getClubName().toLowerCase().contains(lowerQuery) ||
                e.getVenue().toLowerCase().contains(lowerQuery) ||
                e.getCategory().toLowerCase().contains(lowerQuery)) {
                filtered.add(e);
            }
        }

        if (!filtered.isEmpty()) {
            topUpcomingEvent = filtered.get(0);
            if (eventTitle != null) eventTitle.setText(topUpcomingEvent.getEventName());
            if (eventDetails != null) eventDetails.setText("Organized by " + topUpcomingEvent.getClubName() + " • 📍 " + topUpcomingEvent.getVenue());
        } else {
            if (eventTitle != null) eventTitle.setText("No matching events found");
            if (eventDetails != null) eventDetails.setText("Try searching for another keyword.");
        }
    }

    private void loadStudentData() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        firestore.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentStudent = documentSnapshot.toObject(UserModel.class);
                        if (currentStudent == null) currentStudent = new UserModel();
                        currentStudent.setUid(user.getUid());

                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            studentNameTextView.setText("Hello, " + name + " 👋");
                        } else {
                            studentNameTextView.setText("Hello, Student 👋");
                        }

                        loadApprovedEvents();
                    }
                })
                .addOnFailureListener(e -> {
                    studentNameTextView.setText("Hello, Student 👋");
                });
    }

    private void loadApprovedEvents() {
        firestore.collection("events")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    approvedEvents.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        EventModel event = doc.toObject(EventModel.class);
                        if (event == null) event = new EventModel();
                        event.setEventId(doc.getId());
                        if (doc.contains("eventName")) event.setEventName(doc.getString("eventName"));
                        if (doc.contains("clubName")) event.setClubName(doc.getString("clubName"));
                        if (doc.contains("date")) event.setDate(doc.getString("date"));
                        if (doc.contains("venue")) event.setVenue(doc.getString("venue"));
                        if (doc.contains("department")) event.setDepartment(doc.getString("department"));
                        if (doc.contains("category")) event.setCategory(doc.getString("category"));

                        approvedEvents.add(event);
                    }

                    Collections.sort(approvedEvents, Comparator.comparing(EventModel::getDate));

                    updateRecommendedAndUpcoming();
                })
                .addOnFailureListener(e -> {
                    // Ignored or handled gracefully
                });
    }

    private void updateRecommendedAndUpcoming() {
        if (approvedEvents.isEmpty()) {
            if (recommendedEventTitle != null) recommendedEventTitle.setText("No upcoming events yet");
            if (recommendedEventInfo != null) recommendedEventInfo.setText("Approved events will appear here once published.");
            if (eventTitle != null) eventTitle.setText("My Registered Events");
            if (eventDetails != null) eventDetails.setText("Tap to view your registration passes.");
            return;
        }

        // Use RecommendationService based on student department
        List<EventModel> recommendedList = RecommendationService.getRecommendedEvents(currentStudent, approvedEvents);
        if (!recommendedList.isEmpty()) {
            topRecommendedEvent = recommendedList.get(0);
            if (recommendedEventTitle != null) {
                recommendedEventTitle.setText(topRecommendedEvent.getEventName());
            }
            if (recommendedEventInfo != null) {
                recommendedEventInfo.setText("🏛️ " + topRecommendedEvent.getDepartment() + " • 📅 " + topRecommendedEvent.getDate() + " • 📍 " + topRecommendedEvent.getVenue());
            }
        }

        // Set Upcoming event
        topUpcomingEvent = approvedEvents.get(approvedEvents.size() - 1);
        if (eventTitle != null) {
            eventTitle.setText(topUpcomingEvent.getEventName());
        }
        if (eventDetails != null) {
            eventDetails.setText("Organized by " + topUpcomingEvent.getClubName() + " • 📍 " + topUpcomingEvent.getVenue());
        }
    }
}
