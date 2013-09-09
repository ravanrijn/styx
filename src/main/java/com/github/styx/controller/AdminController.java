package com.github.styx.controller;

import com.github.styx.domain.Quota;
import com.github.styx.domain.SimpleOrganization;
import com.github.styx.service.CloudFoundryServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping("/api/administration")
public class AdminController {

    private final CloudFoundryServices cfServices;

    @Autowired
    public AdminController(final CloudFoundryServices cfServices) {
        this.cfServices = cfServices;
    }

    @RequestMapping(method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getAdminOverview(@RequestHeader("Authorization") final String token) {
        final Map<String, Object> adminOverview = new HashMap<>();
        final List<Quota> plans = cfServices.getPlans(token);
        adminOverview.put("plans", plans);
        final List<SimpleOrganization> organizations = cfServices.getOrganizations(token);
        for(SimpleOrganization organization : organizations){
            final Quota searchQuota = new Quota(organization.getQuotaId());
            if(plans.contains(searchQuota)){

            }
        }
        adminOverview.put("organizations", organizations);
        return adminOverview;
    }

}
