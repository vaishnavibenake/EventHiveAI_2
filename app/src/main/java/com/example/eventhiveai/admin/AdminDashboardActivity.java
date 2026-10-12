package com.example.eventhiveai.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventhiveai.LoginActivity;
import com.example.eventhiveai.NotificationsActivity;
import com.example.eventhiveai.R;
import com.example.eventhiveai.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton btnMenu, btnNotifications;
    private TextView tvTotalEvents, tvActiveClubs, tvPendingEvents, tvVenueConflicts;

    private Button btnCreateEvent, btnViewPending;
    private Button btnManageEvents, btnManageClubs, btnManageUsers, quickCalendar, btnViewReports;

    // Custom Drawer Menu Items
    private View menuDashboard, menuCalendar, menuClubs, menuEvents, menuUsers;
    private View menuReports, menuNotifications, menuSettings, menuLogout;

    private FirebaseFirestore db;
    private ListenerRegistration eventsListener, clubsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        btnNotifications = findViewById(R.id.btnNotifications);

        tvTotalEvents = findViewById(R.id.tvTotalEvents);
        tvActiveClubs = findViewById(R.id.tvActiveClubs);
        tvPendingEvents = findViewById(R.id.tvPendingEvents);
        tvVenueConflicts = findViewById(R.id.tvVenueConflicts);

        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnViewPending = findViewById(R.id.btnViewPending);

        btnManageEvents = findViewById(R.id.btnManageEvents);
        btnManageClubs = findViewById(R.id.btnManageClubs);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        quickCalendar = findViewById(R.id.quickCalendar);
        btnViewReports = findViewById(R.id.btnViewReports);

        // Navigation Drawer items
        menuDashboard = findViewById(R.id.menuDashboard);
        menuCalendar = findViewById(R.id.menuCalendar);
        menuClubs = findViewById(R.id.menuClubs);
        menuEvents = findViewById(R.id.menuEvents);
        menuUsers = findViewById(R.id.menuUsers);
        menuReports = findViewById(R.id.menuReports);
        menuNotifications = findViewById(R.id.menuNotifications);
        menuSettings = findViewById(R.id.menuSettings);
        menuLogout = findViewById(R.id.menuLogout);
    }

    private void setupListeners() {
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });
        }

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> openActivity(NotificationsActivity.class));
        }

        if (btnCreateEvent != null) {
            btnCreateEvent.setOnClickListener(v -> openActivity(ManageEventsActivity.class));
        }

        if (btnViewPending != null) {
            btnViewPending.setOnClickListener(v -> openActivity(ManageEventsActivity.class));
        }

        if (btnManageEvents != null) {
            btnManageEvents.setOnClickListener(v -> openActivity(ManageEventsActivity.class));
        }

        if (btnManageClubs != null) {
            btnManageClubs.setOnClickListener(v -> openActivity(ManageClubsActivity.class));
        }

        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> openActivity(ManageUsersActivity.class));
        }

        if (quickCalendar != null) {
            quickCalendar.setOnClickListener(v -> openActivity(AdminCalendarActivity.class));
        }

        if (btnViewReports != null) {
            btnViewReports.setOnClickListener(v -> openActivity(AdminReportsActivity.class));
        }

        if (tvTotalEvents != null) {
            tvTotalEvents.setOnClickListener(v -> openActivity(ManageEventsActivity.class));
        }

        if (tvActiveClubs != null) {
            tvActiveClubs.setOnClickListener(v -> openActivity(ManageClubsActivity.class));
        }

        if (tvPendingEvents != null) {
            tvPendingEvents.setOnClickListener(v -> openActivity(ManageEventsActivity.class));
        }

        if (tvVenueConflicts != null) {
            tvVenueConflicts.setOnClickListener(v -> {
                Toast.makeText(this, "Reviewing events for schedule & venue clashes.", Toast.LENGTH_SHORT).show();
                openActivity(ManageEventsActivity.class);
            });
        }

        // Drawer navigation items
        if (menuDashboard != null) {
            menuDashboard.setOnClickListener(v -> closeDrawer());
        }

        if (menuCalendar != null) {
            menuCalendar.setOnClickListener(v -> {
                closeDrawer();
                openActivity(AdminCalendarActivity.class);
            });
        }

        if (menuClubs != null) {
            menuClubs.setOnClickListener(v -> {
                closeDrawer();
                openActivity(ManageClubsActivity.class);
            });
        }

        if (menuEvents != null) {
            menuEvents.setOnClickListener(v -> {
                closeDrawer();
                openActivity(ManageEventsActivity.class);
            });
        }

        if (menuUsers != null) {
            menuUsers.setOnClickListener(v -> {
                closeDrawer();
                openActivity(ManageUsersActivity.class);
            });
        }

        if (menuReports != null) {
            menuReports.setOnClickListener(v -> {
                closeDrawer();
                openActivity(AdminReportsActivity.class);
            });
        }

        if (menuNotifications != null) {
            menuNotifications.setOnClickListener(v -> {
                closeDrawer();
                openActivity(NotificationsActivity.class);
            });
        }

        if (menuSettings != null) {
            menuSettings.setOnClickListener(v -> {
                closeDrawer();
                openActivity(SettingsActivity.class);
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                closeDrawer();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                intent.putExtra("USER_ROLE", "ADMIN");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void closeDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void openActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.putExtra("USER_ROLE", "ADMIN");
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachFirestoreListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (eventsListener != null) eventsListener.remove();
        if (clubsListener != null) clubsListener.remove();
    }

    private void attachFirestoreListeners() {
        // Real-time Active Clubs counter
        clubsListener = db.collection("clubs")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            if (tvActiveClubs != null) tvActiveClubs.setText("0");
                            return;
                        }
                        if (value != null) {
                            int activeCount = 0;
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                String status = doc.getString("status");
                                if (status != null && status.equalsIgnoreCase("active")) {
                                    activeCount++;
                                }
                            }
                            if (tvActiveClubs != null) tvActiveClubs.setText(String.valueOf(activeCount));
                        }
                    }
                });

        // Real-time Total Events, Pending Approvals & Venue Conflicts
        eventsListener = db.collection("events")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            if (tvTotalEvents != null) tvTotalEvents.setText("0");
                            if (tvPendingEvents != null) tvPendingEvents.setText("0");
                            if (tvVenueConflicts != null) tvVenueConflicts.setText("0");
                            return;
                        }

                        if (value != null) {
                            List<DocumentSnapshot> docs = value.getDocuments();
                            int totalEvents = docs.size();
                            int pendingEvents = 0;

                            for (DocumentSnapshot doc : docs) {
                                String status = doc.getString("status");
                                if (status != null && status.equalsIgnoreCase("pending")) {
                                    pendingEvents++;
                                }
                            }

                            if (tvTotalEvents != null) tvTotalEvents.setText(String.valueOf(totalEvents));
                            if (tvPendingEvents != null) tvPendingEvents.setText(String.valueOf(pendingEvents));

                            int conflicts = calculateVenueConflicts(docs);
                            if (tvVenueConflicts != null) tvVenueConflicts.setText(String.valueOf(conflicts));
                        }
                    }
                });
    }

    private int calculateVenueConflicts(List<DocumentSnapshot> docs) {
        int conflictCount = 0;
        int n = docs.size();

        for (int i = 0; i < n; i++) {
            DocumentSnapshot docA = docs.get(i);
            String statusA = docA.getString("status");
            if (statusA != null && (statusA.equalsIgnoreCase("rejected") || statusA.equalsIgnoreCase("cancelled"))) continue;

            String dateA = docA.getString("date");
            String venueA = docA.getString("venue");
            String startTimeA = docA.getString("startTime");
            String endTimeA = docA.getString("endTime");

            if (dateA == null || venueA == null || dateA.trim().isEmpty() || venueA.trim().isEmpty()) continue;

            for (int j = i + 1; j < n; j++) {
                DocumentSnapshot docB = docs.get(j);
                String statusB = docB.getString("status");
                if (statusB != null && (statusB.equalsIgnoreCase("rejected") || statusB.equalsIgnoreCase("cancelled"))) continue;

                String dateB = docB.getString("date");
                String venueB = docB.getString("venue");
                String startTimeB = docB.getString("startTime");
                String endTimeB = docB.getString("endTime");

                if (dateB == null || venueB == null || dateB.trim().isEmpty() || venueB.trim().isEmpty()) continue;

                if (dateA.trim().equalsIgnoreCase(dateB.trim()) && venueA.trim().equalsIgnoreCase(venueB.trim())) {
                    // Check if time intervals overlap or identical
                    if (startTimeA != null && startTimeB != null && !startTimeA.trim().isEmpty() && !startTimeB.trim().isEmpty()) {
                        if (startTimeA.trim().equalsIgnoreCase(startTimeB.trim())) {
                            conflictCount++;
                        } else {
                            conflictCount++;
                        }
                    } else {
                        conflictCount++;
                    }
                }
            }
        }
        return conflictCount;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}