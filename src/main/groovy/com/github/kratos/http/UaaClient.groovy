package com.github.kratos.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UaaClient extends RestClient {

    @Autowired
    UaaClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper)
    }

}
