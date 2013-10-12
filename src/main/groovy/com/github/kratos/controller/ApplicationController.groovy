package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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
        try{
        final user = apiClient.user(token)
        final applications = apiClient.applications(token)
        final application = apiClient.application(token, id)
        final instances = apiClient.instances(token, id)
        application.instances = instances
        [user: user, organization: application.organization, applications: applications, application: application]
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    @RequestMapping(value = "/apps/{id}/instances", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def instances(@RequestHeader("Authorization") token, @PathVariable("id") id) {
        apiClient.instances(token, id)
    }

    @RequestMapping(value = "/apps/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def remove(@RequestHeader("Authorization") token, @PathVariable("id") id) {
        apiClient.deleteApplication(token, id);
    }

    @RequestMapping(value = "/apps/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    def update(@RequestHeader("Authorization") token, @PathVariable("id") String id, @RequestBody app) {
        app.id = id
        apiClient.updateApplication(token, app)
    }

}
