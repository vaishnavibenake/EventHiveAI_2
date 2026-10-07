package com.example.eventhiveai;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StudentSignupActivity extends AppCompatActivity {

    // ==========================================
    // INPUT FIELDS
    // ==========================================

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText rollNumberEditText;
    private EditText passwordEditText;

    // ==========================================
    // DEPARTMENT
    // ==========================================

    private Spinner departmentSpinner;

    // ==========================================
    // YEAR BUTTONS
    // ==========================================

    private Button year1Button;
    private Button year2Button;
    private Button year3Button;
    private Button year4Button;

    private String selectedYear = "";

    // ==========================================
    // OTHER BUTTONS
    // ==========================================

    private Button createAccountButton;
    private TextView signInText;

    // ==========================================
    // FIREBASE
    // ==========================================

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect Java with XML
        setContentView(R.layout.activity_student_signup);

        // ==========================================
        // INITIALIZE FIREBASE
        // ==========================================

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // ==========================================
        // CONNECT XML VIEWS
        // ==========================================

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        rollNumberEditText = findViewById(R.id.rollNumberEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        departmentSpinner = findViewById(R.id.departmentSpinner);

        year1Button = findViewById(R.id.year1Button);
        year2Button = findViewById(R.id.year2Button);
        year3Button = findViewById(R.id.year3Button);
        year4Button = findViewById(R.id.year4Button);

        createAccountButton = findViewById(R.id.createAccountButton);
        signInText = findViewById(R.id.signInText);

        // ==========================================
        // DEPARTMENT DROPDOWN
        // ==========================================

        setupDepartmentSpinner();

        // ==========================================
        // YEAR BUTTONS
        // ==========================================

        year1Button.setOnClickListener(v -> selectYear("1st"));
        year2Button.setOnClickListener(v -> selectYear("2nd"));
        year3Button.setOnClickListener(v -> selectYear("3rd"));
        year4Button.setOnClickListener(v -> selectYear("4th"));

        // ==========================================
        // CREATE ACCOUNT
        // ==========================================

        createAccountButton.setOnClickListener(v -> {
            createStudentAccount();
        });

        // ==========================================
        // SIGN IN
        // ==========================================

        signInText.setOnClickListener(v -> {

            Intent intent = new Intent(
                    StudentSignupActivity.this,
                    LoginActivity.class
            );

            intent.putExtra("USER_ROLE", "STUDENT");

            startActivity(intent);

            finish();
        });
    }


    // =================================================
    // DEPARTMENT SPINNER
    // =================================================

    private void setupDepartmentSpinner() {

        String[] departments = {
                "Select your department",
                "CSE",
                "CSE (AI & ML)",
                "Information Technology",
                "E&TC",
                "Electrical",
                "Mechanical",
                "Civil"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        departments
                ) {

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            android.view.ViewGroup parent) {

                        View view = super.getView(
                                position,
                                convertView,
                                parent
                        );

                        TextView textView = (TextView) view;

                        textView.setTextSize(12);

                        if (position == 0) {
                            textView.setTextColor(
                                    Color.rgb(100, 116, 139)
                            );
                        } else {
                            textView.setTextColor(Color.WHITE);
                        }

                        return view;
                    }
                };

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        departmentSpinner.setAdapter(adapter);
    }


    // =================================================
    // SELECT YEAR
    // =================================================

    private void selectYear(String year) {

        selectedYear = year;

        resetYearButtons();

        int selectedColor = Color.rgb(109, 40, 217);

        if (year.equals("1st")) {

            year1Button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            selectedColor
                    )
            );

            year1Button.setTextColor(Color.WHITE);

        } else if (year.equals("2nd")) {

            year2Button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            selectedColor
                    )
            );

            year2Button.setTextColor(Color.WHITE);

        } else if (year.equals("3rd")) {

            year3Button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            selectedColor
                    )
            );

            year3Button.setTextColor(Color.WHITE);

        } else if (year.equals("4th")) {

            year4Button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            selectedColor
                    )
            );

            year4Button.setTextColor(Color.WHITE);
        }
    }


    // =================================================
    // RESET YEAR BUTTONS
    // =================================================

    private void resetYearButtons() {

        int defaultColor =
                Color.rgb(23, 26, 39);

        int defaultTextColor =
                Color.rgb(148, 163, 184);

        year1Button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        defaultColor
                )
        );

        year2Button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        defaultColor
                )
        );

        year3Button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        defaultColor
                )
        );

        year4Button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        defaultColor
                )
        );

        year1Button.setTextColor(defaultTextColor);
        year2Button.setTextColor(defaultTextColor);
        year3Button.setTextColor(defaultTextColor);
        year4Button.setTextColor(defaultTextColor);
    }


    // =================================================
    // CREATE STUDENT ACCOUNT
    // =================================================

    private void createStudentAccount() {

        String name =
                nameEditText.getText()
                        .toString()
                        .trim();

        String email =
                emailEditText.getText()
                        .toString()
                        .trim();

        String rollNumber =
                rollNumberEditText.getText()
                        .toString()
                        .trim();

        String password =
                passwordEditText.getText()
                        .toString()
                        .trim();

        String department =
                departmentSpinner
                        .getSelectedItem()
                        .toString();


        // ==========================================
        // VALIDATE NAME
        // ==========================================

        if (TextUtils.isEmpty(name)) {

            nameEditText.setError(
                    "Please enter your full name"
            );

            nameEditText.requestFocus();

            return;
        }


        // ==========================================
        // VALIDATE EMAIL
        // ==========================================

        if (TextUtils.isEmpty(email)) {

            emailEditText.setError(
                    "Please enter your college email"
            );

            emailEditText.requestFocus();

            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            emailEditText.setError(
                    "Enter a valid email address"
            );

            emailEditText.requestFocus();

            return;
        }


        // ==========================================
        // VALIDATE ROLL NUMBER
        // ==========================================

        if (TextUtils.isEmpty(rollNumber)) {

            rollNumberEditText.setError(
                    "Please enter your roll number"
            );

            rollNumberEditText.requestFocus();

            return;
        }


        // ==========================================
        // VALIDATE DEPARTMENT
        // ==========================================

        if (department.equals("Select your department")) {

            Toast.makeText(
                    this,
                    "Please select your department",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ==========================================
        // VALIDATE YEAR
        // ==========================================

        if (TextUtils.isEmpty(selectedYear)) {

            Toast.makeText(
                    this,
                    "Please select your year of study",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ==========================================
        // VALIDATE PASSWORD
        // ==========================================

        if (TextUtils.isEmpty(password)) {

            passwordEditText.setError(
                    "Please create a password"
            );

            passwordEditText.requestFocus();

            return;
        }

        if (password.length() < 6) {

            passwordEditText.setError(
                    "Password must be at least 6 characters"
            );

            passwordEditText.requestFocus();

            return;
        }


        // ==========================================
        // DISABLE BUTTON
        // ==========================================

        createAccountButton.setEnabled(false);

        createAccountButton.setText(
                "Creating Account..."
        );


        // ==========================================
        // CREATE FIREBASE ACCOUNT
        // ==========================================

        firebaseAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {

                        createAccountButton.setEnabled(true);

                        createAccountButton.setText(
                                "Create Account →"
                        );

                        String errorMessage =
                                "Unable to create account.";

                        if (task.getException() != null) {

                            errorMessage =
                                    task.getException()
                                            .getMessage();
                        }

                        Toast.makeText(
                                StudentSignupActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }


                    // ==================================
                    // GET USER ID
                    // ==================================

                    if (firebaseAuth.getCurrentUser() == null) {

                        createAccountButton.setEnabled(true);

                        createAccountButton.setText(
                                "Create Account →"
                        );

                        Toast.makeText(
                                this,
                                "Account created but user information was unavailable.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }


                    String userId =
                            firebaseAuth
                                    .getCurrentUser()
                                    .getUid();


                    // ==================================
                    // CREATE STUDENT PROFILE
                    // ==================================

                    Map<String, Object> student =
                            new HashMap<>();

                    student.put(
                            "name",
                            name
                    );

                    student.put(
                            "email",
                            email
                    );

                    student.put(
                            "rollNumber",
                            rollNumber
                    );

                    student.put(
                            "department",
                            department
                    );

                    student.put(
                            "year",
                            selectedYear
                    );

                    student.put(
                            "role",
                            "STUDENT"
                    );

                    student.put(
                            "createdAt",
                            com.google.firebase.firestore.FieldValue
                                    .serverTimestamp()
                    );


                    // ==================================
                    // SAVE PROFILE TO FIRESTORE
                    // ==================================

                    firestore
                            .collection("users")
                            .document(userId)
                            .set(student)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        StudentSignupActivity.this,
                                        "Account created successfully!",
                                        Toast.LENGTH_LONG
                                ).show();


                                // ==================================
                                // GO TO STUDENT LOGIN
                                // ==================================

                                Intent intent =
                                        new Intent(
                                                StudentSignupActivity.this,
                                                LoginActivity.class
                                        );

                                intent.putExtra(
                                        "USER_ROLE",
                                        "STUDENT"
                                );

                                startActivity(intent);

                                finish();
                            })
                            .addOnFailureListener(e -> {

                                createAccountButton.setEnabled(true);

                                createAccountButton.setText(
                                        "Create Account →"
                                );

                                Toast.makeText(
                                        StudentSignupActivity.this,
                                        "Account created, but profile could not be saved.",
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                });
    }
}