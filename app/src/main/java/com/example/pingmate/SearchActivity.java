package com.example.pingmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
    private boolean matchFound = false;

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

    private void checkForMatch() {
        if (matchFound) return;

        String preferredGender = getSelectedGender();

        Query query = db.collection("users")
                .whereEqualTo("gender", preferredGender)
                .whereEqualTo("status", "Online")
                .whereEqualTo("searching", true);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> matchedUsers = queryDocumentSnapshots.getDocuments();

            // Remove current user manually
            matchedUsers.removeIf(user -> user.getId().equals(currentUserId));

            if (!matchedUsers.isEmpty()) {
                matchFound = true; // Set the flag to true
                searchProgress.setVisibility(View.GONE); // Stop the animation

                int randomIndex = new Random().nextInt(matchedUsers.size());
                DocumentSnapshot match = matchedUsers.get(randomIndex);
                String matchedUser = match.getString("username");
                String receiverId = match.getId();

                db.collection("users").document(currentUserId)
                        .update("matchMessage", "You got matched with " + matchedUser)
                        .addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
                            intent.putExtra("clickedUsername", matchedUser);
                            intent.putExtra("receiverId", receiverId);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> Toast.makeText(SearchActivity.this, "Error saving match message", Toast.LENGTH_SHORT).show());

                matchResult.setText("Matched with: " + matchedUser);
                matchResult.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(SearchActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

//    private void checkForMatch() {
//        if (matchFound) return;
//
//        String preferredGender = getSelectedGender();
//
//        Query query = db.collection("users")
//                .whereEqualTo("gender", preferredGender)
//                .whereEqualTo("status", "Online")
//                .whereEqualTo("searching", true);
//
//        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            List<DocumentSnapshot> matchedUsers = queryDocumentSnapshots.getDocuments();
//
//            // Remove current user manually
//            matchedUsers.removeIf(user -> user.getId().equals(currentUserId));
//
//            if (!matchedUsers.isEmpty()) {
//                matchFound = true; // Set the flag to true
//                searchProgress.setVisibility(View.GONE); // Stop the animation
//
//                int randomIndex = new Random().nextInt(matchedUsers.size());
//                DocumentSnapshot match = matchedUsers.get(randomIndex);
//                String matchedUser = match.getString("username");
//                String receiverId = match.getId();
//
//                db.collection("users").document(currentUserId)
//                        .update("matchMessage", "You got matched with " + matchedUser)
//                        .addOnSuccessListener(aVoid -> {
//                            Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
//                            intent.putExtra("clickedUsername", matchedUser);
//                            intent.putExtra("receiverId", receiverId);
//                            startActivity(intent);
//                        })
//                        .addOnFailureListener(e -> Toast.makeText(SearchActivity.this, "Error saving match message", Toast.LENGTH_SHORT).show());
//
//                matchResult.setText("Matched with: " + matchedUser);
//                matchResult.setVisibility(View.VISIBLE);
//            }
//        }).addOnFailureListener(e -> {
//            Toast.makeText(SearchActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }

    private void searchForMatch() {
        searchProgress.setVisibility(View.VISIBLE);
        matchResult.setVisibility(View.GONE);
        matchFound = false; // Reset the flag

        Handler handler = new Handler();
        Runnable checkMatchTask = new Runnable() {
            @Override
            public void run() {
                checkForMatch();
            }
        };

        // Schedule the checkMatchTask to run every 2 seconds for 10 seconds
        for (int i = 0; i < 5; i++) {
            handler.postDelayed(checkMatchTask, i * 1000);
        }

        // Stop searching after 10 seconds
        handler.postDelayed(() -> {
            if (!matchFound) {
                searchProgress.setVisibility(View.GONE);
                matchResult.setText("No match found");
                matchResult.setVisibility(View.VISIBLE);
                db.collection("users").document(currentUserId).update("searching", false)
                        .addOnSuccessListener(aVoid -> Log.d("SearchActivity", "User is no longer searching"))
                        .addOnFailureListener(e -> Log.e("SearchActivity", "Error updating user status"));
            }
        }, 10000);
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        // set searching to false when leaving the activity
        if (currentUserId != null){
            db.collection("users").document(currentUserId).update("searching", false);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUserId != null) {
            db.collection("users").document(currentUserId).update("searching", false);
        }
    }

}