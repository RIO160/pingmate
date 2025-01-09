package com.example.pingmate;

public class chatMessage {
    private String message;
    private String senderId;
    private String username;
    private long timestamp;

    public chatMessage(){

    }

    public chatMessage(String message, String senderId, String username, long timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.username = username;
        this.timestamp = timestamp;
    }

    // Getters and setters for each field
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
}
