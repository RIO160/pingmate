package com.example.pingmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity{
    private EditText ResetEmail;
    private Button btnResetPassword;
    private TextView BackToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        ResetEmail = findViewById(R.id.ResetEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        BackToLogin = findViewById(R.id.BackToLogin);

        btnResetPassword.setOnClickListener(view -> resetPassword());
        BackToLogin.setOnClickListener(view -> {
            startActivity(new Intent(ResetPasswordActivity.this, MainActivity.class));
        });
    }

    private void resetPassword(){
        String email = ResetEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            ResetEmail.setError("email  is required");
            ResetEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            ResetEmail.setError("please provide a valid email");
            ResetEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ResetPasswordActivity.this, "Check your email to reset your password", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ResetPasswordActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }



}
