package com.example.pingmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class homepage extends AppCompatActivity implements UserAdapter.OnUserClickListener{
    private FirebaseAuth firebaseAuth;
    private TextView Signout;
    private TextView usernameTextView;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List <Users> userList;
    private static String chatId;
    private String otherUserId;
    private String currentUserId;

    @Override
    public void onUserClick(Users user) {
//        otherUserId = user.getUsername();
//        currentUserId = firebaseAuth.getCurrentUser().getUid();
//        chatId = generateOrFetchChatId(currentUserId, otherUserId);
        // Check if chatId is valid
        if (chatId != null && !chatId.isEmpty()) {
            // Pass chatId and otherUserId to the ChatPage activity
            Intent intent = new Intent(homepage.this, ChatPage.class);
            intent.putExtra("chatId", user.getUsername());
            //intent.putExtra("otherUserId", user.getUserId2());
            startActivity(intent);
        } else {
            Toast.makeText(homepage.this, "Error generating chat ID", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        Signout = findViewById(R.id.Signout);
        usernameTextView = findViewById(R.id.usernameTextView);
        userRecyclerView = findViewById(R.id.userRecyclerView);

        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList,  this);
        userRecyclerView.setAdapter(userAdapter);

        fetchUsername();
        fetchUsers();

        Signout.setOnClickListener(v -> signOut()); // Call the signOut method here
    }


    private void fetchUsername() {
        //FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String uid = firebaseAuth.getCurrentUser().getUid();

        // Fetch the user document from Firestore
        db.collection("users").document(uid)
                .get()
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

    private void fetchUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear(); // Clear the list before adding new data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Users user = document.toObject(Users.class);
                            userList.add(user); // Add each user to the list
                        }
                        userAdapter.notifyDataSetChanged(); // Notify the adapter of the changes
                    } else {
                        Log.w("Firestore", "Error getting users.", task.getException());
                    }
                });
    }

    // New method to generate or fetch a chat ID
    private String generateOrFetchChatId(String currentUserId, String otherUserId) {
        // Implement your logic to either generate a new chat ID or fetch an existing one
        // For example, you might query a 'chats' collection in Firestore to see if there's already a chat between these users
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String chatId = null;

        // Placeholder logic: Generate chatId based on the current and other user IDs
        // You should replace this with Firestore lookup logic if needed
        chatId = currentUserId + "_" + otherUserId; // This is just an example format

        // Optionally, you can add logic to query the 'chats' collection in Firestore to fetch an existing chatId

        return chatId; // Return the chatId (either generated or fetched)
    }


    private void signOut() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if the user is signed in
        if (firebaseAuth.getCurrentUser() != null) {
            String uid = firebaseAuth.getCurrentUser().getUid();

            // Remove the token from Firestore
            db.collection("users").document(uid)
                    .update("fcmToken", FieldValue.delete())
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Token removed successfully"))
                    .addOnFailureListener(e -> Log.w("Firestore", "Error removing token", e));
        }
        // Sign the user out
        firebaseAuth.signOut();

        // Optionally, you can also revoke the FCM token here if needed
        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Token deleted successfully");
                    } else {
                        Log.w("FCM", "Failed to delete FCM token", task.getException());
                    }
                });

        // Show a sign-out confirmation message
        Toast.makeText(homepage.this, "You have been signed out", Toast.LENGTH_SHORT).show();

        // Redirect to the login screen (or any other appropriate screen)
        Intent intent = new Intent(homepage.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish the current activity so that the user can't go back to it
        finish();
    }
}
