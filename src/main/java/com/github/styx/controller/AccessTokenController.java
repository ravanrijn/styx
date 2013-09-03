package com.github.styx.controller;

import com.github.styx.console.service.UaaServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Controller for retrieving access tokens.
 *
 * This service basically proxies the requests straight to UAA.
 */
@Controller
@RequestMapping("/api/access_token")
public class AccessTokenController {

    private static final Logger LOG = LoggerFactory.getLogger(AccessTokenController.class);

    private final UaaServices uaaServices;

    @Autowired
    public AccessTokenController(final UaaServices uaaServices) {
        this.uaaServices = uaaServices;
    }

    /**
     *
     *
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(method = POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> authenticate(@RequestParam("username") final String username, @RequestParam("password") final String password) {
        LOG.debug("Login attempt for {}.", username);
        final Map<String, Object> response = new HashMap<>();
        final String token = uaaServices.getAccessToken(username, password);
        response.put("token", token);
        return new ResponseEntity(response, OK);
    }

}
