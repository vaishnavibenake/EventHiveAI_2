package com.example.eventhiveai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout facultyRole;
    private LinearLayout coordinatorRole;
    private LinearLayout studentRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect MainActivity with activity_main.xml
        setContentView(R.layout.activity_main);

        // Connect Java variables with XML views
        facultyRole = findViewById(R.id.facultyRole);
        coordinatorRole = findViewById(R.id.coordinatorRole);
        studentRole = findViewById(R.id.studentRole);

        // ==========================================
        // FACULTY ADMIN
        // ==========================================

        facultyRole.setOnClickListener(v -> {
            openLogin("ADMIN");
        });

        // ==========================================
        // CLUB COORDINATOR
        // ==========================================

        coordinatorRole.setOnClickListener(v -> {
            openLogin("COORDINATOR");
        });

        // ==========================================
        // STUDENT
        // ==========================================

        studentRole.setOnClickListener(v -> {
            openLogin("STUDENT");
        });
    }

    // ==============================================
    // OPEN LOGIN SCREEN
    // ==============================================

    private void openLogin(String role) {

        Intent intent = new Intent(
                MainActivity.this,
                LoginActivity.class
        );

        // Send selected role to LoginActivity
        intent.putExtra("USER_ROLE", role);

        startActivity(intent);
    }
}
