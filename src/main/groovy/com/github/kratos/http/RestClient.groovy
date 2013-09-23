package com.github.kratos.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

class RestClient {

    final RestTemplate restTemplate
    final ObjectMapper objectMapper

    def RestClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    def get(config) {
        def uri = config.path
        final headers = new HttpHeaders();
        headers.add('Accept', 'application/json')
        if (config.headers) {
            config.headers.each({ key, value -> headers.add(key, value) })
        }
        if (config.params) {
            uri = "$uri?"
            def parameterIndex = 1
            config.params.each({ key, value ->
                uri = "$uri$key=$value"
                if (parameterIndex < config.params.size()) {
                    uri = "$uri&"
                }
                parameterIndex++
            })
        }
        try {
            final exchange = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity(headers), new ParameterizedTypeReference<Map<String, Object>>() {})
            return [
                    body: exchange.getBody(),
                    status: exchange.getStatusCode(),
                    headers: exchange.getHeaders()
            ]
        } catch (HttpClientErrorException e) {
            return [
                    body: objectMapper.readValue(e.getResponseBodyAsString(), objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class)),
                    status: e.getStatusCode(),
                    headers: e.getResponseHeaders()
            ]
        }
    }


}
