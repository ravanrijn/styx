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
class RootController {

    final ApiClient apiClient

    @Autowired
    def RootController(ApiClient apiClient){
        this.apiClient = apiClient
    }

    def constructRoot = { String token, String id = null ->
        def organizations = apiClient.organizations(token)
        [user: apiClient.user(token),
                organizations: organizations,
                organization: apiClient.organization(token, id ?: organizations.first().id)]
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    def String htmlResource(){
        return "kratos";
    }

    @RequestMapping(value = "/api", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def index(@RequestHeader("Authorization") String token){
        constructRoot(token)
    }

    @RequestMapping(value = "/api/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def get(@RequestHeader("Authorization") String token, @PathVariable("id") String id){
        constructRoot(token, id)
    }

    @RequestMapping(value = "/api/org/{orgId}/spaces", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    def createSpace(@RequestHeader("Authorization") String token, @PathVariable("orgId") String orgId, @RequestBody space) {
        apiClient.createSpace(token, orgId, space.name)
    }

    @RequestMapping(value = "/api/spaces/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    def deleteSpace(@RequestHeader("Authorization") String token, @PathVariable("id") String id) {
        apiClient.deleteSpace(token, id)
    }

}
