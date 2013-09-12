package com.github.styx.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class ApplicationController {

    @RequestMapping(value = "/app/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApplicationDetails(@RequestHeader("Authorization") final String token, @PathVariable("applicationId") String applicationId) {
        Map<String, Object> application = new HashMap<>();
        return new ResponseEntity(application, HttpStatus.OK);
    }

}
