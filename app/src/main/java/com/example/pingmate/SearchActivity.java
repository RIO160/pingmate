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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Random;

public class SearchActivity extends AppCompatActivity {
    private Button searchBtn, BackBtn;
    private ProgressBar searchProgress;
    private TextView matchResult;
    private RadioGroup genderRadioGroup;
    private FirebaseFirestore db;
    private String currentUserId;

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
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        BackBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SearchActivity.this, homepage.class);
            startActivity(intent);
        });

        searchBtn.setOnClickListener(v -> startSearching());
    }

    private void startSearching() {
        searchProgress.setVisibility(View.VISIBLE);
        matchResult.setVisibility(View.GONE);

        // Set the current user to "searching" in Firestore
        DocumentReference userRef = db.collection("users").document(currentUserId);
        userRef.update("searching", true)
                .addOnSuccessListener(aVoid -> searchForMatch())
                .addOnFailureListener(e -> {
                    searchProgress.setVisibility(View.GONE);
                    Toast.makeText(SearchActivity.this, "Error updating search status", Toast.LENGTH_SHORT).show();
                });
    }

    private void searchForMatch() {
        searchProgress.setVisibility(View.VISIBLE);
        matchResult.setVisibility(View.GONE);

        String preferredGender = getSelectedGender();

        Query query = db.collection("users")
                .whereEqualTo("gender", preferredGender)
                .whereEqualTo("status", "Online")
                .whereEqualTo("searching", true);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            searchProgress.setVisibility(View.GONE);
            List<DocumentSnapshot> matchedUsers = queryDocumentSnapshots.getDocuments();

            // Remove current user manually
            matchedUsers.removeIf(user -> user.getId().equals(currentUserId));

            if (!matchedUsers.isEmpty()) {
                int randomIndex = new Random().nextInt(matchedUsers.size());
                DocumentSnapshot match = matchedUsers.get(randomIndex);
                String matchedUser = match.getString("username");
                String receiverId = match.getId();

                db.collection("users").document(currentUserId)
                        .update("matchMessage", "You got matched with " + matchedUser)
                        .addOnSuccessListener(aVoid -> {
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
        }).addOnFailureListener(e -> {
            searchProgress.setVisibility(View.GONE);
            Toast.makeText(SearchActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


//    private void searchForMatch() {
//        searchProgress.setVisibility(View.VISIBLE); // Show animation
//        matchResult.setVisibility(View.GONE);
//
//        // Get the preferred gender
//        String preferredGender = getSelectedGender();
//
//        Query query = db.collection("users")
//                        .whereEqualTo("gender", preferredGender)
//                        .whereEqualTo("status", "Online")
//                        .whereEqualTo("searching", true)
//                        .whereNotEqualTo("id", currentUserId);
//
//        // Fetch users from Firebase based on gender preference
////        db.collection("users")
////                .whereEqualTo("gender", preferredGender)
////                .whereEqualTo("status", "Online")
////                .whereEqualTo("searching", true)
////                .whereNotEqualTo("id", currentUserId)
//
//                query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//                    searchProgress.setVisibility(View.GONE);
//
//                    List<DocumentSnapshot> matchedUsers = queryDocumentSnapshots.getDocuments();
//                    if (!matchedUsers.isEmpty()) {
//                        int randomIndex = new Random().nextInt(matchedUsers.size());
//                        DocumentSnapshot match = matchedUsers.get(randomIndex);
//                        String matchedUser = match.getString("username");
//                        String receiverId = match.getId();
//
//                        // Save the match message to Firestore
//                        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                        db.collection("users").document(currentUserId)
//                                .update("matchMessage", "You got matched with " + matchedUser)
//                                .addOnSuccessListener(aVoid -> {
//                                    // Start ChatActivity with matched user information
//                                    Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
//                                    intent.putExtra("matchedUser", matchedUser);
//                                    intent.putExtra("receiverId", receiverId);
//                                    startActivity(intent);
//                                })
//                                .addOnFailureListener(e -> Toast.makeText(SearchActivity.this, "Error saving match message", Toast.LENGTH_SHORT).show());
//
//                        matchResult.setText("Matched with: " + matchedUser);
//                        matchResult.setVisibility(View.VISIBLE);
//                    } else {
//                        matchResult.setText("No matches found.");
//                        matchResult.setVisibility(View.VISIBLE);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    searchProgress.setVisibility(View.GONE);
//                    Toast.makeText(SearchActivity.this, "Error finding match", Toast.LENGTH_SHORT).show();
//                });
//    }

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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        // set searching to false when leaving the activity
        if (currentUserId != null){
            db.collection("users").document(currentUserId).update("searching", false);
        }

    }
}