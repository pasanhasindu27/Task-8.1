package com.example.tutorapp;

public class Message {
    private String text;
    private boolean isUser; // true if message is from user, false if from bot

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }
}