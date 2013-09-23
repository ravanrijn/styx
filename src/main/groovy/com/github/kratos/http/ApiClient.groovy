package com.github.kratos.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ApiClient extends RestClient {

    final String apiBaseUri
    final String uaaBaseUri

    @Autowired
    def ApiClient(RestTemplate restTemplate, String apiBaseUri, String uaaBaseUri, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
        this.apiBaseUri = apiBaseUri;
        this.uaaBaseUri = uaaBaseUri;
    }

    def application(token, id) {
        def cfApplication = get([path: "${apiBaseUri}/v2/apps/${id}", headers: ['Authorization': token]])



    }

    def quotas(String token) {
        get([path: "${apiBaseUri}/v2/quota_definitions", headers: ['Authorization': token]])
    }

}
