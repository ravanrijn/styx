package com.github.styx.controller;

import com.github.styx.console.domain.Organization;
import com.github.styx.console.domain.User;
import com.github.styx.console.service.ChuckNorrisQuoter;
import com.github.styx.console.service.CloudFoundryServices;
import com.github.styx.console.service.UaaServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        final User currentUser = uaaServices.getUser(token);
        root.put("user", currentUser);
        root.put("availableOrganizations", organizations);
        root.put("selectedOrganization", organization);
        root.put("chuckQuote", chuckNorrisQuoter.randomQuote());
        final List<Link> links = new ArrayList<>();
        links.add(new Link("changeOrganization", "/{organizationId}"));
        links.add(new Link("manageOrganizationUsers", "/".concat(organization.getId()).concat("/users")));
        links.add(new Link("userInfo", "/users/".concat(currentUser.getId()).concat("/info")));
        root.put("links", links);
        return new ResponseEntity(root, HttpStatus.OK);
    }

}
