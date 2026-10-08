package com.example.eventhiveai.coordinator;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventhiveai.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClubMembersActivity extends AppCompatActivity {

    private LinearLayout membersContainer;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String clubId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_club_members);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        membersContainer = findViewById(R.id.membersContainer);

        loadClubMembers();
    }

    private void loadClubMembers() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Coordinator is not logged in.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String coordinatorId =
                auth.getCurrentUser().getUid();

        // First get coordinator's club
        db.collection("users")
                .document(coordinatorId)
                .get()
                .addOnSuccessListener(userDoc -> {

                    if (!userDoc.exists()) {
                        Toast.makeText(
                                this,
                                "Coordinator profile not found.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    clubId = userDoc.getString("clubId");

                    if (clubId == null || clubId.isEmpty()) {
                        Toast.makeText(
                                this,
                                "No club assigned to coordinator.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    loadMembersFromClub();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load coordinator profile.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void loadMembersFromClub() {

        db.collection("clubMembers")
                .whereEqualTo("clubId", clubId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    membersContainer.removeAllViews();

                    if (snapshot.isEmpty()) {

                        TextView emptyText =
                                createTextView(
                                        "No club members found."
                                );

                        membersContainer.addView(emptyText);
                        return;
                    }

                    for (DocumentSnapshot memberDoc :
                            snapshot.getDocuments()) {

                        String studentId =
                                memberDoc.getString("studentId");

                        String status =
                                memberDoc.getString("status");

                        if (studentId == null ||
                                studentId.isEmpty()) {
                            continue;
                        }

                        loadStudentDetails(
                                studentId,
                                status
                        );
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load club members: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void loadStudentDetails(
            String studentId,
            String membershipStatus) {

        db.collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener(studentDoc -> {

                    if (!studentDoc.exists()) {
                        return;
                    }

                    String name =
                            studentDoc.getString("name");

                    String email =
                            studentDoc.getString("email");

                    if (name == null || name.isEmpty()) {
                        name = "Student";
                    }

                    if (email == null || email.isEmpty()) {
                        email = "Email unavailable";
                    }

                    addMemberCard(
                            name,
                            email,
                            membershipStatus
                    );
                });
    }
    private void addMemberCard(
            String name,
            String email,
            String status) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                24,
                20,
                24,
                20
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                16,
                8,
                16,
                8
        );

        card.setLayoutParams(cardParams);

        TextView nameText =
                createTextView(name);

        nameText.setTextSize(18);
        nameText.setTextColor(
                android.graphics.Color.BLACK
        );

        nameText.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView emailText =
                createTextView(email);

        emailText.setTextSize(14);
        emailText.setTextColor(
                android.graphics.Color.DKGRAY
        );

        TextView statusText =
                createTextView(
                        "Status: "
                                + (status == null
                                ? "ACTIVE"
                                : status)
                );

        statusText.setTextSize(14);
        statusText.setTextColor(
                android.graphics.Color.DKGRAY
        );

        card.addView(nameText);
        card.addView(emailText);
        card.addView(statusText);

        membersContainer.addView(card);
    }


    private TextView createTextView(String text) {

        TextView textView =
                new TextView(this);

        textView.setText(text);
        textView.setGravity(Gravity.START);
        textView.setPadding(
                4,
                4,
                4,
                4
        );

        return textView;
    }
}