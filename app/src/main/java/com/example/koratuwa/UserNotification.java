package com.example.koratuwa;

public class UserNotification {

    private String id;
    private String text;
    private String type;   // "post_action" or "alert"
    private String status; // "pending", "approved", "rejected"
    private long createdAt;

    public UserNotification() {}

    public UserNotification(String id, String text, String type, String status, long createdAt) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setType(String type) { this.type = type; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
