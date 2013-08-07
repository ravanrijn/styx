package com.github.styx.controllers;

import com.github.styx.domain.Organization;
import com.github.styx.domain.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public void deleteOrganizationById(@RequestHeader("Authorization") final String token, @PathVariable("id") final String id) {
        organizationRepository.deleteById(token, id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Organization getOrganization(@RequestHeader("Authorization") final String token, @PathVariable("id") final String id) {
        return organizationRepository.getById(token, id, 2);
    }

    @RequestMapping(method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Organization> getOrganizations(@RequestHeader("Authorization") final String token) {
        return organizationRepository.getAll(token, 2);
    }

}
