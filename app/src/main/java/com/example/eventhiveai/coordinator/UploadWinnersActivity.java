package com.example.eventhiveai.coordinator;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class UploadWinnersActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etWinnerEvent;
    private EditText etFirstPlace;
    private EditText etSecondPlace;
    private EditText etThirdPlace;
    private Button btnPublishWinners;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_winners);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        etWinnerEvent = findViewById(R.id.etWinnerEvent);
        etFirstPlace = findViewById(R.id.etFirstPlace);
        etSecondPlace = findViewById(R.id.etSecondPlace);
        etThirdPlace = findViewById(R.id.etThirdPlace);
        btnPublishWinners = findViewById(R.id.btnPublishWinners);

        btnBack.setOnClickListener(v -> finish());
        btnPublishWinners.setOnClickListener(v -> publishWinners());
    }

    private void publishWinners() {
        String eventName = etWinnerEvent.getText().toString().trim();
        String first = etFirstPlace.getText().toString().trim();
        String second = etSecondPlace.getText().toString().trim();
        String third = etThirdPlace.getText().toString().trim();

        if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(first)) {
            Toast.makeText(this, "Please enter event name and 1st place winner.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPublishWinners.setEnabled(false);

        // 1. Save to winners collection
        String winnerId = "win_" + System.currentTimeMillis();
        Map<String, Object> winnerData = new HashMap<>();
        winnerData.put("winnerId", winnerId);
        winnerData.put("eventName", eventName);
        winnerData.put("firstPlace", "🥇 " + first);
        winnerData.put("secondPlace", second.isEmpty() ? "" : "🥈 " + second);
        winnerData.put("thirdPlace", third.isEmpty() ? "" : "🥉 " + third);
        winnerData.put("clubName", "Campus Club");
        winnerData.put("coordinatorId", FirebaseAuth.getInstance().getUid());
        winnerData.put("publishedAt", FieldValue.serverTimestamp());
        db.collection("winners").document(winnerId).set(winnerData);

        // 2. Post announcement to notifications collection so all students can see
        String notifId = "notif_" + System.currentTimeMillis();
        Map<String, Object> notif = new HashMap<>();
        notif.put("notificationId", notifId);
        notif.put("title", "🏆 Winners Announced: " + eventName);
        notif.put("message", "Congratulations to the winners!\n1st: " + first +
                (second.isEmpty() ? "" : "\n2nd: " + second) +
                (third.isEmpty() ? "" : "\n3rd: " + third));
        notif.put("recipientRole", "ALL");
        notif.put("coordinatorId", FirebaseAuth.getInstance().getUid());
        notif.put("timestamp", FieldValue.serverTimestamp());

        db.collection("notifications").document(notifId).set(notif)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(UploadWinnersActivity.this, "Winners published and broadcast to students!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnPublishWinners.setEnabled(true);
                    Toast.makeText(UploadWinnersActivity.this, "Failed to publish: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
