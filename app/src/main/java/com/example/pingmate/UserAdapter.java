package com.example.pingmate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<Users> userList;
    private Context context;
    private OnUserClickListener onUserClickListener;

    public UserAdapter(List<Users> userList,  UserAdapter.OnUserClickListener onUserClickListener) {
        this.userList = userList;
        this.context = context;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        Users user = userList.get(position);
        //holder.username.setText(user.getUsername());

        holder.itemView.setOnClickListener(v -> {
            // Open a chat with this user
            onUserClickListener.onUserClick(user);
//            Intent intent = new Intent(context, ChatPage.class);
//            intent.putExtra("userId", user.getUsername());
                
//            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username;

        public UserViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.usernameTextView);

        }
    }

    public interface OnUserClickListener {
        void onUserClick(Users user);
    }
}
