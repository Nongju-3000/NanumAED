package com.wook.web.lighten.aio_client.data;

public class UserData {
    private String name;
    private String uuid;

    public UserData() {}

    public UserData(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

