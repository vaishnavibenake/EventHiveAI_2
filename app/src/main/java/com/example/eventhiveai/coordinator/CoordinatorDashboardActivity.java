package com.example.eventhiveai.coordinator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventhiveai.MainActivity;
import com.example.eventhiveai.NotificationsActivity;
import com.example.eventhiveai.R;
import com.example.eventhiveai.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class CoordinatorDashboardActivity extends AppCompatActivity {

    private static final String TAG = "CoordDashboard";

    private DrawerLayout coordinatorDrawerLayout;

    private View menuButton;

    // Metric TextViews
    private TextView tvCoordTotalEvents;
    private TextView tvCoordActiveEvents;
    private TextView tvCoordPendingEvents;
    private TextView tvCoordCompletedEvents;

    // Real-Time Metrics Listener State
    private ListenerRegistration metricsListener;
    private boolean hasSuccessfullyLoadedMetrics = false;

    // Quick Access On-Screen Buttons
    private View btnQuickCreateEvent;
    private View btnCoordNotifications;
    private View btnCoordCreateEvent;
    private View btnCoordMyEvents;
    private View btnCoordRegistrations;
    private View btnCoordWinners;
    private View btnCoordGallery;
    private View btnCoordClubProfile;
    private View btnCoordAnalytics;
    private View btnCoordReports;

    // Drawer Menu Items
    private View menuCoordDashboard;
    private View menuCoordCreateEvent;
    private View menuCoordMyEvents;
    private View menuCoordRegistrations;
    private View menuCoordWinners;
    private View menuCoordGallery;
    private View menuCoordMembers;
    private View menuCoordProfile;
    private View menuCoordAnalytics;
    private View menuCoordReports;
    private View menuCoordNotifications;
    private View menuCoordSettings;
    private View menuCoordLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView clubNameText;
    private TextView tvCoordWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.eventhiveai.SettingsActivity.applySavedTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_coordinator_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupDrawer();
        setupMenuClicks();
        setupQuickAccessClicks();
        loadClubName();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMetricsListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMetricsListener();
    }

    private void initializeViews() {
        coordinatorDrawerLayout = findViewById(R.id.coordinatorDrawerLayout);
        menuButton = findViewById(R.id.btnCoordMenu);
        clubNameText = findViewById(R.id.tvCoordClubName);
        tvCoordWelcome = findViewById(R.id.tvCoordWelcome);

        // Metric TextViews
        tvCoordTotalEvents = findViewById(R.id.tvCoordTotalEvents);
        tvCoordActiveEvents = findViewById(R.id.tvCoordActiveEvents);
        tvCoordPendingEvents = findViewById(R.id.tvCoordPendingEvents);
        tvCoordCompletedEvents = findViewById(R.id.tvCoordCompletedEvents);

        // On-screen Quick Access Buttons
        btnQuickCreateEvent = findViewById(R.id.btnQuickCreateEvent);
        btnCoordNotifications = findViewById(R.id.btnCoordNotifications);
        btnCoordCreateEvent = findViewById(R.id.btnCoordCreateEvent);
        btnCoordMyEvents = findViewById(R.id.btnCoordMyEvents);
        btnCoordRegistrations = findViewById(R.id.btnCoordRegistrations);
        btnCoordWinners = findViewById(R.id.btnCoordWinners);
        btnCoordGallery = findViewById(R.id.btnCoordGallery);
        btnCoordClubProfile = findViewById(R.id.btnCoordClubProfile);
        btnCoordAnalytics = findViewById(R.id.btnCoordAnalytics);
        btnCoordReports = findViewById(R.id.btnCoordReports);

        // Drawer Menu Items
        menuCoordDashboard = findViewById(R.id.menuCoordDashboard);
        menuCoordCreateEvent = findViewById(R.id.menuCoordCreateEvent);
        menuCoordMyEvents = findViewById(R.id.menuCoordMyEvents);
        menuCoordRegistrations = findViewById(R.id.menuCoordRegistrations);
        menuCoordWinners = findViewById(R.id.menuCoordWinners);
        menuCoordGallery = findViewById(R.id.menuCoordGallery);
        menuCoordMembers = findViewById(R.id.menuCoordMembers);
        menuCoordProfile = findViewById(R.id.menuCoordProfile);
        menuCoordAnalytics = findViewById(R.id.menuCoordAnalytics);
        menuCoordReports = findViewById(R.id.menuCoordReports);
        menuCoordNotifications = findViewById(R.id.menuCoordNotifications);
        menuCoordSettings = findViewById(R.id.menuCoordSettings);
        menuCoordLogout = findViewById(R.id.menuCoordLogout);
    }

    private void setupDrawer() {
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (coordinatorDrawerLayout != null) {
                    if (coordinatorDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
                    } else {
                        coordinatorDrawerLayout.openDrawer(Gravity.LEFT);
                    }
                }
            });
        }
    }

    private void setupQuickAccessClicks() {
        if (btnQuickCreateEvent != null) {
            btnQuickCreateEvent.setOnClickListener(v -> openActivity(CreateEventActivity.class));
        }

        if (btnCoordCreateEvent != null) {
            btnCoordCreateEvent.setOnClickListener(v -> openActivity(CreateEventActivity.class));
        }

        if (btnCoordMyEvents != null) {
            btnCoordMyEvents.setOnClickListener(v -> openActivity(MyEventsActivity.class));
        }

        if (btnCoordRegistrations != null) {
            btnCoordRegistrations.setOnClickListener(v -> openActivity(CoordinatorRegistrationsActivity.class));
        }

        if (btnCoordWinners != null) {
            btnCoordWinners.setOnClickListener(v -> openActivity(UploadWinnersActivity.class));
        }

        if (btnCoordGallery != null) {
            btnCoordGallery.setOnClickListener(v -> openActivity(EventGalleryActivity.class));
        }

        if (btnCoordClubProfile != null) {
            btnCoordClubProfile.setOnClickListener(v -> openActivity(ClubProfileActivity.class));
        }

        if (btnCoordAnalytics != null) {
            btnCoordAnalytics.setOnClickListener(v -> openActivity(CoordinatorAnalyticsActivity.class));
        }

        if (btnCoordReports != null) {
            btnCoordReports.setOnClickListener(v -> openActivity(CoordinatorAnalyticsActivity.class));
        }

        if (btnCoordNotifications != null) {
            btnCoordNotifications.setOnClickListener(v -> openActivity(NotificationsActivity.class));
        }
    }

    private void setupMenuClicks() {

        // Dashboard
        if (menuCoordDashboard != null) {
            menuCoordDashboard.setOnClickListener(v -> closeDrawer());
        }

        // Create Event
        if (menuCoordCreateEvent != null) {
            menuCoordCreateEvent.setOnClickListener(v -> {
                closeDrawer();
                openActivity(CreateEventActivity.class);
            });
        }

        // My Events
        if (menuCoordMyEvents != null) {
            menuCoordMyEvents.setOnClickListener(v -> {
                closeDrawer();
                openActivity(MyEventsActivity.class);
            });
        }

        // Registrations
        if (menuCoordRegistrations != null) {
            menuCoordRegistrations.setOnClickListener(v -> {
                closeDrawer();
                openActivity(CoordinatorRegistrationsActivity.class);
            });
        }

        // Winners
        if (menuCoordWinners != null) {
            menuCoordWinners.setOnClickListener(v -> {
                closeDrawer();
                openActivity(UploadWinnersActivity.class);
            });
        }

        // Gallery
        if (menuCoordGallery != null) {
            menuCoordGallery.setOnClickListener(v -> {
                closeDrawer();
                openActivity(EventGalleryActivity.class);
            });
        }

        // Club Members
        if (menuCoordMembers != null) {
            menuCoordMembers.setOnClickListener(v -> {
                closeDrawer();
                openActivity(ClubMembersActivity.class);
            });
        }

        // Club Profile
        if (menuCoordProfile != null) {
            menuCoordProfile.setOnClickListener(v -> {
                closeDrawer();
                openActivity(ClubProfileActivity.class);
            });
        }

        // Analytics
        if (menuCoordAnalytics != null) {
            menuCoordAnalytics.setOnClickListener(v -> {
                closeDrawer();
                openActivity(CoordinatorAnalyticsActivity.class);
            });
        }

        // Reports
        if (menuCoordReports != null) {
            menuCoordReports.setOnClickListener(v -> {
                closeDrawer();
                openActivity(CoordinatorAnalyticsActivity.class);
            });
        }

        // Notifications
        if (menuCoordNotifications != null) {
            menuCoordNotifications.setOnClickListener(v -> {
                closeDrawer();
                openActivity(NotificationsActivity.class);
            });
        }

        // Settings
        if (menuCoordSettings != null) {
            menuCoordSettings.setOnClickListener(v -> {
                closeDrawer();
                openActivity(SettingsActivity.class);
            });
        }

        // Logout
        if (menuCoordLogout != null) {
            menuCoordLogout.setOnClickListener(v -> {
                closeDrawer();
                auth.signOut();

                Intent intent = new Intent(
                        CoordinatorDashboardActivity.this,
                        MainActivity.class
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
                finish();
            });
        }
    }

    private void closeDrawer() {
        if (coordinatorDrawerLayout != null && coordinatorDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    private void openActivity(Class<?> activityClass) {
        closeDrawer();
        Intent intent = new Intent(
                CoordinatorDashboardActivity.this,
                activityClass
        );

        intent.putExtra(
                "USER_ROLE",
                "COORDINATOR"
        );

        startActivity(intent);
    }

    private void startMetricsListener() {
        stopMetricsListener(); // Prevent duplicate listeners

        if (auth.getCurrentUser() == null) {
            if (!hasSuccessfullyLoadedMetrics) {
                updateMetricsUI("0", "0", "0", "0");
            }
            return;
        }

        String coordinatorUid = auth.getCurrentUser().getUid();

        metricsListener = db.collection("events")
                .whereEqualTo("coordinatorId", coordinatorUid)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (isFinishing() || isDestroyed()) return;

                    if (error != null) {
                        Log.e(TAG, "Error listening for coordinator event metrics: " + error.getMessage(), error);
                        if (!hasSuccessfullyLoadedMetrics) {
                            updateMetricsUI("--", "--", "--", "--");
                        }
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        int total = queryDocumentSnapshots.size();
                        int active = 0;
                        int pending = 0;
                        int completed = 0;

                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String status = doc.getString("status");
                            if (status != null) {
                                String s = status.trim().toLowerCase();
                                if ("approved".equals(s) || s.contains("approve")) {
                                    active++;
                                } else if ("pending".equals(s)) {
                                    pending++;
                                } else if ("completed".equals(s)) {
                                    completed++;
                                }
                            }
                        }

                        hasSuccessfullyLoadedMetrics = true;
                        updateMetricsUI(
                                String.valueOf(total),
                                String.valueOf(active),
                                String.valueOf(pending),
                                String.valueOf(completed)
                        );
                    }
                });
    }

    private void stopMetricsListener() {
        if (metricsListener != null) {
            metricsListener.remove();
            metricsListener = null;
        }
    }

    private void updateMetricsUI(String total, String active, String pending, String completed) {
        if (tvCoordTotalEvents != null) tvCoordTotalEvents.setText(total);
        if (tvCoordActiveEvents != null) tvCoordActiveEvents.setText(active);
        if (tvCoordPendingEvents != null) tvCoordPendingEvents.setText(pending);
        if (tvCoordCompletedEvents != null) tvCoordCompletedEvents.setText(completed);
    }

    private void loadClubName() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String coordinatorId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(coordinatorId)
                .get()
                .addOnSuccessListener(userDocument -> {

                    if (isFinishing() || isDestroyed()) return;

                    if (!userDocument.exists()) {
                        return;
                    }

                    // Update personalized greeting with coordinator's name
                    String coordName = userDocument.getString("name");
                    if (coordName != null && !coordName.isEmpty() && tvCoordWelcome != null) {
                        tvCoordWelcome.setText("Welcome, " + coordName + "!");
                    }

                    String clubId = userDocument.getString("clubId");

                    if (clubId == null || clubId.isEmpty()) {
                        if (clubNameText != null) {
                            clubNameText.setText("Club: Not assigned — contact Admin");
                        }
                        return;
                    }

                    db.collection("clubs")
                            .document(clubId)
                            .get()
                            .addOnSuccessListener(clubDocument -> {

                                if (isFinishing() || isDestroyed()) return;

                                if (!clubDocument.exists()) {
                                    return;
                                }

                                String clubName = clubDocument.getString("clubName");

                                if (clubName == null || clubName.isEmpty()) {
                                    clubName = clubDocument.getString("name");
                                }

                                if (clubName != null && !clubName.isEmpty() && clubNameText != null) {
                                    clubNameText.setText("Lead Coordinator • " + clubName);
                                }
                            });
                });
    }
}
