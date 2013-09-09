package com.github.styx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.styx.controller.EndpointException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.util.Assert.hasText;

abstract class RemoteServices {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteServices.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    protected RemoteServices(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    protected ResponseEntity<Map<String, Object>> delete(String path, HttpHeaders httpHeaders, Object body){
        final ResponseEntity<Map<String, Object>> responseEntity = exchange(path, HttpMethod.DELETE, body, httpHeaders, new ParameterizedTypeReference<Map<String, Object>>() {});
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity;
        }
        throw new EndpointException("Cannot perform post for path [" + path + "]", responseEntity);
    }

    protected ResponseEntity<Map<String, Object>> post(String path, HttpHeaders httpHeaders, Object body){
        final ResponseEntity<Map<String, Object>> responseEntity = exchange(path, HttpMethod.POST, body, httpHeaders, new ParameterizedTypeReference<Map<String, Object>>() {});
        if (responseEntity.getStatusCode().equals(HttpStatus.OK) || responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
            return responseEntity;
        }
        throw new EndpointException("Cannot perform post for path [" + path + "]", responseEntity);
    }

    protected ResponseEntity<Map<String, Object>> put(String token, String path, Object body){
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Content-Type", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", token);
        final ResponseEntity<Map<String, Object>> responseEntity = exchange(path, HttpMethod.PUT, body, httpHeaders, new ParameterizedTypeReference<Map<String, Object>>() {});
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity;
        }
        throw new EndpointException("Cannot perform get for path [" + path + "]", responseEntity);
    }

    protected Map<String, Object> get(String token, String path) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", token);
        final ResponseEntity<Map<String, Object>> responseEntity = exchange(path, HttpMethod.GET, null, httpHeaders, new ParameterizedTypeReference<Map<String, Object>>() {});
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            throw new EndpointException("Cannot perform get for path [" + path + "]", responseEntity);
        }
        return responseEntity.getBody();
    }

    protected Map<String, Object> get(String path) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        final ResponseEntity<Map<String, Object>> responseEntity = exchange(path, HttpMethod.GET, null, httpHeaders, new ParameterizedTypeReference<Map<String, Object>>() {});
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            throw new EndpointException("Cannot perform get for path [" + path + "]", responseEntity);
        }
        return responseEntity.getBody();
    }

    protected String sanitizeBaseUri(final String uri) {
        hasText(uri, "Invalid base uri.");
        if (!uri.endsWith("/")) {
            return uri.concat("/");
        }
        return uri;
    }

    private <T> ResponseEntity<T> exchange(String path, HttpMethod method, Object body, HttpHeaders httpHeaders, ParameterizedTypeReference<T> typeReference) {
        HttpEntity request = body == null ? new HttpEntity(httpHeaders) : new HttpEntity(body, httpHeaders);
        try {
            return restTemplate.exchange(path, method, request, typeReference);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity(e.getResponseBodyAsString(), e.getStatusCode());
        }
    }

}
