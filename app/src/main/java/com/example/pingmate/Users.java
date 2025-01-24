package com.example.pingmate;

public class Users implements UserAdapter.ChatItem {
    private String username;
    private String email;
    private String chatId;
    private String userId;
    private String userId2;
    private String id;
    private String fcmToken;
    private String profileImageUrl;

    public Users() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Users(String username, String email, String userId, String chatId, String id) {
        this.username = username;
        this.email = email;
        this.userId = userId;
        this.chatId = chatId;
        this.userId2 = userId2;
        this.id = id;
    }

    public String getName() {
        return getUsername();
    }
    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email){
        this.email = email;
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

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
