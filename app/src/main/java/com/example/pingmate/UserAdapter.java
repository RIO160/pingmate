package com.example.pingmate;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<Users> userList;
    private OnUserClickListener onUserClickListener;

    private static final int TYPE_USER = 0;
    private static final int TYPE_GROUP_CHAT = 1;

    public UserAdapter(List<Users> userList, OnUserClickListener onUserClickListener) {
        this.userList = userList;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        Users user = userList.get(position);
        holder.username.setText(user.getUsername());
        holder.email.setText((user.getEmail()));

        //handlee click event
        holder.itemView.setOnClickListener(view -> onUserClickListener.onUserClick(user));

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public interface ChatItem {
    }


        public static class UserViewHolder extends RecyclerView.ViewHolder {
            TextView username;
            TextView email;
            ImageView profilePic;

            public UserViewHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.usernameTextView);
                email = itemView.findViewById(R.id.emailTextView);
                profilePic = itemView.findViewById(R.id.profilePic);
            }
        }
    public interface OnUserClickListener {
    void onUserClick(Users user);
    }
}
