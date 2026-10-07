package com.example.eventhiveai;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.admin.AdminDashboardActivity;
import com.example.eventhiveai.coordinator.CoordinatorDashboardActivity;
import com.example.eventhiveai.student.activity_student_dashboard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView backButton;
    private TextView signUpText;
    private TextView loginTitle;
    private TextView loginSubtitle;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private String userRole = "STUDENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get selected role from Intent
        String roleExtra = getIntent().getStringExtra("USER_ROLE");
        if (roleExtra != null && !roleExtra.isEmpty()) {
            userRole = roleExtra.toUpperCase();
        }

        // Connect XML views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        backButton = findViewById(R.id.backButton);
        signUpText = findViewById(R.id.signUpText);
        loginTitle = findViewById(R.id.loginTitle);
        loginSubtitle = findViewById(R.id.loginSubtitle);

        setupRoleBasedUI();

        backButton.setOnClickListener(v -> finish());
        loginButton.setOnClickListener(v -> loginUser());

        signUpText.setOnClickListener(v -> {
            if ("STUDENT".equals(userRole)) {
                openStudentSignup();
            }
        });
    }

    private void setupRoleBasedUI() {
        if ("ADMIN".equals(userRole)) {
            if (loginTitle != null) loginTitle.setText("Faculty Admin Login");
            if (loginSubtitle != null) loginSubtitle.setText("Sign in to manage and approve college events");
            signUpText.setVisibility(View.GONE);
            emailEditText.setHint("admin@eventhive.ai or email");
        } else if ("COORDINATOR".equals(userRole)) {
            if (loginTitle != null) loginTitle.setText("Club Coordinator Login");
            if (loginSubtitle != null) loginSubtitle.setText("Sign in to create and manage your club events");
            signUpText.setVisibility(View.GONE);
            emailEditText.setHint("coordinator@eventhive.ai or email");
        } else {
            if (loginTitle != null) loginTitle.setText("Student Login");
            if (loginSubtitle != null) loginSubtitle.setText("Sign in to discover and register for events");
            signUpText.setVisibility(View.VISIBLE);
            emailEditText.setHint("Enter student college email");
        }
    }

    private void openStudentSignup() {
        Intent intent = new Intent(LoginActivity.this, StudentSignupActivity.class);
        intent.putExtra("USER_ROLE", "STUDENT");
        startActivity(intent);
    }

    private void loginUser() {
        String inputEmail = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // 1. Check Empty Fields
        if (TextUtils.isEmpty(inputEmail)) {
            emailEditText.setError("Please enter your email or username");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Please enter your password");
            passwordEditText.requestFocus();
            return;
        }

        // Normalize email: support username format e.g. "EventHive_Admin" or "coordinator"
        String email = inputEmail;
        if (!email.contains("@")) {
            email = email.toLowerCase().replace(" ", "_") + "@eventhive.ai";
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email format");
            emailEditText.requestFocus();
            return;
        }

        setLoading(true);

        final String finalEmail = email;
        firebaseAuth.signInWithEmailAndPassword(finalEmail, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserRoleAndNavigate(user.getUid());
                        } else {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Authentication error: User session not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Sign-in failed. Check your email and password, or create a student account.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Reads users/{UID} from Firestore to determine role and navigate.
     */
    private void fetchUserRoleAndNavigate(String uid) {
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role == null || !("STUDENT".equalsIgnoreCase(role) || "COORDINATOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role))) {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Your account role is not configured. Contact an administrator.", Toast.LENGTH_LONG).show();
                            firebaseAuth.signOut();
                        } else navigateByRole(role);
                    } else {
                        Toast.makeText(LoginActivity.this, "Your profile is missing. Please contact an administrator.", Toast.LENGTH_LONG).show();
                        firebaseAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Failed to fetch user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateByRole(String actualRole) {
        String normalizedRole = actualRole.toUpperCase();

        // Check role permission against selected portal
        if (!normalizedRole.equals(userRole)) {
            Toast.makeText(LoginActivity.this,
                    "Access Notice: You are logged in as " + normalizedRole + ". Redirecting to your dashboard.",
                    Toast.LENGTH_LONG).show();
        }

        Intent targetIntent;
        switch (normalizedRole) {
            case "ADMIN":
                targetIntent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                break;
            case "COORDINATOR":
                targetIntent = new Intent(LoginActivity.this, CoordinatorDashboardActivity.class);
                break;
            case "STUDENT":
            default:
                targetIntent = new Intent(LoginActivity.this, activity_student_dashboard.class);
                break;
        }

        targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(targetIntent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        loginButton.setEnabled(!isLoading);
        loginButton.setText(isLoading ? "Signing In..." : "Sign In");
    }
}
