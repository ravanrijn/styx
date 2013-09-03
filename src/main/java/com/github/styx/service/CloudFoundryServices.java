package com.github.styx.service;

import com.github.styx.console.domain.Organization;

import java.util.List;

public interface CloudFoundryServices {

    List<Organization> getOrganizations(String token);

    Organization getOrganization(String token, String id);

}
