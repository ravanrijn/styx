package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Application extends Identifiable {

    private final String buildPack;
    private final String environment;
    private final String memory;
    private final String diskQuota;
    private final List<String> urls;
    private final List<ServiceBinding> serviceBindings;
    private final List<Instance> instances;
    private final List<Event> events;
    private final ApplicationState state;

    public Application(String id, String name, String buildPack, String environment, String memory, String diskQuota, List<String> urls, List<ServiceBinding> serviceBindings, List<Instance> instances, List<Event> events, ApplicationState state) {
        super(id, name);
        this.buildPack = buildPack;
        this.environment = environment;
        this.memory = memory;
        this.diskQuota = diskQuota;
        this.urls = urls;
        this.serviceBindings = serviceBindings;
        this.instances = instances;
        this.events = events;
        this.state = state;
    }

    public String getBuildPack() {
        return buildPack;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getMemory() {
        return memory;
    }

    public String getDiskQuota() {
        return diskQuota;
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<ServiceBinding> getServiceBindings() {
        return serviceBindings;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public List<Event> getEvents() {
        return events;
    }

    public ApplicationState getState() {
        return state;
    }

}
