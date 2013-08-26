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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

@Repository
public class UserRepository extends BaseRepository {

    private final String apiBaseUri;

    private final String uaaBaseUri;

    private final String clientId;

    private final String clientSecret;

    @Autowired
    protected UserRepository(RestTemplate restTemplate, AsyncTaskExecutor asyncTaskExecutor, ObjectMapper objectMapper, String apiBaseUri, String uaaBaseUri, String clientId, String clientSecret) {
        super(restTemplate, asyncTaskExecutor, objectMapper, apiBaseUri, uaaBaseUri);
        this.apiBaseUri = concatSlashIfNeeded(apiBaseUri);
        this.uaaBaseUri = concatSlashIfNeeded(uaaBaseUri);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public List<User> getAllUsers(String token) {
        Map<String, Object> usersResponse = apiGet(token, "v2/users");

        Set<String> userIds = new HashSet<>();
        for (Object userResource : eval("resources", usersResponse, List.class)) {
            userIds.add(evalToString("metadata.guid", userResource));
        }

        List<User> users = new ArrayList<>();

        Map<String, String> userNames = getUserNames(token, userIds);
        for (Map.Entry<String, String> entry : userNames.entrySet()) {
            users.add(OrganizationUser.Builder.newBuilder(entry.getKey()).setUserName(entry.getValue()).build());
        }
        return users;
    }

    public UserInfo getUserInfo(String token) {
        Map<String, Object> userInfoResponse = uaaGet(token, "userinfo");
        String userId = evalToString("user_id", userInfoResponse);

        // use Styx' token to retrieve the user details
        String accessToken = getAccessToken(clientId, clientSecret);
        Map<String, Object> uaaUserResponse = uaaGet(accessToken, "Users/".concat(userId));
        Map<String, Object> apiUserResponse = apiGet(accessToken, "v2/users/".concat(userId).concat("?inline-relations-depth=1"));
        return UserInfo.fromCloudFoundryModel(uaaUserResponse, apiUserResponse);
    }

    public AccessToken login(String username, String password) {
        String authorizationEndpoint = getAuthorizationEndpoint();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", getAuthorization(clientId, clientSecret));

        MultiValueMap<String, String> model = new LinkedMultiValueMap();
        model.add("grant_type", "password");
        model.add("username", username);
        model.add("password", password);

        ResponseEntity<Map<String, Object>> loginResponse = getRestTemplate().exchange(authorizationEndpoint.concat("/oauth/token"), HttpMethod.POST, new HttpEntity(model, httpHeaders), new ParameterizedTypeReference<Map<String, Object>>() {});
        if (loginResponse.getStatusCode().equals(HttpStatus.OK)) {
            AccessToken accessToken = AccessToken.fromCloudFoundryModel(loginResponse.getBody());

            Map<String, Object> userInfoResponse = uaaGet(accessToken.getTokenType() + " " + accessToken.getAccessToken(), "userinfo");
            accessToken.setId(evalToString("user_id", userInfoResponse));
            accessToken.setUsername(evalToString("user_name", userInfoResponse));
            return accessToken;
        }
        throw new RepositoryException("Unable to login", loginResponse);
    }

    private String getAuthorizationEndpoint() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json;charset=utf-8");

        ResponseEntity<Map<String, Object>> infoResponse = getRestTemplate().exchange(apiBaseUri.concat("info"), HttpMethod.GET, new HttpEntity(httpHeaders), new ParameterizedTypeReference<Map<String, Object>>() {});
        if (!infoResponse.getStatusCode().equals(HttpStatus.OK)) {
            throw new RepositoryException("Unable to retrieve info", infoResponse);
        }
        return (String) infoResponse.getBody().get("authorization_endpoint");
    }

    public void registerUser(String username, String firstName, String lastName, String password) {
        String accessToken = getAccessToken(clientId, clientSecret);
        String userId = uaaCreateUser(accessToken, username, firstName, lastName, password);
        apiCreateUser(accessToken, userId);
    }

    private String getAccessToken(String clientId, String clientSecret) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", getAuthorization(clientId, clientSecret));

        MultiValueMap<String, String> model = new LinkedMultiValueMap();
        model.add("grant_type", "client_credentials");
        model.add("response_type", "token");

        try {
            ResponseEntity<Map<String, Object>> tokenResponse = getRestTemplate().exchange(uaaBaseUri.concat("oauth/token"), HttpMethod.POST, new HttpEntity(model, httpHeaders), new ParameterizedTypeReference<Map<String, Object>>() {});
            if (!tokenResponse.getStatusCode().equals(HttpStatus.OK)) {
                throw new RepositoryException("Problem retrieving access token", tokenResponse);
            }

            String tokenType = evalToString("token_type", tokenResponse.getBody());
            String accessToken = evalToString("access_token", tokenResponse.getBody());
            return tokenType.concat(" ").concat(accessToken);
        } catch (HttpClientErrorException e) {
            throw new RepositoryException("Problem retrieving access token", new ResponseEntity(e.getResponseBodyAsString(), e.getStatusCode()));
        }
    }

    private String uaaCreateUser(String accessToken, String username, String firstName, String lastName, String password) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json;charset=utf-8");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", accessToken);

        String body = new StringBuilder()
                .append("{\"userName\":\"")
                .append(username)
                .append("\",\"emails\":[{\"value\":\"")
                .append(username)
                .append("\"}],\"password\":\"")
                .append(password)
                .append("\",\"name\":{\"givenName\":\"")
                .append(firstName)
                .append("\",\"familyName\":\"")
                .append(lastName)
                .append("\"}}\"").toString();

        try {
            ResponseEntity<Map<String, Object>> createUserResponse = getRestTemplate().exchange(uaaBaseUri.concat("Users"), HttpMethod.POST, new HttpEntity(body, httpHeaders), new ParameterizedTypeReference<Map<String, Object>>() {});
            if (!createUserResponse.getStatusCode().equals(HttpStatus.CREATED)) {
                throw new RepositoryException("Unable to create user in uaa", createUserResponse);
            }
            return evalToString("id", createUserResponse.getBody());
        } catch (HttpClientErrorException e) {
            throw new RepositoryException("Unable to create user in uaa", new ResponseEntity(e.getResponseBodyAsString(), e.getStatusCode()));
        }
    }

    private void apiCreateUser(String accessToken, String userId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json;charset=utf-8");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", accessToken);

        String body = "{\"guid\":\"".concat(userId).concat("\"}");

        try {
            ResponseEntity<Map<String, Object>> createUserResponse = getRestTemplate().exchange(apiBaseUri.concat("v2/users"), HttpMethod.POST, new HttpEntity(body, httpHeaders), new ParameterizedTypeReference<Map<String, Object>>() {});
            if (!createUserResponse.getStatusCode().equals(HttpStatus.CREATED)) {
                throw new RepositoryException("Unable to create user using api", createUserResponse);
            }
        } catch (HttpClientErrorException e) {
            throw new RepositoryException("Unable to create user using api", new ResponseEntity(e.getResponseBodyAsString(), e.getStatusCode()));
        }
    }

    private String getAuthorization(String clientId, String clientSecret) {
        return "Basic ".concat(Base64.encodeBase64String(clientId.concat(":").concat(clientSecret).getBytes()));
    }

}
