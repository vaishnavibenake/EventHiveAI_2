package com.example.eventhiveai.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventhiveai.LoginActivity;
import com.example.eventhiveai.MainActivity;
import com.example.eventhiveai.NotificationsActivity;
import com.example.eventhiveai.R;
import com.example.eventhiveai.SettingsActivity;
import com.example.eventhiveai.utils.FirebaseDataHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    private View btnMenu;
    private View btnNotifications;
    private View btnCreateEvent;
    private View btnViewPending;
    private View btnManageEvents;
    private View btnManageClubs;
    private View btnManageUsers;
    private View quickCalendar;
    private View btnViewReports;

    private View menuDashboard;
    private View menuEvents;
    private View menuClubs;
    private View menuUsers;
    private View menuCalendar;
    private View menuReports;
    private View menuNotifications;
    private View menuSettings;
    private View menuLogout;

    private TextView tvWelcome;
    private TextView tvTotalEvents;
    private TextView tvActiveClubs;
    private TextView tvPendingEvents;
    private TextView tvVenueConflicts;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupHamburger();
        setupDashboardButtons();
        setupDrawerMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);

        btnMenu = findViewById(R.id.btnMenu);
        btnNotifications = findViewById(R.id.btnNotifications);

        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnViewPending = findViewById(R.id.btnViewPending);

        btnManageEvents = findViewById(R.id.btnManageEvents);
        btnManageClubs = findViewById(R.id.btnManageClubs);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        quickCalendar = findViewById(R.id.quickCalendar);
        btnViewReports = findViewById(R.id.btnViewReports);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalEvents = findViewById(R.id.tvTotalEvents);
        tvActiveClubs = findViewById(R.id.tvActiveClubs);
        tvPendingEvents = findViewById(R.id.tvPendingEvents);
        tvVenueConflicts = findViewById(R.id.tvVenueConflicts);

        menuDashboard = findViewById(R.id.menuDashboard);
        menuEvents = findViewById(R.id.menuEvents);
        menuClubs = findViewById(R.id.menuClubs);
        menuUsers = findViewById(R.id.menuUsers);
        menuCalendar = findViewById(R.id.menuCalendar);
        menuReports = findViewById(R.id.menuReports);
        menuNotifications = findViewById(R.id.menuNotifications);
        menuSettings = findViewById(R.id.menuSettings);
        menuLogout = findViewById(R.id.menuLogout);
    }

    private void setupHamburger() {
        btnMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                drawerLayout.closeDrawer(Gravity.LEFT);
            } else {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, NotificationsActivity.class);
            intent.putExtra("USER_ROLE", "ADMIN");
            startActivity(intent);
        });
    }

    private void setupDashboardButtons() {
        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageEventsActivity.class);
            startActivity(intent);
        });

        btnViewPending.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageEventsActivity.class);
            startActivity(intent);
        });

        btnManageEvents.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageEventsActivity.class);
            startActivity(intent);
        });

        btnManageClubs.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageClubsActivity.class);
            startActivity(intent);
        });

        btnManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        quickCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminCalendarActivity.class);
            intent.putExtra("USER_ROLE", "ADMIN");
            startActivity(intent);
        });

        if (btnViewReports != null) {
            btnViewReports.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminReportsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupDrawerMenu() {
        menuDashboard.setOnClickListener(v -> drawerLayout.closeDrawer(Gravity.LEFT));

        menuEvents.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            startActivity(new Intent(AdminDashboardActivity.this, ManageEventsActivity.class));
        });

        menuClubs.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            startActivity(new Intent(AdminDashboardActivity.this, ManageClubsActivity.class));
        });

        menuUsers.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            startActivity(new Intent(AdminDashboardActivity.this, ManageUsersActivity.class));
        });

        menuCalendar.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            Intent intent = new Intent(AdminDashboardActivity.this, AdminCalendarActivity.class);
            intent.putExtra("USER_ROLE", "ADMIN");
            startActivity(intent);
        });

        if (menuReports != null) {
            menuReports.setOnClickListener(v -> {
                drawerLayout.closeDrawer(Gravity.LEFT);
                startActivity(new Intent(AdminDashboardActivity.this, AdminReportsActivity.class));
            });
        }

        if (menuNotifications != null) {
            menuNotifications.setOnClickListener(v -> {
                drawerLayout.closeDrawer(Gravity.LEFT);
                Intent intent = new Intent(AdminDashboardActivity.this, NotificationsActivity.class);
                intent.putExtra("USER_ROLE", "ADMIN");
                startActivity(intent);
            });
        }

        menuSettings.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            Intent intent = new Intent(AdminDashboardActivity.this, SettingsActivity.class);
            intent.putExtra("USER_ROLE", "ADMIN");
            startActivity(intent);
        });

        menuLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            auth.signOut();
            Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadDashboardData() {
        // Fetch events for total, pending, and venue conflict calculation
        db.collection("events").get()
                .addOnSuccessListener(snapshot -> {
                    int totalEvents = snapshot.size();
                    int pendingEvents = 0;
                    List<EventModel> allEvents = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        EventModel event = doc.toObject(EventModel.class);
                        if (event == null) event = new EventModel();
                        event.setEventId(doc.getId());
                        if (doc.contains("eventName")) event.setEventName(doc.getString("eventName"));
                        if (doc.contains("date")) event.setDate(doc.getString("date"));
                        if (doc.contains("venue")) event.setVenue(doc.getString("venue"));
                        if (doc.contains("status")) event.setStatus(doc.getString("status"));
                        allEvents.add(event);

                        String status = doc.getString("status");
                        if (status != null && status.equalsIgnoreCase("pending")) {
                            pendingEvents++;
                        }
                    }

                    tvTotalEvents.setText(String.valueOf(totalEvents));
                    tvPendingEvents.setText(String.valueOf(pendingEvents));

                    // Real venue conflict detection:
                    int conflicts = FirebaseDataHelper.countVenueConflicts(allEvents);
                    if (tvVenueConflicts != null) {
                        tvVenueConflicts.setText(String.valueOf(conflicts));
                    }
                })
                .addOnFailureListener(e -> {
                    tvTotalEvents.setText("0");
                    tvPendingEvents.setText("0");
                    if (tvVenueConflicts != null) tvVenueConflicts.setText("0");
                });

        // Fetch active clubs count
        db.collection("clubs")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snapshot -> {
                    tvActiveClubs.setText(String.valueOf(snapshot.size()));
                })
                .addOnFailureListener(e -> {
                    tvActiveClubs.setText("0");
                });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }
}
