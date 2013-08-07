package com.github.styx.controllers;

import com.github.styx.domain.Application;
import com.github.styx.domain.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationController(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public void deleteApplicationById(@RequestHeader("Authorization") final String token, @PathVariable("id") final String id) {
        applicationRepository.deleteById(token, id);
    }

    @RequestMapping(value = "/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Application getApplicationById(@RequestHeader("Authorization") final String token, @PathVariable("id") String id) {
        return applicationRepository.getById(token, id);
    }

    @RequestMapping(value = "/{id}/instances/{instance}/logs/{logName}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> getApplicationInstanceLog(@RequestHeader("Authorization") final String token,
                                                            @PathVariable("id") String id,
                                                            @PathVariable("instance") String instance,
                                                            @PathVariable("logName") String logName) {
        return applicationRepository.getInstanceLog(token, id, instance, logName);
    }

}
