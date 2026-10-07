package com.example.eventhiveai.coordinator;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClubProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvClubProfileName;
    private TextView tvClubProfileDesc;
    private TextView tvClubProfileEmail;
    private TextView tvClubProfilePhone;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        tvClubProfileName = findViewById(R.id.tvClubProfileName);
        tvClubProfileDesc = findViewById(R.id.tvClubProfileDesc);
        tvClubProfileEmail = findViewById(R.id.tvClubProfileEmail);
        tvClubProfilePhone = findViewById(R.id.tvClubProfilePhone);

        btnBack.setOnClickListener(v -> finish());

        loadClubProfile();
    }

    private void loadClubProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    String clubId = userDoc.getString("clubId");
                    if (clubId == null || clubId.isEmpty()) {
                        tvClubProfileName.setText("No club assigned");
                        tvClubProfileDesc.setText("Ask an administrator to assign a club to your account.");
                        tvClubProfileEmail.setText("");
                        tvClubProfilePhone.setText("");
                        return;
                    }

                    db.collection("clubs").document(clubId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    tvClubProfileName.setText(doc.getString("name"));
                                    tvClubProfileDesc.setText(doc.getString("description"));
                                    tvClubProfileEmail.setText("✉️ " + doc.getString("contactEmail"));
                                    tvClubProfilePhone.setText("📞 " + doc.getString("contactPhone"));
                                }
                            });
                });
    }
}
