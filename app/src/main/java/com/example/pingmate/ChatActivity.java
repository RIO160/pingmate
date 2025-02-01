package com.example.pingmate;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<chatMessage> chatMessageList;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageView backButton, profileImageView;
    private TextView chatUsername;
    private TextView StatusUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        profileImageView = findViewById(R.id.profileImageView);

        // Get the username and receiverId passed from the previous activity
        String username = getIntent().getStringExtra("clickedUsername");
        String receiverId = getIntent().getStringExtra("receiverId");
        loadReceiverProfileImage(receiverId);

        // Ensure receiverId is not null
        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Error: Receiver ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the username at the top of the screen
        TextView usernameTextView = findViewById(R.id.chatUsername);
        usernameTextView.setText(username);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();



        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);


        // Initialize the chat messages list and adapter
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList, firebaseAuth.getCurrentUser().getUid(), receiverId, this);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Load chat messages from Firestore
        loadChatMessages(receiverId);

        fetchUserStatus();

        // Send message on button click
        sendButton.setOnClickListener(v -> sendMessage(receiverId));

        // Back button functionality
        backButton.setOnClickListener(v -> finish()); // Go back to the previous activity
    }

    private void fetchUserStatus() {
        String uid = getIntent().getStringExtra("receiverId");
        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        String fcmToken = documentSnapshot.getString("fcmToken");

                        if (fcmToken == null || fcmToken.isEmpty()) {
                            status = "Offline";
                        }

                        ImageView statusCircle = findViewById(R.id.statusCircle);

                        // Get the current status color
                        int statusColor = Color.GRAY; // Default to gray (Offline)

                        if ("Online".equals(status)) {
                            statusColor = Color.GREEN;
                        } else if ("Offline".equals(status)) {
                            statusColor = Color.GRAY;
                        } else if ("Busy".equals(status)) {
                            statusColor = Color.YELLOW;
                        } else if ("Do Not Disturb".equals(status)) {
                            statusColor = Color.RED;
                        }

                        // Set the status color to the statusCircle drawable
                        Drawable statusDrawable = getResources().getDrawable(R.drawable.status_circle, null);
                        if (statusDrawable != null) {
                            if (statusDrawable instanceof GradientDrawable) {
                                // Set the new color to the circle drawable
                                ((GradientDrawable) statusDrawable).setColor(statusColor);
                            }
                        }
                        statusCircle.setImageDrawable(statusDrawable);

                    } else {
                        Toast.makeText(ChatActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show());
    }


    private void loadReceiverProfileImage(String receiverId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Ensure Firestore is initialized
        if (db == null) {
            Log.e("ChatActivity", "Firestore is NULL!");
            return;
        }

        db.collection("users").document(receiverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String receiverProfileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (receiverProfileImageUrl != null && !receiverProfileImageUrl.isEmpty()) {
                            Glide.with(this).load(receiverProfileImageUrl)
                                    .placeholder(R.drawable.image_user2)
                                    .circleCrop()
                                    .into(profileImageView);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatActivity", "Failed to load profile image", e));
    }


    private void loadChatMessages(String receiverId) {
        String senderId = firebaseAuth.getCurrentUser().getUid();

        // Generate a consistent chatId based on the senderId and receiverId
        String chatId = senderId.compareTo(receiverId) < 0 ? senderId + "_" + receiverId : receiverId + "_" + senderId;

        firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error loading messages", error);
                        return;
                    }

                    if (value != null) {
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    chatMessage chatMessage = doc.getDocument().toObject(chatMessage.class);
                                    chatMessageList.add(chatMessage);
                                    chatAdapter.notifyDataSetChanged();
                                    chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
                                } catch (Exception e) {
                                    Log.e("Firestore", "Error parsing message document", e);
                                }
                            }
                        }
                    }
                });
    }

    private void sendMessage(String receiverId) {
        String message = messageEditText.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderId = firebaseAuth.getCurrentUser().getUid();

        // Generate a unique chatId
        String chatId = senderId.compareTo(receiverId) < 0 ? senderId + "_" + receiverId : receiverId + "_" + senderId;

        // Create a chatMessage object
        chatMessage chatMessage = new chatMessage(
                message,
                senderId,
                receiverId,
                System.currentTimeMillis()
        );

        // Add the message to Firestore
        firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(chatMessage)
                .addOnSuccessListener(aVoid -> {
                    messageEditText.setText(""); // Clear the message input field
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error sending message", e);
                });
    }
}