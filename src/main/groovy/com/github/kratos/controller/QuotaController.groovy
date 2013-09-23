package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.http.MediaType

@Controller
@RequestMapping("/api")
class QuotaController {

    @Autowired
    def ApiClient apiClient;

    @RequestMapping(value = "/quotas", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def show(@RequestHeader("Authorization") token) {
        apiClient.quotas(token).body
    }

}
