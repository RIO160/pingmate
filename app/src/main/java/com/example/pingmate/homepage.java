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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.firestore.QuerySnapshot;

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
//    private UserAdapter.OnUserClickListener onUserCLickListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userReclycerView = findViewById(R.id.userRecyclerView);
        userReclycerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();

        userAdapter = new UserAdapter(userList, user -> {
            // Handle the click event
            String clickedUsername = user.getUsername(); // Get the clicked user's username
            Intent intent = new Intent(homepage.this, ChatActivity.class);
            intent.putExtra("clickedUsername", clickedUsername); // Pass username to ChatActivity
            startActivity(intent);
        });

        userReclycerView.setAdapter(userAdapter);

        Signout = findViewById(R.id.Signout);
        usernameTextView = findViewById(R.id.usernameTextView); // Reference to the username TextView

        FloatingActionButton fabNewchat = findViewById(R.id.fabNewChat);
        fabNewchat.setOnClickListener(view -> {
            Intent intent = new Intent(homepage.this, GroupCreationActivity.class);
            startActivity(intent);
        });

        fetchUsername(); // Call this method to fetch and display username
        fetchUsers(); // cal this method to fetch and display username\
        fetchGroupChats(db);

        Signout.setOnClickListener(v -> signOut());
    }

    private void fetchUsername() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String uid = firebaseAuth.getCurrentUser().getUid();

        // Fetch the user document from Firestore
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the username from Firestore and set it to the TextView
                        String username = documentSnapshot.getString("username"); // Adjust the field name if necessary
                        usernameTextView.setText(username); // Display the username in the TextView
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error retrieving username", e);
                });
    }

    private void fetchUsers(){
        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        // Fetch all users from Firestore
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Users> allUsers = new ArrayList<>();

                    // Loop through all users in Firestore
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Users user = document.toObject(Users.class);

                        // Exclude the current logged-in user
                        if (!document.getId().equals(currentUserId)) {
                            allUsers.add(user);
                        }
                    }

                    // Update the RecyclerView with the list of other users
                    userList.clear();
                    userList.addAll(allUsers);
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching users", e));
    }

    private void fetchGroupChats(FirebaseFirestore db){
        db.collection("groups")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> groupNames = new ArrayList<>();
                    for(DocumentSnapshot snapshot : queryDocumentSnapshots){
                        String groupName = snapshot.getString("groupName");
                        groupNames.add(groupName);
                    }
                    displayGroupChats(groupNames);
                })
                .addOnFailureListener(e -> Log.w("Firestore", "error fetching groups"));
    }

    private void displayGroupChats(List<String> groupNames){
        ListView groupListview = findViewById(R.id.userRecyclerView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupNames);
        groupListview.setAdapter(adapter);

        groupListview.setOnItemClickListener((parent, view, position, id)->{
            String clickedGroup = groupNames.get(position);
            Intent intent = new Intent(homepage.this, GroupChatActivity.class);
            intent.putExtra("groupName", clickedGroup);
            startActivity(intent);
        });
    }

    private void signOut() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        String uid = firebaseAuth.getCurrentUser().getUid();

        // Remove the FCM token from Firestore
        db.collection("users").document(uid).update("fcmToken", FieldValue.delete())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Token removed successfully"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error removing token", e));

        firebaseAuth.signOut();

        // Optionally delete FCM token
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
