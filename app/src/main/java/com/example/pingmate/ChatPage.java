package com.example.pingmate;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.WorkSource;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatPage extends AppCompatActivity {

    private EditText message_input;
    private ImageView backBtn;
    private ImageButton send_button;
    private RecyclerView chat_recycler_view;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String chatId;
    private String otherUserId;
    private TextView name_user;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatpage);

        // Get the data passed from the UserAdapter
        Intent intent = getIntent();
        //homepage.onUserClick();
        chatId = intent.getStringExtra("chatId"); // The chat ID
        //otherUserId = intent.getStringExtra("otherUserId"); // The other user's ID

        Log.d("ChatPage", "otherUserId: " + otherUserId);

        // Check if chatId or otherUserId is null
        if (chatId == null || otherUserId == null) {
            Toast.makeText(this, "Chat ID or User ID is missing", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if the data is missing
            return;
        }

        message_input = findViewById(R.id.message_input);
        send_button = findViewById(R.id.send_button);
        chat_recycler_view = findViewById(R.id.chat_recycler_view);
        backBtn = findViewById(R.id.backBtn);
        name_user = findViewById(R.id.name_user);


        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUserId);
        chat_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        chat_recycler_view.setAdapter(chatAdapter);

        loadMessage();
        fetchOtherUserName();

        send_button.setOnClickListener(view -> sendMessage());

        backBtn.setOnClickListener(view -> {
            startActivity(new Intent(ChatPage.this, homepage.class));

        });

    }

    private void loadMessage(){
        CollectionReference chatref = db.collection("chats").document(chatId).collection("messages");

       chatref.orderBy("timestamp").addSnapshotListener((queryDocumentSnapshots, e) -> {
           if (e != null){
               Log.w("Chatpage", "listend failed", e);
               return;
           }

           if (queryDocumentSnapshots != null) {
               chatMessages.clear();

               for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                   ChatMessage chatMessage = document.toObject(ChatMessage.class);
                   chatMessages.add(chatMessage);
               }

               chatAdapter.notifyDataSetChanged();
               chat_recycler_view.scrollToPosition(chatMessages.size() - 1);
               //chat_recycler_view.setVisibility(View.VISIBLE);

           }

       });
    }

    private void fetchOtherUserName(){

        db.collection("users").document("Zvy1RhwNCnQdB6tdtND3uP47r782").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
                        String username = documentSnapshot.getString("username");
                        Log.d("ChatPage", "Fetched username: " + username);
                        name_user.setText(username);
                    }else {
                        Log.w("ChatPage", "User not found");
                    }
                })
                .addOnFailureListener(e -> Log.w("ChatPage", "Error getting other user's username", e));
    }

    private void sendMessage(){
        String message = message_input.getText().toString().trim();
        if (message.isEmpty()) return;

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderID", currentUserId);
        messageData.put("message", message);
        messageData.put("timestamp", System.currentTimeMillis());

        db.collection("chats").document(chatId).collection("messages").add(messageData)
                .addOnSuccessListener(documentReference -> message_input.setText(""))
                .addOnFailureListener(e -> Log.w("ChatActivity", "Error sending message", e));
    }


}
