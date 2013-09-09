package com.github.styx.domain;

public class SimpleOrganization {

    private final String id;
    private final String name;
    private final String quotaId;

    public SimpleOrganization(String id, String name, String quotaId) {
        this.id = id;
        this.name = name;
        this.quotaId = quotaId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getQuotaId() {
        return quotaId;
    }
}
