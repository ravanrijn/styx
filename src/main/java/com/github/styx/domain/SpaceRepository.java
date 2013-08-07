package com.github.styx.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

@Repository
public class SpaceRepository extends BaseRepository {

    private final ApplicationRepository applicationRepository;

    @Autowired
    protected SpaceRepository(RestTemplate restTemplate, AsyncTaskExecutor asyncTaskExecutor, ObjectMapper objectMapper, String apiBaseUri, String uaaBaseUri, ApplicationRepository applicationRepository) {
        super(restTemplate, asyncTaskExecutor, objectMapper, apiBaseUri, uaaBaseUri);
        this.applicationRepository = applicationRepository;
    }

    public void deleteById(String token, String id) {
        ResponseEntity<String> spaceResponseEntity = apiGet(token, "v2/spaces/".concat(id).concat("?inline-relations-depth=1"));
        if (!spaceResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            throw new RepositoryException("Unable to get space", spaceResponseEntity);
        }

        try {
            Object response = getMapper().readValue(spaceResponseEntity.getBody(), new TypeReference<Map<String, Object>>() {});
            for (Object app : eval("entity.apps", response, List.class)) {
                applicationRepository.deleteById(token, evalToString("metadata.guid", app));
            }
        } catch (IOException e) {
            throw new RepositoryException("Unable to parse JSON from response", e);
        }

        ResponseEntity<String> deleteResponseEntity = apiDelete(token, "v2/spaces/".concat(id));
        if (!deleteResponseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new RepositoryException("Unable to delete space", deleteResponseEntity);
        }
    }

    public Space getById(String token, String id) {
        ResponseEntity<String> spaceResponseEntity = apiGet(token, "v2/spaces/".concat(id).concat("?inline-relations-depth=2"));
        if (spaceResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            try {
                Object response = getMapper().readValue(spaceResponseEntity.getBody(), new TypeReference<Map<String, Object>>() {});
                Space space = Space.fromCloudFoundryModel(response);
                return appendUsername(token, Arrays.asList(space)).get(0); // TODO refactor this
            } catch (IOException e) {
                throw new RepositoryException("Unable to parse JSON from response", e);
            }
        }
        throw new RepositoryException("Unable to get space response", spaceResponseEntity);
    }

    public List<Space> getByOrganizationId(String token, String organizationId) {
        ResponseEntity<String> spacesResponseEntity = apiGet(token, "v2/organizations/".concat(organizationId).concat("/spaces?inline-relations-depth=3"));
        if (spacesResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            try {
                List<Space> spaces = new ArrayList<>();

                Object response = getMapper().readValue(spacesResponseEntity.getBody(), new TypeReference<Map<String, Object>>() {});
                for (Object spaceResource : eval("resources", response, List.class)) {
                    Space space = Space.fromCloudFoundryModel(spaceResource);
                    for (Object application : eval("entity.apps", spaceResource, List.class)) {
                        space.addApplication(Application.fromCloudFoundryModel(application));
                    }
                    for (Object serviceInstance : eval("entity.service_instances", spaceResource, List.class)) {
                        space.addServiceInstance(ServiceInstance.fromCloudFoundryModel(serviceInstance));
                    }
                    spaces.add(space);
                }
                return appendUsername(token, spaces);
            } catch (IOException e) {
                throw new RepositoryException("Unable to parse JSON from response", e);
            }
        }
        throw new RepositoryException("Unable to get spaces response", spacesResponseEntity);
    }

}
