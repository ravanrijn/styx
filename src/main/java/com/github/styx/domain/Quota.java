package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Quota {

    private final String name;
    private final int services;
    private final int memoryLimit;

    public Quota(String name, int services, int memoryLimit) {
        this.name = name;
        this.services = services;
        this.memoryLimit = memoryLimit;
    }

    public String getName() {
        return name;
    }

    public int getServices() {
        return services;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

}
