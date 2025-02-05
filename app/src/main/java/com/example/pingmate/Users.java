package com.example.pingmate;

public class Users implements UserAdapter.ChatItem {
    private String username;
    private String email;
    private String chatId;
    private String userId;
    private String userId2;
    private String id;
    private String matchedMessage;
    private String fcmToken;
    private String profileImageUrl;
    private String latestMessage;

    public Users() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Users(String username, String email, String userId, String chatId, String id, String matchedMessage, String latestMessage) {
        this.username = username;
        this.email = email;
        this.userId = userId;
        this.chatId = chatId;
        this.id = id;
        this.matchedMessage = matchedMessage;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId2() {
        return userId2;
    }

    public void setUserId2(String userId2) {
        this.userId2 = userId2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMatchedMessage() {
        return matchedMessage;
    }

    public void setMatchedMessage(String matchedMessage) {
        this.matchedMessage = matchedMessage;
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