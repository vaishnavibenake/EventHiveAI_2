package com.example.eventhiveai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvSettingsAvatar;
    private TextView tvSettingsName;
    private TextView tvSettingsRoleBadge;
    private TextView tvSettingsEmail;
    private TextView tvSettingsDepartment;
    private TextView tvSettingsExtra;
    private Button btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        tvSettingsAvatar = findViewById(R.id.tvSettingsAvatar);
        tvSettingsName = findViewById(R.id.tvSettingsName);
        tvSettingsRoleBadge = findViewById(R.id.tvSettingsRoleBadge);
        tvSettingsEmail = findViewById(R.id.tvSettingsEmail);
        tvSettingsDepartment = findViewById(R.id.tvSettingsDepartment);
        tvSettingsExtra = findViewById(R.id.tvSettingsExtra);
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

        loadUserProfile();
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
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String role = doc.getString("role");
                        String dept = doc.getString("department");
                        String year = doc.getString("year");
                        String roll = doc.getString("rollNumber");
                        String clubId = doc.getString("clubId");

                        if (name != null && !name.isEmpty()) {
                            tvSettingsName.setText(name);
                            tvSettingsAvatar.setText(name.substring(0, 1).toUpperCase());
                        } else {
                            tvSettingsName.setText("User");
                        }

                        if (role != null) {
                            tvSettingsRoleBadge.setText("ROLE: " + role.toUpperCase());
                        }

                        tvSettingsDepartment.setText("🏛️ Department: " + (dept != null ? dept : "General"));

                        if ("STUDENT".equalsIgnoreCase(role)) {
                            tvSettingsExtra.setText("🎓 Year: " + (year != null ? year : "N/A") + " | Roll No: " + (roll != null ? roll : "N/A"));
                        } else if ("COORDINATOR".equalsIgnoreCase(role)) {
                            tvSettingsExtra.setText("🎯 Assigned Club ID: " + (clubId != null ? clubId : "General"));
                        } else {
                            tvSettingsExtra.setText("🔑 Faculty Administrator Access Level");
                        }
                    }
                });
    }
}
