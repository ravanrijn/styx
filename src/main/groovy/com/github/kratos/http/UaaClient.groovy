package com.github.kratos.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UaaClient {

    private final HttpClient httpClient
    private final String uaaBaseUri
    private final String clientSecret
    private final String clientId
    private final String apiBaseUri

    @Autowired
    UaaClient(HttpClient httpClient, String uaaBaseUri, String apiBaseUri, String clientId, String clientSecret) {
        this.httpClient = httpClient
        this.apiBaseUri = apiBaseUri
        this.clientId = clientId
        this.clientSecret = clientSecret
        this.uaaBaseUri = uaaBaseUri
    }

    def userToken(){

    }

    def applicationToken(){

    }

}
