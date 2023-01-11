package com.wook.web.lighten.aio_client.data;

public class RoomData {
    private String name;
    private int num_of_people;

    public RoomData(){}

    public RoomData(String name, int num_of_people) {
        this.name = name;

        this.num_of_people = num_of_people;
    }

    public String getName() {
        return name;
    }
    public int getNum_of_people() { return num_of_people; }

    public void setName(String name) {
        this.name = name;
    }
    public void setNum_of_people(int num_of_people) { this.num_of_people = num_of_people; }
}