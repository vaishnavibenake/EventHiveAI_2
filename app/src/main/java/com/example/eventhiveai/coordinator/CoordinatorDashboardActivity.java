package com.example.eventhiveai.coordinator;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventhiveai.MainActivity;
import com.example.eventhiveai.NotificationsActivity;
import com.example.eventhiveai.R;
import com.example.eventhiveai.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CoordinatorDashboardActivity extends AppCompatActivity {

    private DrawerLayout coordinatorDrawerLayout;
    private ImageButton btnCoordMenu;
    private ImageButton btnCoordNotifications;
    private Button btnQuickCreateEvent;

    private TextView tvCoordWelcome;
    private TextView tvCoordClubName;
    private TextView tvCoordTotalEvents;
    private TextView tvCoordActiveEvents;
    private TextView tvCoordPendingEvents;
    private TextView tvCoordCompletedEvents;

    private Button btnCoordCreateEvent;
    private Button btnCoordMyEvents;
    private Button btnCoordRegistrations;
    private Button btnCoordWinners;
    private Button btnCoordGallery;
    private Button btnCoordClubProfile;
    private Button btnCoordAnalytics;

    // Drawer Menu Items
    private View menuCoordDashboard;
    private View menuCoordCreateEvent;
    private View menuCoordMyEvents;
    private View menuCoordRegistrations;
    private View menuCoordWinners;
    private View menuCoordGallery;
    private View menuCoordProfile;
    private View menuCoordAnalytics;
    private View menuCoordNotifications;
    private View menuCoordSettings;
    private View menuCoordLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCoordinatorData();
    }

    private void initializeViews() {
        coordinatorDrawerLayout = findViewById(R.id.coordinatorDrawerLayout);
        btnCoordMenu = findViewById(R.id.btnCoordMenu);
        btnCoordNotifications = findViewById(R.id.btnCoordNotifications);
        btnQuickCreateEvent = findViewById(R.id.btnQuickCreateEvent);

        tvCoordWelcome = findViewById(R.id.tvCoordWelcome);
        tvCoordClubName = findViewById(R.id.tvCoordClubName);
        tvCoordTotalEvents = findViewById(R.id.tvCoordTotalEvents);
        tvCoordActiveEvents = findViewById(R.id.tvCoordActiveEvents);
        tvCoordPendingEvents = findViewById(R.id.tvCoordPendingEvents);
        tvCoordCompletedEvents = findViewById(R.id.tvCoordCompletedEvents);

        btnCoordCreateEvent = findViewById(R.id.btnCoordCreateEvent);
        btnCoordMyEvents = findViewById(R.id.btnCoordMyEvents);
        btnCoordRegistrations = findViewById(R.id.btnCoordRegistrations);
        btnCoordWinners = findViewById(R.id.btnCoordWinners);
        btnCoordGallery = findViewById(R.id.btnCoordGallery);
        btnCoordClubProfile = findViewById(R.id.btnCoordClubProfile);
        btnCoordAnalytics = findViewById(R.id.btnCoordAnalytics);

        menuCoordDashboard = findViewById(R.id.menuCoordDashboard);
        menuCoordCreateEvent = findViewById(R.id.menuCoordCreateEvent);
        menuCoordMyEvents = findViewById(R.id.menuCoordMyEvents);
        menuCoordRegistrations = findViewById(R.id.menuCoordRegistrations);
        menuCoordWinners = findViewById(R.id.menuCoordWinners);
        menuCoordGallery = findViewById(R.id.menuCoordGallery);
        menuCoordProfile = findViewById(R.id.menuCoordProfile);
        menuCoordAnalytics = findViewById(R.id.menuCoordAnalytics);
        menuCoordNotifications = findViewById(R.id.menuCoordNotifications);
        menuCoordSettings = findViewById(R.id.menuCoordSettings);
        menuCoordLogout = findViewById(R.id.menuCoordLogout);
    }

    private void setupListeners() {
        btnCoordMenu.setOnClickListener(v -> {
            if (coordinatorDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            } else {
                coordinatorDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        btnCoordNotifications.setOnClickListener(v -> openActivity(NotificationsActivity.class));
        btnQuickCreateEvent.setOnClickListener(v -> openActivity(CreateEventActivity.class));
        btnCoordCreateEvent.setOnClickListener(v -> openActivity(CreateEventActivity.class));
        btnCoordMyEvents.setOnClickListener(v -> openActivity(MyEventsActivity.class));
        btnCoordRegistrations.setOnClickListener(v -> openActivity(CoordinatorRegistrationsActivity.class));
        btnCoordWinners.setOnClickListener(v -> openActivity(UploadWinnersActivity.class));
        btnCoordGallery.setOnClickListener(v -> openActivity(EventGalleryActivity.class));
        btnCoordClubProfile.setOnClickListener(v -> openActivity(ClubProfileActivity.class));
        btnCoordAnalytics.setOnClickListener(v -> openActivity(CoordinatorAnalyticsActivity.class));

        // Drawer Menu
        menuCoordDashboard.setOnClickListener(v -> coordinatorDrawerLayout.closeDrawer(Gravity.LEFT));
        menuCoordCreateEvent.setOnClickListener(v -> {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            openActivity(CreateEventActivity.class);
        });
        menuCoordMyEvents.setOnClickListener(v -> {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            openActivity(MyEventsActivity.class);
        });
        menuCoordRegistrations.setOnClickListener(v -> {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            openActivity(CoordinatorRegistrationsActivity.class);
        });
        menuCoordWinners.setOnClickListener(v -> {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            openActivity(UploadWinnersActivity.class);
        });
        menuCoordGallery.setOnClickListener(v -> {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            openActivity(EventGalleryActivity.class);
        });
        menuCoordProfile.setOnClickListener(v -> {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            openActivity(ClubProfileActivity.class);
        });
        menuCoordAnalytics.setOnClickListener(v -> {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            openActivity(CoordinatorAnalyticsActivity.class);
        });
        menuCoordNotifications.setOnClickListener(v -> {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            openActivity(NotificationsActivity.class);
        });
        menuCoordSettings.setOnClickListener(v -> {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            openActivity(SettingsActivity.class);
        });
        menuCoordLogout.setOnClickListener(v -> {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
            auth.signOut();
            Toast.makeText(CoordinatorDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CoordinatorDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void openActivity(Class<?> cls) {
        Intent intent = new Intent(CoordinatorDashboardActivity.this, cls);
        intent.putExtra("USER_ROLE", "COORDINATOR");
        startActivity(intent);
    }

    private void loadCoordinatorData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        // Fetch User profile to get coordinator name & club
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvCoordWelcome.setText("Welcome, " + name + "!");
                        }
                        String clubId = doc.getString("clubId");
                        if (clubId != null && !clubId.isEmpty()) {
                            db.collection("clubs").document(clubId).get()
                                    .addOnSuccessListener(clubDoc -> {
                                        if (clubDoc.exists() && clubDoc.getString("name") != null) {
                                            tvCoordClubName.setText("Lead Coordinator • " + clubDoc.getString("name"));
                                        } else tvCoordClubName.setText("Club unavailable");
                                    });
                        } else {
                            tvCoordClubName.setText("No club assigned");
                        }
                    } else {
                        tvCoordClubName.setText("Coordinator profile unavailable");
                    }
                });

        // Query events belonging to this coordinator or their club
        db.collection("events").whereEqualTo("coordinatorId", currentUser.getUid()).get()
                .addOnSuccessListener(snapshots -> {
                    int total = 0;
                    int active = 0;
                    int pending = 0;
                    int completed = 0;

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String coord = doc.getString("coordinatorId");
                        boolean matches = currentUser.getUid().equals(coord);
                        if (matches) {
                            total++;
                            String status = doc.getString("status");
                            if (status != null) {
                                if ("approved".equalsIgnoreCase(status)) active++;
                                else if ("pending".equalsIgnoreCase(status)) pending++;
                                else if ("completed".equalsIgnoreCase(status)) completed++;
                            }
                        }
                    }

                    tvCoordTotalEvents.setText(String.valueOf(total));
                    tvCoordActiveEvents.setText(String.valueOf(active));
                    tvCoordPendingEvents.setText(String.valueOf(pending));
                    tvCoordCompletedEvents.setText(String.valueOf(completed));
                });
    }

    @Override
    public void onBackPressed() {
        if (coordinatorDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }
}
