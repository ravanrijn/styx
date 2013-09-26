package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/api/admin")
class AdminController {

    private final ApiClient apiClient

    @Autowired
    def AdminController(ApiClient apiClient) {
        this.apiClient = apiClient
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def index(@RequestHeader("Authorization") token) {
        [quotas: apiClient.quotas(token), organizations: apiClient.organizations(token)]
    }

}
