package com.github.styx.service;

import com.github.styx.domain.Organization;

import java.util.List;

public interface CloudFoundryServices {

    List<Organization> getOrganizations(String token);

    Organization getOrganization(String token, String id);

    boolean isUserAdmin(String id);

}
