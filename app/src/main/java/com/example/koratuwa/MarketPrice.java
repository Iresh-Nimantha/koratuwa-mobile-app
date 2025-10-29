package com.example.koratuwa;

public class MarketPrice {
    private String cropName;
    private String imageUrl;
    private double latestPrice;
    private double oldPrice;

    public MarketPrice(String cropName, String imageUrl, double latestPrice, double oldPrice) {
        this.cropName = cropName;
        this.imageUrl = imageUrl;
        this.latestPrice = latestPrice;
        this.oldPrice = oldPrice;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getLatestPrice() {
        return latestPrice;
    }

    public void setLatestPrice(double latestPrice) {
        this.latestPrice = latestPrice;
    }

    public double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(double oldPrice) {
        this.oldPrice = oldPrice;
    }
}
