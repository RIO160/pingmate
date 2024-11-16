package com.example.pingmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<chatMessage> chatMessageList;
    private String currentUserId;

    public ChatAdapter(List<chatMessage> chatMessageList, String currentUserId) {
        this.chatMessageList = chatMessageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        chatMessage chatMessage = chatMessageList.get(position);

        if (chatMessage.getSenderId().equals(currentUserId)) {
            // Display the message on the right side
            holder.messageTextView.setText(chatMessage.getMessage());
            holder.messageTextView.setBackgroundResource(R.drawable.message_bubble_right);
        } else {
            // Display the message on the left side
            holder.messageTextView.setText(chatMessage.getMessage());
            holder.messageTextView.setBackgroundResource(R.drawable.message_bubble_left);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }
    }
}
