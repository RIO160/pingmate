package com.example.pingmate;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GroupChatActivity extends AppCompatActivity {
    private String chatGroupName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        chatGroupName = getIntent().getStringExtra("groupName");

        TextView groupNameTextView = findViewById(R.id.chatGroupName);
        groupNameTextView.setText(chatGroupName);
    }
}
