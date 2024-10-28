package com.example.pingmate;

public class ChatMessage {
    private String senderID;
    private String message;
    private long timestamp;

    public ChatMessage(){

    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID){
        this.senderID = senderID;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }
}
