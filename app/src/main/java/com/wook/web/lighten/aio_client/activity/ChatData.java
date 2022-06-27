package com.wook.web.lighten.aio_client.activity;

public class ChatData {
    private String userName;
    private String message;
    private String postDate;

    public ChatData(){}

    public ChatData(String message, String postDate, String userName) {
        this.postDate = postDate;
        this.userName = userName;
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

