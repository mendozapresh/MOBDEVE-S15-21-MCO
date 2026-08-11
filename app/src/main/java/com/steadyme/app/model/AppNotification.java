package com.steadyme.app.model;

import com.google.firebase.Timestamp;

public class AppNotification {
    private String id;
    private String title;
    private String message;
    private Timestamp createdAt;

    public AppNotification() {
    }

    public AppNotification(String title, String message) {
        this.title = title;
        this.message = message;
        this.createdAt = Timestamp.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
