package com.github.kratos.http

import ch.qos.logback.classic.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
class HttpClient {

    private final static Logger LOG = LoggerFactory.getLogger(HttpClient.class)
    private final RestTemplate restTemplate
    private final ObjectMapper objectMapper

    @Autowired
    def HttpClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate
        this.objectMapper = objectMapper
    }

    def get(Closure closure) {
        HttpClientDsl.newInstance(closure, HttpMethod.GET, restTemplate, objectMapper)
    }

    def post(Closure closure) {
        HttpClientDsl.newInstance(closure, HttpMethod.POST, restTemplate, objectMapper)
    }

    def put(Closure closure) {
        HttpClientDsl.newInstance(closure, HttpMethod.PUT, restTemplate, objectMapper)
    }

    def delete(Closure closure) {
        HttpClientDsl.newInstance(closure, HttpMethod.DELETE, restTemplate, objectMapper)
    }

    static class HttpClientDsl {

        private final RestTemplate restTemplate
        private final ObjectMapper objectMapper
        private final HttpMethod httpMethod
        private String path
        private Object body
        private Map<String, String> headers
        private Map<String, String> uriParams
        private Map<String, String> queryParams

        def HttpClientDsl(httpMethod, restTemplate, objectMapper) {
            this.httpMethod = httpMethod
            this.restTemplate = restTemplate
            this.objectMapper = objectMapper
        }

        def static newInstance(Closure closure, HttpMethod httpMethod, RestTemplate restTemplate, ObjectMapper objectMapper) {
            HttpClientDsl httpClientDsl = new HttpClientDsl(httpMethod, restTemplate, objectMapper)
            closure.delegate = httpClientDsl
            def closureComposition = closure >> httpClientDsl.exchange
            closureComposition()
        }

        def path(String path) {
            this.path = path
        }

        def headers(Map<String, String> headers) {
            this.headers = headers
        }

        def body(Object body) {
            this.body = body
        }

        def uriParams(Map<String, String> uriParams) {
            this.uriParams = uriParams
        }

        def queryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams
        }

        def exchange = {
            final uri = path
            final httpHeaders = new HttpHeaders()
            if (headers) {
                headers.each { key, value -> httpHeaders.add(key, value) }
            }
            HttpEntity httpEntity = new HttpEntity(httpHeaders)
            if (queryParams) {
                uri = "$uri?"
                def parameterIndex = 1
                queryParams.each { key, value ->
                    uri = "$uri$key=$value"
                    if (parameterIndex < queryParams.size()) {
                        uri = "$uri&"
                    }
                    parameterIndex++
                }
            }
            if (body) {
                httpEntity = new HttpEntity(body, httpHeaders)
            }
            try {
                final exchange = restTemplate.exchange(uri, httpMethod, httpEntity, new ParameterizedTypeReference<Map<String, Object>>() {})
                if (exchange.getStatusCode().value() < 299) {
                    return exchange.getBody()
                }
                throw new RestClientException(body: exchange.getBody(), status: exchange.getStatusCode(), headers: exchange.getHeaders())
            } catch (HttpClientErrorException e) {
                LOG.error("Unable to retrieve result.", e)
                throw new RestClientException(body: objectMapper.readValue(e.getResponseBodyAsString(), objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class)),
                        status: e.getStatusCode(), headers: e.getResponseHeaders())
            }
        }
    }


    public static void main(String[] args) {
        String token = "bearer eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJiNDZkYmZlZC02NDMwLTQ1M2UtOGNjOC00ZjUyN2Q5NDFlNDgiLCJzdWIiOiI1MDM1ZjAwNy1iOWNlLTQyZjktODVlYy03MjNmMjVkNjcwYmYiLCJzY29wZSI6WyJjbG91ZF9jb250cm9sbGVyLmFkbWluIiwiY2xvdWRfY29udHJvbGxlci5yZWFkIiwiY2xvdWRfY29udHJvbGxlci53cml0ZSIsIm9wZW5pZCIsInBhc3N3b3JkLndyaXRlIiwic2NpbS5yZWFkIiwic2NpbS53cml0ZSJdLCJjbGllbnRfaWQiOiJjZiIsImNpZCI6ImNmIiwiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwidXNlcl9pZCI6IjUwMzVmMDA3LWI5Y2UtNDJmOS04NWVjLTcyM2YyNWQ2NzBiZiIsInVzZXJfbmFtZSI6ImFkbWluIiwiZW1haWwiOiJhZG1pbiIsImlhdCI6MTM4MDAxMDUxNCwiZXhwIjoxMzgwMDUzNzE0LCJpc3MiOiJodHRwczovL3VhYS5jZi5lZGVuLmtsbS5jb20vb2F1dGgvdG9rZW4iLCJhdWQiOlsic2NpbSIsIm9wZW5pZCIsImNsb3VkX2NvbnRyb2xsZXIiLCJwYXNzd29yZCJdfQ.FIZ7WfBvzMrR7pp4PodxLwE83jqABFX8ch9MAnvaqUNbXiAWkSFwZ2TTiJhtxiWqli7BdqQaJD4hoM6GPu4cdoCc3TrydbZbXvpZC5zRW184q-zh8oZyjva1KSpiNXvnWHRgr4xgZ6tO1Rdt_mr9l1qOBBNPGQmv4iKSBMyX7G4"
        println new HttpClient(new RestTemplate(), new ObjectMapper()).get {
            path "http://api.cf.eden.klm.com/v2/apps"
            headers authorization: token, accept: 'application/json'
            queryParams 'inline-relations-depth': 0
        }
    }

}
