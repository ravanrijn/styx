package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quota {

    private final String id;
    private final String name;
    private final int services;
    private final int memoryLimit;
    private final boolean nonBasicServicesAllowed;
    private final boolean trialDbAllowed;

    public Quota(String id) {
        this.id = id;
        name = null;
        services = 0;
        memoryLimit = 0;
        nonBasicServicesAllowed = false;
        trialDbAllowed = false;
    }

    @JsonCreator
    public Quota(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("services") int services, @JsonProperty("memoryLimit") int memoryLimit, @JsonProperty("nonBasicServicesAllowed") boolean nonBasicServicesAllowed, @JsonProperty("trialDbAllowed") boolean trialDbAllowed) {
        this.id = id;
        this.name = name;
        this.services = services;
        this.memoryLimit = memoryLimit;
        this.nonBasicServicesAllowed = nonBasicServicesAllowed;
        this.trialDbAllowed = trialDbAllowed;
    }

    public String getId() {
        return id;
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final Quota quota = (Quota) object;
        if (!id.equals(quota.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
