package com.example.pingmate;

public class Users {
    private String username;
    private String email;
    private String chatId;
    private String userId;
    private String userId2;

    public Users() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Users(String username, String email, String userId, String chatId) {
        this.username = username;
        this.email = email;
        this.userId = userId;
        this.chatId = chatId;
        this.userId2 = userId2;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserId2(){
        return userId2;
    }


    public String getChatId() {
        return chatId;
    }


}
