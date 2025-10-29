package com.example.koratuwa;

public class Officer {
    private String name;
    private String state;
    private String phoneNumber;
    private String profileImageUrl;

    public Officer() {}

    public Officer(String name, String state, String phoneNumber, String profileImageUrl) {
        this.name = name;
        this.state = state;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() { return name; }
    public String getState() { return state; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getProfileImageUrl() { return profileImageUrl; }
}
