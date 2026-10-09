package com.example.eventhiveai.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.NotificationsActivity;
import com.example.eventhiveai.R;
import com.example.eventhiveai.SettingsActivity;
import com.example.eventhiveai.admin.AdminCalendarActivity;
import com.example.eventhiveai.admin.EventModel;
import com.example.eventhiveai.admin.ManageClubsActivity;
import com.example.eventhiveai.models.ParticipationModel;
import com.example.eventhiveai.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class activity_student_dashboard extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private DrawerLayout studentDrawerLayout;
    private ImageButton btnStudentMenu;
    private View notificationButton;

    private TextView tvGreetingHeader;
    private TextView studentNameTextView;
    private TextView tvStudentProfileDetails;

    // Stat Cards
    private TextView tvStatEventsJoined;
    private TextView tvStatUpcomingEvents;
    private TextView tvStatCertificates;
    private TextView tvStatClubsJoined;

    private View cardEventsJoined;
    private View cardUpcomingEvents;
    private View cardCertificates;
    private View cardClubsJoined;

    // Quick Action Shortcuts
    private View btnQuickPasses;
    private View btnQuickParticipation;
    private View btnQuickGallery;

    private EditText searchEditText;

    // Category Chips
    private TextView chipCatAll;
    private TextView chipCatTechnical;
    private TextView chipCatCultural;
    private TextView chipCatSports;
    private TextView chipCatWorkshops;
    private TextView[] categoryChips;

    // RecyclerViews & Adapters
    private RecyclerView recyclerRecommendedEvents;
    private TextView tvRecommendedEmpty;
    private RecyclerView recyclerUpcomingEvents;
    private TextView tvUpcomingEmpty;

    private StudentEventAdapter recommendedAdapter;
    private StudentEventAdapter upcomingAdapter;

    // Bottom Navigation
    private View eventsNavButton;
    private View notificationsNavButton;
    private View profileNavButton;

    // Side Drawer Destinations
    private View navDashboard;
    private View navCalendar;
    private View navClubs;
    private View navRegistrations;
    private View navCertificates;
    private View navParticipation;
    private View navNotifications;
    private View navSettings;

    // Data
    private UserModel currentStudent;
    private final ArrayList<EventModel> approvedEvents = new ArrayList<>();
    private final ArrayList<EventModel> recommendedList = new ArrayList<>();
    private final ArrayList<EventModel> filteredUpcomingList = new ArrayList<>();

    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerViews();
        setupClickListeners();
        loadStudentData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApprovedEvents();
        loadStudentLiveMetrics();
    }

    private void initializeViews() {
        studentDrawerLayout = findViewById(R.id.studentDrawerLayout);
        btnStudentMenu = findViewById(R.id.btnStudentMenu);
        notificationButton = findViewById(R.id.notificationButton);

        tvGreetingHeader = findViewById(R.id.tvGreetingHeader);
        studentNameTextView = findViewById(R.id.studentNameTextView);
        tvStudentProfileDetails = findViewById(R.id.tvStudentProfileDetails);

        tvStatEventsJoined = findViewById(R.id.tvStatEventsJoined);
        tvStatUpcomingEvents = findViewById(R.id.tvStatUpcomingEvents);
        tvStatCertificates = findViewById(R.id.tvStatCertificates);
        tvStatClubsJoined = findViewById(R.id.tvStatClubsJoined);

        cardEventsJoined = findViewById(R.id.cardEventsJoined);
        cardUpcomingEvents = findViewById(R.id.cardUpcomingEvents);
        cardCertificates = findViewById(R.id.cardCertificates);
        cardClubsJoined = findViewById(R.id.cardClubsJoined);

        btnQuickPasses = findViewById(R.id.btnQuickPasses);
        btnQuickParticipation = findViewById(R.id.btnQuickParticipation);
        btnQuickGallery = findViewById(R.id.btnQuickGallery);

        searchEditText = findViewById(R.id.searchEditText);

        chipCatAll = findViewById(R.id.chipCatAll);
        chipCatTechnical = findViewById(R.id.chipCatTechnical);
        chipCatCultural = findViewById(R.id.chipCatCultural);
        chipCatSports = findViewById(R.id.chipCatSports);
        chipCatWorkshops = findViewById(R.id.chipCatWorkshops);
        categoryChips = new TextView[]{chipCatAll, chipCatTechnical, chipCatCultural, chipCatSports, chipCatWorkshops};

        recyclerRecommendedEvents = findViewById(R.id.recyclerRecommendedEvents);
        tvRecommendedEmpty = findViewById(R.id.tvRecommendedEmpty);
        recyclerUpcomingEvents = findViewById(R.id.recyclerUpcomingEvents);
        tvUpcomingEmpty = findViewById(R.id.tvUpcomingEmpty);

        eventsNavButton = findViewById(R.id.eventsNavButton);
        notificationsNavButton = findViewById(R.id.notificationsNavButton);
        profileNavButton = findViewById(R.id.profileNavButton);

        navDashboard = findViewById(R.id.navDashboard);
        navCalendar = findViewById(R.id.navCalendar);
        navClubs = findViewById(R.id.navClubs);
        navRegistrations = findViewById(R.id.navRegistrations);
        navCertificates = findViewById(R.id.navCertificates);
        navParticipation = findViewById(R.id.navParticipation);
        navNotifications = findViewById(R.id.navNotifications);
        navSettings = findViewById(R.id.navSettings);
    }

    private void setupRecyclerViews() {
        recyclerRecommendedEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new StudentEventAdapter(recommendedList);
        recyclerRecommendedEvents.setAdapter(recommendedAdapter);

        recyclerUpcomingEvents.setLayoutManager(new LinearLayoutManager(this));
        upcomingAdapter = new StudentEventAdapter(filteredUpcomingList);
        recyclerUpcomingEvents.setAdapter(upcomingAdapter);
    }

    private void setupClickListeners() {
        // Drawer toggle
        if (btnStudentMenu != null && studentDrawerLayout != null) {
            btnStudentMenu.setOnClickListener(v -> {
                if (studentDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    studentDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    studentDrawerLayout.openDrawer(Gravity.LEFT);
                }
            });
        }

        // Overview Cards Click Listeners
        if (cardEventsJoined != null) {
            cardEventsJoined.setOnClickListener(v -> startActivity(new Intent(this, MyParticipationActivity.class)));
        }

        if (cardCertificates != null) {
            cardCertificates.setOnClickListener(v -> {
                Toast.makeText(this, "Showing verified participations & awards", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MyParticipationActivity.class));
            });
        }

        if (cardClubsJoined != null) {
            cardClubsJoined.setOnClickListener(v -> openClubsActivity());
        }

        // Quick Shortcuts
        if (btnQuickPasses != null) {
            btnQuickPasses.setOnClickListener(v -> startActivity(new Intent(this, MyRegistrationsActivity.class)));
        }

        if (btnQuickParticipation != null) {
            btnQuickParticipation.setOnClickListener(v -> startActivity(new Intent(this, MyParticipationActivity.class)));
        }

        if (btnQuickGallery != null) {
            btnQuickGallery.setOnClickListener(v -> startActivity(new Intent(this, com.example.eventhiveai.coordinator.EventGalleryActivity.class)));
        }

        // Notifications & Nav
        if (notificationButton != null) notificationButton.setOnClickListener(v -> openNotificationActivity());
        if (notificationsNavButton != null) notificationsNavButton.setOnClickListener(v -> openNotificationActivity());

        if (eventsNavButton != null) {
            eventsNavButton.setOnClickListener(v -> openCalendarActivity());
        }

        if (profileNavButton != null) {
            profileNavButton.setOnClickListener(v -> openSettingsActivity());
        }

        // Drawer 8 Destinations
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                if (studentDrawerLayout != null) studentDrawerLayout.closeDrawer(Gravity.LEFT);
            });
        }

        if (navCalendar != null) navCalendar.setOnClickListener(v -> { closeDrawer(); openCalendarActivity(); });
        if (navClubs != null) navClubs.setOnClickListener(v -> { closeDrawer(); openClubsActivity(); });
        if (navRegistrations != null) navRegistrations.setOnClickListener(v -> { closeDrawer(); startActivity(new Intent(this, MyRegistrationsActivity.class)); });
        if (navCertificates != null) navCertificates.setOnClickListener(v -> { closeDrawer(); startActivity(new Intent(this, MyParticipationActivity.class)); });
        if (navParticipation != null) navParticipation.setOnClickListener(v -> { closeDrawer(); startActivity(new Intent(this, MyParticipationActivity.class)); });
        if (navNotifications != null) navNotifications.setOnClickListener(v -> { closeDrawer(); openNotificationActivity(); });
        if (navSettings != null) navSettings.setOnClickListener(v -> { closeDrawer(); openSettingsActivity(); });

        // Category Chips
        chipCatAll.setOnClickListener(v -> selectCategoryChip("All", chipCatAll));
        chipCatTechnical.setOnClickListener(v -> selectCategoryChip("Technical", chipCatTechnical));
        chipCatCultural.setOnClickListener(v -> selectCategoryChip("Cultural", chipCatCultural));
        chipCatSports.setOnClickListener(v -> selectCategoryChip("Sports", chipCatSports));
        chipCatWorkshops.setOnClickListener(v -> selectCategoryChip("Workshops", chipCatWorkshops));

        // Live Search
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilterAndSearch(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void closeDrawer() {
        if (studentDrawerLayout != null && studentDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            studentDrawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    private void openCalendarActivity() {
        Intent intent = new Intent(this, AdminCalendarActivity.class);
        intent.putExtra("USER_ROLE", "STUDENT");
        startActivity(intent);
    }

    private void openClubsActivity() {
        Intent intent = new Intent(this, ManageClubsActivity.class);
        intent.putExtra("USER_ROLE", "STUDENT");
        startActivity(intent);
    }

    private void openNotificationActivity() {
        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.putExtra("USER_ROLE", "STUDENT");
        startActivity(intent);
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("USER_ROLE", "STUDENT");
        startActivity(intent);
    }

    private void selectCategoryChip(String category, TextView selectedChip) {
        selectedCategory = category;
        for (TextView chip : categoryChips) {
            if (chip == selectedChip) {
                chip.setBackgroundColor(Color.parseColor("#7C3AED"));
                chip.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                chip.setBackgroundColor(Color.parseColor("#171A27"));
                chip.setTextColor(Color.parseColor("#94A3B8"));
            }
        }
        applyFilterAndSearch();
    }

    private void loadStudentData() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        // Set dynamic time-based greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = "Good Morning 👋";
        if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon 👋";
        } else if (hour >= 17 && hour < 22) {
            greeting = "Good Evening 👋";
        }
        tvGreetingHeader.setText(greeting);

        firestore.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentStudent = doc.toObject(UserModel.class);
                        if (currentStudent == null) currentStudent = new UserModel();
                        currentStudent.setUid(user.getUid());

                        String name = doc.getString("name");
                        studentNameTextView.setText("Hello, " + (name != null && !name.isEmpty() ? name : "Student"));

                        String dept = doc.getString("department");
                        String year = doc.getString("year");
                        String roll = doc.getString("rollNumber");

                        String deptStr = (dept != null && !dept.isEmpty()) ? dept : "General";
                        String yearStr = (year != null && !year.isEmpty()) ? year : "Year 1";
                        String rollStr = (roll != null && !roll.isEmpty()) ? " • Roll: " + roll : "";

                        tvStudentProfileDetails.setText("🏛️ " + deptStr + " • " + yearStr + rollStr);

                        loadApprovedEvents();
                        loadStudentLiveMetrics();
                    }
                })
                .addOnFailureListener(e -> studentNameTextView.setText("Hello, Student 👋"));
    }

    private void loadStudentLiveMetrics() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        firestore.collection("participations")
                .whereEqualTo("studentId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int joinedCount = 0;
                    int certCount = 0;
                    Set<String> uniqueClubs = new HashSet<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ParticipationModel p = doc.toObject(ParticipationModel.class);
                        if (p == null) continue;

                        String status = p.getStatus();
                        if ("cancelled".equalsIgnoreCase(status)) continue;

                        joinedCount++;

                        // Certificates = verified attendance (present) OR non-none award result
                        String att = p.getAttendance();
                        String res = p.getResult();
                        if ("present".equalsIgnoreCase(att) || (res != null && !res.isEmpty() && !"none".equalsIgnoreCase(res))) {
                            certCount++;
                        }

                        // Clubs joined
                        String teamOrClub = p.getTeamName();
                        if (teamOrClub != null && !teamOrClub.isEmpty()) {
                            uniqueClubs.add(teamOrClub);
                        } else if (p.getEventName() != null && !p.getEventName().isEmpty()) {
                            uniqueClubs.add(p.getEventName());
                        }
                    }

                    tvStatEventsJoined.setText(String.valueOf(joinedCount));
                    tvStatCertificates.setText(String.valueOf(certCount));
                    tvStatClubsJoined.setText(String.valueOf(uniqueClubs.size()));
                });
    }

    private void loadApprovedEvents() {
        firestore.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    approvedEvents.clear();
                    int upcomingCount = 0;
                    String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");

                        boolean isApproved = "approved".equalsIgnoreCase(status) ||
                                (status != null && status.toLowerCase().contains("approve"));

                        if (!isApproved) continue;

                        EventModel event = doc.toObject(EventModel.class);
                        if (event == null) event = new EventModel();
                        event.setEventId(doc.getId());

                        if (doc.contains("eventName") && doc.getString("eventName") != null) event.setEventName(doc.getString("eventName"));
                        if (doc.contains("clubName") && doc.getString("clubName") != null) event.setClubName(doc.getString("clubName"));

                        String dateVal = doc.contains("date") ? doc.getString("date") : doc.getString("eventDate");
                        if (dateVal != null) event.setDate(dateVal);

                        if (doc.contains("venue") && doc.getString("venue") != null) event.setVenue(doc.getString("venue"));
                        if (doc.contains("department") && doc.getString("department") != null) event.setDepartment(doc.getString("department"));
                        if (doc.contains("category") && doc.getString("category") != null) event.setCategory(doc.getString("category"));
                        if (doc.contains("participationType") && doc.getString("participationType") != null) event.setParticipationType(doc.getString("participationType"));

                        approvedEvents.add(event);

                        // Count upcoming (date >= today or no date specified)
                        String eventDate = event.getDate();
                        if (eventDate.isEmpty() || eventDate.compareTo(todayStr) >= 0) {
                            upcomingCount++;
                        }
                    }

                    tvStatUpcomingEvents.setText(String.valueOf(upcomingCount));

                    Collections.sort(approvedEvents, (e1, e2) -> e1.getDate().compareTo(e2.getDate()));
                    applyFilterAndSearch();
                });
    }

    private void applyFilterAndSearch() {
        String searchQuery = searchEditText != null ? searchEditText.getText().toString().trim().toLowerCase() : "";

        // 1. Recommended Events using RecommendationService
        List<EventModel> rawRecommended = RecommendationService.getRecommendedEvents(currentStudent, approvedEvents);
        recommendedList.clear();
        for (EventModel event : rawRecommended) {
            if (matchesCategoryAndSearch(event, selectedCategory, searchQuery)) {
                recommendedList.add(event);
            }
        }
        recommendedAdapter.updateList(recommendedList);
        if (tvRecommendedEmpty != null) {
            tvRecommendedEmpty.setVisibility(recommendedList.isEmpty() ? View.VISIBLE : View.GONE);
        }

        // 2. Upcoming Events
        filteredUpcomingList.clear();
        for (EventModel event : approvedEvents) {
            if (matchesCategoryAndSearch(event, selectedCategory, searchQuery)) {
                filteredUpcomingList.add(event);
            }
        }
        upcomingAdapter.updateList(filteredUpcomingList);
        if (tvUpcomingEmpty != null) {
            tvUpcomingEmpty.setVisibility(filteredUpcomingList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private boolean matchesCategoryAndSearch(EventModel event, String category, String searchQuery) {
        boolean matchesCat = false;
        if ("All".equalsIgnoreCase(category) || category == null || category.trim().isEmpty()) {
            matchesCat = true;
        } else {
            String eventCat = event.getCategory() != null ? event.getCategory().trim().toLowerCase() : "";
            String targetCat = category.trim().toLowerCase();
            matchesCat = eventCat.contains(targetCat) || targetCat.contains(eventCat);
        }

        if (!matchesCat) return false;

        if (searchQuery.isEmpty()) return true;

        String name = event.getEventName() != null ? event.getEventName().toLowerCase() : "";
        String club = event.getClubName() != null ? event.getClubName().toLowerCase() : "";
        String venue = event.getVenue() != null ? event.getVenue().toLowerCase() : "";
        String dept = event.getDepartment() != null ? event.getDepartment().toLowerCase() : "";
        String cat = event.getCategory() != null ? event.getCategory().toLowerCase() : "";

        return name.contains(searchQuery) || club.contains(searchQuery) ||
               venue.contains(searchQuery) || dept.contains(searchQuery) || cat.contains(searchQuery);
    }
}
