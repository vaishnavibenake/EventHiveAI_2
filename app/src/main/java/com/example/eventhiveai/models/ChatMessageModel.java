package com.example.eventhiveai.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessageModel {
    private CharSequence message;
    private boolean isUser;
    private long timestamp;

    public ChatMessageModel() {
        // Empty constructor
    }

    public ChatMessageModel(CharSequence message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
    }

    public CharSequence getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(CharSequence message) {
        this.message = message;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp > 0 ? timestamp : System.currentTimeMillis()));
    }
}
