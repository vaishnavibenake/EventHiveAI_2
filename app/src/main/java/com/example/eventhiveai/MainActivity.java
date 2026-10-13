package com.example.eventhiveai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.admin.AdminDashboardActivity;
import com.example.eventhiveai.coordinator.CoordinatorDashboardActivity;
import com.example.eventhiveai.student.activity_student_dashboard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check authentication state
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is signed in, check role from Firestore
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            if ("ADMIN".equalsIgnoreCase(role)) {
                                navigateTo(AdminDashboardActivity.class, "ADMIN");
                                return;
                            } else if ("COORDINATOR".equalsIgnoreCase(role)) {
                                navigateTo(CoordinatorDashboardActivity.class, "COORDINATOR");
                                return;
                            } else if ("STUDENT".equalsIgnoreCase(role)) {
                                navigateTo(activity_student_dashboard.class, "STUDENT");
                                return;
                            }
                        }
                        // Default fallback if role is missing or invalid
                        navigateToLogin();
                    })
                    .addOnFailureListener(e -> navigateToLogin());
        } else {
            // Not signed in -> forward to LoginActivity
            new Handler(Looper.getMainLooper()).postDelayed(this::navigateToLogin, 600);
        }
    }

    private void navigateTo(Class<?> targetClass, String role) {
        Intent intent = new Intent(MainActivity.this, targetClass);
        intent.putExtra("USER_ROLE", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}