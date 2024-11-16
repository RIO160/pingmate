package com.example.pingmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity{
    private EditText Username, Password;
    private Button LoginBtn;
    private Button SignupBtn;
    private TextView ForgetPw;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Username = findViewById(R.id.Username);
        Password = findViewById(R.id.Password);
        LoginBtn = findViewById(R.id.LoginBtn);
        SignupBtn = findViewById(R.id.SignupBtn);
        ForgetPw = findViewById(R.id.ForgetPw);

        LoginBtn.setOnClickListener(v -> loginUser());
        SignupBtn.setOnClickListener(view -> {
            //navigate to sign-up activity
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        ForgetPw.setOnClickListener(view -> {
            // handle forget password logic
            startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
        });
    }

    private void loginUser(){
        String user = Username.getText().toString().trim();
        String password = Password.getText().toString().trim();

        if(TextUtils.isEmpty(user)){
            Username.setError("User name is Required");
            Username.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(user).matches()){
            Username.setError("Please enter your username");
            Username.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)){
            Password.setError("Invalid password");
            Password.requestFocus();
            return;
        }

        if (password.length() < 0){
            Password.setError("Password must be at least 8 characters");
            Password.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(user, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //sign in success, navigate to the main activity
                            Toast.makeText(MainActivity.this, "Login Successfull", Toast.LENGTH_SHORT).show();
                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(task1 -> {
                                        if (!task1.isSuccessful()) {
                                            Log.w("FCM", "Fetching FCM registration token failed", task1.getException());
                                            return;
                                        }

                                        // Get the new FCM registration token
                                        String token = task1.getResult();
                                        Log.d("FCM", "Generated Token: " + token);

                                        // Save the token in Firebase Firestore
                                        saveTokenToFirestore(token);
                                    });

                            startActivity(new Intent(MainActivity.this, homepage.class));
                            finish();
                        } else {
                            // if sign in fails, display a message to the user
                            Toast.makeText(MainActivity.this, "Login Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    }

    private void saveTokenToFirestore(String token) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getCurrentUser().getUid();

        // Create a Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map to store the token
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("fcmToken", token);

        // Save the token to Firestore under the user's document
        db.collection("users").document(uid)
                .set(tokenMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Token saved successfully"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error saving token", e));
    }
}