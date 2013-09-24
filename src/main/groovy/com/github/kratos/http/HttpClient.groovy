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
        private Map<String, String> uriParams = [:]
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
                if (exchange.getStatusCode().value() < 300) {
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

}
