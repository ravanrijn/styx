package com.github.styx.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

import static org.mvel2.MVEL.evalToString;

@Repository
public class UserRepository extends BaseRepository {

    private final String loginBaseUri;

    @Autowired
    protected UserRepository(RestTemplate restTemplate, AsyncTaskExecutor asyncTaskExecutor, ObjectMapper objectMapper, String apiBaseUri, String uaaBaseUri, String loginBaseUri) {
        super(restTemplate, asyncTaskExecutor, objectMapper, apiBaseUri, uaaBaseUri);
        this.loginBaseUri = loginBaseUri;
    }

    public UserDetails login(String username, String password) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", "Basic Y2Y6");

        MultiValueMap<String, String> model = new LinkedMultiValueMap<String, String>();
        model.add("grant_type", "password");
        model.add("username", username);
        model.add("password", password);

        ResponseEntity<String> loginResponse = getRestTemplate().exchange(loginBaseUri.concat("oauth/token"), HttpMethod.POST, new HttpEntity(model, httpHeaders), String.class);
        if (loginResponse.getStatusCode().equals(HttpStatus.OK)) {
            try {
                UserDetails userDetails = UserDetails.fromCloudFoundryModel(getMapper().readValue(loginResponse.getBody(), new TypeReference<Map<String, Object>>() {}));

                ResponseEntity<String> userInfoResponse = uaaGet(userDetails.getTokenType() + " " + userDetails.getAccessToken(), "userinfo");
                if (userInfoResponse.getStatusCode().equals(HttpStatus.OK)) {
                    Map<String, Object> userInfo = getMapper().readValue(userInfoResponse.getBody(), new TypeReference<Map<String, Object>>() {});
                    userDetails.setId(evalToString("user_id", userInfo));
                    userDetails.setUsername(evalToString("user_name", userInfo));
                    return userDetails;
                }
            } catch (IOException e) {
                throw new RepositoryException("Unable to parse JSON from response", e);
            }
        }
        throw new RepositoryException("Unable to login", loginResponse);
    }

}
