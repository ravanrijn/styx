package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import com.github.kratos.http.UaaClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
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

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def index(){

    }

}
