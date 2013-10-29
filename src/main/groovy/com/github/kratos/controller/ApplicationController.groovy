package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/api")
class ApplicationController {

    final ApiClient apiClient
    final RestTemplate restTemplate
    final String apiBaseUri

    @Autowired
    def ApplicationController(ApiClient apiClient, RestTemplate restTemplate, String apiBaseUri){
        this.apiClient = apiClient
        this.restTemplate = restTemplate
        this.apiBaseUri = apiBaseUri
    }

    @RequestMapping(value = "/apps/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def show(@RequestHeader("Authorization") token, @PathVariable("id") id) {
        final user = apiClient.user(token)
        final applications = apiClient.applications(token)
        final application = apiClient.application(token, id)
        if (application.state == 'STARTED') {
            final instances = apiClient.instances(token, id)
            application.instances = instances
        }
        [user: user, organization: application.organization, applications: applications, application: application]
    }

    @RequestMapping(value = "/apps/{id}/instances", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def instances(@RequestHeader("Authorization") token, @PathVariable("id") id) {
        apiClient.instances(token, id)
    }

    @RequestMapping(value = "/apps/{id}/instances/{instance}/logs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def logs(@RequestHeader("Authorization") token, @PathVariable("id") id, @PathVariable("instance") instance){
        final headers = new HttpHeaders()
        headers.add("Authorization", token)
        final exchange = restTemplate.exchange("${apiBaseUri}/v2/apps/${id}/instances/${instance}/files/logs", HttpMethod.GET, new HttpEntity(headers), String.class)
        final tokenizer = new StringTokenizer(exchange.getBody())
        final logs = []
        while (tokenizer.hasMoreElements()){
            final name = tokenizer.nextElement()
            final size = tokenizer.hasMoreElements() ? tokenizer.nextElement() : "unknown"
            logs.add([name:name, size:size, link:"api/apps/${id}/instances/${instance}/logs/${name}" as String])
        }
        [logs:logs]
    }

    @RequestMapping(value = "/apps/{id}/instances/{instance}/logs/{logname}.log", method = RequestMethod.GET)
    def void logs(@RequestHeader("Authorization") token, @PathVariable("id") id, @PathVariable("instance") instance, @PathVariable("logname") logname, HttpServletResponse response){
        final headers = new HttpHeaders()
        headers.add("Authorization", token)
        final exchange = restTemplate.exchange("${apiBaseUri}/v2/apps/${id}/instances/${instance}/files/logs/${logname}.log", HttpMethod.GET, new HttpEntity(headers), String.class)
        response.setStatus(HttpStatus.OK.value())
        response.setContentLength(exchange.getBody().length())
        response.setCharacterEncoding("UTF-8")
        response.setContentType("plain/text")
        response.getWriter().write(exchange.getBody())
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
