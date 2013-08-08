package com.github.styx.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

public abstract class BaseRepository {

    private final RestTemplate restTemplate;
    private final AsyncTaskExecutor asyncTaskExecutor;
    private final ObjectMapper objectMapper;
    private final String apiBaseUri;
    private final String uaaBaseUri;

    protected BaseRepository(RestTemplate restTemplate, AsyncTaskExecutor asyncTaskExecutor, ObjectMapper objectMapper, String apiBaseUri, String uaaBaseUri) {
        this.restTemplate = restTemplate;
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.objectMapper = objectMapper;
        this.apiBaseUri = concatSlashIfNeeded(apiBaseUri);
        this.uaaBaseUri = concatSlashIfNeeded(uaaBaseUri);
    }

    protected String concatSlashIfNeeded(String uri) {
        if (!uri.endsWith("/")) {
            return uri.concat("/");
        }
        return uri;
    }

    protected ObjectMapper getMapper() {
        return objectMapper;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    protected Organization appendUsername(String token, Organization organization) throws IOException {
        Set<String> userIds = getUserIds(organization);
        Map<String, String> userNames = getUserNames(token, userIds);
        for (Map.Entry<String, String> userName : userNames.entrySet()) {
            for (OrganizationUser orgUser : organization.getUsers()) {
                if (orgUser.getId().equals(userName.getKey())) {
                    orgUser.setUsername(userName.getValue());
                }
            }
            for (Space space : organization.getSpaces()) {
                for (SpaceUser spaceUser : space.getUsers()) {
                    if (spaceUser.getId().equals(userName.getKey())) {
                        spaceUser.setUsername(userName.getValue());
                    }
                }
            }
        }
        return organization;
    }

    protected List<Space> appendUsername(String token, List<Space> spaces) throws IOException {
        Set<String> userIds = getUserIds(spaces);
        Map<String, String> userNames = getUserNames(token, userIds);
        for (Map.Entry<String, String> userName : userNames.entrySet()) {
            for (Space space : spaces) {
                for (SpaceUser spaceUser : space.getUsers()) {
                    if (spaceUser.getId().equals(userName.getKey())) {
                        spaceUser.setUsername(userName.getValue());
                    }
                }
            }
        }
        return spaces;
    }

    protected String uaaGet(String token, String path) {
        ResponseEntity<String> responseEntity = exchange(token, uaaBaseUri, HttpMethod.GET, path);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            throw new RepositoryException("Cannot perform uaa get for path [" + path + "]", responseEntity);
        }
        return responseEntity.getBody();
    }

    protected String apiGet(String token, String path) {
        ResponseEntity<String> responseEntity = exchange(token, apiBaseUri, HttpMethod.GET, path);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            throw new RepositoryException("Cannot perform api get for path [" + path + "]", responseEntity);
        }
        return responseEntity.getBody();
    }

    protected String apiDelete(String token, String path) {
        ResponseEntity<String> responseEntity = exchange(token, apiBaseUri, HttpMethod.DELETE, path);
        if (!responseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new RepositoryException("Cannot perform api delete for path [" + path + "]", responseEntity);
        }
        return responseEntity.getBody();
    }

    protected Future<ResponseEntity<String>> asyncApiGet(final String token, final String path) {
        return asyncTaskExecutor.submit(new Callable<ResponseEntity<String>>() {
            @Override
            public ResponseEntity<String> call() throws Exception {
                return exchange(token, apiBaseUri, HttpMethod.GET, path);
            }
        });
    }

    private ResponseEntity<String> exchange(String token, String baseUri, HttpMethod method, String path) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");
        httpHeaders.add("Authorization", token);
        try {
            return restTemplate.exchange(baseUri.concat(path), method, new HttpEntity(httpHeaders), String.class);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity(e.getResponseBodyAsString(), e.getResponseHeaders(), e.getStatusCode());
        }
    }

    private Map<String, String> getUserNames(String token, Set<String> userIds) throws IOException {
        String userDetailsResponse = uaaGet(token, getUserDetailsPath(userIds));

        Map<String, String> userNames = new HashMap<>();
        for (Object resource : eval("resources", objectMapper.readValue(userDetailsResponse, new TypeReference<Map<String, Object>>() {}), List.class)) {
            userNames.put(evalToString("id", resource), evalToString("userName", resource));
        }
        return userNames;
    }

    private Set<String> getUserIds(Organization organization) {
        final Set<String> userIds = new HashSet<>();
        for (OrganizationUser orgUser : organization.getUsers()) {
            if (!userIds.contains(orgUser.getId())) {
                userIds.add(orgUser.getId());
            }
        }
        userIds.addAll(getUserIds(organization.getSpaces()));
        return userIds;
    }

    private Set<String> getUserIds(List<Space> spaces) {
        final Set<String> userIds = new HashSet<>();
        for (Space space : spaces) {
            for (SpaceUser spaceUser : space.getUsers()) {
                if (!userIds.contains(spaceUser.getId())) {
                    userIds.add(spaceUser.getId());
                }
            }
        }
        return userIds;
    }

    private String getUserDetailsPath(Set<String> userIds) {
        String path = "ids/Users?filter=";

        int i = 0;
        for (String userId : userIds) {
            path = path.concat("id eq '").concat(userId).concat("'");
            if (i < userIds.size() - 1) {
                path = path.concat(" or ");
            }
            i++;
        }
        return path;
    }

}
