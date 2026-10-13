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

import android.content.res.ColorStateList;
import android.graphics.Color;
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
    private Button btnRoleStudent;
    private Button btnRoleCoordinator;
    private Button btnRoleAdmin;
    private TextView tvDemoAdmin;
    private TextView tvDemoCoordinator;

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
        btnRoleStudent = findViewById(R.id.btnRoleStudent);
        btnRoleCoordinator = findViewById(R.id.btnRoleCoordinator);
        btnRoleAdmin = findViewById(R.id.btnRoleAdmin);
        tvDemoAdmin = findViewById(R.id.tvDemoAdmin);
        tvDemoCoordinator = findViewById(R.id.tvDemoCoordinator);

        setupRoleBasedUI();

        if (btnRoleStudent != null) {
            btnRoleStudent.setOnClickListener(v -> {
                userRole = "STUDENT";
                setupRoleBasedUI();
            });
        }

        if (btnRoleCoordinator != null) {
            btnRoleCoordinator.setOnClickListener(v -> {
                userRole = "COORDINATOR";
                setupRoleBasedUI();
            });
        }

        if (btnRoleAdmin != null) {
            btnRoleAdmin.setOnClickListener(v -> {
                userRole = "ADMIN";
                setupRoleBasedUI();
            });
        }

        if (tvDemoAdmin != null) {
            tvDemoAdmin.setOnClickListener(v -> {
                userRole = "ADMIN";
                setupRoleBasedUI();
                emailEditText.setText("admin@eventhive.ai");
                passwordEditText.setText("admin@123");
            });
        }

        if (tvDemoCoordinator != null) {
            tvDemoCoordinator.setOnClickListener(v -> {
                userRole = "COORDINATOR";
                setupRoleBasedUI();
                emailEditText.setText("coordinator@eventhive.ai");
                passwordEditText.setText("coord@123");
            });
        }

        backButton.setOnClickListener(v -> finish());
        loginButton.setOnClickListener(v -> loginUser());

        signUpText.setOnClickListener(v -> {
            if ("STUDENT".equals(userRole)) {
                openStudentSignup();
            }
        });
    }

    private void setupRoleBasedUI() {
        int activeBg = Color.parseColor("#8B5CF6");
        int inactiveBg = Color.TRANSPARENT;
        int activeText = Color.WHITE;
        int inactiveText = Color.parseColor("#94A3B8");

        if (btnRoleStudent != null) {
            boolean isStudent = "STUDENT".equals(userRole);
            btnRoleStudent.setBackgroundTintList(ColorStateList.valueOf(isStudent ? activeBg : inactiveBg));
            btnRoleStudent.setTextColor(isStudent ? activeText : inactiveText);
        }

        if (btnRoleCoordinator != null) {
            boolean isCoord = "COORDINATOR".equals(userRole);
            btnRoleCoordinator.setBackgroundTintList(ColorStateList.valueOf(isCoord ? activeBg : inactiveBg));
            btnRoleCoordinator.setTextColor(isCoord ? activeText : inactiveText);
        }

        if (btnRoleAdmin != null) {
            boolean isAdmin = "ADMIN".equals(userRole);
            btnRoleAdmin.setBackgroundTintList(ColorStateList.valueOf(isAdmin ? activeBg : inactiveBg));
            btnRoleAdmin.setTextColor(isAdmin ? activeText : inactiveText);
        }

        if ("ADMIN".equals(userRole)) {
            if (loginTitle != null) loginTitle.setText("Faculty Admin Login");
            if (loginSubtitle != null) loginSubtitle.setText("Sign in to manage and approve college events");
            if (signUpText != null) signUpText.setVisibility(View.GONE);
            if (emailEditText != null) emailEditText.setHint("admin@eventhive.ai or email");
        } else if ("COORDINATOR".equals(userRole)) {
            if (loginTitle != null) loginTitle.setText("Club Coordinator Login");
            if (loginSubtitle != null) loginSubtitle.setText("Sign in to create and manage your club events");
            if (signUpText != null) signUpText.setVisibility(View.GONE);
            if (emailEditText != null) emailEditText.setHint("coordinator@eventhive.ai or email");
        } else {
            if (loginTitle != null) loginTitle.setText("Student Login");
            if (loginSubtitle != null) loginSubtitle.setText("Sign in to discover and register for events");
            if (signUpText != null) signUpText.setVisibility(View.VISIBLE);
            if (emailEditText != null) emailEditText.setHint("Enter student college email");
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
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                        
                        // If logging in as ADMIN with admin@eventhive.ai or admin role and user does not exist yet in Firebase Auth
                        if ("ADMIN".equalsIgnoreCase(userRole) && finalEmail.toLowerCase().startsWith("admin")) {
                            // Automatically provision the default admin account
                            firebaseAuth.createUserWithEmailAndPassword(finalEmail, password)
                                    .addOnSuccessListener(authResult -> {
                                        FirebaseUser newUser = authResult.getUser();
                                        if (newUser != null) {
                                            java.util.Map<String, Object> adminData = new java.util.HashMap<>();
                                            adminData.put("uid", newUser.getUid());
                                            adminData.put("name", "Faculty Administrator");
                                            adminData.put("email", finalEmail);
                                            adminData.put("role", "ADMIN");
                                            adminData.put("department", "Office of Student Affairs & Governance");
                                            adminData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                                            firestore.collection("users").document(newUser.getUid()).set(adminData)
                                                    .addOnSuccessListener(v -> {
                                                        setLoading(false);
                                                        Toast.makeText(LoginActivity.this, "Admin account provisioned successfully!", Toast.LENGTH_SHORT).show();
                                                        navigateByRole("ADMIN");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        setLoading(false);
                                                        Toast.makeText(LoginActivity.this, "Admin Auth created, but Firestore init failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                        navigateByRole("ADMIN");
                                                    });
                                        } else {
                                            setLoading(false);
                                            Toast.makeText(LoginActivity.this, "Admin sign-in failed: " + errorMsg, Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(createEx -> {
                                        setLoading(false);
                                        Toast.makeText(LoginActivity.this, "Admin sign-in error: " + errorMsg, Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Sign-in error: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Reads users/{UID} from Firestore to determine role and navigate.
     * If user exists in Auth with admin email but profile document is missing in Firestore, automatically create the Admin profile.
     */
    private void fetchUserRoleAndNavigate(String uid) {
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        setLoading(false);
                        String role = documentSnapshot.getString("role");
                        if (role == null || !("STUDENT".equalsIgnoreCase(role) || "COORDINATOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role))) {
                            Toast.makeText(LoginActivity.this, "Your account role is not configured. Contact an administrator.", Toast.LENGTH_LONG).show();
                            firebaseAuth.signOut();
                        } else {
                            navigateByRole(role);
                        }
                    } else {
                        // Document missing in Firestore
                        FirebaseUser curUser = firebaseAuth.getCurrentUser();
                        String curEmail = curUser != null && curUser.getEmail() != null ? curUser.getEmail().toLowerCase() : "";
                        if ("ADMIN".equalsIgnoreCase(userRole) || curEmail.contains("admin")) {
                            // Auto-create missing admin document in Firestore
                            java.util.Map<String, Object> adminData = new java.util.HashMap<>();
                            adminData.put("uid", uid);
                            adminData.put("name", "Faculty Administrator");
                            adminData.put("email", curUser != null ? curUser.getEmail() : "admin@eventhive.ai");
                            adminData.put("role", "ADMIN");
                            adminData.put("department", "Office of Student Affairs & Governance");
                            adminData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                            firestore.collection("users").document(uid).set(adminData)
                                    .addOnSuccessListener(v -> {
                                        setLoading(false);
                                        navigateByRole("ADMIN");
                                    })
                                    .addOnFailureListener(e -> {
                                        setLoading(false);
                                        // Still allow navigation for authenticated admin
                                        navigateByRole("ADMIN");
                                    });
                        } else {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Your profile is missing. Please contact an administrator.", Toast.LENGTH_LONG).show();
                            firebaseAuth.signOut();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // On network failure or permission issue, if user is admin role allow navigation
                    setLoading(false);
                    FirebaseUser curUser = firebaseAuth.getCurrentUser();
                    String curEmail = curUser != null && curUser.getEmail() != null ? curUser.getEmail().toLowerCase() : "";
                    if ("ADMIN".equalsIgnoreCase(userRole) || curEmail.contains("admin")) {
                        navigateByRole("ADMIN");
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to fetch user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
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
