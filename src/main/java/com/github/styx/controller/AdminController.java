package com.github.styx.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.styx.domain.Quota;
import com.github.styx.service.CloudFoundryServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mvel2.MVEL.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping("/api/administration")
public class AdminController {

    private final CloudFoundryServices cfServices;
    private final ObjectMapper objectMapper;

    @Autowired
    public AdminController(final CloudFoundryServices cfServices, ObjectMapper objectMapper) {
        this.cfServices = cfServices;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/plans", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public void createPlan(@RequestHeader("Authorization") String token, @RequestBody String body) throws IOException {
        final Object request = objectMapper.readValue(body, objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
        final Quota quota = new Quota("", evalToString("name", request), eval("services", request, Integer.class), eval("memoryLimit", request, Integer.class), evalToBoolean("nonBasicServicesAllowed", request), evalToBoolean("trialDbAllowed", request));
        final HttpStatus quotaResponse = cfServices.createQuota(token, quota);
        if (!quotaResponse.equals(HttpStatus.CREATED)) {
            throw new EndpointException("Invalid response status.");
        }
    }

    @RequestMapping(value = "/plans/{id}", method = RequestMethod.DELETE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void deletePlan(@RequestHeader("Authorization") String token, @PathVariable("id") String planId) {
        final HttpStatus httpStatus = cfServices.deleteQuota(token, planId);
        if(!httpStatus.equals(HttpStatus.OK) && !httpStatus.equals(HttpStatus.NO_CONTENT)){
            throw new EndpointException("Invalid response status.");
        }
    }

    @RequestMapping(value = "/plans/{id}", method = RequestMethod.PUT, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void updatePlan(@RequestHeader("Authorization") String token, @PathVariable("id") String planId, @RequestBody String body) throws IOException {
        final Quota plan = objectMapper.readValue(body, Quota.class);
        final HttpStatus httpStatus = cfServices.updateQuota(token, plan);
        System.out.println("********* " + httpStatus.value() + " -- " + httpStatus.equals(HttpStatus.CREATED));
        if(!httpStatus.equals(HttpStatus.OK) && !httpStatus.equals(HttpStatus.CREATED)){
            throw new EndpointException("Invalid response status.");
        }
    }


    @RequestMapping(method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getAdminOverview(@RequestHeader("Authorization") final String token) {
        final Map<String, Object> adminOverview = new HashMap<>();
        adminOverview.put("plans", cfServices.getPlans(token));
        adminOverview.put("organizations", cfServices.getOrganizations(token));
        return adminOverview;
    }

}
