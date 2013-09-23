package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/api")
class ApplicationController {

    final ApiClient apiClient;

    @Autowired
    def ApplicationController(ApiClient apiClient){
        this.apiClient = apiClient
    }

    @RequestMapping(value = "/apps/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def show(@RequestHeader("Authorization") token, @PathVariable("id") id) {
        def availableApplications = apiClient.applications(token)
        def selectedApplication = apiClient.application(token, id)
        [availableApplications: availableApplications, selectedApplication: selectedApplication]
    }

}
