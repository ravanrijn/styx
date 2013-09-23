package com.github.kratos.http

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@Component
class CloudFoundryClient {

    @Autowired def RestTemplate restTemplate
    @Autowired def String apiBaseUri

    def get(uri, token){
        final headers = new HttpHeaders()
        headers.add("Authorization", token)
        headers.add("Accept", token)
        exchange(uri, GET, headers);
    }

    def exchange = {uri, method, headers ->
        restTemplate.exchange("${apiBaseUri}/$uri", method, new HttpEntity(headers), new ParameterizedTypeReference<Map<String, Object>>() {})
    }

}
