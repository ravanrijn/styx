package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import com.github.kratos.http.UaaClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/api")
class RootController {

    final ApiClient apiClient
    final UaaClient uaaClient

    @Autowired
    def RootController(ApiClient apiClient, UaaClient uaaClient){
        this.apiClient = apiClient
        this.uaaClient = uaaClient
    }

    def constructRoot = { String token, String id = null ->
        [user: uaaClient.userDetails(token),
                organizations: apiClient.organizations(token),
                organization: apiClient.organization(token, id ?: organizations.first().id)]
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def index(@RequestHeader("Authorization") String token){
        constructRoot(token)
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def get(@RequestHeader("Authorization") String token, @PathVariable("id") String id){
        constructRoot(token, id)
    }

}
