package com.github.styx.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mvel2.MVEL.evalToString;

@Repository
public class UserRepository extends BaseRepository {

    private final String uaaBaseUri;

    private final String clientId;

    private final String clientSecret;

    @Autowired
    protected UserRepository(RestTemplate restTemplate, AsyncTaskExecutor asyncTaskExecutor, ObjectMapper objectMapper, String apiBaseUri, String uaaBaseUri, String clientId, String clientSecret) {
        super(restTemplate, asyncTaskExecutor, objectMapper, apiBaseUri, uaaBaseUri);
        this.uaaBaseUri = concatSlashIfNeeded(uaaBaseUri);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public UserDetails login(String username, String password) {
        String authorization = "Basic ".concat(Base64.encodeBase64String(clientId.concat(":").concat(clientSecret).getBytes()));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", authorization);

        MultiValueMap<String, String> model = new LinkedMultiValueMap();
        model.add("grant_type", "password");
        model.add("username", username);
        model.add("password", password);

        ResponseEntity<Map<String, Object>> loginResponse = getRestTemplate().exchange(uaaBaseUri.concat("oauth/token"), HttpMethod.POST, new HttpEntity(model, httpHeaders), new ParameterizedTypeReference<Map<String, Object>>() {});
        if (loginResponse.getStatusCode().equals(HttpStatus.OK)) {
            UserDetails userDetails = UserDetails.fromCloudFoundryModel(loginResponse.getBody());

            Map<String, Object> userInfoResponse = uaaGet(userDetails.getTokenType() + " " + userDetails.getAccessToken(), "userinfo");
            userDetails.setId(evalToString("user_id", userInfoResponse));
            userDetails.setUsername(evalToString("user_name", userInfoResponse));
            return userDetails;
        }
        throw new RepositoryException("Unable to login", loginResponse);
    }

}
