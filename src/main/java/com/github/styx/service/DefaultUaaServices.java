package com.github.styx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.styx.console.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.mvel2.MVEL.evalToString;

@Service
class DefaultUaaServices extends RemoteServices implements UaaServices {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultUaaServices.class);

    private final String baseUaaUri;
    private final String baseApiUri;
    private final String clientId;
    private final String clientSecret;

    @Autowired
    protected DefaultUaaServices(final RestTemplate restTemplate,
                                 final ObjectMapper objectMapper,
                                 final String uaaBaseUri,
                                 final String apiBaseUri,
                                 final String clientId,
                                 final String clientSecret) {
        super(restTemplate, objectMapper);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseUaaUri = sanitizeBaseUri(uaaBaseUri);
        this.baseApiUri = sanitizeBaseUri(apiBaseUri);
    }

    public String getApplicationAccessToken() {
        final MultiValueMap<String, String> body = new LinkedMultiValueMap();
        body.add("grant_type", "client_credentials");
        body.add("response_type", "token");
        final Map<String, Object> tokenResponse = post(getAuthorizationEndpoint().concat("/oauth/token"), getDefaultHeaders(), body);
        return evalToString("token_type", tokenResponse).concat(" ").concat(evalToString("access_token", tokenResponse));
    }

    public String getAccessToken(final String username, final String password) {
        final MultiValueMap<String, String> body = new LinkedMultiValueMap();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);
        final Map<String, Object> tokenResponse = post(getAuthorizationEndpoint().concat("/oauth/token"), getDefaultHeaders(), body);
        return evalToString("token_type", tokenResponse).concat(" ").concat(evalToString("access_token", tokenResponse));
    }

    private HttpHeaders getDefaultHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        httpHeaders.add("Authorization", "Basic ".concat(encodeBase64String(clientId.concat(":").concat(clientSecret).getBytes())));
        return httpHeaders;
    }

    @Override
    public User getUser(String token) {
        final Map<String, Object> userInfoResponse = get(token, baseUaaUri.concat("userinfo"));
        return new User(evalToString("user_id", userInfoResponse), evalToString("user_name", userInfoResponse), null);
    }

    @Override
    public List<User> appendUserNames(final String token, final List<User> users) {
        final List<User> wrappedUsers = new ArrayList<>();
        String path = baseUaaUri.concat("ids/Users?filter=");
        int currentIndex = 0;
        for (final User user : users) {
            path = path.concat("id eq '").concat(user.getId()).concat("'");
            if (currentIndex < users.size() - 1) {
                path = path.concat(" or ");
            }
            currentIndex++;
        }
        final Map<String, Object> userNamesResponse = get(token, path);
        for (Object resource : eval("resources", userNamesResponse, List.class)) {
            final String id = evalToString("id", resource);
            final String userName = evalToString("userName", resource);
            final User searchUser = new User(id, null, null);
            if (users.contains(searchUser)) {
                final User user = users.get(users.indexOf(searchUser));
                wrappedUsers.add(new User(user.getId(), userName, user.getRoles()));
            }
        }
        return unmodifiableList(wrappedUsers);
    }

    private String getAuthorizationEndpoint() {
        Map<String, Object> infoResponse = get(baseApiUri.concat("info"));
        return (String) infoResponse.get("authorization_endpoint");
    }

}
