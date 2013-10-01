package com.github.styx.domain;

public class ServicePlan extends Identifiable {

    private final String description;

    public ServicePlan(String id, String name, String description) {
        super(id, name);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
