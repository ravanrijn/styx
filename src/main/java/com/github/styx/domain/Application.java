package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Application {

    private final String id;
    private final String name;
    private final String memory;
    private final List<String> urls;
    private final List<String> serviceBindings;
    private final int instances;
    private final ApplicationState state;


    public Application(String id, String name, String memory, List<String> urls, List<String> serviceBindings, int instances, ApplicationState state) {
        this.id = id;
        this.name = name;
        this.memory = memory;
        this.urls = urls;
        this.serviceBindings = serviceBindings;
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

    public List<String> getUrls() {
        return urls;
    }

    public List<String> getServiceBindings() {
        return serviceBindings;
    }
}
