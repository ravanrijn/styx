package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleApplication extends Identifiable {

    private final String memory;
    private final List<String> urls;
    private final List<String> serviceBindings;
    private final int instances;
    private final ApplicationState state;

    public SimpleApplication(String id, String name, String memory, List<String> urls, List<String> serviceBindings, int instances, ApplicationState state) {
        super(id, name);
        this.memory = memory;
        this.urls = urls;
        this.serviceBindings = serviceBindings;
        this.instances = instances;
        this.state = state;
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
