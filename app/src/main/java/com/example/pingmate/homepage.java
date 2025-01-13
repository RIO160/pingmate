package com.example.pingmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pingmate.utils.FirebaseUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.pingmate.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class homepage extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private RecyclerView userReclycerView;
    private UserAdapter userAdapter;
    private List<Users> userList;
    private TextView Signout;
    private TextView usernameTextView;
    private Button fabNewchat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_chat);

        if (getIntent().getExtras() != null) {
            // From notification
            String userId = getIntent().getExtras().getString("userId");

            if (userId != null && !userId.isEmpty()) {
                // Fetch the user information and navigate to ChatActivity
                db.collection("users").document(userId).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                Users user = documentSnapshot.toObject(Users.class);

                                if (user != null) {
                                    Intent intent = new Intent(homepage.this, ChatActivity.class);
                                    intent.putExtra("clickedUsername", user.getUsername()); // Pass username
                                    intent.putExtra("receiverId", userId); // Pass userId to ChatActivity
                                    startActivity(intent);
                                    finish(); // Close the homepage activity if needed
                                } else {
                                    Log.w("Notification", "User object is null");
                                }
                            } else {
                                Log.w("Notification", "Failed to fetch user", task.getException());
                            }
                        });
            } else {
                Log.w("Notification", "userId is null or empty");
            }
        } else {
            initializeHomePage();
        }
    }

    private void initializeHomePage() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userReclycerView = findViewById(R.id.userRecyclerView);
        userReclycerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();

        userAdapter = new UserAdapter(userList, user -> {
            String clickedUsername = user.getUsername(); // Get the clicked user's username
            String receiverId = user.getId(); // Get the receiver's UID

            if (receiverId != null && !receiverId.isEmpty()) {
                Intent intent = new Intent(homepage.this, ChatActivity.class);
                intent.putExtra("clickedUsername", clickedUsername); // Pass username
                intent.putExtra("receiverId", receiverId); // Pass receiverId to ChatActivity
                startActivity(intent);
            } else {
                Toast.makeText(homepage.this, "Receiver ID is missing for this user", Toast.LENGTH_SHORT).show();
            }
        });

        userReclycerView.setAdapter(userAdapter);

        Signout = findViewById(R.id.Signout);
        usernameTextView = findViewById(R.id.usernameTextView);

        FloatingActionButton fabNewchat = findViewById(R.id.fabNewChat);
        fabNewchat.setOnClickListener(view -> {
            Intent intent = new Intent(homepage.this, GroupCreationActivity.class);
            startActivity(intent);
        });

        fetchUsername();
        fetchUsers();
        fetchGroupChats(db);

        Signout.setOnClickListener(v -> signOut());
        getFCMToken();
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                FirebaseUtil.currentUserDetails().update("fcmToken", token);
            }
        });
    }

    private void fetchUsername() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        usernameTextView.setText(username);
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Error retrieving username", e));
    }

    private void fetchUsers() {
        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Users> allUsers = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Users user = document.toObject(Users.class);

                        if (user != null) {
                            // Use Firestore's document ID as the user ID
                            user.setId(document.getId());

                            // Exclude the current logged-in user, with a null check
                            if (user.getId() != null && !user.getId().equals(currentUserId)) {
                                allUsers.add(user);
                            }
                        }
                    }
                    userList.clear();
                    userList.addAll(allUsers);
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching users", e));
    }

    private void fetchGroupChats(FirebaseFirestore db) {
        db.collection("groups")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> groupNames = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        String groupName = snapshot.getString("groupName");
                        groupNames.add(groupName);
                    }
                    // Call a method to display the group chats
                    displayGroupChats(groupNames);
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Error fetching groups", e));
    }

    private void displayGroupChats(List<String> groupNames) {
        ListView groupListview = findViewById(R.id.userRecyclerView); // Or another view for group chats
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupNames);
        groupListview.setAdapter(adapter);

        groupListview.setOnItemClickListener((parent, view, position, id) -> {
            String clickedGroup = groupNames.get(position);
            Intent intent = new Intent(homepage.this, GroupChatActivity.class);
            intent.putExtra("groupName", clickedGroup);
            startActivity(intent);
        });
    }

    private void signOut() {
        String uid = firebaseAuth.getCurrentUser().getUid();

        // Remove the FCM token from Firestore
        db.collection("users").document(uid).update("fcmToken", FieldValue.delete())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Token removed successfully"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error removing token", e));

        firebaseAuth.signOut();

        // Delete FCM token
        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Token deleted successfully");
                    } else {
                        Log.w("FCM", "Failed to delete FCM token", task.getException());
                    }
                });

        Toast.makeText(homepage.this, "You have been signed out", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(homepage.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
