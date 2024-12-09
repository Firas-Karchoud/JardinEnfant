package com.example.jardinenfantmobile.events;

import java.util.HashMap;
import java.util.Map;

public class Event {
    private String id;
    private String title;
    private String description;
    private String date;
    private String location;
    private Map<String, Boolean> registered;

    public Event() {}  // Constructeur par défaut pour Firebase

    public Event(String id, String title, String description, String date, String location) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.registered = new HashMap<>();
    }

    // Getters et setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Boolean> getRegistered() {
        return registered;
    }

    public void setRegistered(Map<String, Boolean> registered) {
        this.registered = registered;
    }
}
