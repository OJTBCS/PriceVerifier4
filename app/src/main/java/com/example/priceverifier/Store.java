package com.example.priceverifier;

public class Store {private int id;
    private String key;
    private String description;

    public Store(int id, String key, String description){
        this.id = id;
        this.key = key;
        this.description = description;
    }

    public int getId(){
        return id;
    }

    public String getKey(){
        return key;
    }

    public String getDescription(){
        return description;
    }
}
