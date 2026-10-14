package com.example.eventhiveai.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.models.ClubModel;
import com.example.eventhiveai.models.UserModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageUsersActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnAddCoordinator;
    private RecyclerView recyclerUsers;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private TextView tvEmptyMessage;

    private ArrayList<UserModel> coordinatorList;
    private UserAdapter userAdapter;
    private FirebaseFirestore db;

    // Cache of clubs to associate coordinators
    private List<ClubModel> availableClubs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnAddCoordinator = findViewById(R.id.btnAddCoordinator);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        coordinatorList = new ArrayList<>();
        userAdapter = new UserAdapter(coordinatorList, this::showEditCoordinatorDialog);
        recyclerUsers.setAdapter(userAdapter);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnAddCoordinator != null) btnAddCoordinator.setOnClickListener(v -> showAddCoordinatorDialog());

        loadAvailableClubs();
        loadCoordinators();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAvailableClubs();
        loadCoordinators();
    }

    private void loadAvailableClubs() {
        db.collection("clubs").get().addOnSuccessListener(snapshots -> {
            availableClubs.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                ClubModel club = doc.toObject(ClubModel.class);
                if (club == null) club = new ClubModel();
                club.setClubId(doc.getId());
                if (doc.contains("name")) club.setName(doc.getString("name"));
                availableClubs.add(club);
            }
        });
    }

    private void loadCoordinators() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("role", "COORDINATOR")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    coordinatorList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user == null) user = new UserModel();

                        user.setUid(doc.getId());
                        if (doc.contains("name")) user.setName(doc.getString("name"));
                        if (doc.contains("email")) user.setEmail(doc.getString("email"));
                        user.setRole("COORDINATOR");
                        if (doc.contains("department")) user.setDepartment(doc.getString("department"));
                        if (doc.contains("clubId")) user.setClubId(doc.getString("clubId"));

                        coordinatorList.add(user);
                    }

                    userAdapter.notifyDataSetChanged();

                    if (coordinatorList.isEmpty() && emptyView != null) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else if (emptyView != null) {
                        emptyView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "Failed to load coordinators: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddCoordinatorDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_coordinator, null);
        EditText etName = dialogView.findViewById(R.id.etCoordName);
        Spinner spinnerClubs = dialogView.findViewById(R.id.spinnerClubs);
        EditText etDept = dialogView.findViewById(R.id.etCoordDepartment);
        EditText etPhone = dialogView.findViewById(R.id.etCoordPhone);
        EditText etEmail = dialogView.findViewById(R.id.etCoordEmail);
        EditText etPassword = dialogView.findViewById(R.id.etCoordPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etCoordConfirmPassword);

        // Populate clubs spinner
        List<String> clubDisplayNames = new ArrayList<>();
        if (availableClubs.isEmpty()) {
            clubDisplayNames.add("No Registered Clubs Found");
        } else {
            for (ClubModel c : availableClubs) {
                clubDisplayNames.add(c.getName() + " (" + c.getClubId() + ")");
            }
        }
        ArrayAdapter<String> clubSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clubDisplayNames);
        spinnerClubs.setAdapter(clubSpinnerAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Create Coordinator", null)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dInterface -> {
            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String name = etName != null ? etName.getText().toString().trim() : "";
                String dept = etDept != null ? etDept.getText().toString().trim() : "";
                String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
                String inputEmail = etEmail != null ? etEmail.getText().toString().trim() : "";
                String password = etPassword != null ? etPassword.getText().toString().trim() : "";
                String confirmPass = etConfirmPassword != null ? etConfirmPassword.getText().toString().trim() : "";

                // Get selected club ID
                String selectedClubId = "";
                if (!availableClubs.isEmpty() && spinnerClubs.getSelectedItemPosition() >= 0
                        && spinnerClubs.getSelectedItemPosition() < availableClubs.size()) {
                    selectedClubId = availableClubs.get(spinnerClubs.getSelectedItemPosition()).getClubId();
                }

                if (TextUtils.isEmpty(name)) {
                    if (etName != null) etName.setError("Full name is required");
                    return;
                }

                if (TextUtils.isEmpty(inputEmail)) {
                    if (etEmail != null) etEmail.setError("Email or username is required");
                    return;
                }

                String finalEmail = inputEmail;
                if (!finalEmail.contains("@")) {
                    finalEmail = finalEmail.toLowerCase().replace(" ", "_") + "@eventhive.ai";
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(finalEmail).matches()) {
                    if (etEmail != null) etEmail.setError("Invalid email format");
                    return;
                }

                if (TextUtils.isEmpty(password) || password.length() < 6) {
                    if (etPassword != null) etPassword.setError("Password must be at least 6 characters");
                    return;
                }

                if (!password.equals(confirmPass)) {
                    if (etConfirmPassword != null) etConfirmPassword.setError("Passwords do not match");
                    return;
                }

                dialog.dismiss();
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

                createCoordinatorAccountWithoutAdminSignOut(name, dept, selectedClubId, phone, finalEmail, password);
            });
        });

        dialog.show();
    }

    /**
     * Creates Coordinator using a secondary FirebaseApp to avoid signing out the current Admin session.
     * Fallback to direct Firestore entry if email already exists or secondary auth is restricted.
     */
    private void createCoordinatorAccountWithoutAdminSignOut(String name, String dept, String clubId, String phone, String email, String password) {
        FirebaseApp secondaryApp = null;
        try {
            secondaryApp = FirebaseApp.getInstance("AdminCoordinatorCreator");
        } catch (IllegalStateException e) {
            try {
                FirebaseOptions options = FirebaseApp.getInstance().getOptions();
                secondaryApp = FirebaseApp.initializeApp(this, options, "AdminCoordinatorCreator");
            } catch (Exception initEx) {
                // Ignore initialization error
            }
        }

        if (secondaryApp != null) {
            FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);
            final FirebaseApp finalApp = secondaryApp;
            secondaryAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser() != null ? authResult.getUser().getUid() : "coord_" + System.currentTimeMillis();
                        saveCoordinatorToFirestore(uid, name, dept, clubId, phone, email, secondaryAuth);
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            // The email is already registered in Firebase Auth, so link or update their role in Firestore
                            Toast.makeText(ManageUsersActivity.this, "Email already registered in Auth. Setting up Coordinator profile...", Toast.LENGTH_SHORT).show();
                            // Generate safe key or check existing document
                            db.collection("users").whereEqualTo("email", email).get()
                                    .addOnSuccessListener(docs -> {
                                        String uid = !docs.isEmpty() ? docs.getDocuments().get(0).getId() : "coord_" + System.currentTimeMillis();
                                        saveCoordinatorToFirestore(uid, name, dept, clubId, phone, email, secondaryAuth);
                                    })
                                    .addOnFailureListener(ex -> {
                                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                                        Toast.makeText(ManageUsersActivity.this, "Failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(ManageUsersActivity.this, "Account creation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // Fallback: save directly to Firestore
            String fallbackUid = "coord_" + System.currentTimeMillis();
            saveCoordinatorToFirestore(fallbackUid, name, dept, clubId, phone, email, null);
        }
    }

    private void saveCoordinatorToFirestore(String uid, String name, String dept, String clubId, String phone, String email, FirebaseAuth secondaryAuth) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("role", "COORDINATOR");
        userData.put("department", dept);
        userData.put("clubId", clubId);
        userData.put("phone", phone);
        userData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(unused -> {
                    if (!TextUtils.isEmpty(clubId)) {
                        db.collection("clubs").document(clubId).update("coordinatorId", uid);
                    }

                    if (secondaryAuth != null) {
                        try {
                            secondaryAuth.signOut();
                        } catch (Exception ignored) {}
                    }

                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "Coordinator " + name + " saved successfully!", Toast.LENGTH_LONG).show();
                    loadCoordinators();
                })
                .addOnFailureListener(e -> {
                    if (secondaryAuth != null) {
                        try {
                            secondaryAuth.signOut();
                        } catch (Exception ignored) {}
                    }
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "Firestore save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditCoordinatorDialog(UserModel user) {
        String uid = user.getUid();
        if (TextUtils.isEmpty(uid)) {
            Toast.makeText(this, "Error: Invalid Coordinator ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_coordinator, null);
        TextView tvEmail = dialogView.findViewById(R.id.tvEditCoordEmail);
        EditText etName = dialogView.findViewById(R.id.etEditCoordName);
        Spinner spinnerClubs = dialogView.findViewById(R.id.spinnerEditClubs);
        EditText etDept = dialogView.findViewById(R.id.etEditCoordDepartment);
        EditText etPhone = dialogView.findViewById(R.id.etEditCoordPhone);

        tvEmail.setText("Email: " + (user.getEmail() != null ? user.getEmail() : "N/A"));
        etName.setText(user.getName() != null ? user.getName() : "");
        etDept.setText(user.getDepartment() != null ? user.getDepartment() : "");

        // Setup club spinner with preselected current club
        List<String> clubDisplayNames = new ArrayList<>();
        int preselectedIndex = 0;
        if (availableClubs.isEmpty()) {
            clubDisplayNames.add("No Clubs Found");
        } else {
            for (int i = 0; i < availableClubs.size(); i++) {
                ClubModel c = availableClubs.get(i);
                clubDisplayNames.add(c.getName() + " (" + c.getClubId() + ")");
                if (user.getClubId() != null && (user.getClubId().equalsIgnoreCase(c.getClubId()) || user.getClubId().equalsIgnoreCase(c.getName()))) {
                    preselectedIndex = i;
                }
            }
        }
        ArrayAdapter<String> clubSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clubDisplayNames);
        spinnerClubs.setAdapter(clubSpinnerAdapter);
        if (!availableClubs.isEmpty()) {
            spinnerClubs.setSelection(preselectedIndex);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newDept = etDept.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();

            String newClubId = user.getClubId();
            if (!availableClubs.isEmpty() && spinnerClubs.getSelectedItemPosition() >= 0
                    && spinnerClubs.getSelectedItemPosition() < availableClubs.size()) {
                newClubId = availableClubs.get(spinnerClubs.getSelectedItemPosition()).getClubId();
            }

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);
            updates.put("department", newDept);
            if (!newPhone.isEmpty()) updates.put("phone", newPhone);
            updates.put("clubId", newClubId);

            final String finalClubId = newClubId;
            db.collection("users").document(uid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Also update club's coordinatorId if changed
                        if (!TextUtils.isEmpty(finalClubId)) {
                            db.collection("clubs").document(finalClubId).update("coordinatorId", uid);
                        }

                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Coordinator profile updated successfully!", Toast.LENGTH_SHORT).show();
                        loadCoordinators();
                    })
                    .addOnFailureListener(e -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Safe removal option
        builder.setNeutralButton("Remove Role", (dialog, which) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Coordinator Access?")
                    .setMessage("Are you sure you want to remove coordinator access for " + user.getName() + "? The associated club and events will be preserved.")
                    .setPositiveButton("Confirm Remove", (d2, w2) -> {
                        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                        db.collection("users").document(uid).update("role", "INACTIVE_COORDINATOR")
                                .addOnSuccessListener(v -> {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Coordinator role deactivated.", Toast.LENGTH_SHORT).show();
                                    loadCoordinators();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}