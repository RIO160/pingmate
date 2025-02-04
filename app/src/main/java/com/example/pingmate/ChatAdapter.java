package com.example.pingmate;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<chatMessage> chatMessageList;
    private String currentUserId;
    private String receiverId;
    private String receiverProfileImageUrl;
    private Context context;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<chatMessage> chatMessageList, String currentUserId, String receiverId, Context context) {
        this.chatMessageList = chatMessageList;
        this.currentUserId = currentUserId;
        this.receiverId = receiverId;
        this.context = context;

        // Fetch the receiver's profile image from Firestore
        fetchReceiverProfileImage();
    }

    private void fetchReceiverProfileImage() {
        FirebaseFirestore.getInstance().collection("users").document(receiverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        receiverProfileImageUrl = documentSnapshot.getString("profileImageUrl");
                        notifyDataSetChanged(); // Refresh messages to apply the profile image
                    }
                });
    }

    @Override
    public int getItemViewType(int position) {
        chatMessage message = chatMessageList.get(position);
        return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        chatMessage chatMessage = chatMessageList.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(chatMessage);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(chatMessage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    public class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        public SentMessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        public void bind(chatMessage message) {
            messageTextView.setText(message.getMessage());
        }
    }

    public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;
        private ImageView receiverProfileImageView;

        public ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            receiverProfileImageView = itemView.findViewById(R.id.receiverProfileImageView); // Make sure this ID matches your XML layout
        }

        public void bind(chatMessage message) {
            messageTextView.setText(message.getMessage());

            Glide.with(itemView.getContext())
                    .load(receiverProfileImageUrl)
                    .placeholder(R.drawable.image_user2)
                    .circleCrop()
                    .error(R.drawable.image_user2)
                    .into(receiverProfileImageView);
        }
    }

}



