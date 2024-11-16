package com.example.pingmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GroupCreationActivity extends AppCompatActivity {
    private EditText groupNameEditText;
    private Button createGroupBtn;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);

        groupNameEditText = findViewById(R.id.groupNameEditText);
        createGroupBtn = findViewById(R.id.createGroupBtn);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        createGroupBtn.setOnClickListener(v -> createGroupChat());
    }

    private void createGroupChat() {
        String groupName = groupNameEditText.getText().toString();
        String currentUserId = mAuth.getCurrentUser().getUid();

        if (TextUtils.isEmpty(groupName)) {
            groupNameEditText.setError("Group name is required");
            return;
        }

        // Create a new group chat document in Firestore
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("groupName", groupName);
        groupData.put("members", Collections.singletonList(currentUserId)); // Start with the current user

        db.collection("groups").add(groupData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(GroupCreationActivity.this, "Group created successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to homepage
                })
                .addOnFailureListener(e -> Toast.makeText(GroupCreationActivity.this, "Failed to create group", Toast.LENGTH_SHORT).show());
    }
}
