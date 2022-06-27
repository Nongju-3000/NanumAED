package com.wook.web.lighten.aio_client.activity;

public class NameData {
    private String name;
    private String token;

    public NameData(){}

    public NameData(String name, String token) {
        this.name = name;
        this.token = token;
    }

    public String getName() {
        return name;
    }
    public String getToken() {return token;}
}
