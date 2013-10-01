package com.github.styx.domain;

public class Identifiable {

    private final String id;
    private final String name;

    public Identifiable(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
