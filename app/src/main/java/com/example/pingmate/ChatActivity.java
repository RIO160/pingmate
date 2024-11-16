package com.example.pingmate;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<chatMessage> chatMessageList;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageView backButton;
    private TextView chatUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get the username passed from the homepage activity
        String username = getIntent().getStringExtra("clickedUsername");

        // Set the username at the top of the screen
        TextView usernameTextView = findViewById(R.id.chatUsername);
        usernameTextView.setText(username);


        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        chatUsername = findViewById(R.id.chatUsername);

        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList, firebaseAuth.getCurrentUser().getUid());
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Load chat messages from Firestore
        loadChatMessages();

        // Send message on button click
        sendButton.setOnClickListener(v -> sendMessage());

        // Back button functionality
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Go back to homepage
    }

    private void loadChatMessages() {
        firestore.collection("chats")
                .orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DocumentChange doc : value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                chatMessage chatMessage = doc.getDocument().toObject(chatMessage.class);
                                chatMessageList.add(chatMessage);
                                chatAdapter.notifyDataSetChanged();
                                chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
                            }
                        }
                    }
                });
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();

        if (message.isEmpty()) {
            return;
        }

        chatMessage chatMessage = new chatMessage(
                message,
                firebaseAuth.getCurrentUser().getUid(),
                System.currentTimeMillis()
        );

        firestore.collection("chats").add(chatMessage)
                .addOnSuccessListener(aVoid -> messageEditText.setText(""))
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show());
    }
}
