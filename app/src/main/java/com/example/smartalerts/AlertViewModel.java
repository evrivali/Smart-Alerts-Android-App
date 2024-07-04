package com.example.smartalerts;

public class AlertViewModel {
    private String location;
    private int level;
    private String type;
    private String id;
    private String timestamp;

    public AlertViewModel(String location, int level, String type, String id, String timestamp) {
        this.location = location;
        this.level = level;
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public int getLevel() {
        return level;
    }

    public String getType() {
        return type;
    }
}
