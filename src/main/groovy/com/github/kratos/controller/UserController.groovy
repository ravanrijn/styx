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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@Controller
@RequestMapping("/api")
class UserController {

    private final ApiClient apiClient

    @Autowired
    def UserController(ApiClient apiClient){
        this.apiClient = apiClient
    }

    @RequestMapping(value = "/space/{spaceId}/users", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    def updateSpaceUser(@RequestHeader("Authorization") String token, @PathVariable("spaceId") String spaceId, @RequestBody user){
        apiClient.updateSpaceUser(token, spaceId, user)
    }

    @RequestMapping(value = "/org/{orgId}/users", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    def updateOrganizationUser(@RequestHeader("Authorization") String token, @PathVariable("orgId") String orgId, @RequestBody user){
        apiClient.updateOrganizationUser(token, orgId, user)
    }

    @RequestMapping(value = "/space/{spaceId}/users/{userId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    def deleteSpaceUser(@RequestHeader("Authorization") String token, @PathVariable("spaceId") String spaceId, @PathVariable("userId") String userId){
        apiClient.deleteSpaceUser(token, spaceId, userId)
    }

    @RequestMapping(value = "/org/{orgId}/users/{userId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    def deleteOrganizationUser(@RequestHeader("Authorization") String token, @PathVariable("orgId") String orgId, @PathVariable("userId") String userId){
        apiClient.deleteOrganizationUser(token, orgId, userId)
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def findUser(@RequestHeader("Authorization") String token, @RequestParam("q") String query){
        apiClient.findUserByUsername(token, query)
    }

}
