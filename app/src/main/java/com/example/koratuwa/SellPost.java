package com.example.koratuwa;

public class SellPost {
    private String cropType;
    private int quantity;
    private String harvestedDate;
    private String description;
    private String status;
    private long timestamp;

    public SellPost() {
        // Empty constructor needed for Firestore
    }

    // Getters and setters

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getHarvestedDate() {
        return harvestedDate;
    }

    public void setHarvestedDate(String harvestedDate) {
        this.harvestedDate = harvestedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status == null ? "pending" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
