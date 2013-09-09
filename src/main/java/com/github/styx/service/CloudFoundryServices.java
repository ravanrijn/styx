package com.github.styx.service;

import com.github.styx.domain.Organization;
import com.github.styx.domain.Quota;
import com.github.styx.domain.SimpleOrganization;
import org.apache.http.HttpStatus;

import java.util.List;

public interface CloudFoundryServices {

    List<SimpleOrganization> getOrganizations(String token);

    List<Quota> getPlans(String token);

    HttpStatus updateQuota(String token, String id, Quota quota);

    Organization getOrganization(String token, String id);

    boolean isUserAdmin(String id);

}
