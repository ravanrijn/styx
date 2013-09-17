package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Space extends Identifiable {

    private final List<User> users;
    private final List<SimpleApplication> applications;

    public Space(String id, String name, List<User> users, List<SimpleApplication> applications) {
        super(id, name);
        this.users = users;
        this.applications = applications;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<SimpleApplication> getApplications() {
        return applications;
    }

}
