package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/api")
class UserController {

    private final ApiClient apiClient

    @Autowired
    def UserController(ApiClient apiClient){
        this.apiClient = apiClient
    }

    @RequestMapping(value = "/{orgId}/users", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def updateOrganizationUser(@RequestHeader("Authorization") String token, @PathVariable("orgId") String orgId, @RequestBody user){
        apiClient.updateOrganizationUser(token, orgId, user)
    }

    @RequestMapping(value = "/{orgId}/users/{userId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def deleteOrganizationUser(@RequestHeader("Authorization") String token, @PathVariable("orgId") String orgId, @PathVariable("userId") String userId){
        apiClient.deleteOrganizationUser(token, orgId, userId)
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def findUser(@RequestHeader("Authorization") String token, @RequestParam("q") String query){
        apiClient.findUserByUsername(token, query)
    }

}
