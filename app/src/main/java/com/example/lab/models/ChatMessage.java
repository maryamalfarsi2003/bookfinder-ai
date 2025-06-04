package com.example.lab.models; // Your models package

// Simple model class to represent a single chat message
public class ChatMessage {
    private String text;
    private boolean isUser; // True if message is from user, false if from AI

    // Required empty constructor for potential future use (e.g., with Firestore)
    public ChatMessage() {
    }

    public ChatMessage(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
}
