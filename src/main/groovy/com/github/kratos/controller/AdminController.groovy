package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@Controller
class AdminController {

    private final ApiClient apiClient

    @Autowired
    def AdminController(ApiClient apiClient) {
        this.apiClient = apiClient
    }

    @RequestMapping(value = "/api/admin", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    def index(@RequestHeader("Authorization") token) {
        [quotas: apiClient.quotas(token), organizations: apiClient.organizations(token)]
    }

    @RequestMapping(value = "/api/organizations/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    def removeOrg(@RequestHeader("Authorization") token, @PathVariable("id") id){
        apiClient.deleteOrganization(token, id)
    }

    @RequestMapping(value = "/api/organizations", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    def createOrg(@RequestHeader("Authorization") token, @RequestBody Map org){
        apiClient.createOrganization(token, org)
    }

    @RequestMapping(value = "/api/organizations/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    def updateOrg(@RequestHeader("Authorization") token, @PathVariable("id") String id, @RequestBody Map org){
        org.id = id
        apiClient.updateOrganization(token, org)
    }

    @RequestMapping(value = "/api/quotas", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    def createQuota(@RequestHeader("Authorization") token, @RequestBody Map quota){
        apiClient.createQuota(token, quota)
    }

    @RequestMapping(value = "/api/quotas/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    def removeQuota(@RequestHeader("Authorization") token, @PathVariable("id") id){
        apiClient.deleteQuota(token, id)
    }

    @RequestMapping(value = "/api/quotas/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    def updateQuota(@RequestHeader("Authorization") token, @PathVariable("id") String id, @RequestBody Map quota){
        quota.id = id
        apiClient.updateQuota(token, quota)
    }

}
