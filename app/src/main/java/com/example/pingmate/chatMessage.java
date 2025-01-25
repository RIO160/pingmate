package com.example.pingmate;

import java.util.List;

public class chatMessage {
    private String message = "";
    private String senderId = "";
    private String username = "";
    private long timestamp = 0;
    private String receiverId;
    private boolean isRead;
    private String senderProfileImageUrl;
    private String receiverProfileImageUrl;

    public chatMessage(){

    }

    public chatMessage(String message, String senderId, String username, long timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.username = username;
        this.timestamp = timestamp;
        this.receiverId = receiverId;
    }

    // Getters and setters for each field
    public String getSenderProfileImageUrl() {
        return senderProfileImageUrl != null ? senderProfileImageUrl : "";
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getUsername() {
        return username;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getReceiverId() {return receiverId;}

    public String getReceiverProfileImageUrl() {
        return receiverProfileImageUrl;
    }
}
