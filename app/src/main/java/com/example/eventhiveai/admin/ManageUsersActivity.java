package com.example.eventhiveai.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.models.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ManageUsersActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerUsers;
    private ProgressBar progressBar;
    private LinearLayout emptyView;

    private ArrayList<UserModel> userList;
    private UserAdapter userAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        recyclerUsers.setAdapter(userAdapter);

        btnBack.setOnClickListener(v -> finish());

        loadCoordinators();
    }

    private void loadCoordinators() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("role", "COORDINATOR")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    userList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user == null) user = new UserModel();
                        user.setUid(doc.getId());
                        if (doc.contains("name")) user.setName(doc.getString("name"));
                        if (doc.contains("email")) user.setEmail(doc.getString("email"));
                        if (doc.contains("department")) user.setDepartment(doc.getString("department"));
                        if (doc.contains("clubId")) user.setClubId(doc.getString("clubId"));

                        userList.add(user);
                    }

                    userAdapter.notifyDataSetChanged();

                    if (userList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "Failed to load coordinators: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}