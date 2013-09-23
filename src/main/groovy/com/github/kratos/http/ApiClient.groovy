package com.github.kratos.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@Component
class ApiClient extends RestClient {

    final String apiBaseUri
    final String uaaBaseUri

    @Autowired
    def ApiClient(RestTemplate restTemplate, String apiBaseUri, String uaaBaseUri, ObjectMapper objectMapper){
        super(restTemplate, objectMapper);
        this.apiBaseUri = apiBaseUri;
        this.uaaBaseUri = uaaBaseUri;
    }

    def quotas(String token){
        get([path:"${apiBaseUri}/v2/quota_definitions", headers:['Authorization':token]])
    }

}
