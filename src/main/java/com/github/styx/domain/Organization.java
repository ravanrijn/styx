package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Organization {

    private final String id;
    private final String name;
    private final Quota quota;
    private final List<Domain> domains;
    private final List<Space> spaces;
    private final List<User> users;

    public Organization(String id, String name, Quota quota, List<Domain> domains, List<Space> spaces, List<User> users) {
        this.id = id;
        this.name = name;
        this.quota = quota;
        this.domains = domains;
        this.spaces = spaces;
        this.users = users;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Quota getQuota() {
        return quota;
    }

    public List<Domain> getDomains() {
        return domains;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Space> getSpaces() {
        return spaces;
    }

}
