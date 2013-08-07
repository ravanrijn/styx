package com.github.styx.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

import static java.net.URLDecoder.decode;

@Controller
@RequestMapping("/cloud")
public class CloudFoundryProxy {

    private static final Logger LOG = LoggerFactory.getLogger(CloudFoundryProxy.class);

    private final RestTemplate restTemplate;

    private final String baseApiUrl;
    private final String baseLoginUrl;
    private final String baseUaaUrl;


    @Autowired
    public CloudFoundryProxy(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.baseApiUrl = environment.getProperty("base.api.url");
        this.baseLoginUrl = environment.getProperty("base.login.url");
        this.baseUaaUrl = environment.getProperty("base.uaa.url");
    }

    @RequestMapping(value = "/login/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> proxyTokenRequest(@RequestParam("grant_type") String grantType, @RequestParam("username") String username, @RequestParam("password") String password) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", "Basic Y2Y6");
        final MultiValueMap<String, String> model = new LinkedMultiValueMap<String, String>();
        model.add("grant_type", grantType);
        model.add("username", username);
        model.add("password", password);
        try{
            return stripHeaders(restTemplate.exchange(baseLoginUrl.concat("/oauth/token"), HttpMethod.POST, new HttpEntity(model, httpHeaders), String.class));
        } catch (HttpClientErrorException e) {
            return new ResponseEntity(e.getResponseBodyAsString(), new HttpHeaders(), e.getStatusCode());
        }
    }

    @RequestMapping(value = "/uaa/**", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> proxyUaaRequests(@RequestHeader("Authorization") String token, HttpServletRequest request) throws UnsupportedEncodingException {
        return stripHeaders(proxyGetRequest(buildUri(baseUaaUrl, extractPath("/uaa/", request.getPathInfo()), request.getQueryString()), token));
    }

    @RequestMapping(value = "/api/**", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> proxyApiDeleteRequest(@RequestHeader("Authorization") String token, HttpServletRequest request) {
        return proxyDeleteRequest(buildUri(baseApiUrl, extractPath("/api/", request.getPathInfo()), request.getQueryString()), token);
    }

    @RequestMapping(value = "/api/**", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> proxyApiGetRequest(@RequestHeader("Authorization") String token, HttpServletRequest request) {
        return proxyGetRequest(buildUri(baseApiUrl, extractPath("/api/", request.getPathInfo()), request.getQueryString()), token);
    }

    @RequestMapping(value = "/api/**", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> proxyApiGetTextRequest(@RequestHeader("Authorization") String token, HttpServletRequest request) {
        return proxyGetRequest(buildUri(baseApiUrl, extractPath("/api/", request.getPathInfo()), request.getQueryString()), token);
    }

    @RequestMapping(value = "/api/**", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> proxyApiPostRequest(@RequestHeader("Authorization") String token, HttpServletRequest request, @RequestBody String body) {
        return proxyRequest(buildUri(baseApiUrl, extractPath("/api/", request.getPathInfo()), request.getQueryString()), HttpMethod.POST, body, token);
    }

    @RequestMapping(value = "/api/**", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> proxyApiPutRequest(@RequestHeader("Authorization") String token, HttpServletRequest request, @RequestBody String body) {
        return proxyRequest(buildUri(baseApiUrl, extractPath("/api/", request.getPathInfo()), request.getQueryString()), HttpMethod.PUT, body, token);
    }

    private static String extractPath(String indexOfKey, String path) {
        return path.substring(path.indexOf(indexOfKey) + indexOfKey.length(), path.length());
    }

    private static String buildUri(String baseUri, String path, String queryString) {
        final StringBuilder uriBuilder = new StringBuilder(baseUri).append("/").append(path);
        if (queryString != null) {
            try {
                uriBuilder.append("?").append(decode(queryString, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                /**
                 * todo fix this.
                 */
                throw new RuntimeException(e);
            }
        }
        return uriBuilder.toString();
    }

    private ResponseEntity<String> proxyDeleteRequest(String url, String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");
        httpHeaders.add("Authorization", token);
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity(httpHeaders), String.class);
            return new ResponseEntity(exchange.getBody(), exchange.getHeaders(), exchange.getStatusCode());
        } catch (HttpClientErrorException e) {
            return new ResponseEntity(e.getResponseBodyAsString(), e.getResponseHeaders(), e.getStatusCode());
        }
    }

    private ResponseEntity<String> proxyGetRequest(String url, String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");
        httpHeaders.add("Authorization", token);
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(httpHeaders), String.class);
            return new ResponseEntity(exchange.getBody(), exchange.getHeaders(), exchange.getStatusCode());
        } catch (HttpClientErrorException e) {
            return new ResponseEntity(e.getResponseBodyAsString(), e.getResponseHeaders(), e.getStatusCode());
        }
    }

    private ResponseEntity<String> proxyRequest(String url, HttpMethod httpMethod, String body, String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");
        httpHeaders.add("Authorization", token);
        httpHeaders.add("Content-Type", "application/json");
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, httpMethod, new HttpEntity(body, httpHeaders), String.class);
            return new ResponseEntity(exchange.getBody(), exchange.getHeaders(), exchange.getStatusCode());
        } catch (HttpClientErrorException e) {
            return new ResponseEntity(e.getResponseBodyAsString(), e.getResponseHeaders(), e.getStatusCode());
        }
    }

    private ResponseEntity<String> stripHeaders(ResponseEntity<String> responseEntity){
        if (responseEntity.getHeaders().containsKey("Transfer-Encoding")) {
            final HttpHeaders responseHeaders = new HttpHeaders();
            for (String key : responseEntity.getHeaders().keySet()) {
                if (!key.equals("Transfer-Encoding")) {
                    responseHeaders.put(key, responseEntity.getHeaders().get(key));
                }
            }
            return new ResponseEntity<String>(responseEntity.getBody(), responseHeaders, responseEntity.getStatusCode());
        }
        return responseEntity;
    }

}
