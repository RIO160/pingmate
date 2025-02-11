package com.example.pingmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import java.util.HashMap;
import java.util.Map;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private EditText SignupFirstName, SignupLastName, SignupMiddleName, SignupUsername, SignupEmail, SignupPassword, SignupRe_Password, DateOfBirth;
    private Spinner  SignupGender;
    private Button btnSignUp, btnBack;
    private FirebaseAuth mAtuh;
    private FirebaseFirestore Firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((R.layout.activity_sign_up));

        mAtuh = FirebaseAuth.getInstance();
        Firestore = FirebaseFirestore.getInstance();

        SignupFirstName = findViewById(R.id.SignupFirstName);
        SignupLastName = findViewById(R.id.SignupLastName);
        SignupMiddleName = findViewById(R.id.SignupMiddleName);
        SignupUsername = findViewById(R.id.SignupUsername);
        SignupEmail = findViewById(R.id.SignupEmail);
        SignupPassword = findViewById(R.id.SignupPassword);
        SignupRe_Password = findViewById(R.id.SignupRe_Password);
        SignupGender = findViewById(R.id.SignupGender);
        DateOfBirth = findViewById(R.id.DateOfBirth);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnBack = findViewById(R.id.btnBack);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item, // Create this layout
                new String[]{"Select Gender", "Male", "Female", "Others"}
        );


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SignupGender.setAdapter(adapter);

        btnSignUp.setOnClickListener(view -> registeruser());
        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        });
    }



    private void registeruser() {
        String Fn = SignupFirstName.getText().toString().trim();
        String Ln = SignupLastName.getText().toString().trim();
        String Mn = SignupMiddleName.getText().toString().trim();
        String username = SignupUsername.getText().toString().trim();
        String email = SignupEmail.getText().toString().trim();
        String password = SignupPassword.getText().toString().trim();
        String Repass = SignupRe_Password.getText().toString().trim();
        String DoB = DateOfBirth.getText().toString().trim();
        String Gender = SignupGender.getSelectedItem().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");


        // Validation checks for user inputs
        if (TextUtils.isEmpty(Fn)) {
            SignupFirstName.setError("You must enter your first name!");
            SignupFirstName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(Ln)) {
            SignupLastName.setError("You must enter your last name!");
            SignupLastName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(Mn)) {
            SignupMiddleName.setError("You must enter your middle name!");
            SignupMiddleName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            SignupUsername.setError("You must enter your desired username");
            SignupUsername.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(email)) {
            SignupEmail.setError("Email is required");
            SignupEmail.requestFocus();
            return;
        }

        if (SignupGender.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            SignupGender.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            SignupEmail.setError("Please provide a valid email");
            SignupEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            SignupPassword.setError("Password is required");
            SignupPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {
            SignupPassword.setError("Password must be at least 8 characters");
            SignupPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(Repass)) {
            SignupRe_Password.setError("Confirm your password");
            SignupRe_Password.requestFocus();
            return;
        }

        if (!Repass.equals(password)) {
            SignupRe_Password.setError("Passwords do not match!");
            SignupRe_Password.requestFocus();
            return;
        }

        try {
            Date dateOfBirth = dateFormat.parse(DoB);
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dateOfBirth);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            if (age < 16) {
                DateOfBirth.setError("Your age does not meet our policy");
                DateOfBirth.requestFocus();
                return;
            }

        } catch (ParseException e) {
            DateOfBirth.setError("Please enter a valid date of birth");
            DateOfBirth.requestFocus();
            return;
        }

        // Check if username exists in Firestore
        Firestore.collection("users").whereEqualTo("username", username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        SignupUsername.setError("Username already taken. Please choose another.");
                        SignupUsername.requestFocus();
                    } else {
                        // Check if email already exists in Firebase Auth
                        mAtuh.fetchSignInMethodsForEmail(email)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful() && !Objects.requireNonNull(task1.getResult().getSignInMethods()).isEmpty()) {
                                        SignupEmail.setError("Email already in use. Please choose another.");
                                        SignupEmail.requestFocus();
                                    } else {
                                        createUserInFirebaseAuth(email, password, Fn, Ln, Mn, username, DoB, Gender);
                                    }
                                });
                    }
                });
    }

    private void createUserInFirebaseAuth(String email, String password, String Fn, String Ln, String Mn, String username, String DoB, String Gender) {
        mAtuh.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        FirebaseUser firebaseUser = mAtuh.getCurrentUser();

                        if (firebaseUser != null) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", firebaseUser.getUid());
                            userMap.put("first name", Fn);
                            userMap.put("last name", Ln);
                            userMap.put("middle name", Mn);
                            userMap.put("username", username);
                            userMap.put("email", email);
                            userMap.put("Birth day", DoB);
                            userMap.put("gender", Gender);



                            Firestore.collection("users").document(firebaseUser.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUpActivity.this, "User signed up successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignUpActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                        Log.e("Firestore", "Error saving user data", e);
                                    });
                        }
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
