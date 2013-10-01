package com.github.styx.domain;

public class Event {

    private final String id;
    private final String status;
    private final String description;
    private final String timestamp;

    public Event(String id, String status, String description, String timestamp) {
        this.id = id;
        this.status = status;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
