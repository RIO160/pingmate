package com.example.pingmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>{


    private List<ChatMessage> chatMessages;
    private String currentUserId;


    public ChatAdapter(List<ChatMessage> chatMessages, String currentUserId){
        this.chatMessages = chatMessages;
        this.currentUserId = currentUserId;
    }


    @NonNull
    @Override
    public ChatAdapter.ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        holder.bind(chatMessage);

//        if (chatMessage.getSenderID().equals(currentUserId)) {
//            holder.messageTextView.setText(chatMessage.getMessage());
//        } else {
//            holder.messageTextView.setText(chatMessage.getMessage());
//        }

    }

    public int getItemCount(){
        return chatMessages.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;
        private TextView senderText;

        public ChatViewHolder(@NonNull View itemView){
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            senderText = itemView.findViewById(R.id.sender_text);
        }

        public void bind(ChatMessage message){
            messageTextView.setText(message.getMessage());
            if (message.getSenderID().equals(currentUserId)) {
                senderText.setText("You"); // Or your current user's name
            } else {
                senderText.setText("Other User"); // Or the other user's name
            }
        }



    }


}
