package com.wook.web.lighten.aio_client.data;

public class RemoteChatData {
    private String name;
    private String message;
    private String time;

    public RemoteChatData(){}

    public RemoteChatData(String name, String message, String time) {
        this.name = name;
        this.message = message;
        this.time = time;
    }

    public String getName() {
        return name;
    }
    public String getMessage() {
        return message;
    }
    public String getTime() {
        return time;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setTime(String time) {
        this.time = time;
    }
}