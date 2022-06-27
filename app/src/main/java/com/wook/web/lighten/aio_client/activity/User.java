package com.wook.web.lighten.aio_client.activity;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String device;
    private String userName;
    private String token;
    private String profile;

    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public User() { }

    public User(String device, String token, String userName, String profile) {
        this.device = device;
        this.token = token;
        this.userName = userName;
        this.profile = profile;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("device", device);
        result.put("token", token);
        result.put("userName", userName);
        result.put("profile", profile);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String message) {
        this.token = message;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

}


