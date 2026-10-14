package com.example.eventhiveai;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "eventhive_settings";
    public static final String KEY_THEME = "pref_app_theme";
    public static final String KEY_NOTIFICATIONS = "pref_notifications_enabled";

    private ImageButton btnBack;
    private TextView tvSettingsAvatar;
    private TextView tvSettingsName;
    private TextView tvSettingsRoleBadge;
    private TextView tvSettingsEmail;
    private TextView tvSettingsDepartment;
    private TextView tvSettingsExtra;

    private RadioGroup rgThemeOptions;
    private RadioButton rbThemeDark;
    private RadioButton rbThemeLight;
    private RadioButton rbThemeSystem;
    private SwitchCompat switchNotifications;

    private Button btnEditProfile;
    private Button btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    private String currentName = "";
    private String currentDepartment = "";

    public static void applySavedTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int theme = sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_YES);
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        btnBack = findViewById(R.id.btnBack);
        tvSettingsAvatar = findViewById(R.id.tvSettingsAvatar);
        tvSettingsName = findViewById(R.id.tvSettingsName);
        tvSettingsRoleBadge = findViewById(R.id.tvSettingsRoleBadge);
        tvSettingsEmail = findViewById(R.id.tvSettingsEmail);
        tvSettingsDepartment = findViewById(R.id.tvSettingsDepartment);
        tvSettingsExtra = findViewById(R.id.tvSettingsExtra);

        rgThemeOptions = findViewById(R.id.rgThemeOptions);
        rbThemeDark = findViewById(R.id.rbThemeDark);
        rbThemeLight = findViewById(R.id.rbThemeLight);
        rbThemeSystem = findViewById(R.id.rbThemeSystem);
        switchNotifications = findViewById(R.id.switchNotifications);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(SettingsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        setupPreferences();
        loadUserProfile();
    }

    private void setupPreferences() {
        int savedTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_YES);
        if (savedTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            rbThemeLight.setChecked(true);
        } else if (savedTheme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            rbThemeSystem.setChecked(true);
        } else {
            rbThemeDark.setChecked(true);
        }

        rgThemeOptions.setOnCheckedChangeListener((group, checkedId) -> {
            int newTheme;
            String msg;
            if (checkedId == R.id.rbThemeLight) {
                newTheme = AppCompatDelegate.MODE_NIGHT_NO;
                msg = "Light theme enabled";
            } else if (checkedId == R.id.rbThemeSystem) {
                newTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                msg = "System default theme enabled";
            } else {
                newTheme = AppCompatDelegate.MODE_NIGHT_YES;
                msg = "Dark theme enabled";
            }

            prefs.edit().putInt(KEY_THEME, newTheme).apply();
            AppCompatDelegate.setDefaultNightMode(newTheme);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        boolean notifEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        switchNotifications.setChecked(notifEnabled);
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            String msg = isChecked ? "Event notifications enabled" : "Notifications muted";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            tvSettingsName.setText("Guest / Logged Out");
            return;
        }

        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "N/A";
        tvSettingsEmail.setText("✉️ Email: " + email);

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (isFinishing() || isDestroyed()) return;

                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String role = doc.getString("role");
                        String dept = doc.getString("department");
                        String year = doc.getString("year");
                        String roll = doc.getString("rollNumber");
                        String clubId = doc.getString("clubId");

                        currentName = name != null ? name : "";
                        currentDepartment = dept != null ? dept : "";

                        if (!currentName.isEmpty()) {
                            tvSettingsName.setText(currentName);
                            tvSettingsAvatar.setText(currentName.substring(0, 1).toUpperCase());
                        } else {
                            tvSettingsName.setText("User Profile");
                            tvSettingsAvatar.setText("U");
                        }

                        if (role != null) {
                            tvSettingsRoleBadge.setText("ROLE: " + role.toUpperCase());
                        }

                        tvSettingsDepartment.setText("🏛️ Department: " + (!currentDepartment.isEmpty() ? currentDepartment : "General"));

                        if ("STUDENT".equalsIgnoreCase(role)) {
                            tvSettingsExtra.setText("🎓 Year: " + (year != null ? year : "N/A") + " | Roll No: " + (roll != null ? roll : "N/A"));
                        } else if ("COORDINATOR".equalsIgnoreCase(role)) {
                            if (clubId != null && !clubId.isEmpty()) {
                                db.collection("clubs").document(clubId).get()
                                        .addOnSuccessListener(clubDoc -> {
                                            if (isFinishing() || isDestroyed()) return;
                                            String clubName = clubDoc.getString("name");
                                            if (TextUtils.isEmpty(clubName)) {
                                                clubName = clubDoc.getString("clubName");
                                            }
                                            if (TextUtils.isEmpty(clubName)) {
                                                clubName = clubId;
                                            }
                                            tvSettingsExtra.setText("🎯 Assigned Club: " + clubName + " (Read-Only)");
                                        })
                                        .addOnFailureListener(e ->
                                                tvSettingsExtra.setText("🎯 Assigned Club ID: " + clubId));
                            } else {
                                tvSettingsExtra.setText("🎯 Assigned Club: Unassigned");
                            }
                        } else {
                            tvSettingsExtra.setText("🔑 Faculty Administrator Access Level");
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showEditProfileDialog() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        final EditText etName = new EditText(this);
        etName.setHint("Full Name");
        etName.setText(currentName);
        layout.addView(etName);

        final EditText etDept = new EditText(this);
        etDept.setHint("Department (e.g. CSE)");
        etDept.setText(currentDepartment);
        etDept.setPadding(0, 20, 0, 0);
        layout.addView(etDept);

        builder.setView(layout);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newDept = etDept.getText().toString().trim();

            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(SettingsActivity.this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);
            updates.put("department", newDept);

            db.collection("users").document(currentUser.getUid()).update(updates)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(SettingsActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        loadUserProfile();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(SettingsActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
