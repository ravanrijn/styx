package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Space {

    private final String id;
    private final String name;
    private final List<User> users;
    private final List<Application> applications;

    public Space(String id, String name, List<User> users, List<Application> applications) {
        this.id = id;
        this.name = name;
        this.users = users;
        this.applications = applications;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Application> getApplications() {
        return applications;
    }

}
