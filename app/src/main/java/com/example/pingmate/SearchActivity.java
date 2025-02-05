package com.example.pingmate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
    private SoundPool soundPool;
    private int pingSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity); // Ensure your XML file is named correctly

        searchBtn = findViewById(R.id.searchBtn);
        BackBtn = findViewById(R.id.BackBtn);
        searchProgress = findViewById(R.id.searchProgress);
        matchResult = findViewById(R.id.matchResult);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        ImageView profileImage1 = findViewById(R.id.profileImage1);
        ImageView profileImage2 = findViewById(R.id.profileImage2);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        // Initialize soundPool first, then load the sound
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build();

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                pingSoundId = sampleId;
            }
        });

        pingSoundId = soundPool.load(this, R.raw.ping, 1);

        profileImage1.setVisibility(View.GONE);
        profileImage2.setVisibility(View.GONE);
        BackBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SearchActivity.this, homepage.class);
            startActivity(intent);
        });

        searchBtn.setOnClickListener(v -> startSearching());
    }

    private void playPingSound() {
        if (pingSoundId != 0) {
            soundPool.play(pingSoundId, 1f, 1f, 0, 0, 1f);
        }
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

                showMatchAnimation(receiverId, matchedUser);

                // Show matched user message
                matchResult.setText("Matched with: " + matchedUser);
                matchResult.setVisibility(View.VISIBLE);

                // Save match message in Firestore
                db.collection("users").document(currentUserId)
                        .update("matchMessage", "You got matched with " + matchedUser)
                        .addOnSuccessListener(aVoid -> {
                            // Add a 4-second delay before starting ChatActivity
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
                                intent.putExtra("clickedUsername", matchedUser);
                                intent.putExtra("receiverId", receiverId);
                            }, 7000);
                        })
                        .addOnFailureListener(e -> Toast.makeText(SearchActivity.this, "Error saving match message", Toast.LENGTH_SHORT).show());
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
    private void showMatchAnimation(String receiverId, String matchedUserName) {
        ImageView profileImage1 = findViewById(R.id.profileImage1);
        ImageView profileImage2 = findViewById(R.id.profileImage2);
        TextView matchText = findViewById(R.id.matchText);

        profileImage1.setVisibility(View.VISIBLE);
        profileImage2.setVisibility(View.VISIBLE);
        matchText.setVisibility(View.VISIBLE);


        // Fetch sender's profile image (current user)
        DocumentReference senderRef = db.collection("users").document(currentUserId);
        senderRef.get().addOnSuccessListener(senderSnapshot -> {
            String senderProfileUrl = senderSnapshot.getString("profileImageUrl"); // Fetch sender's profile

            // Fetch receiver's profile image
            DocumentReference receiverRef = db.collection("users").document(receiverId);
            receiverRef.get().addOnSuccessListener(receiverSnapshot -> {
                String receiverProfileUrl = receiverSnapshot.getString("profileImageUrl");

                Log.d("MatchDebug", "Sender Profile URL: " + senderProfileUrl);
                Log.d("MatchDebug", "Receiver Profile URL: " + receiverProfileUrl);

                // Load images with Glide
                if (senderProfileUrl != null && !senderProfileUrl.isEmpty()) {
                    Glide.with(this).load(senderProfileUrl).circleCrop().into(profileImage1);
                } else {
                    profileImage1.setImageResource(R.drawable.image_user2); // Set default if missing
                }

                if (receiverProfileUrl != null && !receiverProfileUrl.isEmpty()) {
                    Glide.with(this).load(receiverProfileUrl).circleCrop().into(profileImage2);
                } else {
                    profileImage2.setImageResource(R.drawable.image_user2);
                }

                // Start animation after images are loaded
                animateMatchFound(() -> {
                    Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
                    intent.putExtra("clickedUsername", matchedUserName);
                    intent.putExtra("receiverId", receiverId);
                    startActivity(intent);
                    finish();
                });

            }).addOnFailureListener(e -> Log.e("MatchDebug", "Error loading receiver profile", e));

        }).addOnFailureListener(e -> Log.e("MatchDebug", "Error loading sender profile", e));
    }


    // Modify animation to run callback after completion
    private void animateMatchFound(Runnable onComplete) {
        ImageView profileImage1 = findViewById(R.id.profileImage1);
        ImageView profileImage2 = findViewById(R.id.profileImage2);
        TextView matchText = findViewById(R.id.matchText);

        // Clear any previous animations
        profileImage1.clearAnimation();
        profileImage2.clearAnimation();
        matchText.clearAnimation();

        // Load sliding animations (e.g., from left and right)
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.profile_image_slide_in);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.profile_image_slide_in_right);

        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(profileImage1, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(profileImage1, "scaleY", 1f, 1.3f, 1f);
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(profileImage2, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(profileImage2, "scaleY", 1f, 1.3f, 1f);

        profileImage1.startAnimation(slideInLeft);
        profileImage2.startAnimation(slideInRight);

        playPingSound();

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX1, scaleY1, scaleX2, scaleY2);
        animatorSet.setDuration(5000);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
            }
        });

        animatorSet.start();
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
        soundPool.release();
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