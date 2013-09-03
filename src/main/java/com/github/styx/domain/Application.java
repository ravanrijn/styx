package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Application {

    private final String id;
    private final String name;
    private final String memory;
    private final int instances;
    private final ApplicationState state;


    public Application(String id, String name, String memory, int instances, ApplicationState state) {
        this.id = id;
        this.name = name;
        this.memory = memory;
        this.instances = instances;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMemory() {
        return memory;
    }

    public int getInstances() {
        return instances;
    }

    public ApplicationState getState() {
        return state;
    }
}
