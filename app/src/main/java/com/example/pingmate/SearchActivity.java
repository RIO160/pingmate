package com.example.pingmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class SearchActivity extends AppCompatActivity {
    private Button searchBtn, BackBtn;
    private ProgressBar searchProgress;
    private TextView matchResult;
    private RadioGroup genderRadioGroup;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity); // Ensure your XML file is named correctly

        searchBtn = findViewById(R.id.searchBtn);
        BackBtn = findViewById(R.id.BackBtn);
        searchProgress = findViewById(R.id.searchProgress);
        matchResult = findViewById(R.id.matchResult);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        db = FirebaseFirestore.getInstance();

        BackBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SearchActivity.this, homepage.class);
            startActivity(intent);
        });

        searchBtn.setOnClickListener(v -> searchForMatch());
    }

    private void searchForMatch() {
        searchProgress.setVisibility(View.VISIBLE); // Show animation
        matchResult.setVisibility(View.GONE);

        // Get the preferred gender
        String preferredGender = getSelectedGender();

        // Fetch users from Firebase based on gender preference
        db.collection("users")
                .whereEqualTo("gender", preferredGender)
                .whereEqualTo("status", "Online")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchProgress.setVisibility(View.GONE);

                    if (!queryDocumentSnapshots.isEmpty()) {
                        int randomIndex = new Random().nextInt(queryDocumentSnapshots.size());
                        DocumentSnapshot match = queryDocumentSnapshots.getDocuments().get(randomIndex);
                        String matchedUser = match.getString("username");
                        String receiverId = match.getId();

                        // Save the match message to Firestore
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        db.collection("users").document(uid)
                                .update("matchMessage", "You got matched with " + matchedUser)
                                .addOnSuccessListener(aVoid -> {
                                    // Start ChatActivity with matched user information
                                    Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
                                    intent.putExtra("matchedUser", matchedUser);
                                    intent.putExtra("receiverId", receiverId);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> Toast.makeText(SearchActivity.this, "Error saving match message", Toast.LENGTH_SHORT).show());

                        matchResult.setText("Matched with: " + matchedUser);
                        matchResult.setVisibility(View.VISIBLE);
                    } else {
                        matchResult.setText("No matches found.");
                        matchResult.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    searchProgress.setVisibility(View.GONE);
                    Toast.makeText(SearchActivity.this, "Error finding match", Toast.LENGTH_SHORT).show();
                });
    }

    private String getSelectedGender() {
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioMale) {
            return "Male";
        } else if (selectedId == R.id.radioFemale) {
            return "Female";
        } else {
            return "Any"; // Fetch any gender
        }
    }
}