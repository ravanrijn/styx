package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Organization extends Identifiable {

    private final Quota quota;
    private final List<Domain> domains;
    private final List<Space> spaces;
    private final List<User> users;

    public Organization(String id, String name, Quota quota, List<Domain> domains, List<Space> spaces, List<User> users) {
        super(id, name);
        this.quota = quota;
        this.domains = domains;
        this.spaces = spaces;
        this.users = users;
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
