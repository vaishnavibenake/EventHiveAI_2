package com.example.eventhiveai.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class NotificationModel {
    private String notificationId;
    private String title;
    private String message;
    private String recipientRole; // "ALL", "ADMIN", "COORDINATOR", "STUDENT"
    private String recipientId;

    @ServerTimestamp
    private Date timestamp;

    public NotificationModel() {
        // Required for Firestore
    }

    public NotificationModel(String notificationId, String title, String message,
                             String recipientRole, String recipientId) {
        this.notificationId = notificationId;
        this.title = title;
        this.message = message;
        this.recipientRole = recipientRole;
        this.recipientId = recipientId;
    }

    public String getNotificationId() {
        return notificationId != null ? notificationId : "";
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipientRole() {
        return recipientRole != null ? recipientRole : "ALL";
    }

    public void setRecipientRole(String recipientRole) {
        this.recipientRole = recipientRole;
    }

    public String getRecipientId() {
        return recipientId != null ? recipientId : "";
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
