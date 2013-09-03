package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Quota {

    private final String name;
    private final int services;
    private final int memoryLimit;
    private final boolean nonBasicServicesAllowed;
    private final boolean trialDbAllowed;


    public Quota(String name, int services, int memoryLimit, boolean nonBasicServicesAllowed, boolean trialDbAllowed) {
        this.name = name;
        this.services = services;
        this.memoryLimit = memoryLimit;
        this.nonBasicServicesAllowed = nonBasicServicesAllowed;
        this.trialDbAllowed = trialDbAllowed;
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

    public boolean isNonBasicServicesAllowed() {
        return nonBasicServicesAllowed;
    }

    public boolean isTrialDbAllowed() {
        return trialDbAllowed;
    }

}
