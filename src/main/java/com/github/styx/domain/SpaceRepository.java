package com.github.styx.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

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
        Map<String, Object> spaceResponse = apiGet(token, "v2/spaces/".concat(id).concat("?inline-relations-depth=1"));
        for (Object app : eval("entity.apps", spaceResponse, List.class)) {
            applicationRepository.deleteById(token, evalToString("metadata.guid", app));
        }
        apiDelete(token, "v2/spaces/".concat(id));
    }

    public Space getById(String token, String id) {
        Map<String, Object> spaceResponse = apiGet(token, "v2/spaces/".concat(id).concat("?inline-relations-depth=2"));
        Space space = Space.fromCloudFoundryModel(spaceResponse);
        return appendUsername(token, Arrays.asList(space)).get(0); // TODO refactor this
    }

    public List<Space> getByOrganizationId(String token, String organizationId) {
        Map<String, Object> spaceResponse = apiGet(token, "v2/organizations/".concat(organizationId).concat("/spaces?inline-relations-depth=3"));

        List<Space> spaces = new ArrayList<>();
        for (Object spaceResource : eval("resources", spaceResponse, List.class)) {
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
    }

    public String updateSpace(String token, String id, String body) {
        return apiPut(token, "v2/spaces/".concat(id).concat("?collection-method=add"), body);
    }

    public String createSpace(String token, String body) {
        return apiPost(token, "v2/spaces", body);
    }
}
