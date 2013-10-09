package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import com.github.kratos.http.HttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.web.bind.annotation.RequestMethod.GET

@Controller
@RequestMapping("/cf")
class CfProxy {

    private final HttpClient httpClient
    private final ApiClient apiClient
    private final String apiBaseUri

    @Autowired
    def CfProxy(HttpClient httpClient, ApiClient apiClient, String apiBaseUri){
        this.httpClient = httpClient
        this.apiBaseUri = apiBaseUri
        this.apiClient = apiClient
    }

    @RequestMapping(value = "/api/**", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    def proxyToApi(@RequestHeader("Authorization") token, HttpServletRequest request){
        def url = request.pathInfo[request.pathInfo.indexOf("/api/")+"/api".length()..request.pathInfo.length()-1]
        final response = httpClient.get {
            path request.queryString == null ? "${apiBaseUri}${url}" : "${apiBaseUri}${url}?${request.queryString}"
            headers Authorization: token, Accept: APPLICATION_JSON_VALUE, "Content-Type": APPLICATION_JSON_VALUE
        }
        [response:response, user:apiClient.user(token)]
    }

}
