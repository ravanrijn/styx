package com.github.styx.domain;

public class ServiceError {

    private final int id;
    private final String message;

    public ServiceError(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
