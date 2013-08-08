package com.github.styx.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;
import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.evalToString;

@Repository
public class OrganizationRepository extends BaseRepository {

    private final SpaceRepository spaceRepository;

    @Autowired
    protected OrganizationRepository(final RestTemplate restTemplate, final AsyncTaskExecutor asyncTaskExecutor, @Qualifier("apiBaseUri") final String apiBaseUri, @Qualifier("uaaBaseUri") final String uaaBaseUri, final ObjectMapper mapper, SpaceRepository spaceRepository) {
        super(restTemplate, asyncTaskExecutor, mapper, apiBaseUri, uaaBaseUri);
        this.spaceRepository = spaceRepository;
    }

    public void deleteById(String token, String id) {
        String organizationResponse = apiGet(token, "v2/organizations/".concat(id).concat("?inline-relations-depth=1"));
        try {
            Object response = getMapper().readValue(organizationResponse, new TypeReference<Map<String, Object>>() {});
            for (Object app : eval("entity.spaces", response, List.class)) {
                spaceRepository.deleteById(token, evalToString("metadata.guid", app));
            }
        } catch (IOException e) {
            throw new RepositoryException("Unable to parse JSON from response", e);
        }
        apiDelete(token, "v2/organizations/".concat(id));
    }

    public Organization getById(final String token, final String id, final int depth) {
        String organizationResponse = apiGet(token, "v2/organizations/".concat(id).concat("?inline-relations-depth=").concat(valueOf(depth)));
        try {
            final Organization organization = Organization.fromCloudFoundryModel(getMapper().readValue(organizationResponse, new TypeReference<Map<String, Object>>() {}));
            return appendUsername(token, organization);
        } catch (IOException e) {
            throw new RepositoryException("Unable to map JSON response.", e);
        }
    }

    public List<Organization> getAll(String token, int depth) {
        String organizationResponse = apiGet(token, "v2/organizations?inline-relations-depth=".concat(valueOf(depth)));
        try {
            List<Organization> organizations = new ArrayList<>();

            Object response = getMapper().readValue(organizationResponse, new TypeReference<Map<String, Object>>() {});
            for (Object organizationResource : eval("resources", response, List.class)) {
                Organization organization = Organization.fromCloudFoundryModel(organizationResource);
                organizations.add(appendUsername(token, organization));
            }
            return organizations;
        } catch (IOException e) {
            throw new RepositoryException("Unable to map JSON response.", e);
        }
    }

}
