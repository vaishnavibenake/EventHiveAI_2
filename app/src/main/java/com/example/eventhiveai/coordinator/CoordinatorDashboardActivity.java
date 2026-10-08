package com.example.eventhiveai.coordinator;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventhiveai.MainActivity;
import com.example.eventhiveai.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CoordinatorDashboardActivity extends AppCompatActivity {

    private DrawerLayout coordinatorDrawerLayout;

    private View menuButton;

    private View menuCoordDashboard;
    private View menuCoordCreateEvent;
    private View menuCoordMyEvents;
    private View menuCoordRegistrations;
    private View menuCoordWinners;
    private View menuCoordGallery;
    private View menuCoordMembers;
    private View menuCoordProfile;
    private View menuCoordAnalytics;
    private View menuCoordNotifications;
    private View menuCoordSettings;
    private View menuCoordLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView clubNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_coordinator_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        coordinatorDrawerLayout =
                findViewById(R.id.coordinatorDrawerLayout);

        menuButton =
                findViewById(R.id.btnCoordMenu);

        clubNameText =
                findViewById(R.id.tvCoordClubName);

        menuCoordDashboard =
                findViewById(R.id.menuCoordDashboard);

        menuCoordCreateEvent =
                findViewById(R.id.menuCoordCreateEvent);

        menuCoordMyEvents =
                findViewById(R.id.menuCoordMyEvents);

        menuCoordRegistrations =
                findViewById(R.id.menuCoordRegistrations);

        menuCoordWinners =
                findViewById(R.id.menuCoordWinners);

        menuCoordGallery =
                findViewById(R.id.menuCoordGallery);

        menuCoordMembers =
                findViewById(R.id.menuCoordMembers);

        menuCoordProfile =
                findViewById(R.id.menuCoordProfile);

        menuCoordAnalytics =
                findViewById(R.id.menuCoordAnalytics);

        menuCoordNotifications =
                findViewById(R.id.menuCoordNotifications);

        menuCoordSettings =
                findViewById(R.id.menuCoordSettings);

        menuCoordLogout =
                findViewById(R.id.menuCoordLogout);

        setupDrawer();
        setupMenuClicks();
        loadClubName();
    }

    private void setupDrawer() {

        menuButton.setOnClickListener(v -> {

            if (coordinatorDrawerLayout.isDrawerOpen(Gravity.LEFT)) {

                coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            } else {

                coordinatorDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
    }

    private void setupMenuClicks() {

        // Dashboard
        menuCoordDashboard.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);
        });

        // Create Event
        menuCoordCreateEvent.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            openActivity(CreateEventActivity.class);
        });

        // My Events
        menuCoordMyEvents.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            openActivity(MyEventsActivity.class);
        });

        // Registrations
        menuCoordRegistrations.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            openActivity(CoordinatorRegistrationsActivity.class);
        });

        // Winners
        menuCoordWinners.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            openActivity(UploadWinnersActivity.class);
        });

        // Gallery
        menuCoordGallery.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            openActivity(EventGalleryActivity.class);
        });

        // Club Members
        menuCoordMembers.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            openActivity(ClubMembersActivity.class);
        });

        // Club Profile
        menuCoordProfile.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            openActivity(ClubProfileActivity.class);
        });

        // Analytics
        menuCoordAnalytics.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            openActivity(CoordinatorAnalyticsActivity.class);
        });

        // Notifications
        menuCoordNotifications.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            Toast.makeText(
                    CoordinatorDashboardActivity.this,
                    "Notifications coming soon",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Settings
        menuCoordSettings.setOnClickListener(v -> {

            coordinatorDrawerLayout.closeDrawer(Gravity.LEFT);

            Toast.makeText(
                    CoordinatorDashboardActivity.this,
                    "Settings coming soon",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Logout
        menuCoordLogout.setOnClickListener(v -> {

            auth.signOut();

            Intent intent =
                    new Intent(
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

    private void openActivity(Class<?> activityClass) {

        Intent intent =
                new Intent(
                        CoordinatorDashboardActivity.this,
                        activityClass
                );

        intent.putExtra(
                "USER_ROLE",
                "COORDINATOR"
        );

        startActivity(intent);
    }

    private void loadClubName() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String coordinatorId =
                auth.getCurrentUser().getUid();

        db.collection("users")
                .document(coordinatorId)
                .get()
                .addOnSuccessListener(userDocument -> {

                    if (!userDocument.exists()) {
                        return;
                    }

                    String clubId =
                            userDocument.getString("clubId");

                    if (clubId == null ||
                            clubId.isEmpty()) {
                        return;
                    }

                    db.collection("clubs")
                            .document(clubId)
                            .get()
                            .addOnSuccessListener(clubDocument -> {

                                if (!clubDocument.exists()) {
                                    return;
                                }

                                String clubName =
                                        clubDocument.getString("clubName");

                                if (clubName == null ||
                                        clubName.isEmpty()) {

                                    clubName =
                                            clubDocument.getString("name");
                                }

                                if (clubName != null &&
                                        !clubName.isEmpty()) {

                                    clubNameText.setText(clubName);
                                }
                            });
                });
    }
}
