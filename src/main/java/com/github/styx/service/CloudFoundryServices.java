package com.github.styx.service;

import com.github.styx.domain.Organization;
import com.github.styx.domain.Quota;
import com.github.styx.domain.SimpleOrganization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CloudFoundryServices {

    List<SimpleOrganization> getOrganizations(String token);

    List<Quota> getPlans(String token);

    ResponseEntity createQuota(String token, Quota quota);

    ResponseEntity deleteQuota(String token, String id);

    ResponseEntity updateQuota(String token, Quota quota);

    Organization getOrganization(String token, String id);

    boolean isUserAdmin(String id);

}
