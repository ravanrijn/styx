package com.github.styx.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.styx.domain.Organization;
import com.github.styx.domain.Role;
import com.github.styx.domain.User;
import com.github.styx.service.ChuckNorrisQuoter;
import com.github.styx.service.CloudFoundryServices;
import com.github.styx.service.UaaServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class RootController {

    private final CloudFoundryServices cfServices;
    private final UaaServices uaaServices;
    private final ChuckNorrisQuoter chuckNorrisQuoter;

    @Autowired
    public RootController(CloudFoundryServices cfServices, UaaServices uaaServices, ChuckNorrisQuoter chuckNorrisQuoter) {
        this.cfServices = cfServices;
        this.uaaServices = uaaServices;
        this.chuckNorrisQuoter = chuckNorrisQuoter;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getWebClient(){
        return "styx";
    }

    @RequestMapping(value = "/api/{organizationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Organization> getRootWithSelectedOrganization(@RequestHeader("Authorization") final String token, @PathVariable("organizationId") String organizationId) {
        return new ResponseEntity(cfServices.getOrganization(token, organizationId), HttpStatus.OK);
    }

    @RequestMapping(value = "/api", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRootWithDefaultOrganization(@RequestHeader("Authorization") final String token) {
        final List<Organization> organizations = cfServices.getOrganizations(token);
        final String organizationId = organizations.get(0).getId();
        final Organization organization = cfServices.getOrganization(token, organizationId);
        final Map<String, Object> root = new LinkedHashMap<>();
        User authenticatedUser = uaaServices.getUser(token);
        if(organization.getUsers().contains(authenticatedUser)){
            authenticatedUser = organization.getUsers().get(organization.getUsers().indexOf(authenticatedUser));
        }
        if(authenticatedUser.getRoles() == null || !authenticatedUser.getRoles().contains(Role.EINDBAAS)){
            if(cfServices.isUserAdmin(authenticatedUser.getId())){
                if(authenticatedUser.getRoles() == null){
                    final Set<Role> roles = new HashSet<>();
                    authenticatedUser = new User(authenticatedUser.getId(), authenticatedUser.getUsername(), roles);
                }
                authenticatedUser.getRoles().add(Role.EINDBAAS);
            }
        }
        root.put("user", authenticatedUser);
        root.put("availableOrganizations", organizations);
        root.put("selectedOrganization", organization);
        root.put("chuckQuote", chuckNorrisQuoter.randomQuote());
        final List<Link> links = new ArrayList<>();
        links.add(new Link("changeOrganization", "/{organizationId}"));
        if(authenticatedUser.getRoles().contains(Role.EINDBAAS)){
            links.add(new Link("manageOrganizationUsers", "/".concat(organization.getId()).concat("/users")));
            links.add(new Link("updateOrganization", "/".concat(organization.getId())));
            links.add(new Link("deleteOrganization", "/".concat(organization.getId())));
        }
        links.add(new Link("userInfo", "/users/".concat(authenticatedUser.getId()).concat("/info")));
        root.put("links", links);
        return new ResponseEntity(root, HttpStatus.OK);
    }

}
