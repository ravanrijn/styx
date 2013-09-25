package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import com.github.kratos.http.UaaClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/api")
class ApplicationController {

    final ApiClient apiClient;
    final UaaClient uaaClient;

    @Autowired
    def ApplicationController(ApiClient apiClient, UaaClient uaaClient){
        this.apiClient = apiClient
        this.uaaClient = uaaClient
    }

    @RequestMapping(value = "/apps/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def show(@RequestHeader("Authorization") token, @PathVariable("id") id) {
        final userDetails = uaaClient.userDetails(token)
        final applications = apiClient.applications(token)
        final application = apiClient.application(token, id)
        [user: userDetails, applications: applications, application: application]
    }

}
